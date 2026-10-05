package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import oshi.util.tuples.Pair;

public class ModBlockModelsProvider extends BlockModelProvider {

    public ModBlockModelsProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (Pair<DeferredHolder<Block, ? extends Block>, ResourceLocation> pair : ModBlocks.BLOCKS_TO_DATAGEN) {
            if (pair.getA().get() instanceof StairBlock) {
                stairsAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof SlabBlock) {
                slabAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof WallBlock) {
                wallAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof FenceBlock) {
                fenceAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof FenceGateBlock) {
                fenceGateAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else {
                cubeAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            }
        }
    }

    private void fenceGateAll(String name, ResourceLocation texture) {
        fenceGate(name, texture);
        fenceGateOpen(name, texture);
        fenceGateWall(name, texture);
        fenceGateWallOpen(name, texture);
    }

    private void fenceAll(String name, ResourceLocation texture) {
        fencePost(name, texture);
        fenceSide(name, texture);
        fenceInventory(name, texture);
    }

    private void stairsAll(String name, ResourceLocation texture) {
        stairs(name, texture, texture, texture);
        stairsInner(name, texture, texture, texture);
        stairsOuter(name, texture, texture, texture);
    }

    private void slabAll(String name, ResourceLocation texture) {
        slab(name, texture, texture, texture);
        slabTop(name, texture, texture, texture);
    }

    private void wallAll(String name, ResourceLocation texture) {
        wallPost(name, texture);
        wallSide(name, texture);
        wallSideTall(name, texture);
        wallInventory(name, texture);
    }
}
