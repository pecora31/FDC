package net.nazarick.artillerytablet.client;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.network.AmmoOptionEntry;
import net.nazarick.artillerytablet.network.NearbyArtilleryEntry;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server replies the tablet's panels read from.
 *
 * <p>Previously each reply was delivered by casting {@code Minecraft.getInstance().screen} to the
 * one screen that had asked for it. With every function now living in a tab of a single screen,
 * that coupling would mean a reply arriving after a tab switch got thrown away — so replies land
 * here instead and panels read whatever is current.
 */
@OnlyIn(Dist.CLIENT)
public final class TabletClientData {
    private static final Map<UUID, BlockPos> GUN_POSITIONS = new HashMap<>();

    private static List<NearbyArtilleryEntry> roster;
    private static List<AmmoOptionEntry> ammo;

    /**
     * Bumped on every change. Buttons for these lists can only be built once the lists exist, and a
     * reply always arrives after the screen was laid out — so the screen watches this and rebuilds,
     * rather than silently rendering rows whose controls were never created.
     */
    private static int version;

    private TabletClientData() {
    }

    public static int version() {
        return version;
    }

    public static void setRoster(List<NearbyArtilleryEntry> entries) {
        roster = entries;
        version++;
    }

    /** Null while the reply is still outstanding, which the panel renders as "loading". */
    public static List<NearbyArtilleryEntry> roster() {
        return roster;
    }

    public static void setAmmo(List<AmmoOptionEntry> options) {
        ammo = options;
        version++;
    }

    public static List<AmmoOptionEntry> ammo() {
        return ammo;
    }

    public static List<NearbyArtilleryEntry> rosterOrEmpty() {
        return roster == null ? Collections.emptyList() : roster;
    }

    public static void clearRoster() {
        roster = null;
        version++;
    }

    public static void clearAmmo() {
        ammo = null;
        version++;
    }

    /**
     * Where each bound gun was last reported.
     *
     * <p>The tablet's NBT records a gun only by UUID, and a client cannot look an entity up by UUID
     * — it has to be told. The roster reply is where those positions become known, so they are kept
     * here for the map to draw gun markers from.
     *
     * <p>Known consequence: a gun that has moved since the roster was last fetched draws at its old
     * position until the Battery tab is opened again. For self-propelled artillery that is a real
     * limitation, accepted for now because the alternative is a position packet on a timer.
     */
    public static void recordGunPosition(UUID id, BlockPos pos) {
        GUN_POSITIONS.put(id, pos);
    }

    public static BlockPos lastKnownGunPosition(UUID id) {
        return GUN_POSITIONS.get(id);
    }

    public static void forgetGunPositions() {
        GUN_POSITIONS.clear();
    }
}
