package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTagInfestationTableEntry implements IBlockInfestationEntry
{
    protected TagKey<Block> normalVariantTag;
    protected ITagInfestedBlock infectedVariant;


    // Default constructor
    public BlockTagInfestationTableEntry(TagKey<Block> normalVariantIn, ITagInfestedBlock infectedVariantIn)
    {
        normalVariantTag = normalVariantIn;
        infectedVariant = infectedVariantIn;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return blockState.is(normalVariantTag);
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return ((Block)infectedVariant).defaultBlockState().is(blockState.getBlock());
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        ITagInfestedBlockEntity blockEntity = infectedVariant.getTagInfestedBlockEntity(level, blockPos);
        if(blockEntity == null || blockEntity.getNormalBlockState() == null)
        {
            return blockState;
        }
        return infectedVariant.getTagInfestedBlockEntity(level, blockPos).getNormalBlockState();
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        return ((Block)infectedVariant).defaultBlockState();
    }
}