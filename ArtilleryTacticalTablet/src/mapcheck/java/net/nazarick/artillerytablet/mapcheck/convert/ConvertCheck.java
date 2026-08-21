package net.nazarick.artillerytablet.mapcheck.convert;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runs the converter once with no window and writes what it produced, so the pipeline can be
 * checked without a person sitting in front of it.
 *
 * <p><b>Why this exists beside the window.</b> The window is for choosing settings; this is for
 * proving the four stages still do what they claim after somebody edits them. It asserts the one
 * property the whole tool rests on — that the rectangles reproduce the logical grid <em>exactly</em>
 * — which no amount of looking at a preview can establish, because a handful of wrong cells in a
 * million is invisible and still means the generated Java draws something the preview did not show.
 */
public final class ConvertCheck {
    public static void main(String[] args) throws Exception {
        Path out = Path.of(args.length > 1 ? args[1] : "build/mapcheck");
        File image = new File(args.length > 0 ? args[0] : "docs/sample.png");
        if (!image.isFile()) {
            System.out.println("convert: skipped, no reference image at " + image);
            return;
        }

        BufferedImage source = ImageIO.read(image);
        Convert.Settings settings = new Convert.Settings(300, 169, 32, 3, 60,
                new Convert.Flatten(0.120, 0.146, 0.878, 0.813));
        Convert.Result result = Convert.run(source, settings);

        verifyExact(result);

        Files.createDirectories(out);
        ImageIO.write(Convert.nearest(result.logicalImage(), 4),
                "png", out.resolve("convert-logical.png").toFile());
        ImageIO.write(Convert.rectPreview(result, 4),
                "png", out.resolve("convert-rects.png").toFile());

        String java = Emit.source(result.rects(), result.palette(), result.pixels(),
                "net.nazarick.artillerytablet.client.screen", "ConvertedCase",
                image.getName(), Emit.Rounding.FLOOR);
        Files.write(out.resolve("ConvertedCase.java.txt"),
                java.getBytes(StandardCharsets.UTF_8));

        System.out.printf("convert: OK  %d cells -> %d rectangles (%.1f%%), %d colours%n",
                result.cellCount(), result.rectCount(),
                100.0 * result.rectCount() / result.cellCount(), result.palette().size());
        System.out.println("convert: wrote convert-logical.png, convert-rects.png, "
                + "ConvertedCase.java.txt");
    }

    /**
     * Paints the rectangles back onto an empty grid and demands it match the one they came from.
     *
     * <p>Catches the two ways the merge can be wrong and neither shows in a preview: a cell covered
     * twice (the second rectangle wins and the picture is subtly not what was approved) and a cell
     * covered by none (a hole that draws as whatever was behind it).
     */
    private static void verifyExact(Convert.Result result) {
        Pixels pixels = result.pixels();
        int[] painted = new int[pixels.w * pixels.h];
        java.util.Arrays.fill(painted, -1);

        for (RectMerge.Rect r : result.rects()) {
            for (int y = r.y0(); y < r.y1(); y++) {
                for (int x = r.x0(); x < r.x1(); x++) {
                    int at = y * pixels.w + x;
                    if (painted[at] != -1) {
                        throw new AssertionError("rectangles overlap at " + x + "," + y);
                    }
                    painted[at] = r.colour();
                }
            }
        }
        for (int i = 0; i < painted.length; i++) {
            if (painted[i] == -1) {
                throw new AssertionError("cell " + (i % pixels.w) + "," + (i / pixels.w)
                        + " is covered by no rectangle");
            }
            if (painted[i] != pixels.cells[i]) {
                throw new AssertionError("cell " + (i % pixels.w) + "," + (i / pixels.w)
                        + " came back a different colour");
            }
        }
    }

    private ConvertCheck() {
    }
}
