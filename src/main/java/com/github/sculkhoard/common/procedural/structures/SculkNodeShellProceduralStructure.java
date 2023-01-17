package com.github.sculkhoard.common.procedural.structures;

import com.github.sculkhoard.util.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Optional;

public class SculkNodeShellProceduralStructure extends ProceduralStructure
{
    private final int RADIUS = 5;
    public SculkNodeShellProceduralStructure(ServerWorld worldIn, BlockPos originIn)
    {
        super(worldIn, originIn);
    }

    public Optional<BlockPos> findLivingRockStructureIfExists(ServerWorld world, BlockPos placementPos)
    {
        if(world.getBlockState(placementPos).getBlock().equals(BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get()))
        {
            return Optional.of(placementPos);
        }

        int offsetBelow = 0;
        int offsetAbove = 0;

        //Search all the way to bedrock for the structure
        while(placementPos.offset(0, offsetBelow, 0).getY() > 0)
        {
            if(world.getBlockState(placementPos.offset(0, offsetBelow, 0)).getBlock().equals(BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get()))
            {
                return Optional.of(placementPos.offset(0, offsetBelow, 0));
            }
            offsetBelow --;
        }

        while(placementPos.offset(0, offsetAbove, 0).getY() > world.getHeight())
        {
            if(world.getBlockState(placementPos.offset(0, offsetAbove, 0)).getBlock().equals(BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get()))
            {
                return Optional.of(placementPos.offset(0, offsetAbove, 0));
            }
            offsetAbove ++;
        }
        return Optional.empty();
    }

    public BlockPos findLivingRockPlacementPosition(ServerWorld world, BlockPos placementPos)
    {
        if(findLivingRockStructureIfExists(world, placementPos).isPresent())
            return findLivingRockStructureIfExists(world, placementPos).get();

        int attempts = 0;
        int MAX_ATTEMPTS = 100;
        int offsetBelow = 0;
        int offsetAbove = 0;
        //Try and find solid ground to place this block on
        while(world.getBlockState(placementPos.offset(0, offsetBelow, 0)).canBeReplaced(Fluids.WATER) && attempts <= MAX_ATTEMPTS)
        {
            offsetBelow --;
            attempts++;
        }
        attempts = 0;
        while(world.getBlockState(placementPos.offset(0, offsetAbove, 0)).canBeReplaced(Fluids.WATER) && attempts <= MAX_ATTEMPTS)
        {
            offsetAbove ++;
            attempts++;
        }

        if(Math.abs(offsetBelow) < offsetAbove)
        {
            return placementPos.offset(0, offsetBelow, 0);
        }
        return placementPos.offset(0, offsetAbove, 0);
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

        ArrayList<BlockPos> surroundingLivingRock = BlockAlgorithms.getPointsOnCircumference(origin, 5, RADIUS*3);
        surroundingLivingRock.addAll(BlockAlgorithms.getPointsOnCircumference(origin, 10, RADIUS*6));
        surroundingLivingRock.addAll(BlockAlgorithms.getPointsOnCircumference(origin, 20, RADIUS*9));
        for(BlockPos position : surroundingLivingRock)
        {
            plannedBlockQueue.add(new PlannedBlock(this.world, BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get().defaultBlockState(), findLivingRockPlacementPosition(this.world, position)));
        }
    }
}
