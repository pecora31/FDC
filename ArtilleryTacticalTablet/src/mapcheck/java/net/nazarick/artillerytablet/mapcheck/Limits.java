package net.nazarick.artillerytablet.mapcheck;

import net.nazarick.artillerytablet.terrain.SurveyLimits;

import java.lang.reflect.Field;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;

/**
 * Checks that the numbers governing how much ground is asked for still agree with each other.
 *
 * <p>This is the only check here that is not about pixels, and it earns its place by count. Six
 * faults in one working session were the same fault: two numbers that had to stand in some relation,
 * written by hand in two files, and nobody to notice when one of them moved. None of them threw.
 * Each showed up as a map that filled wrongly and cost a session to find. A seventh was still in the
 * code when this was written — a request batch twice the size of the packet that carried it, so half
 * of every sweep was marked as asked-about and never sent.
 *
 * <p>The relations now hold by construction: every one of these values is worked out in
 * {@link SurveyLimits}. That is the real fix, and this is what keeps it. The next person to want a
 * bigger batch will reach for the number where it is used, because that is where the effect is, and
 * this is what tells them the relation they have just broken — at build time, in a sentence, rather
 * than as a map that fills at half speed for reasons that take a session to name.
 */
final class Limits {
    private Limits() {
    }

    static void run() throws Exception {
        Class<?> clientCache = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainClientCache");
        Class<?> request = Class.forName("net.nazarick.artillerytablet.network.RequestTerrainTilesMessage");
        Class<?> budget = Class.forName("net.nazarick.artillerytablet.terrain.ServerSurveyBudget");

        int batch = MapCheck.readInt(clientCache, "REQUEST_BATCH");
        int inFlight = MapCheck.readInt(clientCache, "MAX_IN_FLIGHT");
        long retryMs = readLong(clientCache, "RETRY_AFTER_MS");
        int scan = MapCheck.readInt(clientCache, "SCAN_TILES");
        int remembered = MapCheck.readInt(clientCache, "MAX_REMEMBERED");

        int perPacket = MapCheck.readInt(request, "MAX_TILES_PER_REQUEST");
        int perPlayer = MapCheck.readInt(budget, "PER_PLAYER");
        int total = MapCheck.readInt(budget, "TOTAL");
        int queued = MapCheck.readInt(budget, "MAX_QUEUED_PER_PLAYER");
        int warming = MapCheck.readInt(budget, "SPARE_FOR_WARMING");

        // A tile the client believes it asked about must actually have left the machine. This is the
        // one that was broken: 64 against 32, so half of every sweep sat in the window unasked until
        // the retry timer released it.
        check(batch == perPacket, "the client's batch is " + batch
                + " but a request packet carries " + perPacket + " — the difference is marked as "
                + "asked about and never sent");

        // The client's window may exceed the server's share only because the server keeps what it
        // cannot start. The overflow has to fit in the queue that keeps it.
        check(inFlight >= perPlayer, "the client's window (" + inFlight + ") is below the server's "
                + "share (" + perPlayer + "), so the server can never be kept busy");
        check(queued >= inFlight - perPlayer, "a client filling its window (" + inFlight + ") would "
                + "overflow the server's queue (" + queued + " past " + perPlayer + " running) and "
                + "have requests dropped it will not ask about again for "
                + (retryMs / 1000) + "s");

        check(perPlayer <= total, "one player's share (" + perPlayer + ") exceeds the whole server's "
                + "budget (" + total + ")");
        check(warming <= total, "warming alone could fill the server's budget");

        // The retry timer exists for a reply that never came, not for one that is queued. It has to
        // outlast a full window draining at the worst rate the survey has ever been measured at.
        int slowestTilesPerSecond = 2;
        long drainMs = 1000L * inFlight / slowestTilesPerSecond;
        check(retryMs >= drainMs, "the retry timer (" + retryMs + "ms) is shorter than a full window "
                + "takes to drain when the server is slow (" + drainMs + "ms), so it will ask again "
                + "for tiles that are sitting safely in the queue");

        // ---- warming, which is the one rate the server sets for itself ---------------------------
        Class<?> warmer = Class.forName("net.nazarick.artillerytablet.terrain.ServerTerrainWarmer");
        int busy = MapCheck.readInt(warmer, "PER_PASS_BUSY");
        int quiet = MapCheck.readInt(warmer, "PER_PASS_QUIET");
        int perSecond = MapCheck.readInt(warmer, "PASSES_PER_SECOND");

        check(quiet >= busy, "warming is set to run FASTER while somebody is watching a map fill in ("
                + busy + " a pass) than while nobody is (" + quiet + ") — that is the point of it "
                + "exactly inverted");

        // Feeding faster than the budget accepts is not a bigger rate, it is a loop spinning against
        // a refusal. Feeding slower leaves the capacity the budget was sized for unused, which is the
        // fault this pacing was written to fix.
        int quietPerSecond = quiet * perSecond;
        check(quietPerSecond >= warming / 4, "the quiet warming feed (" + quietPerSecond + "/s) "
                + "cannot fill the budget set aside for it (" + warming + " at once), so most of "
                + "what the server reserved for warming can never be used");
        check(quiet <= warming, "one pass would try to start more warming surveys (" + quiet + ") "
                + "than the budget allows at all (" + warming + ")");

        // And the client must be able to hold what it is willing to go and fetch.
        long scanned = (long) (scan + 1) * (scan + 1);
        check(remembered > scanned, "the client fetches up to " + scanned + " tiles for one wide "
                + "view but only remembers " + remembered + ", so a single view would evict itself");

        System.out.printf("limits: OK  batch %d = packet %d | window %d vs share %d, queue %d"
                        + " | retry %ds covers %ds | remembers %d for a view of %d"
                        + " | warming %d/s quiet vs %d/s busy, budget %d%n",
                batch, perPacket, inFlight, perPlayer, queued,
                retryMs / 1000, drainMs / 1000, remembered, scanned,
                quietPerSecond, busy * perSecond, warming);
    }

    private static long readLong(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.getLong(null);
    }
}
