package net.nazarick.artillerytablet.client.screen;

import com.mojang.blaze3d.platform.NativeImage;

/**
 * ASTRA Systems - Pure OLED Tactical Display Interface.
 * Clean hardware baseline with centered ASTRA SYSTEMS logo on FHD OLED screen.
 */
public final class AstraFrontlinePaint {

    public static final int SCR_X = TabletFrame.SCR_X; // 90
    public static final int SCR_Y = TabletFrame.SCR_Y; // 90
    public static final int SCR_W = TabletFrame.SCR_W; // 800
    public static final int SCR_H = TabletFrame.SCR_H; // 450
    public static final int SCR_R = 10;                // Screen corner radius

    private AstraFrontlinePaint() {}

    /**
     * Bakes the complete tablet case with the pure ASTRA SYSTEMS OLED interface.
     */
    public static NativeImage bake() {
        return TabletChassisPaint.bake();
    }
}
