package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.*;
import java.util.concurrent.TimeUnit;

/** This Entity is used to traverse the world and infect blocks.
 * Once spawned, it will use breadth-first search to find the nearest block to infect.
 * Once it has found a block to infect, it will infect it and then move on to the next block.
 * This will continue until it has either reached its max distance or max infections.
 */
public class CursorBridgerEntity extends Entity {
    private int MAX_DISTANCE = 1000;
    private int distanceTraveled = 0;

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
    public CursorBridgerEntity(Level worldIn) {super(EntityRegistry.CURSOR_LONG_RANGE, worldIn);}

    public CursorBridgerEntity(EntityType<?> pType, Level pLevel) {
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


        // Get the neighbors of the current block
        ArrayList<BlockPos> neighbors = BlockAlgorithms.getAdjacentNeighbors(blockPosition());

        // Find the block that is cloest to target in neighbors
        BlockPos closest = neighbors.get(0);
        for (BlockPos pos : neighbors)
        {
            closest = pos;
            if(this.level.getBlockState(closest).isAir())
            {
                this.level.setBlockAndUpdate(closest, BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get().defaultBlockState());
            }

        }


        // Move to the closest block
        this.setPos(closest.getX(), closest.getY(), closest.getZ());

        // Keep track of last known position
        lastKnownBlockPos = this.blockPosition();

        // If block break speed is < 3, then covert it to a sculk block
        if (this.level.getBlockState(this.blockPosition()).getDestroySpeed(this.level, this.blockPosition()) <= 3)
        {
            this.level.setBlockAndUpdate(this.blockPosition(), BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get().defaultBlockState());
        }

        // Keep track of how far we've traveled
        distanceTraveled++;

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
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }



}
