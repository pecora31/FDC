package net.nazarick.artillerytablet.client.terrain.mapengine;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.nazarick.artillerytablet.client.terrain.BlockPalette;
import net.nazarick.artillerytablet.client.terrain.TerrainMips;
import net.nazarick.mapengine.core.BlockStyle;

/**
 * Resolves {@code mapengine}'s colours from the mod's existing palette ({@link BlockPalette}) and
 * ground-colour maths ({@link TerrainMips#groundColour}) — reused, not reimplemented, so the new
 * engine's satellite view looks like the same map this mod has always drawn.
 *
 * <p><b>Byte order.</b> {@link BlockPalette}/{@link TerrainMips} pack colours as
 * {@code 0xAABBGGRR} — chosen there to match {@code NativeImage}'s own pixel order. {@code mapengine}
 * expects standard ARGB, {@code 0xAARRGGBB} (see {@code mapengine.raster.Rasterizer}'s own doc and
 * {@code DebugStyle}, its reference implementation). Every colour crossing this bridge has its red
 * and blue bytes swapped for exactly that reason — get this wrong and the map renders with the sky
 * and the grass wearing each other's colours.
 *
 * <p><b>Coarser colour until prewarmed.</b> {@link BlockPalette#colourOf} and
 * {@link TerrainMips#groundColour}/{@link TerrainMips#waterTint} both need render-thread access
 * (baked models, the texture atlas, biome registry lookups) to resolve a <em>real</em> colour, and
 * both already have a safe fallback for being asked before that happens —
 * {@code state.getMapColor(...)}'s flat colour, not a crash. This bridge does not yet call
 * {@code BlockPalette.prewarm}/{@code TerrainMips.prewarmBiome} itself (a deliberate, called-out
 * scope cut — see the plan this was built against), so until something else in the pipeline warms a
 * given block/biome id, its colour here is that coarser fallback rather than the true texture-sampled
 * one. Real, not broken: every id still resolves to a real colour, just not the sharpest one yet.
 */
@OnlyIn(Dist.CLIENT)
public final class ForgeBlockStyle implements BlockStyle {

    @Override
    public int columnColour(short block, short biome, int waterDepth, int waterTint, short surfaceHeight) {
        int blockId = block & 0xFFFF;
        // waterTint arrives here in mapengine's order — it is whatever this class's own waterTint(),
        // below, just returned, since Rasterizer.shadeCell calls that and passes the result straight
        // into this method. TerrainMips.groundColour needs it back in this mod's own native order, or
        // its internal blend reads the wrong two bytes for "blue" and "red" — the exact bug that
        // turned every body of water brown until this line existed: the swap was being applied once
        // on the way out of waterTint() and never undone on the way back in here.
        int modOrderTint = swapRedBlue(waterTint);
        int packed = TerrainMips.groundColour(blockId, biome, waterDepth, modOrderTint, surfaceHeight);
        return swapRedBlue(packed);
    }

    @Override
    public boolean isHazard(short block) {
        return BlockPalette.isHazard(block & 0xFFFF);
    }

    @Override
    public int waterTint(short biome) {
        return swapRedBlue(TerrainMips.waterTint(biome));
    }

    /**
     * {@code 0xAABBGGRR} (this mod's own order) &harr; {@code 0xAARRGGBB} (mapengine's). Package-
     * visible so {@link MapEngineOverlay} can apply the same swap in the other direction when it
     * uploads a rasterized region to a {@code NativeImage}, which wants this mod's native order.
     */
    static int swapRedBlue(int packed) {
        int a = packed & 0xFF000000;
        int high = (packed >> 16) & 0xFF; // "b" slot in this mod's own packing
        int mid = packed & 0x0000FF00;
        int low = packed & 0xFF; // "r" slot in this mod's own packing
        return a | (low << 16) | mid | high;
    }
}
