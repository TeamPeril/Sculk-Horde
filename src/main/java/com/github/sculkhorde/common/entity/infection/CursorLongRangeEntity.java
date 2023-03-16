package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public class CursorLongRangeEntity extends Entity {
    private int MAX_DISTANCE = 1000;
    private int distanceTraveled = 0;

    private BlockPos target = BlockPos.ZERO;
    private Direction direction = Direction.DOWN;

    private long MAX_LIFETIME_SECONDS = 10;
    private long creationTickTime = System.nanoTime();
    private long lastTickTime = 0;
    private long TICK_INVTERVAL_SECONDS = 5;

    public BlockPos lastKnownBlockPos = BlockPos.ZERO;
    public boolean isSuccessful = false;



    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorLongRangeEntity(World worldIn) {super(EntityRegistry.CURSOR_LONG_RANGE, worldIn);}

    public CursorLongRangeEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
        this.distanceTraveled = 0;
        /**
         * BUG: This is not working properly. The entity is not being removed after 30 seconds.
         * When the entity is spawned, the creationTickTime is not altered in the statement below.
         * TODO Fix this bug.
         */
        creationTickTime = System.nanoTime();
    }

    public void setMAX_DISTANCE(int MAX_DISTANCE) {
        this.MAX_DISTANCE = MAX_DISTANCE;
    }

    public void setDirection(Direction direction)
    {
        this.direction = direction;
    }

    @Override
    protected void defineSynchedData() {

    }

    /**
     * Use Depth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return the position of the nearest infectable block, or null if none is found
     */
    private BlockPos findNearestTargetBlockDepthFirstSearch(Direction direction) {
        BlockPos origin = this.blockPosition();
        int nodesVisited = 0;
        // Breadth-First Search
        Stack<BlockPos> stack = new Stack<>();
        Set<BlockPos> visited = new HashSet<>();
        stack.push(origin);
        visited.add(origin);

        while (!stack.isEmpty())
        {
            BlockPos currentBlock = stack.pop();
            nodesVisited++; // Keep track of how many nodes we've visited

            if (SculkHorde.infestationConversionTable.infestationTable.isNormalVariant(this.level.getBlockState(currentBlock))) {
                return currentBlock;
            }

            // If we've visited too many nodes, return ZERO
            if (nodesVisited > MAX_DISTANCE) {
                break;
            }

            // Will have bias for specific direction specified in the parameter
            ArrayList<Direction> possibleDirections = new ArrayList<>();

            // 25% chance to just add specific direction if it is a solid block
            if(Math.random() < 0.25 && this.level.getBlockState(currentBlock.relative(direction)).isSolidRender(this.level, currentBlock.relative(direction)))
            {
                possibleDirections.add(direction);
            }
            else
            {
                possibleDirections.addAll(Arrays.asList(Direction.values()));
                // Remove any directions that are not solid blocks
                possibleDirections.removeIf(dir -> !this.level.getBlockState(currentBlock.relative(dir)).isSolidRender(this.level, currentBlock.relative(dir)));
            }

            // Add all neighbors to the queue
            for (Direction dir : possibleDirections) {
                BlockPos neighbor = currentBlock.relative(dir);

                // If not visited and is a solid block, add to queue
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    visited.add(neighbor);
                }
            }
        }

        this.remove();
        isSuccessful = false;
        return BlockPos.ZERO;
    }

    @Override
    public void tick()
    {
        super.tick();

        float timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - lastTickTime, TimeUnit.NANOSECONDS);

        if(timeElapsed < TICK_INVTERVAL_SECONDS)
        {
            //return;
        }
        lastTickTime = System.nanoTime();

        // Play Particles on Client
        if (this.level.isClientSide)
        {
            for(int i = 0; i < 2; ++i)
            {
                this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
            }
            return;
        }


        long currentLifeTime = TimeUnit.SECONDS.convert(System.nanoTime() - creationTickTime, TimeUnit.NANOSECONDS);
        // If entity has lived too long, remove it
        if(currentLifeTime >= MAX_LIFETIME_SECONDS || this.distanceTraveled >= MAX_DISTANCE) {
            this.remove();
            return;
        }

        // If we don't have a target, find one
        if(target.equals(BlockPos.ZERO)) {
            // Find nearest target with random direction
            target = findNearestTargetBlockDepthFirstSearch(direction);
        }

        // Get the neighbors of the current block
        ArrayList<BlockPos> neighbors = BlockAlgorithms.getAdjacentNeighbors(blockPosition());

        // Find the block that is cloest to target in neighbors
        BlockPos closest = neighbors.get(0);
        for (BlockPos pos : neighbors)
        {
            if (BlockAlgorithms.getBlockDistance(pos, target) < BlockAlgorithms.getBlockDistance(closest, target))
            {
                closest = pos;
            }
        }

        // If we can't find a target, return;
        if(target.equals(BlockPos.ZERO))
        {
            this.isSuccessful = false;
            return;
        }

        // Move to the closest block
        this.setPos(closest.getX(), closest.getY(), closest.getZ());

        // Keep track of last known position
        lastKnownBlockPos = this.blockPosition();
        SculkHorde.infestationConversionTable.infectBlock((ServerWorld) this.level, this.blockPosition());
        // Keep track of how far we've traveled
        distanceTraveled++;

        // If we've reached the target block, find a new target
        if (this.blockPosition().equals(target)) {

            isSuccessful = true;
            remove();
        }


    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     *
     * @param pCompound
     */
    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {

    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public void setTarget(BlockPos target) {
        this.target = target;
    }


}
