package com.github.sculkhorde.core;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.ChaosTeleporationRiftEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.EnderBubbleAttackEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.*;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalBreezeMagicCircleAttackEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalFireMagicCircleAttackEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalIceMagicCircleAttackEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ElementalPoisonMagicCircleAttackEntity;
import com.github.sculkhorde.common.entity.dev.ChunkInfectEntity;
import com.github.sculkhorde.common.entity.infection.*;
import com.github.sculkhorde.common.entity.projectile.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModEntities {

    /** ENTITY TYPES **/

    public static DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, SculkHorde.MOD_ID);

    // NeoForge 1.21.1 no longer supports Forge's custom MobCategory.create API.
    // Keep the mod mobs in the standard monster category rather than dropping registration.
    public static final MobCategory SCULK_HORDE_CATEGORY = MobCategory.MONSTER;

    public static void register(IEventBus eventBus){
        ENTITY_TYPES.register(eventBus);
    }

    public static <T extends Mob> DeferredHolder<EntityType<?>, EntityType<T>> registerMob(String name, EntityType.EntityFactory<T> entity, float width, float height, int primaryEggColor, int secondaryEggColor) {
        DeferredHolder<EntityType<?>, EntityType<T>> entityType = ENTITY_TYPES.register(name,
                () -> EntityType.Builder.of(entity, SCULK_HORDE_CATEGORY).sized(width, height).build(name));

        return entityType;
    }

    public static final DeferredHolder<EntityType<?>, EntityType<SculkZombieEntity>> SCULK_ZOMBIE = registerMob("sculk_zombie", SculkZombieEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkMiteEntity>> SCULK_MITE = registerMob("sculk_mite", SculkMiteEntity::new, 0.6f, 0.6f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkMiteAggressorEntity>> SCULK_MITE_AGGRESSOR = registerMob("sculk_mite_aggressor", SculkMiteAggressorEntity::new, 0.6f, 0.6f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSpitterEntity>> SCULK_SPITTER = registerMob("sculk_spitter", SculkSpitterEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkBeeInfectorEntity>> SCULK_BEE_INFECTOR = registerMob("sculk_bee_infector", SculkBeeInfectorEntity::new, 0.6f, 0.6f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkBeeHarvesterEntity>> SCULK_BEE_HARVESTER = registerMob("sculk_bee_harvester", SculkBeeHarvesterEntity::new, 0.6f, 0.6f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkHatcherEntity>> SCULK_HATCHER = registerMob("sculk_hatcher", SculkHatcherEntity::new, 0.9f, 1.4f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkVindicatorEntity>> SCULK_VINDICATOR = registerMob("sculk_vindicator", SculkVindicatorEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSporeSpewerEntity>> SCULK_SPORE_SPEWER = registerMob("sculk_spore_spewer", SculkSporeSpewerEntity::new, 1f, 2f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkRavagerEntity>> SCULK_RAVAGER = registerMob("sculk_ravager", SculkRavagerEntity::new, 1.95f, 2.2f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkCreeperEntity>> SCULK_CREEPER = registerMob("sculk_creeper", SculkCreeperEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkPhantomEntity>> SCULK_PHANTOM = registerMob("sculk_phantom", SculkPhantomEntity::new, 2.5f, 1f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkPhantomCorpseEntity>> SCULK_PHANTOM_CORPSE = registerMob("sculk_phantom_corpse", SculkPhantomCorpseEntity::new, 1f, 1f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkEndermanEntity>> SCULK_ENDERMAN = registerMob("sculk_enderman", SculkEndermanEntity::new, 0.6f, 3f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSalmonEntity>> SCULK_SALMON = registerMob("sculk_salmon", SculkSalmonEntity::new, 0.9f, 0.7f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSquidEntity>> SCULK_SQUID = registerMob("sculk_squid", SculkSquidEntity::new, 1f, 1f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkPufferfishEntity>> SCULK_PUFFERFISH = registerMob("sculk_pufferfish", SculkPufferfishEntity::new, 0.9f, 0.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkWitchEntity>> SCULK_WITCH = registerMob("sculk_witch", SculkWitchEntity::new, 0.9f, 2.8f, 0x000000, 0x000000);

    public static final DeferredHolder<EntityType<?>, EntityType<LivingArmorEntity>> LIVING_ARMOR = registerMob("living_armor", LivingArmorEntity::new, 0.6f, 1.95f, 0x000000, 0x000000);

    public static final DeferredHolder<EntityType<?>, EntityType<AngelOfReapingEntity>> ANGEL_OF_REAPING = registerMob("angel_of_reaping", AngelOfReapingEntity::new, 0.9f, 1.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkVexEntity>> SCULK_VEX = registerMob("sculk_vex", SculkVexEntity::new, 0.8f, 0.8f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<GolemOfWrathEntity>> GOLEM_OF_WRATH = registerMob("golem_of_wrath", GolemOfWrathEntity::new, 3f, 3f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkGuardianEntity>> SCULK_GUARDIAN = registerMob("sculk_guardian", SculkGuardianEntity::new, 1.3f, 1.3f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkBroodHatcherEntity>> SCULK_BROOD_HATCHER = registerMob("sculk_brood_hatcher", SculkBroodHatcherEntity::new, 2.9f, 2.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkBroodSpitterEntity>> SCULK_BROOD_SPITTER = registerMob("sculk_brood_spitter", SculkBroodSpitterEntity::new, 1.4f, 0.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkBroodStrikerEntity>> SCULK_BROOD_STRIKER = registerMob("sculk_brood_striker", SculkBroodStrikerEntity::new, 1.4f, 0.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSheepEntity>> SCULK_SHEEP = registerMob("sculk_sheep", SculkSheepEntity::new, 0.9f, 1.4f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkMetamorphosisPodEntity>> SCULK_METAMORPHOSIS_POD = registerMob("sculk_metamorphosis_pod", SculkMetamorphosisPodEntity::new, 1f, 1.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkGhastEntity>> SCULK_GHAST = registerMob("sculk_ghast", SculkGhastEntity::new, 4.9f, 4.9f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkLeechEntity>> SCULK_LEECH = registerMob("sculk_leech", SculkLeechEntity::new, 0.8f, 0.8f, 0x000000, 0x000000);
    public static final DeferredHolder<EntityType<?>, EntityType<SculkStingerEntity>> SCULK_STINGER = registerMob("sculk_stinger", SculkStingerEntity::new, 0.8f, 0.8f, 0x000000, 0x000000);


    public static final DeferredHolder<EntityType<?>, EntityType<CursorProberEntity>> CURSOR_PROBER = ENTITY_TYPES.register("cursor_prober", () -> EntityType.Builder.<CursorProberEntity>of(CursorProberEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_prober"));
    public static final DeferredHolder<EntityType<?>, EntityType<CustomItemProjectileEntity>> CUSTOM_ITEM_PROJECTILE_ENTITY = ENTITY_TYPES.register("custom_item_projectile", () -> EntityType.Builder.<CustomItemProjectileEntity>of(CustomItemProjectileEntity::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(4).updateInterval(10).build("custom_item_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SculkAcidicProjectileEntity>> SCULK_ACIDIC_PROJECTILE_ENTITY = ENTITY_TYPES.register("sculk_acidic_projectile", () -> EntityType.Builder.<SculkAcidicProjectileEntity>of(SculkAcidicProjectileEntity::new, MobCategory.MISC).sized(1.0F, 1.0F).clientTrackingRange(4).updateInterval(4).build("sculk_acidic_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<PurificationFlaskProjectileEntity>> PURIFICATION_FLASK_PROJECTILE_ENTITY = ENTITY_TYPES.register("purification_flask_projectile", () -> EntityType.Builder.<PurificationFlaskProjectileEntity>of(PurificationFlaskProjectileEntity::new, MobCategory.MISC).sized(0.45F, 0.45F).clientTrackingRange(4).updateInterval(10).build("purification_flask_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<EnderBubbleAttackEntity>> ENDER_BUBBLE_ATTACK = ENTITY_TYPES.register("ender_bubble_attack", () -> EntityType.Builder.<EnderBubbleAttackEntity>of(EnderBubbleAttackEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("ender_bubble_attack"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChaosTeleporationRiftEntity>> CHAOS_TELEPORATION_RIFT = ENTITY_TYPES.register("chaos_teleporation_rift", () -> EntityType.Builder.<ChaosTeleporationRiftEntity>of(ChaosTeleporationRiftEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("chaos_teleporation_rift"));
    public static final DeferredHolder<EntityType<?>, EntityType<SculkSpineSpikeAttackEntity>> SCULK_SPINE_SPIKE_ATTACK = ENTITY_TYPES.register("sculk_spine_spike_attack", () -> EntityType.Builder.<SculkSpineSpikeAttackEntity>of(SculkSpineSpikeAttackEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("sculk_spine_spike_attack"));
    public static final DeferredHolder<EntityType<?>, EntityType<CursorBridgerEntity>> CURSOR_BRIDGER = ENTITY_TYPES.register("cursor_bridger", () -> EntityType.Builder.<CursorBridgerEntity>of(CursorBridgerEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_bridger"));
    public static final DeferredHolder<EntityType<?>, EntityType<CursorSurfaceInfectorEntity>> CURSOR_SURFACE_INFECTOR = ENTITY_TYPES.register("cursor_surface_infector", () -> EntityType.Builder.<CursorSurfaceInfectorEntity>of(CursorSurfaceInfectorEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_surface_infector"));
    public static final DeferredHolder<EntityType<?>, EntityType<CursorSurfacePurifierEntity>> CURSOR_SURFACE_PURIFIER = ENTITY_TYPES.register("cursor_surface_purifier", () -> EntityType.Builder.<CursorSurfacePurifierEntity>of(CursorSurfacePurifierEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_surface_purifier"));
    public static final DeferredHolder<EntityType<?>, EntityType<CursorPurifierProberEntity>> CURSOR_PURIFIER_PROBER = ENTITY_TYPES.register("cursor_purifier_prober", () -> EntityType.Builder.<CursorPurifierProberEntity>of(CursorPurifierProberEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("cursor_surface_purifier"));
    public static final DeferredHolder<EntityType<?>, EntityType<InfestationPurifierEntity>> INFESTATION_PURIFIER = ENTITY_TYPES.register("infestation_purifier", () -> EntityType.Builder.<InfestationPurifierEntity>of(InfestationPurifierEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("infestation_purifier"));
    public static final DeferredHolder<EntityType<?>, EntityType<AreaEffectSphericalCloudEntity>> AREA_EFFECT_SPHERICAL_CLOUD = ENTITY_TYPES.register("area_effect_spherical_cloud", () -> EntityType.Builder.<AreaEffectSphericalCloudEntity>of(AreaEffectSphericalCloudEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("area_effect_spherical_cloud"));
    public static final DeferredHolder<EntityType<?>, EntityType<ChunkInfectEntity>> CHUNK_INFECT_ENTITY = ENTITY_TYPES.register("chunk_infect_entity", () -> EntityType.Builder.<ChunkInfectEntity>of(ChunkInfectEntity::new, MobCategory.MISC).sized(1f, 1f).clientTrackingRange(4).updateInterval(10).build("chunk_infect_entity"));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulFireProjectileAttackEntity>> SOUL_FIRE_PROJECTILE = ENTITY_TYPES.register("soul_fire_projectile", () -> EntityType.Builder.<SoulFireProjectileAttackEntity>of(SoulFireProjectileAttackEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(4).updateInterval(4).build("soul_fire_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulPoisonProjectileAttackEntity>> SOUL_POISON_PROJECTILE = ENTITY_TYPES.register("soul_poison_projectile", () -> EntityType.Builder.<SoulPoisonProjectileAttackEntity>of(SoulPoisonProjectileAttackEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(4).updateInterval(4).build("soul_poison_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulIceProjectileAttackEntity>> SOUL_ICE_PROJECTILE = ENTITY_TYPES.register("soul_ice_projectile", () -> EntityType.Builder.<SoulIceProjectileAttackEntity>of(SoulIceProjectileAttackEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(4).updateInterval(4).build("soul_ice_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulBreezeProjectileAttackEntity>> SOUL_BREEZE_PROJECTILE = ENTITY_TYPES.register("soul_breeze_projectile", () -> EntityType.Builder.<SoulBreezeProjectileAttackEntity>of(SoulBreezeProjectileAttackEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(4).updateInterval(4).build("soul_breeze_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulSpearProjectileAttackEntity>> SOUL_SPEAR_PROJECTILE = ENTITY_TYPES.register("soul_spear_projectile", () -> EntityType.Builder.<SoulSpearProjectileAttackEntity>of(SoulSpearProjectileAttackEntity::new, MobCategory.MISC).sized(0.9F, 0.9F).clientTrackingRange(4).updateInterval(4).build("soul_spear_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulFlySwatterProjectileAttackEntity>> SOUL_FLY_SWATTER_PROJECTILE = ENTITY_TYPES.register("soul_fly_swatter_projectile", () -> EntityType.Builder.<SoulFlySwatterProjectileAttackEntity>of(SoulFlySwatterProjectileAttackEntity::new, MobCategory.MISC).sized(3.0F, 3.0F).clientTrackingRange(4).updateInterval(1).build("soul_fly_swatter_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<FloorSoulSpearsAttackEntity>> FLOOR_SOUL_SPEARS = ENTITY_TYPES.register("floor_soul_spears", () -> EntityType.Builder.<FloorSoulSpearsAttackEntity>of(FloorSoulSpearsAttackEntity::new, MobCategory.MISC).sized(0.9F, 2.0F).clientTrackingRange(4).updateInterval(1).build("floor_soul_spears"));

    public static final DeferredHolder<EntityType<?>, EntityType<ElementalFireMagicCircleAttackEntity>> ELEMENTAL_FIRE_MAGIC_CIRCLE = ENTITY_TYPES.register("elemental_fire_magic_circle", () -> EntityType.Builder.<ElementalFireMagicCircleAttackEntity>of(ElementalFireMagicCircleAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("elemental_fire_magic_circle"));
    public static final DeferredHolder<EntityType<?>, EntityType<ElementalPoisonMagicCircleAttackEntity>> ELEMENTAL_POISON_MAGIC_CIRCLE = ENTITY_TYPES.register("elemental_poison_magic_circle", () -> EntityType.Builder.<ElementalPoisonMagicCircleAttackEntity>of(ElementalPoisonMagicCircleAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("elemental_poison_magic_circle"));
    public static final DeferredHolder<EntityType<?>, EntityType<ElementalIceMagicCircleAttackEntity>> ELEMENTAL_ICE_MAGIC_CIRCLE = ENTITY_TYPES.register("elemental_ice_magic_circle", () -> EntityType.Builder.<ElementalIceMagicCircleAttackEntity>of(ElementalIceMagicCircleAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("elemental_ice_magic_circle"));
    public static final DeferredHolder<EntityType<?>, EntityType<ElementalBreezeMagicCircleAttackEntity>> ELEMENTAL_BREEZE_MAGIC_CIRCLE = ENTITY_TYPES.register("elemental_breeze_magic_circle", () -> EntityType.Builder.<ElementalBreezeMagicCircleAttackEntity>of(ElementalBreezeMagicCircleAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("elemental_breeze_magic_circle"));
    public static final DeferredHolder<EntityType<?>, EntityType<SoulSpearSummonerAttackEntity>> SOUL_SPEAR_SUMMONER = ENTITY_TYPES.register("soul_spear_summoner_entity", () -> EntityType.Builder.<SoulSpearSummonerAttackEntity>of(SoulSpearSummonerAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("soul_spear_summoner_entity"));
    public static final DeferredHolder<EntityType<?>, EntityType<ZoltraakAttackEntity>> ZOLTRAAK_ATTACK_ENTITY = ENTITY_TYPES.register("zoltraak_attack_entity", () -> EntityType.Builder.<ZoltraakAttackEntity>of(ZoltraakAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build("zoltraak_attack_entity"));
    public static String SOUL_BLAST_ENTITY_ID = "soul_blast";
    public static final DeferredHolder<EntityType<?>, EntityType<SoulBlastAttackEntity>> SOUL_BLAST_ATTACK_ENTITY = ENTITY_TYPES.register(SOUL_BLAST_ENTITY_ID, () -> EntityType.Builder.<SoulBlastAttackEntity>of(SoulBlastAttackEntity::new, MobCategory.MISC).sized(3f, 3f).clientTrackingRange(4).updateInterval(10).build(SOUL_BLAST_ENTITY_ID));
    public static String ACID_BLOB_PROJECTILE_ENTITY_ID = "acid_blob_projectile";
    public static final DeferredHolder<EntityType<?>, EntityType<AcidBlobProjectileEntity>> ACID_BLOB_PROJECTILE_ENTITY = ENTITY_TYPES.register(ACID_BLOB_PROJECTILE_ENTITY_ID, () -> EntityType.Builder.<AcidBlobProjectileEntity>of(AcidBlobProjectileEntity::new, MobCategory.MISC).sized(0.9f, 0.9f).clientTrackingRange(4).updateInterval(10).build(ACID_BLOB_PROJECTILE_ENTITY_ID));
    public static final String FIRE_BALL_PROJECTILE_ENTITY_ID = "fire_ball_projectile";
    public static final DeferredHolder<EntityType<?>, EntityType<FireBallProjectileEntity>> FIRE_BALL_PROJECTILE_ENTITY = ENTITY_TYPES.register(FIRE_BALL_PROJECTILE_ENTITY_ID, () -> EntityType.Builder.<FireBallProjectileEntity>of(FireBallProjectileEntity::new, MobCategory.MISC).sized(1.5f, 1.5f).clientTrackingRange(4).updateInterval(10).build(FIRE_BALL_PROJECTILE_ENTITY_ID));
    public static final String SMALL_BROOD_ACID_PROJECTILE_ENTITY_ID = "small_brood_acid_projectile";
    public static final DeferredHolder<EntityType<?>, EntityType<SmallBroodAcidProjectileEntity>> SMALL_BROOD_ACID_PROJECTILE_ENTITY = ENTITY_TYPES.register(SMALL_BROOD_ACID_PROJECTILE_ENTITY_ID, () -> EntityType.Builder.<SmallBroodAcidProjectileEntity>of(SmallBroodAcidProjectileEntity::new, MobCategory.MISC).sized(1.5f, 1.5f).clientTrackingRange(4).updateInterval(10).build(SMALL_BROOD_ACID_PROJECTILE_ENTITY_ID));


    public static class EntityTags
    {
        public static TagKey<EntityType<?>> SCULK_ENTITY = create("sculk_entity");
        public static TagKey<EntityType<?>> IMMUNE_TO_CORRODED = create("immune_to_corroded");
        public static TagKey<EntityType<?>> SCULK_HORDE_DO_NOT_ATTACK = create("sculk_horde_do_not_attack");

        private static TagKey<EntityType<?>> create(String string) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, string));
        }
    }
}
