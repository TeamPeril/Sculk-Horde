package com.github.sculkhorde.util.ChunkLoading;

import java.util.UUID;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class EntityChunkLoadRequest extends ChunkLoadRequest {

    protected UUID owner;


    public EntityChunkLoadRequest(ResourceKey<Level> dimension, UUID owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        super(dimension, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        this.owner = owner;
        this.dimension = dimension;
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
        compound.putString("dimension", dimension.location().toString());
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
        ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(compound.getString("dimension")));
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }
        return new EntityChunkLoadRequest(dimensionResourceKey, owner, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }
}