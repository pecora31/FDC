package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.fire.FireMode;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;

import java.util.function.Supplier;

/**
 * Client -> server. Persists the tablet's non-target UI state (fire mode, trajectory choice, and
 * the half-typed coordinate boxes) into item NBT so reopening the screen doesn't reset it.
 */
public class SetTabletSettingsMessage {
    private final boolean mainHand;
    private final FireMode mode;
    private final boolean depressed;
    private final String inputX;
    private final String inputY;
    private final String inputZ;

    public SetTabletSettingsMessage(boolean mainHand, FireMode mode, boolean depressed, String inputX, String inputY, String inputZ) {
        this.mainHand = mainHand;
        this.mode = mode;
        this.depressed = depressed;
        this.inputX = inputX;
        this.inputY = inputY;
        this.inputZ = inputZ;
    }

    public static void encode(SetTabletSettingsMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        buf.writeEnum(msg.mode);
        buf.writeBoolean(msg.depressed);
        buf.writeUtf(msg.inputX, 8);
        buf.writeUtf(msg.inputY, 8);
        buf.writeUtf(msg.inputZ, 8);
    }

    public static SetTabletSettingsMessage decode(FriendlyByteBuf buf) {
        return new SetTabletSettingsMessage(
                buf.readBoolean(),
                buf.readEnum(FireMode.class),
                buf.readBoolean(),
                buf.readUtf(8),
                buf.readUtf(8),
                buf.readUtf(8)
        );
    }

    public static void handle(SetTabletSettingsMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            ArtilleryTacticalTabletItem.setFireMode(stack, msg.mode);
            ArtilleryTacticalTabletItem.setDepressed(stack, msg.depressed);
            ArtilleryTacticalTabletItem.setInputs(stack, msg.inputX, msg.inputY, msg.inputZ);
        });
        ctx.setPacketHandled(true);
    }
}
