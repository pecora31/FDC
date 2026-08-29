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

    public enum Mark { PLUS, MINUS, CENTRE, GRID, BRIGHT, POWER }

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

    public boolean press(double px, double py) {
        if (!contains(px, py)) {
            return false;
        }
        if (hard) {
            click();
        }
        if (!active) {
            return false;
        }
        if (!hard) {
            click();
        }
        if (action != null) {
            action.run();
        }
        return true;
    }

    public void release() {
        if (!hard) {
            return;
        }
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc != null && mc.getSoundManager() != null) {
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.nazarick.artillerytablet.init.ModSounds.TACTICAL_KEY_RELEASE.get(), 0.62f, 2.20f));
        }
    }

    private void click() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc != null && mc.getSoundManager() != null) {
            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                    net.nazarick.artillerytablet.init.ModSounds.TACTICAL_KEY_PRESS.get(), hard ? 0.62f : 1.15f, hard ? 2.20f : 0.30f));
        }
    }

    private void drawMark(Paint p, int cx, int cy, int w, int color) {
        switch (mark) {
            case PLUS -> {
                p.fill(cx - 6, cy, cx + 7, cy + 2, color);
                p.fill(cx, cy - 6, cx + 2, cy + 7, color);
            }
            case MINUS -> {
                p.fill(cx - 6, cy, cx + 7, cy + 2, color);
            }
            case CENTRE -> {
                p.fill(cx - 5, cy, cx + 6, cy + 2, color);
                p.fill(cx, cy - 5, cx + 2, cy + 6, color);
                p.outline(cx - 4, cy - 4, 9, 9, color);
            }
            case GRID -> {
                p.fill(cx - 7, cy, cx + 8, cy + 1, color);
                p.fill(cx, cy - 7, cx + 1, cy + 8, color);
                p.fill(cx - 2, cy - 2, cx + 3, cy + 3, color);
                p.fill(cx, cy, cx + 1, cy + 1, 0xFF363A42);
            }
            case BRIGHT -> {
                // Bold High-Definition Tactical Sun icon
                p.fill(cx - 3, cy - 3, cx + 4, cy + 4, color); // Center sun disc
                p.fill(cx - 1, cy - 8, cx + 1, cy - 4, color);  // Top ray
                p.fill(cx - 1, cy + 5, cx + 1, cy + 9, color);  // Bottom ray
                p.fill(cx - 8, cy - 1, cx - 4, cy + 1, color);  // Left ray
                p.fill(cx + 5, cy - 1, cx + 9, cy + 1, color);  // Right ray
                // Diagonal rays
                for (int d = 4; d <= 6; d++) {
                    p.fill(cx - d, cy - d, cx - d + 2, cy - d + 2, color);
                    p.fill(cx + d - 1, cy - d, cx + d + 1, cy - d + 2, color);
                    p.fill(cx - d, cy + d - 1, cx - d + 2, cy + d + 1, color);
                    p.fill(cx + d - 1, cy + d - 1, cx + d + 1, cy + d + 1, color);
                }
            }
            case POWER -> {
                // Bold High-Definition IEC Power icon
                int radius = 8;
                int rIn2 = 5 * 5;
                int rOut2 = 8 * 8;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= rOut2 && d2 >= rIn2) {
                            if (dy < -2 && Math.abs(dx) <= 3) continue;
                            p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                        }
                    }
                }
                p.fill(cx - 1, cy - 9, cx + 1, cy + 1, color);
            }
        }
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
