package net.nazarick.artillerytablet.client.terrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.terrain.ServerTerrainProvider;
import net.nazarick.artillerytablet.terrain.TerrainTile;

/**
 * Builds tiles from the chunks the client already has.
 *
 * <p>This is the method the map mod this was measured against uses for everything, confirmed in its
 * own bytecode: what it renders is a wrapper around a {@code LevelChunk} held by weak reference, and
 * it never asks anybody for it. The server streams chunks to the client as part of ordinary play, so
 * for the ground around the player the data is already in memory. Reading it costs no disk, no
 * network and no server time, and it is available the instant the player can see it — which is why
 * walking into unexplored country draws immediately over there and did not here.
 *
 * <p>It does not replace the server survey and cannot: the client only ever holds what is inside its
 * render distance, a few hundred blocks, while this device works across kilometres of ground nobody
 * has stood on. What it does is make the part you are standing in appear at once, and take that part
 * off the server's queue entirely — which is the part of the map being looked at most of the time,
 * and the part a fire-direction officer is least willing to wait for.
 *
 * <p><b>All or nothing per tile.</b> A tile is only built here when every one of its sixteen chunks
 * is in memory. A partly-sampled tile would be recorded as an answer with gaps in it, and the cache
 * treats gaps as ground that needs asking about again — so a half-built tile would cost a server
 * request anyway and paint a hole in the meantime.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientTerrainSampler {
    /** Chunks along one edge of a tile. */
    private static final int CHUNKS_PER_TILE = TerrainTile.SIDE / 16;

    /**
     * Whether a tile is near enough that the client could hold all of it.
     *
     * <p>A tile counts as reachable when its far corner is inside the render distance, minus a chunk
     * for the edge the server has not finished sending. Cheap arithmetic that saves the expensive
     * question being asked at all.
     */
    private static boolean within(Minecraft mc, int tileX, int tileZ) {
        int reach = (mc.options.getEffectiveRenderDistance() - 1) * 16;
        if (reach <= 0) {
            return false;
        }
        int fromX = TerrainTile.tileToBlock(tileX);
        int fromZ = TerrainTile.tileToBlock(tileZ);
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();

        // The corner furthest from the player, since all of the tile has to be in hand.
        double farX = Math.max(Math.abs(playerX - fromX), Math.abs(playerX - (fromX + TerrainTile.SIDE)));
        double farZ = Math.max(Math.abs(playerZ - fromZ), Math.abs(playerZ - (fromZ + TerrainTile.SIDE)));
        return farX <= reach && farZ <= reach;
    }

    private ClientTerrainSampler() {
    }

    /**
     * Builds a tile from chunks in memory, or returns null when they are not all there.
     *
     * <p>Render thread only. It reads the client's own chunk store, which belongs to that thread.
     */
    public static TerrainTile tryBuild(int tileX, int tileZ) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return null;
        }

        // Asked about every unanswered tile in a four-kilometre window, of which the client can only
        // ever hold the few hundred blocks it can see. Without this the sweep looked up sixteen
        // chunks for each of four thousand tiles, twice a second, to be told no four thousand times.
        if (!within(mc, tileX, tileZ)) {
            return null;
        }

        int baseChunkX = Math.floorDiv(TerrainTile.tileToBlock(tileX), 16);
        int baseChunkZ = Math.floorDiv(TerrainTile.tileToBlock(tileZ), 16);

        // Looked for first, sampled second. Sampling as we go and then finding the last chunk absent
        // would mean throwing away the work — and tiles at the edge of render distance, which is
        // exactly where this is asked most, are the ones that fail.
        LevelChunk[] chunks = new LevelChunk[CHUNKS_PER_TILE * CHUNKS_PER_TILE];
        for (int dx = 0; dx < CHUNKS_PER_TILE; dx++) {
            for (int dz = 0; dz < CHUNKS_PER_TILE; dz++) {
                // Never loads and never generates: null simply means the client has not been sent it.
                LevelChunk chunk = level.getChunkSource().getChunk(
                        baseChunkX + dx, baseChunkZ + dz, ChunkStatus.FULL, false);
                if (chunk == null || chunk.isEmpty()) {
                    return null;
                }
                chunks[dx * CHUNKS_PER_TILE + dz] = chunk;
            }
        }

        TerrainTile tile = new TerrainTile(tileX, tileZ);
        for (int dx = 0; dx < CHUNKS_PER_TILE; dx++) {
            for (int dz = 0; dz < CHUNKS_PER_TILE; dz++) {
                ServerTerrainProvider.sampleLive(level, chunks[dx * CHUNKS_PER_TILE + dz], tile,
                        baseChunkX + dx, baseChunkZ + dz);
            }
        }

        // A tile the client built from full chunks should be complete. If it is not — a dimension
        // whose heightmap says something unexpected, say — the cache's own rules will notice the
        // gaps and ask the server about it in the ordinary way, so nothing needs deciding here.
        return tile;
    }
}
