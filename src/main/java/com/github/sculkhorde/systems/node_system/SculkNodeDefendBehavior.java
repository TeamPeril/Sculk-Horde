package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.TickUnits;

public class SculkNodeDefendBehavior extends SculkNodeBehavior {

    protected long startTimeOfPlayerBeingInRange = 0;


    public SculkNodeDefendBehavior(String id, SculkNodeBlockEntity node) {
        super(id, node);
    }

    @Override
    public boolean canExecute() {

        if(NodeUtil.isAnyPlayerNearNode(node, 300) && startTimeOfPlayerBeingInRange == 0)
        {
            startTimeOfPlayerBeingInRange = node.getLevel().getGameTime();
        }
        else if(!NodeUtil.isAnyPlayerNearNode(node, 300))
        {
            startTimeOfPlayerBeingInRange = 0;
        }

        if(TickUnits.hasTicksPassed(startTimeOfPlayerBeingInRange, node.getLevel(), TickUnits.convertSecondsToTicks(10)))
        {
            return true;
        }

        return false;
    }

    @Override
    protected BehaviorStatus onTick() {
        return null;
    }
}
