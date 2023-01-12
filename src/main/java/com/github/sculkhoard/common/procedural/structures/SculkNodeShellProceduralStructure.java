package com.github.sculkhoard.common.procedural.structures;

import com.github.sculkhoard.util.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;

public class SculkNodeShellProceduralStructure extends ProceduralStructure
{
    private final int RADIUS = 5;
    public SculkNodeShellProceduralStructure(ServerWorld worldIn, BlockPos originIn)
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

        ArrayList<BlockPos> blockPositionsInCircle = BlockAlgorithms.getBlockPosInCircle(origin, RADIUS, false);

        for(BlockPos position : blockPositionsInCircle)
        {
            //I dont know why i have to do -1 for the radius, something is wrong with the math
            if(BlockAlgorithms.getBlockDistance(origin, position) < RADIUS - 1)
            {
                plannedBlockQueue.add(new PlannedBlock(this.world, BlockRegistry.SCULK_ARACHNOID.get().defaultBlockState(), position));
            }
            else
            {
                plannedBlockQueue.add(new PlannedBlock(this.world, BlockRegistry.SCULK_DURA_MATTER.get().defaultBlockState(), position));
            }
        }
    }
}
