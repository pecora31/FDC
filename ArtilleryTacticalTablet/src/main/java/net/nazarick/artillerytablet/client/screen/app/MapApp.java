package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The home app — the tactical map, exactly as it always rendered.
 *
 * <p>Deliberately empty. The map/marker/header drawing it stands in for is not duplicated here:
 * {@code TabletScreen.renderDevice} already owns that pipeline (terrain, grid, markers, range
 * rings, heading chevron — all tuned, all working), and gating it on "is this app active" right
 * there is far lower risk than routing it through a second draw path that would have to be kept
 * in step with the first. This class exists only so {@link AppManager} has an id to switch to and
 * a slot to be the default — see the {@code id} check in {@code TabletScreen.renderDevice}.
 */
@OnlyIn(Dist.CLIENT)
public final class MapApp extends TacticalApp {
    public static final String ID = "SA_MAP";

    public MapApp() {
        super(ID, "TACTICAL MAP", "");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h) {
        // Intentionally blank — see class javadoc.
    }
}
