package net.nazarick.artillerytablet.terrain;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * How a surveyed tile is laid out on disk, in one place for both ends.
 *
 * <p>There are two stores — the client keeps ground it has been told about, and the server keeps
 * ground it has surveyed — and they are for different reasons, but a tile in a file is a tile in a
 * file. Written twice they would drift, and the way that failure presents is a file written by one
 * and unreadable by the other, discovered weeks later on ground nobody has revisited since.
 *
 * <p>The bytes are exactly what goes over the wire, which is deliberate: the tile already knows how
 * to compress itself and already carries a revision of its own format, so a file from an older build
 * is recognised and dropped rather than decoded into plausible nonsense.
 */
public final class TileFiles {
    /**
     * Tiles per directory before the names are sharded.
     *
     * <p>A session of exploring lays down tens of thousands of these, and a directory holding all of
     * them is slow to walk on every filesystem that has an opinion about it. Floor division, so the
     * western and northern halves of the world shard as evenly as the rest.
     */
    private static final int SHARD = 32;

    private TileFiles() {
    }

    /** Writes a tile, whole or not at all. */
    public static void write(Path root, TerrainTile tile) throws IOException {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            tile.write(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);

            Path file = fileFor(root, TerrainTile.key(tile.tileX, tile.tileZ));
            Files.createDirectories(file.getParent());

            // Written beside and moved into place. A half-written tile is not a tile, and the way
            // this would be discovered is a decode failure weeks later on ground nobody has visited
            // since the crash that caused it.
            Path temp = file.resolveSibling(file.getFileName() + ".part");
            Files.write(temp, bytes);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            buf.release();
        }
    }

    /**
     * Reads a tile, or null when there is none to read.
     *
     * <p>A file this build cannot parse — a format from an older one, or a damaged write — is deleted
     * and reported as absent. Both callers answer that the same way: ask whoever knows what is there
     * now. This is exactly what the tile's own version byte exists for.
     */
    public static TerrainTile read(Path root, long key) throws IOException {
        Path file = fileFor(root, key);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        byte[] bytes = Files.readAllBytes(file);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bytes));
        try {
            return TerrainTile.read(buf);
        } catch (Throwable t) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // Nothing useful to do; the next read fails the same way and tries again.
            }
            return null;
        } finally {
            buf.release();
        }
    }

    /** Where a tile lives. */
    public static Path fileFor(Path root, long key) {
        int tileX = (int) (key >> 32);
        int tileZ = (int) key;
        return root.resolve(Math.floorDiv(tileX, SHARD) + "_" + Math.floorDiv(tileZ, SHARD))
                .resolve("t" + tileX + "_" + tileZ + ".tile");
    }

    /** Anything a filesystem might object to becomes an underscore. */
    public static String safeName(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            out.append(Character.isLetterOrDigit(c) || c == '-' || c == '.' ? c : '_');
        }
        String cleaned = out.toString();
        // "." and ".." survive the cleaning above as themselves, and neither is a folder — they are
        // steps along a path. Resolving one puts the contents where the parent is, so two different
        // worlds land in the same place and are each served the other's ground. That happened: the
        // world name was taken from a path ending in ".", and every single-player world was filed
        // together. Nothing threw; the map simply had somewhere else's country on it.
        if (cleaned.isEmpty() || cleaned.equals(".") || cleaned.equals("..")) {
            return "unknown";
        }
        return cleaned;
    }
}
