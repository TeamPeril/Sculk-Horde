package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public abstract class ChunkLoadRequest {

    ChunkPos[] chunkPositionsToLoad;
    int priority; // Lower number means higher priority

    public ChunkLoadRequest(ChunkPos[] chunkPositionsToLoad, int priority) {
        this.chunkPositionsToLoad = chunkPositionsToLoad;
        this.priority = priority;
    }

    public ChunkPos[] getChunkPositionsToLoad() {
        return chunkPositionsToLoad;
    }

    public int getPriority() {
        return priority;
    }

    public abstract Object getOwner();

    public abstract boolean isOwner(Object owner);

    public boolean isHigherPriorityThan(ChunkLoadRequest other)
    {
        return priority < other.priority;
    }

    public boolean doesContainChunk(ChunkPos chunkPos)
    {
        for(ChunkPos pos : chunkPositionsToLoad)
        {
            if(pos.equals(chunkPos))
            {
                return true;
            }
        }
        return false;
    }

    public void removeChunk(ChunkPos chunkPos)
    {
        if(!doesContainChunk(chunkPos))
        {
            return;
        }

        ChunkPos[] newChunkPositionsToLoad = new ChunkPos[chunkPositionsToLoad.length - 1];
        int index = 0;
        for(ChunkPos pos : chunkPositionsToLoad)
        {
            if(!pos.equals(chunkPos))
            {
                newChunkPositionsToLoad[index] = pos;
                index++;
            }
        }
        chunkPositionsToLoad = newChunkPositionsToLoad;
    }

    public void addChunk(ChunkPos chunkPos)
    {
        if(doesContainChunk(chunkPos))
        {
            return;
        }

        ChunkPos[] newChunkPositionsToLoad = new ChunkPos[chunkPositionsToLoad.length + 1];
        int index = 0;
        for(ChunkPos pos : chunkPositionsToLoad)
        {
            newChunkPositionsToLoad[index] = pos;
            index++;
        }
        newChunkPositionsToLoad[index] = chunkPos;
        chunkPositionsToLoad = newChunkPositionsToLoad;
    }

    public void setChunkPositionsToLoad(ChunkPos[] chunkPositionsToLoad) {
        this.chunkPositionsToLoad = chunkPositionsToLoad;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
