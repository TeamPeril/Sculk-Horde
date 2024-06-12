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

    public static final RegistryObject<SoundEvent> DEEP_GREEN = registerSoundEvent("deep_green");
    public static final RegistryObject<SoundEvent> BLIND_AND_ALONE = registerSoundEvent("blind_and_alone");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_IDLE = registerSoundEvent("sculk_enderman_idle");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_PORTAL = registerSoundEvent("sculk_enderman_portal");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_SCREAM = registerSoundEvent("sculk_enderman_scream");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_DEATH = registerSoundEvent("sculk_enderman_death");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_HIT = registerSoundEvent("sculk_enderman_hit");
    public static final RegistryObject<SoundEvent> SCULK_ENDERMAN_STARE = registerSoundEvent("sculk_enderman_stare");
    public static final RegistryObject<SoundEvent> SOUL_HARVESTER_ITEM_INSERTED = registerSoundEvent("soul_harvester_item_inserted");
    public static final RegistryObject<SoundEvent> SOUL_HARVESTER_FINISHED = registerSoundEvent("soul_harvester_finished");
    public static final RegistryObject<SoundEvent> SOUL_HARVESTER_ACTIVE = registerSoundEvent("soul_harvester_active");
    public static final RegistryObject<SoundEvent> ENDER_BUBBLE_LOOP = registerSoundEvent("ender_bubble_loop");


    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(SculkHorde.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
