package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.fire.FlightProfile;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.terrain.ServerTerrainProvider;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Client -> server. Asks for the ground's height along the straight line from a gun to a target, so
 * the crest-clearance check has real data instead of guessing at ground nobody has walked over.
 *
 * <p>One shot, not a subscription: sent once when a firing solution is looked at, answered once, and
 * forgotten. Nothing here is cached on the server or written to disk — a target that moves gets a
 * fresh line next time it's asked about, the same as everything else this check does.
 */
public class RequestFireLineMessage {
    private final int gunX;
    private final int gunZ;
    private final int targetX;
    private final int targetZ;

    public RequestFireLineMessage(int gunX, int gunZ, int targetX, int targetZ) {
        this.gunX = gunX;
        this.gunZ = gunZ;
        this.targetX = targetX;
        this.targetZ = targetZ;
    }

    public static void encode(RequestFireLineMessage msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.gunX);
        buf.writeVarInt(msg.gunZ);
        buf.writeVarInt(msg.targetX);
        buf.writeVarInt(msg.targetZ);
    }

    public static RequestFireLineMessage decode(FriendlyByteBuf buf) {
        return new RequestFireLineMessage(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(RequestFireLineMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            // Holding the tablet is what authorises this, same as the terrain tile request — without
            // it any client could use the addon as a general-purpose column-height scanner.
            if (!(player.getMainHandItem().getItem() instanceof ArtilleryTacticalTabletItem)
                    && !(player.getOffhandItem().getItem() instanceof ArtilleryTacticalTabletItem)) {
                return;
            }

            if (!(player.level() instanceof ServerLevel level)) {
                return;
            }

            double dx = msg.targetX - msg.gunX;
            double dz = msg.targetZ - msg.gunZ;
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length < 1.0) {
                return;
            }

            int[] sampleX = new int[FlightProfile.SAMPLES];
            int[] sampleZ = new int[FlightProfile.SAMPLES];
            for (int i = 0; i < FlightProfile.SAMPLES; i++) {
                double fraction = i / (double) (FlightProfile.SAMPLES - 1);
                sampleX[i] = (int) Math.round(msg.gunX + dx * fraction);
                sampleZ[i] = (int) Math.round(msg.gunZ + dz * fraction);
            }

            // Consecutive samples along the line often land in the same chunk, so ask each unique
            // column once rather than once per sample — the same chunk read serving several samples
            // at once instead of being repeated.
            java.util.Map<Long, CompletableFuture<Short>> pending = new java.util.HashMap<>();
            for (int i = 0; i < FlightProfile.SAMPLES; i++) {
                int x = sampleX[i];
                int z = sampleZ[i];
                long key = (((long) x) << 32) | (z & 0xFFFFFFFFL);
                pending.computeIfAbsent(key, k -> ServerTerrainProvider.sampleColumnHeight(level, x, z));
            }

            CompletableFuture.allOf(pending.values().toArray(new CompletableFuture[0])).whenComplete((ignored, error) -> {
                if (error != null) {
                    ArtilleryTablet.LOGGER.warn("Fire line query failed for gun {},{} -> target {},{}",
                            msg.gunX, msg.gunZ, msg.targetX, msg.targetZ, error);
                }

                short[] ground = new short[FlightProfile.SAMPLES];
                for (int i = 0; i < FlightProfile.SAMPLES; i++) {
                    long key = (((long) sampleX[i]) << 32) | (sampleZ[i] & 0xFFFFFFFFL);
                    CompletableFuture<Short> column = pending.get(key);
                    ground[i] = column.isCompletedExceptionally() ? TerrainTile.NO_DATA : column.getNow(TerrainTile.NO_DATA);
                }

                // Back onto the server thread: the columns may have finished on an IO worker.
                player.server.execute(() -> {
                    if (player.isAlive() && !player.hasDisconnected()) {
                        ModNetwork.toPlayer(player,
                                new FireLineProfileMessage(msg.gunX, msg.gunZ, msg.targetX, msg.targetZ, ground));
                    }
                });
            });
        });
        ctx.setPacketHandled(true);
    }
}
