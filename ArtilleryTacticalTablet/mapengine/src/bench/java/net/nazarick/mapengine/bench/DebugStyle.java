package net.nazarick.mapengine.bench;

import net.nazarick.mapengine.core.BlockStyle;
import net.nazarick.mapengine.core.ColumnBuffer;

/**
 * A plain style for the benchmarks, so the engine can be looked at before the mod's real one exists.
 *
 * <p>Deliberately not the mod's palette. This one answers from the synthetic world's own handful of
 * ids and nothing else, which is what lets a benchmark render a picture without a texture atlas, a
 * biome registry or a game.
 */
final class DebugStyle implements BlockStyle {
    static final int UNSURVEYED = 0;

    @Override
    public int columnColour(short block, short biome, int waterDepth, int waterTint, short surfaceHeight) {
        if (waterDepth > 0) {
            float murk = Math.min(1f, waterDepth / 12f);
            int r = (int) (58 - 34 * murk);
            int g = (int) (122 - 66 * murk);
            int b = (int) (176 - 68 * murk);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        int base = switch (block) {
            case SyntheticWorld.BLOCK_SAND -> 0xD8CFA0;
            case SyntheticWorld.BLOCK_STONE -> 0x8A8A8A;
            case SyntheticWorld.BLOCK_SNOW -> 0xF2F4F6;
            case SyntheticWorld.BLOCK_GRAVEL -> 0x9B9384;
            default -> 0x5A8F42;
        };
        if (block == SyntheticWorld.BLOCK_GRASS) {
            float t = Math.min(1f, Math.max(0f, (surfaceHeight - SyntheticWorld.SEA_LEVEL) / 60f));
            int r = (int) (0x5A + t * 0x35);
            int g = (int) (0x8F - t * 0x18);
            int b = (int) (0x42 + t * 0x10);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
        return 0xFF000000 | base;
    }

    @Override
    public boolean isHazard(short block) {
        return false;
    }

    @Override
    public int waterTint(short biome) {
        return switch (biome) {
            case 0 -> 0x3D6FB0;
            case 1 -> 0x3A7FA8;
            case 2 -> 0x2F86A0;
            default -> 0x357CAC;
        };
    }

    static int colourOf(DebugStyle style, ColumnBuffer columns, int index) {
        short height = columns.height[index];
        if (height == ColumnBuffer.NO_DATA) {
            return UNSURVEYED;
        }
        return style.columnColour(columns.block[index], columns.biome[index],
                columns.depthAt(index), style.waterTint(columns.biome[index]), height);
    }
}
