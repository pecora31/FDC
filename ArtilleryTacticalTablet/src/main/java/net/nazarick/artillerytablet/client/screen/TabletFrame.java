package net.nazarick.artillerytablet.client.screen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The case: where everything on it is, and how it is drawn.
 *
 * <p>Fixed design canvas, one uniform scale. Every position below is a coordinate on a 980x630
 * reference layout (the exact grid the pixel-art chassis in {@link TabletChassisPaint} was drawn
 * to), converted to window pixels by {@link #toScreenX}/{@link #toScreenY} — a single {@code scale}
 * shared by both axes, so the case is never stretched, only grown or shrunk uniformly and centred
 * in whatever the window gives it.
 */
@OnlyIn(Dist.CLIENT)
final class TabletFrame {
    /** The reference layout every position below is a coordinate on. */
    static final int DESIGN_W = 980;
    static final int DESIGN_H = 630;

    /** The 16:9 screen well, in design space. */
    static final int SCR_X = 90;
    static final int SCR_Y = 90;
    static final int SCR_W = 800;
    static final int SCR_H = 450;

    /** Every key on the case, one standard size, square. */
    private static final int KEY_SIZE = 44;

    /** Top/bottom row: first key's x, the step to the next, and the row's y. */
    private static final int ROW_FIRST_X = 148;
    private static final int ROW_STEP = 76;
    private static final int ROW_TOP_Y = 41;
    private static final int ROW_BOTTOM_Y = 589;

    /** Left/right column: first key's y, the step down, and each flank's x. */
    private static final int COL_FIRST_Y = 155;
    private static final int COL_STEP = 64;
    private static final int COL_LEFT_X = 39;
    private static final int COL_RIGHT_X = 941;

    /** Keys down each flank. */
    static final int KEYS = 6;

    /** Keys along the top and the bottom. */
    static final int ROW_KEYS = 10;

    /** The LED beside a key: a short stroke in the clear band between the cap and the glass. */
    private static final int LED_LONG = 8;
    private static final int LED_SHORT = 4;
    private static final int LED_ROW_TOP_Y = 80;
    private static final int LED_ROW_BOTTOM_Y = 546;
    private static final int LED_COL_LEFT_X = 80;
    private static final int LED_COL_RIGHT_X = 896;

    /** How much of the window the case takes, centred in what is left. */
    private static final float SHELL_FRACTION = 0.95f;

    /** The smallest screen worth framing. */
    private static final int MIN_SCREEN_WIDTH = 360;
    private static final int MIN_SCREEN_HEIGHT = 200;

    private final int leftPos;
    private final int topPos;
    private final float scale;
    private final boolean present;

    private TabletFrame(int leftPos, int topPos, float scale, boolean present) {
        this.leftPos = leftPos;
        this.topPos = topPos;
        this.scale = scale;
        this.present = present;
    }

    int toScreenX(int designX) {
        return leftPos + Math.round(designX * scale);
    }

    int toScreenY(int designY) {
        return topPos + Math.round(designY * scale);
    }

    int toScreenW(int designW) {
        return toScreenW((float) designW);
    }

    int toScreenH(int designH) {
        return toScreenH((float) designH);
    }

    /** Precise sibling of {@link #toScreenW(int)}, for chassis art authored with fractional radii. */
    int toScreenW(float designW) {
        return Math.round(designW * scale);
    }

    int toScreenH(float designH) {
        return Math.round(designH * scale);
    }

    int keySize() {
        return toScreenW(KEY_SIZE);
    }

    int keyW() {
        return keySize();
    }

    int keyH() {
        return keySize();
    }

    int rowKeyW() {
        return keySize();
    }

    /** No wider than any other cap now — the new layout has no bookend keys. Kept as its own
     * method because callers still ask for "wide or not". */
    int rowKeyWide() {
        return keySize();
    }

    int rowKeyH() {
        return keySize();
    }

    int rowKeyBottomH() {
        return keySize();
    }

    /**
     * Fits a case to the window, or decides there is no room for one.
     *
     * <p>When there is no room the frame is still returned rather than left null, holding the
     * screen it would have held and reporting itself absent.
     */
    static TabletFrame fit(int windowWidth, int windowHeight) {
        float scale = Math.min((float) windowWidth / DESIGN_W, (float) windowHeight / DESIGN_H)
                * SHELL_FRACTION;
        scale = Math.max(scale, 0.01f);
        int leftPos = Math.round((windowWidth - DESIGN_W * scale) / 2f);
        int topPos = Math.round((windowHeight - DESIGN_H * scale) / 2f);

        TabletFrame candidate = new TabletFrame(leftPos, topPos, scale, true);
        boolean room = candidate.screenWidth() >= MIN_SCREEN_WIDTH
                && candidate.screenHeight() >= MIN_SCREEN_HEIGHT;
        return room ? candidate : new TabletFrame(leftPos, topPos, scale, false);
    }

    boolean present() {
        return present;
    }

    int screenLeft() {
        return toScreenX(SCR_X);
    }

    int screenTop() {
        return toScreenY(SCR_Y);
    }

    int screenWidth() {
        return toScreenW(SCR_W);
    }

    int screenHeight() {
        return toScreenH(SCR_H);
    }

    /** The x of a key bank. Left when {@code right} is false. */
    int keyX(boolean right) {
        return toScreenX(right ? COL_RIGHT_X - KEY_SIZE / 2 : COL_LEFT_X - KEY_SIZE / 2);
    }

    /** The y of the nth key down a flank, counting from nought. */
    int keyY(int index) {
        return toScreenY(COL_FIRST_Y - KEY_SIZE / 2 + index * COL_STEP);
    }

    int[] rowKey(boolean bottom, int index) {
        return rowKey(bottom, index, false);
    }

    /** Where the nth key of a row sits, and how wide it is if it is one of the two bookends. */
    int[] rowKey(boolean bottom, int index, boolean wide) {
        int w = wide ? rowKeyWide() : rowKeyW();
        int centreX = toScreenX(ROW_FIRST_X + index * ROW_STEP);
        int y = toScreenY((bottom ? ROW_BOTTOM_Y : ROW_TOP_Y) - KEY_SIZE / 2);
        return new int[]{centreX - w / 2, y};
    }

    int[] ledFor(int edge, int index) {
        return ledFor(edge, index, false);
    }

    /** The lamp for a key: a horizontal pill on rows, vertical pill on flanks. */
    int[] ledFor(int edge, int index, boolean wide) {
        switch (edge) {
            case EDGE_TOP: {
                int cx = ROW_FIRST_X + index * ROW_STEP;
                return rect(cx - LED_LONG / 2, LED_ROW_TOP_Y, LED_LONG, LED_SHORT);
            }
            case EDGE_BOTTOM: {
                int cx = ROW_FIRST_X + index * ROW_STEP;
                return rect(cx - LED_LONG / 2, LED_ROW_BOTTOM_Y, LED_LONG, LED_SHORT);
            }
            case EDGE_LEFT: {
                int cy = COL_FIRST_Y + index * COL_STEP;
                return rect(LED_COL_LEFT_X, cy - LED_LONG / 2, LED_SHORT, LED_LONG);
            }
            default: {
                int cy = COL_FIRST_Y + index * COL_STEP;
                return rect(LED_COL_RIGHT_X, cy - LED_LONG / 2, LED_SHORT, LED_LONG);
            }
        }
    }

    private int[] rect(int designX, int designY, int designW, int designH) {
        return new int[]{toScreenX(designX), toScreenY(designY), toScreenW(designW), toScreenH(designH)};
    }

    static final int EDGE_TOP = 0;
    static final int EDGE_BOTTOM = 1;
    static final int EDGE_LEFT = 2;
    static final int EDGE_RIGHT = 3;

    /** Neutral gunmetal fallback shell, drawn only for a {@link Paint} that isn't the real game. */
    private static final int FACE = 0xFF222224;
    private static final int WELL = 0xFF000000;

    /**
     * Paints a plain fallback shell — a rounded rectangle and a recessed well, nothing more.
     *
     * <p>The real game path does not call this for the chassis any more: the detailed pixel-art
     * chassis is baked once into a texture (see {@link TabletChassisPaint#bake()}) and blitted by
     * {@link TabletScreen}, since redrawing hundreds of lines of chamfer and collar geometry every
     * frame is real cost for a shape that never changes once the window stops resizing. This method
     * stays for whatever draws the case through the {@link Paint} seam instead of a texture — a
     * headless preview harness, should one ever call it.
     */
    void draw(Paint p) {
        if (!present) {
            return;
        }
        int x = toScreenX(0);
        int y = toScreenY(0);
        int w = toScreenW(DESIGN_W);
        int h = toScreenH(DESIGN_H);
        int radius = toScreenW(18);
        p.batch(() -> {
            p.rounded(x, y, w, h, radius, FACE);
            int sx = screenLeft();
            int sy = screenTop();
            int sw = screenWidth();
            int sh = screenHeight();
            int bezel = toScreenW(8);
            p.rounded(sx - bezel, sy - bezel, sw + bezel * 2, sh + bezel * 2,
                    Math.max(2, radius / 2), WELL);
        });
    }
}
