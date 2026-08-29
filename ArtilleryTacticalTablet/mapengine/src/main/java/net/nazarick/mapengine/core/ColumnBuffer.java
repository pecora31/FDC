package net.nazarick.mapengine.core;

import java.util.Arrays;

/**
 * A square of surveyed ground, one sample per column, held as four parallel arrays.
 *
 * <p><b>Struct of arrays, not an array of structs.</b> Every pass over this data reads one field
 * across many columns — the rasteriser walks colours, the pyramid averages heights, the ballistic
 * query reads heights alone and never looks at a block id. Four flat arrays let each of those touch
 * only the memory it needs; a column object per sample would drag all four fields through cache on
 * every pass and allocate a quarter of a million objects per region.
 *
 * <p><b>What a column records, and why each field is here:</b>
 * <ul>
 *   <li>{@link #height} — the surface. This is the field artillery needs and the reason this engine
 *       stores terrain rather than pictures: a map that only knows colours cannot answer whether a
 *       shell clears the ridge in between.</li>
 *   <li>{@link #block} — an opaque id, not a colour. Resolving it to a colour is
 *       {@link BlockStyle}'s job, so the same stored ground can be drawn as a photograph, a contour
 *       sheet or a thermal picture without being surveyed again.</li>
 *   <li>{@link #biome} — likewise opaque, for tinting grass, foliage and water.</li>
 *   <li>{@link #depth} — blocks of water above the floor. Carried separately so the floor's own
 *       colour survives: a sea drawn as one flat blue hides every shape beneath it.</li>
 * </ul>
 */
public final class ColumnBuffer {
    /** A column nothing is known about. Distinct from "nothing is there", which is a real answer. */
    public static final short NO_DATA = Short.MIN_VALUE;

    /** A column whose biome was never recorded, so a tint lookup should be skipped rather than guessed. */
    public static final short NO_BIOME = -1;

    /** Deepest water recorded. Past this the drawing has long since bottomed out. */
    public static final int MAX_DEPTH = 255;

    /** Columns along one edge. */
    public final int width;

    /** Surface height in world Y, or {@link #NO_DATA}. Includes whatever stands on the ground. */
    public final short[] height;

    /**
     * The ground's own elevation, canopy and clutter read straight through — the real block a shell
     * would hit, or a contour line would trace, not the top of whatever tree or flower happens to be
     * standing on it. Distinct from {@link #height} on purpose: {@link #height} is what a column looks
     * like from directly above (what {@code Rasterizer.rasterize}, the Ground layer, draws — a forest
     * really does read as a forest there), while this is what {@code Rasterizer.rasterizeHypsometric}
     * (Topo) and a crest-clearance query both need, and neither can get it from {@link #height} no
     * matter how it is post-processed — a canopy column's true ground elevation was never in that
     * field to begin with, only its own topmost occupied Y. Or {@link #NO_DATA}.
     */
    public final short[] groundHeight;

    /** Opaque block id of the surface, meaningful only to the {@link BlockStyle} that issued it. */
    public final short[] block;

    /** Opaque biome id, or {@link #NO_BIOME}. */
    public final short[] biome;

    /** Blocks of water above the floor, 0 where there is none, capped at {@link #MAX_DEPTH}. */
    public final byte[] depth;

    public ColumnBuffer(int width) {
        if (width <= 0 || (width & (width - 1)) != 0) {
            // A power of two, because the pyramid halves it repeatedly and a remainder at any level
            // would leave a column that belongs to no parent — a seam that only shows at one zoom.
            throw new IllegalArgumentException("width must be a power of two, got " + width);
        }
        this.width = width;
        int columns = width * width;
        this.height = new short[columns];
        this.groundHeight = new short[columns];
        this.block = new short[columns];
        this.biome = new short[columns];
        this.depth = new byte[columns];
        clear();
    }

    /** Returns this to "nothing is known", which is what a freshly pooled buffer must look like. */
    public void clear() {
        Arrays.fill(height, NO_DATA);
        Arrays.fill(groundHeight, NO_DATA);
        Arrays.fill(block, (short) 0);
        Arrays.fill(biome, NO_BIOME);
        Arrays.fill(depth, (byte) 0);
    }

    public int columns() {
        return width * width;
    }

    /** Row-major, z outer — the order every pass over this data walks in. */
    public int index(int x, int z) {
        return z * width + x;
    }

    /** Water depth as a number rather than a signed byte. */
    public int depthAt(int index) {
        return depth[index] & 0xFF;
    }

    /** The floor: the surface with any water above it taken off. What a shell would actually hit. */
    public short floorAt(int index) {
        short surface = height[index];
        return surface == NO_DATA ? NO_DATA : (short) (surface - depthAt(index));
    }

    /** Whether any column here has been surveyed at all. */
    public boolean isEmpty() {
        for (short h : height) {
            if (h != NO_DATA) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether every column here has been surveyed. A source that answers from whatever happens to be
     * loaded right now — a client's own nearby chunks, say — routinely returns a buffer that is
     * neither: some columns known, most not, because most of a wide region sits outside render
     * distance the moment it is first asked about. That gap is a real signal, not noise: it is what
     * tells a caller this buffer is still worth asking about again once more of the world has loaded.
     */
    public boolean isComplete() {
        for (short h : height) {
            if (h == NO_DATA) {
                return false;
            }
        }
        return true;
    }

    /**
     * Copies one column across. Used by the sources that fill a region a chunk at a time, and by the
     * pyramid where a level is a straight copy rather than an average.
     */
    public void copyColumnFrom(ColumnBuffer from, int fromIndex, int toIndex) {
        height[toIndex] = from.height[fromIndex];
        groundHeight[toIndex] = from.groundHeight[fromIndex];
        block[toIndex] = from.block[fromIndex];
        biome[toIndex] = from.biome[fromIndex];
        depth[toIndex] = from.depth[fromIndex];
    }
}
