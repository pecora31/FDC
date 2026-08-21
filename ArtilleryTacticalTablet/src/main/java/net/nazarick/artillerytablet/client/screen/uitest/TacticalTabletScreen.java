package net.nazarick.artillerytablet.client.screen.uitest;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Tactical Tablet GUI — Leonardo DRS MRT-104II Pure Java Renderer
 * Version: Minecraft Forge 1.20.1
 *
 * 100% PURE JAVA CODE — NO PNG TEXTURE REQUIRED!
 * Renders tactical gunmetal chassis, 4:3 screen well, heatsinks, screws,
 * ash-grey keys, red CFF/PWR keys, and dynamic LED status indicators.
 */
public class TacticalTabletScreen extends Screen {

    // =========================================================================
    // 1. TACTICAL COLOR PALETTE (0xAARRGGBB)
    // =========================================================================
    // Chassis & Metal Frame (Tactic Xám Đen)
    private static final int COL_BG_TINT             = 0x99000000; // Nền mờ phía sau tablet
    private static final int COL_CHASSIS_SHADOW      = 0xFF0E1013;
    private static final int COL_CHASSIS_DARK        = 0xFF181A1F;
    private static final int COL_CHASSIS_BASE        = 0xFF24272F; // Nền chính máy
    private static final int COL_CHASSIS_MID         = 0xFF2F343E;
    private static final int COL_CHASSIS_LIGHT       = 0xFF434A58;
    private static final int COL_CHASSIS_HIGHLIGHT   = 0xFF5D6679; // Viền phay kim loại

    // Heatsink Cooling Fins (Rãnh tản nhiệt)
    private static final int COL_FIN_DARK            = 0xFF121417;
    private static final int COL_FIN_BASE            = 0xFF1F2228;
    private static final int COL_FIN_LIGHT           = 0xFF3E4450;

    // Screws & Bolts
    private static final int COL_BOLT_HOLE           = 0xFF0A0B0D;
    private static final int COL_BOLT_DARK           = 0xFF252830;
    private static final int COL_BOLT_LIGHT          = 0xFF7A8496;
    private static final int COL_BOLT_HEX            = 0xFF14161A;

    // Screen Well (Viền hốc màn hình 4:3)
    private static final int COL_RECESS_DEEP         = 0xFF0D0E10;
    private static final int COL_RECESS_BASE         = 0xFF191B20;
    private static final int COL_RECESS_BEVEL        = 0xFF353B47;

    // Ash Grey Buttons (Nút xám tro)
    private static final int COL_SLOT_DEEP           = 0xFF101215;
    private static final int COL_SLOT_EDGE           = 0xFF2C323D;
    private static final int COL_BTN_ACCENT_YELLOW   = 0xFFB89628; // Viền chỉ thị vàng DRS
    private static final int COL_BTN_BASE            = 0xFF3A3E47;
    private static final int COL_BTN_FACE            = 0xFF4E5460;
    private static final int COL_BTN_HOVER           = 0xFF5E6573;
    private static final int COL_BTN_LIGHT           = 0xFF70798A;
    private static final int COL_BTN_DARK            = 0xFF22252B;
    private static final int COL_BTN_TEXT            = 0xFFEFF3F8;

    // Red Buttons: CFF (BẮN) & POWER (NGUỒN)
    private static final int COL_RED_ACCENT          = 0xFFD84A4A;
    private static final int COL_RED_BASE            = 0xFFA81C1C;
    private static final int COL_RED_FACE            = 0xFFCC2929;
    private static final int COL_RED_HOVER           = 0xFFE03838;
    private static final int COL_RED_LIGHT           = 0xFFF85858;
    private static final int COL_RED_DARK            = 0xFF6B0E0E;
    private static final int COL_RED_TEXT            = 0xFFFFFFFF;

    // LED Status Indicators
    private static final int COL_LED_HOLE            = 0xFF0A0B0D;
    private static final int COL_LED_BORDER          = 0xFF343A45;
    private static final int COL_LED_OFF             = 0xFF1A1C20;
    private static final int COL_LED_GREEN_ON        = 0xFF00FF66;
    private static final int COL_LED_RED_ON          = 0xFFFF3333;
    private static final int COL_LED_AMBER_ON        = 0xFFFFB300;

    // Stencil Text
    private static final int COL_STENCIL             = 0xFF656F82;

    // =========================================================================
    // 2. VIRTUAL DESIGN RESOLUTION (4:3 RATIO)
    // =========================================================================
    private static final int DESIGN_W = 800;  // Virtual design width
    private static final int DESIGN_H = 600;  // Virtual design height (4:3)

    // Virtual Screen Cutout 4:3: 560 x 420 (exactly 4:3)
    private static final int SCR_W = 560;
    private static final int SCR_H = 420;
    private static final int SCR_X = (DESIGN_W - SCR_W) / 2; // 120
    private static final int SCR_Y = (DESIGN_H - SCR_H) / 2; // 90

    // Runtime Calculated Bounds
    private int leftPos, topPos, frameWidth, frameHeight;
    private float scale;

    // Interactive Buttons List
    private final List<TacticalKey> keys = new ArrayList<>();
    private TacticalKey hoveredKey = null;

    public TacticalTabletScreen() {
        super(Component.literal("Artillery Tactical Tablet"));
    }

    @Override
    protected void init() {
        super.init();

        // Fit 4:3 tablet into player's screen with 5% margin
        float scaleX = (float) this.width / DESIGN_W;
        float scaleY = (float) this.height / DESIGN_H;
        this.scale = Math.min(scaleX, scaleY) * 0.94f;

        this.frameWidth = Math.round(DESIGN_W * scale);
        this.frameHeight = Math.round(DESIGN_H * scale);
        this.leftPos = (this.width - this.frameWidth) / 2;
        this.topPos = (this.height - this.frameHeight) / 2;

        // Initialize 32 Tactical Keys
        buildKeyLayout();
    }

    // =========================================================================
    // 3. KEY LAYOUT DEFINITION (32 KEYS)
    // =========================================================================
    private void buildKeyLayout() {
        keys.clear();

        // 1. TOP ROW (10 Keys): GRID, BTY, TGT, AMO, STA, LOG, F17, F18, BRT-, BRT+
        String[] topLabels = {"GRID", "BTY", "TGT", "AMO", "STA", "LOG", "F17", "F18", "BRT-", "BRT+"};
        int topStartX = 148;
        int topSpacingX = 56;
        int topY = 56;
        for (int i = 0; i < topLabels.length; i++) {
            keys.add(new TacticalKey(
                topLabels[i], topLabels[i],
                topStartX + i * topSpacingX, topY, 40, 24,
                false, KeyType.TOP_ROW, LedPosition.BOTTOM
            ));
        }

        // 2. LEFT COLUMN (6 Keys): CFF (RED BẮN), ADJ, MOD, ARC, F1, F2
        String[] leftLabels = {"CFF", "ADJ", "MOD", "ARC", "F1", "F2"};
        int leftX = 60;
        int leftStartY = 140;
        int leftSpacingY = 62;
        for (int i = 0; i < leftLabels.length; i++) {
            boolean isCff = i == 0; // CFF is RED
            keys.add(new TacticalKey(
                leftLabels[i], leftLabels[i],
                leftX, leftStartY + i * leftSpacingY, 42, 30,
                isCff, KeyType.LEFT_COL, LedPosition.RIGHT
            ));
        }

        // 3. RIGHT COLUMN (6 Keys): F3, F4, F5, F6, F7, F8
        String[] rightLabels = {"F3", "F4", "F5", "F6", "F7", "F8"};
        int rightX = DESIGN_W - 60;
        int rightStartY = 140;
        int rightSpacingY = 62;
        for (int i = 0; i < rightLabels.length; i++) {
            keys.add(new TacticalKey(
                rightLabels[i], rightLabels[i],
                rightX, rightStartY + i * rightSpacingY, 42, 30,
                false, KeyType.RIGHT_COL, LedPosition.LEFT
            ));
        }

        // 4. BOTTOM ROW (10 Keys): FLT, F9, F10, F11, F12, F13, F14, F15, F16, PWR (RED NGUỒN)
        String[] botLabels = {"FLT", "F9", "F10", "F11", "F12", "F13", "F14", "F15", "F16", "PWR"};
        int botStartX = 148;
        int botSpacingX = 56;
        int botY = DESIGN_H - 56;
        for (int i = 0; i < botLabels.length; i++) {
            boolean isPwr = i == botLabels.length - 1; // PWR is RED
            keys.add(new TacticalKey(
                botLabels[i], botLabels[i],
                botStartX + i * botSpacingX, botY, 40, 24,
                isPwr, KeyType.BOT_ROW, LedPosition.TOP
            ));
        }
    }

    // =========================================================================
    // 4. MASTER RENDER PIPELINE
    // =========================================================================
    @Override
    public void render(@NotNull GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        // Dim background outside tablet
        this.renderBackground(g);
        g.fill(0, 0, this.width, this.height, COL_BG_TINT);

        // Update hovered key
        updateHoverState(mouseX, mouseY);

        // 1. Draw Master Chassis Body & Heatsinks
        renderChassis(g);

        // 2. Draw Artillery Map / Radar Layer (in the 4:3 screen well)
        renderArtilleryMap(g, toScreenX(SCR_X), toScreenY(SCR_Y), toScreenW(SCR_W), toScreenH(SCR_H));

        // 3. Draw Screen Bezel Frame (Inner recessed frame)
        renderScreenBezel(g);

        // 4. Draw Stencils & Technical Markings
        renderStencils(g);

        // 5. Draw 32 Tactical Buttons & Status LEDs
        renderAllButtons(g);

        super.render(g, mouseX, mouseY, partialTick);
    }

    // =========================================================================
    // 5. CHASSIS & HARDWARE DRAWING (PURE JAVA)
    // =========================================================================
    private void renderChassis(GuiGraphics g) {
        int x1 = leftPos;
        int y1 = topPos;
        int x2 = leftPos + frameWidth;
        int y2 = topPos + frameHeight;

        // Outer Shadow & Base Frame
        fillChamfer(g, x1, y1, frameWidth, frameHeight, Math.round(14 * scale), COL_CHASSIS_SHADOW);
        fillChamfer(g, x1 + 2, y1 + 2, frameWidth - 4, frameHeight - 4, Math.round(12 * scale), COL_CHASSIS_DARK);
        fillChamfer(g, x1 + 4, y1 + 4, frameWidth - 8, frameHeight - 8, Math.round(10 * scale), COL_CHASSIS_BASE);

        // Top & Left Metallic Bevel Highlight
        g.fill(x1 + toScreenW(24), y1 + 4, x2 - toScreenW(24), y1 + 6, COL_CHASSIS_HIGHLIGHT);
        g.fill(x1 + 4, y1 + toScreenH(24), x1 + 6, y2 - toScreenH(24), COL_CHASSIS_HIGHLIGHT);

        // Bottom & Right Deep Drop Shadow
        g.fill(x1 + toScreenW(24), y2 - 6, x2 - toScreenW(24), y2 - 4, COL_CHASSIS_SHADOW);
        g.fill(x2 - 6, y1 + toScreenH(24), x2 - 4, y2 - toScreenH(24), COL_CHASSIS_SHADOW);

        // Top & Bottom Heatsink Cooling Fins (DRS MRT-104II)
        renderHeatsink(g, 160, 12, 480, 10);
        renderHeatsink(g, 160, DESIGN_H - 22, 480, 10);

        // Left Side Dust Caps (3 Military Connectors)
        renderSideDustCap(g, 6, 210, 8);
        renderSideDustCap(g, 6, 300, 8);
        renderSideDustCap(g, 6, 390, 8);

        // Perimeter Bezel Screws
        int[] screwXs = {90, 180, 290, 400, 510, 620, 710};
        for (int sx : screwXs) {
            renderScrew(g, sx, 20);
            renderScrew(g, sx, DESIGN_H - 20);
        }

        // Speaker / Mic Grille at Top-Right
        renderSpeakerGrille(g, DESIGN_W - 35, 28);
    }

    private void renderHeatsink(GuiGraphics g, int vx, int vy, int vw, int vh) {
        int x = toScreenX(vx);
        int y = toScreenY(vy);
        int w = toScreenW(vw);
        int h = toScreenH(vh);

        g.fill(x, y, x + w, y + h, COL_FIN_DARK);
        int finW = Math.max(1, Math.round(3 * scale));
        int gap = Math.max(2, Math.round(4 * scale));

        for (int curX = x + 2; curX < x + w - 4; curX += finW + gap) {
            g.fill(curX, y, curX + finW, y + h, COL_FIN_BASE);
            g.fill(curX, y, curX + 1, y + h, COL_FIN_LIGHT);
            g.fill(curX + finW - 1, y, curX + finW, y + h, COL_FIN_DARK);
        }
    }

    private void renderSideDustCap(GuiGraphics g, int vx, int vy, int vr) {
        int cx = toScreenX(vx);
        int cy = toScreenY(vy);
        int r = Math.max(4, Math.round(vr * scale));

        g.fill(cx - r - 2, cy - r, cx + r + 2, cy + r, COL_CHASSIS_DARK);
        g.fill(cx - r, cy - r, cx + r, cy + r, 0xFF15171A);
        g.fill(cx - r + 2, cy - r + 2, cx + r - 2, cy + r - 2, 0xFF2A2E36);
        g.fill(cx - 2, cy - 2, cx + 2, cy + 2, COL_CHASSIS_DARK);
    }

    private void renderScrew(GuiGraphics g, int vx, int vy) {
        int cx = toScreenX(vx);
        int cy = toScreenY(vy);
        int r = Math.max(2, Math.round(3 * scale));

        g.fill(cx - r - 1, cy - r - 1, cx + r + 1, cy + r + 1, COL_BOLT_HOLE);
        g.fill(cx - r, cy - r, cx + r, cy + r, COL_BOLT_DARK);
        g.fill(cx - r + 1, cy - r + 1, cx, cy, COL_BOLT_LIGHT);
        g.fill(cx - 1, cy, cx + 2, cy + 1, COL_BOLT_HEX);
    }

    private void renderSpeakerGrille(GuiGraphics g, int vx, int vy) {
        int cx = toScreenX(vx);
        int cy = toScreenY(vy);
        int d = Math.max(2, Math.round(3 * scale));
        int[][] pts = {{0, 0}, {0, -d}, {0, d}, {-d, -d/2}, {-d, d/2}, {d, -d/2}, {d, d/2}};
        for (int[] p : pts) {
            g.fill(cx + p[0], cy + p[1], cx + p[0] + 2, cy + p[1] + 2, COL_BOLT_HOLE);
        }
    }

    private void renderScreenBezel(GuiGraphics g) {
        int x = toScreenX(SCR_X);
        int y = toScreenY(SCR_Y);
        int w = toScreenW(SCR_W);
        int h = toScreenH(SCR_H);

        // Recessed outer border around 4:3 screen
        int b1 = Math.max(1, Math.round(4 * scale));
        int b2 = Math.max(1, Math.round(2 * scale));

        // Outer recess
        g.fill(x - b1, y - b1, x + w + b1, y, COL_RECESS_DEEP); // Top
        g.fill(x - b1, y, x, y + h, COL_RECESS_DEEP);           // Left
        g.fill(x + w, y, x + w + b1, y + h, COL_RECESS_BEVEL);  // Right
        g.fill(x - b1, y + h, x + w + b1, y + h + b1, COL_RECESS_BEVEL); // Bottom

        // Inner bevel
        g.fill(x - b2, y - b2, x + w + b2, y, COL_RECESS_BASE);
        g.fill(x - b2, y, x, y + h, COL_RECESS_BASE);

        // Tactical index notches around screen
        int notchW = Math.max(2, Math.round(3 * scale));
        int notchH = Math.max(2, Math.round(4 * scale));
        for (int i = 1; i <= 7; i++) {
            int tx = x + (w * i) / 8;
            g.fill(tx - notchW/2, y - notchH, tx + notchW/2, y, COL_CHASSIS_LIGHT);
            g.fill(tx - notchW/2, y + h, tx + notchW/2, y + h + notchH, COL_CHASSIS_LIGHT);
        }
        for (int i = 1; i <= 5; i++) {
            int ty = y + (h * i) / 6;
            g.fill(x - notchH, ty - notchW/2, x, ty + notchW/2, COL_CHASSIS_LIGHT);
            g.fill(x + w, ty - notchW/2, x + w + notchH, ty + notchW/2, COL_CHASSIS_LIGHT);
        }
    }

    private void renderStencils(GuiGraphics g) {
        int textY = topPos + toScreenH(32);
        g.drawString(font, "LEONARDO DRS MRT-104II", leftPos + toScreenW(170), textY, COL_STENCIL, false);
        g.drawString(font, "FIRE CONTROL TAC-TAB", leftPos + toScreenW(400), textY, COL_STENCIL, false);
        g.drawString(font, "STATUS", leftPos + toScreenW(60), topPos + toScreenH(85), COL_STENCIL, false);
        g.drawString(font, "AUX", leftPos + toScreenW(DESIGN_W - 75), topPos + toScreenH(85), COL_STENCIL, false);
    }

    // =========================================================================
    // 6. BUTTONS & LEDS RENDERING (PURE JAVA)
    // =========================================================================
    private void renderAllButtons(GuiGraphics g) {
        for (TacticalKey key : keys) {
            boolean isHover = (key == hoveredKey);
            renderSingleKey(g, key, isHover);
        }
    }

    private void renderSingleKey(GuiGraphics g, TacticalKey key, boolean isHover) {
        int bx = toScreenX(key.vx - key.vw / 2);
        int by = toScreenY(key.vy - key.vh / 2);
        int bw = toScreenW(key.vw);
        int bh = toScreenH(key.vh);

        // 1. Routed Recessed Slot (Hốc phím DRS MRT-104II)
        int pad = Math.max(1, Math.round(3 * scale));
        g.fill(bx - pad, by - pad, bx + bw + pad, by + bh + pad, COL_SLOT_DEEP);
        g.fill(bx - pad + 1, by + bh + pad - 1, bx + bw + pad - 1, by + bh + pad, COL_SLOT_EDGE);
        g.fill(bx + bw + pad - 1, by - pad + 1, bx + bw + pad, by + bh + pad, COL_SLOT_EDGE);

        // 2. Button Body Colors
        int baseCol   = key.isRed ? COL_RED_BASE : COL_BTN_BASE;
        int faceCol   = key.isRed ? (isHover ? COL_RED_HOVER : COL_RED_FACE) : (isHover ? COL_BTN_HOVER : COL_BTN_FACE);
        int lightCol  = key.isRed ? COL_RED_LIGHT : COL_BTN_LIGHT;
        int darkCol   = key.isRed ? COL_RED_DARK : COL_BTN_DARK;
        int borderCol = key.isRed ? COL_RED_ACCENT : COL_BTN_ACCENT_YELLOW;
        int textCol   = key.isRed ? COL_RED_TEXT : COL_BTN_TEXT;

        // Outer base & Yellow accent border
        g.fill(bx, by, bx + bw, by + bh, darkCol);
        g.fill(bx + 1, by + 1, bx + bw - 1, by + bh - 1, borderCol);

        // Main tactile face
        g.fill(bx + 2, by + 2, bx + bw - 2, by + bh - 2, faceCol);

        // 3D Bevel Edge
        g.fill(bx + 2, by + 2, bx + bw - 2, by + 3, lightCol);
        g.fill(bx + 2, by + 2, bx + 3, by + bh - 2, lightCol);
        g.fill(bx + 2, by + bh - 3, bx + bw - 2, by + bh - 2, darkCol);
        g.fill(bx + bw - 3, by + 2, bx + bw - 2, by + bh - 2, darkCol);

        // 3. Button Label / Text
        renderButtonLabel(g, key, bx + bw / 2, by + bh / 2, textCol);

        // 4. Status LED Indicator
        renderKeyLED(g, key);
    }

    private void renderButtonLabel(GuiGraphics g, TacticalKey key, int cx, int cy, int color) {
        if ("GRID".equals(key.label)) {
            // Draw Mini Crosshair / Grid Vector
            int s = Math.max(3, Math.round(5 * scale));
            g.fill(cx - s, cy, cx + s + 1, cy + 1, color);
            g.fill(cx, cy - s, cx + 1, cy + s + 1, color);
            g.fill(cx - s, cy - s, cx - s + 2, cy - s + 1, color);
            g.fill(cx + s - 1, cy - s, cx + s + 1, cy - s + 1, color);
            g.fill(cx - s, cy + s, cx - s + 2, cy + s + 1, color);
            g.fill(cx + s - 1, cy + s, cx + s + 1, cy + s + 1, color);
        } else if ("PWR".equals(key.label)) {
            // Draw Mini Power Glyph
            int r = Math.max(3, Math.round(4 * scale));
            g.drawCenteredString(font, "⏻", cx, cy - font.lineHeight / 2, color);
        } else {
            // Text Label
            int textY = cy - font.lineHeight / 2;
            g.drawCenteredString(font, key.label, cx, textY, color);
        }
    }

    private void renderKeyLED(GuiGraphics g, TacticalKey key) {
        int ledCenterX = toScreenX(key.vx);
        int ledCenterY = toScreenY(key.vy);
        int offset = Math.round(18 * scale);

        switch (key.ledPos) {
            case BOTTOM -> ledCenterY = toScreenY(key.vy + key.vh / 2) + Math.round(6 * scale);
            case TOP    -> ledCenterY = toScreenY(key.vy - key.vh / 2) - Math.round(6 * scale);
            case RIGHT  -> ledCenterX = toScreenX(key.vx + key.vw / 2) + Math.round(6 * scale);
            case LEFT   -> ledCenterX = toScreenX(key.vx - key.vw / 2) - Math.round(6 * scale);
        }

        int ledW = Math.max(3, Math.round(6 * scale));
        int ledH = Math.max(2, Math.round(4 * scale));

        // LED Socket Hole
        g.fill(ledCenterX - ledW/2 - 1, ledCenterY - ledH/2 - 1, ledCenterX + ledW/2 + 1, ledCenterY + ledH/2 + 1, COL_LED_HOLE);
        g.fill(ledCenterX - ledW/2, ledCenterY - ledH/2, ledCenterX + ledW/2, ledCenterY + ledH/2, COL_LED_BORDER);

        // LED Core State
        int coreColor = key.isLedActive ? (key.isRed ? COL_LED_RED_ON : COL_LED_GREEN_ON) : COL_LED_OFF;
        g.fill(ledCenterX - ledW/2 + 1, ledCenterY - ledH/2 + 1, ledCenterX + ledW/2 - 1, ledCenterY + ledH/2 - 1, coreColor);

        // Active Glow
        if (key.isLedActive) {
            g.fill(ledCenterX - 1, ledCenterY - 1, ledCenterX + 1, ledCenterY + 1, 0xFFFFFFFF);
        }
    }

    // =========================================================================
    // 7. ARTILLERY MAP / RADAR RENDERER (PLACEHOLDER)
    // =========================================================================
    private void renderArtilleryMap(GuiGraphics g, int x, int y, int w, int h) {
        // Deep tactical map background
        g.fill(x, y, x + w, y + h, 0xFF0D1217);

        // Tactical Grid Lines (Xanh quân sự mờ)
        int gridStep = Math.max(16, Math.round(40 * scale));
        int gridCol = 0x2200E5FF;
        for (int gx = x; gx < x + w; gx += gridStep) {
            g.fill(gx, y, gx + 1, y + h, gridCol);
        }
        for (int gy = y; gy < y + h; gy += gridStep) {
            g.fill(x, gy, x + w, gy + 1, gridCol);
        }

        // Center Crosshair
        int midX = x + w / 2;
        int midY = y + h / 2;
        g.fill(midX - 20, midY, midX + 20, midY + 1, 0xAA00FF66);
        g.fill(midX, midY - 20, midX, midY + 20, 0xAA00FF66);

        // Range rings
        g.drawCenteredString(font, "[ ARTILLERY FIRE-CONTROL MAP ACTIVE ]", midX, midY - 30, 0xFF00FF66);
        g.drawCenteredString(font, "TARGET: LAT 104.22 // LON 52.18 // RANGE: 1,420m", midX, midY + 15, 0xFF7090A8);
    }

    // =========================================================================
    // 8. INTERACTION & INPUT HANDLING
    // =========================================================================
    private void updateHoverState(int mouseX, int mouseY) {
        hoveredKey = null;
        for (TacticalKey key : keys) {
            int bx = toScreenX(key.vx - key.vw / 2);
            int by = toScreenY(key.vy - key.vh / 2);
            int bw = toScreenW(key.vw);
            int bh = toScreenH(key.vh);

            if (mouseX >= bx && mouseX < bx + bw && mouseY >= by && mouseY < by + bh) {
                hoveredKey = key;
                break;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredKey != null) { // Left click
            handleKeyAction(hoveredKey);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleKeyAction(TacticalKey key) {
        // Toggle LED state for testing
        key.isLedActive = !key.isLedActive;

        switch (key.id) {
            case "CFF" -> {
                // Call For Fire! (BẮN)
                if (minecraft != null && minecraft.player != null) {
                    minecraft.player.displayClientMessage(Component.literal("§c[CFF] Khai hỏa trận địa pháo binh!"), true);
                }
            }
            case "PWR" -> {
                // Tắt màn hình / Đóng GUI
                this.onClose();
            }
            case "GRID" -> {
                // Toggle Grid Mode
            }
            default -> {
                // Other F-keys
            }
        }
    }

    // =========================================================================
    // 9. HELPER COORDINATE CONVERSION & DRAW UTILITIES
    // =========================================================================
    private int toScreenX(int vx) { return leftPos + Math.round(vx * scale); }
    private int toScreenY(int vy) { return topPos + Math.round(vy * scale); }
    private int toScreenW(int vw) { return Math.round(vw * scale); }
    private int toScreenH(int vh) { return Math.round(vh * scale); }

    private void fillChamfer(GuiGraphics g, int x, int y, int w, int h, int c, int col) {
        // Main center body
        g.fill(x + c, y, x + w - c, y + h, col);
        // Left & Right sides
        g.fill(x, y + c, x + c, y + h - c, col);
        g.fill(x + w - c, y + c, x + w, y + h - c, col);
        // Corner stepped diagonals
        for (int i = 1; i < c; i++) {
            g.fill(x + c - i, y + i, x + c - i + 1, y + c, col);
            g.fill(x + w - c + i - 1, y + i, x + w - c + i, y + c, col);
            g.fill(x + c - i, y + h - c, x + c - i + 1, y + h - i, col);
            g.fill(x + w - c + i - 1, y + h - c, x + w - c + i, y + h - i, col);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // =========================================================================
    // 10. INNER DATA STRUCTURES
    // =========================================================================
    private static class TacticalKey {
        final String id;
        final String label;
        final int vx, vy, vw, vh;
        final boolean isRed;
        final KeyType type;
        final LedPosition ledPos;
        boolean isLedActive = false;

        TacticalKey(String id, String label, int vx, int vy, int vw, int vh, boolean isRed, KeyType type, LedPosition ledPos) {
            this.id = id;
            this.label = label;
            this.vx = vx;
            this.vy = vy;
            this.vw = vw;
            this.vh = vh;
            this.isRed = isRed;
            this.type = type;
            this.ledPos = ledPos;
        }
    }

    private enum KeyType { TOP_ROW, BOT_ROW, LEFT_COL, RIGHT_COL }
    private enum LedPosition { TOP, BOTTOM, LEFT, RIGHT }
}
