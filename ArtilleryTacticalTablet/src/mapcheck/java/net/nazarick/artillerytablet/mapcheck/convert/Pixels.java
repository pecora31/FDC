package net.nazarick.artillerytablet.mapcheck.convert;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * The logical pixel grid: one palette index per cell, and the two passes that produce it.
 *
 * <p>This is the stage the whole converter turns on. What comes out of here is not a small copy of
 * the photograph — it is a statement about what the design <em>is</em>, in as few cells and as few
 * colours as will still carry it. Everything after this only rearranges those cells into rectangles
 * and writes them out.
 */
final class Pixels {
    final int w;
    final int h;
    /** Palette index per cell, row-major. */
    final int[] cells;

    private Pixels(int w, int h, int[] cells) {
        this.w = w;
        this.h = h;
        this.cells = cells;
    }

    int at(int x, int y) {
        return cells[y * w + x];
    }

    /**
     * Reduces a quantised source to the logical grid by taking each cell's <b>most common</b>
     * colour.
     *
     * <p><b>Not the average, and this is the single most important line in the converter.</b>
     * Averaging is what an image resizer does, and it is exactly wrong here: the mean of two palette
     * colours is a third colour that is in neither the palette nor the design, so every boundary
     * between two flat regions comes back as a soft ramp — the same blur the quantisation step just
     * removed, reintroduced one step later. Taking the most common colour forces every cell to
     * commit to one region or the other, so a boundary stays a boundary and lands on a cell edge
     * where a rectangle can later be cut along it.
     *
     * <p>Ties break towards the colour under the cell's own centre, so the result is deterministic
     * and, where a cell genuinely straddles an edge, follows the middle of the cell rather than
     * whichever colour happened to be counted first.
     */
    static Pixels reduce(BufferedImage src, MedianCut palette, int logicalW, int logicalH) {
        int sw = src.getWidth();
        int sh = src.getHeight();

        // Quantise the full-resolution image once. Done inside the per-cell loop instead, every
        // source pixel under a cell boundary would be quantised twice and the table lookups would
        // dominate the run.
        int[] quantised = new int[sw * sh];
        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                quantised[y * sw + x] = palette.indexOf(src.getRGB(x, y));
            }
        }

        int[] cells = new int[logicalW * logicalH];
        int[] counts = new int[palette.size()];

        for (int cy = 0; cy < logicalH; cy++) {
            int y0 = (int) ((long) cy * sh / logicalH);
            int y1 = (int) ((long) (cy + 1) * sh / logicalH);
            if (y1 <= y0) {
                y1 = y0 + 1;
            }
            for (int cx = 0; cx < logicalW; cx++) {
                int x0 = (int) ((long) cx * sw / logicalW);
                int x1 = (int) ((long) (cx + 1) * sw / logicalW);
                if (x1 <= x0) {
                    x1 = x0 + 1;
                }

                Arrays.fill(counts, 0);
                for (int y = y0; y < y1 && y < sh; y++) {
                    int row = y * sw;
                    for (int x = x0; x < x1 && x < sw; x++) {
                        counts[quantised[row + x]]++;
                    }
                }

                int centre = quantised[Math.min(sh - 1, (y0 + y1) / 2) * sw
                        + Math.min(sw - 1, (x0 + x1) / 2)];
                int best = centre;
                int bestCount = counts[centre];
                for (int i = 0; i < counts.length; i++) {
                    if (counts[i] > bestCount) {
                        bestCount = counts[i];
                        best = i;
                    }
                }
                cells[cy * logicalW + cx] = best;
            }
        }
        return new Pixels(logicalW, logicalH, cells);
    }

    /**
     * Removes specks left over from photographic noise, and leaves deliberate small details alone.
     *
     * <p><b>There is no clever way to tell the two apart, and pretending otherwise would be the
     * dishonest part of this tool.</b> Nothing in the pixels says whether a four-cell blob is a bolt
     * head somebody designed or a compression artefact. What can be measured is <em>contrast</em>:
     * a detail that was put there on purpose is usually far from its surroundings in colour — a
     * black bore in a grey block, a red cap in a dark row — while noise is a slightly different
     * shade of what is already around it. So a small region is dissolved into its surroundings only
     * when it is <em>also</em> close to them in colour, and the two thresholds are left on sliders
     * because the right values are a judgement about one particular picture, not a constant.
     */
    Pixels denoise(MedianCut palette, int maxArea, int keepContrast) {
        if (maxArea <= 0) {
            return this;
        }
        int[] out = cells.clone();
        boolean[] seen = new boolean[cells.length];
        Deque<Integer> stack = new ArrayDeque<>();
        int[] component = new int[cells.length];

        for (int start = 0; start < cells.length; start++) {
            if (seen[start]) {
                continue;
            }
            int colour = cells[start];
            int count = 0;
            stack.push(start);
            seen[start] = true;

            while (!stack.isEmpty()) {
                int at = stack.pop();
                component[count++] = at;
                int x = at % w;
                int y = at / w;
                if (x > 0 && !seen[at - 1] && cells[at - 1] == colour) {
                    seen[at - 1] = true;
                    stack.push(at - 1);
                }
                if (x < w - 1 && !seen[at + 1] && cells[at + 1] == colour) {
                    seen[at + 1] = true;
                    stack.push(at + 1);
                }
                if (y > 0 && !seen[at - w] && cells[at - w] == colour) {
                    seen[at - w] = true;
                    stack.push(at - w);
                }
                if (y < h - 1 && !seen[at + w] && cells[at + w] == colour) {
                    seen[at + w] = true;
                    stack.push(at + w);
                }
            }

            if (count > maxArea) {
                continue;
            }

            // Which colour presses on this region hardest from outside. That is what it would be
            // dissolved into, and it is also what its contrast has to be judged against — measuring
            // against the whole image would call a dark speck on a dark panel "high contrast"
            // because somewhere else there is a red button.
            int[] neighbourCount = new int[palette.size()];
            for (int i = 0; i < count; i++) {
                int at = component[i];
                int x = at % w;
                int y = at / w;
                if (x > 0 && cells[at - 1] != colour) {
                    neighbourCount[cells[at - 1]]++;
                }
                if (x < w - 1 && cells[at + 1] != colour) {
                    neighbourCount[cells[at + 1]]++;
                }
                if (y > 0 && cells[at - w] != colour) {
                    neighbourCount[cells[at - w]]++;
                }
                if (y < h - 1 && cells[at + w] != colour) {
                    neighbourCount[cells[at + w]]++;
                }
            }
            int dominant = -1;
            int dominantCount = 0;
            for (int i = 0; i < neighbourCount.length; i++) {
                if (neighbourCount[i] > dominantCount) {
                    dominantCount = neighbourCount[i];
                    dominant = i;
                }
            }
            if (dominant < 0) {
                continue;
            }
            if (palette.contrast(colour, dominant) >= keepContrast) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                out[component[i]] = dominant;
            }
        }
        return new Pixels(w, h, out);
    }

    /**
     * Replaces a region with the single colour that already dominates it.
     *
     * <p>Its own colour rather than a chosen one, so the flattened area still sits in the picture at
     * roughly the right value and the preview stays readable as a device — a screen blanked to pure
     * black reads as a hole and makes the surrounding bezel impossible to judge.
     */
    Pixels flatten(Convert.Flatten region) {
        if (region == null || !region.active()) {
            return this;
        }
        int x0 = Math.max(0, (int) Math.floor(region.x0() * w));
        int y0 = Math.max(0, (int) Math.floor(region.y0() * h));
        int x1 = Math.min(w, (int) Math.ceil(region.x1() * w));
        int y1 = Math.min(h, (int) Math.ceil(region.y1() * h));
        if (x1 <= x0 || y1 <= y0) {
            return this;
        }

        int[] counts = new int[256];
        int maxIndex = 0;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                int c = at(x, y);
                if (c < counts.length) {
                    counts[c]++;
                    maxIndex = Math.max(maxIndex, c);
                }
            }
        }
        int dominant = 0;
        for (int i = 0; i <= maxIndex; i++) {
            if (counts[i] > counts[dominant]) {
                dominant = i;
            }
        }

        int[] out = cells.clone();
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                out[y * w + x] = dominant;
            }
        }
        return new Pixels(w, h, out);
    }

    /** The grid as an image at one screen pixel per cell, for the previews to magnify. */
    BufferedImage toImage(MedianCut palette) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        int[] entries = palette.palette();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.setRGB(x, y, entries[at(x, y)]);
            }
        }
        return img;
    }
}
