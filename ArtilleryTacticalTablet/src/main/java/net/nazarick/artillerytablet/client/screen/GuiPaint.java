package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The game's own drawing, behind the seam the case is drawn through.
 *
 * <p>Thin on purpose. Everything with an opinion — how a corner is cut, how a gradient runs, how a
 * label is centred on its ink — lives above this, so that the picture the harness produces and the
 * picture the game produces differ only in the two places they must: real pixels, and a real font.
 */
@OnlyIn(Dist.CLIENT)
final class GuiPaint implements Paint {
    private final GuiGraphics g;

    GuiPaint(GuiGraphics g) {
        this.g = g;
    }

    @Override
    public void fill(int x0, int y0, int x1, int y1, int argb) {
        g.fill(x0, y0, x1, y1, argb);
    }

    @Override
    public void label(String text, int x, int y, int w, int h, int argb) {
        Ui.textCentred(g, Component.literal(text), x, y, w, h, argb);
    }

    @Override
    public void label(String text, int x, int y, int w, int h, int argb, double heightShare,
                      int padding) {
        int scale = Ui.scaleFor(text, w, h, heightShare, padding);
        if (scale <= 1) {
            label(text, x, y, w, h, argb);
            return;
        }
        Ui.textCentredScaled(g, Component.literal(text), x, y, w, h, argb, scale);
    }

    @Override
    public void batch(Runnable drawing) {
        Ui.batched(g, drawing);
    }
}
