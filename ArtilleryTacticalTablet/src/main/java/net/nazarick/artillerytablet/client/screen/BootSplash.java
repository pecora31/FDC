package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * ASTRA Systems - Tactical C2 Boot Splash Screen.
 *
 * <p>Displays the official ASTRA SYSTEMS military telemetry startup sequence
 * while the terrain sampler and map tiles are initializing.
 */
@OnlyIn(Dist.CLIENT)
final class BootSplash {

    /** Deep tactical OLED screen backdrop. */
    private static final int BACKDROP = 0xFF080B10;

    /** Crimson laser brand accent. */
    private static final int CRIMSON = 0xFFD32F2F;

    private BootSplash() {
    }

    /**
     * Draws the ASTRA SYSTEMS boot splash centered on the tablet screen.
     *
     * @param g      Graphics context
     * @param x      Screen viewport X
     * @param y      Screen viewport Y
     * @param width  Screen viewport Width
     * @param height Screen viewport Height
     */
    static void draw(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 100 || height < 80) {
            return;
        }

        // 1. Deep OLED Background Backdrop
        g.fill(x, y, x + width, y + height, BACKDROP);

        // 2. Tactical Corner HUD Brackets
        int brkLen = 10;
        int brkCol = 0xFF1E293B;
        // Top-Left
        g.fill(x + 6, y + 6, x + 6 + brkLen, y + 7, brkCol);
        g.fill(x + 6, y + 6, x + 7, y + 6 + brkLen, brkCol);
        // Top-Right
        g.fill(x + width - 6 - brkLen, y + 6, x + width - 6, y + 7, brkCol);
        g.fill(x + width - 7, y + 6, x + width - 6, y + 6 + brkLen, brkCol);
        // Bottom-Left
        g.fill(x + 6, y + height - 7, x + 6 + brkLen, y + height - 6, brkCol);
        g.fill(x + 6, y + height - 6 - brkLen, x + 7, y + height - 6, brkCol);
        // Bottom-Right
        g.fill(x + width - 6 - brkLen, y + height - 7, x + width - 6, y + height - 6, brkCol);
        g.fill(x + width - 7, y + height - 6 - brkLen, x + width - 6, y + height - 6, brkCol);

        // 3. Telemetry Corner Stamps
        TabletTheme.draw(g, Ui.font(), "SYS: ASTRA-OS 4.8", x + 12, y + 10, 0xFF475569, false);
        TabletTheme.draw(g, Ui.font(), "SEC: ENCRYPTED", x + width - 85, y + 10, 0xFF475569, false);
        TabletTheme.draw(g, Ui.font(), "MIL-STD-2525D", x + 12, y + height - 18, 0xFF334155, false);

        int dotCount = (int) ((System.currentTimeMillis() / 250) % 4);
        String standby = "STANDBY" + ".".repeat(dotCount);
        TabletTheme.draw(g, Ui.font(), standby, x + width - 65, y + height - 18, 0xFF38BDF8, false);

        // 4. Centerpiece: Vector ASTRA Logo
        int cx = x + width / 2;
        int cy = y + height / 2 - 8;

        drawAstraVectorLogo(g, cx, cy - 14, CRIMSON);

        // 5. Subtitle: '—  S Y S T E M S  —'
        int subY = cy + 12;
        int wingLen = 32;
        int wingGap = 12;
        String subText = "S Y S T E M S";
        int subW = Ui.width(subText);

        g.fill(cx - subW / 2 - wingGap - wingLen, subY + 3, cx - subW / 2 - wingGap, subY + 5, CRIMSON);
        TabletTheme.draw(g, Ui.font(), subText, cx - subW / 2, subY, 0xFF94A3B8, false);
        g.fill(cx + subW / 2 + wingGap, subY + 3, cx + subW / 2 + wingGap + wingLen, subY + 5, CRIMSON);

        // 6. Tactical Status Text
        String statusText = "INITIALIZING TACTICAL C2 INTERFACE...";
        int statusW = Ui.width(statusText);
        TabletTheme.draw(g, Ui.font(), statusText, cx - statusW / 2, subY + 18, 0xFF64748B, false);

        // 7. High-Tech Animated Scanning Progress Bar
        int barW = 140;
        int barH = 4;
        int barX = cx - barW / 2;
        int barY = subY + 30;

        // Background track & outline
        g.fill(barX, barY, barX + barW, barY + barH, 0xFF0F172A);
        Ui.outline(g, barX - 1, barY - 1, barW + 2, barH + 2, 0xFF1E293B);

        // Sweeping Laser Core & Glow
        long time = System.currentTimeMillis();
        int sweepPos = (int) ((time / 6) % (barW + 40)) - 20;
        for (int i = 0; i < barW; i++) {
            int dist = Math.abs(i - sweepPos);
            if (dist < 24) {
                int alpha = Math.max(0, 255 - dist * 10);
                int col = (alpha << 24) | 0x0038BDF8;
                g.fill(barX + i, barY, barX + i + 1, barY + barH, col);
            }
        }
        // Laser center highlight
        if (sweepPos >= 0 && sweepPos < barW) {
            g.fill(Math.max(barX, barX + sweepPos - 2), barY, Math.min(barX + barW, barX + sweepPos + 2), barY + barH, 0xFFFFFFFF);
        }
    }

    /**
     * Draws the geometric vector ASTRA logo with precision angles and styled letterforms.
     */
    private static void drawAstraVectorLogo(GuiGraphics g, int cx, int cy, int redCol) {
        int letW = 20;
        int letH = 18;
        int gap = 8;
        int totalW = 5 * letW + 4 * gap;
        int startX = cx - totalW / 2;
        int topY = cy - letH / 2;
        int thick = 3;

        for (int i = 0; i < 5; i++) {
            int lx = startX + i * (letW + gap);
            switch (i) {
                case 0, 4 -> { // 'A' (Futuristic Lambda Chevron)
                    for (int dy = 0; dy <= letH; dy++) {
                        float t = (float) dy / letH;
                        int mid = lx + letW / 2;
                        int leftX = Math.round(mid - t * (letW / 2f));
                        int rightX = Math.round(mid + t * (letW / 2f));
                        g.fill(leftX - 1, topY + dy, leftX + 2, topY + dy + 1, redCol);
                        g.fill(rightX - 1, topY + dy, rightX + 2, topY + dy + 1, redCol);
                    }
                }
                case 1 -> { // 'S' (Angular Tactical S)
                    // Top bar
                    g.fill(lx + 2, topY, lx + letW, topY + thick, redCol);
                    // Upper left stem
                    g.fill(lx, topY + 1, lx + thick, topY + letH / 2, redCol);
                    // Middle bar
                    g.fill(lx + 1, topY + letH / 2 - 1, lx + letW - 1, topY + letH / 2 + 2, redCol);
                    // Lower right stem
                    g.fill(lx + letW - thick, topY + letH / 2, lx + letW, topY + letH - 1, redCol);
                    // Bottom bar
                    g.fill(lx, topY + letH - thick, lx + letW - 2, topY + letH, redCol);
                }
                case 2 -> { // 'T' (Tactical T-Bar)
                    // Top bar
                    g.fill(lx, topY, lx + letW, topY + thick, redCol);
                    // Center stem
                    int mid = lx + letW / 2;
                    g.fill(mid - 1, topY + thick, mid + 2, topY + letH, redCol);
                }
                case 3 -> { // 'R' (Tactical R with Angled Kick)
                    // Left stem
                    g.fill(lx, topY, lx + thick, topY + letH, redCol);
                    // Upper loop top
                    g.fill(lx + thick, topY, lx + letW - 2, topY + thick, redCol);
                    // Upper loop right curve
                    g.fill(lx + letW - thick, topY + 1, lx + letW, topY + letH / 2, redCol);
                    // Upper loop bottom
                    g.fill(lx + thick, topY + letH / 2 - 1, lx + letW - 2, topY + letH / 2 + 2, redCol);
                    // Angled leg
                    for (int dy = 0; dy <= letH / 2; dy++) {
                        float t = (float) dy / (letH / 2f);
                        int legX = Math.round(lx + 6 + t * (letW - 8));
                        g.fill(legX - 1, topY + letH / 2 + dy, legX + 2, topY + letH / 2 + dy + 1, redCol);
                    }
                }
            }
        }
    }
}
