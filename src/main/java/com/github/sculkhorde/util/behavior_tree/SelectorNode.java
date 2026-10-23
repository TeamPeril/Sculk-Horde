package com.github.sculkhorde.util.behavior_tree;

import java.util.ArrayList;
import java.util.List;

public class SelectorNode extends BehaviorNode {

    protected final List<BehaviorNode> children = new ArrayList<>();
    protected int runningChildIndex = -1; // -1 means nothing is currently running

    public SelectorNode(String id) {
        super(id);
    }

    public SelectorNode addChild(BehaviorNode child) {
        children.add(child);
        return this;
    }

    @Override
    public boolean canExecute() {
        return !children.isEmpty();
    }

    @Override
    protected void onStart() {
        runningChildIndex = -1;
    }

    @Override
    protected BehaviorStatus onTick() {
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);

            // Check if this child can run (or is already the active running child)
            if (i == runningChildIndex || child.canExecute()) {

                // If a higher-priority child took over, abort the previously running lower-priority child
                if (runningChildIndex != -1 && runningChildIndex != i) {
                    children.get(runningChildIndex).abort();
                }

                BehaviorStatus status = child.execute();

                if (status == BehaviorStatus.RUNNING) {
                    runningChildIndex = i;
                    return BehaviorStatus.RUNNING;
                }

                if (status == BehaviorStatus.SUCCESS) {
                    runningChildIndex = -1;
                    return BehaviorStatus.SUCCESS;
                }

                // Child returned FAILURE -> clear and allow lower priorities to evaluate
                if (runningChildIndex == i) {
                    runningChildIndex = -1;
                }
            }
        }

        // If nothing could run or all failed, ensure any previously running task is aborted
        if (runningChildIndex != -1) {
            children.get(runningChildIndex).abort();
            runningChildIndex = -1;
        }

        return BehaviorStatus.FAILURE;
    }

    @Override
    public void abort() {
        if (runningChildIndex != -1 && runningChildIndex < children.size()) {
            children.get(runningChildIndex).abort();
            runningChildIndex = -1;
        }
        super.abort();
    }
}
