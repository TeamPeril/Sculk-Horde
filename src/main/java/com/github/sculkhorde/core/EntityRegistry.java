package com.github.sculkhorde.core;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.infection.CursorBridgerEntity;
import com.github.sculkhorde.common.entity.infection.CursorProberEntity;
import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class EntityRegistry {

    /** ENTITY TYPES **/

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, SculkHorde.MOD_ID);
    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }

    public static EntityType<CustomItemProjectileEntity> CUSTOM_ITEM_PROJECTILE_ENTITY = buildEntityType(CustomItemProjectileEntity::new, "custom_item_projectile", 0.45F, 0.45F, EntityClassification.MISC, b -> b.clientTrackingRange(4).updateInterval(10));
    public static EntityType<SculkAcidicProjectileEntity> SCULK_ACIDIC_PROJECTILE_ENTITY = buildEntityType(SculkAcidicProjectileEntity::new, "sculk_acidic_projectile", 0.45F, 0.45F, EntityClassification.MISC, b -> b.clientTrackingRange(4).updateInterval(10));

    public static EntityType<SculkZombieEntity> SCULK_ZOMBIE = buildEntityType(SculkZombieEntity::new, "sculk_zombie", 0.6f, 1.95f, EntityClassification.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<SculkMiteEntity> SCULK_MITE = buildEntityType(SculkMiteEntity::new, "sculk_mite", 0.6f, 0.6f, EntityClassification.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<SculkMiteAggressorEntity> SCULK_MITE_AGGRESSOR = buildEntityType(SculkMiteAggressorEntity::new, "sculk_mite_aggressor", 0.6f, 0.6f, EntityClassification.MONSTER, b -> b.clientTrackingRange(4));
    public static EntityType<SculkSpitterEntity> SCULK_SPITTER = buildEntityType(SculkSpitterEntity::new, "sculk_spitter", 0.6f, 1.95f, EntityClassification.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<SculkBeeInfectorEntity> SCULK_BEE_INFECTOR = buildEntityType(SculkBeeInfectorEntity::new, "sculk_bee_infector", 0.7f, 0.6f, EntityClassification.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<SculkBeeHarvesterEntity> SCULK_BEE_HARVESTER = buildEntityType(SculkBeeHarvesterEntity::new, "sculk_bee_harvester", 0.7f, 0.6f, EntityClassification.CREATURE, b -> b.clientTrackingRange(9));
    public static EntityType<SculkHatcherEntity> SCULK_HATCHER = buildEntityType(SculkHatcherEntity::new, "sculk_hatcher", 0.9f, 1.4f, EntityClassification.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<CursorProberEntity> CURSOR_LONG_RANGE = buildEntityType(CursorProberEntity::new, "cursor_long_range", 1.0f, 1.0f, EntityClassification.MISC, b -> b.clientTrackingRange(9));
    public static EntityType<CursorInfectorEntity> CURSOR_SHORT_RANGE = buildEntityType(CursorInfectorEntity::new, "cursor_short_range", 1.0f, 1.0f, EntityClassification.MISC, b -> b.clientTrackingRange(9));
    public static EntityType<CursorBridgerEntity> CURSOR_BRIDGER = buildEntityType(CursorBridgerEntity::new, "cursor_bridger", 1.0f, 1.0f, EntityClassification.MISC, b -> b.clientTrackingRange(9));
    public static EntityType<SculkSporeSpewerEntity> SCULK_SPORE_SPEWER = buildEntityType(SculkSporeSpewerEntity::new, "sculk_spore_spewer", 1.0f, 2.0f, EntityClassification.MISC, b -> b.clientTrackingRange(9));

    /** REGISTRY METHODS **/

    /**
     * Trying out new method for registering entities
     * @param event
     */
    @SubscribeEvent
    public static void registerEntities(final RegistryEvent.Register<EntityType<?>> event)
    {
        //Projectiles
        event.getRegistry().register(CUSTOM_ITEM_PROJECTILE_ENTITY.setRegistryName(SculkHorde.MOD_ID, "custom_item_projectile"));
        event.getRegistry().register(SCULK_ACIDIC_PROJECTILE_ENTITY.setRegistryName(SculkHorde.MOD_ID, "sculk_acidic_projectile"));

        //Mobs
        event.getRegistry().register(SCULK_ZOMBIE.setRegistryName(SculkHorde.MOD_ID, "sculk_zombie"));
        event.getRegistry().register(SCULK_MITE.setRegistryName(SculkHorde.MOD_ID, "sculk_mite"));
        event.getRegistry().register(SCULK_MITE_AGGRESSOR.setRegistryName(SculkHorde.MOD_ID, "sculk_mite_aggressor"));
        event.getRegistry().register(SCULK_SPITTER.setRegistryName(SculkHorde.MOD_ID, "sculk_spitter"));
        event.getRegistry().register(SCULK_BEE_INFECTOR.setRegistryName(SculkHorde.MOD_ID, "sculk_bee_infector"));
        event.getRegistry().register(SCULK_BEE_HARVESTER.setRegistryName(SculkHorde.MOD_ID, "sculk_bee_harvester"));
        event.getRegistry().register(SCULK_HATCHER.setRegistryName(SculkHorde.MOD_ID, "sculk_hatcher"));
        event.getRegistry().register(SCULK_SPORE_SPEWER.setRegistryName(SculkHorde.MOD_ID, "sculk_spore_spewer"));

        //Misc
        event.getRegistry().register(CURSOR_LONG_RANGE.setRegistryName(SculkHorde.MOD_ID, "cursor_long_range"));
        event.getRegistry().register(CURSOR_SHORT_RANGE.setRegistryName(SculkHorde.MOD_ID, "cursor_short_range"));
        event.getRegistry().register(CURSOR_BRIDGER.setRegistryName(SculkHorde.MOD_ID, "cursor_bridger"));
    }

    /** HELPER METHODS **/

    /**
     * Builds and returns (but does not register) an entity type with the given information
     *
     * @param <T>            a class that inherits from Entity
     * @param factoryIn      the entity factory, usually [EntityClass]::new
     * @param name           the entity name for use in registration later
     * @param width          the horizontal size of the entity
     * @param height         the vertical size of the entity
     * @param classification the entity classification
     * @param builderSpecs   a consumer to add other arguments to the builder before the entity type is built
     * @return an entity type
     **/
    private static <T extends Entity> EntityType<T> buildEntityType(final EntityType.IFactory<T> factoryIn, final String name, final float width, final float height,
                                                                    final EntityClassification classification, final Consumer<EntityType.Builder<T>> builderSpecs) {
        EntityType.Builder<T> entityTypeBuilder = EntityType.Builder.of(factoryIn, classification).sized(width, height).clientTrackingRange(8);
        builderSpecs.accept(entityTypeBuilder);
        EntityType<T> entityType = entityTypeBuilder.build(name);
        return entityType;
    }


}
