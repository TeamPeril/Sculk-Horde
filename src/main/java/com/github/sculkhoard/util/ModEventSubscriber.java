package com.github.sculkhoard.util;

import com.github.sculkhoard.common.block.BlockInfestation.InfestationConversionTable;
import com.github.sculkhoard.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.SculkSpitterEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.common.pools.PoolBlocks;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;

import static net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import static net.minecraft.entity.EntitySpawnPlacementRegistry.register;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        ChunkLoaderUtil.register(); //Something related to chunk loading
        //Add entries to the entity factory (please add them in order, I don't want to sort)
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_SPITTER, 20, EntityFactory.StrategicValues.Ranged, Gravemind.evolution_states.Immature);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE, 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_MITE_AGGRESSOR, 6, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Undeveloped);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_MITE, (int) SculkMiteEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Undeveloped);

        SculkHoard.infestationConversionTable = new InfestationConversionTable();
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFECTED_DIRT.get());
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFESTED_STONE_ACTIVE.get());
        SculkHoard.infestationConversionTable.addEntry(BlockRegistry.INFESTED_LOG_ACTIVE.get());

        SculkHoard.gravemind.confirmedThreats = new ArrayList<String>();
        SculkHoard.gravemind.sculkNodePositions = new ArrayList<BlockPos>();

        SculkHoard.randomSculkFlora = new PoolBlocks();

        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.SPIKE.get(), 2);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.SMALL_SHROOM.get(), 3);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.COCOON_ROOT.get(), 5);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.SCULK_SHROOM_CULTURE.get(), 3);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.GRASS_SHORT.get(), 100);
        SculkHoard.randomSculkFlora.addEntry(BlockRegistry.GRASS.get(), 100);

        
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
    }

}

