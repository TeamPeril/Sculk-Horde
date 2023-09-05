package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.infection.DevInfectionHandler;
import com.github.sculkhorde.common.entity.infection.SculkNodeInfectionHandler;
import com.github.sculkhorde.common.structures.procedural.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevMassInfectinator3000BlockEntity extends BlockEntity
{

    private long tickedAt = System.nanoTime();

    public static final int tickIntervalSeconds = 1;

    private DevInfectionHandler infectionHandler1;
    private DevInfectionHandler infectionHandler2;
    private DevInfectionHandler infectionHandler3;
    private DevInfectionHandler infectionHandler4;
    private DevInfectionHandler infectionHandler5;
    private DevInfectionHandler infectionHandler6;
    private DevInfectionHandler infectionHandler7;
    private DevInfectionHandler infectionHandler8;

    public DevMassInfectinator3000BlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DEV_MASS_INFECTINATOR_3000_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/

    /** Modifiers **/

    /** Events **/

    private void initializeInfectionHandler()
    {
        if(infectionHandler1 == null)
        {
            infectionHandler1 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler2 == null)
        {
            infectionHandler2 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler3 == null)
        {
            infectionHandler3 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler4 == null)
        {
            infectionHandler4 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler5 == null)
        {
            infectionHandler5 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler6 == null)
        {
            infectionHandler6 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler7 == null)
        {
            infectionHandler7 = new DevInfectionHandler(this, getBlockPos());
        }
        if(infectionHandler8 == null)
        {
            infectionHandler8 = new DevInfectionHandler(this, getBlockPos());
        }
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DevMassInfectinator3000BlockEntity blockEntity)
    {
        if(level.isClientSide)
        {
            return;
        }

        // Initialize the infection handler
        if(blockEntity.infectionHandler1 == null || blockEntity.infectionHandler2 == null || blockEntity.infectionHandler3 == null || blockEntity.infectionHandler4 == null || blockEntity.infectionHandler5 == null || blockEntity.infectionHandler6 == null || blockEntity.infectionHandler7 == null || blockEntity.infectionHandler8 == null)
        {
            blockEntity.initializeInfectionHandler();
        }

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        blockEntity.infectionHandler1.tick();
        blockEntity.infectionHandler2.tick();
        blockEntity.infectionHandler3.tick();
        blockEntity.infectionHandler4.tick();
        blockEntity.infectionHandler5.tick();
        blockEntity.infectionHandler6.tick();
        blockEntity.infectionHandler7.tick();
        blockEntity.infectionHandler8.tick();

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();

    }
}
