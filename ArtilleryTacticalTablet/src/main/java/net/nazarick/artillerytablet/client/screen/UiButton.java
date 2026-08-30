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

    public enum Mark { PLUS, MINUS, CENTRE, GRID, BRIGHT, POWER, FILTER }

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
        click();
        if (led != null && led.length >= 4) {
            hardOn = !hardOn;
        }
        if (mfd) {
            mfdOn = !mfdOn;
        }
        if (!active) {
            return false;
        }
        if (action != null) {
            action.run();
        }
        return true;
    }

    public boolean release(double px, double py) {
        if (!contains(px, py)) {
            return false;
        }
        release();
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
                // Compact centered cross
                p.fill(cx - 3, cy, cx + 4, cy + 1, color);
                p.fill(cx, cy - 3, cx + 1, cy + 4, color);
            }
            case MINUS -> {
                // Compact centered horizontal bar
                p.fill(cx - 3, cy, cx + 4, cy + 1, color);
            }
            case CENTRE -> {
                // Compact centered crosshair & target reticle
                p.fill(cx - 3, cy, cx + 4, cy + 1, color);
                p.fill(cx, cy - 3, cx + 1, cy + 4, color);
                p.outline(cx - 2, cy - 2, 5, 5, color);
            }
            case GRID -> {
                // Slim, crisp Reticle Crosshair
                p.fill(cx - 4, cy, cx + 5, cy + 1, color);
                p.fill(cx, cy - 4, cx + 1, cy + 5, color);
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dx = -2; dx <= 2; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= 4 && d2 >= 2) {
                            p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                        }
                    }
                }
            }
            case BRIGHT -> {
                // Slim, elegant Tactical Sun icon (nhỏ gọn, thanh mảnh)
                // Center disc (R = 1.5)
                p.fill(cx - 1, cy - 1, cx + 2, cy + 2, color);
                // 4 Cardinal rays (1px wide, length 2px)
                p.fill(cx, cy - 4, cx + 1, cy - 2, color); // Top ray
                p.fill(cx, cy + 3, cx + 1, cy + 5, color); // Bottom ray
                p.fill(cx - 4, cy, cx - 2, cy + 1, color); // Left ray
                p.fill(cx + 3, cy, cx + 5, cy + 1, color); // Right ray
                // 4 Diagonal points
                p.fill(cx - 3, cy - 3, cx - 2, cy - 2, color);
                p.fill(cx + 3, cy - 3, cx + 4, cy - 2, color);
                p.fill(cx - 3, cy + 3, cx - 2, cy + 4, color);
                p.fill(cx + 3, cy + 3, cx + 4, cy + 4, color);
            }
            case POWER -> {
                // Slim, elegant IEC Power icon (nhỏ gọn, không béo, căn chuẩn tâm)
                int radius = 4;
                int rIn2 = 2 * 2;
                int rOut2 = 4 * 4;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= rOut2 && d2 >= rIn2) {
                            if (dy <= -2 && Math.abs(dx) <= 1) continue; // Top opening gap
                            p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                        }
                    }
                }
                // 1px slim vertical power stem
                p.fill(cx, cy - 4, cx + 1, cy, color);
            }
            case FILTER -> {
                // Day/Night filter mark (Half Sun with rays + Crescent Moon)
                // Crescent moon (right side)
                for (int dy = -3; dy <= 3; dy++) {
                    for (int dx = 0; dx <= 3; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= 9 && (dx >= 1 || Math.abs(dy) <= 1)) {
                            if ((dx - 2) * (dx - 2) + dy * dy > 3) {
                                p.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                            }
                        }
                    }
                }
                // 3 Sun rays (left side)
                p.fill(cx - 4, cy, cx - 2, cy + 1, color); // Left horizontal ray
                p.fill(cx - 3, cy - 3, cx - 2, cy - 2, color); // Top-left diagonal ray
                p.fill(cx - 3, cy + 3, cx - 2, cy + 4, color); // Bottom-left diagonal ray
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
            // 1. HARD PHYSICAL CHASSIS KEYS (32 Keys on Bezel - Solid Molded PBT Keycaps)
            // =========================================================================
            int ky = y + dy;
            boolean redKey = danger || power;

            // Colors for Matte Tactical PBT Plastic:
            int borderCol   = redKey ? 0xFF240606 : 0xFF14161B;
            int rimTopLeft  = redKey ? 0xFFF05252 : 0xFF7E8898;
            int rimBody     = redKey ? 0xFFE03838 : 0xFF667080;
            int rimBotRight = redKey ? 0xFF8A1A1A : 0xFF424854;
            int dishFloor   = redKey ? 0xFFA81E1E : 0xFF444A56;
            int dishShadow  = redKey ? 0xFF5A0C0C : 0xFF262A32;
            int dishGlint   = redKey ? 0xFFD42828 : 0xFF586272;

            int keyR = Math.max(2, Math.min(3, w / 6)); // Bo tròn 4 góc nút bấm tự nhiên
            int dishR = Math.max(1, keyR - 1);

            // 1. 1px dark outer socket border (Rounded)
            p.rounded(x, ky, w, h, keyR, borderCol);

            // 2. Slim 1px Raised Rim Bevel (Rounded)
            p.rounded(x + 1, ky + 1, w - 2, h - 2, dishR, rimBody);
            p.fill(x + keyR, ky + 1, x + w - keyR, ky + 2, rimTopLeft); // Top rim highlight
            p.fill(x + 1, ky + keyR, x + 2, ky + h - keyR, rimTopLeft); // Left rim light
            p.fill(x + keyR, ky + h - 2, x + w - keyR, ky + h - 1, rimBotRight); // Bottom rim shadow
            p.fill(x + w - 2, ky + keyR, x + w - 1, ky + h - keyR, rimBotRight); // Right rim shadow
            p.fill(x + 2, ky + 2, x + 3, ky + 3, rimTopLeft);
            p.fill(x + w - 3, ky + h - 3, x + w - 2, ky + h - 2, rimBotRight);

            // 3. Recessed Dish Floor (Gờ viền mỏng 2px, lòng phím rộng rãi bo góc)
            int rim = 2;
            int dw = w - rim * 2;
            int dh = h - rim * 2;
            int dx0 = x + rim;
            int dy0 = ky + rim;

            p.rounded(dx0, dy0, dw, dh, dishR, dishFloor);
            p.fill(dx0 + dishR, dy0, dx0 + dw - dishR, dy0 + 1, dishShadow); // Top dish shadow
            p.fill(dx0, dy0 + dishR, dx0 + 1, dy0 + dh - dishR, dishShadow); // Left dish shadow
            p.fill(dx0 + dishR, dy0 + dh - 1, dx0 + dw - dishR, dy0 + dh, dishGlint); // Bottom dish highlight
            p.fill(dx0 + dw - 1, dy0 + dishR, dx0 + dw, dy0 + dh - dishR, dishGlint); // Right dish highlight

            // 4. Subtle top shadow on press or ambient outline on hover
            if (pressed) {
                p.fill(x + 1, y, x + w - 1, y + 2, 0x55000000);
            } else if (hovered) {
                p.outline(x + 1, y + 1, w - 2, h - 2, redKey ? 0x50FF5A52 : 0x30FFFFFF);
            }

            // 5. Slender Optical LED Capsule with Brighter Translucent Lens
            if (led != null && led.length >= 4) {
                int lx = led[0], ly = led[1], lw = led[2], lh = led[3];
                int ledCol = danger ? 0xFFFF2828 : 0xFF00FF66;

                // 1px uniform dark socket border on all 4 sides
                p.rect(lx - 1, ly - 1, lw + 2, lh + 2, 0xFF0E1014);

                if (hardOn) {
                    // Active Laser Glow
                    p.rect(lx - 1, ly - 1, lw + 2, lh + 2, (ledCol & 0x00FFFFFF) | 0x25000000);
                    p.rect(lx, ly, lw, lh, ledCol);
                    // White-hot center filament
                    if (lh > lw) {
                        p.fill(lx + lw / 2, ly + 1, lx + lw / 2 + 1, ly + lh - 1, 0xFFFFFFFF);
                    } else {
                        p.fill(lx + 1, ly + lh / 2, lx + lw - 1, ly + lh / 2 + 1, 0xFFFFFFFF);
                    }
                } else {
                    // Unlit Frosted Optical Polycarbonate Lens (sáng hơn, trong suốt quang học)
                    p.rect(lx, ly, lw, lh, 0xFF4E5868);
                    p.fill(lx, ly, lx + lw, ly + 1, 0xFF8090A6); // Top optical glint
                    p.fill(lx, ly, lx + 1, ly + lh, 0xFF708096); // Left optical glint
                    p.fill(lx, ly + lh - 1, lx + lw, ly + lh, 0xFF323844); // Bottom shadow
                    p.fill(lx + lw - 1, ly, lx + lw, ly + lh, 0xFF323844); // Right shadow
                }
            }

            // 6. Text / Mark on Hard Keycap (Optically Centered)
            int textCol = active ? (redKey ? 0xFFFFFFFF : 0xFFF0F4FA) : 0xFF6C7684;
            if (mark != null) {
                drawMark(p, cx, cy + dy, w, textCol);
            } else if (label != null && !label.getString().isEmpty()) {
                p.label(label.getString(), x, ky, w, h, textCol);
            }
            if (sub != null && !sub.getString().isEmpty()) {
                p.label(sub.getString(), x, ky + h - 10, w, 8, 0xFF8E99A8);
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
