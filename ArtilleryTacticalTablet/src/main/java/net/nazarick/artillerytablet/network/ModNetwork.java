package net.nazarick.artillerytablet.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.terrain.TerrainTile;

/**
 * The addon's own network channel.
 *
 * <p>Everything used to ride on SuperbWarfare's channel instead, and that was a mistake on two
 * counts. Message ids on a shared channel are handed out in registration order, so ours depended on
 * how many packets SBW happened to register — a single SBW update adding or removing one would have
 * shifted every id underneath us, silently, with the failure showing up as garbled packets rather
 * than anything that names the cause. And because both mods wrote into the same registry from
 * Forge's parallel setup phase, the two raced and crashed the game outright (see the 2026-08-14 log
 * entry). Our own channel makes both problems structurally impossible.
 *
 * <p>Ids are written out explicitly rather than counted up by a variable, so that reordering the
 * registrations cannot quietly renumber the protocol.
 */
public final class ModNetwork {
    /**
     * Bumped whenever a packet's wire format changes in a way an older build could not read.
     * Clients and servers disagreeing on this are refused at handshake, which is a clear error
     * instead of a mysterious one.
     *
     * <p>The second half is not written here on purpose. Terrain tiles are the format that actually
     * churns, and this number stayed at "1" through a change that added two fields to every column —
     * so a mismatched pair would have shaken hands happily and then decoded twelve kilobytes of
     * nonsense. Reading the tile's own revision means that particular slip cannot be made again: the
     * handshake changes by construction whenever the tile layout does. The hand-written half is for
     * every other packet, and still has to be bumped by hand when one of those changes.
     */
    private static final String PROTOCOL_VERSION = "2." + TerrainTile.FORMAT_VERSION;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ArtilleryTablet.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        int id = 0;

        CHANNEL.registerMessage(id++, SetTargetsMessage.class,
                SetTargetsMessage::encode, SetTargetsMessage::decode, SetTargetsMessage::handle);
        CHANNEL.registerMessage(id++, RequestNearbyArtilleryMessage.class,
                RequestNearbyArtilleryMessage::encode, RequestNearbyArtilleryMessage::decode,
                RequestNearbyArtilleryMessage::handle);
        CHANNEL.registerMessage(id++, NearbyArtilleryResponseMessage.class,
                NearbyArtilleryResponseMessage::encode, NearbyArtilleryResponseMessage::decode,
                NearbyArtilleryResponseMessage::handle);
        CHANNEL.registerMessage(id++, BindArtilleryMessage.class,
                BindArtilleryMessage::encode, BindArtilleryMessage::decode, BindArtilleryMessage::handle);
        CHANNEL.registerMessage(id++, FireCommandMessage.class,
                FireCommandMessage::encode, FireCommandMessage::decode, FireCommandMessage::handle);
        CHANNEL.registerMessage(id++, SetTabletSettingsMessage.class,
                SetTabletSettingsMessage::encode, SetTabletSettingsMessage::decode,
                SetTabletSettingsMessage::handle);
        CHANNEL.registerMessage(id++, RequestAmmoOptionsMessage.class,
                RequestAmmoOptionsMessage::encode, RequestAmmoOptionsMessage::decode,
                RequestAmmoOptionsMessage::handle);
        CHANNEL.registerMessage(id++, AmmoOptionsResponseMessage.class,
                AmmoOptionsResponseMessage::encode, AmmoOptionsResponseMessage::decode,
                AmmoOptionsResponseMessage::handle);
        CHANNEL.registerMessage(id++, SelectAmmoMessage.class,
                SelectAmmoMessage::encode, SelectAmmoMessage::decode, SelectAmmoMessage::handle);
        CHANNEL.registerMessage(id++, FireMissionStatusMessage.class,
                FireMissionStatusMessage::encode, FireMissionStatusMessage::decode,
                FireMissionStatusMessage::handle);
        CHANNEL.registerMessage(id++, RequestTargetReachabilityMessage.class,
                RequestTargetReachabilityMessage::encode, RequestTargetReachabilityMessage::decode,
                RequestTargetReachabilityMessage::handle);
        CHANNEL.registerMessage(id++, TargetReachabilityMessage.class,
                TargetReachabilityMessage::encode, TargetReachabilityMessage::decode,
                TargetReachabilityMessage::handle);
        CHANNEL.registerMessage(id++, AbortMissionMessage.class,
                AbortMissionMessage::encode, AbortMissionMessage::decode, AbortMissionMessage::handle);
        CHANNEL.registerMessage(id++, RequestFireLineMessage.class,
                RequestFireLineMessage::encode, RequestFireLineMessage::decode, RequestFireLineMessage::handle);
        CHANNEL.registerMessage(id++, FireLineProfileMessage.class,
                FireLineProfileMessage::encode, FireLineProfileMessage::decode, FireLineProfileMessage::handle);

        ArtilleryTablet.LOGGER.info("Artillery Tactical Tablet: registered {} packets", id);
    }

    public static void toServer(Object message) {
        CHANNEL.sendToServer(message);
    }

    public static void toPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
