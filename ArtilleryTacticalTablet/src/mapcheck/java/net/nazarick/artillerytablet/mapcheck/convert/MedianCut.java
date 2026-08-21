package net.nazarick.artillerytablet.mapcheck.convert;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a small adaptive palette for one image, and maps every pixel onto it.
 *
 * <p><b>Why an adaptive palette and not a fixed one.</b> A fixed palette has to be chosen before the
 * image is seen, so it spends its colours evenly over a space the image mostly does not occupy — on
 * a dark grey instrument panel that means a dozen greens nobody needs and three greys where the
 * whole design lives. Median cut spends every entry where the pixels actually are: the panel gets
 * its greys separated far enough to keep a recess distinct from the face it is cut into, which is
 * the distinction the whole picture is made of.
 *
 * <p><b>Why quantising comes before shrinking.</b> Shrinking first and quantising after averages
 * real colours together and then rounds the average, so a boundary between two flat regions turns
 * into a third colour that exists nowhere in the design — the edge is gone before the palette is
 * ever consulted. Quantised first, the boundary is still a hard step when the grid is reduced, and
 * the reduction can be made to pick a side rather than to blend.
 */
final class MedianCut {
    /** The palette, packed ARGB, always opaque. */
    private final int[] palette;

    /**
     * Nearest-palette-entry for every colour rounded to fifteen bits.
     *
     * <p>Mapping a million and a half pixels against sixty-four entries is a hundred million
     * distance sums, and the overwhelming majority of them are asked about a colour that has already
     * been looked up. Rounding to five bits a channel makes a table of thirty-two thousand answers
     * that fits in memory and turns the whole pass into an array read. The rounding is invisible
     * because it happens on the way into a decision that is itself far coarser.
     */
    private final int[] cache = new int[32768];

    private MedianCut(int[] palette) {
        this.palette = palette;
        Arrays.fill(cache, -1);
    }

    int[] palette() {
        return palette;
    }

    int size() {
        return palette.length;
    }

    /**
     * Builds a palette of at most {@code colours} entries for an image.
     *
     * <p>Pixels are sampled rather than all read: median cut only needs to know the shape of the
     * cloud of colours, and a sixth of a large photograph describes that shape as well as all of it
     * for a hundredth of the sorting. The step is chosen so the sample is spread over the whole
     * image rather than taken from one band of it.
     */
    static MedianCut build(BufferedImage src, int colours) {
        int w = src.getWidth();
        int h = src.getHeight();
        long total = (long) w * h;
        int step = (int) Math.max(1, total / 200_000L);

        List<int[]> sample = new ArrayList<>();
        for (long i = 0; i < total; i += step) {
            int x = (int) (i % w);
            int y = (int) (i / w);
            int rgb = src.getRGB(x, y);
            sample.add(new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF});
        }

        List<List<int[]>> boxes = new ArrayList<>();
        boxes.add(sample);

        // Split the box with the widest single channel each round. Splitting the *largest* box
        // instead is the more obvious rule and the wrong one here: a huge flat expanse of chassis
        // grey is one enormous box with almost no spread, and splitting it wastes entries carving
        // up a colour the eye reads as one, while a small but wide-ranging box — the red button, the
        // lit edges — keeps getting passed over.
        while (boxes.size() < colours) {
            int widest = -1;
            int widestSpread = 0;
            for (int i = 0; i < boxes.size(); i++) {
                List<int[]> box = boxes.get(i);
                if (box.size() < 2) {
                    continue;
                }
                int spread = spread(box);
                if (spread > widestSpread) {
                    widestSpread = spread;
                    widest = i;
                }
            }
            if (widest < 0 || widestSpread == 0) {
                break;
            }
            List<int[]> box = boxes.remove(widest);
            int channel = widestChannel(box);
            box.sort((a, b) -> Integer.compare(a[channel], b[channel]));
            int mid = box.size() / 2;
            boxes.add(new ArrayList<>(box.subList(0, mid)));
            boxes.add(new ArrayList<>(box.subList(mid, box.size())));
        }

        int[] palette = new int[boxes.size()];
        for (int i = 0; i < boxes.size(); i++) {
            List<int[]> box = boxes.get(i);
            long r = 0;
            long g = 0;
            long b = 0;
            for (int[] px : box) {
                r += px[0];
                g += px[1];
                b += px[2];
            }
            int n = Math.max(1, box.size());
            palette[i] = 0xFF000000
                    | ((int) (r / n) << 16)
                    | ((int) (g / n) << 8)
                    | (int) (b / n);
        }
        return new MedianCut(palette);
    }

    private static int spread(List<int[]> box) {
        int best = 0;
        for (int c = 0; c < 3; c++) {
            int lo = 255;
            int hi = 0;
            for (int[] px : box) {
                lo = Math.min(lo, px[c]);
                hi = Math.max(hi, px[c]);
            }
            best = Math.max(best, hi - lo);
        }
        return best;
    }

    private static int widestChannel(List<int[]> box) {
        int bestChannel = 0;
        int bestSpread = -1;
        for (int c = 0; c < 3; c++) {
            int lo = 255;
            int hi = 0;
            for (int[] px : box) {
                lo = Math.min(lo, px[c]);
                hi = Math.max(hi, px[c]);
            }
            if (hi - lo > bestSpread) {
                bestSpread = hi - lo;
                bestChannel = c;
            }
        }
        return bestChannel;
    }

    /** The index of the palette entry nearest a packed colour. */
    int indexOf(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int key = ((r >> 3) << 10) | ((g >> 3) << 5) | (b >> 3);
        int hit = cache[key];
        if (hit >= 0) {
            return hit;
        }
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < palette.length; i++) {
            int p = palette[i];
            int dr = r - ((p >> 16) & 0xFF);
            int dg = g - ((p >> 8) & 0xFF);
            int db = b - (p & 0xFF);
            int distance = dr * dr + dg * dg + db * db;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        cache[key] = best;
        return best;
    }

    /** How far apart two palette entries are, as a plain channel-sum. */
    int contrast(int indexA, int indexB) {
        int a = palette[indexA];
        int b = palette[indexB];
        return Math.abs(((a >> 16) & 0xFF) - ((b >> 16) & 0xFF))
                + Math.abs(((a >> 8) & 0xFF) - ((b >> 8) & 0xFF))
                + Math.abs((a & 0xFF) - (b & 0xFF));
    }
}
