package net.nazarick.artillerytablet.client.terrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

/**
 * What each block looks like from above, as one colour.
 *
 * <p>The map used to be drawn from the game's own map palette, and that palette is the reason this
 * map looked coarser than the two mods it was measured against. It has sixty-two colours in it. Oak,
 * birch, spruce, jungle, acacia and dark oak planks are one brown; every kind of stone is one grey.
 * The mods that look better do not have a better renderer — they store what the block <em>is</em>
 * and look up what its texture averages to.
 *
 * <p>So that is what happens here. Given a block id, take the model the game bakes for it, find the
 * face that points up — the only one a map ever sees — and average its texture. The average is taken
 * in linear light, for the same reason the reduction chain is: the packed values are bent by roughly
 * the 2.2 power that displays expect, and averaging them as they stand comes out darker than the
 * thing it stands for.
 *
 * <p>Where there is no usable face — fluids, which the game draws by a route of its own, and
 * anything whose model is missing — this falls back to the map palette, which is exactly what the
 * whole map used to be. A fallback here is a block that looks the way it did last week, not a hole.
 *
 * <p><b>Tint.</b> Grass and leaves ship as grey textures that the game multiplies by a biome colour
 * at draw time, so an averaged texture for them is grey and useless on its own. The model says which
 * ones those are — a quad carries a tint index — and that flag is stored so {@link TerrainMips} can
 * apply the biome's own colour. The palette fallback needs the opposite treatment, because the map
 * palette already has plains grass baked into it; the two cases are told apart by
 * {@link #isPreTinted}.
 *
 * <p><b>Lifetime.</b> Block ids are handed out by the server at login, so this must be forgotten
 * when the world changes or one world's ids would colour another world's ground. That is done from
 * {@link WorldGeneration#checkWorld()}, beside the biome memo, which has the same problem.
 */
@OnlyIn(Dist.CLIENT)
public final class BlockPalette {
    /**
     * Guards the tables below against concurrent access from the mapengine bridge's background
     * rasterize/pyramid work — moved here from the old tile cache's own lock when that class was
     * retired; the lock's job never had anything to do with tiles.
     */
    private static final Object LOCK = new Object();

    /** No biome tint: the texture is already the colour it should be. */
    public static final byte TINT_NONE = 0;
    public static final byte TINT_GRASS = 1;
    public static final byte TINT_FOLIAGE = 2;
    public static final byte TINT_WATER = 3;

    /**
     * How finely a texture is sampled when averaging it.
     *
     * <p>An ordinary block texture is sixteen pixels square and is read in full. A resource pack can
     * ship five hundred and twelve, and reading a quarter of a million pixels per block to arrive at
     * one number is waste — a grid this size settles the average of a texture to well inside a
     * rounding step. This is not the sparse sampling that ruined the wide zooms: that threw away
     * ground and made each map pixel a coin toss, whereas this is estimating one mean.
     */
    private static final int SAMPLE_GRID = 32;

    /** Below this, a texel is a hole in the texture rather than a dark part of it. */
    private static final int OPAQUE_AT = 128;

    /**
     * Ground that is drawn as a symbol rather than as itself.
     *
     * <p>Everywhere else this class is trying to answer "what does that look like from above", and
     * for lava the honest answer turned out to be useless. Averaged, its texture is a dull orange
     * that sits among the browns of dirt, wood and roofing and cannot be picked out of them — and
     * the one thing a fire-control map has to do with a lava lake is make it impossible to miss.
     * So it is painted the colour of a hazard, which is a decision about what the map is for and not
     * about what the block is.
     *
     * <p>Kept to that one case on purpose. Every entry added here is a place where the map stops
     * telling the truth about the world, and it should have to earn it.
     */
    private static final Map<Block, Integer> HAZARDS = Map.of(
            // From the range the user picked, the middle of it: hot enough to read as fire, far
            // enough from red to not be mistaken for the marks the tablet draws in red.
            Blocks.LAVA, packed(0xFF6600));

    /** A plain 0xRRGGBB in the byte order the rest of this map works in — blue high, red low. */
    private static int packed(int rgb) {
        return 0xFF000000 | ((rgb & 0xFF) << 16) | (rgb & 0xFF00) | ((rgb >> 16) & 0xFF);
    }

    private static int[] colour = new int[0];
    private static byte[] tint = new byte[0];
    private static boolean[] preTinted = new boolean[0];
    private static boolean[] hazard = new boolean[0];
    private static boolean[] resolved = new boolean[0];

    /**
     * The same block, in the coarse ~62-colour map palette rather than its own averaged texture —
     * every kind of leaves one green, every kind of stone one grey. Kept alongside {@link #colour}
     * rather than replacing it: the fine palette still exists for other filters, this one exists
     * only because the Ground layer's own per-block colour variety was found to visually compete
     * with its hillshade, burying the relief "grain" JourneyMap's flatter satellite palette lets
     * show through. Always the map-palette style of tint ({@link #tintFor}'s {@code fromPalette}
     * case) since this array is never anything but a map-colour swatch.
     */
    private static int[] simpleColour = new int[0];
    private static byte[] simpleTint = new byte[0];

    private BlockPalette() {
    }

    /** Throws away everything learned. Called when a change of world makes an id mean something else. */
    public static void forget() {
        synchronized (LOCK) {
            colour = new int[0];
            tint = new byte[0];
            preTinted = new boolean[0];
            hazard = new boolean[0];
            resolved = new boolean[0];
            simpleColour = new int[0];
            simpleTint = new byte[0];
        }
    }

    /** The colour of a block seen from above, packed the way the map palette packs one. */
    public static int colourOf(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return colour[blockId];
        }
    }

    /** Which biome colour, if any, this block's texture is waiting to be multiplied by. */
    public static byte tintOf(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return tint[blockId];
        }
    }

    /** {@link #colourOf}, but the coarse map-palette swatch instead of the averaged texture. */
    public static int simpleColourOf(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return simpleColour[blockId];
        }
    }

    /** {@link #tintOf}, matching {@link #simpleColourOf} — always the map-palette style of tint. */
    public static byte simpleTintOf(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return simpleTint[blockId];
        }
    }

    /**
     * True when this block is painted as a hazard symbol rather than as itself.
     *
     * <p>The drawing has to know, because a symbol must not be shaded. Dimming and relief exist to
     * make ground readable, and they defeat a mark whose whole purpose is to be unmissable — twice
     * over. The dimming alone takes the brightest colour there is down to sixty-two per cent, so no
     * choice of colour can produce bright lava while it applies; and the relief then varies what is
     * left from one pit to the next, according to which way the ground beside it happens to lean.
     */
    public static boolean isHazard(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return hazard[blockId];
        }
    }

    /**
     * True when {@link #colourOf} came from the map palette rather than from a texture.
     *
     * <p>It matters only for tinting. The palette's grass entry is already the green of plains, so
     * tinting it means scaling it from that reference to the biome's; a texture is untinted grey, so
     * tinting it means a plain multiply. Applying either rule to the other case gets it badly wrong,
     * which is why the two are distinguished rather than assumed.
     */
    public static boolean isPreTinted(int blockId) {
        synchronized (LOCK) {
            resolve(blockId);
            return preTinted[blockId];
        }
    }

    /**
     * Works out everything about one block id, if it has not been worked out already.
     *
     * <p>The lock is held by the four accessors above rather than only here, and the difference is
     * not tidiness. Each of them used to resolve under the lock and then read the array
     * <em>outside</em> it — and every one of these arrays is <b>replaced, not mutated</b>, both by
     * {@link #grow} and by {@link #forget}. So a change of world between the two lines handed the
     * reader an array of length zero, and a block resolved on another thread handed it the array
     * from before it grew. Both are an index out of bounds, in a class that is now read from the
     * background bake threads as well as from the render thread.
     */
    private static void resolve(int blockId) {
        synchronized (LOCK) {
            if (blockId >= resolved.length) {
                grow(blockId + 1);
            }
            if (resolved[blockId]) {
                return;
            }

            Block block = BuiltInRegistries.BLOCK.byId(blockId);
            BlockState state = block.defaultBlockState();

            Integer fixed = HAZARDS.get(block);
            if (fixed != null) {
                colour[blockId] = fixed;
                preTinted[blockId] = false;
                tint[blockId] = TINT_NONE;
                hazard[blockId] = true;
                simpleColour[blockId] = fixed;
                simpleTint[blockId] = TINT_NONE;
                resolved[blockId] = true;
                return;
            }

            MapColor mapColour = mapColourOf(state);

            // The palette answer first, so there is always something to fall back to and only one place
            // that decides what the fallback is.
            colour[blockId] = mapColour.calculateRGBColor(MapColor.Brightness.NORMAL);
            preTinted[blockId] = true;
            tint[blockId] = tintFor(mapColour, true);

            // Captured now, before colour[]/tint[] potentially get overwritten below by a texture
            // average — this pair never changes again for this block id, unlike the fine ones.
            simpleColour[blockId] = colour[blockId];
            simpleTint[blockId] = tint[blockId];

            // Everything above needs no client at all — it is registry data. Everything below reads
            // the baked models and the texture atlas, and those belong to the render thread: a
            // resource reload swaps the model manager and closes every sprite, so a background bake
            // reading one mid-reload is reading freed memory.
            //
            // So the fallback stands, and the id is deliberately left UNRESOLVED. This used to be
            // dead code in practice — the old tile cache warmed every block a tile mentioned on the
            // render thread the moment it landed — but the mapengine bridge (ForgeBlockStyle) does
            // not yet call prewarm() at all (a deliberate scope cut, see that class's own doc), so
            // this fallback is presently the common path there, not a rare one. Real, not wrong: the
            // colour returned is just the coarser map-colour approximation until prewarming is wired.
            if (!onRenderThread()) {
                warnOffThreadOnce(block);
                return;
            }
            resolved[blockId] = true;

            BakedModel model = modelOf(state);
            if (model == null) {
                return;
            }

            BakedQuad quad = upwardFace(model, state);
            if (quad != null) {
                int averaged = average(quad.getSprite());
                if (averaged != 0) {
                    colour[blockId] = averaged;
                    preTinted[blockId] = false;
                    tint[blockId] = quad.isTinted() ? tintFor(mapColour, false) : TINT_NONE;
                }
                return;
            }

            // No faces at all, which in practice means a fluid: the game draws those by a route of its
            // own and their models carry nothing but a particle texture. That texture is the fluid's own
            // still image, so it is exactly what a map wants — and reaching it is what stops lava being
            // the palette's flat red, which reads as blood rather than as molten rock.
            //
            // Only ever as a last resort. Tried first, it would be actively wrong: a grass block's
            // particle icon is dirt, so every meadow in the world would come out brown.
            int averaged = average(model.getParticleIcon());
            if (averaged == 0) {
                return;
            }
            colour[blockId] = averaged;
            preTinted[blockId] = false;
            // Nothing says whether a particle texture is tinted, so the palette entry decides. Water is
            // grey-blue until a biome colours it; lava and the rest are already the colour they are.
            tint[blockId] = tintFor(mapColour, false);
        }
    }

    /**
     * Which biome colour a block wants.
     *
     * <p>Decided from the map palette entry rather than from the game's own colour resolvers, which
     * would need a live world and a position to answer. The palette already sorts blocks into grass,
     * plant and water, and those are the three tints that exist, so it answers the question without
     * pretending to have a world.
     *
     * <p>{@code fromPalette} distinguishes the two callers. A palette colour for grass is tinted
     * unconditionally, which is what this map has always done. A texture is tinted only when the
     * model says so, because a texture that is already green would come out doubly so.
     */
    private static byte tintFor(MapColor mapColour, boolean fromPalette) {
        if (mapColour == MapColor.GRASS) {
            return TINT_GRASS;
        }
        if (mapColour == MapColor.PLANT) {
            return TINT_FOLIAGE;
        }
        if (!fromPalette && mapColour == MapColor.WATER) {
            return TINT_WATER;
        }
        return TINT_NONE;
    }

    /** Whether this is the thread that owns the models and the texture atlas. */
    private static boolean onRenderThread() {
        Minecraft mc = Minecraft.getInstance();
        return mc != null && mc.isSameThread();
    }

    private static boolean warnedOffThread;

    /**
     * Says once that a block was asked about from the wrong thread.
     *
     * <p>Not an error: the map draws that block in its palette colour and carries on. It is a report
     * that the warming in {@code accept} has a hole in it — some route reaches the palette with a
     * block id that never came through a tile — and until it is found, that block is flat-coloured
     * on every sheet baked before the render thread happens to ask about it.
     */
    private static void warnOffThreadOnce(Block block) {
        if (warnedOffThread) {
            return;
        }
        warnedOffThread = true;
        ArtilleryTablet.LOGGER.info("Tablet map met block {} on a background thread before the "
                + "render thread had looked at it; drawing it in its palette colour for now. Worth "
                + "knowing about — it means a block reached the map without passing through a "
                + "tile's arrival.", block);
    }

    /**
     * Looks a block up now, on whichever thread is calling, so a later reader never has to.
     *
     * <p>Called for every column of every tile as it arrives, on the render thread. A tile holds
     * four thousand columns and a handful of distinct blocks, and an already-known id costs one
     * array read — so this is far cheaper than it looks, and it is what lets the background bake
     * treat this whole class as read-only.
     */
    public static void prewarm(int blockId) {
        resolve(blockId);
    }

    /** The model the game bakes for a block, or null when there is no client to ask. */
    private static BakedModel modelOf(BlockState state) {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.getModelManager() == null) {
                return null;
            }
            return mc.getModelManager().getBlockModelShaper().getBlockModel(state);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * The quad a map would see: the top face, or failing that whichever face the model offers.
     *
     * <p>Models with no sided faces at all — flowers, saplings, anything drawn as a cross — keep
     * their geometry in the unsided list, so that is tried next rather than giving up on them.
     */
    private static BakedQuad upwardFace(BakedModel model, BlockState state) {
        try {
            // A fixed seed, because a model that varies its faces at random must still give this
            // map the same colour every time it is asked.
            List<BakedQuad> quads = model.getQuads(state, Direction.UP, RandomSource.create(42L));
            if (quads.isEmpty()) {
                quads = model.getQuads(state, null, RandomSource.create(42L));
            }
            return quads.isEmpty() ? null : quads.get(0);
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * A texture's mean colour, in linear light, ignoring the parts of it that are holes.
     *
     * <p>Transparent texels are skipped rather than counted as black. Leaves are half gaps; averaged
     * with those gaps in, a canopy would come out as a dark smudge rather than as foliage.
     *
     * @return the packed colour, or zero when the texture had nothing solid in it at all
     */
    private static int average(TextureAtlasSprite sprite) {
        try {
            int width = sprite.contents().width();
            int height = sprite.contents().height();
            if (width <= 0 || height <= 0) {
                return 0;
            }

            int stepX = Math.max(1, width / SAMPLE_GRID);
            int stepY = Math.max(1, height / SAMPLE_GRID);

            float sumR = 0f;
            float sumG = 0f;
            float sumB = 0f;
            int solid = 0;

            for (int y = 0; y < height; y += stepY) {
                for (int x = 0; x < width; x += stepX) {
                    // Frame zero: an animated texture stacks its frames, and a map wants the one the
                    // block spends its time looking like rather than an average over the animation.
                    int packed = sprite.getPixelRGBA(0, x, y);
                    if (((packed >>> 24) & 0xFF) < OPAQUE_AT) {
                        continue;
                    }
                    // Native images hand back the same byte order the map palette does — blue high,
                    // red low. Checked in the bytecode rather than assumed: getting this backwards
                    // is what once drew every ocean in red, and it survived for days because the
                    // comment claiming otherwise was one I had written myself.
                    sumB += Light.LINEAR[(packed >> 16) & 0xFF];
                    sumG += Light.LINEAR[(packed >> 8) & 0xFF];
                    sumR += Light.LINEAR[packed & 0xFF];
                    solid++;
                }
            }

            if (solid == 0) {
                return 0;
            }
            return 0xFF000000
                    | (Light.encode(sumB / solid) << 16)
                    | (Light.encode(sumG / solid) << 8)
                    | Light.encode(sumR / solid);
        } catch (Throwable t) {
            return 0;
        }
    }

    private static MapColor mapColourOf(BlockState state) {
        try {
            MapColor mapColour = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            return mapColour == null ? MapColor.NONE : mapColour;
        } catch (Throwable t) {
            return MapColor.NONE;
        }
    }

    private static void grow(int wanted) {
        // Sized to the registry, not to what has been asked for, so a walk through a modded world
        // does not keep copying these arrays. The registry is a few thousand entries at most.
        int size = Math.max(wanted, BuiltInRegistries.BLOCK.size());
        colour = java.util.Arrays.copyOf(colour, size);
        tint = java.util.Arrays.copyOf(tint, size);
        preTinted = java.util.Arrays.copyOf(preTinted, size);
        hazard = java.util.Arrays.copyOf(hazard, size);
        resolved = java.util.Arrays.copyOf(resolved, size);
        simpleColour = java.util.Arrays.copyOf(simpleColour, size);
        simpleTint = java.util.Arrays.copyOf(simpleTint, size);
    }

}
