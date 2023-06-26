package com.github.sculkhorde.common.structures.procedural;

import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.common.block.SculkBeeNestCellBlock;
import com.github.sculkhorde.core.BlockRegistry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class SculkBeeNestProceduralStructure extends ProceduralStructure
{

    public SculkBeeNestProceduralStructure(ServerLevel worldIn, BlockPos originIn)
    {
        super(worldIn, originIn);
    }

    /**
     * This method fills the building queue with what blocks should
     * be placed down.
     */
    @Override
    public void generatePlan()
    {
        this.plannedBlockQueue.clear();

        for(ProceduralStructure entry : childStructuresQueue)
        {
            entry.generatePlan();
        }

        //The Stem of the nest
        for(int offset = 1; offset <= 4; offset++)
        {
            plannedBlockQueue.add(
                    new PlannedBlock(world, BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get().defaultBlockState(), origin.below(offset))
            );
        }

        BlockPos branchOrigin = new BlockPos(origin.getX(), origin.getY() - 2, origin.getZ());

        plannedBlockQueue.addAll(generateBranchesPlan(branchOrigin));

    }

    /**
     * Generates horizontal lines with progressive offsets to make curved
     * lines.
     * @param origin
     * @return
     */
    private ArrayList<PlannedBlock> generateBranchesPlan(BlockPos origin)
    {
        ArrayList<PlannedBlock> branches = new ArrayList<>();
        int branchLength = (int) BlockAlgorithms.getSudoRNGFromPosition(origin, 5, 8);
        int chanceToOffsetBranchSideways = 35;
        int branchSidewaysOffset = 0;

        //Make the branches on the X axis
        for(int offset = 1; offset <= branchLength; offset++)
        {
            BlockPos branch1Pos = new BlockPos(origin.getX() + offset, origin.getY(), origin.getZ()  + branchSidewaysOffset);
            BlockPos branch2Pos = new BlockPos(origin.getX() - offset, origin.getY(), origin.getZ()  - branchSidewaysOffset);
            
            if(BlockAlgorithms.getSudoRNGFromPosition(branch1Pos, 0, 100) <= chanceToOffsetBranchSideways)
            {
                branchSidewaysOffset++;
            }

            branches.add(new SculkNestCellPlannedBlock(world, branch1Pos));
            branches.add(new SculkNestCellPlannedBlock(world, branch2Pos));
        }

        branchSidewaysOffset = 0;
        branchLength -= 2;

        //Make the branches on the Z Axis
        for(int offset = 1; offset <= branchLength; offset++)
        {
            BlockPos branch1Pos = new BlockPos(origin.getX() + branchSidewaysOffset, origin.getY() - 1, origin.getZ()  + offset);
            BlockPos branch2Pos = new BlockPos(origin.getX() - branchSidewaysOffset, origin.getY() - 1, origin.getZ()  - offset);

            if(BlockAlgorithms.getSudoRNGFromPosition(branch1Pos, 0, 100) <= chanceToOffsetBranchSideways)
            {
                branchSidewaysOffset++;
            }

            branches.add(new SculkNestCellPlannedBlock(world, branch1Pos));
            branches.add(new SculkNestCellPlannedBlock(world, branch2Pos));
        }

        return branches;
    }

    public void makeRandomBlockMature()
    {
        //Convert Cell to Harvestable Cell
        for(PlannedBlock block : plannedBlockQueue)
        {
            if(block.isPlaced() && this.world.getBlockState(block.getPosition()).getValue(SculkBeeNestCellBlock.MATURE) == 0)
            {
                //Set the MATURE value of the cell block to be a random int between 1 and 3
                //Random rng = new Random();
                //this.world.getBlockState(block.getPosition()).setValue(SculkBeeNestCellBlock.MATURE, /*rng.nextInt(2) + 1*/ 3);
                BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get().setMature(this.world, this.world.getBlockState(block.getPosition()), block.getPosition());
                return;
            }
        }
    }

    /**
     * A custom planned block that wont place blocks if they are exposed to air
     */
    private class SculkNestCellPlannedBlock extends PlannedBlock
    {

        /**
         * Constructor
         *
         * @param worldIn The World
         * @param plannedBlockIn The BlockState
         * @param targetPosIn The Position to spawn it
         */
        public SculkNestCellPlannedBlock(ServerLevel worldIn, BlockState plannedBlockIn, BlockPos targetPosIn)
        {
            super(worldIn, plannedBlockIn, targetPosIn);
        }

        /**
         * Constructor
         *
         * @param worldIn The World
         * @param targetPosIn The Position to spawn it
         */
        public SculkNestCellPlannedBlock(ServerLevel worldIn, BlockPos targetPosIn)
        {
            super(worldIn, BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get().defaultBlockState(), targetPosIn);
        }

        /**
         * Outputs if the block we are trying to place, is able to be placed at a location
         * @return True if able to place, false otherwise.
         */
        @Override
        public boolean canBePlaced()
        {
            return this.VALID_BLOCKS_TO_REPLACE.test(world.getBlockState(targetPos)) &&
                    !BlockAlgorithms.isExposedToAir(world, targetPos);
        }
    }
}
