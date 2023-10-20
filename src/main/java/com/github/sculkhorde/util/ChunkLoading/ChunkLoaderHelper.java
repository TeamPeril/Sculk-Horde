package com.github.sculkhorde.util.ChunkLoading;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.UUID;

public class ChunkLoaderHelper
{
    ArrayList<BlockEntityChunkLoadRequest> blockChunkLoadRequests = new ArrayList<>();
    ArrayList<EntityChunkLoadRequest> entityChunkLoadRequests = new ArrayList<>();

    public ChunkLoaderHelper()
    {

    }

    public static void load(CompoundTag tag)
    {
        getChunkLoaderHelper().blockChunkLoadRequests.clear();
        getChunkLoaderHelper().entityChunkLoadRequests.clear();
        ListTag blockChunkLoadRequestsTag = tag.getList("blockChunkLoadRequests", 10);
        for(int i = 0; i < blockChunkLoadRequestsTag.size(); i++)
        {
            CompoundTag requestTag = blockChunkLoadRequestsTag.getCompound(i);
            BlockEntityChunkLoadRequest request = BlockEntityChunkLoadRequest.serialize(requestTag);
            getChunkLoaderHelper().blockChunkLoadRequests.add(request);
        }

        ListTag entityChunkLoadRequestsTag = tag.getList("entityChunkLoadRequests", 10);
        for(int i = 0; i < entityChunkLoadRequestsTag.size(); i++)
        {
            CompoundTag requestTag = entityChunkLoadRequestsTag.getCompound(i);
            EntityChunkLoadRequest request = EntityChunkLoadRequest.serialize(requestTag);
            getChunkLoaderHelper().entityChunkLoadRequests.add(request);
        }
    }

    public static CompoundTag save(CompoundTag tag)
    {
        ListTag blockChunkLoadRequestsTag = new ListTag();
        for(BlockEntityChunkLoadRequest request : getChunkLoaderHelper().blockChunkLoadRequests)
        {
            blockChunkLoadRequestsTag.add(request.deserialize());
        }
        tag.put("blockChunkLoadRequests", blockChunkLoadRequestsTag);

        ListTag entityChunkLoadRequestsTag = new ListTag();
        for(EntityChunkLoadRequest request : getChunkLoaderHelper().entityChunkLoadRequests)
        {
            entityChunkLoadRequestsTag.add(request.deserialize());
        }
        tag.put("entityChunkLoadRequests", entityChunkLoadRequestsTag);

        return tag;
    }

    public static ChunkLoaderHelper getChunkLoaderHelper()
    {
        return SculkHorde.chunkLoaderHelper;
    }


    public void processBlockChunkLoadRequests(ServerLevel world)
    {
        for(BlockEntityChunkLoadRequest request : blockChunkLoadRequests)
        {
            for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
            {
                // If not loaded
                if(!world.getChunkSource().hasChunk(chunkPos.x, chunkPos.z))
                {
                    forceLoadChunk(world, (BlockPos) request.getOwner(), chunkPos.x, chunkPos.z);
                }
            }
        }
    }

    public void processEntityChunkLoadRequests(ServerLevel world)
    {
        for(EntityChunkLoadRequest request : entityChunkLoadRequests)
        {
            for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
            {
                // If chunk not loaded
                if(!world.getChunkSource().hasChunk(chunkPos.x, chunkPos.z))
                {
                    forceLoadChunk(world, request.getOwner(), chunkPos.x, chunkPos.z);
                }
            }
        }
    }

    private static void forceLoadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
    }

    private static void forceLoadChunk(ServerLevel world, UUID owner, int chunkX, int chunkZ) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
    }
    public static void unloadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
    }

    public static void unloadChunk(ServerLevel world, UUID owner, int chunkX, int chunkZ) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
    }

    public void sortBlockChunkLoadRequests()
    {
        ArrayList<BlockEntityChunkLoadRequest> sortedBlockChunkLoadRequests = new ArrayList<>();
        for(BlockEntityChunkLoadRequest request : blockChunkLoadRequests)
        {
            if(sortedBlockChunkLoadRequests.isEmpty())
            {
                sortedBlockChunkLoadRequests.add(request);
                continue;
            }

            boolean isAdded = false;
            for(int i = 0; i < sortedBlockChunkLoadRequests.size(); i++)
            {
                if(request.isHigherPriorityThan(sortedBlockChunkLoadRequests.get(i)))
                {
                    sortedBlockChunkLoadRequests.add(i, request);
                    isAdded = true;
                    break;
                }
            }
            if(!isAdded)
            {
                sortedBlockChunkLoadRequests.add(request);
            }
        }
        blockChunkLoadRequests = sortedBlockChunkLoadRequests;
    }

    public void sortEntityChunkLoadRequests()
    {
        ArrayList<EntityChunkLoadRequest> sortedEntityChunkLoadRequests = new ArrayList<>();
        for(EntityChunkLoadRequest request : entityChunkLoadRequests)
        {
            if(sortedEntityChunkLoadRequests.isEmpty())
            {
                sortedEntityChunkLoadRequests.add(request);
                continue;
            }

            boolean isAdded = false;
            for(int i = 0; i < sortedEntityChunkLoadRequests.size(); i++)
            {
                if(request.isHigherPriorityThan(sortedEntityChunkLoadRequests.get(i)))
                {
                    sortedEntityChunkLoadRequests.add(i, request);
                    isAdded = true;
                    break;
                }
            }
            if(!isAdded)
            {
                sortedEntityChunkLoadRequests.add(request);
            }
        }
        entityChunkLoadRequests = sortedEntityChunkLoadRequests;
    }

    public void unloadChunksWithOwner(BlockPos owner, ServerLevel level)
    {
        for(BlockEntityChunkLoadRequest request : blockChunkLoadRequests)
        {
            if(request.isOwner(owner))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    unloadChunk(level, owner, chunkPos.x, chunkPos.z);
                }
            }
            removeRequestsWithOwner(owner, level);
        }
    }

    public void unloadChunksWithOwner(Entity owner, ServerLevel level)
    {
        for(EntityChunkLoadRequest request : entityChunkLoadRequests)
        {
            if(request.isOwner(owner.getUUID()))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    unloadChunk(level, owner.getUUID(), chunkPos.x, chunkPos.z);
                }

                removeRequestsWithOwner(owner);
            }
        }
    }

    public void removeRequestsWithOwner(BlockPos owner, ServerLevel level)
    {
        ArrayList<BlockEntityChunkLoadRequest> requestsToRemove = new ArrayList<>();
        for(BlockEntityChunkLoadRequest request : blockChunkLoadRequests)
        {
            if(request.isOwner(owner))
            {
                requestsToRemove.add(request);
            }
        }
        blockChunkLoadRequests.removeAll(requestsToRemove);
    }

    public void removeRequestsWithOwner(Entity owner)
    {
        ArrayList<ChunkLoadRequest> requestsToRemove = new ArrayList<>();
        for(ChunkLoadRequest request : entityChunkLoadRequests)
        {
            if(request.isOwner(owner))
            {
                requestsToRemove.add(request);
            }
        }
        entityChunkLoadRequests.removeAll(requestsToRemove);
    }

    public void createChunkLoadRequestSquareForEntityOrBlockPos(Object owner, int length, int priority)
    {
        int chunkCount = length * length;
        ChunkPos[] chunkPositionsToLoad = new ChunkPos[chunkCount];
        int index = 0;

        if(owner instanceof BlockPos blockPos)
        {
            for(int x = blockPos.getX() - length; x <= blockPos.getX() + length && index < chunkCount; x++)
            {
                for(int z = blockPos.getZ() - length; z <= blockPos.getZ() + length && index < chunkCount; z++)
                {
                    chunkPositionsToLoad[index] = new ChunkPos(x, z);
                    index++;
                }
            }
        }
        else if(owner instanceof Entity entity)
        {
            BlockPos entityPos = entity.blockPosition();
            for(int x = entityPos.getX() - length; x <= entityPos.getX() + length && index < chunkCount; x++)
            {
                for(int z = entityPos.getZ() - length; z <= entityPos.getZ() + length && index < chunkCount; z++)
                {
                    chunkPositionsToLoad[index] = new ChunkPos(x, z);
                    index++;
                }
            }
        }
        else
        {
            throw new IllegalArgumentException("Owner must be either a BlockPos or Entity");
        }

        createChunkLoadRequest(owner, chunkPositionsToLoad, priority);
    }

    public void createChunkLoadRequest(Object owner, ChunkPos[] chunkPositionsToLoad, int priority)
    {
        if(owner instanceof BlockPos blockPos)
        {
            BlockEntityChunkLoadRequest request = new BlockEntityChunkLoadRequest(blockPos, chunkPositionsToLoad, priority);
            blockChunkLoadRequests.add(request);
        }
        else if(owner instanceof UUID entityUUID)
        {
            EntityChunkLoadRequest request = new EntityChunkLoadRequest(entityUUID, chunkPositionsToLoad, priority);
            entityChunkLoadRequests.add(request);
        }
        else
        {
            throw new IllegalArgumentException("Owner must be either a BlockPos or Entity");
        }
    }
}
