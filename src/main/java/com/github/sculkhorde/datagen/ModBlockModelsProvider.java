package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.Minecraft;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.RegistryObject;
import oshi.util.tuples.Pair;

public class ModBlockModelsProvider extends BlockModelProvider {

    public ModBlockModelsProvider(PackOutput output, String modid, ExistingFileHelper existingFileHelper) {
        super(output, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        SculkHorde.LOGGER.info("to datagen:");
        ModBlocks.BLOCKS_TO_DATAGEN.stream().map(pair -> pair.getA().get() + " " + pair.getB() + ", ").forEach(SculkHorde.LOGGER::info);
        for (Pair<RegistryObject<? extends Block>, ResourceLocation> pair : ModBlocks.BLOCKS_TO_DATAGEN) {
            if (pair.getA().get() instanceof StairBlock) {
                stairsAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof SlabBlock) {
                slabAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else if (pair.getA().get() instanceof WallBlock) {
                if (pair.getA() == ModBlocks.INFESTED_BLACKSTONE_WALL) { //i cant be bothered to make a better system for this
                    String name = pair.getA().getId().getPath();
                    withExistingParent(name, BLOCK_FOLDER + "/template_wall_post")
                            .texture("side", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_side"))
                            .texture("bottom", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"))
                            .texture("top", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"));
                    withExistingParent(name, BLOCK_FOLDER + "/template_wall_side")
                            .texture("side", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_side"))
                            .texture("bottom", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"))
                            .texture("top", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"));
                    withExistingParent(name, BLOCK_FOLDER + "/template_wall_side_tall")
                            .texture("side", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_side"))
                            .texture("bottom", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"))
                            .texture("top", new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"));
                    wallInventory(name, new ResourceLocation(SculkHorde.MOD_ID, "block/infested_blackstone_top"));
                    continue; //TODO make a better system for this
                }
                wallAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            } else {
                cubeAll(pair.getA().getId().getPath(), pair.getB().withPrefix("block/"));
            }
        }
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
