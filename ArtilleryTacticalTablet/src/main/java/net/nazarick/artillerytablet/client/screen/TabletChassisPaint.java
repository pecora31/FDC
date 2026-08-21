package net.nazarick.artillerytablet.client.screen;

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
final class TabletChassisPaint {
    private TabletChassisPaint() {
    }

    // =========================================================================
    // BAKE — runs once, into a design-space (980x630) image
    // =========================================================================
    static NativeImage bake() {
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

        // 8. Four recessed corner-key pockets
        int pocketSize = 55;
        bakeRecessedButtonPocket(img, 148, 589, pocketSize);
        bakeRecessedButtonPocket(img, 832, 589, pocketSize);
        bakeRecessedButtonPocket(img, 148, 41, pocketSize);
        bakeRecessedButtonPocket(img, 832, 41, pocketSize);

        // 9. Lit capsule divider ribs
        bakeAllDividerRibs(img);

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

    private static void fillCircle(NativeImage img, int cx, int cy, int radius, int argb) {
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x * x + y * y <= radius * radius) {
                    setPixel(img, cx + x, cy + y, argb);
                }
            }
        }
    }
}
