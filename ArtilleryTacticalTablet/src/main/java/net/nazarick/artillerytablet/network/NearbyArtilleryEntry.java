package net.nazarick.artillerytablet.network;

import java.util.UUID;

/**
 * One row of the tablet's artillery roster, and the whole of what the client knows about a gun.
 *
 * <p>Covers both guns standing near the player and guns already bound to the tablet wherever they
 * are — a bound gun must stay listed however far away it has got, otherwise the only way to release
 * it is to walk back to it.
 *
 * <p>It also carries the firing solution. That went here rather than into a state packet of its own
 * because this reply already exists, already runs on demand rather than being pushed at everyone,
 * and already carries per-gun facts; the laying angles are simply more of them. The tablet asks
 * again on a slow tick while it is open, which is what turns a roster into a live readout.
 *
 * <p>Angles are in <b>mils</b>, 6400 to the circle, because that is the unit gunnery is conducted
 * in. Azimuth is measured clockwise from north and elevation up from the horizontal.
 */
public final class NearbyArtilleryEntry {
    public final UUID id;
    /** Short tag such as "PLZ05". */
    public final String label;
    public final boolean bound;
    /** False when the gun isn't loaded right now, so nothing below is known. */
    public final boolean located;
    public final double distance;
    public final int x;
    public final int y;
    public final int z;

    /** Where the barrel is pointing now. */
    public final int azimuthMil;
    public final int elevationMil;

    /**
     * Where it has been told to point, and whether it has been told anything at all.
     *
     * <p>A gun that has never been laid has no commanded vector, and showing it as zero would read
     * as "ordered due north, flat" — a firing solution it does not have. {@link #laid} is what
     * separates "not yet ordered" from "ordered, and this is the order".
     */
    public final boolean laid;
    public final int commandedAzimuthMil;
    public final int commandedElevationMil;

    /**
     * Muzzle velocity, projectile gravity and the turret's elevation limits, in blocks per tick,
     * blocks per tick squared, and degrees.
     *
     * <p>Sent so the map can draw the gun's reach without asking again every time the arc is
     * toggled. Everything derived from them is <b>advisory</b>: the rings ignore the height
     * difference to the target and the aim offset SBW applies, so they say roughly where a gun can
     * reach, never whether a particular shot will be taken. That decision stays with the gun.
     */
    public final float velocity;
    public final float gravity;
    public final float maxElevationDeg;
    public final float minElevationDeg;

    /** Rounds of the loaded type, or -1 for an unlimited supply. */
    public final int rounds;
    /** Short name of the loaded type, such as "HE Shell". */
    public final String ammoLabel;

    public NearbyArtilleryEntry(UUID id, String label, boolean bound, boolean located,
                                double distance, int x, int y, int z,
                                int azimuthMil, int elevationMil,
                                boolean laid, int commandedAzimuthMil, int commandedElevationMil,
                                int rounds, String ammoLabel,
                                float velocity, float gravity,
                                float maxElevationDeg, float minElevationDeg) {
        this.id = id;
        this.label = label;
        this.bound = bound;
        this.located = located;
        this.distance = distance;
        this.x = x;
        this.y = y;
        this.z = z;
        this.azimuthMil = azimuthMil;
        this.elevationMil = elevationMil;
        this.laid = laid;
        this.commandedAzimuthMil = commandedAzimuthMil;
        this.commandedElevationMil = commandedElevationMil;
        this.rounds = rounds;
        this.ammoLabel = ammoLabel;
        this.velocity = velocity;
        this.gravity = gravity;
        this.maxElevationDeg = maxElevationDeg;
        this.minElevationDeg = minElevationDeg;
    }

    /**
     * Furthest and nearest ground the gun can reach on the given arc, in blocks.
     *
     * <p>Flat-fire ballistics: range is v squared times sine of twice the elevation, over gravity,
     * greatest at forty-five degrees. The lofted arc is everything steeper than that, so its nearest
     * reach is the range at the barrel's steepest angle — which is exactly why lofted fire is
     * useless close in on a turret that only elevates to sixty-five degrees.
     */
    public double[] reachOn(boolean depressed) {
        if (gravity <= 0 || velocity <= 0) {
            return null;
        }
        double flat = velocity * velocity / gravity;
        double atMax = flat * Math.sin(Math.toRadians(2 * Math.min(89.0, maxElevationDeg)));
        double atMin = flat * Math.sin(Math.toRadians(2 * Math.max(0.0, minElevationDeg)));
        // Both arcs reach furthest at forty-five degrees, which is the boundary between them. What
        // separates them is the near edge: flat fire is limited by how far the barrel can depress,
        // lofted fire by how far it can elevate.
        return new double[]{depressed ? atMin : atMax, flat};
    }

    /** A gun that is out of reach or unloaded: identifiable, releasable, and nothing more. */
    public static NearbyArtilleryEntry unlocated(UUID id, String label) {
        return new NearbyArtilleryEntry(id, label, true, false, 0, 0, 0, 0,
                0, 0, false, 0, 0, 0, "", 0, 0, 0, 0);
    }

    /** How far the barrel still has to travel, in mils, or 0 when it has nothing to travel to. */
    public int offMil() {
        if (!laid) {
            return 0;
        }
        int az = Math.abs(shortestTurn(commandedAzimuthMil - azimuthMil));
        int qe = Math.abs(commandedElevationMil - elevationMil);
        return Math.max(az, qe);
    }

    /** Azimuth wraps, so 6390 to 10 is twenty mils of traverse rather than six thousand. */
    private static int shortestTurn(int mil) {
        int wrapped = Math.floorMod(mil, 6400);
        return wrapped > 3200 ? wrapped - 6400 : wrapped;
    }
}
