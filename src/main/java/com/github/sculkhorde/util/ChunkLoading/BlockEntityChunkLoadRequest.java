package com.github.sculkhorde.util.ChunkLoading;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class BlockEntityChunkLoadRequest extends ChunkLoadRequest {

    protected BlockPos owner;

    public BlockEntityChunkLoadRequest(ResourceKey<Level> dimension, BlockPos owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration) {
        super(dimension, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        this.owner = owner;
        this.dimension = dimension;
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
        compound.putString("dimension", dimension.location().toString());
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
        ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(compound.getString("dimension")));
        for(int i = 0; i < chunkPositionsToLoadLength; i++)
        {
            chunkPositionsToLoad[i] = new ChunkPos(compound.getLong("chunkPositionsToLoad" + i));
        }
        return new BlockEntityChunkLoadRequest(dimensionResourceKey, owner, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
    }

    public void setOwner(BlockPos owner) {
        this.owner = owner;
    }

    // Any other unique methods for BlockChunkLoadRequest...
}