package net.nazarick.artillerytablet.client.terrain.mapengine;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.client.terrain.ClientTerrainSampler;
import net.nazarick.artillerytablet.terrain.TerrainTile;
import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.ColumnSource;

import java.util.concurrent.ExecutionException;

/**
 * Feeds {@code mapengine}'s {@link net.nazarick.mapengine.storage.RegionStore} from the client's own
 * already-loaded chunks — the client-side-only map decided earlier this project (a narrow server
 * query answers ballistics separately; the rendered map never asks the network for tiles to draw).
 *
 * <p><b>Why this cannot simply call {@link ClientTerrainSampler#tryBuild} directly.</b> That method
 * reads {@code level.getChunkSource().getChunk(...)}, which is render-thread-only — but
 * {@link ColumnSource#fill} runs on {@code RegionStore}'s own background I/O pool, by design (that is
 * the whole point of {@code RegionStore} never blocking the thread that calls
 * {@link net.nazarick.mapengine.storage.RegionStore#get}). Calling a render-thread-only Minecraft
 * method from that pool would not fail loudly — a chunk source is not built to expect concurrent
 * off-thread callers at all, and the failure mode is undefined behaviour, not an exception. Every
 * tile this region needs is instead fetched in <em>one</em> round trip to the render thread via
 * {@link Minecraft#submit}, and this method blocks the I/O thread waiting for it — which is exactly
 * the kind of wait {@code ColumnSource.fill} is already documented to allow (disk reads block the
 * same I/O thread already).
 */
@OnlyIn(Dist.CLIENT)
public final class ForgeColumnSource implements ColumnSource {

    @Override
    public boolean fill(int blockX, int blockZ, ColumnBuffer into) {
        int side = TerrainTile.SIDE;
        if (into.width % side != 0) {
            throw new IllegalArgumentException("ColumnBuffer width " + into.width
                    + " is not a multiple of TerrainTile.SIDE (" + side + ")");
        }
        int tilesPerSide = into.width / side;
        int count = tilesPerSide * tilesPerSide;
        int[] tileX = new int[count];
        int[] tileZ = new int[count];
        int i = 0;
        for (int tz = 0; tz < tilesPerSide; tz++) {
            for (int tx = 0; tx < tilesPerSide; tx++) {
                tileX[i] = Math.floorDiv(blockX + tx * side, side);
                tileZ[i] = Math.floorDiv(blockZ + tz * side, side);
                i++;
            }
        }

        TerrainTile[] tiles;
        try {
            tiles = Minecraft.getInstance().submit(() -> {
                TerrainTile[] out = new TerrainTile[count];
                for (int t = 0; t < count; t++) {
                    out[t] = ClientTerrainSampler.tryBuild(tileX[t], tileZ[t]);
                }
                return out;
            }).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (ExecutionException e) {
            ArtilleryTablet.LOGGER.warn("mapengine: ForgeColumnSource.fill render-thread task threw for "
                    + "region at ({},{})", blockX, blockZ, e.getCause());
            return false;
        } catch (Throwable t) {
            // Minecraft.submit()'s Future.get() only declares the two checked exceptions above; this
            // is still new enough code that an unexpected throwable is worth reporting rather than
            // letting it vanish silently.
            ArtilleryTablet.LOGGER.warn("mapengine: ForgeColumnSource.fill failed unexpectedly for "
                    + "region at ({},{})", blockX, blockZ, t);
            return false;
        }

        boolean any = false;
        i = 0;
        for (int tz = 0; tz < tilesPerSide; tz++) {
            for (int tx = 0; tx < tilesPerSide; tx++) {
                TerrainTile tile = tiles[i++];
                if (tile != null) {
                    copyInto(tile, into, tx * side, tz * side);
                    any = true;
                }
                // null means "not currently loaded or out of render distance" — left as NO_DATA,
                // the region simply gets asked for again once the store next wants it.
            }
        }
        return any;
    }

    private static void copyInto(TerrainTile tile, ColumnBuffer into, int offsetX, int offsetZ) {
        int side = TerrainTile.SIDE;
        for (int z = 0; z < side; z++) {
            for (int x = 0; x < side; x++) {
                int srcIdx = TerrainTile.index(x, z);
                int dstIdx = into.index(offsetX + x, offsetZ + z);
                into.height[dstIdx] = tile.height[srcIdx];
                into.groundHeight[dstIdx] = tile.groundHeight[srcIdx];
                into.block[dstIdx] = tile.block[srcIdx];
                into.biome[dstIdx] = tile.biome[srcIdx];
                into.depth[dstIdx] = tile.depth[srcIdx];
            }
        }
    }
}
