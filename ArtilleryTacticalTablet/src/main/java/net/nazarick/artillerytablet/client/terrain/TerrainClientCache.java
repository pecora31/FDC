package net.nazarick.artillerytablet.client.terrain;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.nazarick.artillerytablet.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;
import net.nazarick.artillerytablet.network.RequestTerrainTilesMessage;
import net.nazarick.artillerytablet.terrain.SurveyLimits;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The client's copy of the terrain the server has told it about.
 *
 * <p>Nothing is drawn from it directly — {@link TerrainImage} turns it into pixels. This only tracks
 * three states per tile, which is the whole trick to never asking twice: <em>held</em>, <em>asked
 * for and still waiting</em>, and <em>the server says there is nothing there</em>. The last one is
 * as final an answer as the first; treating it as a failure would put the map in a request loop
 * over exactly the ground that costs the server the most to look at.
 */
@OnlyIn(Dist.CLIENT)
public final class TerrainClientCache {
    // ---- how much may be asked for, and how often --------------------------------------------
    //
    // Not written here. Every one of these numbers has a partner on the server, and the six faults
    // of 2026-08-16 were all two partners disagreeing — so they live together in SurveyLimits, where
    // the relations between them are arithmetic rather than a comment asking the reader to remember.
    //
    // The one that had already gone wrong again by the time this was written: the batch was 64 while
    // the packet carried 32, so every sweep marked twice as many tiles asked-about as it actually
    // asked about, and the phantom half sat in the window until the retry timer let it go.

    /** Tiles asked for in one go — exactly what one packet carries, so none can be lost in between. */
    private static final int REQUEST_BATCH = SurveyLimits.TILES_PER_REQUEST;

    /** How long before a tile that never came back is asked for again. */
    private static final long RETRY_AFTER_MS = SurveyLimits.RETRY_AFTER_MS;

    /** How many requests may be with the server at once. */
    private static final int MAX_IN_FLIGHT = SurveyLimits.MAX_IN_FLIGHT;

    /**
     * How long an answer stands before it is worth asking again.
     *
     * <p>No answer about the ground is permanent, and treating one as permanent was a real bug: a
     * tile reported empty because that world had not been generated yet stayed empty in the cache
     * for good, so flying out to that very ground left the map blank exactly where the player had
     * just arrived. The same staleness applies to ground we do hold — a tile fetched while it was
     * half generated would otherwise stay half forever, and a crater put there by our own guns would
     * never appear.
     */
    private static final long REFRESH_AFTER_MS = 10_000L;

    /**
     * How often re-checking old answers may spend request budget. Fetching ground we have never seen
     * always comes first; refreshing is what the leftovers go on, and only now and then, so a wide
     * view cannot turn into a permanent stream of re-reads.
     */
    private static final long REFRESH_SWEEP_MS = 5000L;

    /**
     * Held ground, keyed by packed tile coordinate.
     *
     * <p>A primitive-keyed map rather than {@code Map<Long, …>}. Drawing reads this on the order of
     * a hundred thousand times per coarse sheet — every sample of every averaged pixel — and a boxed
     * key allocates a {@code Long} on each of those lookups for nothing.
     */
    private static final Long2ObjectOpenHashMap<TerrainTile> TILES = new Long2ObjectOpenHashMap<>();
    private static final Map<Long, Long> ANSWERED = new HashMap<>();
    private static final Map<Long, Long> ASKED = new HashMap<>();

    /**
     * Guards {@link #TILES} and {@link #MIPS} against concurrent access from the background bake
     * threads introduced for off-thread rendering.
     *
     * <p>The render thread writes to these maps (via {@link #accept}, {@link #checkWorld},
     * {@link #forgetGroundFarFromView}), and background threads read them (via {@link #tileAt},
     * {@link #mipsAt}). The hash map implementation is not thread-safe for concurrent read+write,
     * so every path through either map goes through this lock.
     *
     * <p>The cost is negligible: {@code tileAt} is called a few dozen times per sheet build — once
     * per tile boundary crossed — and the lock is uncontested most of the time because writes are
     * infrequent (a batch of tiles arriving, or a world change).
     */
    static final Object TILE_LOCK = new Object();

    /** Tiles under a firing line, wanted regardless of where the map is pointed. */
    private static final Set<Long> LINE_WANTED = new LinkedHashSet<>();

    /**
     * Which world this map is of — the world itself, and the dimension inside it.
     *
     * <p>It was the dimension alone, and that is the fault under every "the old world's ground is
     * still on the map" report there has been. Leaving one single-player world for another,
     * {@code minecraft:overworld} equals {@code minecraft:overworld}, so the check said nothing had
     * changed and cleared nothing: not the tiles, not the reduced copies, not the store this points
     * at, and — because it never bumped the generation — none of the guards built downstream of it
     * either. The bake's world stamp, the store's epoch, the pictures on the card, the tablet's boot
     * screen: all of them wait on this one comparison, and it was answering the wrong question.
     *
     * <p>Paired with the store's own idea of which world it is in, deliberately: the two must agree
     * about the world or one will be reading a folder the other has moved on from.
     */
    private static String world;

    private static long lastRefreshSweep;

    /** The world and dimension the client is in, or null when it is in none. */
    private static String worldIdentity(Minecraft mc) {
        if (mc.level == null) {
            return null;
        }
        return TerrainDisk.worldName(mc) + "|" + mc.level.dimension().location();
    }

    /** Bumped whenever new ground arrives, so the map knows to redraw without polling. */
    private static int version;

    // ---- what changed, and when ------------------------------------------------------------------
    //
    // A version number alone says only THAT something moved, so a square holding a stale version had
    // no choice but to redraw all of itself. This says WHICH tile moved, so it can redraw the part of
    // itself that stands on it — the shape the map mod this was measured against uses, where a chunk
    // landing overwrites its own sixteen pixels of a region image and nothing else is touched.
    //
    // A ring, because the only question ever asked of it is "what has happened since", and history
    // older than the oldest square on screen is of no use to anyone. When a caller has fallen further
    // behind than the ring reaches, it is told so and redraws wholesale, which is exactly what it did
    // before this existed.

    private static final int CHANGE_LOG = 4096;
    private static final long[] CHANGED_KEY = new long[CHANGE_LOG];
    private static final int[] CHANGED_VERSION = new int[CHANGE_LOG];

    /** Next slot to write, and how many have ever been written. */
    private static int changeAt;
    private static long changeCount;

    private static void logChange(long key) {
        CHANGED_KEY[changeAt] = key;
        CHANGED_VERSION[changeAt] = version;
        changeAt = (changeAt + 1) % CHANGE_LOG;
        changeCount++;
    }

    /**
     * Collects the tiles that changed after a given version, newest first.
     *
     * @return false when the log no longer reaches back that far, so the caller must redraw the lot
     */
    public static boolean changedSince(int sinceVersion, LongArrayList out) {
        out.clear();
        int held = (int) Math.min(changeCount, CHANGE_LOG);
        for (int back = 1; back <= held; back++) {
            int slot = Math.floorMod(changeAt - back, CHANGE_LOG);
            if (CHANGED_VERSION[slot] <= sinceVersion) {
                return true;
            }
            out.add(CHANGED_KEY[slot]);
        }
        // Walked the whole ring without reaching that version. Either nothing older was ever logged,
        // in which case what was collected is the complete history and the answer stands, or the ring
        // has wrapped and the rest is gone.
        return changeCount <= CHANGE_LOG;
    }

    private TerrainClientCache() {
    }

    /** Bumped when the world changes, so anything holding rendered tiles knows to discard them. */
    private static int generation;

    /**
     * Throws away every colour derived from held ground, keeping the ground itself.
     *
     * <p>For a change in how a column is coloured rather than a change in what is under it — the
     * night filter, which is a different palette and not a different world. The tiles are the
     * survey and stay; the reduced copies have colour baked into them and cannot, and the pictures
     * built from those rebuild through the ordinary staleness path once the version moves.
     *
     * <p>Deliberately expensive and deliberately rare. A repaint costs what a viewful of squares
     * costs to bake, which is a second or two on a key nobody presses twice in a fight — and the
     * alternative, colouring at the moment of drawing, would need a second place that decides what
     * a column looks like. There is one, and it is not the draw loop.
     */
    public static void repaint() {
        synchronized (TILE_LOCK) {
            MIPS.clear();
        }
        paint++;
        version++;
    }

    /**
     * Which palette the held colours were computed under.
     *
     * <p>Bumping the version alone did not work, and the way it failed is worth keeping: a square
     * whose ground had not moved took the shortcut that says "nothing changed here, so this is as
     * current as it ever was", stamped the new version onto pixels painted under the old palette,
     * and was never rebuilt. The shortcut is right — it is what stops one tile landing from
     * dirtying every square on screen — and it is right because it answers a question about
     * <em>ground</em>. A palette is not a change of ground, so no amount of ground-change signalling
     * will carry it.
     */
    private static int paint;

    /**
     * The two things a picture can be out of date with respect to, as one value.
     *
     * <p>One number rather than two checked side by side, because the whole family of faults in
     * this layer has been some piece of work carrying one stamp and being tested against another.
     * A single stamp cannot be half-checked.
     */
    public static long stamp() {
        return ((long) generation << 32) | (paint & 0xFFFFFFFFL);
    }

    public static int generation() {
        return generation;
    }

    public static int version() {
        return version;
    }

    /**
     * Whether ground has been asked for and not yet answered, by the server or by the store.
     *
     * <p>Read by the screen so it can say it is working. Deliberately not "has nothing to draw":
     * a map over country that has never been generated has nothing to draw and never will, and
     * telling the player to keep waiting for it would be a lie the whole cache is careful not to
     * tell elsewhere.
     */
    public static boolean isWaiting() {
        return !ASKED.isEmpty() || !ASKED_DISK.isEmpty();
    }

    /** Tiles taken in since the client started, for measuring how fast ground actually arrives. */
    private static long arrived;

    /** How many of those the client built itself, which is the cheap half of the same number. */
    private static long builtLocally;

    public static long builtLocally() {
        return builtLocally;
    }

    public static long arrived() {
        return arrived;
    }

    /** Tiles asked for and not yet answered, by the server or by the store. */
    public static int outstanding() {
        return ASKED.size() + ASKED_DISK.size();
    }

    /**
     * Fingerprint of each held tile.
     *
     * <p>Kept alongside rather than worked out when a request is assembled: the hash walks twelve
     * thousand values, and a sweep considers dozens of tiles at a time.
     */
    private static final Long2IntOpenHashMap HASHES = new Long2IntOpenHashMap();

    /**
     * Tiles whose every column is known, and which are therefore never asked about again.
     *
     * <p>Refreshing existed so a shell crater would appear on ground already surveyed. That was
     * dropped deliberately: no fire-direction map updates itself with battle damage — an observer
     * reports it — and the polling it required was the single largest cost in this whole layer.
     *
     * <p>What refreshing must still do is catch ground that was not there when it was asked about.
     * A player generates new chunks by walking to them, so an <em>empty</em> or <em>partial</em>
     * answer has a shelf life. A complete one does not.
     */
    private static final Set<Long> COMPLETE = new HashSet<>();

    /** What the client holds for a tile, for the server to compare against. Zero when it holds none. */
    private static int heldHash(long key) {
        // Absent reads as zero, which is what "the client holds nothing" has always meant here.
        return HASHES.get(key);
    }

    public static void confirm(int tileX, int tileZ) {
        long key = TerrainTile.key(tileX, tileZ);
        ASKED.remove(key);
        ANSWERED.put(key, System.currentTimeMillis());

        // The server has looked and agrees with what we hold. Only now is a complete tile allowed
        // to stop being asked about — see accept, where a tile out of our own store deliberately
        // does not earn that on its own word.
        TerrainTile held = TILES.get(key);
        if (held != null && held.isComplete()) {
            COMPLETE.add(key);
        }
    }

    /**
     * The reduced-by-halves copy of each held tile.
     *
     * <p>Built once, when the tile lands, because that is the only moment its contents change. A
     * wide view then reads a level of this instead of sampling the full-resolution data at a stride,
     * which is both cheaper and — the point — an average of every block rather than a few of them.
     */
    private static final Long2ObjectOpenHashMap<TerrainMips> MIPS = new Long2ObjectOpenHashMap<>();

    /**
     * The reduced copy of a tile, built the first time a wide view asks for it.
     *
     * <p>It used to be built the moment a tile landed, on the reasoning that arrival is the only
     * time its contents change. True, and the wrong moment: tiles land in batches of thirty-two, on
     * the render thread, inside the packet handler — so a batch was thirty-two reduction chains in
     * one frame, and every one of them was work the close zooms never read, because the finest level
     * draws from the tile itself.
     *
     * <p>Built here instead, it is paced by the drawing: a wide view reduces the tiles it actually
     * reads, spread across the frames its own build budget already spreads them over, and a player
     * who never zooms out never pays at all.
     */
    public static TerrainMips mipsAt(int tileX, int tileZ) {
        long key = TerrainTile.key(tileX, tileZ);

        TerrainTile tile;
        synchronized (TILE_LOCK) {
            TerrainMips held = MIPS.get(key);
            if (held != null) {
                return held;
            }
            tile = TILES.get(key);
            if (tile == null) {
                return null;
            }
        }

        // Reduced OUTSIDE the lock, and that is the point of the shape rather than an oversight.
        // Building the chain walks the whole tile and every colour in it; done under the lock, a
        // background bake reducing one tile held up the render thread taking in the next — the one
        // lock in this class would have become the thing the map waited on, at exactly the moments
        // ground was arriving fastest.
        //
        // Safe because a tile is never modified after it is accepted: accept() replaces the object
        // rather than writing into it, so the arrays this reads cannot change under it.
        TerrainMips built = TerrainMips.of(tile);

        synchronized (TILE_LOCK) {
            // Another thread may have reduced the same tile while this one was working, and a tile
            // that has been resurveyed in the meantime must not be overwritten with a reduction of
            // the version it replaced.
            TerrainMips raced = MIPS.get(key);
            if (raced != null) {
                return raced;
            }
            if (TILES.get(key) != tile) {
                // Resurveyed or let go while this was reducing. The work is dropped rather than
                // filed under a key that now means different ground.
                return null;
            }
            MIPS.put(key, built);
            return built;
        }
    }

    public static void accept(TerrainTile tile, boolean empty) {
        long key = TerrainTile.key(tile.tileX, tile.tileZ);
        ASKED.remove(key);
        ANSWERED.put(key, System.currentTimeMillis());
        if (!empty) {
            warmLookups(tile);
        }
        synchronized (TILE_LOCK) {
            if (empty) {
                TileArrayPool.release(TILES.remove(key));
                MIPS.remove(key);
                HASHES.remove(key);
                COMPLETE.remove(key);
            } else {
                // The arrays that arrived off the network/disk decode are a one-off allocation
                // either way (that decode path is shared with the server); the copy that actually
                // sits in TILES for a while is built from pooled storage, and whatever tile this
                // one replaces gives its arrays back to the same pool.
                TileArrayPool.release(TILES.put(key, TileArrayPool.pooledCopy(tile)));
                // Not reduced here — see mipsAt. Dropped rather than replaced, so a tile that has been
                // resurveyed is reduced again the next time a wide view reads it and not before.
                MIPS.remove(key);
                HASHES.put(key, tile.contentHash());
                // A tile from the server is the world's own word and is final. A tile from our own store
                // is only a memory of what the world said last time, and it must not be treated as final
                // — that is what made a building never appear on the map: the store handed back a tile
                // from before it was built, the tile was complete, complete tiles are never asked about
                // again, and so the map froze at whatever it had been told once.
                //
                // Left unconfirmed, the ordinary refresh sweep asks about it, carrying the fingerprint we
                // hold. Where nothing has changed the server answers in a few bytes and confirm() marks
                // it settled; where something has, the new ground arrives. One cheap round trip per
                // stored tile per session, against a map that could never notice the world moving on.
                if (tile.isComplete() && !fromDisk) {
                    COMPLETE.add(key);
                } else {
                    COMPLETE.remove(key);
                }
            }
        }
        version++;
        arrived++;
        logChange(key);
        if (fromDisk) {
            // Due for confirmation at the next sweep rather than in ten seconds. It costs the server
            // a fingerprint comparison, and until it answers the map is drawing remembered ground.
            ANSWERED.put(key, System.currentTimeMillis() - REFRESH_AFTER_MS);
        }
        if (!empty && !fromDisk) {
            // Only ground the server just told us about. A tile read back from the store is already
            // in it, and writing it again would be a file rewritten for every square that draws it.
            TerrainDisk.save(tile);
        }
    }

    /**
     * Looks up everything this tile's ground will need to be drawn, here, on the render thread.
     *
     * <p>The one place that makes the background bake safe, and it is worth saying plainly why it is
     * here rather than there. Drawing a column needs two things the game only tells the client:
     * what a block looks like from above ({@link BlockPalette}, which reads baked models and the
     * texture atlas) and what a biome tints it to ({@link TerrainMips}, which reads {@code
     * mc.level}). Both belong to the render thread — a resource reload swaps the models and closes
     * every sprite, and a change of world replaces the level — so a bake thread reaching for either
     * one is reading something that may be freed underneath it.
     *
     * <p>Asking here instead means the tables are complete before any bake can want them, so the
     * bake threads see this whole area as read-only. A tile mentions a handful of distinct blocks
     * and biomes across four thousand columns, and a repeat costs one array read, so the loop is
     * far cheaper than it looks — and it happens on tile arrival, a few dozen times a second at
     * most, not per frame.
     */
    private static void warmLookups(TerrainTile tile) {
        for (int i = 0; i < TerrainTile.COLUMNS; i++) {
            BlockPalette.prewarm(tile.blockAt(i));
            TerrainMips.prewarmBiome(tile.biome[i]);
        }
    }

    /** True while a tile read back from the store is being taken in, so it is not written again. */
    private static boolean fromDisk;

    /**
     * Drops everything on a change of world or dimension. Tile coordinates mean nothing across that
     * boundary, and keeping them would paint one world's ground onto another's.
     */
    public static void checkWorld() {
        Minecraft mc = Minecraft.getInstance();
        String now = worldIdentity(mc);
        if (now == null ? world == null : now.equals(world)) {
            return;
        }
        world = now;
        ResourceLocation dimension = mc.level == null ? null : mc.level.dimension().location();
        TerrainDisk.clear();
        ASKED_DISK.clear();
        TerrainDisk.openFor(dimension);
        synchronized (TILE_LOCK) {
            TILES.clear();
            MIPS.clear();
            HASHES.clear();
            COMPLETE.clear();
        }
        // A biome id means something else in the next world, and the reduced tiles have those
        // colours baked into them — so the memo behind them has to go with the data it produced.
        TerrainMips.forgetBiomes();
        // Block ids are handed out by the server at login, so they mean something else in the next
        // world for exactly the same reason. Keeping this would colour one world's ground with
        // another world's blocks.
        BlockPalette.forget();
        ANSWERED.clear();
        ASKED.clear();
        LINE_WANTED.clear();
        LOCAL_CHECKED.clear();
        version++;
        // The log describes ground in a world we have left. Anything still holding an old version is
        // told to redraw the lot, which is right: none of its pixels mean anything here.
        changeAt = 0;
        changeCount = 0;

        // Pictures already on the graphics card are not held here, and clearing the data alone left
        // them on screen: patches of the previous world drawn over the new one. Anything holding
        // rendered tiles watches this and throws them away.
        generation++;
    }

    /**
     * Asks the server for any tile covering the area that is neither held nor already on its way.
     * Safe to call every frame — it only ever emits one batch, and only for genuinely missing tiles.
     */
    /**
     * Widest sweep, in tiles per axis, however much ground is on screen.
     *
     * <p>Sixty-four tiles is four thousand blocks across, which is a whole view at the widest zoom
     * that still draws ground. It was forty-eight, and the difference showed as a map that stopped
     * three quarters of the way to the edge of the panel however long you waited — not slow, but
     * never going to arrive.
     */
    private static final int SCAN_TILES = 64;

    /**
     * How many tiles are being looked for on disk at once.
     *
     * <p>A cap rather than a queue of everything in sight, because a tile the store does not have
     * costs a wasted round trip before the server is asked — so a wide view over unexplored country
     * should not spend its whole sweep discovering that.
     */
    private static final int DISK_IN_FLIGHT = 64;

    /** Tiles asked of the store and not yet answered. */
    private static final Set<Long> ASKED_DISK = new HashSet<>();

    /**
     * Tiles built from the client's own chunks in one sweep.
     *
     * <p>Sampling sixteen chunks is a few hundred microseconds of work on the render thread, so this
     * is paced rather than let loose — but paced generously, because it is the cheapest ground there
     * is: no disk, no network, and it takes that tile off the server's queue for good.
     */
    private static final int LOCAL_PER_SWEEP = 24;

    /**
     * How often ground the client can see is looked at again.
     *
     * <p>The map's rule is that a tile with every column known is never asked about again, and that
     * rule is right for country nobody is standing in — it is what stopped the map polling the
     * server forever. It is wrong for the ground under your feet: a wall built while the tablet was
     * open never appeared, and only showed up after a restart, when the tile came back off the store
     * unconfirmed and got asked about once more.
     *
     * <p>Nothing needs asking, though. Within render distance the client holds the chunks itself, so
     * it can simply look again — no server, no disk, no request. Two seconds is far below what a
     * fire mission takes to lay and far above what the sampling costs.
     */
    private static final long LOCAL_REFRESH_MS = 2000L;

    /** When each nearby tile was last looked at again, so this is a trickle rather than a sweep. */
    private static final Map<Long, Long> LOCAL_CHECKED = new HashMap<>();

    /**
     * Takes in whatever the store has read back since the last frame.
     *
     * <p>On the render thread, deliberately. Everything downstream of a tile arriving — the
     * reduction chain, the pictures built from it — belongs to that thread, and the store's own
     * thread must not reach into any of it.
     */
    private static void collectFromDisk() {
        TerrainTile tile;
        while ((tile = TerrainDisk.collect()) != null) {
            long key = TerrainTile.key(tile.tileX, tile.tileZ);
            ASKED_DISK.remove(key);
            fromDisk = true;
            try {
                accept(tile, false);
            } finally {
                fromDisk = false;
            }
        }

        Long absent;
        while ((absent = TerrainDisk.missing()) != null) {
            // Nothing stored. Forgotten rather than remembered as absent: the ordinary sweep will
            // now ask the server, and if that answers, the tile lands in the store on its way past.
            ASKED_DISK.remove(absent);
        }
    }

    /**
     * How many tiles the client keeps in memory before it starts letting go of the far ones.
     *
     * <p>Every other store in this map has a ceiling — textures, the server's surveys, the server's
     * queue — and this one had none at all. Nothing was ever dropped short of changing world, so a
     * session spent flying across a world grew this without limit, at some forty kilobytes a tile
     * once the reduced copies are counted. That is not a slow leak; it is a few hundred megabytes an
     * hour for a player who travels.
     *
     * <p>Set above the widest legitimate working set rather than near it. A scan window is 65 tiles
     * a side, so a view at the widest zoom that still fetches ground accounts for about four
     * thousand; this is twice that, which means letting go only ever happens to country the player
     * has genuinely left behind.
     */
    private static final int MAX_REMEMBERED = 8192;

    /**
     * Drops ground far outside the view once too much is being remembered.
     *
     * <p>Cheap to be wrong about, which is what makes it safe: everything dropped here was written
     * to the client's own store on its way in, so returning to that country reads it back off the
     * disk in a frame or two rather than asking the server for it again.
     *
     * <p>Deliberately silent about it — no version bump, nothing written to the change log. A sheet
     * already drawn from a tile that has just been let go must not be told its ground moved, because
     * nothing has: telling it so would rebuild it from data that is no longer there, which is how a
     * cache that is trying to save memory ends up painting holes.
     */
    private static void forgetGroundFarFromView(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        if (ANSWERED.size() <= MAX_REMEMBERED) {
            return;
        }

        // Kept: everything on screen, plus half a view again on each side, and never less than a
        // whole scan window. Panning must not walk into ground that was let go a moment ago.
        int firstX = TerrainTile.blockToTile(minBlockX);
        int lastX = TerrainTile.blockToTile(maxBlockX);
        int firstZ = TerrainTile.blockToTile(minBlockZ);
        int lastZ = TerrainTile.blockToTile(maxBlockZ);
        int marginX = Math.max(SCAN_TILES, (lastX - firstX + 1) / 2);
        int marginZ = Math.max(SCAN_TILES, (lastZ - firstZ + 1) / 2);
        firstX -= marginX;
        lastX += marginX;
        firstZ -= marginZ;
        lastZ += marginZ;

        List<Long> gone = new ArrayList<>();
        for (Long boxed : ANSWERED.keySet()) {
            long key = boxed;
            int tileX = (int) (key >> 32);
            int tileZ = (int) key;
            if (tileX >= firstX && tileX <= lastX && tileZ >= firstZ && tileZ <= lastZ) {
                continue;
            }
            if (ASKED.containsKey(key)) {
                // A reply is on its way. Forgetting it now would only mean asking for it twice.
                continue;
            }
            gone.add(key);
        }

        synchronized (TILE_LOCK) {
            for (long key : gone) {
                TileArrayPool.release(TILES.remove(key));
                MIPS.remove(key);
                HASHES.remove(key);
                COMPLETE.remove(key);
                ANSWERED.remove(key);
                LOCAL_CHECKED.remove(key);
            }
        }
    }

    public static void ensureCovered(int minBlockX, int minBlockZ, int maxBlockX, int maxBlockZ) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }

        collectFromDisk();
        int localLeft = LOCAL_PER_SWEEP;

        boolean mainHand = mc.player.getMainHandItem().getItem() instanceof ArtilleryTacticalTabletItem;
        if (!mainHand && !(mc.player.getOffhandItem().getItem() instanceof ArtilleryTacticalTabletItem)) {
            return;
        }

        forgetGroundFarFromView(minBlockX, minBlockZ, maxBlockX, maxBlockZ);

        long now = System.currentTimeMillis();

        // The window is capped rather than taken whole. A sixteen-kilometre view holds sixty-five
        // thousand tiles; walking them all — allocating and sorting as it goes — cost far more than
        // it could ever buy, because only a few dozen are requested per sweep anyway and the server
        // is in no position to hand over the rest. Past this width the map shows what it has.
        int midX = TerrainTile.blockToTile((minBlockX + maxBlockX) / 2);
        int midZ = TerrainTile.blockToTile((minBlockZ + maxBlockZ) / 2);
        int reach = SCAN_TILES / 2;
        int firstX = Math.max(TerrainTile.blockToTile(minBlockX), midX - reach);
        int lastX = Math.min(TerrainTile.blockToTile(maxBlockX), midX + reach);
        int firstZ = Math.max(TerrainTile.blockToTile(minBlockZ), midZ - reach);
        int lastZ = Math.min(TerrainTile.blockToTile(maxBlockZ), midZ + reach);

        // Middle of the view, in tiles, so the eye gets ground where it is actually looking first.
        double centreX = (firstX + lastX) / 2.0;
        double centreZ = (firstZ + lastZ) / 2.0;

        boolean mayRefresh = now - lastRefreshSweep >= REFRESH_SWEEP_MS;

        List<long[]> unseen = new ArrayList<>();
        List<long[]> stale = new ArrayList<>();

        for (int tileX = firstX; tileX <= lastX; tileX++) {
            for (int tileZ = firstZ; tileZ <= lastZ; tileZ++) {
                long key = TerrainTile.key(tileX, tileZ);

                Long asked = ASKED.get(key);
                if (asked != null && now - asked < RETRY_AFTER_MS) {
                    continue;
                }

                Long answered = ANSWERED.get(key);
                if (answered != null && localLeft > 0 && dueForLocalCheck(key, now)) {
                    // Ground we can see, looked at again. Only what actually changed goes any
                    // further: an unchanged tile is dropped here, so standing still costs nothing
                    // beyond the sampling itself.
                    TerrainTile fresh = ClientTerrainSampler.tryBuild(tileX, tileZ);
                    if (fresh != null) {
                        localLeft--;
                        LOCAL_CHECKED.put(key, now);
                        if (fresh.contentHash() != heldHash(key)) {
                            builtLocally++;
                            accept(fresh, false);
                        }
                    }
                }

                if (answered == null) {
                    // The client's own chunks first, before the store and long before the server.
                    // Ground the player can see is already in memory; asking anyone for it would be
                    // a round trip and a disk read to be told what is sitting in front of us.
                    if (localLeft > 0) {
                        TerrainTile local = ClientTerrainSampler.tryBuild(tileX, tileZ);
                        if (local != null) {
                            localLeft--;
                            builtLocally++;
                            accept(local, false);
                            continue;
                        }
                    }
                    if (ASKED_DISK.contains(key)) {
                        // Already being looked for. The store answers within a frame or two.
                        continue;
                    }
                    if (TerrainDisk.isOpen() && ASKED_DISK.size() < DISK_IN_FLIGHT
                            && TerrainDisk.request(key)) {
                        // Ask the store first. Ground surveyed in an earlier session is right here,
                        // and asking the server for it would cost a round trip and a survey to be
                        // told what we already wrote down.
                        ASKED_DISK.add(key);
                        continue;
                    }
                    unseen.add(new long[]{tileX, tileZ, 0});
                } else if (COMPLETE.contains(key)) {
                    // Known ground, all of it. Nothing will change here that this map reports.
                    continue;
                } else if (mayRefresh && now - answered >= REFRESH_AFTER_MS) {
                    // Refreshes carry what is already held, so most of them come back as a few bytes
                    // saying "still that" rather than as the same twelve kilobytes over again.
                    stale.add(new long[]{tileX, tileZ, heldHash(key)});
                }
            }
        }
        if (unseen.isEmpty() && stale.isEmpty() && LINE_WANTED.isEmpty()) {
            return;
        }

        // Nearest to the middle first. Scanning corner to corner instead — which is what a plain
        // nested loop does — fills a wide view as a creeping edge-on stripe, so the part being
        // looked at is the last to arrive and the map looks broken while it is merely busy.
        Comparator<long[]> nearestToView = Comparator.comparingDouble(
                tile -> Math.pow(tile[0] - centreX, 2) + Math.pow(tile[1] - centreZ, 2));
        unseen.sort(nearestToView);
        stale.sort(nearestToView);

        // Ground never seen before always outranks re-checking ground we already have, so a view
        // full of stale tiles can never starve out a view of genuinely new country.
        // Whichever is smaller: a sweep's worth, or what is left of the window.
        int room = Math.min(REQUEST_BATCH, MAX_IN_FLIGHT - ASKED.size());
        List<long[]> wanted = new ArrayList<>(Math.max(0, room));
        for (long[] tile : unseen) {
            if (wanted.size() >= room) {
                break;
            }
            wanted.add(tile);
        }
        addLineTiles(wanted, now, room);
        for (long[] tile : stale) {
            if (wanted.size() >= room) {
                break;
            }
            wanted.add(tile);
            lastRefreshSweep = now;
        }

        for (long[] tile : wanted) {
            ASKED.put(TerrainTile.key((int) tile[0], (int) tile[1]), now);
        }
        if (!wanted.isEmpty()) {
            ModNetwork.toServer(new RequestTerrainTilesMessage(mainHand, wanted));
        }
    }

    /** Fills leftover request budget with ground under firing lines, nearest gaps first. */
    private static void addLineTiles(List<long[]> wanted, long now, int room) {
        if (LINE_WANTED.isEmpty()) {
            return;
        }

        Iterator<Long> pending = LINE_WANTED.iterator();
        while (pending.hasNext() && wanted.size() < room) {
            long key = pending.next();

            Long asked = ASKED.get(key);
            if (asked != null && now - asked < RETRY_AFTER_MS) {
                continue;
            }
            if (ANSWERED.containsKey(key)) {
                // Already surveyed; the line no longer needs it kept on the list.
                pending.remove();
                continue;
            }

            pending.remove();
            wanted.add(new long[]{(int) (key >> 32), (int) key, heldHash(key)});
        }
    }

    /** Whether a nearby tile is due another look, and cheap enough to ask on every sweep. */
    private static boolean dueForLocalCheck(long key, long now) {
        Long last = LOCAL_CHECKED.get(key);
        return last == null || now - last >= LOCAL_REFRESH_MS;
    }

    /**
     * Registers the tiles under a firing line so they get surveyed like anything else.
     *
     * <p>Kept separate from the visible area and always served after it. A line check wanting ground
     * five kilometres away must never delay the map the player is actually looking at.
     */
    public static void ensureLineCovered(int fromX, int fromZ, int toX, int toZ) {
        // Also here, not only where the map is drawn. This path serves the clearance check on the
        // HUD, which a player can be using with the tablet in hand and no screen open — so it is
        // reachable in a world the map has never been drawn in. Held ground read there would answer
        // a question about whether a shell clears the ground using another world's hills, which is
        // the one kind of wrong answer this device must never give.
        checkWorld();

        double dx = toX - fromX;
        double dz = toZ - fromZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1.0) {
            return;
        }

        // A step of half a tile cannot skip over one, whatever angle the line runs at.
        int steps = (int) Math.ceil(length / (TerrainTile.SIDE / 2.0));
        for (int i = 0; i <= steps; i++) {
            double fraction = i / (double) steps;
            int x = (int) Math.round(fromX + dx * fraction);
            int z = (int) Math.round(fromZ + dz * fraction);
            LINE_WANTED.add(TerrainTile.key(TerrainTile.blockToTile(x), TerrainTile.blockToTile(z)));
        }
    }

    /**
     * The tile covering a tile coordinate, or null when none is held.
     *
     * <p>Deliberately coarser than a per-column accessor. Drawing walks columns in world order, so
     * a caller can look a tile up once and then read many columns straight out of it — the
     * difference between a few thousand lookups per redraw and the best part of a million.
     */
    public static TerrainTile tileAt(int tileX, int tileZ) {
        synchronized (TILE_LOCK) {
            return TILES.get(TerrainTile.key(tileX, tileZ));
        }
    }

    /**
     * Surface height of one block column, or {@link TerrainTile#NO_DATA} where the ground has not
     * been surveyed. For scattered lookups only — drawing walks columns in bulk and should go
     * through {@link #tileAt} instead.
     */
    public static short heightAt(int worldX, int worldZ) {
        TerrainTile tile = tileAt(TerrainTile.blockToTile(worldX), TerrainTile.blockToTile(worldZ));
        if (tile == null) {
            return TerrainTile.NO_DATA;
        }
        return tile.height[TerrainTile.index(
                Math.floorMod(worldX, TerrainTile.SIDE), Math.floorMod(worldZ, TerrainTile.SIDE))];
    }
}
