package com.github.sculkhorde.core;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, SculkHorde.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RAID_START_SOUND = registerSoundEvent("raid_start_sound");
    public static final DeferredHolder<SoundEvent, SoundEvent> HORDE_START_SOUND = registerSoundEvent("horde_start_sound");

    public static final DeferredHolder<SoundEvent, SoundEvent> RAID_SCOUT_SOUND = registerSoundEvent("raid_scout_sound");
    public static final DeferredHolder<SoundEvent, SoundEvent> NODE_SPAWN_SOUND = registerSoundEvent("node_spawn_sound");
    public static final DeferredHolder<SoundEvent, SoundEvent> NODE_DESTROY_SOUND = registerSoundEvent("node_destroy_sound");

    public static final DeferredHolder<SoundEvent, SoundEvent> DEEP_GREEN = registerSoundEvent("deep_green");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLIND_AND_ALONE = registerSoundEvent("blind_and_alone");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_IDLE = registerSoundEvent("sculk_enderman_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_PORTAL = registerSoundEvent("sculk_enderman_portal");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_SCREAM = registerSoundEvent("sculk_enderman_scream");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_DEATH = registerSoundEvent("sculk_enderman_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_HIT = registerSoundEvent("sculk_enderman_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ENDERMAN_STARE = registerSoundEvent("sculk_enderman_stare");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HARVESTER_ITEM_INSERTED = registerSoundEvent("soul_harvester_item_inserted");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HARVESTER_FINISHED = registerSoundEvent("soul_harvester_finished");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HARVESTER_ACTIVE = registerSoundEvent("soul_harvester_active");
    public static final DeferredHolder<SoundEvent, SoundEvent> ENDER_BUBBLE_LOOP = registerSoundEvent("ender_bubble_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ZOMBIE_IDLE = registerSoundEvent("sculk_zombie_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ZOMBIE_HURT = registerSoundEvent("sculk_zombie_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_ZOMBIE_DEATH = registerSoundEvent("sculk_zombie_death");

    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_VINDICATOR_IDLE = registerSoundEvent("sculk_vindicator_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_VINDICATOR_HURT = registerSoundEvent("sculk_vindicator_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_VINDICATOR_DEATH = registerSoundEvent("sculk_vindicator_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BURROWED_BURST = registerSoundEvent("burrowed_burst");

    public static final DeferredHolder<SoundEvent, SoundEvent> ZOLTRAAK_ATTACK = registerSoundEvent("zoltraak");

    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_MITE_IDLE = registerSoundEvent("sculk_mite_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_MITE_HURT = registerSoundEvent("sculk_mite_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_MITE_DEATH = registerSoundEvent("sculk_mite_death");

    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_RAVAGER_AMBIENT = registerSoundEvent("sculk_ravager_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_RAVAGER_HURT = registerSoundEvent("sculk_ravager_hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_RAVAGER_DEATH = registerSoundEvent("sculk_ravager_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_RAVAGER_BITE = registerSoundEvent("sculk_ravager_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_RAVAGER_SCREECH = registerSoundEvent("sculk_ravager_screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOULITE_AMBIENCE = registerSoundEvent("soulite_ambience");
    public static final DeferredHolder<SoundEvent, SoundEvent> INFESTATION_AMBIENCE = registerSoundEvent("infestation_ambience");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SPEAR_EMERGE = registerSoundEvent("soul_spear_emerge");

    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_GHAST_CHARGE = registerSoundEvent("sculk_ghast_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_GHAST_DEATH = registerSoundEvent("sculk_ghast_death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_GHAST_MOAN = registerSoundEvent("sculk_ghast_moan");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_GHAST_SCREAM = registerSoundEvent("sculk_ghast_scream");

    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_POD_OPEN = registerSoundEvent("sculk_pod_open");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_BROOD_FLY = registerSoundEvent("sculk_brood_fly_loop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_BROOD_FLY_START = registerSoundEvent("sculk_brood_fly_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_BROOD_IDLE = registerSoundEvent("sculk_brood_idle");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCULK_BROOD_HURT = registerSoundEvent("sculk_brood_hurt");


    private static DeferredHolder<SoundEvent, SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
