package net.nazarick.artillerytablet.tools;

import java.util.Locale;

/**
 * Turns a vehicle identifier into a short tag for the tablet's lists — "PLZ05" rather than the full
 * translated vehicle name, which ate the whole row and left no space for the position beside it.
 *
 * <p>Accepts either a registry path ({@code plz_05}) or a translation key
 * ({@code entity.superbwarfare.plz_05}), because bound guns are recorded by description id while
 * live ones are read straight off the entity type.
 */
public final class ArtilleryLabel {
    private static final int MAX_LENGTH = 12;

    private ArtilleryLabel() {
    }

    public static String shorten(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return "?";
        }

        String tail = identifier.substring(identifier.lastIndexOf('.') + 1);
        String compact = tail.replace("_", "").toUpperCase(Locale.ROOT);
        if (compact.isEmpty()) {
            return "?";
        }
        return compact.length() > MAX_LENGTH ? compact.substring(0, MAX_LENGTH) : compact;
    }
}
