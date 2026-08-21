package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Which app owns the glass right now, and the switch between them.
 *
 * <p>One instance per open tablet — not a static/shared registry. A static app list (as a demo
 * single-player prototype can get away with) would mean two players' tablets, or a tablet closed
 * and reopened, shared one fire-control app's state, which is wrong for a per-player device the
 * same way the case's own key state already has to be.
 */
@OnlyIn(Dist.CLIENT)
public final class AppManager {
    private final List<TacticalApp> apps;
    private int activeIndex = 0;

    public AppManager(List<TacticalApp> apps) {
        this.apps = apps;
    }

    public TacticalApp active() {
        if (apps.isEmpty()) {
            return null;
        }
        if (activeIndex < 0 || activeIndex >= apps.size()) {
            activeIndex = 0;
        }
        return apps.get(activeIndex);
    }

    public void switchTo(String appId) {
        for (int i = 0; i < apps.size(); i++) {
            if (apps.get(i).id().equals(appId)) {
                activeIndex = i;
                return;
            }
        }
    }

    public boolean isActive(String appId) {
        TacticalApp app = active();
        return app != null && app.id().equals(appId);
    }

    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h) {
        TacticalApp app = active();
        if (app != null) {
            app.render(g, mouseX, mouseY, partialTick, w, h);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int w, int h) {
        TacticalApp app = active();
        return app != null && app.mouseClicked(mouseX, mouseY, button, w, h);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, int w, int h) {
        TacticalApp app = active();
        return app != null && app.mouseReleased(mouseX, mouseY, button, w, h);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, int w, int h) {
        TacticalApp app = active();
        return app != null && app.mouseDragged(mouseX, mouseY, button, dragX, dragY, w, h);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        TacticalApp app = active();
        return app != null && app.keyPressed(keyCode, scanCode, modifiers);
    }
}
