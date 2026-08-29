package net.nazarick.artillerytablet.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads surface colour and height straight out of a saved chunk's NBT, without loading the chunk
 * into the world.
 *
 * <p>This is the whole reason the tablet can show ground nobody is currently standing on. Asking the
 * game for a chunk would <em>generate</em> it when it does not exist — permanent, written to disk,
 * and ruinous over the thousands of blocks this device works across. Parsing the saved bytes only
 * ever reports what has already been generated, and reports nothing where nothing exists.
 *
 * <p>Pure data in, pure data out: no world access, no side effects, safe to run off the server
 * thread while the game carries on.
 */
public final class ChunkNbtSampler {
    private static final int SECTION_VOLUME = 16 * 16 * 16;

    /** Biomes are stored one entry per 4×4×4 cell, so a section holds this many. */
    private static final int BIOME_VOLUME = 4 * 4 * 4;

    private ChunkNbtSampler() {
    }

    /**
     * Surface height of one column of a saved chunk, without decoding any block or biome data.
     *
     * <p>For a ballistic line query, which wants a handful of columns scattered across many chunks
     * rather than a whole tile — reading the heightmap alone skips the palette and section decoding
     * {@link #sample} does for the map's colour, which this has no use for.
     *
     * @return the height, or {@link TerrainTile#NO_DATA} where the chunk has nothing usable
     */
    public static short sampleHeight(CompoundTag chunk, int minY, int levelHeight, int localX, int localZ) {
        try {
            CompoundTag heightmaps = chunk.getCompound("Heightmaps");
            long[] packed = heightmaps.getLongArray("WORLD_SURFACE");
            if (packed.length == 0) {
                return TerrainTile.NO_DATA;
            }
            int bits = Math.max(1, 32 - Integer.numberOfLeadingZeros(levelHeight));
            SimpleBitStorage heights = new SimpleBitStorage(bits, 256, packed);
            int surfaceY = heights.get(localZ * 16 + localX) + minY;
            int blockY = surfaceY - 1;
            return (short) Math.max(Short.MIN_VALUE + 1, Math.min(Short.MAX_VALUE, blockY));
        } catch (Throwable t) {
            return TerrainTile.NO_DATA;
        }
    }

    /**
     * Samples one saved chunk into a tile.
     *
     * @param chunk       the chunk's saved NBT, as handed back by the chunk storage
     * @param minY        the level's lowest build height, needed to turn stored heights back into world Y
     * @param levelHeight the level's total build height, which sets how wide a heightmap entry is
     * @return false when the chunk holds nothing usable, so the caller can leave the area marked unknown
     */
    public static boolean sample(CompoundTag chunk, int minY, int levelHeight,
                                 TerrainTile tile, int chunkX, int chunkZ,
                                 Registry<Biome> biomes) {
        try {
            CompoundTag heightmaps = chunk.getCompound("Heightmaps");
            long[] packed = heightmaps.getLongArray("WORLD_SURFACE");
            if (packed.length == 0) {
                // Chunk exists but generation never reached the surface stage.
                return false;
            }

            // Entry width follows the level's height, not a fixed number: a dimension taller than
            // the overworld packs its heightmap wider, and reading it at the wrong width would give
            // plausible-looking nonsense rather than an obvious failure.
            int bits = Math.max(1, 32 - Integer.numberOfLeadingZeros(levelHeight));
            SimpleBitStorage heights = new SimpleBitStorage(bits, 256, packed);
            Sections sections = readSections(chunk);
            if (sections.isEmpty()) {
                return false;
            }

            int originX = chunkX * 16 - TerrainTile.tileToBlock(tile.tileX);
            int originZ = chunkZ * 16 - TerrainTile.tileToBlock(tile.tileZ);

            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int localX = originX + x;
                    int localZ = originZ + z;
                    if (localX < 0 || localX >= TerrainTile.SIDE || localZ < 0 || localZ >= TerrainTile.SIDE) {
                        continue;
                    }

                    // The surface heightmap records the first free space above the ground, so the
                    // block we actually want to colour sits one below it.
                    int surfaceY = heights.get(z * 16 + x) + minY;
                    int blockY = surfaceY - 1;

                    BlockState state = stateAt(sections, x, blockY, z);
                    if (state == null) {
                        continue;
                    }

                    // Skip down past anything that draws as nothing — glass, and the odd block whose
                    // map colour is NONE. Colouring the topmost block regardless punches transparent
                    // holes through otherwise solid ground. The game's own map walks this same way.
                    int solidY = blockY;
                    while ((mapColourOf(state) == MapColor.NONE || isClutter(state)) && solidY > minY) {
                        BlockState below = stateAt(sections, x, --solidY, z);
                        if (below == null) {
                            break;
                        }
                        state = below;
                    }

                    // Water is not the ground; it is on top of it. Walk down to the floor, keep the
                    // floor's colour, and carry the depth separately. Rendering the water surface as
                    // the ground is what made every ocean one flat sheet of blue: the surface of the
                    // sea is level, so nothing about the shape beneath it could ever show.
                    //
                    // Water specifically, not "any fluid". Written as any fluid at first, which sent
                    // this walking down through lava as well — so a lava lake reported the stone
                    // under it plus a depth, and the drawing, which has only one idea of what a
                    // depth means, painted it as a pool of water. You cannot see the bottom of lava;
                    // it is a surface, and its own colour is the right one.
                    int depth = 0;
                    while (state.getFluidState().is(FluidTags.WATER) && depth < TerrainTile.MAX_DEPTH
                            && solidY > minY) {
                        BlockState below = stateAt(sections, x, --solidY, z);
                        if (below == null) {
                            break;
                        }
                        state = below;
                        depth++;
                    }

                    int index = TerrainTile.index(localX, localZ);
                    tile.block[index] = TerrainTile.idOf(state.getBlock());
                    // The solid surface height without clutter plant bumps
                    tile.height[index] = (short) Math.max(Short.MIN_VALUE + 1, Math.min(Short.MAX_VALUE, solidY + depth));
                    tile.depth[index] = (byte) depth;
                    tile.biome[index] = biomeAt(sections, biomes, x, blockY, z);

                    // The ground's own elevation — see ServerTerrainProvider.sampleLive's own note for
                    // why leaves and clutter are walked through together rather than as two passes, and
                    // ColumnBuffer#groundHeight's for why this is a second field, not a replacement.
                    int groundY = blockY;
                    BlockState groundState = stateAt(sections, x, groundY, z);
                    while (groundState != null
                            && (ServerTerrainProvider.isNonTerrain(groundState) || mapColourOf(groundState) == MapColor.NONE)
                            && groundY > minY) {
                        BlockState below = stateAt(sections, x, groundY - 1, z);
                        if (below == null) {
                            break;
                        }
                        groundY--;
                        groundState = below;
                    }
                    tile.groundHeight[index] = (short) Math.max(Short.MIN_VALUE + 1,
                            Math.min(Short.MAX_VALUE, groundY));
                }
            }
            return true;
        } catch (Throwable t) {
            ArtilleryTablet.LOGGER.warn("Could not read saved chunk {},{} for the tablet map", chunkX, chunkZ, t);
            return false;
        }
    }

    /** Map colour without touching the world — most blocks ignore the position, and any that
     *  insist on one get the empty view rather than a null that would throw. */
    private static MapColor mapColourOf(BlockState state) {
        try {
            MapColor colour = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            return colour == null ? MapColor.NONE : colour;
        } catch (Throwable t) {
            return MapColor.NONE;
        }
    }

    /**
     * The biome at a column, as a registry id.
     *
     * <p>Biomes are stored a quarter of the resolution blocks are — one entry per 4×4×4 cell — which
     * is why the coordinates are shifted down by two before indexing. That coarseness is the game's,
     * not ours, and it is invisible at one pixel per block because biome boundaries are smooth at
     * this scale anyway.
     */
    private static short biomeAt(Sections sections, Registry<Biome> biomes, int x, int worldY, int z) {
        if (biomes == null) {
            return TerrainTile.NO_BIOME;
        }
        Section section = sections.at(Math.floorDiv(worldY, 16));
        if (section == null) {
            return TerrainTile.NO_BIOME;
        }
        ResourceLocation name = section.biome(x, Math.floorMod(worldY, 16), z);
        if (name == null) {
            return TerrainTile.NO_BIOME;
        }
        Biome biome = biomes.get(name);
        if (biome == null) {
            return TerrainTile.NO_BIOME;
        }
        int id = biomes.getId(biome);
        return id < 0 || id > Short.MAX_VALUE ? TerrainTile.NO_BIOME : (short) id;
    }

    private static BlockState stateAt(Sections sections, int x, int worldY, int z) {
        Section section = sections.at(Math.floorDiv(worldY, 16));
        return section == null ? null : section.get(x, Math.floorMod(worldY, 16), z);
    }

    private static boolean isClutter(BlockState state) {
        if (state == null) {
            return false;
        }
        return state.getBlock() instanceof BushBlock
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS)
                || state.is(BlockTags.SAPLINGS);
    }

    /**
     * Lists a chunk's sections without decoding any of them.
     *
     * <p>Decoding used to happen here, for every section, and the map reads two. An overworld chunk
     * has twenty-four — from bedrock to build limit — and each one cost a registry lookup and a fresh
     * {@code ResourceLocation} per palette entry plus a bit-storage over four thousand entries. For a
     * surface map that is around nine tenths of the work thrown away, sixteen times per tile, on the
     * very thread the whole map was waiting behind.
     *
     * <p>What is left here is walking the tag tree, which is reading fields off objects the game has
     * already parsed. The expensive half happens in {@link Section}, the first time somebody actually
     * looks at that section, and for most sections nobody ever does.
     */
    private static Sections readSections(CompoundTag chunk) {
        List<Section> sections = new ArrayList<>();
        ListTag list = chunk.getList("sections", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (!tag.contains("block_states", Tag.TAG_COMPOUND)) {
                continue;
            }
            sections.add(new Section(tag.getByte("Y"), tag));
        }
        return new Sections(sections);
    }

    /**
     * A chunk's sections, reachable by the height they cover.
     *
     * <p>They were a plain list, searched from the top every time a column asked about a block. That
     * is around two dozen comparisons per question, and the questions are not one per column: the
     * walk down through water asks one for every block of depth. An ocean column is therefore some
     * six thousand comparisons, and a chunk of ocean over a million and a half — all of it to work
     * out something the section's own {@code Y} already says.
     *
     * <p>An array indexed by that {@code Y} answers in one step. Nothing about what is sampled
     * changes; this is the same sections, found rather than searched for. Sections a chunk does not
     * save are simply absent, which reads as null, which is what a missing section already meant.
     */
    private static final class Sections {
        private final Section[] bySectionY;
        private final int lowest;

        Sections(List<Section> sections) {
            if (sections.isEmpty()) {
                bySectionY = new Section[0];
                lowest = 0;
                return;
            }
            int low = Integer.MAX_VALUE;
            int high = Integer.MIN_VALUE;
            for (Section section : sections) {
                low = Math.min(low, section.y);
                high = Math.max(high, section.y);
            }
            lowest = low;
            bySectionY = new Section[high - low + 1];
            for (Section section : sections) {
                bySectionY[section.y - low] = section;
            }
        }

        boolean isEmpty() {
            return bySectionY.length == 0;
        }

        Section at(int sectionY) {
            int index = sectionY - lowest;
            return index < 0 || index >= bySectionY.length ? null : bySectionY[index];
        }
    }

    /**
     * Resolved block states, kept by name.
     *
     * <p>The same few hundred names appear in every chunk of a world, and each one was costing a
     * {@code ResourceLocation} and a registry lookup every time it was seen. Concurrent because
     * sampling runs on whichever worker read the chunk, and there may be several.
     */
    private static final java.util.Map<String, BlockState> BY_NAME = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Resolves a palette entry to a block state. Block properties are ignored — they almost never
     * change a block's map colour, and honouring them would mean re-parsing every state's property
     * map for a difference the eye cannot see at one pixel per block.
     */
    private static BlockState blockOf(CompoundTag entry) {
        return BY_NAME.computeIfAbsent(entry.getString("Name"), name ->
                BuiltInRegistries.BLOCK.get(new ResourceLocation(name)).defaultBlockState());
    }

    /**
     * One vertical slice of a chunk, decoded only if something asks it a question.
     *
     * <p>Blocks and biomes are decoded apart from each other, because the two are asked about
     * separately and a column that needed one never needs the other in the same breath.
     */
    private static final class Section {
        final int y;
        private final CompoundTag tag;

        private boolean blocksRead;
        private BlockState[] palette;
        private SimpleBitStorage storage;

        private boolean biomesRead;
        private ResourceLocation[] biomePalette;
        private SimpleBitStorage biomeStorage;

        Section(int y, CompoundTag tag) {
            this.y = y;
            this.tag = tag;
        }

        BlockState get(int x, int y, int z) {
            readBlocks();
            if (palette == null) {
                return null;
            }
            if (storage == null) {
                // A single-entry palette carries no data array: the whole section is that block.
                return palette[0];
            }
            int index = storage.get((y * 16 + z) * 16 + x);
            return index >= 0 && index < palette.length ? palette[index] : null;
        }

        ResourceLocation biome(int x, int y, int z) {
            readBiomes();
            if (biomePalette == null) {
                return null;
            }
            if (biomeStorage == null) {
                return biomePalette[0];
            }
            int index = biomeStorage.get(((y >> 2) * 4 + (z >> 2)) * 4 + (x >> 2));
            return index >= 0 && index < biomePalette.length ? biomePalette[index] : null;
        }

        private void readBlocks() {
            if (blocksRead) {
                return;
            }
            blocksRead = true;

            CompoundTag blockStates = tag.getCompound("block_states");
            ListTag paletteTag = blockStates.getList("palette", Tag.TAG_COMPOUND);
            if (paletteTag.isEmpty()) {
                return;
            }

            palette = new BlockState[paletteTag.size()];
            for (int p = 0; p < paletteTag.size(); p++) {
                palette[p] = blockOf(paletteTag.getCompound(p));
            }

            long[] data = blockStates.getLongArray("data");
            if (data.length > 0) {
                // Section data uses at least four bits per entry, widening only as the palette grows.
                int bits = Math.max(4, 32 - Integer.numberOfLeadingZeros(palette.length - 1));
                storage = new SimpleBitStorage(bits, SECTION_VOLUME, data);
            }
        }

        private void readBiomes() {
            if (biomesRead) {
                return;
            }
            biomesRead = true;

            // Biomes sit beside the blocks in the same section, packed the same way but at a quarter
            // of the resolution and with a palette of plain names rather than of compounds. Absent
            // in some saved sections, which is ordinary rather than an error.
            if (!tag.contains("biomes", Tag.TAG_COMPOUND)) {
                return;
            }
            CompoundTag biomesTag = tag.getCompound("biomes");
            ListTag names = biomesTag.getList("palette", Tag.TAG_STRING);
            if (names.isEmpty()) {
                return;
            }

            biomePalette = new ResourceLocation[names.size()];
            for (int p = 0; p < names.size(); p++) {
                biomePalette[p] = ResourceLocation.tryParse(names.getString(p));
            }

            long[] biomeData = biomesTag.getLongArray("data");
            if (biomeData.length > 0) {
                // No four-bit floor here, unlike block data — biome entries are packed as narrowly
                // as the palette allows.
                int bits = Math.max(1, 32 - Integer.numberOfLeadingZeros(biomePalette.length - 1));
                biomeStorage = new SimpleBitStorage(bits, BIOME_VOLUME, biomeData);
            }
        }
    }
}
