package net.nazarick.artillerytablet.client.terrain.mapengine;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.client.terrain.BlockPalette;
import net.nazarick.artillerytablet.client.terrain.TerrainMips;
import net.nazarick.artillerytablet.client.terrain.WorldGeneration;
import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;
import net.nazarick.mapengine.core.RegionKey;
import net.nazarick.mapengine.overview.WorldOverview;
import net.nazarick.mapengine.raster.Rasterizer;
import net.nazarick.mapengine.storage.RegionStore;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Draws the map from {@code mapengine} — the replacement for the tile-and-sheet renderer this mod
 * used to have ({@code TerrainImage}, {@code TerrainClientCache}, {@code TerrainDisk} and the whole
 * server-side tile-survey path, all retired the same session this landed). One GPU texture per
 * visible {@link Region} at whatever zoom level {@link Region#levelFor} picks, rather than many small
 * tile sheets — the engine's own region-file storage already made "one file per 64 blocks" the thing
 * to stop doing on disk, and the same reasoning carries over to how many textures a frame uploads.
 *
 * <p><b>What this does not yet do</b>, honestly, not silently: no coarse underlay while a level is
 * still baking (the old renderer showed a blurred stand-in level while the sharp one built; this
 * shows nothing there yet, same as genuinely unsurveyed ground, until {@link RegionStore#ensureLevel}
 * finishes), no memory trimming via {@link RegionStore#trimToLevel} (a real, deliberate follow-up —
 * see that method's own doc for the tradeoff), and no colour prewarming (see
 * {@link ForgeBlockStyle}'s own doc — colours are the coarser {@code MapColor} fallback until a
 * separate pass wires that up). None of these are correctness gaps; all three are speed/polish this
 * first working version deferred to get the engine on screen sooner.
 */
@OnlyIn(Dist.CLIENT)
public final class MapEngineOverlay {
    private static MapEngineOverlay instance;

    public static MapEngineOverlay shared() {
        if (instance == null) {
            instance = new MapEngineOverlay();
        }
        return instance;
    }

    private final ForgeColumnSource source = new ForgeColumnSource();
    private final ForgeBlockStyle style = new ForgeBlockStyle();

    private RegionStore store;
    private Path overviewFile;
    private int storeGeneration = -1;

    /** Bumped on a filter change, so every live texture is known stale without walking them all. */
    private int paint;

    private static final class RegionTexture {
        DynamicTexture texture;
        ResourceLocation id;
        int builtLevel = -1;
        int builtPaint = -1;
        // Identity, not equality — a resurvey (see RegionStore.resurvey) always produces a brand new
        // Region object for the same (rx,rz), even though builtLevel/builtPaint are unchanged, and
        // that new object is the only signal available that there is fresher data to rebuild from.
        // Comparing on level+paint alone left a texture stuck at whatever it first baked until the
        // zoom level happened to change too — the exact "only refreshes when you scroll zoom" bug
        // reported this session.
        Region builtFromRegion;
        int lastSeenFrame;

        void close() {
            if (texture != null) {
                texture.close();
                texture = null;
            }
        }
    }

    private final Map<Long, RegionTexture> textures = new HashMap<>();

    /** Roughly what a widest-practical view needs live at once; well past that is ground left behind. */
    private static final int MAX_LIVE_TEXTURES = 128;

    private int frame;

    private MapEngineOverlay() {
    }

    /** Throws away every baked texture, for a filter change — the caller repaints, not this class. */
    public void repaint() {
        paint++;
    }

    private void ensureStore() {
        int generation = WorldGeneration.generation();
        if (store != null && generation == storeGeneration) {
            return;
        }
        if (store != null) {
            store.shutdown();
        }
        for (RegionTexture rt : textures.values()) {
            rt.close();
        }
        textures.clear();
        storeGeneration = generation;

        Path root = worldRoot();
        overviewFile = root == null ? null : root.resolve("overview.dat");
        WorldOverview overview = overviewFile == null ? new WorldOverview() : WorldOverview.read(overviewFile);
        // ioThreads: modest and fixed, not sized off the CPU the way the benchmark's own tests were —
        // this pool also carries every fresh survey's disk write and shard-buildup work, alongside
        // region loads, so leaving room for the game's own threads matters more here than in a
        // benchmark that has the machine to itself.
        store = new RegionStore(root == null ? tempRoot() : root, source, 256, 4, overview);
        prewarmAll();
    }

    private static void prewarmAll() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) {
            return;
        }
        // Prewarm all block models and texture averages on the render thread
        int blockCount = net.minecraft.core.registries.BuiltInRegistries.BLOCK.size();
        for (int i = 0; i < blockCount; i++) {
            BlockPalette.prewarm(i);
        }
        net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomes =
                mc.level.registryAccess().registry(net.minecraft.core.registries.Registries.BIOME).orElse(null);
        if (biomes != null) {
            for (net.minecraft.world.level.biome.Biome b : biomes) {
                int id = biomes.getId(b);
                if (id >= 0) {
                    TerrainMips.prewarmBiome((short) id);
                }
            }
        }
    }

    /**
     * Draws the map into the block-space rectangle the caller establishes — same contract the old
     * {@code TerrainImage.draw} had: {@code x}/{@code y}/{@code width}/{@code height} are the screen
     * rectangle, {@code minBlockX}/{@code minBlockZ}/{@code spanX}/{@code spanZ} the world ground it
     * covers. Returns whether anything was actually drawn, so the caller can decide whether the grid
     * on top of it should read as "over terrain" or "over empty backdrop".
     */
    public boolean draw(GuiGraphics g, int x, int y, int width, int height,
                         int minBlockX, int minBlockZ, int spanX, int spanZ) {
        WorldGeneration.checkWorld();
        ensureStore();
        store.drain();
        flushOverviewNowAndThen();
        frame++;

        double blocksPerPixel = spanX / (double) Math.max(1, width);
        int level = Region.levelFor(blocksPerPixel);

        int firstRx = Math.floorDiv(minBlockX, Region.BLOCKS);
        int lastRx = Math.floorDiv(minBlockX + spanX, Region.BLOCKS);
        int firstRz = Math.floorDiv(minBlockZ, Region.BLOCKS);
        int lastRz = Math.floorDiv(minBlockZ + spanZ, Region.BLOCKS);

        g.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        g.pose().pushPose();
        g.pose().translate((double) x, (double) y, 0.0);
        g.pose().scale((float) width / spanX, (float) height / spanZ, 1f);

        boolean any = false;
        for (int rx = firstRx; rx <= lastRx; rx++) {
            for (int rz = firstRz; rz <= lastRz; rz++) {
                long key = RegionKey.of(rx, rz);
                Region region = store.get(rx, rz);
                if (region != null) {
                    maybeResurvey(rx, rz, region);
                    if (region.hasLevel(level)) {
                        textureFor(key, rx, rz, region, level);
                    } else {
                        store.ensureLevel(rx, rz, level);
                    }
                }

                // Drawn from whatever texture is cached for this key, not from `region` directly — a
                // region mid-resurvey (see maybeResurvey) goes back to null in the store for a frame
                // or several, and skipping the draw whenever that happens would flicker the ground
                // blank every time client-loaded chunks catch up to it, which is routinely, not rarely.
                RegionTexture rt = textures.get(key);
                if (rt == null) {
                    continue;
                }
                rt.lastSeenFrame = frame;

                int blockX = rx * Region.BLOCKS - minBlockX;
                int blockZ = rz * Region.BLOCKS - minBlockZ;
                int texSide = Region.widthOf(rt.builtLevel);
                g.blit(rt.id, blockX, blockZ, Region.BLOCKS, Region.BLOCKS,
                        0, 0, texSide, texSide, texSide, texSide);
                any = true;
            }
        }

        g.pose().popPose();

        evictOffscreen(firstRx, lastRx, firstRz, lastRz);
        return any;
    }

    private void textureFor(long key, int rx, int rz, Region region, int level) {
        RegionTexture rt = textures.get(key);
        if (rt != null && rt.builtLevel == level && rt.builtPaint == paint && rt.builtFromRegion == region) {
            return;
        }

        ColumnBuffer columns = region.level(level);
        int[] pixels = rasterize(columns, level);
        if (pixels == null) {
            return; // nothing new to show; keep whatever was already baked, if anything
        }
        logIfSuspiciouslyUniform(rx, rz, level, columns, pixels);

        int side = Region.widthOf(level);
        if (pixels.length != side * side) {
            // A real, reported mismatch beats a bad upload or a subtly wrong picture — this should be
            // impossible (every Rasterizer method sizes its output to columns.columns(), and a
            // Region's own setLevel enforces columns.width == Region.widthOf(level)), so if it ever
            // fires the assumption connecting the two broke somewhere and needs finding, not papering
            // over silently.
            ArtilleryTablet.LOGGER.warn("mapengine: region ({},{}) level {} produced {} pixels, "
                    + "expected {} ({}x{}) — texture left unchanged", rx, rz, level, pixels.length,
                    side * side, side, side);
            return;
        }

        if (rt == null) {
            rt = new RegionTexture();
            textures.put(key, rt);
        }
        try {
            if (rt.texture == null) {
                rt.texture = new DynamicTexture(side, side, true);
                rt.id = Minecraft.getInstance().getTextureManager().register(
                        "artillerytablet_mapengine_" + rx + "_" + rz, rt.texture);
            } else if (rt.texture.getPixels() == null || rt.texture.getPixels().getWidth() != side) {
                // The level changed to a different texel width — a fresh texture rather than trying
                // to resize the native image in place.
                rt.close();
                rt.texture = new DynamicTexture(side, side, true);
                rt.id = Minecraft.getInstance().getTextureManager().register(
                        "artillerytablet_mapengine_" + rx + "_" + rz, rt.texture);
            }

            NativeImage image = rt.texture.getPixels();
            if (image == null) {
                return;
            }
            // mapengine packs ARGB (0xAARRGGBB); NativeImage.setPixelRGBA wants this mod's own
            // native order (0xAABBGGRR) — see ForgeBlockStyle's own doc for why that swap exists.
            for (int i = 0; i < pixels.length; i++) {
                int z = i / side;
                int xTex = i % side;
                image.setPixelRGBA(xTex, z, ForgeBlockStyle.swapRedBlue(pixels[i]));
            }
            rt.texture.bind();
            image.upload(0, 0, 0, 0, 0, side, side, false, false);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

            rt.builtLevel = level;
            rt.builtPaint = paint;
            rt.builtFromRegion = region;
        } catch (Throwable t) {
            // A texture half-written when something throws partway through is worse than one left
            // exactly as it was: it shows whatever the GPU happened to have in that memory, and
            // nothing about builtLevel/builtPaint/builtFromRegion says so, so a stale-but-consistent
            // texture would otherwise look "done" and never get retried. Discarding it here forces a
            // clean rebuild attempt next frame instead of leaving a wrong picture stuck on screen.
            ArtilleryTablet.LOGGER.warn("mapengine: texture build failed for region ({},{}) level {}, "
                    + "discarding and will retry", rx, rz, level, t);
            rt.close();
            rt.builtLevel = -1;
        }
    }

    // Temporary diagnostic — see the "solid magenta/flat-colour square" report this session. Remove
    // once actually root-caused.
    private static int uniformLogCount;

    private static void logIfSuspiciouslyUniform(int rx, int rz, int level, ColumnBuffer columns, int[] pixels) {
        if (uniformLogCount >= 5 || pixels.length < 4) {
            return;
        }
        int first = pixels[0];
        int sameCount = 0;
        for (int p : pixels) {
            if (p == first) {
                sameCount++;
            }
        }
        // Legitimately uniform is normal for a region that is entirely unsurveyed (all transparent,
        // first == 0) — only flag a *coloured* uniform block, which real terrain essentially never is.
        if (first == 0 || sameCount < pixels.length * 9 / 10) {
            return;
        }
        uniformLogCount++;
        StringBuilder sample = new StringBuilder();
        int n = Math.min(8, columns.columns());
        for (int i = 0; i < n; i++) {
            sample.append(String.format("[h=%d b=%d bi=%d d=%d] ",
                    columns.height[i], columns.block[i], columns.biome[i], columns.depthAt(i)));
        }
        ArtilleryTablet.LOGGER.warn("mapengine: region ({},{}) level {} rasterized {}% uniform "
                        + "(colour 0x{}, {} of {} pixels) — columns.isEmpty={}, first columns: {}",
                rx, rz, level, sameCount * 100 / pixels.length, Integer.toHexString(first),
                sameCount, pixels.length, columns.isEmpty(), sample);
    }

    private final Map<Long, Long> lastResurveyAttempt = new HashMap<>();
    private static final long RESURVEY_INTERVAL_MS = 2000L;

    /**
     * Asks the source for this region again if it might have more to say than it did last time —
     * see {@link RegionStore#resurvey}'s own doc for why a client-chunk source needs this at all.
     * Throttled per region so a wide view sitting on incomplete ground does not resurvey all of it
     * every single frame.
     */
    private void maybeResurvey(int rx, int rz, Region region) {
        ColumnBuffer level0 = region.level(0);
        if (level0 != null && level0.isComplete()) {
            return; // nothing missing to catch up on
        }
        long key = RegionKey.of(rx, rz);
        long now = System.currentTimeMillis();
        Long last = lastResurveyAttempt.get(key);
        if (last != null && now - last < RESURVEY_INTERVAL_MS) {
            return;
        }
        lastResurveyAttempt.put(key, now);
        store.resurvey(rx, rz);
    }

    /** Null when the level simply is not built yet — the caller keeps whatever texture already exists. */
    private int[] rasterize(ColumnBuffer columns, int level) {
        if (columns == null) {
            return null;
        }
        return switch (TerrainMips.filter()) {
            case RELIEF -> Rasterizer.rasterizeHypsometric(columns);
            case THERMAL -> Rasterizer.rasterizeThermal(columns, style, level);
            case NONE -> Rasterizer.rasterize(columns, style, level);
        };
    }

    private void evictOffscreen(int firstRx, int lastRx, int firstRz, int lastRz) {
        if (textures.size() <= MAX_LIVE_TEXTURES) {
            return;
        }
        Iterator<Map.Entry<Long, RegionTexture>> it = textures.entrySet().iterator();
        while (it.hasNext() && textures.size() > MAX_LIVE_TEXTURES) {
            Map.Entry<Long, RegionTexture> e = it.next();
            int rx = RegionKey.x(e.getKey());
            int rz = RegionKey.z(e.getKey());
            boolean onScreen = rx >= firstRx && rx <= lastRx && rz >= firstRz && rz <= lastRz;
            if (onScreen || e.getValue().lastSeenFrame == frame) {
                continue;
            }
            e.getValue().close();
            it.remove();
        }
    }

    private long lastOverviewFlush;
    private static final long OVERVIEW_FLUSH_INTERVAL_MS = 30_000L;

    /** Periodically persists the overview and any partial shards — see both classes' own doc. */
    private void flushOverviewNowAndThen() {
        long now = System.currentTimeMillis();
        if (now - lastOverviewFlush < OVERVIEW_FLUSH_INTERVAL_MS) {
            return;
        }
        lastOverviewFlush = now;
        store.flushShards();
        if (overviewFile != null) {
            try {
                store.overview().write(overviewFile);
            } catch (IOException ignored) {
                // Costs the next session a slower open, not correctness now.
            }
        }
    }

    private static Path worldRoot() {
        try {
            Minecraft mc = Minecraft.getInstance();
            String name;
            if (mc.getSingleplayerServer() != null) {
                name = mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT)
                        .getParent().getFileName().toString();
            } else {
                ServerData server = mc.getCurrentServer();
                name = server == null ? "unknown" : server.ip;
            }
            ResourceLocation dimension = mc.level == null ? null : mc.level.dimension().location();
            if (dimension == null) {
                return null;
            }
            Path root = mc.gameDirectory.toPath()
                    .resolve(ArtilleryTablet.MODID)
                    .resolve("mapengine")
                    .resolve(safeName(name))
                    .resolve(safeName(dimension.getNamespace() + "_" + dimension.getPath()));
            Files.createDirectories(root);
            return root;
        } catch (Throwable t) {
            ArtilleryTablet.LOGGER.warn("Could not open the map engine's region store; "
                    + "the map will work but will not remember ground between sessions", t);
            return null;
        }
    }

    private static Path tempRoot;

    /** Falls back to a session-only temp directory when the real save location cannot be opened. */
    private static Path tempRoot() {
        if (tempRoot == null) {
            try {
                tempRoot = Files.createTempDirectory("artillerytablet-mapengine");
            } catch (IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        }
        return tempRoot;
    }

    /** Anything a filesystem might object to becomes an underscore. Ported from the old TileFiles. */
    private static String safeName(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            out.append(Character.isLetterOrDigit(c) || c == '-' || c == '.' ? c : '_');
        }
        String cleaned = out.toString();
        return cleaned.equals(".") || cleaned.equals("..") || cleaned.isEmpty() ? "_" : cleaned;
    }
}
