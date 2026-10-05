package com.github.sculkhorde.core;

import com.github.sculkhorde.common.blockentity.*;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {


    public static DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SculkHorde.MOD_ID);

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkMassBlockEntity>> SCULK_MASS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_mass_block_entity", () -> BlockEntityType.Builder.of(
                    SculkMassBlockEntity::new, ModBlocks.SCULK_MASS.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkNodeBlockEntity>> SCULK_NODE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_node_block_entity", () -> BlockEntityType.Builder.of(
                    SculkNodeBlockEntity::new, ModBlocks.SCULK_NODE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkAncientNodeBlockEntity>> SCULK_ANCIENT_NODE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_ancient_node_block_entity", () -> BlockEntityType.Builder.of(
                    SculkAncientNodeBlockEntity::new, ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkBeeNestBlockEntity>> SCULK_BEE_NEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_bee_nest_block_entity", () -> BlockEntityType.Builder.of(
                    SculkBeeNestBlockEntity::new, ModBlocks.SCULK_BEE_NEST_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkBeeNestCellBlockEntity>> SCULK_BEE_NEST_CELL_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_bee_nest_cell_block_entity", () -> BlockEntityType.Builder.of(
                    SculkBeeNestCellBlockEntity::new, ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkSummonerBlockEntity>> SCULK_SUMMONER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_summoner_block_entity", () -> BlockEntityType.Builder.of(
                    SculkSummonerBlockEntity::new, ModBlocks.SCULK_SUMMONER_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SculkLivingRockRootBlockEntity>> SCULK_LIVING_ROCK_ROOT_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sculk_living_rock_root_block_entity", () -> BlockEntityType.Builder.of(
                    SculkLivingRockRootBlockEntity::new, ModBlocks.SCULK_LIVING_ROCK_ROOT_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<DevStructureTesterBlockEntity>> DEV_STRUCTURE_TESTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dev_structure_tester_block_entity", () -> BlockEntityType.Builder.of(
                    DevStructureTesterBlockEntity::new, ModBlocks.DEV_STRUCTURE_TESTER_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<DevMassInfectinator3000BlockEntity>> DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dev_mass_infectinator_3000_block_entity", () -> BlockEntityType.Builder.of(
                    DevMassInfectinator3000BlockEntity::new, ModBlocks.DEV_MASS_INFECTINATOR_3000_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<InfestedTagBlockEntity>> INFESTED_LOG_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infested_log_block_entity", () -> BlockEntityType.Builder.of(
                    InfestedTagBlockEntity::new, infestedTagBlocks()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<InfestedTagBlockEntity>> INFESTED_WOOD_MASS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infested_wood_mass_block_entity", () -> BlockEntityType.Builder.of(
                    InfestedTagBlockEntity::new, infestedTagBlocks()).build(null));
    
    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<InfestedTagBlockEntity>> INFESTED_WOOD_STAIRS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("infested_wood_stairs_block_entity", () -> BlockEntityType.Builder.of(
                    InfestedTagBlockEntity::new, infestedTagBlocks()).build(null));

    /**
     * The same persistent tag block entity is used by all infested structural blocks.
     * Keep the three legacy registry IDs, but allow each type to validate every such block
     * so world infestation can safely replace any matching block variant.
     */
    private static Block[] infestedTagBlocks() {
        return new Block[] {
                ModBlocks.INFESTED_LOG.get(),
                ModBlocks.INFESTED_DEEPSLATE.get(),
                ModBlocks.INFESTED_BASALT.get(),
                ModBlocks.INFESTED_WOOD_MASS.get(),
                ModBlocks.INFESTED_WOOD_STAIRS.get(),
                ModBlocks.INFESTED_WOOD_SLAB.get(),
                ModBlocks.INFESTED_WOOD_FENCE.get(),
                ModBlocks.INFESTED_WOOD_FENCE_GATE.get(),
                ModBlocks.INFESTED_STURDY_MASS.get(),
                ModBlocks.INFESTED_STURDY_STAIRS.get(),
                ModBlocks.INFESTED_STURDY_SLAB.get(),
                ModBlocks.INFESTED_STURDY_WALL.get(),
                ModBlocks.INFESTED_STURDY_FENCE.get(),
                ModBlocks.INFESTED_STURDY_FENCE_GATE.get(),
                ModBlocks.INFESTED_CRUMBLING_STAIRS.get(),
                ModBlocks.INFESTED_CRUMBLING_SLAB.get(),
                ModBlocks.INFESTED_CRUMBLING_WALL.get(),
                ModBlocks.INFESTED_CRUMPLED_MASS.get(),
                ModBlocks.INFESTED_COMPOST_MASS.get()
        };
    }

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SoulHarvesterBlockEntity>> SOUL_HARVESTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("soul_harvester_block_entity", () -> BlockEntityType.Builder.of(
                    SoulHarvesterBlockEntity::new, ModBlocks.SOUL_HARVESTER_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<FleshyCompostBlockEntity>> FLESHY_COMPOST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fleshy_compost_block_entity", () -> BlockEntityType.Builder.of(
                    FleshyCompostBlockEntity::new, ModBlocks.PASTY_ORGANIC_MASS.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<StructureCoreBlockEntity>> STRUCTURE_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("structure_core_block_entity", () -> BlockEntityType.Builder.of(
                    StructureCoreBlockEntity::new, ModBlocks.STRUCTURE_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<SouliteCoreBlockEntity>> SOULITE_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("soulite_core_block_entity", () -> BlockEntityType.Builder.of(
                    SouliteCoreBlockEntity::new, ModBlocks.SOULITE_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<FungalShroomCoreBlockEntity>> FUNGAL_SHROOM_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("fungal_shroom_core_block_entity", () -> BlockEntityType.Builder.of(
                    FungalShroomCoreBlockEntity::new, ModBlocks.FUNGAL_SHROOM_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<TendrilCoreBlockEntity>> TENDRIL_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("tendril_core_block_entity", () -> BlockEntityType.Builder.of(
                    TendrilCoreBlockEntity::new, ModBlocks.TENDRIL_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<GolemOfWrathAnimatorBlockEntity>> GOLEM_OF_WRATH_ANIMATOR_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("golem_of_wrath_animator_block_entity", () -> BlockEntityType.Builder.of(
                    GolemOfWrathAnimatorBlockEntity::new, ModBlocks.GOLEM_OF_WRATH_ANIMATOR_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BeeColonyCoreBlockEntity>> BEE_COLONY_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("bee_colony_core_block_entity", () -> BlockEntityType.Builder.of(
                    BeeColonyCoreBlockEntity::new, ModBlocks.BEE_COLONY_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BroodNestBlockEntity>> BROOD_NEST_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("brood_nest_block_entity", () -> BlockEntityType.Builder.of(
                    BroodNestBlockEntity::new, ModBlocks.BROOD_NEST_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<BroodNestCoreBlockEntity>> BROOD_NEST_CORE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("brood_nest_core_block_entity", () -> BlockEntityType.Builder.of(
                    BroodNestCoreBlockEntity::new, ModBlocks.BROOD_NEST_CORE_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PerimeterWardRelayBlockEntity>> PERIMETER_WARD_RELAY_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("perimeter_ward_relay_block_entity", () -> BlockEntityType.Builder.of(
                    PerimeterWardRelayBlockEntity::new, ModBlocks.PERIMETER_WARD_RELAY_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<PerimeterWardEmitterBlockEntity>> PERIMETER_WARD_EMITTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("perimeter_ward_emitter_block_entity", () -> BlockEntityType.Builder.of(
                    PerimeterWardEmitterBlockEntity::new, ModBlocks.PERIMETER_WARD_EMITTER_BLOCK.get()).build(null));

    public static DeferredHolder<BlockEntityType<?>, BlockEntityType<CreativeInfestationSpreaderBlockEntity>> CREATIVE_INFESTATION_SPREADER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("creative_infestation_spreader_block_entity", () -> BlockEntityType.Builder.of(
                    CreativeInfestationSpreaderBlockEntity::new, ModBlocks.CREATIVE_INFESTATION_SPREADER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
