package com.github.sculkhorde.systems.event_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.DifficultyUtil;
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

import java.util.UUID;

public class Event {
    protected UUID eventUUID;
    protected int eventCost;
    protected BlockPos eventLocation;
    protected long EXECUTION_COOLDOWN;
    protected long lastGameTimeOfEventExecution;
    protected long lastTimeOfCanStartCheck = 0;
    protected long CAN_START_CHECK_INTERVAL = TickUnits.convertSecondsToTicks(30);

    protected ResourceKey<Level> dimension;
    protected boolean isEventReocurring = false;

    protected boolean isEventActive = false;
    protected boolean toBeRemoved = false;
    protected boolean wasLoadedFromSaveData = false;

    protected Difficulty minimumDifficulty = Difficulty.EASY;

    public Event(ResourceKey<Level> dimension) {
        this.dimension = dimension;
        setEventUUID(UUID.randomUUID());
        minimumDifficulty = Difficulty.EASY;

        if(dimension == null)
        {
            DebuggerSystem.eventDebuggerModule.logError(getClass().getSimpleName() + " | ERROR: NULL was passed as a dimension to an event. Event will be set to be deleted. Event UUID: " + getEventUUID().toString());
            toBeRemoved = true;
        }
    }

    public Event(ResourceKey<Level> dimension, Difficulty difficultyRequired) {
        this(dimension);
        minimumDifficulty = difficultyRequired;
    }

    // Getters and Setters
    public UUID getEventUUID() {
        return eventUUID;
    }

    public Difficulty getMinimumDifficulty() {
        return minimumDifficulty;
    }

    public void setWasLoadedFromSaveData(boolean value)
    {
        wasLoadedFromSaveData = value;
    }

    public boolean wasLoadedFromSaveData()
    {
        return wasLoadedFromSaveData;
    }

    // Logic

    public boolean canStart() {
        if(getDimension() == null)
        {
            return false;
        }

        if(getDimension().getGameTime() - lastTimeOfCanStartCheck < CAN_START_CHECK_INTERVAL)
        {
            return false;
        }
        lastTimeOfCanStartCheck = getDimension().getGameTime();

        //boolean hasEnoughTimePassed = getDimension().getGameTime() - lastGameTimeOfEventExecution >= EXECUTION_COOLDOWN;
        boolean hasEnoughTimePassed = TickUnits.hasTicksPassed(lastGameTimeOfEventExecution, getDimension(), EXECUTION_COOLDOWN);

        if(!hasEnoughTimePassed)
        {
            return false;
        }

        if(!DifficultyUtil.isCurrentDifficultyEqualToOrGreaterThan(minimumDifficulty))
        {
            return false;
        }

        return true;
    }

    public boolean canContinue() {
        return false;
    }

    public void start() {
        ModSavedData.getSaveData().subtractSculkAccumulatedMass(eventCost);
        setEventActive(true);
    }

    public void serverTick() {

    }

    public void markEventAsFinished() {
        if (!isEventReocurring) {
            toBeRemoved = true;
        }

        setEventActive(false);
        setLastGameTimeOfEventExecution(getDimension().getGameTime());
        DebuggerSystem.eventDebuggerModule.logInfo("Finished event " + getClass().getSimpleName() + " with ID: " + getEventUUID());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Event.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        return eventUUID == ((Event) obj).eventUUID;
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

    public Event setMinimumDifficulty(Difficulty difficulty) {
        minimumDifficulty = difficulty;
        return this;
    }

    public Event setMinimumDifficulty(int difficulty) {
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

        if(tag.contains("eventID"))
        {
            event.setEventUUID(tag.getUUID("eventID"));
        }
        if(tag.contains("eventCost"))
        {
            event.setEventCost(tag.getInt("eventCost"));
        }
        if(tag.contains(Difficulty.class.getSimpleName()))
        {
            event.setMinimumDifficulty(tag.getInt(Difficulty.class.getSimpleName()));
        }
        if(tag.contains("EXECUTION_COOLDOWN"))
        {
            event.setEXECUTION_COOLDOWN(tag.getLong("EXECUTION_COOLDOWN"));
        }
        if(tag.contains("lastGameTimeOfEventExecution"))
        {
            event.setLastGameTimeOfEventExecution(tag.getLong("lastGameTimeOfEventExecution"));
        }
        if(tag.contains("isEventReoccurring"))
        {
            event.setEventReocurring(tag.getBoolean("isEventReoccurring"));
        }
        if(tag.contains("isEventActive"))
        {
            event.setEventActive(tag.getBoolean("isEventActive"));
        }
        if(tag.contains("toBeRemoved"))
        {
            event.setToBeRemoved(tag.getBoolean("toBeRemoved"));
        }
        if(tag.contains("eventLocation"))
        {
            event.setEventLocation(BlockPos.of(tag.getLong("eventLocation")));
        }
        if(tag.contains("dimension"))
        {
            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")));
            event.setDimension(dimensionResourceKey);
        }
        else
        {
            DebuggerSystem.eventDebuggerModule.logError(event.getClass().getSimpleName() + " | Attempted to load event from save data with no Dimension.");
            event.setToBeRemoved(true);
        }

    }
}
