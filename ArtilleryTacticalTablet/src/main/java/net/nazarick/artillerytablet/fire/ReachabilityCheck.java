package net.nazarick.artillerytablet.fire;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import com.atsuishio.superbwarfare.entity.vehicle.utils.VehicleVecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Answers "could this gun put a round there?" without firing, so the HUD can flag targets the
 * battery cannot service before the player wastes a fire order on them.
 *
 * <p>This mirrors what ArtilleryEntity.setTarget does internally: solve the arc, then check the
 * required elevation against the mount's own limits. The awkward half of that — the aim point below
 * the block and the inverted flag — lives in {@link LaunchSolution}, because {@link FlightProfile}
 * needs exactly the same thing and two copies of it would not have stayed in step.
 *
 * <p>Being a copy of another mod's internals, this is advisory only: it drives a warning colour,
 * never a decision to fire. The authoritative check stays where it belongs — FireCommandMessage
 * asks the gun itself and aborts if the bearing is refused — so if SBW changes these rules, the
 * worst case is a HUD marker that disagrees, not a gun that misbehaves.
 *
 * <p>Note what this deliberately does <em>not</em> model: it answers "can the gun be laid to throw a
 * shell there", not "will the shell arrive". Terrain along the flight path and any drag on the real
 * projectile are outside it, so a flat arc marked OK can still clip a hill on the way — which is
 * exactly the sort of shot a lofted arc exists to solve.
 */
public final class ReachabilityCheck {
    private ReachabilityCheck() {
    }

    /** Whether this gun can service the target on the given arc, and if not, why not. */
    public static TargetStatus evaluate(ArtilleryEntity artillery, BlockPos targetPos, boolean depressed) {
        // Through LaunchSolution, which is the only place that reproduces what setTarget does
        // before it solves — the aim point below the block, and the inverted flag.
        Vec3 solution = LaunchSolution.launchVector(artillery, targetPos, depressed);
        // No arc exists at any elevation: the shot is simply longer than the gun can throw.
        if (solution == null) {
            return TargetStatus.MAX_RANGE;
        }

        double pitch = -VehicleVecUtils.getXRotFromVector(solution);
        if (pitch >= -artillery.getTurretMaxPitch() && pitch <= -artillery.getTurretMinPitch()) {
            return TargetStatus.OK;
        }
        // An arc exists but the mount cannot be laid that steeply — the classic minimum-range gap.
        return TargetStatus.MIN_RANGE;
    }

    public static boolean canReach(ArtilleryEntity artillery, BlockPos targetPos, boolean depressed) {
        return evaluate(artillery, targetPos, depressed) == TargetStatus.OK;
    }
}
