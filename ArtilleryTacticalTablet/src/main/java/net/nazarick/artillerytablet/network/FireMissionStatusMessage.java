package net.nazarick.artillerytablet.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.hud.FireMissionClientState;
import net.nazarick.artillerytablet.fire.MissionState;

import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client. Drives the tablet HUD's live mission readout. */
public class FireMissionStatusMessage {
    private final UUID gunId;
    private final MissionState state;
    private final long impactGameTime;

    public FireMissionStatusMessage(UUID gunId, MissionState state, long impactGameTime) {
        this.gunId = gunId;
        this.state = state;
        this.impactGameTime = impactGameTime;
    }

    public static void encode(FireMissionStatusMessage msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.gunId);
        buf.writeEnum(msg.state);
        buf.writeLong(msg.impactGameTime);
    }

    public static FireMissionStatusMessage decode(FriendlyByteBuf buf) {
        return new FireMissionStatusMessage(buf.readUUID(), buf.readEnum(MissionState.class), buf.readLong());
    }

    public static void handle(FireMissionStatusMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // Wrapped for the dedicated server, which strips the client classes this reaches for. Calling
        // them from a method the server can load is a crash waiting for the first time somebody runs
        // this anywhere but their own machine — and it is the one configuration this mod is written
        // for and has never been started in. The terrain packet beside this already did it this way.
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return;
            }
            FireMissionClientState.update(msg.gunId, msg.state, msg.impactGameTime, mc.level.getGameTime());
        }));
        ctx.setPacketHandled(true);
    }
}
