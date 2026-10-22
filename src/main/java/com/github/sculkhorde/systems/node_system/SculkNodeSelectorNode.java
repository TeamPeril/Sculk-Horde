package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.util.behavior_tree.SelectorNode;

public class SculkNodeSelectorNode extends SelectorNode {

    public final SculkNodeBlockEntity node;

    public SculkNodeSelectorNode(String id, SculkNodeBlockEntity node) {
        super(id);
        this.node = node;
    }

    public SculkNodeSelectorNode(SculkNodeBlockEntity node)
    {
        this("main_selector", node);
    }






}
