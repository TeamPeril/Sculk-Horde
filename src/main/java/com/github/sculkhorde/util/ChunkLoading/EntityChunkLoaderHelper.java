package com.github.sculkhorde.util.ChunkLoading;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class EntityChunkLoaderHelper
{
    private ArrayList<EntityChunkLoadRequest> entityChunkLoadRequests = new ArrayList<>();

    private int tickCooldownRemaining = 0;
    private final int TICKS_BETWEEN_PROCESSING = TickUnits.convertSecondsToTicks(10);

    public EntityChunkLoaderHelper()
    {

    }
    public ArrayList<EntityChunkLoadRequest> getEntityChunkLoadRequests()
    {
        return entityChunkLoadRequests;
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
        if(getEntityChunkLoaderHelper() == null)
        {
            SculkHorde.LOGGER.error("EntityChunkLoaderHelper is null. Cannot Load");
            return;
        }

        getEntityChunkLoaderHelper().entityChunkLoadRequests.clear();
        ListTag entityChunkLoadRequestsTag = tag.getList("entityChunkLoadRequests", 10);
        for(int i = 0; i < entityChunkLoadRequestsTag.size(); i++)
        {
            CompoundTag requestTag = entityChunkLoadRequestsTag.getCompound(i);
            EntityChunkLoadRequest request = EntityChunkLoadRequest.serialize(requestTag);
            getEntityChunkLoaderHelper().entityChunkLoadRequests.add(request);
        }
    }

    public static CompoundTag save(CompoundTag tag)
    {
        if(getEntityChunkLoaderHelper() == null)
        {
            SculkHorde.LOGGER.error("EntityChunkLoaderHelper is null. Cannot Save");
            return tag;
        }

        ListTag entityChunkLoadRequestsTag = new ListTag();
        for(EntityChunkLoadRequest request : getEntityChunkLoaderHelper().entityChunkLoadRequests)
        {
            entityChunkLoadRequestsTag.add(request.deserialize());
        }
        tag.put("entityChunkLoadRequests", entityChunkLoadRequestsTag);

        return tag;
    }

    public static EntityChunkLoaderHelper getEntityChunkLoaderHelper()
    {
        return SculkHorde.entityChunkLoaderHelper;
    }


    public void processEntityChunkLoadRequests(ServerLevel world)
    {
        if(!isTickCooldownFinished())
        {
            tickCooldownRemaining--;
            return;
        }

        resetTickCooldown();

        for(int i = 0; i < entityChunkLoadRequests.size(); i++)
        {
            EntityChunkLoadRequest request = entityChunkLoadRequests.get(i);
            request.decrementTicksUntilExpiration(TICKS_BETWEEN_PROCESSING);
            if(request.isExpired())
            {
                if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.info("EntityChunkLoader | Chunk EXPIRED, Unloading and Removing");}
                unloadAndRemoveChunksWithOwner(request.getOwner(), world);
                return;
            }
            //if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.info("EntityChunkLoader | Chunk OK, Making Sure Loaded");}
            loadChunksWithOwner(request.getOwner(), world);
        }
    }

    private static void forceLoadChunk(ServerLevel world, int chunkX, int chunkZ) {

        // Old shit method that causes runaway chunks
        //ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
        world.setChunkForced(chunkX, chunkZ, true);

    }
    public static void unloadChunk(ServerLevel world, int chunkX, int chunkZ) {

        // Old shit method that causes runaway chunks
        //ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
        world.setChunkForced(chunkX, chunkZ, false);
    }
    public void unloadAndRemoveChunksWithOwner(UUID owner, ServerLevel level)
    {
        Iterator<EntityChunkLoadRequest> iterator = entityChunkLoadRequests.iterator();
        while(iterator.hasNext())
        {
            EntityChunkLoadRequest request = iterator.next();
            if(request.isOwner(owner))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    unloadChunk(level, chunkPos.x, chunkPos.z);
                }
                iterator.remove();
            }
        }
    }
    public void loadChunksWithOwner(UUID owner, ServerLevel level)
    {
        Iterator<EntityChunkLoadRequest> iterator = entityChunkLoadRequests.iterator();
        while(iterator.hasNext())
        {
            EntityChunkLoadRequest request = iterator.next();
            if(request.isOwner(owner))
            {
                for(ChunkPos chunkPos : request.getChunkPositionsToLoad())
                {
                    forceLoadChunk(level, chunkPos.x, chunkPos.z);
                }
            }
        }
    }

    public boolean doesChunkLoadRequestAlreadyExist(String requestID)
    {
        for(EntityChunkLoadRequest request : entityChunkLoadRequests)
        {
            if(request.isRequestID(requestID))
            {
                return true;
            }
        }
        return false;
    }

    private String generateRequestIDFromEntity(Entity owner)
    {
        ChunkPos centerChunkPos = new ChunkPos(owner.blockPosition());
        return String.valueOf(centerChunkPos.x) + String.valueOf(centerChunkPos.z);
    }

    public void createChunkLoadRequestSquareForEntityIfAbsent(Entity owner, int length, int priority, long ticksUnitExpiration)
    {
        if (length % 2 == 0) {
            length++; // Ensure the length is odd
        }

        String requestID = generateRequestIDFromEntity(owner);

        // Calculate the half-length of the square to determine chunks before and after the entity's chunk
        int halfLength = length / 2;

        // Get the chunk position where the entity is located
        ChunkPos entityChunkPos = new ChunkPos(owner.blockPosition());

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

    private void createChunkLoadRequest(Entity owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration)
    {
        if(doesChunkLoadRequestAlreadyExist(requestID))
        {
            return;
        }
        EntityChunkLoadRequest request = new EntityChunkLoadRequest(owner.getUUID(), chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        entityChunkLoadRequests.add(request);
    }
}
