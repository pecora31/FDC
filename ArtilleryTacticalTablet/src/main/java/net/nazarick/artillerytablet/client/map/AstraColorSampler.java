package net.nazarick.artillerytablet.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * High-Performance Color Sampler & Military Topographic Hillshading Engine.
 * - Extracts accurate top-facing block and biome colors.
 * - Computes realistic northwest 45-degree sun slope hillshading (shaded relief).
 * - Generates crisp military contour elevation lines (10m / 25m / 50m intervals).
 * - Simulates bathymetric water depth shading and tactical hazard warnings (lava).
 */
@OnlyIn(Dist.CLIENT)
public final class AstraColorSampler {

    // Sun illumination direction vector for hillshading (Standard Military Topo Map: 315° NW, 45° Altitude)
    private static final double SUN_DX = -0.7071; // West
    private static final double SUN_DZ = -0.7071; // North

    // Contour Line Settings
    public static final int CONTOUR_INTERVAL_MINOR = 10; // Every 10 blocks (meters)
    public static final int CONTOUR_INTERVAL_MAJOR = 25; // Every 25 blocks (meters)

    private AstraColorSampler() {}

    /**
     * Samples the final 32-bit ARGB color for a single surface block column at (x, z),
     * including block texture, biome tint, hillshading, water depth, and contour lines.
     */
    public static int sampleColumn(BlockGetter level, int x, int y, int z,
                                   int westY, int northY, int northWestY,
                                   Biome biome, boolean enableHillshading, boolean enableContours) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);
        FluidState fluid = level.getFluidState(pos);

        // 1. Base Block Color
        int baseColor = getBaseColor(state, fluid, biome, pos);

        // 2. Tactical Hazard Overrides
        if (state.is(Blocks.LAVA) || fluid.is(Fluids.LAVA) || state.is(Blocks.MAGMA_BLOCK)) {
            return 0xFFFF5500; // Bright Tactical Lava Amber
        }

        // 3. Water Bathymetry (Depth Shading)
        if (fluid.is(Fluids.WATER) || state.is(Blocks.WATER)) {
            baseColor = computeWaterDepthColor(level, x, y, z, baseColor);
        }

        // 4. Military Topographic Hillshading (Slope Relief Lighting)
        if (enableHillshading && !fluid.is(Fluids.WATER)) {
            baseColor = applySlopeHillshading(baseColor, y, westY, northY, northWestY);
        }

        // 5. Military Contour Lines
        if (enableContours && !fluid.is(Fluids.WATER)) {
            baseColor = applyContourLine(baseColor, y);
        }

        return baseColor;
    }

    /**
     * Extracts base RGB color from BlockState, Biome, or MapColor.
     */
    public static int getBaseColor(BlockState state, FluidState fluid, Biome biome, BlockPos pos) {
        if (state.isAir()) {
            return 0xFF080A0E; // Dark Void
        }

        // Check if fluid
        if (!fluid.isEmpty()) {
            if (fluid.is(Fluids.WATER)) {
                int waterColor = (biome != null) ? biome.getWaterColor() : 0x3F76E4;
                return 0xFF000000 | waterColor;
            }
            if (fluid.is(Fluids.LAVA)) {
                return 0xFFFF5500;
            }
        }

        // Biome tinted blocks (Grass, Leaves, Vines)
        if (state.is(Blocks.GRASS_BLOCK)) {
            int grassColor = (biome != null) ? biome.getGrassColor(pos.getX(), pos.getZ()) : 0x7CBD6B;
            return 0xFF000000 | grassColor;
        }
        if (state.is(Blocks.OAK_LEAVES) || state.is(Blocks.JUNGLE_LEAVES) || state.is(Blocks.ACACIA_LEAVES) || state.is(Blocks.DARK_OAK_LEAVES)) {
            int foliageColor = (biome != null) ? biome.getFoliageColor() : 0x48B518;
            return 0xFF000000 | foliageColor;
        }
        if (state.is(Blocks.BIRCH_LEAVES)) {
            return 0xFF80A755;
        }
        if (state.is(Blocks.SPRUCE_LEAVES)) {
            return 0xFF619961;
        }

        // MapColor fallback for standard solid blocks (Stone, Dirt, Sand, Concrete, Wood...)
        MapColor mapColor = state.getMapColor(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (mapColor != null && mapColor.col != 0) {
            return 0xFF000000 | mapColor.col;
        }

        return 0xFF5A6472; // Neutral tactical slate default
    }

    /**
     * Applies NW 45° slope lighting (Hillshading) based on elevation difference.
     */
    public static int applySlopeHillshading(int argb, int currentY, int westY, int northY, int northWestY) {
        // Compute elevation gradient against NW sun
        double dz = (currentY - northY);
        double dx = (currentY - westY);
        double dnw = (currentY - northWestY) * 0.7071;

        double slope = (dx * SUN_DX + dz * SUN_DZ + dnw) * 0.35;

        // Brightness multiplier: 1.0 is flat ground, >1.0 is sunlit slope, <1.0 is shadow slope
        double factor = 1.0 + Math.max(-0.40, Math.min(0.30, slope * 0.15));

        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;

        r = Math.min(255, Math.max(0, (int) (r * factor)));
        g = Math.min(255, Math.max(0, (int) (g * factor)));
        b = Math.min(255, Math.max(0, (int) (b * factor)));

        return (0xFF000000) | (r << 16) | (g << 8) | b;
    }

    /**
     * Applies subtle military contour elevation lines.
     */
    public static int applyContourLine(int argb, int y) {
        int absY = Math.abs(y);
        boolean isMajor = (absY % CONTOUR_INTERVAL_MAJOR == 0);
        boolean isMinor = (absY % CONTOUR_INTERVAL_MINOR == 0);

        if (isMajor) {
            // Darken slightly to form crisp major contour line
            int r = Math.max(0, ((argb >> 16) & 0xFF) - 45);
            int g = Math.max(0, ((argb >> 8) & 0xFF) - 45);
            int b = Math.max(0, (argb & 0xFF) - 45);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else if (isMinor) {
            // Subtle minor contour line
            int r = Math.max(0, ((argb >> 16) & 0xFF) - 22);
            int g = Math.max(0, ((argb >> 8) & 0xFF) - 22);
            int b = Math.max(0, (argb & 0xFF) - 22);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }

        return argb;
    }

    /**
     * Computes water bathymetry (shallow water is brighter, deep water is dark navy).
     */
    private static int computeWaterDepthColor(BlockGetter level, int x, int surfaceY, int z, int baseWaterColor) {
        int depth = 1;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos(x, surfaceY - 1, z);

        while (depth < 16 && level.getFluidState(mpos).is(Fluids.WATER)) {
            depth++;
            mpos.setY(surfaceY - depth);
        }

        // Deeper water scales down to deep tactical navy
        float depthRatio = Math.min(1.0f, depth / 12.0f);
        int r = (baseWaterColor >> 16) & 0xFF;
        int g = (baseWaterColor >> 8) & 0xFF;
        int b = baseWaterColor & 0xFF;

        r = (int) (r * (1.0f - depthRatio * 0.55f));
        g = (int) (g * (1.0f - depthRatio * 0.50f));
        b = (int) (b * (1.0f - depthRatio * 0.30f));

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static final class EmptyBlockGetter implements BlockGetter {
        static final EmptyBlockGetter INSTANCE = new EmptyBlockGetter();
        @Override public net.minecraft.world.level.block.entity.BlockEntity getBlockEntity(BlockPos pos) { return null; }
        @Override public BlockState getBlockState(BlockPos pos) { return Blocks.AIR.defaultBlockState(); }
        @Override public FluidState getFluidState(BlockPos pos) { return Fluids.EMPTY.defaultFluidState(); }
        @Override public int getHeight() { return 0; }
        @Override public int getMinBuildHeight() { return 0; }
    }
}
