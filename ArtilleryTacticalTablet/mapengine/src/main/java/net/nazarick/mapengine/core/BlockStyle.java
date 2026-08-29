package net.nazarick.mapengine.core;

/**
 * What a column looks like before relief shading, and what marks it out as a hazard. The engine
 * stores ids; this turns one into a base colour.
 *
 * <p>The second seam that keeps Minecraft out of the engine — resolving a block id to a colour means
 * reading baked models and the texture atlas, and a biome id to a tint means reaching into the level.
 * Both are the game's business and both are things a background thread must not touch while a
 * resource reload is swapping them out.
 *
 * <p><b>Relief/hillshade is deliberately not this interface's job.</b> It depends on a column's
 * neighbours and on the zoom level, and the rasteriser is the one place already holding both —
 * folding it in here would mean resolving a colour once per neighbour instead of once per column.
 * See {@code Rasterizer} for where the shading this project's map is known for actually happens.
 *
 * <p>Implementations must be safe to call from a background thread and must not block. The mod's
 * implementation answers from tables warmed on the render thread as ground arrives.
 */
public interface BlockStyle {
    /**
     * The colour of one column, packed 0xAARRGGBB, before relief shading. Alpha zero means "draw
     * nothing here" and is how unsurveyed ground stays visibly unknown.
     *
     * @param waterTint the blended water colour for this column, or 0 where it has none — handed in
     *                  rather than looked up because it depends on the column's neighbours, which
     *                  only the caller is holding
     */
    int columnColour(short block, short biome, int waterDepth, int waterTint, short surfaceHeight);

    /** Whether this block is one the map must never let the eye slide over, such as lava. */
    boolean isHazard(short block);

    /** This biome's water colour, packed 0x00RRGGBB, for the caller to blend across neighbours. */
    int waterTint(short biome);
}
