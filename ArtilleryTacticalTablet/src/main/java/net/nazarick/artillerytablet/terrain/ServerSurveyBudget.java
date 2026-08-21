package net.nazarick.artillerytablet.terrain;

import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * How much surveying the server will do at once, for everybody together.
 *
 * <p>Every limit the map has until now lives on the client: how many tiles it asks for in a sweep,
 * how many it lets itself have outstanding. Those are the right numbers for one player and they are
 * the wrong shape entirely for a server, because they are <em>per client</em>. Ten people with the
 * tablet open is ten times the requests, ten times the chunk reads, and ten times the disk — and the
 * server had nothing to say about it. That is a mod that behaves impeccably in single player and
 * falls over the first evening it is put somewhere real.
 *
 * <p>So the ceiling moves to where the resource actually is. The server decides how much it is
 * prepared to do <em>at once</em>, and what it has no room for it <b>keeps</b>.
 *
 * <p>Keeping is the part the first version got wrong. It refused what it had no room for and left
 * the client to ask again five seconds later, which sounds harmless and was not. A client holds a
 * window of requests open, so the refused ones sit in that window doing nothing and it stops asking
 * for anything new — and because they are scattered through the order it asked in, so are the holes.
 * What that looked like was a map filling outward in a neat ring speckled with black, and long
 * seconds where nothing arrived at all. A queue turns it into a ring that simply advances.
 *
 * <p>The per-player share exists for a different reason from the total. The total protects the
 * server; the share protects the other players from the one who has just opened a map of somewhere
 * nobody has ever been.
 */
@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerSurveyBudget {
    // The numbers themselves are in SurveyLimits, with the client's window they have to agree with.
    // Named here only so the code below reads as prose.

    private static final int TOTAL = SurveyLimits.TOTAL;
    private static final int PER_PLAYER = SurveyLimits.PER_PLAYER;
    private static final int SPARE_FOR_WARMING = SurveyLimits.SPARE_FOR_WARMING;

    /**
     * Requests waiting for room, per player.
     *
     * <p>Bounded, and it is the <em>oldest</em> that goes when it overflows. A player who has panned
     * across a continent has a queue full of ground they are no longer looking at, and the newest
     * request is always the one nearest what is on their screen now.
     */
    private static final int MAX_QUEUED_PER_PLAYER = SurveyLimits.MAX_QUEUED_PER_PLAYER;

    private static final AtomicInteger RUNNING = new AtomicInteger();
    private static final Map<UUID, AtomicInteger> PER = new ConcurrentHashMap<>();
    private static final class QueuedTask {
        final Runnable start;
        final Runnable onDrop;

        QueuedTask(Runnable start, Runnable onDrop) {
            this.start = start;
            this.onDrop = onDrop;
        }
    }

    private static final Map<UUID, ArrayDeque<QueuedTask>> QUEUED = new ConcurrentHashMap<>();

    private ServerSurveyBudget() {
    }

    /**
     * Takes a survey, starting it now if there is room and holding it if there is not.
     *
     * <p>The task must call {@link #finish} when its survey completes, however it completes.
     */
    public static void submit(UUID player, Runnable start, Runnable onDrop) {
        if (tryStart(player)) {
            run(player, start);
            return;
        }
        ArrayDeque<QueuedTask> queue = QUEUED.computeIfAbsent(player, key -> new ArrayDeque<>());
        synchronized (queue) {
            if (queue.size() >= MAX_QUEUED_PER_PLAYER) {
                QueuedTask dropped = queue.pollFirst();
                if (dropped != null && dropped.onDrop != null) {
                    dropped.onDrop.run();
                }
            }
            queue.addLast(new QueuedTask(start, onDrop));
        }
    }

    /**
     * Starts one survey that has already claimed its room.
     *
     * <p>The claim and its release are a pair, and until now the release lived entirely inside the
     * task: a task that threw before it could arrange to call {@link #finish} took its slot with it
     * for good. Nothing announces that. The map simply serves a little less at a time, then a little
     * less again, and after enough of them it stops serving anything at all — a fault that arrives
     * hours after its cause and looks like the map having died on its own.
     */
    private static void run(UUID player, Runnable start) {
        boolean started = false;
        try {
            start.run();
            started = true;
        } finally {
            if (!started) {
                // It threw, so nothing it might have arranged can be relied on. The slot comes back
                // here or nowhere.
                finish(player);
            }
        }
    }

    /**
     * Starts whatever the freed room allows, once a tick.
     *
     * <p>Every player is offered as much as their own share permits, so fairness falls out of the
     * per-player cap rather than needing a turn order of its own.
     */
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) {
            return;
        }
        report(event.getServer().getTickCount());

        for (Map.Entry<UUID, ArrayDeque<QueuedTask>> entry : QUEUED.entrySet()) {
            ArrayDeque<QueuedTask> queue = entry.getValue();
            while (!queue.isEmpty() && tryStart(entry.getKey())) {
                QueuedTask next;
                synchronized (queue) {
                    next = queue.pollFirst();
                }
                if (next == null) {
                    // Emptied between the two checks. Give the room straight back.
                    finish(entry.getKey());
                    break;
                }
                run(entry.getKey(), next.start);
            }
        }
    }

    /** Ticks between reports, so the log says where the ground came from without becoming the noise. */
    private static final int REPORT_TICKS = 100;

    /**
     * Says what the server has been doing, when it has been doing anything.
     *
     * <p>Silent when idle on purpose: a line every five seconds saying nothing happened is how a
     * useful log becomes one nobody reads.
     */
    private static void report(int tick) {
        if (tick % REPORT_TICKS != 0) {
            return;
        }
        int[] counts = ServerTileCache.takeCounts();
        int warmed = WARMED.getAndSet(0);
        if (counts[0] == 0 && counts[1] == 0 && warmed == 0) {
            return;
        }
        int queued = 0;
        for (ArrayDeque<QueuedTask> queue : QUEUED.values()) {
            queued += queue.size();
        }
        // Warming is reported beside the rest because it is the half nobody can see. A survey that
        // finished before anybody asked leaves no trace on any screen — the map simply appears — so
        // without this line the only evidence of it working is the absence of a wait.
        ArtilleryTablet.LOGGER.info("tile source: {} from store, {} surveyed, in {}s"
                        + " | {} running of {}, {} queued | {} warmed ({})",
                counts[0], counts[1], REPORT_TICKS / 20, RUNNING.get(), TOTAL, queued,
                warmed, anyoneWatching() ? "someone watching" : "quiet");
    }

    /**
     * Claims room for one survey on a player's behalf.
     *
     * @return false when the server or that player is already at their limit, in which case the
     *         caller queues the work rather than dropping it
     */
    private static boolean tryStart(UUID player) {
        AtomicInteger mine = PER.computeIfAbsent(player, key -> new AtomicInteger());
        if (mine.get() >= PER_PLAYER) {
            return false;
        }
        // Claimed then checked, and given back on failure. Two players claiming at once can briefly
        // see the total from either side of the other's increment; handing it back keeps the count
        // honest, and a survey either side of the boundary makes no difference to anything.
        if (RUNNING.incrementAndGet() > TOTAL) {
            RUNNING.decrementAndGet();
            return false;
        }
        mine.incrementAndGet();
        return true;
    }

    /** Gives the room back. Must be called for every {@link #tryStart} that returned true. */
    public static void finish(UUID player) {
        RUNNING.decrementAndGet();
        AtomicInteger mine = PER.get(player);
        if (mine != null) {
            mine.decrementAndGet();
        }
    }

    // ---- is anybody actually waiting on the map right now ---------------------------------------
    //
    // Warming exists to have finished before the race starts, and how hard it may run depends on one
    // question: is somebody watching a map fill in at this moment? There is no need to invent a
    // signal for it — a client only asks for ground while a tablet is in its player's hand, and it
    // only asks at all when it is short of something. A map sitting open and fully drawn asks for
    // nothing, and that is not an oversight: nobody is waiting for anything on that screen either.

    /** When a player last asked for ground. */
    private static volatile long lastRequestAt;

    /**
     * How long after the last request the map still counts as being watched.
     *
     * <p>A client sweeps four times a second while its tablet is open, so anything actively pulling
     * ground cannot look quiet for this long. Long enough to cover a pause between sweeps, short
     * enough that warming gives way within one of its own passes when somebody starts panning.
     */
    private static final long QUIET_MS = 3000L;

    /** Told by the packet handler that somebody is pulling ground. */
    public static void noteRequest() {
        lastRequestAt = System.currentTimeMillis();
    }

    /** Whether somebody is waiting on ground right now, as opposed to merely being logged in. */
    public static boolean anyoneWatching() {
        return System.currentTimeMillis() - lastRequestAt < QUIET_MS;
    }

    /** Warming surveys started since the last report, so the pacing can be seen rather than assumed. */
    private static final AtomicInteger WARMED = new AtomicInteger();

    /**
     * Claims room for one survey nobody has asked for.
     *
     * <p>Warming used to run outside this class entirely — it read a "is there room" answer and then
     * went off and surveyed without ever being counted. So the number this class exists to hold down
     * did not include the only work the server starts on its own initiative, and the check that was
     * meant to keep warming out of a busy server was reading a figure warming never appeared in. On
     * a slow disk, sixteen tiles a second of unaccounted surveying accumulates into a queue nobody
     * can see and nothing will stop.
     *
     * <p>Claimed rather than queued, unlike a player's request. Nobody is waiting on this: if there
     * is no room now there will be room in a moment, and holding on to the work would only mean
     * doing it at the exact time it was decided not to.
     *
     * @return false when the server is busy enough that this can simply be skipped
     */
    public static boolean beginWarming() {
        if (RUNNING.get() >= SPARE_FOR_WARMING) {
            return false;
        }
        if (RUNNING.incrementAndGet() > SPARE_FOR_WARMING) {
            RUNNING.decrementAndGet();
            return false;
        }
        WARMED.incrementAndGet();
        return true;
    }

    /** Gives back the room one warming survey claimed. */
    public static void finishWarming() {
        RUNNING.decrementAndGet();
    }

    /**
     * Forgets a player who has left.
     *
     * <p>Their surveys may still be finishing, and those will decrement a counter that is no longer
     * indexed — which is harmless, and the alternative, waiting for them, would hold a slot open for
     * somebody who has gone.
     */
    /**
     * Forgets the players when the server stops.
     *
     * <p>Lesson 24 again. A single-player client loads the next world in the same process, so
     * without this the next world starts with a queue of requests for ground in the last one, filed
     * under UUIDs that are about to log in again and be handed their own stale backlog.
     *
     * <p>{@code RUNNING} is deliberately left alone. Surveys already handed to a worker will finish
     * and give their slot back on their own, and zeroing it here would mean those returns push the
     * count below zero — a budget that then believes it has more room than the server does.
     */
    @SubscribeEvent
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        PER.clear();
        QUEUED.clear();
        lastRequestAt = 0L;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PER.remove(event.getEntity().getUUID());
        QUEUED.remove(event.getEntity().getUUID());
    }
}
