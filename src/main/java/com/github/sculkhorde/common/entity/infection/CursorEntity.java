package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public abstract class CursorEntity extends Entity
{
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

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorEntity(Level worldIn) {super(ModEntities.CURSOR_INFECTOR.get(), worldIn);}

    public CursorEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
        /*
         * BUG: This is not working properly. The entity is not being removed after 30 seconds.
         * When the entity is spawned, the creationTickTime is not altered in the statement below.
         * TODO Fix this bug.
         */
        creationTickTime = System.currentTimeMillis();
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

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            return false;
        }

        if(!state.isSolidRender(this.level(), pos))
        {
            return true;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        // This is to prevent the entity from getting stuck in a loop
        else if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }
        return false;
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
        level().setBlockAndUpdate(pos, Blocks.DIAMOND_BLOCK.defaultBlockState());
    }

    protected void spawnParticleEffects()
    {
        this.level().addParticle(ParticleTypes.TOTEM_OF_UNDYING, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.1D, 0.0D);
    }

    /**
     * Use Breadth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return true if complete. false if not complete.
     */
    protected boolean searchTick()
    {
        // Complete 20 times.
        for (int i = 0; i < searchIterationsPerTick; i++)
        {
            // Breadth-First Search

            if (queue.isEmpty()) {
                isSuccessful = false;
                target = BlockPos.ZERO;
                return true;
            }

            BlockPos currentBlock = queue.poll();

            // If the current block is a target, return it
            if (isTarget(this.level().getBlockState(currentBlock), currentBlock)) {
                isSuccessful = true;
                target = currentBlock;
                return true;
            }

            // Get all possible directions
            ArrayList<BlockPos> possiblePaths = BlockAlgorithms.getNeighborsCube(currentBlock, false);
            Collections.shuffle(possiblePaths);

            // Add all neighbors to the queue
            for (BlockPos neighbor : possiblePaths) {

                // If not visited and is a solid block, add to queue
                if (!visitedPositons.containsKey(neighbor.asLong()) && !isObstructed(this.level().getBlockState(neighbor), neighbor)) {
                    queue.add(neighbor);
                    visitedPositons.put(neighbor.asLong(), true);
                }
            }
        }

        return false;
    }

    @Override
    public void tick() {
        super.tick();

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
            queue.add(this.blockPosition());
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
            this.setPos(closest.getX() + 0.5, closest.getY(), closest.getZ() + 0.5);
            visitedPositons.put(closest.asLong(), true);

            // If we've reached the target block, find a new target
            if (this.blockPosition().equals(target))
            {
                target = BlockPos.ZERO;

                // If the block is not obstructed, infect it
                if(isTarget(level().getBlockState(this.blockPosition()), this.blockPosition()))
                {
                    // Infect the block and increase the infection count
                    transformBlock(this.blockPosition());
                }

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


    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     *
     * @param pCompound
     */
    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void defineSynchedData() {

    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }

}
