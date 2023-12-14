package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ITagInfestedBlock {
    ITagInfestedBlockEntity getTagInfestedBlockEntity(Level level, BlockPos blockPos);

}
