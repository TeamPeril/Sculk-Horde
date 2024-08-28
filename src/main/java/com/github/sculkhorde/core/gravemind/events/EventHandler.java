package com.github.sculkhorde.core.gravemind.events;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;

public class EventHandler {

    //Hash Map of Events using event IDs as keys
    private HashMap<Long, Event> events;

    private long lastGameTimeOfExecution;
    private final long EXECUTION_COOLDOWN_TICKS = TickUnits.convertMinutesToTicks(1);

    public EventHandler()
    {
        events = new HashMap<Long, Event>();
    }

    public HashMap<Long, Event> getEvents()
    {
        return events;
    }

    public boolean canExecute()
    {
        boolean isHordeActive = SculkHorde.savedData.isHordeActive();
        // Check overworld time
        return isHordeActive && (ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - lastGameTimeOfExecution) > EXECUTION_COOLDOWN_TICKS;
    }

    public Event getEvent(long eventID)
    {
        return events.get(eventID);
    }

    public boolean doesEventExist(long eventID)
    {
        return events.containsKey(eventID);
    }

    public void addEvent(Event event)
    {
        // If event doesnt already exist
        if(!events.containsKey(event.getEventID()))
        {
            events.put(event.getEventID(), event);
            SculkHorde.LOGGER.info("Added event " + event.getClass() + " with ID: " + event.getEventID());
        }
    }

    public void removeEvent(long eventID)
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
                removeEvent(event.getEventID());
                SculkHorde.LOGGER.info("Removed event " + event.getClass() + " with ID: " + event.getEventID());

                // WE CANNOT CONTINUE, WE NEED TO RETURN AND START OVER SO WE DON'T GET A CONCURRENT MODIFICATION EXCEPTION
                return;
            }

            boolean isEventActive = event.isEventActive();
            boolean canEventStart = event.canStart();
            boolean canEventContinue = event.canContinue();

            if(!isEventActive && canEventStart)
            {
                event.start();
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
                continue;
            }
        }
    }

    public static void save(CompoundTag tag)
    {
        SculkHorde.LOGGER.info("Saving " + SculkHorde.eventHandler.getEvents().size() + " events.");
        CompoundTag eventsTag = new CompoundTag();
        long startTime = System.currentTimeMillis();
        for(Event event : SculkHorde.eventHandler.getEvents().values())
        {
            CompoundTag eventTag = new CompoundTag();
            event.save(eventTag);
            eventsTag.put(event.getClass().getName(), eventTag);
        }
        tag.put("events", eventsTag);
        SculkHorde.LOGGER.info("Saved " + SculkHorde.eventHandler.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static EventHandler load(CompoundTag tag)
    {

        EventHandler eventHandler = new EventHandler();
        CompoundTag eventsTag = tag.getCompound("events");

        SculkHorde.LOGGER.info("Loading " + eventsTag.getAllKeys().size() + " events.");
        long startTime = System.currentTimeMillis();
        for(String key : eventsTag.getAllKeys())
        {
            CompoundTag eventTag = eventsTag.getCompound(key);
            Event event = Event.load(eventTag);
            eventHandler.addEvent(event);
        }
        SculkHorde.LOGGER.info("Loaded " + eventHandler.getEvents().size() + " events. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
        return eventHandler;
    }

}
