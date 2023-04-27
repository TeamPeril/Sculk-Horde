package com.github.sculkhorde.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;
import java.util.function.Predicate;

public class BlockSearcher
{
    private ServerLevel level;
    private BlockPos origin;
    protected BlockPos target = BlockPos.ZERO;
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
        EXPLORING,
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

    protected boolean searchTick()
    {
        // Complete 20 times.
        for (int i = 0; i < searchIterationsPerTick; i++)
        {


            // Breadth-First Search

            if (stack.isEmpty()) {
                isSuccessful = false;
                target = BlockPos.ZERO;
                return true;
            }


            BlockPos currentBlock = stack.pop();

            // If the current block is a target, return it
            if (isValidTargetBlock.test(currentBlock)) {
                isSuccessful = true;
                target = currentBlock;
                return true;
            }


            // Get all possible directions
            ArrayList<BlockPos> possiblePaths = BlockAlgorithms.getNeighborsCube(currentBlock, false);


            // Add all neighbors to the queue
            for (BlockPos neighbor : possiblePaths) {

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

        return false;
    }

    public void tick()
    {
        if(state == State.IDLE)
        {
            stack.add(currentPosition);
            //queue.addAll(BlockAlgorithms.getAdjacentNeighbors(this.blockPosition()));
            state = State.SEARCHING;
        }
        else if (state == State.SEARCHING)
        {

            // IF not complete, just return;
            if(!searchTick())
            {
                return;
            }

            // If we can't find a target, finish
            if (target.equals(BlockPos.ZERO)) {
                state = State.FINISHED;
            }
            else // If we find target, start infecting
            {
                state = State.EXPLORING;
                visitedPositons.clear();
            }
        }
        else if (state == State.EXPLORING)
        {
            // Get Neighbors of Each Block
            ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(currentPosition, false);
            // Create a new list to store unobstructed neighbors
            ArrayList<BlockPos> unobstructedNeighbors = new ArrayList<>();
            // Check each neighbor for obstructions and add unobstructed neighbors to the new list
            for (BlockPos neighbor : neighbors)
            {
                if (!isObstructed.test(neighbor)) {
                    unobstructedNeighbors.add(neighbor);
                }
            }


            // If there are no non-obstructed neighbors, return
            if (neighbors.size() == 0) {
                return;
            }

            // Find the block that is closest to target in neighbors
            BlockPos closest = neighbors.get(0);
            for (BlockPos pos : neighbors)
            {
                if (BlockAlgorithms.getBlockDistance(pos, target) < BlockAlgorithms.getBlockDistance(closest, target)) {
                    closest = pos;
                }
            }


            // Move to the closest block
            currentPosition = closest;
            visitedPositons.put(closest.asLong(), true);

            // If we've reached the target block, conclude
            if (currentPosition.equals(target))
            {
                state = State.FINISHED;
            }
        }
        else if (state == State.FINISHED)
        {
            isFinished = true;
        }
    }
}