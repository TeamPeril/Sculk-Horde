package com.github.sculkhoard.core;

import com.github.sculkhoard.common.block.BlockInfestation.SpreadingTile;
import com.github.sculkhoard.common.tileentity.*;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegistry {

    public static DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, SculkHoard.MOD_ID);

    public static RegistryObject<TileEntityType<InfectedDirtTile>> INFECTED_DIRT_TILE =
            TILE_ENTITIES.register("infected_dirt_tile", () -> TileEntityType.Builder.of(
                    InfectedDirtTile::new, BlockRegistry.INFECTED_DIRT.get()).build(null));

    public static RegistryObject<TileEntityType<InfestedLogTile>> INFESTED_LOG_TILE =
            TILE_ENTITIES.register("infested_log_tile", () -> TileEntityType.Builder.of(
                    InfestedLogTile::new, BlockRegistry.INFESTED_LOG_ACTIVE.get()).build(null));

    public static RegistryObject<TileEntityType<InfestedStoneActiveTile>> INFESTED_STONE_ACTIVE_TILE =
            TILE_ENTITIES.register("infested_stone_active_tile", () -> TileEntityType.Builder.of(
                    InfestedStoneActiveTile::new, BlockRegistry.INFESTED_STONE_ACTIVE.get()).build(null));

    public static RegistryObject<TileEntityType<SculkMassTile>> SCULK_MASS_TILE =
            TILE_ENTITIES.register("sculk_mass_tile", () -> TileEntityType.Builder.of(
                    SculkMassTile::new, BlockRegistry.SCULK_MASS.get()).build(null));

    public static RegistryObject<TileEntityType<SculkNodeTile>> SCULK_BRAIN_TILE =
            TILE_ENTITIES.register("sculk_brain_tile", () -> TileEntityType.Builder.of(
                    SculkNodeTile::new, BlockRegistry.SCULK_NODE_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<SpreadingTile>> SPREADING_BLOCK_TILE =
            TILE_ENTITIES.register("spreading_block_tile", () -> TileEntityType.Builder.of(
                    SpreadingTile::new, BlockRegistry.SPREADING_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<SculkBeeNestTile>> SCULK_BEE_NEST_TILE =
            TILE_ENTITIES.register("sculk_bee_nest_tile", () -> TileEntityType.Builder.of(
                    SculkBeeNestTile::new, BlockRegistry.SCULK_BEE_NEST_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<SculkBeeNestCellTile>> SCULK_BEE_NEST_CELL_TILE =
            TILE_ENTITIES.register("sculk_bee_nest_cell_tile", () -> TileEntityType.Builder.of(
                    SculkBeeNestCellTile::new, BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<SculkSummonerTile>> SCULK_SUMMONER_TILE =
            TILE_ENTITIES.register("sculk_summoner_tile", () -> TileEntityType.Builder.of(
                    SculkSummonerTile::new, BlockRegistry.SCULK_SUMMONER_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<SculkLivingRockRootTile>> SCULK_LIVING_ROCK_ROOT_TILE =
            TILE_ENTITIES.register("sculk_living_rock_root_tile", () -> TileEntityType.Builder.of(
                    SculkLivingRockRootTile::new, BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get()).build(null));

    public static RegistryObject<TileEntityType<DevStructureTesterTile>> DEV_STRUCTURE_TESTER_TILE =
            TILE_ENTITIES.register("dev_structure_tester_tile", () -> TileEntityType.Builder.of(
                    DevStructureTesterTile::new, BlockRegistry.DEV_STRUCTURE_TESTER_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        TILE_ENTITIES.register(eventBus);
    }
}
