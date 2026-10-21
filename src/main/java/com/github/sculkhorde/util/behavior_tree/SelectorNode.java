package com.github.sculkhorde.util.behavior_tree;

import java.util.ArrayList;
import java.util.List;

public class SelectorNode extends BehaviorNode {

    protected final List<BehaviorNode> children = new ArrayList<>();
    protected int currentChildIndex = 0;

    public SelectorNode(String id) {
        super(id);
    }

    public SelectorNode addChild(BehaviorNode child) {
        children.add(child);
        return this;
    }

    @Override
    public boolean canExecute() {
        // Always allowed to run if it has children
        return !children.isEmpty();
    }

    @Override
    protected void onStart() {
        currentChildIndex = 0;
    }

    @Override
    protected BehaviorStatus onTick() {
        while (currentChildIndex < children.size()) {
            BehaviorNode child = children.get(currentChildIndex);
            BehaviorStatus result = child.execute();

            if (result == BehaviorStatus.RUNNING) {
                return BehaviorStatus.RUNNING;
            }

            if (result == BehaviorStatus.SUCCESS) {
                return BehaviorStatus.SUCCESS;
            }

            // Child failed its condition or action; advance to next priority
            currentChildIndex++;
        }
        return BehaviorStatus.FAILURE;
    }

    @Override
    public void abort() {
        if (currentChildIndex < children.size()) {
            children.get(currentChildIndex).abort();
        }
        super.abort();
    }
}
