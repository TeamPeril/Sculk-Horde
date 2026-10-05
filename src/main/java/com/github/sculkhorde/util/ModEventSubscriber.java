package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.LivingArmorEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModPotions;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        EntityFactory.initialize();
        BlockInfestationSystem.initialize();
    }

    @SubscribeEvent
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.SCULK_MITE.get(),
                SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                SculkMiteEntity::additionalSpawnCheck, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public static void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        ModPotions.registerRecipes(event);
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
        event.put(ModEntities.ANGEL_OF_REAPING.get(), AngelOfReapingEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VEX.get(), SculkVexEntity.createAttributes().build());
        event.put(ModEntities.LIVING_ARMOR.get(), LivingArmorEntity.createAttributes().build());
        event.put(ModEntities.GOLEM_OF_WRATH.get(), GolemOfWrathEntity.createAttributes().build());
        event.put(ModEntities.SCULK_GUARDIAN.get(), SculkGuardianEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BROOD_HATCHER.get(), SculkBroodHatcherEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BROOD_SPITTER.get(), SculkBroodSpitterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BROOD_STRIKER.get(), SculkBroodStrikerEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SHEEP.get(), SculkSheepEntity.createAttributes().build());
        event.put(ModEntities.SCULK_METAMORPHOSIS_POD.get(), SculkMetamorphosisPodEntity.createAttributes().build());
        event.put(ModEntities.SCULK_GHAST.get(), SculkGhastEntity.createAttributes().build());
        event.put(ModEntities.SCULK_LEECH.get(), SculkLeechEntity.createAttributes().build());
        event.put(ModEntities.SCULK_STINGER.get(), SculkStingerEntity.createAttributes().build());
    }
}
