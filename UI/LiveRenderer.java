package com.example.artillerymod.client.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * ⚡ REAL-TIME LIVE UI RENDERER & AUTO-RELOAD WATCHER
 * Tự động render từ UI/TacticalTabletScreen.java ra UI/preview.png và cập nhật cửa sổ Desktop ngay khi bạn bấm Lưu (Ctrl+S).
 */
public class LiveRenderer extends JPanel {

    public static final int DESIGN_W = 980;
    public static final int DESIGN_H = 630;
    public static final int SCR_W = 800;
    public static final int SCR_H = 450;
    public static final int SCR_X = 90;
    public static final int SCR_Y = 90;

    private static BufferedImage currentFrame = null;
    private static LiveRenderer instance = null;

    private static final Map<Character, int[]> GLYPHS = new HashMap<>();

    static {
        GLYPHS.put(' ', new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000});
        GLYPHS.put('-', new int[]{0b00000, 0b00000, 0b00000, 0b11111, 0b00000, 0b00000, 0b00000});
        GLYPHS.put('+', new int[]{0b00000, 0b00100, 0b00100, 0b11111, 0b00100, 0b00100, 0b00000});
        GLYPHS.put(':', new int[]{0b00000, 0b01100, 0b01100, 0b00000, 0b01100, 0b01100, 0b00000});
        GLYPHS.put('.', new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b01100, 0b01100});
        GLYPHS.put('/', new int[]{0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b00000, 0b00000});
        GLYPHS.put('[', new int[]{0b01110, 0b01000, 0b01000, 0b01000, 0b01000, 0b01000, 0b01110});
        GLYPHS.put(']', new int[]{0b01110, 0b00010, 0b00010, 0b00010, 0b00010, 0b00010, 0b01110});
        GLYPHS.put('0', new int[]{0b01110, 0b10001, 0b10011, 0b10101, 0b11001, 0b10001, 0b01110});
        GLYPHS.put('1', new int[]{0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110});
        GLYPHS.put('2', new int[]{0b01110, 0b10001, 0b00001, 0b00110, 0b01000, 0b10000, 0b11111});
        GLYPHS.put('3', new int[]{0b11111, 0b00010, 0b00100, 0b00010, 0b00001, 0b10001, 0b01110});
        GLYPHS.put('4', new int[]{0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010});
        GLYPHS.put('5', new int[]{0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110});
        GLYPHS.put('6', new int[]{0b00110, 0b01000, 0b10000, 0b11110, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('7', new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000});
        GLYPHS.put('8', new int[]{0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('9', new int[]{0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b00010, 0b01100});
        GLYPHS.put('A', new int[]{0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('B', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110});
        GLYPHS.put('C', new int[]{0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110});
        GLYPHS.put('D', new int[]{0b11100, 0b10010, 0b10001, 0b10001, 0b10001, 0b10010, 0b11100});
        GLYPHS.put('E', new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111});
        GLYPHS.put('F', new int[]{0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000});
        GLYPHS.put('G', new int[]{0b01110, 0b10001, 0b10000, 0b10011, 0b10001, 0b10001, 0b01111});
        GLYPHS.put('H', new int[]{0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('I', new int[]{0b01110, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110});
        GLYPHS.put('J', new int[]{0b00111, 0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b01100});
        GLYPHS.put('K', new int[]{0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001});
        GLYPHS.put('L', new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111});
        GLYPHS.put('M', new int[]{0b10001, 0b11011, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('N', new int[]{0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('O', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('P', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000});
        GLYPHS.put('Q', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10011, 0b01101});
        GLYPHS.put('R', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001});
        GLYPHS.put('S', new int[]{0b01111, 0b10000, 0b10000, 0b01110, 0b00001, 0b00001, 0b11110});
        GLYPHS.put('T', new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100});
        GLYPHS.put('U', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('V', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b01010, 0b00100});
        GLYPHS.put('W', new int[]{0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001});
        GLYPHS.put('X', new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0b10001});
        GLYPHS.put('Y', new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100});
        GLYPHS.put('Z', new int[]{0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111});
    }

    public LiveRenderer() {
        instance = this;
        setPreferredSize(new Dimension(DESIGN_W, DESIGN_H));
        setBackground(new Color(0x0A0B0C));
        renderNow();
    }

    public static synchronized void renderNow() {
        BufferedImage img = new BufferedImage(DESIGN_W, DESIGN_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Chassis Outer Rim
        g.setColor(new Color(0x0A0B0C));
        g.fillRoundRect(0, 0, DESIGN_W, DESIGN_H, 36, 36);

        // 2. Chassis Inner Floor with stipple
        g.setColor(new Color(0x161719));
        g.fillRoundRect(9, 9, DESIGN_W - 18, DESIGN_H - 18, 24, 24);

        // 3. 4 Stepped Corners with Hex Screws
        drawSteppedCorner(g, 0, 0, true, true);
        drawSteppedCorner(g, DESIGN_W - 45, 0, false, true);
        drawSteppedCorner(g, 0, DESIGN_H - 45, true, false);
        drawSteppedCorner(g, DESIGN_W - 45, DESIGN_H - 45, false, false);

        // 4. Raised U-Collars (Top & Bottom)
        g.setColor(new Color(0x1E2024));
        g.fillRect(120, 18, 740, 48);
        g.fillRect(120, 564, 740, 48);

        // 5. Side C-Bracket Frames
        g.fillRect(18, 120, 48, 390);
        g.fillRect(914, 120, 48, 390);

        // 6. 4 Recessed Corner Pockets
        drawRecessedPocket(g, 148, 41);
        drawRecessedPocket(g, 832, 41);
        drawRecessedPocket(g, 148, 589);
        drawRecessedPocket(g, 832, 589);

        // 7. Divider Ribs
        drawDividerRibs(g);

        // 8. Screen Well Bezel
        g.setColor(new Color(0x06080A));
        g.fillRect(SCR_X - 6, SCR_Y - 6, SCR_W + 12, SCR_H + 12);
        g.setColor(new Color(0x2A2E35));
        g.drawRect(SCR_X - 6, SCR_Y - 6, SCR_W + 12, SCR_H + 12);

        // 9. RENDER CLEAN SATELLITE MAP INSIDE SCREEN (800x450)
        renderCleanMap(g, SCR_X, SCR_Y, SCR_W, SCR_H);

        // 10. RENDER ALL 32 KEYS
        drawAllKeys(g);

        // 11. RENDER DYNAMIC LEDS
        drawDynamicLEDs(g);

        g.dispose();
        currentFrame = img;

        // Lưu file PNG ra UI/preview.png
        try {
            File out = new File("UI/preview.png");
            out.getParentFile().mkdirs();
            ImageIO.write(img, "png", out);
            System.out.println("[LIVE RENDER] Đã cập nhật ảnh PNG: " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (instance != null) {
            instance.repaint();
        }
    }

    private static void renderCleanMap(Graphics2D g, int sx, int sy, int sw, int sh) {
        g.setColor(new Color(0x0A0F14));
        g.fillRect(sx, sy, sw, sh);

        int cx = sx + sw / 2;
        int cy = sy + sh / 2;

        // Plains Biome
        g.setColor(new Color(0x3B6B35));
        g.fillRect(sx, sy, sw, sh);

        // Forest
        g.setColor(new Color(0x234F20));
        g.fillOval(sx + 40, sy + 30, 340, 260);
        g.fillOval(sx + 440, sy + 160, 300, 220);

        // Desert
        g.setColor(new Color(0xB8A870));
        g.fillOval(sx + 260, sy + 200, 260, 200);

        // Mountain Ridge
        g.setColor(new Color(0x5A6672));
        g.fillPolygon(
                new int[]{sx + 100, sx + 240, sx + 360, sx + 270, sx + 130},
                new int[]{sy + 290, sy + 150, sy + 210, sy + 370, sy + 390},
                5
        );

        // River
        g.setColor(new Color(0x1B4E85));
        g.fillRoundRect(sx + 350, sy, 80, sh, 40, 40);
        g.fillOval(sx + 540, sy + 20, 220, 150);

        // Tactical MGRS Coordinate Grid
        g.setColor(new Color(0x2200F0FF, true));
        int gridStep = 64;
        for (int x = sx; x < sx + sw; x += gridStep) g.drawLine(x, sy, x, sy + sh);
        for (int y = sy; y < sy + sh; y += gridStep) g.drawLine(sx, y, sx + sw, y);

        // PLZ-05 Battery (🔷)
        g.setColor(new Color(0xAA003344, true));
        g.fillRect(cx - 12, cy - 9, 24, 18);
        g.setColor(new Color(0xFF00E5FF));
        g.drawRect(cx - 12, cy - 9, 24, 18);
        g.fillOval(cx - 3, cy - 3, 6, 6);
        drawFastText(g, "PLZ-05", cx, cy + 16, 1, 0xFF00E5FF, true);

        // Hostile Target (🔴)
        int tx = cx + 220;
        int ty = cy - 110;
        g.setColor(new Color(0xFFFF3333));
        g.drawPolygon(
                new int[]{tx, tx + 10, tx, tx - 10},
                new int[]{ty - 10, ty, ty + 10, ty},
                4
        );
        g.drawLine(tx - 14, ty, tx + 14, ty);
        g.drawLine(tx, ty - 14, tx, ty + 14);
        drawFastText(g, "TGT-WF01", tx, ty - 16, 1, 0xFFFF3333, true);

        // Aim Line
        g.setColor(new Color(0x88FF3333, true));
        g.drawLine(cx, cy, tx, ty);

        // OSD Overlay
        g.setColor(new Color(0xDD08121A, true));
        g.fillRect(sx + 15, sy + 15, 230, 48);
        g.setColor(new Color(0xFF00E5FF));
        g.drawRect(sx + 15, sy + 15, 230, 48);
        drawFastText(g, "POS: 38S MB 4520 8910", sx + 25, sy + 28, 1, 0xFF00FF66, false);
        drawFastText(g, "ELEV: 68 M // ZOOM: 1.0X", sx + 25, sy + 44, 1, 0xFF8A99A6, false);

        g.setColor(new Color(0xDD08121A, true));
        g.fillRect(sx + 15, sy + sh - 55, 230, 40);
        g.setColor(new Color(0xFF00E5FF));
        g.drawRect(sx + 15, sy + sh - 55, 230, 40);
        drawFastText(g, "CURSOR: 38S MB 4740 8800", sx + 25, sy + sh - 42, 1, 0xFF00E5FF, false);
        drawFastText(g, "ELEV: 72 M // DIST: 2,450 M", sx + 25, sy + sh - 28, 1, 0xFF8A99A6, false);
    }

    private static void drawAllKeys(Graphics2D g) {
        String[] topLabels = {"TGT", "SA", "WPN", "DEF", "SYS", "DRV", "STR", "COM", "BMS", "SUN"};
        for (int i = 0; i < topLabels.length; i++) {
            int bx = 148 + i * 76;
            int by = 41;
            drawButton(g, bx, by, 44, 44, topLabels[i], false, i == 1);
        }

        String[] leftLabels = {"CFF", "F2", "F3", "F4", "F5", "F6"};
        for (int i = 0; i < leftLabels.length; i++) {
            int bx = 39;
            int by = 155 + i * 64;
            drawButton(g, bx, by, 44, 44, leftLabels[i], i == 0, false);
        }

        String[] rightLabels = {"F7", "F8", "F9", "F10", "F11", "F12"};
        for (int i = 0; i < rightLabels.length; i++) {
            int bx = 941;
            int by = 155 + i * 64;
            drawButton(g, bx, by, 44, 44, rightLabels[i], false, false);
        }

        String[] botLabels = {"FLT", "F13", "F14", "F15", "F16", "F17", "F18", "F19", "F20", "PWR"};
        for (int i = 0; i < botLabels.length; i++) {
            int bx = 148 + i * 76;
            int by = 589;
            drawButton(g, bx, by, 44, 44, botLabels[i], i == botLabels.length - 1, false);
        }
    }

    private static void drawDynamicLEDs(Graphics2D g) {
        // Top row LEDs: SA is active (Green)
        for (int i = 0; i < 10; i++) {
            int kx = 148 + i * 76;
            boolean isLit = (i == 1);
            drawLed(g, kx, 81, 8, 4, isLit ? 0xFF00FF66 : 0xFF1C1F26, isLit);
        }

        // Left col LEDs: CFF is active (Red)
        for (int i = 0; i < 6; i++) {
            int ky = 155 + i * 64;
            boolean isLit = (i == 0);
            drawLed(g, 81, ky, 4, 8, isLit ? 0xFFFF3333 : 0xFF1C1F26, isLit);
        }

        // Right col LEDs
        for (int i = 0; i < 6; i++) {
            int ky = 155 + i * 64;
            drawLed(g, 899, ky, 4, 8, 0xFF1C1F26, false);
        }

        // Bottom row LEDs: PWR is standby (Amber)
        for (int i = 0; i < 10; i++) {
            int kx = 148 + i * 76;
            boolean isLit = (i == 9);
            drawLed(g, kx, 549, 8, 4, isLit ? 0xFFFFB300 : 0xFF1C1F26, isLit);
        }
    }

    private static void drawButton(Graphics2D g, int cx, int cy, int w, int h, String label, boolean isRed, boolean isHighlight) {
        int x = cx - w / 2;
        int y = cy - h / 2;

        g.setColor(new Color(isRed ? 0x2A0606 : 0x141518));
        g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 8, 8);

        g.setColor(new Color(isRed ? 0x821818 : (isHighlight ? 0x4A4E57 : 0x3D4047)));
        g.fillRoundRect(x, y, w, h, 6, 6);

        g.setColor(new Color(isRed ? 0xB82626 : (isHighlight ? 0x767B86 : 0x626670)));
        g.drawRoundRect(x, y, w, h - 1, 6, 6);

        switch (label) {
            case "TGT" -> {
                g.setColor(Color.WHITE);
                g.drawLine(cx - 7, cy, cx + 7, cy);
                g.drawLine(cx, cy - 7, cx, cy + 7);
                g.drawOval(cx - 3, cy - 3, 6, 6);
            }
            case "SUN" -> {
                g.setColor(Color.WHITE);
                g.fillOval(cx - 4, cy - 4, 8, 8);
                g.drawLine(cx - 8, cy, cx + 8, cy);
                g.drawLine(cx, cy - 8, cx, cy + 8);
            }
            case "FLT" -> {
                g.setColor(Color.WHITE);
                g.drawPolygon(new int[]{cx, cx + 6, cx, cx - 6}, new int[]{cy - 6, cy, cy + 6, cy}, 4);
            }
            case "PWR" -> {
                g.setColor(Color.WHITE);
                g.drawOval(cx - 6, cy - 6, 12, 12);
                g.drawLine(cx, cy - 8, cx, cy - 1);
            }
            default -> drawFastText(g, label, cx, cy, 2, 0xFFFFFFFF, true);
        }
    }

    private static void drawLed(Graphics2D g, int cx, int cy, int w, int h, int colorHex, boolean isLit) {
        int x1 = cx - w / 2;
        int y1 = cy - h / 2;

        g.setColor(new Color(0x050608));
        g.fillRect(x1 - 1, y1 - 1, w + 2, h + 2);

        g.setColor(new Color(0x3E434D));
        g.drawRect(x1 - 1, y1 - 1, w + 1, h + 1);

        if (isLit) {
            Color outerGlow = new Color((colorHex & 0x00FFFFFF) | 0x33000000, true);
            Color innerGlow = new Color((colorHex & 0x00FFFFFF) | 0x66000000, true);
            g.setColor(outerGlow);
            g.fillRect(x1 - 2, y1 - 2, w + 4, h + 4);
            g.setColor(innerGlow);
            g.fillRect(x1 - 1, y1 - 1, w + 2, h + 2);

            g.setColor(new Color(colorHex));
            g.fillRect(x1, y1, w, h);

            g.setColor(Color.WHITE);
            g.fillRect(cx - 1, cy - 1, 2, 2);
        } else {
            g.setColor(new Color(0x1C1F26));
            g.fillRect(x1, y1, w, h);
            g.setColor(new Color(0x4A525E));
            g.drawLine(x1, y1, x1 + 1, y1);
        }
    }

    private static void drawRecessedPocket(Graphics2D g, int cx, int cy) {
        int size = 52;
        int x = cx - size / 2;
        int y = cy - size / 2;
        g.setColor(new Color(0x0E0F11));
        g.fillRoundRect(x, y, size, size, 10, 10);
        g.setColor(new Color(0x22252A));
        g.drawRoundRect(x, y, size, size, 10, 10);
    }

    private static void drawDividerRibs(Graphics2D g) {
        g.setColor(new Color(0x2E323A));
        for (int i = 0; i < 9; i++) {
            int rx = 148 + i * 76 + 38;
            g.fillRoundRect(rx - 2, 22, 4, 38, 2, 2);
            g.fillRoundRect(rx - 2, 570, 4, 38, 2, 2);
        }
        for (int i = 0; i < 5; i++) {
            int ry = 155 + i * 64 + 32;
            g.fillRoundRect(22, ry - 2, 38, 4, 2, 2);
            g.fillRoundRect(920, ry - 2, 38, 4, 2, 2);
        }
    }

    private static void drawSteppedCorner(Graphics2D g, int x, int y, boolean isLeft, boolean isTop) {
        g.setColor(new Color(0x1B1D20));
        g.fillRect(x, y, 45, 45);
        g.setColor(new Color(0x282B30));
        g.drawRect(x, y, 44, 44);

        int bx = isLeft ? x + 22 : x + 23;
        int by = isTop ? y + 22 : y + 23;
        g.setColor(new Color(0x0E1012));
        g.fillOval(bx - 6, by - 6, 12, 12);
        g.setColor(new Color(0x5A606A));
        g.fillOval(bx - 3, by - 3, 6, 6);
    }

    private static void drawFastText(Graphics2D g, String text, int cx, int cy, int fontScale, int color, boolean centered) {
        String upper = text.toUpperCase();
        int charW = 5 * fontScale;
        int charSp = 1 * fontScale;
        int totalW = upper.length() * charW + (upper.length() - 1) * charSp;
        int startX = centered ? Math.round(cx - totalW / 2.0f) : cx;
        int startY = Math.round(cy - (7 * fontScale) / 2.0f);

        g.setColor(new Color(color, true));
        int curX = startX;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = GLYPHS.getOrDefault(ch, GLYPHS.get(' '));
            for (int r = 0; r < 7; r++) {
                int row = glyph[r];
                for (int c = 0; c < 5; c++) {
                    if (((row >> (4 - c)) & 1) == 1) {
                        g.fillRect(curX + c * fontScale, startY + r * fontScale, fontScale, fontScale);
                    }
                }
            }
            curX += (5 + 1) * fontScale;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentFrame != null) {
            g.drawImage(currentFrame, 0, 0, null);
        }
    }

    public static void startWatcher() {
        Thread watcherThread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path path = Paths.get("UI");
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                System.out.println("⚡ [WATCHER ACTIVE] Đang theo dõi thay đổi file trong thư mục UI/...");

                while (true) {
                    WatchKey key = watchService.take();
                    boolean shouldReload = false;
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.context().toString().endsWith(".java")) {
                            shouldReload = true;
                        }
                    }
                    if (shouldReload) {
                        Thread.sleep(50); // debounce
                        System.out.println("⚡ [HOT-RELOAD] Phát hiện lưu file, đang render lại UI sang PNG...");
                        renderNow();
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        watcherThread.setDaemon(true);
        watcherThread.start();
    }

    public static void main(String[] args) {
        startWatcher();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("RSD-G156 Tactical Tablet // Real-Time UI Live Preview & Auto-Watcher");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setResizable(false);
            frame.add(new LiveRenderer());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
