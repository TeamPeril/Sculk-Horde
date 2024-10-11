package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.TierSortingRegistry;

import javax.annotation.Nullable;
import java.util.Optional;

public class MultiTagInfestationTableEntry extends BlockEntityInfestationTableEntry {

    protected TagKey<Block> normalVariantTag1;
    protected TagKey<Block> normalVariantTag2;
    protected Tier tierRequired;

    public MultiTagInfestationTableEntry(TagKey<Block> normalVariant1In, TagKey<Block> normalVariant2In, Tier tierRequiredIn, ITagInfestedBlock infectedVariantIn, Block defaultNormalVariantIn)
    {
        super(infectedVariantIn, defaultNormalVariantIn);
        normalVariantTag1 = normalVariant1In;
        normalVariantTag2 = normalVariant2In;
        tierRequired = tierRequiredIn;
    }

    @Override
    public boolean isNormalVariant(BlockState blockState) {
        return blockState.is(normalVariantTag1) && blockState.is(normalVariantTag2) && TierSortingRegistry.isCorrectTierForDrops(tierRequired, blockState);
    }
}
