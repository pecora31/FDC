package net.nazarick.artillerytablet.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    /**
     * The screen-edge readout is not registered at present.
     *
     * <p>It was carrying too much — bound guns, mode, arc, the whole target list — all of which the
     * tablet says better when it is open. The one part worth having outside the tablet is the count
     * down to impact, because after firing you close the device to watch the ground. That is what
     * this should come back as, rather than the panel it had grown into.
     *
     * <p>{@code TabletHudOverlay} is deliberately left in place. It also drew in device pixels, so
     * whatever returns has to be rewritten for interface pixels anyway, and the countdown logic in
     * it is worth keeping as the starting point.
     */
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
    }
}
