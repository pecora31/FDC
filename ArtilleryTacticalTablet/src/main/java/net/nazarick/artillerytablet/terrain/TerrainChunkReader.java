package net.nazarick.artillerytablet.terrain;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Reads saved chunks for the map on a thread of the survey's own.
 *
 * <p>The trace put the map's remaining cost squarely here. Drawing is a tenth of a millisecond a
 * frame; ground arrives at a few dozen tiles a second, and every tile is sixteen chunks read from
 * disk. Those reads were going through {@code chunkMap.read}, which queues them on the level's own
 * worker — <em>one</em> thread, and the same one the game uses to save chunks as the player moves.
 * So the survey waited behind the game, and the game waited behind the survey.
 *
 * <p>The fix is not a faster reader, it is a second queue. This opens the same region folder through
 * the game's own storage class, which brings its own worker thread with it — so the two no longer
 * contend. Nothing here parses a region file or reimplements anything; that would be exactly the
 * mistake this project keeps writing down.
 *
 * <p><b>Reads only, and that bounds the risk.</b> Two readers on one region folder can, in principle,
 * catch a file mid-write. Minecraft writes a chunk into fresh sectors and only then updates the
 * header, so a reader sees the old chunk or the new one — and if it somehow saw neither, the worst
 * case is one tile that fails to decode, which is caught per chunk and reported as unsurveyed ground.
 * Nothing here can write, so nothing here can damage a world.
 *
 * <p>Falls back to the level's own worker whenever the second queue cannot be opened. A map that
 * fills at the old speed is a great deal better than one that does not fill.
 */
@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TerrainChunkReader {
    /**
     * How many read queues to open per dimension.
     *
     * <p>One was already the difference between four tiles a second and a hundred and eighteen,
     * because it stopped the survey queueing behind the game's own saving. More is the same argument
     * again: a disk that can serve several reads at once was being asked for one at a time, and this
     * is how the web map plugins do it too — a thread count you set against the cores you have.
     *
     * <p>Held back from taking the machine. Half the cores, capped at four, because the server has a
     * game to run and this is a map.
     */
    private static final int QUEUES =
            Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() / 2));

    /** The queues per dimension. Empty where opening them failed, so it is not retried every read. */
    private static final Map<ResourceKey<Level>, Optional<ChunkStorage[]>> OPEN = new HashMap<>();

    private TerrainChunkReader() {
    }

    /**
     * Reads a saved chunk, off the game's own I/O queue where possible.
     *
     * <p>Called from the server thread, and the read itself happens on a worker, so this returns
     * immediately either way.
     */
    public static CompletableFuture<Optional<CompoundTag>> read(ServerLevel level, ChunkPos pos) {
        ChunkStorage ours = storageFor(level, pos);
        if (ours == null) {
            return level.getChunkSource().chunkMap.read(pos);
        }
        try {
            // Ours is the fast path; the level's worker is the authority. It has to be asked whenever
            // ours comes back with nothing, and the reason is the thing that made the map break out
            // in black squares the moment it started filling quickly: the level's worker keeps a map
            // of chunks it has been handed and has not yet written, and answers reads from that
            // before it touches a file. A separate reader cannot see them. So a chunk generated a
            // moment ago — which is most of the ground around a moving player, exactly the ground
            // being asked about — is simply absent from the disk, and absent reads as unsurveyed.
            //
            // The cost is a second read for country that genuinely has never been generated, and
            // that read is a header lookup in a file already open. Cheap, next to painting holes in
            // ground the game knows perfectly well.
            return ours.read(pos).thenCompose(found -> found.isPresent()
                    ? CompletableFuture.completedFuture(found)
                    : level.getChunkSource().chunkMap.read(pos));
        } catch (Throwable t) {
            warnOnce("read a chunk", t);
            return level.getChunkSource().chunkMap.read(pos);
        }
    }

    /**
     * Which queue reads this chunk.
     *
     * <p>Routed by the region it lives in, not round-robin, so a region file is only ever opened by
     * one of our queues. That keeps each queue's own cache of open files useful, and it means the
     * only concurrent readers of any one file are ours and the game's — which is the arrangement
     * already reasoned about, rather than a new one.
     */
    private static synchronized ChunkStorage storageFor(ServerLevel level, ChunkPos pos) {
        ChunkStorage[] queues = open(level);
        if (queues == null) {
            return null;
        }
        int region = Math.floorDiv(pos.x, 32) * 31 + Math.floorDiv(pos.z, 32);
        return queues[Math.floorMod(region, queues.length)];
    }

    private static ChunkStorage[] open(ServerLevel level) {
        return OPEN.computeIfAbsent(level.dimension(), key -> {
            try {
                Path root = level.getServer().getWorldPath(LevelResource.ROOT);
                Path region = DimensionType.getStorageFolder(key, root).resolve("region");
                if (!Files.isDirectory(region)) {
                    // A world with nothing saved yet. Nothing to open, and nothing wrong either.
                    return Optional.empty();
                }
                ChunkStorage[] queues = new ChunkStorage[QUEUES];
                for (int i = 0; i < QUEUES; i++) {
                    queues[i] = new ChunkStorage(region, level.getServer().getFixerUpper(), false);
                }
                ArtilleryTablet.LOGGER.info("Tablet terrain reads for {} have {} queues of their own at {}",
                        key.location(), QUEUES, region);
                return Optional.of(queues);
            } catch (Throwable t) {
                warnOnce("open its own read queue", t);
                return Optional.empty();
            }
        }).orElse(null);
    }

    /**
     * Shuts the extra queues when the server does.
     *
     * <p>On stopping rather than stopped: the worker has a thread and open file handles, and a world
     * that is about to be copied or uploaded should not have ours still holding its region files.
     */
    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        closeAll();
    }

    public static synchronized void closeAll() {
        for (Optional<ChunkStorage[]> queues : OPEN.values()) {
            queues.ifPresent(open -> {
                for (ChunkStorage storage : open) {
                    try {
                        storage.close();
                    } catch (Throwable t) {
                        ArtilleryTablet.LOGGER.warn("Could not close a tablet terrain read queue", t);
                    }
                }
            });
        }
        OPEN.clear();
    }

    private static boolean warned;

    private static synchronized void warnOnce(String what, Throwable t) {
        if (warned) {
            return;
        }
        warned = true;
        ArtilleryTablet.LOGGER.warn("The tablet's terrain survey could not {}; falling back to the "
                + "game's own read queue, which is slower but works", what, t);
    }
}
