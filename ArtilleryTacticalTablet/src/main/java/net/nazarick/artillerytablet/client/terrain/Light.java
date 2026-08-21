package net.nazarick.artillerytablet.client.terrain;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Turning packed colour channels into light and back.
 *
 * <p>A channel value is not proportional to light — it is bent by roughly the 2.2 power that
 * displays expect. Anything that averages colours has to straighten them first or the result comes
 * out darker and muddier than what it stands for, which is what a zoomed-out map that has "gone
 * grey" usually is.
 *
 * <p>Two places average colours: the reduction chain that halves a tile, and the pass that reduces a
 * whole block texture to one colour. They are here together because they must agree — if the
 * texture average and the mip average bent light differently, a block would change colour at the
 * zoom step where the map stops reading columns and starts reading a reduced level, which is exactly
 * the class of fault this project keeps producing by working the same value out in two places.
 */
@OnlyIn(Dist.CLIENT)
final class Light {
    /** Each packed channel value as a share of light. */
    static final float[] LINEAR = new float[256];

    static {
        for (int i = 0; i < 256; i++) {
            LINEAR[i] = (float) Math.pow(i / 255.0, 2.2);
        }
    }

    private Light() {
    }

    /** A share of light back to a packed channel value. */
    static int encode(float light) {
        int value = Math.round((float) Math.pow(light, 1 / 2.2) * 255f);
        return Math.max(0, Math.min(255, value));
    }
}
