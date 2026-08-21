package net.nazarick.artillerytablet.client.screen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The tablet's views, reached from the rail down the left edge.
 *
 * <p>All of them now open on the same side, next to the rail, so the right of the display is left to
 * the map and the action box. Standing two lists open on opposite edges read well until the guns had
 * to be paired with the targets, at which point neither side was wide enough for what it held.
 * Pairing moved into a window opened from a gun, which is what freed the layout.
 *
 * <p>Each key is three letters in a box, as an aircraft multi-function display labels the keys down
 * its bezel. Drawn symbols were tried first and read as decoration at this size; a fixed-width code
 * with the full name on the tooltip is what the reference hardware actually does, and three letters
 * carry far more than the two this layout was rejected for the first time round.
 */
@OnlyIn(Dist.CLIENT)
public enum TabletTab {
    BATTERY("gui.artillerytablet.tab.battery", "BTY"),
    TARGETS("gui.artillerytablet.tab.targets", "TGT"),
    AMMO("gui.artillerytablet.tab.ammo", "AMO"),
    STATUS("gui.artillerytablet.tab.status", "STA"),
    LOG("gui.artillerytablet.tab.log", "LOG");

    public final String titleKey;

    /** Three letters in a box, the way a multi-function display labels its bezel keys. */
    public final String code;

    TabletTab(String titleKey, String code) {
        this.titleKey = titleKey;
        this.code = code;
    }
}
