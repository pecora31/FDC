package net.nazarick.artillerytablet.terrain;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Ground the server has already surveyed, kept beside the world.
 *
 * <p>The map's whole remaining cost is here, and the measurement is stark: drawing a viewful of
 * ground takes about four hundredths of a second, and fetching that ground takes ten to twenty. Each
 * tile is sixteen chunks read off the disk and decompressed, and until now that happened <em>every
 * time anybody asked</em> — the server remembered a survey for two seconds, in memory, and then
 * threw it away.
 *
 * <p>So a surveyed tile is written down. A chunk is then read once in the life of a world rather
 * than once per request, the saving is shared by every player on the server, and it survives a
 * restart. It is the same file format the client's own store uses, for the same reason: a tile
 * already knows how to compress itself and already carries a revision of its format.
 *
 * <p><b>What is stored, and what is not.</b> Only tiles where every column was surveyed. A tile with
 * gaps means chunks that had not been generated when it was looked at, and walking over there
 * generates them — writing that down would freeze a blank into the world's record. Those are
 * surveyed afresh each time, which is cheap, because reading a chunk that does not exist is a look
 * at a file header rather than a file.
 *
 * <p><b>Staying honest about ground that changes.</b> A stored tile is served only when none of its
 * chunks are loaded. Ground with a player near it is surveyed live, which is both the fast path
 * anyway and the only place building happens. Country nobody is standing in does not change, so
 * remembering it is not a lie — and the client already never re-asks about complete ground, so this
 * makes the server agree with a decision the map made long ago.
 */
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(
        modid = ArtilleryTablet.MODID,
        bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerTileStore {
    /**
     * One thread, and a low priority.
     *
     * <p>The reads it does are pure gain — worst case the tile is surveyed the old way — so it must
     * never take a tick from the server to get them. One rather than a pool because the work is
     * disk-bound and ordered, and two threads writing one tile is a corrupt file, not a faster one.
     */
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "artillerytablet-tile-store");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    /**
     * Which tiles the store actually holds, per dimension.
     *
     * <p>Without this every request queued behind one thread to be told the store was empty, which
     * turned a pipeline of two hundred parallel surveys into two hundred waits in a line — the store
     * made the first visit to anywhere <em>slower</em>, which is the opposite of the point. A miss
     * has to cost nothing, and a set in memory is the only way it can.
     *
     * <p>Filled once by walking the folder, in the background. Until that finishes every tile reads
     * as absent and is surveyed, which is exactly what happened before this class existed.
     */
    private static final Map<ResourceKey<Level>, LongOpenHashSet> HELD = new ConcurrentHashMap<>();

    /** Dimensions whose folder has been walked, so it is walked once and not once per tile. */
    private static final Set<ResourceKey<Level>> INDEXED = ConcurrentHashMap.newKeySet();

    private ServerTileStore() {
    }

    /**
     * Whether the store is worth asking about this tile.
     *
     * <p>Answered from memory, on the caller's thread, because the answer for ground nobody has
     * surveyed yet has to be free.
     */
    public static boolean holds(ServerLevel level, int tileX, int tileZ) {
        LongOpenHashSet held = HELD.get(level.dimension());
        return held != null && held.contains(TerrainTile.key(tileX, tileZ));
    }

    /**
     * Looks for a stored tile.
     *
     * @return the tile, or null when there is none — never blocks the caller's thread
     */
    public static CompletableFuture<TerrainTile> read(ServerLevel level, int tileX, int tileZ) {
        Path root = rootFor(level);
        if (root == null || !holds(level, tileX, tileZ)) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return TileFiles.read(root, TerrainTile.key(tileX, tileZ));
            } catch (Throwable t) {
                warnOnce("read", t);
                return null;
            }
        }, IO);
    }

    /** Files a tile away, if it is worth keeping. Returns at once; the write happens behind. */
    public static void write(ServerLevel level, TerrainTile tile) {
        if (!tile.isComplete()) {
            // Gaps mean ungenerated chunks, and those become ground the moment somebody walks there.
            return;
        }
        Path root = rootFor(level);
        if (root == null) {
            return;
        }
        ResourceKey<Level> dimension = level.dimension();
        IO.execute(() -> {
            try {
                TileFiles.write(root, tile);
                // Only if this is still the world it was written for. A write handed over just as
                // the server stopped would otherwise put a tile from the old world into the index
                // the next one reads.
                if (ROOTS.get(dimension) == root) {
                    HELD.computeIfAbsent(dimension, key -> new LongOpenHashSet())
                            .add(TerrainTile.key(tile.tileX, tile.tileZ));
                }
            } catch (Throwable t) {
                warnOnce("write", t);
            }
        });
    }

    /** Where each dimension's tiles live, worked out once. */
    private static final Map<ResourceKey<Level>, Path> ROOTS = new ConcurrentHashMap<>();

    /**
     * Where this dimension's tiles live: inside the world's own folder.
     *
     * <p>Beside the world rather than beside the mod, so it travels when the world is copied and
     * goes when the world is deleted. It is a cache of what the world already says, so losing it
     * costs time and nothing else.
     *
     * <p>Worked out once per dimension and then remembered. It used to resolve the path and call
     * {@code createDirectories} on every read and every write — and reads are asked for on the
     * server thread, dozens per tile request, so the tick was making a filesystem call to be told
     * about a directory it had made a moment earlier. The whole reason the store keeps an index in
     * memory is that a miss must cost nothing, and this was charging for one anyway.
     */
    private static Path rootFor(ServerLevel level) {
        Path known = ROOTS.get(level.dimension());
        if (known != null) {
            return known;
        }
        try {
            Path root = level.getServer().getWorldPath(LevelResource.ROOT)
                    .resolve(ArtilleryTablet.MODID)
                    .resolve(TileFiles.safeName(level.dimension().location().getNamespace()
                            + "_" + level.dimension().location().getPath()));
            Files.createDirectories(root);
            ROOTS.put(level.dimension(), root);
            index(level.dimension(), root);
            return root;
        } catch (Throwable t) {
            warnOnce("open", t);
            return null;
        }
    }

    /**
     * Walks the folder once and remembers what is in it.
     *
     * <p>File names carry the coordinates, so this reads no tiles at all — it is a directory listing,
     * and the answer it produces is what makes every later miss free.
     */
    private static void index(ResourceKey<Level> dimension, Path root) {
        if (!INDEXED.add(dimension)) {
            return;
        }
        IO.execute(() -> {
            LongOpenHashSet held = new LongOpenHashSet();
            try (java.util.stream.Stream<Path> walk = Files.walk(root)) {
                walk.filter(Files::isRegularFile).forEach(file -> {
                    Long key = keyOf(file.getFileName().toString());
                    if (key != null) {
                        held.add(key);
                    }
                });
            } catch (Throwable t) {
                warnOnce("list what it holds", t);
            }
            if (ROOTS.get(dimension) != root) {
                // The world was closed while this was listing it. Its answer is about somewhere the
                // server has left.
                return;
            }
            HELD.merge(dimension, held, (existing, found) -> {
                // Anything written while the walk was running stays: it is newer than the listing.
                found.addAll(existing);
                return found;
            });
            ArtilleryTablet.LOGGER.info("Tablet knows {} surveyed tiles for {}", held.size(),
                    dimension.location());
        });
    }

    /** The tile a file name stands for, or null when the name is not one of ours. */
    private static Long keyOf(String name) {
        if (!name.startsWith("t") || !name.endsWith(".tile")) {
            return null;
        }
        try {
            String body = name.substring(1, name.length() - ".tile".length());
            int split = body.indexOf('_', body.startsWith("-") ? 1 : 0);
            if (split < 0) {
                return null;
            }
            return TerrainTile.key(Integer.parseInt(body.substring(0, split)),
                    Integer.parseInt(body.substring(split + 1)));
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Forgets which world this was.
     *
     * <p>A single-player client keeps this class loaded across a quit to the menu and a load of the
     * next world, and a dimension key is the same object in both — {@code minecraft:overworld} is
     * {@code minecraft:overworld} wherever it lives. So everything remembered here has to go when
     * the server does, or the next world starts life believing it already holds several thousand
     * tiles of ground that belongs to a world the player has left. That is the one fault this whole
     * layer is most careful about: not a crash, but one world's ground painted onto another's.
     */
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        ROOTS.clear();
        HELD.clear();
        INDEXED.clear();
        ServerTileCache.forgetAll();
    }

    private static boolean warned;

    /**
     * Complains once and then stops.
     *
     * <p>A disk that is full or read-only fails on every tile, and tiles are surveyed dozens at a
     * time — the log would become the fault rather than the report of it. Everything works without
     * this store; it is only slower.
     */
    private static synchronized void warnOnce(String what, Throwable t) {
        if (warned) {
            return;
        }
        warned = true;
        ArtilleryTablet.LOGGER.warn("The tablet's surveyed-ground store could not {}; the map still "
                + "works, it will just survey the same ground again", what, t);
    }
}
