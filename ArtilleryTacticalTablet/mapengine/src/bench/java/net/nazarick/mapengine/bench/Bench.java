package net.nazarick.mapengine.bench;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;
import net.nazarick.mapengine.lod.Pyramid;
import net.nazarick.mapengine.overview.WorldOverview;
import net.nazarick.mapengine.raster.Rasterizer;
import net.nazarick.mapengine.storage.RegionFile;
import net.nazarick.mapengine.storage.RegionShardFile;
import net.nazarick.mapengine.storage.RegionStore;
import net.nazarick.mapengine.core.RegionKey;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Times the engine and writes pictures of what it produced.
 *
 * <p>Every map fault this project has spent a session on was found by launching Minecraft, waiting a
 * minute and judging a screen. Here a claim about speed is a number printed in seconds, and a claim
 * about how the map looks is a PNG on disk.
 */
public final class Bench {
    // Raised from 10s once real disk round trips joined the suite (region file, shard file, cold
    // open), then from 30s once the world-overview phase's own setup — generating 400 synthetic
    // regions, not the read being measured — joined it. Still a fraction of the ~60s a single
    // Minecraft launch cost to check one thing.
    private static final long SUITE_BUDGET_MS = 60_000L;
    private static final long T1_COLD_OPEN_MS = 250L;
    private static final long T3_RASTER_MS = 8L;
    private static final long T5_VIEW_RAM_MB = 20L;
    private static final long T8_OVERVIEW_OPEN_MS = 100L;

    private static final List<String> RESULTS = new ArrayList<>();
    private static boolean anyFailed;

    public static void main(String[] args) throws Exception {
        long started = System.nanoTime();

        Path out = Path.of(args.length > 0 ? args[0] : "build/mapengine");
        Files.createDirectories(out);

        System.out.println("map engine bench - synthetic terrain, no Minecraft");
        System.out.println();

        columnGeneration();
        writePreview(out);
        regionFileRoundTrip();
        pyramidCorrectness(out);
        rasterizerLook(out);
        singleRegionIoCost();
        coldOpen();
        shardColdOpen();
        worldOverviewInstantOpen();
        partialShardFlush();
        resurveyNeverErasesGround();
        viewMemoryFootprint();

        long suiteMs = (System.nanoTime() - started) / 1_000_000L;
        report("T7  suite runtime", suiteMs, SUITE_BUDGET_MS, "ms");

        System.out.println();
        for (String line : RESULTS) {
            System.out.println(line);
        }

        System.out.println();
        if (anyFailed) {
            throw new AssertionError("a target was missed - see the FAIL lines above");
        }
        System.out.println("all targets met");
    }

    private static void columnGeneration() {
        SyntheticWorld world = new SyntheticWorld(1234L);
        ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);

        for (int i = 0; i < 3; i++) {
            world.fill(i * Region.BLOCKS, 0, columns);
        }

        int runs = 8;
        long best = Long.MAX_VALUE;
        for (int i = 0; i < runs; i++) {
            columns.clear();
            long t0 = System.nanoTime();
            world.fill(i * Region.BLOCKS, 512, columns);
            best = Math.min(best, System.nanoTime() - t0);
        }

        double ms = best / 1_000_000.0;
        double columnsPerSecond = columns.columns() / (best / 1_000_000_000.0);
        System.out.printf("source: one %d x %d region generated in %.2f ms (%.1f M columns/s)%n",
                Region.BLOCKS, Region.BLOCKS, ms, columnsPerSecond / 1_000_000.0);
    }

    private static void writePreview(Path out) throws Exception {
        writeRegionPng(out.resolve("phase0-region.png"), new SyntheticWorld(1234L));
        writeRegionPng(out.resolve("phase0-region-gaps.png"), new SyntheticWorld(1234L, 0.42));
        System.out.println("preview: wrote " + out.resolve("phase0-region.png")
                + " and phase0-region-gaps.png");
    }

    /** T4: one file per region, and what goes in must come back out byte-for-byte. */
    private static void regionFileRoundTrip() throws Exception {
        Path root = Files.createTempDirectory("mapengine-regionfile");
        SyntheticWorld world = new SyntheticWorld(4242L);

        int[][] where = {{0, 0}, {5, 9}, {-1, -1}, {-33, 40}, {31, -32}};
        for (int[] r : where) {
            ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);
            world.fill(r[0] * Region.BLOCKS, r[1] * Region.BLOCKS, columns);
            RegionFile.write(root, r[0], r[1], columns);
        }

        for (int[] r : where) {
            ColumnBuffer want = new ColumnBuffer(Region.BLOCKS);
            world.fill(r[0] * Region.BLOCKS, r[1] * Region.BLOCKS, want);

            ColumnBuffer back = RegionFile.read(root, r[0], r[1]);
            if (back == null) {
                throw new AssertionError("FAILED: region " + r[0] + "," + r[1] + " did not come back");
            }
            if (!java.util.Arrays.equals(back.height, want.height)
                    || !java.util.Arrays.equals(back.block, want.block)
                    || !java.util.Arrays.equals(back.biome, want.biome)
                    || !java.util.Arrays.equals(back.depth, want.depth)) {
                throw new AssertionError("FAILED: region " + r[0] + "," + r[1] + " came back different");
            }
        }

        long files = Files.walk(root).filter(Files::isRegularFile).count();
        if (files != where.length) {
            throw new AssertionError("FAILED: " + files + " files on disk for " + where.length + " regions");
        }
        System.out.printf("region file: OK  %d regions written and read back byte-for-byte, %d files on disk%n",
                where.length, files);
    }

    /** Builds a pyramid from a synthetic region and writes a contact sheet of every level. */
    private static void pyramidCorrectness(Path out) throws Exception {
        SyntheticWorld world = new SyntheticWorld(77L);
        Region region = new Region(0, 0);
        ColumnBuffer level0 = new ColumnBuffer(Region.BLOCKS);
        world.fill(0, 0, level0);
        region.setLevel(0, level0);
        Pyramid.build(region);

        DebugStyle style = new DebugStyle();
        BufferedImage sheet = new BufferedImage(Region.BLOCKS * 2, 256, BufferedImage.TYPE_INT_ARGB);
        int x0 = 0;
        for (int level = 0; level < Region.LEVELS; level++) {
            ColumnBuffer columns = region.level(level);
            if (columns == null) {
                throw new AssertionError("FAILED: pyramid level " + level + " was never built");
            }
            int w = columns.width;
            for (int z = 0; z < w && z < 256; z++) {
                for (int x = 0; x < w && x0 + x < sheet.getWidth(); x++) {
                    sheet.setRGB(x0 + x, z, DebugStyle.colourOf(style, columns, columns.index(x, z)));
                }
            }
            x0 += w + 4;
        }
        ImageIO.write(sheet, "png", new File(out.resolve("phase1-pyramid.png").toString()));
        System.out.println("pyramid: OK  all " + Region.LEVELS + " levels built, wrote "
                + out.resolve("phase1-pyramid.png"));
    }

    /**
     * T3: real hillshade, not the flat-colour version tried earlier this session and reversed once
     * the user clarified they wanted the old shaded look kept. Rasterizes one full 512x512 region at
     * level 0 with {@link Rasterizer}, writes it as a PNG so the shading can actually be looked at
     * rather than trusted from the numbers alone, and times the pass against the 8&nbsp;ms budget a
     * live pan/zoom needs to stay under to avoid a stutter.
     */
    private static void rasterizerLook(Path out) throws Exception {
        SyntheticWorld world = new SyntheticWorld(9001L);
        ColumnBuffer level0 = new ColumnBuffer(Region.BLOCKS);
        world.fill(0, 0, level0);
        DebugStyle style = new DebugStyle();

        int[] pixels = null;
        long best = Long.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            long t0 = System.nanoTime();
            pixels = Rasterizer.rasterize(level0, style, 0);
            best = Math.min(best, System.nanoTime() - t0);
        }
        long ms = best / 1_000_000L;

        BufferedImage image = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, pixels, 0, Region.BLOCKS);
        ImageIO.write(image, "png", new File(out.resolve("phase2-rasterizer.png").toString()));

        System.out.printf("rasterizer: one %d x %d region shaded in %d ms, wrote %s%n",
                Region.BLOCKS, Region.BLOCKS, ms, out.resolve("phase2-rasterizer.png"));
        report("T3  rasterize (512x512)", ms, T3_RASTER_MS, "ms");

        // T3': the same region, split across the machine's own cores. Shading is exact per row (see
        // Rasterizer.rasterizeParallel's own doc) — this is real parallelism, not an approximation,
        // and it is the practical way a live pan/zoom actually stays under an 8ms frame budget once
        // single-threaded cost hits a wall that further micro-optimization cannot move.
        int workers = Math.max(2, Runtime.getRuntime().availableProcessors());
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(workers);
        try {
            long bestParallel = Long.MAX_VALUE;
            for (int i = 0; i < 5; i++) {
                long t0 = System.nanoTime();
                Rasterizer.rasterizeParallel(level0, style, 0, pool, workers);
                bestParallel = Math.min(bestParallel, System.nanoTime() - t0);
            }
            long msParallel = bestParallel / 1_000_000L;
            System.out.printf("rasterizer (parallel, %d workers): one %d x %d region shaded in %d ms%n",
                    workers, Region.BLOCKS, Region.BLOCKS, msParallel);
            report("T3' rasterize parallel", msParallel, T3_RASTER_MS, "ms");
        } finally {
            pool.shutdown();
        }

        int[] topoPixels = Rasterizer.rasterizeTopo(level0);
        BufferedImage topoImage = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        topoImage.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, topoPixels, 0, Region.BLOCKS);
        ImageIO.write(topoImage, "png", new File(out.resolve("phase2-topo.png").toString()));
        System.out.println("topo: wrote " + out.resolve("phase2-topo.png"));

        // A hand-placed scene (sea, plains, hill, valley, mountain) instead of the deliberately
        // rough fractal terrain above — legible by eye, so a filter's own look can be judged against
        // what the ground is actually supposed to be, not argued about over noisy hills.
        ColumnBuffer scene = new ColumnBuffer(Region.BLOCKS);
        new SceneWorld().fill(0, 0, scene);

        int[] sceneShaded = Rasterizer.rasterize(scene, style, 0);
        BufferedImage sceneShadedImage = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        sceneShadedImage.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, sceneShaded, 0, Region.BLOCKS);
        ImageIO.write(sceneShadedImage, "png", new File(out.resolve("phase2-scene-shaded.png").toString()));

        int[] sceneTopo = Rasterizer.rasterizeTopo(scene);
        BufferedImage sceneTopoImage = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        sceneTopoImage.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, sceneTopo, 0, Region.BLOCKS);
        ImageIO.write(sceneTopoImage, "png", new File(out.resolve("phase2-scene-topo.png").toString()));

        int[] sceneOverlay = Rasterizer.rasterizeTopoOverlay(scene, style);
        BufferedImage sceneOverlayImage = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        sceneOverlayImage.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, sceneOverlay, 0, Region.BLOCKS);
        ImageIO.write(sceneOverlayImage, "png", new File(out.resolve("phase2-scene-topo-overlay.png").toString()));

        int[] sceneHypso = Rasterizer.rasterizeHypsometric(scene);
        BufferedImage sceneHypsoImage = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        sceneHypsoImage.setRGB(0, 0, Region.BLOCKS, Region.BLOCKS, sceneHypso, 0, Region.BLOCKS);
        ImageIO.write(sceneHypsoImage, "png", new File(out.resolve("phase2-scene-hypso.png").toString()));

        System.out.println("scene: wrote " + out.resolve("phase2-scene-shaded.png")
                + ", phase2-scene-topo.png, phase2-scene-topo-overlay.png and phase2-scene-hypso.png");
    }

    /**
     * Isolates one region's cost, single-threaded, no queue, no polling loop — so a slow
     * {@link #coldOpen} number can be pinned on disk I/O, decompression, the pyramid, or the
     * benchmark's own polling rather than guessed at.
     */
    private static void singleRegionIoCost() throws Exception {
        Path root = Files.createTempDirectory("mapengine-single");
        SyntheticWorld world = new SyntheticWorld(55L);
        ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);
        world.fill(0, 0, columns);

        long t0 = System.nanoTime();
        RegionFile.write(root, 0, 0, columns);
        long writeMs = (System.nanoTime() - t0) / 1_000_000L;

        // A second file, read cold — not the one just written, so the OS page cache from the write
        // above cannot be quietly answering the read.
        world.fill(Region.BLOCKS, 0, columns);
        RegionFile.write(root, 1, 0, columns);

        long t1 = System.nanoTime();
        ColumnBuffer back = RegionFile.read(root, 1, 0);
        long readMs = (System.nanoTime() - t1) / 1_000_000L;

        long t2 = System.nanoTime();
        Region region = new Region(1, 0);
        region.setLevel(0, back);
        Pyramid.build(region);
        long pyramidMs = (System.nanoTime() - t2) / 1_000_000L;

        // What a fresh survey pays extra now that it also feeds WorldOverview — background, off the
        // player's own load, but real CPU that competes with everything else on the I/O pool.
        long t3 = System.nanoTime();
        new WorldOverview().merge(1, 0, back);
        long overviewMergeMs = (System.nanoTime() - t3) / 1_000_000L;

        System.out.printf("single region: write %d ms, read %d ms, pyramid %d ms, overview merge %d ms (root=%s)%n",
                writeMs, readMs, pyramidMs, overviewMergeMs, root);
    }

    /**
     * T1: what a restart actually costs, through the real {@link RegionStore} — not a direct call
     * into {@link RegionShardFile}. Pre-fills disk exactly as a previous session leaves it once a
     * shard has fully surveyed (one {@link RegionShardFile}, the state {@link RegionStore}'s own
     * shard-buildup logic produces on its own once every region in a shard has been seen), builds a
     * fresh store — the state right after the game boots — and times every region an 8x8, ~4000&nbsp;m
     * view needs coming back through {@link RegionStore#get} <em>and</em> reaching the one pyramid
     * level that view actually draws from, via {@link Pyramid#ensureLevel} — the level a real
     * renderer would ask for at this span ({@link Region#levelFor}), not all seven. Building levels
     * this view never reads was the gap between the shard file's own ~200&nbsp;ms and what an earlier
     * version of this benchmark reported before that eager build was found and removed.
     */
    private static void coldOpen() throws Exception {
        Path root = Files.createTempDirectory("mapengine-coldopen");
        SyntheticWorld world = new SyntheticWorld(99L);

        int span = RegionShardFile.SIDE; // 8 — one full shard, matching a ~4000 m view exactly
        Map<Long, ColumnBuffer> toWrite = new java.util.HashMap<>();
        for (int rx = 0; rx < span; rx++) {
            for (int rz = 0; rz < span; rz++) {
                ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);
                world.fill(rx * Region.BLOCKS, rz * Region.BLOCKS, columns);
                toWrite.put(RegionKey.of(rx, rz), columns);
            }
        }
        RegionShardFile.write(root, 0, 0, toWrite);

        // What a real screen at this span actually draws from — the same choice MapPanel's own
        // renderer would make, not the finest level this engine happens to have.
        double blocksPerPixel = (span * Region.BLOCKS) / 800.0;
        int targetLevel = Region.levelFor(blocksPerPixel);

        RegionStore store = new RegionStore(root, world, 256, 16);
        try {
            long start = System.nanoTime();
            long deadline = start + 5_000_000_000L;
            while (System.nanoTime() < deadline) {
                store.drain();
                boolean allLoaded = true;
                // Every incomplete region's build is dispatched every tick, not just the first one
                // found — ensureLevel is a cheap no-op once a build is already in flight or done, and
                // stopping at the first gap would serialize what the I/O pool exists to parallelize.
                for (int rx = 0; rx < span; rx++) {
                    for (int rz = 0; rz < span; rz++) {
                        Region region = store.get(rx, rz);
                        if (region == null) {
                            allLoaded = false;
                            continue;
                        }
                        if (!region.hasLevel(targetLevel)) {
                            store.ensureLevel(rx, rz, targetLevel);
                            allLoaded = false;
                        }
                    }
                }
                if (allLoaded) {
                    break;
                }
                // A real sleep, not a spin: this loop and the store's own I/O threads are competing
                // for the same cores, and busy-spinning here was measured taking cycles the I/O
                // threads needed, inflating the very number this method exists to report honestly.
                Thread.sleep(1);
            }
            long ms = (System.nanoTime() - start) / 1_000_000L;
            System.out.printf("cold open: %d regions (%dx%d) loaded and level %d built in %d ms, %d live in store%n",
                    span * span, span, span, targetLevel, ms, store.liveCount());
            report("T1  cold open (4000m)", ms, T1_COLD_OPEN_MS, "ms");
        } finally {
            store.shutdown();
        }
    }

    /**
     * The fix {@link #coldOpen} pointed at: the same 64 regions, filed as one
     * {@link RegionShardFile} instead of 64 {@link RegionFile}s, read back in one file open.
     */
    private static void shardColdOpen() throws Exception {
        Path root = Files.createTempDirectory("mapengine-shardcoldopen");
        SyntheticWorld world = new SyntheticWorld(99L);

        int side = RegionShardFile.SIDE; // 8 — matches coldOpen's 8x8 view exactly
        Map<Long, ColumnBuffer> toWrite = new java.util.HashMap<>();
        for (int rx = 0; rx < side; rx++) {
            for (int rz = 0; rz < side; rz++) {
                ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);
                world.fill(rx * Region.BLOCKS, rz * Region.BLOCKS, columns);
                toWrite.put(RegionKey.of(rx, rz), columns);
            }
        }
        RegionShardFile.write(root, 0, 0, toWrite);

        long t0 = System.nanoTime();
        Map<Long, ColumnBuffer> back = RegionShardFile.read(root, 0, 0);
        long ms = (System.nanoTime() - t0) / 1_000_000L;

        if (back.size() != side * side) {
            throw new AssertionError("FAILED: shard came back with " + back.size()
                    + " regions, expected " + (side * side));
        }
        System.out.printf("shard cold open: %d regions (%dx%d), one file, loaded in %d ms%n",
                back.size(), side, side, ms);
        report("T1' shard cold open", ms, T1_COLD_OPEN_MS, "ms");
    }

    /**
     * T8: the user's actual acceptance test — "open the map and see the whole area already explored,
     * no waiting." Simulates a world explored across many past sessions (20x20 regions, ~10&nbsp;km
     * across — far more than fits in one shard, the point being that this must stay instant
     * regardless of how much ground has ever been surveyed), merges every one of those regions into
     * a {@link WorldOverview} exactly as {@link RegionStore} would have while it was played, writes
     * that as the one file a boot reads, then times reading it back cold and confirms it answers for
     * ground scattered across the whole area — without loading a single {@link RegionFile}.
     */
    private static void worldOverviewInstantOpen() throws Exception {
        Path root = Files.createTempDirectory("mapengine-overview");
        SyntheticWorld world = new SyntheticWorld(2024L);

        int span = 20; // 20x20 regions, ~10240 blocks across — a big, many-session exploration history
        WorldOverview building = new WorldOverview();
        for (int rx = 0; rx < span; rx++) {
            for (int rz = 0; rz < span; rz++) {
                ColumnBuffer level0 = new ColumnBuffer(Region.BLOCKS);
                world.fill(rx * Region.BLOCKS, rz * Region.BLOCKS, level0);
                building.merge(rx, rz, level0);
            }
        }
        Path file = root.resolve("overview.dat");
        building.write(file);

        long t0 = System.nanoTime();
        WorldOverview back = WorldOverview.read(file);
        long ms = (System.nanoTime() - t0) / 1_000_000L;

        int expectedSamples = span * span * WorldOverview.BOXES_PER_REGION * WorldOverview.BOXES_PER_REGION;
        if (back.size() == 0 || back.size() > expectedSamples) {
            throw new AssertionError("FAILED: overview came back with " + back.size()
                    + " samples, expected at most " + expectedSamples + " and more than 0");
        }
        // Ground scattered across the whole 10 km history, never loaded as a Region this session —
        // the property this benchmark exists to prove.
        int hits = 0;
        for (int rx = 0; rx < span; rx += 3) {
            for (int rz = 0; rz < span; rz++) {
                int blockX = rx * Region.BLOCKS + Region.BLOCKS / 2;
                int blockZ = rz * Region.BLOCKS + Region.BLOCKS / 2;
                if (back.sampleAt(blockX, blockZ) != null) {
                    hits++;
                }
            }
        }
        if (hits == 0) {
            throw new AssertionError("FAILED: overview answered nothing for ground it was built from");
        }
        System.out.printf("world overview: %d regions (%dx%d, ~%d m across) merged, %d samples, "
                        + "read back in %d ms, %d spot checks answered%n",
                span * span, span, span, span * Region.BLOCKS, back.size(), ms, hits);
        report("T8  world overview open", ms, T8_OVERVIEW_OPEN_MS, "ms");
    }

    /**
     * A real player never fills a whole 8x8 shard in one sitting — the gap this benchmark exists to
     * catch is a store that only ever writes a shard file once every one of its 64 regions is known,
     * which for realistic, incremental play means the shard-read cold-open win almost never actually
     * applies. Surveys a handful of regions, flushes, surveys a few more, flushes again, and checks
     * the shard file on disk ends up with the union of both batches — proof a partial flush merges
     * with what is already there rather than losing it.
     */
    private static void partialShardFlush() throws Exception {
        Path root = Files.createTempDirectory("mapengine-partialshard");
        SyntheticWorld world = new SyntheticWorld(4040L);
        RegionStore store = new RegionStore(root, world, 256, 4);
        try {
            int[][] firstBatch = {{0, 0}, {1, 0}, {2, 0}, {0, 1}};
            surveyAndDrain(store, firstBatch);
            store.flushShards();
            Map<Long, ColumnBuffer> afterFirst = waitForShardCount(root, firstBatch.length);
            if (afterFirst.size() != firstBatch.length) {
                throw new AssertionError("FAILED: partial flush wrote " + afterFirst.size()
                        + " regions, expected " + firstBatch.length);
            }

            int[][] secondBatch = {{3, 0}, {0, 2}, {4, 4}};
            surveyAndDrain(store, secondBatch);
            store.flushShards();
            int expected = firstBatch.length + secondBatch.length;
            Map<Long, ColumnBuffer> afterSecond = waitForShardCount(root, expected);
            if (afterSecond.size() != expected) {
                throw new AssertionError("FAILED: second flush left " + afterSecond.size()
                        + " regions on disk, expected " + expected
                        + " — the first batch was lost instead of merged");
            }
            System.out.printf("partial shard flush: OK  %d then +%d regions merged, %d on disk after both flushes%n",
                    firstBatch.length, secondBatch.length, afterSecond.size());
        } finally {
            store.shutdown();
        }
    }

    /**
     * The regression this session actually shipped once: {@code RegionStore.resurvey} exists so a
     * client-chunk source gets asked again as more of the world loads, but the first version of it
     * replaced a region's data outright instead of merging into it — so resurveying ground the
     * player had already walked away from (source now correctly answers "nothing here", since
     * nothing is in range any more) silently erased what had already been surveyed. Proven here with
     * a source that behaves exactly that way: real data on the first call for a region, nothing on
     * every call after — the "player moved on" case in miniature.
     */
    private static void resurveyNeverErasesGround() throws Exception {
        Path root = Files.createTempDirectory("mapengine-resurvey");
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        SyntheticWorld real = new SyntheticWorld(5050L);
        net.nazarick.mapengine.core.ColumnSource fadeAway = (blockX, blockZ, into) -> {
            if (calls.getAndIncrement() == 0) {
                return real.fill(blockX, blockZ, into);
            }
            return false; // "player walked away" — the source has nothing to say any more
        };

        RegionStore store = new RegionStore(root, fadeAway, 256, 2);
        try {
            surveyAndDrain(store, new int[][]{{0, 0}});
            Region firstLoad = store.get(0, 0);
            if (firstLoad == null || firstLoad.level(0) == null || firstLoad.level(0).isEmpty()) {
                throw new AssertionError("FAILED: first load produced no ground to test against");
            }
            long knownBefore = countKnown(firstLoad.level(0));

            store.resurvey(0, 0);
            long deadline = System.nanoTime() + 3_000_000_000L;
            Region after = null;
            while (System.nanoTime() < deadline) {
                store.drain();
                after = store.get(0, 0);
                if (after != null) {
                    break;
                }
                Thread.sleep(2);
            }
            if (after == null || after.level(0) == null) {
                throw new AssertionError("FAILED: resurvey erased the region entirely "
                        + "(source answered nothing, and the old ground went with it)");
            }
            long knownAfter = countKnown(after.level(0));
            if (knownAfter != knownBefore) {
                throw new AssertionError("FAILED: resurvey changed known-column count from "
                        + knownBefore + " to " + knownAfter + " when the source found nothing new");
            }
            System.out.printf("resurvey merge: OK  %d known columns survived a resurvey the source "
                    + "answered nothing to (%d source calls)%n", knownAfter, calls.get());
        } finally {
            store.shutdown();
        }
    }

    private static long countKnown(ColumnBuffer columns) {
        long known = 0;
        for (short h : columns.height) {
            if (h != ColumnBuffer.NO_DATA) {
                known++;
            }
        }
        return known;
    }

    private static void surveyAndDrain(RegionStore store, int[][] regions) throws InterruptedException {
        long deadline = System.nanoTime() + 5_000_000_000L;
        boolean allLoaded = false;
        while (!allLoaded && System.nanoTime() < deadline) {
            store.drain();
            allLoaded = true;
            for (int[] r : regions) {
                if (store.get(r[0], r[1]) == null) {
                    allLoaded = false;
                }
            }
            if (!allLoaded) {
                Thread.sleep(1);
            }
        }
        if (!allLoaded) {
            throw new AssertionError("FAILED: regions did not finish loading within the deadline");
        }
    }

    /**
     * Polls the shard file until it holds at least {@code expectedCount} regions or a deadline
     * passes — a flush's write is async, so a fixed sleep here would either be too short (a flaky
     * failure on a slow run) or wastefully long on a fast one.
     */
    private static Map<Long, ColumnBuffer> waitForShardCount(Path root, int expectedCount) throws InterruptedException {
        long deadline = System.nanoTime() + 5_000_000_000L;
        Map<Long, ColumnBuffer> result = RegionShardFile.read(root, 0, 0);
        while (result.size() < expectedCount && System.nanoTime() < deadline) {
            Thread.sleep(5);
            result = RegionShardFile.read(root, 0, 0);
        }
        return result;
    }

    /**
     * T5: RAM for a 4000&nbsp;m (8x8 region) view, measured against the real {@link RegionStore} —
     * not arithmetic on array sizes, because the first version of this check was exactly that
     * arithmetic and it silently assumed a level-0 buffer gets freed once the coarser level a wide
     * view actually draws from is built. Nothing in {@link RegionStore} did that: level 0 was kept
     * forever alongside every built level, which this check caught at 119&nbsp;MB against a
     * 20&nbsp;MB budget — six times over, for the exact reason the original engine plan called out
     * (holding level 0 for a whole wide view is what the stored pyramid exists to avoid). Fixed by
     * adding {@link Region#retainedBytes()} and {@link RegionStore#trimToLevel}, and this check now
     * exercises both: load the view, build the level it actually needs, trim away level 0, then sum
     * what is really still held — followed by one more {@link RegionStore#ensureLevel} call to prove
     * the trimmed region can still be rebuilt from disk rather than staying broken.
     */
    private static void viewMemoryFootprint() throws Exception {
        Path root = Files.createTempDirectory("mapengine-viewmemory");
        SyntheticWorld world = new SyntheticWorld(3030L);
        int span = RegionShardFile.SIDE;
        double blocksPerPixel = (span * Region.BLOCKS) / 800.0;
        int targetLevel = Region.levelFor(blocksPerPixel);

        RegionStore store = new RegionStore(root, world, 256, 8);
        try {
            for (int rx = 0; rx < span; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    store.get(rx, rz);
                }
            }
            waitUntilAllLoaded(store, span);
            for (int rx = 0; rx < span; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    store.ensureLevel(rx, rz, targetLevel);
                }
            }
            waitUntilAllHaveLevel(store, span, targetLevel);

            for (int rx = 0; rx < span; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    store.trimToLevel(rx, rz, targetLevel);
                }
            }

            long totalBytes = 0;
            for (int rx = 0; rx < span; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    totalBytes += store.get(rx, rz).retainedBytes();
                }
            }
            long totalMb = totalBytes / (1024 * 1024);
            System.out.printf("view memory: %d regions trimmed to level %d, %d MB held (%.1f KB/region)%n",
                    span * span, targetLevel, totalMb, totalBytes / (double) (span * span) / 1024.0);
            report("T5  view memory (4000m)", totalMb, T5_VIEW_RAM_MB, "MB");

            // Prove the trim is recoverable, not a one-way break: ask for level 0 again on one
            // trimmed region and confirm it comes back rather than throwing.
            store.ensureLevel(0, 0, 0);
            long deadline = System.nanoTime() + 2_000_000_000L;
            while (!store.get(0, 0).hasLevel(0) && System.nanoTime() < deadline) {
                store.drain();
                Thread.sleep(2);
            }
            if (!store.get(0, 0).hasLevel(0)) {
                throw new AssertionError("FAILED: level 0 did not come back after trimToLevel + ensureLevel");
            }
            System.out.println("view memory: trimmed region rebuilt level 0 on demand — OK");
        } finally {
            store.shutdown();
        }
    }

    private static void waitUntilAllLoaded(RegionStore store, int span) throws InterruptedException {
        long deadline = System.nanoTime() + 5_000_000_000L;
        boolean allLoaded = false;
        while (!allLoaded && System.nanoTime() < deadline) {
            store.drain();
            allLoaded = true;
            for (int rx = 0; rx < span && allLoaded; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    if (store.get(rx, rz) == null) {
                        allLoaded = false;
                        break;
                    }
                }
            }
            if (!allLoaded) {
                Thread.sleep(1);
            }
        }
    }

    private static void waitUntilAllHaveLevel(RegionStore store, int span, int level) throws InterruptedException {
        long deadline = System.nanoTime() + 5_000_000_000L;
        boolean allBuilt = false;
        while (!allBuilt && System.nanoTime() < deadline) {
            store.drain();
            allBuilt = true;
            for (int rx = 0; rx < span && allBuilt; rx++) {
                for (int rz = 0; rz < span; rz++) {
                    if (!store.get(rx, rz).hasLevel(level)) {
                        allBuilt = false;
                        break;
                    }
                }
            }
            if (!allBuilt) {
                Thread.sleep(1);
            }
        }
    }

    private static void writeRegionPng(Path file, SyntheticWorld world) throws Exception {
        ColumnBuffer columns = new ColumnBuffer(Region.BLOCKS);
        world.fill(0, 0, columns);

        DebugStyle style = new DebugStyle();
        BufferedImage image = new BufferedImage(Region.BLOCKS, Region.BLOCKS, BufferedImage.TYPE_INT_ARGB);
        for (int z = 0; z < Region.BLOCKS; z++) {
            for (int x = 0; x < Region.BLOCKS; x++) {
                image.setRGB(x, z, DebugStyle.colourOf(style, columns, columns.index(x, z)));
            }
        }
        ImageIO.write(image, "png", new File(file.toString()));
    }

    private static void report(String name, long measured, long budget, String unit) {
        boolean ok = measured <= budget;
        anyFailed |= !ok;
        RESULTS.add(String.format("%-22s %6d %s  (budget %d %s)  %s",
                name, measured, unit, budget, unit, ok ? "PASS" : "FAIL"));
    }
}
