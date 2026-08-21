package net.nazarick.artillerytablet.client.screen;

import net.nazarick.artillerytablet.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.client.FireLog;
import net.nazarick.artillerytablet.client.TabletClientData;
import net.nazarick.artillerytablet.client.hud.FireMissionClientState;
import net.nazarick.artillerytablet.fire.MissionState;
import net.nazarick.artillerytablet.network.AbortMissionMessage;
import net.nazarick.artillerytablet.client.hud.FireMissionClientState;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.network.AmmoOptionEntry;
import net.nazarick.artillerytablet.network.BindArtilleryMessage;
import net.nazarick.artillerytablet.network.NearbyArtilleryEntry;
import net.nazarick.artillerytablet.network.RequestNearbyArtilleryMessage;
import net.nazarick.artillerytablet.network.SelectAmmoMessage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Renderers and controls for the tablet's side panel, one per tab. Split out of
 * {@link TabletScreen} purely so that class stays about layout rather than about every tab's
 * contents.
 */
@OnlyIn(Dist.CLIENT)
final class TabletPanels {
    private TabletPanels() {
    }

    /** Clips text to a pixel width, marking it so a cut name doesn't read as the whole name. */
    private static String trim(TabletScreen screen, String text, int maxWidth) {
        if (TabletTheme.width(screen.font(), text) <= maxWidth) {
            return text;
        }
        return screen.font().plainSubstrByWidth(text, maxWidth - TabletTheme.width(screen.font(), "...")) + "...";
    }

    // ---- Battery -------------------------------------------------------------------------

    static void buildBattery(TabletScreen screen, int x, int y, int width, int height) {
        List<NearbyArtilleryEntry> entries = TabletClientData.roster();
        if (entries == null) {
            return;
        }

        Set<UUID> bound = boundIds(screen);
        int rowY = y + Ui.GAP_MD;
        for (NearbyArtilleryEntry entry : entries) {
            if (rowY > y + height - 18) {
                break;
            }
            UUID id = entry.id;
            boolean isBound = bound.contains(entry.id);
            int cardY = rowY;

            // The card selects and nothing more. Every key that used to live on it is in the gun's
            // own menu now — a column this narrow cannot hold a readout and a control panel at once,
            // which is why text and keys collided here three separate times.
            screen.addWidget(new UiButton(x, rowY, width, ROW_FOUR_LINE,
                    Component.empty(), () -> screen.selectGun(id)).invisible());

            // A gun not yet bound has one thing worth doing, so it keeps a key rather than hiding it
            // behind a menu on something not yet part of the battery.
            if (!isBound) {
                screen.addWidget(new UiButton(x + width - 34, rowY, 34, 11,
                        Component.translatable("gui.artillerytablet.bind"),
                        () -> toggleBind(screen, entry, false)).active(canBind(entry)));
            } else {
                screen.addWidget(new UiButton(x + width - 12, rowY, 12, 11, Component.literal("···"),
                        () -> screen.openGunMenu(id, x + width - 12, cardY + 11)));
            }
            rowY += ROW_FOUR_LINE;
        }

        screen.addWidget(new UiButton(x + Ui.GAP_XS, y + height - 13, width - Ui.GAP_SM, 11, Component.translatable("gui.artillerytablet.refresh"), () -> {
                    TabletClientData.clearRoster();
                    ModNetwork.toServer(new RequestNearbyArtilleryMessage(screen.isMainHand()));
                    screen.refresh();
                }));
    }

    static void renderBattery(TabletScreen screen, GuiGraphics g, int x, int y) {
        List<NearbyArtilleryEntry> entries = TabletClientData.roster();
        if (entries == null) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.loading"), x, y, TabletScreen.contentWidth(), TabletScreen.colourMuted());
            return;
        }
        if (entries.isEmpty()) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.no_nearby"), x, y, TabletScreen.contentWidth(), TabletScreen.colourMuted());
            return;
        }

        Set<UUID> bound = boundIds(screen);
        long now = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        int rowY = y + Ui.GAP_MD;
        for (NearbyArtilleryEntry entry : entries) {
            boolean isBound = bound.contains(entry.id);
            String trimmed = trim(screen, entry.label, TabletScreen.contentWidth() - 38);
            if (entry.id.equals(screen.selectedGun())) {
                // Fill and a border. The fill alone was a shade apart from the panel behind it, which
                // is not enough to find at a glance on a list this dense.
                int bx = x - Ui.GAP_SM;
                int bw = TabletScreen.contentWidth() + Ui.GAP_SM * 2;
                Ui.rect(g, bx, rowY - Ui.GAP_XS, bw, ROW_FOUR_LINE, TabletScreen.colourSelected());
                Ui.outline(g, bx, rowY - Ui.GAP_XS, bw, ROW_FOUR_LINE, TabletScreen.colourAccent());
            }
            TabletTheme.draw(g, screen.font(), trimmed, x, rowY, isBound ? TabletScreen.colourGood() : TabletScreen.colourText(), false);
            Component where = entry.located
                    ? Component.literal(String.format("%d %d  %.0fm", entry.x, entry.z, entry.distance))
                    : Component.translatable("gui.artillerytablet.position_unknown");
            TabletTheme.draw(g, screen.font(), where, x, rowY + ROW_LINE, TabletScreen.colourMuted(), false);

            // The firing solution: where the barrel is, and where it has been told to be. This is
            // what a gun display shows and the tablet never did — it quoted range and bearing to the
            // target, which is the observer's language, not the battery's.
            TabletTheme.draw(g, screen.font(), solutionLine(entry), x, rowY + ROW_LINE * 2,
                    entry.laid && entry.offMil() > OFF_TOLERANCE_MIL
                            ? TabletScreen.colourWarn() : TabletScreen.colourText(), false);

            // What the gun is doing, on the gun. This was a column of its own until it became clear
            // a fire mission has no meaning apart from the piece carrying it out.
            FireMissionClientState.Entry mission = missionOf(entry.id);
            TabletTheme.draw(g, screen.font(), missionLine(mission, now), x, rowY + ROW_LINE * 3,
                    missionColour(mission), false);
            rowY += ROW_FOUR_LINE;
        }
    }

    /** Within this many mils the barrel counts as on its bearing rather than still travelling. */
    private static final int OFF_TOLERANCE_MIL = 18;

    /**
     * Azimuth and elevation, current and commanded.
     *
     * <p>Fixed-width fields with leading zeros, the way a gun display writes them: the numbers stay
     * in the same place as they change, so a barrel coming onto its bearing reads as movement rather
     * than as text reflowing.
     */
    private static Component solutionLine(NearbyArtilleryEntry entry) {
        if (!entry.located) {
            return Component.literal("");
        }
        if (!entry.laid) {
            return Component.literal(String.format("AZ %04d QE %04d",
                    Math.floorMod(entry.azimuthMil, 6400), Math.max(0, entry.elevationMil)));
        }
        return Component.literal(String.format("AZ %04d→%04d",
                Math.floorMod(entry.azimuthMil, 6400), Math.floorMod(entry.commandedAzimuthMil, 6400)));
    }

    private static FireMissionClientState.Entry missionOf(UUID gun) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return null;
        }
        return FireMissionClientState.active(mc.level.getGameTime()).get(gun);
    }

    /** Laying, holding for ammunition, shell in the air with its countdown, or simply ready. */
    private static Component missionLine(FireMissionClientState.Entry mission, long now) {
        if (mission == null) {
            return Component.translatable("gui.artillerytablet.gun_ready");
        }
        if (mission.state != MissionState.IN_FLIGHT) {
            return Component.translatable("hud.artillerytablet.state." + mission.state.name().toLowerCase());
        }
        long ticks = mission.ticksToImpact(now);
        return ticks <= 0
                ? Component.translatable("hud.artillerytablet.impact")
                : Component.translatable("hud.artillerytablet.time_of_flight", String.format("%.1f", ticks / 20.0));
    }

    private static int missionColour(FireMissionClientState.Entry mission) {
        if (mission == null) {
            return TabletScreen.colourMuted();
        }
        return switch (mission.state) {
            case ABORTED -> TabletScreen.colourWarn();
            case IN_FLIGHT -> TabletScreen.colourAccent();
            // Laid and standing by is the battery ready, so it reads as ready. Held up by an empty
            // gun is a problem, and the two must not look alike.
            case LAID -> TabletScreen.colourGood();
            case WAITING -> TabletScreen.colourWarn();
            default -> TabletScreen.colourText();
        };
    }

    private static boolean canBind(NearbyArtilleryEntry entry) {
        return entry.located && entry.distance <= RequestNearbyArtilleryMessage.NEARBY_RADIUS;
    }

    private static void toggleBind(TabletScreen screen, NearbyArtilleryEntry entry, boolean isBound) {
        ModNetwork.toServer(
                new BindArtilleryMessage(entry.id, !isBound, screen.isMainHand()));
        screen.setBoundOptimistically(entry.id, !isBound);
        screen.refresh();
    }

    /**
     * The screen's own view of what is bound, which updates the instant a button is pressed rather
     * than a round trip later.
     */
    private static Set<UUID> boundIds(TabletScreen screen) {
        return screen.boundIds();
    }

    // ---- Ammunition ----------------------------------------------------------------------

    static void buildAmmo(TabletScreen screen, int x, int y, int width, int height) {
        List<AmmoOptionEntry> options = TabletClientData.ammo();
        if (options == null) {
            return;
        }

        int rowY = y + Ui.GAP_MD;
        for (AmmoOptionEntry option : options) {
            if (rowY > y + height - 8) {
                break;
            }
            boolean selected = isSelected(screen, option);
            UiButton button = new UiButton(x + width - 46, rowY, 40, 12,
                    Component.translatable(selected
                            ? "gui.artillerytablet.ammo_selected"
                            : "gui.artillerytablet.ammo_select"),
                    () -> selectAmmo(screen, option)).active(!selected);
            screen.addWidget(button);
            rowY += ROW_TWO_LINE;
        }
    }

    /**
     * One line of an ammunition row, and the pitch of the whole row.
     *
     * <p>Both the battery list and the ammunition list put two lines in a row — a name, and the
     * detail under it — so their pitch cannot be the single-line one the log uses. It was, and the
     * second line sat two pixels into the name below it in both places. Each list has a builder and
     * a renderer that take the row apart separately, so all four read these.
     */
    private static final int ROW_LINE = 9;
    private static final int ROW_TWO_LINE = ROW_LINE * 2 + Ui.GAP_XS;

    /**
     * A gun card: what it is, where it is, the firing solution, and what it is doing.
     *
     * <p>Four lines rather than three. The card was sized to fit the column and the column won,
     * which left no room for the two numbers a gun is actually laid by. A card can be as tall as it
     * needs; a laying angle cannot be abbreviated into nothing.
     */
    private static final int ROW_FOUR_LINE = ROW_LINE * 4 + Ui.GAP_SM;

    /** Rows the battery list currently has, so the screen can bound its scroll to real content. */
    static int batteryRows() {
        java.util.List<net.nazarick.artillerytablet.network.NearbyArtilleryEntry> e = TabletClientData.roster();
        return e == null ? 0 : e.size();
    }

    static int rowPitch() {
        return ROW_FOUR_LINE;
    }

    static void renderAmmo(TabletScreen screen, GuiGraphics g, int x, int y) {
        List<AmmoOptionEntry> options = TabletClientData.ammo();
        if (options == null) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.loading"), x, y, TabletScreen.contentWidth(), TabletScreen.colourMuted());
            return;
        }
        if (options.isEmpty()) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.no_ammo_options"), x, y, TabletScreen.contentWidth(), TabletScreen.colourMuted());
            return;
        }

        int rowY = y + Ui.GAP_MD;
        for (AmmoOptionEntry option : options) {
            // Ammunition names are translated and can run long; clip to the space left beside the
            // button instead of letting the text spill past the tablet's edge.
            String name = trim(screen, option.displayName, screen.sideWidth() - 56);
            TabletTheme.draw(g, screen.font(), name, x, rowY,
                    isSelected(screen, option) ? TabletScreen.colourGood() : TabletScreen.colourText(), false);
            Component count = option.available < 0
                    ? Component.translatable("gui.artillerytablet.ammo_unlimited")
                    : Component.literal("x" + option.available);
            TabletTheme.draw(g, screen.font(), count, x, rowY + ROW_LINE, TabletScreen.colourMuted(), false);
            rowY += ROW_TWO_LINE;
        }
    }

    /** A pending choice wins until the server's next list replaces it. */
    private static boolean isSelected(TabletScreen screen, AmmoOptionEntry option) {
        String pending = screen.pendingAmmoId();
        return pending == null ? option.selected : pending.equals(option.ammoId);
    }

    private static void selectAmmo(TabletScreen screen, AmmoOptionEntry option) {
        ModNetwork.toServer(
                new SelectAmmoMessage(screen.isMainHand(), option.ammoId));
        FireLog.record("Ammo: " + option.displayName);
        screen.setPendingAmmo(option.ammoId);
        screen.refresh();
    }

    // ---- Status --------------------------------------------------------------------------

    static void renderStatus(TabletScreen screen, GuiGraphics g, int x, int y) {
        if (screen.mc() == null || screen.mc().level == null) {
            return;
        }

        long now = screen.mc().level.getGameTime();
        var missions = FireMissionClientState.active(now);
        if (missions.isEmpty()) {
            TabletTheme.draw(g, screen.font(), Component.translatable("gui.artillerytablet.no_missions"), x, y, TabletScreen.colourMuted(), false);
            return;
        }

        int rowY = y;
        for (var entry : missions.values()) {
            Component line = switch (entry.state) {
                case IN_FLIGHT -> {
                    long ticks = entry.ticksToImpact(now);
                    yield ticks <= 0
                            ? Component.translatable("hud.artillerytablet.impact")
                            : Component.translatable("hud.artillerytablet.time_of_flight", String.format("%.1f", ticks / 20.0));
                }
                default -> Component.translatable("hud.artillerytablet.state." + entry.state.name().toLowerCase());
            };
            rowY += Ui.textWrapped(g, line, x, rowY, TabletScreen.contentWidth(), TabletScreen.colourText()) + Ui.GAP_XS;
        }
    }

    // ---- Fire log ------------------------------------------------------------------------

    static void renderLog(TabletScreen screen, GuiGraphics g, int x, int y) {
        List<FireLog.Entry> entries = FireLog.entries();
        if (entries.isEmpty()) {
            Ui.textWrapped(g, Component.translatable("gui.artillerytablet.no_log"), x, y, TabletScreen.contentWidth(), TabletScreen.colourMuted());
            return;
        }

        int rowY = y;
        for (FireLog.Entry entry : entries) {
            if (rowY > y + 60) {
                break;
            }
            rowY += Ui.textWrapped(g, entry.text(), x, rowY, TabletScreen.contentWidth(), TabletScreen.colourText()) + Ui.GAP_XS;
        }
    }
}
