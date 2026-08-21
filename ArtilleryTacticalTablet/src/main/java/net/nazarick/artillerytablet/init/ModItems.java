package net.nazarick.artillerytablet.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nazarick.artillerytablet.ArtilleryTablet;
import net.nazarick.artillerytablet.item.ArtilleryTacticalTabletItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArtilleryTablet.MODID);

    public static final RegistryObject<Item> ARTILLERY_TACTICAL_TABLET = ITEMS.register("artillery_tactical_tablet",
            () -> new ArtilleryTacticalTabletItem(new Item.Properties().stacksTo(1)));
}
