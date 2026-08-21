package net.nazarick.artillerytablet.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.TabletClientData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Server -> client. Answers {@link RequestNearbyArtilleryMessage}. */
public class NearbyArtilleryResponseMessage {
    private final List<NearbyArtilleryEntry> entries;

    public NearbyArtilleryResponseMessage(List<NearbyArtilleryEntry> entries) {
        this.entries = entries;
    }

    public static void encode(NearbyArtilleryResponseMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries.size());
        for (NearbyArtilleryEntry entry : msg.entries) {
            buf.writeUUID(entry.id);
            buf.writeUtf(entry.label, 32);
            buf.writeBoolean(entry.bound);
            buf.writeBoolean(entry.located);
            buf.writeDouble(entry.distance);
            buf.writeInt(entry.x);
            buf.writeInt(entry.y);
            buf.writeInt(entry.z);
            buf.writeVarInt(entry.azimuthMil);
            buf.writeVarInt(entry.elevationMil);
            buf.writeBoolean(entry.laid);
            buf.writeVarInt(entry.commandedAzimuthMil);
            buf.writeVarInt(entry.commandedElevationMil);
            buf.writeInt(entry.rounds);
            buf.writeUtf(entry.ammoLabel, 32);
            buf.writeFloat(entry.velocity);
            buf.writeFloat(entry.gravity);
            buf.writeFloat(entry.maxElevationDeg);
            buf.writeFloat(entry.minElevationDeg);
        }
    }

    public static NearbyArtilleryResponseMessage decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<NearbyArtilleryEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            UUID id = buf.readUUID();
            String label = buf.readUtf(32);
            boolean bound = buf.readBoolean();
            boolean located = buf.readBoolean();
            double distance = buf.readDouble();
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            int az = buf.readVarInt();
            int el = buf.readVarInt();
            boolean laid = buf.readBoolean();
            int cmdAz = buf.readVarInt();
            int cmdEl = buf.readVarInt();
            int rounds = buf.readInt();
            String ammoLabel = buf.readUtf(32);
            entries.add(new NearbyArtilleryEntry(id, label, bound, located, distance, x, y, z,
                    az, el, laid, cmdAz, cmdEl, rounds, ammoLabel,
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()));
        }
        return new NearbyArtilleryResponseMessage(entries);
    }

    public static void handle(NearbyArtilleryResponseMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // Wrapped for the dedicated server, which strips the client classes this reaches for. Calling
        // them from a method the server can load is a crash waiting for the first time somebody runs
        // this anywhere but their own machine — and it is the one configuration this mod is written
        // for and has never been started in. The terrain packet beside this already did it this way.
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            // Cache positions for the map: the tablet stores only a UUID, and the client can't
            // resolve an entity from one, so the roster reply is where gun positions become known.
            for (NearbyArtilleryEntry entry : msg.entries) {
                if (entry.located) {
                    TabletClientData.recordGunPosition(entry.id, new BlockPos(entry.x, entry.y, entry.z));
                }
            }
            TabletClientData.setRoster(msg.entries);
        }));
        ctx.setPacketHandled(true);
    }
}
