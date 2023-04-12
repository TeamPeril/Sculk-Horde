package com.github.sculkhorde.core;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.infection.*;
import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class EntityRegistry {

    /** ENTITY TYPES **/

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SculkHorde.MOD_ID);

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }

    public static <T extends Mob> RegistryObject<EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> entity, float width, float height, int primaryEggColor, int secondaryEggColor) {
        RegistryObject<EntityType<T>> entityType = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(entity, MobCategory.MONSTER).sized(width, height).build(name));

        return entityType;
    }

    public static final RegistryObject<EntityType<SculkZombieEntity>> SCULK_ZOMBIE = registerMob("sculk_zombie", SculkZombieEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkMiteEntity>> SCULK_MITE = registerMob("sculk_mite", SculkMiteEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkMiteAggressorEntity>> SCULK_MITE_AGGRESSOR = registerMob("sculk_mite_aggressor", SculkMiteAggressorEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkSpitterEntity>> SCULK_SPITTER = registerMob("sculk_spitter", SculkSpitterEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkBeeInfectorEntity>> SCULK_BEE_INFECTOR = registerMob("sculk_bee_infector", SculkBeeInfectorEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkBeeHarvesterEntity>> SCULK_BEE_HARVESTER = registerMob("sculk_bee_harvester", SculkBeeHarvesterEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final RegistryObject<EntityType<SculkHatcherEntity>> SCULK_HATCHER = registerMob("sculk_hatcher", SculkHatcherEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);

    public static final RegistryObject<EntityType<SculkSporeSpewerEntity>> SCULK_SPORE_SPEWER = registerMob("sculk_spore_spewer", SculkSporeSpewerEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);

    public static final RegistryObject<EntityType<SculkRavagerEntity>> SCULK_RAVAGER = registerMob("sculk_ravager", SculkRavagerEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);

    public static final RegistryObject<EntityType<CursorProberEntity>> CURSOR_PROBER = ENTITY_TYPES.register("cursor_prober", () -> EntityType.Builder.<CursorProberEntity>of(CursorProberEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_prober"));
    public static final RegistryObject<EntityType<CustomItemProjectileEntity>> CUSTOM_ITEM_PROJECTILE_ENTITY = ENTITY_TYPES.register("custom_item_projectile", () -> EntityType.Builder.<CustomItemProjectileEntity>of(CustomItemProjectileEntity::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(4).updateInterval(10).build("custom_item_projectile"));
    public static final RegistryObject<EntityType<SculkAcidicProjectileEntity>> SCULK_ACIDIC_PROJECTILE_ENTITY = ENTITY_TYPES.register("sculk_acidic_projectile", () -> EntityType.Builder.<SculkAcidicProjectileEntity>of(SculkAcidicProjectileEntity::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(4).updateInterval(10).build("sculk_acidic_projectile"));

    public static final RegistryObject<EntityType<CursorInfectorEntity>> CURSOR_INFECTOR = ENTITY_TYPES.register("cursor_infector", () -> EntityType.Builder.<CursorInfectorEntity>of(CursorInfectorEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_infector"));
    public static final RegistryObject<EntityType<CursorBridgerEntity>> CURSOR_BRIDGER = ENTITY_TYPES.register("cursor_bridger", () -> EntityType.Builder.<CursorBridgerEntity>of(CursorBridgerEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_bridger"));
    public static final RegistryObject<EntityType<CursorSurfaceInfectorEntity>> CURSOR_SURFACE_INFECTOR = ENTITY_TYPES.register("cursor_surface_infector", () -> EntityType.Builder.<CursorSurfaceInfectorEntity>of(CursorSurfaceInfectorEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_surface_infector"));
    public static final RegistryObject<EntityType<CursorSurfacePurifierEntity>> CURSOR_SURFACE_PURIFIER = ENTITY_TYPES.register("cursor_surface_purifier", () -> EntityType.Builder.<CursorSurfacePurifierEntity>of(CursorSurfacePurifierEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_surface_purifier"));
    public static final RegistryObject<EntityType<InfestationPurifierEntity>> INFESTATION_PURIFIER = ENTITY_TYPES.register("infestation_purifier", () -> EntityType.Builder.<InfestationPurifierEntity>of(InfestationPurifierEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("infestation_purifier"));

    /*


    public static EntityType<SculkSporeSpewerEntity> SCULK_SPORE_SPEWER = buildEntityType(SculkSporeSpewerEntity::new, "sculk_spore_spewer", 1.0f, 2.0f, MobCategory.MISC, b -> b.clientTrackingRange(9));
    public static EntityType<SculkRavagerEntity> SCULK_RAVAGER = buildEntityType(SculkRavagerEntity::new, "sculk_ravager", 1.95f, 2.2f, MobCategory.MONSTER, b -> b.clientTrackingRange(9));
    public static EntityType<InfestationPurifierEntity> INFESTATION_PURIFIER = buildEntityType(InfestationPurifierEntity::new, "infestation_purifier", 1f, 1f, MobCategory.CREATURE, b -> b.clientTrackingRange(9));
    */


    /**
     * Trying out new method for registering entities
     * @param event
     */
    /*
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
        event.getRegistry().register(SCULK_RAVAGER.setRegistryName(SculkHorde.MOD_ID, "sculk_ravager"));
        event.getRegistry().register(INFESTATION_PURIFIER.setRegistryName(SculkHorde.MOD_ID, "infestation_purifier"));

        //Misc
        event.getRegistry().register(CURSOR_LONG_RANGE.setRegistryName(SculkHorde.MOD_ID, "cursor_long_range"));
        event.getRegistry().register(CURSOR_SHORT_RANGE.setRegistryName(SculkHorde.MOD_ID, "cursor_short_range"));
        event.getRegistry().register(CURSOR_BRIDGER.setRegistryName(SculkHorde.MOD_ID, "cursor_bridger"));
        event.getRegistry().register(CURSOR_SURFACE_INFECTOR.setRegistryName(SculkHorde.MOD_ID, "cursor_surface_infector"));
        event.getRegistry().register(CURSOR_SURFACE_PURIFIER.setRegistryName(SculkHorde.MOD_ID, "cursor_surface_purifier"));

    }
    */
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
    private static <T extends Entity> EntityType<T> buildEntityType(final EntityType.EntityFactory<T> factoryIn, final String name, final float width, final float height,
                                                                    final MobCategory classification, final Consumer<EntityType.Builder<T>> builderSpecs) {
        EntityType.Builder<T> entityTypeBuilder = EntityType.Builder.of(factoryIn, classification).sized(width, height).clientTrackingRange(8);
        builderSpecs.accept(entityTypeBuilder);
        EntityType<T> entityType = entityTypeBuilder.build(name);
        return entityType;
    }


}
