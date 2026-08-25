package net.nazarick.artillerytablet.client.map;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Master Controller & Coordinator for the ASTRA Tactical Map Subsystem.
 * - Handles Instant Loading, Pre-Warming, and Lifecycle management.
 * - Bridges Minecraft Client World and the hardware-accelerated AstraTacticalCanvas.
 */
@OnlyIn(Dist.CLIENT)
public final class AstraMapEngine {

    private static final AstraMapEngine INSTANCE = new AstraMapEngine();
    public static AstraMapEngine instance() { return INSTANCE; }

    private final AstraTacticalCanvas canvas = new AstraTacticalCanvas();
    private boolean initialized = false;

    private AstraMapEngine() {}

    public AstraTacticalCanvas getCanvas() {
        return canvas;
    }

    /**
     * Called when opening the tablet to guarantee 0ms instant display.
     */
    public void onOpenTablet(Level level) {
        if (level == null) return;
        AstraTileCache.instance().checkWorld(level);

        Player player = Minecraft.getInstance().player;
        if (player != null && !initialized) {
            // First open: Center camera on player coordinates
            canvas.centerOn(player.getX(), player.getZ());
            initialized = true;
        }

        // Pre-warm the surrounding 3x3 regions in background thread
        preWarmSurroundingRegions(level, canvas.getCenterBlockX(), canvas.getCenterBlockZ());
    }

    /**
     * Periodic background tick to pre-bake regions while player travels.
     */
    public void tick(Level level) {
        if (level == null) return;
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            preWarmSurroundingRegions(level, player.getX(), player.getZ());
        }
    }

    private void preWarmSurroundingRegions(Level level, double worldX, double worldZ) {
        int currentRx = (int) Math.floor(worldX / AstraTileCache.TILE_SIZE);
        int currentRz = (int) Math.floor(worldZ / AstraTileCache.TILE_SIZE);

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                AstraTileCache.instance().getTileTexture(currentRx + dx, currentRz + dz, level);
            }
        }
    }

    public void centerOnPlayer() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            canvas.centerOn(player.getX(), player.getZ());
        }
    }

    public void centerOn(double x, double z) {
        canvas.centerOn(x, z);
    }
}
