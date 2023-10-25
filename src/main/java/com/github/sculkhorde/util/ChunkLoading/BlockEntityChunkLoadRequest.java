package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public class BlockEntityChunkLoadRequest extends ChunkLoadRequest {

    private BlockPos owner;

    public BlockEntityChunkLoadRequest(BlockPos owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        super(chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        this.owner = owner;
    }

    @Override
    public BlockPos getOwner() {
        return owner;
    }

    @Override
    public boolean isOwner(Object ownerObj) {
        if(!(ownerObj instanceof BlockPos)) return false;
        return owner.equals((BlockPos) ownerObj);
    }

    public CompoundTag deserialize()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt("priority", priority);
        compound.putLong("owner", owner.asLong());
        compound.putInt("chunkPositionsToLoadLength", chunkPositionsToLoad.length);
        compound.putString("requestID", requestID);
        compound.putLong("ticksUntilExpiration", ticksUntilExpiration);
        for(int i = 0; i < chunkPositionsToLoad.length; i++)
        {
            compound.putLong("chunkPositionsToLoad" + i, chunkPositionsToLoad[i].toLong());
        }
        return compound;
    }


    public static BlockEntityChunkLoadRequest serialize(CompoundTag compound)
    {
        int priority = compound.getInt("priority");
        BlockPos owner = BlockPos.of(compound.getLong("owner"));
        int chunkPositionsToLoadLength = compound.getInt("chunkPositionsToLoadLength");
        String requestID = compound.getString("requestID");
        long ticksUntilExpiration = compound.getLong("ticksUntilExpiration");
        ChunkPos[] chunkPositionsToLoad = new ChunkPos[chunkPositionsToLoadLength];
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }
        return new BlockEntityChunkLoadRequest(owner, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
    }

    public void setOwner(BlockPos owner) {
        this.owner = owner;
    }

    // Any other unique methods for BlockChunkLoadRequest...
}