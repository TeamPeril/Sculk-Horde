package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.core.TileEntityRegistry;
import com.github.sculkhoard.util.ChunkLoaderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.ChunkPos;

import javax.annotation.Nullable;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkBrainTile extends TileEntity implements ITickableTileEntity {

    private int gridSize = 10;
    private int radius = (gridSize - 1) / 2;
    private boolean [][] grid; //[X][Z]
    private boolean dataDirty = false;


    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkBrainTile(TileEntityType<?> type) {
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

    public void unloadAllChunks(){
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

    public void loadAllChunks(){
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

    public void toggleChunks(int xOffset, int zOffset){
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

    public boolean isLoaded(int xOffset, int zOffset){
        return this.grid[xOffset + radius][zOffset + radius];
    }

    public int getGridSize(){
        return this.gridSize;
    }

    public void dataDirty(){
        if(this.level.isClientSide)
            return;
        this.dataDirty = true;
        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 2);
    }

    private CompoundNBT getDirtyData(){
        if(this.dataDirty){
            this.dataDirty = false;
            return this.getData();
        }
        return null;
    }

    private CompoundNBT getData(){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("gridSize", this.gridSize);
        for(int x = 0; x < this.gridSize; x++){
            for(int z = 0; z < this.gridSize; z++){
                tag.putBoolean(x + ";" + z, this.grid[x][z]);
            }
        }
        return tag;
    }

    private void handleData(CompoundNBT tag){
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
    public CompoundNBT save(CompoundNBT compound){
        super.save(compound);
        compound.put("data", this.getData());
        return compound;
    }

    @Override
    public void load(BlockState state, CompoundNBT compound){
        super.load(state, compound);
        this.handleData(compound.getCompound("data"));
    }

    @Override
    public CompoundNBT getUpdateTag(){
        CompoundNBT tag = super.getUpdateTag();
        tag.put("data", this.getData());
        return tag;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag){
        super.handleUpdateTag(state, tag);
        this.handleData(tag.getCompound("data"));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket(){
        CompoundNBT tag = this.getDirtyData();
        return tag == null || tag.isEmpty() ? null : new SUpdateTileEntityPacket(this.worldPosition, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        this.handleData(pkt.getTag());
    }

    @Override
    public void tick() {

    }
}
