package com.github.sculkhorde.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.Predicate;

public class BlockSearcher
{
    protected boolean debugMode = false;
    protected ArmorStand debugStand;

    public int searchIterationsPerTick = 20;
    public int DELAY_BETWEEN_SEARCH_TICKS = TickUnits.convertSecondsToTicks(0);
    public int ticksSinceLastSearchTick = DELAY_BETWEEN_SEARCH_TICKS;

    protected ServerLevel level;
    public BlockPos origin;

    public int MAX_TARGETS = 1;
    public ArrayList<BlockPos> foundTargets = new ArrayList<>();
    public boolean ignoreBlocksNearTargets = false;
    public int distanceToIgnoreBlocksNearTargets = 5;

    protected int MAX_DISTANCE;
    protected Predicate<BlockPos> isObstructed;
    protected Predicate<BlockPos> isValidTargetBlock;

    ArrayList<BlockPos> queue = new ArrayList<BlockPos>();
    public boolean isSuccessful = false;
    public boolean isFinished = false;

    protected HashMap<Long, Boolean> visitedPositons = new HashMap<>();

    public BlockPos currentPosition;
    protected BlockPos positionToMoveAwayFrom;

    enum State
    {
        IDLE,
        SEARCHING,
        FINISHED
    }

    State state = State.IDLE;

    public BlockSearcher(ServerLevel level, BlockPos origin)
    {
        this.level = level;
        this.origin = origin;
        currentPosition = origin;
    }

    public BlockSearcher setMaxDistance(int maxDistance)
    {
        this.MAX_DISTANCE = maxDistance;
        return this;
    }

    public BlockSearcher setMaxTargets(int maxTargets)
    {
        this.MAX_TARGETS = maxTargets;
        return this;
    }

    public BlockSearcher setObstructionPredicate(Predicate<BlockPos> isObstructed)
    {
        this.isObstructed = isObstructed;
        return this;
    }

    public BlockSearcher setTargetBlockPredicate(Predicate<BlockPos> isValidTargetBlock)
    {
        this.isValidTargetBlock = isValidTargetBlock;
        return this;
    }

    public void setPositionToMoveAwayFrom(BlockPos positionToMoveAwayFrom)
    {
        this.positionToMoveAwayFrom = positionToMoveAwayFrom;
    }

    public void setDebugMode(boolean debugMode)
    {
        this.debugMode = debugMode;
    }

    protected boolean isNearOtherTargets(BlockPos position)
    {
        for(BlockPos target : foundTargets)
        {
            if(target.closerThan(position, distanceToIgnoreBlocksNearTargets))
            {
                return true;
            }
        }
        return false;
    }

    protected void searchTick()
    {
        boolean debugObstruction = false;

        // Complete 20 times.
        for (int i = 0; i < searchIterationsPerTick; i++)
        {
            // Spawn Debug Stand if Necessary
            if(debugStand == null && debugMode)
            {
                debugStand = new ArmorStand(level, origin.getX(), origin.getY(), origin.getZ());
                debugStand.setInvisible(true);
                debugStand.setNoGravity(true);
                debugStand.addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 3));
                level.addFreshEntity(debugStand);
            }

            // If the stack is empty, we are finished
            if (queue.isEmpty())
            {
                state = State.FINISHED;
                return;
            }
            // If we have found enough targets, we are finished
            else if(foundTargets.size() >= MAX_TARGETS)
            {
                state = State.FINISHED;
                return;
            }

            // If we have a position to move away from, sort the stack by distance to that position
            if(positionToMoveAwayFrom != null && !positionToMoveAwayFrom.equals(BlockPos.ZERO))
            {
                // Sort stack by distance to origin
                queue.sort(Comparator.comparingInt((BlockPos pos) ->
                {
                    return (int) BlockAlgorithms.getBlockDistance(pos, positionToMoveAwayFrom);
                }).reversed());
            }

            // Pop the next block off the stack
            BlockPos currentBlock = queue.get(0);
            queue.remove(0);
            if(debugObstruction) { level.setBlockAndUpdate(currentBlock, Blocks.GREEN_STAINED_GLASS.defaultBlockState()); }

            if(debugMode)
            {
                debugStand.teleportTo(currentBlock.getX() + 0.5, currentBlock.getY(), currentBlock.getZ() + 0.5);

            }

            // If the current block is a target, return true
            if (!ignoreBlocksNearTargets && isValidTargetBlock.test(currentBlock) || (ignoreBlocksNearTargets && isValidTargetBlock.test(currentBlock) && !isNearOtherTargets(currentBlock)))
            {
                foundTargets.add(currentBlock);

            }

            // Get all possible directions
            ArrayList<BlockPos> possiblePaths = BlockAlgorithms.getNeighborsCube(currentBlock, false);

            // Add all neighbors to the queue
            for (BlockPos neighbor : possiblePaths)
            {
                // If not visited and is a solid block, add to queue
                if (visitedPositons.getOrDefault(neighbor.asLong(), false))
                {
                    continue;
                }
                else if(isObstructed.test(neighbor))
                {
                    continue;
                }
                else if(BlockAlgorithms.getBlockDistance(origin, neighbor) > MAX_DISTANCE)
                {
                    continue;
                }

                queue.add(neighbor);
                visitedPositons.put(neighbor.asLong(), true);
            }
        }
    }

    public void idleTick()
    {
        queue.add(currentPosition);
        //queue.addAll(BlockAlgorithms.getAdjacentNeighbors(this.blockPosition()));
        state = State.SEARCHING;
    }

    public void finishedTick()
    {
        if(foundTargets.size() > 0)
        {
            isSuccessful = true;
        }
        else
        {
            isSuccessful = false;
        }
        isFinished = true;
    }

    public void tick()
    {
        ticksSinceLastSearchTick++;
        if(!(ticksSinceLastSearchTick >= DELAY_BETWEEN_SEARCH_TICKS))
        {
            return;
        }
        ticksSinceLastSearchTick = 0;

        switch (state)
        {
            case IDLE:
                idleTick();
                break;
            case SEARCHING:
                searchTick();
                break;
            case FINISHED:
                finishedTick();
                break;
        }
    }

    /**
     * Returns true if any of the targets are closer than the given distance
     * @param position The position to check
     * @param distance The distance to check
     * @return True if any of the targets are closer than the given distance
     */
    public boolean isAnyTargetCloserThan(BlockPos position, int distance)
    {
        for(BlockPos target : foundTargets)
        {
            if(target.closerThan(position, distance))
            {
                return true;
            }
        }
        return false;
    }
}