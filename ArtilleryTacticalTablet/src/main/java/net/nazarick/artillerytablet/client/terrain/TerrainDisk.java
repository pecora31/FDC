package net.nazarick.artillerytablet.client.terrain;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.terrain.TerrainTile;
import net.nazarick.artillerytablet.terrain.TileFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The client's own store of surveyed ground, so a tile is asked for once ever rather than once per
 * session.
 *
 * <p>This is the last of the three things that make the map mod this was measured against feel
 * instant, and the only one of them that survives a restart. It writes a region image per 512 blocks
 * and reads it back; the same idea here, one file per tile, since a tile is already a self-contained
 * unit with its own compression and its own format revision.
 *
 * <p><b>Which world.</b> Ground is stored under the name of the world it came from and the dimension
 * within it. Two servers may hand out the same tile coordinates for entirely different country, and
 * the nether is not the overworld. Getting this wrong would not fail — it would paint one world's
 * ground onto another, which is the fault the whole cache is careful about elsewhere.
 *
 * <p><b>Which thread(s).</b> Writes stay on a single background thread — the work is ordered and two
 * threads writing the same tile is a corrupt file rather than a faster one. Reads run on a small
 * pool instead: a read never corrupts anything, whether racing another read or a write to the same
 * key (see {@link #READ_IO}), and refilling from disk after a restart is genuinely slow one file at
 * a time. Either way, results hand back through a queue that the cache drains on the render thread,
 * because everything downstream of a tile arriving — the reduction chain, the pictures — belongs to
 * that thread.
 *
 * <p><b>Trusting what is stored.</b> A tile read back is treated exactly like one the server sent,
 * which is deliberate and worth stating: this map does not update itself with battle damage, so
 * ground that was complete when surveyed is complete for good. A tile that was only partly surveyed
 * carries that in its own data, and the cache's refresh rules re-ask about it in the ordinary way.
 */
@OnlyIn(Dist.CLIENT)
public final class TerrainDisk {
    /**
     * Writes, one thread. Two threads writing the same tile is a corrupt file rather than a faster
     * one, and {@link #save} is rare enough (once per newly-surveyed tile) that serialising it costs
     * nothing worth avoiding.
     */
    private static final ExecutorService WRITE_IO = Executors.newSingleThreadExecutor(diskThread("write"));

    /**
     * Reads, several threads. Unlike a write, two reads of different keys — or even the same key,
     * racing a write to it — never corrupt anything: {@link net.nazarick.artillerytablet.terrain.TileFiles#write}
     * writes to a temp file and atomically renames it into place, so a concurrent reader always sees
     * either the old complete file or the new one, never a torn one. A restart that refills from
     * disk was reading thousands of small tile files one at a time on a single thread — genuinely
     * slow, not just perceived, and the actual cause behind the map looking like it "reloads from
     * scratch" every time the tablet opens. Four rather than more: modest, on the same reasoning
     * {@code TerrainImage.BAKE_POOL} already argues for elsewhere in this codebase — leave room for
     * the game's own threads.
     */
    private static final ExecutorService READ_IO = Executors.newFixedThreadPool(4, diskThread("read"));

    private static java.util.concurrent.ThreadFactory diskThread(String kind) {
        return runnable -> {
            Thread thread = new Thread(runnable, "artillerytablet-terrain-disk-" + kind);
            thread.setDaemon(true);
            // Below the game. A map that loads a moment later is nothing; a map that steals time from
            // the frame it is drawn in is the fault this whole session has been chasing.
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        };
    }

    /** Tiles read back, waiting for the render thread to collect them. */
    private static final Queue<TerrainTile> ARRIVED = new ConcurrentLinkedQueue<>();

    /** Keys that came back with nothing, so the caller knows to ask the server instead. */
    private static final Queue<Long> MISSING = new ConcurrentLinkedQueue<>();

    /** How many reads and writes are outstanding, so a flood of them can be throttled. */
    private static final AtomicInteger PENDING = new AtomicInteger();

    /** Past this, asking for more would only lengthen a queue nobody is waiting on. */
    private static final int MAX_PENDING = 256;

    private static Path root;

    /**
     * Which world the store is currently open on.
     *
     * <p>Reads happen on another thread and answer whenever the disk gets round to them, and a
     * change of world does not reach into work already handed over. So a read started in the world
     * being left could land after the cache had been emptied and been taken in as ground for the
     * world being entered — tile coordinates mean something different there, and the map would have
     * painted one world onto another. Emptying the queues on the way past does not cover it: the
     * reads that matter are the ones that had not finished yet.
     *
     * <p>Every request carries the number that was current when it was made, and an answer whose
     * number has moved on is dropped. Volatile because the thread that stamps it and the thread that
     * checks it are not the same one.
     */
    private static volatile int epoch;

    private TerrainDisk() {
    }

    /**
     * Points the store at a world and dimension, or at nothing when there is no world.
     *
     * <p>Called from the cache's own change-of-world check, so the two can never disagree about
     * which world is current — the one place that decides gets to tell this one.
     */
    public static void openFor(ResourceLocation dimension) {
        epoch++;
        if (dimension == null) {
            root = null;
            return;
        }
        try {
            Minecraft mc = Minecraft.getInstance();
            root = mc.gameDirectory.toPath()
                    .resolve(ArtilleryTablet.MODID)
                    .resolve(TileFiles.safeName(worldName(mc)))
                    .resolve(TileFiles.safeName(dimension.getNamespace() + "_" + dimension.getPath()));
            Files.createDirectories(root);
            // Said out loud, once per world, because the fault this line now guards against was
            // invisible from inside the game: the map simply had ground on it that belonged to
            // somewhere else, and the only place the truth was written down was the folder layout.
            ArtilleryTablet.LOGGER.info("Tablet remembers ground for this world at {}", root);
        } catch (Throwable t) {
            ArtilleryTablet.LOGGER.warn("Could not open the tablet's terrain store; "
                    + "the map will work but will not remember ground between sessions", t);
            root = null;
        }
    }

    public static boolean isOpen() {
        return root != null;
    }

    /** Files a tile away, in the background. Does nothing when there is nowhere to put it. */
    public static void save(TerrainTile tile) {
        Path at = root;
        if (at == null || PENDING.get() > MAX_PENDING) {
            return;
        }
        PENDING.incrementAndGet();
        WRITE_IO.execute(() -> {
            try {
                TileFiles.write(at, tile);
            } catch (Throwable t) {
                warnOnce("write", t);
            } finally {
                PENDING.decrementAndGet();
            }
        });
    }

    /**
     * Asks for a stored tile. The answer turns up later through {@link #collect}, either as the tile
     * or as its key in {@link #missing}.
     *
     * @return false when the store is shut or too busy, so the caller should go to the server now
     */
    public static boolean request(long key) {
        Path at = root;
        if (at == null || PENDING.get() > MAX_PENDING) {
            return false;
        }
        int asOf = epoch;
        PENDING.incrementAndGet();
        READ_IO.execute(() -> {
            try {
                TerrainTile tile = TileFiles.read(at, key);
                if (epoch != asOf) {
                    // Read for a world the player has since left. Silently dropped: reporting it
                    // missing would be an answer about the wrong world just as much as the tile is.
                    return;
                }
                if (tile == null) {
                    MISSING.add(key);
                } else {
                    ARRIVED.add(tile);
                }
            } catch (Throwable t) {
                warnOnce("read", t);
                if (epoch == asOf) {
                    MISSING.add(key);
                }
            } finally {
                PENDING.decrementAndGet();
            }
        });
        return true;
    }

    /** A tile that has been read back, or null when none is waiting. Render thread only. */
    public static TerrainTile collect() {
        return ARRIVED.poll();
    }

    /** A key the store had nothing for, or null when none is waiting. Render thread only. */
    public static Long missing() {
        return MISSING.poll();
    }

    /**
     * Drops anything still in flight, for a change of world.
     *
     * <p>Both halves matter. What has already been read back is emptied here; what is still being
     * read is turned away when it arrives, by the world number it was asked under.
     */
    public static void clear() {
        epoch++;
        ARRIVED.clear();
        MISSING.clear();
    }

    /**
     * Which world this is, as a name no two worlds can share.
     *
     * <p>Single player used to be filed under the world's <em>displayed</em> name, and that is not
     * an identity: "New World" is what the game offers every time, so a player who makes two of them
     * has two worlds sharing one folder of remembered ground — and the second one opens its map
     * already full of the first one's country. The save folder is the game's own answer to the same
     * question, unique by construction because the game itself appends a suffix rather than reuse
     * one.
     *
     * <p>Changing this orphans whatever was stored under the old scheme. That costs one resurvey of
     * ground already surveyed, which is the cheapest possible price for the alternative.
     */
    static String worldName(Minecraft mc) {
        if (mc.getSingleplayerServer() != null) {
            String folder = saveFolderName(
                    mc.getSingleplayerServer().getWorldPath(LevelResource.ROOT));
            if (folder != null) {
                return folder;
            }
            // Nothing usable came back. The displayed name is a poorer identity — two worlds can
            // share one — but it is an identity, where "." is the absence of one.
            return mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        ServerData server = mc.getCurrentServer();
        return server == null ? "unknown" : server.ip;
    }

    /**
     * The folder a save lives in, or null when the path does not name one.
     *
     * <p>Its own method so it can be checked outside the game, which the fault it exists for needed:
     * {@code LevelResource.ROOT} has the id ".", so the path arrives as "saves/&lt;world&gt;/." and
     * {@code getFileName()} on it is "." — the same answer for every world there has ever been.
     * Written without the normalise, this filed every single-player world in one folder and served
     * each of them the others' ground, which is a worse version of the fault it was written to fix.
     */
    static String saveFolderName(Path worldPath) {
        Path save = worldPath.toAbsolutePath().normalize().getFileName();
        String folder = save == null ? "" : save.toString();
        if (folder.isEmpty() || folder.equals(".") || folder.equals("..")) {
            return null;
        }
        return folder;
    }

    private static boolean warned;

    /**
     * Complains once and then stops.
     *
     * <p>A disk that is full or read-only fails on every tile, and tiles arrive dozens at a time —
     * the log would be the fault rather than the report of it. The map works without this store;
     * it just forgets between sessions.
     */
    private static synchronized void warnOnce(String what, Throwable t) {
        if (warned) {
            return;
        }
        warned = true;
        ArtilleryTablet.LOGGER.warn("The tablet's terrain store failed to {} and will keep trying "
                + "quietly; the map still works, it just will not remember ground between "
                + "sessions", what, t);
    }
}
