package net.nazarick.artillerytablet.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;

/**
 * ASTRA Frontline Tactical C2 System UI (Sitaware Frontline Inspired).
 * Renders the full military command-and-control software application inside the tablet OLED screen.
 */
public final class AstraFrontlinePaint {

    public static final int SCR_X = 144;
    public static final int SCR_Y = 104;
    public static final int SCR_W = 692;
    public static final int SCR_H = 422;

    private AstraFrontlinePaint() {}

    /**
     * Bakes the complete tablet case with the active ASTRA Frontline C2 application running on screen.
     */
    public static NativeImage bake() {
        // 1. Bake the master chassis
        NativeImage img = TabletChassisPaint.bake();

        // 2. Render the ASTRA Frontline C2 Software Layer onto the OLED screen
        renderAstraFrontlineApp(img, SCR_X, SCR_Y, SCR_W, SCR_H);

        return img;
    }

    private static void renderAstraFrontlineApp(NativeImage img, int sx, int sy, int sw, int sh) {
        // 1. Base Tactical Map Canvas (Topographic relief + Dark Grid)
        drawTacticalMap(img, sx, sy, sw, sh);

        // 2. NATO Tactical Symbols, BFT (Blue Force Tracking) and Hostile Red Contacts
        drawTacticalUnitsAndOverlays(img, sx, sy, sw, sh);

        // 3. Artillery Fire Direction Control (FDC) Fire Mission Arc & Solution
        drawArtilleryFireMission(img, sx, sy, sw, sh);

        // 4. Drone Thermal UAV Recon Picture-In-Picture (PIP) Window
        drawUavThermalPip(img, sx + sw - 210, sy + 38, 200, 125);

        // 5. Left Quick-Action Tactical Tool Dock
        drawLeftToolDock(img, sx + 8, sy + 38, 38, 300);

        // 6. Top Military Header & C2 Command Bar
        drawTopHeaderBar(img, sx, sy, sw, 30);

        // 7. Bottom Artillery Status, Telemetry & Key Bindings Bar
        drawBottomStatusBar(img, sx, sy + sh - 28, sw, 28);
    }

    // =========================================================================
    // 1. TACTICAL MAP BACKGROUND
    // =========================================================================
    private static void drawTacticalMap(NativeImage img, int sx, int sy, int sw, int sh) {
        // Deep tactical OLED dark navy background
        for (int y = sy; y < sy + sh; y++) {
            for (int x = sx; x < sx + sw; x++) {
                int col = 0xFF080D14;
                // Subtle map contour lines
                int cx = x - sx;
                int cy = y - sy;
                double elevation = Math.sin(cx * 0.02) * Math.cos(cy * 0.02) * 50
                        + Math.sin((cx + cy) * 0.015) * 40
                        + Math.cos((cx * 0.03 - cy * 0.01)) * 30;
                int contourStep = ((int) Math.abs(elevation)) % 25;
                if (contourStep == 0 || contourStep == 1) {
                    col = 0xFF0F1824; // Contour line
                } else if (contourStep == 12) {
                    col = 0xFF0A121C; // Secondary contour
                }

                // Grid lines (MGRS 1km grid)
                if (cx % 70 == 0 || cy % 70 == 0) {
                    col = 0xFF142030;
                }
                // Grid intersection crosshairs
                if (cx % 70 == 0 && cy % 70 == 0) {
                    col = 0xFF2B4466;
                }

                setPixel(img, x, y, col);
            }
        }

        // River / Waterway vector
        for (int y = sy + 30; y < sy + sh - 28; y++) {
            int rx = sx + 220 + (int) (Math.sin((y - sy) * 0.03) * 35 + Math.cos((y - sy) * 0.01) * 20);
            for (int dx = -3; dx <= 3; dx++) {
                int col = (Math.abs(dx) <= 1) ? 0xFF1A3854 : 0xFF12263A;
                setPixel(img, rx + dx, y, col);
            }
        }

        // Tactical MSR (Main Supply Route) Road Vector
        for (int x = sx; x < sx + sw; x++) {
            int ry = sy + 220 + (int) (Math.sin((x - sx) * 0.012) * 45);
            if (ry >= sy + 30 && ry < sy + sh - 28) {
                for (int dy = -1; dy <= 1; dy++) {
                    setPixel(img, x, ry + dy, (dy == 0) ? 0xFF4A4E58 : 0xFF2A2E35);
                }
            }
        }

        // Grid coordinate labels along edges
        for (int i = 1; i < sw / 70; i++) {
            int gx = sx + i * 70;
            drawSmallText(img, "4" + (2 + i) + "E", gx + 4, sy + 34, 0xFF4A6B8A);
        }
        for (int i = 1; i < sh / 70; i++) {
            int gy = sy + i * 70;
            drawSmallText(img, "8" + (4 + i) + "N", sx + 50, gy - 10, 0xFF4A6B8A);
        }
    }

    // =========================================================================
    // 2. NATO BLUE FORCE & RED FORCE TRACKING (MIL-STD-2525D)
    // =========================================================================
    private static void drawTacticalUnitsAndOverlays(NativeImage img, int sx, int sy, int sw, int sh) {
        // Friendly Units (NATO Blue - Rectangle frame)
        // 1. Headquarters / CP
        drawNatoFriendly(img, sx + 160, sy + 180, "HQ", "TF-ASTRA CP");

        // 2. M109A6 Paladin Artillery Battery (Our Battery)
        drawNatoFriendly(img, sx + 280, sy + 300, "FA", "BTY-A [6x 155mm]");

        // 3. Armor Platoon (Tank)
        drawNatoFriendly(img, sx + 380, sy + 160, "AR", "1/A/1-66 AR");

        // 4. Mechanized Infantry
        drawNatoFriendly(img, sx + 430, sy + 240, "IN", "2/B/1-22 IN");

        // 5. Forward Observer Team (FO) with Line-of-Sight Cone
        drawNatoFriendly(img, sx + 340, sy + 110, "FO", "OBSERVER 1-1");
        drawSensorCone(img, sx + 340, sy + 110, 52.0f, 90, 0x3300D2FF);

        // Hostile Units (NATO Red - Diamond frame)
        // 1. Enemy Artillery Battery (Target Alpha)
        drawNatoHostile(img, sx + 540, sy + 110, "FA", "TGT-001 [ENEMY BTY]");
        drawThreatRing(img, sx + 540, sy + 110, 55, 0x55EF4444);

        // 2. Enemy Mechanized Armor Column
        drawNatoHostile(img, sx + 600, sy + 220, "AR", "TGT-002 [BTR-82A x4]");

        // 3. Enemy Air Defense (SAM / Radar)
        drawNatoHostile(img, sx + 490, sy + 80, "AD", "TGT-003 [TOR-M2]");
        drawThreatRing(img, sx + 490, sy + 80, 75, 0x33EF4444);
    }

    private static void drawNatoFriendly(NativeImage img, int x, int y, String sym, String label) {
        int w = 24, h = 18;
        int bx = x - w / 2, by = y - h / 2;
        int fillCol = 0xAA0B2440;
        int borderCol = 0xFF3B82F6;
        int textCol = 0xFFE2E8F0;

        // Rectangle frame
        for (int py = by; py <= by + h; py++) {
            for (int px = bx; px <= bx + w; px++) {
                if (px == bx || px == bx + w || py == by || py == by + h) {
                    setPixel(img, px, py, borderCol);
                } else {
                    setPixel(img, px, py, fillCol);
                }
            }
        }
        drawSmallText(img, sym, bx + 5, by + 5, textCol);
        drawSmallText(img, label, bx - 10, by + h + 3, 0xFF93C5FD);
    }

    private static void drawNatoHostile(NativeImage img, int x, int y, String sym, String label) {
        int r = 13;
        int borderCol = 0xFFEF4444;
        int fillCol = 0xAA3B0D0D;

        // Diamond frame
        for (int dy = -r; dy <= r; dy++) {
            int span = r - Math.abs(dy);
            for (int dx = -span; dx <= span; dx++) {
                if (Math.abs(dx) == span || Math.abs(dy) == r) {
                    setPixel(img, x + dx, y + dy, borderCol);
                } else {
                    setPixel(img, x + dx, y + dy, fillCol);
                }
            }
        }
        drawSmallText(img, sym, x - 6, y - 4, 0xFFFEE2E2);
        drawSmallText(img, label, x - 18, y + r + 3, 0xFFFCA5A5);
    }

    private static void drawThreatRing(NativeImage img, int cx, int cy, int radius, int argb) {
        int r2 = radius * radius;
        int rIn2 = (radius - 1) * (radius - 1);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r2 && d2 >= rIn2) {
                    // Dashed circle
                    double angle = Math.atan2(dy, dx);
                    if (((int) (angle * 10)) % 2 == 0) {
                        setPixel(img, cx + dx, cy + dy, argb);
                    }
                }
            }
        }
    }

    private static void drawSensorCone(NativeImage img, int cx, int cy, float headingDeg, int range, int argb) {
        double headRad = Math.toRadians(headingDeg);
        double fovRad = Math.toRadians(35.0);
        for (int dy = -range; dy <= range; dy++) {
            for (int dx = -range; dx <= range; dx++) {
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist <= range && dist >= 8) {
                    double angle = Math.atan2(dy, dx);
                    double diff = Math.abs(normalizeAngle(angle - headRad));
                    if (diff <= fovRad / 2.0) {
                        setPixel(img, cx + dx, cy + dy, argb);
                    }
                }
            }
        }
    }

    private static double normalizeAngle(double a) {
        while (a > Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    // =========================================================================
    // 3. ARTILLERY FIRE DIRECTION CONTROL (FDC) OVERLAY
    // =========================================================================
    private static void drawArtilleryFireMission(NativeImage img, int sx, int sy, int sw, int sh) {
        int gunX = sx + 280, gunY = sy + 300;
        int tgtX = sx + 540, tgtY = sy + 110;

        // Gun-Target Line (GTL) dashed line
        int steps = 100;
        for (int i = 0; i <= steps; i++) {
            if ((i / 4) % 2 == 0) {
                int px = gunX + (tgtX - gunX) * i / steps;
                int py = gunY + (tgtY - gunY) * i / steps;
                setPixel(img, px, py, 0xFFF59E0B);
                setPixel(img, px + 1, py, 0xFFF59E0B);
            }
        }

        // Impact Dispersion Ellipse (CEP 90) around Target
        int elA = 22, elB = 10;
        for (int deg = 0; deg < 360; deg += 3) {
            double rad = Math.toRadians(deg);
            int ex = tgtX + (int) (Math.cos(rad) * elA);
            int ey = tgtY + (int) (Math.sin(rad) * elB);
            setPixel(img, ex, ey, 0xFFFF0044);
        }

        // Tactical Engagement Info Box
        int boxX = sx + 340, boxY = sy + 205;
        drawGlassPanel(img, boxX, boxY, 150, 60, 0xDD0B121C, 0xFFF59E0B);
        drawSmallText(img, "FIRE MISSION: ACTIVE", boxX + 8, boxY + 6, 0xFFFBBF24);
        drawSmallText(img, "AZ: 052.4°  EL: 48.2°", boxX + 8, boxY + 18, 0xFFE2E8F0);
        drawSmallText(img, "TOF: 28.4s  MAXORD: FL140", boxX + 8, boxY + 30, 0xFFCBD5E1);
        drawSmallText(img, "SPLASH IN: 00:12", boxX + 8, boxY + 44, 0xFFEF4444);
    }

    // =========================================================================
    // 4. PICTURE-IN-PICTURE (PIP) UAV THERMAL FLIR CAM
    // =========================================================================
    private static void drawUavThermalPip(NativeImage img, int px, int py, int pw, int ph) {
        drawGlassPanel(img, px, py, pw, ph, 0xEE05080E, 0xFF0284C7);

        // Header of PIP
        for (int x = px; x < px + pw; x++) {
            for (int y = py; y < py + 14; y++) {
                setPixel(img, x, y, 0xFF0D1B2A);
            }
        }
        drawSmallText(img, "UAV RECON [MQ-9 FLIR IR]", px + 6, py + 3, 0xFF38BDF8);
        drawSmallText(img, "REC [●]", px + pw - 46, py + 3, 0xFFEF4444);

        // Thermal Viewfinder Canvas
        int camY1 = py + 15, camY2 = py + ph - 2;
        int camX1 = px + 2, camX2 = px + pw - 2;
        for (int y = camY1; y < camY2; y++) {
            for (int x = camX1; x < camX2; x++) {
                int noise = ((x * 19 + y * 23) ^ (x * 7)) % 8;
                int col = 0xFF0A0F16 + (noise * 0x010101);
                setPixel(img, x, y, col);
            }
        }

        // Thermal Target Heat Signatures (White-Hot)
        int tx = px + pw / 2 + 10, ty = py + ph / 2 + 5;
        for (int dy = -4; dy <= 4; dy++) {
            for (int dx = -7; dx <= 7; dx++) {
                int heat = 255 - (Math.abs(dx) * 20 + Math.abs(dy) * 30);
                if (heat > 80) {
                    setPixel(img, tx + dx, ty + dy, 0xFF000000 | (heat << 16) | (heat << 8) | heat);
                }
            }
        }

        // Crosshair reticle in thermal cam
        int cx = px + pw / 2, cy = py + ph / 2 + 4;
        for (int x = cx - 12; x <= cx + 12; x++) setPixel(img, x, cy, 0xAA38BDF8);
        for (int y = cy - 12; y <= cy + 12; y++) setPixel(img, cx, y, 0xAA38BDF8);
        drawSmallText(img, "LASER: LOCKED (TGT-001)", px + 6, py + ph - 12, 0xFF10B981);
    }

    // =========================================================================
    // 5. LEFT TACTICAL TOOL DOCK
    // =========================================================================
    private static void drawLeftToolDock(NativeImage img, int dx, int dy, int dw, int dh) {
        drawGlassPanel(img, dx, dy, dw, dh, 0xCC090E17, 0xFF1E293B);

        String[] toolIcons = { "M", "C", "T", "L", "D", "N" };
        String[] toolNames = { "MAP", "CFF", "TGT", "LOS", "DRN", "NAV" };

        for (int i = 0; i < 6; i++) {
            int by = dy + 10 + i * 46;
            boolean active = (i == 1); // CFF Active
            int btnCol = active ? 0xFF2563EB : 0xFF131C28;
            int border = active ? 0xFF60A5FA : 0xFF253347;

            drawGlassPanel(img, dx + 4, by, dw - 8, 38, btnCol, border);
            drawSmallText(img, toolIcons[i], dx + 14, by + 8, active ? 0xFFFFFFFF : 0xFF94A3B8);
            drawSmallText(img, toolNames[i], dx + 6, by + 24, active ? 0xFF93C5FD : 0xFF64748B);
        }
    }

    // =========================================================================
    // 6. TOP APP HEADER BAR (SITAWARE / ASTRA C2 STYLE)
    // =========================================================================
    private static void drawTopHeaderBar(NativeImage img, int sx, int sy, int sw, int sh) {
        for (int y = sy; y < sy + sh; y++) {
            for (int x = sx; x < sx + sw; x++) {
                int col = (y == sy + sh - 1) ? 0xFF0284C7 : 0xFF0B121C;
                setPixel(img, x, y, col);
            }
        }

        // Left Branding
        drawSmallText(img, "ASTRA", sx + 10, sy + 10, 0xFFEF4444);
        drawSmallText(img, "FRONTLINE C2", sx + 48, sy + 10, 0xFF00D2FF);

        // System telemetry
        drawSmallText(img, "[TF-1/77 FA]", sx + 145, sy + 10, 0xFF38BDF8);
        drawSmallText(img, "MGRS: 38SMB 42918 84920", sx + 240, sy + 10, 0xFFCBD5E1);
        drawSmallText(img, "GPS: 3D-FIX (12 SAT)", sx + 410, sy + 10, 0xFF10B981);
        drawSmallText(img, "TIME: 12:04:18 UTC", sx + 540, sy + 10, 0xFFE2E8F0);
        drawSmallText(img, "NET: SECURE", sx + sw - 85, sy + 10, 0xFF10B981);
    }

    // =========================================================================
    // 7. BOTTOM STATUS & HARDWARE KEY MAPPING BAR
    // =========================================================================
    private static void drawBottomStatusBar(NativeImage img, int sx, int sy, int sw, int sh) {
        for (int y = sy; y < sy + sh; y++) {
            for (int x = sx; x < sx + sw; x++) {
                int col = (y == sy) ? 0xFF1E293B : 0xFF080D14;
                setPixel(img, x, y, col);
            }
        }

        // Function Key Quick Indicators matching tablet chassis keys
        drawSmallText(img, "F1:CFF", sx + 12, sy + 9, 0xFFF59E0B);
        drawSmallText(img, "F2:BTY", sx + 65, sy + 9, 0xFF94A3B8);
        drawSmallText(img, "F3:TGT", sx + 120, sy + 9, 0xFF94A3B8);
        drawSmallText(img, "F4:UAV", sx + 175, sy + 9, 0xFF94A3B8);
        drawSmallText(img, "F5:AMMO", sx + 230, sy + 9, 0xFF94A3B8);

        // Battery Status
        drawSmallText(img, "BATTERY STATUS: [READY 6/6]", sx + 310, sy + 9, 0xFF10B981);
        drawSmallText(img, "HE: 142 | PGK: 38 | SMOKE: 12", sx + 490, sy + 9, 0xFFE2E8F0);
        drawSmallText(img, "PWR: 98% 28V", sx + sw - 90, sy + 9, 0xFF10B981);
    }

    // =========================================================================
    // UTILITY DRAWING HELPERS
    // =========================================================================
    private static void drawGlassPanel(NativeImage img, int x, int y, int w, int h, int fillCol, int borderCol) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (px == x || px == x + w - 1 || py == y || py == y + h - 1) {
                    setPixel(img, px, py, borderCol);
                } else {
                    setPixel(img, px, py, fillCol);
                }
            }
        }
    }

    private static void drawSmallText(NativeImage img, String text, int x, int y, int col) {
        String upper = text.toUpperCase();
        int curX = x;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = GLYPHS.getOrDefault(ch, GLYPHS.get(' '));
            for (int r = 0; r < 7; r++) {
                int row = glyph[r];
                for (int c = 0; c < 5; c++) {
                    if (((row >> (4 - c)) & 1) == 1) {
                        setPixel(img, curX + c, y + r, col);
                    }
                }
            }
            curX += 6;
        }
    }

    private static void setPixel(NativeImage img, int x, int y, int col) {
        if (x < 0 || x >= TabletFrame.DESIGN_W || y < 0 || y >= TabletFrame.DESIGN_H) return;
        int a = (col >> 24) & 0xFF;
        int r = (col >> 16) & 0xFF;
        int g = (col >> 8) & 0xFF;
        int b = col & 0xFF;
        if (a < 255) {
            int prev = img.getPixelRGBA(x, y);
            int prevR = prev & 0xFF;
            int prevG = (prev >> 8) & 0xFF;
            int prevB = (prev >> 16) & 0xFF;
            float alpha = a / 255.0f;
            float inv = 1.0f - alpha;
            r = Math.round(r * alpha + prevR * inv);
            g = Math.round(g * alpha + prevG * inv);
            b = Math.round(b * alpha + prevB * inv);
            a = 255;
        }
        int abgr = (a << 24) | (b << 16) | (g << 8) | r;
        img.setPixelRGBA(x, y, abgr);
    }

    // 5x7 Standard Military Glyph Matrix
    private static final Map<Character, int[]> GLYPHS = new HashMap<>();
    static {
        GLYPHS.put(' ', new int[] { 0, 0, 0, 0, 0, 0, 0 });
        GLYPHS.put('-', new int[] { 0, 0, 0, 0b11111, 0, 0, 0 });
        GLYPHS.put('—', new int[] { 0, 0, 0, 0b11111, 0, 0, 0 });
        GLYPHS.put('.', new int[] { 0, 0, 0, 0, 0, 0b01100, 0b01100 });
        GLYPHS.put(':', new int[] { 0, 0b01100, 0b01100, 0, 0b01100, 0b01100, 0 });
        GLYPHS.put('/', new int[] { 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0, 0 });
        GLYPHS.put('[', new int[] { 0b01110, 0b01000, 0b01000, 0b01000, 0b01000, 0b01000, 0b01110 });
        GLYPHS.put(']', new int[] { 0b01110, 0b00010, 0b00010, 0b00010, 0b00010, 0b00010, 0b01110 });
        GLYPHS.put('|', new int[] { 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('%', new int[] { 0b11001, 0b11010, 0b00100, 0b01000, 0b01011, 0b10011, 0 });
        GLYPHS.put('●', new int[] { 0, 0b01110, 0b11111, 0b11111, 0b11111, 0b01110, 0 });
        GLYPHS.put('°', new int[] { 0b01100, 0b10010, 0b10010, 0b01100, 0, 0, 0 });

        GLYPHS.put('0', new int[] { 0b01110, 0b10011, 0b10101, 0b11001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('1', new int[] { 0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110 });
        GLYPHS.put('2', new int[] { 0b01110, 0b10001, 0b00001, 0b00010, 0b00100, 0b01000, 0b11111 });
        GLYPHS.put('3', new int[] { 0b11110, 0b00001, 0b00001, 0b01110, 0b00001, 0b00001, 0b11110 });
        GLYPHS.put('4', new int[] { 0b00010, 0b00110, 0b01010, 0b10010, 0b11111, 0b00010, 0b00010 });
        GLYPHS.put('5', new int[] { 0b11111, 0b10000, 0b11110, 0b00001, 0b00001, 0b10001, 0b01110 });
        GLYPHS.put('6', new int[] { 0b00110, 0b01000, 0b10000, 0b11110, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('7', new int[] { 0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b01000, 0b01000 });
        GLYPHS.put('8', new int[] { 0b01110, 0b10001, 0b10001, 0b01110, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('9', new int[] { 0b01110, 0b10001, 0b10001, 0b01111, 0b00001, 0b00010, 0b01100 });

        GLYPHS.put('A', new int[] { 0b01110, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('B', new int[] { 0b11110, 0b10001, 0b10001, 0b11110, 0b10001, 0b10001, 0b11110 });
        GLYPHS.put('C', new int[] { 0b01110, 0b10001, 0b10000, 0b10000, 0b10000, 0b10001, 0b01110 });
        GLYPHS.put('D', new int[] { 0b11100, 0b10010, 0b10001, 0b10001, 0b10001, 0b10010, 0b11100 });
        GLYPHS.put('E', new int[] { 0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b11111 });
        GLYPHS.put('F', new int[] { 0b11111, 0b10000, 0b10000, 0b11110, 0b10000, 0b10000, 0b10000 });
        GLYPHS.put('G', new int[] { 0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('H', new int[] { 0b10001, 0b10001, 0b10001, 0b11111, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('I', new int[] { 0b01110, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110 });
        GLYPHS.put('J', new int[] { 0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b10010, 0b01100 });
        GLYPHS.put('K', new int[] { 0b10001, 0b10010, 0b10100, 0b11000, 0b10100, 0b10010, 0b10001 });
        GLYPHS.put('L', new int[] { 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111 });
        GLYPHS.put('M', new int[] { 0b10001, 0b11011, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('N', new int[] { 0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('O', new int[] { 0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('P', new int[] { 0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000 });
        GLYPHS.put('Q', new int[] { 0b01110, 0b10001, 0b10001, 0b10001, 0b10101, 0b10011, 0b01111 });
        GLYPHS.put('R', new int[] { 0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10010, 0b10001 });
        GLYPHS.put('S', new int[] { 0b01111, 0b10000, 0b10000, 0b01110, 0b00001, 0b00001, 0b11110 });
        GLYPHS.put('T', new int[] { 0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('U', new int[] { 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('V', new int[] { 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100 });
        GLYPHS.put('W', new int[] { 0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001 });
        GLYPHS.put('X', new int[] { 0b10001, 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0b10001 });
        GLYPHS.put('Y', new int[] { 0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('Z', new int[] { 0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111 });
    }
}
