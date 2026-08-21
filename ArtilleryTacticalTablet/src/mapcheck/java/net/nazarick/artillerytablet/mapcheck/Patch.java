package net.nazarick.artillerytablet.mapcheck;

import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.lang.reflect.Method;
import java.util.Arrays;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;
import static net.nazarick.artillerytablet.mapcheck.MapCheck.readField;
import static net.nazarick.artillerytablet.mapcheck.MapCheck.readInt;

/**
 * Proves that repainting the part of a square a tile landed on gives the same pixels as redrawing
 * the whole square.
 *
 * <p>This is the check the change is not safe to make without. Redrawing everything is obviously
 * correct and was measured at three milliseconds; repainting a rectangle is a fraction of that and
 * is correct only if two ranges are exactly right — which texels a tile can affect, and how far the
 * gather has to reach for the shading of those texels to have something to read. Getting either
 * wrong does not crash. It leaves a seam of stale lighting along tile boundaries, which reads as a
 * feature of the terrain.
 *
 * <p>The scratch arrays are deliberately <em>poisoned</em> before the patch runs. In play they hold
 * whatever the last square gathered, so a patch that reads one cell outside what it gathered would
 * pick up another square's ground — and would still look plausible. Filling them with nonsense first
 * turns that from a subtle wrongness into a mismatch this can see.
 */
final class Patch {
    private Patch() {
    }

    static void run() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");

        int border = readInt(c, "BORDER");
        int margin = readInt(c, "MARGIN");
        int field = readInt(c, "FIELD");
        int texture = readInt(c, "TEXTURE_PIXELS");
        int relief = readInt(c, "RELIEF_RUN");

        // Static and workspace-taking since the bake moved off the render thread. Renaming them
        // did not break any compile, only this line, which is why mapCheck had been dead.
        Class<?> ws = Class.forName(
                "net.nazarick.artillerytablet.client.terrain.TerrainImage$BakeWorkspace");
        java.lang.reflect.Constructor<?> newWorkspace = ws.getDeclaredConstructor();
        newWorkspace.setAccessible(true);

        Method gather = c.getDeclaredMethod("gatherInto", ws, int.class, int.class, int.class,
                int.class, int.class, int.class, int.class);
        Method shade = c.getDeclaredMethod("shadeCell", ws, int.class, int.class);
        Method rectFor = c.getDeclaredMethod("rectFor", long.class, int.class, int.class, int.class);
        Method strideFor = c.getDeclaredMethod("strideFor", int.class);
        Method groundPerSheet = c.getDeclaredMethod("groundPerSheet", int.class);
        for (Method m : new Method[]{gather, shade, rectFor, strideFor, groundPerSheet}) {
            m.setAccessible(true);
        }

        // Coarse levels as well as fine: a tile covers sixty-four texels at level zero and four at
        // level four, and the rounding that turns blocks into texels is where this would go wrong.
        for (int level : new int[]{0, 1, 2, 4}) {
            int stride = (int) strideFor.invoke(null, level);
            int cover = (int) groundPerSheet.invoke(null, level);

            // A square somewhere in the middle of the ground the scene lays down.
            int sheetX = 2000 / cover;
            int sheetZ = 2000 / cover;
            int originX = sheetX * cover;
            int originZ = sheetZ * cover;

            Object image = c.getDeclaredConstructor().newInstance();
            Object work = newWorkspace.newInstance();
            short[] floor = (short[]) readField(ws, work, "fieldFloor");
            int[] colour = (int[]) readField(ws, work, "fieldColour");
            float[] murk = (float[]) readField(ws, work, "fieldMurk");
            float[] hazard = (float[]) readField(ws, work, "fieldHazard");

            // What the whole square looks like when every texel is drawn from a full gather.
            gather.invoke(null, work, originX, originZ, stride, 0, 0, field - 1, field - 1);
            int[] whole = new int[texture * texture];
            for (int tz = 0; tz < texture; tz++) {
                for (int tx = 0; tx < texture; tx++) {
                    int cell = (tz - border + margin) * field + tx - border + margin;
                    whole[tz * texture + tx] = floor[cell] == TerrainTile.NO_DATA
                            ? 0 : (int) shade.invoke(null, work, cell, stride);
                }
            }

            // A tile that lands somewhere inside it.
            int tileX = TerrainTile.blockToTile(originX + cover / 2);
            int tileZ = TerrainTile.blockToTile(originZ + cover / 2);
            int[] at = (int[]) rectFor.invoke(image, TerrainTile.key(tileX, tileZ),
                    level, originX, originZ);
            check(at != null, "level " + level + ": the tile in the middle lands in the square");

            int fromTx = at[0];
            int fromTz = at[1];
            int toTx = at[2];
            int toTz = at[3];
            int area = (toTx - fromTx + 1) * (toTz - fromTz + 1);
            check(area < texture * texture,
                    "level " + level + ": a patch of " + area + " texels is smaller than the square");

            // Nonsense everywhere, so anything read from outside the patch's own gather shows up.
            Arrays.fill(floor, (short) 12345);
            Arrays.fill(colour, 0xFF00FF00);
            Arrays.fill(murk, 0.75f);
            Arrays.fill(hazard, 0.5f);

            // Exactly the range the production repaint gathers for this rectangle.
            gather.invoke(null, work, originX, originZ, stride,
                    fromTx - border + margin - relief, fromTz - border + margin - relief,
                    toTx - border + margin, toTz - border + margin);

            int compared = 0;
            for (int tz = fromTz; tz <= toTz; tz++) {
                for (int tx = fromTx; tx <= toTx; tx++) {
                    int cell = (tz - border + margin) * field + tx - border + margin;
                    int got = floor[cell] == TerrainTile.NO_DATA
                            ? 0 : (int) shade.invoke(null, work, cell, stride);
                    check(got == whole[tz * texture + tx],
                            "level " + level + ": patched texel " + tx + "," + tz
                                    + " is " + Integer.toHexString(got)
                                    + ", whole-square is " + Integer.toHexString(whole[tz * texture + tx]));
                    compared++;
                }
            }

            System.out.printf("patch: OK  level %d, %d of %d texels repainted, all identical%n",
                    level, compared, texture * texture);
        }
    }
}
