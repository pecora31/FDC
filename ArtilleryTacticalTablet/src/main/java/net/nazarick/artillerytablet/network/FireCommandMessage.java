package net.nazarick.artillerytablet.network;

import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import com.atsuishio.superbwarfare.item.misc.FiringParametersItem;
import com.atsuishio.superbwarfare.item.misc.FiringParametersItemKt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.fire.ArtilleryAimTracker;
import net.nazarick.artillerytablet.fire.FireMode;
import net.nazarick.artillerytablet.fire.MissionState;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.tools.EntityLookup;
import net.nazarick.artillerytablet.tools.FireScheduler;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Client -> server. Fires the tablet's bound artillery at one target-queue entry.
 *
 * <p>Aiming goes through ArtilleryEntity.setTarget(ItemStack, Entity, String), which reads a
 * FiringParameters record off of <em>any</em> ItemStack (via the FiringParametersItemKt extension
 * functions) — so a disposable scratch stack works as the carrier. setTarget only stores the
 * computed launch vector and clears lockTurret; the barrel then rotates toward it gradually, one
 * step per baseTick, so firing in the same tick would launch along the <em>old</em> barrel
 * direction. {@link ArtilleryAimTracker} watches the barrel until it actually settles rather than
 * guessing a fixed delay, since traverse time scales with how far the turret has to swing.
 *
 * <p>A fire mission is a sequence of waits (slew onto the bearing, then hold for ammo and for the
 * gun's own rate of fire) rather than one instant action, so orders have to be interruptible: each
 * gun keeps a generation counter in {@link #ORDER_GEN}, a new order bumps it, and every pending
 * callback from an older generation quietly stands down. That's what lets the player re-lay a gun
 * mid-slew onto a completely different target instead of waiting out the first mission.
 *
 * <p>Rate limiting is ours to enforce: SBW gates its vehicle weapons client-side (the fire key's
 * RPM timer in ClientEventHandler), so calling vehicleShoot straight from a GUI button bypasses it
 * entirely and turns a howitzer into a machine gun. {@link #LAST_SHOT_TICK} reimposes the gun's own
 * RPM server-side, per artillery entity.
 */
public class FireCommandMessage {
    private static final int RIPPLE_DELAY_TICKS = 10;

    /** Hard ceiling on how long to wait for a turret to come onto target before giving up. */
    private static final int AIM_TIMEOUT_TICKS = 200;

    /** Hard ceiling on how long a laid gun holds its bearing waiting to shoot (60s). */
    private static final int READY_WAIT_TIMEOUT_TICKS = 1200;


    private static final Map<UUID, Long> LAST_SHOT_TICK = new HashMap<>();
    private static final Map<UUID, Integer> ORDER_GEN = new HashMap<>();

    private final int targetIndex;
    private final FireMode mode;
    private final boolean mainHand;

    /**
     * Lays the guns without firing them.
     *
     * <p>An order to lay is the same order as an order to fire, right up to the moment the barrel
     * stops moving — so it is a flag on this message rather than a mechanism of its own. It also
     * ignores the fire mode: laying is something the whole battery does at once, and a mode says how
     * rounds are spaced, which is a question that has not arisen yet.
     */
    private final boolean layOnly;

    public FireCommandMessage(int targetIndex, FireMode mode, boolean mainHand) {
        this(targetIndex, mode, mainHand, false);
    }

    public FireCommandMessage(int targetIndex, FireMode mode, boolean mainHand, boolean layOnly) {
        this.layOnly = layOnly;
        this.targetIndex = targetIndex;
        this.mode = mode;
        this.mainHand = mainHand;
    }

    public static void encode(FireCommandMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targetIndex);
        buf.writeEnum(msg.mode);
        buf.writeBoolean(msg.mainHand);
        buf.writeBoolean(msg.layOnly);
    }

    public static FireCommandMessage decode(FriendlyByteBuf buf) {
        return new FireCommandMessage(buf.readVarInt(), buf.readEnum(FireMode.class),
                buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(FireCommandMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
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
            if (msg.targetIndex < 0 || msg.targetIndex >= targets.size()) {
                return;
            }
            TargetEntry target = targets.get(msg.targetIndex);
            BlockPos targetPos = new BlockPos(target.x, target.y, target.z);
            boolean depressed = ArtilleryTacticalTabletItem.isDepressed(stack);

            List<ArtilleryEntity> guns = new ArrayList<>();
            for (BoundArtillery entry : ArtilleryTacticalTabletItem.getBoundArtillery(stack)) {
                Entity entity = EntityLookup.findAcrossLevels(player.getServer(), entry.id);
                if (entity instanceof ArtilleryEntity artillery) {
                    guns.add(artillery);
                }
            }
            if (guns.isEmpty()) {
                notify(player, "tips.artillerytablet.no_bound_artillery");
                return;
            }

            // Laying is the whole battery, always. Narrowing to one gun is a property of how rounds
            // are delivered, and nothing is being delivered here.
            if (!msg.layOnly && msg.mode == FireMode.SINGLE) {
                guns = guns.subList(0, 1);
            }

            int ordered = 0;
            for (int i = 0; i < guns.size(); i++) {
                ArtilleryEntity gun = guns.get(i);
                if (gun.isWreck() || gun.getGunData("Main") == null) {
                    continue;
                }

                // Supersede whatever this gun was doing — the player has given it a new mission.
                int generation = ORDER_GEN.merge(gun.getUUID(), 1, Integer::sum);
                int extraDelay = !msg.layOnly && msg.mode == FireMode.RIPPLE ? i * RIPPLE_DELAY_TICKS : 0;
                aimAndFire(gun, player, targetPos, depressed, extraDelay, generation, msg.layOnly);
                ordered++;
            }

            if (ordered == 0) {
                notify(player, "tips.artillerytablet.all_guns_unavailable");
            }
        });
        ctx.setPacketHandled(true);
    }

    /** False once a newer fire order has taken this gun over. */
    private static boolean isCurrent(ArtilleryEntity artillery, int generation) {
        Integer current = ORDER_GEN.get(artillery.getUUID());
        return current != null && current == generation;
    }

    private static boolean isAlive(ArtilleryEntity artillery) {
        return !artillery.isRemoved() && !artillery.isWreck();
    }

    private static void aimAndFire(ArtilleryEntity artillery, ServerPlayer shooter, BlockPos targetPos,
                                   boolean depressed, int extraDelay, int generation, boolean layOnly) {
        FireScheduler.schedule(Math.max(1, extraDelay), () -> {
            if (!isCurrent(artillery, generation) || !isAlive(artillery)) {
                return;
            }

            // setTarget refuses a target on several paths — no ballistic solution at all, or a
            // solution whose elevation is outside the turret's limits (what a too-close target
            // gives you) — and every one of them leaves the previous aim vector in place rather
            // than clearing it. The barrel would then already "match" its commanded vector, the aim
            // tracker would call it laid, and the gun would fire on the OLD bearing while the HUD
            // warned about the new one.
            //
            // Detect that by clearing the aim vector first and seeing whether setTarget writes a
            // new one: a real launch vector is never zero, so a still-zero vector afterwards means
            // refusal. Nothing observes the cleared value — setTarget runs before the entity's next
            // baseTick — and unlike re-deriving SBW's rejection rules (which live across several
            // branches and operate on an offset aim point), this stays correct however those rules
            // change.
            artillery.setShootVec(new Vector3f());

            ItemStack scratch = new ItemStack(Items.PAPER);
            FiringParametersItemKt.setFiringParameters(scratch, new FiringParametersItem.Parameters(targetPos, 0, depressed));
            artillery.setTarget(scratch, shooter, "Main");

            Vector3f commanded = artillery.getShootVec();
            if (commanded.x == 0f && commanded.y == 0f && commanded.z == 0f) {
                reportStatus(artillery, shooter, MissionState.ABORTED, 0);
                notify(shooter, "tips.artillerytablet.no_firing_solution");
                return;
            }

            reportStatus(artillery, shooter, MissionState.AIMING, 0);

            ArtilleryAimTracker tracker = new ArtilleryAimTracker(artillery, "Main");
            FireScheduler.pollUntil(
                    () -> !isCurrent(artillery, generation) || tracker.getAsBoolean(),
                    AIM_TIMEOUT_TICKS,
                    () -> {
                        if (!isCurrent(artillery, generation)) {
                            return;
                        }
                        if (tracker.wasAborted()) {
                            reportStatus(artillery, shooter, MissionState.ABORTED, 0);
                            notify(shooter, "tips.artillerytablet.cannot_traverse");
                            return;
                        }
                        if (layOnly) {
                            // Laid, and left there. The turret holds its own bearing from here:
                            // setTarget left lockTurret clear, so the gun keeps steering onto the
                            // commanded vector every tick until a newer order supersedes it.
                            reportStatus(artillery, shooter, MissionState.LAID, 0);
                            return;
                        }
                        waitUntilReadyThenShoot(artillery, shooter, generation, targetPos);
                    },
                    () -> {
                        if (isCurrent(artillery, generation)) {
                            reportStatus(artillery, shooter, MissionState.ABORTED, 0);
                            notify(shooter, "tips.artillerytablet.cannot_traverse");
                        }
                    }
            );
        });
    }

    /**
     * Holds the laid gun on target until it can actually shoot — a round loaded and its rate of
     * fire elapsed. The turret keeps its bearing on its own while we wait: setTarget left
     * lockTurret clear, so ArtilleryEntity's own baseTick keeps steering toward the commanded
     * vector every tick.
     */
    private static void waitUntilReadyThenShoot(ArtilleryEntity artillery, ServerPlayer shooter, int generation, BlockPos targetPos) {
        if (canShootNow(artillery, shooter)) {
            shoot(artillery, shooter, generation, targetPos);
            return;
        }

        reportStatus(artillery, shooter, MissionState.WAITING, 0);
        if (!hasAmmo(artillery)) {
            notify(shooter, "tips.artillerytablet.waiting_for_ammo");
        }

        FireScheduler.pollUntil(
                () -> !isCurrent(artillery, generation) || !isAlive(artillery) || canShootNow(artillery, shooter),
                READY_WAIT_TIMEOUT_TICKS,
                () -> shoot(artillery, shooter, generation, targetPos),
                () -> {
                    if (isCurrent(artillery, generation)) {
                        reportStatus(artillery, shooter, MissionState.ABORTED, 0);
                        notify(shooter, "tips.artillerytablet.reload_timeout");
                    }
                }
        );
    }

    private static boolean hasAmmo(ArtilleryEntity artillery) {
        GunData data = artillery.getGunData("Main");
        return data != null && data.ammo.get() > 0;
    }

    private static boolean canShootNow(ArtilleryEntity artillery, ServerPlayer shooter) {
        if (!hasAmmo(artillery)) {
            return false;
        }
        Long last = LAST_SHOT_TICK.get(artillery.getUUID());
        return last == null || shooter.level().getGameTime() - last >= cooldownTicks(artillery);
    }

    /** The gun's own RPM expressed as a minimum tick gap between shots. */
    private static int cooldownTicks(ArtilleryEntity artillery) {
        int rpm = Math.max(1, artillery.vehicleWeaponRpm("Main"));
        return Math.max(1, 1200 / rpm);
    }

    private static void shoot(ArtilleryEntity artillery, ServerPlayer shooter, int generation, BlockPos targetPos) {
        if (!isCurrent(artillery, generation) || !isAlive(artillery) || !canShootNow(artillery, shooter)) {
            return;
        }

        long now = shooter.level().getGameTime();
        artillery.vehicleShoot(shooter, "Main");
        LAST_SHOT_TICK.put(artillery.getUUID(), now);
        reportStatus(artillery, shooter, MissionState.IN_FLIGHT, now + estimateFlightTicks(artillery, targetPos));
    }

    /**
     * Rough time of flight, for the HUD countdown only — never for aiming. Treats the shell as
     * travelling at a constant horizontal speed, which is true enough of a drag-free ballistic arc
     * to put a countdown within a tick or two of the real impact.
     */
    private static long estimateFlightTicks(ArtilleryEntity artillery, BlockPos targetPos) {
        Vec3 muzzle = artillery.getShootPos("Main", 1f);
        Vec3 target = targetPos.getCenter();
        double horizontalDistance = Math.sqrt(Math.pow(target.x - muzzle.x, 2) + Math.pow(target.z - muzzle.z, 2));

        Vec3 direction = artillery.getShootVec("Main", 1f);
        double horizontalFraction = Math.sqrt(direction.x * direction.x + direction.z * direction.z)
                / Math.max(1.0e-6, direction.length());
        double horizontalSpeed = artillery.getProjectileVelocity("Main") * horizontalFraction;
        if (horizontalSpeed <= 1.0e-6) {
            return 0;
        }
        return Math.max(0, Math.round(horizontalDistance / horizontalSpeed));
    }

    /**
     * Stands a gun down from whatever it was told to do.
     *
     * <p>No new mechanism is needed for this: bumping the order counter is exactly what a fresh fire
     * order already does, and every pending step checks the counter before acting. An abort is just
     * that bump with no order following it, so the gun stops without anything having to unwind.
     */
    public static void cancel(ArtilleryEntity artillery, ServerPlayer shooter) {
        ORDER_GEN.merge(artillery.getUUID(), 1, Integer::sum);
        reportStatus(artillery, shooter, MissionState.ABORTED, 0);
    }

    private static void reportStatus(ArtilleryEntity artillery, ServerPlayer shooter, MissionState state, long impactGameTime) {
        ModNetwork.toPlayer(shooter,
                new FireMissionStatusMessage(artillery.getUUID(), state, impactGameTime));
    }

    private static void notify(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
    }
}
