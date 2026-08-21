package net.nazarick.artillerytablet.fire;

public enum MissionState {
    /** Turret is slewing onto the commanded bearing. */
    AIMING,
    /** Laid on target, held up by an empty gun or its rate of fire. */
    WAITING,
    /**
     * Laid on target and standing by, because that is all that was asked for.
     *
     * <p>Distinct from {@link #WAITING}: that gun wants to shoot and cannot, this one has been told
     * not to yet. On a fire-control display those are opposite situations — one is a problem and the
     * other is the battery ready — so they must not read the same.
     */
    LAID,
    /** Round is away; the HUD counts down to impact. */
    IN_FLIGHT,
    /** Mission ended without firing (couldn't traverse, no ammo supplied, gun lost). */
    ABORTED
}
