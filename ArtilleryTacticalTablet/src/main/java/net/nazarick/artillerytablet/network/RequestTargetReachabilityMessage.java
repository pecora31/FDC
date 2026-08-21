package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.fire.FlightProfile;
import net.nazarick.artillerytablet.fire.ReachabilityCheck;
import net.nazarick.artillerytablet.fire.TargetStatus;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.tools.EntityLookup;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> server. Asks which queued targets the bound battery can service. The client sends its
 * signature back unchanged so the reply can be matched to the state it was asked about.
 */
public class RequestTargetReachabilityMessage {
    private final boolean mainHand;
    private final int signature;

    public RequestTargetReachabilityMessage(boolean mainHand, int signature) {
        this.mainHand = mainHand;
        this.signature = signature;
    }

    public static void encode(RequestTargetReachabilityMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        buf.writeInt(msg.signature);
    }

    public static RequestTargetReachabilityMessage decode(FriendlyByteBuf buf) {
        return new RequestTargetReachabilityMessage(buf.readBoolean(), buf.readInt());
    }

    public static void handle(RequestTargetReachabilityMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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

            List<TargetEntry> targets = ArtilleryTacticalTabletItem.getTargets(stack);
            boolean depressed = ArtilleryTacticalTabletItem.isDepressed(stack);

            List<ArtilleryEntity> guns = new ArrayList<>();
            for (BoundArtillery bound : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
                Entity entity = EntityLookup.findAcrossLevels(player.getServer(), bound.id);
                if (entity instanceof ArtilleryEntity artillery && !artillery.isWreck()) {
                    guns.add(artillery);
                }
            }

            byte[] statuses = new byte[targets.size()];
            FlightProfile[] profiles = new FlightProfile[targets.size()];
            for (int i = 0; i < targets.size(); i++) {
                TargetEntry target = targets.get(i);
                statuses[i] = (byte) evaluate(guns, target, depressed).ordinal();
                profiles[i] = traceBestGun(guns, target, depressed);
            }

            ModNetwork.toPlayer(player,
                    new TargetReachabilityMessage(msg.signature, statuses, profiles));
        });
        ctx.setPacketHandled(true);
    }

    /**
     * The arc of whichever gun would actually take this target, for the client to check against the
     * ground. Prefers a gun that can service it, since that is the shot the player would really be
     * firing; failing that any gun's arc still shows roughly where the shell would run into a hill.
     */
    private static FlightProfile traceBestGun(List<ArtilleryEntity> guns, TargetEntry target, boolean depressed) {
        BlockPos pos = new BlockPos(target.x, target.y, target.z);
        FlightProfile fallback = null;

        for (ArtilleryEntity gun : guns) {
            FlightProfile profile = FlightProfile.trace(gun, pos, depressed);
            if (profile == null) {
                continue;
            }
            if (ReachabilityCheck.evaluate(gun, pos, depressed) == TargetStatus.OK) {
                return profile;
            }
            if (fallback == null) {
                fallback = profile;
            }
        }
        return fallback;
    }

    /**
     * Best answer across the battery: a target counts as serviceable if any bound gun can take it.
     * When none can on the chosen arc but one could on the other, say so — that's the actionable
     * case, and far more useful than just colouring the line red.
     */
    private static TargetStatus evaluate(List<ArtilleryEntity> guns, TargetEntry target, boolean depressed) {
        // With nothing bound there is no gun to judge against, so don't cry wolf.
        if (guns.isEmpty()) {
            return TargetStatus.OK;
        }

        BlockPos pos = new BlockPos(target.x, target.y, target.z);
        TargetStatus worstReason = null;
        for (ArtilleryEntity gun : guns) {
            TargetStatus status = ReachabilityCheck.evaluate(gun, pos, depressed);
            if (status == TargetStatus.OK) {
                return TargetStatus.OK;
            }
            // Prefer reporting a minimum-range problem: it's the one the player can fix by moving
            // the target, whereas out-of-range on one gun of a spread-out battery says little.
            if (worstReason == null || status == TargetStatus.MIN_RANGE) {
                worstReason = status;
            }
        }

        for (ArtilleryEntity gun : guns) {
            if (ReachabilityCheck.canReach(gun, pos, !depressed)) {
                return TargetStatus.USE_OTHER_ARC;
            }
        }
        return worstReason;
    }
}
