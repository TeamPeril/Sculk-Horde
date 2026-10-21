package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.util.behavior_tree.BehaviorNode;

public class SculkNodeDefenseBehavior extends BehaviorNode {
    public SculkNodeDefenseBehavior(String id) {
        super(id);
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
