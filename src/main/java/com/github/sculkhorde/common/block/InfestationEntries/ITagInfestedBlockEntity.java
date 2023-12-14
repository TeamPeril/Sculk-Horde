package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.world.level.block.state.BlockState;

public interface ITagInfestedBlockEntity {

    void setNormalBlockState(BlockState blockState);

    BlockState getNormalBlockState();
}
