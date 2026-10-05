package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
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
            if (pair.getA().get() instanceof StairBlock stairs) {
                stairsBlock(stairs, pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof SlabBlock slab) {
                slabBlock(slab, pair.getB(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof WallBlock wall) {
                wallBlock(wall, pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof FenceBlock fence) {
                fenceBlock(fence, pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof FenceGateBlock fenceGate) {
                fenceGateBlock(fenceGate, pair.getB().withPrefix("block/"));
            } else {
                simpleBlock(pair.getA().get(), models().cubeAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/")));
            }
            simpleBlockItem(pair.getA().get(), models().getExistingFile(pair.getA().getId()));
        }
    }
}
