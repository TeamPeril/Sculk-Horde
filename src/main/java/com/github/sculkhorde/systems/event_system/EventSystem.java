package com.github.sculkhorde.systems.event_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadEvent;
import com.github.sculkhorde.systems.event_system.events.SpawnPhantomsEvent;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
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
            SculkHorde.LOGGER.info("Added event " + event.getClass() + " with ID: " + event.getEventUUID() + " to EventSystem " + eventSystemUUID.toString());
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
                SculkHorde.LOGGER.info("Removed event " + event.getClass() + " with ID: " + event.getEventUUID() + " from EventSystem " + eventSystemUUID.toString());

                // WE CANNOT CONTINUE, WE NEED TO RETURN AND START OVER SO WE DON'T GET A CONCURRENT MODIFICATION EXCEPTION
                return;
            }

            boolean isEventActive = event.isEventActive();
            boolean canEventStart = event.canStart();
            boolean canEventContinue = event.canContinue();

            if(!isEventActive && canEventStart)
            {
                event.start();
                SculkHorde.LOGGER.info("Starting event " + event.getClass() + " with ID: " + event.getEventUUID() + " from EventSystem " + eventSystemUUID.toString());
                continue;
            }

            if(isEventActive && canEventContinue)
            {
                event.serverTick();
                continue;
            }

            if(isEventActive && !canEventContinue)
            {
                event.end();
                SculkHorde.LOGGER.info("Ending event " + event.getClass() + " with ID: " + event.getEventUUID() + " from EventSystem " + eventSystemUUID.toString());
                continue;
            }
        }
    }

    public static void save(CompoundTag tag)
    {
        SculkHorde.LOGGER.info("Saving " + SculkHorde.eventSystem.getEvents().size() + " events.");
        CompoundTag eventsTag = new CompoundTag();
        long startTime = System.currentTimeMillis();
        for(Event event : SculkHorde.eventSystem.getEvents().values())
        {
            CompoundTag eventTag = new CompoundTag();
            event.save(eventTag);

            if(event instanceof HitSquadEvent hitSquadEvent)
            {
                hitSquadEvent.saveAdditional(eventTag);
            }
            else if (event instanceof SpawnPhantomsEvent phantomsEvent)
            {
                phantomsEvent.saveAdditional(eventsTag);
            }

            eventsTag.put(event.getClass().getName(), eventTag);
            eventTag.putInt(Difficulty.class.getSimpleName(), event.getMinimumDifficulty().getId());
            SculkHorde.LOGGER.info("Saved " + event.getClass().getName() + " event.");
        }
        tag.put("events", eventsTag);
        SculkHorde.LOGGER.info("Saved " + SculkHorde.eventSystem.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static void load(CompoundTag tag)
    {

        SculkHorde.eventSystem = new EventSystem();
        CompoundTag eventsTag = tag.getCompound("events");

        SculkHorde.LOGGER.info("Loading " + eventsTag.getAllKeys().size() + " events.");
        long startTime = System.currentTimeMillis();

        for(String key : eventsTag.getAllKeys())
        {
            Event event;
            CompoundTag eventTag = eventsTag.getCompound(key);

            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(eventTag.getString("dimension")));
            String eventType = eventTag.getString("eventType");

            if (HitSquadEvent.class.getName().equals(eventType)) {
                HitSquadEvent hitSquadEvent = new HitSquadEvent(dimensionResourceKey);
                hitSquadEvent.loadAdditional(eventTag);
                event = hitSquadEvent;
            } else if (SpawnPhantomsEvent.class.getName().equals(eventType)) {
                SpawnPhantomsEvent phantomEvent = new SpawnPhantomsEvent(dimensionResourceKey);
                phantomEvent.loadAdditional(eventTag);
                event = phantomEvent;
            } else {
                event = new Event(dimensionResourceKey);
            }

            Event.loadCommonPropertiesFromTag(event, eventTag);
            SculkHorde.eventSystem.addEvent(event);
        }
        SculkHorde.LOGGER.info("Loaded " + SculkHorde.eventSystem.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

}
