package net.nazarick.artillerytablet.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A coordinate box, in device pixels.
 *
 * <p>Only ever holds a signed whole number of at most a few digits, so it needs none of the
 * machinery of a general text field — no selection, no clipboard, no scrolling. Typing appends,
 * backspace removes, and anything that is not a digit or a leading minus is refused outright rather
 * than accepted and validated later.
 */
@OnlyIn(Dist.CLIENT)
public class UiField {
    private static final int MAX_LENGTH = 7;

    public final int x;
    public final int y;
    public final int w;
    public final int h;

    private String value = "";
    private boolean focused;

    public UiField(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public String value() {
        return value;
    }

    public void setValue(String text) {
        this.value = text == null ? "" : text;
    }

    public boolean focused() {
        return focused;
    }

    public void setFocused(boolean value) {
        this.focused = value;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    /** @return true when the key was consumed, so the screen does not also act on it */
    public boolean keyPressed(int keyCode) {
        if (!focused) {
            return false;
        }
        // Backspace.
        if (keyCode == 259) {
            if (!value.isEmpty()) {
                value = value.substring(0, value.length() - 1);
            }
            return true;
        }
        return false;
    }

    public boolean charTyped(char typed) {
        if (!focused || value.length() >= MAX_LENGTH) {
            return false;
        }
        boolean digit = typed >= '0' && typed <= '9';
        boolean leadingMinus = typed == '-' && value.isEmpty();
        if (!digit && !leadingMinus) {
            return false;
        }
        value += typed;
        return true;
    }

    public void render(GuiGraphics g, double px, double py) {
        Ui.rect(g, x, y, w, h, TabletTheme.SURFACE);
        Ui.outline(g, x, y, w, h,
                focused ? TabletTheme.FRIENDLY : contains(px, py) ? TabletTheme.MUTED : TabletTheme.LINE);

        // Longer than the box: keep the end in view and let the front scroll off, the way any text
        // field does. Showing the start instead would hide the digit being typed, and letting it run
        // over the border spills onto whatever is beside it.
        String shown = focused ? value + "_" : value;
        int room = w - Ui.GAP_SM;
        while (shown.length() > 1 && Ui.width(shown) > room) {
            shown = shown.substring(1);
        }

        Ui.textIn(g, Component.literal(shown), x + Ui.GAP_XS, y, h,
                value.isEmpty() && !focused ? TabletTheme.MUTED : TabletTheme.TEXT);
    }
}
