package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
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
        //Add entries to the entity factory (please add them in order, I don't want to sort)
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_SPORE_SPEWER.get(), (int) SculkSporeSpewerEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Immature).setLimit(1);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_RAVAGER.get(), (int) SculkRavagerEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature).setLimit(1);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_HATCHER.get(), (int) SculkHatcherEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_CREEPER.get(), 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_SPITTER.get(), 20, EntityFactory.StrategicValues.Ranged, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE.get(), 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_VINDICATOR.get(), (int) SculkVindicatorEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_MITE_AGGRESSOR.get(), 6, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Undeveloped);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_MITE.get(), (int) SculkMiteEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Undeveloped);

        SculkHorde.infestationConversionTable = new InfestationConversionHandler();

        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.DIRT, Blocks.SCULK.defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.COARSE_DIRT, Blocks.SCULK.defaultBlockState());
        //SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.GRASS_PATH.defaultBlockState(), BlockRegistry.CRUST.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.GRASS_BLOCK, Blocks.SCULK.defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.PODZOL, Blocks.SCULK.defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.STONE, BlockRegistry.INFESTED_STONE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.DEEPSLATE, BlockRegistry.INFESTED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.COBBLED_DEEPSLATE, BlockRegistry.INFESTED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.SAND, BlockRegistry.INFESTED_SAND.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.SANDSTONE, BlockRegistry.INFESTED_SANDSTONE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.DIORITE, BlockRegistry.INFESTED_DIORITE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.GRANITE, BlockRegistry.INFESTED_GRANITE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.ANDESITE, BlockRegistry.INFESTED_ANDESITE.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.TUFF, BlockRegistry.INFESTED_TUFF.get().defaultBlockState());

        SculkHorde.randomSculkFlora = new PoolBlocks();
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_CATALYST, 1);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SCULK_SUMMONER_BLOCK.get(), 2);
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_SENSOR, 3);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SPIKE.get(), 4);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SMALL_SHROOM.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SCULK_SHROOM_CULTURE.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.GRASS_SHORT.get(), 200);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.GRASS.get(), 200);

        event.enqueueWork(() -> {
            SpawnPlacements.register(EntityRegistry.SCULK_MITE.get(),
                    SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SculkMiteEntity::passSpawnCondition);
        });
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_SPITTER.get(), SculkSpitterEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_BEE_INFECTOR.get(), SculkBeeInfectorEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_BEE_HARVESTER.get(), SculkBeeHarvesterEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_HATCHER.get(), SculkHatcherEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_SPORE_SPEWER.get(), SculkSporeSpewerEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_RAVAGER.get(), SculkRavagerEntity.createAttributes().build());
        event.put(EntityRegistry.INFESTATION_PURIFIER.get(), InfestationPurifierEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_VINDICATOR.get(), SculkVindicatorEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_CREEPER.get(), SculkCreeperEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_ENDERMAN.get(), SculkEndermanEntity.createAttributes().build());
    }
}

