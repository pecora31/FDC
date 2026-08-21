package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client -> server. Stands one gun down from the mission it is running.
 *
 * <p>A fire order is not instantaneous — the gun traverses, waits for the loaders, waits out its own
 * rate of fire — and during all of that the player may see something that changes their mind. Until
 * now the only way to stop a gun was to give it a different target, which is a poor answer when the
 * right answer is to not fire at all.
 */
public class AbortMissionMessage {
    private final boolean mainHand;
    private final UUID gunId;

    public AbortMissionMessage(boolean mainHand, UUID gunId) {
        this.mainHand = mainHand;
        this.gunId = gunId;
    }

    public static void encode(AbortMissionMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        buf.writeUUID(msg.gunId);
    }

    public static AbortMissionMessage decode(FriendlyByteBuf buf) {
        return new AbortMissionMessage(buf.readBoolean(), buf.readUUID());
    }

    public static void handle(AbortMissionMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            // Only guns this tablet commands. Without the check a modified client could stand down
            // anyone's battery by sending a UUID.
            boolean commanded = false;
            for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
                if (bound.id.equals(msg.gunId)) {
                    commanded = true;
                    break;
                }
            }
            if (!commanded) {
                return;
            }

            Entity entity = EntityLookup.findAcrossLevels(player.getServer(), msg.gunId);
            if (entity instanceof ArtilleryEntity artillery) {
                FireCommandMessage.cancel(artillery, player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
