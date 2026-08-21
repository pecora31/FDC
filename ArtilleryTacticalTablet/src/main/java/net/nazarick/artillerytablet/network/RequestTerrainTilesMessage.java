package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.terrain.ServerSurveyBudget;
import net.nazarick.artillerytablet.terrain.ServerTileCache;
import net.nazarick.artillerytablet.terrain.SurveyLimits;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Client -> server. Asks for terrain imagery covering a set of tiles the tablet is about to draw.
 *
 * <p>Pull, not push. The server never streams map data at anybody: it answers when a tablet is open
 * and looking somewhere, and says nothing otherwise. That keeps the cost proportional to use rather
 * than to the number of players logged in.
 *
 * <p>The batch is capped server-side. A view at the widest zoom covers a great many tiles, and a
 * client asking for all of them at once — whether by accident or on purpose — would be asking the
 * server for a large pile of disk reads in one go.
 */
public class RequestTerrainTilesMessage {
    /**
     * Most tiles one request may ask for.
     *
     * <p>Not a number of its own — it is the client's batch size, and it has to be, because the
     * client marks a tile as asked-about the moment it puts it in a batch. Written separately it was
     * half the batch, so every sweep quietly dropped thirty-two tiles that the client then believed
     * were with the server and would not ask about again until the retry timer released them. The
     * map filled at half the rate it was measuring itself at.
     */
    private static final int MAX_TILES_PER_REQUEST = SurveyLimits.TILES_PER_REQUEST;

    private final boolean mainHand;
    private final List<long[]> tiles;

    public RequestTerrainTilesMessage(boolean mainHand, List<long[]> tiles) {
        this.mainHand = mainHand;
        this.tiles = tiles;
    }

    public static void encode(RequestTerrainTilesMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.mainHand);
        int count = Math.min(msg.tiles.size(), MAX_TILES_PER_REQUEST);
        if (count < msg.tiles.size()) {
            // Cannot happen while the client's batch is this same number, and said out loud rather
            // than trimmed in silence because the silent trim is exactly what went wrong: the sender
            // goes on believing it asked about tiles that never left the machine.
            ArtilleryTablet.LOGGER.warn("A terrain request of {} tiles was trimmed to {}; the caller "
                    + "believes it asked about ground it did not", msg.tiles.size(), count);
        }
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            long[] tile = msg.tiles.get(i);
            buf.writeVarInt((int) tile[0]);
            buf.writeVarInt((int) tile[1]);
            // What the client already holds for this tile, so an unchanged one can be answered in
            // a few bytes rather than resent whole. Zero means it holds nothing.
            buf.writeInt((int) tile[2]);
        }
    }

    public static RequestTerrainTilesMessage decode(FriendlyByteBuf buf) {
        boolean mainHand = buf.readBoolean();
        int count = Math.min(buf.readVarInt(), MAX_TILES_PER_REQUEST);
        List<long[]> tiles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            tiles.add(new long[]{buf.readVarInt(), buf.readVarInt(), buf.readInt()});
        }
        return new RequestTerrainTilesMessage(mainHand, tiles);
    }

    public static void handle(RequestTerrainTilesMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }

            // Holding the tablet is what authorises this. Without the check any client could use
            // the addon as a general-purpose world scanner.
            if (!(player.getMainHandItem().getItem() instanceof ArtilleryTacticalTabletItem)
                    && !(player.getOffhandItem().getItem() instanceof ArtilleryTacticalTabletItem)) {
                return;
            }

            if (!(player.level() instanceof ServerLevel level)) {
                return;
            }

            // Somebody is waiting on ground right now. The background warming reads this and gives
            // way — it exists to have finished before a player looks, never to compete with one.
            ServerSurveyBudget.noteRequest();

            for (long[] coords : msg.tiles) {
                int tileX = (int) coords[0];
                int tileZ = (int) coords[1];

                // The server decides how much surveying it is doing at once, not the clients. Left
                // to them it is one client's limit times however many are logged in, which is fine
                // alone and is a different mod entirely on a server with ten people on it. What
                // there is no room for now is held rather than refused — a refused tile sits in the
                // client's window doing nothing, and that is what put speckled holes in a map that
                // was otherwise filling outward neatly.
                int known = (int) coords[2];
                ServerSurveyBudget.submit(player.getUUID(), () ->
                        ServerTileCache.get(level, tileX, tileZ).whenComplete((entry, error) -> {
                            ServerSurveyBudget.finish(player.getUUID());
                            if (error != null) {
                                ArtilleryTablet.LOGGER.warn("Terrain tile {},{} could not be built",
                                        tileX, tileZ, error);
                                player.server.execute(() -> ModNetwork.toPlayer(player,
                                        new TerrainTileMessage(new TerrainTile(tileX, tileZ), true)));
                                return;
                            }
                            // Back onto the server thread: the tile may have finished on an IO worker.
                            player.server.execute(() -> reply(player, entry, known));
                        }),
                        () -> player.server.execute(() -> ModNetwork.toPlayer(player,
                                new TerrainTileMessage(new TerrainTile(tileX, tileZ), true)))
                );
            }
        });
        ctx.setPacketHandled(true);
    }

    private static void reply(ServerPlayer player, ServerTileCache.Entry entry, int known) {
        if (!player.isAlive() || player.hasDisconnected()) {
            return;
        }

        // Already holds this exact ground: say so and stop. This is most refreshes, and it is what
        // keeps a client that re-asks on a timer from paying for twelve kilobytes each time — and,
        // on the client, from treating a re-send of what it had as new data.
        if (known != 0 && known == entry.hash) {
            ModNetwork.toPlayer(player,
                    TerrainTileMessage.unchanged(entry.tile.tileX, entry.tile.tileZ));
            return;
        }

        // An empty tile still gets a reply. Without it the client cannot tell "never surveyed" from
        // "still waiting", and would ask again forever.
        ModNetwork.toPlayer(player,
                new TerrainTileMessage(entry.tile, entry.tile.isEmpty()));
    }
}
