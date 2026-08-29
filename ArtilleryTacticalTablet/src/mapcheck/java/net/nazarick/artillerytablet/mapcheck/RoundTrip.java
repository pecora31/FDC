package net.nazarick.artillerytablet.mapcheck;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import java.util.Arrays;
import java.util.Random;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.check;

/**
 * Writes a tile and reads it back, and reports what it costs on the wire.
 *
 * <p>The layout is a set of offsets into one flat array, computed by hand at both ends. Get one of
 * them wrong and nothing throws: the tile decodes, every column has a value, and the map paints
 * heights as biomes. This is the check that makes a change to the format safe to make at all.
 *
 * <p>The wire sizes are printed rather than asserted. They are a measurement — the point of writing
 * each field as its own run of bytes is that deflate can then see the repetition, and the only way
 * to know whether that is working is to look at the number for ground shaped like real ground.
 */
final class RoundTrip {
    private RoundTrip() {
    }

    static void run() {
        Random rng = new Random(1234);
        TerrainTile out = new TerrainTile(-7, 13);
        for (int i = 0; i < TerrainTile.COLUMNS; i++) {
            out.block[i] = (short) rng.nextInt(65536);
            out.height[i] = (short) (rng.nextInt(400) - 64);
            out.groundHeight[i] = (short) (rng.nextInt(400) - 64);
            out.depth[i] = (byte) rng.nextInt(256);       // exercises the unsigned range
            out.biome[i] = (short) rng.nextInt(600);
        }

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        out.write(buf);
        int wire = buf.readableBytes();
        TerrainTile in = TerrainTile.read(buf);

        check(in.tileX == -7 && in.tileZ == 13, "tile coords");
        for (int i = 0; i < TerrainTile.COLUMNS; i++) {
            check(in.block[i] == out.block[i], "block " + i);
            check(in.height[i] == out.height[i], "height " + i);
            check(in.groundHeight[i] == out.groundHeight[i], "groundHeight " + i);
            check(in.depth[i] == out.depth[i], "depth " + i);
            check(in.biome[i] == out.biome[i], "biome " + i);
            check(in.depthAt(i) == (out.depth[i] & 0xFF), "depthAt " + i);
            check(in.blockAt(i) == (out.block[i] & 0xFFFF), "blockAt " + i);
        }
        check(in.contentHash() == out.contentHash(), "contentHash");
        check(buf.readableBytes() == 0, "buffer fully consumed");

        // Worst case for deflate: random data. Real terrain compresses far better.
        System.out.println("roundtrip: raw=" + TerrainTile.RAW_BYTES + "B  wire(random)=" + wire + "B");

        // And the shape real ground has: mostly uniform.
        TerrainTile flat = new TerrainTile(0, 0);
        Arrays.fill(flat.height, (short) 64);
        FriendlyByteBuf uniform = new FriendlyByteBuf(Unpooled.buffer());
        flat.write(uniform);
        System.out.println("roundtrip: wire(uniform)=" + uniform.readableBytes() + "B");

        // Smooth heights, a handful of materials, one or two biomes. This is the number that
        // actually matters, since it is what a tile costs in play.
        TerrainTile real = new TerrainTile(0, 0);
        for (int z = 0; z < TerrainTile.SIDE; z++) {
            for (int x = 0; x < TerrainTile.SIDE; x++) {
                int i = TerrainTile.index(x, z);
                int h = (int) (68 + 9 * Math.sin(x / 11.0) + 7 * Math.cos(z / 13.0));
                boolean sea = h < 63;
                real.height[i] = (short) (sea ? 63 : h);
                real.depth[i] = (byte) (sea ? 63 - h : 0);
                // A few block ids in the range vanilla actually uses, not spread over the whole
                // sixteen bits — which is the case that decides whether the high byte compresses.
                real.block[i] = (short) (sea ? 267 : (h > 74 ? 1 : 9));
                real.biome[i] = (short) (x < 40 ? 4 : 7);
            }
        }
        FriendlyByteBuf realistic = new FriendlyByteBuf(Unpooled.buffer());
        real.write(realistic);
        int realWire = realistic.readableBytes();
        check(TerrainTile.read(realistic).contentHash() == real.contentHash(), "realistic round trip");
        System.out.println("roundtrip: OK  wire(realistic terrain)=" + realWire + "B");
    }
}
