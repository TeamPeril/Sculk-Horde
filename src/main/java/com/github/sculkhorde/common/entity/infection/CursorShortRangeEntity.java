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
public class CursorShortRangeEntity extends Entity
{
    private int MAX_INFECTIONS = 100;
    private int infections = 0;

    private int MAX_RANGE = 100;
    private BlockPos origin = BlockPos.ZERO;

    private BlockPos target = BlockPos.ZERO;

    private long MAX_LIFETIME_SECONDS = 30;
    private long creationTickTime = System.nanoTime();
    private long lastTickTime = 0;
    private long TICK_INVTERVAL_SECONDS = 5;

    public boolean isSuccessful = false;



    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorShortRangeEntity(World worldIn) {super(EntityRegistry.CURSOR_SHORT_RANGE, worldIn);}

    public CursorShortRangeEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
        /**
         * BUG: This is not working properly. The entity is not being removed after 30 seconds.
         * When the entity is spawned, the creationTickTime is not altered in the statement below.
         * TODO Fix this bug.
         */
        creationTickTime = System.nanoTime();
    }

    public void setMaxInfections(int MAX_INFECTIONS) {
        this.MAX_INFECTIONS = MAX_INFECTIONS;
    }

    public void setMaxRange(int MAX_RANGE) {
        this.MAX_RANGE = MAX_RANGE;
    }

    @Override
    protected void defineSynchedData() {

    }

    /**
     * Use Breadth-First Search to find the nearest infectable block within a certain maximum distance.
     * @return the position of the nearest infectable block, or null if none is found
     */
    private BlockPos findNearestTargetBlock()
    {
        BlockPos origin = this.blockPosition();
        int nodesVisited = 0;
        // Breadth-First Search
        Queue<BlockPos> queue = new LinkedList<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            BlockPos currentBlock = queue.poll();
            if (SculkHorde.infestationConversionTable.infestationTable.isNormalVariant(this.level.getBlockState(currentBlock)))
            {
                isSuccessful = true;
                return currentBlock;
            }


            /// Will have bias for specific direction specified in the parameter
            ArrayList<Direction> possibleDirections = new ArrayList<>();
            possibleDirections.addAll(Arrays.asList(Direction.values()));
            // Remove any directions that are not solid blocks or if the block distance from the origin is too far
            possibleDirections.removeIf(dir -> !this.level.getBlockState(currentBlock.relative(dir)).isSolidRender(this.level, currentBlock.relative(dir)) || BlockAlgorithms.getBlockDistance(origin, currentBlock.relative(dir)) > MAX_RANGE);
            Collections.shuffle(possibleDirections);


            // Add all neighbors to the queue
            for (Direction dir : possibleDirections)
            {
                BlockPos neighbor = currentBlock.relative(dir);

                // If not visited and is a solid block, add to queue
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
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

        // Keep track of the origin
        if(origin == BlockPos.ZERO)
        {
            origin = this.blockPosition();
        }


        long currentLifeTime = TimeUnit.SECONDS.convert(System.nanoTime() - creationTickTime, TimeUnit.NANOSECONDS);
        // If entity has lived too long, remove it
        if(currentLifeTime >= MAX_LIFETIME_SECONDS) {
            this.remove();
            return;
        }
        else if (infections >= MAX_INFECTIONS)
        {
            this.remove();
            return;
        }

        // If we don't have a target, find one
        if(target.equals(BlockPos.ZERO)) {
            // Find nearest target with random direction
            target = findNearestTargetBlock();
        }

        // Get the neighbors of the current block
        ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(this.blockPosition());

        // Find the block that is cloest to target in neighbors
        BlockPos closest = neighbors.get(0);
        for (BlockPos pos : neighbors) {
            if (BlockAlgorithms.getBlockDistance(pos, target) < BlockAlgorithms.getBlockDistance(closest, target))
            {
                closest = pos;
            }
        }

        // If we can't find a target, return;
        if(target.equals(BlockPos.ZERO))
        {
            return;
        }

        // Move to the closest block
        this.setPos(closest.getX(), closest.getY(), closest.getZ());

        // If we've reached the target block, find a new target
        if (this.blockPosition().equals(target)) {

            target = BlockPos.ZERO;
            // Infect the block and increase the infection count
            SculkHorde.infestationConversionTable.infectBlock((ServerWorld) this.level, this.blockPosition());
            infections++;

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
