package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.IPlantable;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class BlockInfestationHelper {

    public static void initializeInfestationTables()
    {
        // Used to infect blocks that are explicitly listed. Order Matters
        SculkHorde.explicitInfectableBlocks = new BlockInfestationTable(false);

        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIRT, Blocks.SCULK.defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COARSE_DIRT, Blocks.SCULK.defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRASS_BLOCK, Blocks.SCULK.defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIRT_PATH, Blocks.SCULK.defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.PODZOL, Blocks.SCULK.defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CLAY, ModBlocks.INFESTED_CLAY.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE, ModBlocks.INFESTED_STONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_STAIRS, ModBlocks.INFESTED_STONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_SLAB, ModBlocks.INFESTED_STONE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DEEPSLATE, ModBlocks.INFESTED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLED_DEEPSLATE, ModBlocks.INFESTED_COBBLED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLED_DEEPSLATE_STAIRS, ModBlocks.INFESTED_COBBLED_DEEPSLATE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLED_DEEPSLATE_SLAB, ModBlocks.INFESTED_COBBLED_DEEPSLATE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLED_DEEPSLATE_WALL, ModBlocks.INFESTED_COBBLED_DEEPSLATE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SAND, ModBlocks.INFESTED_SAND.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SANDSTONE, ModBlocks.INFESTED_SANDSTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SANDSTONE_STAIRS, ModBlocks.INFESTED_SANDSTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SANDSTONE_SLAB, ModBlocks.INFESTED_SANDSTONE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SANDSTONE_WALL, ModBlocks.INFESTED_SANDSTONE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.RED_SAND, ModBlocks.INFESTED_RED_SAND.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIORITE, ModBlocks.INFESTED_DIORITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIORITE_STAIRS, ModBlocks.INFESTED_DIORITE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIORITE_SLAB, ModBlocks.INFESTED_DIORITE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIORITE_WALL, ModBlocks.INFESTED_DIORITE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRANITE, ModBlocks.INFESTED_GRANITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRANITE_STAIRS, ModBlocks.INFESTED_GRANITE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRANITE_SLAB, ModBlocks.INFESTED_GRANITE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRANITE_WALL, ModBlocks.INFESTED_GRANITE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ANDESITE, ModBlocks.INFESTED_ANDESITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ANDESITE_STAIRS, ModBlocks.INFESTED_ANDESITE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ANDESITE_SLAB, ModBlocks.INFESTED_ANDESITE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ANDESITE_WALL, ModBlocks.INFESTED_ANDESITE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.TUFF, ModBlocks.INFESTED_TUFF.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CALCITE, ModBlocks.INFESTED_CALCITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE, ModBlocks.INFESTED_COBBLESTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE_STAIRS, ModBlocks.INFESTED_COBBLESTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE_SLAB, ModBlocks.INFESTED_COBBLESTONE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE_WALL, ModBlocks.INFESTED_COBBLESTONE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE, ModBlocks.INFESTED_MOSSY_COBBLESTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE_STAIRS, ModBlocks.INFESTED_MOSSY_COBBLESTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE_SLAB, ModBlocks.INFESTED_MOSSY_COBBLESTONE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE_WALL, ModBlocks.INFESTED_MOSSY_COBBLESTONE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRAVEL, ModBlocks.INFESTED_GRAVEL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD, ModBlocks.INFESTED_MUD.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.PACKED_MUD, ModBlocks.INFESTED_PACKED_MUD.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD_BRICKS, ModBlocks.INFESTED_MUD_BRICKS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD_BRICK_STAIRS, ModBlocks.INFESTED_MUD_BRICK_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD_BRICK_SLAB, ModBlocks.INFESTED_MUD_BRICK_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD_BRICK_WALL, ModBlocks.INFESTED_MUD_BRICK_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SNOW_BLOCK, ModBlocks.INFESTED_SNOW.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSS_BLOCK, ModBlocks.INFESTED_MOSS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.TERRACOTTA, ModBlocks.INFESTED_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLACK_TERRACOTTA, ModBlocks.INFESTED_BLACK_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLUE_TERRACOTTA, ModBlocks.INFESTED_BLUE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BROWN_TERRACOTTA, ModBlocks.INFESTED_BROWN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CYAN_TERRACOTTA, ModBlocks.INFESTED_CYAN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRAY_TERRACOTTA, ModBlocks.INFESTED_GRAY_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GREEN_TERRACOTTA, ModBlocks.INFESTED_GREEN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.LIGHT_BLUE_TERRACOTTA, ModBlocks.INFESTED_LIGHT_BLUE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.LIGHT_GRAY_TERRACOTTA, ModBlocks.INFESTED_LIGHT_GRAY_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.LIME_TERRACOTTA, ModBlocks.INFESTED_LIME_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MAGENTA_TERRACOTTA, ModBlocks.INFESTED_MAGENTA_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ORANGE_TERRACOTTA, ModBlocks.INFESTED_ORANGE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.PINK_TERRACOTTA, ModBlocks.INFESTED_PINK_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.PURPLE_TERRACOTTA, ModBlocks.INFESTED_PURPLE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.RED_TERRACOTTA, ModBlocks.INFESTED_RED_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.WHITE_TERRACOTTA, ModBlocks.INFESTED_WHITE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.YELLOW_TERRACOTTA, ModBlocks.INFESTED_YELLOW_TERRACOTTA.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CRYING_OBSIDIAN, ModBlocks.INFESTED_CRYING_OBSIDIAN.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.NETHERRACK, ModBlocks.INFESTED_NETHERRACK.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CRIMSON_NYLIUM, ModBlocks.INFESTED_CRIMSON_NYLIUM.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.WARPED_NYLIUM, ModBlocks.INFESTED_WARPED_NYLIUM.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLACKSTONE, ModBlocks.INFESTED_BLACKSTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLACKSTONE_STAIRS, ModBlocks.INFESTED_BLACKSTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLACKSTONE_SLAB, ModBlocks.INFESTED_BLACKSTONE_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BLACKSTONE_WALL, ModBlocks.INFESTED_BLACKSTONE_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.POLISHED_BLACKSTONE_BRICKS, ModBlocks.INFESTED_BLACKSTONE_BRICKS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, ModBlocks.INFESTED_BLACKSTONE_BRICK_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, ModBlocks.INFESTED_BLACKSTONE_BRICK_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.POLISHED_BLACKSTONE_BRICK_WALL, ModBlocks.INFESTED_BLACKSTONE_BRICK_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_BRICKS, ModBlocks.INFESTED_STONE_BRICKS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_BRICK_STAIRS, ModBlocks.INFESTED_STONE_BRICK_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_BRICK_SLAB, ModBlocks.INFESTED_STONE_BRICK_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.STONE_BRICK_WALL, ModBlocks.INFESTED_STONE_BRICK_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_STONE_BRICKS, ModBlocks.INFESTED_MOSSY_STONE_BRICKS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_STONE_BRICK_STAIRS, ModBlocks.INFESTED_MOSSY_STONE_BRICK_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_STONE_BRICK_SLAB, ModBlocks.INFESTED_MOSSY_STONE_BRICK_SLAB.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_STONE_BRICK_WALL, ModBlocks.INFESTED_MOSSY_STONE_BRICK_WALL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BASALT, ModBlocks.INFESTED_BASALT.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SMOOTH_BASALT, ModBlocks.INFESTED_SMOOTH_BASALT.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.END_STONE, ModBlocks.INFESTED_ENDSTONE.get().defaultBlockState());
        if(ModConfig.isExperimentalFeaturesEnabled()) { SculkHorde.explicitInfectableBlocks.addEntry(Blocks.KELP_PLANT, ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState()); }
        if(ModConfig.isExperimentalFeaturesEnabled()) { SculkHorde.explicitInfectableBlocks.addEntry(Blocks.KELP, ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState()); }

        // Deeper and Darker Compatibility
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate", "deeperdarker:sculk_stone");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_coal_ore", "deeperdarker:sculk_stone_coal_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_iron_ore", "deeperdarker:sculk_stone_iron_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_gold_ore", "deeperdarker:sculk_stone_gold_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_copper_ore", "deeperdarker:sculk_stone_copper_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_lapis_ore", "deeperdarker:sculk_stone_lapis_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_redstone_ore", "deeperdarker:sculk_stone_redstone_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_emerald_ore", "deeperdarker:sculk_stone_emerald_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_diamond_ore", "deeperdarker:sculk_stone_diamond_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:moss_block", "deeperdarker:echo_soil");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:flowering_azalea_leaves", "deeperdarker:echo_leaves");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:oak_log", "deeperdarker:echo_log");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:pearlescent_froglight", "deeperdarker:sculk_gleam");

        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:blackstone", "deeperdarker:gloomslate");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_coal_ore", "deeperdarker:gloomslate_coal_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_iron_ore", "deeperdarker:gloomslate_iron_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_gold_ore", "deeperdarker:gloomslate_gold_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_copper_ore", "deeperdarker:gloomslate_copper_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_lapis_ore", "deeperdarker:gloomslate_lapis_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_redstone_ore", "deeperdarker:gloomslate_redstone_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_emerald_ore", "deeperdarker:gloomslate_emerald_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate_diamond_ore", "deeperdarker:gloomslate_diamond_ore");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:lava", "deeperdarker:gloomy_geyser");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:smooth_basalt", "deeperdarker:gloomy_sculk");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:ochre_froglight", "deeperdarker:crystallized_amber");

        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:deepslate", "deeperdarker:sculk_grime");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:air", "deeperdarker:sculk_tendrils");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:cave_vines", "deeperdarker:sculk_vines");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:mud", "deeperdarker:sculk_jaw");

        // Remove Sculk Vein
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:air", "minecraft:sculk_vein");
        SculkHorde.explicitInfectableBlocks.addEntry("minecraft:air", "sculkhorde:tendrils");



        // Used to infect non-full blocks. Order Matters
        SculkHorde.tagInfectableNonFullBlocks = new BlockInfestationTable(false);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.STAIRS, BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_STAIRS.get(), Blocks.COBBLESTONE_STAIRS);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.STAIRS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_STAIRS.get(), Blocks.MOSSY_COBBLESTONE_STAIRS); //vanilla doesn't have any stairs that match these criteria so this is about the best i could get
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.WOODEN_SLABS, ModBlocks.INFESTED_WOOD_SLAB.get(), Blocks.OAK_SLAB);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.SLABS, BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_SLAB.get(), Blocks.COBBLESTONE_SLAB);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.SLABS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_SLAB.get(), Blocks.MOSSY_COBBLESTONE_SLAB);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.WALLS, ModBlocks.BlockTags.CONVERTS_TO_CRUMBLING_VARIANT, Tiers.IRON, ModBlocks.INFESTED_CRUMBLING_WALL.get(), Blocks.MOSSY_COBBLESTONE_WALL);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.WALLS, Tiers.IRON, ModBlocks.INFESTED_STURDY_WALL.get(), Blocks.COBBLESTONE_WALL);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.WOODEN_FENCES, ModBlocks.INFESTED_WOOD_FENCE.get(), Blocks.OAK_FENCE);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.FENCES, ModBlocks.INFESTED_STURDY_FENCE.get(), Blocks.NETHER_BRICK_FENCE);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.FENCE_GATES, BlockTags.MINEABLE_WITH_AXE, Tiers.IRON, ModBlocks.INFESTED_WOOD_FENCE_GATE.get(), Blocks.OAK_FENCE_GATE);
        SculkHorde.tagInfectableNonFullBlocks.addEntry(BlockTags.FENCE_GATES, ModBlocks.INFESTED_STURDY_FENCE_GATE.get(), Blocks.OAK_FENCE_GATE);

        // Used to infect generic types of blocks like wood-like, stone-like, etc. Order Matters
        SculkHorde.tagInfectableBlocks = new BlockInfestationTable(true);
        SculkHorde.tagInfectableBlocks.addEntry(net.minecraft.tags.BlockTags.LOGS, ModBlocks.INFESTED_LOG.get(), Blocks.OAK_LOG);
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_AXE, ModBlocks.INFESTED_WOOD_MASS.get(), Blocks.OAK_PLANKS);
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_MASS.get(), Blocks.COBBLESTONE);
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_SHOVEL, Tiers.IRON, ModBlocks.INFESTED_CRUMPLED_MASS.get(), Blocks.COARSE_DIRT);
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_HOE, Tiers.IRON, ModBlocks.INFESTED_COMPOST_MASS.get(), Blocks.MOSS_BLOCK);

        SculkHorde.configInfectableBlocks = new BlockInfestationTable(false);
        SculkHorde.configInfectableBlocks.addEntry(ModBlocks.INFESTED_STURDY_MASS.get());

        SculkHorde.INFESTATION_TABLES = new BlockInfestationTable[]{
                SculkHorde.explicitInfectableBlocks,
                SculkHorde.tagInfectableNonFullBlocks,
                SculkHorde.tagInfectableBlocks,
                SculkHorde.configInfectableBlocks
        };
    }

    public static boolean isExplicitlyNotInfectable(BlockState blockState)
    {
        return blockState.is(ModBlocks.BlockTags.NOT_INFESTABLE) ||
                blockState.is(ModBlocks.BlockTags.INFESTED_BLOCK) ||
                blockState.isAir() ||
                blockState.hasBlockEntity();
    }

    public static boolean isInfectable(ServerLevel level, BlockPos pos)
    {
        BlockState blockState = level.getBlockState(pos);
        if(isExplicitlyNotInfectable(blockState))
        {
            return false;
        }

        for(BlockInfestationTable table : SculkHorde.INFESTATION_TABLES)
        {
            if(table.canBeInfectedByThisTable(level, pos))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isCurable(ServerLevel level, BlockPos pos)
    {
        for(BlockInfestationTable table : SculkHorde.INFESTATION_TABLES)
        {
            if(table.getNormalVariant(level, pos) != null)
            {
                return true;
            }
        }

        return false;
    }

    public static void tryToInfestBlock(ServerLevel world, BlockPos targetPos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        BlockState victimBlockState = world.getBlockState(targetPos);
        boolean wasAbleToInfestBlock = false;

        if(isExplicitlyNotInfectable(victimBlockState))
        {
            return;
        }

        for(BlockInfestationTable table : SculkHorde.INFESTATION_TABLES)
        {
            if(table.canBeInfectedByThisTable(world, targetPos))
            {
                wasAbleToInfestBlock = table.infectBlock(world, targetPos);
                break;
            }
        }


        // If we did not successfully infect the block, return
        if(!wasAbleToInfestBlock)
        {
            return;
        }

        world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        world.playSound(null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        BlockInfestationHelper.removeNearbyVein(world, targetPos);

        BlockInfestationHelper.placeSculkFlora(world, targetPos);

        // Chance to place a sculk node above the block
        SculkNodeBlock.tryPlaceSculkNode(world, targetPos, false);

        // Chance to place a sculk bee hive above the block
        BlockInfestationHelper.tryPlaceSculkBeeHive(world, targetPos.above());

        BlockInfestationHelper.tryPlaceDiseasedKelp(world, targetPos.above());
    }

    public static boolean tryToCureBlock(ServerLevel world, BlockPos targetPos)
    {
        boolean wasAbleToCureBlock = false;
        BlockState getNormalVariant = null;

        if(!isCurable(world, targetPos)) { return false; }

        for(BlockInfestationTable table : SculkHorde.INFESTATION_TABLES)
        {
            getNormalVariant = table.getNormalVariant(world, targetPos);

            if(getNormalVariant == null) { continue; }

            wasAbleToCureBlock = true;

            break;
        }

        // If we did not successfully cure the block, return
        if(!wasAbleToCureBlock)
        {
            return false;
        }

        // Convert Block
        world.setBlockAndUpdate(targetPos, getNormalVariant);

        if(shouldBeRemovedFromAboveBlock.test(world.getBlockState(targetPos.above())))
        {
            world.setBlockAndUpdate(targetPos.above(), Blocks.AIR.defaultBlockState());
        }

        boolean canCuredBlockSustatinPlant = world.getBlockState(targetPos).canSustainPlant(world, targetPos, Direction.UP, (IPlantable) Blocks.POPPY);
        Random rand = new Random();
        if(rand.nextBoolean() && canCuredBlockSustatinPlant && world.getBlockState(targetPos.above()).isAir())
        {
            world.setBlockAndUpdate(targetPos.above(), Blocks.GRASS.defaultBlockState());
        }

        return true;
    }

    /**
     * Determines if a blockstate is considered to be sculk Flora
     * @return True if Valid, False otherwise
     */
    public static Predicate<BlockState> shouldBeRemovedFromAboveBlock = (b) ->
    {
        if (b.is(ModBlocks.GRASS.get()))
        {
            return true;
        }

        if(b.is(ModBlocks.GRASS_SHORT.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SMALL_SHROOM.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SHROOM_CULTURE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SPIKE.get()))
        {
            return true;
        }

        if( b.is(ModBlocks.SCULK_SUMMONER_BLOCK.get()))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_CATALYST))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SHRIEKER))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_VEIN))
        {
            return true;
        }

        if(b.is(Blocks.SCULK_SENSOR))
        {
            return true;
        }

        if(b.is(ModBlocks.TENDRILS.get()))
        {
            return true;
        }

        return false;
    };

    public static void removeNearbyVein(ServerLevel world, BlockPos position)
    {
        // Update each adjacent block if it is a sculk vein
        // This is to prevent vein from staying on blocks that it does not belong on.
        List<BlockPos> adjacentBlockPos = BlockAlgorithms.getAdjacentNeighbors(position);
        for(BlockPos neighbors : adjacentBlockPos)
        {
            BlockState blockState = world.getBlockState(neighbors);
            if(blockState.getBlock() == ModBlocks.TENDRILS.get())
            {
                if(!blockState.getBlock().canSurvive(blockState, world, neighbors))
                    world.destroyBlock(neighbors, false);

            }
        }
    }

    public static void placeSculkFlora(ServerLevel world, BlockPos position)
    {
        // Given a 25% chance, place down sculk flora on block
        if (world.random.nextInt(4) <= 0)
        {
            BlockAlgorithms.tryPlaceSculkFlora(position.above(), world);
        }
    }

    public static boolean blockIsAirOrSnow(BlockState state)
    {
        return state.isAir() || state.getBlock() == Blocks.SNOW;
    }

    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void tryPlaceSculkBeeHive(ServerLevel world, BlockPos targetPos)
    {

        //Given random chance and the target location can see the sky, create a sculk hive
        if(new Random().nextInt(4000) <= 1 && blockIsAirOrSnow(world.getBlockState(targetPos)) && blockIsAirOrSnow(world.getBlockState(targetPos.above())) && blockIsAirOrSnow(world.getBlockState(targetPos.above().above())))
        {
            world.setBlockAndUpdate(targetPos, ModBlocks.SCULK_BEE_NEST_BLOCK.get().defaultBlockState());
            SculkBeeNestBlockEntity nest = (SculkBeeNestBlockEntity) world.getBlockEntity(targetPos);

            //Add bees
            nest.addFreshInfectorOccupant();
            nest.addFreshInfectorOccupant();
            nest.addFreshHarvesterOccupant();
            nest.addFreshHarvesterOccupant();
        }

    }

    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void tryPlaceDiseasedKelp(ServerLevel world, BlockPos targetPos)
    {

        //Given random chance and the target location can see the sky, create a sculk hive
        if(ModConfig.isExperimentalFeaturesEnabled() && new Random().nextInt(30) <= 1 && world.getFluidState(targetPos).is(Fluids.WATER))
        {
            boolean isTargetPosEmptyWater = world.getBlockState(targetPos).is(Blocks.WATER);
            if(!isTargetPosEmptyWater) { return; }


            int height = world.random.nextInt(25);
            for(int i = 0; i < height && !world.getBlockState(targetPos.above(i + 1)).getFluidState().isEmpty(); i++)
            {
                world.setBlockAndUpdate(targetPos.above(i), ModBlocks.DISEASED_KELP_BLOCK.get().defaultBlockState());
            }
        }

    }
}
