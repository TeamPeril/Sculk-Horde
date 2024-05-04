package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.ModBlocks;
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
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DEEPSLATE, ModBlocks.INFESTED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLED_DEEPSLATE, ModBlocks.INFESTED_COBBLED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SAND, ModBlocks.INFESTED_SAND.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SANDSTONE, ModBlocks.INFESTED_SANDSTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.RED_SAND, ModBlocks.INFESTED_RED_SAND.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.DIORITE, ModBlocks.INFESTED_DIORITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRANITE, ModBlocks.INFESTED_GRANITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.ANDESITE, ModBlocks.INFESTED_ANDESITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.TUFF, ModBlocks.INFESTED_TUFF.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.CALCITE, ModBlocks.INFESTED_CALCITE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE, ModBlocks.INFESTED_COBBLESTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.COBBLESTONE_STAIRS, ModBlocks.INFESTED_COBBLESTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE, ModBlocks.INFESTED_MOSSY_COBBLESTONE.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MOSSY_COBBLESTONE_STAIRS, ModBlocks.INFESTED_MOSSY_COBBLESTONE_STAIRS.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.GRAVEL, ModBlocks.INFESTED_GRAVEL.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD, ModBlocks.INFESTED_MUD.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.PACKED_MUD, ModBlocks.INFESTED_PACKED_MUD.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.MUD_BRICKS, ModBlocks.INFESTED_MUD_BRICKS.get().defaultBlockState());
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
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.BASALT, ModBlocks.INFESTED_BASALT.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.SMOOTH_BASALT, ModBlocks.INFESTED_SMOOTH_BASALT.get().defaultBlockState());
        SculkHorde.explicitInfectableBlocks.addEntry(Blocks.END_STONE, ModBlocks.INFESTED_ENDSTONE.get().defaultBlockState());

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



        // Used to infect stairs and slabs. Order Matters
        SculkHorde.tagInfectableStairsAndSlabsBlocks = new BlockInfestationTable(false);
        SculkHorde.tagInfectableStairsAndSlabsBlocks.addEntry(BlockTags.WOODEN_STAIRS, ModBlocks.INFESTED_WOOD_STAIRS.get());

        // Used to infect generic types of blocks like wood-like, stone-like, etc. Order Matters
        SculkHorde.tagInfectableBlocks = new BlockInfestationTable(true);
        SculkHorde.tagInfectableBlocks.addEntry(net.minecraft.tags.BlockTags.LOGS, ModBlocks.INFESTED_LOG.get());
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_AXE, ModBlocks.INFESTED_WOOD_MASS.get());
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_MASS.get());
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_SHOVEL, Tiers.IRON, ModBlocks.INFESTED_CRUMPLED_MASS.get());
        SculkHorde.tagInfectableBlocks.addEntry(BlockTags.MINEABLE_WITH_HOE, Tiers.IRON, ModBlocks.INFESTED_COMPOST_MASS.get());

        SculkHorde.configInfectableBlocks = new BlockInfestationTable(false);
        SculkHorde.configInfectableBlocks.addEntry(ModBlocks.INFESTED_STURDY_MASS.get());

        SculkHorde.INFESTATION_TABLES = new BlockInfestationTable[]{
                SculkHorde.explicitInfectableBlocks,
                SculkHorde.tagInfectableStairsAndSlabsBlocks,
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
}
