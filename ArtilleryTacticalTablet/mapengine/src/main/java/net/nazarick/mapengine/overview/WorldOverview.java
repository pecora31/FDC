package net.nazarick.mapengine.overview;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;
import net.nazarick.mapengine.core.RegionKey;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * One coarse, always-resident picture of every region this store has ever surveyed — the thing that
 * answers "what does the player already know" the instant the map opens, before a single
 * {@link net.nazarick.mapengine.storage.RegionStore#get} call has resolved.
 *
 * <p><b>Why this exists, distinct from the pyramid.</b> A {@link Region}'s own coarse levels only
 * exist once that region is loaded, and only cover that one region — asking "what has the player
 * explored in the last three sessions, ten thousand blocks away from here" would mean loading every
 * region they ever walked over, which is the exact cold-open cost this project has spent this whole
 * pass cutting down. This is the opposite trade: one texel per {@value #STRIDE} blocks, for the
 * <em>whole</em> explored world, small enough that "load everything" and "load nothing" cost about
 * the same — a few megabytes at most for an enormously explored world, one sequential file read, no
 * per-region file opens at all.
 *
 * <p><b>How a texel gets in here.</b> {@code RegionStore} calls {@link #merge} once, the moment a
 * region is freshly surveyed (never on a load from disk — that ground already merged in whichever
 * session surveyed it first, and re-averaging it on every subsequent open would be exactly the
 * wasted repeat work this class exists to avoid). One region contributes an 8×8 grid of samples,
 * each a box-average of {@value #STRIDE}×{@value #STRIDE} raw columns — not a walk down the pyramid
 * (level 1's own halving is the expensive step there; this reduces straight from level 0 in one
 * pass, so it costs what building level 1 alone costs and nothing more, regardless of how coarse the
 * pyramid itself would eventually go).
 *
 * <p><b>Sparse, keyed the same way a region is.</b> {@link RegionKey#of} packs two ints; nothing
 * about it is specific to region coordinates, so it packs an overview texel's coordinates just as
 * well. A {@link ConcurrentHashMap} because {@code merge} runs on whichever I/O thread just finished
 * surveying a region — different regions never share a texel, so concurrent merges never race on the
 * same key, and no external locking is needed for that reason alone.
 */
public final class WorldOverview {
    private static final byte FORMAT_VERSION = 1;

    /** Blocks covered by one overview texel. Matches {@link Region#BLOCKS} divided into 8 boxes. */
    public static final int STRIDE = 64;

    /** Overview texels along one edge of a region — {@link Region#BLOCKS} / {@link #STRIDE}. */
    public static final int BOXES_PER_REGION = Region.BLOCKS / STRIDE;

    public record Sample(short height, short block, short biome, byte depth) {
    }

    private final Map<Long, Sample> samples = new ConcurrentHashMap<>();

    /**
     * Folds one freshly surveyed region's level 0 into the overview, as {@value #BOXES_PER_REGION}
     * &times; {@value #BOXES_PER_REGION} box-averaged samples. Safe to call from any thread; safe to
     * call more than once for the same region (a re-survey simply overwrites the same texels).
     */
    public void merge(int regionX, int regionZ, ColumnBuffer level0) {
        if (level0.width != Region.BLOCKS) {
            throw new IllegalArgumentException("overview merge expects a level-0 buffer, got width "
                    + level0.width);
        }
        // Reused across all 64 boxes in this region rather than allocated per box — the earlier
        // HashMap<Short,Integer> version cost 37 ms/region in practice, almost entirely boxing and
        // hashing overhead rather than the counting itself. These two plain arrays plus a sort carry
        // no boxing at all.
        short[] blockScratch = new short[STRIDE * STRIDE];
        short[] biomeScratch = new short[STRIDE * STRIDE];
        for (int bz = 0; bz < BOXES_PER_REGION; bz++) {
            for (int bx = 0; bx < BOXES_PER_REGION; bx++) {
                Sample sample = reduceBox(level0, bx * STRIDE, bz * STRIDE, blockScratch, biomeScratch);
                if (sample == null) {
                    continue; // nothing known in this box — leave it absent, not a false "empty" answer
                }
                long key = RegionKey.of(regionX * BOXES_PER_REGION + bx, regionZ * BOXES_PER_REGION + bz);
                samples.put(key, sample);
            }
        }
    }

    private static Sample reduceBox(ColumnBuffer level0, int x0, int z0,
                                     short[] blockScratch, short[] biomeScratch) {
        long heightSum = 0;
        long depthSum = 0;
        int known = 0;
        for (int z = z0; z < z0 + STRIDE; z++) {
            for (int x = x0; x < x0 + STRIDE; x++) {
                int idx = level0.index(x, z);
                short h = level0.height[idx];
                if (h == ColumnBuffer.NO_DATA) {
                    continue;
                }
                heightSum += h;
                depthSum += level0.depthAt(idx);
                blockScratch[known] = level0.block[idx];
                biomeScratch[known] = level0.biome[idx];
                known++;
            }
        }
        if (known == 0) {
            return null;
        }
        short height = (short) Math.round(heightSum / (double) known);
        byte depth = (byte) Math.min(ColumnBuffer.MAX_DEPTH, Math.round(depthSum / (double) known));
        short block = mode(blockScratch, known);
        short biome = mode(biomeScratch, known);
        return new Sample(height, block, biome, depth);
    }

    /**
     * The most frequent value among the first {@code n} entries of {@code values} — sorted in place,
     * then a single linear pass counting runs. {@code O(n log n)} against a hash map's amortized
     * {@code O(n)}, but with zero boxing and zero allocation beyond the sort itself, which is what
     * actually dominated at this scale (up to 4096 candidates, sorted 64 times per region).
     */
    private static short mode(short[] values, int n) {
        java.util.Arrays.sort(values, 0, n);
        short best = values[0];
        int bestCount = 1;
        short current = values[0];
        int currentCount = 1;
        for (int i = 1; i < n; i++) {
            if (values[i] == current) {
                currentCount++;
            } else {
                current = values[i];
                currentCount = 1;
            }
            if (currentCount > bestCount) {
                bestCount = currentCount;
                best = current;
            }
        }
        return best;
    }

    /** The sample covering this block coordinate, or null where nothing has ever been surveyed. */
    public Sample sampleAt(int blockX, int blockZ) {
        long key = RegionKey.of(Math.floorDiv(blockX, STRIDE), Math.floorDiv(blockZ, STRIDE));
        return samples.get(key);
    }

    public int size() {
        return samples.size();
    }

    /**
     * Writes every known texel as one file: a fixed header, then every sample back to back,
     * deflated as a single stream — the same "one open, one bulk read" reasoning as
     * {@link net.nazarick.mapengine.storage.RegionShardFile}, applied here to the whole world instead
     * of one shard, because this file is small regardless of how much has been explored.
     */
    public void write(Path file) throws IOException {
        int count = samples.size();
        byte[] raw = new byte[count * 15]; // overviewX(4) + overviewZ(4) + height(2) + block(2) + biome(2) + depth(1)
        int at = 0;
        for (Map.Entry<Long, Sample> e : samples.entrySet()) {
            int ox = RegionKey.x(e.getKey());
            int oz = RegionKey.z(e.getKey());
            Sample s = e.getValue();
            raw[at] = (byte) (ox >> 24);
            raw[at + 1] = (byte) (ox >> 16);
            raw[at + 2] = (byte) (ox >> 8);
            raw[at + 3] = (byte) ox;
            raw[at + 4] = (byte) (oz >> 24);
            raw[at + 5] = (byte) (oz >> 16);
            raw[at + 6] = (byte) (oz >> 8);
            raw[at + 7] = (byte) oz;
            raw[at + 8] = (byte) (s.height() >> 8);
            raw[at + 9] = (byte) s.height();
            raw[at + 10] = (byte) (s.block() >> 8);
            raw[at + 11] = (byte) s.block();
            raw[at + 12] = (byte) (s.biome() >> 8);
            raw[at + 13] = (byte) s.biome();
            raw[at + 14] = s.depth();
            at += 15;
        }

        byte[] packed = deflate(raw);
        ByteArrayOutputStream out = new ByteArrayOutputStream(packed.length + 32);
        DataOutputStream data = new DataOutputStream(out);
        data.writeByte(FORMAT_VERSION);
        data.writeInt(count);
        data.writeInt(raw.length);
        data.writeInt(packed.length);
        data.write(packed);

        Files.createDirectories(file.toAbsolutePath().getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".part");
        Files.write(temp, out.toByteArray());
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads the overview back, or an empty one when the file is missing, corrupt, or from a format
     * this build no longer reads — same "absent is safe, never decode nonsense" discipline every
     * other store in this engine follows. Meant to run synchronously at boot: this is the one read
     * the whole "see everything explored immediately" property depends on actually being instant.
     */
    public static WorldOverview read(Path file) {
        WorldOverview overview = new WorldOverview();
        if (!Files.isRegularFile(file)) {
            return overview;
        }
        try {
            byte[] bytes = Files.readAllBytes(file);
            DataInputStream data = new DataInputStream(new java.io.ByteArrayInputStream(bytes));
            byte version = data.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException("overview is format " + version + ", this build reads " + FORMAT_VERSION);
            }
            int count = data.readInt();
            int rawLength = data.readInt();
            int packedLength = data.readInt();
            byte[] packed = new byte[packedLength];
            data.readFully(packed);
            byte[] raw = inflate(packed, rawLength);
            if (raw.length != count * 15) {
                throw new IOException("overview body is " + raw.length + " bytes, expected " + (count * 15));
            }
            int at = 0;
            for (int i = 0; i < count; i++) {
                int ox = ((raw[at] & 0xFF) << 24) | ((raw[at + 1] & 0xFF) << 16)
                        | ((raw[at + 2] & 0xFF) << 8) | (raw[at + 3] & 0xFF);
                int oz = ((raw[at + 4] & 0xFF) << 24) | ((raw[at + 5] & 0xFF) << 16)
                        | ((raw[at + 6] & 0xFF) << 8) | (raw[at + 7] & 0xFF);
                short height = (short) ((raw[at + 8] << 8) | (raw[at + 9] & 0xFF));
                short block = (short) ((raw[at + 10] << 8) | (raw[at + 11] & 0xFF));
                short biome = (short) ((raw[at + 12] << 8) | (raw[at + 13] & 0xFF));
                byte depth = raw[at + 14];
                at += 15;
                overview.samples.put(RegionKey.of(ox, oz), new Sample(height, block, biome, depth));
            }
            return overview;
        } catch (Throwable t) {
            // A corrupt overview costs a slower open (regions load individually until it is rebuilt
            // by fresh merges) never a wrong answer, so this is deleted and treated as absent rather
            // than trusted.
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // Next read fails the same way and tries again.
            }
            return new WorldOverview();
        }
    }

    private static byte[] deflate(byte[] raw) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        try {
            deflater.setInput(raw);
            deflater.finish();
            byte[] packed = new byte[Math.max(64, raw.length / 2)];
            int size = 0;
            while (!deflater.finished()) {
                if (size == packed.length) {
                    packed = java.util.Arrays.copyOf(packed, packed.length * 2);
                }
                size += deflater.deflate(packed, size, packed.length - size);
            }
            return java.util.Arrays.copyOf(packed, size);
        } finally {
            deflater.end();
        }
    }

    private static byte[] inflate(byte[] packed, int rawLength) throws IOException {
        byte[] raw = new byte[rawLength];
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(packed);
            int read = inflater.inflate(raw);
            if (read != rawLength) {
                throw new IOException("overview was " + read + " bytes, expected " + rawLength);
            }
            return raw;
        } catch (DataFormatException e) {
            throw new IOException("corrupt overview", e);
        } finally {
            inflater.end();
        }
    }
}
