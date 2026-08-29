package net.nazarick.mapengine.lod;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;

/**
 * Builds a region's coarse levels from its level 0, by averaging two-by-two boxes repeatedly.
 *
 * <p><b>Only level 0 is ever read from disk or a {@link net.nazarick.mapengine.core.ColumnSource}.</b>
 * Levels 1..6 are pure arithmetic on data already in memory — no I/O, nothing that can be slow for a
 * reason outside this class's control. That is deliberate: it means a region file only has to hold
 * the one level survey ever produces, and the "levels are stored, not recomputed every zoom" property
 * the map wants only has to be paid for once per region load, not once per disk read.
 *
 * <p><b>What survives averaging and what does not.</b> Height, block id and biome id are all
 * meaningless once blended — there is no "block id 1.5" — so a coarse texel's block/biome is a plain
 * majority vote (ties broken toward whichever came first) while height and water depth are averaged
 * properly. This mirrors {@code TerrainMips}, the levelled reducer this project used before, with one
 * difference: colour is not baked in at this stage, because {@link net.nazarick.mapengine.core.BlockStyle}
 * and relief shading are the rasteriser's job, applied after a level is read — the same reasoning
 * that keeps {@code BlockStyle} out of {@code ColumnBuffer}.
 */
public final class Pyramid {
    private Pyramid() {
    }

    /** Fills every empty level above level 0 in {@code region}. Level 0 must already be set. */
    public static void build(Region region) {
        ensureLevel(region, Region.LEVELS - 1);
    }

    /**
     * Builds only as far as {@code level}, not the whole pyramid. A load only ever has level 0 in
     * hand; a wide zoom needs one coarse level, not all six — building the other five just to throw
     * most of them away (a 4000&nbsp;m cold open only ever reads the one level its own zoom picks,
     * per {@link Region#levelFor}) was pure-CPU cost with no reader for the levels it paid for. This
     * is the same reduction {@link #build} does, just stopped at the level actually asked for; each
     * intermediate level it does pass through is still kept (via {@link Region#setLevel}), so asking
     * for a coarser level later resumes from here instead of restarting at level 0.
     */
    public static void ensureLevel(Region region, int level) {
        if (region.hasLevel(level)) {
            return;
        }
        ColumnBuffer parent = region.level(0);
        if (parent == null) {
            throw new IllegalStateException("level 0 must be loaded before the pyramid can be built");
        }
        for (int l = 1; l <= level; l++) {
            if (region.hasLevel(l)) {
                parent = region.level(l);
                continue;
            }
            ColumnBuffer child = reduce(parent);
            region.setLevel(l, child);
            parent = child;
        }
    }

    /**
     * Computes every level from the finest one already present up to {@code upTo}, without mutating
     * {@code region} — for a caller that wants to do this work on a thread other than the one that
     * owns the region's mutable state (see {@code RegionStore.ensureLevel}, which runs this on its
     * I/O pool and only applies the result on the thread that calls {@code drain}, the same
     * cross-thread-publish discipline every other write to a loaded region already follows there).
     *
     * <p>Level 1 is the expensive one and stays expensive regardless of {@code upTo} — it is the only
     * step that reduces the full 512×512 level 0, and every coarser level is a quarter the size of
     * the one before it, so going no further than level 2 instead of level 6 barely moves the total
     * cost. What actually parallelizes this work is running it across regions concurrently, which is
     * exactly what dispatching it onto the I/O pool, one call per region, buys — not trimming levels.
     *
     * @return newly computed levels, indexed by level number; entries already present in
     *         {@code region} or beyond {@code upTo} are left null
     */
    public static ColumnBuffer[] computeLevels(Region region, int upTo) {
        ColumnBuffer[] built = new ColumnBuffer[Region.LEVELS];
        ColumnBuffer parent = region.level(0);
        if (parent == null) {
            throw new IllegalStateException("level 0 must be loaded before the pyramid can be built");
        }
        for (int l = 1; l <= upTo; l++) {
            if (region.hasLevel(l)) {
                parent = region.level(l);
                continue;
            }
            ColumnBuffer child = reduce(parent);
            built[l] = child;
            parent = child;
        }
        return built;
    }

    /** One level, averaged from the level above it (twice its width). */
    static ColumnBuffer reduce(ColumnBuffer parent) {
        int childWidth = parent.width / 2;
        ColumnBuffer child = new ColumnBuffer(childWidth);

        for (int cz = 0; cz < childWidth; cz++) {
            for (int cx = 0; cx < childWidth; cx++) {
                int a = parent.index(cx * 2, cz * 2);
                int b = parent.index(cx * 2 + 1, cz * 2);
                int c = parent.index(cx * 2, cz * 2 + 1);
                int d = parent.index(cx * 2 + 1, cz * 2 + 1);
                reduceOne(parent, a, b, c, d, child, child.index(cx, cz));
            }
        }
        return child;
    }

    private static void reduceOne(ColumnBuffer p, int a, int b, int c, int d, ColumnBuffer out, int at) {
        int knownSum = 0;
        int known = 0;
        int depthSum = 0;
        int groundSum = 0;
        int groundKnown = 0;
        // Majority vote for block/biome: count occurrences among the up-to-four known samples,
        // keep whichever is seen most, first-seen breaks a tie. Four options is cheap enough to do
        // by hand rather than pull in a map for it.
        short[] blocks = new short[4];
        short[] biomes = new short[4];
        int n = 0;

        int[] indices = {a, b, c, d};
        for (int idx : indices) {
            short h = p.height[idx];
            if (h == ColumnBuffer.NO_DATA) {
                continue;
            }
            knownSum += h;
            depthSum += p.depthAt(idx);
            blocks[n] = p.block[idx];
            biomes[n] = p.biome[idx];
            n++;
            known++;

            // Averaged on its own count, not known's — a column can have a surface height with no
            // ground-height answer yet (a source that fills height but not groundHeight), and one
            // missing corner should not drag the other three's real ground elevation off target.
            short g = p.groundHeight[idx];
            if (g != ColumnBuffer.NO_DATA) {
                groundSum += g;
                groundKnown++;
            }
        }

        if (known == 0) {
            out.height[at] = ColumnBuffer.NO_DATA;
            out.groundHeight[at] = ColumnBuffer.NO_DATA;
            return;
        }

        out.height[at] = (short) Math.round(knownSum / (double) known);
        out.groundHeight[at] = groundKnown == 0 ? ColumnBuffer.NO_DATA
                : (short) Math.round(groundSum / (double) groundKnown);
        out.depth[at] = (byte) Math.min(ColumnBuffer.MAX_DEPTH, Math.round(depthSum / (double) known));
        out.block[at] = majority(blocks, n);
        out.biome[at] = majority(biomes, n);
    }

    private static short majority(short[] values, int n) {
        short best = values[0];
        int bestCount = 0;
        for (int i = 0; i < n; i++) {
            int count = 0;
            for (int j = 0; j < n; j++) {
                if (values[j] == values[i]) {
                    count++;
                }
            }
            if (count > bestCount) {
                bestCount = count;
                best = values[i];
            }
        }
        return best;
    }
}
