package net.nazarick.artillerytablet.client.hud;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.fire.MissionState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Client-side mirror of what the player's guns are currently doing, fed by
 * FireMissionStatusMessage. Purely for display — nothing here is authoritative.
 */
@OnlyIn(Dist.CLIENT)
public final class FireMissionClientState {
    /** How long a finished or aborted entry lingers on the HUD before it's dropped. */
    private static final long LINGER_TICKS = 60;

    private static final Map<UUID, Entry> ENTRIES = new LinkedHashMap<>();

    private FireMissionClientState() {
    }

    public static void update(UUID gunId, MissionState state, long impactGameTime, long nowGameTime) {
        ENTRIES.put(gunId, new Entry(state, impactGameTime, nowGameTime));
    }

    /** Drops stale entries and returns what's still worth showing. */
    public static Map<UUID, Entry> active(long nowGameTime) {
        ENTRIES.entrySet().removeIf(e -> e.getValue().isExpired(nowGameTime));
        return ENTRIES;
    }

    public static void clear() {
        ENTRIES.clear();
    }

    public static final class Entry {
        public final MissionState state;
        /** Game time the round lands; only meaningful for {@link MissionState#IN_FLIGHT}. */
        public final long impactGameTime;
        private final long updatedAt;

        Entry(MissionState state, long impactGameTime, long updatedAt) {
            this.state = state;
            this.impactGameTime = impactGameTime;
            this.updatedAt = updatedAt;
        }

        public long ticksToImpact(long nowGameTime) {
            return impactGameTime - nowGameTime;
        }

        boolean isExpired(long nowGameTime) {
            if (state == MissionState.IN_FLIGHT) {
                return nowGameTime > impactGameTime + LINGER_TICKS;
            }
            if (state == MissionState.ABORTED) {
                return nowGameTime > updatedAt + LINGER_TICKS;
            }
            // Aiming/waiting entries are refreshed by the server; if updates stop coming for a
            // long while (gun unloaded, player disconnected mid-mission), let them fall off.
            return nowGameTime > updatedAt + 20 * 90;
        }
    }
}
