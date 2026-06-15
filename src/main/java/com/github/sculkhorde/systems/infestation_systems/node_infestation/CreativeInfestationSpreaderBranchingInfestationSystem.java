package com.github.sculkhorde.systems.infestation_systems.node_infestation;

import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class CreativeInfestationSpreaderBranchingInfestationSystem extends NodeBranchingInfestationSystem{

    public CreativeInfestationSpreaderBranchingInfestationSystem(BlockEntity parent, BlockPos origin, boolean isPerformanceExempt) {
        super(parent, origin, isPerformanceExempt);
        northInfectionTree = new CreativeInfectionTree(world, Direction.NORTH, origin, isPerformanceExempt);
        northInfectionTree.activate();

        southInfectionTree = new CreativeInfectionTree(world, Direction.SOUTH, origin, isPerformanceExempt);
        southInfectionTree.activate();

        eastInfectionTree = new CreativeInfectionTree(world, Direction.EAST, origin, isPerformanceExempt);
        eastInfectionTree.activate();

        westInfectionTree = new CreativeInfectionTree(world, Direction.WEST, origin, isPerformanceExempt);
        westInfectionTree.activate();

        upInfectionTree = new CreativeInfectionTree(world, Direction.UP, origin, isPerformanceExempt);
        upInfectionTree.activate();

        downInfectionTree = new CreativeInfectionTree(world, Direction.DOWN, origin, isPerformanceExempt);
        downInfectionTree.activate();
    }

    public boolean canBeActivated()
    {
        if(!TickUnits.hasTicksPassed(lastActivationCheckTime, world, CHECK_FOR_ACTIVATION_INTERVAL))
        {
            return false;
        }
        lastActivationCheckTime = world.getGameTime();

        if(parent == null || world == null || origin == null)
        {
            return false;
        }

        if(calculateSpawnPosition() != null)
        {
            return true;
        }

        return false;
    }


}
