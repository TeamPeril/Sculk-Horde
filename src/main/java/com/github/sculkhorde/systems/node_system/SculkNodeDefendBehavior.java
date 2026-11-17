package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.TickUnits;

public class SculkNodeDefendBehavior extends SculkNodeBehavior {

    protected long startTimeOfPlayerBeingInRange = 0;


    public SculkNodeDefendBehavior(String id, SculkNodeBlockEntity node) {
        super(id, node);
    }

    public SculkNodeDefendBehavior(SculkNodeBlockEntity node) {
        super("defense", node);
    }


    @Override
    public boolean canExecute() {

        boolean isPlayerNear = NodeUtil.isAnyPlayerNearNode(node, 300);

        if(isPlayerNear && startTimeOfPlayerBeingInRange == -1)
        {
            startTimeOfPlayerBeingInRange = node.getLevel().getGameTime();
        }
        else if(!isPlayerNear)
        {
            startTimeOfPlayerBeingInRange = -1;
        }

        if(isPlayerNear && TickUnits.hasTicksPassed(startTimeOfPlayerBeingInRange, node.getLevel(), TickUnits.convertSecondsToTicks(10)))
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
