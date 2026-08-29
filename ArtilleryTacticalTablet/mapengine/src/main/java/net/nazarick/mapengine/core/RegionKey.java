package net.nazarick.mapengine.core;

/**
 * A region's coordinate, packed into one {@code long}.
 *
 * <p>Packed rather than an object because the store looks regions up per frame and a boxed key
 * allocates on every lookup. Static methods rather than instances for the same reason: there is
 * nothing to hold, only arithmetic.
 *
 * <p>Floor division throughout, never a plain {@code /}. Half of every world has negative
 * coordinates, and truncating division puts block -1 and block +1 in the same region — a fault that
 * does not throw, it just serves one piece of ground under another's name.
 */
public final class RegionKey {
    private RegionKey() {
    }

    public static long of(int regionX, int regionZ) {
        return ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
    }

    public static int x(long key) {
        return (int) (key >> 32);
    }

    public static int z(long key) {
        return (int) key;
    }

    /** Which region a block coordinate falls in. */
    public static int blockToRegion(int block) {
        return Math.floorDiv(block, Region.BLOCKS);
    }

    /** The north-west block of a region. */
    public static int regionToBlock(int region) {
        return region * Region.BLOCKS;
    }

    public static String describe(long key) {
        return x(key) + "," + z(key);
    }
}
