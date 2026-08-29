package net.nazarick.artillerytablet.item;

/**
 * One queued target: where it is, and what it is.
 *
 * <p>The classification fields exist for the tablet screen's NATO symbology (see
 * {@code client.screen.NatoSymbolRenderer}), but this class does not reference that renderer at all —
 * it is {@code client.screen}, rendering-only code that a dedicated server never loads, while this
 * class is shared by both sides. {@link #unitType} and {@link #echelon} are plain strings that happen
 * to match {@code NatoSymbolRenderer.UnitType}/{@code Echelon} enum constant names, resolved with
 * {@code valueOf} on the rendering side rather than referenced directly here, the same seam
 * {@code BlockStyle} keeps between {@code mapengine} and the game.
 *
 * <p><b>No player-facing classification UI exists yet.</b> Every target marked today gets the same
 * placeholder classification ({@link #defaultClassification}) — real, not a stub that crashes, just
 * not yet what the player actually chose. Revisit once the tablet has a screen for picking one.
 */
public final class TargetEntry {
    public enum Affiliation {
        FRIENDLY, HOSTILE, NEUTRAL, UNKNOWN
    }

    public final int x;
    public final int y;
    public final int z;
    public final Affiliation affiliation;

    /** Matches one of {@code NatoSymbolRenderer.UnitType}'s constant names. */
    public final String unitType;

    /** Matches one of {@code NatoSymbolRenderer.Echelon}'s constant names. */
    public final String echelon;

    /** Short unit label, e.g. "1-501 FA". Empty when not set. */
    public final String designation;

    /** Parent formation label, e.g. "3rd BDE". Empty when not set. */
    public final String higherFormation;

    /** Spotted by right-click, with no classification chosen yet — the placeholder default. */
    public TargetEntry(int x, int y, int z) {
        this(x, y, z, Affiliation.HOSTILE, defaultUnitType(), defaultEchelon(), "", "");
    }

    public TargetEntry(int x, int y, int z, Affiliation affiliation, String unitType, String echelon,
                        String designation, String higherFormation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.affiliation = affiliation;
        this.unitType = unitType;
        this.echelon = echelon;
        this.designation = designation;
        this.higherFormation = higherFormation;
    }

    /**
     * What a target is marked as before the player has actually classified it. Called out by name in
     * both places it is used ({@link #TargetEntry(int, int, int)} and the NBT reader that fills in a
     * saved tag written before this field existed) so both stay in step if the default ever changes.
     */
    static String defaultUnitType() {
        return "INFANTRY";
    }

    static String defaultEchelon() {
        return "SQUAD";
    }
}
