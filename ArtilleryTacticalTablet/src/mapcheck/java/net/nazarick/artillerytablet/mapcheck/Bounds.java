package net.nazarick.artillerytablet.mapcheck;

import java.lang.reflect.Method;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;
import static net.nazarick.artillerytablet.mapcheck.MapCheck.readInt;

/**
 * Checks that every array index the shading pass computes lands inside the gathered field, using the
 * real constants read off the class rather than copies of them.
 *
 * <p>This is the check that was missing. A boot test only proves the client reaches the main menu;
 * it never opens the tablet, so it never runs a single line of the map's drawing. The border ring
 * added one texel to the shaded area without widening the margin it reads from, and the very first
 * frame of the very first map drawn read off the front of the array.
 */
final class Bounds {
    private Bounds() {
    }

    static void run() throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");

        int sheet = readInt(c, "SHEET_PIXELS");
        int border = readInt(c, "BORDER");
        int texture = readInt(c, "TEXTURE_PIXELS");
        int run = readInt(c, "RELIEF_RUN");
        int margin = readInt(c, "MARGIN");
        int field = readInt(c, "FIELD");
        System.out.printf("bounds: SHEET=%d BORDER=%d TEXTURE=%d RELIEF_RUN=%d MARGIN=%d FIELD=%d%n",
                sheet, border, texture, run, margin, field);

        check(texture == sheet + 2 * border, "texture is the sheet plus its border");
        check(margin >= border + run, "margin covers the border ring and what it reads past itself");
        check(field == sheet + 2 * margin, "field is the sheet plus its margin");

        int cells = field * field;
        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;

        // Exactly the loop build() runs, and exactly the samples slope() takes from each cell.
        for (int tz = 0; tz < texture; tz++) {
            int fz = tz - border + margin;
            for (int tx = 0; tx < texture; tx++) {
                int cell = fz * field + tx - border + margin;
                check(cell >= 0 && cell < cells, "cell " + cell + " at texel " + tx + "," + tz);

                for (int samples : new int[]{1, run}) {
                    int north = cell - samples * field;
                    int west = cell - samples;
                    check(north >= 0, "north sample " + north + " at texel " + tx + "," + tz);
                    check(west >= 0, "west sample " + west + " at texel " + tx + "," + tz);
                    lowest = Math.min(lowest, Math.min(north, west));
                }
                highest = Math.max(highest, cell);
            }
        }

        // And the loop gather() runs, which must cover every cell those samples reach.
        for (int fz = 0; fz < field; fz++) {
            for (int fx = 0; fx < field; fx++) {
                int cell = fz * field + fx;
                check(cell >= 0 && cell < cells, "gathered cell " + cell);
            }
        }

        System.out.printf("bounds: OK  %d texels shaded, indices %d..%d of %d, %d to spare at each end%n",
                texture * texture, lowest, highest, cells, Math.min(lowest, cells - 1 - highest));

        patchesLandInside(c, sheet, border);
    }

    /**
     * Checks the rectangle the coarse patch reads out of a coarser sheet.
     *
     * <p>The patch pass stands a piece of a coarse sheet in for a fine square that has not been
     * built. That only works because a coarse sheet covers a whole number of fine squares and both
     * are laid out from the world origin, so the offset into the coarse texture is exact. If it were
     * not, the patch would read the border ring or run off the end — and neither shows up as a
     * crash, only as a smear that looks like ordinary map at the wrong offset.
     *
     * <p>Negative tile coordinates are in the range on purpose. Half the world has them, and the
     * whole thing rests on floor division rather than truncation.
     */
    private static void patchesLandInside(Class<?> c, int sheetPixels, int border) throws Exception {
        int steps = readInt(c, "UNDERLAY_STEPS");
        int maxLevel = readInt(c, "MAX_LEVEL");

        Method strideFor = c.getDeclaredMethod("strideFor", int.class);
        Method groundPerSheet = c.getDeclaredMethod("groundPerSheet", int.class);
        strideFor.setAccessible(true);
        groundPerSheet.setAccessible(true);

        int checked = 0;
        for (int fine = 0; fine <= maxLevel; fine++) {
            int under = Math.min(fine + steps, maxLevel);
            if (under == fine) {
                continue;
            }

            int coverFine = (int) groundPerSheet.invoke(null, fine);
            int coverUnder = (int) groundPerSheet.invoke(null, under);
            int strideUnder = (int) strideFor.invoke(null, under);

            check(coverUnder % coverFine == 0,
                    "a coarse square at level " + under + " is a whole number of level " + fine + " squares");
            check(coverFine % strideUnder == 0,
                    "a level " + fine + " square is a whole number of level " + under + " texels");

            int span = coverFine / strideUnder;
            check(span >= 1, "level " + fine + " patched from level " + under + " has " + span + " texels");

            for (int tile : new int[]{-100000, -33, -8, -1, 0, 1, 7, 64, 100000}) {
                int origin = tile * coverFine;
                int underTile = Math.floorDiv(origin, coverUnder);
                int inset = origin - underTile * coverUnder;

                check(inset >= 0 && inset < coverUnder, "inset " + inset + " at tile " + tile);
                check(inset % strideUnder == 0, "inset " + inset + " is a whole number of texels");

                int at = border + inset / strideUnder;
                check(at >= border && at + span <= border + sheetPixels,
                        "patch reads texels " + at + ".." + (at + span)
                                + " of level " + under + ", outside the inner square at tile " + tile);
                checked++;
            }
        }
        System.out.printf("bounds: OK  %d patch rectangles land inside the coarse sheet%n", checked);
    }
}
