package net.nazarick.artillerytablet.mapcheck.convert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Turns the logical grid into as few filled rectangles as will reproduce it exactly.
 *
 * <p><b>Exactly</b> is the word that matters. This step is not allowed to be an approximation of the
 * grid: whatever comes out here is what the game will draw, so if it drifted from the preview the
 * preview would stop being worth looking at — which is the whole failure this converter exists to
 * end. Every cell is covered by exactly one rectangle of its own colour, so the rectangles tile the
 * picture without overlap and the result cannot depend on the order they are drawn in.
 *
 * <p>That last property is worth stating plainly because it buys something: since nothing overlaps,
 * the draw order is free, and it can be spent entirely on making the generated source readable.
 */
final class RectMerge {
    /** One filled rectangle in logical cells: half-open in x and y, as the drawing code wants. */
    record Rect(int x0, int y0, int x1, int y1, int colour) {
        int area() {
            return (x1 - x0) * (y1 - y0);
        }
    }

    private RectMerge() {
    }

    /**
     * Runs of one colour along a row, grown downwards while the row below repeats them.
     *
     * <p>Greedy, and deliberately not the theoretically minimal decomposition. Finding the fewest
     * possible rectangles is a much harder problem, and what it would buy is a few per cent fewer
     * {@code fill} calls in exchange for an arrangement nobody can look at and follow. This produces
     * long horizontal bands across flat areas — which is what the picture is mostly made of — and
     * they read in the generated source the way the panel reads on screen.
     */
    static List<Rect> merge(Pixels pixels) {
        boolean[] used = new boolean[pixels.w * pixels.h];
        List<Rect> rects = new ArrayList<>();

        for (int y = 0; y < pixels.h; y++) {
            int x = 0;
            while (x < pixels.w) {
                if (used[y * pixels.w + x]) {
                    x++;
                    continue;
                }
                int colour = pixels.at(x, y);
                int runEnd = x + 1;
                while (runEnd < pixels.w
                        && !used[y * pixels.w + runEnd]
                        && pixels.at(runEnd, y) == colour) {
                    runEnd++;
                }

                int bottom = y + 1;
                while (bottom < pixels.h && rowRepeats(pixels, used, x, runEnd, bottom, colour)) {
                    bottom++;
                }

                for (int yy = y; yy < bottom; yy++) {
                    for (int xx = x; xx < runEnd; xx++) {
                        used[yy * pixels.w + xx] = true;
                    }
                }
                rects.add(new Rect(x, y, runEnd, bottom, colour));
                x = runEnd;
            }
        }

        // Largest first: the chassis and the screen well come out at the top of the generated
        // method and the bolt heads at the bottom, so reading the source top to bottom is reading
        // the panel from its biggest surfaces inward. Free to do, because nothing overlaps.
        rects.sort(Comparator.comparingInt(Rect::area).reversed());
        return rects;
    }

    private static boolean rowRepeats(Pixels pixels, boolean[] used,
                                      int x0, int x1, int y, int colour) {
        for (int x = x0; x < x1; x++) {
            if (used[y * pixels.w + x] || pixels.at(x, y) != colour) {
                return false;
            }
        }
        return true;
    }
}
