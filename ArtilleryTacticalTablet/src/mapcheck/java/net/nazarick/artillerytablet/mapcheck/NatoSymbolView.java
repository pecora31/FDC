package net.nazarick.artillerytablet.mapcheck;

import com.mojang.blaze3d.platform.NativeImage;
import net.nazarick.artillerytablet.client.screen.NatoSymbolRenderer;
import net.nazarick.artillerytablet.client.screen.TabletChassisPaint;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Dedicated Showcase & Test Harness for the NATO Military Symbology Library (MIL-STD-2525D).
 * Renders all standard branch icons, affiliations, and echelon sizes on the tablet display.
 */
public final class NatoSymbolView {

    public static final class Main {
        public static void main(String[] args) throws Exception {
            net.minecraft.SharedConstants.tryDetectVersion();
            net.minecraft.server.Bootstrap.bootStrap();
            NatoSymbolView.run(Path.of(args.length > 0 ? args[0] : "build/mapcheck"));
        }
    }

    private NatoSymbolView() {}

    public static void run(Path out) throws Exception {
        Files.createDirectories(out);

        // 1. Bake Tablet Chassis
        NativeImage img = TabletChassisPaint.bake();

        // 2. Render NATO Symbology Matrix onto 800x450 OLED Screen
        int sx = 90, sy = 90, sw = 800, sh = 450;
        renderNatoShowcase(img, sx, sy, sw, sh);

        // 3. Write outputs
        img.writeToFile(out.resolve("nato-symbols.png"));
        img.writeToFile(out.resolve("case.png"));
        System.out.printf("NATO Symbols Showcase: OK -> %s and %s%n",
                out.resolve("nato-symbols.png"), out.resolve("case.png"));
    }

    private static void renderNatoShowcase(NativeImage img, int sx, int sy, int sw, int sh) {
        // Deep Charcoal OLED Canvas
        for (int y = sy; y < sy + sh; y++) {
            for (int x = sx; x < sx + sw; x++) {
                if (isInsideRoundedRect(x, y, sx, sy, sx + sw, sy + sh, 10)) {
                    setPixel(img, x, y, 0xFF080A0E);
                }
            }
        }

        // Header Title
        drawSmallText(img, "NATO MIL-STD-2525D / APP-6D TACTICAL SYMBOLOGY LIBRARY", sx + 16, sy + 14, 0xFFFFFFFF);
        for (int px = sx + 16; px < sx + sw - 16; px++) setPixel(img, px, sy + 28, 0xFF334155);

        // Row 1: FRIENDLY (Blue Force) Combat Units
        drawSmallText(img, "FRIENDLY (BLUE FORCE):", sx + 16, sy + 38, 0xFF3B82F6);
        NatoSymbolRenderer.drawSymbol(img, sx + 50,  sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.INFANTRY, NatoSymbolRenderer.Echelon.PLATOON, "1/A/1-22", "IN");
        NatoSymbolRenderer.drawSymbol(img, sx + 120, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.ARMOR, NatoSymbolRenderer.Echelon.COMPANY, "A/1-66", "AR");
        NatoSymbolRenderer.drawSymbol(img, sx + 190, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.MECH_INFANTRY, NatoSymbolRenderer.Echelon.BATTALION, "1-8", "MECH");
        NatoSymbolRenderer.drawSymbol(img, sx + 260, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.FIELD_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "A/1-77", "FA");
        NatoSymbolRenderer.drawSymbol(img, sx + 330, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.SP_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "BTY-1", "PZH");
        NatoSymbolRenderer.drawSymbol(img, sx + 400, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.ROCKET_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "HIMARS", "MLRS");
        NatoSymbolRenderer.drawSymbol(img, sx + 470, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.MORTAR, NatoSymbolRenderer.Echelon.SECTION, "MTR-1", "120M");
        NatoSymbolRenderer.drawSymbol(img, sx + 540, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.RECONNAISSANCE, NatoSymbolRenderer.Echelon.COMPANY, "1/1-10", "CAV");
        NatoSymbolRenderer.drawSymbol(img, sx + 610, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.AIR_DEFENSE, NatoSymbolRenderer.Echelon.COMPANY, "PATRIOT", "AD");
        NatoSymbolRenderer.drawSymbol(img, sx + 680, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.AVIATION_ROTARY, NatoSymbolRenderer.Echelon.COMPANY, "APACHE", "AV");
        NatoSymbolRenderer.drawSymbol(img, sx + 750, sy + 75, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.OBSERVATION_POST, NatoSymbolRenderer.Echelon.TEAM, "JFST-1", "FO");

        // Row 2: HOSTILE (Red OPFOR) Combat Units
        drawSmallText(img, "HOSTILE (RED OPFOR):", sx + 16, sy + 130, 0xFFEF4444);
        NatoSymbolRenderer.drawSymbol(img, sx + 50,  sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.INFANTRY, NatoSymbolRenderer.Echelon.PLATOON, "TGT-10", "IN");
        NatoSymbolRenderer.drawSymbol(img, sx + 120, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.ARMOR, NatoSymbolRenderer.Echelon.COMPANY, "TGT-11", "T90");
        NatoSymbolRenderer.drawSymbol(img, sx + 190, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.MECH_INFANTRY, NatoSymbolRenderer.Echelon.COMPANY, "TGT-12", "BMP");
        NatoSymbolRenderer.drawSymbol(img, sx + 260, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.FIELD_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "TGT-13", "D30");
        NatoSymbolRenderer.drawSymbol(img, sx + 330, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.SP_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "TGT-14", "2S19");
        NatoSymbolRenderer.drawSymbol(img, sx + 400, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.ROCKET_ARTILLERY, NatoSymbolRenderer.Echelon.COMPANY, "TGT-15", "GRAD");
        NatoSymbolRenderer.drawSymbol(img, sx + 470, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.MORTAR, NatoSymbolRenderer.Echelon.SECTION, "TGT-16", "2B11");
        NatoSymbolRenderer.drawSymbol(img, sx + 540, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.RECONNAISSANCE, NatoSymbolRenderer.Echelon.SECTION, "TGT-17", "BRDM");
        NatoSymbolRenderer.drawSymbol(img, sx + 610, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.AIR_DEFENSE, NatoSymbolRenderer.Echelon.COMPANY, "TGT-18", "TOR");
        NatoSymbolRenderer.drawSymbol(img, sx + 680, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.AVIATION_ROTARY, NatoSymbolRenderer.Echelon.COMPANY, "TGT-19", "KA52");
        NatoSymbolRenderer.drawSymbol(img, sx + 750, sy + 170, 30, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.ANTI_TANK, NatoSymbolRenderer.Echelon.SECTION, "TGT-20", "KORNET");

        // Row 3: SUPPORT, COMBAT SERVICE & NEUTRAL / UNKNOWN
        drawSmallText(img, "COMBAT SUPPORT & SERVICE (ENGINEER / SIGNAL / MEDICAL / LOGISTICS / HQ):", sx + 16, sy + 225, 0xFFCBD5E1);
        NatoSymbolRenderer.drawSymbol(img, sx + 50,  sy + 265, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.ENGINEER, NatoSymbolRenderer.Echelon.COMPANY, "54 EN", "EN");
        NatoSymbolRenderer.drawSymbol(img, sx + 130, sy + 265, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.SIGNAL, NatoSymbolRenderer.Echelon.COMPANY, "121 SIG", "SIG");
        NatoSymbolRenderer.drawSymbol(img, sx + 210, sy + 265, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.MEDICAL, NatoSymbolRenderer.Echelon.COMPANY, "MED-1", "MED");
        NatoSymbolRenderer.drawSymbol(img, sx + 290, sy + 265, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.LOGISTICS, NatoSymbolRenderer.Echelon.BATTALION, "BSB", "LOG");
        NatoSymbolRenderer.drawSymbol(img, sx + 370, sy + 265, 30, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.HEADQUARTERS, NatoSymbolRenderer.Echelon.BRIGADE, "1 ABCT", "HQ");

        drawSmallText(img, "NEUTRAL (GREEN) & UNKNOWN (AMBER):", sx + 450, sy + 225, 0xFF10B981);
        NatoSymbolRenderer.drawSymbol(img, sx + 490, sy + 265, 30, NatoSymbolRenderer.Affiliation.NEUTRAL, NatoSymbolRenderer.UnitType.LOGISTICS, null, "UN-CONVOY", "CIV");
        NatoSymbolRenderer.drawSymbol(img, sx + 570, sy + 265, 30, NatoSymbolRenderer.Affiliation.NEUTRAL, NatoSymbolRenderer.UnitType.MEDICAL, null, "ICRC-1", "HOSP");
        NatoSymbolRenderer.drawSymbol(img, sx + 660, sy + 265, 30, NatoSymbolRenderer.Affiliation.UNKNOWN, NatoSymbolRenderer.UnitType.ARMOR, null, "TRACK-402", "UNK");
        NatoSymbolRenderer.drawSymbol(img, sx + 740, sy + 265, 30, NatoSymbolRenderer.Affiliation.UNKNOWN, NatoSymbolRenderer.UnitType.AIR_DEFENSE, null, "RADAR-88", "EMIT");

        // Row 4: ECHELON SIZES LEGEND
        for (int px = sx + 16; px < sx + sw - 16; px++) setPixel(img, px, sy + 325, 0xFF334155);
        drawSmallText(img, "NATO ECHELON SIZES:  [Ø] TEAM  [●] SQUAD  [●●] SECTION  [●●●] PLATOON  [|] COMPANY/BATTERY  [||] BATTALION  [|||] REGIMENT  [X] BRIGADE  [XX] DIVISION", sx + 16, sy + 340, 0xFFCBD5E1);

        // Technical Spec Notice
        drawSmallText(img, "STANDARDS COMPLIANCE: MIL-STD-2525D / NATO APP-6D VECTOR GRAPHICS ENGINE (ASTRA C2 CORE)", sx + 16, sy + 380, 0xFF7A8699);
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
        if (x < 0 || x >= 980 || y < 0 || y >= 630) return;
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
        GLYPHS.put('/', new int[] { 0b00001, 0b00010, 0b00100, 0b01000, 0b10000, 0, 0 });
        GLYPHS.put('[', new int[] { 0b01110, 0b01000, 0b01000, 0b01000, 0b01000, 0b01000, 0b01110 });
        GLYPHS.put(']', new int[] { 0b01110, 0b00010, 0b00010, 0b00010, 0b00010, 0b00010, 0b01110 });
        GLYPHS.put('(', new int[] { 0b00110, 0b01000, 0b01000, 0b01000, 0b01000, 0b01000, 0b00110 });
        GLYPHS.put(')', new int[] { 0b01100, 0b00010, 0b00010, 0b00010, 0b00010, 0b00010, 0b01100 });
        GLYPHS.put('|', new int[] { 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('●', new int[] { 0, 0b01110, 0b11111, 0b11111, 0b11111, 0b01110, 0 });
        GLYPHS.put('Ø', new int[] { 0b01111, 0b10011, 0b10101, 0b11001, 0b11110, 0, 0 });
        GLYPHS.put(':', new int[] { 0, 0b01100, 0b01100, 0, 0b01100, 0b01100, 0 });
        GLYPHS.put('&', new int[] { 0b01100, 0b10010, 0b01100, 0b10101, 0b10010, 0b01101, 0 });

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
