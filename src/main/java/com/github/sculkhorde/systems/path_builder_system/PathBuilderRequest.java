package com.github.sculkhorde.systems.path_builder_system;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class PathBuilderRequest {

    public final UUID uuid = UUID.randomUUID();
    protected BlockPos desiredDestination;
    protected BlockPos startLocation;

    protected Predicate<BlockPos> isObstructed;
    protected Predicate<BlockPos> isValidTargetBlock;

    protected boolean hasPathBuildingStarted = false;
    protected boolean isPathBuildingInProgress = false;
    protected boolean isSearching = false;
    protected boolean isPathBuildSuccessful = false;


    protected ServerLevel level;
    protected int requiredProximityToDesiredLocation = 5;

    protected BuiltPath builtPath = new BuiltPath();

    public PathBuilderRequest(ServerLevel levelIn, BlockPos desiredDestinationIn, BlockPos startLocationIn, int requiredProximityIn, Predicate<BlockPos> isObstructedIn, Predicate<BlockPos> isValidTargetBlockIn)
    {
        level = levelIn;
        desiredDestination = desiredDestinationIn;
        startLocation = startLocationIn;
        requiredProximityToDesiredLocation = requiredProximityIn;
        isObstructed = isObstructedIn;
        isValidTargetBlock = isValidTargetBlockIn;
    }

    public boolean hasPathBuildStarted()
    {
        return hasPathBuildingStarted;
    }

    public boolean isPathBuildingInProgress()
    {
        return isPathBuildingInProgress;
    }

    public boolean isPathBuildSuccessful()
    {
        return isPathBuildSuccessful;
    }

    public boolean isSearching()
    {
        return isSearching;
    }

    public Optional<List<BlockPos>> getFinalPath()
    {
        if(isPathBuildingInProgress)
        {
            return Optional.empty();
        }
        else if(!isPathBuildSuccessful)
        {
            return Optional.empty();
        }
        else if(!builtPath.hasPath())
        {
            return Optional.empty();
        }
        return Optional.of(builtPath.getSteps());
    }

    public BlockPos getDesiredDestination()
    {
        return desiredDestination;
    }

    public BlockPos getStartLocation()
    {
        return startLocation;
    }

    public ServerLevel getLevel()
    {
        return level;
    }

    public void setPath(BuiltPath pathIn)
    {
        builtPath = pathIn;
    }

    public void startPathBuilding()
    {
        hasPathBuildingStarted = true;
    }

    public void setPathBuildingInProgress(boolean value)
    {
        isPathBuildingInProgress = value;
    }

    public List<BlockPos> getPath()
    {
        return builtPath.getSteps();
    }

    /**
     * Direct access to the built path object for advanced operations.
     */
    public BuiltPath getBuiltPath()
    {
        return builtPath;
    }

    /**
     * Returns true if this request currently has a non-empty path.
     */
    public boolean hasPath()
    {
        return builtPath.hasPath();
    }

    /**
     * Returns the index of the next step the mob should move toward.
     */
    public int getCurrentStepIndex()
    {
        return builtPath.getCurrentStepIndex();
    }

    /**
     * Returns the total number of steps in the path.
     */
    public int getTotalSteps()
    {
        return builtPath.getTotalSteps();
    }

    /**
     * Returns how many steps have been completed (i.e., how many indices we have advanced past).
     */
    public int getCompletedSteps()
    {
        return builtPath.getCompletedSteps();
    }

    /**
     * Returns how many steps remain (including the next target step, if any).
     */
    public int getRemainingSteps()
    {
        int total = getTotalSteps();
        int completed = getCompletedSteps();
        return Math.max(0, total - completed);
    }

    /**
     * Returns a value in [0.0, 1.0] representing progress along the path.
     * When there are no steps, returns 0.0.
     */
    public double getProgressFraction()
    {
        int total = getTotalSteps();
        if (total <= 0) { return 0.0; }
        return Math.min(1.0, (double) getCompletedSteps() / (double) total);
    }

    /**
     * Returns the next BlockPos the mob should move toward, if any.
     */
    public Optional<BlockPos> getNextStep()
    {
        return builtPath.getNextStep();
    }

    /**
     * Advances to the next step in the path. Returns true if successfully advanced, false if already complete.
     */
    public boolean advanceToNextStep()
    {
        return builtPath.advanceToNextStep();
    }

    /**
     * Returns true if the mob has completed all steps along the path.
     */
    public boolean isPathComplete()
    {
        return getRemainingSteps() == 0;
    }

    /**
     * Resets following progress back to the first step (does not change the path list itself).
     */
    public void resetProgress()
    {
        builtPath.resetProgress();
    }

    /**
     * The proximity (in blocks) considered close enough to the desired destination.
     */
    public int getRequiredProximityToDesiredLocation()
    {
        return requiredProximityToDesiredLocation;
    }

}
