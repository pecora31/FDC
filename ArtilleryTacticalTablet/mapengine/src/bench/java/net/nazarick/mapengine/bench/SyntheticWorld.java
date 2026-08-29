package net.nazarick.mapengine.bench;

import net.nazarick.mapengine.core.ColumnBuffer;
import net.nazarick.mapengine.core.ColumnSource;

/**
 * Terrain out of thin air, so the engine can be measured without a game.
 *
 * <p>Every timing this engine reports comes from here — deterministic, generated in memory, identical
 * on every machine and every run — rather than from launching Minecraft, loading somebody's save and
 * judging a screen by eye.
 *
 * <p>The shape is chosen to be awkward in the ways real ground is awkward: continents so the coarse
 * levels have something to average, hills at the scale of a few dozen blocks so relief shading has
 * work to do, and sea so the water path is exercised.
 */
public final class SyntheticWorld implements ColumnSource {
    public static final int SEA_LEVEL = 62;

    public static final short BLOCK_STONE = 1;
    public static final short BLOCK_GRASS = 2;
    public static final short BLOCK_SAND = 3;
    public static final short BLOCK_SNOW = 4;
    public static final short BLOCK_GRAVEL = 5;

    private final long seed;
    private final double unexplored;

    public SyntheticWorld(long seed) {
        this(seed, 0.0);
    }

    public SyntheticWorld(long seed, double unexplored) {
        this.seed = seed;
        this.unexplored = unexplored;
    }

    @Override
    public boolean fill(int blockX, int blockZ, ColumnBuffer into) {
        boolean any = false;
        for (int z = 0; z < into.width; z++) {
            int worldZ = blockZ + z;
            for (int x = 0; x < into.width; x++) {
                int worldX = blockX + x;
                int index = into.index(x, z);

                if (unexplored > 0.0 && fbm(seed + 99, worldX / 384.0, worldZ / 384.0, 2) < unexplored) {
                    continue;
                }

                int ground = groundAt(worldX, worldZ);
                int surface;
                int depth;
                if (ground < SEA_LEVEL) {
                    surface = SEA_LEVEL;
                    depth = Math.min(ColumnBuffer.MAX_DEPTH, SEA_LEVEL - ground);
                } else {
                    surface = ground;
                    depth = 0;
                }

                into.height[index] = (short) surface;
                // No canopy in this synthetic world — the ground is the surface.
                into.groundHeight[index] = (short) surface;
                into.depth[index] = (byte) depth;
                into.block[index] = blockFor(ground, depth);
                into.biome[index] = biomeAt(worldX, worldZ);
                any = true;
            }
        }
        return any;
    }

    public int groundAt(int worldX, int worldZ) {
        double continent = fbm(seed, worldX / 700.0, worldZ / 700.0, 3);
        double hills = fbm(seed + 1, worldX / 70.0, worldZ / 70.0, 4);
        double detail = fbm(seed + 2, worldX / 14.0, worldZ / 14.0, 2);
        double h = 30.0 + continent * 90.0 + hills * 22.0 + detail * 4.0;
        return (int) Math.round(Math.max(4.0, Math.min(200.0, h)));
    }

    private short blockFor(int ground, int depth) {
        if (depth > 0) {
            return depth > 6 ? BLOCK_GRAVEL : BLOCK_SAND;
        }
        if (ground <= SEA_LEVEL + 2) {
            return BLOCK_SAND;
        }
        if (ground > 150) {
            return BLOCK_SNOW;
        }
        if (ground > 120) {
            return BLOCK_STONE;
        }
        return BLOCK_GRASS;
    }

    private short biomeAt(int worldX, int worldZ) {
        double n = fbm(seed + 7, worldX / 500.0, worldZ / 500.0, 2);
        return (short) Math.max(0, Math.min(3, (int) (n * 4.0)));
    }

    private static double fbm(long seed, double x, double z, int octaves) {
        double sum = 0.0;
        double amplitude = 1.0;
        double total = 0.0;
        for (int i = 0; i < octaves; i++) {
            sum += valueNoise(seed + i * 131L, x, z) * amplitude;
            total += amplitude;
            amplitude *= 0.5;
            x *= 2.0;
            z *= 2.0;
        }
        return sum / total;
    }

    private static double valueNoise(long seed, double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        double fx = x - x0;
        double fz = z - z0;
        double sx = fx * fx * (3.0 - 2.0 * fx);
        double sz = fz * fz * (3.0 - 2.0 * fz);

        double n00 = hash(seed, x0, z0);
        double n10 = hash(seed, x0 + 1, z0);
        double n01 = hash(seed, x0, z0 + 1);
        double n11 = hash(seed, x0 + 1, z0 + 1);

        return lerp(lerp(n00, n10, sx), lerp(n01, n11, sx), sz);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private static double hash(long seed, int x, int z) {
        long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
        h ^= h >>> 33;
        h *= 0xFF51AFD7ED558CCDL;
        h ^= h >>> 33;
        h *= 0xC4CEB9FE1A85EC53L;
        h ^= h >>> 33;
        return (h >>> 11) / (double) (1L << 53);
    }
}
