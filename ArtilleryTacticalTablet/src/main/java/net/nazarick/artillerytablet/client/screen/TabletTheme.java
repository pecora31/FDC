package net.nazarick.artillerytablet.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * One place for the tablet's colours and typeface.
 *
 * <p>The palette follows the rule the old one broke: <b>colour carries meaning, it does not
 * decorate</b>. Everything used to be the same phosphor green, which is why nothing on the display
 * stood out — a warning looked exactly like a coordinate label. Here the chrome is neutral grey and
 * the only saturated colours are the four that mean something, borrowed from the standard military
 * symbol conventions so they read the way an operator already expects.
 *
 * <p>The typeface is Minecraft's own. A packaged TrueType face was tried and dropped: it read as a
 * web page pasted into the game rather than as part of it, and the game's own face turns out to suit
 * a fire-control readout well — it is fixed-height and never blurs, so columns of figures line up
 * and stay legible at any window size. The military look has to come from layout, symbols and
 * colour discipline, not from importing a typeface that fights the rest of the screen.
 *
 * <p>{@link #text} and friends are kept as the single point every label passes through even though
 * they no longer restyle anything. They are where a face would go back in if that ever changes, and
 * routing every draw through one place is what made swapping the face a small edit rather than a
 * hunt through thirty call sites.
 */
@OnlyIn(Dist.CLIENT)
public final class TabletTheme {
    // Chrome — neutral, so it stays out of the way of anything that means something.
    public static final int FRAME = 0xF20F1216;
    public static final int SURFACE = 0xE6171C22;
    public static final int BEZEL = 0xFF1C232B;
    public static final int OVERLAY = 0xF6141A20;
    public static final int LINE = 0xFF2A333D;
    public static final int LINE_SOFT = 0x802A333D;

    public static final int TEXT = 0xFFD7DEE5;
    public static final int MUTED = 0xFF7C8894;

    // Meaning. Nothing else in the interface may use these.
    /** Ours: bound guns, the selected control, the player. */
    public static final int FRIENDLY = 0xFF4DA3FF;
    /** Theirs: targets, impact points. */
    public static final int HOSTILE = 0xFFFF5A52;
    /** Something needs a decision: out of range, terrain in the way, danger close. */
    public static final int WARNING = 0xFFFFB454;
    /** Ready, loaded, reachable. */
    public static final int GOOD = 0xFF5FD08A;

    private TabletTheme() {
    }

    /** The tablet's text, as a component. */
    public static Component text(String value) {
        return Component.literal(value);
    }

    public static Component text(Component value) {
        return value;
    }

    /** Emphasis. The game's face has no separate bold cut, so this is the built-in weight. */
    public static Component bold(Component value) {
        return value.copy().withStyle(ChatFormatting.BOLD);
    }

    public static int draw(GuiGraphics g, Font font, String value, int x, int y, int colour) {
        return draw(g, font, value, x, y, colour, false);
    }

    public static int draw(GuiGraphics g, Font font, Component value, int x, int y, int colour) {
        return draw(g, font, value, x, y, colour, false);
    }

    public static int draw(GuiGraphics g, Font font, String value, int x, int y, int colour, boolean shadow) {
        return g.drawString(font, text(value), x, y, colour, shadow);
    }

    public static int draw(GuiGraphics g, Font font, Component value, int x, int y, int colour, boolean shadow) {
        return g.drawString(font, text(value), x, y, colour, shadow);
    }

    /** Width in the tablet's typeface, which is not the width of the same text in Minecraft's. */
    public static int width(Font font, String value) {
        return font.width(text(value));
    }

    public static int width(Font font, Component value) {
        return font.width(text(value));
    }
}
