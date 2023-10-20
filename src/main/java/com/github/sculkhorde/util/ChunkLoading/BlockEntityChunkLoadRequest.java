package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class BlockEntityChunkLoadRequest extends ChunkLoadRequest {

    private BlockPos owner;

    public BlockEntityChunkLoadRequest(BlockPos owner, ChunkPos[] chunkPositionsToLoad, int priority) {
        super(chunkPositionsToLoad, priority);
        this.owner = owner;
    }

    @Override
    public Object getOwner() {
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
        ChunkPos[] chunkPositionsToLoad = new ChunkPos[chunkPositionsToLoadLength];
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }
        return new BlockEntityChunkLoadRequest(owner, chunkPositionsToLoad, priority);
    }

    public void setOwner(BlockPos owner) {
        this.owner = owner;
    }

    // Any other unique methods for BlockChunkLoadRequest...
}