package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.behavior_tree.BehaviorNode;

import java.util.Optional;

public class SculkNodeBehavior extends BehaviorNode {

    public final SculkNodeBlockEntity node;

    public SculkNodeBehavior(String id, SculkNodeBlockEntity node) {
        super(id);
        this.node = node;
    }

    public SculkNodeBlockEntity getBlockEntity()
    {
        return node;
    }

    public Optional<ModSavedData.NodeEntry> getNodeEntry()
    {
        return NodeUtil.getNodeEntry(node);
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
