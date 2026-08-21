package net.nazarick.artillerytablet.client.screen.uitest;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.nazarick.artillerytablet.ArtilleryTablet;

/**
 * F6 opens {@link TacticalTabletScreen} directly, bypassing the real tablet item.
 * Preview-only wiring for the standalone mockup in {@code screen/uitest} — not part of the
 * product tablet's open path.
 */
@Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class UiTestKeybind {
    public static final KeyMapping OPEN_UI_TEST = new KeyMapping(
        "key.artillerytablet.uitest_open",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        InputConstants.KEY_F6,
        "key.categories.artillerytablet"
    );

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_UI_TEST);
    }

    @Mod.EventBusSubscriber(modid = ArtilleryTablet.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Handler {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen == null && OPEN_UI_TEST.consumeClick()) {
                mc.setScreen(new TacticalTabletScreen());
            }
        }
    }
}
