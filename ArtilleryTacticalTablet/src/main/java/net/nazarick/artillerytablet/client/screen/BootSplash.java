package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * ASTRA Systems - Pure OLED Minimalist Boot Splash Screen.
 *
 * <p>Clean pitch black screen with centered ASTRA SYSTEMS logo during initialization.
 */
@OnlyIn(Dist.CLIENT)
final class BootSplash {

    /** Pure pitch black OLED backdrop. */
    private static final int BACKDROP = 0xFF000000;

    /** Crimson laser brand accent. */
    private static final int CRIMSON = 0xFFB8141D;

    private BootSplash() {
    }

    /**
     * Draws the minimalist ASTRA SYSTEMS boot splash centered on the tablet screen.
     */
    static void draw(GuiGraphics g, int x, int y, int width, int height) {
        if (width < 50 || height < 40) {
            return;
        }

        // 1. Pure Pitch Black OLED Backdrop
        g.fill(x, y, x + width, y + height, BACKDROP);

        // 2. Centerpiece: Enlarged Vector ASTRA Logo
        int cx = x + width / 2;
        int cy = y + height / 2;

        drawAstraVectorLogo(g, cx, cy - 12, CRIMSON);

        // 3. Subtitle: '—  S   Y   S   T   E   M   S  —' with smooth letter shimmer
        int subY = cy + 18;
        int wingLen = 38;
        int wingGap = 14;
        String[] letters = {"S", "Y", "S", "T", "E", "M", "S"};
        int charGap = 10; // Giãn cách thoáng đãng giữa các chữ cái
        int totalLettersW = 0;
        for (int i = 0; i < letters.length; i++) {
            totalLettersW += Ui.width(letters[i]);
            if (i < letters.length - 1) totalLettersW += charGap;
        }

        int startLettersX = cx - totalLettersW / 2;
        int leftWingX1 = startLettersX - wingGap - wingLen;
        int leftWingX2 = startLettersX - wingGap;
        int rightWingX1 = startLettersX + totalLettersW + wingGap;
        int rightWingX2 = rightWingX1 + wingLen;

        // Static Crimson Wing Lines (Hai vạch đỏ bên cạnh giữ màu đỏ tĩnh, không hiệu ứng)
        g.fill(leftWingX1, subY + 3, leftWingX2, subY + 5, CRIMSON);
        g.fill(rightWingX1, subY + 3, rightWingX2, subY + 5, CRIMSON);

        // Animated light wave sweeping across the letters ONLY
        long time = System.currentTimeMillis();
        float cycle = (time % 1100L) / 1100.0f; // 1.1s cycle
        float sweepX = (startLettersX - 12) + cycle * (totalLettersW + 24);

        // Letters rendered with smooth continuous metallic shimmer
        int curX = startLettersX;
        for (String let : letters) {
            int letW = Ui.width(let);
            float letMidX = curX + letW / 2.0f;
            float dist = Math.abs(letMidX - sweepX);
            float glow = Math.max(0f, 1f - dist / 22f);
            glow = glow * glow; // Quadratic ease

            // Interpolate from deep metallic slate (0xFF525E70) to radiant white-silver (0xFFFFFFFF)
            int letterCol = Ui.blend(0xFF525E70, 0xFFFFFFFF, glow);
            TabletTheme.draw(g, Ui.font(), let, curX, subY, letterCol, false);

            curX += letW + charGap;
        }
    }

    /**
     * Draws the enlarged geometric vector ASTRA logo with precision angles and styled letterforms.
     */
    private static void drawAstraVectorLogo(GuiGraphics g, int cx, int cy, int redCol) {
        int letW = 30;
        int letH = 26;
        int gap = 14;
        int totalW = 5 * letW + 4 * gap;
        int startX = cx - totalW / 2;
        int topY = cy - letH / 2;
        int thick = 4;

        for (int i = 0; i < 5; i++) {
            int lx = startX + i * (letW + gap);
            switch (i) {
                case 0, 4 -> { // 'A' (Futuristic Lambda Chevron)
                    for (int dy = 0; dy <= letH; dy++) {
                        float t = (float) dy / letH;
                        int mid = lx + letW / 2;
                        int leftX = Math.round(mid - t * (letW / 2f));
                        int rightX = Math.round(mid + t * (letW / 2f));
                        g.fill(leftX - 2, topY + dy, leftX + 2, topY + dy + 1, redCol);
                        g.fill(rightX - 1, topY + dy, rightX + 3, topY + dy + 1, redCol);
                    }
                }
                case 1 -> { // 'S' (Angular Tactical S)
                    // Top bar
                    g.fill(lx + 3, topY, lx + letW, topY + thick, redCol);
                    // Upper left stem
                    g.fill(lx, topY + 2, lx + thick, topY + letH / 2, redCol);
                    // Middle bar
                    g.fill(lx + 2, topY + letH / 2 - 2, lx + letW - 2, topY + letH / 2 + 2, redCol);
                    // Lower right stem
                    g.fill(lx + letW - thick, topY + letH / 2, lx + letW, topY + letH - 2, redCol);
                    // Bottom bar
                    g.fill(lx, topY + letH - thick, lx + letW - 3, topY + letH, redCol);
                }
                case 2 -> { // 'T' (Tactical T-Bar)
                    // Top bar
                    g.fill(lx, topY, lx + letW, topY + thick, redCol);
                    // Center stem
                    int mid = lx + letW / 2;
                    g.fill(mid - 2, topY + thick, mid + 2, topY + letH, redCol);
                }
                case 3 -> { // 'R' (Tactical R with Angled Kick)
                    // Left stem
                    g.fill(lx, topY, lx + thick, topY + letH, redCol);
                    // Upper loop top
                    g.fill(lx + thick, topY, lx + letW - 3, topY + thick, redCol);
                    // Upper loop right curve
                    g.fill(lx + letW - thick, topY + 2, lx + letW, topY + letH / 2, redCol);
                    // Upper loop bottom
                    g.fill(lx + thick, topY + letH / 2 - 2, lx + letW - 3, topY + letH / 2 + 2, redCol);
                    // Angled leg
                    for (int dy = 0; dy <= letH / 2; dy++) {
                        float t = (float) dy / (letH / 2.0f);
                        int legX = Math.round(lx + 8 + t * (letW - 12));
                        g.fill(legX - 2, topY + letH / 2 + dy, legX + 2, topY + letH / 2 + dy + 1, redCol);
                    }
                }
            }
        }
    }
}
