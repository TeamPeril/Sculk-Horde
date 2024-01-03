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

public class ToolTaglInfestationTableEntry implements IBlockInfestationEntry
{
    protected TagKey<Block> toolRequiredTag;

    protected Tier tierRequired = Tiers.IRON;
    protected ITagInfestedBlock infectedVariant;


    // Default constructor
    public ToolTaglInfestationTableEntry(TagKey<Block> toolRequiredTag, Tier tierRequired, ITagInfestedBlock infectedVariantIn)
    {
        this.toolRequiredTag = toolRequiredTag;
        this.tierRequired = tierRequired;
        infectedVariant = infectedVariantIn;
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