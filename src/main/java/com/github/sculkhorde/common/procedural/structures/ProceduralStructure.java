package com.github.sculkhorde.common.procedural.structures;

import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

public class ProceduralStructure
{
    protected ServerWorld world;

    protected BlockPos origin; //This represents the position of the origin

    protected boolean isCurrentlyBuilding = false;

    public ArrayList<PlannedBlock> plannedBlockQueue; //A list of individual planned blocks to build

    protected int currentPlannedBlockQueueIndex = 0;

    protected ArrayList<ProceduralStructure> childStructuresQueue; //A list of child structures to build

    /**
     * Default Constructor
     * @param originIn The location of the origin
     */
    public ProceduralStructure(ServerWorld worldIn, BlockPos originIn)
    {
        origin = originIn;
        plannedBlockQueue = new ArrayList<>();
        childStructuresQueue = new ArrayList<>();
        world = worldIn;

    }

    /** ACCESSORS **/

    public boolean isCurrentlyBuilding()
    {
        return isCurrentlyBuilding;
    }

    protected int getPlannedBlocksThatCanBePlaced()
    {
        int plannedBlocksThatAreAbleToBePlaced = 0;

        //Check planned Blocks Queue to check out of all the blocks we are able to place
        //and see how many of those are placed.
        for(PlannedBlock entry : plannedBlockQueue)
        {
            //If the block can be placed
            if(entry.canBePlaced())
            {
                plannedBlocksThatAreAbleToBePlaced++;
            }
        }

        return plannedBlocksThatAreAbleToBePlaced;
    }

    protected int getPlannedBlocksPlaced()
    {
        int plannedBlocksPlaced = 0;

        //Check planned Blocks Queue to check out of all the blocks we are able to place
        //and see how many of those are placed.
        for(PlannedBlock entry : plannedBlockQueue)
        {
            //If the block can be placed
            if(entry.canBePlaced() && entry.isPlaced())
            {
                plannedBlocksPlaced++;
            }
        }

        return plannedBlocksPlaced;
    }

    /** MODIFIERS **/

    /**
     * Counts how many blocks that can be placed, are placed from this
     * procedural structure and all child structures.
     */
    protected float getBuildProgress()
    {
        int totalPlannedBlocksThatAreAbleToBePlaced = getPlannedBlocksThatCanBePlaced();
        int totalPlannedBlocksPlaced = getPlannedBlocksPlaced();

        //Check all sub structures
        for(ProceduralStructure entry : childStructuresQueue)
        {
            totalPlannedBlocksThatAreAbleToBePlaced += entry.getPlannedBlocksThatCanBePlaced();
            totalPlannedBlocksPlaced += entry.getPlannedBlocksPlaced();
        }

        if(totalPlannedBlocksThatAreAbleToBePlaced == 0)
        {
            return 1.0F;
        }
        else
        {
            return ((float) totalPlannedBlocksPlaced) / ((float) totalPlannedBlocksThatAreAbleToBePlaced);
        }
    }

    /** BOOLEANS **/

    /**
     * If build progress is 100%, then true, else false
     * @return True/False
     */
    public boolean isStructureComplete()
    {
        if(getBuildProgress() < 1.0)
        {
            return false;
        }

        for(ProceduralStructure childStructure : childStructuresQueue)
        {
            if(!childStructure.isStructureComplete())
            {
                return false;
            }
        }

        return true;
    }

    /** EVENTS **/

    /**
     * This method fills the building queue with what blocks should
     * be placed down.
     */
    public void generatePlan()
    {
        plannedBlockQueue.clear();

        for(ProceduralStructure entry : childStructuresQueue)
        {
            entry.generatePlan();
        }

        return; //This return is here only so that I can put a debug breakpoint here
    }

    /**
     * Gets called when starting the building processes once.
     * Determines if the process will actually start.
     * @return True/False
     */
    protected boolean canStartToBuild()
    {
        boolean result = true;

        if(isStructureComplete())
        {
            result = false;
        }
        else if(plannedBlockQueue.isEmpty())
        {
            result = false;
        }

        return result;
    }

    /**
     * Gets called during the building process
     * to determine if it should continiue to be built
     * @return True/False
     */
    protected boolean canContinueToBuild()
    {
        return canStartToBuild() && !isStructureComplete();
    }

    /**
     * Called to start the building procedure.
     */
    public void startBuildProcedure()
    {
        if(canStartToBuild())
        {
            isCurrentlyBuilding = true;
            currentPlannedBlockQueueIndex = 0;
            /*
            This method sorts the plannedBlockQueue ArrayList by using the Collections.sort() method, with a custom comparator
            function that compares the distance of each block to the origin using the getBlockDistance method provided.
            The Float.compare() method is used to compare the two distances, and the sort method will use the returned value
            to order the elements in the ArrayList in ascending order (i.e. closest to the origin first)
             */
                plannedBlockQueue.sort((block1, block2) ->
                        Float.compare(BlockAlgorithms.getBlockDistance(block1.getPosition(), origin),
                                BlockAlgorithms.getBlockDistance(block2.getPosition(), origin)));
        }
    }

    /**
     * Stops the building procedure.
     */
    public void stopBuildProcedure()
    {
        isCurrentlyBuilding = false;
    }

    /**
     * Will place a single block. This function should be called multiple times.
     */
    public void buildTick()
    {
        //Do Not Tick if we arent in build mode
        if(!isCurrentlyBuilding) { return; }
        if(!canContinueToBuild())
        {
            stopBuildProcedure();
            return;
        }

        //Build blocks from main structure
        if(currentPlannedBlockQueueIndex < plannedBlockQueue.size())
        {
            PlannedBlock currentPlannedBlock = plannedBlockQueue.get(currentPlannedBlockQueueIndex);
            // If it can be placed, place it, then keep track
            if(currentPlannedBlock.canBePlaced())
            {
                currentPlannedBlock.build();
            }
            currentPlannedBlockQueueIndex++;
        }
    }
}
