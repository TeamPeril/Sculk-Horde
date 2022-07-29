package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.common.block.SculkBrainBlock;
import com.github.sculkhoard.common.procedural.structures.SculkNodeShellProceduralStructure;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import com.github.sculkhoard.util.ChunkLoaderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkBrainTile extends TileEntity implements ITickableTileEntity
{
    private int gridSize = 10;
    private int radius = (gridSize - 1) / 2;
    private boolean [][] grid; //[X][Z]
    private boolean dataDirty = false;

    private long tickedAt = System.nanoTime();

    private SculkNodeShellProceduralStructure shellProceduralStructure;

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
    public SculkBrainTile(TileEntityType<?> type)
    {
        super(type);
        this.grid = new boolean[gridSize][gridSize];
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public SculkBrainTile() {

        this(TileEntityRegistry.SCULK_BRAIN_TILE.get());
    }

    /** Accessors **/

    public int getGridSize() {return this.gridSize;}

    private CompoundNBT getDirtyData()
    {
        if(this.dataDirty){
            this.dataDirty = false;
            return this.getData();
        }
        return null;
    }

    private CompoundNBT getData()
    {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("gridSize", this.gridSize);
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                tag.putBoolean(x + ";" + z, this.grid[x][z]);
            }
        }
        return tag;
    }

    @Override
    public CompoundNBT getUpdateTag()
    {
        CompoundNBT tag = super.getUpdateTag();
        tag.put("data", this.getData());
        return tag;
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket()
    {
        CompoundNBT tag = this.getDirtyData();
        return tag == null || tag.isEmpty() ? null : new SUpdateTileEntityPacket(this.worldPosition, 0, tag);
    }

    /** Modifiers **/


    /** Events **/

    @Override
    public void tick()
    {
        if(this.level != null && !this.level.isClientSide)
        {
            long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - tickedAt, TimeUnit.NANOSECONDS);
            if(timeElapsed >= SculkBrainBlock.tickIntervalSeconds)
            {
                tickedAt = System.nanoTime();
                ServerWorld thisWorld = (ServerWorld) this.level;
                BlockPos thisPos = this.worldPosition;

                /** Building Shell Process **/
                long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - lastTimeSinceRepair, TimeUnit.NANOSECONDS);

                //If the Bee Nest Structure hasnt been initialized yet, do it
                if(shellProceduralStructure == null)
                {
                    //Create Structure
                    shellProceduralStructure = new SculkNodeShellProceduralStructure((ServerWorld) this.level, this.getBlockPos());
                }

                //If currently building, call build tick.
                if(shellProceduralStructure.isCurrentlyBuilding())
                {
                    shellProceduralStructure.buildTick();
                    lastTimeSinceRepair = System.nanoTime();
                }
                //If enough time has passed, or we havent built yet, start build
                else if(repairTimeElapsed >= repairIntervalInMinutes || lastTimeSinceRepair == -1)
                {
                    shellProceduralStructure.startBuildProcedure();
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

    public void unloadAllChunks()
    {
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    if(this.grid[x][z])
                        tracker.remove(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.worldPosition);
                }
            }
        });
    }

    public void loadAllChunks()
    {
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            for(int x = 0; x < this.gridSize; x++){
                for(int z = 0; z < this.gridSize; z++){
                    this.grid[x][z] = true;
                    tracker.add(new ChunkPos(pos.x + x - radius, pos.z + z - radius), this.worldPosition);
                }
            }
        });
        this.dataDirty();
    }

    public void toggleChunks(int xOffset, int zOffset)
    {
        this.level.getCapability(ChunkLoaderUtil.TRACKER_CAPABILITY).ifPresent(tracker -> {
            ChunkPos pos = this.level.getChunk(this.worldPosition).getPos();
            if(this.grid[xOffset + radius][zOffset + radius])
                tracker.remove(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.worldPosition);
            else
                tracker.add(new ChunkPos(pos.x + xOffset, pos.z + zOffset), this.worldPosition);
            this.grid[xOffset + radius][zOffset + radius] = !this.grid[xOffset + radius][zOffset + radius];
        });
        this.dataDirty();
    }

    public boolean isLoaded(int xOffset, int zOffset)
    {
        return this.grid[xOffset + radius][zOffset + radius];
    }

    public void dataDirty()
    {
        if(this.level.isClientSide){return;}

        this.dataDirty = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
    }

    private void handleData(CompoundNBT tag)
    {
        this.gridSize = tag.contains("gridSize") ? tag.getInt("gridSize") : this.gridSize;
        if(this.gridSize < 1 || this.gridSize % 2 == 0)
            this.gridSize = 1;
        this.radius = (this.gridSize - 1) / 2;
        this.grid = new boolean[this.gridSize][this.gridSize];
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                this.grid[x][z] = tag.contains(x + ";" + z) && tag.getBoolean(x + ";" + z);
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT compound)
    {
        super.save(compound);
        compound.put("data", this.getData());
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound)
    {
        super.load(state, compound);
        this.handleData(compound.getCompound("data"));
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag)
    {
        super.handleUpdateTag(state, tag);
        this.handleData(tag.getCompound("data"));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleData(pkt.getTag());
    }
}
