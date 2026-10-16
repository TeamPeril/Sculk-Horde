package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredHolder;
import oshi.util.tuples.Pair;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Pair<DeferredHolder<Block, ? extends Block>, ResourceLocation> pair : ModBlocks.BLOCKS_TO_DATAGEN) {
            Block block = pair.getA().get();
            String name = pair.getA().getId().getPath();
            ResourceLocation texture = pair.getB().withPrefix("block/");

            if (block instanceof StairBlock stairs) {
                stairsBlock(stairs, texture);
                simpleBlockItem(stairs, models().getExistingFile(modLoc("block/" + name)));
            } else if (block instanceof SlabBlock slab) {
                // Uses the double-slab model if existing, otherwise uses texture for the slab models
                slabBlock(slab, texture, texture);
                simpleBlockItem(slab, models().getExistingFile(modLoc("block/" + name)));
            } else if (block instanceof WallBlock wall) {
                wallBlock(wall, texture);
                itemModels().wallInventory(name, texture);
            } else if (block instanceof FenceBlock fence) {
                fenceBlock(fence, texture);
                itemModels().fenceInventory(name, texture);
            } else if (block instanceof FenceGateBlock fenceGate) {
                fenceGateBlock(fenceGate, texture);
                simpleBlockItem(fenceGate, models().getExistingFile(modLoc("block/" + name)));
            } else {
                ModelFile model = models().cubeAll(name, texture);
                simpleBlock(block, model);
                simpleBlockItem(block, model);
            }
        }
    }
}
