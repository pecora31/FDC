package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.init.ModSounds;

/**
 * A key on the tablet, positioned and drawn in device pixels.
 *
 * <p>Deliberately not a Minecraft widget. Those are drawn by the screen's own render pass, outside
 * the scaled matrix the tablet works in, and they hit-test against logical mouse coordinates — so
 * one would have appeared at a quarter of its stated size in the wrong place. Owning the button is
 * what buys the crisp result; it is not incidental complexity.
 */
@OnlyIn(Dist.CLIENT)
public class UiButton {
    /** Almost transparent, so the map stays faintly visible behind a key. */
    private static final int BACKGROUND = 0xB0141A21;

    // Injection-moulded matte PBT key (ash grey) — case-key palette.
    private static final int COL_BTN_DROP_SHADOW = 0x88030304;
    private static final int COL_BTN_BORDER_DARK = 0xFF141518;
    private static final int COL_BTN_WALL_EXTRUSION = 0xFF24262C;
    private static final int COL_BTN_SHOULDER_LIGHT = 0xFF626670;
    private static final int COL_BTN_SHOULDER_HOVER = 0xFF767B86;
    private static final int COL_BTN_SHOULDER_PRESSED = 0xFF1C1E22;
    private static final int COL_BTN_RIM_TOP = 0xFF4A4E56;
    private static final int COL_BTN_RIM_TOP_PRESSED = 0xFF1E2024;
    private static final int COL_BTN_DISH_BASE = 0xFF3D4047;
    private static final int COL_BTN_DISH_HOVER = 0xFF4A4E57;
    private static final int COL_BTN_DISH_PRESSED = 0xFF202227;
    private static final int COL_BTN_DISH_SHADOW = 0xFF26282E;
    private static final int COL_BTN_DISH_SHADOW_PRESSED = 0xFF101114;
    private static final int COL_BTN_DISH_HIGHLIGHT = 0xFF525660;
    private static final int COL_BTN_DISH_HIGHLIGHT_PRESSED = 0xFF2C2F36;
    private static final int COL_BTN_TEXT = 0xFFF2F4F8;
    private static final int COL_BTN_TEXT_PRESSED = 0xFFB0B4BC;

    // Bright tactical crimson key — CFF and POWER.
    private static final int COL_RED_BORDER_DARK = 0xFF2A0606;
    private static final int COL_RED_WALL_EXTRUSION = 0xFF500C0C;
    private static final int COL_RED_SHOULDER_LIGHT = 0xFFB82626;
    private static final int COL_RED_SHOULDER_HOVER = 0xFFD42E2E;
    private static final int COL_RED_SHOULDER_PRESSED = 0xFF500A0A;
    private static final int COL_RED_RIM_TOP = 0xFF8E1B1B;
    private static final int COL_RED_RIM_TOP_PRESSED = 0xFF4A0808;
    private static final int COL_RED_DISH_BASE = 0xFF821818;
    private static final int COL_RED_DISH_HOVER = 0xFFA62222;
    private static final int COL_RED_DISH_PRESSED = 0xFF580E0E;
    private static final int COL_RED_DISH_SHADOW = 0xFF4C0A0A;
    private static final int COL_RED_DISH_SHADOW_PRESSED = 0xFF180303;
    private static final int COL_RED_DISH_HIGHLIGHT = 0xFFA62222;
    private static final int COL_RED_DISH_HIGHLIGHT_PRESSED = 0xFF681414;
    private static final int COL_RED_TEXT = 0xFFFFFFFF;
    private static final int COL_RED_TEXT_PRESSED = 0xFFC0C0C0;

    // Smoked-lens LED, on the bezel beside a key.
    private static final int LED_LIT_GOOD = 0xFF2BE05E;
    private static final int LED_LIT_DANGER = 0xFFFF3333;
    private static final int LED_LIT_POWER = 0xFFFFB020;

    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private final Component label;
    private final Runnable action;

    private Component tooltip;
    private boolean active = true;
    private boolean invisible;
    private boolean nav;
    private boolean selected;
    private boolean danger;
    private boolean mfd;
    private boolean mfdOn;
    private boolean menuItem;
    private Mark mark;

    public UiButton(int x, int y, int w, int h, Component label, Runnable action) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.label = label;
        this.action = action;
    }

    public UiButton tooltip(Component text) {
        this.tooltip = text;
        return this;
    }

    public UiButton active(boolean value) {
        this.active = value;
        return this;
    }

    /** An invisible hit area, for when what is being clicked is already drawn by something else. */
    public UiButton invisible() {
        this.invisible = true;
        return this;
    }

    /** A menu entry rather than a key: no box, a rule under the selected one. */
    public UiButton asNav(boolean isSelected) {
        this.nav = true;
        this.selected = isSelected;
        return this;
    }

    /**
     * Filled red with white lettering, for the one control that cannot be taken back.
     *
     * <p>Every other key here is an outline, because a fire-control display should spend colour on
     * what is true rather than on what is clickable. This is the deliberate exception, and it stays
     * an exception: exactly one key on the device may ask for it.
     */
    /**
     * A bezel key: a short code in a box, lit when whatever it controls is active.
     *
     * <p>The border carries the state and the fill stays the panel's own colour, so a row of these
     * reads as one instrument rather than as a row of coloured chips.
     */
    public UiButton mfd(boolean on) {
        this.mfd = true;
        this.mfdOn = on;
        return this;
    }

    /**
     * The controls that carry a drawn mark rather than a word.
     *
     * <p>The first three are the small keys on the glass, where three letters would not fit. The
     * last three are on the case, and they are there for the opposite reason: the panel this device
     * is drawn after gives those three keys symbols rather than codes, and a symbol on a hard key is
     * how a hand finds it without reading — which is the whole argument for putting a key on a case
     * instead of on the screen.
     */
    public enum Mark { PLUS, MINUS, CENTRE, GRID, BRIGHT, NIGHT, POWER }

    /**
     * Draws a mark instead of a label.
     *
     * <p>Three characters in a box twelve pixels wide left the recentre key touching its own border,
     * and a glyph from the font sits wherever its own metrics put it. Drawing the mark means every
     * one of these keys is centred on the same arithmetic instead of on five different letterforms.
     */
    public UiButton mark(Mark kind) {
        this.mark = kind;
        return this;
    }

    /** A row in a pop-up menu: left-aligned, no box until the pointer is on it. */
    public UiButton asMenuItem() {
        this.menuItem = true;
        return this;
    }

    public UiButton danger() {
        this.danger = true;
        return this;
    }

    public Component tooltip() {
        return tooltip;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    public boolean press(double px, double py) {
        if (!contains(px, py)) {
            return false;
        }
        // The click comes before the refusal, and only for the keys on the case. A moulded key makes
        // its noise when it is pressed whether or not the circuit behind it does anything — that is
        // what a key is — and it is the only feedback left now that those keys no longer dim. On the
        // glass a control that cannot act stays silent, because there is nothing there to move.
        if (hard) {
            click();
        }
        if (!active) {
            return false;
        }
        if (!hard) {
            click();
        }
        action.run();
        return true;
    }

    /**
     * The lamp: dark when the thing behind the key is off, lit when it is on.
     *
     * <p>One straight stroke and nothing added to it — no halo, no bleed onto the case round it. It
     * carried both for a while, on the reasoning that a real lamp is a bright thing behind a
     * diffuser rather than a flat rectangle. True on a photograph and beside the point on a case
     * this plain: the halo was decoration standing in for the one fact worth showing, which is
     * simply whether the stroke is lit, and a plain stroke says that without saying anything else.
     *
     * <p>Dark rather than absent when off.
     */
    private void drawLamp(Paint p) {
        if (led == null) {
            return;
        }
        int litColour = danger ? LED_LIT_DANGER : power ? LED_LIT_POWER : LED_LIT_GOOD;
        boolean isLit = hardOn;

        int x1 = led[0], y1 = led[1], x2 = x1 + led[2], y2 = y1 + led[3];
        int w = led[2], h = led[3];

        // 1. 3D Dark Socket Recess (Hốc kim loại chìm)
        p.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF050608);
        p.fill(x1 - 1, y1 - 1, x2 + 1, y1, 0xFF363A42); // Top rim highlight
        p.fill(x1 - 1, y1 - 1, x1, y2 + 1, 0xFF2A2D35); // Left rim highlight

        if (isLit) {
            // 2. Multi-layer Radial Diffusion Halo (Quầng sáng phốt pho)
            int glowOuter = 0x25000000 | (litColour & 0x00FFFFFF);
            int glowInner = 0x55000000 | (litColour & 0x00FFFFFF);
            p.rect(x1 - 2, y1 - 2, w + 4, h + 4, glowOuter);
            p.rect(x1 - 1, y1 - 1, w + 2, h + 2, glowInner);

            // 3. Vivid Semiconductor Diode Core (Thân bóng LED phát sáng)
            p.fill(x1, y1, x2, y2, litColour);

            // 4. Specular White Hot-Spot (Tâm sáng trắng của bóng bán dẫn)
            if (w >= 4 && h >= 4) {
                int cx = x1 + w / 2;
                int cy = y1 + h / 2;
                p.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFFFFFFFF);
            } else {
                p.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, 0xFFFFFFFF);
            }
        } else {
            // Unlit Smoked Glass Diode Lens (Kính hun khói khi tắt)
            p.fill(x1, y1, x2, y2, 0xFF1A1C22);
            p.fill(x1, y1, x1 + 1, y1 + 1, 0xFF586270); // Top specular glint
            p.fill(x1, y2 - 1, x2, y2, 0xFF0A0C0E);     // Bottom inner shadow
        }
    }

    /**
     * The glyph on a case key that carries a symbol rather than a word: a crosshair for the grid
     * toggle, a sun for brightness, a diamond for the display filter, a ring-and-bar for power.
     *
     * <p>Sized off the cap's own width rather than a scale factor — a case key is coordinate-system
     * agnostic by design (see the class javadoc), so its glyph has to be too.
     */
    private void drawMark(Paint p, int cx, int cy, int w, int color) {
        switch (mark) {
            case GRID -> { // crosshair
                int s = Math.max(4, Math.round(w * (7f / 44)));
                p.fill(cx - s, cy - 1, cx + s + 1, cy + 1, color);
                p.fill(cx - 1, cy - s, cx + 1, cy + s + 1, color);
                fillCircle(p, cx, cy, Math.max(1, Math.round(w * (2.5f / 44))), color);
            }
            case BRIGHT -> { // sun and rays
                int r = Math.max(3, Math.round(w * (4.5f / 44)));
                fillCircle(p, cx, cy, r, color);
                int rayLen = Math.max(3, Math.round(w * (4f / 44)));
                p.fill(cx - r - rayLen, cy - 1, cx + r + rayLen + 1, cy + 1, color);
                p.fill(cx - 1, cy - r - rayLen, cx + 1, cy + r + rayLen + 1, color);
            }
            case NIGHT -> { // diamond outline
                int s = Math.max(4, Math.round(w * (6f / 44)));
                for (int dy = -s; dy <= s; dy++) {
                    for (int dx = -s; dx <= s; dx++) {
                        int m = Math.abs(dx) + Math.abs(dy);
                        if (m <= s && m >= s - 2) {
                            p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                        }
                    }
                }
            }
            case POWER -> { // ring with a bar through the top
                int r = Math.max(6, Math.round(w * (8.5f / 44)));
                int barH = Math.max(5, Math.round(w * (7.5f / 44)));
                p.fill(cx - 1, cy - r - 1, cx + 2, cy - r - 1 + barH, color);

                int ringThick = Math.max(1, Math.round(w * (1.6f / 44)));
                float rIn2 = (float) (r - ringThick) * (r - ringThick);
                float rOut2 = (float) r * r;
                int gapHalfW = Math.round(w * (4f / 44));

                for (int dy = -r; dy <= r; dy++) {
                    for (int dx = -r; dx <= r; dx++) {
                        float d2 = dx * dx + dy * dy;
                        if (d2 <= rOut2 && d2 >= rIn2) {
                            if (dy < -0.2f * r && Math.abs(dx) <= gapHalfW) {
                                continue;
                            }
                            p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                        }
                    }
                }
            }
            default -> { }
        }
    }

    /**
     * A filled rounded rectangle whose curve is antialiased, unlike {@link Paint#rounded}'s hard
     * pixel test.
     *
     * <p>Straight edges stay three big rectangle fills — cheap, and there is nothing to smooth
     * about a straight line. Only the four corner boxes pay for the per-pixel signed-distance
     * coverage test, so this costs about the same number of draw calls as the plain version it
     * replaces; what changes is the handful of corner pixels getting a soft edge instead of a
     * staircase. Doing this at draw time rather than baking it once was the whole point of not
     * porting the prototype's atlas approach — a key still has to look right while it moves.
     */
    private static void fillRoundedAA(Paint p, int x, int y, int w, int h, float radius, int argb) {
        int ir = Math.max(1, Math.round(radius));
        ir = Math.min(ir, Math.min(w, h) / 2);
        if (ir <= 0) {
            p.rect(x, y, w, h, argb);
            return;
        }
        p.rect(x + ir, y, w - ir * 2, h, argb);
        p.rect(x, y + ir, ir, h - ir * 2, argb);
        p.rect(x + w - ir, y + ir, ir, h - ir * 2, argb);
        aaCorner(p, x, y, ir, radius, false, false, argb);
        aaCorner(p, x + w - ir, y, ir, radius, true, false, argb);
        aaCorner(p, x, y + h - ir, ir, radius, false, true, argb);
        aaCorner(p, x + w - ir, y + h - ir, ir, radius, true, true, argb);
    }

    private static void aaCorner(Paint p, int bx, int by, int box, float radius, boolean right, boolean bottom, int argb) {
        float cx = right ? bx : bx + radius;
        float cy = bottom ? by : by + radius;
        int maxAlpha = (argb >>> 24) & 0xFF;
        int rgb = argb & 0x00FFFFFF;
        for (int dy = 0; dy < box; dy++) {
            for (int dx = 0; dx < box; dx++) {
                int px = bx + dx, py = by + dy;
                float ddx = (px + 0.5f) - cx;
                float ddy = (py + 0.5f) - cy;
                float dist = (float) Math.sqrt(ddx * ddx + ddy * ddy) - radius;
                if (dist <= 0.5f) {
                    float cov = Math.max(0f, Math.min(1f, 0.5f - dist));
                    int a = Math.round(cov * maxAlpha);
                    if (a > 0) {
                        p.fill(px, py, px + 1, py + 1, (a << 24) | rgb);
                    }
                }
            }
        }
    }

    private static void fillCircle(Paint p, int cx, int cy, int radius, int color) {
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    /**
     * The noise of the key going down.
     *
     * <p>A tactile switch click, pitched by what was pressed: low for the moulded keys on the case,
     * where the ear expects mass, and near its own pitch for the small controls on the glass. One
     * sound rather than two files, because two would be two things to keep matched and the pitch is
     * the whole of the difference the ear is listening for.
     */
    private void click() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) {
            return;
        }
        mc.getSoundManager().play(SimpleSoundInstance.forUI(
                ModSounds.TACTICAL_KEY_PRESS.get(), hard ? 0.62f : 1.15f, hard ? 2.20f : 0.30f));
    }

    /**
     * Draws this as a key on the case rather than a control on the glass.
     *
     * <p>A moulded cap: a shadow under it, a lit edge along its top and a dark one along its foot.
     * Those three are the whole of what makes a shape read as standing proud of what it sits on, and
     * they matter here because the thing they sit on is drawn to look like a case. A flat square
     * beside a moulded shell reads as a hole in it.
     *
     * <p>Takes the same argument as {@link #mfd(boolean)} and means the same thing by it, so a key
     * can move between the glass and the case without its state changing meaning on the way.
     */
    public UiButton hard(boolean on) {
        this.hard = true;
        this.hardOn = on;
        return this;
    }

    private boolean hard;
    private boolean hardOn;
    private boolean power;
    private Component sub;
    private int[] led;

    /**
     * Gives this key a lamp on the bezel, at a place the case decided.
     *
     * <p>State used to be carried by the cap — a green border when the thing behind the key was on.
     * That was the device arguing with itself: a moulded key is a physical fact and should not
     * change colour to report something, yet something has to report it. A lamp is what real panels
     * use and it settles both halves, because a lamp is a separate part. The cap can now stay
     * exactly what it is under every state, and the only thing that changes is a light beside it.
     */
    public UiButton lamp(int[] rect) {
        this.led = rect;
        return this;
    }

    /**
     * A second line under the label, for a key whose name and whose direction are two things.
     *
     * <p>"BRT+" written along one line reads as a four-character word, and the sign — the only part
     * that differs between the pair — ends up as its last letter, where it is smallest and furthest
     * from the eye's centre. Stacked, the name says what is being changed and the sign says which
     * way, and the two keys differ in the middle of themselves rather than at one edge.
     */
    public UiButton sub(Component text) {
        this.sub = text;
        return this;
    }

    /**
     * The key that ends the session: red, and labelled.
     *
     * <p>It wore a drawn ring for a while, on the reasoning that the symbol needs no translating.
     * True, and beside it sat six keys carrying three-letter codes — so the one key that was not a
     * word became the odd one out rather than the obvious one. Consistency across a column of
     * controls beats recognisability of one of them, and red already says "this is the way out".
     */
    public UiButton power() {
        this.power = true;
        return this;
    }

    public void render(GuiGraphics g, double px, double py, boolean mouseDown) {
        render(new GuiPaint(g), px, py, mouseDown);
    }

    public void render(Paint p, double px, double py) {
        render(p, px, py, false);
    }

    /**
     * The same key, drawn through the seam.
     *
     * <p>Split out so the case and its keys can be drawn outside the game — see {@link Paint}. The
     * game's own path goes through here too rather than beside it, which is the whole point: a
     * preview that runs different code from the game is a picture of an intention.
     */
    public void render(Paint p, double px, double py, boolean mouseDown) {
        if (invisible) {
            return;
        }
        boolean lit = active && contains(px, py);

        if (hard) {
            // A moulded cap: rounded shell, wall extrusion, a dished face with its own bevel, and a
            // real 2px sink into its socket while held — a hand pressing a physical key feels it
            // travel, and a cap that only ever sat flush would be lying about being a key at all.
            boolean red = danger || power;
            boolean isPressed = lit && mouseDown;

            int kx = x, ky = y;
            float r = w * (5.5f / 44f);
            int roundRadius = Math.max(2, Math.round(r));
            int shadowSize = Math.max(1, Math.round(w * (1.5f / 44)));

            if (!isPressed) {
                p.rect(kx + roundRadius, ky + h, w + shadowSize - roundRadius * 2, shadowSize, COL_BTN_DROP_SHADOW);
                p.rect(kx + w, ky + roundRadius, shadowSize, h - roundRadius * 2, COL_BTN_DROP_SHADOW);
            } else {
                int pressOffset = Math.max(1, Math.round(w * (2f / 44)));
                kx += pressOffset;
                ky += pressOffset;
            }

            int borderCol = red ? COL_RED_BORDER_DARK : COL_BTN_BORDER_DARK;
            int wallCol = red ? COL_RED_WALL_EXTRUSION : COL_BTN_WALL_EXTRUSION;
            int shoulderCol = red
                    ? (isPressed ? COL_RED_SHOULDER_PRESSED : lit ? COL_RED_SHOULDER_HOVER : COL_RED_SHOULDER_LIGHT)
                    : (isPressed ? COL_BTN_SHOULDER_PRESSED : lit ? COL_BTN_SHOULDER_HOVER : COL_BTN_SHOULDER_LIGHT);
            int rimTopCol = red
                    ? (isPressed ? COL_RED_RIM_TOP_PRESSED : COL_RED_RIM_TOP)
                    : (isPressed ? COL_BTN_RIM_TOP_PRESSED : COL_BTN_RIM_TOP);

            fillRoundedAA(p, kx, ky, w, h, r, borderCol);

            int wallThickness = Math.max(1, Math.round(w * (1.5f / 44)));
            if (!isPressed) {
                p.rect(kx + roundRadius, ky + h - wallThickness, w - roundRadius * 2, wallThickness, wallCol);
                p.rect(kx + w - wallThickness, ky + roundRadius, wallThickness, h - roundRadius * 2, wallCol);
                p.rect(kx + roundRadius, ky + 1, w - roundRadius * 2, 1, shoulderCol);
                p.rect(kx + 1, ky + roundRadius, 1, h - roundRadius * 2, shoulderCol);
            } else {
                p.rect(kx + roundRadius, ky + 1, w - roundRadius * 2, 2, 0xFF08090B);
                p.rect(kx + 1, ky + roundRadius, 2, h - roundRadius * 2, 0xFF08090B);
            }

            fillRoundedAA(p, kx + 1, ky + 1, w - 2, h - 2, Math.max(1f, r - 1f), rimTopCol);

            int dishBaseCol = red
                    ? (isPressed ? COL_RED_DISH_PRESSED : lit ? COL_RED_DISH_HOVER : COL_RED_DISH_BASE)
                    : (isPressed ? COL_BTN_DISH_PRESSED : lit ? COL_BTN_DISH_HOVER : COL_BTN_DISH_BASE);
            int dishShadow = red
                    ? (isPressed ? COL_RED_DISH_SHADOW_PRESSED : COL_RED_DISH_SHADOW)
                    : (isPressed ? COL_BTN_DISH_SHADOW_PRESSED : COL_BTN_DISH_SHADOW);
            int dishLight = red
                    ? (isPressed ? COL_RED_DISH_HIGHLIGHT_PRESSED : COL_RED_DISH_HIGHLIGHT)
                    : (isPressed ? COL_BTN_DISH_HIGHLIGHT_PRESSED : COL_BTN_DISH_HIGHLIGHT);
            int textCol = red
                    ? (isPressed ? COL_RED_TEXT_PRESSED : COL_RED_TEXT)
                    : (isPressed ? COL_BTN_TEXT_PRESSED : COL_BTN_TEXT);

            int innerMargin = Math.max(1, Math.round(w * (2.5f / 44)));
            int ix = kx + innerMargin, iy = ky + innerMargin;
            int iw = w - innerMargin * 2, ih = h - innerMargin * 2;

            fillRoundedAA(p, ix, iy, iw, ih, Math.max(1f, r - 2f), dishBaseCol);
            int dishRadius = Math.max(1, roundRadius - 2);

            int dishBevel = Math.max(1, Math.round(w * (1.5f / 44)));
            p.rect(ix + dishRadius, iy, iw - dishRadius * 2, dishBevel, dishShadow);
            p.rect(ix, iy + dishRadius, dishBevel, ih - dishRadius * 2, dishShadow);
            p.rect(ix + dishRadius, iy + ih - dishBevel, iw - dishRadius * 2, dishBevel, dishLight);
            p.rect(ix + iw - dishBevel, iy + dishRadius, dishBevel, ih - dishRadius * 2, dishLight);

            drawLamp(p);

            if (mark != null) {
                drawMark(p, kx + w / 2, ky + h / 2, w, textCol);
            } else if (sub == null) {
                p.label(TabletTheme.text(label).getString(), kx, ky, w, h, textCol);
            } else {
                p.label(TabletTheme.text(label).getString(), kx, ky, w, h / 2, textCol);
                p.label(TabletTheme.text(sub).getString(), kx, ky + h / 2, w, h / 2, textCol);
            }
            return;
        }

        if (nav) {
            if (selected) {
                p.rect(x, y + h - 2, w, 2, TabletTheme.FRIENDLY);
            } else if (lit) {
                p.rect(x, y + h - 2, w, 2, TabletTheme.LINE);
            }
            p.label(TabletTheme.text(label).getString(), x, y, w, h,
                    selected || lit ? TabletTheme.TEXT : TabletTheme.MUTED);
            return;
        }

        if (mfd) {
            // White while it is merely available, green once it is the one selected. Border and
            // lettering move together, so the state reads at a glance rather than being decoded from
            // a change of edge alone.
            int ink = !active ? 0x667C8894 : mfdOn || lit ? TabletTheme.GOOD : TabletTheme.TEXT;
            p.rect(x, y, w, h, TabletTheme.OVERLAY);
            p.outline(x, y, w, h, ink);
            p.label(TabletTheme.text(label).getString(), x, y, w, h, ink);
            return;
        }

        if (mark != null) {
            int ink = !active ? 0x667C8894 : lit ? TabletTheme.FRIENDLY : TabletTheme.TEXT;
            p.rect(x, y, w, h, BACKGROUND);
            p.outline(x, y, w, h, lit ? TabletTheme.FRIENDLY : TabletTheme.LINE);

            // Odd-length arms about an odd-length centre, so the mark lands on the box's middle
            // rather than half a pixel off it whatever size the box is.
            int cx = x + (w - 1) / 2;
            int cy = y + (h - 1) / 2;
            switch (mark) {
                case PLUS -> {
                    p.rect(cx - 2, cy, 5, 1, ink);
                    p.rect(cx, cy - 2, 1, 5, ink);
                }
                case MINUS -> p.rect(cx - 2, cy, 5, 1, ink);
                case CENTRE -> {
                    p.rect(cx - 3, cy, 2, 1, ink);
                    p.rect(cx + 2, cy, 2, 1, ink);
                    p.rect(cx, cy - 3, 1, 2, ink);
                    p.rect(cx, cy + 2, 1, 2, ink);
                    p.rect(cx, cy, 1, 1, ink);
                }
            }
            return;
        }

        if (menuItem) {
            if (lit) {
                p.rect(x, y, w, h, 0x334DA3FF);
            }
            p.label(TabletTheme.text(label).getString(), x + Ui.GAP_SM, y, w, h,
                    lit ? TabletTheme.TEXT : TabletTheme.MUTED);
            return;
        }

        if (danger) {
            int fill = !active ? 0x552A333D : lit ? 0xFFFF7A72 : TabletTheme.HOSTILE;
            p.rect(x, y, w, h, fill);
            p.label(TabletTheme.text(label).getString(), x, y, w, h,
                    !active ? 0x667C8894 : 0xFFFFFFFF);
            return;
        }

        // Only the outline answers the cursor. Filling a key on hover made the pointer the brightest
        // thing on a display where colour is meant to carry state.
        p.rect(x, y, w, h, BACKGROUND);
        p.outline(x, y, w, h, !active ? 0x552A333D : lit ? TabletTheme.FRIENDLY : TabletTheme.LINE);
        p.label(TabletTheme.text(label).getString(), x, y, w, h,
                !active ? 0x667C8894 : lit ? TabletTheme.FRIENDLY : TabletTheme.TEXT);
    }

}
