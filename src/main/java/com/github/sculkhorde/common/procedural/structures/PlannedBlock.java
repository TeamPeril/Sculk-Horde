package com.github.sculkhorde.common.procedural.structures;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.function.Predicate;

public class PlannedBlock
{
    protected BlockState plannedBlock = null;
    protected BlockPos targetPos;
    protected ServerLevel world;

    /**
     * Constructor
     * @param worldIn
     * @param plannedBlockIn
     * @param targetPosIn
     */
    public PlannedBlock(ServerLevel worldIn, BlockState plannedBlockIn, BlockPos targetPosIn)
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
        // If the block is a normal variant,
        // or is replaceable by water,
        // or is an infected variant,
        // or can be broken by an iron pickaxe
        if(
                SculkHorde.infestationConversionTable.infestationTable.isNormalVariant(validBlocksPredicate)
                || validBlocksPredicate.canBeReplaced(Fluids.WATER)
                || SculkHorde.infestationConversionTable.infestationTable.isInfectedVariant(validBlocksPredicate)
                || validBlocksPredicate.getDestroySpeed(world, targetPos) <= 3.0F)
        //TODO PORT
                //|| validBlocksPredicate.getBlock().isAir(validBlocksPredicate, world, targetPos))

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
