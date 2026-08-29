package net.nazarick.mapengine.core;

/**
 * One square of the world, {@value #BLOCKS} blocks on a side, together with the coarser copies of
 * itself that wide views are drawn from.
 *
 * <p><b>Why 512 and not the 64 this project used before.</b> The old store wrote one file per 64×64
 * tile: 1992 files in a modest test world, and a 4000&nbsp;m view spans nearly four thousand of them.
 * Both map mods this engine is measured against write one file per 512×512 region — about 64 files
 * for that same view. The work saved is not the reading, it is the opening: four thousand directory
 * lookups and file handles against sixty-four. That was the direct cause of a cold open taking
 * seconds instead of a fraction of one.
 *
 * <p><b>Why the coarse copies are kept rather than recomputed.</b> Levels here are stored, on disk
 * and in memory, exactly as the reference mods store theirs. Recomputing them meant a wide view had
 * to hold every full-resolution column under it — around 112&nbsp;MB for a 4000&nbsp;m view — and do
 * the reduction again on every session. Stored, that same view reads level 3 alone: 64×64 per
 * region, under 2&nbsp;MB for the whole screen. Wide zooms stop being the expensive case, and — the
 * point of keeping this project's hillshaded look rather than dropping it — the *shaded* pixels are
 * what gets stored, not the raw heights, so the relief math that made the old renderer slow runs
 * once per region per level rather than once per frame.
 *
 * <p>Levels are held sparsely on purpose. A view at one zoom wants one level, and loading level 0 to
 * answer a question about level 3 would give back everything the pyramid just bought.
 */
public final class Region {
    /** Blocks along one edge of a region. */
    public static final int BLOCKS = 512;

    /**
     * How many levels of detail a region carries, level 0 being one texel per block.
     *
     * <p>Seven, and the number is not arbitrary: the tablet's eight zoom steps (250&nbsp;m to
     * 32000&nbsp;m across a panel of roughly 800 pixels) work out at 0.31 to 40 blocks per pixel,
     * which is levels 0 through 6 with nothing left over. Every zoom therefore draws from a stored
     * level rather than reducing at the moment of drawing.
     */
    public static final int LEVELS = 7;

    /** Blocks covered by one texel at this level: 1, 2, 4 … 64. */
    public static int strideOf(int level) {
        return 1 << level;
    }

    /** Texels along one edge at this level: 512, 256, 128 … 8. */
    public static int widthOf(int level) {
        return BLOCKS >> level;
    }

    /** The finest level whose texels are no larger than the given blocks-per-pixel. */
    public static int levelFor(double blocksPerPixel) {
        int level = 0;
        while (level < LEVELS - 1 && strideOf(level + 1) <= blocksPerPixel) {
            level++;
        }
        return level;
    }

    public final int regionX;
    public final int regionZ;

    /** Sparse: an entry is null until that level is loaded or built. */
    private final ColumnBuffer[] levels = new ColumnBuffer[LEVELS];

    public Region(int regionX, int regionZ) {
        this.regionX = regionX;
        this.regionZ = regionZ;
    }

    public long key() {
        return RegionKey.of(regionX, regionZ);
    }

    /** The north-west block of this region. */
    public int originX() {
        return regionX * BLOCKS;
    }

    public int originZ() {
        return regionZ * BLOCKS;
    }

    /** The columns at this level, or null when it has not been loaded or built. */
    public ColumnBuffer level(int level) {
        return levels[level];
    }

    public boolean hasLevel(int level) {
        return levels[level] != null;
    }

    public void setLevel(int level, ColumnBuffer columns) {
        int wanted = widthOf(level);
        if (columns != null && columns.width != wanted) {
            // Said out loud rather than trusted, because the failure is silent: a buffer of the wrong
            // width still indexes, still draws, and simply shows the wrong ground at one zoom step.
            throw new IllegalArgumentException(
                    "level " + level + " wants width " + wanted + ", got " + columns.width);
        }
        levels[level] = columns;
    }

    /** Frees every level but the one named, for a store trimming memory without dropping the region. */
    public void keepOnly(int level) {
        for (int i = 0; i < LEVELS; i++) {
            if (i != level) {
                levels[i] = null;
            }
        }
    }

    /** Bytes of column data currently held, across whichever levels are non-null right now. */
    public long retainedBytes() {
        long bytes = 0;
        for (int i = 0; i < LEVELS; i++) {
            if (levels[i] != null) {
                bytes += (long) levels[i].columns() * 7; // ColumnBuffer's own fixed 7 bytes/column
            }
        }
        return bytes;
    }

    /** The coarsest loaded level at or beyond the one asked for, or -1 when none is loaded. */
    public int bestAvailableFrom(int level) {
        for (int i = level; i < LEVELS; i++) {
            if (levels[i] != null) {
                return i;
            }
        }
        for (int i = level - 1; i >= 0; i--) {
            if (levels[i] != null) {
                return i;
            }
        }
        return -1;
    }
}
