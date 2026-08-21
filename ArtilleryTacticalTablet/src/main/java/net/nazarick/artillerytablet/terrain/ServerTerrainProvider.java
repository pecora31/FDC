package net.nazarick.artillerytablet.terrain;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Builds terrain tiles on the server, which is the only place that knows more ground than one
 * player has walked over.
 *
 * <p>Two sources, in this order:
 * <ol>
 *   <li><b>Chunks loaded right now.</b> The freshest possible picture, and the only one that shows
 *       damage as it happens — a shell landing 3000 blocks away lands in a loaded chunk, so the
 *       crater is here immediately rather than after the next save.</li>
 *   <li><b>Chunks saved on disk.</b> Everywhere anyone has ever been, back to when the world was
 *       created, including long before this addon was installed.</li>
 * </ol>
 *
 * <p>There is deliberately no third source. Ground that has never been generated is left blank and
 * reported blank. Asking the game to produce it would write it to disk permanently and balloon the
 * world by gigabytes across the distances this device works at — the map showing an honest gap is
 * far cheaper than that.
 */
public final class ServerTerrainProvider {
    /** Chunks along one edge of a tile. */
    private static final int CHUNKS_PER_TILE = TerrainTile.SIDE / 16;

    private ServerTerrainProvider() {
    }

    /**
     * Assembles one tile. Must be called on the server thread — it reaches into loaded chunks — but
     * the disk reads and their decoding happen off it, so a request for unexplored ground does not
     * stall the tick.
     */
    public static CompletableFuture<TerrainTile> buildTile(ServerLevel level, int tileX, int tileZ) {
        TerrainTile tile = new TerrainTile(tileX, tileZ);
        int minY = level.getMinBuildHeight();
        int levelHeight = level.getHeight();

        // Pulled out here rather than looked up per chunk: it is immutable once the world is up, so
        // it is safe to hand to the off-thread decoding, which is the whole reason it is a parameter
        // instead of something the sampler reaches for itself.
        Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);

        int baseChunkX = Math.floorDiv(TerrainTile.tileToBlock(tileX), 16);
        int baseChunkZ = Math.floorDiv(TerrainTile.tileToBlock(tileZ), 16);

        List<CompletableFuture<?>> pending = new ArrayList<>();

        for (int dx = 0; dx < CHUNKS_PER_TILE; dx++) {
            for (int dz = 0; dz < CHUNKS_PER_TILE; dz++) {
                int chunkX = baseChunkX + dx;
                int chunkZ = baseChunkZ + dz;

                // getChunkNow never generates and never blocks: null simply means "not in memory".
                LevelChunk live = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (live != null) {
                    sampleLive(level, live, tile, chunkX, chunkZ);
                    continue;
                }

                pending.add(readFromDisk(level, tile, chunkX, chunkZ, minY, levelHeight, biomes));
            }
        }

        if (pending.isEmpty()) {
            return CompletableFuture.completedFuture(tile);
        }
        return CompletableFuture.allOf(pending.toArray(new CompletableFuture[0])).thenApply(ignored -> tile);
    }

    private static CompletableFuture<?> readFromDisk(ServerLevel level, TerrainTile tile,
                                                     int chunkX, int chunkZ, int minY, int levelHeight,
                                                     Registry<Biome> biomes) {
        try {
            // Through the survey's own queue, not the level's. The level's is one thread shared
            // with saving chunks as the player moves, and the two were waiting on each other.
            return TerrainChunkReader.read(level, new ChunkPos(chunkX, chunkZ))
                    .thenAccept(saved -> saved.ifPresent(
                            nbt -> ChunkNbtSampler.sample(nbt, minY, levelHeight, tile, chunkX, chunkZ, biomes)))
                    .exceptionally(t -> {
                        ArtilleryTablet.LOGGER.warn("Terrain read failed for chunk {},{}", chunkX, chunkZ, t);
                        return null;
                    });
        } catch (Throwable t) {
            ArtilleryTablet.LOGGER.warn("Terrain read could not be started for chunk {},{}", chunkX, chunkZ, t);
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    /**
     * Samples one chunk that is already in memory.
     *
     * <p>Takes a plain {@link Level}, not a server one, because the client has chunks in memory too —
     * every chunk it can see — and reading those costs nothing at all: no disk, no network, nobody
     * asked. That is the whole of why the map mod this was measured against feels instant, confirmed
     * in its own bytecode, and this is the method both sides now share so that a column sampled on
     * the client is indistinguishable from one sampled on the server.
     */
    public static void sampleLive(Level level, LevelChunk chunk, TerrainTile tile, int chunkX, int chunkZ) {
        int originX = chunkX * 16 - TerrainTile.tileToBlock(tile.tileX);
        int originZ = chunkZ * 16 - TerrainTile.tileToBlock(tile.tileZ);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int localX = originX + x;
                int localZ = originZ + z;
                if (localX < 0 || localX >= TerrainTile.SIDE || localZ < 0 || localZ >= TerrainTile.SIDE) {
                    continue;
                }

                // Unlike the saved heightmap, this already points at the topmost block itself.
                int blockY = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;
                pos.set(worldX, blockY, worldZ);

                BlockState state = chunk.getBlockState(pos);

                // The same two walks the saved-chunk path does — down past anything that draws as
                // nothing, then down through water to the floor. These two paths feed the same tile
                // and a column sampled by one must be indistinguishable from a column sampled by the
                // other, or the map would change appearance along the edge of the loaded area.
                int solidY = blockY;
                while (isBlank(state, level, pos) && solidY > level.getMinBuildHeight()) {
                    pos.set(worldX, --solidY, worldZ);
                    state = chunk.getBlockState(pos);
                }

                // Water only — see the note in ChunkNbtSampler. Any fluid sent this down through
                // lava too, and a lava lake came back as stone under a depth, which the map drew as
                // a pool of water.
                int depth = 0;
                while (state.getFluidState().is(FluidTags.WATER) && depth < TerrainTile.MAX_DEPTH
                        && solidY > level.getMinBuildHeight()) {
                    pos.set(worldX, --solidY, worldZ);
                    state = chunk.getBlockState(pos);
                    depth++;
                }

                int index = TerrainTile.index(localX, localZ);
                tile.block[index] = TerrainTile.idOf(state.getBlock());
                tile.height[index] = (short) Math.max(Short.MIN_VALUE + 1, Math.min(Short.MAX_VALUE, blockY));
                tile.depth[index] = (byte) depth;
                tile.biome[index] = biomeIdOf(level, chunk, worldX, blockY, worldZ);
            }
        }
    }

    /** Whether a block contributes no colour at all, so the sample should look beneath it. */
    private static boolean isBlank(BlockState state, Level level, BlockPos pos) {
        MapColor colour = state.getMapColor(level, pos);
        return colour == null || colour == MapColor.NONE;
    }

    private static short biomeIdOf(Level level, LevelChunk chunk, int worldX, int worldY, int worldZ) {
        try {
            Registry<Biome> registry = level.registryAccess().registryOrThrow(Registries.BIOME);
            // Quarter resolution, matching how the chunk stores them.
            Biome biome = chunk.getNoiseBiome(worldX >> 2, worldY >> 2, worldZ >> 2).value();
            int id = registry.getId(biome);
            return id < 0 || id > Short.MAX_VALUE ? TerrainTile.NO_BIOME : (short) id;
        } catch (Throwable t) {
            return TerrainTile.NO_BIOME;
        }
    }
}
