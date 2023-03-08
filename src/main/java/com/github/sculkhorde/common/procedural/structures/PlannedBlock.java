package com.github.sculkhorde.common.procedural.structures;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.function.Predicate;

public class PlannedBlock
{
    protected BlockState plannedBlock = null;
    protected BlockPos targetPos;
    protected ServerWorld world;

    /**
     * Constructor
     * @param worldIn
     * @param plannedBlockIn
     * @param targetPosIn
     */
    public PlannedBlock(ServerWorld worldIn, BlockState plannedBlockIn, BlockPos targetPosIn)
    {
        plannedBlock = plannedBlockIn;
        targetPos = targetPosIn;
        world = worldIn;
    }

    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Determines if a block can be replaced in the building process
     */
    protected final Predicate<BlockState> VALID_BLOCKS_TO_REPLACE = (validBlocksPredicate) ->
    {
        if(SculkHorde.infestationConversionTable.isConsideredVictim(validBlocksPredicate)
        || SculkHorde.infestationConversionTable.isConsideredActiveSpreader(validBlocksPredicate)
        || SculkHorde.infestationConversionTable.isConsideredDormantSpreader(validBlocksPredicate)
        || validBlocksPredicate.canBeReplaced(Fluids.WATER))
        {
            return true;
        }
        return false;
    };

    /**
     * Outputs if the block we are trying to place, is able to be placed at a location
     * @return True if able to place, false otherwise.
     */
    public boolean canBePlaced()
    {
        return this.VALID_BLOCKS_TO_REPLACE.test(world.getBlockState(targetPos));
    }

    /**
     * Outputs if the block has been placed
     * @return True if placed, false otherwise.
     */
    public boolean isPlaced()
    {
        return world.getBlockState(targetPos).is(plannedBlock.getBlock());
    }

    /**
     * If able, will place the block in the world.
     */
    public void build()
    {
        //If we 1n replace the block at the location
        if(canBePlaced())
        {
            world.setBlockAndUpdate(targetPos, plannedBlock);
        }
        else
        {
            //Very Bad
        }
    }

    public BlockPos getPosition()
    {
        return targetPos;
    }

}
