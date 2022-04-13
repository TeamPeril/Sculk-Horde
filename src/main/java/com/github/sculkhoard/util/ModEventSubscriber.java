package com.github.sculkhoard.util;

import com.github.sculkhoard.common.block.BlockInfestation.InfestationConversionTable;
import com.github.sculkhoard.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.common.pools.PoolBlocks;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import static net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import static net.minecraft.entity.EntitySpawnPlacementRegistry.register;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        ChunkLoaderUtil.register(); //Something related to chunk loading
        //Add entries to the entity factory (please add them in order, I don't want to sort)
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE.get(), 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_MITE_AGGRESSOR.get(), 6, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Undeveloped);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_MITE.get(), 1, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Undeveloped);

        SculkHoard.infestationConversionTable = new InfestationConversionTable();
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFECTED_DIRT.get(), BlockRegistry.CRUST.get());
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFESTED_STONE_ACTIVE.get(), BlockRegistry.INFESTED_STONE_DORMANT.get());
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFESTED_LOG_ACTIVE.get(), BlockRegistry.INFESTED_LOG_DORMANT.get());

        SculkHoard.randomSculkFlora = new PoolBlocks();
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.COCOON_ROOT.get(), 1);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.SPIKE.get(), 10);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.SMALL_SHROOM.get(), 10);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.GRASS_SHORT.get(), 90);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.GRASS.get(), 100);

        
    }

    /* registerEntities
     * @Description Registers entities
     */
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        register(EntityRegistry.SCULK_ZOMBIE.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkZombieEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_MITE.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkMiteEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_MITE_AGGRESSOR.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkMiteAggressorEntity::passSpawnCondition);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorEntity.createAttributes().build());
    }

}

