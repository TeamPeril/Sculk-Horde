package com.github.sculkhoard.core;

import com.github.sculkhoard.common.tileentity.InfectedDirtTile;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TileEntityRegistry {

    public static DeferredRegister<TileEntityType<?>> TILE_ENTITIES =
            DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, SculkHoard.MOD_ID);

    public static RegistryObject<TileEntityType<InfectedDirtTile>> INFECTED_DIRT_TILE =
            TILE_ENTITIES.register("infected_dirt_tile", () -> TileEntityType.Builder.of(
                    InfectedDirtTile::new, BlockRegistry.INFECTED_DIRT.get()).build(null));

    public static void register(IEventBus eventBus) {
        TILE_ENTITIES.register(eventBus);
    }
}
