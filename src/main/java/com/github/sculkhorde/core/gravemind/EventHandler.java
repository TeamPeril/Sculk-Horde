package com.github.sculkhorde.core.gravemind;

import java.util.HashMap;

public class EventHandler {

    //Hash Map of Events using event IDs as keys
    private HashMap<Integer, Event> events;

    public EventHandler()
    {
        events = new HashMap<Integer, Event>();
    }

    public Event getEvent(int eventID)
    {
        return events.get(eventID);
    }

    public void addEvent(Event event)
    {
        events.put(event.getEventID(), event);
    }

    public void removeEvent(int eventID)
    {
        events.remove(eventID);
    }

    public void serverTick()
    {

    }

}
