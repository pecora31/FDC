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

    public void render(GuiGraphics g, double px, double py, boolean mouseDown) {
        render(new GuiPaint(g), px, py, mouseDown);
    }

    public void render(Paint p, double px, double py) {
        render(p, px, py, false);
    }

    public void render(Paint p, double px, double py, boolean mouseDown) {
        if (invisible) {
            return;
        }

        boolean hovered = active && contains(px, py);
        boolean pressed = hovered && mouseDown;
        int dy = pressed ? 1 : 0;
        int cx = x + w / 2;
        int cy = y + h / 2;

        if (hard) {
            // =========================================================================
            // 1. HARD PHYSICAL CHASSIS KEYS (32 Keys on Bezel)
            // =========================================================================
            // Background case is already rendered by TabletChassisPaint.
            // Here we render interactive feedback (pressed depression, hover glow, active LED lamp)

            if (pressed) {
                // Sunken pressed keycap face depression
                int rim = 4;
                int dishX = x + rim;
                int dishY = y + rim + 1;
                int dishW = w - rim * 2;
                int dishH = h - rim * 2;

                int floorCol = danger ? 0xFF7A1414 : 0xFF2A2E36;
                p.rect(dishX, dishY, dishW, dishH, floorCol);

                // Deep inner drop shadow on top & left
                int shadowCol = danger ? 0xFF4A0808 : 0xFF14161A;
                p.fill(dishX, dishY, dishX + dishW, dishY + 2, shadowCol);
                p.fill(dishX, dishY, dishX + 2, dishY + dishH, shadowCol);

                // Dim highlight on bottom & right
                int hiCol = danger ? 0xFF9E1E1E : 0xFF3E4450;
                p.fill(dishX, dishY + dishH - 1, dishX + dishW, dishY + dishH, hiCol);
                p.fill(dishX + dishW - 1, dishY, dishX + dishW, dishY + dishH, hiCol);
            } else if (hovered) {
                // Subtle tactical ambient hover sheen around raised lip
                int sheen = danger ? 0x40FF6666 : 0x30FFFFFF;
                p.rect(x + 2, y + 2, w - 4, h - 4, sheen);
            }

            // Render glowing optical LED if active
            if (led != null && led.length >= 4 && hardOn) {
                int lx = led[0], ly = led[1], lw = led[2], lh = led[3];
                int ledCol = danger ? 0xFFFF2222 : 0xFF00FF66;

                // Outer phosphor glow halos
                p.rect(lx - 2, ly - 2, lw + 4, lh + 4, (ledCol & 0x00FFFFFF) | 0x22000000);
                p.rect(lx - 1, ly - 1, lw + 2, lh + 2, (ledCol & 0x00FFFFFF) | 0x55000000);

                // High saturation luminous light-pipe body
                p.rect(lx, ly, lw, lh, ledCol);

                // Axial optical core filament (white hot center)
                if (lh > lw) {
                    p.fill(lx + lw / 2 - 1, ly + 1, lx + lw / 2 + 1, ly + lh - 1, 0xFFFFFFFF);
                } else {
                    p.fill(lx + 1, ly + lh / 2 - 1, lx + lw - 1, ly + lh / 2 + 1, 0xFFFFFFFF);
                }
            }

            // Text / Mark on Hard Keycap
            int textCol = active ? (danger ? 0xFFFFFFFF : 0xFFF0F4FA) : 0xFF6C7684;
            if (mark != null) {
                drawMark(p, cx, cy + dy, w, textCol);
            } else if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x, y + dy, w, h, textCol);
            }
            if (sub != null && !sub.getString().isEmpty()) {
                p.label(sub.getString(), x, y + h - 10 + dy, w, 8, 0xFF8E99A8);
            }

        } else if (mfd) {
            // =========================================================================
            // 2. MFD SOFTKEYS (On-Screen Glass Edge Buttons)
            // =========================================================================
            int bgCol = pressed ? 0xEE1E242E : (hovered ? 0xDD252D3A : 0xAA12161E);
            p.rect(x, y, w, h, bgCol);

            int borderCol = mfdOn ? 0xFF5FD08A : (hovered ? 0xFF4DA3FF : 0xFF2A333D);
            p.outline(x, y, w, h, borderCol);

            // On-screen indicator pip
            int pipSize = 4;
            int pipX = x + 3;
            int pipY = cy - pipSize / 2;
            p.rect(pipX, pipY, pipSize, pipSize, mfdOn ? 0xFF5FD08A : 0xFF1C242E);

            int textCol = mfdOn ? 0xFFFFFFFF : (hovered ? 0xFFE0E8F2 : 0xFFA0ACBA);
            if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x + 4, y + dy, w - 4, h, textCol);
            }

        } else if (menuItem) {
            // =========================================================================
            // 3. MENU LIST ITEM BUTTONS
            // =========================================================================
            int bgCol = pressed ? 0xDD252C38 : (hovered ? 0xBB1E2430 : (selected ? 0x9918202A : 0x44141820));
            p.rect(x, y, w, h, bgCol);

            if (selected) {
                // Left accent indicator bar
                int barCol = danger ? 0xFFFF5A52 : 0xFF4DA3FF;
                p.fill(x, y, x + 3, y + h, barCol);
            }
            p.fill(x, y + h - 1, x + w, y + h, 0x402A333D);

            int textCol = selected ? 0xFFFFFFFF : (hovered ? 0xFFE8EEF5 : (active ? 0xFFB8C2CE : 0xFF5A6472));
            if (mark != null) {
                drawMark(p, x + 12, cy + dy, w, textCol);
            }
            if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x + (selected ? 6 : 4), y + dy, w, h, textCol);
            }

        } else if (nav) {
            // =========================================================================
            // 4. NAVIGATION TAB BUTTONS
            // =========================================================================
            int bgCol = selected ? 0xDD1E2632 : (hovered ? 0xAA18202A : 0x5512161E);
            p.rect(x, y, w, h, bgCol);
            p.outline(x, y, w, h, 0xFF2A333D);

            if (selected || hovered) {
                int barCol = selected ? 0xFF4DA3FF : 0x884DA3FF;
                p.fill(x, y + h - 2, x + w, y + h, barCol);
            }

            int textCol = selected ? 0xFFFFFFFF : (hovered ? 0xFFD0DAE6 : 0xFF8E9AA8);
            if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x, y + dy, w, h, textCol);
            }

        } else {
            // =========================================================================
            // 5. STANDARD IN-SCREEN ACTION & MAP BUTTONS (+, -, Center, Danger, Dialogs)
            // =========================================================================
            int bgCol;
            int borderCol;
            if (danger) {
                bgCol = pressed ? 0xEE7A1414 : (hovered ? 0xEEB02424 : 0xDD8E1B1B);
                borderCol = hovered ? 0xFFFF5A52 : 0xFFB02424;
            } else {
                bgCol = pressed ? 0xEE141820 : (hovered ? 0xEE262E3B : 0xDD181E27);
                borderCol = hovered ? 0xFF5A94D8 : (active ? 0xFF364050 : 0xFF202630);
            }

            p.rect(x, y, w, h, bgCol);
            p.outline(x, y, w, h, borderCol);

            // Subtle 3D top bevel light
            p.fill(x + 1, y + 1, x + w - 1, y + 2, hovered ? 0x44FFFFFF : 0x22FFFFFF);

            int textCol = active ? (danger ? 0xFFFFFFFF : (hovered ? 0xFFFFFFFF : 0xFFD7DEE5)) : 0xFF647080;
            if (mark != null) {
                drawMark(p, cx, cy + dy, w, textCol);
            } else if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x, y + dy, w, h, textCol);
            }
            if (sub != null && !sub.getString().isEmpty()) {
                p.label(sub.getString(), x, y + h - 9 + dy, w, 8, 0xFF8E99A8);
            }
        }
    }
}
