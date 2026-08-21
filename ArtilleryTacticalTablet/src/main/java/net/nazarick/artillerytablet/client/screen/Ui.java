package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Drawing for the tablet, in Minecraft's own interface pixels.
 *
 * <p>An earlier version drew in real device pixels — inside a matrix scaled by the inverse of the
 * GUI scale — to get a packaged TrueType face rendered at its baked size. That worked, and it was
 * abandoned anyway: the result read as a web page pasted into the game. With the game's own bitmap
 * face back, device pixels actively hurt, because that face is drawn for this space and would come
 * out at a third of its size in the other one.
 *
 * <p>So layout is in interface pixels again and the matrix is gone, which takes the mouse conversion
 * and the scissor conversion with it. What survives from that work is the part worth keeping: one
 * place that draws text, one spacing scale, and the tablet's own {@link UiButton} and
 * {@link UiField}. Those replaced Minecraft's widgets to escape the vanilla button texture, and that
 * reason still holds — the device should not look like a dialog box.
 */
@OnlyIn(Dist.CLIENT)
public final class Ui {
    /**
     * Height of a line of text, used for vertical centring.
     *
     * <p>The game reports a line height of nine, which includes room under the baseline for
     * descenders; eight is the height of the glyphs themselves and is what centres a label inside a
     * key. This is the one number that decides whether text sits in the middle of its box — if it
     * looks high or low, nudge this and nothing else.
     */
    public static final int TEXT_HEIGHT = 8;

    /** The spacing scale. Every gap in the tablet is one of these, so nothing is picked by eye. */
    public static final int GAP_XS = 2;
    public static final int GAP_SM = 4;
    public static final int GAP_MD = 6;
    public static final int GAP_LG = 10;

    private Ui() {
    }

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static int width(Component text) {
        return font().width(TabletTheme.text(text));
    }

    public static int width(String text) {
        return font().width(TabletTheme.text(text));
    }

    /** Draws with the text's left edge at x and its optical middle at the centre of a box of h. */
    public static void textIn(GuiGraphics g, Component text, int x, int y, int h, int colour) {
        g.drawString(font(), TabletTheme.text(text), x, y + (h - TEXT_HEIGHT) / 2, colour, false);
    }

    public static void textIn(GuiGraphics g, String text, int x, int y, int h, int colour) {
        textIn(g, Component.literal(text), x, y, h, colour);
    }

    /**
     * Centred both ways inside a box — the one place button labels are positioned — and centred on
     * the ink, not on the advance.
     *
     * <p>A font's width is how far the pen travels, and that includes the one-pixel gap that follows
     * the last glyph — a gap that is never drawn. Centring on it hands that pixel to the left side,
     * so every centred label on the device sat half a pixel off in the same direction. Half a pixel
     * is invisible in a paragraph and plain on a key, where the eye has two short edges to compare
     * it against.
     *
     * <p>Half a pixel is also all it can be corrected to unless the box and the ink agree about
     * being odd or even, which is the standing rule about centring by pixel — see the key size in
     * {@link TabletFrame} for the other half of this fix.
     */
    public static void textCentred(GuiGraphics g, Component text, int x, int y, int w, int h, int colour) {
        int ink = Math.max(0, width(text) - 1);
        textIn(g, text, x + (w - ink) / 2, y, h, colour);
    }

    /**
     * Centred both ways in a box, with the glyphs magnified a whole number of times.
     *
     * <p>Done by scaling the matrix rather than by picking a bigger face, because there is no bigger
     * face — the game ships one, drawn for one size. Scaling it by a whole number keeps every stem
     * on the pixel grid, so it comes out as the same letterforms with thicker strokes rather than as
     * a blurred copy of them.
     *
     * <p>The translation happens before the scale and the text is then drawn about the origin, so
     * the magnification is about the middle of the box. Scaling first and translating after would
     * multiply the offset by the scale as well, which puts a large label off the key it belongs to.
     */
    public static void textCentredScaled(GuiGraphics g, Component text, int x, int y, int w, int h,
                                         int colour, int scale) {
        int ink = Math.max(0, width(text) - 1);
        g.pose().pushPose();
        g.pose().translate(x + w / 2f, y + h / 2f, 0);
        g.pose().scale(scale, scale, 1f);
        g.drawString(font(), TabletTheme.text(text), -ink / 2, -TEXT_HEIGHT / 2, colour, false);
        g.pose().popPose();
    }

    /**
     * How many times over the face fits a label of this height, with the box's width as a ceiling.
     *
     * <p>One place, because two callers picking a magnification each would drift apart the first
     * time either cap changed size — and the width check is the half that is easy to forget: a code
     * three letters long at three times over is wider than the cap that was tall enough for it.
     */
    public static int scaleFor(String text, int boxW, int boxH, double heightShare, int padding) {
        int byHeight = (int) Math.floor(boxH * heightShare / TEXT_HEIGHT);
        int ink = Math.max(1, width(text) - 1);
        int byWidth = (boxW - padding) / ink;
        return Math.max(1, Math.min(byHeight, byWidth));
    }

    /** Right edge of the text against x, vertically centred in h. */
    public static void textRight(GuiGraphics g, Component text, int x, int y, int h, int colour) {
        textIn(g, text, x - width(text), y, h, colour);
    }

    /**
     * Clips to a rectangle.
     *
     * <p>Kept as a wrapper even though it now forwards straight through: this used to convert out of
     * device pixels, and every clip in the tablet already calls it. Taking a width and a height also
     * matches how the rest of the drawing here reads, where Minecraft's own call wants a second
     * corner instead.
     */
    public static void scissor(GuiGraphics g, int x, int y, int w, int h) {
        g.enableScissor(x, y, x + w, y + h);
    }

    /**
     * Draws text broken across as many lines as it needs, and returns the height it used.
     *
     * <p>Panels are narrow and their messages are sentences. Clipping one to the panel edge loses
     * the half that says what to do about it, which is the half worth reading.
     */
    public static int textWrapped(GuiGraphics g, String text, int x, int y, int width, int colour) {
        return textWrapped(g, Component.literal(text), x, y, width, colour);
    }

    public static int textWrapped(GuiGraphics g, Component text, int x, int y, int width, int colour) {
        int line = y;
        for (FormattedCharSequence row : font().split(text, width)) {
            g.drawString(font(), row, x, line, colour, false);
            line += TEXT_HEIGHT + 1;
        }
        return line - y;
    }

    /**
     * A filled triangle, scanned into rows and filled like everything else here.
     *
     * <p>Handing three corners to the graphics card was tried first and drew nothing visible. The
     * state around such a call — shader, blend, where it lands against a batch that has not been
     * flushed — is not something that can be reasoned about from outside a running game, so this
     * takes the path the rest of the interface already proves works.
     *
     * <p>It still turns smoothly: each row's span is computed from the real edges at that height
     * rather than from rectangles rotated into place, so the shape is resolved per row instead of
     * breaking up differently at every angle.
     */
    public static void triangle(GuiGraphics g, double x1, double y1, double x2, double y2,
                                double x3, double y3, int colour) {
        int top = (int) Math.floor(Math.min(y1, Math.min(y2, y3)));
        int bottom = (int) Math.ceil(Math.max(y1, Math.max(y2, y3)));

        for (int y = top; y < bottom; y++) {
            double centre = y + 0.5;
            double left = Double.MAX_VALUE;
            double right = -Double.MAX_VALUE;

            double[][] edges = {{x1, y1, x2, y2}, {x2, y2, x3, y3}, {x3, y3, x1, y1}};
            for (double[] e : edges) {
                double ay = e[1];
                double by = e[3];
                if (centre < Math.min(ay, by) || centre >= Math.max(ay, by) || ay == by) {
                    continue;
                }
                double cross = e[0] + (e[2] - e[0]) * (centre - ay) / (by - ay);
                left = Math.min(left, cross);
                right = Math.max(right, cross);
            }

            if (right > left) {
                int a = (int) Math.round(left);
                int b = (int) Math.round(right);
                g.fill(a, y, Math.max(b, a + 1), y + 1, colour);
            }
        }
    }

    /**
     * Runs drawing made only of filled shapes as one batch instead of one draw call per rectangle.
     *
     * <p>A {@code fill} is not cheap the way it looks. Read in the game's own code: it puts four
     * vertices into the shared buffer and then calls {@code flushIfUnmanaged}, which empties that
     * buffer to the card <em>unless</em> a block like this one is open. Every rectangle is therefore
     * its own upload and its own draw call. That is fine for a dialog box with thirty of them and
     * ruinous for anything plotted point by point — a range ring is seven hundred draw calls and a
     * firing line is one per pixel of its length, so a plotted fire mission was issuing tens of
     * thousands of them per frame, more than every other cost in the map put together.
     *
     * <p><b>Shapes only, and only where no text is mixed in.</b> Text is batched under a render type
     * of its own and leaves the batch after the plain quads however it went in, so a fill submitted
     * after a label inside here would come out on top of it rather than under. Outside a batch the
     * two interleave in submission order, which is what the rest of the tablet is drawn assuming.
     */
    @SuppressWarnings("deprecation") // drawManaged is the only way in; nothing has replaced it.
    public static void batched(GuiGraphics g, Runnable drawing) {
        g.drawManaged(drawing);
    }

    /**
     * How many real pixels the display spends on one of this interface's.
     *
     * <p>Layout is done in interface pixels and magnified by this on the way to the screen, which is
     * why a shape assembled from them comes out in steps: it was drawn on a grid three times coarser
     * than the one it is shown on.
     */
    public static double deviceScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    /**
     * Runs some drawing at the display's own resolution rather than the interface's.
     *
     * <p>Everything inside is positioned in real pixels, so multiply interface coordinates by
     * {@link #deviceScale()} before passing them in. Marks drawn this way have three times the
     * resolution to turn in, which is the difference between a chevron that rotates and one that
     * breaks up. It is not true smoothing, but the edges are hard at the size of a screen pixel
     * rather than at the size of a magnified block.
     *
     * <p>Shapes only. Text must not come through here: the face is a fixed bitmap drawn for the
     * interface grid, and that mismatch is why this project stopped laying out in device pixels.
     */
    public static void inDevicePixels(GuiGraphics g, Runnable drawing) {
        float inverse = (float) (1.0 / deviceScale());
        g.pose().pushPose();
        g.pose().scale(inverse, inverse, 1f);
        drawing.run();
        g.pose().popPose();
    }

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int colour) {
        g.fill(x, y, x + w, y + h, colour);
    }

    /**
     * A filled rectangle with its corners taken off.
     *
     * <p>Row by row, with the inset from a circle rather than from a table of hand-picked steps: a
     * radius is a number that changes, and a table of steps is a second definition of the same curve
     * that stops matching the moment it does.
     *
     * <p>Here rather than in either of its callers because the case and the keys standing on it are
     * meant to look like the same moulding. Written twice they would round differently at some
     * radius nobody tested, and a key whose corner is a pixel squarer than its own case is the sort
     * of wrongness that is felt long before it is seen.
     */
    public static void rounded(GuiGraphics g, int x, int y, int w, int h, int radius, int colour) {
        int r = Math.max(0, Math.min(radius, Math.min(w / 2, h / 2)));
        if (r == 0) {
            g.fill(x, y, x + w, y + h, colour);
            return;
        }
        g.fill(x, y + r, x + w, y + h - r, colour);
        for (int i = 0; i < r; i++) {
            // Half a pixel out from the row's centre, so the curve is measured where the row is
            // rather than at its edge — without it the top row comes out a pixel too wide.
            double dy = r - i - 0.5;
            int inset = r - (int) Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(x + inset, y + i, x + w - inset, y + i + 1, colour);
            g.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, colour);
        }
    }

    /**
     * A rounded rectangle lit from above: one colour at its top edge, another at its foot.
     *
     * <p>The same row loop as {@link #rounded}, and it has to be — a gradient drawn as a separate
     * rectangle laid over a rounded fill cannot reach into the corners, and leaves a seam wherever
     * it stops. A moulded thing is lit across the whole of itself or it does not read as moulded.
     */
    public static void roundedShaded(GuiGraphics g, int x, int y, int w, int h, int radius,
                                     int top, int bottom) {
        int r = Math.max(0, Math.min(radius, Math.min(w / 2, h / 2)));
        for (int row = 0; row < h; row++) {
            int inset = 0;
            if (row < r) {
                double dy = r - row - 0.5;
                inset = r - (int) Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            } else if (row >= h - r) {
                double dy = r - (h - row) + 0.5;
                inset = r - (int) Math.floor(Math.sqrt(Math.max(0, r * r - dy * dy)));
            }
            g.fill(x + inset, y + row, x + w - inset, y + row + 1,
                    blend(top, bottom, row / (float) Math.max(1, h - 1)));
        }
    }

    /** Straight between two packed colours, channel by channel. */
    public static int blend(int a, int b, float t) {
        int out = 0;
        for (int shift = 0; shift <= 24; shift += 8) {
            int ca = (a >> shift) & 0xFF;
            int cb = (b >> shift) & 0xFF;
            out |= (Math.round(ca + (cb - ca) * t) & 0xFF) << shift;
        }
        return out;
    }

    /** A one-pixel frame — thin against the panel behind it, whatever the GUI scale magnifies it to. */
    public static void outline(GuiGraphics g, int x, int y, int w, int h, int colour) {
        g.fill(x, y, x + w, y + 1, colour);
        g.fill(x, y + h - 1, x + w, y + h, colour);
        g.fill(x, y, x + 1, y + h, colour);
        g.fill(x + w - 1, y, x + w, y + h, colour);
    }
}
