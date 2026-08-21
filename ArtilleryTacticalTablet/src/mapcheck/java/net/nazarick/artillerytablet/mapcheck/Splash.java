package net.nazarick.artillerytablet.mapcheck;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;

/**
 * Draws the boot mark to a picture, laid out exactly as the tablet lays it out.
 *
 * <p>Artwork made of characters is the one thing in this project that cannot be judged from the
 * source: a stray space or a row a cell short is invisible in a string literal and obvious the
 * moment it is seen. Nobody can open the tablet from here, so the mark is rendered the way the
 * screen renders it — one cell per character on a fixed pitch, nothing drawn for a space — and
 * written out to be looked at.
 *
 * <p>It also fails the build on the two mistakes that would not survive being looked at either but
 * would waste a round trip to find: a cell that is neither a block nor a space, and a mark taller or
 * wider than the panel it has to sit in.
 */
final class Splash {
    /** A cell in the picture. Nothing to do with the game; this is only so the shape is legible. */
    private static final int CELL = 8;

    private Splash() {
    }

    static void run(File out) throws Exception {
        Class<?> c = Class.forName("net.nazarick.artillerytablet.client.screen.BootSplash");
        Field mark = c.getDeclaredField("MARK");
        mark.setAccessible(true);
        String[] rows = (String[]) mark.get(null);

        // The real colours, not a guess at them. Choosing a colour and then previewing a different
        // one is how a picture comes back saying the choice was fine when it was never tried.
        int ink = MapCheck.readInt(c, "MARK_COLOUR");
        int ground = MapCheck.readInt(c, "BACKDROP");

        check(rows.length > 0, "the mark has rows");

        int widest = 0;
        for (String row : rows) {
            widest = Math.max(widest, row.length());
            for (int i = 0; i < row.length(); i++) {
                char cell = row.charAt(i);
                check(cell == ' ' || cell == '█',
                        "the mark holds only blocks and spaces, found " + Integer.toHexString(cell));
            }
        }

        // The panel this has to fit in is the map area at its smallest, in interface pixels. The
        // mark is laid out one text line per row, and the pitch is the width of a block glyph —
        // eight and six in the game's own face, which is what these numbers are.
        int pitch = 6;
        int line = 8;
        check(widest * pitch <= 300,
                "the mark is " + (widest * pitch) + " interface pixels wide, too wide for a small panel");
        check(rows.length * line <= 80, "the mark is too tall for a small panel");

        BufferedImage image = new BufferedImage(widest * CELL, rows.length * CELL,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, ground & 0xFFFFFF);
            }
        }

        for (int row = 0; row < rows.length; row++) {
            String cells = rows[row];
            for (int cell = 0; cell < cells.length(); cell++) {
                if (cells.charAt(cell) == ' ') {
                    continue;
                }
                for (int y = 0; y < CELL; y++) {
                    for (int x = 0; x < CELL; x++) {
                        image.setRGB(cell * CELL + x, row * CELL + y, ink & 0xFFFFFF);
                    }
                }
            }
        }

        ImageIO.write(image, "png", out);
        System.out.println("splash: OK  wrote " + out.getAbsolutePath()
                + "  (" + widest + " cells by " + rows.length + " rows)");
    }
}
