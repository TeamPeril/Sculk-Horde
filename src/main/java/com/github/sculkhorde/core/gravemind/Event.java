package com.github.sculkhorde.core.gravemind;

import java.util.logging.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

public class Event {


    protected int eventID;
    protected int eventCost;
    protected BlockPos eventLocation;
    protected long EXECUTION_COOLDOWN;
    protected long lastGameTimeOfEventExecution;

    protected ResourceKey<Level> dimension;

    private Event(ResourceKey<Level> dimension)
    {
        this.dimension = dimension;
    }

    public static Event createEvent(ResourceKey<Level> dimension)
    {
        return new Event(dimension);
    }

    // Getters and Setters

    public int getEventID()
    {
        return eventID;
    }

    // Logic

    public boolean canStart()
    {
        return true;
    }

    public boolean canContinue()
    {
        return false;
    }

    public void start()
    {

    }

    public void serverTick()
    {

    }

    public void end()
    {

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }
        if(!Event.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        return eventID == ((Event)obj).eventID;
    }
}
