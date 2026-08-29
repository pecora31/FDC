package net.nazarick.mapengine.raster;

import net.nazarick.mapengine.core.BlockStyle;
import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;

/**
 * Turns one stored level of a region into pixels — {@link BlockStyle}'s base colour, then the same
 * relief/hillshade this project has always drawn on top of it. Ported line-for-line from the main
 * mod's {@code TerrainImage.shadeCell}, not redesigned — the standing decision this session is to
 * keep the old shaded look, after an earlier attempt to simplify it to flat colour was reversed once
 * the user clarified they actually liked the shading and only wanted load time and frame smoothness
 * fixed, not the picture itself changed.
 *
 * <p><b>What this pass does not yet do.</b> Shading reads north/west neighbours inside the same
 * level's own buffer only — a texel on a region's north or west edge has no neighbour to read and
 * draws flat there, exactly the same edge behaviour the old renderer had within one sheet. Carrying
 * shading <em>across</em> a region boundary is deliberately not attempted here: {@code RegionStore}
 * has no notion of "the region to my north" yet, and reaching for one here would be exactly the
 * premature abstraction this project has been trying to avoid. Revisit once a caller actually needs
 * seamless cross-region shading rather than guessing that it will.
 */
public final class Rasterizer {
    private Rasterizer() {
    }

    // Ported as-is from TerrainImage — same numbers, same look. See that class's own doc for why
    // each one is what it is; this is a port, not a redesign.
    //
    // Tried raising MACRO/MICRO and lowering SOFTNESS once, to make gentle slopes read more clearly
    // (JourneyMap's own render of the same ground showed visible shape where this stayed nearly flat)
    // — reverted immediately, confirmed by rendering the bench scene and looking at it: real Minecraft
    // "flat" ground has enough small per-block noise that any shading sensitive enough to reveal a
    // gentle macro slope also reveals that noise, at the same "steel wool" density this project already
    // fought and lost once on Topo. Raw per-pixel squash cannot serve both goals with one run distance;
    // see HANDOFF.md for the actual recommendation (macro shape from a separately-sampled wide term,
    // texture from a raw narrow one, not one term doing both).
    private static final int RELIEF_STEP_RUN = 1;
    private static final int RELIEF_MACRO_RUN = 4;
    private static final float RELIEF_STEP_WEIGHT = 0.32f;
    private static final float RELIEF_MACRO_WEIGHT = 0.08f;
    private static final float RELIEF_SOFTNESS = 0.30f;
    private static final float WATER_RELIEF_SHALLOW = 0.45f;
    private static final float WATER_RELIEF_DEEP = 0.12f;
    private static final float TERRAIN_DIM = 0.68f;

    /**
     * {@code Math.tanh}, read from a table instead of computed. The old renderer's own doc measured
     * this as the dominant cost of shading a square — a transcendental, called twice per texel,
     * tens of thousands of times per sheet. Same table, same reasoning, ported rather than redone.
     */
    private static final int SQUASH_STEPS = 1024;
    private static final float SQUASH_LIMIT = 4f;
    private static final float SQUASH_SCALE = SQUASH_STEPS / (2f * SQUASH_LIMIT);
    private static final float[] SQUASH = new float[SQUASH_STEPS + 1];

    static {
        for (int i = 0; i <= SQUASH_STEPS; i++) {
            SQUASH[i] = (float) Math.tanh(i / SQUASH_SCALE - SQUASH_LIMIT);
        }
    }

    /**
     * Rasterizes every texel of {@code columns} (one level of one region) into ARGB pixels. Unknown
     * ground draws fully transparent — never a guessed colour — so the caller's own "unsurveyed"
     * underlay shows through rather than this painting over it.
     */
    public static int[] rasterize(ColumnBuffer columns, BlockStyle style, int level) {
        int width = columns.width;
        int stride = Region.strideOf(level);
        int[] pixels = new int[columns.columns()];
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                pixels[columns.index(x, z)] = shadeCell(columns, style, x, z, stride);
            }
        }
        return pixels;
    }

    /**
     * The same picture as {@link #rasterize}, split across {@code executor} by row range. Rows never
     * read or write another row's pixels — shading only ever looks north/west within the same
     * buffer, and each row owns a disjoint slice of {@code pixels} — so this is exact, not an
     * approximation, and needs no locking.
     *
     * <p>Why this exists at all: single-threaded shading of a full 512×512 region costs roughly
     * 15-20&nbsp;ms in practice, measurably over the 8&nbsp;ms budget a live pan/zoom needs — and
     * that cost is dominated by per-cell branching and array reads that do not reduce further without
     * either SIMD (the Vector API, ruled out earlier this project for needing a JVM flag a normal
     * launcher cannot set) or real parallelism. A modern machine has cores to spare for this; using
     * them is the practical fix, and it is exact rather than a compromise.
     */
    public static int[] rasterizeParallel(ColumnBuffer columns, BlockStyle style, int level,
                                           java.util.concurrent.ExecutorService executor, int workers) {
        int width = columns.width;
        int stride = Region.strideOf(level);
        int[] pixels = new int[columns.columns()];
        int rowsPerTask = Math.max(1, (width + workers - 1) / workers);
        java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>();
        for (int z0 = 0; z0 < width; z0 += rowsPerTask) {
            int zStart = z0;
            int zEnd = Math.min(width, z0 + rowsPerTask);
            futures.add(executor.submit(() -> {
                for (int z = zStart; z < zEnd; z++) {
                    for (int x = 0; x < width; x++) {
                        pixels[columns.index(x, z)] = shadeCell(columns, style, x, z, stride);
                    }
                }
            }));
        }
        for (java.util.concurrent.Future<?> f : futures) {
            try {
                f.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            } catch (java.util.concurrent.ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
        return pixels;
    }

    /**
     * The same base colours as {@link #rasterize}, without the hillshade term — flat 2D fill, no
     * directional lighting. A contour sheet reads elevation from its lines; shading the fill under
     * it as well reads as relief-shaded 3D terrain, which is exactly the look the overlay TOPO mode
     * is not supposed to have (real topographic maps, JourneyMap's own TOPO included, are flat
     * colour underneath their contour lines).
     */
    public static int[] rasterizeFlat(ColumnBuffer columns, BlockStyle style) {
        int width = columns.width;
        int[] pixels = new int[columns.columns()];
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                pixels[columns.index(x, z)] = flatCell(columns, style, x, z);
            }
        }
        return pixels;
    }

    private static int flatCell(ColumnBuffer columns, BlockStyle style, int x, int z) {
        int idx = columns.index(x, z);
        short height = columns.height[idx];
        if (height == ColumnBuffer.NO_DATA) {
            return 0;
        }
        int waterDepth = columns.depthAt(idx);
        short block = columns.block[idx];
        short biome = columns.biome[idx];
        int waterTint = waterDepth > 0 ? blendedWaterTint(columns, style, x, z) : 0;
        int base = style.columnColour(block, biome, waterDepth, waterTint, height);

        int b = (base >> 16) & 0xFF;
        int g = (base >> 8) & 0xFF;
        int r = base & 0xFF;

        float lit = style.isHazard(block) ? 1f : TERRAIN_DIM;
        return 0xFF000000 | (light(b, lit) << 16) | (light(g, lit) << 8) | light(r, lit);
    }

    private static int shadeCell(ColumnBuffer columns, BlockStyle style, int x, int z, int stride) {
        int idx = columns.index(x, z);
        short height = columns.height[idx];
        if (height == ColumnBuffer.NO_DATA) {
            return 0;
        }
        int waterDepth = columns.depthAt(idx);
        short block = columns.block[idx];
        short biome = columns.biome[idx];
        int waterTint = waterDepth > 0 ? blendedWaterTint(columns, style, x, z) : 0;
        int base = style.columnColour(block, biome, waterDepth, waterTint, height);

        int b = (base >> 16) & 0xFF;
        int g = (base >> 8) & 0xFF;
        int r = base & 0xFF;

        float murk = Math.min(1f, waterDepth / 12f);
        float reliefScale = murk <= 0f ? 1f
                : WATER_RELIEF_SHALLOW - (WATER_RELIEF_SHALLOW - WATER_RELIEF_DEEP) * murk;
        float relief = 1f + (reliefOf(columns, x, z, stride) - 1f) * reliefScale;

        float lit = TERRAIN_DIM * relief;
        if (style.isHazard(block)) {
            // The old renderer blends toward full brightness by a soft per-texel weight; BlockStyle
            // only offers a yes/no here, so this is a binary version of the same highlight rather
            // than a lost feature — worth revisiting only if a style ever needs graded hazard.
            lit = 1f;
        }
        return 0xFF000000 | (light(b, lit) << 16) | (light(g, lit) << 8) | light(r, lit);
    }

    /** How far the water tint is averaged, in columns — matches the old renderer's own radius. */
    private static final int WATER_BLEND = 2;

    /**
     * The colour of the water over one column, averaged across its water neighbours. Ported from the
     * old renderer's own reasoning: a biome's water colour is a step function — a warm sea and a cold
     * one meet along a line with nothing in between — and painting it neat reproduces that step, which
     * from above looks like a coastline-shaped seam sitting out in open water where there is no coast
     * at all. Land neighbours never contribute a colour here (there is no "land water tint" to blend
     * in), and the buffer edge is a hard clamp rather than reaching into a region this call does not
     * have — the same discipline {@link #slopeOf} already follows for relief.
     */
    private static int blendedWaterTint(ColumnBuffer columns, BlockStyle style, int x, int z) {
        int width = columns.width;
        long bSum = 0;
        long gSum = 0;
        long rSum = 0;
        int n = 0;
        for (int dz = -WATER_BLEND; dz <= WATER_BLEND; dz++) {
            int nz = z + dz;
            if (nz < 0 || nz >= width) {
                continue;
            }
            for (int dx = -WATER_BLEND; dx <= WATER_BLEND; dx++) {
                int nx = x + dx;
                if (nx < 0 || nx >= width) {
                    continue;
                }
                int nIdx = columns.index(nx, nz);
                if (columns.depthAt(nIdx) <= 0) {
                    continue;
                }
                int tint = style.waterTint(columns.biome[nIdx]);
                bSum += (tint >> 16) & 0xFF;
                gSum += (tint >> 8) & 0xFF;
                rSum += tint & 0xFF;
                n++;
            }
        }
        if (n == 0) {
            // This column is itself under water — why else would this be called — but nothing within
            // the radius was, a lone puddle the clamp cut off from any wider body. Its own colour is
            // the honest answer, same fallback the old renderer used for the same case.
            return style.waterTint(columns.biome[columns.index(x, z)]);
        }
        return (int) (0xFF000000 | ((bSum / n) << 16) | ((gSum / n) << 8) | (rSum / n));
    }

    private static float reliefOf(ColumnBuffer columns, int x, int z, int stride) {
        float step = slopeOf(columns, x, z, RELIEF_STEP_RUN, stride * RELIEF_STEP_RUN);
        float macro = slopeOf(columns, x, z, RELIEF_MACRO_RUN, stride * RELIEF_MACRO_RUN);
        return 1f + RELIEF_STEP_WEIGHT * step + RELIEF_MACRO_WEIGHT * macro;
    }

    /**
     * Directional slope from North, West, and Northwest (315° cartographic lighting).
     */
    private static float slopeOf(ColumnBuffer columns, int x, int z, int texelOffset, int runBlocks) {
        short here = columns.floorAt(columns.index(x, z));
        short north = (z - texelOffset >= 0)
                ? columns.floorAt(columns.index(x, z - texelOffset)) : ColumnBuffer.NO_DATA;
        short west = (x - texelOffset >= 0)
                ? columns.floorAt(columns.index(x - texelOffset, z)) : ColumnBuffer.NO_DATA;
        short northWest = (z - texelOffset >= 0 && x - texelOffset >= 0)
                ? columns.floorAt(columns.index(x - texelOffset, z - texelOffset)) : ColumnBuffer.NO_DATA;

        float rise = 0f;
        float weights = 0f;
        if (north != ColumnBuffer.NO_DATA) {
            rise += (here - north);
            weights += 1.0f;
        }
        if (west != ColumnBuffer.NO_DATA) {
            rise += (here - west);
            weights += 1.0f;
        }
        if (northWest != ColumnBuffer.NO_DATA) {
            rise += (here - northWest) * 0.7071f;
            weights += 0.7071f;
        }
        if (weights == 0f) {
            return 0f;
        }
        return squash((rise / weights) / ((float) runBlocks * RELIEF_SOFTNESS));
    }

    private static float squash(float x) {
        if (x <= -SQUASH_LIMIT) {
            return -1f;
        }
        if (x >= SQUASH_LIMIT) {
            return 1f;
        }
        return SQUASH[(int) ((x + SQUASH_LIMIT) * SQUASH_SCALE + 0.5f)];
    }

    private static int light(int channel, float lit) {
        return Math.max(0, Math.min(255, Math.round(channel * lit)));
    }

    // --- TOPO: black-and-white vector-style contours, a separate render entirely from the shaded
    // satellite view above. See TerrainMips (the main mod's coloured-band version this replaces) for
    // where SEA_LEVEL and RELIEF_BAND came from — same numbers, so a contour line falls on the same
    // real elevation boundary the old coloured bands did.

    private static final int TOPO_SEA_LEVEL = 62;

    /** Overworld build height, -64 to 320 — the "world height" JM's own interval rule divides by. */
    private static final int TOPO_WORLD_HEIGHT = 384;

    /**
     * How many colour bands this map's own land ramp is treated as covering. JM's own dev (the
     * Discord screenshot this project checked against): "It is just the configured shift in height,
     * we draw the lines" — {@code world_height / colour_count = interval}. 32 was the number in
     * JM's own example config, not a fixed standard the rule requires; this is this map's own choice
     * of how many bands its ramp covers. Set back to 32 (interval 12) after 128 (interval 3), tried
     * for extra detail, was checked directly against a JourneyMap screenshot of the exact same
     * ground and turned out far denser than JM's own real output — flat ground there carried almost
     * no lines at all, which 128 could not reproduce no matter how the height field was smoothed.
     */
    private static final int TOPO_LAND_BANDS = 96;

    private static final int TOPO_BAND = TOPO_WORLD_HEIGHT / TOPO_LAND_BANDS;

    /** Every 5th line is an index contour — real cartographic convention, not a stylistic choice. */
    private static final int TOPO_INDEX_EVERY = 5;

    private static final int TOPO_LINE_MINOR = 0xFFA0A0A0;
    private static final int TOPO_LINE_INDEX = 0xFFFFFFFF;
    private static final int TOPO_WATER_SHALLOW = 0xFF17232E;
    private static final int TOPO_WATER_DEEP = 0xFF060B10;

    /** Hypsometric grayscale range for the land fill — low ground near-black, high ground lighter. */
    private static final int TOPO_LAND_LOW = 0x0B;
    private static final int TOPO_LAND_HIGH = 0x2E;

    /** Elevation span over which the grayscale gradient runs before it clamps flat. */
    private static final float TOPO_TINT_RANGE = 200f;

    /**
     * How far the height field is smoothed before contouring, in texels. Narrowed back down from 9:
     * that radius was fighting noise from two different sources at once — real small-scale terrain
     * texture, and canopy/clutter columns whose height was only ever an approximation (before
     * {@link ColumnBuffer#groundHeight} existed, this layer estimated ground under a tree by
     * averaging nearby real ground; now it reads the block actually there). With the second source
     * gone, 9 was smoothing real terrain shape away that no longer needed hiding — rounding off
     * ridges and softening slopes a printed contour sheet, tracing the true ground, would keep. A
     * little smoothing still earns its place (raw per-block noise is real and would still ring every
     * bump), just far less of it. This only ever smooths what the lines and the flat colour ramp
     * read — {@link #rasterize}, the Ground layer's own per-block pixel look, is untouched by it.
     */
    private static final int TOPO_SMOOTH_RADIUS = 2;

    /**
     * A contour map: a black-to-gray hypsometric fill with a line wherever a <em>smoothed</em>
     * elevation band differs from its north or west neighbour's — a boundary, not a blend, which is
     * what makes it read as a vector drawing rather than a photograph. Every fifth band is an index
     * contour, drawn brighter, so relative elevation reads at a glance without counting lines. An
     * axis with no surveyed neighbour draws no line there, the same "don't invent a boundary against
     * unknown ground" rule {@link #slopeOf} already follows for the shaded view above.
     */
    public static int[] rasterizeTopo(ColumnBuffer columns) {
        int width = columns.width;
        float[] smoothed = smoothHeights(columns, TOPO_SMOOTH_RADIUS);
        int[] pixels = new int[columns.columns()];
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                pixels[columns.index(x, z)] = topoCell(columns, smoothed, x, z, width);
            }
        }
        return pixels;
    }

    /**
     * A separable box blur — a horizontal pass then a vertical one, each summed per row rather than
     * an {@code O(radius^2)} lookup per texel. {@link ColumnBuffer#NO_DATA} columns never contribute
     * to an average and never receive a fabricated one either — an unsurveyed texel stays
     * {@link ColumnBuffer#NO_DATA} in the output, it only ever lends real neighbours a value.
     */
    private static float[] smoothHeights(ColumnBuffer columns, int radius) {
        int n = columns.columns();
        float[] source = new float[n];
        for (int i = 0; i < n; i++) {
            source[i] = columns.height[i];
        }
        return smoothFloatField(source, columns.width, radius);
    }

    /**
     * The separable box blur {@link #smoothHeights} does, generalised to any per-column float field
     * rather than always reading {@link ColumnBuffer#height} directly — so the Topo layer can smooth
     * {@link ColumnBuffer#groundHeight} ({@link #smoothGroundHeights}) through the exact same maths
     * instead of duplicating it. {@link ColumnBuffer#NO_DATA} is the one sentinel both callers agree
     * never to average across or fabricate.
     */
    private static float[] smoothFloatField(float[] source, int width, int radius) {
        int n = source.length;
        float[] hSum = new float[n];
        int[] hCount = new int[n];
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                float sum = 0f;
                int count = 0;
                for (int dx = -radius; dx <= radius; dx++) {
                    int nx = x + dx;
                    if (nx < 0 || nx >= width) {
                        continue;
                    }
                    float h = source[z * width + nx];
                    if (h == ColumnBuffer.NO_DATA) {
                        continue;
                    }
                    sum += h;
                    count++;
                }
                int idx = z * width + x;
                hSum[idx] = sum;
                hCount[idx] = count;
            }
        }
        float[] out = new float[n];
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                int idx = z * width + x;
                if (source[idx] == ColumnBuffer.NO_DATA) {
                    out[idx] = ColumnBuffer.NO_DATA;
                    continue;
                }
                float sum = 0f;
                int count = 0;
                for (int dz = -radius; dz <= radius; dz++) {
                    int nz = z + dz;
                    if (nz < 0 || nz >= width) {
                        continue;
                    }
                    int nIdx = nz * width + x;
                    sum += hSum[nIdx];
                    count += hCount[nIdx];
                }
                out[idx] = count > 0 ? sum / count : ColumnBuffer.NO_DATA;
            }
        }
        return out;
    }

    private static int topoCell(ColumnBuffer columns, float[] smoothed, int x, int z, int width) {
        int idx = columns.index(x, z);
        if (columns.height[idx] == ColumnBuffer.NO_DATA) {
            return 0;
        }
        float height = smoothed[idx];
        int band = topoBand(height);
        int strength = Math.max(
                topoLineStrength(smoothed, x, z - 1, width, band),
                topoLineStrength(smoothed, x - 1, z, width, band));
        if (strength == 2) {
            return TOPO_LINE_INDEX;
        }
        if (strength == 1) {
            return TOPO_LINE_MINOR;
        }
        int waterDepth = columns.depthAt(idx);
        return waterDepth > 0 ? topoWaterWash(waterDepth) : topoLandTint(height);
    }

    private static int topoBand(float height) {
        return (int) Math.floor((height - TOPO_SEA_LEVEL) / TOPO_BAND);
    }

    /** 0 = no boundary here, 1 = an ordinary contour, 2 = an index contour (every 5th). */
    private static int topoLineStrength(float[] smoothed, int nx, int nz, int width, int band) {
        if (nx < 0 || nz < 0 || nx >= width || nz >= width) {
            return 0;
        }
        float neighbourHeight = smoothed[nz * width + nx];
        if (neighbourHeight == ColumnBuffer.NO_DATA) {
            return 0;
        }
        int neighbourBand = topoBand(neighbourHeight);
        if (neighbourBand == band) {
            return 0;
        }
        int higher = Math.max(band, neighbourBand);
        return Math.floorMod(higher, TOPO_INDEX_EVERY) == 0 ? 2 : 1;
    }

    private static int topoLandTint(float height) {
        float t = (height - TOPO_SEA_LEVEL) / TOPO_TINT_RANGE;
        t = Math.max(0f, Math.min(1f, t));
        int g = Math.round(TOPO_LAND_LOW + (TOPO_LAND_HIGH - TOPO_LAND_LOW) * t);
        return 0xFF000000 | (g << 16) | (g << 8) | g;
    }

    // --- TOPO hypsometric: coloured by elevation, the JourneyMap-reference look the user actually
    // asked to match — a smooth colour gradient by height (green low ground through tan and brown to
    // the highest peaks, blue graded by water depth) with contour lines darkening the fill rather than
    // replacing it, so the gradient still reads through a line instead of the line cutting a flat
    // stripe out of it. Distinct from both rasterizeTopo (monochrome, no colour at all) and
    // rasterizeTopoOverlay (real block/biome colour, not an elevation ramp) below — three genuinely
    // different looks this project tried in sequence, kept side by side rather than each replacing
    // the last, since nothing here says only one may exist.

    // JourneyMap's own rule, not just its look: JM's land colour is a plain list of N evenly-spaced
    // stops covering the whole world height, N tied to the same world_height/interval arithmetic as
    // its contour spacing (its own dev, in the Discord screenshot this was checked against: "just the
    // configured shift in height, we draw the lines... not anything really special about it") — which
    // collapses to a straight linear gradient from sea level to the world ceiling, not the curated,
    // uneven-spaced ramp this used to be (denser stops near sea level, wider gaps higher up). Two
    // stops here produce exactly that: everything in between is {@link #rampColour}'s own linear
    // interpolation, the same maths a longer evenly-spaced list would produce. The surface reads
    // simpler because the rule is simpler — the contour lines drawn over it are what were asked to
    // stay sharp, and drawing them over a plainer gradient is what lets them actually read that way.
    // Minimalist line-art — the user's own reference image: solid black everywhere, land and water
    // alike (asked for by name, not a placeholder), white contour lines and nothing else. No
    // elevation ramp at all now — "biết chỗ nào cao chỗ nào thấp bằng cách đi theo đường" is the
    // whole point of this look, not a limitation of it.
    // Softened from near-black/near-white — full contrast made sense as pure line art, but leaves no
    // headroom for icons drawn on top later to still stand out against either the fill or the lines.
    private static final int TOPO_BACKGROUND = 0xFF242424;

    /** Contour ink — near-white, the one thing that reads against the flat black background. */
    private static final int TOPO_INK = 0xFFC8C8C8;
    private static final float TOPO_INK_MIX_MINOR = 0.85f;
    private static final float TOPO_INK_MIX_INDEX = 0.97f;

    /**
     * The same smoothed-height contour lines as {@link #rasterizeTopo}, drawn over a flat
     * elevation-coloured gradient instead of a real block colour — no hillshade under it at all, per
     * JourneyMap's own documented rule (see {@link #hypsoCell}'s own doc). A line blends toward real
     * contour ink rather than just darkening whatever colour is already there, which is what makes it
     * read as drawn rather than merely shadowed — an index contour blends further than an ordinary
     * one, and a soft one-sided shadow falls on its lower side, the same graded emphasis and sense of
     * depth a real topographic sheet gets from its own line work rather than from shading the ground.
     */
    public static int[] rasterizeHypsometric(ColumnBuffer columns) {
        int width = columns.width;
        float[] smoothed = smoothGroundHeights(columns, TOPO_SMOOTH_RADIUS);
        int[] pixels = new int[columns.columns()];
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                pixels[columns.index(x, z)] = hypsoCell(columns, smoothed, x, z, width);
            }
        }
        return pixels;
    }

    /** {@link #smoothHeights}, but averaging {@link ColumnBuffer#groundHeight} — Topo's own field. */
    private static float[] smoothGroundHeights(ColumnBuffer columns, int radius) {
        int n = columns.columns();
        float[] source = new float[n];
        for (int i = 0; i < n; i++) {
            source[i] = columns.groundHeight[i];
        }
        return smoothFloatField(source, columns.width, radius);
    }

    /**
     * No hillshade, no elevation ramp, no shadow — the flattest reading of the reference image the
     * user actually asked for: solid black ground (land and water drawn identically, on purpose — a
     * shape a line does not cross is a shape, whether it happens to be a lake or a hillside), white
     * contour lines, nothing else competing with them. The soft one-sided shadow this layer briefly
     * carried made sense against a lit elevation ramp, blending toward a dark ink; against flat black
     * with white ink it would only fight the line for attention, so it goes with the ramp it was
     * built for rather than being adapted to a look it was never meant for.
     */
    private static int hypsoCell(ColumnBuffer columns, float[] smoothed, int x, int z, int width) {
        int idx = columns.index(x, z);
        if (columns.height[idx] == ColumnBuffer.NO_DATA) {
            return 0;
        }
        float height = smoothed[idx];
        boolean waterHere = columns.depthAt(idx) > 0;
        int base = TOPO_BACKGROUND;

        // A coastline, not a colour — the one place land and water still read as different things in
        // an otherwise flat black fill: drawn exactly like an elevation contour, the same line style,
        // just at the water/land boundary instead of an interval crossing. On the water side this is
        // the only line ever drawn there at all — see the elevation contour skip below for why a lake's
        // own surface still traces no lines of its own.
        int coast = Math.max(
                coastLineStrength(columns, x, z - 1, width, waterHere),
                coastLineStrength(columns, x - 1, z, width, waterHere));

        if (waterHere) {
            if (coast > 0) {
                return lerpColour(base, TOPO_INK, TOPO_INK_MIX_MINOR);
            }
            // A fine stipple, not a colour, so water still reads as water even far from any shore —
            // a staggered dot grid rather than a straight one, so it does not read as a screen-door
            // pattern at a glance. Same restraint as the coastline: texture, not a second fill colour.
            return isWaterStipple(x, z) ? lerpColour(base, TOPO_INK, TOPO_WATER_STIPPLE_MIX) : base;
        }

        // Which band this column falls in, plain integer division — the line is drawn wherever a
        // neighbour falls in a different band, not wherever a computed distance-to-isoline crosses
        // some threshold. This is the simpler, more literal reading of JM's own rule ("just the
        // configured shift in height, we draw the lines"): slice the terrain into flat steps first,
        // then draw the boundary between two different steps, the way a contour model is physically
        // built up from stacked flat layers rather than computed as a smooth mathematical surface.
        // No sub-pixel interpolation, no gradient estimate — a boundary is either there or it isn't,
        // decided the same way for every pixel regardless of how steep or shallow the ground is there.
        int band = topoBand(height);
        int strength = Math.max(coast, Math.max(
                topoLineStrength(smoothed, x, z - 1, width, band),
                topoLineStrength(smoothed, x - 1, z, width, band)));
        if (strength == 0) {
            return base;
        }
        float mix = strength == 2 ? TOPO_INK_MIX_INDEX : TOPO_INK_MIX_MINOR;
        return lerpColour(base, TOPO_INK, mix);
    }

    /** 1 when the neighbour is on the other side of the land/water boundary, 0 otherwise. */
    private static int coastLineStrength(ColumnBuffer columns, int nx, int nz, int width, boolean hereWater) {
        if (nx < 0 || nz < 0 || nx >= width || nz >= width) {
            return 0;
        }
        int nIdx = columns.index(nx, nz);
        if (columns.height[nIdx] == ColumnBuffer.NO_DATA) {
            return 0;
        }
        return (columns.depthAt(nIdx) > 0) != hereWater ? 1 : 0;
    }

    /** How far apart, in texels, the water stipple's dots sit along each row. */
    private static final int TOPO_WATER_STIPPLE_SPACING = 3;

    /** How strongly a stipple dot blends toward the ink — light, texture rather than a second fill. */
    private static final float TOPO_WATER_STIPPLE_MIX = 0.35f;

    /**
     * A staggered dot grid — every other row of dots offset half a spacing sideways, brick-fashion,
     * rather than lined up into columns. A plain square grid reads as a mechanical screen-door pattern
     * at a glance; staggering it is the same trick engraved stipple shading has always used to read as
     * texture instead of a grid.
     */
    private static boolean isWaterStipple(int x, int z) {
        int row = Math.floorDiv(z, TOPO_WATER_STIPPLE_SPACING);
        int offset = Math.floorMod(row, 2) == 0 ? 0 : TOPO_WATER_STIPPLE_SPACING / 2;
        return Math.floorMod(x + offset, TOPO_WATER_STIPPLE_SPACING) == 0
                && Math.floorMod(z, TOPO_WATER_STIPPLE_SPACING) == 0;
    }

    /**
     * How much of a contour line covers this texel, as {@code {coverage 0..1, isIndexLine 0 or 1}} —
     * the practical, raster-native equivalent of marching squares. Rather than discretising the
     * height field into grid cells and building line-segment geometry (marching squares proper, which
     * still has to be rasterised back onto a pixel grid afterwards, and has to special-case a saddle
     * point where a cell's corners disagree two ways at once), this reads the crossing distance
     * straight off the local gradient of the already-smoothed height field: a texel exactly on the
     * interpolated isoline is distance zero, and one gradient-magnitude of height away in world space
     * is, by the definition of a gradient, one texel away on screen — precisely the point marching
     * squares would linearly interpolate an edge crossing to, arrived at without ever building a grid
     * of cells. Antialiasing falls out for free: coverage fades smoothly with distance instead of a
     * pixel being flatly in or out of a boundary texel, which is what actually reads as "the terrain's
     * own edge" rather than "a neighbour comparison" at native resolution.
     */
    private static float[] contourCoverage(float[] smoothed, int x, int z, int width, float halfWidth) {
        int idx = z * width + x;
        float hC = smoothed[idx];
        float hE = smoothedOrSelf(smoothed, x + 1, z, width, hC);
        float hW = smoothedOrSelf(smoothed, x - 1, z, width, hC);
        float hN = smoothedOrSelf(smoothed, x, z - 1, width, hC);
        float hS = smoothedOrSelf(smoothed, x, z + 1, width, hC);
        float gradX = (hE - hW) / 2f;
        float gradZ = (hS - hN) / 2f;
        float gradMag = (float) Math.sqrt(gradX * gradX + gradZ * gradZ);

        float bandPos = (hC - TOPO_SEA_LEVEL) / TOPO_BAND;
        float nearestBand = Math.round(bandPos);
        float signedBandOffset = bandPos - nearestBand;
        float distBlocks = Math.abs(signedBandOffset) * TOPO_BAND;
        float distTexels = gradMag > 0.001f ? distBlocks / gradMag : Float.MAX_VALUE;

        float coverage = Math.max(0f, 1f - distTexels / halfWidth);
        boolean isIndex = Math.floorMod((int) nearestBand, TOPO_INDEX_EVERY) == 0;
        // Signed so the caller can tell which side of the line a texel sits on — negative is the
        // lower-elevation side, which is where a real cartographic shadow falls (light from "above").
        return new float[]{coverage, isIndex ? 1f : 0f, Math.signum(signedBandOffset)};
    }

    private static float smoothedOrSelf(float[] smoothed, int x, int z, int width, float fallback) {
        if (x < 0 || x >= width || z < 0 || z >= width) {
            return fallback;
        }
        float h = smoothed[z * width + x];
        return h == ColumnBuffer.NO_DATA ? fallback : h;
    }

    /** Linearly interpolated colour along a ramp of (position, colour) stops, clamped at the ends. */
    private static int rampColour(int[] stops, int[] colours, float value) {
        if (value <= stops[0]) {
            return colours[0];
        }
        int last = stops.length - 1;
        if (value >= stops[last]) {
            return colours[last];
        }
        for (int i = 0; i < last; i++) {
            if (value <= stops[i + 1]) {
                float t = (value - stops[i]) / (float) (stops[i + 1] - stops[i]);
                return lerpColour(colours[i], colours[i + 1], t);
            }
        }
        return colours[last];
    }

    private static int lerpColour(int from, int to, float t) {
        int fr = (from >> 16) & 0xFF;
        int fg = (from >> 8) & 0xFF;
        int fb = from & 0xFF;
        int tr = (to >> 16) & 0xFF;
        int tg = (to >> 8) & 0xFF;
        int tb = to & 0xFF;
        int r = Math.round(fr + (tr - fr) * t);
        int g = Math.round(fg + (tg - fg) * t);
        int b = Math.round(fb + (tb - fb) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    // --- TOPO overlay: JourneyMap's own convention, not the poster-style black-and-white above.
    // Real terrain colour survives; contour lines are drawn on top of it rather than replacing the
    // palette. The elevation logic is identical to rasterizeTopo (same smoothing, same bands, same
    // index-every-5th rule) — only what a boundary is painted as differs.

    private static final int TOPO_OVERLAY_LINE_MINOR = 0xFF6B4F33;
    private static final int TOPO_OVERLAY_LINE_INDEX = 0xFF3E2C1A;

    /**
     * The flat "Ground" colours from {@link #rasterizeFlat} — no hillshade — with contour lines
     * painted on top: real terrain colour survives, elevation reads entirely from the lines, the
     * same convention JourneyMap's own TOPO mode and every real topographic map use. Reuses
     * {@link #smoothHeights} and the same band logic as {@link #rasterizeTopo}, so a line here falls
     * on the identical real elevation boundary the black-and-white version draws; only the paint
     * differs.
     */
    public static int[] rasterizeTopoOverlay(ColumnBuffer columns, BlockStyle style) {
        int width = columns.width;
        int[] pixels = rasterizeFlat(columns, style);
        float[] smoothed = smoothHeights(columns, TOPO_SMOOTH_RADIUS);
        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                int idx = columns.index(x, z);
                if (columns.height[idx] == ColumnBuffer.NO_DATA) {
                    continue;
                }
                int band = topoBand(smoothed[idx]);
                int strength = Math.max(
                        topoLineStrength(smoothed, x, z - 1, width, band),
                        topoLineStrength(smoothed, x - 1, z, width, band));
                if (strength == 2) {
                    pixels[idx] = TOPO_OVERLAY_LINE_INDEX;
                } else if (strength == 1) {
                    pixels[idx] = TOPO_OVERLAY_LINE_MINOR;
                }
            }
        }
        return pixels;
    }

    private static int topoWaterWash(int waterDepth) {
        float murk = Math.min(1f, waterDepth / 12f);
        int sr = (TOPO_WATER_SHALLOW >> 16) & 0xFF, sg = (TOPO_WATER_SHALLOW >> 8) & 0xFF, sb = TOPO_WATER_SHALLOW & 0xFF;
        int dr = (TOPO_WATER_DEEP >> 16) & 0xFF, dg = (TOPO_WATER_DEEP >> 8) & 0xFF, db = TOPO_WATER_DEEP & 0xFF;
        int r = Math.round(sr + (dr - sr) * murk);
        int g = Math.round(sg + (dg - sg) * murk);
        int b = Math.round(sb + (db - sb) * murk);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
