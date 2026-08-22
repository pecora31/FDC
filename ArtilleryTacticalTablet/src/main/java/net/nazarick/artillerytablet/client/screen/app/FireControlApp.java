package net.nazarick.artillerytablet.client.screen.app;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.nazarick.artillerytablet.client.TabletClientData;
import net.nazarick.artillerytablet.client.screen.TabletScreen;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.network.NearbyArtilleryEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * Fire control readout: selected target, the battery's real laying solution, ammo, and the way
 * out — CFF.
 *
 * <p>Backend-only content on purpose. Layout here is plain text lines, not the reference repo's
 * six-line CFF panel chrome — that belongs to whoever owns rendering, and every number on this
 * screen already comes straight from {@link TabletScreen}/{@link NearbyArtilleryEntry} rather
 * than a demo bus, which was the actual point of building this app.
 *
 * <p>Two kinds of laying solution, and they must not be confused for each other: once a gun
 * reports {@code laid}, its {@code commandedAzimuthMil}/{@code commandedElevationMil} are the
 * real order already sent to the server — shown as-is. Before that, {@link #previewArc} solves
 * the same flat-fire equation client-side, using that gun's own real velocity/gravity, purely so
 * the operator has a number to look at before pressing ADJ — it is never sent anywhere and is
 * labelled PREVIEW so it is never mistaken for a live reading.
 */
@OnlyIn(Dist.CLIENT)
public final class FireControlApp extends TacticalApp {
    public static final String ID = "WPN_FIRE";

    private final TabletScreen screen;

    public FireControlApp(TabletScreen screen) {
        super(ID, "FIRE CONTROL", "[SPACE] cycle target  [F] call for fire");
        this.screen = screen;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick, int w, int h) {
        var font = Minecraft.getInstance().font;
        int x = 10;
        int y = 10;
        int lineH = font.lineHeight + 3;
        int good = 0xFF6FE07A;
        int warn = 0xFFE0C36F;
        int muted = 0xFF8A99A6;
        int text = 0xFFE8E8E6;

        g.drawString(font, "FIRE CONTROL", x, y, good, false);
        y += lineH * 2;

        List<TargetEntry> targets = screen.targets();
        int sel = screen.selectedTarget();
        TargetEntry target = (sel >= 0 && sel < targets.size()) ? targets.get(sel) : null;

        if (target == null) {
            g.drawString(font, "No target selected.", x, y, muted, false);
            y += lineH;
        } else {
            g.drawString(font, String.format("Target: X%d Y%d Z%d", target.x, target.y, target.z),
                    x, y, text, false);
            y += lineH;
        }

        NearbyArtilleryEntry gun = representativeGun();
        y += lineH;

        if (gun == null) {
            g.drawString(font, "No bound gun in range.", x, y, muted, false);
        } else {
            g.drawString(font, "Gun: " + gun.label + (gun.rounds >= 10_000
                    ? "  (" + gun.ammoLabel + ", unlimited)"
                    : "  (" + gun.ammoLabel + " x" + gun.rounds + ")"), x, y, text, false);
            y += lineH;

            if (target == null) {
                g.drawString(font, "Select a target for a laying solution.", x, y, muted, false);
            } else {
                double dist = Math.hypot(target.x - gun.x, target.z - gun.z);
                double bearingDeg = bearing(gun.x, gun.z, target.x, target.z);

                if (gun.laid) {
                    g.drawString(font, String.format("Laid: AZ %04d mil  EL %04d mil (commanded)",
                            gun.commandedAzimuthMil, gun.commandedElevationMil), x, y, good, false);
                } else {
                    double[] arc = previewArc(dist, target.y - gun.y, gun.velocity, gun.gravity, screen.depressed());
                    if (arc == null) {
                        g.drawString(font, "PREVIEW: target out of range on this arc.", x, y, warn, false);
                    } else {
                        int azMil = degToMils(bearingDeg);
                        int elMil = degToMils(arc[0]);
                        g.drawString(font, String.format("PREVIEW: AZ %04d mil  EL %04d mil  TOF %.1fs",
                                azMil, elMil, arc[1] / 20.0), x, y, warn, false);
                    }
                }
                y += lineH;
                g.drawString(font, String.format("Range %.0f m", dist), x, y, muted, false);
            }
        }

        y += lineH * 2;
        g.drawString(font, "Mode: " + screen.fireModeValue().name()
                + "   Arc: " + (screen.depressed() ? "DEPRESSED" : "LOFTED"), x, y, text, false);
    }

    /** The bound, located gun closest to giving a real reading — first one found, same rule the
     * case's own header used to pick which gun's ammo to show. */
    private NearbyArtilleryEntry representativeGun() {
        List<NearbyArtilleryEntry> roster = TabletClientData.roster();
        if (roster == null) {
            return null;
        }
        for (NearbyArtilleryEntry gun : roster) {
            if (gun.located && screen.boundIds().contains(gun.id)) {
                return gun;
            }
        }
        return null;
    }

    /** Degrees clockwise from north, matching the convention {@code NearbyArtilleryEntry}'s own
     * azimuth is reported in. North is -Z in Minecraft. */
    private static double bearing(double fromX, double fromZ, double toX, double toZ) {
        double angle = Math.toDegrees(Math.atan2(toZ - fromZ, toX - fromX)) + 90.0;
        return (angle % 360.0 + 360.0) % 360.0;
    }

    private static int degToMils(double deg) {
        double normalised = (deg % 360.0 + 360.0) % 360.0;
        return (int) Math.round(normalised / 360.0 * 6400.0) % 6400;
    }

    /**
     * Flat-fire elevation and time of flight for a shot that has not been laid yet, in the same
     * blocks/tick units {@link NearbyArtilleryEntry#velocity}/{@link NearbyArtilleryEntry#gravity}
     * already arrive in — {@code {elevationDeg, timeOfFlightTicks}}, or {@code null} if the shot
     * is beyond what this arc can reach.
     */
    private static double[] previewArc(double dist, double deltaY, double v0, double g, boolean depressed) {
        if (dist <= 0 || v0 <= 0 || g <= 0) {
            return null;
        }
        double v2 = v0 * v0;
        double discriminant = v2 * v2 - g * (g * dist * dist + 2 * deltaY * v2);
        if (discriminant < 0) {
            return null;
        }
        double root = Math.sqrt(discriminant);
        double tanTheta = depressed ? (v2 - root) / (g * dist) : (v2 + root) / (g * dist);
        double angle = Math.atan(tanTheta);
        double tof = dist / (v0 * Math.cos(angle));
        return new double[]{Math.toDegrees(angle), tof};
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        List<TargetEntry> targets = screen.targets();
        if (targets.isEmpty()) {
            return false;
        }
        if (keyCode == 32) { // Space
            int next = (screen.selectedTarget() + 1) % targets.size();
            screen.select(next);
            return true;
        }
        if (keyCode == 70 || keyCode == 257) { // F, Enter
            int sel = screen.selectedTarget();
            screen.fire(sel >= 0 ? sel : 0);
            return true;
        }
        return false;
    }
}
