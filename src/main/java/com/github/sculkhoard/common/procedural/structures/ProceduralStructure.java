package com.github.sculkhoard.common.procedural.structures;

import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

public class ProceduralStructure
{
    protected ServerWorld world;

    protected BlockPos origin; //This represents the position of the origin

    protected boolean isCurrentlyBuilding = false;

    protected float buildProgress = 0.0f;

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

        generatePlan();
    }

    /** ACCESSORS **/

    public float getBuildProgress()
    {
        return buildProgress;
    }

    public boolean isCurrentlyBuilding()
    {
        return isCurrentlyBuilding;
    }

    public int getPlannedBlocksThatCanBePlaced()
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

    public int getPlannedBlocksPlaced()
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
    private void calculateBuildProgress()
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
            buildProgress = 0;
        }
        else
        {
            buildProgress = ((float) totalPlannedBlocksPlaced) / ((float) totalPlannedBlocksThatAreAbleToBePlaced);
        }
    }

    /** BOOLEANS **/

    /**
     * If build progress is 100%, then true, else false
     * @return True/False
     */
    public boolean isStructureComplete()
    {
        if(buildProgress == 100.0) return true;

        return false;
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

        for(int offset = 1; offset <= 4; offset++)
        {
            plannedBlockQueue.add(
                    new PlannedBlock(world, BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get().defaultBlockState(), origin.below(offset))
            );
        }
    }

    /**
     * Gets called when starting the building processes once.
     * Determines if the process will actually start.
     * @return True/False
     */
    public boolean canStartToBuild()
    {
        boolean result = true;

        if(isStructureComplete())
        {
            result = false;
        }
        else if(plannedBlockQueue.isEmpty() && childStructuresQueue.isEmpty())
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
    public boolean canContinueToBuild()
    {
        return canStartToBuild();
    }

    /**
     * Called to start the building procedure.
     */
    public void startBuildProcedure()
    {
        calculateBuildProgress();
        if(!isCurrentlyBuilding && canStartToBuild())
        {
            isCurrentlyBuilding = true;
            currentPlannedBlockQueueIndex = 0;
        }
    }

    /**
     * Stops the building procedure.
     */
    public void stopBuildProcedure()
    {
        if(isCurrentlyBuilding)
        {
            isCurrentlyBuilding = false;
        }
    }

    /**
     * Will place a single block. This function should be called multiple times.
     */
    public void buildTick()
    {
        //Build Child Structures
        for(ProceduralStructure childStructure : childStructuresQueue)
        {
            childStructure.buildTick();
        }

        //Build blocks from main structure
        if(canContinueToBuild() && currentPlannedBlockQueueIndex < plannedBlockQueue.size())
        {
            PlannedBlock currentPlannedBlock = plannedBlockQueue.get(currentPlannedBlockQueueIndex);
            currentPlannedBlock.build();
            calculateBuildProgress();
            currentPlannedBlockQueueIndex++;
        }
        else
        {
            stopBuildProcedure();
        }
    }
}
