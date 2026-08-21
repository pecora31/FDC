package net.nazarick.artillerytablet.terrain;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps built tiles so the same ground is not surveyed again seconds later.
 *
 * <p>Without this, every refresh re-read sixteen chunks and decoded their NBT from scratch, however
 * little had changed — and clients expire their answers on a timer, so refreshes are most of the
 * traffic. Ground does not move often; the survey of it can be reused.
 *
 * <p>The cache also carries each tile's fingerprint, which is what lets a request that already knows
 * the answer be told so in a few bytes rather than being sent twelve kilobytes again.
 */
public final class ServerTileCache {
    /**
     * How long a survey is taken as current.
     *
     * <p>Short on purpose. It is not there to hold ground for long — it is there so that a burst of
     * requests covering the same tiles, which is what panning produces, costs one survey rather than
     * dozens. Anything that actually changes the ground shows up within this.
     */
    private static final long FRESH_MS = 2000L;

    /** Cap on remembered tiles, so a player touring the world cannot grow this without limit. */
    private static final int MAX_ENTRIES = 4096;

    private static final Map<String, Entry> CACHE = new ConcurrentHashMap<>();

    /** A surveyed tile, its fingerprint, and when it was taken. */
    public static final class Entry {
        public final TerrainTile tile;
        public final int hash;
        final long built;

        Entry(TerrainTile tile, long built) {
            this.tile = tile;
            this.hash = tile.contentHash();
            this.built = built;
        }
    }

    private ServerTileCache() {
    }

    /**
     * The tile, from memory when it is current, from the store when nobody is standing on it, and
     * from a fresh survey otherwise.
     *
     * <p>The order is the whole point. Memory is free. The store costs one small file instead of
     * sixteen chunk reads, which is the difference between a map that fills in seconds and one that
     * fills in twenty. And a survey is what ground with a player near it always gets, because that
     * is where building happens and because loaded chunks are the fast path anyway.
     *
     * <p>Must be called on the server thread: it asks the chunk source what is loaded.
     */
    public static CompletableFuture<Entry> get(ServerLevel level, int tileX, int tileZ) {
        String key = keyOf(level, tileX, tileZ);
        long now = System.currentTimeMillis();

        Entry held = CACHE.get(key);
        if (held != null && now - held.built < FRESH_MS) {
            return CompletableFuture.completedFuture(held);
        }

        if (anyChunkLoaded(level, tileX, tileZ)) {
            // Somebody is near it. Survey it: it may have changed, and the loaded chunks make that
            // cheap — no disk at all for the parts that are in memory.
            return survey(level, tileX, tileZ, key);
        }

        return ServerTileStore.read(level, tileX, tileZ).thenCompose(stored -> {
            if (stored == null) {
                return survey(level, tileX, tileZ, key);
            }
            FROM_STORE.incrementAndGet();
            Entry entry = new Entry(stored, System.currentTimeMillis());
            CACHE.put(key, entry);
            trim();
            return CompletableFuture.completedFuture(entry);
        });
    }

    // ---- where tiles actually come from ----------------------------------------------------------
    //
    // The client's trace says how fast ground arrives and cannot say where from, and the two are
    // wildly different jobs: a file read against sixteen chunks off the disk. Without splitting them
    // there is no way to tell whether making a survey cheaper would move anything at all, or whether
    // the fast numbers are simply the store answering.

    private static final java.util.concurrent.atomic.AtomicInteger FROM_STORE =
            new java.util.concurrent.atomic.AtomicInteger();
    private static final java.util.concurrent.atomic.AtomicInteger SURVEYED =
            new java.util.concurrent.atomic.AtomicInteger();

    /** Counts since the last call, as {store hits, surveys}. Reading them clears them. */
    public static int[] takeCounts() {
        return new int[]{FROM_STORE.getAndSet(0), SURVEYED.getAndSet(0)};
    }

    private static CompletableFuture<Entry> survey(ServerLevel level, int tileX, int tileZ, String key) {
        SURVEYED.incrementAndGet();
        return ServerTerrainProvider.buildTile(level, tileX, tileZ).thenApply(tile -> {
            Entry entry = new Entry(tile, System.currentTimeMillis());
            CACHE.put(key, entry);
            trim();
            ServerTileStore.write(level, tile);
            return entry;
        });
    }

    /** Chunks along one edge of a tile. */
    private static final int CHUNKS_PER_TILE = TerrainTile.SIDE / 16;

    /**
     * Whether any of a tile's chunks is in memory right now.
     *
     * <p>The test for "could this have changed since it was written down". Ground nobody is loading
     * is ground nobody is building on, and the map has never claimed to show battle damage anyway.
     */
    private static boolean anyChunkLoaded(ServerLevel level, int tileX, int tileZ) {
        int baseChunkX = Math.floorDiv(TerrainTile.tileToBlock(tileX), 16);
        int baseChunkZ = Math.floorDiv(TerrainTile.tileToBlock(tileZ), 16);
        for (int dx = 0; dx < CHUNKS_PER_TILE; dx++) {
            for (int dz = 0; dz < CHUNKS_PER_TILE; dz++) {
                // Never loads and never generates; null simply means it is not in memory.
                if (level.getChunkSource().getChunkNow(baseChunkX + dx, baseChunkZ + dz) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Drops the oldest surveys once there are too many.
     *
     * <p>Oldest by when they were taken rather than by when they were last wanted: a survey's worth
     * decays with age regardless of who asked for it, and the ones still being looked at are the
     * ones being refreshed, so they replace themselves anyway.
     */
    private static void trim() {
        if (CACHE.size() <= MAX_ENTRIES) {
            return;
        }
        long cutoff = System.currentTimeMillis() - FRESH_MS;
        Iterator<Map.Entry<String, Entry>> it = CACHE.entrySet().iterator();
        while (it.hasNext() && CACHE.size() > MAX_ENTRIES) {
            if (it.next().getValue().built < cutoff) {
                it.remove();
            }
        }
    }

    /**
     * Forgets everything, for a server that is shutting down.
     *
     * <p>A single-player client loads one world after another in the same process, and a dimension
     * is named the same in both. Surveys are only held for two seconds, so what this prevents is
     * narrow — a tile from the world just left, served to the first request of the world just
     * entered — but it is the one kind of wrong answer this layer must never give.
     */
    public static void forgetAll() {
        CACHE.clear();
    }

    private static String keyOf(ServerLevel level, int tileX, int tileZ) {
        ResourceLocation dimension = level.dimension().location();
        return dimension + ":" + tileX + ":" + tileZ;
    }
}
