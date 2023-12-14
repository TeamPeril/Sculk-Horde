package com.github.sculkhorde.common.structures.procedural;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SculkNodeCaveProceduralStructure extends ProceduralStructure
{
    private int radius;

    SculkNodeCaveHallwayProceduralStructure westCave;
    SculkNodeCaveHallwayProceduralStructure eastCave;
    SculkNodeCaveHallwayProceduralStructure northCave;
    SculkNodeCaveHallwayProceduralStructure southCave;

    public SculkNodeCaveProceduralStructure(ServerLevel worldIn, BlockPos originIn, int radiusIn)
    {
        super(worldIn, originIn);
        this.radius = radiusIn;
        generatePlan();
    }

    @Override
    public void buildTick() {

        /**
         * Fixed Bug
         * Checking if we are currently building here causes this structure
         * not to be built. This is because isCurrentlyBuilding is always false
         * since building is controlled by The Sculk Node Structure. {@link SculkNodeProceduralStructure#buildTick()}
         */

        //Build blocks from main structure
        if(currentPlannedBlockQueueIndex < plannedBlockQueue.size())
        {
            PlannedBlock currentPlannedBlock = plannedBlockQueue.get(currentPlannedBlockQueueIndex);

            // While the block isPlaced(), keep incrementing the index
            while(currentPlannedBlock.canBePlaced() && currentPlannedBlock.isPlaced() && currentPlannedBlockQueueIndex < plannedBlockQueue.size())
            {
                currentPlannedBlock = plannedBlockQueue.get(currentPlannedBlockQueueIndex);
                currentPlannedBlockQueueIndex++;
            }

            // If it can be placed, place it, then keep track
            if(currentPlannedBlock.canBePlaced())
            {
                currentPlannedBlock.build();
            }
            currentPlannedBlockQueueIndex++;
        }

        westCave.buildTick();
        eastCave.buildTick();
        northCave.buildTick();
        southCave.buildTick();

    }

    /**
     * This method fills the building queue with what blocks should
     * be placed down.
     */
    @Override
    public void generatePlan()
    {
        this.plannedBlockQueue.clear();
        // is returning empty
        ArrayList<BlockPos> blockPositionsInCircle = BlockAlgorithms.getBlockPosInCircle(origin, radius, false);

        for(BlockPos position : blockPositionsInCircle)
        {
            plannedBlockQueue.add(new CaveAirPlannedBlock(this.world, position));
        }

        ConnectionPoint westConnection = new ConnectionPoint(new BlockPos(this.origin.offset(-radius, 0 ,0)), Direction.WEST);
        ConnectionPoint eastConnection = new ConnectionPoint(new BlockPos(this.origin.offset(radius, 0 ,0)), Direction.EAST);
        ConnectionPoint northConnection = new ConnectionPoint(new BlockPos(this.origin.offset(0, 0 ,-radius)), Direction.NORTH);
        ConnectionPoint southConnection = new ConnectionPoint(new BlockPos(this.origin.offset(0, 0 ,radius)), Direction.SOUTH);

        westCave = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) this.world, westConnection.getPosition(), 5, 32, Direction.WEST);
        eastCave = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) this.world, eastConnection.getPosition(), 5, 32, Direction.EAST);
        northCave = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) this.world, northConnection.getPosition(), 5, 32, Direction.NORTH);
        southCave = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) this.world, southConnection.getPosition(), 5, 32, Direction.SOUTH);

        return;
    }

    /**
     * A custom planned block that wont place blocks if they are exposed to air
     */
    public class CaveAirPlannedBlock extends PlannedBlock
    {
        /**
         * Constructor
         *
         * @param worldIn The World
         * @param targetPosIn The Position to spawn it
         */
        public CaveAirPlannedBlock(ServerLevel worldIn, BlockPos targetPosIn)
        {
            super(worldIn, Blocks.CAVE_AIR.defaultBlockState(), targetPosIn);
        }

        /**
         * Outputs if the block we are trying to place, is able to be placed at a location
         * @return True if able to place, false otherwise.
         */
        @Override
        public boolean canBePlaced()
        {
            return CAN_BLOCK_BE_REPLACED.test(world.getBlockState(targetPos));
        }

        /**
         * Represents a predicate (boolean-valued function) of one argument. <br>
         * Determines if a block can be replaced in the building process
         */
        protected final Predicate<BlockState> CAN_BLOCK_BE_REPLACED = (validBlocksPredicate) ->
        {
            // Explicit Deny List. We put this first as it has higher priority.
            if(validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_NODE_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_DURA_MATTER.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_ARACHNOID.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_LIVING_ROCK_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_LIVING_ROCK_ROOT_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_SUMMONER_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.GRASS.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.GRASS_SHORT.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.CALCITE_ORE.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SMALL_SHROOM.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.TENDRILS.get())
                    || validBlocksPredicate.getBlock().equals(ModBlocks.SCULK_SHROOM_CULTURE.get())
            )
            {
                return false;
            }

            //Conditional Denies. Secondary Priority
            if(world.canSeeSky(targetPos.above()))
            {
                return false;
            }
            if(validBlocksPredicate.getDestroySpeed(world, targetPos) > 3.0F) {
                return false;
            }

            // Otherwise, return true
            return true;
        };

        /**
         * If able, will place the block in the world.
         */
        @Override
        public void build()
        {
            //If we 1n replace the block at the location
            if(canBePlaced())
            {
                /**
                 * Sets a block state into this world.Flags are as follows:
                 * 1 will cause a block update.
                 * 2 will send the change to clients.
                 * 4 will prevent the block from being re-rendered.
                 * 8 will force any re-renders to run on the main thread instead
                 * 16 will prevent neighbor reactions (e.g. fences connecting, observers pulsing).
                 * 32 will prevent neighbor reactions from spawning drops.
                 * 64 will signify the block is being moved.
                 * Flags can be OR-ed
                 */
                // We use 3 because we want to flag 1 and 2
                world.setBlockAndUpdate(targetPos, plannedBlock);
            }
        }
    }
}
