package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ParticleRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.block.BlockState;
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

import java.util.*;
import java.util.concurrent.TimeUnit;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public class CursorInfectorEntity extends Entity
{
    protected int MAX_INFECTIONS = 100;
    protected int infections = 0;

    protected int MAX_RANGE = 100;
    protected BlockPos origin = BlockPos.ZERO;

    protected BlockPos target = BlockPos.ZERO;

    protected long MAX_LIFETIME_SECONDS = 60;
    protected long creationTickTime = System.nanoTime();
    protected long lastTickTime = 0;
    protected float TICK_INVTERVAL_SECONDS = 0.25F;

    public boolean isSuccessful = false;

    //Create a hash map to store all visited nodes
    protected HashMap<Long, Boolean> visitedPositons = new HashMap<>();

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public CursorInfectorEntity(World worldIn) {super(EntityRegistry.CURSOR_SHORT_RANGE, worldIn);}

    public CursorInfectorEntity(EntityType<?> pType, World pLevel) {
        super(pType, pLevel);
        /*
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
    protected BlockPos findNearestTargetBlock()
    {
        BlockPos origin = this.blockPosition();
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
            // Remove any directions that are obstructed
            possibleDirections.removeIf(dir -> isObstructed(level.getBlockState(currentBlock.relative(dir)), currentBlock.relative(dir)));
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
            return;
        }
        lastTickTime = System.nanoTime();

        // Play Particles on Client
        if (this.level.isClientSide)
        {
            for(int i = 0; i < 2; ++i)
            {
                this.level.addParticle(ParticleRegistry.SCULK_CRUST_PARTICLE.get(), this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D), 0.0D, 0.0D, 0.0D);
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

        // If we can't find a target, return;
        if(target.equals(BlockPos.ZERO))
        {
            return;
        }

        // Get the neighbors of the current block
        ArrayList<BlockPos> neighbors = BlockAlgorithms.getNeighborsCube(this.blockPosition());
        // Remove andy obstructions
        neighbors.removeIf(blockPos -> isObstructed(level.getBlockState(blockPos), blockPos));


        // If there are no non-obstructed neighbors, return
        if(neighbors.size() == 0)
        {
            return;
        }

        // Find the block that is closest to target in neighbors
        BlockPos closest = neighbors.get(0);
        for (BlockPos pos : neighbors)
        {
            if (BlockAlgorithms.getBlockDistance(pos, target) < BlockAlgorithms.getBlockDistance(closest, target))
            {
                closest = pos;
            }
        }



        // Move to the closest block
        this.setPos(closest.getX(), closest.getY(), closest.getZ());
        visitedPositons.put(closest.asLong(), true);

        // If we've reached the target block, find a new target
        if (this.blockPosition().equals(target))
        {
            target = BlockPos.ZERO;
            // Infect the block and increase the infection count
            SculkHorde.infestationConversionTable.infectBlock((ServerWorld) this.level, this.blockPosition());
            infections++;
        }
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(!state.isSolidRender(this.level, pos))
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
