package com.github.sculkhorde.common.blockentity;

import java.util.concurrent.TimeUnit;

import com.github.sculkhorde.common.structures.procedural.ProceduralStructure;
import com.github.sculkhorde.common.structures.procedural.SculkNodeCaveHallwayProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterBlockEntity extends BlockEntity
{

    private long tickedAt = System.nanoTime();

    private ProceduralStructure proceduralStructure;


    /**
     * The Constructor that takes in properties
     * @param blockPos The Position
     * @param blockState The Properties
     */
    public DevStructureTesterBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(ModBlockEntities.DEV_STRUCTURE_TESTER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, DevStructureTesterBlockEntity blockEntity)
    {

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);
        if(timeElapsed < 1) { return;}

        blockEntity.tickedAt = System.nanoTime();

        /** Building Process **/

        //If the Structure hasnt been initialized yet, do it
        if(blockEntity.proceduralStructure == null)
        {
            //Create Structure
            blockEntity.proceduralStructure = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) level, blockPos, 5, 10, Direction.NORTH);
            blockEntity.proceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(!blockEntity.proceduralStructure.isStructureComplete() && blockEntity.proceduralStructure.isCurrentlyBuilding())
        {
            blockEntity.proceduralStructure.buildTick();
        }

        //If structure not complete, start build
        if(!blockEntity.proceduralStructure.isStructureComplete() && !blockEntity.proceduralStructure.isCurrentlyBuilding())
        {
            blockEntity.proceduralStructure.startBuildProcedure();
        }
    }
}
