package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MultiTagInfestationTableEntry extends BlockEntityInfestationTableEntry {

    protected TagKey<Block> normalVariantTag1;
    protected TagKey<Block> normalVariantTag2;
    protected Tier tierRequired;

    public MultiTagInfestationTableEntry(float priority, TagKey<Block> normalVariant1In, TagKey<Block> normalVariant2In, Tier tierRequiredIn, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        super(priority, infectedVariantIn, defaultNormalVariantIn);
        normalVariantTag1 = normalVariant1In;
        normalVariantTag2 = normalVariant2In;
        tierRequired = tierRequiredIn;
    }

    @Override
    public boolean isNormalVariant(BlockState blockState) {
        return blockState.is(normalVariantTag1) && blockState.is(normalVariantTag2);
    }
}
