package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.tools.AmmoTool;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> server. Asks what ammunition the tablet's bound artillery can fire.
 *
 * <p>Options are read off the first bound gun. Selection is then applied by ammo id to every bound
 * gun that offers it (see {@link SelectAmmoMessage}), rather than by list index, since a mixed
 * battery's guns need not enumerate the same types in the same order.
 */
public class RequestAmmoOptionsMessage {
    private final boolean mainHand;

    public RequestAmmoOptionsMessage(boolean mainHand) {
        this.mainHand = mainHand;
    }

    public static void encode(RequestAmmoOptionsMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
    }

    public static RequestAmmoOptionsMessage decode(FriendlyByteBuf buf) {
        return new RequestAmmoOptionsMessage(buf.readBoolean());
    }

    public static void handle(RequestAmmoOptionsMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            List<AmmoOptionEntry> entries = new ArrayList<>();
            ArtilleryEntity reference = firstBoundGun(player, stack);
            if (reference != null) {
                GunData data = reference.getGunData("Main");
                if (data != null) {
                    List<AmmoConsumer> consumers = AmmoTool.consumersOf(data);
                    int selectedIndex = data.selectedAmmoType.get();
                    for (int i = 0; i < consumers.size(); i++) {
                        AmmoConsumer consumer = consumers.get(i);
                        entries.add(new AmmoOptionEntry(
                                consumer.getAmmo(),
                                AmmoTool.displayNameOf(consumer),
                                AmmoTool.availableCount(consumer, data, reference),
                                i == selectedIndex
                        ));
                    }
                }
            }

            ModNetwork.toPlayer(player,
                    new AmmoOptionsResponseMessage(entries));
        });
        ctx.setPacketHandled(true);
    }

    private static ArtilleryEntity firstBoundGun(ServerPlayer player, ItemStack stack) {
        for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
            Entity entity = EntityLookup.findAcrossLevels(player.getServer(), bound.id);
            if (entity instanceof ArtilleryEntity artillery && !artillery.isWreck()) {
                return artillery;
            }
        }
        return null;
    }
}
