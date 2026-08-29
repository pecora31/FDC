package net.nazarick.artillerytablet.terrain;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.nazarick.artillerytablet.ArtilleryTablet;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * One square of surveyed ground: {@value #SIDE} blocks on a side, one sample per block column.
 *
 * <p>Each column carries what block is on the surface and how high that surface is. The block is a
 * registry id rather than a colour, which is the difference between this map and one drawn from the
 * game's own palette: that palette has sixty-two colours in it, so oak, birch, spruce and every
 * other wood in the game arrive as one identical brown. An id lets the client look up what the
 * block actually looks like, and the map stops flattening the world into five materials.
 *
 * <p>Height is stored from the very first version even though nothing reads it yet. It is what will
 * let the tablet answer the question the fire-control side still cannot: whether a flat-trajectory
 * shell clears the ground between gun and target. Adding it later would mean discarding everything
 * surveyed up to that point, so it goes in now.
 *
 * <p>Shading is deliberately <em>not</em> baked in. Relief comes from comparing a column against its
 * northern neighbour, and neighbours cross tile edges — bake it here and every tile boundary shows
 * as a seam. The client has the heights of adjacent tiles and shades across them cleanly.
 */
public final class TerrainTile {
    /**
     * What revision of the layout below {@link #write} produces and {@link #read} understands.
     *
     * <p>Bump this whenever the bytes change — a column growing a field, a field changing width, the
     * order changing. The format has already changed several times without any way to say so, which
     * is a whole class of fault waiting to happen: two builds that disagree about the layout do not
     * fail at the disagreement, they decode plausible nonsense and paint it on the map.
     *
     * <p>Two things use it. {@link #read} refuses a tile it cannot parse, with a message that names
     * the cause rather than surfacing as an out-of-bounds read halfway down the array. And the
     * network channel folds it into its own version, so mismatched builds are turned away at the
     * handshake and never get as far as sending a tile at all.
     */
    public static final byte FORMAT_VERSION = 3;

    /** Blocks along one edge of a tile. Sized so one tile is a comfortable packet, ~12 KB raw. */
    public static final int SIDE = 64;
    public static final int COLUMNS = SIDE * SIDE;

    /** Height value meaning "no data" — nothing has ever been surveyed in this column. */
    public static final short NO_DATA = Short.MIN_VALUE;

    public final int tileX;
    public final int tileZ;

    /** Deepest water this records. Past this the shading has long since bottomed out. */
    public static final int MAX_DEPTH = 255;

    /** Biome value meaning "not recorded", so a tile from before biomes were carried still reads. */
    public static final short NO_BIOME = -1;

    /**
     * Block registry id per column, indexed {@code z * SIDE + x}. Zero is air, meaning nothing.
     *
     * <p>Sent as the numeric id rather than the name, and safe for exactly the reason the biome id
     * beneath is: block ids live in a registry the server hands the client at login, so both ends
     * number them identically for the life of the connection. It is the same guarantee the game's
     * own chunk packets lean on.
     *
     * <p>Held as a {@code short} but read unsigned through {@link #blockAt}, so the range runs to
     * 65535 rather than stopping halfway. A large modpack can carry more than thirty thousand
     * blocks, which is comfortably past where a signed short turns negative.
     *
     * <p>Under water this is the <em>sea floor</em>, not the water. The water is carried separately
     * as a depth and laid over the floor when drawing, which is what the game's own map item does
     * and what makes an ocean read as ground under water rather than as a flat sheet of blue.
     */
    public final short[] block;

    /**
     * Surface world Y per column, or {@link #NO_DATA}.
     *
     * <p>Deliberately still the <em>surface</em> — the top of the water where there is water, not
     * the floor beneath it. Ballistics reads this: a shell crossing an ocean is stopped by the sea,
     * and a height that pointed at the sea bed would quietly report clearance that does not exist.
     * The floor is recovered where it is wanted for drawing, as {@code height - depth}.
     */
    public final short[] height;

    /**
     * The ground's own elevation per column, or {@link #NO_DATA} — leaves and clutter (tall grass,
     * flowers, saplings...) read straight through to whatever is really underneath, unlike
     * {@link #height}, which draws a forest's canopy as the surface on purpose (see
     * {@link ChunkNbtSampler}/{@code ServerTerrainProvider}'s own notes on why both are computed and
     * kept, not one replacing the other). This is what a contour line traces and what a crest-clearance
     * query should read for "the ground", not the top of whatever is standing on it.
     */
    public final short[] groundHeight;

    /** Blocks of water above the floor, 0 where there is none. Capped at {@link #MAX_DEPTH}. */
    public final byte[] depth;

    /**
     * Biome id per column, or {@link #NO_BIOME}.
     *
     * <p>Sent as the registry id rather than the name, which is safe for the same reason the game's
     * own chunk packets do it: biomes live in a dynamic registry that the server hands to the client
     * on join, so both ends number them identically for the life of the connection.
     *
     * <p>Here so the client can tint grass, foliage and water the way the world actually looks. The
     * map palette has one green for grass everywhere on earth; a swamp, a jungle and a savanna are
     * the same pixel without this.
     */
    public final short[] biome;

    public TerrainTile(int tileX, int tileZ) {
        this(tileX, tileZ, new short[COLUMNS], newHeights(), newHeights(), new byte[COLUMNS], newBiomes());
    }

    public TerrainTile(int tileX, int tileZ, short[] block, short[] height, short[] groundHeight,
                        byte[] depth, short[] biome) {
        this.tileX = tileX;
        this.tileZ = tileZ;
        this.block = block;
        this.height = height;
        this.groundHeight = groundHeight;
        this.depth = depth;
        this.biome = biome;
    }

    private static short[] newHeights() {
        short[] heights = new short[COLUMNS];
        java.util.Arrays.fill(heights, NO_DATA);
        return heights;
    }

    private static short[] newBiomes() {
        short[] biomes = new short[COLUMNS];
        java.util.Arrays.fill(biomes, NO_BIOME);
        return biomes;
    }

    /** Water depth as a plain count, since the stored byte is unsigned. */
    public int depthAt(int index) {
        return depth[index] & 0xFF;
    }

    /** Block registry id as a plain number, since the stored short is unsigned. */
    public int blockAt(int index) {
        return block[index] & 0xFFFF;
    }

    // ---- wire layout ---------------------------------------------------------------------------
    //
    // Every field gets its own run of bytes, and the two halves of a two-byte field get a run each.
    // That last part is not tidiness — it is what makes the block id nearly free. Ids sit in the low
    // thousands, so the high byte of every column in a tile is almost always the same value, and
    // four thousand identical bytes in a row cost deflate almost nothing. Interleaved as hi,lo,hi,lo
    // that repetition is broken up by the varying half and the compressor cannot see it.
    //
    // The offsets are derived from each other rather than written out, and the total is derived from
    // the last of them. Restating any of these numbers is how a layout comes to disagree with
    // itself, which is a fault that does not throw — it decodes, and paints heights as biomes.

    private static final int BLOCK_HI = 0;
    private static final int BLOCK_LO = BLOCK_HI + COLUMNS;
    private static final int HEIGHT_HI = BLOCK_LO + COLUMNS;
    private static final int HEIGHT_LO = HEIGHT_HI + COLUMNS;
    private static final int GROUND_HEIGHT_HI = HEIGHT_LO + COLUMNS;
    private static final int GROUND_HEIGHT_LO = GROUND_HEIGHT_HI + COLUMNS;
    private static final int DEPTH = GROUND_HEIGHT_LO + COLUMNS;
    private static final int BIOME_HI = DEPTH + COLUMNS;
    private static final int BIOME_LO = BIOME_HI + COLUMNS;

    /**
     * Bytes one tile occupies before compression.
     *
     * <p>Named rather than written out at each end because the two must agree exactly — a decode
     * that expects a different length fails loudly here, which is the whole reason the length is
     * checked on read at all.
     */
    public static final int RAW_BYTES = BIOME_LO + COLUMNS;

    public static int index(int localX, int localZ) {
        return localZ * SIDE + localX;
    }

    /** World coordinate of a tile's north-west corner. */
    public static int tileToBlock(int tile) {
        return tile * SIDE;
    }

    public static int blockToTile(int block) {
        return Math.floorDiv(block, SIDE);
    }

    /** True when nothing at all was surveyed — worth skipping rather than sending 12 KB of nothing. */
    public boolean isEmpty() {
        for (short h : height) {
            if (h != NO_DATA) {
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the tile compressed. Terrain repeats heavily, so deflate typically takes the 12 KB
     * payload down to a couple of KB — worth doing when a single map view is dozens of tiles.
     */
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(FORMAT_VERSION);
        buf.writeVarInt(tileX);
        buf.writeVarInt(tileZ);

        byte[] raw = new byte[RAW_BYTES];
        for (int i = 0; i < COLUMNS; i++) {
            raw[BLOCK_HI + i] = (byte) (block[i] >> 8);
            raw[BLOCK_LO + i] = (byte) block[i];
            raw[HEIGHT_HI + i] = (byte) (height[i] >> 8);
            raw[HEIGHT_LO + i] = (byte) height[i];
            raw[GROUND_HEIGHT_HI + i] = (byte) (groundHeight[i] >> 8);
            raw[GROUND_HEIGHT_LO + i] = (byte) groundHeight[i];
            raw[DEPTH + i] = depth[i];
            raw[BIOME_HI + i] = (byte) (biome[i] >> 8);
            raw[BIOME_LO + i] = (byte) biome[i];
        }

        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        try {
            deflater.setInput(raw);
            deflater.finish();

            // One deflate() call fills the buffer it is given and returns — it is not a promise that
            // the stream is done. Ground that barely compresses can outgrow a buffer sized to the
            // input, so grow and keep going until finished() says so. Stopping at the first call
            // would truncate the tile silently here and surface as a confusing decode failure on the
            // client, far from the cause.
            byte[] packed = new byte[raw.length];
            int size = 0;
            while (!deflater.finished()) {
                if (size == packed.length) {
                    packed = java.util.Arrays.copyOf(packed, packed.length * 2);
                }
                size += deflater.deflate(packed, size, packed.length - size);
            }

            buf.writeVarInt(size);
            buf.writeBytes(packed, 0, size);
        } finally {
            deflater.end();
        }
    }

    public static TerrainTile read(FriendlyByteBuf buf) {
        byte version = buf.readByte();
        if (version != FORMAT_VERSION) {
            throw new IllegalStateException("terrain tile is format " + version
                    + ", this build reads format " + FORMAT_VERSION
                    + " — the client and server are different builds of the addon");
        }

        int tileX = buf.readVarInt();
        int tileZ = buf.readVarInt();

        int size = buf.readVarInt();
        byte[] packed = new byte[size];
        buf.readBytes(packed);

        byte[] raw = new byte[RAW_BYTES];
        Inflater inflater = new Inflater();
        try {
            inflater.setInput(packed);
            int read = inflater.inflate(raw);
            if (read != raw.length) {
                throw new IllegalStateException("terrain tile was " + read + " bytes, expected " + raw.length);
            }
        } catch (DataFormatException e) {
            throw new IllegalStateException("corrupt terrain tile", e);
        } finally {
            inflater.end();
        }

        short[] block = new short[COLUMNS];
        short[] height = new short[COLUMNS];
        short[] groundHeight = new short[COLUMNS];
        byte[] depth = new byte[COLUMNS];
        short[] biome = new short[COLUMNS];
        for (int i = 0; i < COLUMNS; i++) {
            block[i] = (short) ((raw[BLOCK_HI + i] << 8) | (raw[BLOCK_LO + i] & 0xFF));
            height[i] = (short) ((raw[HEIGHT_HI + i] << 8) | (raw[HEIGHT_LO + i] & 0xFF));
            groundHeight[i] = (short) ((raw[GROUND_HEIGHT_HI + i] << 8) | (raw[GROUND_HEIGHT_LO + i] & 0xFF));
            depth[i] = raw[DEPTH + i];
            biome[i] = (short) ((raw[BIOME_HI + i] << 8) | (raw[BIOME_LO + i] & 0xFF));
        }
        return new TerrainTile(tileX, tileZ, block, height, groundHeight, depth, biome);
    }

    /**
     * True when every column was surveyed.
     *
     * <p>Ground that is fully known does not need asking about again. Ground that came back with
     * gaps does — those are chunks that had not been generated yet when the survey ran, and walking
     * over there generates them.
     */
    public boolean isComplete() {
        for (short h : height) {
            if (h == NO_DATA) {
                return false;
            }
        }
        return true;
    }

    /**
     * A fingerprint of this tile's contents.
     *
     * <p>Lets a client say what it already holds and a server answer "still that" in a few bytes
     * instead of resending twelve kilobytes it has seen before. Defined here, once, because both
     * ends must agree on it exactly — the recurring fault in this project has been two places
     * working out the same value separately and drifting apart.
     */
    public int contentHash() {
        int hash = 1;
        for (short b : block) {
            hash = hash * 31 + b;
        }
        for (short h : height) {
            hash = hash * 31 + h;
        }
        for (short g : groundHeight) {
            hash = hash * 31 + g;
        }
        // Depth and biome belong in here too. Leaving either out would let the server answer
        // "unchanged" to a tile that had in fact changed in the only way the client could see.
        for (byte d : depth) {
            hash = hash * 31 + d;
        }
        for (short b : biome) {
            hash = hash * 31 + b;
        }
        return hash;
    }

    /**
     * The number this format stores for a block.
     *
     * <p>Here, once, because two different samplers feed the same tile — one reading saved chunk NBT
     * and one reading loaded chunks — and a column produced by either must be indistinguishable from
     * a column produced by the other. Every time this project has let two places work out the same
     * value separately, they have drifted, and the seam has shown up as a visible line on the map
     * along the edge of the loaded area.
     *
     * <p>Ids past what two bytes hold come back as air. That is a modpack with more than sixty-five
     * thousand blocks in it, which nothing near this project approaches, but a quiet wrap-around
     * would paint one block as another rather than as nothing — so it is said out loud once.
     */
    public static short idOf(Block block) {
        int id = BuiltInRegistries.BLOCK.getId(block);
        if (id < 0 || id > 0xFFFF) {
            if (!warnedAboutRange) {
                warnedAboutRange = true;
                ArtilleryTablet.LOGGER.warn(
                        "Block {} has registry id {}, past what the tablet map's two bytes hold — "
                                + "it and any others like it will show as blank ground", block, id);
            }
            return 0;
        }
        return (short) id;
    }

    private static boolean warnedAboutRange;

    public static long key(int tileX, int tileZ) {
        return (long) tileX << 32 | (tileZ & 0xFFFFFFFFL);
    }
}
