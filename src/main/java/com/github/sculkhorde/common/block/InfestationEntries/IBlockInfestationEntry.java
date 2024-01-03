package com.github.sculkhorde.common.block.InfestationEntries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockInfestationEntry {

    boolean isNormalVariant(BlockState blockState);
    boolean isInfectedVariant(BlockState blockState);
    BlockState getNormalVariant(Level level, BlockPos blockPos);
    BlockState getInfectedVariant(Level level, BlockPos blockPos);
}
