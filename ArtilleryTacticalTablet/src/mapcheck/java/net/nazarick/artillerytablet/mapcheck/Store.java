package net.nazarick.artillerytablet.mapcheck;

import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;

/**
 * Writes tiles to the client's own store and reads them back, outside Minecraft.
 *
 * <p>The store is what makes ground surveyed in one session still be there in the next, and it is
 * the part of the map that can fail in the quietest way: a file written under the wrong name, or a
 * coordinate that shards differently going out than coming back, does not throw. It simply means
 * nothing is ever found, and the map looks exactly as it did before the store existed — slower to
 * fill, for a reason nobody would think to look for.
 *
 * <p>Negative coordinates are in the range deliberately. Half of every world has them, the sharding
 * divides by a power of two, and truncating division rather than flooring it would put two different
 * tiles in one file.
 *
 * <p>The store's own thread does the work, so this waits for it rather than pretending it is
 * synchronous.
 */
final class Store {
    /** Long enough that a slow disk is not mistaken for a broken one, short enough to fail a build. */
    private static final long WAIT_MS = 20_000L;

    private Store() {
    }

    static void run() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainDisk");

        Path at = Files.createTempDirectory("artillerytablet-store");
        Field root = c.getDeclaredField("root");
        root.setAccessible(true);
        root.set(null, at);

        Method save = c.getDeclaredMethod("save", TerrainTile.class);
        Method request = c.getDeclaredMethod("request", long.class);
        Method collect = c.getDeclaredMethod("collect");
        Method missing = c.getDeclaredMethod("missing");
        for (Method m : new Method[]{save, request, collect, missing}) {
            m.setAccessible(true);
        }

        // Both signs on both axes, and one either side of a shard boundary.
        int[][] where = {{0, 0}, {5, 9}, {-1, -1}, {-33, 40}, {31, -32}, {-100000, 100000}};

        for (int[] tile : where) {
            TerrainTile out = TileRender.scene(tile[0], tile[1]);
            save.invoke(null, out);
        }

        for (int[] tile : where) {
            long key = TerrainTile.key(tile[0], tile[1]);
            check((boolean) request.invoke(null, key), "the store took a request for " + key);

            TerrainTile back = (TerrainTile) waitFor(collect, missing, key);
            check(back != null, "tile " + tile[0] + "," + tile[1] + " came back from the store");
            check(back.tileX == tile[0] && back.tileZ == tile[1],
                    "it came back as " + back.tileX + "," + back.tileZ);

            TerrainTile want = TileRender.scene(tile[0], tile[1]);
            check(back.contentHash() == want.contentHash(),
                    "tile " + tile[0] + "," + tile[1] + " came back with different ground");
        }

        // And ground that was never stored has to answer promptly rather than hanging.
        long absent = TerrainTile.key(4242, -4242);
        check((boolean) request.invoke(null, absent), "the store took a request for absent ground");
        Object nothing = waitFor(collect, missing, absent);
        check(nothing == null, "absent ground came back as absent");

        int files = (int) Files.walk(at).filter(Files::isRegularFile).count();
        check(files == where.length, "the store holds " + files + " files for " + where.length + " tiles");

        System.out.printf("store: OK  %d tiles written and read back, absent ground reported absent%n",
                where.length);

        inFlightReadsDoNotCrossWorlds();
        sharedLayout(where);
    }

    /**
     * Checks that the layout both stores share puts one tile in one file.
     *
     * <p>There are two stores now — the client keeps ground it was told about, the server keeps
     * ground it surveyed — and they write through the same code so that a file written by one is
     * readable by the other. What that code must never do is collide: two tiles landing on one name
     * would silently serve one world's ground for another's, and the sharding divides by a power of
     * two, where truncating rather than flooring puts a tile and its mirror in the same folder.
     */
    private static void sharedLayout(int[][] where) throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.terrain.TileFiles");
        Method fileFor = c.getDeclaredMethod("fileFor", Path.class, long.class);
        fileFor.setAccessible(true);

        Path root = Path.of("nowhere");
        java.util.Map<String, String> seen = new java.util.HashMap<>();

        for (int x = -80; x <= 80; x++) {
            for (int z = -80; z <= 80; z++) {
                String at = fileFor.invoke(null, root, TerrainTile.key(x, z)).toString();
                String was = seen.put(at, x + "," + z);
                check(was == null, "tiles " + was + " and " + x + "," + z + " both land on " + at);
            }
        }

        // And a spot check that the ones the round trip used are among them, so this is checking the
        // same function the files were actually written through.
        for (int[] tile : where) {
            String at = fileFor.invoke(null, root, TerrainTile.key(tile[0], tile[1])).toString();
            check(at.contains("t" + tile[0] + "_" + tile[1]), "tile " + tile[0] + "," + tile[1]
                    + " is named after itself, got " + at);
        }

        System.out.printf("store: OK  %d tile names across the origin, no two alike%n", seen.size());

        namesReadBack(fileFor, root);
    }

    /**
     * Checks that a file name can be read back into the tile it stands for.
     *
     * <p>The server's store keeps an index of what it holds so that a miss costs nothing, and it
     * builds that index by listing the folder and parsing the names. If parsing disagrees with
     * naming by so much as a minus sign, the index comes out empty, every lookup misses, and the
     * store silently does nothing at all — the map is merely slow again, for a reason no error
     * message would ever mention.
     */
    private static void namesReadBack(Method fileFor, Path root) throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.terrain.ServerTileStore");
        Method keyOf = c.getDeclaredMethod("keyOf", String.class);
        keyOf.setAccessible(true);

        int checked = 0;
        for (int x : new int[]{-100000, -1000, -33, -1, 0, 1, 33, 1000, 100000}) {
            for (int z : new int[]{-100000, -1000, -33, -1, 0, 1, 33, 1000, 100000}) {
                long key = TerrainTile.key(x, z);
                Path file = (Path) fileFor.invoke(null, root, key);
                Object back = keyOf.invoke(null, file.getFileName().toString());
                check(back != null, "the name " + file.getFileName() + " was not recognised at all");
                check(((Long) back) == key, "the name " + file.getFileName()
                        + " read back as " + back + ", not " + key);
                checked++;
            }
        }

        check(keyOf.invoke(null, "region.mca") == null, "a stranger's file is not mistaken for a tile");
        check(keyOf.invoke(null, "t5_9.tile.part") == null, "a half-written tile is not counted");

        System.out.printf("store: OK  %d file names read back to the tile they name%n", checked);

        worldFoldersStayApart();
    }

    /**
     * Checks that two worlds can never be filed in one folder.
     *
     * <p>Written after they were. The client's store puts a world's name in the path, and the name
     * was taken from a path ending in "." — so every single-player world resolved to the same
     * folder and each of them served the others' ground. Nothing failed: the map simply had country
     * on it that belonged somewhere else, and the only record of the truth was the folder layout.
     *
     * <p>What makes that possible is a name that survives cleaning as nothing, or as a path step
     * that means "here" or "up". A folder component has to be a name, and this is where that is
     * said.
     */
    private static void worldFoldersStayApart() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.terrain.TileFiles");
        Method safeName = c.getDeclaredMethod("safeName", String.class);
        safeName.setAccessible(true);

        // Nothing may clean into a path step. These are the inputs that produced the fault, and a
        // fallback placeholder is the right answer to all of them — an unusable name has to become
        // *something*, and the caller is responsible for not handing one over in the first place.
        String[] unusable = {".", "..", "", "   ", "/", "\\", "./."};
        for (String name : unusable) {
            String cleaned = (String) safeName.invoke(null, name);
            check(!cleaned.isEmpty(), "the world name \"" + name + "\" cleans to nothing, which "
                    + "resolves to the folder above and files every world together");
            check(!cleaned.equals(".") && !cleaned.equals(".."),
                    "the world name \"" + name + "\" cleans to \"" + cleaned + "\", which is a step "
                    + "along a path rather than a folder of its own");
        }

        // And names a player can actually produce have to stay apart from each other.
        String[] real = {"New World", "New World 2", "new world", "New-World", "Thế Giới", "a.b"};
        java.util.Map<String, String> seen = new java.util.HashMap<>();
        for (String name : real) {
            String cleaned = (String) safeName.invoke(null, name);
            String was = seen.put(cleaned, name);
            check(was == null, "worlds \"" + was + "\" and \"" + name + "\" both file under \""
                    + cleaned + "\", so each would be served the other's ground");
        }

        System.out.printf("store: OK  %d unusable world names refused, %d real ones each its own"
                + " folder%n", unusable.length, real.length);

        savePathNamesTheWorld();
    }

    /**
     * Checks that a save's path becomes the save's folder name.
     *
     * <p>The name a world is filed under comes from the path the game gives for the save root, and
     * that path ends in "." — the id of {@code LevelResource.ROOT}. Asked for its last part without
     * normalising first, every world in the game answers "." and every world is filed together. The
     * game is not here to ask, but the path arithmetic is the whole of the fix, and it is arithmetic.
     */
    private static void savePathNamesTheWorld() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainDisk");
        Method saveFolderName = c.getDeclaredMethod("saveFolderName", Path.class);
        saveFolderName.setAccessible(true);

        // Shaped as the game hands them over: the save root with ROOT's "." on the end.
        String[] worlds = {"New World", "New World 2", "New World (1)", "Thế Giới"};
        java.util.Map<String, String> seen = new java.util.HashMap<>();
        for (String world : worlds) {
            Path path = Path.of("saves", world, ".");
            String name = (String) saveFolderName.invoke(null, path);
            check(world.equals(name), "the save at " + path + " is named \"" + name + "\"");
            String was = seen.put(name, world);
            check(was == null, "saves \"" + was + "\" and \"" + world + "\" both name \"" + name
                    + "\", so each would be served the other's ground");
        }

        // A path that names no folder has to say so, rather than answer with a path step. The caller
        // falls back to the displayed name there — a poorer identity, but an identity.
        check(saveFolderName.invoke(null, Path.of("/")) == null,
                "a path naming no folder was taken as a name");

        System.out.printf("store: OK  %d save paths each named after its own folder%n",
                worlds.length);
    }

    /**
     * Checks that a read started in one world cannot answer in the next.
     *
     * <p>Reads happen on another thread and answer whenever the disk gets round to them. Leaving a
     * world empties the queues, and that covers only what has already come back — the dangerous one
     * is the read still in flight, whose tile coordinates mean somewhere else by the time it lands.
     *
     * <p>Made to happen rather than waited for: the store has one IO thread, so a task holding it
     * keeps the read pending while the world changes underneath.
     */
    private static void inFlightReadsDoNotCrossWorlds() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainDisk");
        Field io = c.getDeclaredField("IO");
        io.setAccessible(true);
        java.util.concurrent.ExecutorService pool =
                (java.util.concurrent.ExecutorService) io.get(null);

        Method request = c.getDeclaredMethod("request", long.class);
        Method collect = c.getDeclaredMethod("collect");
        Method missing = c.getDeclaredMethod("missing");
        Method clear = c.getDeclaredMethod("clear");
        for (Method m : new Method[]{request, collect, missing, clear}) {
            m.setAccessible(true);
        }

        java.util.concurrent.CountDownLatch gate = new java.util.concurrent.CountDownLatch(1);
        pool.execute(() -> {
            try {
                gate.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Ground that is really there, so anything arriving is the guard failing rather than the
        // store having nothing to give.
        long key = TerrainTile.key(5, 9);
        check((boolean) request.invoke(null, key), "the store took a request before the world changed");

        clear.invoke(null);
        gate.countDown();

        // One thread, so a task queued behind the read has run only once the read is done.
        pool.submit(() -> null).get();

        check(collect.invoke(null) == null,
                "a tile read for the previous world arrived in this one");
        check(missing.invoke(null) == null,
                "a read for the previous world reported ground missing in this one");

        System.out.println("store: OK  a read outlived its world and was refused on arrival");
    }

    /**
     * Waits for the store's thread to answer about one tile.
     *
     * @return the tile, or null when the store reported it absent
     */
    private static Object waitFor(Method collect, Method missing, long key) throws Exception {
        long until = System.currentTimeMillis() + WAIT_MS;
        while (System.currentTimeMillis() < until) {
            TerrainTile tile = (TerrainTile) collect.invoke(null);
            if (tile != null) {
                return tile;
            }
            Long absent = (Long) missing.invoke(null);
            if (absent != null) {
                check(absent == key, "the store answered about " + absent + ", not " + key);
                return null;
            }
            Thread.sleep(5);
        }
        throw new AssertionError("FAILED: the store never answered about " + key);
    }
}
