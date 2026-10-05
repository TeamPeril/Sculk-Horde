package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ToolTaglInfestationTableEntry extends BlockEntityInfestationTableEntry
{
    protected TagKey<Block> toolRequiredTag;
    protected Tier tierRequired = Tiers.IRON;

    // Default constructor
    public ToolTaglInfestationTableEntry(float priority, TagKey<Block> toolRequiredTag, Tier tierRequired, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        super(priority, infectedVariantIn, defaultNormalVariantIn);
        this.toolRequiredTag = toolRequiredTag;
        this.tierRequired = tierRequired;
    }

    public boolean isNormalVariant(BlockState blockState)
    {
        if(blockState.getBlock() instanceof BaseEntityBlock)
        {
            return false;
        }

        return blockState.is(toolRequiredTag);
    }
}