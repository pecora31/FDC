package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.nazarick.artillerytablet.client.FireLog;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/** The fire log — read-only, newest first, the same {@link FireLog} entries the old LOG tab
 * showed, just laid out for the full screen. */
@OnlyIn(Dist.CLIENT)
public final class LogApp extends TacticalApp {
    public static final String ID = "COM_LOG";

    public LogApp() {
        super(ID, "FIRE LOG", "");
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h) {
        Font font = Minecraft.getInstance().font;
        int x = 10, y = 10;
        int lineH = font.lineHeight + 4;
        g.drawString(font, "FIRE LOG", x, y, 0xFF6FE07A, false);
        y += lineH * 2;

        List<FireLog.Entry> entries = FireLog.entries();
        if (entries.isEmpty()) {
            g.drawString(font, "No entries yet.", x, y, 0xFF8A99A6, false);
            return;
        }

        for (FireLog.Entry entry : entries) {
            if (y > h - lineH) {
                break;
            }
            g.drawString(font, entry.text(), x, y, 0xFFE8E8E6, false);
            y += lineH;
        }
    }
}
