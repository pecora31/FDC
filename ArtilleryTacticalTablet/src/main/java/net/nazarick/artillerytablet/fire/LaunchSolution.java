package net.nazarick.artillerytablet.fire;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import com.atsuishio.superbwarfare.tools.TrajectoryCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * The one place that works out how a gun would be laid on a target.
 *
 * <p>Two things about {@code ArtilleryEntity.setTarget} have to be reproduced to predict it, and
 * both are easy to miss: it aims at a point a little <em>below</em> the target block, by more the
 * further away the target is, and it <em>inverts</em> the depressed flag before solving. Neither is
 * documented anywhere; both were read out of SBW's own code.
 *
 * <p>They were written out twice — once in {@link ReachabilityCheck}, once in {@link FlightProfile}
 * — and that is the fault this project has made more often than any other, here in the half where
 * it matters most. A drifting map draws a seam; a drifting solver tells a player a target is in
 * reach and then throws the shell somewhere else. Neither copy was wrong when written. The point is
 * that nothing would have made them stay in step: no compiler error, no failing check, just two
 * arcs that quietly disagree the day somebody updates one of them against a new SBW build.
 *
 * <p>So both go through here, and any future third caller does too. If SBW changes these rules,
 * there is exactly one line to change.
 */
public final class LaunchSolution {
    private LaunchSolution() {
    }

    /**
     * Where the gun actually aims for a target block.
     *
     * <p>Below the block's centre, and further below the longer the shot. This is SBW's own
     * correction, reproduced rather than invented — it is what makes the predicted arc land where
     * the real one does.
     */
    public static Vec3 aimPoint(Vec3 muzzle, BlockPos target) {
        Vec3 centre = target.getCenter();
        return centre.add(0.0, -1.0 - 0.0015 * centre.distanceTo(muzzle), 0.0);
    }

    /**
     * The direction the gun would throw a shell to reach this target, or null when no arc exists.
     *
     * @param depressed the flag as the player set it — the inversion {@code setTarget} applies is
     *                  done here, so callers pass what they mean rather than what the solver wants
     */
    public static Vec3 launchVector(ArtilleryEntity gun, BlockPos target, boolean depressed) {
        Vec3 muzzle = gun.getShootPos("Main", 1f);
        return TrajectoryCalculator.calculateLaunchVector(
                muzzle,
                aimPoint(muzzle, target),
                gun.getProjectileVelocity("Main"),
                gun.getProjectileGravity("Main"),
                // Inverted exactly once, here. Doing it at the call sites is how a caller comes to
                // do it twice and quietly ask about the opposite arc.
                !depressed);
    }
}
