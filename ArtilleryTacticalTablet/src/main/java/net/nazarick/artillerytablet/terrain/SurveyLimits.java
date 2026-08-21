package net.nazarick.artillerytablet.terrain;

/**
 * Every number that decides how much surveying is in flight, and the relations between them.
 *
 * <p>This class exists because of a counted fault. Six separate faults in one working session were
 * all the same shape: two numbers that had to agree, written by hand in two files that never
 * mentioned each other. A batch size larger than the patch threshold that had just been built to
 * serve it. A client window larger than the server share it was drawn from. A retry timer shorter
 * than the backlog it was meant to outlive. None of them threw; each one showed up as a map that
 * filled wrongly, and each cost a session to find.
 *
 * <p>The cure is not a longer table of numbers to keep in mind. It is that there is only one number
 * to choose at each level and the rest are <em>worked out from it here</em>, so a value that would
 * break a relation cannot be written down at all. Anything in this file may be tuned; nothing
 * outside it may restate one of these values.
 *
 * <p>Common to both sides on purpose. The client's window and the server's share are two ends of one
 * agreement, and the moment they live in different files they start to drift.
 */
public final class SurveyLimits {
    private SurveyLimits() {
    }

    /**
     * Surveys the server will run at once, for everybody together.
     *
     * <p>The one number that decides what this map costs a host. Each survey is up to sixteen chunk
     * reads. Deliberately flat rather than scaled by player count: a busier server should serve each
     * player more slowly, not work harder.
     */
    public static final int TOTAL = 256;

    /**
     * Of that, the most any one player may have running.
     *
     * <p>A different job from the total. The total protects the server; this protects the other
     * players from the one who has just opened a map of somewhere nobody has ever been.
     */
    public static final int PER_PLAYER = 96;

    /**
     * Tiles one request packet carries.
     *
     * <p>Both ends read this, and that is the whole point of its being here. It was written twice —
     * thirty-two on the wire, sixty-four in the client's batch — and the client's own bookkeeping
     * believed the larger one. So every sweep marked sixty-four tiles as asked about, sent
     * thirty-two, and left the other thirty-two occupying the window, unasked and unanswerable,
     * until the retry timer released them half a minute later. Half the map's request capacity spent
     * on tiles nobody had been told about.
     */
    public static final int TILES_PER_REQUEST = 32;

    /**
     * Requests' worth of backlog a client may keep waiting at the server, over what it is running.
     *
     * <p>This is the only reason the client's window is allowed to be wider than the server's share
     * at all: the server <em>keeps</em> what it has no room to start. Deeper than this is not
     * throughput, it is latency — a tile answered long after the view moved off it.
     */
    private static final int QUEUED_REQUESTS = 2;

    /**
     * How many tiles one client may have outstanding.
     *
     * <p>Worked out rather than chosen, and the arithmetic is the rule: what the server will run for
     * this player, plus the backlog we are prepared to have it hold. Written by hand it was 192
     * against a share of 96, which was safe only by accident — the server had been changed to queue
     * instead of refuse a few hours earlier. Before that change the same pair of numbers filled the
     * map in a ring speckled with holes.
     */
    public static final int MAX_IN_FLIGHT = PER_PLAYER + QUEUED_REQUESTS * TILES_PER_REQUEST;

    /**
     * The slowest the ground has ever been measured arriving, in tiles a second.
     *
     * <p>Cold country, read off the disk, nothing cached. The fast case is thirty times this and is
     * of no interest here: a timer sized for the fast case fires during the slow one.
     */
    private static final int SLOWEST_TILES_PER_SECOND = 2;

    /**
     * How long an unanswered tile waits before it is asked about again.
     *
     * <p>Long enough for a full window to drain at the worst rate ever seen, because anything
     * shorter asks again for tiles that are sitting perfectly safely in the server's queue — which
     * is the same disk read done twice, which makes the slow case slower, which fires the timer
     * sooner. Five seconds, once, against a backlog that could run to a hundred: a loop of one's own
     * making.
     *
     * <p>It has almost nothing left to cover. A request the server accepts is answered, and the
     * queue it waits in cannot overflow while the client honours {@link #MAX_IN_FLIGHT}. What
     * remains is a reply that never arrived at all, over a protocol that is TCP underneath.
     */
    public static final long RETRY_AFTER_MS = 1000L * MAX_IN_FLIGHT / SLOWEST_TILES_PER_SECOND;

    /**
     * How many requests the server will hold for one player.
     *
     * <p>Exactly what a client obeying the window above can produce, so overflow no longer means
     * "the server is behind" — it means a client that is not following the protocol, and dropping
     * its oldest request is the right answer to that.
     */
    public static final int MAX_QUEUED_PER_PLAYER = MAX_IN_FLIGHT;

    /**
     * Below this much running work, surveying nobody has asked for is allowed to start.
     *
     * <p>Warming is work no one is waiting on, so it only ever gets what is spare. Sharing a queue
     * with a player watching a map fill would be precisely backwards.
     */
    public static final int SPARE_FOR_WARMING = TOTAL / 2;

    static {
        // Stated as checks rather than as prose, because prose is what the six faults all had.
        if (PER_PLAYER > TOTAL) {
            throw new IllegalStateException("a player's share cannot exceed the server's whole budget");
        }
        if (MAX_QUEUED_PER_PLAYER < MAX_IN_FLIGHT - PER_PLAYER) {
            throw new IllegalStateException("the server would drop requests a well-behaved client sent");
        }
        if (SPARE_FOR_WARMING > TOTAL) {
            throw new IllegalStateException("warming would be allowed to fill the whole budget");
        }
    }
}
