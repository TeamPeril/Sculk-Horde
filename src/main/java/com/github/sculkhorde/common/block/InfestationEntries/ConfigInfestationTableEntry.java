package com.github.sculkhorde.common.block.InfestationEntries;

import com.github.sculkhorde.core.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ConfigInfestationTableEntry implements IBlockInfestationEntry
{
    protected ITagInfestedBlock infectedVariant;
    protected float priority = 0;

    // Default constructor
    public ConfigInfestationTableEntry(ITagInfestedBlock infectedVariantIn)
    {
        infectedVariant = infectedVariantIn;
    }

    public float getPriority()
    {
        return priority;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return ModConfig.SERVER.isBlockConfiguredToBeInfestable(blockState);
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return ((Block)infectedVariant).defaultBlockState().is(blockState.getBlock());
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos)
    {
        ITagInfestedBlockEntity blockEntity = infectedVariant.getTagInfestedBlockEntity(level, blockPos);
        if(blockEntity == null || blockEntity.getNormalBlockState() == null)
        {
            return level.getBlockState(blockPos);
        }
        return infectedVariant.getTagInfestedBlockEntity(level, blockPos).getNormalBlockState();
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos)
    {
        return ((Block)infectedVariant).defaultBlockState();
    }
}