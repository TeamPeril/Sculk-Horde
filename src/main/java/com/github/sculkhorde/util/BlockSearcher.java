package com.github.sculkhorde.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.function.Predicate;

public class BlockSearcher
{
    private ServerLevel level;
    private BlockPos origin;
    public int MAX_TARGETS = 1;
    public ArrayList<BlockPos> foundTargets = new ArrayList<>();
    private int MAX_DISTANCE;
    private Predicate<BlockPos> isObstructed;
    private Predicate<BlockPos> isValidTargetBlock;
    Stack<BlockPos> stack = new Stack<>();
    public boolean isSuccessful = false;
    public boolean isFinished = false;
    protected HashMap<Long, Boolean> visitedPositons = new HashMap<>();
    protected int searchIterationsPerTick = 20;
    public BlockPos currentPosition;

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

    protected void searchTick()
    {
        // Complete 20 times.
        for (int i = 0; i < searchIterationsPerTick; i++)
        {
            if (stack.isEmpty())
            {
                state = State.FINISHED;
                return;
            }
            else if(foundTargets.size() >= MAX_TARGETS)
            {
                state = State.FINISHED;
                return;
            }

            BlockPos currentBlock = stack.pop();

            // If the current block is a target, return true
            if (isValidTargetBlock.test(currentBlock)) {
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

                stack.add(neighbor);
                visitedPositons.put(neighbor.asLong(), true);
            }
        }
    }

    public void idleTick()
    {
        stack.add(currentPosition);
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
}