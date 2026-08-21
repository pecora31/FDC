package net.nazarick.artillerytablet.client.hud;

import net.nazarick.artillerytablet.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.nazarick.artillerytablet.client.screen.TabletTheme;
import net.nazarick.artillerytablet.client.screen.Ui;
import net.nazarick.artillerytablet.fire.MissionState;
import net.nazarick.artillerytablet.fire.TargetStatus;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.item.BoundArtillery;
import net.nazarick.artillerytablet.item.TargetEntry;
import net.nazarick.artillerytablet.network.RequestTargetReachabilityMessage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Fixed status panel drawn while the tablet is held — the fire-direction centre's readout.
 *
 * <p>Most of it comes straight off the held stack's NBT, which the server already syncs, so no
 * polling is needed. Only live mission state (slewing / holding / shell in flight) is pushed, by
 * FireMissionStatusMessage. Danger-close is judged here on the client against the players it can
 * see; that makes it advisory rather than authoritative, which is the right trade for a warning.
 */
@OnlyIn(Dist.CLIENT)
public class TabletHudOverlay implements IGuiOverlay {
    public static final TabletHudOverlay INSTANCE = new TabletHudOverlay();

    /** Blocks from a target inside which friendly players get flagged. */
    private static final double DANGER_CLOSE_RADIUS = 24.0;

    private static final int MARGIN = 6;
    private static final int LINE_HEIGHT = 9;
    private static final int PANEL_WIDTH = 168;

    private static final int COLOUR_TITLE = TabletTheme.FRIENDLY;
    private static final int COLOUR_TEXT = TabletTheme.TEXT;
    private static final int COLOUR_MUTED = TabletTheme.MUTED;
    private static final int COLOUR_WARN = TabletTheme.WARNING;

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        ItemStack stack = heldTablet(player);
        if (stack == null) {
            return;
        }

        List<TargetEntry> targets = ArtilleryTacticalTabletItem.getTargets(stack);
        List<BoundArtillery> bound = ArtilleryTacticalTabletItem.getBoundArtillery(stack);
        boolean depressed = ArtilleryTacticalTabletItem.isDepressed(stack);
        long now = mc.level.getGameTime();
        Map<UUID, FireMissionClientState.Entry> missions = FireMissionClientState.active(now);

        int signature = signatureOf(targets, bound, depressed);
        if (TargetReachabilityCache.shouldRequest(signature, now)) {
            ModNetwork.toServer(new RequestTargetReachabilityMessage(
                    player.getMainHandItem() == stack, signature));
        }

        int lines = 3 + Math.max(1, targets.size()) + (missions.isEmpty() ? 0 : 1 + missions.size());
        int panelHeight = lines * LINE_HEIGHT + Ui.GAP_MD;
        int left = MARGIN;
        int top = screenHeight / 2 - panelHeight / 2;

        graphics.fill(left - Ui.GAP_SM, top - Ui.GAP_SM, left + PANEL_WIDTH, top + panelHeight, 0xE6141A21);
        graphics.fill(left - Ui.GAP_SM, top - Ui.GAP_SM, left + PANEL_WIDTH, top - Ui.GAP_SM + 2, COLOUR_TITLE);

        int y = top;
        Ui.textIn(graphics, Component.translatable("hud.artillerytablet.title"), left, y, LINE_HEIGHT, COLOUR_TITLE);
        y += LINE_HEIGHT;

        Ui.textIn(graphics, Component.translatable("hud.artillerytablet.guns", bound.size(),
                ArtilleryTacticalTabletItem.MAX_BOUND), left, y, LINE_HEIGHT, COLOUR_TEXT);
        y += LINE_HEIGHT;

        Component arc = Component.translatable(depressed
                ? "gui.artillerytablet.trajectory_depressed"
                : "gui.artillerytablet.trajectory_lofted");
        Ui.textIn(graphics, Component.literal(ArtilleryTacticalTabletItem.getFireMode(stack).name() + " / ")
                .append(arc), left, y, LINE_HEIGHT, COLOUR_TEXT);
        y += LINE_HEIGHT;

        if (targets.isEmpty()) {
            Ui.textIn(graphics, Component.translatable("gui.artillerytablet.no_targets"), left, y, LINE_HEIGHT, COLOUR_MUTED);
            y += LINE_HEIGHT;
        } else {
            for (int i = 0; i < targets.size(); i++) {
                TargetEntry target = targets.get(i);
                TargetStatus status = TargetReachabilityCache.statusOf(signature, i);
                boolean dangerClose = isDangerClose(mc, target);

                MutableComponent line = Component.literal((i + 1) + ": " + target.x + " " + target.y + " " + target.z);
                // Reach problems first: they decide whether the gun fires at all, whereas danger
                // close only says to think twice about a shot that would otherwise happen.
                if (status != TargetStatus.OK) {
                    line = line.append(reachWarning(status, depressed));
                } else {
                    // A shot the gun can lay onto can still be stopped by a hill in between, and
                    // that is worth saying only when nothing else already rules the target out.
                    int obstruction = TargetReachabilityCache.obstructionOf(signature, i, target);
                    if (obstruction >= 0) {
                        line = line.append(Component.translatable("hud.artillerytablet.blocked", obstruction));
                    }
                }
                if (dangerClose) {
                    line = line.append(Component.translatable("hud.artillerytablet.danger_close"));
                }

                int colour = status != TargetStatus.OK || dangerClose ? COLOUR_WARN : COLOUR_TEXT;
                Ui.textIn(graphics, line, left, y, LINE_HEIGHT, colour);
                y += LINE_HEIGHT;
            }
        }

        if (!missions.isEmpty()) {
            y += 2;
            Ui.textIn(graphics, Component.translatable("hud.artillerytablet.missions"), left, y, LINE_HEIGHT, COLOUR_TITLE);
            y += LINE_HEIGHT;

            for (FireMissionClientState.Entry entry : missions.values()) {
                Ui.textIn(graphics, describe(entry, now), left, y, LINE_HEIGHT, colourFor(entry.state));
                y += LINE_HEIGHT;
            }
        }

    }

    private static Component describe(FireMissionClientState.Entry entry, long now) {
        if (entry.state != MissionState.IN_FLIGHT) {
            return Component.translatable("hud.artillerytablet.state." + entry.state.name().toLowerCase());
        }

        long ticks = entry.ticksToImpact(now);
        if (ticks <= 0) {
            return Component.translatable("hud.artillerytablet.impact");
        }
        return Component.translatable("hud.artillerytablet.time_of_flight", String.format("%.1f", ticks / 20.0));
    }

    private static int colourFor(MissionState state) {
        return switch (state) {
            case ABORTED -> COLOUR_WARN;
            case IN_FLIGHT -> COLOUR_TITLE;
            default -> COLOUR_TEXT;
        };
    }

    /** Names the specific arc to switch to, since "wrong arc" alone leaves the player guessing. */
    private static Component reachWarning(TargetStatus status, boolean depressed) {
        return switch (status) {
            case MIN_RANGE -> Component.translatable("hud.artillerytablet.min_range");
            case MAX_RANGE -> Component.translatable("hud.artillerytablet.max_range");
            case USE_OTHER_ARC -> Component.translatable(depressed
                    ? "hud.artillerytablet.use_lofted"
                    : "hud.artillerytablet.use_depressed");
            default -> Component.empty();
        };
    }

    /** Everything the reachability answer depends on, so a change invalidates the cached reply. */
    private static int signatureOf(List<TargetEntry> targets, List<BoundArtillery> bound, boolean depressed) {
        int hash = depressed ? 1 : 0;
        for (TargetEntry target : targets) {
            hash = hash * 31 + target.x;
            hash = hash * 31 + target.y;
            hash = hash * 31 + target.z;
        }
        for (BoundArtillery entry : bound) {
            hash = hash * 31 + entry.id.hashCode();
        }
        return hash;
    }

    private static boolean isDangerClose(Minecraft mc, TargetEntry target) {
        if (mc.level == null) {
            return false;
        }
        for (Player other : mc.level.players()) {
            double dx = other.getX() - (target.x + 0.5);
            double dy = other.getY() - target.y;
            double dz = other.getZ() - (target.z + 0.5);
            if (dx * dx + dy * dy + dz * dz <= DANGER_CLOSE_RADIUS * DANGER_CLOSE_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack heldTablet(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof ArtilleryTacticalTabletItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        return off.getItem() instanceof ArtilleryTacticalTabletItem ? off : null;
    }
}
