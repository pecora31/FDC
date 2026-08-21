package net.nazarick.artillerytablet.fire;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import com.atsuishio.superbwarfare.tools.VectorToolKt;
import net.minecraft.world.phys.Vec3;

import java.util.function.BooleanSupplier;

/**
 * Decides when a slewing turret has actually settled onto its commanded aim vector.
 *
 * <p>A fixed "wait N ticks then shoot" delay is wrong: traverse time scales with how far the turret
 * has to swing, so a distance that needed a big slew would fire early and miss. Instead this
 * compares the barrel's live direction — ArtilleryEntity.getShootVec(weapon, partialTick), the same
 * vector vehicleShoot launches along — against the commanded direction stored by setTarget.
 *
 * <p>Being merely <em>close</em> is not enough to shoot: firing while the turret is still moving
 * launches along a barrel that is mid-interpolation and still travelling, which threw the first
 * round of every new fire mission off target while subsequent rounds — fired from an already
 * stationary gun — landed correctly. So the gun is considered laid only once its bearing has
 * stopped changing. That doubles as the "this turret physically cannot get there" test: a gun at
 * its mechanical limit also stops changing, just while still far off target.
 */
public final class ArtilleryAimTracker implements BooleanSupplier {
    /** Below this per-tick change (degrees) the turret counts as no longer moving. */
    private static final double SETTLED_EPSILON_DEG = 0.05;

    /** Consecutive stationary ticks before the bearing is trusted. */
    private static final int SETTLED_TICKS = 5;

    /** Once stopped, refuse the shot if the barrel is still this far off — it cannot get there. */
    private static final double ABORT_DEG = 12.0;

    private final ArtilleryEntity artillery;
    private final String weaponName;

    private double previousAngle = Double.MAX_VALUE;
    private int settledTicks;
    private boolean aborted;

    public ArtilleryAimTracker(ArtilleryEntity artillery, String weaponName) {
        this.artillery = artillery;
        this.weaponName = weaponName;
    }

    /** True once waiting should stop — check {@link #wasAborted()} before firing. */
    @Override
    public boolean getAsBoolean() {
        if (artillery.isRemoved() || artillery.isWreck()) {
            aborted = true;
            return true;
        }

        double angle = currentAngleDeg();
        boolean stationary = Math.abs(previousAngle - angle) < SETTLED_EPSILON_DEG;
        previousAngle = angle;

        if (!stationary) {
            settledTicks = 0;
            return false;
        }

        if (++settledTicks < SETTLED_TICKS) {
            return false;
        }

        // Stopped moving: either laid on target, or as close as this mount allows.
        aborted = angle > ABORT_DEG;
        return true;
    }

    /** True when waiting stopped for a reason other than the gun being laid on target. */
    public boolean wasAborted() {
        return aborted;
    }

    private double currentAngleDeg() {
        Vec3 commanded = new Vec3(artillery.getShootVec());
        Vec3 actual = artillery.getShootVec(weaponName, 1f);
        if (commanded.lengthSqr() == 0 || actual.lengthSqr() == 0) {
            return 0;
        }
        return VectorToolKt.angleTo(actual, commanded);
    }
}
