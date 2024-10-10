package com.github.sculkhorde.common.block.InfestationEntries;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockTagInfestationTableEntry implements IBlockInfestationEntry
{
    protected TagKey<Block> normalVariantTag;
    protected ITagInfestedBlock infectedVariant;
    protected Block defaultNormalVariant;

    // This is needed to tell java that the properties are of the same type
    private static <T extends Comparable<T>> BlockState copyBlockProperty(BlockState from, BlockState to, Property<T> property) {
    	// copy only if from and to have the same Property (this solution is a little bit hacky but I don't no a better way)
    	try {
    		return to.setValue(property, from.getValue(property));
    	} catch(IllegalArgumentException e) {
    		return to;
    	}
    }

    // Default constructor
    public BlockTagInfestationTableEntry(TagKey<Block> normalVariantIn, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        normalVariantTag = normalVariantIn;
        infectedVariant = infectedVariantIn;
        defaultNormalVariant = defaultNormalVariantIn;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return blockState.is(normalVariantTag);
    }

    public boolean isInfectedVariant(BlockState blockState)
    {
        return ((Block)infectedVariant).defaultBlockState().is(blockState.getBlock());
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos)
    {
        ITagInfestedBlockEntity blockEntity = infectedVariant.getTagInfestedBlockEntity(level, blockPos);
        if(blockEntity == null)
        {
            return level.getBlockState(blockPos);
        }
        return infectedVariant.getTagInfestedBlockEntity(level, blockPos).getNormalBlockState(defaultNormalVariant);
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos)
    {
        // copy block properties of normal block to infected block
        BlockState infectedState = ((Block)infectedVariant).defaultBlockState();

        for(Property<?> prop : level.getBlockState(blockPos).getProperties()) {
            infectedState = copyBlockProperty(level.getBlockState(blockPos), infectedState, prop);
        }

        return infectedState;
    }

}