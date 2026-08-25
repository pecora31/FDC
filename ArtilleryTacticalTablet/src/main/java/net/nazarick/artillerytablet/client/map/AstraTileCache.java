package net.nazarick.artillerytablet.client.map;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Dual-Tier Map Tile Cache Engine (GPU VRAM + Persistent Fast Disk Storage).
 * - Manages 512x512 block Region Tiles (32x32 Chunks).
 * - Tier 1: OpenGL DynamicTexture in VRAM for 0ms instant display.
 * - Tier 2: Binary file cache (.bin) for instant loading across game launches.
 * - Multi-threaded async worker threads for zero-lag background baking.
 */
@OnlyIn(Dist.CLIENT)
public final class AstraTileCache {

    public static final int TILE_SIZE = 512; // 512x512 pixels per region (32x32 chunks)
    public static final int MAX_VRAM_TILES = 16; // 16 live textures in VRAM (~16MB VRAM)

    private static final AstraTileCache INSTANCE = new AstraTileCache();
    public static AstraTileCache instance() { return INSTANCE; }

    // LRU Cache for live OpenGL textures in VRAM
    private final Map<Long, TileTextureEntry> vramCache = new LinkedHashMap<>(MAX_VRAM_TILES, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, TileTextureEntry> eldest) {
            if (size() > MAX_VRAM_TILES) {
                eldest.getValue().dispose();
                return true;
            }
            return false;
        }
    };

    // Background Thread Pool for non-blocking tile baking
    private final ExecutorService asyncWorker = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "AstraMapWorker");
        t.setDaemon(true);
        return t;
    });

    // Queue of textures pending OpenGL upload on render thread
    private final ConcurrentLinkedQueue<PendingUpload> uploadQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<Long, Boolean> queuedBakes = new ConcurrentHashMap<>();

    private Path cacheDirectory;
    private String currentWorldId = "";

    private AstraTileCache() {}

    /**
     * Initializes cache directory for the current active world/dimension.
     */
    public synchronized void checkWorld(Level level) {
        if (level == null) return;
        String worldKey = level.dimension().location().toString().replace(':', '_').replace('/', '_');
        if (!worldKey.equals(currentWorldId)) {
            clearAll();
            currentWorldId = worldKey;
            cacheDirectory = Minecraft.getInstance().gameDirectory.toPath()
                    .resolve("astra_map_cache").resolve(worldKey);
            try {
                Files.createDirectories(cacheDirectory);
            } catch (IOException ignored) {}
        }
    }

    /**
     * Retrieves the OpenGL ResourceLocation for a given Region Tile coordinate (rx, rz).
     * If not loaded, returns null and triggers instant async load/bake.
     */
    public synchronized ResourceLocation getTileTexture(int rx, int rz, Level level) {
        long key = packKey(rx, rz);
        processUploads();

        TileTextureEntry entry = vramCache.get(key);
        if (entry != null && entry.textureLocation != null) {
            return entry.textureLocation;
        }

        // Trigger background load/bake if not already requested
        if (queuedBakes.putIfAbsent(key, Boolean.TRUE) == null) {
            asyncWorker.execute(() -> loadOrBakeRegion(rx, rz, key, level));
        }

        return null;
    }

    private void loadOrBakeRegion(int rx, int rz, long key, Level level) {
        try {
            // 1. Try loading from Fast Disk Cache (Tier 2)
            Path diskFile = (cacheDirectory != null) ? cacheDirectory.resolve("r." + rx + "." + rz + ".bin") : null;
            if (diskFile != null && Files.exists(diskFile)) {
                NativeImage img = readDiskCache(diskFile);
                if (img != null) {
                    uploadQueue.add(new PendingUpload(key, img));
                    return;
                }
            }

            // 2. Not on disk: Bake from loaded world chunks (Tier 3)
            NativeImage bakedImg = bakeRegionFromWorld(rx, rz, level);
            if (bakedImg != null) {
                if (diskFile != null) {
                    writeDiskCache(diskFile, bakedImg);
                }
                uploadQueue.add(new PendingUpload(key, bakedImg));
            }
        } finally {
            queuedBakes.remove(key);
        }
    }

    private NativeImage bakeRegionFromWorld(int rx, int rz, Level level) {
        if (level == null) return null;
        NativeImage img = new NativeImage(TILE_SIZE, TILE_SIZE, false);
        img.fillRect(0, 0, TILE_SIZE, TILE_SIZE, 0xFF080A0E); // Default dark background

        int startBlockX = rx * TILE_SIZE;
        int startBlockZ = rz * TILE_SIZE;

        // Iterate through all 32x32 chunks in this 512x512 region
        for (int cz = 0; cz < 32; cz++) {
            for (int cx = 0; cx < 32; cx++) {
                int chunkX = (rx * 32) + cx;
                int chunkZ = (rz * 32) + cz;

                ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) continue;

                int basePixelX = cx * 16;
                int basePixelZ = cz * 16;

                for (int lz = 0; lz < 16; lz++) {
                    for (int lx = 0; lx < 16; lx++) {
                        int worldX = startBlockX + basePixelX + lx;
                        int worldZ = startBlockZ + basePixelZ + lz;

                        int surfaceY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz);
                        if (surfaceY <= level.getMinBuildHeight()) {
                            surfaceY = level.getMinBuildHeight() + 1;
                        }

                        // Sample neighbor elevation for hillshading
                        int westY = surfaceY;
                        int northY = surfaceY;
                        int nwY = surfaceY;
                        if (lx > 0) westY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx - 1, lz);
                        if (lz > 0) northY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx, lz - 1);
                        if (lx > 0 && lz > 0) nwY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, lx - 1, lz - 1);

                        int col = AstraColorSampler.sampleColumn(
                                level, worldX, surfaceY, worldZ,
                                westY, northY, nwY,
                                chunk.getNoiseBiome(lx >> 2, surfaceY >> 2, lz >> 2).value(),
                                true, true
                        );

                        img.setPixelRGBA(basePixelX + lx, basePixelZ + lz, toAbgr(col));
                    }
                }
            }
        }

        return img;
    }

    private NativeImage readDiskCache(Path path) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            int magic = in.readInt();
            if (magic != 0x41535452) return null; // 'ASTR' magic header

            NativeImage img = new NativeImage(TILE_SIZE, TILE_SIZE, false);
            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    img.setPixelRGBA(x, y, in.readInt());
                }
            }
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private void writeDiskCache(Path path, NativeImage img) {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            out.writeInt(0x41535452); // 'ASTR'
            for (int y = 0; y < TILE_SIZE; y++) {
                for (int x = 0; x < TILE_SIZE; x++) {
                    out.writeInt(img.getPixelRGBA(x, y));
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Flushes newly baked textures onto OpenGL VRAM (must be called on client render thread).
     */
    public synchronized void processUploads() {
        PendingUpload upload;
        while ((upload = uploadQueue.poll()) != null) {
            DynamicTexture texture = new DynamicTexture(upload.image);
            ResourceLocation loc = Minecraft.getInstance().getTextureManager()
                    .register("astra_tile_" + upload.key, texture);

            // Replace old entry if present
            TileTextureEntry old = vramCache.put(upload.key, new TileTextureEntry(texture, loc));
            if (old != null) old.dispose();
        }
    }

    public synchronized void clearAll() {
        for (TileTextureEntry entry : vramCache.values()) {
            entry.dispose();
        }
        vramCache.clear();
        queuedBakes.clear();
        PendingUpload p;
        while ((p = uploadQueue.poll()) != null) {
            p.image.close();
        }
    }

    private static long packKey(int rx, int rz) {
        return (((long) rx) << 32) | (rz & 0xFFFFFFFFL);
    }

    private static int toAbgr(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    private record PendingUpload(long key, NativeImage image) {}

    private static final class TileTextureEntry {
        final DynamicTexture texture;
        final ResourceLocation textureLocation;

        TileTextureEntry(DynamicTexture texture, ResourceLocation loc) {
            this.texture = texture;
            this.textureLocation = loc;
        }

        void dispose() {
            if (texture != null) {
                texture.close();
                if (textureLocation != null && Minecraft.getInstance() != null) {
                    Minecraft.getInstance().getTextureManager().release(textureLocation);
                }
            }
        }
    }
}
