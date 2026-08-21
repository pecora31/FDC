package net.nazarick.artillerytablet.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.nazarick.artillerytablet.ArtilleryTablet;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArtilleryTablet.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.artillerytablet"))
                    .icon(() -> new ItemStack(ModItems.ARTILLERY_TACTICAL_TABLET.get()))
                    .displayItems((params, output) -> output.accept(ModItems.ARTILLERY_TACTICAL_TABLET.get()))
                    .build());
}
