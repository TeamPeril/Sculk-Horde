package com.github.sculkhorde.util.behavior_tree;

public abstract class BehaviorNode {

    public enum BehaviorStatus {
        SUCCESS,
        FAILURE,
        RUNNING
    }

    public final String id;
    private BehaviorStatus status = BehaviorStatus.FAILURE;

    public BehaviorNode(String id) {
        this.id = id;
    }

    /** Condition: If false, the node will not run. */
    public abstract boolean canExecute();

    /** Lifecycle: Called once before the first onTick(). */
    protected void onStart() {}

    /** Action: Executed every tick while active. */
    protected abstract BehaviorStatus onTick();

    /** Lifecycle: Called once the action succeeds or fails. */
    protected void onEnd(BehaviorStatus finalStatus) {}

    public BehaviorStatus execute() {
        // Condition gate
        if (status != BehaviorStatus.RUNNING && !canExecute()) {
            return BehaviorStatus.FAILURE;
        }

        if (status != BehaviorStatus.RUNNING) {
            onStart();
        }

        status = onTick();

        if (status != BehaviorStatus.RUNNING) {
            onEnd(status);
        }

        return status;
    }

    public void abort() {
        if (status == BehaviorStatus.RUNNING) {
            onEnd(BehaviorStatus.FAILURE);
        }
        status = BehaviorStatus.FAILURE;
    }

    public BehaviorStatus getStatus() {
        return status;
    }
}
