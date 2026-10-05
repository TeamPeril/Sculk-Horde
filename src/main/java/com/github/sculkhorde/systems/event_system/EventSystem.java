package com.github.sculkhorde.systems.event_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.event_system.events.GhastDeploymentEvent;
import com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadEvent;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.systems.event_system.events.SpawnPhantomsAtRandomNodeEvent;
import com.github.sculkhorde.systems.event_system.events.SpawnPhantomsEvent;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class EventSystem {

    private UUID eventSystemUUID;

    //Hash Map of Events using event IDs as keys
    private HashMap<UUID, Event> events;

    private long lastGameTimeOfExecution;
    private final long EXECUTION_COOLDOWN_TICKS = TickUnits.convertSecondsToTicks(0.5F);

    public EventSystem()
    {
        events = new HashMap<UUID, Event>();
        eventSystemUUID = UUID.randomUUID();
    }

    public static int howManyActiveRaids()
    {
        int result = 0;
        for(Event e : SculkHorde.eventSystem.getEvents().values())
        {
            if(e instanceof RaidEvent)
            {
                result++;
            }
        }

        return result;
    }

    public HashMap<UUID, Event> getEvents()
    {
        return events;
    }

    public boolean canExecute()
    {
        boolean isHordeActive = ModSavedData.getSaveData().isHordeActive();
        // Check overworld time
        return isHordeActive && (ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - lastGameTimeOfExecution) > EXECUTION_COOLDOWN_TICKS;
    }

    public Event getEvent(UUID eventID)
    {
        return events.get(eventID);
    }

    public boolean doesEventExist(UUID eventID)
    {
        return events.containsKey(eventID);
    }

    public void addEvent(Event event)
    {
        // If event doesnt already exist
        if(!events.containsKey(event.getEventUUID()))
        {
            events.put(event.getEventUUID(), event);
            DebuggerSystem.eventDebuggerModule.logInfo("Added event " + event.getClass().getSimpleName() + " with ID: " + event.getEventUUID() + " to EventSystem " + eventSystemUUID.toString());
        }
    }

    public void removeEvent(UUID eventID)
    {
        events.remove(eventID);
    }

    public void serverTick()
    {
        if(!canExecute())
        {
            return;
        }

        lastGameTimeOfExecution = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();

        for(Event event : events.values())
        {
            if(event.isToBeRemoved())
            {
                removeEvent(event.getEventUUID());
                DebuggerSystem.eventDebuggerModule.logInfo("Removed event " + event.getClass().getSimpleName() + " with ID: " + event.getEventUUID() + " from EventSystem " + eventSystemUUID.toString());

                // WE CANNOT CONTINUE, WE NEED TO RETURN AND START OVER SO WE DON'T GET A CONCURRENT MODIFICATION EXCEPTION
                return;
            }

            boolean isEventActive = event.isEventActive();
            boolean canEventStart = event.canStart();
            boolean canEventContinue = event.canContinue();

            if(!isEventActive && canEventStart)
            {
                event.start();
                DebuggerSystem.eventDebuggerModule.logInfo("Starting event " + event.getClass().getSimpleName() + " with ID: " + event.getEventUUID() + " from EventSystem " + eventSystemUUID.toString());
                continue;
            }

            if(isEventActive && canEventContinue)
            {
                event.serverTick();
                continue;
            }

            if(isEventActive && !canEventContinue)
            {
                event.markEventAsFinished();
                continue;
            }
        }
    }

    public static void save(CompoundTag tag)
    {
        if(SculkHorde.eventSystem.getEvents().isEmpty())
        {
            return;
        }

        DebuggerSystem.eventDebuggerModule.logInfo("Saving " + SculkHorde.eventSystem.getEvents().size() + " events.");
        CompoundTag eventsTag = new CompoundTag();
        long startTime = System.currentTimeMillis();
        for(Event event : SculkHorde.eventSystem.getEvents().values())
        {
            CompoundTag eventTag = new CompoundTag();
            event.save(eventTag);

            if(event instanceof HitSquadEvent hitSquadEvent)
            {
                hitSquadEvent.saveAdditional(eventTag);
                eventTag.putString("eventType", hitSquadEvent.getClass().getName());
            }
            else if (event instanceof SpawnPhantomsEvent phantomsEvent)
            {
                phantomsEvent.saveAdditional(eventTag);
                eventTag.putString("eventType", phantomsEvent.getClass().getName());
            }
            else if (event instanceof RaidEvent raidEvent)
            {
                raidEvent.saveAdditional(eventTag);
                eventTag.putString("eventType", raidEvent.getClass().getName());
            }
            else if(event instanceof SpawnPhantomsAtRandomNodeEvent rngNode)
            {
                rngNode.saveAdditional(eventTag);
                eventTag.putString("eventType", rngNode.getClass().getName());
            }
            else if(event instanceof GhastDeploymentEvent ghastDeploymentEvent)
            {
                ghastDeploymentEvent.saveAdditional(eventTag);
                eventTag.putString("eventType", ghastDeploymentEvent.getClass().getName());
            }

            eventsTag.put(event.getEventUUID().toString(), eventTag);
            eventTag.putInt(Difficulty.class.getSimpleName(), event.getMinimumDifficulty().getId());
            DebuggerSystem.eventDebuggerModule.logInfo("Saved " + event.getClass().getSimpleName() + " event.");
        }
        tag.put("events", eventsTag);
        DebuggerSystem.eventDebuggerModule.logInfo("Saved " + SculkHorde.eventSystem.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static void load(CompoundTag tag)
    {

        SculkHorde.eventSystem = new EventSystem();
        CompoundTag eventsTag = tag.getCompound("events");

        DebuggerSystem.eventDebuggerModule.logInfo("Loading " + eventsTag.getAllKeys().size() + " events.");
        long startTime = System.currentTimeMillis();

        for(String key : eventsTag.getAllKeys())
        {
            Event event;
            CompoundTag eventTag = eventsTag.getCompound(key);

            if(!eventTag.contains("dimension"))
            {
                DebuggerSystem.eventDebuggerModule.logError("EventSystem | load | " + "Attempted to load event with no dimension.");
                continue;
            }

            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(eventTag.getString("dimension")));
            String eventType = eventTag.getString("eventType");

            if (HitSquadEvent.class.getName().equals(eventType)) {
                HitSquadEvent hitSquadEvent = new HitSquadEvent(dimensionResourceKey);
                hitSquadEvent.loadAdditional(eventTag);
                event = hitSquadEvent;
            }
            else if (SpawnPhantomsEvent.class.getName().equals(eventType)) {
                SpawnPhantomsEvent phantomEvent = new SpawnPhantomsEvent(dimensionResourceKey);
                phantomEvent.loadAdditional(eventTag);
                event = phantomEvent;
            }
            else if (RaidEvent.class.getName().equals(eventType)) {
                RaidEvent raidEvent = new RaidEvent(dimensionResourceKey);
                raidEvent.loadAdditional(eventTag);
                event = raidEvent;
            }
            else if (SpawnPhantomsAtRandomNodeEvent.class.getName().equals(eventType)){
                SpawnPhantomsAtRandomNodeEvent rngNodeEvent = new SpawnPhantomsAtRandomNodeEvent(dimensionResourceKey);
                rngNodeEvent.loadAdditional(eventTag);
                event = rngNodeEvent;

            }
            else if(GhastDeploymentEvent.class.getName().equals(eventType)) {
                GhastDeploymentEvent ghastDeploymentEvent = new GhastDeploymentEvent(dimensionResourceKey);
                ghastDeploymentEvent.loadAdditional(eventTag);
                event = ghastDeploymentEvent;
            }
            else {
                DebuggerSystem.eventDebuggerModule.logError("EventSystem | load | " + "Attempted to load event with no known eventType: " + eventType);
                continue;
            }

            Event.loadCommonPropertiesFromTag(event, eventTag);
            event.setWasLoadedFromSaveData(true);
            SculkHorde.eventSystem.addEvent(event);
        }
        DebuggerSystem.eventDebuggerModule.logInfo("Loaded " + SculkHorde.eventSystem.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }


    public static Optional<RaidEvent> getNearestRaidEvent(ServerLevel dimension, BlockPos location)
    {
        Optional<RaidEvent> result = Optional.empty();
        for(Event e : SculkHorde.eventSystem.getEvents().values())
        {
            if(e instanceof RaidEvent raidEvent)
            {
                // If not in same dimension, or location not set yet, ignore.
                if(!BlockAlgorithms.areTheseDimensionsEqual(raidEvent.getDimension(), dimension) || raidEvent.getEventLocation() == null)
                {
                    continue;
                }
                else if(result.isEmpty())
                {
                    result = Optional.of(raidEvent);
                    continue;
                }
                else if(BlockAlgorithms.getBlockDistanceXZ(result.get().getEventLocation(), location) > BlockAlgorithms.getBlockDistanceXZ(raidEvent.getEventLocation(), location))
                {
                    result = Optional.of(raidEvent);
                }
            }
        }

        return result;
    }

}
