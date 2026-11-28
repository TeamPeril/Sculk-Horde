package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.blockentity.InfestedTagBlockEntity;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.ITagInfestedBlock;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.ITagInfestedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedPillarBlock extends RotatedPillarBlock implements EntityBlock, ITagInfestedBlock {

    public InfestedPillarBlock(Properties prop) {
        super(prop);
    }

    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new InfestedTagBlockEntity(blockPos, state);
    }

    @Override
    public ITagInfestedBlockEntity getTagInfestedBlockEntity(LevelReader level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof ITagInfestedBlockEntity)
        {
            return (ITagInfestedBlockEntity) blockEntity;
        }
        return null;
    }
}
