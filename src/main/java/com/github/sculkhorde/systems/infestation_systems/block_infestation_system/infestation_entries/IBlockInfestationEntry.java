package com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

public interface IBlockInfestationEntry {

    boolean isNormalVariant(BlockState blockState);
    boolean isInfectedVariant(BlockState blockState);
    BlockState getNormalVariant(LevelReader level, BlockPos blockPos);
    BlockState getInfectedVariant(LevelReader level, BlockPos blockPos);

    float getPriority();
}
