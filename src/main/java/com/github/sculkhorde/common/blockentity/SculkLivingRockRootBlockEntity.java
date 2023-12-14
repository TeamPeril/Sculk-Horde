package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.structures.procedural.SculkLivingRockProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkLivingRockRootBlockEntity extends BlockEntity
{
    private long tickedAt = 0;
    private SculkLivingRockProceduralStructure proceduralStructure;
    private final long repairIntervalTicks = TickUnits.convertMinutesToTicks(15);
    private long ticksSinceLastRepair = -1;

    public SculkLivingRockRootBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.SCULK_LIVING_ROCK_ROOT_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkLivingRockRootBlockEntity blockEntity)
    {
        long ticksElapsedSinceLastTick = level.getGameTime() - blockEntity.tickedAt;
        if(ticksElapsedSinceLastTick < TickUnits.convertSecondsToTicks(1)) { return;}

        blockEntity.tickedAt = level.getGameTime();

        /** Building Shell Process **/
        long ticksElapsedSinceLastRepair = level.getGameTime() - blockEntity.ticksSinceLastRepair;

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
            blockEntity.ticksSinceLastRepair = level.getGameTime();
        }
        //If enough time has passed, or we havent built yet, start build
        else if(ticksElapsedSinceLastRepair >= blockEntity.repairIntervalTicks || blockEntity.ticksSinceLastRepair == -1)
        {
            blockEntity.proceduralStructure.startBuildProcedure();
        }
    }
}
