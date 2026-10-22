package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;

public class SculkNodeDefendBehavior extends SculkNodeBehavior {

    public SculkNodeDefendBehavior(String id, SculkNodeBlockEntity node) {
        super(id, node);
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    protected BehaviorStatus onTick() {
        return null;
    }
}
