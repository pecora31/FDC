package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One full-screen page of the tablet's glass — the base every domain key opens into.
 *
 * <p>Everything here is in local coordinates: (0,0) is the top-left of the 800x450 screen well,
 * not the window. The screen well itself, the case around it, and every physical key stay
 * {@link net.nazarick.artillerytablet.client.screen.TabletScreen}'s business; an app only ever
 * draws and is clicked inside the rectangle it was handed.
 */
@OnlyIn(Dist.CLIENT)
public abstract class TacticalApp {
    private final String id;
    private final String title;
    private final String keyHint;

    protected TacticalApp(String id, String title, String keyHint) {
        this.id = id;
        this.title = title;
        this.keyHint = keyHint;
    }

    public String id() {
        return id;
    }

    public String title() {
        return title;
    }

    public String keyHint() {
        return keyHint;
    }

    public abstract void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h);

    public boolean mouseClicked(double mouseX, double mouseY, int button, int w, int h) {
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, int w, int h) {
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int w, int h) {
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }
}
