package com.github.sculkhorde.core;

import com.github.sculkhorde.common.blockentity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TileEntityRegistry {

    public static DeferredRegister<BlockEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SculkHorde.MOD_ID);

    public static RegistryObject<BlockEntityType<SculkMassBlockEntity>> SCULK_MASS_TILE =
            TILE_ENTITIES.register("sculk_mass_tile", () -> BlockEntityType.Builder.of(
                    SculkMassBlockEntity::new, BlockRegistry.SCULK_MASS.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkNodeBlockEntity>> SCULK_NODE_BLOCK_ENTITY =
            TILE_ENTITIES.register("sculk_node_block_entity", () -> BlockEntityType.Builder.of(
                    SculkNodeBlockEntity::new, BlockRegistry.SCULK_NODE_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkBeeNestTile>> SCULK_BEE_NEST_TILE =
            TILE_ENTITIES.register("sculk_bee_nest_tile", () -> BlockEntityType.Builder.of(
                    SculkBeeNestTile::new, BlockRegistry.SCULK_BEE_NEST_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkBeeNestCellTile>> SCULK_BEE_NEST_CELL_TILE =
            TILE_ENTITIES.register("sculk_bee_nest_cell_tile", () -> BlockEntityType.Builder.of(
                    SculkBeeNestCellTile::new, BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkSummonerBlockEntity>> SCULK_SUMMONER_TILE =
            TILE_ENTITIES.register("sculk_summoner_tile", () -> BlockEntityType.Builder.of(
                    SculkSummonerBlockEntity::new, BlockRegistry.SCULK_SUMMONER_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<SculkLivingRockRootTile>> SCULK_LIVING_ROCK_ROOT_TILE =
            TILE_ENTITIES.register("sculk_living_rock_root_tile", () -> BlockEntityType.Builder.of(
                    SculkLivingRockRootTile::new, BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get()).build(null));

    public static RegistryObject<BlockEntityType<DevStructureTesterTile>> DEV_STRUCTURE_TESTER_TILE =
            TILE_ENTITIES.register("dev_structure_tester_tile", () -> BlockEntityType.Builder.of(
                    DevStructureTesterTile::new, BlockRegistry.DEV_STRUCTURE_TESTER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        TILE_ENTITIES.register(eventBus);
    }
}
