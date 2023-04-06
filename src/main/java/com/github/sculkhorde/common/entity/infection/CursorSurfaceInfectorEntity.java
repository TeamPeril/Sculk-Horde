package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

public class CursorSurfaceInfectorEntity extends CursorInfectorEntity{
    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     *
     * @param worldIn The world to initialize this mob in
     */
    public CursorSurfaceInfectorEntity(World worldIn) {
        super(worldIn);
    }

    public CursorSurfaceInfectorEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
    }


    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(!state.isSolidRender(this.level, pos))
        {
            return true;
        }

        if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        if(state.isAir())
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }

        ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(pos);

        int airCount = 0;
        // If any of the neighbors in the world are air, then this block is not obstructed.
        for(BlockPos neighbor : neighbors)
        {
            if(this.level.getBlockState(neighbor).isAir())
            {
                airCount++;
            }
        }

        if(airCount <= 0)
        {
            return true;
        }

        return false;
    }
}
