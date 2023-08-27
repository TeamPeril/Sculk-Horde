package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.*;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public class CursorProberEntity extends CursorSurfaceInfectorEntity {

    Direction preferedDirection = Direction.NORTH;
    Stack<BlockPos> stack = new Stack<>();

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorProberEntity(Level worldIn) {super(ModEntities.CURSOR_PROBER.get(), worldIn);}

    public CursorProberEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }

    protected void setPreferedDirection(Direction direction) {
        this.preferedDirection = direction;
    }

    /**
     * Use Depth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return the position of the nearest infectable block, or null if none is found
     */
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
            if (isTarget(this.level().getBlockState(currentBlock), currentBlock)) {
                isSuccessful = true;
                target = currentBlock;
                return true;
            }

            // Get all possible directions
            ArrayList<BlockPos> possiblePaths = BlockAlgorithms.getNeighborsCube(currentBlock, false);
            Collections.shuffle(possiblePaths);

            // Add all neighbors to the stack
            for (BlockPos neighbor : possiblePaths) {

                // If not visited and is a solid block, add to stack
                if (!visitedPositons.containsKey(neighbor.asLong()) && !isObstructed(this.level().getBlockState(neighbor), neighbor)) {
                    stack.add(neighbor);
                    visitedPositons.put(neighbor.asLong(), true);
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {

        float timeElapsedMilliSeconds = System.currentTimeMillis() - lastTickTime;

        if (timeElapsedMilliSeconds < tickIntervalMilliseconds) {
            return;
        }
        lastTickTime = System.currentTimeMillis();


        // Play Particles on Client
        if (this.level().isClientSide) {
            for (int i = 0; i < 2; ++i) {
                spawnParticleEffects();
            }
            return;
        }

        // Keep track of the origin
        if (origin == BlockPos.ZERO)
        {
            origin = this.blockPosition();
        }

        long currentLifeTimeMilliseconds = System.currentTimeMillis() - creationTickTime;

        // Convert to seconds
        // If entity has lived too long, remove it
        if (currentLifeTimeMilliseconds >= MAX_LIFETIME_MILLIS)
        {
            state = State.FINISHED;
        }
        else if (currentTransformations >= MAX_TRANSFORMATIONS)
        {
            state = State.FINISHED;
        }

        if(state == State.IDLE)
        {
            stack.add(this.blockPosition());
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
            ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(this.blockPosition(), false);
            // Create a new list to store unobstructed neighbors
            ArrayList<BlockPos> unobstructedNeighbors = new ArrayList<>();
            // Check each neighbor for obstructions and add unobstructed neighbors to the new list
            for (BlockPos neighbor : neighbors)
            {
                if (!isObstructed(level().getBlockState(neighbor), neighbor)) {
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
            this.setPos(closest.getX(), closest.getY(), closest.getZ());
            visitedPositons.put(closest.asLong(), true);

            // If we've reached the target block, die then report successful
            if (this.blockPosition().equals(target))
            {
                target = BlockPos.ZERO;
                // Infect the block and increase the infection count
                transformBlock(this.blockPosition());
                currentTransformations++;
                state = State.SEARCHING;
                visitedPositons.clear();
                queue.clear();
                queue.add(this.blockPosition());
            }
        }
        else if (state == State.FINISHED)
        {
            this.remove(RemovalReason.DISCARDED);
        }
    }
}
