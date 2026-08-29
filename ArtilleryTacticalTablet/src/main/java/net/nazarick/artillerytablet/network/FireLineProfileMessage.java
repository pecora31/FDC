package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.hud.FireLineCache;
import net.nazarick.artillerytablet.fire.FlightProfile;

import java.util.function.Supplier;

/**
 * Server -> client. Answers {@link RequestFireLineMessage}: the ground's height at each of
 * {@link FlightProfile#SAMPLES} points along the line, in the same order and at the same fractions
 * {@link FlightProfile} itself samples the shell's arc at — so the crest-clearance check can compare
 * the two arrays index for index without recomputing anything.
 */
public class FireLineProfileMessage {
    private final int gunX;
    private final int gunZ;
    private final int targetX;
    private final int targetZ;
    private final short[] ground;

    public FireLineProfileMessage(int gunX, int gunZ, int targetX, int targetZ, short[] ground) {
        this.gunX = gunX;
        this.gunZ = gunZ;
        this.targetX = targetX;
        this.targetZ = targetZ;
        this.ground = ground;
    }

    public static void encode(FireLineProfileMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.gunX);
        buf.writeVarInt(msg.gunZ);
        buf.writeVarInt(msg.targetX);
        buf.writeVarInt(msg.targetZ);
        for (short y : msg.ground) {
            buf.writeShort(y);
        }
    }

    public static FireLineProfileMessage decode(FriendlyByteBuf buf) {
        int gunX = buf.readVarInt();
        int gunZ = buf.readVarInt();
        int targetX = buf.readVarInt();
        int targetZ = buf.readVarInt();
        short[] ground = new short[FlightProfile.SAMPLES];
        for (int i = 0; i < FlightProfile.SAMPLES; i++) {
            ground[i] = buf.readShort();
        }
        return new FireLineProfileMessage(gunX, gunZ, targetX, targetZ, ground);
    }

    public static void handle(FireLineProfileMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // Wrapped for the dedicated server, which strips the client classes this reaches for — the
        // same reason TargetReachabilityMessage does it this way.
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> FireLineCache.store(msg.gunX, msg.gunZ, msg.targetX, msg.targetZ, msg.ground)));
        ctx.setPacketHandled(true);
    }
}
