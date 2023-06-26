package com.github.sculkhorde.common.structures.procedural;

import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class SculkLivingRockProceduralStructure extends ProceduralStructure
{

    public SculkLivingRockProceduralStructure(ServerLevel worldIn, BlockPos originIn)
    {
        super(worldIn, originIn);
    }

    /**
     * This method generates a plan for a structure.
     * It clears the plannedBlockQueue and generates a circle base and pillars.
     * The circle base is a 2D circle of blocks with a specified diameter, centered at the origin.
     * The pillars are vertical columns of blocks that are generated above each block in the circle base.
     * The height of each pillar is determined by how close the corresponding block in the circle base is to the origin.
     * Additionally, it will generate downward pillars as well.
     */
    @Override
    public void generatePlan()
    {
        // Clear the planned block queue before generating a new plan
        this.plannedBlockQueue.clear();

        // Iterate through all child structures and generate a plan for them
        for(ProceduralStructure entry : childStructuresQueue) {
            entry.generatePlan();
        }
        // Get a random diameter for the circle base between 5 and 9
        int DIAMETER = (int) BlockAlgorithms.getSudoRNGFromPosition(origin, 5, 9);
        // Get random offsets for the X and Z coordinates of the highest pillar
        int pillarOffsetX = (int) BlockAlgorithms.getSudoRNGFromPosition(origin, ((DIAMETER/2) - 1) * -1, (DIAMETER/2) - 1);
        int pillarOffsetZ = (int) BlockAlgorithms.getSudoRNGFromPosition(origin, ((DIAMETER/2) - 1) * -1, (DIAMETER/2) - 1);
        // Determine the highest pillar's position by applying the offsets to the origin
        BlockPos highestPillar = origin.offset(pillarOffsetX, 0, pillarOffsetZ);

        // The height degradation multiplier is half of the diameter
        int HEIGHT_DEGRADE_MULTIPLIER = (DIAMETER/2);
        // The maximum height is the square of the height degradation multiplier, or the diameter times 2
        int MAXIMUM_HEIGHT = Math.max(HEIGHT_DEGRADE_MULTIPLIER * (DIAMETER), DIAMETER * 2);
        //Generate Circle Base
        ArrayList<PlannedBlock> circleBase = BlockAlgorithms.generate2DCirclePlan(origin, DIAMETER, world, BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get().defaultBlockState());
        // Create a new ArrayList to store all the planned pillar blocks
        ArrayList<PlannedBlock> pillarList = new ArrayList<PlannedBlock>();

        // Iterate through each block in the circle base
        for (PlannedBlock plannedBlock : circleBase) {
            // Get the position of the current block
            BlockPos pos = plannedBlock.getPosition();
            // Calculate the distance of the current block to the highestPillar
            int distanceFromHighestPillar = (int) BlockAlgorithms.getBlockDistance(highestPillar, pos);
            // Calculate the height of the pillar for the current block
            int height =  ((MAXIMUM_HEIGHT - distanceFromHighestPillar * HEIGHT_DEGRADE_MULTIPLIER) - (int) BlockAlgorithms.getSudoRNGFromPosition(pos, 0, 2));

            // Generate upwards pillar
            for (int y = pos.getY(); y < pos.getY() + height; y++) {
                BlockPos newPos = new BlockPos(pos.getX(), y, pos.getZ());
                if(BlockAlgorithms.getSudoRNGFromPosition(newPos, 0, 10) == 0)
                {
                    pillarList.add(new PlannedBlock(world, BlockRegistry.CALCITE_ORE.get().defaultBlockState(), newPos));
                }
                else
                {
                    pillarList.add(new PlannedBlock(world, BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get().defaultBlockState(), newPos));
                }

            }

            // Generate downward pillars
            for (int y = pos.getY(); y > pos.getY() - height; y--) {
                BlockPos newPos = new BlockPos(pos.getX(), y, pos.getZ());
                pillarList.add(new PlannedBlock(world, BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get().defaultBlockState(), newPos));
            }
        }
        plannedBlockQueue.addAll(circleBase);
        plannedBlockQueue.addAll(pillarList);

    }

}
