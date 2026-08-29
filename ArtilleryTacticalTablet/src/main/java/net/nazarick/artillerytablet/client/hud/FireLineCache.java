package net.nazarick.artillerytablet.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.client.terrain.WorldGeneration;
import net.nazarick.artillerytablet.network.ModNetwork;
import net.nazarick.artillerytablet.network.RequestFireLineMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side cache of {@link RequestFireLineMessage} answers — the ground height sampled along one
 * gun-to-target line, keyed by the four coordinates that define it.
 *
 * <p>A handful of lines at once, not one: the HUD asks about every queued target's line each frame it
 * draws warnings, and a battery can have several targets queued together. A plain list is enough —
 * fire missions rarely queue more than a few targets, so a linear scan costs nothing next to what
 * asking the server for the same line every frame would.
 */
@OnlyIn(Dist.CLIENT)
public final class FireLineCache {
    /** Don't re-ask about the same line more than once a second. */
    private static final long REQUEST_COOLDOWN_TICKS = 20;

    /** Bounded so a session that has aimed at many different points doesn't grow this forever. */
    private static final int MAX_LINES = 64;

    private static final class Line {
        final int gunX;
        final int gunZ;
        final int targetX;
        final int targetZ;
        short[] ground;
        long lastRequestTick = Long.MIN_VALUE;

        Line(int gunX, int gunZ, int targetX, int targetZ) {
            this.gunX = gunX;
            this.gunZ = gunZ;
            this.targetX = targetX;
            this.targetZ = targetZ;
        }

        boolean matches(int gunX, int gunZ, int targetX, int targetZ) {
            return this.gunX == gunX && this.gunZ == gunZ && this.targetX == targetX && this.targetZ == targetZ;
        }
    }

    private static final List<Line> LINES = new ArrayList<>();

    /** Which world this cache's lines belong to, so leaving one world does not answer with another's. */
    private static int worldGeneration = -1;

    private FireLineCache() {
    }

    /**
     * Sends a fresh request for this line if one isn't already on the way, subject to the cooldown.
     * Safe to call every frame — {@link TrajectoryClearance#surveyLine} does exactly that.
     */
    public static void request(int gunX, int gunZ, int targetX, int targetZ) {
        checkWorld();

        Line line = find(gunX, gunZ, targetX, targetZ);
        long now = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();

        if (line == null) {
            line = new Line(gunX, gunZ, targetX, targetZ);
            if (LINES.size() >= MAX_LINES) {
                LINES.remove(0);
            }
            LINES.add(line);
        }

        // The MIN_VALUE sentinel is checked directly rather than subtracted from: "now minus never"
        // overflows a signed long and wraps around to a small number, which would have made the very
        // first request for any new line silently never fire.
        boolean neverAsked = line.lastRequestTick == Long.MIN_VALUE;
        if (line.ground == null && (neverAsked || now - line.lastRequestTick >= REQUEST_COOLDOWN_TICKS)) {
            line.lastRequestTick = now;
            ModNetwork.toServer(new RequestFireLineMessage(gunX, gunZ, targetX, targetZ));
        }
    }

    /** The cached ground heights for this line, or null while the answer is still on its way. */
    public static short[] groundFor(int gunX, int gunZ, int targetX, int targetZ) {
        Line line = find(gunX, gunZ, targetX, targetZ);
        return line == null ? null : line.ground;
    }

    public static void store(int gunX, int gunZ, int targetX, int targetZ, short[] ground) {
        checkWorld();
        Line line = find(gunX, gunZ, targetX, targetZ);
        if (line == null) {
            line = new Line(gunX, gunZ, targetX, targetZ);
            if (LINES.size() >= MAX_LINES) {
                LINES.remove(0);
            }
            LINES.add(line);
        }
        line.ground = ground;
    }

    private static Line find(int gunX, int gunZ, int targetX, int targetZ) {
        for (Line line : LINES) {
            if (line.matches(gunX, gunZ, targetX, targetZ)) {
                return line;
            }
        }
        return null;
    }

    /**
     * Drops every line on a change of world or dimension, the same boundary the terrain cache itself
     * guards — coordinates mean nothing across it, and answering with a stale line would be exactly
     * the "another world's hills" mistake {@link TrajectoryClearance} exists to never make.
     */
    private static void checkWorld() {
        WorldGeneration.checkWorld();
        int generation = WorldGeneration.generation();
        if (generation != worldGeneration) {
            worldGeneration = generation;
            LINES.clear();
        }
    }
}
