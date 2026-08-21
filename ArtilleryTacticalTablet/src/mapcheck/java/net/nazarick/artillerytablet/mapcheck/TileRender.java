package net.nazarick.artillerytablet.mapcheck;

import net.minecraft.world.level.block.Blocks;
import net.nazarick.artillerytablet.terrain.TerrainTile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;

import static net.nazarick.artillerytablet.mapcheck.MapCheck.readField;
import static net.nazarick.artillerytablet.mapcheck.MapCheck.readInt;

/**
 * Renders the map's real shading path to a PNG, outside Minecraft, so it can be looked at.
 *
 * <p>This is the thing that was missing all along. A clean build proves the code compiles; a clean
 * boot proves it loads; neither runs a single line of the drawing. Every colour fault this project
 * has had — water in red, lava drawn as a lake, an ocean erupting into contour lines — is obvious in
 * one glance at a picture and invisible in a log.
 *
 * <p>It calls the production code rather than a copy of it: a synthetic tile goes into the real
 * cache, the real gather runs, and the real shade decides every pixel. Nothing here re-implements
 * anything, so it cannot drift away from what the game draws.
 *
 * <p>The one thing it cannot exercise is biome tint, which needs a running client to read the colour
 * maps. Those columns are marked as having no biome, which is the same path an unmapped biome takes
 * in play.
 */
final class TileRender {
    private static final int SIDE = TerrainTile.SIDE;

    private TileRender() {
    }

    static void run(File out) throws Exception {
        // Enough ground for the widest level to have something under all of it: the coarsest sheet
        // shown here covers a thousand blocks, and a sheet that runs off the edge of the data tells
        // you nothing except that the data ran out.
        for (int tz = -2; tz <= 66; tz++) {
            for (int tx = -2; tx <= 66; tx++) {
                accept(scene(tx, tz));
            }
        }

        int[] strides = {1, 4, 16, 64};
        int zoom = 4;
        int pane = SIDE * zoom;
        int gap = 10;
        BufferedImage sheet = new BufferedImage(pane * strides.length + gap * (strides.length - 1),
                pane, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < strides.length; i++) {
            BufferedImage one = render(strides[i]);
            int left = i * (pane + gap);
            for (int y = 0; y < pane; y++) {
                for (int x = 0; x < pane; x++) {
                    sheet.setRGB(left + x, y, one.getRGB(x / zoom, y / zoom));
                }
            }
        }

        ImageIO.write(sheet, "png", out);
        System.out.println();
        System.out.println("render: wrote " + out.getAbsolutePath()
                + "  (strides " + java.util.Arrays.toString(strides) + ", left to right)");
    }

    /**
     * A tile with the four things worth seeing at once: an ocean that deepens away from the shore,
     * a beach, a wooded hill, and a lava lake — the case that was drawn as water.
     */
    static TerrainTile scene(int tileX, int tileZ) {
        TerrainTile tile = new TerrainTile(tileX, tileZ);
        for (int z = 0; z < SIDE; z++) {
            for (int x = 0; x < SIDE; x++) {
                int worldX = tileX * SIDE + x;
                int worldZ = tileZ * SIDE + z;
                int i = TerrainTile.index(x, z);

                // A meandering coast with headlands and bays, hills inland, and a shelf that drops
                // away offshore.
                //
                // Built from value noise rather than from sines, and that matters for what this
                // harness is FOR. A sum of sines is a handful of pure tones, and downscaling a pure
                // tone produces moire — so the first version of this scene showed bold diagonal
                // banding at the coarse levels and looked exactly like a filtering bug. It was the
                // test that was wrong. Real ground is broadband, so the test ground must be too, or
                // it accuses the code of faults it does not have.
                double coast = 300 + 160 * ridged(worldZ * 0.004, 11.0);
                double land = (worldX - coast) * 0.09
                        + 26 * (noise(worldX * 0.011, worldZ * 0.011) - 0.5)
                        + 9 * (noise(worldX * 0.043, worldZ * 0.043) - 0.5)
                        + 3 * (noise(worldX * 0.17, worldZ * 0.17) - 0.5);
                int floor = (int) Math.round(62 + land);

                if (floor < 62) {
                    // Sea: the surface is level, the floor is not. This is the case that used to draw
                    // as one flat sheet of blue, and later as a contour map of the sea bed.
                    tile.height[i] = 62;
                    tile.depth[i] = (byte) Math.min(255, 62 - floor);
                    tile.block[i] = TerrainTile.idOf(Blocks.SAND);
                } else {
                    tile.height[i] = (short) floor;
                    tile.depth[i] = 0;
                    boolean lava = Math.hypot(worldX - 700, worldZ - 400) < 40;
                    boolean beach = floor < 65;
                    tile.block[i] = TerrainTile.idOf(lava ? Blocks.LAVA
                            : beach ? Blocks.SAND
                            : floor > 78 ? Blocks.STONE
                            : Blocks.OAK_LEAVES);
                    if (lava) {
                        tile.height[i] = 64;
                    }
                }
                tile.biome[i] = TerrainTile.NO_BIOME;
            }
        }
        return tile;
    }

    /** Smooth value noise in [0,1] from a hash — no periodicity for the filter to beat against. */
    private static double noise(double x, double z) {
        int x0 = (int) Math.floor(x);
        int z0 = (int) Math.floor(z);
        double fx = smooth(x - x0);
        double fz = smooth(z - z0);
        double top = lerp(hash(x0, z0), hash(x0 + 1, z0), fx);
        double bottom = lerp(hash(x0, z0 + 1), hash(x0 + 1, z0 + 1), fx);
        return lerp(top, bottom, fz);
    }

    /** One-dimensional noise, for the run of the coastline. */
    private static double ridged(double t, double seed) {
        return noise(t, seed);
    }

    private static double hash(int x, int z) {
        int h = x * 374761393 + z * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return ((h ^ (h >>> 16)) & 0xFFFF) / 65535.0;
    }

    private static double smooth(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Puts a tile into the real client cache, which also builds its real mip chain. */
    static void accept(TerrainTile tile) throws Exception {
        Class<?> cache = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainClientCache");
        Method accept = cache.getMethod("accept", TerrainTile.class, boolean.class);
        accept.invoke(null, tile, false);
    }

    /**
     * Runs the real gather and the real shade for one level, into an image.
     *
     * <p>Rewritten 2026-08-17 to follow the split into a background bake. The pair it calls used to
     * be {@code gather}/{@code shade} on a {@link TerrainImage} instance; they are now static and
     * take a workspace, because the heavy half runs off the render thread. Worth stating why this
     * check is being repaired rather than quietly updated: when those methods were renamed, nothing
     * failed to compile — this harness finds them <em>by name at run time</em> — so
     * {@code ./gradlew mapCheck} died at this line and took the four checks after it with it. The
     * picture, the patch check, the store round trip and the splash had not run since.
     */
    private static BufferedImage render(int stride) throws Exception {
        Class<?> imageClass = Class.forName("net.nazarick.artillerytablet.client.terrain.TerrainImage");
        Class<?> workspaceClass = Class.forName(
                "net.nazarick.artillerytablet.client.terrain.TerrainImage$BakeWorkspace");
        java.lang.reflect.Constructor<?> newWorkspace = workspaceClass.getDeclaredConstructor();
        newWorkspace.setAccessible(true);
        Object ws = newWorkspace.newInstance();

        Method gather = imageClass.getDeclaredMethod("gatherInto", workspaceClass,
                int.class, int.class, int.class, int.class, int.class, int.class, int.class);
        Method shade = imageClass.getDeclaredMethod("shadeCell", workspaceClass,
                int.class, int.class);
        gather.setAccessible(true);
        shade.setAccessible(true);

        int margin = readInt(imageClass, "MARGIN");
        short[] floor = (short[]) readField(workspaceClass, ws, "fieldFloor");

        int field = readInt(imageClass, "FIELD");
        gather.invoke(null, ws, 0, 0, stride, 0, 0, field - 1, field - 1);

        BufferedImage out = new BufferedImage(SIDE, SIDE, BufferedImage.TYPE_INT_RGB);
        for (int z = 0; z < SIDE; z++) {
            for (int x = 0; x < SIDE; x++) {
                int cell = (z + margin) * field + x + margin;
                if (floor[cell] == TerrainTile.NO_DATA) {
                    out.setRGB(x, z, 0x101010);
                    continue;
                }
                int packed = (int) shade.invoke(null, ws, cell, stride);
                // The texture order the palette hands back, turned into the order an image wants.
                int b = (packed >> 16) & 0xFF;
                int g = (packed >> 8) & 0xFF;
                int r = packed & 0xFF;
                out.setRGB(x, z, (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }
}
