package net.nazarick.artillerytablet.mapcheck;

import com.mojang.blaze3d.platform.NativeImage;
import net.nazarick.artillerytablet.client.screen.NatoSymbolRenderer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Complete Master Specification & Reference Sheet for NATO MIL-STD-2525D / APP-6D Symbology.
 * Standalone High-Resolution Canvas (1920x1080 Full HD) with exhaustive branches, annotations,
 * echelon hierarchy, and operational tactical graphics in 100% standard NATO English.
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

    public static final int POSTER_W = 1920;
    public static final int POSTER_H = 1080;

    public static void run(Path out) throws Exception {
        Files.createDirectories(out);

        NativeImage img = new NativeImage(POSTER_W, POSTER_H, false);

        // Render the Complete Master Specification Poster
        renderMasterSpecPoster(img);

        // Write image files
        img.writeToFile(out.resolve("nato-symbols.png"));
        img.writeToFile(out.resolve("case.png"));
        System.out.printf("NATO Symbology Master Specification (English): OK -> %s and %s%n",
                out.resolve("nato-symbols.png"), out.resolve("case.png"));
    }

    private static void renderMasterSpecPoster(NativeImage img) {
        // 1. Dark Blueprint Tactical Canvas Background
        for (int y = 0; y < POSTER_H; y++) {
            for (int x = 0; x < POSTER_W; x++) {
                int col = 0xFF080A0E;
                if (x % 40 == 0 || y % 40 == 0) col = 0xFF0F131A;
                if (x % 200 == 0 || y % 200 == 0) col = 0xFF171E28;
                setPixel(img, x, y, col);
            }
        }

        // Master Outer Frame
        drawGlassPanel(img, 12, 12, POSTER_W - 24, POSTER_H - 24, 0x00000000, 0xFF334155);
        drawGlassPanel(img, 16, 16, POSTER_W - 32, POSTER_H - 32, 0x00000000, 0xFF1E293B);

        // 2. Main Header Banner
        for (int y = 22; y < 82; y++) {
            for (int x = 22; x < POSTER_W - 22; x++) {
                setPixel(img, x, y, 0xFF0F1522);
            }
        }
        for (int x = 22; x < POSTER_W - 22; x++) setPixel(img, x, 82, 0xFF3B82F6);

        drawTextScaled(img, "NATO MIL-STD-2525D / APP-6D TACTICAL SYMBOLOGY SPECIFICATION", 36, 34, 2, 0xFFFFFFFF);
        drawSmallText(img, "OFFICIAL REFERENCE MANUAL & MILITARY ASSET SPECIFICATION SHEET -- ASTRA FRONTLINE C2 & ARTILLERY FDC", 36, 62, 0xFF94A3B8);
        drawSmallText(img, "DOC-REF: ASTRA-SPEC-MIL2525D-MASTER-REV5 | BASELINE APPROVED", POSTER_W - 460, 62, 0xFF38BDF8);

        // =========================================================================
        // COLUMN 1: AFFILIATIONS & ECHELONS (X = 30 -> 440, W = 410)
        // =========================================================================
        int c1X = 30, c1Y = 96, c1W = 410, c1H = 960;
        drawGlassPanel(img, c1X, c1Y, c1W, c1H, 0xF00D111A, 0xFF2A3444);
        drawSectionHeader(img, c1X, c1Y, c1W, "1. STANDARD AFFILIATIONS & GEOMETRY");

        // 1. Friendly (Blue Box)
        int afY1 = c1Y + 42;
        NatoSymbolRenderer.drawSymbol(img, c1X + 45, afY1 + 25, 34, NatoSymbolRenderer.Affiliation.FRIENDLY, NatoSymbolRenderer.UnitType.INFANTRY, NatoSymbolRenderer.Echelon.PLATOON, "1/A/1-22", "FRIENDLY");
        drawSmallText(img, "FRIENDLY (OWN & ALLIED FORCES)", c1X + 90, afY1 + 10, 0xFF3B82F6);
        drawSmallText(img, "Color: Tactical Blue (#3B82F6 / #0E294B)", c1X + 90, afY1 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Frame: Solid Rounded Rectangle Box", c1X + 90, afY1 + 38, 0xFF94A3B8);

        // 2. Hostile (Red Diamond)
        int afY2 = afY1 + 76;
        NatoSymbolRenderer.drawSymbol(img, c1X + 45, afY2 + 25, 34, NatoSymbolRenderer.Affiliation.HOSTILE, NatoSymbolRenderer.UnitType.ARMOR, NatoSymbolRenderer.Echelon.COMPANY, "TGT-01", "HOSTILE");
        drawSmallText(img, "HOSTILE (ENEMY FORCES - OPFOR)", c1X + 90, afY2 + 10, 0xFFEF4444);
        drawSmallText(img, "Color: Crimson Red (#EF4444 / #3B0D0D)", c1X + 90, afY2 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Frame: Solid Diamond Frame", c1X + 90, afY2 + 38, 0xFF94A3B8);

        // 3. Neutral (Green Square)
        int afY3 = afY2 + 76;
        NatoSymbolRenderer.drawSymbol(img, c1X + 45, afY3 + 25, 34, NatoSymbolRenderer.Affiliation.NEUTRAL, NatoSymbolRenderer.UnitType.MEDICAL, null, "UN-01", "NEUTRAL");
        drawSmallText(img, "NEUTRAL (NON-ALIGNED / CIVILIAN / UN)", c1X + 90, afY3 + 10, 0xFF10B981);
        drawSmallText(img, "Color: Emerald Green (#10B981 / #0A2E1C)", c1X + 90, afY3 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Frame: Solid Square Frame", c1X + 90, afY3 + 38, 0xFF94A3B8);

        // 4. Unknown (Amber Quatrefoil)
        int afY4 = afY3 + 76;
        NatoSymbolRenderer.drawSymbol(img, c1X + 45, afY4 + 25, 34, NatoSymbolRenderer.Affiliation.UNKNOWN, NatoSymbolRenderer.UnitType.AIR_DEFENSE, null, "TRK-99", "UNKNOWN");
        drawSmallText(img, "UNKNOWN / PENDING (UNIDENTIFIED TRACK)", c1X + 90, afY4 + 10, 0xFFF59E0B);
        drawSmallText(img, "Color: Tactical Amber (#F59E0B / #3D2808)", c1X + 90, afY4 + 24, 0xFFCBD5E1);
        drawSmallText(img, "Frame: Quatrefoil / Clover Frame", c1X + 90, afY4 + 38, 0xFF94A3B8);

        // Echelon Hierarchy Legend inside Column 1
        int echBoxY = afY4 + 82;
        for (int px = c1X + 10; px < c1X + c1W - 10; px++) setPixel(img, px, echBoxY, 0xFF334155);
        drawSmallText(img, "2. ECHELON HIERARCHY (COMMAND SIZES)", c1X + 15, echBoxY + 12, 0xFFFFFFFF);

        String[][] echelons = {
                {"[ Ø ]", "TEAM", "Fireteam / Combat Team (3-4 Soldiers)"},
                {"[ ● ]", "SQUAD", "Squad / Crew (8 - 12 Soldiers / 1 IFV)"},
                {"[ ●● ]", "SECTION", "Section / Heavy Weapons Team (2 Squads)"},
                {"[ ●●● ]", "PLATOON", "Platoon (3 - 4 Squads / 4 Tanks)"},
                {"[ | ]", "COMPANY / BTY", "Company / Artillery Battery (6-8 Guns)"},
                {"[ || ]", "BATTALION", "Battalion / Cavalry Squadron (300-800)"},
                {"[ ||| ]", "REGIMENT", "Regiment / Tactical Group"},
                {"[ X ]", "BRIGADE", "Brigade Combat Team (BCT / 3,000-5,000)"},
                {"[ XX ]", "DIVISION", "Division (10,000 - 15,000 Soldiers)"},
                {"[ XXX ]", "CORPS", "Field Corps (2 - 4 Combat Divisions)"},
                {"[ XXXX ]", "ARMY", "Field Army (2 - 4 Field Corps)"},
                {"[ XXXXX ]", "ARMY GROUP", "Army Group / Front Level Command"}
        };

        for (int i = 0; i < echelons.length; i++) {
            int ey = echBoxY + 34 + i * 23;
            drawSmallText(img, echelons[i][0], c1X + 15, ey, 0xFF38BDF8);
            drawSmallText(img, echelons[i][1], c1X + 80, ey, 0xFFFFFFFF);
            drawSmallText(img, echelons[i][2], c1X + 195, ey, 0xFF94A3B8);
        }

        // Operational Design Rules
        int ruleY = echBoxY + 325;
        drawGlassPanel(img, c1X + 10, ruleY, c1W - 20, 205, 0xAA080B10, 0xFF475569);
        drawSmallText(img, "ASTRA C2 INTERFACE DESIGN RULES:", c1X + 20, ruleY + 12, 0xFFF59E0B);
        drawSmallText(img, "1. All map icons must strictly follow", c1X + 20, ruleY + 32, 0xFFCBD5E1);
        drawSmallText(img, "   MIL-STD-2525D vector geometry.", c1X + 20, ruleY + 46, 0xFFCBD5E1);
        drawSmallText(img, "2. Blue (#3B82F6) is reserved for Friendly.", c1X + 20, ruleY + 64, 0xFF93C5FD);
        drawSmallText(img, "3. Red (#EF4444) is reserved for Hostile.", c1X + 20, ruleY + 78, 0xFFFCA5A5);
        drawSmallText(img, "4. Frame aspect ratios must remain fixed.", c1X + 20, ruleY + 96, 0xFFCBD5E1);
        drawSmallText(img, "5. Echelon markers are always at top center.", c1X + 20, ruleY + 110, 0xFFCBD5E1);
        drawSmallText(img, "6. Unit designation is always at bottom.", c1X + 20, ruleY + 124, 0xFFCBD5E1);
        drawSmallText(img, "7. Minimum tactical icon size is 24x24 px.", c1X + 20, ruleY + 138, 0xFFCBD5E1);
        drawSmallText(img, "8. Vector borders rendered sharp for OLED.", c1X + 20, ruleY + 152, 0xFFCBD5E1);
        drawSmallText(img, "9. Grid coordinates synchronized with MGRS.", c1X + 20, ruleY + 166, 0xFFCBD5E1);
        drawSmallText(img, "10. This baseline is permanently hard locked.", c1X + 20, ruleY + 180, 0xFF10B981);

        // =========================================================================
        // COLUMN 2: COMBAT ARMS & FIRE SUPPORT BRANCHES (X = 460 -> 1460, W = 1000)
        // =========================================================================
        int c2X = 460, c2Y = 96, c2W = 1000, c2H = 960;
        drawGlassPanel(img, c2X, c2Y, c2W, c2H, 0xF00D111A, 0xFF2A3444);
        drawSectionHeader(img, c2X, c2Y, c2W, "3. MASTER BRANCH & COMBAT FUNCTION ICONS (MIL-STD-2525D)");

        // Table Header
        int thY = c2Y + 34;
        for (int px = c2X; px < c2X + c2W; px++) {
            for (int py = thY; py < thY + 24; py++) setPixel(img, px, py, 0xFF161E2C);
        }
        for (int px = c2X; px < c2X + c2W; px++) setPixel(img, px, thY + 24, 0xFF3B82F6);

        drawSmallText(img, "ICON (FRIEND/FOE)", c2X + 18, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "MILITARY BRANCH", c2X + 150, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "GEOMETRIC SPECIFICATION", c2X + 370, thY + 8, 0xFF38BDF8);
        drawSmallText(img, "OPERATIONAL EXAMPLES & PLATFORMS", c2X + 650, thY + 8, 0xFF38BDF8);

        record BranchRow(NatoSymbolRenderer.UnitType type, String name, String geom, String examples) {}
        BranchRow[] rows = {
                // Ground Combat
                new BranchRow(NatoSymbolRenderer.UnitType.INFANTRY, "INFANTRY (IN)", "Diagonal Cross (X) - Cartridge Belts", "Light Infantry, Airborne, Marine Battalions"),
                new BranchRow(NatoSymbolRenderer.UnitType.MOTORIZED_INFANTRY, "MOTORIZED INFANTRY (MOT)", "Diagonal Cross (X) + Wheels (O O)", "Wheeled Light Armor (Humvee, JLTV, Mastiff)"),
                new BranchRow(NatoSymbolRenderer.UnitType.MECH_INFANTRY, "MECHANIZED INFANTRY (MECH)", "Armor Track Oval + Infantry Cross (X)", "Infantry Fighting Vehicles (Bradley, Puma, BMP-3)"),
                new BranchRow(NatoSymbolRenderer.UnitType.ARMOR, "ARMOR / TANK (AR)", "Horizontal Capsule Track Oval (Track)", "Main Battle Tanks (M1A2 Abrams, Leopard 2, T-90M)"),
                new BranchRow(NatoSymbolRenderer.UnitType.RECONNAISSANCE, "RECONNAISSANCE / CAVALRY", "Single Forward Diagonal Slash (/)", "Reconnaissance Patrols (Fennek, BRDM-2, Jackal)"),
                new BranchRow(NatoSymbolRenderer.UnitType.ARMORED_RECON, "ARMORED RECON (ARM CAV)", "Armor Track Oval + Diagonal Slash (/)", "Cavalry Fighting Vehicles (M3A3 CFV, SpPz Luchs)"),
                new BranchRow(NatoSymbolRenderer.UnitType.ANTI_TANK, "ANTI-TANK (AT)", "Inverted Arrowhead (Inverted V)", "Anti-Tank Guided Missile Teams (Javelin, Kornet)"),
                new BranchRow(NatoSymbolRenderer.UnitType.ARMORED_ANTI_TANK, "SP ANTI-TANK (TANK DESTROYER)", "Armor Track Oval + Inverted V", "Tank Destroyer Platforms (Stryker TOW, Khrizantema)"),
                new BranchRow(NatoSymbolRenderer.UnitType.SPECIAL_FORCES, "SPECIAL OPERATIONS (SOF)", "Special Operations Modifier (SF)", "Special Forces (SAS, US Green Berets, KSK, Delta)"),
                new BranchRow(NatoSymbolRenderer.UnitType.SNIPER, "SNIPER / MARKSMAN (SNP)", "Crosshair Reticle with Center Point", "Long-Range Sniper Teams, Anti-Materiel Rifles"),

                // Artillery & Fire Support
                new BranchRow(NatoSymbolRenderer.UnitType.FIELD_ARTILLERY, "FIELD ARTILLERY (FA)", "Solid Filled Circle (Cannonball Dot)", "Towed Howitzers (M777 155mm, D-30 122mm, FH-70)"),
                new BranchRow(NatoSymbolRenderer.UnitType.SP_ARTILLERY, "SP HOWITZER (SPG)", "Armor Track Oval + Cannonball Dot", "Self-Propelled Guns (PZh 2000, M109A6, 2S19, Archer)"),
                new BranchRow(NatoSymbolRenderer.UnitType.ROCKET_ARTILLERY, "ROCKET ARTILLERY (MLRS)", "Cannonball Dot + Upward Rocket Arrow", "Precision Rockets (M142 HIMARS, M270, BM-21 Grad)"),
                new BranchRow(NatoSymbolRenderer.UnitType.MORTAR, "MORTAR SYSTEMS (MTR)", "Cannonball Dot + Upward Mortar Tube", "Infantry Mortars (M120 120mm, L16 81mm, 2B11)"),
                new BranchRow(NatoSymbolRenderer.UnitType.SP_MORTAR, "SP MORTAR CARRIER", "Armor Track Oval + Mortar Tube", "Armored Mortar Carriers (Stryker MCV, M1064A3)"),
                new BranchRow(NatoSymbolRenderer.UnitType.OBSERVATION_POST, "FORWARD OBSERVER (FO / JFST)", "Observation Triangle + Target Eye", "Forward Fire Support Teams, Joint Fires Observers"),
                new BranchRow(NatoSymbolRenderer.UnitType.RADAR_ARTILLERY, "COUNTER-BATTERY RADAR", "Artillery Dot + Radar Emitting Wave", "Counter-Battery Systems (AN/TPQ-53, COBRA Radar)"),

                // Air & Air Defense
                new BranchRow(NatoSymbolRenderer.UnitType.AIR_DEFENSE, "AIR DEFENSE GUN (SPAAG)", "Dome Arc (Sky Protection Dome)", "Self-Propelled AA Guns (Gepard, Tunguska, Shilka)"),
                new BranchRow(NatoSymbolRenderer.UnitType.AIR_DEFENSE_MISSILE, "AIR DEFENSE MISSILE (SAM)", "Dome Arc + Surface-to-Air Arrow", "SAM Batteries (MIM-104 Patriot, IRIS-T, Tor-M2)"),
                new BranchRow(NatoSymbolRenderer.UnitType.AVIATION_ROTARY, "ATTACK HELICOPTER (ROTARY)", "Rotary Wing Bowtie Symbol (Bowtie)", "Attack & Recon Helicopters (AH-64 Apache, Ka-52)"),
                new BranchRow(NatoSymbolRenderer.UnitType.AVIATION_FIXED_WING, "COMBAT AIRCRAFT (FIXED-WING)", "Fixed-Wing Jet Silhouette", "Fighter Aircraft (F-35 Lightning II, F-16V, Su-35)"),
                new BranchRow(NatoSymbolRenderer.UnitType.UAV, "UNMANNED AERIAL VEHICLE", "Delta-Wing Drone Silhouette", "Recon & Strike Drones (MQ-9 Reaper, Bayraktar TB2)")
        };

        for (int i = 0; i < rows.length; i++) {
            BranchRow r = rows[i];
            int ry = thY + 28 + i * 41;

            if (i % 2 == 0) {
                for (int px = c2X + 4; px < c2X + c2W - 4; px++) {
                    for (int py = ry - 3; py < ry + 36; py++) setPixel(img, px, py, 0xFF101520);
                }
            }

            NatoSymbolRenderer.drawSymbol(img, c2X + 40, ry + 15, 26, NatoSymbolRenderer.Affiliation.FRIENDLY, r.type, null, null, null);
            NatoSymbolRenderer.drawSymbol(img, c2X + 95, ry + 15, 26, NatoSymbolRenderer.Affiliation.HOSTILE, r.type, null, null, null);

            drawSmallText(img, r.name, c2X + 150, ry + 11, 0xFFFFFFFF);
            drawSmallText(img, r.geom, c2X + 370, ry + 11, 0xFFCBD5E1);
            drawSmallText(img, r.examples, c2X + 650, ry + 11, 0xFF94A3B8);

            for (int px = c2X + 8; px < c2X + c2W - 8; px++) setPixel(img, px, ry + 37, 0xFF1C2433);
        }

        // =========================================================================
        // COLUMN 3: COMBAT SERVICE SUPPORT & TACTICAL GRAPHICS (X = 1480 -> 1890, W = 410)
        // =========================================================================
        int c3X = 1480, c3Y = 96, c3W = 410, c3H = 960;
        drawGlassPanel(img, c3X, c3Y, c3W, c3H, 0xF00D111A, 0xFF2A3444);
        drawSectionHeader(img, c3X, c3Y, c3W, "4. SERVICE SUPPORT & TACTICAL GRAPHICS");

        // CSS Items Table
        BranchRow[] cssRows = {
                new BranchRow(NatoSymbolRenderer.UnitType.HEADQUARTERS, "HEADQUARTERS (HQ / CP)", "Staff Flagpole on Frame Corner", "Brigade TOC, Battalion Command Post"),
                new BranchRow(NatoSymbolRenderer.UnitType.ENGINEER, "COMBAT ENGINEER (EN)", "Sapper Arch / Fortification Bridge", "Breaching Engineers, Armored Bridgelayers"),
                new BranchRow(NatoSymbolRenderer.UnitType.ARMORED_ENGINEER, "ARMORED ENGINEER (AVRE)", "Armor Track Oval + Sapper Arch", "Assault Breacher Vehicles (ABV, Wisent 2)"),
                new BranchRow(NatoSymbolRenderer.UnitType.SIGNAL, "SIGNAL COMMUNICATIONS (SIG)", "Radio Transmission Lightning Bolt", "C2 Satellite Terminals, Relay Vehicles"),
                new BranchRow(NatoSymbolRenderer.UnitType.ELECTRONIC_WARFARE, "ELECTRONIC WARFARE (EW)", "Special Electronic Warfare (EW)", "Jamming Platforms, SIGINT Intercept Stations"),
                new BranchRow(NatoSymbolRenderer.UnitType.CBRN, "CBRN DEFENSE (NBC)", "CBRN Defense Modifier (NBC)", "NBC Reconnaissance, Decontamination Units"),
                new BranchRow(NatoSymbolRenderer.UnitType.LOGISTICS, "LOGISTICS SUPPLY (LOG)", "Crossbeam Horizontal Bar", "General Cargo Logistics, Forward Supply Depot"),
                new BranchRow(NatoSymbolRenderer.UnitType.AMMO_SUPPLY, "AMMUNITION POINT (ASP)", "Artillery Ammunition Shell Icon", "Ammunition Supply Point, Reload Vehicles"),
                new BranchRow(NatoSymbolRenderer.UnitType.FUEL_SUPPLY, "PETROLEUM SUPPLY (POL)", "Special Fuel Modifier (POL)", "Forward Arming & Refueling Point (FARP)"),
                new BranchRow(NatoSymbolRenderer.UnitType.MAINTENANCE, "MAINTENANCE & RECOVERY", "Mechanic Wrench Symbol (MTR)", "Armored Recovery Vehicles (Bergepanzer)"),
                new BranchRow(NatoSymbolRenderer.UnitType.MEDICAL, "MEDICAL TREATMENT (MED)", "Geneva Red Cross Symbol (+)", "Field Hospitals, Armored Ambulances"),
                new BranchRow(NatoSymbolRenderer.UnitType.MILITARY_POLICE, "MILITARY POLICE (MP)", "Military Police Modifier (MP)", "Route Control, Provost Marshal, Security")
        };

        int cssStartY = c3Y + 38;
        for (int i = 0; i < cssRows.length; i++) {
            BranchRow r = cssRows[i];
            int ry = cssStartY + i * 36;

            NatoSymbolRenderer.drawSymbol(img, c3X + 30, ry + 12, 22, NatoSymbolRenderer.Affiliation.FRIENDLY, r.type, null, null, null);
            drawSmallText(img, r.name, c3X + 55, ry + 6, 0xFFFFFFFF);
            drawSmallText(img, r.examples, c3X + 55, ry + 18, 0xFF94A3B8);

            for (int px = c3X + 10; px < c3X + c3W - 10; px++) setPixel(img, px, ry + 32, 0xFF1C2433);
        }

        // Tactical Operational Control Graphics Section
        int togY = cssStartY + cssRows.length * 36 + 10;
        for (int px = c3X + 10; px < c3X + c3W - 10; px++) setPixel(img, px, togY, 0xFF334155);
        drawSmallText(img, "5. TACTICAL CONTROL GRAPHICS (OVERLAYS)", c3X + 15, togY + 10, 0xFF38BDF8);

        int gy = togY + 28;
        // 1. Boundary Line
        drawSmallText(img, "TACTICAL BOUNDARY LINE:", c3X + 15, gy, 0xFFCBD5E1);
        for (int px = c3X + 15; px < c3X + 220; px++) {
            if ((px / 6) % 2 == 0) setPixel(img, px, gy + 14, 0xFF3B82F6);
        }
        drawSmallText(img, "— || — (BATTALION BOUNDARY)", c3X + 230, gy + 10, 0xFF38BDF8);

        // 2. Phase Line
        gy += 32;
        drawSmallText(img, "OPERATIONAL PHASE LINE:", c3X + 15, gy, 0xFFCBD5E1);
        for (int px = c3X + 15; px < c3X + 220; px++) setPixel(img, px, gy + 14, 0xFF10B981);
        drawSmallText(img, "PL OAK (CONTROL LINE)", c3X + 230, gy + 10, 0xFF10B981);

        // 3. Gun-Target Line (GTL)
        gy += 32;
        drawSmallText(img, "GUN-TARGET LINE (GTL):", c3X + 15, gy, 0xFFCBD5E1);
        for (int px = c3X + 15; px < c3X + 220; px++) {
            if ((px / 4) % 2 == 0) setPixel(img, px, gy + 14, 0xFFF59E0B);
        }
        drawSmallText(img, "GTL AZ: 052.4° (DASHED)", c3X + 230, gy + 10, 0xFFF59E0B);

        // 4. Target Reference Point (TRP)
        gy += 32;
        drawSmallText(img, "TARGET REFERENCE POINT (TRP):", c3X + 15, gy, 0xFFCBD5E1);
        int trX = c3X + 110, trY = gy + 14;
        for (int px = trX - 10; px <= trX + 10; px++) setPixel(img, px, trY, 0xFFEF4444);
        for (int py = trY - 10; py <= trY + 10; py++) setPixel(img, trX, py, 0xFFEF4444);
        drawSmallText(img, "TRP-001 (POINT TARGET)", c3X + 230, gy + 10, 0xFFEF4444);

        // 5. CEP 90 Dispersion Ellipse
        gy += 32;
        drawSmallText(img, "DISPERSION ELLIPSE (CEP 90):", c3X + 15, gy, 0xFFCBD5E1);
        for (int deg = 0; deg < 360; deg += 15) {
            double rad = Math.toRadians(deg);
            setPixel(img, c3X + 110 + (int)(Math.cos(rad) * 20), gy + 14 + (int)(Math.sin(rad) * 8), 0xFFFF0044);
        }
        drawSmallText(img, "DISPERSION ZONE", c3X + 230, gy + 10, 0xFFFF0044);

        // 6. Sensor Cone (LOS FOV)
        gy += 32;
        drawSmallText(img, "RADAR / LOS SENSOR CONE:", c3X + 15, gy, 0xFFCBD5E1);
        drawSmallText(img, "FOV 35° SECTOR (TRANSLUCENT)", c3X + 230, gy + 10, 0xFFFFFFFF);
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
        GLYPHS.put('—', new int[] { 0, 0, 0, 0b11111, 0, 0, 0 });
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
        GLYPHS.put('°', new int[] { 0b01100, 0b10010, 0b10010, 0b01100, 0, 0, 0 });
        GLYPHS.put(',', new int[] { 0, 0, 0, 0, 0b01100, 0b01100, 0b01000 });

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
        GLYPHS.put('N', new int[] { 0b10001, 0b11001, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001 });
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
