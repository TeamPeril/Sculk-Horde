package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.util.behavior_tree.BehaviorNode;

public class SculkNodeBehavior extends BehaviorNode {

    public final SculkNodeBlockEntity node;

    public SculkNodeBehavior(String id, SculkNodeBlockEntity node) {
        super(id);
        this.node = node;
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
