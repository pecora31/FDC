package net.nazarick.artillerytablet;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.nazarick.artillerytablet.init.ModItems;
import net.nazarick.artillerytablet.init.ModSounds;
import net.nazarick.artillerytablet.init.ModTabs;
import net.nazarick.artillerytablet.network.ModNetwork;
import org.slf4j.Logger;

@Mod(ArtilleryTablet.MODID)
public class ArtilleryTablet {
    public static final String MODID = "artillerytablet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ArtilleryTablet() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(bus);
        ModTabs.TABS.register(bus);
        ModSounds.SOUNDS.register(bus);

        bus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Deferred to the main thread, and it should stay that way even now that the channel is our
        // own. Common setup runs for every mod IN PARALLEL on a worker pool; when these packets rode
        // on SuperbWarfare's channel, both mods wrote into one registry from two threads and the
        // game died on a torn array. Registering our own channel from a worker is safe today, but
        // the habit is what keeps it safe, and enqueueWork costs nothing.
        event.enqueueWork(ModNetwork::register);
    }
}
