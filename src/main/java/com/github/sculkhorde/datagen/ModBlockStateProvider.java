package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import oshi.util.tuples.Pair;

public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, String modid, ExistingFileHelper exFileHelper) {
        super(output, modid, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (Pair<RegistryObject<? extends Block>, ResourceLocation> pair : ModBlocks.BLOCKS_TO_DATAGEN) {
            if (pair.getA().get() instanceof StairBlock stairs) {
                stairsBlock(stairs, pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof SlabBlock slab) {
                slabBlock(slab, pair.getB(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof WallBlock wall) {
                if (pair.getA() == ModBlocks.INFESTED_BLACKSTONE_WALL) {
                    wallBlock(wall, models().wallPost("infested_blackstone_wall_post", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top")),
                            models().wallSide("infested_blackstone_wall_side", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_side")),
                            models().wallSideTall("infested_blackstone_wall_side_tall", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top")));
                    simpleBlockItem(pair.getA().get(), models().getExistingFile(pair.getA().getId()));
                    continue;
                }
                wallBlock(wall, pair.getB().withPrefix("block/"));
            } else {
                simpleBlock(pair.getA().get(), models().cubeAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/")));
            }
            simpleBlockItem(pair.getA().get(), models().getExistingFile(pair.getA().getId()));
        }
    }
}
