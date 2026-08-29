package net.nazarick.artillerytablet.mapcheck;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Runs every check the map has, outside Minecraft.
 *
 * <p>This exists because a clean build and a clean boot prove almost nothing about this part of the
 * mod. A build proves the code compiles; a boot proves it loads. Neither opens the tablet, so
 * neither runs a single line of the drawing — and the drawing is where every fault of the last
 * fortnight has been. Two of the checks here found crashes that a boot had already declared clean:
 * one that fired on the first frame the map was ever shown, and one that needed several minutes of
 * play before there was enough data to trip it.
 *
 * <p>Everything below reaches into the production classes by reflection and calls them. Nothing is
 * re-implemented, deliberately: a harness that keeps its own copy of the loop it is checking drifts
 * away from the real one and then confidently checks the copy. The cost is that renaming a field
 * breaks these at run time rather than at compile time, which is a fair trade for their being unable
 * to lie.
 *
 * <p>Run with {@code ./gradlew mapCheck}. The first argument is the directory to write the picture
 * into.
 */
public final class MapCheck {
    private MapCheck() {
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "build/mapcheck");
        if (!out.isDirectory() && !out.mkdirs()) {
            throw new IllegalStateException("could not make " + out.getAbsolutePath());
        }

        // The block registry has to exist before anything can ask it for an id, and outside the
        // game nothing has filled it. This is the same pair of calls a data generator makes: it
        // registers vanilla content and nothing else — no window, no world, no mods.
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();

        // Assertions first, picture last. The assertions are quick and they fail loudly, so there is
        // no sense spending a second and a half drawing a preview of code that is already broken.
        //
        // Limits/Bounds/Relief/Evict/Zooms/TileRender/Patch/Store all tested the old tile-and-sheet
        // renderer (TerrainImage, TerrainClientCache, TerrainDisk, TileFiles, the server-side survey
        // path) — retired this session in favour of the mapengine module, which has its own,
        // considerably more thorough benchmark/correctness suite: run `./gradlew :mapengine:bench`.
        // What remains here (RoundTrip, Splash) tests things that did not move: TerrainTile's own
        // wire format, and the boot-splash art, neither of which mapengine has any part in.
        RoundTrip.run();
        Splash.run(new File(out, "boot-splash.png"));

        System.out.println();
        System.out.println("map checks passed");
    }

    // ---- shared reflection ---------------------------------------------------------------------
    //
    // Read the real constants off the real classes rather than restating them here. A harness that
    // holds its own copy of MARGIN is a harness that will keep passing after MARGIN changes.

    static int readInt(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(null);
    }

    static Object readField(Class<?> owner, Object on, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(on);
    }

    static void check(boolean ok, String what) {
        if (!ok) {
            throw new AssertionError("FAILED: " + what);
        }
    }
}
