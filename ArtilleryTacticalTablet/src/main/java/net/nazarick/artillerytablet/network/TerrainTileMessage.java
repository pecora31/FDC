package net.nazarick.artillerytablet.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.nazarick.artillerytablet.client.terrain.TerrainClientCache;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.function.Supplier;

/**
 * Server -> client. One square of surveyed ground for the tablet map.
 *
 * <p>An <em>empty</em> tile is a real answer, not a failure: it means the server looked and that
 * ground has never been generated. The client records it as such so the map can show an honest gap
 * instead of asking again every frame.
 */
public class TerrainTileMessage {
    private final TerrainTile tile;
    private final boolean empty;

    /** True when the client already holds this tile and only needs telling it is still current. */
    private final boolean unchanged;

    public TerrainTileMessage(TerrainTile tile, boolean empty) {
        this(tile, empty, false);
    }

    private TerrainTileMessage(TerrainTile tile, boolean empty, boolean unchanged) {
        this.tile = tile;
        this.empty = empty;
        this.unchanged = unchanged;
    }

    /** "Still what you have." Coordinates and nothing else. */
    public static TerrainTileMessage unchanged(int tileX, int tileZ) {
        return new TerrainTileMessage(new TerrainTile(tileX, tileZ), false, true);
    }

    public static void encode(TerrainTileMessage msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.unchanged);
        if (msg.unchanged) {
            buf.writeVarInt(msg.tile.tileX);
            buf.writeVarInt(msg.tile.tileZ);
            return;
        }

        buf.writeBoolean(msg.empty);
        if (msg.empty) {
            // Nothing to survey: send the coordinates alone rather than 12 KB of blank.
            buf.writeVarInt(msg.tile.tileX);
            buf.writeVarInt(msg.tile.tileZ);
            return;
        }
        msg.tile.write(buf);
    }

    public static TerrainTileMessage decode(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            return unchanged(buf.readVarInt(), buf.readVarInt());
        }

        boolean empty = buf.readBoolean();
        if (empty) {
            return new TerrainTileMessage(new TerrainTile(buf.readVarInt(), buf.readVarInt()), true);
        }
        return new TerrainTileMessage(TerrainTile.read(buf), false);
    }

    public static void handle(TerrainTileMessage msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    if (msg.unchanged) {
                        TerrainClientCache.confirm(msg.tile.tileX, msg.tile.tileZ);
                    } else {
                        TerrainClientCache.accept(msg.tile, msg.empty);
                    }
                }));
        ctx.setPacketHandled(true);
    }
}
