package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public interface ITagInfestedBlockEntity {

    void setNormalBlockState(BlockState blockState);

    @Nullable
    BlockState getNormalBlockState();

    default BlockState getNormalBlockState(Block defaultBlock) {
        BlockState result = getNormalBlockState();
        if (result == null) return defaultBlock.defaultBlockState();
        return result;
    }
}
