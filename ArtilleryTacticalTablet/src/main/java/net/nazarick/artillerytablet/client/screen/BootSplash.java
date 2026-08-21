package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * What the map shows while it has nothing to show yet.
 *
 * <p>The first time the tablet is opened in a world there is a second or two where the ground has
 * been asked for and has not arrived, and an empty grid says nothing about whether the device is
 * working. A named screen says the instrument has started and is doing something, which is the
 * difference between waiting and wondering.
 *
 * <p>Drawn with characters rather than a texture, deliberately for now — the mark is going to be
 * designed properly later, and a placeholder that is obviously a placeholder is better than one
 * that quietly becomes the final thing.
 *
 * <p><b>Why every character is placed by hand.</b> The game's face is not fixed-pitch: a space is
 * narrower than a block, and every proportional font makes the two disagree. Drawn as whole lines
 * this artwork would shear apart row by row, in a way that reads as a rendering bug rather than as
 * the wrong tool. So the pitch is measured once from the block glyph itself and every cell is drawn
 * on that grid — which is what a terminal does, and the reason terminal artwork holds its shape.
 */
@OnlyIn(Dist.CLIENT)
final class BootSplash {
    /** The mark, one string per row of cells. Ragged ends are fine; nothing is drawn for a space. */
    private static final String[] MARK = {
            " █████  ████████ ██       █████   ██████",
            "██   ██    ██    ██      ██   ██ ██     ",
            "███████    ██    ██      ███████  █████ ",
            "██   ██    ██    ██      ██   ██      ██",
            "██   ██    ██    ███████ ██   ██ ██████ ",
    };

    /** The cell the pitch is measured from. Any of the artwork's solid cells would do. */
    private static final String PITCH_GLYPH = "█";

    private static final String SUBTITLE = "Fire Direction Center";

    /** Rows of the mark, in text lines, so the block sits on the interface's own line spacing. */
    private static final int ROW_STEP = Ui.TEXT_HEIGHT;

    /**
     * Air between the three things on this screen, and the same amount between each pair.
     *
     * <p>One number rather than one per gap. The mark, the words and the waiting line are a single
     * stack, and a stack whose gaps differ reads as three things that happen to be near each other.
     * Changing the rhythm here moves all of it together, which is the only way it stays a rhythm.
     */
    private static final int GAP = Ui.TEXT_HEIGHT * 2;

    /** How long one turn of the waiting line takes. */
    private static final long SWEEP_MS = 1400L;

    /** Cells in the waiting line under the subtitle. */
    private static final int SWEEP_CELLS = 40;

    /** The panel's own ground colour, laid over the map at nearly full strength. */
    private static final int BACKDROP = 0xEE0C1015;

    /**
     * The mark's colour.
     *
     * <p>Navy, not the interface's accent. The accent means something everywhere else on this device
     * — it is the colour of a thing you can act on — and a maker's mark is not one of those. Lifted
     * off true navy far enough to read against a nearly black panel while still being unmistakably
     * the same colour; sat at 0x000080 it is a shape you can find rather than one you can see.
     */
    /** The device's own navy. Package-visible because the case wears the same colour as the mark. */
    static final int MARK_COLOUR = 0xFF1B3F8F;

    private BootSplash() {
    }

    /**
     * Draws the mark centred in a rectangle.
     *
     * <p>Silently does nothing when the rectangle is too small to hold it. A device that crams its
     * own name over the map at a small window size would be worse than one that says nothing.
     */
    static void draw(GuiGraphics g, int x, int y, int width, int height) {
        // Nearly opaque, not entirely. The map is arriving underneath and being able to half-see it
        // do so is the honest version of a loading screen — it says what is being waited for, rather
        // than hiding it behind a claim that something is happening.
        Ui.rect(g, x, y, width, height, BACKDROP);

        int pitch = Math.max(1, TabletTheme.width(Ui.font(), PITCH_GLYPH));
        int markWidth = widestRow() * pitch;
        int markHeight = MARK.length * ROW_STEP;
        int subtitleWidth = TabletTheme.width(Ui.font(), SUBTITLE);
        int whole = markHeight + GAP + Ui.TEXT_HEIGHT + GAP + Ui.TEXT_HEIGHT;

        if (Math.max(markWidth, subtitleWidth) > width || whole > height) {
            return;
        }

        int left = x + (width - markWidth) / 2;
        int top = y + (height - whole) / 2;

        for (int row = 0; row < MARK.length; row++) {
            String cells = MARK[row];
            int lineY = top + row * ROW_STEP;
            for (int cell = 0; cell < cells.length(); cell++) {
                if (cells.charAt(cell) == ' ') {
                    continue;
                }
                TabletTheme.draw(g, Ui.font(), String.valueOf(cells.charAt(cell)),
                        left + cell * pitch, lineY, MARK_COLOUR, false);
            }
        }

        int under = top + markHeight + GAP;
        TabletTheme.draw(g, Ui.font(), SUBTITLE,
                x + (width - subtitleWidth) / 2, under, TabletScreen.colourText(), false);

        drawSweep(g, x, under + Ui.TEXT_HEIGHT + GAP, width, pitch);
    }

    /**
     * A cell travelling along a line, to say the device is working rather than stopped.
     *
     * <p>Not a proportion of anything, on purpose. The map does not know how much ground it is about
     * to be told about — the server answers a batch at a time and there is no total to be a fraction
     * of — and a bar that claims to know would be inventing a number. This claims only that time is
     * passing and the device is still asking.
     */
    private static void drawSweep(GuiGraphics g, int x, int y, int width, int pitch) {
        int lineWidth = SWEEP_CELLS * pitch;
        if (lineWidth > width) {
            return;
        }
        int left = x + (width - lineWidth) / 2;
        int at = (int) ((System.currentTimeMillis() % SWEEP_MS) * SWEEP_CELLS / SWEEP_MS);

        for (int cell = 0; cell < SWEEP_CELLS; cell++) {
            int distance = Math.abs(cell - at);
            String glyph = distance == 0 ? "█" : distance <= 2 ? "▓" : distance <= 4 ? "░" : "·";
            TabletTheme.draw(g, Ui.font(), glyph, left + cell * pitch, y,
                    distance <= 4 ? TabletScreen.colourAccent() : TabletScreen.colourMuted(), false);
        }
    }

    private static int widestRow() {
        int widest = 0;
        for (String row : MARK) {
            widest = Math.max(widest, row.length());
        }
        return widest;
    }
}
