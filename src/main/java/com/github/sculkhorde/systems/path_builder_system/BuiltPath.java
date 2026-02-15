package com.github.sculkhorde.systems.path_builder_system;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a built path consisting of a sequence of BlockPos steps and
 * provides progress/query utilities for following that path.
 */
public class BuiltPath {

    private List<BlockPos> steps = new ArrayList<>();

    /**
     * Index of the next path step the mob should target.
     * 0 when starting, increases up to steps.size(). When it equals size, the path is complete.
     */
    private int currentStepIndex = 0;

    public BuiltPath() {}

    public BuiltPath(List<BlockPos> stepsIn) {
        setSteps(stepsIn);
    }

    public void setSteps(List<BlockPos> stepsIn) {
        if (stepsIn == null) {
            this.steps = new ArrayList<>();
        } else {
            this.steps = new ArrayList<>(stepsIn);
        }
        this.currentStepIndex = 0;
    }

    /**
     * Returns an unmodifiable view of the steps list to prevent external mutation.
     */
    public List<BlockPos> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public boolean hasPath() {
        return steps != null && !steps.isEmpty();
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public int getTotalSteps() {
        return steps == null ? 0 : steps.size();
    }

    public int getCompletedSteps() {
        return Math.min(currentStepIndex, getTotalSteps());
    }

    public void setComplete()
    {
        currentStepIndex = getTotalSteps();
    }

    public int getRemainingSteps() {
        int total = getTotalSteps();
        int completed = getCompletedSteps();
        return Math.max(0, total - completed);
    }

    public double getProgressFraction() {
        int total = getTotalSteps();
        if (total <= 0) { return 0.0; }
        return Math.min(1.0, (double) getCompletedSteps() / (double) total);
    }

    public Optional<BlockPos> getNextStep() {
        if (steps == null) { return Optional.empty(); }
        if (currentStepIndex < 0 || currentStepIndex >= steps.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(steps.get(currentStepIndex));
    }

    public boolean advanceToNextStep() {
        if (steps == null) { return false; }
        if (currentStepIndex < steps.size()) {
            currentStepIndex++;
            return currentStepIndex <= steps.size();
        }
        return false;
    }

    public boolean isPathComplete() {
        return getRemainingSteps() == 0;
    }

    public void resetProgress() {
        currentStepIndex = 0;
    }

    public BuiltPath createCopy() {
        BuiltPath copy = new BuiltPath();
        copy.steps = this.steps == null ? new ArrayList<>() : new ArrayList<>(this.steps);
        copy.currentStepIndex = this.currentStepIndex;
        return copy;
    }
}
