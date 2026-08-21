package net.nazarick.artillerytablet.terrain;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Surveys ground quietly, before anybody asks for it.
 *
 * <p>The web map plugins that do server-side mapping — Dynmap and the rest — solve exactly the
 * expensive half of this problem, and the thing to learn from them is not how they read a chunk. It
 * is <em>when</em>. They do it as a batch render run while the server is quiet, with a configurable
 * number of threads, and everyone accepts that it takes a while. Their own documentation warns that
 * starting one while players are online costs tick time.
 *
 * <p>This tablet was doing the same amount of work, in the same way, at the moment a player opened
 * the screen and stood there waiting for it. No amount of tuning fixes a race against a disk; the
 * answer is to have finished before the race starts.
 *
 * <p>So: work ahead. Around each player, spiralling outward, skipping anything the store already
 * holds. By the time the tablet is opened the ground is filed and the map is a file read rather than
 * a survey.
 *
 * <p><b>At two speeds, and the difference is the point.</b> This ran at a flat sixteen tiles a
 * second, chosen when nothing had been measured. Then it was measured: the survey path delivers
 * sixty to a hundred and seventy, and the server's own trace read {@code 0 running of 256} on line
 * after line. The server was idle almost all of the time, and a player who walked somewhere new
 * still waited for their map — a machine doing nothing, next to somebody waiting.
 *
 * <p>The fix is not a bigger number. A bigger flat number is the same mistake pointed the other way:
 * it would be surveying hardest at exactly the moments a player is watching a map fill in. What was
 * missing is that the right rate depends on something the server can simply <em>ask</em> — is anyone
 * waiting right now? A client only requests ground while a tablet is in hand, and only when it is
 * short of something, so the requests themselves are the signal. Quiet, and this runs at whatever
 * the spare budget will take. Somebody watching, and it drops back to the trickle and stays out of
 * the way.
 *
 * <p><b>It gives way to the game, always.</b> Nothing here is worth a stutter — the map still works
 * without it, merely slower — so it stops on any tick that has run long, and it stops when the tick
 * loop says it has no time to spare. A background task that has to be noticed has failed.
 */
@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerTerrainWarmer {
    /**
     * How far out to work, in tiles. Forty-eight is three thousand blocks, which is about what the
     * guns reach and rather more than a map view holds.
     */
    private static final int REACH_TILES = 48;

    /** Ticks between passes. */
    private static final int EVERY_TICKS = 5;

    /** Passes a second, which is what turns a per-pass count into a rate. */
    private static final int PASSES_PER_SECOND = 20 / EVERY_TICKS;

    /**
     * Tiles started per pass while somebody is watching a map fill in.
     *
     * <p>A trickle, and it should stay one. The player waiting on the screen is the whole reason
     * this class exists; taking disk from them to warm ground nobody has asked for would be the
     * exact inversion of the point.
     */
    private static final int PER_PASS_BUSY = 4;

    /**
     * How long, in passes, the quiet rate should take to fill the whole warming budget.
     *
     * <p>The one number chosen by hand here, and it is a choice about <em>smoothness</em> rather
     * than about throughput: the budget is what caps the work, so this only decides how abruptly the
     * server ramps into it after a quiet moment begins. Two seconds is far below anything a player
     * notices and far above one tick's worth of chunk lookups.
     */
    private static final int PASSES_TO_FILL = 2 * PASSES_PER_SECOND;

    /**
     * Tiles started per pass when nobody is waiting on anything.
     *
     * <p>Worked out from the budget rather than picked, because the budget is what actually decides
     * how much warming happens: {@link ServerSurveyBudget#beginWarming} refuses past
     * {@code SPARE_FOR_WARMING}, so feeding faster than that only spins this loop. What is being
     * sized here is the <em>feed</em> — enough to keep the budget full, not more.
     *
     * <p>This is the whole of what "adaptive warming" means, and it is worth stating what it fixes.
     * The rate was a flat sixteen tiles a second against a survey path measured at sixty to a
     * hundred and seventy, and the trace showed the server idle — {@code 0 running of 256} on every
     * line — while a player who walked somewhere new still waited for their map. The capacity was
     * there the whole time and nothing was allowed to reach for it.
     */
    private static final int PER_PASS_QUIET =
            Math.max(PER_PASS_BUSY, SurveyLimits.SPARE_FOR_WARMING / PASSES_TO_FILL);

    /**
     * Longest mean tick this will work through.
     *
     * <p>A server at fifty milliseconds a tick is at twenty per second and has nothing to spare.
     * Well under that, and the survey is using time nobody wanted.
     */
    private static final float TICK_BUDGET_MS = 35f;

    /**
     * Where each player's spiral has got to.
     *
     * <p>Per player rather than global, so two people in different countries each get the ground
     * around them warmed rather than taking turns. It is an index into the ring order below, and it
     * simply keeps going: when a player moves, the same index now means somewhere else, and the
     * store's index makes re-treading old ground free.
     */
    private static final Map<UUID, Integer> CURSOR = new HashMap<>();

    /**
     * Offsets from a player's tile, nearest first.
     *
     * <p>Built once. Nearest first matters: ground under the player's feet is what a fire-direction
     * officer opens the tablet to look at, and the far corners are what nobody minds waiting for.
     */
    private static final int[][] RINGS = buildRings();

    private ServerTerrainWarmer() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = event.getServer();
        if (server.getTickCount() % EVERY_TICKS != 0) {
            return;
        }
        // Two ways of asking the same question, and both have to say yes. The average catches a
        // server that is generally struggling; haveTime catches the tick that is struggling now.
        if (server.getAverageTickTime() > TICK_BUDGET_MS || !event.haveTime()) {
            return;
        }
        // The whole of the adaptation, in one line. Everything else about this class is unchanged:
        // the same spiral, the same budget, the same giving way to a busy tick. What was wrong was
        // only ever the rate, and the rate was flat.
        int perPass = ServerSurveyBudget.anyoneWatching() ? PER_PASS_BUSY : PER_PASS_QUIET;

        // Whoever went first last time goes last this time.
        //
        // The budget is shared and a pass stops the moment it is refused, so without this the first
        // player in the list takes as much of the warming budget as they can use and everybody
        // behind them gets whatever is left — which, on a busy server warming hard, is nothing. The
        // effect is invisible in single player, which is exactly the kind of thing that has to be
        // right by construction rather than by testing.
        java.util.List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }
        firstPlayer = (firstPlayer + 1) % players.size();
        for (int i = 0; i < players.size(); i++) {
            warmAround(players.get((firstPlayer + i) % players.size()), perPass);
        }
    }

    /** Which player's turn it is to be offered the warming budget first. */
    private static int firstPlayer;

    private static void warmAround(ServerPlayer player, int perPass) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        int centreX = TerrainTile.blockToTile((int) player.getX());
        int centreZ = TerrainTile.blockToTile((int) player.getZ());
        int at = CURSOR.getOrDefault(player.getUUID(), 0);

        int started = 0;
        // Bounded by the ring count as well as by the budget, so a player standing in country that is
        // entirely surveyed does not spin through the whole spiral in one tick looking for work.
        for (int step = 0; step < RINGS.length && started < perPass; step++) {
            int[] offset = RINGS[at];
            at = (at + 1) % RINGS.length;

            int tileX = centreX + offset[0];
            int tileZ = centreZ + offset[1];
            if (ServerTileStore.holds(level, tileX, tileZ)) {
                // Already filed. Free to skip, which is the whole reason the store keeps an index.
                continue;
            }

            // Through the budget, exactly like a player's request, because a survey costs the same
            // whoever wanted it. Asking the budget whether there was room and then surveying outside
            // it — which is what this did — meant the one figure the server holds itself down by did
            // not count the only work the server starts on its own.
            if (!ServerSurveyBudget.beginWarming()) {
                break;
            }

            // Straight through the cache, so this shares the survey and the store with everything
            // else. Nothing is done with the result: filing it away is the point.
            boolean handedOver = false;
            try {
                ServerTileCache.get(level, tileX, tileZ)
                        .whenComplete((entry, error) -> ServerSurveyBudget.finishWarming());
                handedOver = true;
            } finally {
                if (!handedOver) {
                    ServerSurveyBudget.finishWarming();
                }
            }
            started++;
        }

        CURSOR.put(player.getUUID(), at);
    }

    /**
     * Forgets where every player's spiral had got to, when the server stops.
     *
     * <p>An index into the ring is meaningless in the next world, and the map it lives in is keyed
     * by UUID, so without this it grows by one entry per player per world for the life of the
     * process. Harmless on its own, and the third static in this package to need saying so.
     */
    @SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        CURSOR.clear();
        firstPlayer = 0;
    }

    /** Every offset within reach, sorted by how far it is from the middle. */
    private static int[][] buildRings() {
        int side = REACH_TILES * 2 + 1;
        int[][] rings = new int[side * side][];
        int at = 0;
        for (int dx = -REACH_TILES; dx <= REACH_TILES; dx++) {
            for (int dz = -REACH_TILES; dz <= REACH_TILES; dz++) {
                rings[at++] = new int[]{dx, dz};
            }
        }
        java.util.Arrays.sort(rings, java.util.Comparator.comparingInt(o -> o[0] * o[0] + o[1] * o[1]));
        return rings;
    }
}
