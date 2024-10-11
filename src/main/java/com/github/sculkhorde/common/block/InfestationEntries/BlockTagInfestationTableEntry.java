package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockTagInfestationTableEntry extends BlockEntityInfestationTableEntry
{
    protected TagKey<Block> normalVariantTag;

    // Default constructor
    public BlockTagInfestationTableEntry(TagKey<Block> normalVariantIn, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        super(infectedVariantIn, defaultNormalVariantIn);
        normalVariantTag = normalVariantIn;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        return blockState.is(normalVariantTag);
    }

}