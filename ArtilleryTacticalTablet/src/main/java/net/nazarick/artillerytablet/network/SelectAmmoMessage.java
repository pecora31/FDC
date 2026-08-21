package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.tools.AmmoTool;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.function.Supplier;

/**
 * Client -> server. Switches every bound gun that offers this ammo type over to it. Matching is by
 * ammo id rather than list position so a mixed battery still lines up.
 */
public class SelectAmmoMessage {
    private final boolean mainHand;
    private final String ammoId;

    public SelectAmmoMessage(boolean mainHand, String ammoId) {
        this.mainHand = mainHand;
        this.ammoId = ammoId;
    }

    public static void encode(SelectAmmoMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        buf.writeUtf(msg.ammoId);
    }

    public static SelectAmmoMessage decode(FriendlyByteBuf buf) {
        return new SelectAmmoMessage(buf.readBoolean(), buf.readUtf());
    }

    public static void handle(SelectAmmoMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            int switched = 0;
            int total = 0;
            for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
                Entity entity = EntityLookup.findAcrossLevels(player.getServer(), bound.id);
                if (!(entity instanceof ArtilleryEntity artillery) || artillery.isWreck()) {
                    continue;
                }
                total++;
                if (AmmoTool.selectAmmo(artillery, msg.ammoId)) {
                    switched++;
                }
            }

            if (switched < total) {
                player.displayClientMessage(
                        Component.translatable("tips.artillerytablet.ammo_partial", switched, total)
                                .withStyle(ChatFormatting.YELLOW),
                        true
                );
            }
        });
        ctx.setPacketHandled(true);
    }
}
