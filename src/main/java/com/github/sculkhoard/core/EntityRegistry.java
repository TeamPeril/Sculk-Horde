package com.github.sculkhoard.core;

import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityRegistry {
    public static DeferredRegister<EntityType<?>> ENTITY_TYPES
            = DeferredRegister.create(ForgeRegistries.ENTITIES, SculkHoard.MOD_ID);

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }

    public static final RegistryObject<EntityType<SculkZombieEntity>> SCULK_ZOMBIE =
            ENTITY_TYPES.register("sculk_zombie", () -> EntityType.Builder.<SculkZombieEntity>of(
                    SculkZombieEntity::new,
                    EntityClassification.MONSTER)
                    .sized(0.6f, 1.95f)
                    .clientTrackingRange(9)
                    .build(new ResourceLocation(SculkHoard.MOD_ID, "sculk_zombie").toString())
            );

    public static final RegistryObject<EntityType<SculkMiteEntity>> SCULK_MITE =
            ENTITY_TYPES.register("sculk_mite", () -> EntityType.Builder.<SculkMiteEntity>of(
                    SculkMiteEntity::new,
                    EntityClassification.MONSTER)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(9)
                    .build(new ResourceLocation(SculkHoard.MOD_ID, "sculk_mite").toString())
            );


}
