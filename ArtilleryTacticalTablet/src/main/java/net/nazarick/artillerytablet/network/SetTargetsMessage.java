package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.TargetEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> server. Sent once when the player presses "Done" in the tablet Screen; overwrites the
 * held tablet's full target list server-side (the authoritative copy).
 */
public class SetTargetsMessage {
    private final boolean mainHand;
    private final List<TargetEntry> targets;

    public SetTargetsMessage(boolean mainHand, List<TargetEntry> targets) {
        this.mainHand = mainHand;
        this.targets = targets;
    }

    public static void encode(SetTargetsMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        buf.writeVarInt(msg.targets.size());
        for (TargetEntry entry : msg.targets) {
            buf.writeInt(entry.x);
            buf.writeInt(entry.y);
            buf.writeInt(entry.z);
        }
    }

    public static SetTargetsMessage decode(FriendlyByteBuf buf) {
        boolean mainHand = buf.readBoolean();
        int count = buf.readVarInt();
        List<TargetEntry> targets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            targets.add(new TargetEntry(buf.readInt(), buf.readInt(), buf.readInt()));
        }
        return new SetTargetsMessage(mainHand, targets);
    }

    public static void handle(SetTargetsMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            ItemStack stack = msg.mainHand ? player.getMainHandItem() : player.getOffhandItem();
            if (!(stack.getItem() instanceof ArtilleryTacticalTabletItem)) {
                return;
            }

            ArtilleryTacticalTabletItem.setTargets(stack, msg.targets);
        });
        ctx.setPacketHandled(true);
    }
}
