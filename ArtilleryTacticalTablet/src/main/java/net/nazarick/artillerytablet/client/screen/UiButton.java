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

    // Injection-moulded matte PBT key (ash grey polymer) — authentic MFD palette matching Image 1.
    private static final int COL_BTN_DROP_SHADOW = 0x88040508;
    private static final int COL_BTN_BORDER_DARK = 0xFF101216;
    private static final int COL_BTN_WALL_EXTRUSION = 0xFF2A2D36;
    private static final int COL_BTN_SHOULDER_LIGHT = 0xFF565C6A;
    private static final int COL_BTN_SHOULDER_HOVER = 0xFF6E7688;
    private static final int COL_BTN_SHOULDER_PRESSED = 0xFF22252C;
    private static final int COL_BTN_RIM_TOP = 0xFF4A4E5C;
    private static final int COL_BTN_RIM_TOP_PRESSED = 0xFF22252C;
    private static final int COL_BTN_DISH_BASE = 0xFF3A3E48;
    private static final int COL_BTN_DISH_HOVER = 0xFF484D5A;
    private static final int COL_BTN_DISH_PRESSED = 0xFF24262E;
    private static final int COL_BTN_DISH_SHADOW = 0xFF22242B;
    private static final int COL_BTN_DISH_SHADOW_PRESSED = 0xFF16181E;
    private static final int COL_BTN_DISH_HIGHLIGHT = 0xFF525868;
    private static final int COL_BTN_DISH_HIGHLIGHT_PRESSED = 0xFF343844;
    private static final int COL_BTN_TEXT = 0xFFFFFFFF;
    private static final int COL_BTN_TEXT_PRESSED = 0xFFB0B4BC;

    // Bright tactical crimson key — CFF and POWER.
    private static final int COL_RED_BORDER_DARK = 0xFF180303;
    private static final int COL_RED_WALL_EXTRUSION = 0xFF440808;
    private static final int COL_RED_SHOULDER_LIGHT = 0xFFC82424;
    private static final int COL_RED_SHOULDER_HOVER = 0xFFE63030;
    private static final int COL_RED_SHOULDER_PRESSED = 0xFF360404;
    private static final int COL_RED_RIM_TOP = 0xFF9E1818;
    private static final int COL_RED_RIM_TOP_PRESSED = 0xFF360404;
    private static final int COL_RED_DISH_BASE = 0xFF7A1212;
    private static final int COL_RED_DISH_HOVER = 0xFF961818;
    private static final int COL_RED_DISH_PRESSED = 0xFF400606;
    private static final int COL_RED_DISH_SHADOW = 0xFF480606;
    private static final int COL_RED_DISH_SHADOW_PRESSED = 0xFF1C0202;
    private static final int COL_RED_DISH_HIGHLIGHT = 0xFFA41E1E;
    private static final int COL_RED_DISH_HIGHLIGHT_PRESSED = 0xFF500808;
    private static final int COL_RED_TEXT = 0xFFFFFFFF;
    private static final int COL_RED_TEXT_PRESSED = 0xFFC0C0C0;

    // Smoked-lens LED, on the bezel beside a key.
    private static final int LED_LIT_GOOD = 0xFF00E65A;
    private static final int LED_LIT_DANGER = 0xFFFF2A2A;
    private static final int LED_LIT_POWER = 0xFFFFB000;

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

    /** The noise of a moulded case key coming back up. Soft controls stay silent, same as press. */
    public void release() {
        if (!hard) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getSoundManager() == null) {
            return;
        }
        mc.getSoundManager().play(SimpleSoundInstance.forUI(
                ModSounds.TACTICAL_KEY_RELEASE.get(), 0.62f, 2.20f));
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
        int x1 = led[0], y1 = led[1], w = led[2], h = led[3];
        int x2 = x1 + w, y2 = y1 + h;

        boolean isLit = hardOn;
        int litColour = danger ? LED_LIT_DANGER : power ? LED_LIT_POWER : LED_LIT_GOOD;

        if (isLit) {
            // 1. Phosphor Bloom Halo (Quầng sáng phốt pho 1px tỏa rộng ra ngoài)
            int glow = 0x55000000 | (litColour & 0x00FFFFFF);
            p.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, glow);

            // 2. Vivid Semiconductor Diode Body
            p.fill(x1, y1, x2, y2, litColour);

            // 3. Specular White Core Dot/Line
            if (h >= 4) {
                p.fill(x1 + w / 2, y1 + 1, x1 + w / 2 + 1, y2 - 1, 0xFFFFFFFF);
            } else if (w >= 4) {
                p.fill(x1 + 1, y1 + h / 2, x2 - 1, y1 + h / 2 + 1, 0xFFFFFFFF);
            } else {
                p.fill(x1, y1, x1 + 1, y1 + 1, 0xFFFFFFFF);
            }
        } else {
            // 1. Thin Dark Recess Socket Border (Viền đen mỏng xung quanh hốc chìm)
            p.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xFF0A0B0E);

            // 2. Smoked optical glass diode body
            p.fill(x1, y1, x2, y2, 0xFF222834);

            // 3. Top glass reflection sheen (1px)
            p.fill(x1, y1, x2, y1 + 1, 0xFF4A5468);

            // 4. Bottom inner shadow (1px)
            p.fill(x1, y2 - 1, x2, y2, 0xFF101318);
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
            case BRIGHT -> { // 8-pointed star / sun
                int s = Math.max(4, Math.round(w * (6.5f / 44)));
                p.fill(cx - s, cy, cx + s + 1, cy + 1, color);
                p.fill(cx, cy - s, cx + 1, cy + s + 1, color);
                int d = Math.max(2, Math.round(s * 0.7f));
                p.fill(cx - d, cy - d, cx - d + 1, cy - d + 1, color);
                p.fill(cx + d, cy - d, cx + d + 1, cy - d + 1, color);
                p.fill(cx - d, cy + d, cx - d + 1, cy + d + 1, color);
                p.fill(cx + d, cy + d, cx + d + 1, cy + d + 1, color);
                fillCircle(p, cx, cy, Math.max(1, Math.round(w * (2f / 44))), color);
            }
            case NIGHT -> { // diamond
                int s = Math.max(4, Math.round(w * (6f / 44)));
                for (int dy = -s; dy <= s; dy++) {
                    int span = s - Math.abs(dy);
                    p.fill(cx - span, cy + dy, cx + span + 1, cy + dy + 1, color);
                }
            }
            case POWER -> { // IEC 60417-5009 standby symbol (slender power bar & ring)
                int radius = Math.max(4, Math.round(w * (6.5f / 44)));
                int ringThick = Math.max(1, Math.round(w * (1.8f / 44)));
                int gapHalfAngleDeg = 28;

                drawAARingArc(p, cx, cy, radius, ringThick, gapHalfAngleDeg, color);

                int barHalfW = (w <= 24) ? 0 : Math.max(0, Math.round(w * (1.0f / 44)) / 2);
                int barTop = cy - radius - Math.max(1, Math.round(w * (1.5f / 44)));
                int barBottom = cy + Math.max(1, Math.round(w * (1.2f / 44)));
                p.fill(cx - barHalfW, barTop, cx + barHalfW + 1, barBottom, color);
            }
            default -> {}
        }
    }

    private static void drawAARingArc(Paint p, int cx, int cy, int radius, int thickness, int gapAngleDeg, int argb) {
        int box = radius + 2;
        int bx = cx - box, by = cy - box;
        int rOut = radius;
        int rIn = radius - thickness;
        int maxAlpha = (argb >>> 24) & 0xFF;
        int rgb = argb & 0x00FFFFFF;

        for (int dy = -box; dy <= box; dy++) {
            for (int dx = -box; dx <= box; dx++) {
                int px = cx + dx, py = cy + dy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist >= rIn - 0.5f && dist <= rOut + 0.5f) {
                    double angleDeg = Math.toDegrees(Math.atan2(dx, -dy));
                    if (Math.abs(angleDeg) > gapAngleDeg) {
                        float cov = 1.0f;
                        if (dist > rOut - 0.5f) {
                            cov = Math.max(0f, Math.min(1f, rOut + 0.5f - dist));
                        } else if (dist < rIn + 0.5f) {
                            cov = Math.max(0f, Math.min(1f, dist - (rIn - 0.5f)));
                        }
                        int a = Math.round(cov * maxAlpha);
                        if (a > 0) {
                            p.fill(px, py, px + 1, py + 1, (a << 24) | rgb);
                        }
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

    public UiButton lamp(int[] rect) {
        this.led = rect;
        return this;
    }

    public UiButton sub(Component text) {
        this.sub = text;
        return this;
    }

    public UiButton power() {
        this.power = true;
        return this;
    }

    public boolean isPressed(double px, double py) {
        return active && contains(px, py);
    }

    public void render(GuiGraphics g, double px, double py, boolean mouseDown) {
        if (invisible) {
            return;
        }
        boolean lit = active && contains(px, py);
        boolean isPressed = lit && mouseDown;

        if (hard && TabletScreen.chassisTextureLocation != null) {
            // 1. Dynamic Lit LED overlay (only blits when LED is turned on)
            if (led != null && hardOn) {
                int colType = danger ? 1 : power ? 2 : 0;
                boolean isVert = led[3] > led[2];
                TabletChassisPaint.blitLed(g, led[0], led[1], led[2], led[3], true, colType, isVert, TabletScreen.chassisTextureLocation);
            }

            // 2. Dynamic Hover / Pressed Keycap overlay (only when interacted with)
            if (lit || isPressed) {
                boolean red = danger || power;
                TabletChassisPaint.blitButton(g, x, y, w, h, red, lit, isPressed, TabletScreen.chassisTextureLocation);

                int textCol = red
                        ? (isPressed ? COL_RED_TEXT_PRESSED : COL_RED_TEXT)
                        : (isPressed ? COL_BTN_TEXT_PRESSED : COL_BTN_TEXT);
                Paint p = new GuiPaint(g);
                int kx = isPressed ? x + Math.max(1, Math.round(w * (2f / 44))) : x;
                int ky = isPressed ? y + Math.max(1, Math.round(w * (2f / 44))) : y;
                if (mark != null) {
                    drawMark(p, kx + w / 2, ky + h / 2, w, textCol);
                } else if (sub == null) {
                    p.label(TabletTheme.text(label).getString(), kx, ky, w, h, textCol);
                } else {
                    p.label(TabletTheme.text(label).getString(), kx, ky, w, h / 2, textCol);
                    p.label(TabletTheme.text(sub).getString(), kx, ky + h / 2, w, h / 2, textCol);
                }
            }
            return;
        }

        render(new GuiPaint(g), px, py, mouseDown);
    }

    public void render(Paint p, double px, double py) {
        render(p, px, py, false);
    }

    public void render(Paint p, double px, double py, boolean mouseDown) {
        if (invisible) {
            return;
        }
        boolean lit = active && contains(px, py);

        if (hard) {
            boolean red = danger || power;
            boolean isPressed = lit && mouseDown;

            int kx = x, ky = y;
            if (!isPressed) {
                p.fill(kx + 1, ky + h, kx + w, ky + h + 1, COL_BTN_DROP_SHADOW);
                p.fill(kx + w, ky + 1, kx + w + 1, ky + h, COL_BTN_DROP_SHADOW);
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

            // 1. Dark Border with clean rounded corner steps (Bo góc mềm mại, chuẩn pixel Minecraft)
            int br = Math.max(1, Math.round(w * (4f / 44f)));
            for (int dy = 0; dy < h; dy++) {
                int inset = (dy < br) ? (br - dy) : (dy >= h - br) ? (dy - (h - br) + 1) : 0;
                p.fill(kx + inset, ky + dy, kx + w - inset, ky + dy + 1, borderCol);
            }

            // 2. Cap Shoulder Body (Matte PBT nhám mịn, không bóng bẩy)
            for (int dy = 1; dy < h - 1; dy++) {
                int inset = (dy < br) ? Math.max(1, br - dy) : (dy >= h - br) ? Math.max(1, dy - (h - br) + 1) : 1;
                int col = (dy <= 2) ? shoulderCol : wallCol;
                p.fill(kx + inset, ky + dy, kx + w - inset, ky + dy + 1, col);
            }

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

            // 3. Concave Dish Bowl (Lòng chảo 3D lõm bo gọn)
            int innerMargin = Math.max(2, Math.round(w * (3.5f / 44f)));
            int ix = kx + innerMargin, iy = ky + innerMargin;
            int iw = w - innerMargin * 2, ih = h - innerMargin * 2;

            p.fill(ix, iy, ix + iw, iy + 1, dishShadow);
            p.fill(ix, iy + 1, ix + 1, iy + ih - 1, dishShadow);
            p.fill(ix + iw - 1, iy + 1, ix + iw, iy + ih - 1, dishLight);
            p.fill(ix, iy + ih - 1, ix + iw, iy + ih, dishLight);

            // Dish Floor
            p.fill(ix + 1, iy + 1, ix + iw - 1, iy + ih - 1, dishBaseCol);

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
