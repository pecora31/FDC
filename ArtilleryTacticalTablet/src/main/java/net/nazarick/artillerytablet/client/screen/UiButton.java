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
    // STUB — the previous bevel/LED/press-animation rendering was removed on request (2026-08-29)
    // so it can be redesigned from scratch. Everything below draws the plainest possible box; the
    // builder API and state fields are untouched so TabletScreen's calls keep compiling.
    private static final int BACKGROUND = 0xB0141A21;
    private static final int STUB_OUTLINE = 0xFF3A4048;
    private static final int STUB_OUTLINE_LIT = 0xFF7C8894;
    private static final int STUB_TEXT = 0xFFE0E4E8;
    private static final int STUB_TEXT_DIM = 0x667C8894;

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
        p.fill(x1, y1, x2, y2, hardOn ? STUB_OUTLINE_LIT : 0xFF222834);
    }

    /**
     * The glyph on a case key that carries a symbol rather than a word: a crosshair for the grid
     * toggle, a sun for brightness, a diamond for the display filter, a ring-and-bar for power.
     *
     * <p>Sized off the cap's own width rather than a scale factor — a case key is coordinate-system
     * agnostic by design (see the class javadoc), so its glyph has to be too.
     */
    private void drawMark(Paint p, int cx, int cy, int w, int color) {
        // Stub — draws the mark's name as a single letter instead of a glyph, until redesigned.
        String letter = switch (mark) {
            case PLUS -> "+";
            case MINUS -> "-";
            case CENTRE -> "C";
            case GRID -> "G";
            case BRIGHT -> "B";
            case NIGHT -> "N";
            case POWER -> "P";
        };
        p.label(letter, cx - 3, cy - 4, 6, 8, color);
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
        render(new GuiPaint(g), px, py, mouseDown);
    }

    public void render(Paint p, double px, double py) {
        render(p, px, py, false);
    }

    public void render(Paint p, double px, double py, boolean mouseDown) {
        // STUB — a single plain box for every button kind (hard, nav, mfd, mark, menuItem, danger,
        // default), no hover fill, no press offset, no LED glow. Redesign from here.
        if (invisible) {
            return;
        }
        boolean lit = active && contains(px, py);

        int textCol = !active ? STUB_TEXT_DIM : STUB_TEXT;
        int outlineCol = !active ? STUB_OUTLINE : lit ? STUB_OUTLINE_LIT : STUB_OUTLINE;

        p.rect(x, y, w, h, BACKGROUND);
        p.outline(x, y, w, h, outlineCol);
        if (led != null) {
            drawLamp(p);
        }
        if (mark != null) {
            drawMark(p, x + w / 2, y + h / 2, w, textCol);
        } else if (sub == null) {
            p.label(TabletTheme.text(label).getString(), x, y, w, h, textCol);
        } else {
            p.label(TabletTheme.text(label).getString(), x, y, w, h / 2, textCol);
            p.label(TabletTheme.text(sub).getString(), x, y + h / 2, w, h / 2, textCol);
        }
    }

}
