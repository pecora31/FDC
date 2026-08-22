package net.nazarick.artillerytablet.client.terrain;

import net.nazarick.artillerytablet.terrain.TerrainTile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayDeque;

/**
 * A small free-list of the four fixed-size arrays every {@link TerrainTile} carries, so panning
 * into new ground doesn't have to allocate a fresh set every time old ground is let go — held
 * ground is exactly what churns as a player explores, and that churn is what this smooths, not
 * the resident size of whatever is currently cached.
 *
 * <p>Bounded on purpose — this is a pool for reuse, not a second cache. {@code
 * TerrainClientCache}'s own {@code MAX_REMEMBERED} already decides how much ground stays held;
 * this only softens the allocation traffic right at that boundary, so a modest cap is enough and
 * an unbounded one would just be a second copy of the cache's own job.
 *
 * <p>{@code block}, {@code height} and {@code biome} are all {@code short[COLUMNS]} and
 * interchangeable for pooling purposes — only {@code depth}'s {@code byte[COLUMNS]} is a
 * different shape, hence the two pools rather than four.
 */
@OnlyIn(Dist.CLIENT)
final class TileArrayPool {
    private static final int CAP = 512;

    private static final ArrayDeque<short[]> SHORTS = new ArrayDeque<>();
    private static final ArrayDeque<byte[]> BYTES = new ArrayDeque<>();

    private TileArrayPool() {
    }

    static synchronized short[] takeShorts() {
        short[] arr = SHORTS.poll();
        return arr != null ? arr : new short[TerrainTile.COLUMNS];
    }

    static synchronized byte[] takeBytes() {
        byte[] arr = BYTES.poll();
        return arr != null ? arr : new byte[TerrainTile.COLUMNS];
    }

    static synchronized void giveShorts(short[] arr) {
        if (SHORTS.size() < CAP) {
            SHORTS.push(arr);
        }
    }

    static synchronized void giveBytes(byte[] arr) {
        if (BYTES.size() < CAP) {
            BYTES.push(arr);
        }
    }

    /**
     * Copies a tile's four arrays into pooled storage and returns the copy — the arrays that
     * arrive fresh off the network or disk decode are still one-off allocations (that decode path
     * is shared with the server and not worth complicating for a client-only concern), but the
     * copy that actually sits in {@code TerrainClientCache.TILES} for a while is the one built
     * from reused storage.
     */
    static TerrainTile pooledCopy(TerrainTile source) {
        short[] block = takeShorts();
        short[] height = takeShorts();
        byte[] depth = takeBytes();
        short[] biome = takeShorts();
        System.arraycopy(source.block, 0, block, 0, TerrainTile.COLUMNS);
        System.arraycopy(source.height, 0, height, 0, TerrainTile.COLUMNS);
        System.arraycopy(source.depth, 0, depth, 0, TerrainTile.COLUMNS);
        System.arraycopy(source.biome, 0, biome, 0, TerrainTile.COLUMNS);
        return new TerrainTile(source.tileX, source.tileZ, block, height, depth, biome);
    }

    /** Releases every array a tile holds back to the pool. Only safe once the tile has already
     * been removed from every map that shared its arrays — {@code TerrainClientCache} owns that
     * ordering, this just does the giving-back. */
    static void release(TerrainTile tile) {
        if (tile == null) {
            return;
        }
        giveShorts(tile.block);
        giveShorts(tile.height);
        giveBytes(tile.depth);
        giveShorts(tile.biome);
    }
}
