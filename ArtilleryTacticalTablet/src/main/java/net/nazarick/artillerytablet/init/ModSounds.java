package net.nazarick.artillerytablet.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.nazarick.artillerytablet.ArtilleryTablet;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArtilleryTablet.MODID);

    public static final RegistryObject<SoundEvent> TACTICAL_KEY_PRESS = SOUNDS.register("tactical_key_press",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArtilleryTablet.MODID, "tactical_key_press")));

    public static final RegistryObject<SoundEvent> TACTICAL_KEY_RELEASE = SOUNDS.register("tactical_key_release",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArtilleryTablet.MODID, "tactical_key_release")));
}
