package com.github.sculkhorde.systems.path_builder_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PathBuilderSystem {
    //Hash Map of Events using event IDs as keys
    private LinkedHashMap<UUID, PathBuilderRequest> pathBuilderRequests;
    private HashMap<UUID, PathBuilder> pathBuilders;

    private long lastGameTimeOfExecution;
    private final long EXECUTION_COOLDOWN_TICKS = TickUnits.convertSecondsToTicks(0.5F);
    private static final int MAX_ACTIVE_BUILDERS = 3;

    public PathBuilderSystem()
    {
        pathBuilderRequests = new LinkedHashMap<UUID, PathBuilderRequest>();
        pathBuilders = new HashMap<UUID, PathBuilder>();
    }

    public Map<UUID, PathBuilderRequest> getPathBuilderRequests()
    {
        return pathBuilderRequests;
    }
    public Map<UUID, PathBuilder> getPathBuilders()
    {
        return pathBuilders;
    }

    public boolean canExecute()
    {
        boolean isHordeActive = ModSavedData.getSaveData().isHordeActive();
        // Check overworld time
        return isHordeActive && (ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - lastGameTimeOfExecution) > EXECUTION_COOLDOWN_TICKS;
    }

    public PathBuilderRequest getPathBuilderRequest(UUID uuid)
    {
        return pathBuilderRequests.get(uuid);
    }
    public PathBuilder getPathBuilder(UUID uuid)
    {
        return pathBuilders.get(uuid);
    }

    public boolean hasPathBuilderRequest(UUID uuid)
    {
        return pathBuilderRequests.containsKey(uuid);
    }
    public boolean hasPathBuilder(UUID uuid)
    {
        return pathBuilders.containsKey(uuid);
    }

    public void addPathBuilder(PathBuilder pathBuilder)
    {
        // If event doesnt already exist
        if(!pathBuilders.containsKey(pathBuilder.uuid))
        {
            pathBuilders.put(pathBuilder.uuid, pathBuilder);
            SculkHorde.LOGGER.info("Added pathBuilder " + pathBuilder.getClass() + " with ID: " + pathBuilder.uuid + " to PathBuilderSystem");
        }
    }

    public void addPathBuilderRequest(PathBuilderRequest pathBuilderRequest)
    {
        // If event doesnt already exist
        if(!pathBuilderRequests.containsKey(pathBuilderRequest.uuid))
        {
            pathBuilderRequests.put(pathBuilderRequest.uuid, pathBuilderRequest);
            SculkHorde.LOGGER.info("Added pathBuilderRequest " + pathBuilderRequest.getClass() + " with ID: " + pathBuilderRequest.uuid + " to PathBuilderSystem");
        }
    }

    public void removePathBuilder(UUID uuid)
    {
        pathBuilders.remove(uuid);
    }
    public void removePathBuilderRequest(UUID uuid)
    {
        pathBuilderRequests.remove(uuid);
    }

    public boolean isActivePathBuildersAtMax()
    {
        return getActivePathBuilders() >= MAX_ACTIVE_BUILDERS;
    }

    public int getActivePathBuilders()
    {
        int count = 0;
        for(PathBuilder pathBuilder: pathBuilders.values())
        {
            if(pathBuilder.isWorking())
            {
                count +=1;
            }
        }

        return count;
    }

    public PathBuilderRequest popNextPathBuilderRequest()
    {
        PathBuilderRequest request = pathBuilderRequests.values().iterator().next();
        removePathBuilderRequest(request.uuid);
        return request;
    }

    public void serverTick()
    {
        if(!canExecute())
        {
            return;
        }

        lastGameTimeOfExecution = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();

        // Iterate through each pathBuilder. Remove expired ones safely, skip finished ones, and tick active ones.
        Iterator<PathBuilder> iterator = pathBuilders.values().iterator();
        while (iterator.hasNext())
        {
            PathBuilder currentPathBuilder = iterator.next();

            if(currentPathBuilder.isFinished() && currentPathBuilder.isExpired())
            {
                // Safe removal during iteration to avoid ConcurrentModificationException
                iterator.remove();
                continue;
            }

            if(currentPathBuilder.isFinished())
            {
                continue;
            }

            currentPathBuilder.serverTick();
        }

        // If there is no more room for PathBuilders, just return
        if(isActivePathBuildersAtMax() || pathBuilderRequests.size() <= 0)
        {
            return;
        }

        // Create path builder if we have room
        PathBuilder pathBuilder = new PathBuilder();
        pathBuilder.request = Optional.of(popNextPathBuilderRequest());
        addPathBuilder(pathBuilder);
    }

    public static void save(CompoundTag tag)
    {
        //SculkHorde.LOGGER.info("Saving " + SculkHorde.eventSystem.getEvents().size() + " events.");
        //CompoundTag eventsTag = new CompoundTag();
        //long startTime = System.currentTimeMillis();
        //tag.put("events", eventsTag);
        //SculkHorde.LOGGER.info("Saved Path Builder System. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static void load(CompoundTag tag)
    {

        SculkHorde.pathBuilderSystem = new PathBuilderSystem();
        SculkHorde.LOGGER.info("Loading Path Builder System.");
        SculkHorde.LOGGER.info("Loaded Path Builder System.");
    }

}
