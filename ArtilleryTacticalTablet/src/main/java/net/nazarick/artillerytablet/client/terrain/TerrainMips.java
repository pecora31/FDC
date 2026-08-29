package net.nazarick.artillerytablet.client.terrain;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.terrain.TerrainTile;

/**
 * A tile reduced by halves, each level the average of the one above it.
 *
 * <p>The map used to draw a wide view by <em>sampling</em> the tile data at a stride — reading one
 * column in every four, or one in sixty-four, and throwing the rest away. At the widest zoom that
 * meant each pixel was decided by four tenths of one per cent of the ground beneath it, so
 * neighbouring pixels disagreed at random and coastlines dissolved into speckle. It was not a
 * shortage of pixels; every zoom step holds much the same number. It was that each pixel was a coin
 * toss.
 *
 * <p>Averaging by halves fixes that completely: at every level, <em>every</em> block under a pixel
 * contributes to it with equal weight. And it is cheaper than what it replaces, which is the part
 * worth stating plainly — a level costs four reads per texel from the level above and each level is
 * a quarter the size, so the whole chain costs about a third more than one pass over the tile, once,
 * for good. The sampling it replaces cost sixty-four reads per texel <em>on every rebuild</em> and
 * produced a worse picture.
 *
 * <p>This is the shape the map mod this was measured against uses, confirmed in its own bytecode: a
 * chain of images per region, each built by blending a two-by-two box of the one above, in linear
 * light rather than in the packed values.
 */
@OnlyIn(Dist.CLIENT)
public final class TerrainMips {
    /** Levels below the tile itself: 32, 16, 8, 4, 2 and 1 across. */
    public static final int LEVELS = 6;

    /** Colour per texel, in the byte order the palette hands back. Alpha zero means unsurveyed. */
    private final int[][] colour = new int[LEVELS][];

    /** Floor height per texel, or {@link TerrainTile#NO_DATA}. */
    private final short[][] floor = new short[LEVELS][];

    /**
     * Water depth per texel, averaged like everything else.
     *
     * <p>Carried through the chain rather than left at the finest level, which is what it was at
     * first on the reasoning that a sea bed's relief must be negligible once a texel covers sixteen
     * blocks. That was wrong, and visibly so: the sea bed is the roughest ground in the world, and
     * dropping the damping made an ocean erupt into contour lines at the exact zoom step where the
     * level changes. The colour survives averaging; the reason to <em>soften</em> it has to survive
     * with it.
     */
    private final byte[][] depth = new byte[LEVELS][];

    /**
     * How much of a texel is ground painted as a hazard, 0 to 255.
     *
     * <p>Carried through the chain for the same reason the depth above it is, and the note there is
     * the whole argument: a reason to treat a texel differently has to survive the reduction along
     * with the colour it applies to, or the map changes appearance at the zoom step where the level
     * changes. Lava drawn flat and bright at one zoom and shaded at the next would be that fault
     * again, in the one place on the map that exists to be unmissable.
     */
    private final byte[][] hazard = new byte[LEVELS][];

    private TerrainMips() {
    }

    /** Width of a level, counting the tile itself as level zero. */
    public static int widthOf(int level) {
        return TerrainTile.SIDE >> level;
    }

    /**
     * Which level a stride wants. Level {@code k} holds one texel per {@code 2^k} blocks, so this is
     * simply the log — and it is exact for every stride the drawing uses, since those are powers of
     * the level factor, which is itself a power of two.
     */
    public static int levelForStride(int stride) {
        return Math.min(LEVELS, Integer.numberOfTrailingZeros(Math.max(1, stride)));
    }

    public int colourAt(int level, int x, int z) {
        int width = widthOf(level);
        return colour[level - 1][z * width + x];
    }

    public short floorAt(int level, int x, int z) {
        int width = widthOf(level);
        return floor[level - 1][z * width + x];
    }

    /** Averaged water depth over this texel, unsigned. */
    public int depthAt(int level, int x, int z) {
        int width = widthOf(level);
        return depth[level - 1][z * width + x] & 0xFF;
    }

    /** How much of this texel is hazard ground, unsigned. */
    public int hazardAt(int level, int x, int z) {
        int width = widthOf(level);
        return hazard[level - 1][z * width + x] & 0xFF;
    }

    /**
     * Builds the whole chain for a tile.
     *
     * <p>Level zero is not kept. The tile already is level zero, and holding a second copy of it
     * would double what the cache costs for nothing — so it is built into a scratch buffer, used to
     * seed level one, and dropped.
     */
    public static TerrainMips of(TerrainTile tile) {
        TerrainMips mips = new TerrainMips();

        int width = TerrainTile.SIDE;
        int[] parentColour = new int[width * width];
        short[] parentFloor = new short[width * width];
        byte[] parentDepth = new byte[width * width];
        byte[] parentHazard = new byte[width * width];

        // Two savings, and both of them are about not computing an answer nobody will read.
        //
        // The water tint is a twenty-five sample average per water column — easily the dearest
        // thing in this loop, and an ocean tile is four thousand water columns. The thermal and
        // relief palettes do not use it at all, so under those it is not computed; and a tile that
        // holds one biome, which most do and every ocean does, has one answer for all of it rather
        // than four thousand copies of the same average.
        boolean tinting = filter == Filter.NONE;
        short only = tile.biome[0];
        boolean oneBiome = true;
        for (int i = 1; i < tile.biome.length; i++) {
            if (tile.biome[i] != only) {
                oneBiome = false;
                break;
            }
        }
        int sameTint = oneBiome ? waterTint(only) : 0;

        for (int z = 0; z < width; z++) {
            for (int x = 0; x < width; x++) {
                int i = TerrainTile.index(x, z);
                short surface = tile.height[i];
                if (surface == TerrainTile.NO_DATA) {
                    parentColour[i] = 0;
                    parentFloor[i] = TerrainTile.NO_DATA;
                    continue;
                }
                int here = tile.depthAt(i);
                int block = tile.blockAt(i);
                int tint = 0;
                if (tinting && here > 0) {
                    tint = oneBiome ? sameTint : blendedWaterTint(tile, x, z);
                }
                parentColour[i] = groundColour(block, tile.biome[i], here, tint, surface);
                parentFloor[i] = (short) (surface - here);
                parentDepth[i] = (byte) here;
                parentHazard[i] = (byte) (BlockPalette.isHazard(block) ? 255 : 0);
            }
        }

        for (int level = 1; level <= LEVELS; level++) {
            int half = widthOf(level);
            int[] downColour = new int[half * half];
            short[] downFloor = new short[half * half];
            byte[] downDepth = new byte[half * half];
            byte[] downHazard = new byte[half * half];

            for (int z = 0; z < half; z++) {
                for (int x = 0; x < half; x++) {
                    int parentWidth = half * 2;
                    int a = (z * 2) * parentWidth + x * 2;
                    int b = a + 1;
                    int c = a + parentWidth;
                    int d = c + 1;
                    int at = z * half + x;

                    downColour[at] = average(
                            parentColour[a], parentColour[b], parentColour[c], parentColour[d]);
                    downFloor[at] = averageHeight(
                            parentFloor[a], parentFloor[b], parentFloor[c], parentFloor[d]);
                    // Depth is averaged over the texels that hold ground, matching the floor: a
                    // shoreline texel that is half beach and half shallows should come out shallow,
                    // not as deep as the water in it.
                    downDepth[at] = averageOverGround(parentFloor, parentDepth, a, b, c, d);
                    // Half a texel of lava reads as half as strong a mark, which is the same rule
                    // the depth beside it follows and the same one the colour follows.
                    downHazard[at] = averageOverGround(parentFloor, parentHazard, a, b, c, d);
                }
            }

            mips.colour[level - 1] = downColour;
            mips.floor[level - 1] = downFloor;
            mips.depth[level - 1] = downDepth;
            mips.hazard[level - 1] = downHazard;
            parentColour = downColour;
            parentFloor = downFloor;
            parentDepth = downDepth;
            parentHazard = downHazard;
        }

        return mips;
    }

    /**
     * Blends four texels into one, in linear light.
     *
     * <p>The packed values are not proportional to light — they are bent by roughly the 2.2 power
     * that displays expect. Averaging them as they stand makes every reduction darker and muddier
     * than the ground it stands for, which is what a zoomed-out map that has "gone grey" usually is.
     * Straightening them first, averaging, and bending them back keeps a coast the colour of a coast
     * however far out the view goes.
     *
     * <p>Unsurveyed texels carry no colour and are left out of the average rather than counted as
     * black, so the edge of known ground stays an edge instead of fading into a dark fringe.
     */
    private static int average(int a, int b, int c, int d) {
        float sum0 = 0f;
        float sum1 = 0f;
        float sum2 = 0f;
        int known = 0;

        for (int packed : new int[]{a, b, c, d}) {
            if ((packed >>> 24) == 0) {
                continue;
            }
            sum0 += Light.LINEAR[(packed >> 16) & 0xFF];
            sum1 += Light.LINEAR[(packed >> 8) & 0xFF];
            sum2 += Light.LINEAR[packed & 0xFF];
            known++;
        }
        if (known == 0) {
            return 0;
        }

        return 0xFF000000
                | (Light.encode(sum0 / known) << 16)
                | (Light.encode(sum1 / known) << 8)
                | Light.encode(sum2 / known);
    }

    /**
     * A per-column value over the four texels that have ground, ignoring the ones that have none.
     *
     * <p>Used for both the water depth and the hazard fraction. Counting the empty texels would drag
     * a shoreline towards dry and a lake's edge towards bare, when what those texels mean is "no
     * information", not "zero".
     */
    private static byte averageOverGround(short[] floors, byte[] values, int a, int b, int c, int d) {
        int sum = 0;
        int known = 0;
        for (int at : new int[]{a, b, c, d}) {
            if (floors[at] != TerrainTile.NO_DATA) {
                sum += values[at] & 0xFF;
                known++;
            }
        }
        return known == 0 ? 0 : (byte) Math.min(255, Math.round(sum / (float) known));
    }

    private static short averageHeight(short a, short b, short c, short d) {
        int sum = 0;
        int known = 0;
        for (short h : new short[]{a, b, c, d}) {
            if (h != TerrainTile.NO_DATA) {
                sum += h;
                known++;
            }
        }
        return known == 0 ? TerrainTile.NO_DATA : (short) Math.round(sum / (float) known);
    }

    // ---- ground colour -------------------------------------------------------------------------
    //
    // Resolved here, once, for both the chain above and the finest level drawn straight from the
    // tile. It has to be one function: a mip level averages finished colours, so if the two ends
    // disagreed about what a column's colour is, the map would change appearance at the zoom step
    // where the level changes. Relief and the overall dimming are deliberately NOT in here — those
    // depend on the neighbourhood and on the level, and are applied when drawing.

    /** Depth at which water has swallowed the floor entirely. */
    private static final int WATER_OPAQUE_AT = 8;

    /** How much water colour even the shallowest water carries. */
    private static final float WATER_MIN_MIX = 0.45f;

    /** How much deep water darkens on top of that, so an ocean has a near edge and a far one. */
    private static final float WATER_DARKEN = 0.40f;

    /** The biome colours the map palette already has baked into it — plains grass and plains leaf. */
    private static final int GRASS_REFERENCE = 0x91BD59;
    private static final int FOLIAGE_REFERENCE = 0x77AB2F;

    // A table, one entry per biome, filled the first time each is asked for.
    //
    // This was a memo of ONE biome, on the reasoning that biomes come in patches so a run of columns
    // nearly always shares one. True of a run; false of a boundary, and a tile that straddles two
    // biomes flips the memo back and forth. A miss is not cheap — it reaches through Minecraft to
    // the level, to the registry, to the biome, and asks it for three colours — and this is called
    // once per column of every square built, eighteen thousand times.
    //
    // The same shape as BlockPalette, for the same reason: a lookup that is asked per pixel has to
    // be an array read.

    private static int[] tintGrass = new int[0];
    private static int[] tintFoliage = new int[0];
    private static int[] tintWater = new int[0];
    /** Zero unresolved, 1 resolved and usable, -1 resolved and not usable. */
    private static byte[] tintState = new byte[0];

    /**
     * Guards the biome tint tables above against concurrent access from the mapengine bridge's
     * background rasterize/pyramid work — the same reason the old tile cache had a lock here, just
     * owned by this class now that the old cache is gone.
     */
    private static final Object BIOME_LOCK = new Object();

    /** Forgets the table, for when a change of world makes a biome id mean something else. */
    public static void forgetBiomes() {
        synchronized (BIOME_LOCK) {
            tintGrass = new int[0];
            tintFoliage = new int[0];
            tintWater = new int[0];
            tintState = new byte[0];
        }
    }

    /**
     * A column's colour before any shading: what the block looks like from above, tinted to its
     * biome, with water laid over it to the depth recorded.
     */
    public static int groundColour(int blockId, short biomeId, int depth, int waterTint,
                                   int surfaceY) {
        // The Ground layer draws the coarse map-palette swatch rather than each block's own averaged
        // texture: grass/dirt, stone, gravel, sand and wood/leaves each collapse to one tone apiece
        // (the map palette's own sixty-two categories, still biome-tinted), rather than every distinct
        // block having its own shade — different materials stay visually distinct from one another
        // (ground vs. stone vs. trees), just not distinct *within* a material. That leaves the
        // hillshade term (applied afterwards, in Rasterizer) as the only thing varying brightness
        // cell-to-cell within one material, reading as terrain relief instead of getting lost among
        // Fine per-texture averaged colour: every material (granite, diorite, andesite, cobblestone,
        // wood types, dirt types, sandstones) carries its true, faithful in-game appearance.
        int packed = BlockPalette.colourOf(blockId);
        int b = (packed >> 16) & 0xFF;
        int g = (packed >> 8) & 0xFF;
        int r = packed & 0xFF;

        byte kind = BlockPalette.tintOf(blockId);
        if (kind != BlockPalette.TINT_NONE && known(biomeId)) {
            int tint = tintColour(kind, biomeId);
            if (kind == BlockPalette.TINT_FOLIAGE) {
                // Base un-tinted foliage reference is FOLIAGE_REFERENCE (0x77AB2F in standard order, swapped for texture).
                // Scale directly from FOLIAGE_REFERENCE so Red, Green, and Blue channels
                // accurately reflect the biome's foliage climate (e.g. golden savanna olive acacia vs emerald forest).
                int ref = swapForTexture(FOLIAGE_REFERENCE);
                int refB = (ref >> 16) & 0xFF;
                int refG = (ref >> 8) & 0xFF;
                int refR = ref & 0xFF;
                int tintB = (tint >> 16) & 0xFF;
                int tintG = (tint >> 8) & 0xFF;
                int tintR = tint & 0xFF;
                b = scale(refB, tintB, refB);
                g = scale(refG, tintG, refG);
                r = scale(refR, tintR, refR);
                // Realistic natural tree canopy depth
                b = Math.round(b * 0.82f);
                g = Math.round(g * 0.82f);
                r = Math.round(r * 0.82f);
            } else if (BlockPalette.isPreTinted(blockId)) {
                // The palette's grass is already the green of plains, so this scales it from that
                // reference to the biome's rather than multiplying, which would darken it twice.
                b = scale(b, (tint >> 16) & 0xFF, reference(kind, 16));
                g = scale(g, (tint >> 8) & 0xFF, reference(kind, 8));
                r = scale(r, tint & 0xFF, reference(kind, 0));
            } else {
                // A texture the game tints is grey and waiting for exactly this multiply. It is the
                // same arithmetic the block renderer does, and doing anything else here would make
                // the map disagree with the world it is a map of.
                b = multiply(b, (tint >> 16) & 0xFF);
                g = multiply(g, (tint >> 8) & 0xFF);
                r = multiply(r, tint & 0xFF);
            }
        }

        if (depth > 0) {
            // Handed in, not looked up. The colour of water is the one part of a column that its
            // neighbours have a say in, and the caller is the only one holding them.
            int water = waterTint;
            float murk = Math.min(1f, depth / (float) WATER_OPAQUE_AT);
            float mix = WATER_MIN_MIX + (1f - WATER_MIN_MIX) * murk;
            b = blend(b, (water >> 16) & 0xFF, mix);
            g = blend(g, (water >> 8) & 0xFF, mix);
            r = blend(r, water & 0xFF, mix);

            float sink = 1f - WATER_DARKEN * murk;
            b = Math.round(b * sink);
            g = Math.round(g * sink);
            r = Math.round(r * sink);
        }

        if (filter == Filter.THERMAL) {
            return thermalOf(r, g, b, BlockPalette.isHazard(blockId));
        }
        if (filter == Filter.RELIEF) {
            return reliefOf(surfaceY, depth, BlockPalette.isHazard(blockId));
        }

        // JourneyMap-grade earthy matte tone: subtly dampen oversaturation (16% towards luminance)
        float lum = 0.299f * r + 0.587f * g + 0.114f * b;
        r = Math.max(0, Math.min(255, Math.round(r + (lum - r) * 0.16f)));
        g = Math.max(0, Math.min(255, Math.round(g + (lum - g) * 0.16f)));
        b = Math.max(0, Math.min(255, Math.round(b + (lum - b) * 0.16f)));

        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    /**
     * Whether ground is coloured as a thermal picture rather than as itself.
     *
     * <p>Here, and not at the moment of drawing, because a column's colour is decided in exactly one
     * place and this is it. It cannot be done at the draw either way: turning a picture monochrome
     * means adding its three channels together, and the only thing a draw call can do to a colour
     * without a shader of its own is scale each channel on its own — which can tint an image but can
     * never desaturate one.
     *
     * <p>Volatile because the bake threads read it and the render thread sets it.
     */
    private static volatile Filter filter = Filter.NONE;

    /**
     * How ground is coloured.
     *
     * <p>Three ways of drawing the same survey, not three sources of it. Every one of them is a
     * function of what a column already holds — its block, its biome, its depth, its height — so
     * changing between them costs a repaint and never a resurvey.
     */
    public enum Filter {
        /** The ground as it looks: block colours, biome tints, water over them. */
        NONE,
        /** White-hot grey, for reading shape when the map would otherwise light up the room. */
        THERMAL,
        /**
         * Height as colour, which is what a map for laying guns actually is.
         *
         * <p>Artillery is worked off a topographic sheet rather than a photograph, and for a
         * reason this device shares: what matters is where the ground rises, not what is growing
         * on it. Block colour is discarded here — deliberately, because a green that means "trees"
         * competing with a green that means "low ground" is the whole reason photographs are bad
         * gunnery maps.
         */
        RELIEF
    }

    /**
     * Switches the palette. The caller is responsible for throwing away what was coloured under the
     * old one — see {@code MapEngineOverlay.repaint()}, which is the only sane way to do it.
     */
    public static void filter(Filter value) {
        filter = value;
    }

    public static Filter filter() {
        return filter;
    }

    /**
     * One column as a thermal sight would draw it: white-hot, on a grey scale.
     *
     * <p><b>Luminance, then stretched.</b> The plain luminance of a map is a poor thermal picture —
     * forest and open water land within a few percent of each other and the whole image comes out
     * as one mid-grey. So the range is pulled apart about its middle. It is a lie about the numbers
     * and the truth about the job: this exists to make shape readable in the dark.
     *
     * <p><b>Hazards go white.</b> On a white-hot sight the hottest thing in frame is the brightest,
     * and lava is the hottest thing there is here. It is also the one mark on this map that exists
     * to be unmissable, so a palette that let it settle into the greys would be a palette that
     * hides it — the same reason the green filter kept some red.
     */
    private static int thermalOf(int r, int g, int b, boolean hazard) {
        if (hazard) {
            return 0xFFF4F4F4;
        }
        float lum = (0.2126f * r + 0.7152f * g + 0.0722f * b) / 255f;
        float stretched = 0.5f + (lum - 0.42f) * THERMAL_CONTRAST;
        int v = Math.max(6, Math.min(248, Math.round(stretched * 255f)));
        return 0xFF000000 | (v << 16) | (v << 8) | v;
    }

    /** How far the grey scale is pulled apart about its middle. Enough to read, short of posterised. */
    private static final float THERMAL_CONTRAST = 1.75f;

    /**
     * The hypsometric ramp: height in the world, and the colour a topographic sheet gives it.
     *
     * <p>The scheme every relief map uses, and it is not arbitrary — low ground green, rising
     * through the yellows into the browns of high ground, then rock grey and snow. It is read
     * without a legend by anyone who has held a map, which is the whole argument for using it
     * rather than something prettier.
     *
     * <p>Anchored at sea level rather than at the world's floor, because sea level is the height
     * everything on a gunnery chart is quoted against.
     */
    /** The height everything on a gunnery chart is quoted against. */
    private static final int SEA_LEVEL = 62;

    private static final int[][] RELIEF_RAMP = {
            {SEA_LEVEL, 0x2C5A36},
            {78, 0x4C8A3E},
            {96, 0x93A94C},
            {116, 0xC4A961},
            {138, 0xA87A50},
            {162, 0x8E8E8E},
            {200, 0xF2F2F2},
    };

    /**
     * How thick a contour band is, in blocks.
     *
     * <p>Twelve, which is a common sheet interval and — not by accident — comfortably more than a
     * tree. A band shorter than the canopy would put the wood in one colour and the clearing beside
     * it in another, which is the fault this banding exists to remove.
     */
    private static final int RELIEF_BAND = 12;


    /**
     * One column as a topographic sheet would print it.
     *
     * <p><b>Block colour is thrown away here, on purpose.</b> A photograph makes a poor gunnery map
     * because the green of a forest competes with the green of low ground, and the eye cannot tell
     * a wood on a ridge from a meadow in a valley. Height alone answers the only question this
     * device is for.
     *
     * <p><b>Water is one flat colour</b> rather than a shade of the ramp. A river and a hilltop can
     * sit at heights a few metres apart and must never look alike: water is an obstacle, not an
     * elevation.
     *
     * <p><b>Banded, not contoured.</b> Alternate bands are darkened a little, which gives the
     * stepped look of a relief sheet. True contour lines want a column's neighbours and this
     * function has only the column — and drawn from the column alone they would flood every plateau
     * that happens to sit on a multiple of the interval, which in this world is most of the plains.
     */
    private static int reliefOf(int surfaceY, int depth, boolean hazard) {
        if (hazard) {
            return 0xFFF06030;
        }
        if (depth > 0) {
            // Water is kept and quietened rather than dropped. On a printed sheet the water is the
            // palest thing on the page — it is a reference you glance at, not a feature you read —
            // and a strong blue beside these browns and greens is the one colour loud enough to
            // pull the eye off the landform, which is the only thing this filter is for.
            int shade = Math.max(0, 26 - Math.min(depth, 12) * 2);
            return 0xFF000000 | ((0x74 + shade) << 16) | ((0x86 + shade) << 8) | (0x8E + shade);
        }

        // Banded, and the band is what removes the trees. The surveyed height of a wooded column is
        // the top of its canopy, six or eight blocks above the ground it stands on, so a ramp read
        // per block draws every treetop as a small hill — which is exactly the noise a printed
        // sheet does not have. Rounding to the band the column falls in puts the wood and the floor
        // beneath it in the same colour, and it is what a contour sheet does anyway: it reports the
        // band, not the block.
        int band = Math.floorDiv(surfaceY - SEA_LEVEL, RELIEF_BAND);
        int rgb = rampAt(SEA_LEVEL + band * RELIEF_BAND + RELIEF_BAND / 2);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        if (Math.floorMod(band, 2) == 1) {
            r = Math.round(r * 0.90f);
            g = Math.round(g * 0.90f);
            b = Math.round(b * 0.90f);
        }
        // Swapped on the way out: everything downstream reads these as blue, green, red.
        return 0xFF000000 | (b << 16) | (g << 8) | r;
    }

    /** The ramp read at one height, straight between the two stops either side of it. */
    private static int rampAt(int y) {
        if (y <= RELIEF_RAMP[0][0]) {
            return RELIEF_RAMP[0][1];
        }
        for (int i = 1; i < RELIEF_RAMP.length; i++) {
            if (y <= RELIEF_RAMP[i][0]) {
                int[] lo = RELIEF_RAMP[i - 1];
                int[] hi = RELIEF_RAMP[i];
                float t = (y - lo[0]) / (float) (hi[0] - lo[0]);
                return mix(lo[1], hi[1], t);
            }
        }
        return RELIEF_RAMP[RELIEF_RAMP.length - 1][1];
    }

    private static int mix(int a, int b, float t) {
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * t);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * t);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** How much water is over a column, as the drawing needs it to damp the relief. */
    public static float murkOf(int depth) {
        return Math.min(1f, depth / (float) WATER_OPAQUE_AT);
    }

    /** A stored hazard fraction as the drawing wants it, 0 to 1. */
    public static float hazardOf(int raw) {
        return Math.min(1f, raw / 255f);
    }

    /** The biome's own colour for a kind of tint. Only valid once {@link #known} has said yes. */
    private static int tintColour(byte kind, short biomeId) {
        return switch (kind) {
            case BlockPalette.TINT_GRASS -> tintGrass[biomeId];
            case BlockPalette.TINT_FOLIAGE -> tintFoliage[biomeId];
            case BlockPalette.TINT_WATER -> tintWater[biomeId];
            default -> 0;
        };
    }

    /**
     * One channel of the biome colour the map palette already carries for this kind of tint.
     *
     * <p>Zero where there is no such reference, which {@link #scale} reads as "leave it alone" —
     * water has no baked-in palette reference because the palette's water is never tinted this way.
     */
    private static int reference(byte kind, int shift) {
        int rgb = switch (kind) {
            case BlockPalette.TINT_GRASS -> GRASS_REFERENCE;
            case BlockPalette.TINT_FOLIAGE -> FOLIAGE_REFERENCE;
            default -> 0;
        };
        return rgb == 0 ? 0 : (swapForTexture(rgb) >> shift) & 0xFF;
    }

    /** Widened from private for the mapengine bridge ({@code ForgeBlockStyle}) — same pure lookup. */
    public static int waterTint(short biomeId) {
        return known(biomeId) ? tintWater[biomeId]
                : MapColor.WATER.calculateRGBColor(MapColor.Brightness.NORMAL);
    }

    /**
     * How far the water tint is averaged, in columns.
     *
     * <p>Two, which is the radius the game itself blends biome colours over before it paints
     * anything. Matching it is the point: the map is meant to look like the water it is a map of.
     */
    private static final int WATER_BLEND = 2;

    /**
     * The colour of the water over one column, averaged across its neighbours.
     *
     * <p>A biome's water colour is a step function — a warm sea and a cold one meet along a line
     * with nothing in between — and the game hides that by averaging the colour over a
     * neighbourhood before it draws. Taking one column's biome and painting it neat reproduces the
     * step instead, and what that looks like from above is a coastline-shaped staircase out in open
     * water where there is no coast at all.
     *
     * <p>Only water columns are averaged in. A land column carries a water colour too and it means
     * nothing there, so counting it would drag the sea's colour towards whatever the shore happens
     * to be — and the land edge itself stays crisp, which on a map used to lay guns is worth more
     * than a soft one.
     *
     * <p>Clamped at the tile's edge rather than reaching into the next tile. The neighbour may not
     * be held, and a colour that depends on whether it happens to be would make the same water two
     * different colours on two visits. The cost is that the outermost two columns average over a
     * slightly short neighbourhood, which can only matter where a sea boundary runs within two
     * blocks of a tile edge, and even then the answer stays between the two true colours.
     *
     * <p>Averaged plainly, not in linear light, because this is a biome tint rather than a
     * brightness — it is the arithmetic the game does, and doing anything else here would put the
     * map a shade off the world.
     */
    public static int blendedWaterTint(TerrainTile tile, int x, int z) {
        int b = 0;
        int g = 0;
        int r = 0;
        int n = 0;
        for (int dz = -WATER_BLEND; dz <= WATER_BLEND; dz++) {
            int nz = Math.max(0, Math.min(TerrainTile.SIDE - 1, z + dz));
            for (int dx = -WATER_BLEND; dx <= WATER_BLEND; dx++) {
                int nx = Math.max(0, Math.min(TerrainTile.SIDE - 1, x + dx));
                int at = TerrainTile.index(nx, nz);
                if (tile.depthAt(at) <= 0) {
                    continue;
                }
                int tint = waterTint(tile.biome[at]);
                b += (tint >> 16) & 0xFF;
                g += (tint >> 8) & 0xFF;
                r += tint & 0xFF;
                n++;
            }
        }
        if (n == 0) {
            // The column asking is itself under water, so this is only reachable for a lone puddle
            // the clamp cut off. Its own colour is the honest answer.
            return waterTint(tile.biome[TerrainTile.index(x, z)]);
        }
        return ((b / n) << 16) | ((g / n) << 8) | (r / n);
    }

    /** Whether this biome's three colours are on hand, fetching them once if they are not. */
    private static boolean known(short biomeId) {
        if (biomeId == TerrainTile.NO_BIOME || biomeId < 0) {
            return false;
        }
        // Fast path: read the volatile-ish array reference. Safe even without the lock because
        // tintState is only ever replaced with a larger array (never shrunk except at world change,
        // which drains all bake tasks first), and the element is written before the reference.
        byte[] state = tintState;
        if (biomeId < state.length && state[biomeId] != 0) {
            return state[biomeId] > 0;
        }
        return resolve(biomeId);
    }

    /**
     * Looks a biome's tints up now, so a later reader never has to.
     *
     * <p>The partner of {@link BlockPalette#prewarm}: called for every column of every tile as it
     * arrives, on the render thread, so the background bake finds this table already answered and
     * never has to reach into {@code mc.level} itself.
     */
    public static void prewarmBiome(short biomeId) {
        if (biomeId != TerrainTile.NO_BIOME) {
            known(biomeId);
        }
    }

    private static boolean resolve(short biomeId) {
        synchronized (BIOME_LOCK) {
            // Double-check inside lock: another thread may have resolved this biome while we waited.
            if (biomeId < tintState.length && tintState[biomeId] != 0) {
                return tintState[biomeId] > 0;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null) {
                // No world to ask. Deliberately not remembered — the next world is the answer
                return false;
            }

            if (biomeId >= tintState.length) {
                int size = biomeId + 1;
                tintGrass = java.util.Arrays.copyOf(tintGrass, size);
                tintFoliage = java.util.Arrays.copyOf(tintFoliage, size);
                tintWater = java.util.Arrays.copyOf(tintWater, size);
                tintState = java.util.Arrays.copyOf(tintState, size);
            }

            Biome biome = mc.level.registryAccess().registryOrThrow(Registries.BIOME).byId(biomeId);
            if (biome == null) {
                tintState[biomeId] = -1;
                return false;
            }

            tintGrass[biomeId] = swapForTexture(biome.getGrassColor(0.0, 0.0));
            tintFoliage[biomeId] = swapForTexture(biome.getFoliageColor());
            tintWater[biomeId] = swapForTexture(biome.getSpecialEffects().getWaterColor());
            tintState[biomeId] = 1;
            return true;
        }
    }

    /**
     * Puts a plain 0xRRGGBB into the same byte order the palette hands back.
     *
     * <p>The biome accessors return ordinary RGB, unlike the map palette, which returns the order a
     * texture wants because it exists to feed one. Mixing the two without this is the fault that
     * once put water on this map in red, so the conversion is named and done in one place.
     */
    private static int swapForTexture(int rgb) {
        return ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int scale(int channel, int tint, int reference) {
        return reference <= 0 ? channel : Math.min(255, channel * tint / reference);
    }

    /** A grey texture channel taken to the tint's, the way the block renderer does it. */
    private static int multiply(int channel, int tint) {
        return channel * tint / 255;
    }

    private static int blend(int from, int to, float amount) {
        return Math.round(from + (to - from) * amount);
    }
}
