package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockInfestationTableEntry implements IBlockInfestationEntry
{
    protected Block normalVariant;
    protected BlockState infectedVariant;


    // Default constructor
    public BlockInfestationTableEntry(Block normalVariantIn, BlockState infectedVariantIn)
    {
        normalVariant = normalVariantIn;
        infectedVariant = infectedVariantIn;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return blockState.is(normalVariant);
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return infectedVariant.is(blockState.getBlock());
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        return normalVariant.defaultBlockState();
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        return infectedVariant;
    }
}