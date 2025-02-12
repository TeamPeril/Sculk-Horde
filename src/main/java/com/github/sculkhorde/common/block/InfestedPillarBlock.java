package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlock;
import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlockEntity;
import com.github.sculkhorde.common.blockentity.InfestedTagBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;

public class InfestedPillarBlock extends RotatedPillarBlock implements EntityBlock, IForgeBlock, ITagInfestedBlock {

    public InfestedPillarBlock(Properties prop) {
        super(prop);
    }

    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState state) {
        return new InfestedTagBlockEntity(blockPos, state);
    }

    @Override
    public ITagInfestedBlockEntity getTagInfestedBlockEntity(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof ITagInfestedBlockEntity)
        {
            return (ITagInfestedBlockEntity) blockEntity;
        }
        return null;
    }
}
