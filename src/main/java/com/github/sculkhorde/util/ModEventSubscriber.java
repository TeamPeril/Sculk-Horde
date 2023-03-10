package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import static net.minecraft.entity.EntitySpawnPlacementRegistry.register;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        //Add entries to the entity factory (please add them in order, I don't want to sort)
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_HATCHER, (int) SculkHatcherEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_SPITTER, 20, EntityFactory.StrategicValues.Ranged, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE, 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_MITE_AGGRESSOR, 6, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Undeveloped);
        SculkHorde.entityFactory.addEntry(EntityRegistry.SCULK_MITE, (int) SculkMiteEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Undeveloped);

        SculkHorde.infestationConversionTable = new InfestationConversionHandler();

        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.DIRT.defaultBlockState(), BlockRegistry.CRUST.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.COARSE_DIRT.defaultBlockState(), BlockRegistry.CRUST.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.GRASS_PATH.defaultBlockState(), BlockRegistry.CRUST.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.GRASS_BLOCK.defaultBlockState(), BlockRegistry.CRUST.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.STONE.defaultBlockState(), BlockRegistry.INFESTED_STONE_DORMANT.get().defaultBlockState());
        SculkHorde.infestationConversionTable.infestationTable.addEntry(Blocks.STONE.defaultBlockState(), BlockRegistry.INFESTED_STONE_DORMANT.get().defaultBlockState());

        SculkHorde.randomSculkFlora = new PoolBlocks();
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SCULK_SUMMONER_BLOCK.get(), 1);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SPIKE.get(), 4);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SMALL_SHROOM.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.SCULK_SHROOM_CULTURE.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.GRASS_SHORT.get(), 200);
        SculkHorde.randomSculkFlora.addEntry(BlockRegistry.GRASS.get(), 200);

        
    }

    /* registerEntities
     * @Description Registers entities
     */
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        register(EntityRegistry.SCULK_ZOMBIE, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkZombieEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_MITE, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkMiteEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_MITE_AGGRESSOR, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkMiteAggressorEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_SPITTER, PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkSpitterEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_BEE_INFECTOR, PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkBeeInfectorEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_BEE_HARVESTER, PlacementType.NO_RESTRICTIONS, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkBeeHarvesterEntity::passSpawnCondition);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SCULK_ZOMBIE, SculkZombieEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE, SculkMiteEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE_AGGRESSOR, SculkMiteAggressorEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_SPITTER, SculkSpitterEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_BEE_INFECTOR, SculkBeeInfectorEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_BEE_HARVESTER, SculkBeeHarvesterEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_HATCHER, SculkHatcherEntity.createAttributes().build());
    }

}

