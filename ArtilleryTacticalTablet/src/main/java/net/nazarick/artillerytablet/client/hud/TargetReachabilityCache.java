package net.nazarick.artillerytablet.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.fire.FlightProfile;
import net.nazarick.artillerytablet.fire.TargetStatus;
import net.nazarick.artillerytablet.item.TargetEntry;

/**
 * Client-side cache of which queued targets the bound battery can actually service.
 *
 * <p>Keyed by a signature of everything the answer depends on (the targets, the trajectory choice,
 * which guns are bound), so the HUD asks the server again exactly when one of those changes rather
 * than polling on a timer.
 */
@OnlyIn(Dist.CLIENT)
public final class TargetReachabilityCache {
    /** Don't re-ask more than once a second, even if the signature keeps changing. */
    private static final long REQUEST_COOLDOWN_TICKS = 20;

    private static int cachedSignature;
    private static byte[] statuses = new byte[0];
    private static FlightProfile[] profiles = new FlightProfile[0];

    private static int requestedSignature;
    private static long lastRequestTick = Long.MIN_VALUE;

    private TargetReachabilityCache() {
    }

    public static void store(int signature, byte[] values, FlightProfile[] arcs) {
        cachedSignature = signature;
        statuses = values;
        profiles = arcs;
    }

    /**
     * How far along the flight the ground gets in the way, or {@link TrajectoryClearance#CLEAR} /
     * {@link TrajectoryClearance#UNKNOWN}.
     *
     * <p>Also puts the corridor on the survey list, so asking the question is what eventually makes
     * it answerable. A firing line the player has never flown along starts out unknown and becomes
     * known a second or two later.
     */
    public static int obstructionOf(int signature, int index, TargetEntry target) {
        if (signature != cachedSignature || index >= profiles.length) {
            return TrajectoryClearance.CLEAR;
        }
        FlightProfile profile = profiles[index];
        TrajectoryClearance.surveyLine(profile, target);
        return TrajectoryClearance.obstructionAt(profile, target);
    }

    /**
     * The signature the cached answer belongs to. The tablet screen shows the same warnings as the
     * HUD but has no reason to recompute the signature itself — whatever the HUD last asked about
     * is the state the player is looking at.
     */
    public static int currentSignature() {
        return cachedSignature;
    }

    /** @return the known status, or {@link TargetStatus#OK} while the answer is still unknown. */
    public static TargetStatus statusOf(int signature, int index) {
        if (signature != cachedSignature || index >= statuses.length) {
            return TargetStatus.OK;
        }
        TargetStatus[] values = TargetStatus.values();
        int ordinal = statuses[index];
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : TargetStatus.OK;
    }

    /** @return true if the caller should send a fresh request for this signature. */
    public static boolean shouldRequest(int signature, long nowTick) {
        if (signature == cachedSignature) {
            return false;
        }
        if (signature == requestedSignature && nowTick - lastRequestTick < REQUEST_COOLDOWN_TICKS) {
            return false;
        }
        requestedSignature = signature;
        lastRequestTick = nowTick;
        return true;
    }
}
