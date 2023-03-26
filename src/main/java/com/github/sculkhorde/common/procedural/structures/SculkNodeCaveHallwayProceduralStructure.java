package com.github.sculkhorde.common.procedural.structures;

import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.core.jmx.Server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SculkNodeCaveHallwayProceduralStructure extends ProceduralStructure
{
    private int radius = -1;
    private int length = -1;
    private Direction direction;

    /**
     * This is the constructor for the Sculk Node Cave Hallway Structure.
     * @param worldIn The world that the structure is being built in.
     * @param originIn The origin of the structure.
     * @param radiusIn The radius of the structure.
     * @param length The length of the structure.
     * @param direction The direction of the structure.
     */
    public SculkNodeCaveHallwayProceduralStructure(ServerWorld worldIn, BlockPos originIn, int radiusIn, int length, Direction direction)
    {
        super(worldIn, originIn);
        this.radius = radiusIn;
        this.length = length;
        this.direction = direction;
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
        ArrayList<BlockPos> plannedBlockPositions = createCaveBlockPositions();

        for(BlockPos position : plannedBlockPositions)
        {
            //Every block of the Outer shell of cylinder is set to stone
            plannedBlockQueue.add(new CaveAirPlannedBlock(this.world, position));
        }
        return;
    }

    /**
     * Creates a line of blocks that represent a cylinder
     * @return An array list of block positions that represent a cylinder
     */
    public ArrayList<BlockPos> createCaveBlockPositions() {

        Line3D line;
        switch (direction) {
            case NORTH:
                line = new Line3D(origin, origin.north(length));
                break;
            case SOUTH:
                line = new Line3D(origin, origin.south(length));
                break;
            case EAST:
                line = new Line3D(origin, origin.east(length));
                break;
            case WEST:
                line = new Line3D(origin, origin.west(length));
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        return line.getBlockPositionsOnLineWithSphere(radius);
    }


    /**
     * A custom planned block that wont place blocks if they are exposed to air
     */
    private class CaveWallPlannedBlock extends PlannedBlock
    {
        /**
         * Constructor
         *
         * @param worldIn The World
         * @param targetPosIn The Position to spawn it
         */
        public CaveWallPlannedBlock(ServerWorld worldIn, BlockPos targetPosIn)
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
            // Otherwise, return true
            return validBlocksPredicate.isAir();
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
                world.setBlockAndUpdate(targetPos, plannedBlock);
            }
        }
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
        public CaveAirPlannedBlock(ServerWorld worldIn, BlockPos targetPosIn)
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
            if(validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_NODE_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_DURA_MATTER.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_ARACHNOID.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_SUMMONER_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.GRASS.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.GRASS_SHORT.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.CALCITE_ORE.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SMALL_SHROOM.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.VEIN.get())
                    || validBlocksPredicate.getBlock().is(BlockRegistry.SCULK_SHROOM_CULTURE.get())
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
