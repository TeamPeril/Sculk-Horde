package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.structures.procedural.SculkLivingRockProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkLivingRockRootBlockEntity extends BlockEntity
{
    private long tickedAt = System.nanoTime();

    private SculkLivingRockProceduralStructure proceduralStructure;

    //Repair routine will restart after an hour
    private final long repairIntervalInMinutes = 60;
    //Keep track of last time since repair so we know when to restart
    private long lastTimeSinceRepair = -1;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkLivingRockRootBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.SCULK_LIVING_ROCK_ROOT_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkLivingRockRootBlockEntity blockEntity)
    {
        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);
        if(timeElapsed < 0.1) { return;}

        blockEntity.tickedAt = System.nanoTime();

        /** Building Shell Process **/
        long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - blockEntity.lastTimeSinceRepair, TimeUnit.NANOSECONDS);

        //If the Bee Nest Structure hasnt been initialized yet, do it
        if(blockEntity.proceduralStructure == null)
        {
            //Create Structure
            blockEntity.proceduralStructure = new SculkLivingRockProceduralStructure((ServerLevel) level, blockPos);
            blockEntity.proceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(blockEntity.proceduralStructure.isCurrentlyBuilding())
        {
            blockEntity.proceduralStructure.buildTick();
            blockEntity.lastTimeSinceRepair = System.nanoTime();
        }
        //If enough time has passed, or we havent built yet, start build
        else if(repairTimeElapsed >= blockEntity.repairIntervalInMinutes || blockEntity.lastTimeSinceRepair == -1)
        {
            blockEntity.proceduralStructure.startBuildProcedure();
        }
    }
}
