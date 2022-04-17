package com.github.sculkhoard.core;

import com.github.sculkhoard.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.common.entity.SculkSpitterEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.common.entity.projectile.SculkAcidSpitEntity;
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

    public static final RegistryObject<EntityType<SculkMiteAggressorEntity>> SCULK_MITE_AGGRESSOR =
            ENTITY_TYPES.register("sculk_mite_aggressor", () -> EntityType.Builder.<SculkMiteAggressorEntity>of(
                            SculkMiteAggressorEntity::new,
                            EntityClassification.MONSTER)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(9)
                    .build(new ResourceLocation(SculkHoard.MOD_ID, "sculk_mite_aggressor").toString())
            );

    public static final RegistryObject<EntityType<SculkSpitterEntity>> SCULK_SPITTER =
            ENTITY_TYPES.register("sculk_spitter", () -> EntityType.Builder.<SculkSpitterEntity>of(
                            SculkSpitterEntity::new,
                            EntityClassification.MONSTER)
                    .sized(0.6f, 0.6f)
                    .clientTrackingRange(9)
                    .build(new ResourceLocation(SculkHoard.MOD_ID, "sculk_spitter").toString())
            );

    public static final RegistryObject<EntityType<SculkAcidSpitEntity>> SCULK_ACID_SPIT = ENTITY_TYPES.register("sculk_acid_spit",
            () -> EntityType.Builder.<SculkAcidSpitEntity>of(SculkAcidSpitEntity::new, EntityClassification.MISC)
                    .clientTrackingRange(9).sized(0.5F, 0.5F)
                    .build(new ResourceLocation(SculkHoard.MOD_ID, "unmaykr_bolt").toString()));



}
