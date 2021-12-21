package com.github.sculkhoard.util;

import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.common.world.gen.ModEntityGen;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import static net.minecraft.entity.EntitySpawnPlacementRegistry.register;
import static net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    /* registerEntities
     * @Description Registers entities
     */
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event) {
        register(EntityRegistry.SCULK_ZOMBIE.get(), PlacementType.ON_GROUND, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, SculkZombieEntity::passSpawnCondition);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
    }
}

