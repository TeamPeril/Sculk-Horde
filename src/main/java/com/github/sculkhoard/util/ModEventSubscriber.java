package com.github.sculkhoard.util;

import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.SculkWorldData;
import net.minecraft.entity.EntityType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.WorldEvent;
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
        //Add entries to the entity factory (please add them in order, I don't want to sort)
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE.get(), 20);
        SculkHoard.entityFactory.addEntry(EntityRegistry.SCULK_MITE.get(), 1);
        
    }


    /* registerEntities
     * @Description Registers entities
     */
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        register(EntityRegistry.SCULK_ZOMBIE.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkZombieEntity::passSpawnCondition);
        register(EntityRegistry.SCULK_MITE.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkMiteEntity::passSpawnCondition);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(EntityRegistry.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
    }

}

