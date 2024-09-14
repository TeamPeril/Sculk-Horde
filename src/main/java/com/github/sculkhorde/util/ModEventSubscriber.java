package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.*;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        //Add entries to the entity factory (please add them in order of cost, I don't want to sort)
        EntityFactoryEntry[] entries = {
                new EntityFactoryEntry(ModEntities.SCULK_SPORE_SPEWER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkSporeSpewerEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(EntityFactoryEntry.StrategicValues.Infector, EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_PHANTOM.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Mature)
                        .setCost((int) SculkPhantomEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass, ReinforcementRequest.senderType.Raid)
                        .addStrategicValues(
                            EntityFactoryEntry.StrategicValues.Infector,
                            EntityFactoryEntry.StrategicValues.Melee,
                            EntityFactoryEntry.StrategicValues.EffectiveOnGround,
                            EntityFactoryEntry.StrategicValues.EffectiveInSkies),

                new EntityFactoryEntry(ModEntities.SCULK_RAVAGER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkRavagerEntity.MAX_HEALTH)
                        .setLimit(1)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(
                            EntityFactoryEntry.StrategicValues.Combat,
                            EntityFactoryEntry.StrategicValues.Tank,
                            EntityFactoryEntry.StrategicValues.Melee,
                            EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_HATCHER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkHatcherEntity.MAX_HEALTH)
                        .setExplicitlyDeniedSenders(ReinforcementRequest.senderType.SculkMass)
                        .addStrategicValues(
                            EntityFactoryEntry.StrategicValues.Combat,
                            EntityFactoryEntry.StrategicValues.Melee,
                            EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_CREEPER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost(20)
                        .addStrategicValues(
                            EntityFactoryEntry.StrategicValues.Combat,
                            EntityFactoryEntry.StrategicValues.Melee,
                            EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_SPITTER.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(20)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Ranged,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_ZOMBIE.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(20)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_VINDICATOR.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkVindicatorEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_MITE_AGGRESSOR.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost(6)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Combat,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_MITE.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkMiteEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Infector,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.EffectiveOnGround),

                new EntityFactoryEntry(ModEntities.SCULK_SALMON.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkSalmonEntity.MAX_HEALTH)
                        .addStrategicValues(
                        EntityFactoryEntry.StrategicValues.Infector,
                        EntityFactoryEntry.StrategicValues.Melee,
                        EntityFactoryEntry.StrategicValues.Aquatic)
                        .enableExperimentalMode(ModConfig.SERVER.experimental_features_enabled),

                new EntityFactoryEntry(ModEntities.SCULK_SQUID.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Undeveloped)
                        .setCost((int) SculkSquidEntity.MAX_HEALTH)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Infector,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .enableExperimentalMode(ModConfig.SERVER.experimental_features_enabled),

                new EntityFactoryEntry(ModEntities.SCULK_PUFFERFISH.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Immature)
                        .setCost((int) SculkPufferfishEntity.MAX_HEALTH)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Support,
                                EntityFactoryEntry.StrategicValues.Combat,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.Aquatic)
                        .enableExperimentalMode(ModConfig.SERVER.experimental_features_enabled),

                new EntityFactoryEntry(ModEntities.SCULK_WITCH.get())
                        .setMinEvolutionRequired(Gravemind.evolution_states.Mature)
                        .setCost((int) SculkWitchEntity.MAX_HEALTH)
                        .setChanceToSpawn(0.5F)
                        .setLimit(2)
                        .addStrategicValues(
                                EntityFactoryEntry.StrategicValues.Support,
                                EntityFactoryEntry.StrategicValues.Combat,
                                EntityFactoryEntry.StrategicValues.Melee,
                                EntityFactoryEntry.StrategicValues.EffectiveOnGround),
        };

        SculkHorde.entityFactory.addEntriesToFactory(entries);

        BlockInfestationHelper.initializeInfestationTables();

        SculkHorde.randomSculkFlora = new PoolBlocks();
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_CATALYST, 1);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SUMMONER_BLOCK.get(), 2);
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_SENSOR, 3);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SPIKE.get(), 4);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SMALL_SHROOM.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SHROOM_CULTURE.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS_SHORT.get(), 200);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS.get(), 200);

        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.SCULK_MITE.get(),
                    SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SculkMiteEntity::additionalSpawnCheck);
            afterCommonSetup();
        });
    }

    // runs on main thread after common setup event
    // adding things to unsynchronized registries (i.e. most vanilla registries) can be done here
    private static void afterCommonSetup()
    {
        CriteriaTriggers.register(GravemindEvolveImmatureTrigger.INSTANCE);
        CriteriaTriggers.register(GravemindEvolveMatureTrigger.INSTANCE);
        CriteriaTriggers.register(SculkHordeStartTrigger.INSTANCE);
        CriteriaTriggers.register(SculkNodeSpawnTrigger.INSTANCE);
        CriteriaTriggers.register(SoulHarvesterTrigger.INSTANCE);
        CriteriaTriggers.register(SculkHordeDefeatTrigger.INSTANCE);
        CriteriaTriggers.register(ContributeTrigger.INSTANCE);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPITTER.get(), SculkSpitterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_INFECTOR.get(), SculkBeeInfectorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_HARVESTER.get(), SculkBeeHarvesterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_HATCHER.get(), SculkHatcherEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPORE_SPEWER.get(), SculkSporeSpewerEntity.createAttributes().build());
        event.put(ModEntities.SCULK_RAVAGER.get(), SculkRavagerEntity.createAttributes().build());
        event.put(ModEntities.INFESTATION_PURIFIER.get(), InfestationPurifierEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VINDICATOR.get(), SculkVindicatorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_CREEPER.get(), SculkCreeperEntity.createAttributes().build());
        event.put(ModEntities.SCULK_ENDERMAN.get(), SculkEndermanEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM.get(), SculkPhantomEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM_CORPSE.get(), SculkPhantomCorpseEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SALMON.get(), SculkSalmonEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SQUID.get(), SculkSquidEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PUFFERFISH.get(), SculkPufferfishEntity.createAttributes().build());
        event.put(ModEntities.SCULK_WITCH.get(), SculkWitchEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SOUL_REAPER.get(), SculkSoulReaperEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VEX.get(), SculkVexEntity.createAttributes().build());
        event.put(ModEntities.LIVING_ARMOR.get(), LivingArmorEntity.createAttributes().build());
    }
}

