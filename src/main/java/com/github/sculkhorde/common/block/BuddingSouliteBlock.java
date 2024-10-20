package com.github.sculkhorde.common.block;

import com.github.sculkhorde.core.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeBlock;

public class BuddingSouliteBlock extends HalfTransparentBlock implements IForgeBlock {

    /**
     * HARDNESS determines how difficult a block is to break<br>
     * 0.6f = dirt<br>
     * 1.5f = stone<br>
     * 2f = log<br>
     * 3f = iron ore<br>
     * 50f = obsidian
     */
    public static float HARDNESS = 2f;

    /**
     * BLAST_RESISTANCE determines how difficult a block is to blow up<br>
     * 0.5f = dirt<br>
     * 2f = wood<br>
     * 6f = cobblestone<br>
     * 1,200f = obsidian
     */
    public static float BLAST_RESISTANCE = 2f;


    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public BuddingSouliteBlock(Properties prop) {
        super(prop);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering blocks in BlockRegistry.java can look cleaner
     */
    public BuddingSouliteBlock() {
        this(getProperties());
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        Properties prop = Properties.copy(Blocks.STONE)
                .mapColor(MapColor.COLOR_CYAN)
                .strength(HARDNESS, BLAST_RESISTANCE)//Hardness & Resistance
                .sound(SoundType.HONEY_BLOCK)
                .destroyTime(5f)
                .sound(SoundType.AMETHYST)
                .noOcclusion()
                .lightLevel((value) -> 15);
        return prop;
    }

    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    @Override
    public boolean isRandomlyTicking(BlockState p_49921_) {
        return true;
    }

    public static void spawnSouliteClusters(Level level, BlockPos pos)
    {
        BlockPos north = pos.north();
        BlockPos east = pos.east();
        BlockPos south = pos.south();
        BlockPos west = pos.west();
        BlockPos up = pos.above();
        BlockPos down = pos.below();
        if(level.getBlockState(north).canBeReplaced())
        {
            level.setBlockAndUpdate(north, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.NORTH));
        }

        if(level.getBlockState(east).canBeReplaced())
        {
            level.setBlockAndUpdate(east, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.EAST));
        }

        if(level.getBlockState(south).canBeReplaced())
        {
            level.setBlockAndUpdate(south, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.SOUTH));
        }

        if(level.getBlockState(west).canBeReplaced())
        {
            level.setBlockAndUpdate(west, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.WEST));
        }

        if(level.getBlockState(up).canBeReplaced())
        {
            level.setBlockAndUpdate(up, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.UP));
        }

        if(level.getBlockState(down).canBeReplaced())
        {
            level.setBlockAndUpdate(down, ModBlocks.SOULITE_CLUSTER_BLOCK.get().defaultBlockState().setValue(SouliteClusterBlock.FACING, Direction.DOWN));
        }
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(blockState, level, pos, random);

        level.setBlockAndUpdate(pos, ModBlocks.SOULITE_BLOCK.get().defaultBlockState());
        spawnSouliteClusters(level, pos);
    }
}
