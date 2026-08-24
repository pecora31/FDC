package net.nazarick.artillerytablet.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;

/**
 * ASTRA Frontline Tactical C2 System UI (Sitaware Frontline Inspired).
 * - Full 800x450 Map Canvas (Header & Footer hidden by default).
 * - F2 LED illuminated on left chassis flank.
 * - Left Sidebar displaying registered PZh 2000 Artillery Battery units.
 * - Bottom-Right Vertical Zoom & Center Controls (+ / - / ⌖).
 */
public final class AstraFrontlinePaint {

    public static final int SCR_X = TabletFrame.SCR_X; // 90
    public static final int SCR_Y = TabletFrame.SCR_Y; // 90
    public static final int SCR_W = TabletFrame.SCR_W; // 800
    public static final int SCR_H = TabletFrame.SCR_H; // 450
    public static final int SCR_R = 10;                // Screen corner radius

    private AstraFrontlinePaint() {}

    /**
     * Bakes the complete tablet case with the active ASTRA Frontline C2 application running on screen.
     */
    public static NativeImage bake() {
        // 1. Bake the master chassis
        NativeImage img = TabletChassisPaint.bake();

        // 2. Turn on F2 Button LED on left flank (Green optical light pipe)
        // Left Flank F2 Key is at index 1: cy = 155 + 1 * 64 = 219, LED at (76, 217)
        bakeLedSprite(img, 76, 217, 8, 4, true, 0xFF00FF66);

        // 3. Render the ASTRA Frontline C2 Fullscreen Map & Tactical Sidebar
        renderAstraFrontlineApp(img, SCR_X, SCR_Y, SCR_W, SCR_H);

        return img;
    }

    private static void renderAstraFrontlineApp(NativeImage img, int sx, int sy, int sw, int sh) {
        // 1. Fullscreen Tactical Map Canvas (Topographic relief + MGRS Grid)
        drawTacticalMap(img, sx, sy, sw, sh);

        // 2. NATO Tactical Symbols & Units (Blue Force & Red OPFOR)
        drawTacticalUnitsAndOverlays(img, sx, sy, sw, sh);

        // 3. Artillery Fire Direction Control (FDC) Fire Mission Arc
        drawArtilleryFireMission(img, sx, sy, sw, sh);

        // 4. Left Tactical Sidebar: Registered PZh 2000 Artillery Battery
        drawPzh2000Sidebar(img, sx + 8, sy + 8, 246, sh - 16);

        // 5. Bottom-Right Vertical Map Controls (+, -, ⌖) and Scale Ruler
        drawMapControls(img, sx + sw - 48, sy + sh - 138, 38, 126);
    }

    // =========================================================================
    // 1. TACTICAL MAP BACKGROUND (FULLSCREEN)
    // =========================================================================
    private static void drawTacticalMap(NativeImage img, int sx, int sy, int sw, int sh) {
        for (int y = sy; y < sy + sh; y++) {
            for (int x = sx; x < sx + sw; x++) {
                if (!isInsideRoundedRect(x, y, sx, sy, sx + sw, sy + sh, SCR_R)) continue;

                int col = 0xFF08090C;
                int cx = x - sx;
                int cy = y - sy;

                // Contour lines
                double elevation = Math.sin(cx * 0.018) * Math.cos(cy * 0.018) * 50
                        + Math.sin((cx + cy) * 0.012) * 40
                        + Math.cos((cx * 0.025 - cy * 0.01)) * 30;
                int contourStep = ((int) Math.abs(elevation)) % 24;
                if (contourStep == 0 || contourStep == 1) {
                    col = 0xFF14171E; // Contour line
                } else if (contourStep == 12) {
                    col = 0xFF0E1015; // Secondary contour
                }

                // Grid lines (MGRS 1km grid)
                if (cx % 80 == 0 || cy % 75 == 0) {
                    col = 0xFF181C24;
                }
                // Grid intersection crosshairs
                if (cx % 80 == 0 && cy % 75 == 0) {
                    col = 0xFF3E4856;
                }

                setPixel(img, x, y, col);
            }
        }

        // River / Waterway vector
        for (int y = sy; y < sy + sh; y++) {
            int rx = sx + 390 + (int) (Math.sin((y - sy) * 0.025) * 45 + Math.cos((y - sy) * 0.01) * 25);
            for (int dx = -4; dx <= 4; dx++) {
                int col = (Math.abs(dx) <= 1) ? 0xFF222834 : 0xFF141820;
                setPixel(img, rx + dx, y, col);
            }
        }

        // Tactical MSR (Main Supply Route) Road Vector
        for (int x = sx; x < sx + sw; x++) {
            int ry = sy + 220 + (int) (Math.sin((x - sx) * 0.01) * 50);
            if (ry >= sy && ry < sy + sh) {
                for (int dy = -1; dy <= 1; dy++) {
                    setPixel(img, x, ry + dy, (dy == 0) ? 0xFF5A606E : 0xFF282C34);
                }
            }
        }

        // Grid coordinate labels along top & bottom edges
        for (int i = 1; i < sw / 80; i++) {
            int gx = sx + i * 80;
            drawSmallText(img, "4" + (2 + i) + "E", gx + 4, sy + 8, 0xFF5A6678);
        }
        for (int i = 1; i < sh / 75; i++) {
            int gy = sy + i * 75;
            drawSmallText(img, "8" + (4 + i) + "N", sx + sw - 40, gy - 8, 0xFF5A6678);
        }
    }

    // =========================================================================
    // 2. NATO BLUE FORCE & RED FORCE TRACKING
    // =========================================================================
    private static void drawTacticalUnitsAndOverlays(NativeImage img, int sx, int sy, int sw, int sh) {
        // Friendly Units (Preserved standard NATO Blue - Rectangle frame)
        // 1. PZh 2000 Battery Alpha
        drawNatoFriendly(img, sx + 410, sy + 320, "FA", "BTY-A [4x PZh 2000]");

        // 2. Armor Platoon (Leopard 2A7)
        drawNatoFriendly(img, sx + 530, sy + 200, "AR", "1/PzBtl 104");

        // 3. Mechanized Infantry (Puma IFV)
        drawNatoFriendly(img, sx + 580, sy + 280, "IN", "2/PzGrenBtl 122");

        // 4. Forward Observer Team (JFST)
        drawNatoFriendly(img, sx + 480, sy + 130, "FO", "JFST OBS-1");
        drawSensorCone(img, sx + 480, sy + 130, 48.0f, 120, 0x24FFFFFF);

        // Hostile Units (Preserved standard NATO Red - Diamond frame)
        // 1. Enemy Artillery Battery (Target Alpha)
        drawNatoHostile(img, sx + 680, sy + 110, "FA", "TGT-001 [2S19 BTY]");
        drawThreatRing(img, sx + 680, sy + 110, 65, 0x55EF4444);

        // 2. Enemy Mechanized Armor Column
        drawNatoHostile(img, sx + 730, sy + 230, "AR", "TGT-002 [BMP-3 x4]");

        // 3. Enemy Air Defense
        drawNatoHostile(img, sx + 620, sy + 75, "AD", "TGT-003 [PANTSIR]");
        drawThreatRing(img, sx + 620, sy + 75, 80, 0x33EF4444);
    }

    private static void drawNatoFriendly(NativeImage img, int x, int y, String sym, String label) {
        int w = 26, h = 19;
        int bx = x - w / 2, by = y - h / 2;
        int fillCol = 0xAA0B2440;
        int borderCol = 0xFF3B82F6;
        int textCol = 0xFFE2E8F0;

        for (int py = by; py <= by + h; py++) {
            for (int px = bx; px <= bx + w; px++) {
                if (px == bx || px == bx + w || py == by || py == by + h) {
                    setPixel(img, px, py, borderCol);
                } else {
                    setPixel(img, px, py, fillCol);
                }
            }
        }
        drawSmallText(img, sym, bx + 6, by + 6, textCol);
        drawSmallText(img, label, bx - 12, by + h + 4, 0xFFCBD5E1);
    }

    private static void drawNatoHostile(NativeImage img, int x, int y, String sym, String label) {
        int r = 14;
        int borderCol = 0xFFEF4444;
        int fillCol = 0xAA3B0D0D;

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
        drawSmallText(img, label, x - 20, y + r + 4, 0xFFFCA5A5);
    }

    private static void drawThreatRing(NativeImage img, int cx, int cy, int radius, int argb) {
        int r2 = radius * radius;
        int rIn2 = (radius - 1) * (radius - 1);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r2 && d2 >= rIn2) {
                    double angle = Math.atan2(dy, dx);
                    if (((int) (angle * 12)) % 2 == 0) {
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
        int gunX = sx + 410, gunY = sy + 320;
        int tgtX = sx + 680, tgtY = sy + 110;

        // Gun-Target Line (GTL) dashed line
        int steps = 120;
        for (int i = 0; i <= steps; i++) {
            if ((i / 4) % 2 == 0) {
                int px = gunX + (tgtX - gunX) * i / steps;
                int py = gunY + (tgtY - gunY) * i / steps;
                setPixel(img, px, py, 0xFFF59E0B);
                setPixel(img, px + 1, py, 0xFFF59E0B);
            }
        }

        // Impact Dispersion Ellipse (CEP 90) around Target
        int elA = 26, elB = 12;
        for (int deg = 0; deg < 360; deg += 3) {
            double rad = Math.toRadians(deg);
            int ex = tgtX + (int) (Math.cos(rad) * elA);
            int ey = tgtY + (int) (Math.sin(rad) * elB);
            setPixel(img, ex, ey, 0xFFFF0044);
        }
    }

    // =========================================================================
    // 4. LEFT TACTICAL SIDEBAR: REGISTERED PZH 2000 BATTERY
    // =========================================================================
    private static void drawPzh2000Sidebar(NativeImage img, int x, int y, int w, int h) {
        // Main glass panel
        drawGlassPanel(img, x, y, w, h, 0xF00B0E14, 0xFF334155);

        // Header
        for (int py = y; py < y + 36; py++) {
            for (int px = x; px < x + w; px++) {
                setPixel(img, px, py, 0xFF141923);
            }
        }
        for (int px = x; px < x + w; px++) setPixel(img, px, y + 36, 0xFF475569);

        drawSmallText(img, "REGISTERED BATTERY", x + 10, y + 8, 0xFFFFFFFF);
        drawSmallText(img, "4x PZH 2000 [155MM/L52] - BTY A", x + 10, y + 22, 0xFF94A3B8);

        // 4 Howitzer Unit Cards
        int cardY = y + 44;
        int cardH = 90;
        int gap = 6;

        // Gun 1: PZh-01 (READY)
        drawGunCard(img, x + 6, cardY + 0 * (cardH + gap), w - 12, cardH,
                "01", "PZH-2000 'WOTAN'", "READY", 0xFF10B981,
                "AZ: 052.4°  EL: 48.2°",
                "AMMO: 48/60 (HE:32, PGK:12, SMK:4)",
                "CHG: 5 (DM72) | TUBE: 28°C");

        // Gun 2: PZh-02 (READY)
        drawGunCard(img, x + 6, cardY + 1 * (cardH + gap), w - 12, cardH,
                "02", "PZH-2000 'THOR'", "READY", 0xFF10B981,
                "AZ: 052.4°  EL: 48.2°",
                "AMMO: 52/60 (HE:36, PGK:12, SMK:4)",
                "CHG: 5 (DM72) | TUBE: 32°C");

        // Gun 3: PZh-03 (LOADING)
        drawGunCard(img, x + 6, cardY + 2 * (cardH + gap), w - 12, cardH,
                "03", "PZH-2000 'ODIN'", "LOADING", 0xFFF59E0B,
                "AUTOLOADER: 82% (HE-FRAG)",
                "AMMO: 36/60 (HE:20, PGK:12, SMK:4)",
                "CHG: 5 (DM72) | TUBE: 68°C");

        // Gun 4: PZh-04 (MOVING)
        drawGunCard(img, x + 6, cardY + 3 * (cardH + gap), w - 12, cardH,
                "04", "PZH-2000 'TYR'", "MOVING", 0xFF60A5FA,
                "SPEED: 34 KM/H  HDG: 045°",
                "AMMO: 60/60 (FULL LOAD)",
                "TO DP-ALPHA (ETA 02:40)");
    }

    private static void drawGunCard(NativeImage img, int x, int y, int w, int h,
                                    String id, String callsign, String status, int statusCol,
                                    String line1, String line2, String line3) {
        // Card background & subtle border
        drawGlassPanel(img, x, y, w, h, 0xEE121620, 0xFF2A3444);

        // Left accent tag
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + 3; px++) {
                setPixel(img, px, py, statusCol);
            }
        }

        // Title and Status Badge
        drawSmallText(img, "[" + id + "] " + callsign, x + 8, y + 8, 0xFFFFFFFF);
        
        // Status Badge Pill
        int badgeW = status.length() * 6 + 10;
        int badgeX = x + w - badgeW - 6;
        drawGlassPanel(img, badgeX, y + 6, badgeW, 12, (statusCol & 0x00FFFFFF) | 0x22000000, statusCol);
        drawSmallText(img, status, badgeX + 5, y + 9, statusCol);

        // Technical data lines
        drawSmallText(img, line1, x + 8, y + 26, 0xFFE2E8F0);
        drawSmallText(img, line2, x + 8, y + 44, 0xFFCBD5E1);
        drawSmallText(img, line3, x + 8, y + 62, 0xFF94A3B8);
    }

    // =========================================================================
    // 5. BOTTOM-RIGHT MAP CONTROLS (+, -, ⌖) & SCALE RULER
    // =========================================================================
    private static void drawMapControls(NativeImage img, int x, int y, int w, int h) {
        int btnSize = w; // 38x38
        int gap = 6;

        // Button 1: [ + ] (Zoom In)
        drawControlButton(img, x, y + 0 * (btnSize + gap), btnSize, btnSize, "+");

        // Button 2: [ - ] (Zoom Out)
        drawControlButton(img, x, y + 1 * (btnSize + gap), btnSize, btnSize, "-");

        // Button 3: [ ⌖ ] (Center Reticle)
        drawControlButton(img, x, y + 2 * (btnSize + gap), btnSize, btnSize, "⌖");

        // Map Scale Ruler (1 KM) directly to the left of zoom buttons
        int scaleX = x - 90, scaleY = y + 2 * (btnSize + gap) + 20;
        for (int px = scaleX; px <= scaleX + 70; px++) {
            setPixel(img, px, scaleY, 0xFFCBD5E1);
            if (px == scaleX || px == scaleX + 35 || px == scaleX + 70) {
                for (int py = scaleY - 3; py <= scaleY + 3; py++) setPixel(img, px, py, 0xFFCBD5E1);
            }
        }
        drawSmallText(img, "1 KM", scaleX + 22, scaleY - 12, 0xFFCBD5E1);
    }

    private static void drawControlButton(NativeImage img, int x, int y, int w, int h, String symbol) {
        drawGlassPanel(img, x, y, w, h, 0xEE141923, 0xFF475569);

        int cx = x + w / 2;
        int cy = y + h / 2;
        int col = 0xFFFFFFFF;

        switch (symbol) {
            case "+" -> {
                for (int px = cx - 6; px <= cx + 6; px++) {
                    setPixel(img, px, cy, col);
                    setPixel(img, px, cy + 1, col);
                }
                for (int py = cy - 6; py <= cy + 6; py++) {
                    setPixel(img, cx, py, col);
                    setPixel(img, cx + 1, py, col);
                }
            }
            case "-" -> {
                for (int px = cx - 6; px <= cx + 6; px++) {
                    setPixel(img, px, cy, col);
                    setPixel(img, px, cy + 1, col);
                }
            }
            case "⌖" -> {
                // Circle reticle with center dot
                for (int deg = 0; deg < 360; deg += 10) {
                    double rad = Math.toRadians(deg);
                    int px = cx + (int) (Math.cos(rad) * 6);
                    int py = cy + (int) (Math.sin(rad) * 6);
                    setPixel(img, px, py, col);
                }
                setPixel(img, cx, cy, col);
                setPixel(img, cx - 9, cy, col);
                setPixel(img, cx + 9, cy, col);
                setPixel(img, cx, cy - 9, col);
                setPixel(img, cx, cy + 9, col);
            }
        }
    }

    // =========================================================================
    // UTILITY DRAWING HELPERS
    // =========================================================================
    private static void bakeLedSprite(NativeImage img, int lx, int ly, int w, int h, boolean lit, int litCol) {
        if (lit) {
            int rgb = litCol & 0x00FFFFFF;
            // 1. Phosphor Halo
            for (int dy = -3; dy <= h + 2; dy++) {
                for (int dx = -3; dx <= w + 2; dx++) {
                    int distSq = 0;
                    if (dx < 0) distSq += dx * dx;
                    else if (dx >= w) distSq += (dx - w + 1) * (dx - w + 1);
                    if (dy < 0) distSq += dy * dy;
                    else if (dy >= h) distSq += (dy - h + 1) * (dy - h + 1);

                    int alpha = (distSq == 0) ? 0 : ((distSq <= 2) ? 0x80 : ((distSq <= 5) ? 0x40 : ((distSq <= 9) ? 0x20 : 0)));
                    if (alpha > 0) {
                        setPixel(img, lx + dx, ly + dy, (alpha << 24) | rgb);
                    }
                }
            }

            // 2. Body
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF000000 | rgb);
                }
            }

            // 3. Core filament
            int coreY = ly + h / 2 - 1;
            for (int x = 1; x < w - 1; x++) {
                setPixel(img, lx + x, coreY, 0xFFFFFFFF);
                setPixel(img, lx + x, coreY + 1, 0xFFE0FFF0);
            }
        }
    }

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

    private static boolean isInsideRoundedRect(int px, int py, int x1, int y1, int x2, int y2, int r) {
        if (px < x1 || px >= x2 || py < y1 || py >= y2) return false;
        if (px < x1 + r && py < y1 + r) {
            int dx = x1 + r - px - 1, dy = y1 + r - py - 1;
            return dx * dx + dy * dy <= r * r;
        }
        if (px >= x2 - r && py < y1 + r) {
            int dx = px - (x2 - r), dy = y1 + r - py - 1;
            return dx * dx + dy * dy <= r * r;
        }
        if (px < x1 + r && py >= y2 - r) {
            int dx = x1 + r - px - 1, dy = py - (y2 - r);
            return dx * dx + dy * dy <= r * r;
        }
        if (px >= x2 - r && py >= y2 - r) {
            int dx = px - (x2 - r), dy = py - (y2 - r);
            return dx * dx + dy * dy <= r * r;
        }
        return true;
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
        GLYPHS.put('(', new int[] { 0b00110, 0b01000, 0b01000, 0b01000, 0b01000, 0b01000, 0b00110 });
        GLYPHS.put(')', new int[] { 0b01100, 0b00010, 0b00010, 0b00010, 0b00010, 0b00010, 0b01100 });
        GLYPHS.put('|', new int[] { 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('%', new int[] { 0b11001, 0b11010, 0b00100, 0b01000, 0b01011, 0b10011, 0 });
        GLYPHS.put('●', new int[] { 0, 0b01110, 0b11111, 0b11111, 0b11111, 0b01110, 0 });
        GLYPHS.put('°', new int[] { 0b01100, 0b10010, 0b10010, 0b01100, 0, 0, 0 });
        GLYPHS.put('\'', new int[] { 0b01100, 0b01100, 0b01000, 0, 0, 0, 0 });
        GLYPHS.put('"', new int[] { 0b10100, 0b10100, 0b10100, 0, 0, 0, 0 });

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
