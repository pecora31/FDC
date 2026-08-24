package net.nazarick.artillerytablet.mapcheck;

import com.mojang.blaze3d.platform.NativeImage;
import net.nazarick.artillerytablet.client.screen.NatoSymbolRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone Master Reference Table & Specification Sheet for NATO MIL-STD-2525D / APP-6D Symbology.
 * Exports a high-resolution standalone poster (1600x1000) with comprehensive annotations and technical rules.
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

    public static final int POSTER_W = 1600;
    public static final int POSTER_H = 1000;

    public static void run(Path out) throws Exception {
        Files.createDirectories(out);

        NativeImage img = new NativeImage(POSTER_W, POSTER_H, false);

        // 1. Draw Master Specification Poster
        renderMasterSpecTable(img);

        // 2. Write outputs
        img.writeToFile(out.resolve("nato-symbols.png"));
        img.writeToFile(out.resolve("case.png"));
        System.out.printf("NATO Master Reference Table: OK -> %s and %s%n",
                out.resolve("nato-symbols.png"), out.resolve("case.png"));
    }

    private static void renderMasterSpecTable(NativeImage img) {
        // 1. Dark Tactical Navy-Charcoal Background with subtle blueprint grid
        for (int y = 0; y < POSTER_H; y++) {
            for (int x = 0; x < POSTER_W; x++) {
                int col = 0xFF080A0E;
                if (x % 50 == 0 || y % 50 == 0) col = 0xFF10141C;
                if (x % 200 == 0 || y % 200 == 0) col = 0xFF181F2C;
                setPixel(img, x, y, col);
            }
        }

        // Outer border
        drawGlassPanel(img, 10, 10, POSTER_W - 20, POSTER_H - 20, 0x00000000, 0xFF334155);
        drawGlassPanel(img, 14, 14, POSTER_W - 28, POSTER_H - 28, 0x00000000, 0xFF1E293B);

        // 2. Master Header
        for (int y = 20; y < 80; y++) {
            for (int x = 20; x < POSTER_W - 20; x++) {
                setPixel(img, x, y, 0xFF0F141E);
            }
        }
        for (int x = 20; x < POSTER_W - 20; x++) setPixel(img, x, 80, 0xFF3B82F6);

        drawTextScaled(img, "NATO MIL-STD-2525D / APP-6D TACTICAL SYMBOLOGY SPECIFICATION", 32, 32, 2, 0xFFFFFFFF);
        drawSmallText(img, "OFFICIAL MILITARY REFERENCE STANDARD & ASSET DESIGN SYSTEM -- ASTRA C2 SYSTEM CORE", 32, 60, 0xFF94A3B8);
        drawSmallText(img, "DOC-REF: ASTRA-SPEC-MIL2525D-REV4", POSTER_W - 300, 60, 0xFF38BDF8);

        // =========================================================================
        // SECTION 1: AFFILIATIONS & GEOMETRIC FRAMES (LEFT PANEL: X = 30 to 420)
        // =========================================================================
        int p1X = 30, p1Y = 100, p1W = 390, p1H = 870;
        drawGlassPanel(img, p1X, p1Y, p1W, p1H, 0xF00D111A, 0xFF2A3444);
        drawSectionHeader(img, p1X, p1Y, p1W, "1. AFFILIATIONS (NHAN DANG LUC LUONG)");

        // 1. Friendly (Blue Box)
        int afY1 = p1Y + 45;
        NatoSymbolRenderer.drawSymbol(img, p1X + 45, afY1 + 25, 36, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.INFANTRY, NatoSymbolRenderer.Echelon.PLATOON, "1/A", "FRIENDLY");
        drawSmallText(img, "FRIENDLY (QUAN DONG MINH)", p1X + 90, afY1 + 10, 0xFF3B82F6);
        drawSmallText(img, "Màu: Xanh Biển (#3B82F6 / #0E294B)", p1X + 90, afY1 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Khung: Hinh chu nhat bo goc (Rounded Box)", p1X + 90, afY1 + 38, 0xFF94A3B8);

        // 2. Hostile (Red Diamond)
        int afY2 = afY1 + 80;
        NatoSymbolRenderer.drawSymbol(img, p1X + 45, afY2 + 25, 36, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.ARMOR, NatoSymbolRenderer.Echelon.COMPANY, "TGT-01", "HOSTILE");
        drawSmallText(img, "HOSTILE (QUAN DOI PHUONG - OPFOR)", p1X + 90, afY2 + 10, 0xFFEF4444);
        drawSmallText(img, "Màu: Do Tham (#EF4444 / #3B0D0D)", p1X + 90, afY2 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Khung: Hinh thoi sac canh (Diamond)", p1X + 90, afY2 + 38, 0xFF94A3B8);

        // 3. Neutral (Green Square)
        int afY3 = afY2 + 80;
        NatoSymbolRenderer.drawSymbol(img, p1X + 45, afY3 + 25, 36, NatoSymbolRenderer.Affiliation.NEUTRAL, NatoSymbolRenderer.UnitType.MEDICAL, null, "UN-01", "NEUTRAL");
        drawSmallText(img, "NEUTRAL (LUC LUONG TRUNG LAP / CIV)", p1X + 90, afY3 + 10, 0xFF10B981);
        drawSmallText(img, "Màu: Xanh Luc (#10B981 / #0A2E1C)", p1X + 90, afY3 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Khung: Hinh vuong (Square)", p1X + 90, afY3 + 38, 0xFF94A3B8);

        // 4. Unknown (Amber Quatrefoil)
        int afY4 = afY3 + 80;
        NatoSymbolRenderer.drawSymbol(img, p1X + 45, afY4 + 25, 36, NatoSymbolRenderer.Affiliation.UNKNOWN, NatoSymbolRenderer.UnitType.AIR_DEFENSE, null, "TRK-99", "UNKNOWN");
        drawSmallText(img, "UNKNOWN / PENDING (CHUA XAC DINH)", p1X + 90, afY4 + 10, 0xFFF59E0B);
        drawSmallText(img, "Màu: Vang Ho Phach (#F59E0B / #3D2808)", p1X + 90, afY4 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Khung: Co 4 la uon cong (Quatrefoil)", p1X + 90, afY4 + 38, 0xFF94A3B8);

        // Echelon Hierarchy Legend inside Left Panel
        int echBoxY = afY4 + 85;
        for (int px = p1X + 10; px < p1X + p1W - 10; px++) setPixel(img, px, echBoxY, 0xFF334155);
        drawSmallText(img, "2. ECHELON HIERARCHY (CAP BAC DON VI)", p1X + 15, echBoxY + 12, 0xFFFFFFFF);

        String[][] echelons = {
                {"[ Ø ]", "TEAM", "To Chien Dau / To 3-4 Nguoi"},
                {"[ ● ]", "SQUAD", "Tieu Doi (8 - 12 Chien Si)"},
                {"[ ●● ]", "SECTION", "Phan Doi / Khau Doi Hoa Luc"},
                {"[ ●●● ]", "PLATOON", "Trung Doi (3 - 4 Tieu Doi)"},
                {"[ | ]", "COMPANY / BTY", "Dai Doi / Khau Doi Phao"},
                {"[ || ]", "BATTALION", "Tieu Doan / Lien Doan"},
                {"[ ||| ]", "REGIMENT", "Trung Doan / Group"},
                {"[ X ]", "BRIGADE", "Lu Doan (BCT)"},
                {"[ XX ]", "DIVISION", "Su Doan Bo Binh / Thiet Giap"},
                {"[ XXX ]", "CORPS", "Quan Doan Tac Chien"}
        };

        for (int i = 0; i < echelons.length; i++) {
            int ey = echBoxY + 36 + i * 24;
            drawSmallText(img, echelons[i][0], p1X + 15, ey, 0xFF38BDF8);
            drawSmallText(img, echelons[i][1], p1X + 75, ey, 0xFFFFFFFF);
            drawSmallText(img, echelons[i][2], p1X + 195, ey, 0xFF94A3B8);
        }

        // Technical Rule Box at bottom
        int ruleY = echBoxY + 290;
        drawGlassPanel(img, p1X + 10, ruleY, p1W - 20, 160, 0xAA080B10, 0xFF475569);
        drawSmallText(img, "QUY TAC DONG BO THIET KE ASTRA C2:", p1X + 20, ruleY + 12, 0xFFF59E0B);
        drawSmallText(img, "- Toan bo icon tren map bat buoc theo", p1X + 20, ruleY + 32, 0xFFCBD5E1);
        drawSmallText(img, "  khung vector MIL-STD-2525D.", p1X + 20, ruleY + 46, 0xFFCBD5E1);
        drawSmallText(img, "- Mau Xanh (#3B82F6) cho Dong Minh.", p1X + 20, ruleY + 64, 0xFF93C5FD);
        drawSmallText(img, "- Mau Do (#EF4444) cho Muc Tieu Dich.", p1X + 20, ruleY + 78, 0xFFFCA5A5);
        drawSmallText(img, "- Khong tu y thay doi hinh dang khung.", p1X + 20, ruleY + 96, 0xFFCBD5E1);
        drawSmallText(img, "- Chu thich don vi luon dat phia duoi.", p1X + 20, ruleY + 110, 0xFFCBD5E1);
        drawSmallText(img, "- Cap bac don vi luon dat tren dau khung.", p1X + 20, ruleY + 124, 0xFFCBD5E1);

        // =========================================================================
        // SECTION 2: MASTER TABLE OF BRANCH & FUNCTION ICONS (MAIN AREA: X = 440 to 1570)
        // =========================================================================
        int p2X = 440, p2Y = 100, p2W = 1130, p2H = 870;
        drawGlassPanel(img, p2X, p2Y, p2W, p2H, 0xF00D111A, 0xFF2A3444);
        drawSectionHeader(img, p2X, p2Y, p2W, "3. MASTER BRANCH & COMBAT FUNCTION ICONS (BANG QUY CHUAN BINH CHUNG QUAN SU)");

        // Table Header
        int thY = p2Y + 36;
        for (int px = p2X; px < p2X + p2W; px++) {
            for (int py = thY; py < thY + 24; py++) setPixel(img, px, py, 0xFF161E2C);
        }
        for (int px = p2X; px < p2X + p2W; px++) setPixel(img, px, thY + 24, 0xFF3B82F6);

        drawSmallText(img, "BIEU TUONG (ICON)", p2X + 20, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "BINH CHUNG (BRANCH)", p2X + 160, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "Y NGHIA HINH HOC (GEOMETRY)", p2X + 380, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "UNG DUNG & TRANG BI THUC TE (APPLICATIONS)", p2X + 700, thY + 8, 0xFF38BDF8);

        // Table Content Rows
        record BranchRow(NatoSymbolRenderer.UnitType type, String name, String geom, String examples) {}
        BranchRow[] rows = {
                new BranchRow(NatoSymbolRenderer.UnitType.INFANTRY, "BO BINH (INFANTRY)", "Dau gach cheo X (Day deo dan cheo)", "Bo binh co dong, Tieu doan Bo binh Nhe"),
                new BranchRow(NatoSymbolRenderer.UnitType.ARMOR, "TANG THIET GIAP (ARMOR)", "Vong xich xe oval (Armor Track)", "Xe tang chu luc M1A2 Abrams, Leopard 2, T-90M"),
                new BranchRow(NatoSymbolRenderer.UnitType.FIELD_ARTILLERY, "PHAO BINH MAT DAT (FA)", "Diem tron dac (Dan phao Cannonball)", "Luu phao keo M777 155mm, D-30 122mm"),
                new BranchRow(NatoSymbolRenderer.UnitType.SP_ARTILLERY, "PHAO TU HANH (SPG)", "Vong xich Oval + Diem tron phao", "PZh 2000, M109A6 Paladin, 2S19 Msta-S, Archer"),
                new BranchRow(NatoSymbolRenderer.UnitType.ROCKET_ARTILLERY, "PHAO PHAN LUC (MLRS)", "Diem tron phao + Mui ten phong", "M142 HIMARS, M270 MLRS, BM-21 Grad, BM-30"),
                new BranchRow(NatoSymbolRenderer.UnitType.MORTAR, "SUNG COI (MORTAR)", "Diem tron + Nong coi dung thang", "Sung coi 120mm M120, 81mm L16, Coi tu hanh"),
                new BranchRow(NatoSymbolRenderer.UnitType.MECH_INFANTRY, "BO BINH CO GIOI (MECH)", "Vong xich Oval + Chu X bo binh", "Xe chien dau bo binh IFV (M2 Bradley, Puma, BMP-3)"),
                new BranchRow(NatoSymbolRenderer.UnitType.AIR_DEFENSE, "PHONG KHONG (AIR DEFENSE)", "Vom cung bao ve bau troi (Dome)", "MIM-104 Patriot, IRIS-T SLM, Tor-M2, Pantsir-S1"),
                new BranchRow(NatoSymbolRenderer.UnitType.AVIATION_ROTARY, "KHONG QUAN TRUC THANG (AV)", "Canh quat xoay hinh no (Bowtie)", "Truc thang chien dau AH-64 Apache, Ka-52, Mi-28"),
                new BranchRow(NatoSymbolRenderer.UnitType.RECONNAISSANCE, "TRINH SAT CO DONG (RECON)", "Duong vat cheo trinh sat (Slash)", "Phan doi trinh sat kieu Fennek, BRDM-2, Jackal"),
                new BranchRow(NatoSymbolRenderer.UnitType.ANTI_TANK, "CHONG TANG (ANTI-TANK)", "Ky hieu chu V nguoc (Inverted V)", "To doi ten lua Javelin, Spike, Kornet-EM, Stugna-P"),
                new BranchRow(NatoSymbolRenderer.UnitType.OBSERVATION_POST, "QUAN SAT TIEN DUYEN (FO)", "Tam giac dai quan sat + Tam mat", "Dai chi huy phao binh JFST, Forward Observer (FO)"),
                new BranchRow(NatoSymbolRenderer.UnitType.HEADQUARTERS, "SO CHI HUY (HQ / CP)", "Can co chi huy o goc duoi khung", "So chi huy Lu doan TOC, So chi huy Tieu doan CP"),
                new BranchRow(NatoSymbolRenderer.UnitType.ENGINEER, "CONG BINH (ENGINEER)", "Vom cau vuot / Thanh luy kien co", "Cong binh mo duong, Xe bac cau, Xe pha loi ABV"),
                new BranchRow(NatoSymbolRenderer.UnitType.SIGNAL, "THONG TIN LIEN LAC (SIG)", "Tia chop song vo tuyen (Bolt)", "Tram thu phat ve tinh C2, Xe tiep song chien thuat"),
                new BranchRow(NatoSymbolRenderer.UnitType.LOGISTICS, "HAU CAN TIEP VAN (LOG)", "Thanh ngang tiep van quan su", "Doan xe tai dan duoc, Tiep te xang dau nhien lieu"),
                new BranchRow(NatoSymbolRenderer.UnitType.MEDICAL, "QUAN Y CUU THUONG (MED)", "Chu thap Geneva Y te (+)", "Benh vien da chien, Xe cuu thuong bop the M113")
        };

        for (int i = 0; i < rows.length; i++) {
            BranchRow r = rows[i];
            int ry = thY + 30 + i * 46;

            // Zebra striping
            if (i % 2 == 0) {
                for (int px = p2X + 4; px < p2X + p2W - 4; px++) {
                    for (int py = ry - 4; py < ry + 38; py++) setPixel(img, px, py, 0xFF101520);
                }
            }

            // Draw Sample Symbol in Friendly Blue and Hostile Red
            NatoSymbolRenderer.drawSymbol(img, p2X + 45, ry + 16, 28, NatoSymbolRenderer.Affiliation.FRIENDLY, r.type, null, null, null);
            NatoSymbolRenderer.drawSymbol(img, p2X + 105, ry + 16, 28, NatoSymbolRenderer.Affiliation.HOSTILE, r.type, null, null, null);

            // Columns
            drawSmallText(img, r.name, p2X + 160, ry + 12, 0xFFFFFFFF);
            drawSmallText(img, r.geom, p2X + 380, ry + 12, 0xFFCBD5E1);
            drawSmallText(img, r.examples, p2X + 700, ry + 12, 0xFF94A3B8);

            // Row divider
            for (int px = p2X + 10; px < p2X + p2W - 10; px++) setPixel(img, px, ry + 40, 0xFF1C2433);
        }
    }

    private static void drawSectionHeader(NativeImage img, int x, int y, int w, String title) {
        for (int py = y; py < y + 30; py++) {
            for (int px = x; px < x + w; px++) setPixel(img, px, py, 0xFF141923);
        }
        for (int px = x; px < x + w; px++) setPixel(img, px, y + 30, 0xFF334155);
        drawSmallText(img, title, x + 12, y + 10, 0xFFFFFFFF);
    }

    private static void drawGlassPanel(NativeImage img, int x, int y, int w, int h, int fillCol, int borderCol) {
        for (int py = y; py < y + h; py++) {
            for (int px = x; px < x + w; px++) {
                if (px == x || px == x + w - 1 || py == y || py == y + h - 1) {
                    setPixel(img, px, py, borderCol);
                } else if (fillCol != 0) {
                    setPixel(img, px, py, fillCol);
                }
            }
        }
    }

    private static void drawTextScaled(NativeImage img, String text, int x, int y, int scale, int col) {
        String upper = text.toUpperCase();
        int curX = x;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = GLYPHS.getOrDefault(ch, GLYPHS.get(' '));
            for (int r = 0; r < 7; r++) {
                int row = glyph[r];
                for (int c = 0; c < 5; c++) {
                    if (((row >> (4 - c)) & 1) == 1) {
                        for (int dy = 0; dy < scale; dy++) {
                            for (int dx = 0; dx < scale; dx++) {
                                setPixel(img, curX + c * scale + dx, y + r * scale + dy, col);
                            }
                        }
                    }
                }
            }
            curX += (5 + 1) * scale;
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
        if (x < 0 || x >= POSTER_W || y < 0 || y >= POSTER_H) return;
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
        GLYPHS.put('.', new int[] { 0, 0, 0, 0, 0, 0b01100, 0b01100 });
        GLYPHS.put('#', new int[] { 0b01010, 0b11111, 0b01010, 0b01010, 0b11111, 0b01010, 0 });

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
