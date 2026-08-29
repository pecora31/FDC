package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A key on the tablet, positioned in device pixels.
 *
 * <p>DEAD STUB (2026-08-29) — on request, drawing, click feedback, and action dispatch were all
 * removed so Gemini can rebuild the whole thing from scratch. Only position/size and the builder
 * API survive, so {@link TabletScreen}'s calls keep compiling — every key is currently invisible
 * and does nothing when clicked. Buttons will not fire, lay, or toggle anything in-game until the
 * render/press logic below is rebuilt.
 */
@OnlyIn(Dist.CLIENT)
public class UiButton {
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
    private boolean hard;
    private boolean hardOn;
    private boolean power;
    private Component sub;
    private int[] led;

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

    public UiButton invisible() {
        this.invisible = true;
        return this;
    }

    public UiButton asNav(boolean isSelected) {
        this.nav = true;
        this.selected = isSelected;
        return this;
    }

    public UiButton mfd(boolean on) {
        this.mfd = true;
        this.mfdOn = on;
        return this;
    }

    public enum Mark { PLUS, MINUS, CENTRE, GRID, BRIGHT, NIGHT, POWER }

    public UiButton mark(Mark kind) {
        this.mark = kind;
        return this;
    }

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

    /** Dead — always refuses. No sound, no action dispatch. */
    public boolean press(double px, double py) {
        return false;
    }

    /** Dead — no sound. */
    public void release() {
    }

    public UiButton hard(boolean on) {
        this.hard = true;
        this.hardOn = on;
        return this;
    }

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

    /** Dead — draws nothing. */
    public void render(GuiGraphics g, double px, double py, boolean mouseDown) {
    }

    /** Dead — draws nothing. */
    public void render(Paint p, double px, double py) {
    }

    /** Dead — draws nothing. */
    public void render(Paint p, double px, double py, boolean mouseDown) {
    }

}
