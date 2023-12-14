package com.github.sculkhorde.common.block;

import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlock;
import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlockEntity;
import com.github.sculkhorde.common.blockentity.InfestedTagBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;

public class InfestedTagBlock extends BaseEntityBlock implements IForgeBlock, ITagInfestedBlock {

    /**
     * The Constructor that takes in properties
     * @param prop The Properties
     */
    public InfestedTagBlock(Properties prop) {
        super(prop);
    }


    /**
     * Determines if this block will randomly tick or not.
     * @param blockState The current blockstate
     * @return True/False
     */
    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return false;
    }

    @Override
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

    @Override
    public RenderShape getRenderShape(BlockState p_54296_) {
        return RenderShape.MODEL;
    }
}
