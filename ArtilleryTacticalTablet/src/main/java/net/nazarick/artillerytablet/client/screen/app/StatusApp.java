package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.nazarick.artillerytablet.client.hud.FireMissionClientState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Map;

/** Every active fire mission and what it is doing — read-only, the same data the old STATUS tab
 * showed, just laid out for the full screen instead of a 114px column. */
@OnlyIn(Dist.CLIENT)
public final class StatusApp extends TacticalApp {
    public static final String ID = "SYS_STATUS";

    public StatusApp() {
        super(ID, "SYSTEM STATUS", "");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h) {
        Font font = Minecraft.getInstance().font;
        int x = 10, y = 10;
        int lineH = font.lineHeight + 4;
        g.drawString(font, "SYSTEM STATUS", x, y, 0xFF6FE07A, false);
        y += lineH * 2;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        long now = mc.level.getGameTime();
        Map<java.util.UUID, FireMissionClientState.Entry> missions = FireMissionClientState.active(now);
        if (missions.isEmpty()) {
            g.drawString(font, "No active fire missions.", x, y, 0xFF8A99A6, false);
            return;
        }

        for (FireMissionClientState.Entry entry : missions.values()) {
            String line = switch (entry.state) {
                case IN_FLIGHT -> {
                    long ticks = entry.ticksToImpact(now);
                    yield ticks <= 0 ? "Impact" : String.format("In flight — impact in %.1fs", ticks / 20.0);
                }
                default -> entry.state.name();
            };
            g.drawString(font, line, x, y, 0xFFE8E8E6, false);
            y += lineH;
        }
    }
}
