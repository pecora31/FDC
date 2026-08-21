package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.UUID;
import java.util.function.Supplier;

/** Client -> server. Binds/unbinds one nearby artillery entity to/from the held tablet. */
public class BindArtilleryMessage {
    private final UUID entityId;
    private final boolean bind;
    private final boolean mainHand;

    public BindArtilleryMessage(UUID entityId, boolean bind, boolean mainHand) {
        this.entityId = entityId;
        this.bind = bind;
        this.mainHand = mainHand;
    }

    public static void encode(BindArtilleryMessage msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.entityId);
        buf.writeBoolean(msg.bind);
        buf.writeBoolean(msg.mainHand);
    }

    public static BindArtilleryMessage decode(FriendlyByteBuf buf) {
        return new BindArtilleryMessage(buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(BindArtilleryMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            if (msg.bind) {
                // Never trust the client: re-check the entity is real, is artillery, and is
                // actually within bind range before accepting — same radius the roster listing
                // itself uses, so a modified client can't bind arbitrary far-away/other-dimension
                // UUIDs it wasn't shown.
                Entity entity = EntityLookup.findAcrossLevels(player.getServer(), msg.entityId);
                if (!(entity instanceof ArtilleryEntity)) {
                    return;
                }
                if (entity.level() != player.level() || player.distanceTo(entity) > RequestNearbyArtilleryMessage.NEARBY_RADIUS) {
                    return;
                }

                ArtilleryTacticalTabletItem.bindArtillery(stack, msg.entityId, entity.getType().getDescriptionId());
            } else {
                ArtilleryTacticalTabletItem.unbindArtillery(stack, msg.entityId);
            }
        });
        ctx.setPacketHandled(true);
    }
}
