package com.github.sculkhorde.systems.event_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.DifficultyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;

public class Event {
    protected UUID eventUUID;
    protected int eventCost;
    protected BlockPos eventLocation;
    protected long EXECUTION_COOLDOWN;
    protected long lastGameTimeOfEventExecution;

    protected ResourceKey<Level> dimension;
    protected boolean isEventReocurring = false;

    protected boolean isEventActive = false;
    protected boolean toBeRemoved = false;

    protected Difficulty minimumDifficulty = Difficulty.EASY;

    public Event(ResourceKey<Level> dimension)
    {
        this.dimension = dimension;
        setEventUUID(UUID.randomUUID());
        minimumDifficulty = Difficulty.EASY;
    }

    public Event(ResourceKey<Level> dimension, Difficulty difficultyRequired)
    {
        this.dimension = dimension;
        setEventUUID(UUID.randomUUID());
        minimumDifficulty = difficultyRequired;
    }

    // Getters and Setters
    public UUID getEventUUID()
    {
        return eventUUID;
    }

    public Difficulty getMinimumDifficulty()
    {
        return minimumDifficulty;
    }

    // Logic

    public boolean canStart()
    {
        boolean hasEnoughTimePassed = getDimension().getGameTime() - lastGameTimeOfEventExecution >= EXECUTION_COOLDOWN;
        return hasEnoughTimePassed && DifficultyUtil.isCurrentDifficultyEqualToOrGreaterThan(minimumDifficulty);
    }

    public boolean canContinue()
    {
        return false;
    }

    public void start()
    {
        ModSavedData.getSaveData().subtractSculkAccumulatedMass(eventCost);
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
        return eventUUID == ((Event)obj).eventUUID;
    }

    public void setEventLocation(BlockPos eventLocation) {
        this.eventLocation = eventLocation;
    }

    public BlockPos getEventLocation() {
        return eventLocation;
    }

    protected Event setEventUUID(UUID eventUUID) {
        this.eventUUID = eventUUID;
        return this;
    }

    public Event setMinimumDifficulty(Difficulty difficulty)
    {
        minimumDifficulty = difficulty;
        return this;
    }

    public Event setMinimumDifficulty(int difficulty)
    {
        minimumDifficulty = Difficulty.byId(difficulty);
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

    public Event setDimension(ResourceKey<Level> dimension) {
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
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
    }

    public boolean isEventReoccurring() {
        return isEventReocurring;
    }

    public boolean isToBeRemoved() {
        return toBeRemoved;
    }

    // Save and Load

    public void saveAdditional(CompoundTag tag)
    {

    }

    public void save(CompoundTag tag)
    {
        tag.putString("eventType", this.getClass().getName());

        //  This is to handle an edge case:
        //  Events used to have an ID that was a long, instead of a UUID.
        //  It's possible that during a version update, if an event in the save data does not have a UUID,
        //  It would just indefinitely crash.
        if(getEventUUID() != null)
        {
            tag.putUUID("eventID", getEventUUID());
        }
        else
        {
            tag.putUUID("eventID", UUID.randomUUID());
        }
        tag.putInt("eventCost", getEventCost());
        tag.putLong("EXECUTION_COOLDOWN", getEXECUTION_COOLDOWN());
        tag.putLong("lastGameTimeOfEventExecution", getLastGameTimeOfEventExecution());
        tag.putBoolean("isEventReoccurring", isEventReoccurring());
        tag.putBoolean("isEventActive", isEventActive());
        tag.putBoolean("toBeRemoved", isToBeRemoved());
        if(dimension != null) { tag.putString("dimension", dimension.location().toString()); }
        if(eventLocation != null) { tag.putLong("eventLocation", eventLocation.asLong()); }
    }

    public void loadAdditional(CompoundTag tag)
    {

    }

    public static void loadCommonPropertiesFromTag(Event event, CompoundTag tag) {
        Optional.of(tag.getUUID("eventID")).ifPresent(event::setEventUUID);
        Optional.of(tag.getInt("eventCost")).ifPresent(event::setEventCost);
        Optional.of(tag.getInt(Difficulty.class.getSimpleName())).ifPresent(event::setMinimumDifficulty);
        Optional.of(tag.getLong("EXECUTION_COOLDOWN")).ifPresent(event::setEXECUTION_COOLDOWN);
        Optional.of(tag.getLong("lastGameTimeOfEventExecution")).ifPresent(event::setLastGameTimeOfEventExecution);
        Optional.of(tag.getBoolean("isEventReoccurring")).ifPresent(event::setEventReocurring);
        Optional.of(tag.getBoolean("isEventActive")).ifPresent(event::setEventActive);
        Optional.of(tag.getBoolean("toBeRemoved")).ifPresent(event::setToBeRemoved);
        Optional.of(tag.getLong("eventLocation")).map(BlockPos::of).ifPresent(event::setEventLocation);
    }
}
