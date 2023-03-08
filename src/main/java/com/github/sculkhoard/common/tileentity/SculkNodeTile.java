package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.common.procedural.structures.SculkNodeProceduralStructure;
import com.github.sculkhoard.util.BlockAlgorithms;
import com.github.sculkhoard.common.block.SculkNodeBlock;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkNodeTile extends TileEntity implements ITickableTileEntity
{

    private final int CHUNK_LOAD_RADIUS = 15;

    private long tickedAt = System.nanoTime();

    private SculkNodeProceduralStructure nodeProceduralStructure;

    //The current circle radius, this increments at an interval
    private int infectCircleRadius = 1;
    //Once we are done spreading, how long should we wait before trying again?
    private final long spreadRoutineIntervalInMinutes = 60;
    //The time which this node has finished the spread routine
    private long finishedInfectionRoutineAt = 0;
    //Whether we are currently doing the spread routine
    private boolean currentlySpreading = true;

    //Repair routine will restart after an hour
    private final long repairIntervalInMinutes = 60;
    //Keep track of last time since repair so we know when to restart
    private long lastTimeSinceRepair = -1;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkNodeTile(TileEntityType<?> type)
    {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public SculkNodeTile() {

        this(TileEntityRegistry.SCULK_BRAIN_TILE.get());
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    @Override
    public void tick()
    {
        if(this.level != null && !this.level.isClientSide)
        {
            long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - tickedAt, TimeUnit.NANOSECONDS);
            if(timeElapsed >= SculkNodeBlock.tickIntervalSeconds)
            {
                tickedAt = System.nanoTime();
                ServerWorld thisWorld = (ServerWorld) this.level;
                BlockPos thisPos = this.worldPosition;

                /** Building Shell Process **/
                long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - lastTimeSinceRepair, TimeUnit.NANOSECONDS);

                //If the Bee Nest Structure hasnt been initialized yet, do it
                if(nodeProceduralStructure == null)
                {
                    //Create Structure
                    nodeProceduralStructure = new SculkNodeProceduralStructure((ServerWorld) this.level, this.getBlockPos());
                    nodeProceduralStructure.generatePlan();
                }

                //If currently building, call build tick.
                if(nodeProceduralStructure.isCurrentlyBuilding())
                {
                    nodeProceduralStructure.buildTick();
                    lastTimeSinceRepair = System.nanoTime();
                }
                //If enough time has passed, or we havent built yet, start build
                else if(repairTimeElapsed >= repairIntervalInMinutes || lastTimeSinceRepair == -1)
                {
                    nodeProceduralStructure.startBuildProcedure();
                }


                /** Infection Routine **/

                infectionTick(thisWorld, thisPos);
            }
        }
    }


    /**
     * Will infect blocks in an increasing sized sphere. Once reaches max radius,
     * will stop and pause for a specified amount of time, then restart.
     * @param serverWorld The world
     * @param bp The block position
     */
    public void infectionTick(ServerWorld serverWorld, BlockPos bp)
    {
        long timeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - finishedInfectionRoutineAt, TimeUnit.NANOSECONDS);
        if(!currentlySpreading && timeElapsed >= spreadRoutineIntervalInMinutes)
        {
            currentlySpreading = true;
        }

        if(currentlySpreading)
        {
            //TODO Make a new function that does a sphere-ring hybrid instead of a full circle
            SculkHoard.infestationConversionTable.convertToInfectedNodeQueue.addAll(
                    BlockAlgorithms.getBlockPosInCircle(bp, infectCircleRadius, false)
            );
            infectCircleRadius++;
            if(infectCircleRadius > SculkHoard.gravemind.sculk_node_infect_radius)
            {
                infectCircleRadius = 1;
                finishedInfectionRoutineAt = System.nanoTime();
                currentlySpreading = false;
            }
        }
    }

    public static void forceLoadChunk(ServerWorld world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHoard.MOD_ID, owner, chunkX, chunkZ, true, true);
    }

    public void forceLoadChunksInRadius(ServerWorld world, BlockPos owner, int chunkOriginX, int chunkOriginZ)
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

    public static void unloadChunk(ServerWorld world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHoard.MOD_ID, owner, chunkX, chunkZ, false, false);
    }

    public void unloadChunksInRadius(ServerWorld world, BlockPos owner, int chunkOriginX, int chunkOriginZ)
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
