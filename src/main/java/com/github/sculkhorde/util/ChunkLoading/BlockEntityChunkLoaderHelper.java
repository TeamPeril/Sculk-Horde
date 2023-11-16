package com.github.sculkhorde.util.ChunkLoading;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;

public class BlockEntityChunkLoaderHelper
{
    private ArrayList<BlockEntityChunkLoadRequest> blockChunkLoadRequests = new ArrayList<>();
    private int tickCooldownRemaining = 0;
    private final int TICKS_BETWEEN_PROCESSING = TickUnits.convertSecondsToTicks(10);

    public BlockEntityChunkLoaderHelper()
    {

    }

    public ArrayList<BlockEntityChunkLoadRequest> getBlockChunkLoadRequests()
    {
        return blockChunkLoadRequests;
    }

    private boolean isTickCooldownFinished()
    {
        return tickCooldownRemaining <= 0;
    }

    private void resetTickCooldown()
    {
        tickCooldownRemaining = TICKS_BETWEEN_PROCESSING;
    }

    public static void load(CompoundTag tag)
    {
        if(getChunkLoaderHelper() == null)
        {
            SculkHorde.LOGGER.error("BlockEntityChunkLoaderHelper is null. Cannot Load");
            return;
        }

        getChunkLoaderHelper().blockChunkLoadRequests.clear();
        ListTag blockChunkLoadRequestsTag = tag.getList("blockChunkLoadRequests", 10);
        for(int i = 0; i < blockChunkLoadRequestsTag.size(); i++)
        {
            CompoundTag requestTag = blockChunkLoadRequestsTag.getCompound(i);
            BlockEntityChunkLoadRequest request = BlockEntityChunkLoadRequest.serialize(requestTag);
            getChunkLoaderHelper().blockChunkLoadRequests.add(request);
        }
    }

    public static CompoundTag save(CompoundTag tag)
    {
        if(getChunkLoaderHelper() == null)
        {
            SculkHorde.LOGGER.error("BlockEntityChunkLoaderHelper is null. Cannot Save.");
            return tag;
        }

        ListTag blockChunkLoadRequestsTag = new ListTag();
        for(BlockEntityChunkLoadRequest request : getChunkLoaderHelper().blockChunkLoadRequests)
        {
            blockChunkLoadRequestsTag.add(request.deserialize());
        }
        tag.put("blockChunkLoadRequests", blockChunkLoadRequestsTag);
        return tag;
    }

    public static BlockEntityChunkLoaderHelper getChunkLoaderHelper()
    {
        return SculkHorde.blockEntityChunkLoaderHelper;
    }


    public void processBlockChunkLoadRequests(ServerLevel world)
    {
        if(!isTickCooldownFinished())
        {
            tickCooldownRemaining--;
            return;
        }

        resetTickCooldown();

        for(int i = 0; i < blockChunkLoadRequests.size(); i++)
        {
            BlockEntityChunkLoadRequest request = blockChunkLoadRequests.get(i);
            request.decrementTicksUntilExpiration(TICKS_BETWEEN_PROCESSING);
            if(request.isExpired())
            {
                if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.info("BlockEntityChunkLoader | Chunk EXPIRED, Unloading and Removing");}
                unloadAndRemoveChunksWithOwner(request.getOwner(), world);
            }

            loadChunksWithOwner(request.getOwner(), world);
        }
    }

    public static void forceLoadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ) {
        // Old shit method that causes runaway chunks
        //ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
        world.setChunkForced(chunkX, chunkZ, true);
    }

    public static void unloadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ) {
        // Old shit method that causes runaway chunks
        //ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
        world.setChunkForced(chunkX, chunkZ, false);
    }
    public void unloadAndRemoveChunksWithOwner(BlockPos owner, ServerLevel level)
    {
        for(int i = 0; i < blockChunkLoadRequests.size(); i++)
        {
            BlockEntityChunkLoadRequest request = blockChunkLoadRequests.get(i);
            if(request.isOwner(owner))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    unloadChunk(level, owner, chunkPos.x, chunkPos.z);
                }
                blockChunkLoadRequests.remove(i);
                i--;
            }
        }
    }

    public void loadChunksWithOwner(BlockPos owner, ServerLevel level)
    {
        for(int i = 0; i < blockChunkLoadRequests.size(); i++)
        {
            BlockEntityChunkLoadRequest request = blockChunkLoadRequests.get(i);
            if(request.isOwner(owner))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    forceLoadChunk(level, owner, chunkPos.x, chunkPos.z);
                }
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

    public boolean doesChunkLoadRequestAlreadyExist(String requestID)
    {
        for(BlockEntityChunkLoadRequest request : blockChunkLoadRequests)
        {
            if(request.isRequestID(requestID))
            {
                return true;
            }
        }
        return false;
    }

    private String generateRequestIDFromBlockPos(BlockPos owner)
    {
        ChunkPos centerChunkPos = new ChunkPos(owner);
        return String.valueOf(centerChunkPos.x) + String.valueOf(centerChunkPos.z);
    }

    public void createChunkLoadRequestSquare(BlockPos owner, int length, int priority, long ticksUnitExpiration)
    {
        if (length % 2 == 0) {
            length++; // Ensure the length is odd
        }

        String requestID = generateRequestIDFromBlockPos(owner);

        // Calculate the half-length of the square to determine chunks before and after the entity's chunk
        int halfLength = length / 2;

        // Get the chunk position where the entity is located
        ChunkPos entityChunkPos = new ChunkPos(owner);

        // Calculate the chunk positions to be loaded
        ChunkPos[] chunkPositionsToLoad = new ChunkPos[length * length];
        int index = 0;
        for (int dx = -halfLength; dx <= halfLength; dx++) {
            for (int dz = -halfLength; dz <= halfLength; dz++) {
                chunkPositionsToLoad[index++] = new ChunkPos(entityChunkPos.x + dx, entityChunkPos.z + dz);
            }
        }
        createChunkLoadRequest(owner, chunkPositionsToLoad, priority, requestID, ticksUnitExpiration);
    }

    private void createChunkLoadRequest(BlockPos owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration)
    {

        if(!doesChunkLoadRequestAlreadyExist(requestID))
        {
            BlockEntityChunkLoadRequest request = new BlockEntityChunkLoadRequest(owner, chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
            blockChunkLoadRequests.add(request);
        }

    }
}
