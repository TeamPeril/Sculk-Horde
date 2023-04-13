package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkBeeNestCellTile extends BlockEntity {

    private boolean isMature = false;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkBeeNestCellTile(BlockPos pos, BlockState state)
    {
        super(TileEntityRegistry.SCULK_BEE_NEST_CELL_TILE.get(), pos, state);
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
