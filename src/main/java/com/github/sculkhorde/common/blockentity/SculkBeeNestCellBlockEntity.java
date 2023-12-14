package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkBeeNestCellBlockEntity extends BlockEntity {

    private boolean isMature = false;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkBeeNestCellBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.SCULK_BEE_NEST_CELL_BLOCK_ENTITY.get(), pos, state);
    }

    /** Accessors **/

    public boolean isMature()
    {
        return isMature;
    }

    /** Modifiers **/

    public void setMature()
    {
        isMature = true;
    }

    public void setImmature()
    {
        isMature = false;
    }


}
