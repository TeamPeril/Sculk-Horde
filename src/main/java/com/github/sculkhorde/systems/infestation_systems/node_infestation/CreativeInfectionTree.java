package com.github.sculkhorde.systems.infestation_systems.node_infestation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

public class CreativeInfectionTree extends InfectionTree {

    /**
     * Creates a new binary tree with the given value.
     *
     * @param world
     * @param direction
     * @param rootPos
     * @param isPerformanceExempt
     */
    public CreativeInfectionTree(ServerLevel world, Direction direction, BlockPos rootPos, boolean isPerformanceExempt) {
        super(world, direction, rootPos, isPerformanceExempt);
    }

    @Override
    protected boolean canTick() {
        return true;
    }
}
