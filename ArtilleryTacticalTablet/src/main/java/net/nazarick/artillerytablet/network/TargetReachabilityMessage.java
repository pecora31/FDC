package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.hud.TargetReachabilityCache;
import net.nazarick.artillerytablet.fire.FlightProfile;

import java.util.function.Supplier;

/**
 * Server -> client. Answers {@link RequestTargetReachabilityMessage}: one TargetStatus per target,
 * plus the arc the gun would throw at it.
 *
 * <p>The arc travels with the answer because the client cannot work it out. Muzzle velocity, gravity
 * and SBW's own solver all live on the server, and copying them across would repeat a mistake this
 * project has made twice already. What the client has instead is the surveyed ground, so it takes
 * the shape of the flight and checks it against the hills itself.
 */
public class TargetReachabilityMessage {
    private final int signature;
    private final byte[] statuses;
    private final FlightProfile[] profiles;

    public TargetReachabilityMessage(int signature, byte[] statuses, FlightProfile[] profiles) {
        this.signature = signature;
        this.statuses = statuses;
        this.profiles = profiles;
    }

    public static void encode(TargetReachabilityMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.signature);
        buf.writeVarInt(msg.statuses.length);
        for (byte value : msg.statuses) {
            buf.writeByte(value);
        }
        for (FlightProfile profile : msg.profiles) {
            // A target with no solution has no arc to describe.
            buf.writeBoolean(profile != null);
            if (profile != null) {
                profile.write(buf);
            }
        }
    }

    public static TargetReachabilityMessage decode(FriendlyByteBuf buf) {
        int signature = buf.readInt();
        int count = buf.readVarInt();
        byte[] statuses = new byte[count];
        for (int i = 0; i < count; i++) {
            statuses[i] = buf.readByte();
        }
        FlightProfile[] profiles = new FlightProfile[count];
        for (int i = 0; i < count; i++) {
            profiles[i] = buf.readBoolean() ? FlightProfile.read(buf) : null;
        }
        return new TargetReachabilityMessage(signature, statuses, profiles);
    }

    public static void handle(TargetReachabilityMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // Wrapped for the dedicated server, which strips the client classes this reaches for. Calling
        // them from a method the server can load is a crash waiting for the first time somebody runs
        // this anywhere but their own machine — and it is the one configuration this mod is written
        // for and has never been started in. The terrain packet beside this already did it this way.
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> TargetReachabilityCache.store(msg.signature, msg.statuses, msg.profiles)));
        ctx.setPacketHandled(true);
    }
}
