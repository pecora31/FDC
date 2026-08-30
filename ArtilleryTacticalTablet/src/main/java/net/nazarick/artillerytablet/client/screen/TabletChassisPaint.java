package net.nazarick.artillerytablet.client.screen;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The chassis: carbon-composite shell with true transparent corners, SDF
 * collars around the
 * screen well, a real tactical grid baked into the well floor, SDF recessed
 * corner pockets,
 * lit capsule divider ribs, layered hex bolts.
 *
 * <p>
 * Ported verbatim (same colours, same design-space maths, same pixel order)
 * from the shell
 * portion of a visually-only prototype the user supplied. The prototype's
 * newest revision also
 * bakes the 32 keys' idle look, all 29 LEDs, and button hover/press sprites
 * into the same atlas —
 * deliberately NOT ported here, because those bake once and never change, while
 * this mod's keys
 * and LEDs must keep reflecting live game state (which tab is open,
 * armed/danger, grid on/off,
 * screen power) every frame; {@link UiButton} still draws itself and its own
 * lamp for exactly
 * that reason. Only the static background — the part with no game state behind
 * it — is baked.
 */
@OnlyIn(Dist.CLIENT)
public final class TabletChassisPaint {
    private TabletChassisPaint() {
    }

    // =========================================================================
    // BAKE — runs once, into a design-space (980x630) image
    // =========================================================================
    public static NativeImage bake() {
        NativeImage img = new NativeImage(TabletFrame.DESIGN_W, TabletFrame.DESIGN_H, false);

        int w = TabletFrame.DESIGN_W;
        int h = TabletFrame.DESIGN_H;
        int r = 34; // Further increased curvature for outer L dưới
        int bW = 9;
        int cornerW = 62; // L trên shifted further deeper into chassis
        int cornerH = 62;

        // 1. Base rim, true-transparent outside the rounded rect (no square corners
        // bleeding through)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isInsideRoundedRect(x, y, 0, 0, w, h, r)) {
                    setPixel(img, x, y, 0xFF0A0B0C);
                } else {
                    setPixel(img, x, y, 0x00000000);
                }
            }
        }

        // 2. Main chassis floor with stipple
        for (int cy = bW; cy < h - bW; cy++) {
            for (int cx = bW; cx < w - bW; cx++) {
                int grain = ((cx * 17 + cy * 31) ^ (cx * 11)) % 3;
                int col = (grain == 1) ? 0xFF141517 : ((grain == 2) ? 0xFF18191B : 0xFF161719);
                setPixel(img, cx, cy, col);
            }
        }

        // 3. Four soft-corner chamfer facets (connecting seamlessly with corner pockets)
        for (int cy = 1; cy < bW; cy++) {
            int d = cy - 1;
            int col = (d == 0) ? 0xFF2D2F33 : ((d == 1) ? 0xFF26282C : ((d == bW - 2) ? 0xFF1B1D1F : 0xFF202225));
            for (int cx = cornerW; cx < w - cornerW; cx++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cx = 1; cx < bW; cx++) {
            int d = cx - 1;
            int col = (d == 0) ? 0xFF2A2C30 : ((d == 1) ? 0xFF242629 : ((d == bW - 2) ? 0xFF1B1D1F : 0xFF1E2023));
            for (int cy = cornerH; cy < h - cornerH; cy++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cy = h - bW; cy < h - 1; cy++) {
            int d = (h - 2) - cy;
            int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0C0D0F : ((d == bW - 2) ? 0xFF141517 : 0xFF101113));
            for (int cx = cornerW; cx < w - cornerW; cx++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cx = w - bW; cx < w - 1; cx++) {
            int d = (w - 2) - cx;
            int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0C0D0F : ((d == bW - 2) ? 0xFF141517 : 0xFF101113));
            for (int cy = cornerH; cy < h - cornerH; cy++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }

        // 4. Stepped corners, each with its own layered hex bolt
        bakeSteppedCorner(img, 0, 0, true, true, cornerW, cornerH, 28, r, bW);
        bakeSteppedCorner(img, w - cornerW, 0, false, true, cornerW, cornerH, 28, r, bW);
        bakeSteppedCorner(img, 0, h - cornerH, true, false, cornerW, cornerH, 28, r, bW);
        bakeSteppedCorner(img, w - cornerW, h - cornerH, false, false, cornerW, cornerH, 28, r, bW);

        // 5. Screen bezel well + tactical grid baked into the well floor
        bakeScreenAndTacticalGrid(img);

        // 6. Top & bottom raised U-collars
        bakeRaisedUCollar(img, false, bW);
        bakeRaisedUCollar(img, true, bW);

        // 7. Left & right C-brackets
        bakeSideCBracket(img, true, bW);
        bakeSideCBracket(img, false, bW);

        // 8. Divider Ribs between inner key clusters
        bakeAllDividerRibs(img);

        // 9. Bake all 32 default tactical keys and unlit LEDs directly onto 980px chassis
        bakeAllDefaultKeysAndLeds(img);

        return img;
    }

    /**
     * Stretches the baked design-space image over whatever rectangle the frame is
     * drawing at.
     */
    static void blit(GuiGraphics g, TabletFrame frame, ResourceLocation texture) {
        int x = frame.toScreenX(0);
        int y = frame.toScreenY(0);
        int w = frame.toScreenW(TabletFrame.DESIGN_W);
        int h = frame.toScreenH(TabletFrame.DESIGN_H);
        g.blit(texture, x, y, w, h, 0f, 0f, TabletFrame.DESIGN_W, TabletFrame.DESIGN_H,
                TabletFrame.DESIGN_W, TabletFrame.DESIGN_H);
    }

    public static void drawScreenCornerMasks(GuiGraphics g, TabletFrame frame) {
        int x0 = frame.screenLeft();
        int y0 = frame.screenTop();
        int w = frame.screenWidth();
        int h = frame.screenHeight();
        int r = Math.max(3, frame.toScreenW(10f)); // Screen well inner corner radius

        int colTL = 0xFF08090B;
        int colTR = 0xFF08090B;
        int colBL = 0xFF16181C;
        int colBR = 0xFF202226;

        for (int dy = 0; dy < r; dy++) {
            for (int dx = 0; dx < r; dx++) {
                // Top-Left corner
                int dtl = (r - dx) * (r - dx) + (r - dy) * (r - dy);
                if (dtl > r * r) {
                    g.fill(x0 + dx, y0 + dy, x0 + dx + 1, y0 + dy + 1, colTL);
                }
                // Top-Right corner
                int dtr = (dx + 1) * (dx + 1) + (r - dy) * (r - dy);
                if (dtr > r * r) {
                    g.fill(x0 + w - r + dx, y0 + dy, x0 + w - r + dx + 1, y0 + dy + 1, colTR);
                }
                // Bottom-Left corner
                int dbl = (r - dx) * (r - dx) + (dy + 1) * (dy + 1);
                if (dbl > r * r) {
                    g.fill(x0 + dx, y0 + h - r + dy, x0 + dx + 1, y0 + h - r + dy + 1, colBL);
                }
                // Bottom-Right corner
                int dbr = (dx + 1) * (dx + 1) + (dy + 1) * (dy + 1);
                if (dbr > r * r) {
                    g.fill(x0 + w - r + dx, y0 + h - r + dy, x0 + w - r + dx + 1, y0 + h - r + dy + 1, colBR);
                }
            }
        }
    }

    private static void bakeSteppedCorner(NativeImage img, int vx, int vy, boolean isLeft, boolean isTop,
            int cornerW, int cornerH, int rIn, int outerR, int bW) {
        int w = TabletFrame.DESIGN_W;
        int h = TabletFrame.DESIGN_H;
        int pSize = cornerW;
        int rInner = rIn; // 28px curve for L trên

        int kx1 = isLeft ? 0 : w - pSize;
        int kx2 = isLeft ? pSize : w;
        int ky1 = isTop ? 0 : h - pSize;
        int ky2 = isTop ? pSize : h;

        for (int y = ky1; y < ky2; y++) {
            for (int x = kx1; x < kx2; x++) {
                // 1. Calculate local coordinates from outer corner inwards
                int lx = isLeft ? x : (w - 1 - x);
                int ly = isTop ? y : (h - 1 - y);

                // 2. Outer tablet rounded corner check (R = outerR)
                if (lx < outerR && ly < outerR) {
                    float dx = outerR - lx, dy = outerR - ly;
                    float distOut = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distOut > outerR) {
                        setPixel(img, x, y, 0x00000000); // Transparent outside tablet
                        continue;
                    }
                }

                // 3. Signed Distance Field to the inner L-pocket fillet wall
                float distToWall;
                int centerFillet = pSize - rInner;
                if (lx > centerFillet && ly > centerFillet) {
                    float dx = lx - centerFillet, dy = ly - centerFillet;
                    distToWall = rInner - (float) Math.sqrt(dx * dx + dy * dy);
                } else if (lx > ly) {
                    distToWall = pSize - lx;
                } else {
                    distToWall = pSize - ly;
                }

                // 4. Color assignment based on wall distance and 3D surface plane
                if (distToWall < -2.0f) {
                    // Outside pocket: On the main raised chassis plateau
                    continue;
                } else if (distToWall >= -2.0f && distToWall < 0.0f) {
                    // Top crest highlight ridge along the mouth of the pocket
                    int crestCol = (isTop && isLeft) ? 0xFF363940
                            : (isTop ? 0xFF2C2F35
                                    : (isLeft ? 0xFF282B30 : 0xFF1C1E22));
                    setPixel(img, x, y, applyStipple(crestCol, x, y));
                } else if (distToWall >= 0.0f && distToWall < 2.0f) {
                    // Vertical sloped fillet wall descending into the pocket
                    int wallCol = 0xFF0D0E11;
                    setPixel(img, x, y, applyStipple(wallCol, x, y));
                } else if (distToWall >= 2.0f && distToWall < 3.5f) {
                    // Cavity root drop shadow along the bottom of the wall
                    setPixel(img, x, y, 0xFF040506);
                } else {
                    // Recessed pocket floor
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int floorCol = (grain == 1) ? 0xFF0C0D0F : ((grain == 2) ? 0xFF101113 : 0xFF0E0F11);
                    setPixel(img, x, y, floorCol);
                }

                // 5. Outer edge bevel contour around the rounded corner
                if (lx < outerR && ly < outerR) {
                    float dx = outerR - lx, dy = outerR - ly;
                    float distOut = (float) Math.sqrt(dx * dx + dy * dy);
                    if (distOut >= outerR - 2.0f && distOut <= outerR) {
                        int rimCol = (isTop && isLeft) ? 0xFF2E3136
                                : (isTop ? 0xFF24262A : (isLeft ? 0xFF222428 : 0xFF08080A));
                        setPixel(img, x, y, rimCol);
                    }
                } else if (lx < 2) {
                    setPixel(img, x, y, isLeft ? 0xFF2A2C30 : 0xFF08080A);
                } else if (ly < 2) {
                    setPixel(img, x, y, isTop ? 0xFF2D2F33 : 0xFF08080A);
                }
            }
        }

        // 6. Deep CNC-milled counterbore screw hole (shifted towards inner corner of L-tren)
        int cx = isLeft ? 36 : (w - 37);
        int cy = isTop ? 36 : (h - 37);
        int boltR = 7;

        fillCircle(img, cx, cy, boltR + 4, 0xFF1A1C20); // Outer counterbore rim
        fillCircle(img, cx, cy, boltR + 3, 0xFF0E0F12);
        fillCircle(img, cx, cy, boltR + 1, 0xFF030304); // Deep black cavity opening
        fillCircle(img, cx, cy, boltR - 1, 0xFF010102); // Hole bottom
        fillCircle(img, cx, cy, Math.max(1, boltR - 4), 0xFF08090C); // Center socket pin
    }

    private static void bakeScreenAndTacticalGrid(NativeImage img) {
        int mapX = TabletFrame.SCR_X, mapY = TabletFrame.SCR_Y, mapW = TabletFrame.SCR_W, mapH = TabletFrame.SCR_H;
        int scrR = 16, bevelW = 6;
        int outR = scrR + bevelW;
        int outX1 = mapX - bevelW, outY1 = mapY - bevelW;
        int outX2 = mapX + mapW + bevelW, outY2 = mapY + mapH + bevelW;

        // 1. Sleek tactical bezel well
        for (int y = outY1; y < outY2; y++) {
            for (int x = outX1; x < outX2; x++) {
                if (isInsideRoundedRect(x, y, outX1, outY1, outX2, outY2, outR)
                        && !isInsideRoundedRect(x, y, mapX, mapY, mapX + mapW, mapY + mapH, scrR)) {

                    boolean inTopLeft = x < mapX + scrR && y < mapY + scrR;
                    boolean inBotRight = x > mapX + mapW - scrR && y > mapY + mapH - scrR;
                    boolean inTopRight = x > mapX + mapW - scrR && y < mapY + scrR;
                    boolean inBotLeft = x < mapX + scrR && y > mapY + mapH - scrR;

                    int bCol;
                    if (inTopLeft) {
                        bCol = 0xFF070809;
                    } else if (inBotRight) {
                        bCol = 0xFF24262A;
                    } else if (inTopRight) {
                        int localX = x - (mapX + mapW - scrR);
                        int localY = y - (mapY + scrR);
                        bCol = (localX < -localY) ? 0xFF0D0E10 : 0xFF1A1C1F;
                    } else if (inBotLeft) {
                        int localX = x - (mapX + scrR);
                        int localY = y - (mapY + mapH - scrR);
                        bCol = (localX > -localY) ? 0xFF1A1C1F : 0xFF0D0E10;
                    } else if (y < mapY) {
                        bCol = 0xFF08090B;
                    } else if (x < mapX) {
                        bCol = 0xFF0C0D0F;
                    } else if (y >= mapY + mapH) {
                        bCol = 0xFF202226;
                    } else if (x >= mapX + mapW) {
                        bCol = 0xFF1C1E22;
                    } else {
                        bCol = 0xFF08090B;
                    }
                    setPixel(img, x, y, bCol);
                }
            }
        }

        // 2. Pure True OLED FHD Display Floor (No grid lines, no CRT scanlines, true pure OLED black)
        for (int y = mapY; y < mapY + mapH; y++) {
            for (int x = mapX; x < mapX + mapW; x++) {
                if (isInsideRoundedRect(x, y, mapX, mapY, mapX + mapW, mapY + mapH, scrR)) {
                    setPixel(img, x, y, 0xFF030406);
                }
            }
        }

        // 3. Crisp ASTRA SYSTEMS Logo
        drawAstraLogo(img, mapX + mapW / 2, mapY + mapH / 2);
    }

    private static void drawAstraLogo(NativeImage img, int cx, int cy) {
        int redCol = 0xFFB8141D;
        int subTextCol = 0xFF9AA4B2;
        int topY = cy - 24;
        int letH = 30;
        int letW = 32;
        int gap = 16;
        int totalW = 5 * letW + 4 * gap;
        int startX = cx - totalW / 2;

        // Render 'ASTRA'
        for (int i = 0; i < 5; i++) {
            int lx = startX + i * (letW + gap);
            switch (i) {
                case 0, 4 -> { // 'A' Chevron (Lambda shape with no crossbar)
                    for (int y = topY; y <= topY + letH; y++) {
                        float t = (float) (y - topY) / letH;
                        int leftX = Math.round(lx + 16 - t * 16);
                        int rightX = Math.round(lx + 16 + t * 16);
                        for (int dx = -2; dx <= 2; dx++) {
                            setPixel(img, leftX + dx, y, redCol);
                            setPixel(img, rightX + dx, y, redCol);
                        }
                    }
                }
                case 1 -> { // 'S'
                    int thick = 5;
                    // Top bar
                    for (int x = lx + 4; x <= lx + letW; x++)
                        for (int y = topY; y < topY + thick; y++) setPixel(img, x, y, redCol);
                    // Top-left round corner & upper stem
                    for (int y = topY + 2; y <= topY + letH / 2; y++)
                        for (int x = lx; x < lx + thick; x++) setPixel(img, x, y, redCol);
                    // Middle bar
                    for (int x = lx + 2; x <= lx + letW - 2; x++)
                        for (int y = topY + letH / 2 - 2; y <= topY + letH / 2 + 2; y++) setPixel(img, x, y, redCol);
                    // Lower-right stem
                    for (int y = topY + letH / 2; y <= topY + letH - 2; y++)
                        for (int x = lx + letW - thick; x <= lx + letW; x++) setPixel(img, x, y, redCol);
                    // Bottom bar
                    for (int x = lx; x <= lx + letW - 4; x++)
                        for (int y = topY + letH - thick; y <= topY + letH; y++) setPixel(img, x, y, redCol);
                }
                case 2 -> { // 'T'
                    int thick = 5;
                    // Top horizontal bar
                    for (int x = lx; x <= lx + letW; x++)
                        for (int y = topY; y < topY + thick; y++) setPixel(img, x, y, redCol);
                    // Center vertical stem
                    for (int y = topY + thick; y <= topY + letH; y++)
                        for (int x = lx + letW / 2 - 2; x <= lx + letW / 2 + 2; x++) setPixel(img, x, y, redCol);
                }
                case 3 -> { // 'R'
                    int thick = 5;
                    // Left vertical stem
                    for (int y = topY; y <= topY + letH; y++)
                        for (int x = lx; x < lx + thick; x++) setPixel(img, x, y, redCol);
                    // Upper loop top
                    for (int x = lx + thick; x <= lx + letW - 4; x++)
                        for (int y = topY; y < topY + thick; y++) setPixel(img, x, y, redCol);
                    // Upper loop right curve
                    for (int y = topY + 2; y <= topY + letH / 2; y++)
                        for (int x = lx + letW - thick; x <= lx + letW; x++) setPixel(img, x, y, redCol);
                    // Upper loop bottom
                    for (int x = lx + thick; x <= lx + letW - 4; x++)
                        for (int y = topY + letH / 2 - 2; y <= topY + letH / 2 + 2; y++) setPixel(img, x, y, redCol);
                    // Angled leg with stylized notch
                    for (int y = topY + letH / 2; y <= topY + letH; y++) {
                        float t = (float) (y - (topY + letH / 2)) / (letH / 2.0f);
                        int legX = Math.round(lx + 10 + t * (letW - 10));
                        for (int dx = -2; dx <= 2; dx++) {
                            setPixel(img, legX + dx, y, redCol);
                        }
                    }
                }
            }
        }

        // Subtitle: '—  S Y S T E M S  —'
        int subY = cy + 22;
        // Left & right crimson accent lines
        int lineLen = 50;
        int lineGap = 16;
        int textSubW = 140;
        int line1X2 = cx - textSubW / 2 - lineGap;
        int line1X1 = line1X2 - lineLen;
        int line2X1 = cx + textSubW / 2 + lineGap;
        int line2X2 = line2X1 + lineLen;

        for (int x = line1X1; x <= line1X2; x++) {
            setPixel(img, x, subY + 3, redCol);
            setPixel(img, x, subY + 4, redCol);
        }
        for (int x = line2X1; x <= line2X2; x++) {
            setPixel(img, x, subY + 3, redCol);
            setPixel(img, x, subY + 4, redCol);
        }

        // Draw tracked 'S Y S T E M S'
        String sub = "SYSTEMS";
        int subCharCount = sub.length();
        int subSlot = textSubW / subCharCount;
        for (int i = 0; i < subCharCount; i++) {
            char ch = sub.charAt(i);
            int subX = (cx - textSubW / 2) + i * subSlot + subSlot / 2;
            rasterizePixelString(img, String.valueOf(ch), subX, subY + 4, 1, subTextCol);
        }
    }

    private static void bakeRaisedUCollar(NativeImage img, boolean isTop, int bW) {
        int uX1 = 106, uX2 = 874;
        int cutX1 = 188, cutX2 = 792;
        int uY1 = !isTop ? 540 : 0;
        int uY2 = !isTop ? TabletFrame.DESIGN_H : 90;
        int cutY1 = !isTop ? 558 : 0;
        int cutY2 = !isTop ? TabletFrame.DESIGN_H : 72;
        int rInner = 9, bChamfer = 9;

        for (int y = (isTop ? 90 : 540); y <= (isTop ? 96 : TabletFrame.DESIGN_H - bW); y++) {
            for (int x = uX1 + bW; x <= uX2 - bW; x++) {
                if (isInsideUPlateau(x - 4, y - 4, isTop, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                        bChamfer)
                        && !isInsideUPlateau(x, y, isTop, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                                bChamfer)) {
                    setPixel(img, x, y, 0x55010102);
                }
            }
        }

        for (int y = uY1; y < uY2; y++) {
            for (int x = uX1; x < uX2; x++) {
                if (isInsideUPlateau(x, y, isTop, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int col = (grain == 1) ? 0xFF1F2124 : ((grain == 2) ? 0xFF232528 : 0xFF212326);
                    setPixel(img, x, y, col);
                }
            }
        }

        if (!isTop) {
            // Slim lateral side bevels (uX1 left facing light, uX2 right next to Power
            // button)
            int bevelW = 3;
            for (int x = uX1; x < uX1 + bevelW; x++) {
                int d = x - uX1;
                int col = (d == 0) ? 0xFF32353A : ((d == 1) ? 0xFF282A2F : 0xFF222428);
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int x = uX2 - bevelW; x < uX2; x++) {
                int d = (uX2 - 1) - x;
                int col = (d == 0) ? 0xFF050608 : ((d == 1) ? 0xFF101215 : 0xFF1A1C20);
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int bEdgeY = TabletFrame.DESIGN_H - 9;
            for (int y = bEdgeY; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0B0C0E : (y <= bEdgeY + 1 ? 0xFF16171A : 0xFF0E0F11));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int bevelH = 8;
            for (int y = uY1; y < uY1 + bevelH; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF141518
                        : ((d == 1) ? 0xFF191B1E
                                : (d == bevelH - 2 ? 0xFF26282D : (d == bevelH - 1 ? 0xFF2B2D32 : 0xFF1F2125)));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        } else {
            // Slim Top U-collar lateral side bevels
            int bevelW = 3;
            for (int x = uX1; x < uX1 + bevelW; x++) {
                int d = x - uX1;
                int col = (d == 0) ? 0xFF32353A : ((d == 1) ? 0xFF282A2F : 0xFF222428);
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int x = uX2 - bevelW; x < uX2; x++) {
                int d = (uX2 - 1) - x;
                int col = (d == 0) ? 0xFF050608 : ((d == 1) ? 0xFF101215 : 0xFF1A1C20);
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int tEdgeY = 9;
            for (int y = uY1; y < tEdgeY; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF2D2F33 : ((d == 1) ? 0xFF282A2E : (y >= tEdgeY - 2 ? 0xFF1B1C1F : 0xFF222427));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int bevelH = 8;
            for (int y = uY2 - bevelH; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08090B
                        : ((d == 1) ? 0xFF0D0E10
                                : (d == bevelH - 2 ? 0xFF1F2125 : (d == bevelH - 1 ? 0xFF282A2F : 0xFF141619)));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner,
                            bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }

        int boundY1 = !isTop ? cutY1 - 4 : uY1;
        int boundY2 = !isTop ? uY2 : cutY2 + 4;
        for (int y = boundY1; y <= boundY2; y++) {
            for (int x = cutX1 - 4; x <= cutX2 + 4; x++) {
                if (x < uX1 || x >= uX2 || y < uY1 || y >= uY2)
                    continue;
                float sdf = getCutoutSDF(x, y, isTop, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer);
                if (sdf >= 0 && sdf <= 1.5f) {
                    setPixel(img, x, y, applyStipple(0xFF383B41, x, y));
                } else if (sdf >= -1.2f && sdf < 0) {
                    setPixel(img, x, y, 0xFF040506);
                } else if (sdf >= -3.5f && sdf < -1.2f) {
                    setPixel(img, x, y, applyStipple(0xFF0A0B0D, x, y));
                } else if (sdf >= -5.0f && sdf < -3.5f) {
                    setPixel(img, x, y, applyStipple(0xFF111214, x, y));
                }
            }
        }
    }

    private static boolean isInsideUPlateau(int px, int py, boolean isTop, int uX1, int uX2, int uY1, int uY2,
            int cutX1, int cutX2, int cutY1, int cutY2, int rInner, int bChamfer) {
        if (px < uX1 || px >= uX2 || py < uY1 || py >= uY2)
            return false;
        return getCutoutSDF(px, py, isTop, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer) >= 0;
    }

    private static float getCutoutSDF(int px, int py, boolean isTop, int cutX1, int cutX2, int cutY1, int cutY2,
            int rInner, int bChamfer) {
        // uY1/uY2 not needed once inside the plateau test above; only the cutout
        // geometry matters.
        if (!isTop) {
            int curCutX1 = cutX1, curCutX2 = cutX2;
            int uY2 = TabletFrame.DESIGN_H;
            if (py >= uY2 - bChamfer) {
                int offset = py - (uY2 - bChamfer);
                curCutX1 = cutX1 + offset;
                curCutX2 = cutX2 - offset;
            }
            if (py < cutY1 + rInner) {
                if (px < cutX1 + rInner) {
                    float dx = px - (cutX1 + rInner), dy = py - (cutY1 + rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                } else if (px > cutX2 - rInner) {
                    float dx = px - (cutX2 - rInner), dy = py - (cutY1 + rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                } else {
                    return (float) (cutY1 - py);
                }
            }
            if (px < curCutX1)
                return (float) (curCutX1 - px);
            if (px > curCutX2)
                return (float) (px - curCutX2);
            return (float) -Math.min(px - curCutX1, Math.min(curCutX2 - px, py - cutY1));
        } else {
            int curCutX1 = cutX1, curCutX2 = cutX2;
            int uY1 = 0;
            if (py <= uY1 + bChamfer) {
                int offset = (uY1 + bChamfer) - py;
                curCutX1 = cutX1 + offset;
                curCutX2 = cutX2 - offset;
            }
            if (py > cutY2 - rInner) {
                if (px < cutX1 + rInner) {
                    float dx = px - (cutX1 + rInner), dy = py - (cutY2 - rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                } else if (px > cutX2 - rInner) {
                    float dx = px - (cutX2 - rInner), dy = py - (cutY2 - rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                } else {
                    return (float) (py - cutY2);
                }
            }
            if (px < curCutX1)
                return (float) (curCutX1 - px);
            if (px > curCutX2)
                return (float) (px - curCutX2);
            return (float) -Math.min(px - curCutX1, Math.min(curCutX2 - px, cutY2 - py));
        }
    }

    private static void bakeSideCBracket(NativeImage img, boolean isLeft, int bW) {
        int uY1 = 105, uY2 = 525;
        int cutY1 = 124, cutY2 = 506;
        int uX1 = isLeft ? 0 : 890;
        int uX2 = isLeft ? 90 : TabletFrame.DESIGN_W;
        int cutX1 = isLeft ? 0 : 908;
        int cutX2 = isLeft ? 72 : TabletFrame.DESIGN_W;
        int rInner = 14;
        int x1 = bW; // 9px

        // 0. Soft directional ambient occlusion shadow under C-bracket
        for (int dy = 1; dy <= 4; dy++) {
            int dx = dy;
            int alpha = (dy == 1) ? 0x66 : ((dy == 2) ? 0x44 : ((dy == 3) ? 0x26 : 0x12));
            int shadowCol = (alpha << 24) | 0x010102;
            for (int y = uY1 - 2; y <= uY2 + 6; y++) {
                for (int x = uX1 - 2; x <= uX2 + 6; x++) {
                    if (x < 0 || x >= TabletFrame.DESIGN_W || y < 0 || y >= TabletFrame.DESIGN_H) continue;
                    if (isInsideSidePlateau(x - dx, y - dy, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)
                            && !isInsideSidePlateau(x, y, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        setPixel(img, x, y, shadowCol);
                    }
                }
            }
        }

        // 1. Base plateau surface with fine tactical stipple grain
        for (int y = uY1; y < uY2; y++) {
            for (int x = uX1; x < uX2; x++) {
                if (isInsideSidePlateau(x, y, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int col = (grain == 1) ? 0xFF1F2124 : ((grain == 2) ? 0xFF232528 : 0xFF212326);
                    setPixel(img, x, y, col);
                }
            }
        }

        // 2. Bevel Shading with ultra-smooth mechanical gradients
        if (isLeft) {
            // Smooth outer flank slope (X in [0, 9])
            for (int x = uX1; x < uX1 + x1; x++) {
                int distFromOuter = x - uX1;
                for (int y = uY1; y < uY2; y++) {
                    if (!isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) continue;
                    int col;
                    if (y < uY1 + 4) {
                        int d = y - uY1;
                        col = (d == 0) ? 0xFF383C44 : ((d == 1) ? 0xFF2E3137 : 0xFF24262B);
                    } else if (y >= uY2 - 4) {
                        int d = (uY2 - 1) - y;
                        col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : 0xFF16171A);
                    } else {
                        // Smooth transition across flank
                        float t = (float) distFromOuter / (x1 - 1);
                        int rC = (int) (0x28 + t * (0x20 - 0x28));
                        int gC = (int) (0x2A + t * (0x22 - 0x2A));
                        int bC = (int) (0x2E + t * (0x26 - 0x2E));
                        col = 0xFF000000 | (rC << 16) | (gC << 8) | bC;
                    }
                    setPixel(img, x, y, applyStipple(col, x, y));
                }
            }

            // Top & Bottom Horizontal Bevels (X in [9, 90])
            for (int x = uX1 + x1; x < uX2; x++) {
                for (int d = 0; d < 4; d++) {
                    int yTop = uY1 + d;
                    if (isInsideSidePlateau(x, yTop, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        int col = (d == 0) ? 0xFF383C44 : ((d == 1) ? 0xFF2E3137 : ((d == 2) ? 0xFF26282D : 0xFF222428));
                        setPixel(img, x, yTop, applyStipple(col, x, yTop));
                    }
                    int yBot = (uY2 - 1) - d;
                    if (isInsideSidePlateau(x, yBot, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : ((d == 2) ? 0xFF141518 : 0xFF1A1C1F));
                        setPixel(img, x, yBot, applyStipple(col, x, yBot));
                    }
                }
            }

            // Smooth subtle ridge highlight at X = 9
            for (int y = uY1 + 4; y < uY2 - 4; y++) {
                if (isInsideSidePlateau(x1, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                    setPixel(img, x1, y, applyStipple(0xFF26282D, x1, y));
                }
            }
        } else {
            // Right outer flank bevel (X in [971, 980])
            int rx1 = TabletFrame.DESIGN_W - x1; // 971
            for (int x = rx1; x < uX2; x++) {
                int distFromOuter = (uX2 - 1) - x;
                for (int y = uY1; y < uY2; y++) {
                    if (!isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) continue;
                    int col;
                    if (y < uY1 + 4) {
                        int d = y - uY1;
                        col = (d == 0) ? 0xFF383C44 : ((d == 1) ? 0xFF2E3137 : 0xFF24262B);
                    } else if (y >= uY2 - 4) {
                        int d = (uY2 - 1) - y;
                        col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : 0xFF16171A);
                    } else {
                        float t = (float) distFromOuter / (x1 - 1);
                        int rC = (int) (0x08 + t * (0x14 - 0x08));
                        int gC = (int) (0x09 + t * (0x16 - 0x09));
                        int bC = (int) (0x0B + t * (0x19 - 0x0B));
                        col = 0xFF000000 | (rC << 16) | (gC << 8) | bC;
                    }
                    setPixel(img, x, y, applyStipple(col, x, y));
                }
            }

            // Top & Bottom Horizontal Bevels (X in [890, 971])
            for (int x = uX1; x < rx1; x++) {
                for (int d = 0; d < 4; d++) {
                    int yTop = uY1 + d;
                    if (isInsideSidePlateau(x, yTop, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        int col = (d == 0) ? 0xFF383C44 : ((d == 1) ? 0xFF2E3137 : ((d == 2) ? 0xFF26282D : 0xFF222428));
                        setPixel(img, x, yTop, applyStipple(col, x, yTop));
                    }
                    int yBot = (uY2 - 1) - d;
                    if (isInsideSidePlateau(x, yBot, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : ((d == 2) ? 0xFF141518 : 0xFF1A1C1F));
                        setPixel(img, x, yBot, applyStipple(col, x, yBot));
                    }
                }
            }

            // Smooth subtle ridge shadow at X = 971
            for (int y = uY1 + 4; y < uY2 - 4; y++) {
                if (isInsideSidePlateau(rx1, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                    setPixel(img, rx1, y, applyStipple(0xFF101114, rx1, y));
                }
            }
        }

        // 3. Smooth Inner cutout contour with refined anti-aliasing & soft transitions
        int boundX1 = isLeft ? 0 : cutX1 - 5;
        int boundX2 = isLeft ? cutX2 + 5 : TabletFrame.DESIGN_W;
        int boundY1 = cutY1 - 24;
        int boundY2 = cutY2 + 24;

        for (int y = boundY1; y <= boundY2; y++) {
            for (int x = boundX1; x <= boundX2; x++) {
                if (x < uX1 || x >= uX2 || y < uY1 || y >= uY2) continue;
                float sdf = getSideCutoutSDF(x, y, isLeft, cutX1, cutX2, cutY1, cutY2, rInner, bW);
                if (sdf >= 0 && sdf <= 1.2f) {
                    setPixel(img, x, y, applyStipple(0xFF3A3D44, x, y));
                } else if (sdf > 1.2f && sdf <= 2.2f) {
                    setPixel(img, x, y, applyStipple(0xFF282A2F, x, y));
                } else if (sdf >= -1.0f && sdf < 0) {
                    setPixel(img, x, y, 0xFF040506);
                } else if (sdf >= -2.5f && sdf < -1.0f) {
                    setPixel(img, x, y, applyStipple(0xFF090A0C, x, y));
                } else if (sdf >= -4.5f && sdf < -2.5f) {
                    setPixel(img, x, y, applyStipple(0xFF101113, x, y));
                }
            }
        }

        // 4. Uniform flat planar bevel facing screen well
        int bevelW = 8;
        if (isLeft) {
            int bx1 = uX2 - bevelW, bx2 = uX2;
            for (int x = bx1; x < bx2; x++) {
                int d = x - bx1;
                int col = (d == 0) ? 0xFF2E3137
                        : ((d == bevelW - 1) ? 0xFF08090B
                        : 0xFF15171B);
                for (int y = uY1 + 2; y < uY2 - 2; y++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        } else {
            int bx1 = uX1, bx2 = uX1 + bevelW;
            for (int x = bx1; x < bx2; x++) {
                int d = (bx2 - 1) - x;
                int col = (d == 0) ? 0xFF2E3137
                        : ((d == bevelW - 1) ? 0xFF08090B
                        : 0xFF15171B);
                for (int y = uY1 + 2; y < uY2 - 2; y++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bW)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }
    }

    private static boolean isInsideSidePlateau(int px, int py, boolean isLeft, int uX1, int uX2, int uY1, int uY2,
                                                int cutX1, int cutX2, int cutY1, int cutY2, int rInner, int bW) {
        if (px < uX1 || px >= uX2 || py < uY1 || py >= uY2) return false;
        return getSideCutoutSDF(px, py, isLeft, cutX1, cutX2, cutY1, cutY2, rInner, bW) >= 0;
    }

    private static float getSideCutoutSDF(int px, int py, boolean isLeft, int cutX1, int cutX2, int cutY1, int cutY2,
                                           int rInner, int bW) {
        int x1 = bW; // 9px

        if (isLeft) {
            // Cutout ceiling & floor with 45-degree diagonal flare starting from outer edge X=0 to vertical line X=bW (9)
            int curCutY1 = (px < x1) ? (cutY1 + (x1 - px)) : cutY1;
            int curCutY2 = (px < x1) ? (cutY2 - (x1 - px)) : cutY2;

            if (px > cutX2 - rInner) {
                if (py < cutY1 + rInner) {
                    float dx = px - (cutX2 - rInner), dy = py - (cutY1 + rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                }
                if (py > cutY2 - rInner) {
                    float dx = px - (cutX2 - rInner), dy = py - (cutY2 - rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                }
                return (float) (px - cutX2);
            }
            return (float) Math.max(curCutY1 - py, py - curCutY2);
        } else {
            // Right Side cutout ceiling & floor with 45-degree diagonal flare from X=980 to X=971
            int distFromRight = TabletFrame.DESIGN_W - 1 - px;
            int curCutY1 = (distFromRight < x1) ? (cutY1 + (x1 - distFromRight)) : cutY1;
            int curCutY2 = (distFromRight < x1) ? (cutY2 - (x1 - distFromRight)) : cutY2;

            if (px < cutX1 + rInner) {
                if (py < cutY1 + rInner) {
                    float dx = px - (cutX1 + rInner), dy = py - (cutY1 + rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                }
                if (py > cutY2 - rInner) {
                    float dx = px - (cutX1 + rInner), dy = py - (cutY2 - rInner);
                    return (float) (Math.sqrt(dx * dx + dy * dy) - rInner);
                }
                return (float) (cutX1 - px);
            }
            return (float) Math.max(curCutY1 - py, py - curCutY2);
        }
    }

    private static void bakeRecessedButtonPocket(NativeImage img, int cx, int cy, int size) {
        int px1 = cx - size / 2, py1 = cy - size / 2;
        int px2 = px1 + size, py2 = py1 + size;
        int pR = 6;

        for (int y = py1 - 2; y <= py2 + 2; y++) {
            for (int x = px1 - 2; x <= px2 + 2; x++) {
                if (isInsideRoundedRect(x, y, px1 - 2, py1 - 2, px2 + 2, py2 + 2, pR + 2)
                        && !isInsideRoundedRect(x, y, px1, py1, px2, py2, pR)) {
                    setPixel(img, x, y, (y >= py2 || x >= px2) ? 0xFF353940 : 0xFF141518);
                }
            }
        }

        for (int y = py1; y < py2; y++) {
            for (int x = px1; x < px2; x++) {
                if (isInsideRoundedRect(x, y, px1, py1, px2, py2, pR)) {
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int col = (grain == 1) ? 0xFF101114 : ((grain == 2) ? 0xFF15161A : 0xFF121316);
                    setPixel(img, x, y, col);
                }
            }
        }

        for (int y = py1; y < py1 + 2; y++) {
            for (int x = px1 + pR; x < px2 - pR; x++)
                setPixel(img, x, y, 0xFF040506);
        }
        for (int y = py1 + pR; y < py2 - pR; y++) {
            for (int x = px1; x < px1 + 2; x++)
                setPixel(img, x, y, 0xFF040506);
        }
        for (int y = py2 - 2; y < py2; y++) {
            for (int x = px1 + pR; x < px2 - pR; x++)
                setPixel(img, x, y, 0xFF282B30);
        }
        for (int y = py1 + pR; y < py2 - pR; y++) {
            for (int x = px2 - 2; x < px2; x++)
                setPixel(img, x, y, 0xFF282B30);
        }
    }

    private static void bakeAllDividerRibs(NativeImage img) {
        int ribLen = 48;
        // Top row single ribs between inner key pairs (SA-WPN-DEF-SYS-DRV-STR-COM-BMS)
        for (int i = 1; i <= 7; i++) {
            int cx = 148 + 38 + i * 76;
            bakeCapsuleRib(img, cx, 41, ribLen, true);
        }
        // Bottom row single ribs between inner key pairs (F13-F14-F15-F16-F17-F18-F19-F20)
        for (int i = 1; i <= 7; i++) {
            int cx = 148 + 38 + i * 76;
            bakeCapsuleRib(img, cx, 589, ribLen, true);
        }
        // Left flank single ribs between all key pairs (CFF-F2-F3-F4-F5-F6)
        for (int i = 0; i <= 4; i++) {
            int cy = 155 + 32 + i * 64;
            bakeCapsuleRib(img, 39, cy, ribLen, false);
        }
        // Right flank single ribs between all key pairs (F7-F8-F9-F10-F11-F12)
        for (int i = 0; i <= 4; i++) {
            int cy = 155 + 32 + i * 64;
            bakeCapsuleRib(img, 941, cy, ribLen, false);
        }
    }

    private static void bakeCapsuleRib(NativeImage img, int cx, int cy, int len, boolean isVertical) {
        float ribRadius = 2.0f;
        int halfLen = len / 2;
        int rx1 = isVertical ? Math.round(cx - ribRadius) : cx - halfLen;
        int rx2 = isVertical ? Math.round(cx + ribRadius) : cx + halfLen;
        int ry1 = isVertical ? cy - halfLen : Math.round(cy - ribRadius);
        int ry2 = isVertical ? cy + halfLen : Math.round(cy + ribRadius);

        int boundX1 = rx1 - 2, boundX2 = rx2 + 2;
        int boundY1 = ry1 - 2, boundY2 = ry2 + 2;

        for (int y = boundY1; y <= boundY2; y++) {
            for (int x = boundX1; x <= boundX2; x++) {
                float[] geom = getCapsuleDist(x, y, cx, cy, rx1, ry1, rx2, ry2, ribRadius, isVertical);
                float dist = geom[0], projX = geom[1], projY = geom[2];

                if (dist <= ribRadius) {
                    float nx = (x - projX) / ribRadius;
                    float ny = (y - projY) / ribRadius;
                    float nz2 = 1.0f - (nx * nx + ny * ny);
                    float nz = nz2 > 0 ? (float) Math.sqrt(nz2) : 0f;

                    float lx = -0.55f, ly = -0.55f, lz = 0.62f;
                    float dot = Math.max(0, nx * lx + ny * ly + nz * lz);
                    float spec = dot * dot;

                    int baseR = 30, baseG = 32, baseB = 36;
                    int lightR = 52, lightG = 55, lightB = 62;
                    int darkR = 12, darkG = 13, darkB = 15;

                    int cr, cg, cb;
                    if (nx + ny < 0) {
                        float factor = Math.min(1.0f, dot * 1.1f);
                        cr = Math.round(baseR + (lightR - baseR) * factor + spec * 10);
                        cg = Math.round(baseG + (lightG - baseG) * factor + spec * 10);
                        cb = Math.round(baseB + (lightB - baseB) * factor + spec * 10);
                    } else {
                        float factor = Math.min(1.0f, (nx + ny) / 1.4f);
                        cr = Math.round(baseR - (baseR - darkR) * factor);
                        cg = Math.round(baseG - (baseG - darkG) * factor);
                        cb = Math.round(baseB - (baseB - darkB) * factor);
                    }

                    cr = Math.min(255, Math.max(0, cr));
                    cg = Math.min(255, Math.max(0, cg));
                    cb = Math.min(255, Math.max(0, cb));

                    setPixel(img, x, y, (0xFF << 24) | (cr << 16) | (cg << 8) | cb);
                } else if (dist <= ribRadius + 1.2f) {
                    setPixel(img, x, y, 0x44040506);
                }
            }
        }
    }

    private static float[] getCapsuleDist(float px, float py, float cx, float cy, float rx1, float ry1, float rx2,
            float ry2, float ribRadius, boolean isVertical) {
        float segX1 = isVertical ? cx : rx1 + ribRadius;
        float segX2 = isVertical ? cx : rx2 - ribRadius;
        float segY1 = isVertical ? ry1 + ribRadius : cy;
        float segY2 = isVertical ? ry2 - ribRadius : cy;

        float dx = segX2 - segX1, dy = segY2 - segY1;
        float l2 = dx * dx + dy * dy;
        float t = (l2 > 0) ? Math.max(0, Math.min(1, ((px - segX1) * dx + (py - segY1) * dy) / l2)) : 0;
        float projX = segX1 + t * dx, projY = segY1 + t * dy;
        float dist = (float) Math.sqrt((px - projX) * (px - projX) + (py - projY) * (py - projY));
        return new float[] { dist, projX, projY };
    }

    // =========================================================================
    // SHARED PIXEL PRIMITIVES
    // =========================================================================
    private static boolean isInsideRoundedRect(int px, int py, int rx1, int ry1, int rx2, int ry2, float radius) {
        if (px < rx1 || px >= rx2 || py < ry1 || py >= ry2)
            return false;
        if (px < rx1 + radius && py < ry1 + radius) {
            float dx = px - (rx1 + radius), dy = py - (ry1 + radius);
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px >= rx2 - radius && py < ry1 + radius) {
            float dx = px - (rx2 - radius), dy = py - (ry1 + radius);
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px < rx1 + radius && py >= ry2 - radius) {
            float dx = px - (rx1 + radius), dy = py - (ry2 - radius);
            return dx * dx + dy * dy <= radius * radius;
        }
        if (px >= rx2 - radius && py >= ry2 - radius) {
            float dx = px - (rx2 - radius), dy = py - (ry2 - radius);
            return dx * dx + dy * dy <= radius * radius;
        }
        return true;
    }

    private static int applyStipple(int baseCol, int x, int y) {
        int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
        if (grain == 0)
            return baseCol;
        int a = (baseCol >>> 24) & 0xFF;
        int r = (baseCol >>> 16) & 0xFF;
        int g = (baseCol >>> 8) & 0xFF;
        int b = baseCol & 0xFF;
        int delta = (grain == 1) ? -2 : 2;
        r = Math.max(0, Math.min(255, r + delta));
        g = Math.max(0, Math.min(255, g + delta));
        b = Math.max(0, Math.min(255, b + delta));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void setPixel(NativeImage img, int x, int y, int argb) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight())
            return;
        int a = (argb >>> 24) & 0xFF;
        if (a == 0)
            return;
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        if (a < 255) {
            int prev = img.getPixelRGBA(x, y);
            int prevR = prev & 0xFF;
            int prevG = (prev >>> 8) & 0xFF;
            int prevB = (prev >>> 16) & 0xFF;
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

    private static void bakeKeySprite(NativeImage img, int kx, int ky, int w, int h, int roundRadius, int dropShadow, int borderDark, int wallExtrusion, int shoulderLight, int rimTop, int dishBase, int dishShadow, int dishHighlight, boolean pressed) {
        // 1. 1px dark outer socket border (Uniform on all keys)
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isInsideRounded(x, y, w, h, roundRadius)) {
                    setPixel(img, kx + x, ky + y, borderDark);
                }
            }
        }

        // 2. Raised outer rim body (Symmetric 1px bevel on top/left/right/bottom)
        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                if (isInsideRounded(x - 1, y - 1, w - 2, h - 2, roundRadius - 1)) {
                    int rimCol = rimTop;
                    if (y == 1 || x == 1) {
                        rimCol = shoulderLight; // 1px Top & Left light highlight
                    } else if (y == h - 2 || x == w - 2) {
                        rimCol = wallExtrusion; // 1px Bottom & Right shadow
                    }
                    setPixel(img, kx + x, ky + y, rimCol);
                }
            }
        }

        // 3. Recessed Dish Floor
        int rimThickness = 4;
        int dishW = w - rimThickness * 2;
        int dishH = h - rimThickness * 2;
        int dishX = kx + rimThickness;
        int dishY = ky + rimThickness;
        int innerRadius = Math.max(2, roundRadius - rimThickness + 1);

        // 3a. Fill recessed dish floor
        for (int y = 0; y < dishH; y++) {
            for (int x = 0; x < dishW; x++) {
                if (isInsideRounded(x, y, dishW, dishH, innerRadius)) {
                    setPixel(img, dishX + x, dishY + y, dishBase);
                }
            }
        }

        // 3b. Inner top & left uniform 1px drop shadow
        for (int x = 0; x < dishW; x++) {
            if (isInsideRounded(x, 0, dishW, dishH, innerRadius)) {
                setPixel(img, dishX + x, dishY, dishShadow);
            }
        }
        for (int y = 0; y < dishH; y++) {
            if (isInsideRounded(0, y, dishW, dishH, innerRadius)) {
                setPixel(img, dishX, dishY + y, dishShadow);
            }
        }

        // 3c. Inner bottom & right uniform 1px highlight
        for (int x = 0; x < dishW; x++) {
            if (isInsideRounded(x, dishH - 1, dishW, dishH, innerRadius)) {
                setPixel(img, dishX + x, dishY + dishH - 1, dishHighlight);
            }
        }
    }

    private static boolean isInsideRounded(int px, int py, int w, int h, int r) {
        if (px < 0 || px >= w || py < 0 || py >= h)
            return false;
        if (r <= 0)
            return true;
        if (px < r && py < r) {
            int dx = r - px - 1, dy = r - py - 1;
            return dx * dx + dy * dy <= r * r;
        }
        if (px >= w - r && py < r) {
            int dx = px - (w - r), dy = r - py - 1;
            return dx * dx + dy * dy <= r * r;
        }
        if (px < r && py >= h - r) {
            int dx = r - px - 1, dy = py - (h - r);
            return dx * dx + dy * dy <= r * r;
        }
        if (px >= w - r && py >= h - r) {
            int dx = px - (w - r), dy = py - (h - r);
            return dx * dx + dy * dy <= r * r;
        }
        return true;
    }

    private static void bakeLedSprite(NativeImage img, int lx, int ly, int w, int h, boolean lit, int color) {
        boolean isVert = h > w;

        if (lit) {
            // High-luminance saturated active laser light-pipe
            int baseCol = (color == 0) ? 0xFF00FF66 : color;
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    setPixel(img, lx + x, ly + y, baseCol);
                }
            }
            // Axial optical core filament (pure white hot core)
            if (isVert) {
                for (int y = 1; y < h - 1; y++) {
                    setPixel(img, lx + 1, ly + y, 0xFFFFFFFF);
                    setPixel(img, lx + 2, ly + y, 0xFFFFFFFF);
                }
            } else {
                for (int x = 1; x < w - 1; x++) {
                    setPixel(img, lx + x, ly + 1, 0xFFFFFFFF);
                    setPixel(img, lx + x, ly + 2, 0xFFFFFFFF);
                }
            }
        } else {
            // Unlit Optical Frosted Polycarbonate Light-Pipe Capsule (Thấu kính khói đối xứng 100%)
            // 1. Perfectly uniform 1px recessed dark bezel (đúng 1px đều cả 4 cạnh)
            for (int y = -1; y <= h; y++) {
                for (int x = -1; x <= w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF08090C);
                }
            }

            // 2. Uniform smoked translucent optical lens body
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF343C48);
                }
            }

            // 3. Symmetric optical core filament (đối xứng hoàn hảo, không có bóng đổ lệch hướng gây dày viền)
            if (isVert) {
                for (int y = 1; y < h - 1; y++) {
                    setPixel(img, lx + 1, ly + y, 0xFF4E5868);
                    setPixel(img, lx + 2, ly + y, 0xFF4E5868);
                }
            } else {
                for (int x = 1; x < w - 1; x++) {
                    setPixel(img, lx + x, ly + 1, 0xFF4E5868);
                    setPixel(img, lx + x, ly + 2, 0xFF4E5868);
                }
            }
        }
    }

    private static void drawScreenCornerMasks(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight(), r = 32;
        for (int y = 0; y < r; y++) {
            for (int x = 0; x < r; x++) {
                if ((r - x) * (r - x) + (r - y) * (r - y) > r * r) {
                    setPixel(img, x, y, 0xFF000000);
                    setPixel(img, w - 1 - x, y, 0xFF000000);
                    setPixel(img, x, h - 1 - y, 0xFF000000);
                    setPixel(img, w - 1 - x, h - 1 - y, 0xFF000000);
                }
            }
        }
    }

    private static void fillCircle(NativeImage img, int cx, int cy, int radius, int argb) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    setPixel(img, cx + x, cy + y, argb);
                }
            }
        }
    }

    // =========================================================================
    // 32 PRE-BAKED MASTER TACTICAL KEYS & 32 UNLIT LEDS
    // =========================================================================
    private static final Map<Character, int[]> GLYPHS = new HashMap<>();

    static {
        GLYPHS.put(' ', new int[] { 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000 });
        GLYPHS.put('-', new int[] { 0b00000, 0b00000, 0b00000, 0b11111, 0b00000, 0b00000, 0b00000 });
        GLYPHS.put('+', new int[] { 0b00000, 0b00100, 0b00100, 0b11111, 0b00100, 0b00100, 0b00000 });
        GLYPHS.put(':', new int[] { 0b00000, 0b01100, 0b01100, 0b00000, 0b01100, 0b01100, 0b00000 });
        GLYPHS.put('0', new int[] { 0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('1', new int[] { 0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110 });
        GLYPHS.put('2', new int[] { 0b01110, 0b10001, 0b00001, 0b00110, 0b01000, 0b10000, 0b11111 });
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
        GLYPHS.put('I', new int[] { 0b01110, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110 });
        GLYPHS.put('J', new int[] { 0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b10010, 0b01100 });
        GLYPHS.put('L', new int[] { 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111 });
        GLYPHS.put('M', new int[] { 0b10001, 0b11011, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('N', new int[] { 0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001 });
        GLYPHS.put('O', new int[] { 0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('P', new int[] { 0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000 });
        GLYPHS.put('R', new int[] { 0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10001, 0b10001 });
        GLYPHS.put('S', new int[] { 0b01111, 0b10000, 0b10000, 0b01110, 0b00001, 0b00001, 0b11110 });
        GLYPHS.put('T', new int[] { 0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100 });
        GLYPHS.put('U', new int[] { 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110 });
        GLYPHS.put('V', new int[] { 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100 });
        GLYPHS.put('W', new int[] { 0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001 });
        GLYPHS.put('Y', new int[] { 0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100 });
    }

    private static void bakeAllDefaultKeysAndLeds(NativeImage img) {
        int keySize = 44;
        int half = keySize / 2;

        // 1. Top Row (10 Keys centered at ROW_TOP_Y = 41 + 8 LEDs at LED_ROW_TOP_Y = 72, 4x8)
        String[] topLabels = {"GRID", "SA", "WPN", "DEF", "STA", "DRV", "STR", "LOG", "BTY", null};
        for (int i = 0; i < 10; i++) {
            int cx = 148 + i * 76;
            int cy = 41;
            if (i == 0 || i == 9) {
                bakeSunkenButtonWell(img, cx, cy);
            }
            bakeKeySocket(img, cx - half, cy - half, keySize, keySize);
            bakeSingleKey(img, cx, cy, topLabels[i], (i == 9) ? UiButton.Mark.BRIGHT : null, false);
            if (i != 0 && i != 9) {
                bakeLedSprite(img, cx - 2, 72, 4, 8, false, 0);
            }
        }

        // 2. Left Flank (6 Keys F1-F6 centered at COL_LEFT_X = 39 + 6 LEDs at LED_COL_LEFT_X = 71, 8x4)
        for (int i = 0; i < 6; i++) {
            int cx = 39;
            int cy = 155 + i * 64;
            bakeKeySocket(img, cx - half, cy - half, keySize, keySize);
            bakeSingleKey(img, cx, cy, "F" + (i + 1), null, false);
            bakeLedSprite(img, 71, cy - 2, 8, 4, false, 0);
        }

        // 3. Right Flank (6 Keys F7-F12 centered at COL_RIGHT_X = 941 + 6 LEDs at LED_COL_RIGHT_X = 900, 8x4)
        for (int i = 0; i < 6; i++) {
            int cx = 941;
            int cy = 155 + i * 64;
            bakeKeySocket(img, cx - half, cy - half, keySize, keySize);
            bakeSingleKey(img, cx, cy, "F" + (i + 7), null, false);
            bakeLedSprite(img, 900, cy - 2, 8, 4, false, 0);
        }

        // 4. Bottom Row (10 Keys centered at ROW_BOTTOM_Y = 589 + 8 LEDs at LED_ROW_BOTTOM_Y = 549, 4x8)
        for (int i = 0; i < 10; i++) {
            int cx = 148 + i * 76;
            int cy = 589;
            if (i == 0 || i == 9) {
                bakeSunkenButtonWell(img, cx, cy);
            }
            bakeKeySocket(img, cx - half, cy - half, keySize, keySize);
            if (i == 0) {
                bakeSingleKey(img, cx, cy, null, UiButton.Mark.FILTER, false);
            } else if (i == 9) {
                bakeSingleKey(img, cx, cy, null, UiButton.Mark.POWER, true);
            } else {
                bakeSingleKey(img, cx, cy, "F" + (i + 12), null, false);
                bakeLedSprite(img, cx - 2, 549, 4, 8, false, 0);
            }
        }
    }

    private static void bakeSingleKey(NativeImage img, int cx, int cy, String label, UiButton.Mark mark, boolean redKey) {
        int keySize = 44;
        int half = keySize / 2;
        int kx = cx - half;
        int ky = cy - half;
        int w = keySize, h = keySize;

        int borderDark = redKey ? 0xFF240606 : 0xFF14161B;
        int shoulderLight = redKey ? 0xFFF05252 : 0xFF7E8898;
        int rimTop = redKey ? 0xFFE03838 : 0xFF667080;
        int wallExtrusion = redKey ? 0xFF8A1A1A : 0xFF3A404C;
        int dishBase = redKey ? 0xFFA81E1E : 0xFF444A56;
        int dishShadow = redKey ? 0xFF5A0C0C : 0xFF262A32;
        int dishHighlight = redKey ? 0xFFD42828 : 0xFF586272;

        bakeKeySprite(img, kx, ky, w, h, 8, 0, borderDark, wallExtrusion, shoulderLight, rimTop, dishBase, dishShadow, dishHighlight, false);

        int textCol = redKey ? 0xFFFFFFFF : 0xFFF0F4FA;
        if (mark != null) {
            drawMarkToImage(img, cx, cy, mark, textCol);
        } else if (label != null && !label.isEmpty()) {
            rasterizePixelString(img, label, cx, cy, 2, textCol);
        }
    }

    private static void drawMarkToImage(NativeImage img, int cx, int cy, UiButton.Mark mark, int color) {
        switch (mark) {
            case BRIGHT -> {
                // Sunburst (8 rays + center)
                fillCircle(img, cx, cy, 4, color);
                for (int d = 6; d <= 9; d++) {
                    setPixel(img, cx, cy - d, color);
                    setPixel(img, cx, cy + d, color);
                    setPixel(img, cx - d, cy, color);
                    setPixel(img, cx + d, cy, color);
                    int diag = (int) Math.round(d * 0.7071);
                    setPixel(img, cx - diag, cy - diag, color);
                    setPixel(img, cx + diag, cy - diag, color);
                    setPixel(img, cx - diag, cy + diag, color);
                    setPixel(img, cx + diag, cy + diag, color);
                }
            }
            case POWER -> {
                int radius = 8;
                int rIn2 = 5 * 5;
                int rOut2 = 8 * 8;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= rOut2 && d2 >= rIn2) {
                            if (dy <= -4 && Math.abs(dx) <= 2) continue; // Top opening gap
                            setPixel(img, cx + dx, cy + dy, color);
                        }
                    }
                }
                // 2px wide vertical power stem
                for (int y = -8; y <= 0; y++) {
                    setPixel(img, cx, cy + y, color);
                    setPixel(img, cx - 1, cy + y, color);
                }
            }
            case FILTER -> {
                // Crescent moon (right side)
                for (int dy = -6; dy <= 6; dy++) {
                    for (int dx = 0; dx <= 6; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= 36 && (dx >= 2 || Math.abs(dy) <= 2)) {
                            if ((dx - 4) * (dx - 4) + dy * dy > 12) {
                                setPixel(img, cx + dx, cy + dy, color);
                            }
                        }
                    }
                }
                // 3 Sun rays (left side)
                for (int dx = -8; dx <= -4; dx++) {
                    setPixel(img, cx + dx, cy, color);
                }
                for (int d = 3; d <= 6; d++) {
                    setPixel(img, cx - d, cy - d, color);
                    setPixel(img, cx - d, cy + d, color);
                }
            }
            default -> {}
        }
    }

    private static void bakeSunkenButtonWell(NativeImage img, int cx, int cy) {
        int keyW = 44, keyH = 44;
        int wellPad = 5; // Sculpted 5px chamfered milled recess
        int outW = keyW + wellPad * 2; // 54px
        int outH = keyH + wellPad * 2; // 54px
        int outR = 11;
        int inR = 8;
        int ox1 = cx - outW / 2, oy1 = cy - outH / 2;
        int ox2 = ox1 + outW, oy2 = oy1 + outH;
        int ix1 = cx - keyW / 2, iy1 = cy - keyH / 2;
        int ix2 = ix1 + keyW, iy2 = iy1 + keyH;

        for (int y = oy1; y < oy2; y++) {
            for (int x = ox1; x < ox2; x++) {
                if (isInsideRoundedRect(x, y, ox1, oy1, ox2, oy2, outR)) {
                    if (isInsideRoundedRect(x, y, ix1, iy1, ix2, iy2, inR)) {
                        // Deep socket floor
                        setPixel(img, x, y, 0xFF0A0B0E);
                    } else {
                        // Symmetric chamfered milled bevel (equal thickness on all 4 sides, no directional light shadow)
                        int dOut = Math.min(Math.min(x - ox1, ox2 - 1 - x), Math.min(y - oy1, oy2 - 1 - y));
                        int col = switch (Math.min(dOut, 4)) {
                            case 0 -> 0xFF242830;
                            case 1 -> 0xFF1C1E24;
                            case 2 -> 0xFF15171C;
                            case 3 -> 0xFF0F1014;
                            default -> 0xFF0A0B0E;
                        };
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }
    }

    private static void bakeKeySocket(NativeImage img, int kx, int ky, int w, int h) {
        int r = 8;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isInsideRounded(x, y, w, h, r)) {
                    setPixel(img, kx + x, ky + y, 0xFF0A0B0E);
                }
            }
        }
    }

    private static void bakeLedSocket(NativeImage img, int lx, int ly, int w, int h) {
        for (int y = -1; y <= h; y++) {
            for (int x = -1; x <= w; x++) {
                setPixel(img, lx + x, ly + y, 0xFF08090C);
            }
        }
    }

    private static void rasterizePixelString(NativeImage img, String text, int cx, int cy, int fontScale, int color) {
        String upper = text.toUpperCase();
        int charW = 5 * fontScale;
        int charSp = 1 * fontScale;
        int totalW = upper.length() * charW + (upper.length() - 1) * charSp;
        int startX = Math.round(cx - totalW / 2.0f);
        int startY = Math.round(cy - (7 * fontScale) / 2.0f);

        int curX = startX;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = GLYPHS.getOrDefault(ch, GLYPHS.get(' '));
            for (int r = 0; r < 7; r++) {
                int row = glyph[r];
                for (int c = 0; c < 5; c++) {
                    if (((row >> (4 - c)) & 1) == 1) {
                        for (int dy = 0; dy < fontScale; dy++) {
                            for (int dx = 0; dx < fontScale; dx++) {
                                setPixel(img, curX + c * fontScale + dx, startY + r * fontScale + dy, color);
                            }
                        }
                    }
                }
            }
            curX += (5 + 1) * fontScale;
        }
    }
}
