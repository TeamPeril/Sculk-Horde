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

    public void setPath(List<BlockPos> pathIn)
    {
        builtPath.setSteps(pathIn);
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
     * The proximity (in blocks) considered close enough to the desired destination.
     */
    public int getRequiredProximityToDesiredLocation()
    {
        return requiredProximityToDesiredLocation;
    }
}
