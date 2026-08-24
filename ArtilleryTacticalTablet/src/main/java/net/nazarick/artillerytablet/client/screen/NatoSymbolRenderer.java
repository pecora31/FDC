package net.nazarick.artillerytablet.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete Military Standard NATO Tactical Symbology Renderer (MIL-STD-2525D / APP-6D).
 * Supports standard affiliations, unit types, echelon modifiers, and tactical operational graphics.
 */
public final class NatoSymbolRenderer {

    // Standard NATO Affiliation Types
    public enum Affiliation {
        FRIENDLY(0xFF3B82F6, 0xDD0E294B, 0xFFE2E8F0),   // Blue Box
        HOSTILE(0xFFEF4444, 0xDD3B0D0D, 0xFFFEE2E2),    // Red Diamond
        NEUTRAL(0xFF10B981, 0xDD0A2E1C, 0xFFD1FAE5),    // Green Square
        UNKNOWN(0xFFF59E0B, 0xDD3D2808, 0xFFFEF3C7);    // Amber Quatrefoil

        public final int borderColor;
        public final int fillColor;
        public final int textColor;

        Affiliation(int borderColor, int fillColor, int textColor) {
            this.borderColor = borderColor;
            this.fillColor = fillColor;
            this.textColor = textColor;
        }
    }

    // Standard NATO Combat & Service Branches
    public enum UnitType {
        // Ground Combat
        INFANTRY,               // Cross (X)
        MOTORIZED_INFANTRY,     // Cross (X) with wheels (O O)
        MECH_INFANTRY,          // Mechanized Infantry (Oval + Cross)
        ARMOR,                  // Oval Track (⬭)
        RECONNAISSANCE,         // Cavalry Recon Slash (/)
        ARMORED_RECON,          // Armored Recon (Oval + Slash)
        ANTI_TANK,              // Inverted V (∧)
        ARMORED_ANTI_TANK,      // Tank Destroyer (Oval + ∧)
        SPECIAL_FORCES,         // Special Forces (SF / Dagger)
        SNIPER,                 // Sniper / Marksman

        // Artillery & Fire Support
        FIELD_ARTILLERY,        // Cannonball Dot (●)
        SP_ARTILLERY,           // Self-Propelled Howitzer (Oval + Dot)
        ROCKET_ARTILLERY,       // MLRS / HIMARS (Dot + Upward Arrow)
        MORTAR,                 // Mortar tube (Dot + vertical bar)
        SP_MORTAR,              // Armored Mortar (Oval + Mortar)
        OBSERVATION_POST,       // Forward Observer (Triangle + Eye)
        RADAR_ARTILLERY,        // Counter-Battery Radar

        // Air & Air Defense
        AIR_DEFENSE,            // Air Defense Dome Arc (⌢)
        AIR_DEFENSE_MISSILE,    // SAM (Dome + Arrow)
        AVIATION_ROTARY,        // Attack Helicopter (Bowtie ⋈)
        AVIATION_FIXED_WING,    // Combat Jet
        UAV,                    // Unmanned Aerial Vehicle (Drone)

        // Combat Support & Service Support
        HEADQUARTERS,           // Command Post (HQ staff flagpole)
        ENGINEER,               // Sapper Bridge (⊓)
        ARMORED_ENGINEER,       // Breaching ABV (Oval + ⊓)
        SIGNAL,                 // Communications Lightning Bolt (⚡)
        ELECTRONIC_WARFARE,     // Electronic Warfare / Cyber (EW)
        CBRN,                   // Chemical / Biological / Rad / Nuclear
        LOGISTICS,              // General Supply (Cross bar ⊞)
        AMMO_SUPPLY,            // Ammunition Supply Point (ASP)
        FUEL_SUPPLY,            // POL Fuel Depot
        MAINTENANCE,            // Recovery / Repair
        MEDICAL,                // Geneva Medical Cross (+)
        MILITARY_POLICE         // Military Police (MP)
    }

    // Standard NATO Echelon Sizes
    public enum Echelon {
        TEAM("Ø"),
        SQUAD("●"),
        SECTION("●●"),
        PLATOON("●●●"),
        COMPANY("|"),
        BATTALION("||"),
        REGIMENT("|||"),
        BRIGADE("X"),
        DIVISION("XX"),
        CORPS("XXX"),
        ARMY("XXXX"),
        ARMY_GROUP("XXXXX"),
        THEATER("XXXXXX");

        public final String symbol;
        Echelon(String symbol) { this.symbol = symbol; }
    }

    private NatoSymbolRenderer() {}

    /**
     * Draws a complete NATO MIL-STD-2525D symbol with affiliation frame, unit icon, echelon marker, and labels.
     */
    public static void drawSymbol(NativeImage img, int cx, int cy, int size,
                                  Affiliation aff, UnitType type, Echelon echelon,
                                  String unitDesignation, String higherFormation) {
        int w = size;
        int h = (int) (size * 0.75f);
        int x1 = cx - w / 2;
        int y1 = cy - h / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;

        // 1. Draw Affiliation Frame
        switch (aff) {
            case FRIENDLY -> drawFriendlyFrame(img, x1, y1, x2, y2, aff.fillColor, aff.borderColor);
            case HOSTILE -> drawHostileFrame(img, cx, cy, (int)(size * 0.65f), aff.fillColor, aff.borderColor);
            case NEUTRAL -> drawNeutralFrame(img, x1 + 2, y1 - 2, x2 - 2, y2 + 2, aff.fillColor, aff.borderColor);
            case UNKNOWN -> drawUnknownFrame(img, cx, cy, (int)(size * 0.55f), aff.fillColor, aff.borderColor);
        }

        // 2. Draw Branch / Function Icon inside frame
        drawBranchIcon(img, cx, cy, size, type, aff.borderColor, aff.textColor);

        // 3. Draw Echelon Modifier above the top of frame
        if (echelon != null) {
            int echY = y1 - 10;
            drawSmallText(img, echelon.symbol, cx - (echelon.symbol.length() * 3), echY, aff.borderColor);
        }

        // 4. Draw Unit Designation (Unique callsign on bottom)
        if (unitDesignation != null && !unitDesignation.isEmpty()) {
            drawSmallText(img, unitDesignation, cx - (unitDesignation.length() * 3), y2 + 4, 0xFFCBD5E1);
        }

        // 5. Higher Formation (Right side of frame)
        if (higherFormation != null && !higherFormation.isEmpty()) {
            drawSmallText(img, higherFormation, x2 + 5, cy - 3, 0xFF94A3B8);
        }
    }

    // =========================================================================
    // AFFILIATION FRAME DRAWING
    // =========================================================================
    private static void drawFriendlyFrame(NativeImage img, int x1, int y1, int x2, int y2, int fillCol, int borderCol) {
        int r = 3;
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (isInsideRounded(x, y, x1, y1, x2, y2, r)) {
                    if (isBorder(x, y, x1, y1, x2, y2, r, 2)) {
                        setPixel(img, x, y, borderCol);
                    } else {
                        setPixel(img, x, y, fillCol);
                    }
                }
            }
        }
    }

    private static void drawHostileFrame(NativeImage img, int cx, int cy, int r, int fillCol, int borderCol) {
        for (int dy = -r; dy <= r; dy++) {
            int span = r - Math.abs(dy);
            for (int dx = -span; dx <= span; dx++) {
                if (Math.abs(dx) >= span - 1 || Math.abs(dy) >= r - 1) {
                    setPixel(img, cx + dx, cy + dy, borderCol);
                } else {
                    setPixel(img, cx + dx, cy + dy, fillCol);
                }
            }
        }
    }

    private static void drawNeutralFrame(NativeImage img, int x1, int y1, int x2, int y2, int fillCol, int borderCol) {
        for (int y = y1; y <= y2; y++) {
            for (int x = x1; x <= x2; x++) {
                if (x <= x1 + 1 || x >= x2 - 1 || y <= y1 + 1 || y >= y2 - 1) {
                    setPixel(img, x, y, borderCol);
                } else {
                    setPixel(img, x, y, fillCol);
                }
            }
        }
    }

    private static void drawUnknownFrame(NativeImage img, int cx, int cy, int r, int fillCol, int borderCol) {
        int r2 = r * r;
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 <= r2) {
                    if (d2 >= (r - 2) * (r - 2)) {
                        setPixel(img, cx + dx, cy + dy, borderCol);
                    } else {
                        setPixel(img, cx + dx, cy + dy, fillCol);
                    }
                }
            }
        }
    }

    // =========================================================================
    // BRANCH / FUNCTION ICONS (MIL-STD-2525D)
    // =========================================================================
    private static void drawBranchIcon(NativeImage img, int cx, int cy, int size, UnitType type, int borderCol, int iconCol) {
        int halfW = (int) (size * 0.32f);
        int halfH = (int) (size * 0.22f);

        switch (type) {
            case INFANTRY -> {
                drawLine(img, cx - halfW, cy - halfH, cx + halfW, cy + halfH, iconCol);
                drawLine(img, cx + halfW, cy - halfH, cx - halfW, cy + halfH, iconCol);
            }
            case MOTORIZED_INFANTRY -> {
                drawLine(img, cx - halfW, cy - halfH + 2, cx + halfW, cy + halfH - 2, iconCol);
                drawLine(img, cx + halfW, cy - halfH + 2, cx - halfW, cy + halfH - 2, iconCol);
                fillCircle(img, cx - halfW / 2, cy + halfH, 2, iconCol);
                fillCircle(img, cx + halfW / 2, cy + halfH, 2, iconCol);
            }
            case ARMOR -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
            }
            case MECH_INFANTRY -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                drawLine(img, cx - halfW + 3, cy - halfH + 2, cx + halfW - 3, cy + halfH - 2, iconCol);
                drawLine(img, cx + halfW - 3, cy - halfH + 2, cx - halfW + 3, cy + halfH - 2, iconCol);
            }
            case RECONNAISSANCE -> {
                drawLine(img, cx - halfW, cy + halfH, cx + halfW, cy - halfH, iconCol);
            }
            case ARMORED_RECON -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                drawLine(img, cx - halfW + 2, cy + halfH - 2, cx + halfW - 2, cy - halfH + 2, iconCol);
            }
            case ANTI_TANK -> {
                drawLine(img, cx - halfW, cy + halfH, cx, cy - halfH, iconCol);
                drawLine(img, cx, cy - halfH, cx + halfW, cy + halfH, iconCol);
            }
            case ARMORED_ANTI_TANK -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                drawLine(img, cx - halfW / 2, cy + halfH - 2, cx, cy - halfH + 2, iconCol);
                drawLine(img, cx, cy - halfH + 2, cx + halfW / 2, cy + halfH - 2, iconCol);
            }
            case SPECIAL_FORCES -> {
                drawSmallText(img, "SF", cx - 5, cy - 3, iconCol);
            }
            case SNIPER -> {
                drawLine(img, cx - halfW + 2, cy, cx + halfW - 2, cy, iconCol);
                drawLine(img, cx, cy - halfH, cx, cy + halfH, iconCol);
                fillCircle(img, cx, cy, 2, iconCol);
            }

            // Artillery & Fire Support
            case FIELD_ARTILLERY -> {
                fillCircle(img, cx, cy, (int)(halfH * 0.65f), iconCol);
            }
            case SP_ARTILLERY -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                fillCircle(img, cx, cy, (int)(halfH * 0.45f), iconCol);
            }
            case ROCKET_ARTILLERY -> {
                fillCircle(img, cx, cy + 2, (int)(halfH * 0.45f), iconCol);
                drawLine(img, cx, cy - halfH, cx, cy - 1, iconCol);
                drawLine(img, cx - 1, cy - halfH + 2, cx, cy - halfH, iconCol);
                drawLine(img, cx + 1, cy - halfH + 2, cx, cy - halfH, iconCol);
            }
            case MORTAR -> {
                fillCircle(img, cx, cy + 3, (int)(halfH * 0.40f), iconCol);
                drawLine(img, cx, cy - halfH, cx, cy + 1, iconCol);
                drawLine(img, cx - 3, cy - halfH, cx + 3, cy - halfH, iconCol);
            }
            case SP_MORTAR -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                fillCircle(img, cx, cy + 2, 2, iconCol);
                drawLine(img, cx, cy - halfH + 2, cx, cy + 1, iconCol);
            }
            case OBSERVATION_POST -> {
                drawLine(img, cx - halfW, cy + halfH, cx + halfW, cy + halfH, iconCol);
                drawLine(img, cx - halfW, cy + halfH, cx, cy - halfH, iconCol);
                drawLine(img, cx, cy - halfH, cx + halfW, cy + halfH, iconCol);
                fillCircle(img, cx, cy + 2, 2, iconCol);
            }
            case RADAR_ARTILLERY -> {
                fillCircle(img, cx, cy + 2, 3, iconCol);
                for (int deg = 180; deg <= 360; deg += 15) {
                    double rad = Math.toRadians(deg);
                    setPixel(img, cx + (int)(Math.cos(rad) * 6), cy - 2 + (int)(Math.sin(rad) * 5), iconCol);
                }
            }

            // Air & Air Defense
            case AIR_DEFENSE -> {
                for (int deg = 180; deg <= 360; deg += 6) {
                    double rad = Math.toRadians(deg);
                    int px = cx + (int) (Math.cos(rad) * halfW);
                    int py = cy + (int) (Math.sin(rad) * halfH) + halfH / 2;
                    setPixel(img, px, py, iconCol);
                    setPixel(img, px, py - 1, iconCol);
                }
            }
            case AIR_DEFENSE_MISSILE -> {
                for (int deg = 180; deg <= 360; deg += 6) {
                    double rad = Math.toRadians(deg);
                    int px = cx + (int) (Math.cos(rad) * halfW);
                    int py = cy + (int) (Math.sin(rad) * halfH) + halfH / 2;
                    setPixel(img, px, py, iconCol);
                }
                drawLine(img, cx, cy - halfH, cx, cy + halfH / 2, iconCol);
                drawLine(img, cx - 2, cy - halfH + 2, cx, cy - halfH, iconCol);
                drawLine(img, cx + 2, cy - halfH + 2, cx, cy - halfH, iconCol);
            }
            case AVIATION_ROTARY -> {
                drawLine(img, cx - halfW, cy - halfH, cx + halfW, cy + halfH, iconCol);
                drawLine(img, cx - halfW, cy + halfH, cx + halfW, cy - halfH, iconCol);
                drawLine(img, cx - halfW, cy - halfH, cx - halfW, cy + halfH, iconCol);
                drawLine(img, cx + halfW, cy - halfH, cx + halfW, cy + halfH, iconCol);
            }
            case AVIATION_FIXED_WING -> {
                drawLine(img, cx - halfW, cy, cx + halfW, cy, iconCol);
                drawLine(img, cx, cy - halfH, cx, cy + halfH, iconCol);
                drawLine(img, cx - halfW / 2, cy + halfH, cx + halfW / 2, cy + halfH, iconCol);
            }
            case UAV -> {
                drawLine(img, cx, cy - halfH, cx - halfW, cy + halfH, iconCol);
                drawLine(img, cx, cy - halfH, cx + halfW, cy + halfH, iconCol);
                drawLine(img, cx - halfW, cy + halfH, cx, cy + halfH / 2, iconCol);
                drawLine(img, cx + halfW, cy + halfH, cx, cy + halfH / 2, iconCol);
            }

            // Combat Support & Logistics
            case HEADQUARTERS -> {
                drawLine(img, cx - halfW, cy + halfH, cx - halfW, cy + halfH + 8, borderCol);
                fillCircle(img, cx, cy, 3, iconCol);
            }
            case ENGINEER -> {
                drawLine(img, cx - halfW, cy + halfH, cx - halfW, cy - halfH, iconCol);
                drawLine(img, cx - halfW, cy - halfH, cx + halfW, cy - halfH, iconCol);
                drawLine(img, cx + halfW, cy - halfH, cx + halfW, cy + halfH, iconCol);
                drawLine(img, cx - halfW / 2, cy - halfH, cx - halfW / 2, cy + halfH, iconCol);
                drawLine(img, cx + halfW / 2, cy - halfH, cx + halfW / 2, cy + halfH, iconCol);
            }
            case ARMORED_ENGINEER -> {
                drawOvalTrack(img, cx, cy, halfW, halfH, iconCol);
                drawLine(img, cx - halfW / 2, cy + halfH - 2, cx - halfW / 2, cy - halfH + 2, iconCol);
                drawLine(img, cx + halfW / 2, cy + halfH - 2, cx + halfW / 2, cy - halfH + 2, iconCol);
            }
            case SIGNAL -> {
                drawLine(img, cx + 3, cy - halfH, cx - 2, cy, iconCol);
                drawLine(img, cx - 2, cy, cx + 2, cy, iconCol);
                drawLine(img, cx + 2, cy, cx - 3, cy + halfH, iconCol);
            }
            case ELECTRONIC_WARFARE -> {
                drawSmallText(img, "EW", cx - 5, cy - 3, iconCol);
            }
            case CBRN -> {
                drawSmallText(img, "NBC", cx - 8, cy - 3, iconCol);
            }
            case LOGISTICS -> {
                drawLine(img, cx - halfW, cy, cx + halfW, cy, iconCol);
                drawLine(img, cx, cy - halfH, cx, cy + halfH, iconCol);
            }
            case AMMO_SUPPLY -> {
                drawLine(img, cx - 2, cy - halfH, cx + 2, cy - halfH, iconCol);
                drawLine(img, cx - 3, cy - halfH + 3, cx - 3, cy + halfH, iconCol);
                drawLine(img, cx + 3, cy - halfH + 3, cx + 3, cy + halfH, iconCol);
                drawLine(img, cx - 3, cy + halfH, cx + 3, cy + halfH, iconCol);
            }
            case FUEL_SUPPLY -> {
                drawSmallText(img, "POL", cx - 8, cy - 3, iconCol);
            }
            case MAINTENANCE -> {
                drawLine(img, cx - halfW, cy + halfH, cx + halfW, cy - halfH, iconCol);
                drawLine(img, cx - halfW, cy + halfH - 3, cx - halfW + 3, cy + halfH, iconCol);
                drawLine(img, cx + halfW - 3, cy - halfH, cx + halfW, cy - halfH + 3, iconCol);
            }
            case MEDICAL -> {
                for (int y = cy - halfH; y <= cy + halfH; y++) {
                    setPixel(img, cx - 1, y, iconCol);
                    setPixel(img, cx, y, iconCol);
                    setPixel(img, cx + 1, y, iconCol);
                }
                for (int x = cx - halfW; x <= cx + halfW; x++) {
                    setPixel(img, x, cy - 1, iconCol);
                    setPixel(img, x, cy, iconCol);
                    setPixel(img, x, cy + 1, iconCol);
                }
            }
            case MILITARY_POLICE -> {
                drawSmallText(img, "MP", cx - 5, cy - 3, iconCol);
            }
            default -> {
                fillCircle(img, cx, cy, 3, iconCol);
            }
        }
    }

    private static void drawOvalTrack(NativeImage img, int cx, int cy, int hw, int hh, int col) {
        int r = hh;
        int straightW = hw - r;
        for (int x = cx - straightW; x <= cx + straightW; x++) {
            setPixel(img, x, cy - hh, col);
            setPixel(img, x, cy + hh, col);
        }
        for (int deg = 90; deg <= 270; deg += 10) {
            double rad = Math.toRadians(deg);
            setPixel(img, (int) (cx - straightW + Math.cos(rad) * r), (int) (cy + Math.sin(rad) * r), col);
        }
        for (int deg = -90; deg <= 90; deg += 10) {
            double rad = Math.toRadians(deg);
            setPixel(img, (int) (cx + straightW + Math.cos(rad) * r), (int) (cy + Math.sin(rad) * r), col);
        }
    }

    // =========================================================================
    // PRIMITIVE UTILITIES
    // =========================================================================
    public static void drawLine(NativeImage img, int x0, int y0, int x1, int y1, int col) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
        int err = dx + dy, e2;
        while (true) {
            setPixel(img, x0, y0, col);
            if (x0 == x1 && y0 == y1) break;
            e2 = 2 * err;
            if (e2 >= dy) { err += dy; x0 += sx; }
            if (e2 <= dx) { err += dx; y0 += sy; }
        }
    }

    public static void fillCircle(NativeImage img, int cx, int cy, int r, int col) {
        for (int dy = -r; dy <= r; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                if (dx * dx + dy * dy <= r * r) {
                    setPixel(img, cx + dx, cy + dy, col);
                }
            }
        }
    }

    private static boolean isInsideRounded(int x, int y, int x1, int y1, int x2, int y2, int r) {
        if (x < x1 || x > x2 || y < y1 || y > y2) return false;
        if (x < x1 + r && y < y1 + r) {
            int dx = x1 + r - x, dy = y1 + r - y;
            return dx * dx + dy * dy <= r * r;
        }
        if (x > x2 - r && y < y1 + r) {
            int dx = x - (x2 - r), dy = y1 + r - y;
            return dx * dx + dy * dy <= r * r;
        }
        if (x < x1 + r && y > y2 - r) {
            int dx = x1 + r - x, dy = y - (y2 - r);
            return dx * dx + dy * dy <= r * r;
        }
        if (x > x2 - r && y > y2 - r) {
            int dx = x - (x2 - r), dy = y - (y2 - r);
            return dx * dx + dy * dy <= r * r;
        }
        return true;
    }

    private static boolean isBorder(int x, int y, int x1, int y1, int x2, int y2, int r, int thick) {
        if (!isInsideRounded(x, y, x1, y1, x2, y2, r)) return false;
        return !isInsideRounded(x, y, x1 + thick, y1 + thick, x2 - thick, y2 - thick, Math.max(0, r - thick));
    }

    public static void drawSmallText(NativeImage img, String text, int x, int y, int col) {
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

    public static void setPixel(NativeImage img, int x, int y, int col) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return;
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

    private static final Map<Character, int[]> GLYPHS = new HashMap<>();
    static {
        GLYPHS.put(' ', new int[] { 0, 0, 0, 0, 0, 0, 0 });
        GLYPHS.put('-', new int[] { 0, 0, 0, 0b11111, 0, 0, 0 });
        GLYPHS.put('|', new int[] { 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('●', new int[] { 0, 0b01110, 0b11111, 0b11111, 0b11111, 0b01110, 0 });
        GLYPHS.put('Ø', new int[] { 0b01111, 0b10011, 0b10101, 0b11001, 0b11110, 0, 0 });
        GLYPHS.put('X', new int[] { 0b10001, 0b01010, 0b00100, 0b01010, 0b10001, 0, 0 });

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
        GLYPHS.put('Y', new int[] { 0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('Z', new int[] { 0b11111, 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0b11111 });
    }
}
