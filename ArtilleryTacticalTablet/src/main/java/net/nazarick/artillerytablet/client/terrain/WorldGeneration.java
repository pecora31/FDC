package net.nazarick.artillerytablet.client.terrain;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Which world and dimension the client is in, as one number that bumps whenever either changes.
 *
 * <p>Split out from the old tile-caching layer this mod used to have, because "has the world
 * changed" turned out to be a question more than one thing needs answered independently of whether
 * that old cache still exists — {@code FireLineCache} (ballistics) and the map panel's own boot latch
 * both watch this, and neither one is otherwise about terrain tiles at all.
 *
 * <p>Block and biome ids are handed out by the server per session, so anything that resolved a
 * colour or a tint from one has to forget it here too — {@link BlockPalette#forget} and
 * {@link TerrainMips#forgetBiomes} stay wired into this exact boundary for that reason, regardless
 * of which renderer is asking them to resolve an id.
 */
@OnlyIn(Dist.CLIENT)
public final class WorldGeneration {
    private WorldGeneration() {
    }

    private static String world;

    /** Bumped whenever the world changes, so anything holding rendered ground knows to discard it. */
    private static int generation;

    private static String worldIdentity(Minecraft mc) {
        if (mc.level == null) {
            return null;
        }
        return mc.level.dimension().location().toString() + "@" + System.identityHashCode(mc.level);
    }

    /** Drops per-session lookups on a change of world or dimension. Safe to call every frame. */
    public static void checkWorld() {
        Minecraft mc = Minecraft.getInstance();
        String now = worldIdentity(mc);
        if (now == null ? world == null : now.equals(world)) {
            return;
        }
        world = now;
        BlockPalette.forget();
        TerrainMips.forgetBiomes();
        generation++;
    }

    public static int generation() {
        return generation;
    }
}
