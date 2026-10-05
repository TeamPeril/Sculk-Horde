package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class EntityChunkLoadRequest extends ChunkLoadRequest {

    protected UUID owner;
    protected String ownerNickname = "";


    public EntityChunkLoadRequest(ResourceKey<Level> dimension, UUID owner, String ownerNickname, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        super(dimension, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        this.owner = owner;
        this.dimension = dimension;
        this.ownerNickname = ownerNickname;
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
        compound.putString("ownerNickname", ownerNickname);
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
        ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(compound.getString("dimension")));
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }

        String ownerNickName = "";
        if(compound.contains("ownerNickname"))
        {
            ownerNickName = compound.getString("ownerNickname");
        }

        return new EntityChunkLoadRequest(dimensionResourceKey, owner, ownerNickName, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
    }

    public void setOwner(UUID owner, String ownerNickname) {
        this.owner = owner;
        this.ownerNickname = ownerNickname;
    }
}