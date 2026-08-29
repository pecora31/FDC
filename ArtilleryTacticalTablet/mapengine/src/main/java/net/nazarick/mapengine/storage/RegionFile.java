package net.nazarick.mapengine.storage;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.Region;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * How one region's level-0 columns are laid out on disk, and how they get there safely.
 *
 * <p><b>Only level 0.</b> Levels 1..6 are {@link net.nazarick.mapengine.lod.Pyramid} arithmetic on
 * data already in memory once level 0 is loaded, so storing them too would be seven times the bytes
 * for zero new information — this file holds the one level a survey actually produces.
 *
 * <p><b>One file per region, not per tile.</b> The property this exists for: a 4000&nbsp;m view is
 * ~64 of these instead of ~3900 of the old 64-block tile files. The cost of opening a file dominates
 * the cost of reading one this size, so file count is what a cold open actually pays for.
 *
 * <p><b>Written whole, moved into place.</b> A half-written region is not a region, and the failure
 * mode of writing in place is a torn file discovered on the read that matters — a restart after a
 * crash. Written beside the real name and renamed over it, atomically where the filesystem allows it,
 * so a reader always sees the old complete file or the new one.
 */
public final class RegionFile {
    private static final byte FORMAT_VERSION = 2;

    private RegionFile() {
    }

    public static void write(Path root, int regionX, int regionZ, ColumnBuffer level0) throws IOException {
        int columns = level0.columns();
        // 9 bytes/column: height hi/lo, groundHeight hi/lo, block hi/lo, biome hi/lo, depth.
        byte[] raw = new byte[columns * 9];
        for (int i = 0; i < columns; i++) {
            int at = i * 9;
            raw[at] = (byte) (level0.height[i] >> 8);
            raw[at + 1] = (byte) level0.height[i];
            raw[at + 2] = (byte) (level0.groundHeight[i] >> 8);
            raw[at + 3] = (byte) level0.groundHeight[i];
            raw[at + 4] = (byte) (level0.block[i] >> 8);
            raw[at + 5] = (byte) level0.block[i];
            raw[at + 6] = (byte) (level0.biome[i] >> 8);
            raw[at + 7] = (byte) level0.biome[i];
            raw[at + 8] = level0.depth[i];
        }

        byte[] packed = deflate(raw);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(packed.length + 32);
        DataOutputStream data = new DataOutputStream(buffer);
        data.writeByte(FORMAT_VERSION);
        data.writeInt(regionX);
        data.writeInt(regionZ);
        data.writeInt(level0.width);
        data.writeInt(raw.length);
        data.writeInt(packed.length);
        data.write(packed);

        Path file = fileFor(root, regionX, regionZ);
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".part");
        Files.write(temp, buffer.toByteArray());
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /**
     * Reads a region's level 0, or null when there is none or it could not be decoded.
     *
     * <p>A file this build cannot parse — an old format, a damaged write, a size that does not match
     * what the header claims — is deleted and reported absent rather than trusted: the caller's
     * answer is then "resurvey this region", which is always safe, against "decode nonsense into the
     * map", which is not.
     */
    public static ColumnBuffer read(Path root, int regionX, int regionZ) {
        Path file = fileFor(root, regionX, regionZ);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            byte[] bytes = Files.readAllBytes(file);
            DataInputStream data = new DataInputStream(new java.io.ByteArrayInputStream(bytes));

            byte version = data.readByte();
            if (version != FORMAT_VERSION) {
                throw new IOException("region is format " + version + ", this build reads " + FORMAT_VERSION);
            }
            int fileX = data.readInt();
            int fileZ = data.readInt();
            if (fileX != regionX || fileZ != regionZ) {
                throw new IOException("region names (" + fileX + "," + fileZ + ") but was filed as ("
                        + regionX + "," + regionZ + ")");
            }
            int width = data.readInt();
            int rawLength = data.readInt();
            int packedLength = data.readInt();
            byte[] packed = new byte[packedLength];
            data.readFully(packed);

            byte[] raw = inflate(packed, rawLength);
            ColumnBuffer columns = new ColumnBuffer(width);
            int count = columns.columns();
            if (raw.length != count * 9) {
                throw new IOException("region body is " + raw.length + " bytes, expected " + (count * 9));
            }
            for (int i = 0; i < count; i++) {
                int at = i * 9;
                columns.height[i] = (short) ((raw[at] << 8) | (raw[at + 1] & 0xFF));
                columns.groundHeight[i] = (short) ((raw[at + 2] << 8) | (raw[at + 3] & 0xFF));
                columns.block[i] = (short) ((raw[at + 4] << 8) | (raw[at + 5] & 0xFF));
                columns.biome[i] = (short) ((raw[at + 6] << 8) | (raw[at + 7] & 0xFF));
                columns.depth[i] = raw[at + 8];
            }
            return columns;
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // Next read fails the same way and tries again.
            }
            return null;
        }
    }

    private static Path fileFor(Path root, int regionX, int regionZ) {
        // Sharded the same way TileFiles was: a session over a modest area still produces enough
        // regions that one flat directory becomes slow to list on every filesystem that has an
        // opinion about it.
        int shard = 16;
        return root.resolve(Math.floorDiv(regionX, shard) + "_" + Math.floorDiv(regionZ, shard))
                .resolve("r" + regionX + "_" + regionZ + ".region");
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
                throw new IOException("region was " + read + " bytes, expected " + rawLength);
            }
            return raw;
        } catch (DataFormatException e) {
            throw new IOException("corrupt region", e);
        } finally {
            inflater.end();
        }
    }
}
