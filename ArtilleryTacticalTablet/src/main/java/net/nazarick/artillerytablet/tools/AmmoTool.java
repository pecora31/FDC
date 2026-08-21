package net.nazarick.artillerytablet.tools;

import com.atsuishio.superbwarfare.data.gun.AmmoConsumer;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.vehicle.base.ArtilleryEntity;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Helpers for reading and switching a vehicle gun's ammo type.
 *
 * <p>Ammo options come from the gun's own datapack definition (GunProp.AMMO_CONSUMER), and how many
 * rounds are actually available comes from the ammo supplier attached to the vehicle — so this
 * reports the real supply rather than assuming everything is loadable. Worth stating because
 * testing happens with a Creative Ammo Box installed, which makes every type available in unlimited
 * quantity; that must not be mistaken for how a survival gun behaves.
 */
public final class AmmoTool {
    /** Counts at or above this are reported as unlimited rather than a misleading exact number. */
    private static final int UNLIMITED_THRESHOLD = 10_000;

    /** Ammo-id suffix to the name gunners would actually use. Longest suffixes first. */
    private static final Map<String, String> SHELL_TYPES = new LinkedHashMap<>();

    static {
        SHELL_TYPES.put("_smoke", "Smoke");
        SHELL_TYPES.put("_illum", "Illumination");
        SHELL_TYPES.put("_he", "HE Shell");
        SHELL_TYPES.put("_ap", "AP Shell");
        SHELL_TYPES.put("_cm", "Cluster");
        SHELL_TYPES.put("_wp", "Phosphorus");
        // The plain shell is the armour-piercing round these guns default to.
        SHELL_TYPES.put("_shell", "AP Shell");
    }

    private AmmoTool() {
    }

    @SuppressWarnings("unchecked")
    public static List<AmmoConsumer> consumersOf(GunData data) {
        return (List<AmmoConsumer>) data.get(GunProp.AMMO_CONSUMER);
    }

    /**
     * A short name for the tablet's ammunition list.
     *
     * <p>The items' own names all begin with the calibre — "Large Caliber AP Shell", "Large Caliber
     * HE Shell" — so a list of them clipped to the panel width read as several identical rows of
     * "Large Calibe...". The distinguishing part is the suffix of the ammo id, so name them from
     * that instead.
     */
    public static String displayNameOf(AmmoConsumer consumer) {
        String id = consumer.getAmmo();
        if (id == null || id.isEmpty()) {
            return "?";
        }

        String path = id.substring(id.indexOf(':') + 1);
        for (Map.Entry<String, String> known : SHELL_TYPES.entrySet()) {
            if (path.endsWith(known.getKey())) {
                return known.getValue();
            }
        }

        // Unknown type: strip the calibre words and tidy what's left, so a gun this mod has never
        // seen still gets a readable row rather than a raw id.
        String rest = path.replace("large_", "").replace("small_", "").replace('_', ' ').trim();
        return rest.isEmpty() ? path : Character.toUpperCase(rest.charAt(0)) + rest.substring(1);
    }

    /** @return rounds available, or -1 when the supply is effectively unlimited. */
    public static int availableCount(AmmoConsumer consumer, GunData data, ArtilleryEntity artillery) {
        int count = consumer.count(data, artillery.getAmmoSupplier());
        return count >= UNLIMITED_THRESHOLD ? -1 : count;
    }

    /**
     * Switches the gun to the ammo type with the given id.
     *
     * <p>The mutation has to go through {@code modifyGunData}, the same wrapper vehicleShoot uses.
     * A vehicle's GunData handed out by getGunData is a detached view: calling changeAmmoConsumer
     * plus save() on it appears to work but never reaches the entity, which is why the first
     * attempt at this silently reverted to the previous ammo.
     *
     * @return true if that gun offered the type and was switched.
     */
    public static boolean selectAmmo(ArtilleryEntity artillery, String ammoId) {
        GunData data = artillery.getGunData("Main");
        if (data == null) {
            return false;
        }

        List<AmmoConsumer> consumers = consumersOf(data);
        for (int i = 0; i < consumers.size(); i++) {
            if (!consumers.get(i).getAmmo().equals(ammoId)) {
                continue;
            }

            int index = i;
            artillery.modifyGunData("Main", gunData ->
                    gunData.changeAmmoConsumer(index, artillery.getAmmoSupplier()));
            return true;
        }
        return false;
    }
}
