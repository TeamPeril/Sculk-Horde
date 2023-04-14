package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.infection.SculkNodeInfectionHandler;
import com.github.sculkhorde.common.procedural.structures.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.BlockEntityRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkNodeBlockEntity extends BlockEntity
{

    private final int CHUNK_LOAD_RADIUS = 15;

    private long tickedAt = System.nanoTime();

    private SculkNodeProceduralStructure nodeProceduralStructure;

    //Repair routine will restart after an hour
    private final long repairIntervalInMinutes = 60;
    //Keep track of last time since repair so we know when to restart
    private long lastTimeSinceRepair = -1;

    public static final int tickIntervalSeconds = 1;

    private SculkNodeInfectionHandler infectionHandler;

    public SculkNodeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityRegistry.SCULK_NODE_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    private long heartBeatDelayMillis = TimeUnit.SECONDS.toMillis(10);
    private long lastHeartBeat = System.currentTimeMillis();


    /** Accessors **/


    /** Modifiers **/


    /** Events **/
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkNodeBlockEntity blockEntity)
    {
        if(level.isClientSide)
        {
            if(System.currentTimeMillis() - blockEntity.lastHeartBeat > blockEntity.heartBeatDelayMillis)
            {
                blockEntity.lastHeartBeat = System.currentTimeMillis();
                level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 5.0F, 1.0F, false);
            }
            return;
        }
            long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

            // If the time elapsed is less than the tick interval, return
            if(timeElapsed < tickIntervalSeconds) { return; }

            // Update the tickedAt time
            blockEntity.tickedAt = System.nanoTime();

            /** Building Shell Process **/
            long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - blockEntity.lastTimeSinceRepair, TimeUnit.NANOSECONDS);

            //If the structure has not been initialized yet, do it
            if(blockEntity.nodeProceduralStructure == null)
            {
                //Create Structure
                blockEntity.nodeProceduralStructure = new SculkNodeProceduralStructure((ServerLevel) level, blockPos);
                blockEntity.nodeProceduralStructure.generatePlan();
            }

            //If currently building, call build tick.
            if(blockEntity.nodeProceduralStructure.isCurrentlyBuilding())
            {
                blockEntity.nodeProceduralStructure.buildTick();
                blockEntity.lastTimeSinceRepair = System.nanoTime();
            }
            //If enough time has passed, or we havent built yet, and we can build, start build
            else if((repairTimeElapsed >= blockEntity.repairIntervalInMinutes || blockEntity.lastTimeSinceRepair == -1) && blockEntity.nodeProceduralStructure.canStartToBuild())
            {
                blockEntity.nodeProceduralStructure.startBuildProcedure();
            }


            /** Infection Routine **/
            if(blockEntity.infectionHandler == null)
            {
                blockEntity.infectionHandler = new SculkNodeInfectionHandler(blockEntity);
            }
            else
            {
                blockEntity.infectionHandler.tick();
            }
    }


    public static void forceLoadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
    }

    public void forceLoadChunksInRadius(ServerLevel world, BlockPos owner, int chunkOriginX, int chunkOriginZ)
    {
        /*
        If radius is 3, this is what the area of chunk loading will look like.
            ooooooo
            ooo*ooo
            ooooooo
        This means that the length of any side is (CHUNK_LOAD_RADIUS * 2) + 1.
         */

        int startChunkX = chunkOriginX - CHUNK_LOAD_RADIUS;
        int startChunkZ = chunkOriginZ - CHUNK_LOAD_RADIUS;

        for(int xOffset = 0; xOffset < (CHUNK_LOAD_RADIUS * 2) + 1; xOffset++)
        {
            for(int zOffset = 0; zOffset < (CHUNK_LOAD_RADIUS * 2) + 1; zOffset++)
            {
                forceLoadChunk(world, owner, startChunkX + xOffset, startChunkZ + zOffset, true);
            }
        }
    }

    public static void unloadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
    }

    public void unloadChunksInRadius(ServerLevel world, BlockPos owner, int chunkOriginX, int chunkOriginZ)
    {
        /*
        If radius is 3, this is what the area of chunk loading will look like.
            ooooooo
            ooo*ooo
            ooooooo
        This means that the length of any side is (CHUNK_LOAD_RADIUS * 2) + 1.
         */

        int startChunkX = chunkOriginX - CHUNK_LOAD_RADIUS;
        int startChunkZ = chunkOriginZ - CHUNK_LOAD_RADIUS;

        for(int xOffset = 0; xOffset < (CHUNK_LOAD_RADIUS * 2) + 1; xOffset++)
        {
            for(int zOffset = 0; zOffset < (CHUNK_LOAD_RADIUS * 2) + 1; zOffset++)
            {
                unloadChunk(world, owner, startChunkX + xOffset, startChunkZ + zOffset, true);
            }
        }
    }
}
