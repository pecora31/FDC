package net.nazarick.artillerytablet.mapcheck.convert;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The whole conversion, in the order the stages have to happen.
 *
 * <p><b>The order is the design.</b> Each stage throws away a different kind of information, and
 * doing any two of them in the wrong order throws away the wrong thing:
 *
 * <ol>
 *   <li><b>Quantise at full resolution.</b> Decide what colours the design is made of while every
 *       pixel is still there to vote.</li>
 *   <li><b>Reduce to the logical grid by majority.</b> Decide which side of every boundary each
 *       cell falls on — never blend across one.</li>
 *   <li><b>Dissolve small low-contrast regions.</b> Drop photographic noise, keep deliberate
 *       detail.</li>
 *   <li><b>Merge cells into rectangles.</b> Say the same picture in the fewest draw calls.</li>
 * </ol>
 *
 * <p>Doing (2) before (1) blurs boundaries into colours that are in no palette. Doing (3) before (2)
 * cleans noise that the reduction was about to remove anyway, and removes detail the reduction would
 * have kept. Doing (4) at any other point has nothing stable to merge.
 */
final class Convert {
    /**
     * Everything a run is allowed to be told.
     *
     * @param logicalW      cells across; the single biggest control over how the result reads
     * @param logicalH      cells down
     * @param colours       palette ceiling; median cut may return fewer on a flat picture
     * @param noiseMaxArea  regions this size or smaller may be dissolved — 0 disables the pass
     * @param keepContrast  a small region this far from its surroundings is kept regardless
     */
    record Settings(int logicalW, int logicalH, int colours, int noiseMaxArea, int keepContrast,
                    Flatten flatten) {
    }

    /**
     * A region to replace with one flat colour before the rectangles are cut.
     *
     * <p><b>Why a converter for a device case needs this at all.</b> The reference picture shows the
     * whole tablet, screen included, and the screen is a photograph — trees, roads, buildings. That
     * region cannot be said in rectangles cheaply and there is no reason to try: at runtime the map
     * renderer draws over every pixel of it. Left in, it produced nine tenths of the rectangles in
     * the file and none of them would ever be seen.
     *
     * <p>So the screen is stated once, as its own flat colour, and the rectangle budget goes where
     * the design actually is. Given as fractions of the picture rather than pixels, so the same
     * numbers keep meaning the same region if the reference is ever re-exported at another size.
     */
    record Flatten(double x0, double y0, double x1, double y1) {
        static final Flatten NONE = new Flatten(0, 0, 0, 0);

        boolean active() {
            return x1 > x0 && y1 > y0;
        }
    }

    /** What a run produced, including the two intermediate pictures worth looking at. */
    record Result(MedianCut palette, Pixels pixels, List<RectMerge.Rect> rects,
                  BufferedImage logicalImage) {
        int rectCount() {
            return rects.size();
        }

        int cellCount() {
            return pixels.w * pixels.h;
        }
    }

    private Convert() {
    }

    static Result run(BufferedImage source, Settings settings) {
        MedianCut palette = MedianCut.build(source, settings.colours());
        Pixels reduced = Pixels.reduce(source, palette, settings.logicalW(), settings.logicalH());
        // Flattening happens before denoising, not after: a flat region has no small components
        // left in it, so the denoise pass then has nothing to do there and spends its whole budget
        // on the case, which is the only part anybody is going to look at.
        Pixels flattened = reduced.flatten(settings.flatten());
        Pixels cleaned = flattened.denoise(palette, settings.noiseMaxArea(),
                settings.keepContrast());
        List<RectMerge.Rect> rects = RectMerge.merge(cleaned);
        return new Result(palette, cleaned, rects, cleaned.toImage(palette));
    }

    /**
     * The rectangles drawn as rectangles, so the decomposition itself can be looked at.
     *
     * <p>Worth its own preview rather than trusting the count alone. The number of rectangles says
     * how expensive the result is; only the picture says <em>where</em> they went, and a run that
     * has quietly shattered a flat panel into hundreds of slivers looks fine by its colours and
     * obviously wrong here.
     */
    static BufferedImage rectPreview(Result result, int scale) {
        int w = result.pixels().w;
        int h = result.pixels().h;
        BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = img.createGraphics();
        int[] entries = result.palette().palette();

        for (RectMerge.Rect r : result.rects()) {
            g.setColor(new java.awt.Color(entries[r.colour()]));
            g.fillRect(r.x0() * scale, r.y0() * scale,
                    (r.x1() - r.x0()) * scale, (r.y1() - r.y0()) * scale);
            g.setColor(new java.awt.Color(0x60FF00FF, true));
            g.drawRect(r.x0() * scale, r.y0() * scale,
                    (r.x1() - r.x0()) * scale - 1, (r.y1() - r.y0()) * scale - 1);
        }
        g.dispose();
        return img;
    }

    /** The logical grid blown up with hard edges, which is the only honest way to show it. */
    static BufferedImage nearest(BufferedImage src, int scale) {
        BufferedImage img = new BufferedImage(src.getWidth() * scale, src.getHeight() * scale,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                img.setRGB(x, y, src.getRGB(x / scale, y / scale));
            }
        }
        return img;
    }
}
