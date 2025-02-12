package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ITagInfestedBlock {
    ITagInfestedBlockEntity getTagInfestedBlockEntity(Level level, BlockPos blockPos);

}
