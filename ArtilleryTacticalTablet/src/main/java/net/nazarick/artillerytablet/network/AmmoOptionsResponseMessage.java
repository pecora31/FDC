package net.nazarick.artillerytablet.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.TabletClientData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Server -> client. Answers {@link RequestAmmoOptionsMessage}. */
public class AmmoOptionsResponseMessage {
    private final List<AmmoOptionEntry> entries;

    public AmmoOptionsResponseMessage(List<AmmoOptionEntry> entries) {
        this.entries = entries;
    }

    public static void encode(AmmoOptionsResponseMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries.size());
        for (AmmoOptionEntry entry : msg.entries) {
            buf.writeUtf(entry.ammoId);
            buf.writeUtf(entry.displayName);
            buf.writeVarInt(entry.available);
            buf.writeBoolean(entry.selected);
        }
    }

    public static AmmoOptionsResponseMessage decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<AmmoOptionEntry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new AmmoOptionEntry(buf.readUtf(), buf.readUtf(), buf.readVarInt(), buf.readBoolean()));
        }
        return new AmmoOptionsResponseMessage(entries);
    }

    public static void handle(AmmoOptionsResponseMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        // Wrapped for the dedicated server, which strips the client classes this reaches for. Calling
        // them from a method the server can load is a crash waiting for the first time somebody runs
        // this anywhere but their own machine — and it is the one configuration this mod is written
        // for and has never been started in. The terrain packet beside this already did it this way.
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> TabletClientData.setAmmo(msg.entries)));
        ctx.setPacketHandled(true);
    }
}
