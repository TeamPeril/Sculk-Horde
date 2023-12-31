package com.github.sculkhorde.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Cursor {

    protected ServerLevel level;

    protected BlockPos pos;

    protected final Random random = new Random();

    protected boolean toBeDeleted = false;

    boolean isActive = false;

    protected enum State
    {
        IDLE,
        SEARCHING,
        EXPLORING,
        FINISHED
    }

    protected State state = State.IDLE;

    protected int MAX_TRANSFORMATIONS = 100;
    protected int currentTransformations = 0;
    protected int MAX_RANGE = 20;
    protected long MAX_LIFETIME_MILLIS = TimeUnit.SECONDS.toMillis(60 * 5);
    protected long creationTickTime = System.currentTimeMillis();
    protected long lastTickTime = 0;

    protected int searchIterationsPerTick = 20;
    protected long tickIntervalMilliseconds = 1000;

    protected BlockPos origin = BlockPos.ZERO;
    protected BlockPos target = BlockPos.ZERO;
    Queue<BlockPos> queue = new LinkedList<>();
    public boolean isSuccessful = false;

    //Create a hash map to store all visited nodes
    protected HashMap<Long, Boolean> visitedPositons = new HashMap<>();

    public Cursor(Level worldIn)
    {
        creationTickTime = System.currentTimeMillis();
        this.level = (ServerLevel) worldIn;
    }

    // Core Entity-like Methods

    /**
     * Returns the level this entity is currently in.
     */
    public ServerLevel level() { return level;}

    /**
     * Returns the position of this entity.
     */
    public BlockPos blockPosition() { return pos; }

    /**
     * Sets the position of this entity.
     */
    public void setPos(int x, int y, int z) { pos = new BlockPos(x, y, z); }

    public void setPos(BlockPos pos) { this.pos = pos; }

    public double getX() { return this.pos.getX(); }

    public double getY() { return this.pos.getY(); }

    public double getZ() { return this.pos.getZ(); }

    public Random getRandom() { return random; }

    public double getRandomX(double range) { return this.getX() + (this.getRandom().nextDouble() - 0.5D) * range; }

    public double getRandomY() { return this.getY() + this.getRandom().nextDouble() * 1.5D; }

    public double getRandomZ(double range) { return this.getZ() + (this.getRandom().nextDouble() - 0.5D) * range; }

    public void setToBeDeleted(boolean toBeDeleted) { this.toBeDeleted = toBeDeleted; }

    // Custom Methods


    public boolean isToBeDeleted() {
        return toBeDeleted;
    }

    public int getCurrentTransformations() {
        return currentTransformations;
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

    public void setMaxTransformations(int MAX_INFECTIONS) {
        this.MAX_TRANSFORMATIONS = MAX_INFECTIONS;
    }

    public void setMaxRange(int MAX_RANGE) {
        this.MAX_RANGE = MAX_RANGE;
    }

    public void setMaxLifeTimeMillis(long MAX_LIFETIME) {
        this.MAX_LIFETIME_MILLIS = MAX_LIFETIME;
    }

    public void setSearchIterationsPerTick(int iterations) {
        this.searchIterationsPerTick = iterations;
    }

    public void setTickIntervalMilliseconds(long milliseconds) {
        this.tickIntervalMilliseconds = milliseconds;
    }

    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    protected boolean isNotObstructed(BlockState state, BlockPos pos)
    {
      	if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            return false;
        }
      	
        if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return false;
        }
        else if(state.isAir())
        {
            return false;
        }
        // This is to prevent the entity from getting stuck in a loop
        else if(visitedPositons.containsKey(pos.asLong()))
        {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the block is considered a target.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered a target
     */
    protected boolean isTarget(BlockState state, BlockPos pos)
    {
        return state.equals(Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    protected void transformBlock(BlockPos pos)
    {
        level.setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    protected void spawnParticleEffects()
    {
        level.addParticle(ParticleTypes.TOTEM_OF_UNDYING, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.1D, 0.0D);
    }

    public void start()
    {
        isActive = true;
    }

    /**
     * Use Breadth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return true if complete. false if not complete.
     */
    protected boolean searchTick()
    {
        // Complete 20 times.
        for (int i = 0; i < Math.max(searchIterationsPerTick, 1); i++)
        {
            // Breadth-First Search

            if (queue.isEmpty()) {
                isSuccessful = false;
                setTarget(BlockPos.ZERO);
                return true;
            }

            BlockPos currentBlock = queue.poll();

            // If the current block is a target, return it
            if (isTarget(this.level().getBlockState(currentBlock), currentBlock)) {
                isSuccessful = true;
                setTarget(currentBlock);
                return true;
            }

            // Get all possible directions
            ArrayList<BlockPos> possiblePaths = BlockAlgorithms.getNeighborsCube(currentBlock, false);
            Collections.shuffle(possiblePaths);

            // Add all neighbors to the queue
            for (BlockPos neighbor : possiblePaths) {

                // If not visited and is a solid block, add to queue
                if (!visitedPositons.containsKey(neighbor.asLong()) && isNotObstructed(this.level().getBlockState(neighbor), neighbor)) {
                    queue.add(neighbor);
                    visitedPositons.put(neighbor.asLong(), true);
                }
            }
        }

        return false;
    }

    public void tick() {

        float timeElapsedMilliSeconds = System.currentTimeMillis() - lastTickTime;
        double tickIntervalMillisecondsAfterMultiplier = tickIntervalMilliseconds - (tickIntervalMilliseconds * (ModConfig.SERVER.infestation_speed_multiplier.get()));
        if (timeElapsedMilliSeconds < Math.max(tickIntervalMillisecondsAfterMultiplier, 1)) {
            return;
        }

        lastTickTime = System.currentTimeMillis();

        // Play Particles on Client
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
            setState(State.FINISHED);
        }
        else if (currentTransformations >= MAX_TRANSFORMATIONS)
        {
            setState(State.FINISHED);
        }

        if(state == State.IDLE)
        {
            if(!isActive)
            {
                return;
            }
            queue.add(this.blockPosition());
            setState(State.SEARCHING);
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
                setState(state = State.FINISHED);
            }
            else // If we find target, start infecting
            {
                setState(state = State.EXPLORING);
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
                if (isNotObstructed(level().getBlockState(neighbor), neighbor)) {
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

            // If we've reached the target block, find a new target
            if (this.blockPosition().equals(target))
            {
                setTarget(BlockPos.ZERO);
                BlockState stateOfCurrentBlock = level().getBlockState(this.blockPosition());

                boolean isTarget = isTarget(stateOfCurrentBlock, this.blockPosition());
                boolean isNotObstructed = isNotObstructed(stateOfCurrentBlock, this.blockPosition());
                // If the block is not obstructed, infect it
                if(isTarget && isNotObstructed)
                {
                    // Infect the block and increase the infection count
                    transformBlock(this.blockPosition());
                    currentTransformations++;
                }

                setState(State.SEARCHING);
                visitedPositons.clear();
                queue.clear();
                queue.add(this.blockPosition());
            }

            // Mark position as visited
            visitedPositons.put(closest.asLong(), true);
        }
        else if (state == State.FINISHED)
        {
            this.setToBeDeleted(true);
        }

    }

}
