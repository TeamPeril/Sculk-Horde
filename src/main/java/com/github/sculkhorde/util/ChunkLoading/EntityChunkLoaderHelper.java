package com.github.sculkhorde.util.ChunkLoading;

import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

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

    public static EntityChunkLoaderHelper getEntityChunkLoaderHelper()
    {
        return SculkHorde.entityChunkLoaderHelper;
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

    public void processEntityChunkLoadRequests()
    {
        if(!isTickCooldownFinished())
        {
            tickCooldownRemaining--;
            return;
        }

        resetTickCooldown();

        safelyCleanExpiredChunks();
        loadChunksThatNeedToBeLoaded();
    }

    public void safelyRemoveChunksAtIndexes(ArrayList<Integer> indexesToRemove)
    {
        // New List to reassign to entityChunkLoadRequests
        ArrayList<EntityChunkLoadRequest> newList = new ArrayList<>();

        for(int i = 0; i < entityChunkLoadRequests.size(); i++)
        {
            // Skip the index to remove
            if(indexesToRemove.contains(i))
            {
                unloadChunksWithOwner(entityChunkLoadRequests.get(i).getOwner(), entityChunkLoadRequests.get(i).getDimension());
                continue;
            }

            newList.add(entityChunkLoadRequests.get(i));
        }

        // Reassign the list
        entityChunkLoadRequests = newList;
    }

    public void safelyCleanExpiredChunks()
    {
        ArrayList<Integer> indexesToRemove = new ArrayList<>();

        for(int i = 0; i < entityChunkLoadRequests.size(); i++)
        {
            EntityChunkLoadRequest request = entityChunkLoadRequests.get(i);
            request.decrementTicksUntilExpiration(TICKS_BETWEEN_PROCESSING);

            if(request.getDimension() == null)
            {
                if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.error("EntityChunkLoader | Dimension is null, Removing");}
                indexesToRemove.add(i);
                continue;
            }

            if(request.isExpired())
            {
                if(SculkHorde.isDebugMode()) {SculkHorde.LOGGER.info("EntityChunkLoader | Chunk EXPIRED, Unloading and Removing");}
                indexesToRemove.add(i);
                return;
            }
        }

        safelyRemoveChunksAtIndexes(indexesToRemove);
    }

    public void loadChunksThatNeedToBeLoaded()
    {
        for(int i = 0; i < entityChunkLoadRequests.size(); i++)
        {
            EntityChunkLoadRequest request = entityChunkLoadRequests.get(i);
            loadChunksWithOwner(request.getOwner(), request.getDimension());
        }
    }

    private static void forceLoadChunk(ServerLevel world, int chunkX, int chunkZ) {

        if(world == null)
        {
            SculkHorde.LOGGER.error("World is null. Cannot Force Load Chunk");
            return;
        }
        world.setChunkForced(chunkX, chunkZ, true);
    }
    public static void unloadChunk(ServerLevel world, int chunkX, int chunkZ) {

        if(world == null)
        {
            SculkHorde.LOGGER.error("World is null. Cannot Force Unload Chunk");
            return;
        }
        world.setChunkForced(chunkX, chunkZ, false);
    }
    public void unloadChunksWithOwner(UUID owner, ServerLevel level)
    {
        for (EntityChunkLoadRequest request : entityChunkLoadRequests) {
            if (request.isOwner(owner)) {
                for (ChunkPos chunkPos : request.getChunkPositionsToLoad()) {
                    unloadChunk(level, chunkPos.x, chunkPos.z);
                }
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

        ServerLevel serverLevel = ((ServerLevel) owner.level());

        serverLevel.getServer().tell(new TickTask(serverLevel.getServer().getTickCount() + 1, () -> {
            loadChunksWithOwner(owner.getUUID(), serverLevel);
        }));

    }

    private void createChunkLoadRequest(Entity owner, ChunkPos[] chunkPositionsToLoad, int priority, String requestID, long ticksUntilExpiration)
    {
        if(doesChunkLoadRequestAlreadyExist(requestID) || !ModConfig.SERVER.chunk_loading_enabled.get())
        {
            return;
        }
        EntityChunkLoadRequest request = new EntityChunkLoadRequest(owner.level().dimension(), owner.getUUID(), chunkPositionsToLoad, priority, requestID, ticksUntilExpiration);
        entityChunkLoadRequests.add(request);
        loadChunksWithOwner(request.getOwner(), request.getDimension());
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

}
