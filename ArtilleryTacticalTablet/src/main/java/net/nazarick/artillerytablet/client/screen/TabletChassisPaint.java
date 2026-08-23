package net.nazarick.artillerytablet.client.screen;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The chassis: carbon-composite shell with true transparent corners, SDF collars around the
 * screen well, a real tactical grid baked into the well floor, SDF recessed corner pockets,
 * lit capsule divider ribs, layered hex bolts.
 *
 * <p>Ported verbatim (same colours, same design-space maths, same pixel order) from the shell
 * portion of a visually-only prototype the user supplied. The prototype's newest revision also
 * bakes the 32 keys' idle look, all 29 LEDs, and button hover/press sprites into the same atlas —
 * deliberately NOT ported here, because those bake once and never change, while this mod's keys
 * and LEDs must keep reflecting live game state (which tab is open, armed/danger, grid on/off,
 * screen power) every frame; {@link UiButton} still draws itself and its own lamp for exactly
 * that reason. Only the static background — the part with no game state behind it — is baked.
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
        int r = 18;
        int bW = 9;
        int cornerW = 45;
        int cornerH = 45;

        // 1. Base rim, true-transparent outside the rounded rect (no square corners bleeding
        // through) rather than a filled drop shadow.
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

        // 3. Four soft-corner chamfer facets
        for (int cy = 1; cy < bW; cy++) {
            int d = cy - 1;
            int col = (d == 0) ? 0xFF2D2F33 : ((d == 1) ? 0xFF282A2E : ((d == bW - 2) ? 0xFF1A1C1E : 0xFF222427));
            for (int cx = cornerW; cx < w - cornerW; cx++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cx = 1; cx < bW; cx++) {
            int d = cx - 1;
            int col = (d == 0) ? 0xFF2A2C30 : ((d == 1) ? 0xFF242629 : ((d == bW - 2) ? 0xFF191A1D : 0xFF1E2023));
            for (int cy = cornerH; cy < h - cornerH; cy++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cy = h - bW; cy < h - 1; cy++) {
            int d = (h - 2) - cy;
            int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0B0C0E : ((d == bW - 2) ? 0xFF131416 : 0xFF0E0F11));
            for (int cx = cornerW; cx < w - cornerW; cx++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }
        for (int cx = w - bW; cx < w - 1; cx++) {
            int d = (w - 2) - cx;
            int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0B0C0E : ((d == bW - 2) ? 0xFF131416 : 0xFF0F1012));
            for (int cy = cornerH; cy < h - cornerH; cy++) {
                setPixel(img, cx, cy, applyStipple(col, cx, cy));
            }
        }

        // 4. Stepped corners, each with its own layered hex bolt
        bakeSteppedCorner(img, 0, 0, true, true, cornerW, cornerH, 7, r, bW);
        bakeSteppedCorner(img, w - 45, 0, false, true, cornerW, cornerH, 7, r, bW);
        bakeSteppedCorner(img, 0, h - 45, true, false, cornerW, cornerH, 7, r, bW);
        bakeSteppedCorner(img, w - 45, h - 45, false, false, cornerW, cornerH, 7, r, bW);

        // 5. Screen bezel well + tactical grid baked into the well floor
        bakeScreenAndTacticalGrid(img);

        // 6. Top & bottom raised U-collars
        bakeRaisedUCollar(img, false, bW);
        bakeRaisedUCollar(img, true, bW);

        // 7. Left & right C-brackets
        bakeSideCBracket(img, true, bW);
        bakeSideCBracket(img, false, bW);

        // 9. Lit capsule divider ribs
        bakeAllDividerRibs(img);

        // 10. Dynamic high-resolution Keycap & LED Sprite Atlas Bank
        bakeDynamicKeySprites(img);

        // 11. Bake all 32 default tactical keys and unlit LEDs directly onto 980px chassis
        bakeAllDefaultKeysAndLeds(img);

        return img;
    }

    /** Stretches the baked design-space image over whatever rectangle the frame is drawing at. */
    static void blit(GuiGraphics g, TabletFrame frame, ResourceLocation texture) {
        int x = frame.toScreenX(0);
        int y = frame.toScreenY(0);
        int w = frame.toScreenW(TabletFrame.DESIGN_W);
        int h = frame.toScreenH(TabletFrame.DESIGN_H);
        g.blit(texture, x, y, w, h, 0f, 0f, TabletFrame.DESIGN_W, TabletFrame.DESIGN_H,
                TabletFrame.DESIGN_W, TabletFrame.DESIGN_H);
    }

    private static void bakeSteppedCorner(NativeImage img, int vx, int vy, boolean isLeft, boolean isTop,
                                           int cornerW, int cornerH, int rIn, int outerR, int bW) {
        int kx1 = vx, ky1 = vy, kx2 = kx1 + cornerW, ky2 = ky1 + cornerH;

        for (int y = ky1; y < ky2; y++) {
            for (int x = kx1; x < kx2; x++) {
                if (!isInsideRoundedRect(x, y, 0, 0, TabletFrame.DESIGN_W, TabletFrame.DESIGN_H, outerR)) continue;

                boolean inCut = false;
                if (isLeft && isTop) {
                    if (x > kx2 - rIn && y > ky2 - rIn) {
                        int dx = x - (kx2 - rIn), dy = y - (ky2 - rIn);
                        if (dx * dx + dy * dy > rIn * rIn) inCut = true;
                    }
                } else if (!isLeft && isTop) {
                    if (x < kx1 + rIn && y > ky2 - rIn) {
                        int dx = x - (kx1 + rIn), dy = y - (ky2 - rIn);
                        if (dx * dx + dy * dy > rIn * rIn) inCut = true;
                    }
                } else if (isLeft) {
                    if (x > kx2 - rIn && y < ky1 + rIn) {
                        int dx = x - (kx2 - rIn), dy = y - (ky1 + rIn);
                        if (dx * dx + dy * dy > rIn * rIn) inCut = true;
                    }
                } else {
                    if (x < kx1 + rIn && y < ky1 + rIn) {
                        int dx = x - (kx1 + rIn), dy = y - (ky1 + rIn);
                        if (dx * dx + dy * dy > rIn * rIn) inCut = true;
                    }
                }

                if (!inCut) {
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int col = (grain == 1) ? 0xFF0E0F11 : ((grain == 2) ? 0xFF121315 : 0xFF101113);
                    setPixel(img, x, y, col);
                }
            }
        }

        if (isLeft && isTop) {
            for (int y = ky1 + bW; y < ky2 - rIn; y++) setPixel(img, kx2, y, 0xFF2A2C30);
            for (int x = kx1 + bW; x < kx2 - rIn; x++) setPixel(img, x, ky2, 0xFF2A2C30);
        } else if (!isLeft && isTop) {
            for (int y = ky1 + bW; y < ky2 - rIn; y++) setPixel(img, kx1, y, 0xFF090A0C);
            for (int x = kx1 + rIn; x < kx2 - bW; x++) setPixel(img, x, ky2, 0xFF2A2C30);
        } else if (isLeft) {
            for (int x = kx1 + bW; x < kx2 - rIn; x++) setPixel(img, x, ky1, 0xFF090A0C);
            for (int y = ky1 + rIn; y < ky2 - bW; y++) setPixel(img, kx2, y, 0xFF2A2C30);
        } else {
            for (int y = ky1 + rIn; y < ky2 - bW; y++) setPixel(img, kx1, y, 0xFF090A0C);
            for (int x = kx1 + rIn; x < kx2 - bW; x++) setPixel(img, x, ky1, 0xFF090A0C);
        }

        int cx = vx + 22, cy = vy + 22, boltR = 5;
        fillCircle(img, cx, cy, boltR + 2, 0xFF030304);
        fillCircle(img, cx, cy, boltR + 1, 0xFF08090B);
        fillCircle(img, cx - 1, cy - 1, boltR, 0xFF030304);
        fillCircle(img, cx, cy, boltR - 2, 0xFF141518);
        fillCircle(img, cx, cy, Math.max(1, boltR - 4), 0xFF050507);
    }

    private static void bakeScreenAndTacticalGrid(NativeImage img) {
        int mapX = TabletFrame.SCR_X, mapY = TabletFrame.SCR_Y, mapW = TabletFrame.SCR_W, mapH = TabletFrame.SCR_H;
        int scrR = 10, bevelW = 8;
        int outR = scrR + bevelW;
        int outX1 = mapX - bevelW, outY1 = mapY - bevelW;
        int outX2 = mapX + mapW + bevelW, outY2 = mapY + mapH + bevelW;

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

        for (int y = mapY; y < mapY + mapH; y++) {
            for (int x = mapX; x < mapX + mapW; x++) {
                if (isInsideRoundedRect(x, y, mapX, mapY, mapX + mapW, mapY + mapH, scrR)) {
                    int col = 0xFF02070A;
                    int relX = x - mapX;
                    int relY = y - mapY;
                    if (relX % 32 == 0 || relY % 32 == 0) {
                        col = 0xFF06151C;
                    }
                    if (relX % 160 == 0 || relY % 160 == 0) {
                        col = 0xFF0A222C;
                    }
                    setPixel(img, x, y, col);
                }
            }
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
                if (isInsideUPlateau(x - 4, y - 4, isTop, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)
                        && !isInsideUPlateau(x, y, isTop, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
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
            for (int y = uY1; y < uY2; y++) {
                for (int x = uX1; x < uX2; x++) {
                    if (!isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) continue;
                    if (x < uX1 + 2) setPixel(img, x, y, applyStipple(0xFF36393E, x, y));
                    else if (x >= uX2 - 2) setPixel(img, x, y, applyStipple(0xFF050506, x, y));
                }
            }

            int bEdgeY = TabletFrame.DESIGN_H - 9;
            for (int y = bEdgeY; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0B0C0E : (y <= bEdgeY + 1 ? 0xFF16171A : 0xFF0E0F11));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int bevelH = 8;
            for (int y = uY1; y < uY1 + bevelH; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF141518 : ((d == 1) ? 0xFF191B1E : (d == bevelH - 2 ? 0xFF26282D : (d == bevelH - 1 ? 0xFF2B2D32 : 0xFF1F2125)));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        } else {
            for (int y = uY1; y < uY2; y++) {
                for (int x = uX1; x < uX2; x++) {
                    if (!isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) continue;
                    if (x < uX1 + 2) setPixel(img, x, y, applyStipple(0xFF36393E, x, y));
                    else if (x >= uX2 - 2) setPixel(img, x, y, applyStipple(0xFF050506, x, y));
                }
            }

            int tEdgeY = 9;
            for (int y = uY1; y < tEdgeY; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF2D2F33 : ((d == 1) ? 0xFF282A2E : (y >= tEdgeY - 2 ? 0xFF1B1C1F : 0xFF222427));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }

            int bevelH = 8;
            for (int y = uY2 - bevelH; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08090B : ((d == 1) ? 0xFF0D0E10 : (d == bevelH - 2 ? 0xFF1F2125 : (d == bevelH - 1 ? 0xFF282A2F : 0xFF141619)));
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideUPlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }

        int boundY1 = !isTop ? cutY1 - 4 : uY1;
        int boundY2 = !isTop ? uY2 : cutY2 + 4;
        for (int y = boundY1; y <= boundY2; y++) {
            for (int x = cutX1 - 4; x <= cutX2 + 4; x++) {
                if (x < uX1 || x >= uX2 || y < uY1 || y >= uY2) continue;
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
        if (px < uX1 || px >= uX2 || py < uY1 || py >= uY2) return false;
        return getCutoutSDF(px, py, isTop, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer) >= 0;
    }

    private static float getCutoutSDF(int px, int py, boolean isTop, int cutX1, int cutX2, int cutY1, int cutY2,
                                       int rInner, int bChamfer) {
        // uY1/uY2 not needed once inside the plateau test above; only the cutout geometry matters.
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
            if (px < curCutX1) return (float) (curCutX1 - px);
            if (px > curCutX2) return (float) (px - curCutX2);
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
            if (px < curCutX1) return (float) (curCutX1 - px);
            if (px > curCutX2) return (float) (px - curCutX2);
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
        int rInner = 9, bChamfer = 9;

        for (int y = uY1 - 4; y <= uY2 + 5; y++) {
            for (int x = uX1; x <= uX2; x++) {
                if (isLeft && x >= TabletFrame.DESIGN_W - bW) continue;
                if (!isLeft && x <= bW) continue;
                if (isInsideSidePlateau(x - 3, y - 3, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)
                        && !isInsideSidePlateau(x, y, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                    setPixel(img, x, y, 0x33010102);
                } else if (isInsideSidePlateau(x - 1, y - 1, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)
                        && !isInsideSidePlateau(x, y, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                    setPixel(img, x, y, 0x66020203);
                }
            }
        }

        for (int y = uY1; y < uY2; y++) {
            for (int x = uX1; x < uX2; x++) {
                if (isInsideSidePlateau(x, y, isLeft, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                    int grain = ((x * 17 + y * 31) ^ (x * 11)) % 3;
                    int col = (grain == 1) ? 0xFF1F2124 : ((grain == 2) ? 0xFF232528 : 0xFF212326);
                    setPixel(img, x, y, col);
                }
            }
        }

        if (isLeft) {
            for (int x = uX1; x < uX1 + bW; x++) {
                int d = x - uX1;
                int col = (d == 0) ? 0xFF2A2C30 : ((d == 1) ? 0xFF242629 : (d == bW - 2 ? 0xFF191A1D : (d == bW - 1 ? 0xFF17181A : 0xFF222428)));
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int y = uY1; y < uY1 + 4; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF36393E : ((d == 1) ? 0xFF2C2F34 : 0xFF24262A);
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int y = uY2 - 4; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : 0xFF16171A);
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            int bevelW = 8;
            for (int x = uX2 - bevelW; x < uX2; x++) {
                int d = (uX2 - 1) - x;
                int col = (d == 0) ? 0xFF08090B : ((d == 1) ? 0xFF0D0E10 : (d == bevelW - 2 ? 0xFF1F2125 : (d == bevelW - 1 ? 0xFF282A2F : 0xFF141619)));
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        } else {
            for (int x = uX2 - bW; x < uX2; x++) {
                int d = (uX2 - 1) - x;
                int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0B0C0E : (d == bW - 2 ? 0xFF131416 : (d == bW - 1 ? 0xFF151618 : 0xFF0F1012)));
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int y = uY1; y < uY1 + 4; y++) {
                int d = y - uY1;
                int col = (d == 0) ? 0xFF36393E : ((d == 1) ? 0xFF2C2F34 : 0xFF24262A);
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            for (int y = uY2 - 4; y < uY2; y++) {
                int d = (uY2 - 1) - y;
                int col = (d == 0) ? 0xFF08080A : ((d == 1) ? 0xFF0E0F12 : 0xFF16171A);
                for (int x = uX1; x < uX2; x++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
            int bevelW = 8;
            for (int x = uX1; x < uX1 + bevelW; x++) {
                int d = x - uX1;
                int col = (d == 0) ? 0xFF141518 : ((d == 1) ? 0xFF191B1E : (d == bevelW - 2 ? 0xFF26282D : (d == bevelW - 1 ? 0xFF2B2D32 : 0xFF1F2125)));
                for (int y = uY1; y < uY2; y++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }

        int boundX1 = isLeft ? cutX1 : cutX1 - 4;
        int boundX2 = isLeft ? cutX2 + 4 : cutX2;
        int boundY1 = cutY1 - 4;
        int boundY2 = cutY2 + 4;

        for (int y = boundY1; y <= boundY2; y++) {
            for (int x = boundX1; x <= boundX2; x++) {
                if (x < uX1 || x >= uX2 || y < uY1 || y >= uY2) continue;
                float sdf = getSideCutoutSDF(x, y, isLeft, cutX1, cutX2, cutY1, cutY2, uX1, uX2, rInner, bChamfer);
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

        int bevelW = 8;
        if (isLeft) {
            int bx1 = uX2 - bevelW, bx2 = uX2;
            for (int x = bx1; x < bx2; x++) {
                int d = x - bx1;
                int col = (d == 0) ? 0xFF36393F : ((d == 1) ? 0xFF2B2D32 : (d == bevelW - 2 ? 0xFF101113 : (d == bevelW - 1 ? 0xFF0C0D0F : 0xFF181A1D)));
                for (int y = uY1 + 2; y < uY2 - 2; y++) {
                    if (isInsideSidePlateau(x, y, true, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        } else {
            int bx1 = uX1, bx2 = uX1 + bevelW;
            for (int x = bx1; x < bx2; x++) {
                int d = (bx2 - 1) - x;
                int col = (d == 0) ? 0xFF36393F : ((d == 1) ? 0xFF2B2D32 : (d == bevelW - 2 ? 0xFF101113 : (d == bevelW - 1 ? 0xFF0C0D0F : 0xFF181A1D)));
                for (int y = uY1 + 2; y < uY2 - 2; y++) {
                    if (isInsideSidePlateau(x, y, false, uX1, uX2, uY1, uY2, cutX1, cutX2, cutY1, cutY2, rInner, bChamfer)) {
                        setPixel(img, x, y, applyStipple(col, x, y));
                    }
                }
            }
        }
    }

    private static boolean isInsideSidePlateau(int px, int py, boolean isLeft, int uX1, int uX2, int uY1, int uY2,
                                                int cutX1, int cutX2, int cutY1, int cutY2, int rInner, int bChamfer) {
        if (px < uX1 || px >= uX2 || py < uY1 || py >= uY2) return false;
        return getSideCutoutSDF(px, py, isLeft, cutX1, cutX2, cutY1, cutY2, uX1, uX2, rInner, bChamfer) >= 0;
    }

    private static float getSideCutoutSDF(int px, int py, boolean isLeft, int cutX1, int cutX2, int cutY1, int cutY2,
                                           int uX1, int uX2, int rInner, int bChamfer) {
        if (isLeft) {
            int curCutY1 = cutY1, curCutY2 = cutY2;
            if (px <= uX1 + bChamfer) {
                int offset = (uX1 + bChamfer) - px;
                curCutY1 = cutY1 + offset;
                curCutY2 = cutY2 - offset;
            }
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
            int curCutY1 = cutY1, curCutY2 = cutY2;
            if (px >= uX2 - bChamfer) {
                int offset = px - (uX2 - bChamfer);
                curCutY1 = cutY1 + offset;
                curCutY2 = cutY2 - offset;
            }
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
            for (int x = px1 + pR; x < px2 - pR; x++) setPixel(img, x, y, 0xFF040506);
        }
        for (int y = py1 + pR; y < py2 - pR; y++) {
            for (int x = px1; x < px1 + 2; x++) setPixel(img, x, y, 0xFF040506);
        }
        for (int y = py2 - 2; y < py2; y++) {
            for (int x = px1 + pR; x < px2 - pR; x++) setPixel(img, x, y, 0xFF282B30);
        }
        for (int y = py1 + pR; y < py2 - pR; y++) {
            for (int x = px2 - 2; x < px2; x++) setPixel(img, x, y, 0xFF282B30);
        }
    }

    private static void bakeAllDividerRibs(NativeImage img) {
        int ribLen = 46;
        for (int i = 1; i <= 7; i++) bakeCapsuleRib(img, 148 + 38 + i * 76, 41, ribLen, true);
        for (int i = 1; i <= 7; i++) bakeCapsuleRib(img, 148 + 38 + i * 76, 589, ribLen, true);
        for (int i = 0; i < 5; i++) bakeCapsuleRib(img, 39, 155 + 32 + i * 64, ribLen, false);
        for (int i = 0; i < 5; i++) bakeCapsuleRib(img, 941, 155 + 32 + i * 64, ribLen, false);
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
        return new float[]{dist, projX, projY};
    }

    // =========================================================================
    // SHARED PIXEL PRIMITIVES
    // =========================================================================
    private static boolean isInsideRoundedRect(int px, int py, int rx1, int ry1, int rx2, int ry2, float radius) {
        if (px < rx1 || px >= rx2 || py < ry1 || py >= ry2) return false;
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
        if (grain == 0) return baseCol;
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
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) return;
        int a = (argb >>> 24) & 0xFF;
        if (a == 0) return;
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

    // High-resolution Keycap & LED Sprite Atlas Coordinates in the 980x630 texture
    public static final int SPRITE_KEY_W = 44;
    public static final int SPRITE_KEY_H = 44;

    public static final int SPRITE_KEY_PBT_IDLE_X = 120, SPRITE_KEY_PBT_IDLE_Y = 100;
    public static final int SPRITE_KEY_PBT_HOVER_X = 170, SPRITE_KEY_PBT_HOVER_Y = 100;
    public static final int SPRITE_KEY_PBT_PRESSED_X = 220, SPRITE_KEY_PBT_PRESSED_Y = 100;

    public static final int SPRITE_KEY_RED_IDLE_X = 270, SPRITE_KEY_RED_IDLE_Y = 100;
    public static final int SPRITE_KEY_RED_HOVER_X = 320, SPRITE_KEY_RED_HOVER_Y = 100;
    public static final int SPRITE_KEY_RED_PRESSED_X = 370, SPRITE_KEY_RED_PRESSED_Y = 100;

    public static final int SPRITE_LED_UNLIT_V_X = 120, SPRITE_LED_UNLIT_V_Y = 160;
    public static final int SPRITE_LED_UNLIT_H_X = 130, SPRITE_LED_UNLIT_H_Y = 160;
    public static final int SPRITE_LED_GREEN_V_X = 145, SPRITE_LED_GREEN_V_Y = 160;
    public static final int SPRITE_LED_GREEN_H_X = 155, SPRITE_LED_GREEN_H_Y = 160;
    public static final int SPRITE_LED_RED_V_X = 170, SPRITE_LED_RED_V_Y = 160;
    public static final int SPRITE_LED_RED_H_X = 180, SPRITE_LED_RED_H_Y = 160;
    public static final int SPRITE_LED_AMBER_V_X = 195, SPRITE_LED_AMBER_V_Y = 160;
    public static final int SPRITE_LED_AMBER_H_X = 205, SPRITE_LED_AMBER_H_Y = 160;

    private static void bakeDynamicKeySprites(NativeImage img) {
        int r = 7;
        // PBT Idle (Matte Ash-Gray PBT - Brighter, cleaner)
        bakeKeySprite(img, SPRITE_KEY_PBT_IDLE_X, SPRITE_KEY_PBT_IDLE_Y, 44, 44, r,
                0x88040508, 0xFF1C1F26, 0xFF323640, 0xFF5A6272, 0xFF626B7C, 0xFF444954, 0xFF2E323A, 0xFF545C6C, false);
        // PBT Hover
        bakeKeySprite(img, SPRITE_KEY_PBT_HOVER_X, SPRITE_KEY_PBT_HOVER_Y, 44, 44, r,
                0x88040508, 0xFF1C1F26, 0xFF3C424E, 0xFF6E788C, 0xFF768296, 0xFF505664, 0xFF363C46, 0xFF606A7C, false);
        // PBT Pressed
        bakeKeySprite(img, SPRITE_KEY_PBT_PRESSED_X, SPRITE_KEY_PBT_PRESSED_Y, 44, 44, r,
                0x88040508, 0xFF1C1F26, 0xFF282C34, 0xFF323640, 0xFF363B46, 0xFF30343E, 0xFF242830, 0xFF404654, true);

        // Red Idle
        bakeKeySprite(img, SPRITE_KEY_RED_IDLE_X, SPRITE_KEY_RED_IDLE_Y, 44, 44, r,
                0x88040508, 0xFF2A0808, 0xFF6A1414, 0xFFB82E2E, 0xFFC83636, 0xFF9C2222, 0xFF540E0E, 0xFFBE3232, false);
        // Red Hover
        bakeKeySprite(img, SPRITE_KEY_RED_HOVER_X, SPRITE_KEY_RED_HOVER_Y, 44, 44, r,
                0x88040508, 0xFF2A0808, 0xFF7E1818, 0xFFD43838, 0xFFE64646, 0xFFB22A2A, 0xFF641212, 0xFFD84040, false);
        // Red Pressed
        bakeKeySprite(img, SPRITE_KEY_RED_PRESSED_X, SPRITE_KEY_RED_PRESSED_Y, 44, 44, r,
                0x88040508, 0xFF2A0808, 0xFF460A0A, 0xFF5C1010, 0xFF681414, 0xFF561010, 0xFF300606, 0xFF6E1616, true);

        // LEDs with high-aura bloom (Unlit, Green, Red, Amber)
        bakeLedSprite(img, SPRITE_LED_UNLIT_V_X, SPRITE_LED_UNLIT_V_Y, 4, 8, false, 0);
        bakeLedSprite(img, SPRITE_LED_UNLIT_H_X, SPRITE_LED_UNLIT_H_Y, 8, 4, false, 0);

        bakeLedSprite(img, SPRITE_LED_GREEN_V_X, SPRITE_LED_GREEN_V_Y, 4, 8, true, 0xFF00FF66);
        bakeLedSprite(img, SPRITE_LED_GREEN_H_X, SPRITE_LED_GREEN_H_Y, 8, 4, true, 0xFF00FF66);

        bakeLedSprite(img, SPRITE_LED_RED_V_X, SPRITE_LED_RED_V_Y, 4, 8, true, 0xFFFF2233);
        bakeLedSprite(img, SPRITE_LED_RED_H_X, SPRITE_LED_RED_H_Y, 8, 4, true, 0xFFFF2233);

        bakeLedSprite(img, SPRITE_LED_AMBER_V_X, SPRITE_LED_AMBER_V_Y, 4, 8, true, 0xFFFFBB00);
        bakeLedSprite(img, SPRITE_LED_AMBER_H_X, SPRITE_LED_AMBER_H_Y, 8, 4, true, 0xFFFFBB00);
    }

    private static void bakeKeySprite(NativeImage img, int kx, int ky, int w, int h, int roundRadius,
                                      int dropShadow, int borderDark, int wallExtrusion,
                                      int shoulderLight, int rimTop, int dishBase,
                                      int dishShadow, int dishHighlight, boolean pressed) {
        // 1. Thin 1px dark perimeter socket border
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (isInsideRounded(x, y, w, h, roundRadius)) {
                    setPixel(img, kx + x, ky + y, borderDark);
                }
            }
        }

        // 2. Matte PBT Body (Xám tro nhạt hơn một chút, sạch sẽ, mỏng gọn)
        for (int y = 1; y < h - 1; y++) {
            int col = dishBase;
            if (y <= 2) col = shoulderLight;
            else if (y >= h - 3) col = wallExtrusion;

            for (int x = 1; x < w - 1; x++) {
                if (isInsideRounded(x - 1, y - 1, w - 2, h - 2, roundRadius - 1)) {
                    setPixel(img, kx + x, ky + y, col);
                }
            }
        }

        // 3. Clean 1px top highlight rim
        for (int x = roundRadius; x < w - roundRadius; x++) {
            setPixel(img, kx + x, ky + 1, rimTop);
        }

        // 4. Flat low-profile key face
        int inset = 3;
        int dishW = w - inset * 2;
        int dishH = h - inset * 2;
        int dishX = kx + inset;
        int dishY = ky + (pressed ? inset + 1 : inset);
        int dishRadius = Math.max(2, roundRadius - 2);

        for (int y = 0; y < dishH; y++) {
            for (int x = 0; x < dishW; x++) {
                if (isInsideRounded(x, y, dishW, dishH, dishRadius)) {
                    setPixel(img, dishX + x, dishY + y, dishBase);
                }
            }
        }

        // Top subtle shadow & bottom rim
        for (int x = dishRadius; x < dishW - dishRadius; x++) {
            setPixel(img, dishX + x, dishY, dishShadow);
            setPixel(img, dishX + x, dishY + dishH - 1, dishHighlight);
        }
    }

    private static boolean isInsideRounded(int px, int py, int w, int h, int r) {
        if (px < 0 || px >= w || py < 0 || py >= h) return false;
        if (r <= 0) return true;
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

    private static void bakeLedSprite(NativeImage img, int lx, int ly, int w, int h, boolean lit, int litCol) {
        if (lit) {
            int rgb = litCol & 0x00FFFFFF;
            // Moderate Phosphor Aura (Quầng sáng vừa phải, nổi bật mà không chói gắt)
            for (int dy = -2; dy < h + 2; dy++) {
                for (int dx = -2; dx < w + 2; dx++) {
                    int distSq = 0;
                    if (dx < 0) distSq += dx * dx;
                    else if (dx >= w) distSq += (dx - w + 1) * (dx - w + 1);
                    if (dy < 0) distSq += dy * dy;
                    else if (dy >= h) distSq += (dy - h + 1) * (dy - h + 1);

                    int alpha = 0;
                    if (distSq <= 1) alpha = 0x55;
                    else if (distSq <= 4) alpha = 0x22;

                    if (alpha > 0) {
                        setPixel(img, lx + dx, ly + dy, (alpha << 24) | rgb);
                    }
                }
            }

            // High-clarity diode body
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF000000 | rgb);
                }
            }

            // Soft white core dot (chấm sáng dịu ở tâm)
            int dotX = lx + w / 2 - 1;
            int dotY = ly + h / 2 - 1;
            for (int y = 0; y < 2; y++) {
                for (int x = 0; x < 2; x++) {
                    setPixel(img, dotX + x, dotY + y, 0xFFEFFFF5);
                }
            }
        } else {
            // Unlit compact translucent optical smoked glass with thin 1px recessed socket border
            for (int y = -1; y <= h; y++) {
                for (int x = -1; x <= w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF0E1014);
                }
            }
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    setPixel(img, lx + x, ly + y, 0xFF242A36);
                }
            }
            // Top reflection sheen
            for (int x = 0; x < w; x++) {
                setPixel(img, lx + x, ly, 0xFF4A5468);
            }
            // Bottom shadow
            for (int x = 0; x < w; x++) {
                setPixel(img, lx + x, ly + h - 1, 0xFF10141C);
            }
        }
    }

    public static void blitButton(GuiGraphics g, int destX, int destY, int destW, int destH,
                                  boolean isRed, boolean isHovered, boolean isPressed,
                                  ResourceLocation texture) {
        int u, v;
        if (isRed) {
            if (isPressed) {
                u = SPRITE_KEY_RED_PRESSED_X; v = SPRITE_KEY_RED_PRESSED_Y;
            } else if (isHovered) {
                u = SPRITE_KEY_RED_HOVER_X; v = SPRITE_KEY_RED_HOVER_Y;
            } else {
                u = SPRITE_KEY_RED_IDLE_X; v = SPRITE_KEY_RED_IDLE_Y;
            }
        } else {
            if (isPressed) {
                u = SPRITE_KEY_PBT_PRESSED_X; v = SPRITE_KEY_PBT_PRESSED_Y;
            } else if (isHovered) {
                u = SPRITE_KEY_PBT_HOVER_X; v = SPRITE_KEY_PBT_HOVER_Y;
            } else {
                u = SPRITE_KEY_PBT_IDLE_X; v = SPRITE_KEY_PBT_IDLE_Y;
            }
        }
        g.blit(texture, destX, destY, destW, destH, u, v, SPRITE_KEY_W, SPRITE_KEY_H, TabletFrame.DESIGN_W, TabletFrame.DESIGN_H);
    }

    public static void blitLed(GuiGraphics g, int destX, int destY, int destW, int destH,
                               boolean isLit, int colorType, boolean isVertical,
                               ResourceLocation texture) {
        int u, v, srcW, srcH;
        if (!isLit) {
            if (isVertical) {
                u = SPRITE_LED_UNLIT_V_X; v = SPRITE_LED_UNLIT_V_Y; srcW = 4; srcH = 8;
            } else {
                u = SPRITE_LED_UNLIT_H_X; v = SPRITE_LED_UNLIT_H_Y; srcW = 8; srcH = 4;
            }
        } else {
            // colorType: 0 = GREEN, 1 = RED (danger), 2 = AMBER (power)
            if (colorType == 1) {
                if (isVertical) { u = SPRITE_LED_RED_V_X; v = SPRITE_LED_RED_V_Y; srcW = 4; srcH = 8; }
                else { u = SPRITE_LED_RED_H_X; v = SPRITE_LED_RED_H_Y; srcW = 8; srcH = 4; }
            } else if (colorType == 2) {
                if (isVertical) { u = SPRITE_LED_AMBER_V_X; v = SPRITE_LED_AMBER_V_Y; srcW = 4; srcH = 8; }
                else { u = SPRITE_LED_AMBER_H_X; v = SPRITE_LED_AMBER_H_Y; srcW = 8; srcH = 4; }
            } else {
                if (isVertical) { u = SPRITE_LED_GREEN_V_X; v = SPRITE_LED_GREEN_V_Y; srcW = 4; srcH = 8; }
                else { u = SPRITE_LED_GREEN_H_X; v = SPRITE_LED_GREEN_H_Y; srcW = 8; srcH = 4; }
            }
        }
        g.blit(texture, destX, destY, destW, destH, u, v, srcW, srcH, TabletFrame.DESIGN_W, TabletFrame.DESIGN_H);
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
        GLYPHS.put(' ', new int[]{0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000, 0b00000});
        GLYPHS.put('-', new int[]{0b00000, 0b00000, 0b00000, 0b11111, 0b00000, 0b00000, 0b00000});
        GLYPHS.put('+', new int[]{0b00000, 0b00100, 0b00100, 0b11111, 0b00100, 0b00100, 0b00000});
        GLYPHS.put(':', new int[]{0b00000, 0b01100, 0b01100, 0b00000, 0b01100, 0b01100, 0b00000});
        GLYPHS.put('0', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('1', new int[]{0b00100, 0b01100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110});
        GLYPHS.put('2', new int[]{0b01110, 0b10001, 0b00001, 0b00110, 0b01000, 0b10000, 0b11111});
        GLYPHS.put('3', new int[]{0b11110, 0b00001, 0b00001, 0b01110, 0b00001, 0b00001, 0b11110});
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
        GLYPHS.put('G', new int[]{0b01110, 0b10001, 0b10000, 0b10111, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('I', new int[]{0b01110, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b01110});
        GLYPHS.put('J', new int[]{0b00010, 0b00010, 0b00010, 0b00010, 0b10010, 0b10010, 0b01100});
        GLYPHS.put('L', new int[]{0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b10000, 0b11111});
        GLYPHS.put('M', new int[]{0b10001, 0b11011, 0b10101, 0b10001, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('N', new int[]{0b10001, 0b11001, 0b10101, 0b10011, 0b10001, 0b10001, 0b10001});
        GLYPHS.put('O', new int[]{0b01110, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('P', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10000, 0b10000, 0b10000});
        GLYPHS.put('R', new int[]{0b11110, 0b10001, 0b10001, 0b11110, 0b10100, 0b10001, 0b10001});
        GLYPHS.put('S', new int[]{0b01111, 0b10000, 0b10000, 0b01110, 0b00001, 0b00001, 0b11110});
        GLYPHS.put('T', new int[]{0b11111, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100, 0b00100});
        GLYPHS.put('U', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01110});
        GLYPHS.put('V', new int[]{0b10001, 0b10001, 0b10001, 0b10001, 0b10001, 0b01010, 0b00100});
        GLYPHS.put('W', new int[]{0b10001, 0b10001, 0b10001, 0b10101, 0b10101, 0b11011, 0b10001});
        GLYPHS.put('Y', new int[]{0b10001, 0b10001, 0b01010, 0b00100, 0b00100, 0b00100, 0b00100});
    }

    private static void bakeAllDefaultKeysAndLeds(NativeImage img) {
        int keySize = 44;
        int half = keySize / 2;

        // 1. Top Row (10 Keys centered at ROW_TOP_Y = 41 + 10 LEDs at LED_ROW_TOP_Y = 76)
        String[] topLabels = {"GRID", "SA", "WPN", "DEF", "SYS", "DRV", "STR", "COM", "BMS", "BRIGHT"};
        for (int i = 0; i < 10; i++) {
            int cx = 148 + i * 76;
            int cy = 41;
            bakeSingleKey(img, cx - half, cy - half, keySize, keySize, false, topLabels[i]);
            bakeLedSprite(img, cx - 2, 76, 4, 8, false, 0);
        }

        // 2. Left Flank (6 Keys centered at COL_LEFT_X = 39 + 6 LEDs at LED_COL_LEFT_X = 76)
        String[] leftLabels = {"CFF", "F2", "F3", "F4", "F5", "F6"};
        for (int i = 0; i < 6; i++) {
            int cx = 39;
            int cy = 155 + i * 64;
            bakeSingleKey(img, cx - half, cy - half, keySize, keySize, (i == 0), leftLabels[i]);
            bakeLedSprite(img, 76, cy - 4, 4, 8, false, 0);
        }

        // 3. Right Flank (6 Keys centered at COL_RIGHT_X = 941 + 6 LEDs at LED_COL_RIGHT_X = 896)
        String[] rightLabels = {"F7", "F8", "F9", "F10", "F11", "F12"};
        for (int i = 0; i < 6; i++) {
            int cx = 941;
            int cy = 155 + i * 64;
            bakeSingleKey(img, cx - half, cy - half, keySize, keySize, false, rightLabels[i]);
            bakeLedSprite(img, 896, cy - 4, 4, 8, false, 0);
        }

        // 4. Bottom Row (10 Keys centered at ROW_BOTTOM_Y = 589 + 10 LEDs at LED_ROW_BOTTOM_Y = 546)
        String[] botLabels = {"NIGHT", "F13", "F14", "F15", "F16", "F17", "F18", "F19", "F20", "POWER"};
        for (int i = 0; i < 10; i++) {
            int cx = 148 + i * 76;
            int cy = 589;
            bakeSingleKey(img, cx - half, cy - half, keySize, keySize, (i == 9), botLabels[i]);
            bakeLedSprite(img, cx - 2, 546, 4, 8, false, 0);
        }
    }

    private static void bakeSingleKey(NativeImage img, int kx, int ky, int w, int h, boolean red, String label) {
        int r = 7;
        if (red) {
            bakeKeySprite(img, kx, ky, w, h, r,
                    0x88040508, 0xFF2A0808, 0xFF6A1414, 0xFFB82E2E, 0xFFC83636, 0xFF9C2222, 0xFF540E0E, 0xFFBE3232, false);
        } else {
            bakeKeySprite(img, kx, ky, w, h, r,
                    0x88040508, 0xFF1C1F26, 0xFF323640, 0xFF5A6272, 0xFF626B7C, 0xFF444954, 0xFF2E323A, 0xFF545C6C, false);
        }

        int textCol = 0xFFF0F4FA;
        int cx = kx + w / 2;
        int cy = ky + h / 2;

        switch (label) {
            case "GRID" -> { // crosshair
                for (int x = cx - 7; x <= cx + 7; x++) setPixel(img, x, cy, textCol);
                for (int y = cy - 7; y <= cy + 7; y++) setPixel(img, cx, y, textCol);
                fillCircle(img, cx, cy, 2, textCol);
            }
            case "BRIGHT" -> { // 8-pointed star
                fillCircle(img, cx, cy, 3, textCol);
                for (int x = cx - 8; x <= cx + 8; x++) setPixel(img, x, cy, textCol);
                for (int y = cy - 8; y <= cy + 8; y++) setPixel(img, cx, y, textCol);
                int d = 5;
                setPixel(img, cx - d, cy - d, textCol);
                setPixel(img, cx + d, cy - d, textCol);
                setPixel(img, cx - d, cy + d, textCol);
                setPixel(img, cx + d, cy + d, textCol);
            }
            case "NIGHT" -> { // diamond
                int s = 7;
                for (int dy = -s; dy <= s; dy++) {
                    int span = s - Math.abs(dy);
                    for (int dx = -span; dx <= span; dx++) {
                        setPixel(img, cx + dx, cy + dy, textCol);
                    }
                }
            }
            case "POWER" -> { // IEC Standby symbol
                int radius = 8;
                for (int y = cy - radius - 2; y <= cy - radius + 6; y++) {
                    setPixel(img, cx, y, textCol);
                }
                int rIn2 = (radius - 2) * (radius - 2);
                int rOut2 = radius * radius;
                int gapHalfW = 3;
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int d2 = dx * dx + dy * dy;
                        if (d2 <= rOut2 && d2 >= rIn2) {
                            if (dy < -2 && Math.abs(dx) <= gapHalfW) continue;
                            setPixel(img, cx + dx, cy + dy, textCol);
                        }
                    }
                }
            }
            default -> rasterizePixelString(img, label, cx, cy, 2, textCol);
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

