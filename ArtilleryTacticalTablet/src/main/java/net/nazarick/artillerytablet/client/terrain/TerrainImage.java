package net.nazarick.artillerytablet.client.terrain;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The surveyed ground, as one texture per tile, drawn as quads.
 *
 * <p>This was a single large texture rebuilt from the cached tile data whenever the view moved, and
 * that is what made dragging expensive: a pan of one block meant sampling a quarter of a million
 * pixels again and re-uploading them, on every frame the mouse was held down. The sampling was made
 * cheaper twice over and it was still the same shape of work.
 *
 * <p>A tile's pixels only change when that tile's <em>data</em> changes, which is rare. So they are
 * built once, uploaded once, and then left alone; panning and zooming cost nothing but working out
 * where each quad goes. That is how the map mods this was measured against stay smooth — they are
 * not drawing a map every frame, they are moving pictures of one.
 *
 * <p>The trade is memory, so the number of live textures is capped and the ones least recently drawn
 * are released first.
 *
 * <p>One texture per 64-block tile only works while there are few of them. The count grows with the
 * square of the view, so a sixteen-kilometre span would want sixty-five thousand — far past the cap,
 * which turned every frame into building a thousand textures and throwing a thousand away. So the
 * tiles are stacked into levels, each covering eight times the ground of the one below at an eighth
 * of the detail, and the view picks the level that keeps the number of quads roughly constant. This
 * is what a "levelled region" is in the map mods this was measured against.
 */
@OnlyIn(Dist.CLIENT)
public final class TerrainImage {
    /**
     * The one instance the tablet draws from, kept for as long as the world lasts.
     *
     * <p>It used to belong to the map panel, which belongs to the screen, which is built anew every
     * time the tablet is opened — so closing the tablet destroyed several hundred sheets and opening
     * it began again from an empty cache. That is what made every open start black and fill in a
     * piece at a time, and it was not a slow build: the work had simply been thrown away seconds
     * earlier for no reason. Ground does not change while a screen is shut.
     *
     * <p>Freed on a change of world, which {@link #draw} notices through the cache's generation
     * counter — the only moment the sheets stop meaning anything.
     */
    private static final TerrainImage SHARED = new TerrainImage();

    public static TerrainImage shared() {
        return SHARED;
    }

    /**
     * Shortest gap between two sweeps for missing tiles.
     *
     * <p>Halved along with the batch doubling, and bounded by the window the cache keeps rather than
     * by this — so the pair sets how quickly the queue refills, and the window sets how deep it is
     * allowed to get.
     */
    private static final long REQUEST_INTERVAL_MS = 250L;

    /**
     * How many tile textures may be alive at once.
     *
     * <p>Each is 258×258 in four channels — 260 kilobytes on the card, and the same again in the
     * image kept beside it — so this is about fifty megabytes each side.
     *
     * <p>The number that matters is what one view wants, and the level is chosen to keep that
     * bounded: nine squares across at any zoom, so 81 at the worst. The cap was once 384 against a
     * working set of 289, which read as headroom and was not any — a single screen's pan evicted
     * ground about to be wanted again. Rather more than twice the working set is the point of a
     * cache; anything less is a queue.
     *
     * <p>Room for three sets of squares, not one: the level on screen, the coarse pass drawn beneath
     * it, and the finer level warmed for the next zoom step in. Sized to hold all three rather than
     * to evict one of them on every frame, which is the failure mode a cap has when it is set to the
     * working set exactly.
     */
    private static final int MAX_LIVE_TEXTURES = 256;

    /**
     * Tiles along one edge of a sheet at the finest level.
     *
     * <p>Was one, so a sheet and a tile were the same square. That made the bookkeeping trivial
     * and cost the map its detail at three of the eight zoom steps: the level is picked to keep
     * the number of quads across bounded, so a small sheet forces a coarse level long before the
     * panel has run out of pixels. At two thousand blocks the map drew five hundred texels into a
     * panel that could show sixteen hundred, because the only alternative was eleven hundred quads.
     *
     * <p>Doubling the sheet doubles the ground each quad covers, so the same zoom now reaches a
     * level four times finer for exactly the same number of draw calls.
     *
     * <p>Four now, not two, and the reason is the other end of the same trade. With the quad budget
     * dropped to match, every zoom keeps the level and the stride it had, and the view falls from
     * 289 squares to 81 — measured at 7.7 microseconds per blit, since each is its own draw call,
     * that is the map's whole standing cost cut to a third. A square costs four times as much to
     * draw from scratch, which is why this had to wait until a tile landing repaints a rectangle
     * rather than redrawing the square: what a patch costs is set by the size of a tile, and a tile
     * is the same size whatever a sheet holds.
     */
    private static final int TILES_PER_SHEET = 4;

    /** Every level's texture is this square; what changes between them is how much ground it covers. */
    private static final int SHEET_PIXELS = TerrainTile.SIDE * TILES_PER_SHEET;

    /**
     * Texels of the neighbouring ground carried around each sheet, for the filter to read across.
     *
     * <p>Everything that depends on how far outside a sheet the drawing reaches is derived from this
     * and from {@link #RELIEF_RUN}, in one place. They were separate numbers once, and the moment
     * this border was introduced the gathered margin no longer covered the widest sample the
     * shading takes from it — the border ring is shaded too, and it reaches a further
     * {@code RELIEF_RUN} beyond itself. That read off the front of the array and crashed the screen
     * the first time the map was opened.
     */
    private static final int BORDER = 1;

    /** The texture itself, wider all round than the ground it shows by exactly that border. */
    private static final int TEXTURE_PIXELS = SHEET_PIXELS + 2 * BORDER;

    /**
     * Ground per texture goes up by this much per level, and detail goes down by the same.
     *
     * <p>Halving, not quartering. The level is picked as the finest one whose quads still fit the
     * budget, so the coarser the steps between levels the further past "just enough" the choice
     * lands. At eight it was brutal; at four it still left two of the eight zooms stranded a whole
     * step too coarse, because the only choices there were one texel per block (far too many quads)
     * and one per four (visibly soft). Two puts a level within reach of every zoom.
     *
     * <p>The symptom that found this is worth recording, because it is a precise fingerprint: 4000 m
     * and 16000 m read as blurred while every other step was sharp, and those are exactly the two
     * where the stride came out coarser than the number of blocks a display pixel covers. Data
     * coarser than the screen looks soft however it is filtered; data finer than the screen looks
     * sharp. Nothing else in the eight steps distinguishes those two.
     */
    private static final int LEVEL_FACTOR = 2;

    /**
     * How many levels coarser the pass drawn underneath is.
     *
     * <p>Two, which is sixteen times the ground per sheet — so a view that needs close to three
     * hundred fine sheets needs about twenty coarse ones, and twenty fit inside a frame's build
     * budget. One step would barely help; three would be so coarse the stand-in read as a smear
     * rather than as the map.
     */
    private static final int UNDERLAY_STEPS = 2;

    /**
     * The coarsest level there is data for.
     *
     * <p>Taken from the reduction chain rather than written down, because it <em>is</em> the chain's
     * limit: past its last level there is nothing to sample and a sheet would be built from a stride
     * finer than it asked for, which looks like ordinary ground and is not.
     */
    private static final int MAX_LEVEL = TerrainMips.LEVELS;

    /** As many quads as the widest view may draw across. Sets where each level takes over. */
    private static final int MAX_QUADS_ACROSS = 8;

    // Primitive-keyed for the same reason the tile cache is: this is looked up once per quad per
    // frame, and a boxed key turns a hash lookup into a hash lookup plus an allocation.
    private final Long2ObjectOpenHashMap<Sheet> sheets = new Long2ObjectOpenHashMap<>();
    private long lastRequest;
    private long drawClock;

    /**
     * How far away the column a sample is compared against sits, in samples.
     *
     * <p>The game's own map compares against the block immediately north, and that is why it reads
     * as speckle on rough ground rather than as landform: Minecraft's surface is quantised to whole
     * blocks, so a comparison across one block can only ever answer higher, same, or lower — three
     * values, and on ordinary terrain almost every column differs from its neighbour by exactly one.
     * The result is noise at the scale of a block laid over a map read at the scale of a kilometre.
     *
     * <p>Measuring across several blocks asks a different question — <em>which way does this ground
     * fall, and how steeply</em> — which has many answers rather than three, and which is the
     * question someone siting a gun or judging dead ground is actually asking.
     */
    private static final int RELIEF_MACRO_RUN = 6;
    private static final int RELIEF_MICRO_RUN = 1;
    private static final int RELIEF_RUN = RELIEF_MACRO_RUN;

    /**
     * Samples either side of a sheet that the drawing reaches into.
     *
     * <p>The border ring, plus the widest comparison the shading makes from it. Derived rather than
     * written down, so that changing either of the two cannot leave this stale — which is exactly
     * what happened when it was a number of its own.
     */
    private static final int MARGIN = BORDER + RELIEF_RUN;

    /** Width of the gathered neighbourhood: the sheet, plus a margin on each side. */
    private static final int FIELD = SHEET_PIXELS + 2 * MARGIN;

    // ---- background bake infrastructure ---------------------------------------------------------
    //
    // Gathering and shading a sheet is pure arithmetic on flat arrays — no OpenGL, no game state
    // beyond what the tile cache holds — so it runs on a small pool of daemon threads. The render
    // thread's only duty is to pick up the finished pixels and upload them, which is one memcpy and
    // one GL call.

    /**
     * Per-thread scratch arrays for the gather-and-shade pass.
     *
     * <p>Held in a {@link ThreadLocal} so each background worker has its own, avoiding both
     * allocation on every build and contention between workers. The render thread itself keeps the
     * instance it has always had, for the rare synchronous paths that survive (patches, rebuilds).
     */
    private static final class BakeWorkspace {
        final short[] fieldFloor = new short[FIELD * FIELD];
        final int[] fieldColour = new int[FIELD * FIELD];
        final float[] fieldMurk = new float[FIELD * FIELD];
        final float[] fieldHazard = new float[FIELD * FIELD];
    }

    private static final ThreadLocal<BakeWorkspace> THREAD_WORKSPACE =
            ThreadLocal.withInitial(BakeWorkspace::new);

    /**
     * What a background bake produces: a rectangle of RGBA pixels and the metadata the render
     * thread needs to file them.
     */
    private static final class BakeResult {
        final long sheetKey;
        final int level;
        final int sheetX;
        final int sheetZ;
        final int[] pixels;      // TEXTURE_PIXELS × TEXTURE_PIXELS
        final boolean anyGround;
        final int version;

        /**
         * The world these pixels are of.
         *
         * <p>A bake outlives the world it was started in. Nothing cancels one — the pool thread is
         * already deep in a tile when the player quits to the menu — so its result arrives in
         * whichever world is loaded by then, and the square it belongs to is looked up by
         * coordinate, which means something else there. What that looked like was one sharp square
         * of the previous world's forest sitting in the middle of the new world's ocean.
         *
         * <p>The same guard the client's tile store already carries, one layer up: stamp the work
         * with the world it was asked under, and refuse an answer whose world has moved on.
         */
        final long generation;

        final int fromTx, fromTz, toTx, toTz;

        BakeResult(long sheetKey, int level, int sheetX, int sheetZ, int[] pixels,
                   boolean anyGround, int version, long generation,
                   int fromTx, int fromTz, int toTx, int toTz) {
            this.sheetKey = sheetKey;
            this.level = level;
            this.sheetX = sheetX;
            this.sheetZ = sheetZ;
            this.pixels = pixels;
            this.anyGround = anyGround;
            this.version = version;
            this.generation = generation;
            this.fromTx = fromTx;
            this.fromTz = fromTz;
            this.toTx = toTx;
            this.toTz = toTz;
        }
    }

    /**
     * Two daemon threads. More than one lets two sheets bake at once, which hides the latency of
     * the lock on the tile cache; more than two gains nothing because this is CPU-bound arithmetic
     * and the game's own threads need the rest of the cores.
     */
    private static final ExecutorService BAKE_POOL = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "TerrainBake");
        t.setDaemon(true);
        return t;
    });

    /** Results ready for the render thread to upload. Drained at the start of every frame. */
    private final ConcurrentLinkedQueue<BakeResult> finishedBakes = new ConcurrentLinkedQueue<>();

    /** Sheet keys currently being baked, so the same sheet is not submitted twice. */
    private final Set<Long> bakingKeys = new HashSet<>();

    /**
     * How many background bake tasks may be in flight at once.
     *
     * <p>More than this and the pool falls behind, the queue grows, and the results arrive after
     * the view has moved on — wasted work. Sized off {@code MAX_BUILDS_PER_FRAME} (16, declared
     * below — a plain field reference here would be an illegal forward reference, hence the literal):
     * enough to hold two frames' worth of fresh submissions without refusing work the pool's two
     * threads could still get to in time — confirmed against the {@code artillerytablet.mapTrace}
     * log, which was showing "refused ... for pool" as the dominant refusal once
     * {@code MAX_BUILDS_PER_FRAME} was corrected from 4 to 16 (this constant was never recalibrated
     * when that happened).
     */
    private static final int MAX_BAKING = 32;

    /**
     * How many sheets may be built <em>again</em> in one frame.
     *
     * <p>Averaging made a coarse sheet sixteen times dearer to build, and a wide view holds hundreds
     * of them. Their expiry is staggered so they do not all come due at once, but staggering only
     * spreads the load — it does not cap it, and a cap is what keeps one unlucky frame from stalling.
     *
     * <p>Deliberately applies to rebuilds only. A sheet that has never been built is ground the
     * player cannot see at all, and no budget is worth showing a hole; one that already exists is
     * merely showing something a second or two old, which at these zooms is invisible.
     */
    private static final int MAX_REBUILDS_PER_FRAME = 1;

    /**
     * How many sheets may be built for the first time in one frame.
     *
     * <p>Separate from the rebuild budget and far larger, because the two are not the same problem.
     * A sheet that has never been built is ground the player cannot see at all, so making it wait is
     * a hole in the map; a sheet that merely wants refreshing is a second out of date, which nobody
     * can see. Holes have to fill fast, refreshes can take their time.
     *
     * <p>There is a budget here at all because a sheet is now four times the work it used to be. In
     * play the fill-in is paced by the network anyway - tiles arrive a few dozen at a time - but
     * zooming out over ground already surveyed asks for three hundred sheets at once, and without a
     * cap that is one frame doing a third of a second of work. Sixteen a frame fills a fresh view in
     * about a fifth of a second, which reads as the map arriving rather than as the game stopping.
     */
    private static final int MAX_BUILDS_PER_FRAME = 16;

    /**
     * How many finished background bakes may be copied into a {@link NativeImage} and uploaded to
     * the GPU in one frame.
     *
     * <p>{@link #drainBakes} used to drain the whole queue unconditionally every frame, on the
     * reasoning that a copy-and-upload is cheap — true for one, not for as many as {@link
     * #MAX_BAKING} now allows to queue up at once. A burst of surveyed ground landing all together
     * (a fresh boot over already-explored country, say) could fill the bake queue and then have this
     * drain every one of them in a single frame — measured at 68ms in one such frame, well past a
     * dropped-frame stutter. Capped the same way the submit side already is, so a burst spreads
     * across a few frames instead of spiking one of them.
     */
    private static final int MAX_DRAINS_PER_FRAME = 8;

    private int rebuildsLeft;
    private int buildsLeft;
    private int drainsLeft;

    /** The world these pictures belong to. A change means every one of them is of somewhere else. */
    private long builtGeneration = -1L;

    /** One square of ground on the graphics card, and what it was built from. */
    private static final class Sheet {
        DynamicTexture texture;
        ResourceLocation id;

        /**
         * The cache version these pixels are current as of.
         *
         * <p>This was a fingerprint of the tiles read, at the finest level, and a timer at the
         * coarse ones. Both are gone: the cache now says which tiles moved and when, so one number
         * answers it at every level, and a square whose ground did not move simply takes the new
         * version and is done.
         */
        int builtVersion = -1;

        /** True when nothing under it has been surveyed, so it is kept but not drawn. */
        boolean empty;

        /** When this was last drawn, for deciding what to release. */
        long lastDrawn;

        /** True while a background thread is baking pixels for this sheet. */
        boolean baking;

        /**
         * Whether there are pixels on the card that may be drawn.
         *
         * <p>The one question the draw loop is actually asking, and until now it asked a different
         * one — {@code empty} — eight separate times. Those two questions agreed for as long as a
         * sheet went from nothing to painted inside a single call. Baking in the background put a
         * third state between them: <em>exists, is not bare, has no texture yet</em>. A sheet in
         * that state answered "not empty", was handed to {@code blit}, and the game died on a null
         * texture id in the first frame the map was opened.
         *
         * <p>So the question is asked here, once. A sheet is drawable when it has a picture and that
         * picture is of something — never by inference from one half of that.
         */
        boolean drawable() {
            return id != null && !empty;
        }

        /**
         * Whether this square holds an answer at all — pixels, or a settled "there is nothing here".
         *
         * <p>The other half of the same distinction, and the half that made a part of the map blur
         * permanently at every zoom past four kilometres. A square whose bake was skipped, because
         * the pool was full the frame it was asked for, is <em>neither</em>: no pixels, and no
         * finding of bare ground. It was reaching the shortcut below that says "nothing changed on
         * this square, so it is as current as it ever was" — perfectly true, and catastrophic here,
         * because it stamped the current version onto a square that had never been painted. From
         * then on the square counted as up to date and was never baked again, the fine pass never
         * drew it, and the coarse underlay showed through it for the rest of the session.
         *
         * <p>Which is why blur appeared at four kilometres and not below: that is the first zoom
         * step where the level changes, so a whole viewful of squares is demanded in one frame and
         * the ones past the budget are exactly the ones this stranded.
         */
        boolean answered() {
            return id != null || empty;
        }
    }

    /**
     * A sheet if it can be drawn, or null.
     *
     * <p>Every path out of {@link #sheetFor} goes through this, so there is no way to hand the draw
     * loop a sheet it cannot blit. That is the same rule as the one about geometry being defined in
     * exactly one place, applied to a predicate: restated at eight return statements it was only a
     * matter of time before a new state made one of them wrong, and it did.
     */
    private static Sheet drawableOrNull(Sheet sheet) {
        return sheet != null && sheet.drawable() ? sheet : null;
    }

    /**
     * Draws every surveyed tile that falls inside the view.
     *
     * @return false when nothing is known about this area yet and nothing was drawn
     */
    public boolean draw(GuiGraphics g, int x, int y, int width, int height,
                        int minBlockX, int minBlockZ, int spanX, int spanZ) {
        TerrainClientCache.checkWorld();

        // Textures live here, not in the cache, so clearing the cache's data left the previous
        // world's ground drawn over the new one.
        long generation = TerrainClientCache.stamp();
        if (generation != builtGeneration) {
            builtGeneration = generation;
            close();
        }

        // Pick up whatever the background threads have finished since the last frame.
        drainsLeft = MAX_DRAINS_PER_FRAME;
        drainBakes();

        // On a clock and nothing else. It used to run whenever the view had moved as well, which
        // sounds thrifty and means "every frame of a drag" — and this sweep walks every tile in
        // sight. That was the dominant cost at a wide zoom, well past anything the drawing did.
        // Half a second late asking for ground nobody has scrolled to yet is not noticeable.
        long now = System.currentTimeMillis();
        if (now - lastRequest > REQUEST_INTERVAL_MS) {
            lastRequest = now;
            TerrainClientCache.ensureCovered(minBlockX, minBlockZ, minBlockX + spanX, minBlockZ + spanZ);
        }

        // Push the panel backdrop out first: blit reaches the screen immediately while fills are
        // batched, so without this the backdrop would land on top and hide the ground.
        g.flush();
        // Unsurveyed columns are transparent and must let the grid backdrop through.
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int level = levelFor(spanX);

        long startedAt = TRACE ? System.nanoTime() : 0L;

        drawClock++;
        rebuildsLeft = MAX_REBUILDS_PER_FRAME;
        buildsLeft = MAX_BUILDS_PER_FRAME;
        patchesLeft = MAX_PATCHES_PER_FRAME;
        boolean any = false;

        // Every sheet drawn inside one transform from blocks to screen, rather than each working out
        // its own corners in whole pixels.
        //
        // This is the last place the old rounding survived. Carrying the map centre as a fraction
        // fixed the layer as a whole, but each sheet still rounded its own two edges to whole pixels
        // — so as the view crept, one sheet's edge crossed a pixel boundary a moment before its
        // neighbour's, and the pair squeezed and stretched against each other by a pixel. That is
        // the rippling: not the picture moving unevenly, but the pieces of it moving at slightly
        // different moments. Inside a shared transform there is nothing left to round separately;
        // the rasteriser places every quad in the same frame, to the same subpixel grid.
        g.pose().pushPose();
        g.pose().translate((double) x, (double) y, 0.0);
        g.pose().scale((float) width / spanX, (float) height / spanZ, 1f);

        // Coarser ground underneath, first, wherever the fine pass has nothing to show.
        //
        // A zoom step changes which level is drawn and nothing at the new level has ever been built,
        // so the panel went black and filled in a square at a time. The ground is not missing — it is
        // held, at a coarseness two levels up, where one sheet covers sixteen times the ground for
        // the same single build. So the eye sees the map arrive blurred and sharpen, rather than
        // arrive in pieces.
        //
        // Two shapes, because two situations. A change of level has nothing at all, and gets the
        // whole view. Everything else — panning, reopening on a centre that has shifted — is missing
        // a handful of squares at an edge, and gets a patch over exactly those. That distinction is
        // the whole of the second fix: covering the whole view for one absent edge square meant
        // reopening the tablet put a coarse sheet under ground that was already sharp, and the eye
        // catches that as a blur that resolves even though nothing was ever wrong with it.
        int under = Math.min(level + UNDERLAY_STEPS, MAX_LEVEL);
        if (under > level) {
            if (level != lastFineLevel) {
                any = drawLevel(g, under, minBlockX, minBlockZ, spanX, spanZ, now, false);
            } else if (!missing.isEmpty()) {
                any = drawPatches(g, level, under, minBlockX, minBlockZ, now);
            }
        }

        // Read above, written here: the patch pass works from what the last frame could not draw.
        // A frame of lag is invisible, and it buys the coarse pass first claim on the build budget —
        // which is what it should have, since one of its sheets stands in for sixteen.
        missing.clear();
        any |= drawLevel(g, level, minBlockX, minBlockZ, spanX, spanZ, now, true);
        lastFineLevel = level;
        if (TRACE) {
            traceBlurred += missing.size();
        }

        g.pose().popPose();

        warmNextZoom(level, minBlockX, minBlockZ, spanX, spanZ, now);

        evictSurplus();
        if (TRACE) {
            trace(startedAt, now, level);
        }
        return any;
    }

    /**
     * Adds this frame to the running totals and reports them now and then.
     *
     * <p>Per frame, not per second: a sheet build is around two hundred thousand cells of arithmetic
     * and a sixty-six kilobyte upload to the card, and every blit is its own draw call. Those two
     * numbers are what a frame actually costs, and the millisecond figure is there to say whether
     * either of them matters.
     */
    private void trace(long startedAt, long now, int level) {
        traceNanos += System.nanoTime() - startedAt;
        traceFrames++;

        if (traceSince == 0L) {
            traceSince = now;
        }
        if (now - traceSince < TRACE_EVERY_MS) {
            return;
        }

        int builds = Math.max(1, traceBuilds);
        ArtilleryTablet.LOGGER.info(
                "map trace: {} frames | {} ms/frame in draw | {} builds/frame | {} blits/frame"
                        + " | {} sheets live of {} | level {}"
                        + " || per build {} ms = gather {} + shade {} + upload {}"
                        + " || {} blurred/frame, refused {} for budget + {} for pool,"
                        + " {} evicted/frame, {} patched unbuilt"
                        + " || {} tiles/s arriving ({} of them local), {} outstanding",
                traceFrames,
                String.format("%.2f", traceNanos / 1e6 / traceFrames),
                String.format("%.1f", traceBuilds / (float) traceFrames),
                String.format("%.0f", traceBlits / (float) traceFrames),
                sheets.size(), MAX_LIVE_TEXTURES, level,
                String.format("%.2f",
                        (traceGatherNanos + traceShadeNanos + traceUploadNanos) / 1e6 / builds),
                String.format("%.2f", traceGatherNanos / 1e6 / builds),
                String.format("%.2f", traceShadeNanos / 1e6 / builds),
                String.format("%.2f", traceUploadNanos / 1e6 / builds),
                String.format("%.1f", traceBlurred / (float) traceFrames),
                traceNoBudget,
                traceNoPool,
                String.format("%.1f", traceEvicted / (float) traceFrames),
                patchedFresh,
                // The one number that says whether the map is drawing-bound or fetch-bound, and the
                // one nobody had until raising the request rate made everything worse.
                String.format("%.0f",
                        (TerrainClientCache.arrived() - traceArrived) * 1000f / (now - traceSince)),
                String.format("%.0f",
                        (TerrainClientCache.builtLocally() - traceLocal) * 1000f / (now - traceSince)),
                TerrainClientCache.outstanding());

        traceSince = now;
        traceArrived = TerrainClientCache.arrived();
        traceLocal = TerrainClientCache.builtLocally();
        traceFrames = 0;
        traceNanos = 0L;
        traceBuilds = 0;
        traceBlits = 0;
        traceGatherNanos = 0L;
        traceShadeNanos = 0L;
        traceUploadNanos = 0L;
        traceBlurred = 0;
        traceNoBudget = 0;
        traceNoPool = 0;
        traceEvicted = 0;
        // patchedFresh is a running total on purpose: it counts a thing that should never happen at
        // all, so what matters is whether it has ever happened, not how often it did lately.
    }

    // ---- what a frame costs ----------------------------------------------------------------------
    //
    // Off unless asked for, and asked for only in the dev client (see build.gradle). It exists
    // because the honest answer to "why is the map heavy" is that nobody has measured it, and this
    // layer has now had three separate performance faults whose cause was not what it looked like.
    // Guessing at this altitude is how a day gets spent optimising the wrong loop.
    //
    // Every number here is a count of something that happens per frame, because that is the only
    // unit that means anything: a sheet build is roughly two hundred thousand cells of arithmetic
    // and a sixty-six kilobyte upload to the card, and a blit is one draw call each.

    private static final boolean TRACE = Boolean.getBoolean("artillerytablet.mapTrace");

    private static final long TRACE_EVERY_MS = 2000L;

    private long traceSince;
    private int traceFrames;
    private long traceNanos;
    private int traceBuilds;
    private int traceBlits;

    // A build split three ways. Knowing it costs three milliseconds does not say what to do about
    // it; knowing which of the three phases holds those milliseconds does.
    /** Tiles taken in when the last report went out, so the next one can report a rate. */
    private long traceArrived;

    /** Tiles the client had built itself when the last report went out. */
    private long traceLocal;

    private long traceGatherNanos;
    private long traceShadeNanos;
    private long traceUploadNanos;

    // ---- why the map is blurred, as opposed to what a frame costs ---------------------------------
    //
    // The numbers above answer "is the map expensive". They cannot answer "why is that patch still
    // soft", and three different faults produce that one appearance: the frame budget refusing
    // builds, the bake pool being full, and squares being evicted as fast as they are built. These
    // four separate them. A blurred count that stays put while all three refusal counts are nought
    // is the fourth answer, and the worst one: a square nothing is trying to build any more.

    /** Squares the fine pass could not draw, summed over the frames in a report. */
    private int traceBlurred;

    /** Builds refused because the frame had spent its allowance. */
    private int traceNoBudget;

    /** Builds refused because every bake slot was taken. */
    private int traceNoPool;

    /** Sheets dropped to stay under the texture cap. */
    private int traceEvicted;

    /**
     * Whether the last frame drew every square it wanted.
     *
     * <p>Not the same question as "is anything on screen", which is answered by {@link #draw}
     * returning true and is answered yes by a single square out of eighty. This is what the boot
     * screen waits on, and the difference between the two is why it used to vanish while the map
     * was still filling in.
     *
     * <p>Ground that has been looked at and found bare does not count as missing. Over country that
     * has never been generated there is nothing to wait for, and waiting anyway would promise
     * something that is never coming.
     */
    public boolean isComplete() {
        return missing.isEmpty();
    }

    /** How often a patch has landed on a square with no picture yet. Should stay at nought. */
    private int patchedFresh;

    /** The level the fine pass drew last frame. A change of it means nothing new is built yet. */
    private int lastFineLevel = -1;

    /**
     * Squares the fine pass wanted last frame and could not draw, as packed sheet coordinates.
     *
     * <p>Only ever squares that have not been built. A square that came back bare is an answer, not
     * a gap — no coarser pass would find ground there either, and patching it would put a smear of
     * averaged neighbours over country nobody has surveyed.
     */
    private final LongArrayList missing = new LongArrayList();

    /**
     * Draws one level's worth of sheets across the view, inside the caller's transform.
     *
     * @param record whether to note the squares that were not there, for the next frame to patch
     * @return whether anything reached the screen
     */
    private boolean drawLevel(GuiGraphics g, int level, int minBlockX, int minBlockZ,
                              int spanX, int spanZ, long now, boolean record) {
        int cover = groundPerSheet(level);
        int firstTileX = Math.floorDiv(minBlockX, cover);
        // One tile further than the view strictly needs. The layer is shifted by up to a block to
        // carry the sub-block remainder, which would otherwise leave a strip of bare ground along
        // the trailing edge.
        int lastTileX = Math.floorDiv(minBlockX + spanX + cover, cover);
        int firstTileZ = Math.floorDiv(minBlockZ, cover);
        int lastTileZ = Math.floorDiv(minBlockZ + spanZ + cover, cover);

        boolean any = false;

        for (int tileZ = firstTileZ; tileZ <= lastTileZ; tileZ++) {
            for (int tileX = firstTileX; tileX <= lastTileX; tileX++) {
                Sheet sheet = sheetFor(level, tileX, tileZ, now);
                if (sheet == null) {
                    if (record && !isKnownBare(level, tileX, tileZ)) {
                        missing.add(TerrainTile.key(tileX, tileZ));
                    }
                    continue;
                }

                // Positions are in blocks now, whole numbers by construction, and the transform
                // turns them into screen coordinates. Nothing here can disagree with its neighbour
                // about where a shared edge falls, because neither of them decides.
                int blockX = tileX * cover - minBlockX;
                int blockZ = tileZ * cover - minBlockZ;

                // The inner square only, skipping the border ring that exists for the filtering.
                g.blit(sheet.id, blockX, blockZ, cover, cover,
                        BORDER, BORDER, SHEET_PIXELS, SHEET_PIXELS, TEXTURE_PIXELS, TEXTURE_PIXELS);
                traceBlits++;
                any = true;
            }
        }
        return any;
    }

    /**
     * Spends whatever build budget the frame did not need on the zoom step below this one.
     *
     * <p>Zooming in changes which level is drawn, and until this existed nothing at the new level had
     * ever been built — so every step in showed the coarse pass and sharpened over the following
     * second. Nothing was wrong with the map; the work simply had not been started until the moment
     * it was wanted. This is the same trick as drawing the map while you play rather than when it is
     * opened, which is what the mod this was measured against does with its whole pipeline.
     *
     * <p>Over the span the next step in will actually use — half this one, about the same centre —
     * not over the present view. A finer level covers a quarter of the ground per square, so warming
     * the current span would build four times what any zoom will ever ask for, and evict the map to
     * do it.
     *
     * <p>What is warmed is marked as drawn a frame ago. That puts it behind everything on screen when
     * the cache has to drop something, which is the right order: a square nobody has looked at yet is
     * worth less than one being looked at now.
     */
    private void warmNextZoom(int level, int minBlockX, int minBlockZ, int spanX, int spanZ, long now) {
        if (level <= 0 || buildsLeft <= 0) {
            return;
        }

        int warm = level - 1;
        int intoX = spanX / 2;
        int intoZ = spanZ / 2;
        int fromX = minBlockX + intoX / 2;
        int fromZ = minBlockZ + intoZ / 2;

        int cover = groundPerSheet(warm);
        int firstTileX = Math.floorDiv(fromX, cover);
        int lastTileX = Math.floorDiv(fromX + intoX + cover, cover);
        int firstTileZ = Math.floorDiv(fromZ, cover);
        int lastTileZ = Math.floorDiv(fromZ + intoZ + cover, cover);

        for (int tileZ = firstTileZ; tileZ <= lastTileZ; tileZ++) {
            for (int tileX = firstTileX; tileX <= lastTileX; tileX++) {
                if (buildsLeft <= 0) {
                    return;
                }
                Sheet sheet = sheets.get(keyOf(warm, tileX, tileZ));
                if (sheet != null && sheet.builtVersion == TerrainClientCache.version()) {
                    continue;
                }
                sheetFor(warm, tileX, tileZ, now);
                // Read back rather than taken from the call, because the square this most needs to
                // demote is the one just created for it, and that comes back as null until its bake
                // lands. Left undemoted it would rank level with the ground actually on screen and
                // could crowd it out — which is the fault above, wearing the other hat.
                Sheet warmed = sheets.get(keyOf(warm, tileX, tileZ));
                if (warmed != null) {
                    warmed.lastDrawn = drawClock - 1;
                }
            }
        }
    }

    /** Whether this square has been looked at and found to hold no surveyed ground. */
    private boolean isKnownBare(int level, int sheetX, int sheetZ) {
        Sheet sheet = sheets.get(keyOf(level, sheetX, sheetZ));
        return sheet != null && sheet.empty;
    }

    /**
     * Fills the squares the fine pass could not draw with the part of a coarser sheet that covers
     * the same ground.
     *
     * <p>All of this arithmetic is exact, which is why it is safe to do at all. A coarse sheet
     * covers a whole number of fine squares — {@code LEVEL_FACTOR} to the power of the step between
     * them, squared — and both are laid out from the world origin in multiples of their own size, so
     * a fine square never straddles two coarse ones and the offset into the coarse texture is a
     * whole number of texels. Nothing here rounds, and so nothing here can drift a pixel against the
     * pass it is standing in for.
     *
     * @return whether anything reached the screen
     */
    private boolean drawPatches(GuiGraphics g, int fine, int under, int minBlockX, int minBlockZ,
                                long now) {
        int coverFine = groundPerSheet(fine);
        int coverUnder = groundPerSheet(under);
        int strideUnder = strideFor(under);
        // Texels of the coarse sheet that stand over one fine square.
        int span = coverFine / strideUnder;

        boolean any = false;
        for (int i = 0; i < missing.size(); i++) {
            long key = missing.getLong(i);
            int tileX = (int) (key >> 32);
            int tileZ = (int) key;

            int originX = tileX * coverFine;
            int originZ = tileZ * coverFine;
            int underX = Math.floorDiv(originX, coverUnder);
            int underZ = Math.floorDiv(originZ, coverUnder);

            Sheet sheet = sheetFor(under, underX, underZ, now);
            if (sheet == null) {
                continue;
            }

            // Past the border ring, then in by however far this square sits into the coarse one.
            int u = BORDER + (originX - underX * coverUnder) / strideUnder;
            int v = BORDER + (originZ - underZ * coverUnder) / strideUnder;

            g.blit(sheet.id, originX - minBlockX, originZ - minBlockZ, coverFine, coverFine,
                    u, v, span, span, TEXTURE_PIXELS, TEXTURE_PIXELS);
            traceBlits++;
            any = true;
        }
        return any;
    }

    /**
     * The coarsest level that still shows detail, given how much ground is on screen.
     *
     * <p>Chosen so the number of quads across stays bounded whatever the zoom. Without this the
     * count grows with the square of the view and overwhelms both the draw and the cap on live
     * textures — which is what made wide views crawl while close ones were smooth.
     */
    private static int levelFor(int span) {
        int level = 0;
        while (span / groundPerSheet(level) > MAX_QUADS_ACROSS) {
            level++;
        }
        return level;
    }

    /** Blocks along one edge of a sheet at this level: one per texel, times the stride. */
    private static int groundPerSheet(int level) {
        return SHEET_PIXELS * strideFor(level);
    }

    /** How many blocks one pixel of a sheet stands for at this level. */
    private static int strideFor(int level) {
        return (int) Math.pow(LEVEL_FACTOR, level);
    }

    /** A key that keeps the levels apart, since the same square of ground exists at several. */
    private static long keyOf(int level, int sheetX, int sheetZ) {
        return ((long) level << 56) ^ (TerrainTile.key(sheetX, sheetZ) & 0x00FFFFFFFFFFFFFFL);
    }

    /**
     * How much of a square a patch may cover before redrawing all of it is the cheaper answer.
     *
     * <p>A share of the texture, not a count of tiles. It was a count, of every tile that had changed
     * <em>anywhere in the world</em> — twenty-four, against a request batch of thirty-two. So every
     * sweep of ground arriving pushed every square past it, every square took the wholesale path,
     * and the wholesale path is rationed to one a frame. The patching this was all built for
     * essentially never ran, and it ran least during loading, which is the only time it matters.
     *
     * <p>The question a square has to answer is how much of <em>itself</em> is out of date, and it
     * already works that out: the union of the changed tiles that land on it. Sixteen tiles arriving
     * far away are nothing to it; two arriving under it are two.
     */
    private static final int PATCH_MAX_PERCENT = 50;

    /** How many squares may be patched in one frame. A patch is a fraction of a build, not free. */
    private static final int MAX_PATCHES_PER_FRAME = 48;

    private int patchesLeft;

    /** Scratch for the tiles that changed since a sheet was drawn. Reused, never handed out. */
    private final LongArrayList changed = new LongArrayList();

    /** The rectangle a patch will repaint, gathered by {@link #union}. */
    private final int[] rect = new int[4];

    /** Scratch for one tile's texel rectangle, so working it out allocates nothing. */
    private final int[] tileRect = new int[4];

    /**
     * The sheet for a square of ground, brought up to date if the ground under it has moved on.
     *
     * <p>Three outcomes, and which one applies is the whole of this round of performance work.
     * Nothing under it changed — the common case, and free. Something did, and it is a few tiles:
     * repaint just those, which is what the map mod this was measured against does when a chunk
     * lands. Or so much changed that finding out where would cost more than redrawing.
     *
     * <p>Full builds are now submitted to the background pool rather than done here. The sheet is
     * returned as null while baking, which the draw loop reads as "not ready yet" and patches with
     * the coarser underlay. When the bake finishes, {@link #drainBakes} picks it up and uploads it
     * the next frame.
     */
    private Sheet sheetFor(int level, int sheetX, int sheetZ, long now) {
        int cover = groundPerSheet(level);
        int originX = sheetX * cover;
        int originZ = sheetZ * cover;
        long key = keyOf(level, sheetX, sheetZ);

        Sheet sheet = sheets.get(key);
        int version = TerrainClientCache.version();

        // Current, and holding something to be current WITH. The second half is not belt and braces:
        // a square that reached the current version without ever being painted can never leave this
        // branch again, and that is exactly how a patch of the map came to stay blurred for good.
        if (sheet != null && sheet.builtVersion == version && sheet.answered()) {
            sheet.lastDrawn = drawClock;
            return drawableOrNull(sheet);
        }

        // Already being baked in the background — return what it has (possibly nothing).
        if (sheet != null && sheet.baking) {
            sheet.lastDrawn = drawClock;
            return drawableOrNull(sheet);
        }

        if (sheet == null) {
            if (!mayBuild()) {
                return null;
            }
            sheet = new Sheet();
            // Wanted this frame, and said so before anything can be dropped for room. Every other
            // way out of this method marks the square; this one did not, and a square made here has
            // the oldest possible age until it does — so once the cache was full, the eviction pass
            // at the end of the very same frame threw away the four squares just asked for, while
            // their bakes were still running. The next frame asked for the same four and lost them
            // the same way. Nothing was slow and nothing was broken: the map simply built ground and
            // discarded it, for ever, and what that looks like is a patch that never sharpens.
            sheet.lastDrawn = drawClock;
            sheets.put(key, sheet);
            submitBake(key, sheet, level, originX, originZ, sheetX, sheetZ, version);
            return null; // not ready yet — underlay will cover
        }

        // Held, and the cache has moved on. Work out how much of THIS square is out of date.
        boolean logReaches = TerrainClientCache.changedSince(sheet.builtVersion, changed);
        boolean spread = logReaches && union(level, originX, originZ);

        if (logReaches && !spread && sheet.answered()) {
            // Ground changed elsewhere in the world. This square is as current as it ever was, and
            // saying so is what stops one tile landing from dirtying every square on screen.
            //
            // Only for a square that holds an answer, though. "As current as it ever was" is a
            // statement about a square that HAS been looked at; applied to one whose bake was
            // skipped it is a promotion, and it stranded that square unpainted for good.
            sheet.builtVersion = version;
            sheet.lastDrawn = drawClock;
            return drawableOrNull(sheet);
        }

        // A bare square gave its texture back, so there is nothing to patch into; and a square most
        // of which has moved is one a patch would repaint anyway, for more bookkeeping.
        if (!logReaches || sheet.empty || tooWide()) {
            // A square with nothing on screen is drawing something from nothing, which is the budget
            // for building, not the one for redrawing. That question is "has it pixels", not "is it
            // bare" — a square waiting on a bake has no pixels either, and asking the wrong one put
            // it on the rebuild budget of one a frame while the map showed a blur where it stood.
            boolean allowed = sheet.drawable() ? mayRebuild() : mayBuild();
            if (!allowed) {
                sheet.lastDrawn = drawClock;
                return drawableOrNull(sheet);
            }
            submitBake(key, sheet, level, originX, originZ, sheetX, sheetZ, version);
            // Return stale content while re-baking.
            sheet.lastDrawn = drawClock;
            return drawableOrNull(sheet);
        }

        // Small patch — still done on the render thread because the upload is immediate and the
        // work is a fraction of a full build.
        if (patchesLeft <= 0) {
            // Out of budget. What it holds is a survey behind, which is not worth a stutter to fix.
            sheet.lastDrawn = drawClock;
            return drawableOrNull(sheet);
        }
        patchesLeft--;
        traceBuilds++;
        boolean ground = repaint(sheet, level, originX, originZ, sheetX, sheetZ,
                rect[0], rect[1], rect[2], rect[3]);
        // A patch can only ever ADD ground: it repaints where a tile landed and leaves everywhere
        // else holding what it held. So a patch finding nothing says nothing about the rest.
        return finish(sheet, ground || !sheet.empty, version);
    }

    // ---- background bake submit / drain -----------------------------------------------------------

    /**
     * Submits a full build of a sheet to the background pool.
     *
     * <p>The heavy work — gathering tile data and computing shaded pixels — happens off the render
     * thread. The result is an {@code int[]} of RGBA values that the render thread will copy into
     * a {@link NativeImage} and upload to the GPU in {@link #drainBakes}.
     */
    private void submitBake(long key, Sheet sheet, int level, int originX, int originZ,
                            int sheetX, int sheetZ, int version) {
        if (bakingKeys.size() >= MAX_BAKING) {
            // Note that the frame budget was spent to get here and bought nothing: the caller has
            // already counted this square as built for the purposes of its own allowance.
            if (TRACE) {
                traceNoPool++;
            }
            return; // pool is full — try again next frame
        }
        sheet.baking = true;
        bakingKeys.add(key);

        // Read here, on the render thread, and carried into the task. Read inside the task it would
        // be whatever the world had become by the time the task got a thread.
        long bakedFor = TerrainClientCache.stamp();

        BAKE_POOL.execute(() -> {
            try {
                BakeWorkspace ws = THREAD_WORKSPACE.get();
                int stride = strideFor(level);
                int[] pixels = new int[TEXTURE_PIXELS * TEXTURE_PIXELS];

                // Gather: read tile data into the workspace's flat arrays.
                gatherInto(ws, originX, originZ, stride,
                        0 - BORDER + MARGIN - RELIEF_RUN, 0 - BORDER + MARGIN - RELIEF_RUN,
                        TEXTURE_PIXELS - 1 - BORDER + MARGIN,
                        TEXTURE_PIXELS - 1 - BORDER + MARGIN);

                // Shade: compute final RGBA per texel into the pixels array.
                boolean anyGround = shadePixels(ws, pixels, stride,
                        0, 0, TEXTURE_PIXELS - 1, TEXTURE_PIXELS - 1);

                finishedBakes.add(new BakeResult(
                        key, level, sheetX, sheetZ, pixels, anyGround, version, bakedFor,
                        0, 0, TEXTURE_PIXELS - 1, TEXTURE_PIXELS - 1));
            } catch (Throwable t) {
                ArtilleryTablet.LOGGER.error("Background bake failed for sheet ({},{}) level {}",
                        sheetX, sheetZ, level, t);
                // Put a dummy result so the sheet is unblocked.
                finishedBakes.add(new BakeResult(
                        key, level, sheetX, sheetZ, null, false, version, bakedFor,
                        0, 0, TEXTURE_PIXELS - 1, TEXTURE_PIXELS - 1));
            }
        });
    }

    /**
     * Picks up finished bakes and uploads them to the GPU, up to {@link #MAX_DRAINS_PER_FRAME} of
     * them.
     *
     * <p>Called once at the start of every {@link #draw} frame. Each result is a rectangle of pixels
     * computed on a background thread; the render thread's own work is to copy them into a
     * {@link NativeImage} and issue one {@code glTexSubImage} — cheap for one result, not for as
     * many as {@link #MAX_BAKING} lets queue up in a burst. Whatever doesn't fit this frame's budget
     * stays in the queue and is picked up next frame; nothing here depends on being drained
     * promptly.
     */
    private void drainBakes() {
        BakeResult result;
        while (drainsLeft > 0 && (result = finishedBakes.poll()) != null) {
            drainsLeft--;
            bakingKeys.remove(result.sheetKey);

            Sheet sheet = sheets.get(result.sheetKey);
            if (sheet == null) {
                // Evicted or closed while baking — discard.
                continue;
            }

            // Cleared before anything is allowed to refuse this result, and the order is the whole
            // point. A square left marked as baking is never drawn and never baked again: sheetFor
            // returns nothing for it on every frame afterwards, so the coarse underlay stands in for
            // it for good. That is a permanent blur, arrived at by a guard meant to prevent a
            // different fault.
            sheet.baking = false;

            if (result.generation != builtGeneration) {
                // Baked in a world that has since been left. The square it names exists here too —
                // sheets are keyed by coordinate, and a coordinate means something else in the next
                // world — so painting this in would put the old world's ground on the new one's map.
                continue;
            }

            if (result.pixels == null) {
                // Bake threw — leave the sheet as it was.
                continue;
            }

            // Ensure the sheet has a texture to paint into.
            if (sheet.texture == null) {
                sheet.texture = new DynamicTexture(TEXTURE_PIXELS, TEXTURE_PIXELS, true);
                sheet.id = Minecraft.getInstance().getTextureManager().register(
                        "artillerytablet_terrain_" + result.level + "_"
                                + result.sheetX + "_" + result.sheetZ, sheet.texture);
            }

            NativeImage image = sheet.texture.getPixels();
            if (image == null) {
                continue;
            }

            // Copy the baked pixels into the NativeImage.
            for (int tz = result.fromTz; tz <= result.toTz; tz++) {
                for (int tx = result.fromTx; tx <= result.toTx; tx++) {
                    image.setPixelRGBA(tx, tz, result.pixels[tz * TEXTURE_PIXELS + tx]);
                }
            }

            // Upload just the changed rectangle to the GPU.
            sheet.texture.bind();
            image.upload(0, result.fromTx, result.fromTz, result.fromTx, result.fromTz,
                    result.toTx - result.fromTx + 1, result.toTz - result.fromTz + 1,
                    false, false);
            applyFilter(sheet);

            traceBuilds++;
            finish(sheet, result.anyGround, result.version);
        }
    }

    /** Records what a build or patch found and hands the sheet back, or null when it is bare. */
    private Sheet finish(Sheet sheet, boolean ground, int version) {
        sheet.builtVersion = version;
        sheet.empty = !ground;
        sheet.lastDrawn = drawClock;
        if (!ground) {
            // A sheet over unsurveyed country is kept, marked bare. Dropping it meant scanning its
            // pixels again on the very next frame and finding nothing again — and at a wide zoom
            // most of the view is unsurveyed, so that was most of the sheets, every frame.
            giveBackTexture(sheet);
            return null;
        }
        // Through the same predicate as every other way out, so a build that somehow ended without
        // a texture cannot reach the draw loop by this door either.
        return drawableOrNull(sheet);
    }

    /**
     * Gathers the changed tiles that land on this sheet into one rectangle, left in {@link #rect}.
     *
     * <p>One rectangle, not one per tile. Tiles arrive from the server in batches covering ground
     * next to each other, so their union is nearly always tight — and where it is not it grows
     * towards the whole texture, which {@link #tooWide} then reads as a reason to redraw instead.
     * A dozen small uploads would also cost more in calls to the card than one larger one.
     *
     * @return false when none of them touch it, which is the common case and the cheap one
     */
    private boolean union(int level, int originX, int originZ) {
        int fromX = Integer.MAX_VALUE;
        int fromZ = Integer.MAX_VALUE;
        int toX = Integer.MIN_VALUE;
        int toZ = Integer.MIN_VALUE;

        for (int i = 0; i < changed.size(); i++) {
            int[] at = rectFor(changed.getLong(i), level, originX, originZ);
            if (at == null) {
                continue;
            }
            fromX = Math.min(fromX, at[0]);
            fromZ = Math.min(fromZ, at[1]);
            toX = Math.max(toX, at[2]);
            toZ = Math.max(toZ, at[3]);
        }

        if (fromX > toX || fromZ > toZ) {
            return false;
        }
        rect[0] = fromX;
        rect[1] = fromZ;
        rect[2] = toX;
        rect[3] = toZ;
        return true;
    }

    /** Whether the rectangle just gathered covers enough of the square to be worth redrawing whole. */
    private boolean tooWide() {
        long cells = (long) (rect[2] - rect[0] + 1) * (rect[3] - rect[1] + 1);
        return cells * 100 > (long) TEXTURE_PIXELS * TEXTURE_PIXELS * PATCH_MAX_PERCENT;
    }

    /**
     * Where a tile lands in a sheet's texels, or null when it lands outside it.
     *
     * <p>Widened to the south and east by the reach of the shading. Not because those texels stand
     * on the tile — they do not — but because shading one looks back to the north and west, so a
     * texel three along still reads ground that has just changed. Leaving them out would put a
     * three-pixel seam of stale lighting along every tile boundary, which is the kind of fault that
     * survives for weeks because it reads as a feature of the terrain rather than as a bug.
     */
    private int[] rectFor(long tileKey, int level, int originX, int originZ) {
        int tileX = (int) (tileKey >> 32);
        int tileZ = (int) tileKey;
        int stride = strideFor(level);

        int blockX = TerrainTile.tileToBlock(tileX);
        int blockZ = TerrainTile.tileToBlock(tileZ);

        int fromX = Math.max(0, texelOf(blockX, originX, stride));
        int fromZ = Math.max(0, texelOf(blockZ, originZ, stride));
        int toX = Math.min(TEXTURE_PIXELS - 1,
                texelOf(blockX + TerrainTile.SIDE - 1, originX, stride) + RELIEF_RUN);
        int toZ = Math.min(TEXTURE_PIXELS - 1,
                texelOf(blockZ + TerrainTile.SIDE - 1, originZ, stride) + RELIEF_RUN);

        if (fromX > toX || fromZ > toZ) {
            return null;
        }

        tileRect[0] = fromX;
        tileRect[1] = fromZ;
        tileRect[2] = toX;
        tileRect[3] = toZ;
        return tileRect;
    }

    /** Which texel of a sheet a world coordinate falls on. Floor division: half the world is west. */
    private static int texelOf(int world, int origin, int stride) {
        return Math.floorDiv(world - origin, stride) + BORDER;
    }

    /**
     * Redraws a rectangle of texels, from the ground the cache holds now.
     *
     * <p>Used for patches (small, synchronous repaints on the render thread). Full builds go
     * through {@link #submitBake} instead.
     *
     * @return whether any ground was found inside the sheet proper, ignoring its border ring
     */
    private boolean repaint(Sheet sheet, int level, int originX, int originZ, int sheetX, int sheetZ,
                            int fromTx, int fromTz, int toTx, int toTz) {
        if (sheet.texture == null) {
            sheet.texture = new DynamicTexture(TEXTURE_PIXELS, TEXTURE_PIXELS, true);
            sheet.id = Minecraft.getInstance().getTextureManager().register(
                    "artillerytablet_terrain_" + level + "_" + sheetX + "_" + sheetZ, sheet.texture);

            if (fromTx != 0 || fromTz != 0
                    || toTx != TEXTURE_PIXELS - 1 || toTz != TEXTURE_PIXELS - 1) {
                patchedFresh++;
                if (patchedFresh == 1) {
                    ArtilleryTablet.LOGGER.info("Tablet map patched a square before it had been "
                            + "drawn (level {}, {},{}); painting all of it instead. Worth knowing "
                            + "about \u2014 it means a square lost its picture without being forgotten.",
                            level, sheetX, sheetZ);
                }
                fromTx = 0;
                fromTz = 0;
                toTx = TEXTURE_PIXELS - 1;
                toTz = TEXTURE_PIXELS - 1;
            }
        }

        NativeImage image = sheet.texture.getPixels();
        if (image == null) {
            return false;
        }

        int stride = strideFor(level);
        BakeWorkspace ws = THREAD_WORKSPACE.get();

        long phase = TRACE ? System.nanoTime() : 0L;
        gatherInto(ws, originX, originZ, stride,
                fromTx - BORDER + MARGIN - RELIEF_RUN, fromTz - BORDER + MARGIN - RELIEF_RUN,
                toTx - BORDER + MARGIN, toTz - BORDER + MARGIN);
        if (TRACE) {
            long done = System.nanoTime();
            traceGatherNanos += done - phase;
            phase = done;
        }

        // Shade into a temporary pixel array, then copy to NativeImage.
        int[] pixels = new int[TEXTURE_PIXELS * TEXTURE_PIXELS];
        boolean anyGround = shadePixels(ws, pixels, stride, fromTx, fromTz, toTx, toTz);

        for (int tz = fromTz; tz <= toTz; tz++) {
            for (int tx = fromTx; tx <= toTx; tx++) {
                image.setPixelRGBA(tx, tz, pixels[tz * TEXTURE_PIXELS + tx]);
            }
        }

        if (TRACE) {
            long done = System.nanoTime();
            traceShadeNanos += done - phase;
            phase = done;
        }

        sheet.texture.bind();
        image.upload(0, fromTx, fromTz, fromTx, fromTz,
                toTx - fromTx + 1, toTz - fromTz + 1, false, false);
        applyFilter(sheet);
        if (TRACE) {
            traceUploadNanos += System.nanoTime() - phase;
        }
        return anyGround;
    }

    /**
     * Nearest when the texture is stretched, linear when it is squeezed.
     *
     * <p>Read straight off the map mod this was compared against, which sets exactly this pair on
     * every region texture. It is the right pair and the reason is worth stating: the two directions
     * are different problems. Stretched, a texel <em>is</em> a block and smoothing it would invent
     * ground that was never surveyed — a fire-control map must not do that. Squeezed, several texels
     * fall inside one screen pixel, and picking one of them and discarding the rest is not honesty,
     * it is aliasing: coastlines break into staircases and small features flicker as the view moves.
     * Averaging them is the truthful answer there.
     *
     * <p>Has to be reapplied after every upload. {@code NativeImage.upload} sets both filters itself
     * from its blur flag, so anything set once at creation is undone the first time the sheet is
     * built again.
     */
    private static void applyFilter(Sheet sheet) {
        sheet.texture.bind();
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
    }

    /**
     * Reads the cache into flat arrays covering the sheet plus a margin all round.
     *
     * <p>Static so it can run on a background thread with its own workspace.
     * The tile cache reads go through synchronized accessors.
     */
    private static void gatherInto(BakeWorkspace ws, int originX, int originZ, int stride,
                                   int fromFx, int fromFz, int toFx, int toFz) {
        int level = TerrainMips.levelForStride(stride);

        TerrainTile heldTile = null;
        TerrainMips heldMips = null;
        int heldTileX = Integer.MIN_VALUE;
        int heldTileZ = Integer.MIN_VALUE;

        int perTile = TerrainMips.widthOf(level);

        for (int fz = Math.max(0, fromFz); fz <= Math.min(FIELD - 1, toFz); fz++) {
            int worldZ = originZ + (fz - MARGIN) * stride;
            int tileZ = TerrainTile.blockToTile(worldZ);

            for (int fx = Math.max(0, fromFx); fx <= Math.min(FIELD - 1, toFx); fx++) {
                int worldX = originX + (fx - MARGIN) * stride;
                int tileX = TerrainTile.blockToTile(worldX);
                int cell = fz * FIELD + fx;

                if (tileX != heldTileX || tileZ != heldTileZ) {
                    heldTile = TerrainClientCache.tileAt(tileX, tileZ);
                    heldMips = level == 0 ? null : TerrainClientCache.mipsAt(tileX, tileZ);
                    heldTileX = tileX;
                    heldTileZ = tileZ;
                }
                if (heldTile == null) {
                    ws.fieldFloor[cell] = TerrainTile.NO_DATA;
                    continue;
                }

                if (level == 0) {
                    int inTileX = Math.floorMod(worldX, TerrainTile.SIDE);
                    int inTileZ = Math.floorMod(worldZ, TerrainTile.SIDE);
                    int index = TerrainTile.index(inTileX, inTileZ);
                    short surface = heldTile.height[index];
                    if (surface == TerrainTile.NO_DATA) {
                        ws.fieldFloor[cell] = TerrainTile.NO_DATA;
                        continue;
                    }
                    int depth = heldTile.depthAt(index);
                    ws.fieldFloor[cell] = (short) (surface - depth);
                    // Through the same blend the reduced copies are built with. Were this to look up
                    // the column's own tint instead, the sea would change colour at the zoom step
                    // where the finest level gives way to the next — the standing rule that one
                    // column's colour is decided in exactly one place.
                    ws.fieldColour[cell] = TerrainMips.groundColour(
                            heldTile.blockAt(index), heldTile.biome[index], depth,
                            depth > 0 ? TerrainMips.blendedWaterTint(heldTile, inTileX, inTileZ) : 0,
                            surface);
                    ws.fieldMurk[cell] = TerrainMips.murkOf(depth);
                    ws.fieldHazard[cell] = BlockPalette.isHazard(heldTile.blockAt(index)) ? 1f : 0f;
                    continue;
                }

                if (heldMips == null) {
                    ws.fieldFloor[cell] = TerrainTile.NO_DATA;
                    continue;
                }

                int localX = Math.floorMod(worldX, TerrainTile.SIDE) * perTile / TerrainTile.SIDE;
                int localZ = Math.floorMod(worldZ, TerrainTile.SIDE) * perTile / TerrainTile.SIDE;
                short reduced = heldMips.floorAt(level, localX, localZ);
                if (reduced == TerrainTile.NO_DATA) {
                    ws.fieldFloor[cell] = TerrainTile.NO_DATA;
                    continue;
                }
                ws.fieldFloor[cell] = reduced;
                ws.fieldColour[cell] = heldMips.colourAt(level, localX, localZ);
                ws.fieldMurk[cell] = TerrainMips.murkOf(heldMips.depthAt(level, localX, localZ));
                ws.fieldHazard[cell] = TerrainMips.hazardOf(heldMips.hazardAt(level, localX, localZ));
            }
        }
    }

    /**
     * Shades a rectangle of gathered data into an RGBA pixel array.
     *
     * <p>Static and pure — reads only from the workspace's flat arrays and the constant
     * {@code SQUASH} table, so it can run on any thread.
     *
     * @return whether any surveyed ground was found inside the sheet proper
     */
    private static boolean shadePixels(BakeWorkspace ws, int[] pixels, int stride,
                                       int fromTx, int fromTz, int toTx, int toTz) {
        boolean anyGround = false;
        for (int tz = fromTz; tz <= toTz; tz++) {
            int fz = tz - BORDER + MARGIN;
            for (int tx = fromTx; tx <= toTx; tx++) {
                int cell = fz * FIELD + tx - BORDER + MARGIN;
                if (ws.fieldFloor[cell] == TerrainTile.NO_DATA) {
                    pixels[tz * TEXTURE_PIXELS + tx] = 0;
                    continue;
                }
                pixels[tz * TEXTURE_PIXELS + tx] = shadeCell(ws, cell, stride);
                if (tx >= BORDER && tz >= BORDER
                        && tx < BORDER + SHEET_PIXELS && tz < BORDER + SHEET_PIXELS) {
                    anyGround = true;
                }
            }
        }
        return anyGround;
    }


    /** Spends one of this frame's rebuilds, or refuses when they are gone. */
    private boolean mayRebuild() {
        if (rebuildsLeft <= 0) {
            if (TRACE) {
                traceNoBudget++;
            }
            return false;
        }
        rebuildsLeft--;
        return true;
    }

    /** Spends one of this frame's first builds. A refusal leaves a hole for a frame or two. */
    private boolean mayBuild() {
        if (buildsLeft <= 0) {
            if (TRACE) {
                traceNoBudget++;
            }
            return false;
        }
        buildsLeft--;
        return true;
    }

    /**
     * Hands a bare sheet's texture back while keeping the sheet itself.
     *
     * <p>The record has to stay - it is the memory of having looked here and found nothing, and
     * without it the square is examined again every frame. The texture does not: at the widest
     * zoom nearly three hundred squares in view are over ground nobody has surveyed, and each was
     * holding sixty-six kilobytes of blank picture and a slot in a cache of three hundred and
     * eighty-four. The blanks were crowding out the ground.
     */
    private static void giveBackTexture(Sheet sheet) {
        release(sheet);
        sheet.texture = null;
        sheet.id = null;
    }

    /**
     * Releases the textures furthest from the eye once too many are alive.
     *
     * <p>Works from a copy of the <em>keys</em>, never from the entry set. This map is a fastutil
     * one, and its entry set hands out a single recycled Entry object as it iterates — so copying
     * the entries into a list gives a list of references to one entry that is invalid the moment the
     * iteration moves on. That crashed the game a few minutes into play, once enough squares had
     * accumulated to push past the cap for the first time.
     *
     * <p>Worth stating plainly, because it looked like a drop-in change when the map was swapped for
     * speed: a primitive-keyed map is not a {@code HashMap} with the boxing removed. It trades those
     * allocations for rules about what its views may be used for.
     *
     * <p>Boxing here is deliberate and harmless — this runs only when the cap is exceeded, which is
     * rare, unlike the lookups the map was made primitive for.
     */
    /**
     * Room for one pass of {@link #evictSurplus}, kept between frames rather than allocated per call.
     */
    private long[] evictKeys = new long[0];
    private long[] evictOrder = new long[0];

    /** Bits of {@link #evictOrder} holding the index; the rest holds the age it sorts on. */
    private static final int EVICT_INDEX_BITS = 20;

    private void evictSurplus() {
        int size = sheets.size();
        int surplus = size - MAX_LIVE_TEXTURES;
        if (surplus <= 0) {
            return;
        }

        if (evictKeys.length < size) {
            evictKeys = new long[size];
            evictOrder = new long[size];
        }

        // Age and index packed into one long, so the sort is a primitive sort with no comparator and
        // no boxing. It used to be an array of boxed keys sorted by a comparator that looked each one
        // up in the map — several thousand hash lookups and an object per sheet, on exactly the
        // frames that were already the most expensive ones, since this only runs when a square was
        // added. Each sheet's age is now read once.
        LongIterator keys = sheets.keySet().iterator();
        int at = 0;
        while (keys.hasNext() && at < size) {
            long key = keys.nextLong();
            evictKeys[at] = key;
            evictOrder[at] = (sheets.get(key).lastDrawn << EVICT_INDEX_BITS) | at;
            at++;
        }
        Arrays.sort(evictOrder, 0, at);

        // Collected first, removed after. Taking entries out of a fastutil map while walking its own
        // view is the trap that crashed this class once already.
        for (int i = 0; i < at && surplus > 0; i++) {
            long key = evictKeys[(int) (evictOrder[i] & ((1L << EVICT_INDEX_BITS) - 1))];
            Sheet sheet = sheets.get(key);
            if (sheet.lastDrawn == drawClock) {
                // In view this frame, and everything after it is younger still.
                break;
            }
            release(sheet);
            sheets.remove(key);
            surplus--;
            if (TRACE) {
                traceEvicted++;
            }
        }
    }

    private static void release(Sheet sheet) {
        if (sheet.id != null) {
            Minecraft.getInstance().getTextureManager().release(sheet.id);
        }
        if (sheet.texture != null) {
            sheet.texture.close();
        }
    }

    /**
     * How much of the shading range the landform scale gets, and how much the block scale gets.
     *
     * <p>Both, not one. The wide comparison alone reads the shape of a hill and erases the surface
     * texture of it, which is the difference the map mod comparison showed most plainly — their
     * beaches show every one-block terrace as a fine line and ours had smoothed them all away. The
     * narrow comparison alone is the noise this whole scheme was written to escape. Added together,
     * the wide one carries the form and the narrow one puts the grain back on it.
     */
    private static final float RELIEF_MACRO = 0.42f;
    private static final float RELIEF_MICRO = 0.14f;

    /**
     * The slope, in blocks of rise per block of run, that uses up most of that range.
     *
     * <p>Anything steeper is compressed rather than clipped, so a cliff and a mountainside stay
     * distinguishable instead of both saturating to white.
     */
    private static final float RELIEF_SOFTNESS = 0.28f;

    /**
     * Lights the ground from the north-west and shades it by how the surface leans.
     *
     * <p>North-west because that is the cartographic convention, and because a single axis cannot
     * show a ridge that runs along it — a north–south spur lit from due north is uniformly flat, and
     * that is exactly the ground a gun position gets hidden behind.
     */
    /**
     * Applies relief and the overall dimming to a colour that is already resolved.
     *
     * <p>The palette lookup, the biome tint and the water no longer happen here. They are decided
     * once per column, in {@link TerrainMips}, because a reduced level averages <em>finished</em>
     * colours — half sand and half water is not a palette entry, and there is no id to store for it.
     * Having the two ends work it out separately would mean the map changed appearance at whichever
     * zoom step the level changes, which is the recurring fault this project keeps paying for.
     *
     * <p>What stays here is everything that depends on the neighbourhood or on the level: the lean
     * of the ground, and how far the whole map is pulled down behind the symbols.
     */
    private static int shadeCell(BakeWorkspace ws, int cell, int stride) {
        int packed = ws.fieldColour[cell];
        int b = (packed >> 16) & 0xFF;
        int g = (packed >> 8) & 0xFF;
        int r = packed & 0xFF;

        float murk = ws.fieldMurk[cell];
        float reliefScale = murk <= 0f ? 1f
                : WATER_RELIEF_SHALLOW - (WATER_RELIEF_SHALLOW - WATER_RELIEF_DEEP) * murk;

        float relief = 1f + (reliefOf(ws, cell, stride) - 1f) * reliefScale;

        float lit = TERRAIN_DIM * relief;
        lit += (1f - lit) * ws.fieldHazard[cell];
        return 0xFF000000 | (light(b, lit) << 16) | (light(g, lit) << 8) | light(r, lit);
    }

    /**
     * How much brighter or darker this column is than flat ground, from the lean of the surface.
     *
     * <p>Two scales added together — see {@link #RELIEF_MACRO}. Each scale takes one sample to the
     * north and one to the west and averages them, so a slope facing the light squarely is no
     * brighter than it should be. Where a neighbour has not been surveyed that axis is left out
     * rather than counted as flat, which would drag every edge of the known ground towards grey.
     */
    private static float reliefOf(BakeWorkspace ws, int cell, int stride) {
        float macro = slopeOf(ws, cell, RELIEF_MACRO_RUN, stride * RELIEF_MACRO_RUN);
        float micro = slopeOf(ws, cell, RELIEF_MICRO_RUN, stride * RELIEF_MICRO_RUN);
        // The block-scale term is dropped under the relief filter. It exists to put the grain of the
        // surface back on — every one-block terrace as a fine line — and grain is the whole of what
        // a canopy contributes to a height field. On a sheet that reports bands rather than blocks,
        // that term draws the trees back on after the banding has just taken them off.
        float micros = TerrainMips.filter() == TerrainMips.Filter.RELIEF ? 0f : RELIEF_MICRO;
        return 1f + RELIEF_MACRO * macro + micros * micro;
    }

    private static float slopeOf(BakeWorkspace ws, int cell, int samples, int run) {
        short here = ws.fieldFloor[cell];
        short north = ws.fieldFloor[cell - samples * FIELD];
        short west = ws.fieldFloor[cell - samples];
        short northWest = ws.fieldFloor[cell - samples * FIELD - samples];

        float rise = 0f;
        float weights = 0f;
        if (north != TerrainTile.NO_DATA) {
            rise += (here - north);
            weights += 1.0f;
        }
        if (west != TerrainTile.NO_DATA) {
            rise += (here - west);
            weights += 1.0f;
        }
        if (northWest != TerrainTile.NO_DATA) {
            rise += (here - northWest) * 0.7071f;
            weights += 0.7071f;
        }
        if (weights == 0f) {
            return 0f;
        }

        return squash((rise / weights) / ((float) run * RELIEF_SOFTNESS));
    }

    /**
     * The squashing curve, read from a table rather than computed.
     *
     * <p>This was {@code Math.tanh}, and it was the whole cost of drawing a square. The shading pass
     * calls it twice for every texel — nearly thirty-four thousand times per square — and a hyperbolic
     * tangent is a transcendental in double precision, tens of nanoseconds each. Timed by phase, the
     * shading held about 2.6 of the 3 milliseconds a build took, and this line held nearly all of the
     * shading. Nothing else in that loop is more than arithmetic on arrays.
     *
     * <p>Same curve, not a substitute for it. A thousand steps across the range where a hyperbolic
     * tangent still changes is a rounding error of four thousandths, and the relief it feeds is
     * scaled by a third and then by the dimming, so the worst any texel can move is a fifth of one
     * channel value. Beyond the ends it is flat to within seven ten-thousandths of one, so clamping
     * there costs nothing either.
     */
    private static final int SQUASH_STEPS = 1024;

    /** Past this the curve is flat enough to call flat. */
    private static final float SQUASH_LIMIT = 4f;

    private static final float SQUASH_SCALE = SQUASH_STEPS / (2f * SQUASH_LIMIT);

    private static final float[] SQUASH = new float[SQUASH_STEPS + 1];

    static {
        for (int i = 0; i <= SQUASH_STEPS; i++) {
            SQUASH[i] = (float) Math.tanh(i / SQUASH_SCALE - SQUASH_LIMIT);
        }
    }

    static float squash(float x) {
        if (x <= -SQUASH_LIMIT) {
            return -1f;
        }
        if (x >= SQUASH_LIMIT) {
            return 1f;
        }
        return SQUASH[(int) ((x + SQUASH_LIMIT) * SQUASH_SCALE + 0.5f)];
    }

    /**
     * How much of the relief survives under water, at the surface and at full depth.
     *
     * <p>Separate from the colour, and the more important of the two. Blending towards blue hides
     * the sea bed's <em>colour</em> but leaves its <em>shading</em> at full contrast, and it was the
     * shading that shouted — a shoal read louder than a hillside on land.
     *
     * <p>Only the finest level carries the depth needed to apply this. Beyond it, at four blocks a
     * texel and more, a sea bed's relief is a whisper and the averaged colour carries the water on
     * its own.
     */
    private static final float WATER_RELIEF_SHALLOW = 0.45f;
    private static final float WATER_RELIEF_DEEP = 0.12f;

    /**
     * How much of the map palette's brightness survives.
     *
     * <p>The one number behind the look of the ground. Lower it and the terrain recedes further
     * behind the symbols; raise it towards one and the map becomes the game's own map item.
     */
    private static final float TERRAIN_DIM = 0.78f;

    /** A channel taken to however lit this cell ended up being. */
    private static int light(int channel, float lit) {
        return Math.max(0, Math.min(255, Math.round(channel * lit)));
    }

    public void close() {
        // Drain any in-flight bake results so the background threads do not write to evicted sheets.
        BakeResult r;
        while ((r = finishedBakes.poll()) != null) {
            // discard
        }
        bakingKeys.clear();

        for (Sheet sheet : sheets.values()) {
            release(sheet);
        }
        sheets.clear();
    }
}
