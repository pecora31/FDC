package net.nazarick.artillerytablet.fire;

/** Why the battery can or cannot service a queued target, as shown on the HUD. */
public enum TargetStatus {
    /** The selected trajectory reaches it. */
    OK,
    /** Too close: the arc would need more elevation than the mount has. */
    MIN_RANGE,
    /** No ballistic solution at all — past the gun's maximum range. */
    MAX_RANGE,
    /** The selected trajectory can't reach it, but the other one can. */
    USE_OTHER_ARC
}
