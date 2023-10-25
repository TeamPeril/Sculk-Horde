package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public class EntityChunkLoadRequest extends ChunkLoadRequest {

    private UUID owner;

    public EntityChunkLoadRequest(UUID owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        super(chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        this.owner = owner;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public boolean isOwner(Object ownerObj) {
        if(!(ownerObj instanceof UUID)) return false;
        return owner.equals((UUID) ownerObj);
    }

    public CompoundTag deserialize()
    {
        CompoundTag compound = new CompoundTag();
        compound.putInt("priority", priority);
        compound.putUUID("owner", owner);
        compound.putInt("chunkPositionsToLoadLength", chunkPositionsToLoad.length);
        compound.putString("requestID", requestID);
        compound.putLong("ticksUntilExpiration", ticksUntilExpiration);
        for(int i = 0; i < chunkPositionsToLoad.length; i++)
        {
            compound.putLong("chunkPositionsToLoad" + i, chunkPositionsToLoad[i].toLong());
        }
        return compound;
    }


    public static EntityChunkLoadRequest serialize(CompoundTag compound)
    {
        int priority = compound.getInt("priority");
        UUID owner = compound.getUUID("owner");
        int chunkPositionsToLoadLength = compound.getInt("chunkPositionsToLoadLength");
        String requestID = compound.getString("requestID");
        long ticksUntilExpiration = compound.getLong("ticksUntilExpiration");
        ChunkPos[] chunkPositionsToLoad = new ChunkPos[chunkPositionsToLoadLength];
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }
        return new EntityChunkLoadRequest(owner, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}