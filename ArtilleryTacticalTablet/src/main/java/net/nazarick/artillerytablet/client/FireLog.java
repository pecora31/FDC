package net.nazarick.artillerytablet.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Running record of fire missions, the tablet's equivalent of a gun log.
 *
 * <p>Kept in memory for the session rather than written into the item: a log that grows without
 * bound inside NBT would be synced to the client on every change, and losing it on relog is a far
 * smaller cost than that.
 */
@OnlyIn(Dist.CLIENT)
public final class FireLog {
    private static final int MAX_ENTRIES = 32;

    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();

    private FireLog() {
    }

    public static void record(String text) {
        ENTRIES.addFirst(new Entry(text, System.currentTimeMillis()));
        while (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.removeLast();
        }
    }

    /** Newest first. */
    public static List<Entry> entries() {
        return new ArrayList<>(ENTRIES);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    public record Entry(String text, long timestamp) {
    }
}
