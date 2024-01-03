package com.github.sculkhorde.core.gravemind.events;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class Event {


    protected long eventID;
    protected int eventCost;
    protected BlockPos eventLocation;
    protected long EXECUTION_COOLDOWN;
    protected long lastGameTimeOfEventExecution;

    protected ResourceKey<net.minecraft.world.level.Level> dimension;
    protected boolean isEventReocurring = false;

    protected boolean isEventActive = false;
    protected boolean toBeRemoved = false;


    public Event(ResourceKey<net.minecraft.world.level.Level> dimension)
    {
        this.dimension = dimension;
    }

    public static Event createEvent(ResourceKey<net.minecraft.world.level.Level> dimension)
    {
        return new Event(dimension);
    }


    // Getters and Setters

    public long getEventID()
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
        SculkHorde.savedData.subtractSculkAccumulatedMass(eventCost);
        setEventActive(true);
    }

    public void serverTick()
    {

    }

    public void end()
    {
        if(!isEventReocurring)
        {
            toBeRemoved = true;
        }

        setEventActive(false);
        setLastGameTimeOfEventExecution(getDimension().getGameTime());
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

    public void setEventLocation(BlockPos eventLocation) {
        this.eventLocation = eventLocation;
    }

    public BlockPos getEventLocation() {
        return eventLocation;
    }

    public Event setEventID(long eventID) {
        this.eventID = eventID;
        return this;
    }

    public Event setEventCost(int eventCost) {
        this.eventCost = eventCost;
        return this;
    }

    public Event setEXECUTION_COOLDOWN(long EXECUTION_COOLDOWN) {
        this.EXECUTION_COOLDOWN = EXECUTION_COOLDOWN;
        return this;
    }

    public Event setLastGameTimeOfEventExecution(long lastGameTimeOfEventExecution) {
        this.lastGameTimeOfEventExecution = lastGameTimeOfEventExecution;
        return this;
    }

    public Event setDimension(ResourceKey<net.minecraft.world.level.Level> dimension) {
        this.dimension = dimension;
        return this;
    }

    public Event setEventReocurring(boolean isEventReocurring) {
        this.isEventReocurring = isEventReocurring;
        return this;
    }

    public Event setToBeRemoved(boolean toBeRemoved) {
        this.toBeRemoved = toBeRemoved;
        return this;
    }

    public Event setEventActive(boolean eventActive) {
        isEventActive = eventActive;
        return this;
    }

    public boolean isEventActive() {
        return isEventActive;
    }

    public int getEventCost() {
        return eventCost;
    }

    public long getEXECUTION_COOLDOWN() {
        return EXECUTION_COOLDOWN;
    }

    public long getLastGameTimeOfEventExecution() {
        return lastGameTimeOfEventExecution;
    }

    public ServerLevel getDimension()
    {
        return SculkHorde.savedData.level.getServer().getLevel(dimension);
    }

    public boolean isEventReocurring() {
        return isEventReocurring;
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    // Save and Load

    public void save(CompoundTag tag)
    {
        tag.putLong("eventID", getEventID());
        tag.putInt("eventCost", getEventCost());
        tag.putLong("EXECUTION_COOLDOWN", getEXECUTION_COOLDOWN());
        tag.putLong("lastGameTimeOfEventExecution", getLastGameTimeOfEventExecution());
        tag.putBoolean("isEventReocurring", isEventReocurring());
        tag.putBoolean("isEventActive", isEventActive());
        tag.putBoolean("toBeRemoved", isToBeRemoved());
        tag.putString("dimension", dimension.location().toString());
        tag.putLong("eventLocation", eventLocation.asLong());
    }

    public static Event load(CompoundTag tag)
    {
        ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")));

        Event event = Event.createEvent(dimensionResourceKey);
        event.setEventID(tag.getInt("eventID"));
        event.setEventCost(tag.getInt("eventCost"));
        event.setEXECUTION_COOLDOWN(tag.getLong("EXECUTION_COOLDOWN"));
        event.setLastGameTimeOfEventExecution(tag.getLong("lastGameTimeOfEventExecution"));
        event.setEventReocurring(tag.getBoolean("isEventReocurring"));
        event.setEventActive(tag.getBoolean("isEventActive"));
        event.setToBeRemoved(tag.getBoolean("toBeRemoved"));
        event.setEventLocation(BlockPos.of(tag.getLong("eventLocation")));
        return event;
    }
}
