package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

public class ToolTaglInfestationTableEntry extends BlockEntityInfestationTableEntry
{
    protected TagKey<Block> toolRequiredTag;
    protected Tier tierRequired = Tiers.IRON;

    // Default constructor
    public ToolTaglInfestationTableEntry(TagKey<Block> toolRequiredTag, Tier tierRequired, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        super(infectedVariantIn, defaultNormalVariantIn);
        this.toolRequiredTag = toolRequiredTag;
        this.tierRequired = tierRequired;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        if(!TierSortingRegistry.isCorrectTierForDrops(tierRequired, blockState))
        {
            return false;
        }
        else if(blockState.getBlock() instanceof BaseEntityBlock)
        {
            return false;
        }

        return blockState.is(toolRequiredTag);
    }

    public BlockState getInfectedVariant(Level level, BlockPos blockPos)
    {
        return ((Block)infectedVariant).defaultBlockState();
    }
}