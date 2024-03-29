package com.github.sculkhorde.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, SculkHorde.MOD_ID);

    public static final RegistryObject<SoundEvent> RAID_START_SOUND = registerSoundEvent("raid_start_sound");
    public static final RegistryObject<SoundEvent> HORDE_START_SOUND = registerSoundEvent("horde_start_sound");

    public static final RegistryObject<SoundEvent> RAID_SCOUT_SOUND = registerSoundEvent("raid_scout_sound");
    public static final RegistryObject<SoundEvent> NODE_SPAWN_SOUND = registerSoundEvent("node_spawn_sound");
    public static final RegistryObject<SoundEvent> NODE_DESTROY_SOUND = registerSoundEvent("node_destroy_sound");


    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(SculkHorde.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
