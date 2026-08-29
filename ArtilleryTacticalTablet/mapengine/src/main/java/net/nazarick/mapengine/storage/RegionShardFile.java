package net.nazarick.mapengine.storage;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * A grid of {@value #SIDE}×{@value #SIDE} regions in one file, because one file per region turned
 * out not to be enough.
 *
 * <p>Measured, not assumed: with real disk exclusions confirmed in place and an NVMe SSD confirmed as
 * the drive, sixty-four separate {@link RegionFile} reads still took over 600&nbsp;ms regardless of
 * how many threads read them — raising the reader pool from four threads to sixteen made no
 * measurable difference. That is the signature of a per-file-open cost the filesystem itself imposes
 * (NTFS journals a metadata transaction for every file created or opened) rather than of anything
 * this engine's own code was doing slowly. The fix a thread pool cannot buy has to be fewer files.
 *
 * <p>One shard covers {@code SIDE × Region.BLOCKS} blocks on a side — 4096 blocks at the default
 * size — which is close to the tablet's own 4000&nbsp;m widest zoom with hillshading still on. A
 * view that fits in one shard becomes one file open on a cold start instead of sixty-four.
 *
 * <p><b>What is inside.</b> Every region's level 0 columns, back to back, deflated as one stream —
 * not one deflate call per region. Compressing the whole shard together lets deflate's own dictionary
 * find repetition <em>across</em> region boundaries (flat sea meeting flat sea, forest meeting
 * forest), which per-region compression could never see. A presence bitmap says which of the
 * {@code SIDE × SIDE} slots actually hold a region; the rest of the world has not been surveyed and
 * costs nothing in the file.
 */
public final class RegionShardFile {
    private static final byte FORMAT_VERSION = 2;

    /** Regions along one edge of a shard file. */
    public static final int SIDE = 8;

    private RegionShardFile() {
    }

    public static int shardOf(int region) {
        return Math.floorDiv(region, SIDE);
    }

    /**
     * Writes every region handed in as one shard file, at the shard the first one's coordinates
     * belong to. The caller is responsible for grouping regions that share a shard before calling —
     * see {@link RegionStore}, which is the only caller and does exactly that.
     */
    public static void write(Path root, int shardX, int shardZ, Map<Long, ColumnBuffer> regions) throws IOException {
        boolean[] present = new boolean[SIDE * SIDE];
        int width = Region.BLOCKS; // assumed uniform; checked below rather than trusted
        int bytesPerRegion = width * width * 9;

        // Which slots are present has to be known before the raw buffer can be sized, so this walks
        // the grid twice — once to count, once to pack. Both passes are plain array reads; the cost
        // that mattered was never the loop, it was the per-field stream calls the first version of
        // this method made two million times over. See the class doc for the actual measurement.
        int filled = 0;
        for (int dz = 0; dz < SIDE; dz++) {
            for (int dx = 0; dx < SIDE; dx++) {
                int regionX = shardX * SIDE + dx;
                int regionZ = shardZ * SIDE + dz;
                if (regions.containsKey(net.nazarick.mapengine.core.RegionKey.of(regionX, regionZ))) {
                    present[dz * SIDE + dx] = true;
                    filled++;
                }
            }
        }

        byte[] raw = new byte[filled * bytesPerRegion];
        int at = 0;
        for (int dz = 0; dz < SIDE; dz++) {
            for (int dx = 0; dx < SIDE; dx++) {
                if (!present[dz * SIDE + dx]) {
                    continue;
                }
                int regionX = shardX * SIDE + dx;
                int regionZ = shardZ * SIDE + dz;
                ColumnBuffer columns = regions.get(net.nazarick.mapengine.core.RegionKey.of(regionX, regionZ));
                if (columns.width != width) {
                    throw new IOException("region (" + regionX + "," + regionZ + ") has width "
                            + columns.width + ", shard expects " + width);
                }
                packColumns(columns, raw, at);
                at += bytesPerRegion;
            }
        }

        byte[] packed = deflate(raw);

        ByteArrayOutputStream out = new ByteArrayOutputStream(packed.length + 64);
        DataOutputStream data = new DataOutputStream(out);
        data.writeByte(FORMAT_VERSION);
        data.writeInt(shardX);
        data.writeInt(shardZ);
        data.writeInt(width);
        for (boolean b : present) {
            data.writeBoolean(b);
        }
        data.writeInt(raw.length);
        data.writeInt(packed.length);
        data.write(packed);

        Path file = fileFor(root, shardX, shardZ);
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".part");
        Files.write(temp, out.toByteArray());
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads a whole shard in one file open, one read and one inflate. Missing or corrupt reads as
     * "no shard yet" rather than throwing — the caller falls back to surveying, the same discipline
     * every store in this project follows for a file it cannot trust.
     *
     * @return region key to columns, for every region the shard actually holds
     */
    public static Map<Long, ColumnBuffer> read(Path root, int shardX, int shardZ) {
        Path file = fileFor(root, shardX, shardZ);
        if (!Files.isRegularFile(file)) {
            return Map.of();
        }
        try {
            byte[] bytes = Files.readAllBytes(file);
            DataInputStream data = new DataInputStream(new java.io.ByteArrayInputStream(bytes));

            byte version = data.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException("shard is format " + version + ", this build reads " + FORMAT_VERSION);
            }
            int fileX = data.readInt();
            int fileZ = data.readInt();
            if (fileX != shardX || fileZ != shardZ) {
                throw new IOException("shard names (" + fileX + "," + fileZ + ") but was filed as ("
                        + shardX + "," + shardZ + ")");
            }
            int width = data.readInt();
            boolean[] present = new boolean[SIDE * SIDE];
            for (int i = 0; i < present.length; i++) {
                present[i] = data.readBoolean();
            }
            int rawLength = data.readInt();
            int packedLength = data.readInt();
            byte[] packed = new byte[packedLength];
            data.readFully(packed);
            byte[] raw = inflate(packed, rawLength);

            int bytesPerRegion = width * width * 9;
            int at = 0;
            Map<Long, ColumnBuffer> result = new HashMap<>();
            for (int dz = 0; dz < SIDE; dz++) {
                for (int dx = 0; dx < SIDE; dx++) {
                    if (!present[dz * SIDE + dx]) {
                        continue;
                    }
                    int regionX = shardX * SIDE + dx;
                    int regionZ = shardZ * SIDE + dz;
                    ColumnBuffer columns = unpackColumns(raw, at, width);
                    at += bytesPerRegion;
                    result.put(net.nazarick.mapengine.core.RegionKey.of(regionX, regionZ), columns);
                }
            }
            return result;
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // Next read fails the same way and tries again.
            }
            return Map.of();
        }
    }

    /**
     * Packs one region's columns straight into {@code raw} at {@code offset}, by index rather than
     * through a stream. The first version of this class called {@code DataOutputStream.writeShort}
     * four times per column — two million calls for one shard — and that call overhead, not disk,
     * was the whole of why shards first measured slower than the many-small-files layout they were
     * meant to replace. {@link RegionFile} never made that mistake; this now matches it.
     */
    private static void packColumns(ColumnBuffer columns, byte[] raw, int offset) {
        int n = columns.columns();
        for (int i = 0; i < n; i++) {
            int at = offset + i * 9;
            raw[at] = (byte) (columns.height[i] >> 8);
            raw[at + 1] = (byte) columns.height[i];
            raw[at + 2] = (byte) (columns.groundHeight[i] >> 8);
            raw[at + 3] = (byte) columns.groundHeight[i];
            raw[at + 4] = (byte) (columns.block[i] >> 8);
            raw[at + 5] = (byte) columns.block[i];
            raw[at + 6] = (byte) (columns.biome[i] >> 8);
            raw[at + 7] = (byte) columns.biome[i];
            raw[at + 8] = columns.depth[i];
        }
    }

    private static ColumnBuffer unpackColumns(byte[] raw, int offset, int width) {
        ColumnBuffer columns = new ColumnBuffer(width);
        int n = columns.columns();
        for (int i = 0; i < n; i++) {
            int at = offset + i * 9;
            columns.height[i] = (short) ((raw[at] << 8) | (raw[at + 1] & 0xFF));
            columns.groundHeight[i] = (short) ((raw[at + 2] << 8) | (raw[at + 3] & 0xFF));
            columns.block[i] = (short) ((raw[at + 4] << 8) | (raw[at + 5] & 0xFF));
            columns.biome[i] = (short) ((raw[at + 6] << 8) | (raw[at + 7] & 0xFF));
            columns.depth[i] = raw[at + 8];
        }
        return columns;
    }

    private static Path fileFor(Path root, int shardX, int shardZ) {
        return root.resolve("shard_" + shardX + "_" + shardZ + ".shard");
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
                throw new IOException("shard body was " + read + " bytes, expected " + rawLength);
            }
            return raw;
        } catch (DataFormatException e) {
            throw new IOException("corrupt shard", e);
        } finally {
            inflater.end();
        }
    }
}
