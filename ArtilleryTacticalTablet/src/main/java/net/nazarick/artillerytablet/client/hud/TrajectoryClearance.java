package net.nazarick.artillerytablet.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.fire.FlightProfile;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.terrain.TerrainTile;

/**
 * Checks whether the ground gets in the way between gun and target.
 *
 * <p>This is the question the reachability check has never been able to answer. That one asks
 * whether the mount can be laid steeply enough to throw a shell the distance — a fair question, and
 * it says nothing at all about the hill in between. At long range a flat arc runs close to the
 * ground for most of its flight, so "the gun can reach it" and "the shell will get there" are
 * genuinely different answers, and the difference is what a lofted arc exists to fix.
 *
 * <p>Advisory only, like every other warning here. It colours a line and names a distance; the
 * decision to fire still belongs to the gun itself.
 */
@OnlyIn(Dist.CLIENT)
public final class TrajectoryClearance {
    /**
     * How far above the ground the shell has to pass to count as clear. Terrain is stored one sample
     * per block column, so a shell skimming within a block of the surface is inside the noise —
     * calling that clear would be a confident answer the data cannot support.
     */
    private static final int CLEARANCE_BLOCKS = 2;

    /**
     * Fractions of the flight ignored at each end. The gun's own position sits inside its hull, and
     * the last stretch is the shell arriving at the target, which is supposed to meet the ground.
     */
    private static final double IGNORE_FROM_GUN = 0.04;
    private static final double IGNORE_BEFORE_TARGET = 0.06;

    /** No obstruction found. */
    public static final int CLEAR = -1;
    /** Ground along the path has not been surveyed, so no honest answer is possible. */
    public static final int UNKNOWN = -2;

    private TrajectoryClearance() {
    }

    /**
     * @return distance in blocks from the gun to the first obstruction, or {@link #CLEAR} /
     *         {@link #UNKNOWN}
     */
    public static int obstructionAt(FlightProfile profile, TargetEntry target) {
        if (profile == null) {
            return CLEAR;
        }

        double dx = target.x - profile.gunX;
        double dz = target.z - profile.gunZ;
        double groundRange = Math.sqrt(dx * dx + dz * dz);
        if (groundRange < 1.0) {
            return CLEAR;
        }

        short[] ground = FireLineCache.groundFor(profile.gunX, profile.gunZ, target.x, target.z);
        if (ground == null) {
            // The line hasn't been answered yet. surveyLine() already asked; this just isn't a
            // clear-path answer this codebase is willing to give before the ground is actually known.
            return UNKNOWN;
        }

        boolean sawGap = false;
        for (int i = 0; i < FlightProfile.SAMPLES; i++) {
            double fraction = i / (double) (FlightProfile.SAMPLES - 1);
            if (fraction < IGNORE_FROM_GUN || fraction > 1.0 - IGNORE_BEFORE_TARGET) {
                continue;
            }

            if (ground[i] == TerrainTile.NO_DATA) {
                sawGap = true;
                continue;
            }

            if (ground[i] + CLEARANCE_BLOCKS >= profile.altitude[i]) {
                return (int) Math.round(groundRange * fraction);
            }
        }

        // Never claim a clear path over ground that was never looked at.
        return sawGap ? UNKNOWN : CLEAR;
    }

    /**
     * Makes sure the ground under a firing line gets surveyed.
     *
     * <p>Without this the check would only ever work for ground the rendered map happens to hold —
     * and the map is client-side only now, so it holds nothing the player hasn't personally walked
     * near. This asks the server directly for just the line, one column per sample point, regardless
     * of what's on screen.
     */
    public static void surveyLine(FlightProfile profile, TargetEntry target) {
        if (profile == null) {
            return;
        }
        FireLineCache.request(profile.gunX, profile.gunZ, target.x, target.z);
    }
}
