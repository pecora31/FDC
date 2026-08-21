package net.nazarick.artillerytablet.client.screen;

/**
 * Everything the case and its keys need in order to be drawn — and nothing that needs a graphics
 * card.
 *
 * <p><b>Why this exists.</b> The case was designed by drawing a picture of it by hand, writing code
 * meant to match, and asking someone to launch the game and look. That loop is slow, and worse, it
 * is unsound: the picture and the code are two descriptions of one thing, so they agree only for as
 * long as somebody keeps them agreeing. Several rounds of "not quite like the photograph" were
 * exactly that gap — the sketch was right and the code was something else.
 *
 * <p>With the drawing behind this seam, the same production code can be run outside Minecraft and
 * asked to write an SVG or a PNG. What comes out is not an impression of the case; it is the case,
 * drawn by the code the game will run, which is the only kind of preview worth trusting. It is the
 * rule the map harnesses already work by, applied to the one part of the device they could not
 * reach.
 *
 * <p><b>Why text is a method here rather than a call into {@code Ui}.</b> Lettering needs the game's
 * font, which does not exist outside it. So the caller says <em>put this word in the middle of this
 * box</em> and the implementation decides how: in the game, through the one place that centres text
 * on its ink; in the harness, as a block of the right width. The harness's lettering is therefore a
 * lie, deliberately — what is being checked out here is mouldings and proportion, and a wrong glyph
 * in the preview is cheaper than a font that has to be shipped to draw it.
 */
public interface Paint {
    /** A filled rectangle, from one corner to the other, in packed ARGB. */
    void fill(int x0, int y0, int x1, int y1, int argb);

    /** A word centred in a box, at the face's own size. */
    void label(String text, int x, int y, int w, int h, int argb);

    /**
     * A word centred in a box, magnified.
     *
     * <p>The case sizes its keys to the window and the game's face is eight pixels tall whatever the
     * window is, so on a large one a three-letter code covers a tenth of the cap it is supposed to
     * name. Everything else on the case is a share of the shell; lettering has to be one too, and
     * the only way to make a bitmap face bigger is to magnify it.
     *
     * <p><b>The caller asks for a share of the box, not for a number of times over.</b> Working out
     * how many times a word fits means measuring it, and measuring needs the face — which does not
     * exist outside the game. So the caller says <em>as big as it goes, up to about a third of this
     * box, keeping this much clear at the sides</em>, and each side of the seam answers with what it
     * has: the game measures, the harness estimates. This is the same split the plain label already
     * works by, and getting it wrong is what made the case impossible to preview for one round —
     * the magnification was computed above the seam and took the font with it.
     *
     * <p>Whichever side answers, it answers in <b>whole numbers</b>. The face is a bitmap drawn for
     * one grid; at 2.4 times over its stems land on fractions of a pixel and come out soft, which on
     * a fire-control readout is worse than being small.
     */
    void label(String text, int x, int y, int w, int h, int argb, double heightShare,
               int padding);

    /**
     * Runs drawing made only of fills as one batch where that is possible.
     *
     * <p>Default is to simply run it. Batching is an optimisation the game can make and the harness
     * has no use for, and a seam that forced every implementation to have an opinion about draw
     * calls would be a seam about the wrong thing.
     */
    default void batch(Runnable drawing) {
        drawing.run();
    }

    /** A rectangle given as a corner and a size, which is how most of this code thinks. */
    default void rect(int x, int y, int w, int h, int argb) {
        fill(x, y, x + w, y + h, argb);
    }

    /** A one-pixel frame. */
    default void outline(int x, int y, int w, int h, int argb) {
        fill(x, y, x + w, y + 1, argb);
        fill(x, y + h - 1, x + w, y + h, argb);
        fill(x, y, x + 1, y + h, argb);
        fill(x + w - 1, y, x + w, y + h, argb);
    }

    /**
     * A filled rectangle with its corners taken off.
     *
     * <p>Row by row, with the inset from a circle rather than from a table of hand-picked steps: a
     * radius is a number that changes, and a table of steps is a second definition of the same curve
     * that stops matching the moment it does.
     *
     * <p>On the interface rather than in either caller because the case and the keys standing on it
     * are meant to look like the same moulding. Written twice they would round differently at some
     * radius nobody tested, and a key whose corner is a pixel squarer than its own case is the sort
     * of wrongness that is felt long before it is seen.
     */
    default void rounded(int x, int y, int w, int h, int radius, int argb) {
        int r = Math.max(0, Math.min(radius, Math.min(w / 2, h / 2)));
        if (r == 0) {
            fill(x, y, x + w, y + h, argb);
            return;
        }
        fill(x, y + r, x + w, y + h - r, argb);
        int a = (argb >> 24) & 0xFF;
        int rgb = argb & 0x00FFFFFF;

        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            double exact = Math.sqrt(Math.max(0, r * r - dy * dy));
            int fullInset = (int) Math.floor(exact);
            int inset = r - fullInset;
            double frac = exact - fullInset;

            // Full inner row
            fill(x + inset, y + i, x + w - inset, y + i + 1, argb);
            fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, argb);

            // Subpixel anti-aliasing edge pixel
            if (frac > 0.15 && inset > 0) {
                int aaAlpha = (int) Math.round(a * frac);
                int aaCol = (aaAlpha << 24) | rgb;
                fill(x + inset - 1, y + i, x + inset, y + i + 1, aaCol);
                fill(x + w - inset, y + i, x + w - inset + 1, y + i + 1, aaCol);
                fill(x + inset - 1, y + h - i - 1, x + inset, y + h - i, aaCol);
                fill(x + w - inset, y + h - i - 1, x + w - inset + 1, y + h - i, aaCol);
            }
        }
    }

    /** The same shape lit from above: one colour at its top edge, another at its foot. */
    default void roundedShaded(int x, int y, int w, int h, int radius, int top, int bottom) {
        int r = Math.max(0, Math.min(radius, Math.min(w / 2, h / 2)));
        for (int row = 0; row < h; row++) {
            int inset = 0;
            double frac = 0;
            if (row < r) {
                double dy = r - row - 0.5;
                double exact = Math.sqrt(Math.max(0, r * r - dy * dy));
                int fullInset = (int) Math.floor(exact);
                inset = r - fullInset;
                frac = exact - fullInset;
            } else if (row >= h - r) {
                double dy = r - (h - row) + 0.5;
                double exact = Math.sqrt(Math.max(0, r * r - dy * dy));
                int fullInset = (int) Math.floor(exact);
                inset = r - fullInset;
                frac = exact - fullInset;
            }
            int col = blend(top, bottom, row / (float) Math.max(1, h - 1));
            fill(x + inset, y + row, x + w - inset, y + row + 1, col);

            if (frac > 0.15 && inset > 0) {
                int a = (col >> 24) & 0xFF;
                int aaAlpha = (int) Math.round(a * frac);
                int aaCol = (aaAlpha << 24) | (col & 0x00FFFFFF);
                fill(x + inset - 1, y + row, x + inset, y + row + 1, aaCol);
                fill(x + w - inset, y + row, x + w - inset + 1, y + row + 1, aaCol);
            }
        }
    }

    /** Straight between two packed colours, channel by channel. */
    static int blend(int a, int b, float t) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = (a >> shift) & 0xFF;
            int cb = (b >> shift) & 0xFF;
            out |= (Math.round(ca + (cb - ca) * t) & 0xFF) << shift;
        }
        return out;
    }
}
