package net.nazarick.artillerytablet.fire;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * The height a shell will be at, sampled evenly along the ground track from gun to target.
 *
 * <p>This exists so the two halves of the terrain-clearance warning can each be done where the data
 * actually is. Ballistics belong to the server — it has the gun, its muzzle velocity and SBW's own
 * solver, and reimplementing any of that on the client would repeat a mistake this project has
 * already made twice. The ground the shell has to clear belongs to the client, which is holding a
 * cache of surveyed terrain for the map anyway. So the server sends the shape of the arc and the
 * client checks it against the hills.
 */
public final class FlightProfile {
    /** Points along the ground track, endpoints included. Fine enough to catch a ridge, small
     *  enough that eight of these cost under a kilobyte on the wire. */
    public static final int SAMPLES = 48;

    /** A shell is never simulated past this; SBW despawns its own projectiles well before it. */
    private static final int MAX_TICKS = 1200;

    public final int gunX;
    public final int gunZ;

    /** Shell altitude at each fraction of the way to the target, in world Y. */
    public final short[] altitude;

    public FlightProfile(int gunX, int gunZ, short[] altitude) {
        this.gunX = gunX;
        this.gunZ = gunZ;
        this.altitude = altitude;
    }

    /**
     * Traces the arc the gun would actually throw, or null when it has no solution for this target.
     *
     * <p>The flight is stepped exactly the way the game moves a projectile — add velocity, then take
     * gravity off it, once per tick — rather than evaluated from a closed-form parabola. The solver
     * that produced the launch vector assumes that same stepping, so anything else would drift away
     * from where the shell really goes.
     */
    public static FlightProfile trace(ArtilleryEntity gun, BlockPos target, boolean depressed) {
        Vec3 muzzle = gun.getShootPos("Main", 1f);
        Vec3 centre = target.getCenter();

        // Through LaunchSolution, the same call ReachabilityCheck makes. The aim point below the
        // block and the inverted flag were written out here as well, and the two copies had to
        // agree exactly or the arc drawn on the map would be a different arc from the one the
        // reachability colour was decided by — with nothing to say which of them was lying.
        Vec3 direction = LaunchSolution.launchVector(gun, target, depressed);
        if (direction == null) {
            return null;
        }

        double speed = gun.getProjectileVelocity("Main");
        double gravity = gun.getProjectileGravity("Main");
        Vec3 velocity = direction.normalize().scale(speed);

        double groundRange = Math.sqrt(Math.pow(centre.x - muzzle.x, 2) + Math.pow(centre.z - muzzle.z, 2));
        if (groundRange < 1.0) {
            return null;
        }

        short[] altitude = new short[SAMPLES];
        int nextSample = 0;

        Vec3 pos = muzzle;
        double travelled = 0.0;
        double lastY = muzzle.y;

        for (int tick = 0; tick < MAX_TICKS && nextSample < SAMPLES; tick++) {
            Vec3 next = pos.add(velocity);
            double nextTravelled = Math.sqrt(Math.pow(next.x - muzzle.x, 2) + Math.pow(next.z - muzzle.z, 2));

            // A tick covers many blocks at these speeds, so fill in every sample the step flew past
            // rather than one per tick, interpolating height across the step.
            while (nextSample < SAMPLES) {
                double wanted = groundRange * nextSample / (double) (SAMPLES - 1);
                if (wanted > nextTravelled) {
                    break;
                }
                double span = nextTravelled - travelled;
                double part = span <= 0 ? 0 : (wanted - travelled) / span;
                double y = lastY + (next.y - lastY) * part;
                altitude[nextSample++] = clampToShort(y);
            }

            pos = next;
            travelled = nextTravelled;
            lastY = next.y;
            velocity = new Vec3(velocity.x, velocity.y - gravity, velocity.z);
        }

        // The shell fell short of the target's ground range — treat the remainder as ground level so
        // the client reads it as blocked rather than as clear sky.
        while (nextSample < SAMPLES) {
            altitude[nextSample++] = clampToShort(centre.y);
        }
        return new FlightProfile(gun.getBlockX(), gun.getBlockZ(), altitude);
    }

    private static short clampToShort(double y) {
        return (short) Math.max(Short.MIN_VALUE + 1, Math.min(Short.MAX_VALUE, Math.round(y)));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(gunX);
        buf.writeVarInt(gunZ);
        for (short y : altitude) {
            buf.writeShort(y);
        }
    }

    public static FlightProfile read(FriendlyByteBuf buf) {
        int gunX = buf.readVarInt();
        int gunZ = buf.readVarInt();
        short[] altitude = new short[SAMPLES];
        for (int i = 0; i < SAMPLES; i++) {
            altitude[i] = buf.readShort();
        }
        return new FlightProfile(gunX, gunZ, altitude);
    }
}
