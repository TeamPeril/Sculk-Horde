package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.common.structures.procedural.ProceduralStructure;
import com.github.sculkhorde.common.structures.procedural.SculkNodeCaveHallwayProceduralStructure;
import com.github.sculkhorde.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterBlockEntity extends BlockEntity
{

    private long tickedAt = System.nanoTime();

    private ProceduralStructure proceduralStructure;

    private int spawnedCursors = 0;
    private int maxSPawned = 1000;


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
        if(blockEntity.spawnedCursors < blockEntity.maxSPawned)
        {
            CursorSurfaceInfectorEntity entity = new CursorSurfaceInfectorEntity(level);
            entity.setPos(blockPos.getCenter());
            entity.setMaxTransformations(100);
            entity.setMaxRange(100);
            level.addFreshEntity(entity);
        }
    }
}
