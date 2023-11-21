package com.github.sculkhorde.util.ChunkLoading;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Objects;


public abstract class ChunkLoadRequest {

    ChunkPos[] chunkPositionsToLoad;
    int priority; // Lower number means higher priority
    String requestID;
    ResourceKey<Level> dimension;

    long ticksUntilExpiration = 0;

    public ChunkLoadRequest(ResourceKey<Level> dimension, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        this.chunkPositionsToLoad = chunkPositionsToLoad;
        this.priority = priority;
        this.requestID = requestID;
        this.ticksUntilExpiration = ticksUntilExpiration;
    }

    public ChunkPos[] getChunkPositionsToLoad() {
        return chunkPositionsToLoad;
    }

    public int getPriority() {
        return priority;
    }

    public ServerLevel getDimension()
    {
        return SculkHorde.savedData.level.getServer().getLevel(dimension);
    }

    public String getRequestID() {
        return requestID;
    }

    public boolean isRequestID(String requestID) {
        return Objects.equals(this.requestID, requestID);
    }

    public abstract Object getOwner();

    public abstract boolean isOwner(Object owner);

    public boolean isExpired()
    {
        return ticksUntilExpiration <= 0;
    }

    public void decrementTicksUntilExpiration(int amountToDecrement)
    {
        ticksUntilExpiration -= amountToDecrement;
    }

    public long getTicksUntilExpiration()
    {
        return ticksUntilExpiration;
    }

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
