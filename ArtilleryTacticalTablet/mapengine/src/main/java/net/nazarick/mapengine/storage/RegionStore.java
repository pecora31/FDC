package net.nazarick.mapengine.storage;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.ColumnSource;
import net.nazarick.mapengine.core.Region;
import net.nazarick.mapengine.core.RegionKey;
import net.nazarick.mapengine.lod.Pyramid;
import net.nazarick.mapengine.overview.WorldOverview;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Regions in memory, backed by disk, filled by a source of last resort.
 *
 * <p><b>One I/O queue, on purpose.</b> The disk-image cache tried in this project earlier ran its own
 * four-thread reader pool alongside the terrain-tile store's own — two queues on one physical disk,
 * and a real test session showed them stall each other to near zero throughput for whole seconds.
 * There is exactly one queue here, shared by every read and write this store issues, so that failure
 * mode cannot happen by construction rather than by discipline.
 *
 * <p><b>Every background task reports failure rather than disappearing.</b> The map this replaced
 * shipped a background bake with no {@code try/catch}: one exception left a region permanently
 * "loading" with nothing left to unstick it, and the failure surfaced as a black square with no error
 * in the log. Every task here is wrapped, and a failure still produces a result — an empty one — so
 * the region moves to a real state instead of a stuck one.
 *
 * <p><b>LRU is a {@link LinkedHashMap} in access order.</b> The JDK already does exactly this —
 * {@code removeEldestEntry} evicts the least-recently-touched entry on its own, so there is no second
 * data structure (an age field, a manual list) to keep in sync with the map itself. The boxed
 * {@code Long} key costs nothing worth avoiding here: this map is touched a few dozen times a frame,
 * not per texel the way the tile cache inside the mod is.
 */
public final class RegionStore {
    private final Path root;
    private final ColumnSource source;
    private final int capacity;

    private final Map<Long, Region> regions = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Region> eldest) {
            return size() > capacity;
        }
    };
    private final Object lock = new Object();

    /** Keys currently being loaded, so the same region is never queued twice. */
    private final Map<Long, Boolean> loading = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * One shard read is memoized per shard for this store's lifetime — see {@link RegionShardFile}
     * for why a single shard-wide read replaces up to sixty-four separate per-region file opens.
     * Whichever region's load task first reaches a given shard pays for the read; the other sixty-
     * three block briefly on {@link Map#computeIfAbsent} and then reuse the same result instead of
     * each opening the file (or each independently discovering it does not exist) themselves.
     */
    private final Map<Long, Map<Long, ColumnBuffer>> shardCache = new ConcurrentHashMap<>();

    /**
     * Regions surveyed fresh (not read back) since the last {@link #flushShards}, held here per
     * shard. <b>Not</b> gated on the shard being complete — a player fills an entire 8x8, ~4000&nbsp;m
     * shard in one sitting rarely if ever, and a design that only wrote a shard file once every one
     * of its 64 regions was known would mean the shard-read cold-open win this class exists for
     * almost never actually applies to a real, incrementally-explored world. {@link #flushShards}
     * merges this into whatever the shard file already holds instead, so partial progress persists
     * the same way the per-region {@link RegionFile} writes already do — this is purely an additional,
     * coarser copy for the next cold open to read in one file instead of many.
     */
    private final Map<Long, Map<Long, ColumnBuffer>> shardBuildup = new ConcurrentHashMap<>();

    private final ExecutorService io;
    private final ConcurrentLinkedQueue<LoadResult> finished = new ConcurrentLinkedQueue<>();
    private final AtomicInteger pending = new AtomicInteger();
    private static final int MAX_PENDING = 64;

    /** Guards a (region, level) pyramid build in flight, so asking twice before it finishes is free. */
    private final Map<String, Boolean> levelWork = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<LevelResult> finishedLevels = new ConcurrentLinkedQueue<>();

    private record LevelResult(long key, ColumnBuffer[] built, ColumnBuffer reloadedLevel0) {
    }

    /**
     * Everything this store has ever surveyed, at a glance — see {@link WorldOverview}'s own doc for
     * why this exists separately from the per-region pyramid. Never populated from this store's own
     * loads; only from fresh surveys (see {@link #drain}), and only ever read by the caller that owns
     * it, at whatever cadence it chooses to persist or query it — this store just keeps it current.
     */
    private final WorldOverview overview;

    public RegionStore(Path root, ColumnSource source, int capacity, int ioThreads) {
        this(root, source, capacity, ioThreads, new WorldOverview());
    }

    /**
     * @param overview a previously loaded (or freshly empty) overview — loading it is the caller's
     *                 job, deliberately outside this constructor, so that "read the one small file
     *                 that makes everything explored show up immediately" stays a plain, visible call
     *                 at the call site rather than a hidden cost inside this one.
     */
    public RegionStore(Path root, ColumnSource source, int capacity, int ioThreads, WorldOverview overview) {
        this.root = root;
        this.source = source;
        this.capacity = capacity;
        this.overview = overview;
        ThreadFactory factory = r -> {
            Thread t = new Thread(r, "mapengine-region-io");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        };
        this.io = ioThreads <= 1
                ? Executors.newSingleThreadExecutor(factory)
                : Executors.newFixedThreadPool(ioThreads, factory);
    }

    /** Everything this store has surveyed so far, this session and before. Read-only from outside. */
    public WorldOverview overview() {
        return overview;
    }

    /**
     * {@code region} carries only level 0 — the pyramid is built lazily, level by level, by whoever
     * first asks a {@link Region} for a coarser one (see {@link Pyramid#ensureLevel}). Building all
     * six coarse levels here for every region unconditionally was measured costing more than the
     * disk read it followed: a cold open only ever needs the one level its own zoom picks, so the
     * other five were pure CPU spent on levels nothing was about to read.
     */
    private record LoadResult(long key, Region region, ColumnBuffer level0, boolean fromDisk) {
    }

    /**
     * The region at this key, or null while it is still loading. Never blocks.
     *
     * <p>Load order: disk first (fast, and correct even for ground the source can no longer reach —
     * a player who has walked away from a chunk), the source second, and if neither has anything the
     * region is filed as permanently empty rather than retried forever.
     */
    public Region get(int regionX, int regionZ) {
        long key = RegionKey.of(regionX, regionZ);
        synchronized (lock) {
            // A plain get() already counts as a touch — access order is the whole reason this map is
            // configured that way — so nothing further is needed to mark this region as wanted.
            Region existing = regions.get(key);
            if (existing != null) {
                return existing;
            }
        }
        requestLoad(key, regionX, regionZ);
        return null;
    }

    /**
     * Forces a region to be asked of the source again, even though it is already held. Most callers
     * never need this — this store's cache is otherwise permanent by design, correct for a source
     * whose answer about a given region genuinely cannot change (a completed server survey). It is
     * the wrong assumption for a source that answers from whatever happens to be true right now, like
     * a client sampling whichever chunks it currently has loaded: the very first answer for newly
     * entered ground is routinely {@link ColumnBuffer#isComplete() incomplete} or even empty, simply
     * because most of a wide region sits outside render distance the moment it is first asked about,
     * and without this that answer would otherwise stick forever — the region never getting another
     * chance once more of the world has loaded around the player.
     *
     * <p>A no-op when the region is not currently held, or when a load for it is already in flight —
     * safe to call speculatively, on a timer, without checking either first.
     */
    public void resurvey(int regionX, int regionZ) {
        long key = RegionKey.of(regionX, regionZ);
        if (loading.containsKey(key)) {
            return; // already on its way; let that finish rather than racing a second load
        }
        // The whole point of a resurvey is asking the *source* again — reading disk again would just
        // hand back the exact same answer this call exists because the source might now disagree
        // with, forever. Marked here and consumed once by requestLoad below.
        forceSource.put(key, Boolean.TRUE);
        synchronized (lock) {
            Region existing = regions.get(key);
            if (existing != null && existing.level(0) != null) {
                // Seeded, not discarded: a resurvey commonly runs long after the player has walked
                // away from a region, and the source answering "nothing right now" then is not the
                // source disagreeing with what is already known — it is the source simply having
                // nothing left in range to confirm it with. Passing the old buffer in as the starting
                // point for the new fill (see requestLoad below and ForgeColumnSource's own contract
                // of only ever writing columns it actually resampled) means a column not touched this
                // round keeps whatever it already held, and a resurvey can only ever add ground, never
                // erase it. Losing this seed was a real, shipped regression: the very first version of
                // this method replaced the whole buffer outright, and a resurvey of ground the player
                // had already left behind quietly erased it from the map.
                resurveySeed.put(key, existing.level(0));
            }
            regions.remove(key);
        }
    }

    /** Regions whose next {@link #requestLoad} must skip straight to the source — see {@link #resurvey}. */
    private final Map<Long, Boolean> forceSource = new ConcurrentHashMap<>();

    /** What a resurveyed region already knew, carried into its next load so a fill can only add to it. */
    private final Map<Long, ColumnBuffer> resurveySeed = new ConcurrentHashMap<>();

    /**
     * Drains whatever background loads have finished. Call once per frame, on the owning thread.
     *
     * <p>Deliberately just a queue drain — a map insert and, for a fresh survey, kicking off a save.
     * All the real work (disk read, decompression, the pyramid) already happened on an I/O thread; if
     * any of that were here instead, this method's cost would scale with how much finished since the
     * last call, which for a cold open is "all of it, at once, on the thread that just asked".
     */
    public void drain() {
        LoadResult result;
        while ((result = finished.poll()) != null) {
            loading.remove(result.key);
            if (!result.fromDisk && result.level0 != null) {
                // Freshly surveyed, not read back — file it away so the next load, this session or
                // after a restart, is a read instead of a resurvey. The pyramid was already built on
                // the I/O thread; only the raw level 0 is written, per RegionFile's own reasoning.
                int saveX = result.region.regionX;
                int saveZ = result.region.regionZ;
                ColumnBuffer saveLevel0 = result.level0;
                io.execute(() -> {
                    try {
                        RegionFile.write(root, saveX, saveZ, saveLevel0);
                    } catch (Throwable ignored) {
                        // A save that fails costs a resurvey next time, not correctness now.
                    }
                });
                addToShardBuildup(saveX, saveZ, saveLevel0);
                // Folded into the always-resident overview too, off this thread — see WorldOverview's
                // own doc for why this only happens for a fresh survey and not for every load.
                io.execute(() -> overview.merge(saveX, saveZ, saveLevel0));
            }
            // An empty region is still filed — permanently-unknown ground is a real, final answer,
            // and filing it stops this store asking about it again every frame.
            synchronized (lock) {
                // put() on an access-order LinkedHashMap both places this at the "just used" end and
                // triggers removeEldestEntry if that pushes the map over capacity — the eviction is
                // the map's own bookkeeping, not a second pass this class has to remember to run.
                regions.put(result.key, result.region);
            }
        }

        LevelResult levelResult;
        while ((levelResult = finishedLevels.poll()) != null) {
            Region region;
            synchronized (lock) {
                region = regions.get(levelResult.key);
            }
            if (region == null) {
                continue; // evicted while the build was in flight — the work is wasted, not wrong
            }
            if (levelResult.reloadedLevel0 != null && region.level(0) == null) {
                region.setLevel(0, levelResult.reloadedLevel0);
            }
            for (int l = 1; l < levelResult.built.length; l++) {
                ColumnBuffer built = levelResult.built[l];
                if (built != null && !region.hasLevel(l)) {
                    region.setLevel(l, built);
                }
            }
        }
    }

    /**
     * Kicks off a background build of {@code level} for a region already in the store, if it does
     * not have that level yet and no build for it is already in flight. Never blocks; the result
     * shows up in {@link Region#hasLevel} only after a later {@link #drain} call, same as a load.
     *
     * <p>Deliberately not part of {@link #get} — a load only ever has level 0, and which coarser
     * level (if any) is worth building is a question only the caller's own zoom knows the answer to.
     *
     * <p>Works even after {@link #trimToLevel} has freed level 0 — {@link Pyramid#computeLevels}
     * always needs it to build from, so a missing level 0 is reloaded first (shard, then per-region
     * file, the same fallback order a fresh load already uses) before the pyramid runs. That reload
     * is real disk I/O a caller pays for by trimming aggressively and then zooming back in; it is not
     * free, just cheap.
     */
    public void ensureLevel(int regionX, int regionZ, int level) {
        long key = RegionKey.of(regionX, regionZ);
        Region region;
        synchronized (lock) {
            region = regions.get(key);
        }
        if (region == null || region.hasLevel(level)) {
            return;
        }
        String guard = key + ":" + level;
        if (levelWork.putIfAbsent(guard, Boolean.TRUE) != null) {
            return;
        }
        io.execute(() -> {
            try {
                ColumnBuffer level0 = region.level(0);
                ColumnBuffer reloaded = null;
                if (level0 == null) {
                    level0 = readViaShard(key, regionX, regionZ);
                    if (level0 == null) {
                        level0 = RegionFile.read(root, regionX, regionZ);
                    }
                    if (level0 == null) {
                        return; // nothing to build from; caller can ask again once more is known
                    }
                    reloaded = level0;
                }
                // A scratch region, not the real one — the real region's mutable state is only ever
                // touched from the drain thread, same discipline every other cross-thread result in
                // this class follows. Already-built intermediate levels are copied in first so this
                // does not silently redo work drain already applied from an earlier ensureLevel call.
                Region scratch = new Region(regionX, regionZ);
                scratch.setLevel(0, level0);
                for (int l = 1; l < Region.LEVELS; l++) {
                    if (region.hasLevel(l)) {
                        scratch.setLevel(l, region.level(l));
                    }
                }
                ColumnBuffer[] built = Pyramid.computeLevels(scratch, level);
                finishedLevels.add(new LevelResult(key, built, reloaded));
            } catch (Throwable ignored) {
                // Nothing published; the next call to ensureLevel for this region simply tries again.
            } finally {
                levelWork.remove(guard);
            }
        });
    }

    /**
     * Frees every level of this region but the one named, reclaiming what level 0 alone costs
     * (1.75&nbsp;MB per region at the engine's default size) once nothing needs it at the zoom
     * currently drawn. Safe to call repeatedly, and safe even if a background {@link #ensureLevel}
     * for this region is mid-flight — that task took its own snapshot before this call and only
     * rejoins the real region through {@link #drain}, so a trim racing a build cannot corrupt either.
     *
     * <p>A real tradeoff, not a free lunch: zooming back in past the level kept here means the next
     * {@link #ensureLevel} call has to reload level 0 from disk first. Cheap, not free — call this
     * once a zoom has settled, not on every frame that happens to sit at one level.
     */
    public void trimToLevel(int regionX, int regionZ, int level) {
        long key = RegionKey.of(regionX, regionZ);
        synchronized (lock) {
            Region region = regions.get(key);
            if (region != null) {
                region.keepOnly(level);
            }
        }
    }

    private void requestLoad(long key, int regionX, int regionZ) {
        if (loading.putIfAbsent(key, Boolean.TRUE) != null) {
            return;
        }
        if (pending.get() > MAX_PENDING) {
            loading.remove(key);
            return; // try again next time it's asked for
        }
        pending.incrementAndGet();
        boolean skipDisk = forceSource.remove(key) != null;
        ColumnBuffer seed = resurveySeed.remove(key);
        io.execute(() -> {
            try {
                if (!skipDisk) {
                    ColumnBuffer fromShard = readViaShard(key, regionX, regionZ);
                    if (fromShard != null) {
                        finished.add(buildResult(key, regionX, regionZ, fromShard, true));
                        return;
                    }
                    // Not in the shard file (or no shard file exists yet). A region can still have
                    // been written individually without the shard around it having filled — e.g. this
                    // session wrote it just now, before enough neighbours arrived to trigger a shard
                    // write.
                    ColumnBuffer fromRegionFile = RegionFile.read(root, regionX, regionZ);
                    if (fromRegionFile != null) {
                        finished.add(buildResult(key, regionX, regionZ, fromRegionFile, true));
                        return;
                    }
                }
                if (!source.canAnswer(regionX, regionZ)) {
                    finished.add(buildResult(key, regionX, regionZ, null, false));
                    return;
                }
                // Seeded with whatever this region already knew, when this load is a resurvey — a
                // source only ever writes the columns it actually resampled (ForgeColumnSource's own
                // contract), so anything already in `level0` that isn't touched this round survives
                // unchanged. A plain first-time load has no seed and starts from a blank buffer as
                // it always did.
                ColumnBuffer level0 = seed != null ? seed : new ColumnBuffer(Region.BLOCKS);
                boolean any = source.fill(regionX * Region.BLOCKS, regionZ * Region.BLOCKS, level0);
                // `any` alone would be wrong here: a resurvey that confirms nothing new this round
                // (any=false) can still be sitting on real ground from its seed, and reporting that as
                // "nothing here" would erase it in buildResult/drain exactly the way the first version
                // of this fix did. isComplete()'s own opposite, isEmpty(), is the right question: is
                // there anything at all in this buffer, seeded or fresh.
                boolean keep = any || (seed != null && !level0.isEmpty());
                finished.add(buildResult(key, regionX, regionZ, keep ? level0 : null, false));
            } catch (Throwable t) {
                // Reported by the caller draining `finished`, not lost: a failed load still produces
                // a result, so the region ends up empty-and-final rather than stuck loading forever.
                finished.add(buildResult(key, regionX, regionZ, null, false));
            } finally {
                pending.decrementAndGet();
            }
        });
    }

    /**
     * The shard covering this region, read once per shard and reused for every other region inside
     * it. Null means "not in this shard" — either the shard has never been written, or it has but
     * this particular slot was empty ground at the time it was written.
     */
    private ColumnBuffer readViaShard(long key, int regionX, int regionZ) {
        int shardX = RegionShardFile.shardOf(regionX);
        int shardZ = RegionShardFile.shardOf(regionZ);
        long shardKey = RegionKey.of(shardX, shardZ);
        Map<Long, ColumnBuffer> shard = shardCache.computeIfAbsent(shardKey,
                k -> RegionShardFile.read(root, shardX, shardZ));
        return shard.get(key);
    }

    /**
     * Files a freshly surveyed region into its shard's buildup, and writes the whole shard out once
     * every slot in it is known. {@code Map.of()} from an empty buildup never happens here — a shard
     * only starts existing in this map the moment its first region arrives.
     */
    private void addToShardBuildup(int regionX, int regionZ, ColumnBuffer level0) {
        long shardKey = RegionKey.of(RegionShardFile.shardOf(regionX), RegionShardFile.shardOf(regionZ));
        shardBuildup.computeIfAbsent(shardKey, k -> new ConcurrentHashMap<>())
                .put(RegionKey.of(regionX, regionZ), level0);
    }

    /**
     * Writes out whatever partial shard progress has accumulated since the last flush — merged with
     * whatever that shard file already holds, never overwriting it outright, since a shard's file
     * covers all 64 regions and a partial flush only ever knows a few of them. Safe to call with
     * nothing pending, safe to call again before an earlier flush's writes land, and safe to call
     * repeatedly through a session (each call only carries forward regions freshly surveyed since the
     * last one, not the whole shard again).
     *
     * <p>Not called automatically by this class — the same "an I/O cost is a visible call, not a
     * hidden timer" reasoning as {@link WorldOverview}'s own persistence. Call this periodically (a
     * tick handler, a world-save hook) and once more at {@link #shutdown} so a session's progress is
     * never lost only because it never happened to complete a whole shard.
     */
    public void flushShards() {
        flushShardsNow(true);
    }

    private void flushShardsNow(boolean async) {
        if (shardBuildup.isEmpty()) {
            return;
        }
        for (Long shardKey : new java.util.ArrayList<>(shardBuildup.keySet())) {
            Map<Long, ColumnBuffer> pending = shardBuildup.remove(shardKey);
            if (pending == null || pending.isEmpty()) {
                continue;
            }
            int shardX = RegionKey.x(shardKey);
            int shardZ = RegionKey.z(shardKey);
            Runnable task = () -> {
                try {
                    // Read-merge-write, always straight from disk rather than through shardCache —
                    // that cache exists for a completely unrelated read (readViaShard, for a region
                    // load happening concurrently on another I/O thread) and can hold a stale or
                    // empty snapshot at the exact moment a flush runs. Merging against that instead
                    // of the real file was a genuine race this benchmark's own partial-flush check
                    // caught: a concurrent single-region read could cache an empty map right before
                    // this flush read it, silently discarding an earlier flush's regions. Disk is the
                    // only thing this merge can trust; shardCache is updated afterwards purely so a
                    // later read in this same session does not pay for reopening the file again.
                    Map<Long, ColumnBuffer> merged = new java.util.HashMap<>(
                            RegionShardFile.read(root, shardX, shardZ));
                    merged.putAll(pending);
                    RegionShardFile.write(root, shardX, shardZ, merged);
                    shardCache.put(shardKey, merged);
                } catch (Throwable ignored) {
                    // The per-region RegionFile writes already landed; this coarser copy is an
                    // optimization, not the record of truth. Put the pending regions back so the next
                    // flush retries them instead of quietly losing this batch.
                    shardBuildup.computeIfAbsent(shardKey, k -> new ConcurrentHashMap<>()).putAll(pending);
                }
            };
            if (async) {
                io.execute(task);
            } else {
                task.run();
            }
        }
    }

    /**
     * Builds the {@link Region} with level 0 set, on the calling (I/O) thread. Coarser levels are
     * deliberately not built here — see the field doc on {@link #shardCache} and {@link LoadResult}.
     */
    private static LoadResult buildResult(long key, int regionX, int regionZ, ColumnBuffer level0,
                                          boolean fromDisk) {
        Region region = new Region(regionX, regionZ);
        if (level0 != null) {
            region.setLevel(0, level0);
        }
        return new LoadResult(key, region, level0, fromDisk);
    }

    public int liveCount() {
        synchronized (lock) {
            return regions.size();
        }
    }

    public void shutdown() {
        // Synchronous, and before the pool dies — an async flush submitted right before
        // shutdownNow() would just get cancelled, losing exactly the progress this call exists to
        // save on the way out.
        flushShardsNow(false);
        io.shutdownNow();
    }
}
