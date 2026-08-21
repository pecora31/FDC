package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import net.minecraft.world.phys.Vec3;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.tools.AmmoTool;
import net.nazarick.artillerytablet.tools.ArtilleryLabel;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client -> server. Builds the tablet's artillery roster: every gun already bound to it, plus any
 * unbound gun standing within {@link #NEARBY_RADIUS} of the player.
 *
 * <p>Bound guns are listed unconditionally, wherever they are and even while their chunk is
 * unloaded. Listing only what was nearby meant a battery left behind vanished from the roster and
 * could not be released without travelling back to it.
 *
 * <p>No ownership concept exists here — SBW doesn't track one, and an addon-side ownership registry
 * was tried and dropped as more complexity than this mechanic warranted. Known consequence: on a
 * PvP server the nearby half of this list also shows other players' artillery (binding is still
 * range-checked server-side, see BindArtilleryMessage).
 */
public class RequestNearbyArtilleryMessage {
    /** SBW's internal name for an artillery piece's only weapon slot. */
    private static final String WEAPON = "Main";

    public static final double NEARBY_RADIUS = 128.0;

    private final boolean mainHand;

    public RequestNearbyArtilleryMessage(boolean mainHand) {
        this.mainHand = mainHand;
    }

    public static void encode(RequestNearbyArtilleryMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
    }

    public static RequestNearbyArtilleryMessage decode(FriendlyByteBuf buf) {
        return new RequestNearbyArtilleryMessage(buf.readBoolean());
    }

    public static void handle(RequestNearbyArtilleryMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            List<NearbyArtilleryEntry> entries = new ArrayList<>();
            Set<UUID> listed = new HashSet<>();

            // Bound guns first, so releasing one never depends on where the player is standing.
            for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
                Entity entity = EntityLookup.findAcrossLevels(player.getServer(), bound.id);
                listed.add(bound.id);

                if (entity instanceof ArtilleryEntity artillery) {
                    entries.add(describe(artillery, player, true));
                } else {
                    // Not loaded: fall back to the type recorded when it was bound, so the row is
                    // still identifiable and still has a working Unbind button.
                    entries.add(NearbyArtilleryEntry.unlocated(bound.id, ArtilleryLabel.shorten(bound.typeId)));
                }
            }

            List<ArtilleryEntity> nearby = player.level().getEntitiesOfClass(
                    ArtilleryEntity.class,
                    player.getBoundingBox().inflate(NEARBY_RADIUS)
            );
            nearby.sort(Comparator.comparingDouble(player::distanceTo));
            for (ArtilleryEntity artillery : nearby) {
                if (listed.add(artillery.getUUID())) {
                    entries.add(describe(artillery, player, false));
                }
            }

            ModNetwork.toPlayer(player,
                    new NearbyArtilleryResponseMessage(entries));
        });
        ctx.setPacketHandled(true);
    }

    private static NearbyArtilleryEntry describe(ArtilleryEntity artillery, ServerPlayer player, boolean bound) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(artillery.getType());
        String label = ArtilleryLabel.shorten(key == null ? artillery.getType().getDescriptionId() : key.getPath());

        // Distance is only meaningful within one dimension; across dimensions report it as unknown.
        boolean sameLevel = artillery.level() == player.level();

        // Where the barrel is, and where it has been told to be. Both come from the gun rather than
        // being worked out here: getShootVec is the same vector vehicleShoot launches along, and
        // ShootVec is what setTarget wrote. Recomputing either would be reproducing SBW's internals,
        // which this project has been bitten by three times.
        Vec3 actual = artillery.getShootVec(WEAPON, 1f);
        Vec3 commanded = new Vec3(artillery.getShootVec());
        boolean laid = commanded.lengthSqr() > 1.0E-6;

        int rounds = 0;
        String ammoLabel = "";
        GunData data = artillery.getGunData(WEAPON);
        if (data != null) {
            List<AmmoConsumer> consumers = AmmoTool.consumersOf(data);
            int index = data.selectedAmmoType.get();
            if (index >= 0 && index < consumers.size()) {
                AmmoConsumer consumer = consumers.get(index);
                ammoLabel = AmmoTool.displayNameOf(consumer);
                rounds = AmmoTool.availableCount(consumer, data, artillery);
            }
        }

        return new NearbyArtilleryEntry(
                artillery.getUUID(),
                label,
                bound,
                sameLevel,
                sameLevel ? player.distanceTo(artillery) : 0,
                artillery.getBlockX(), artillery.getBlockY(), artillery.getBlockZ(),
                azimuthMil(actual), elevationMil(actual),
                laid, azimuthMil(commanded), elevationMil(commanded),
                rounds, ammoLabel,
                artillery.getProjectileVelocity(WEAPON), artillery.getProjectileGravity(WEAPON),
                artillery.getTurretMaxPitch(), -artillery.getTurretMinPitch()
        );
    }

    /** Clockwise from north, in mils. Matches the bearing the tablet quotes for a target. */
    private static int azimuthMil(Vec3 dir) {
        double degrees = (Math.toDegrees(Math.atan2(dir.x, -dir.z)) + 360) % 360;
        return (int) Math.round(degrees * 6400 / 360);
    }

    /** Up from the horizontal, in mils. Negative when the barrel is depressed below level. */
    private static int elevationMil(Vec3 dir) {
        double length = dir.length();
        if (length < 1.0E-6) {
            return 0;
        }
        return (int) Math.round(Math.toDegrees(Math.asin(dir.y / length)) * 6400 / 360);
    }
}
