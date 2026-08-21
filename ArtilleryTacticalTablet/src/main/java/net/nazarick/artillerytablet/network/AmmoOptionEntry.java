package net.nazarick.artillerytablet.network;

public final class AmmoOptionEntry {
    /** AmmoConsumer.getAmmo() — the stable id we match on, since indices differ between guns. */
    public final String ammoId;
    public final String displayName;
    /** Rounds available to the gun, or -1 when the supply is effectively unlimited. */
    public final int available;
    public final boolean selected;

    public AmmoOptionEntry(String ammoId, String displayName, int available, boolean selected) {
        this.ammoId = ammoId;
        this.displayName = displayName;
        this.available = available;
        this.selected = selected;
    }
}
