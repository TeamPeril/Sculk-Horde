package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.event_system.events.SpawnPhantomsEvent;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Optional;

public class SculkNodesSystem {

    protected boolean isActive = false;
    protected long TICK_COOLDOWN = TickUnits.convertMinutesToTicks(5);
    protected long lastTimeSinceTick = 0;
    protected boolean cleanUpRequired = false;
    protected long timeOfLastValidation = 0;
    protected final long VALIDATION_INTERVAL = TickUnits.convertMinutesToTicks(1);
    public long timeOfLastNodeMove = 0;
    public final long NODE_RELOCATION_COOLDOWN = TickUnits.convertMinutesToTicks(30);


    public SculkNodesSystem() {
        isActive = true;
    }

    public void flagCleanUpRequired()
    {
        cleanUpRequired = true;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive;
    }

    protected ServerLevel getLevel() {
        return ServerLifecycleHooks.getCurrentServer().overworld();
    }

    protected ArrayList<ModSavedData.NodeEntry> getNodes() {
        return ModSavedData.getSaveData().getNodeEntries();
    }

    protected Optional<ModSavedData.NodeEntry> getNodeWithLongestTimeOfInactivity()
    {
        Optional<ModSavedData.NodeEntry> nodeWithLongestTimeOfInactivity = Optional.empty();
        for(ModSavedData.NodeEntry node : getNodes())
        {
            ServerLevel dimension = node.getDimension();
            // This is likely due to an old world that was created before multi-dimensional support was added.
            if(dimension == null)
            {
                ModSavedData.NodeEntry nodeToRemove = node;
                SculkHorde.LOGGER.warn("Removing Node at: " + nodeToRemove.getPosition().toString() + " due to it being in a null dimension.");
                getNodes().remove(nodeToRemove);
                continue;

            }

            long currentTime = node.getDimension().getGameTime();
            long currentNodeDurationOfInactivity =  currentTime - node.getLastTimeWasActive();
            long nodeWithLongestTimeOfInactivityDuration = nodeWithLongestTimeOfInactivity.isEmpty() ? 0 : currentTime - nodeWithLongestTimeOfInactivity.get().getLastTimeWasActive();

            boolean hasCurrentNodeBeenInactiveForLonger = currentNodeDurationOfInactivity > nodeWithLongestTimeOfInactivityDuration;
            boolean hasCurrentNodeNeverBeenActive = node.getLastTimeWasActive() == 0;

            if(nodeWithLongestTimeOfInactivity.isEmpty())
            {
                nodeWithLongestTimeOfInactivity = Optional.of(node);
            }
            else if((hasCurrentNodeBeenInactiveForLonger || hasCurrentNodeNeverBeenActive) && !node.isActive())
            {
                nodeWithLongestTimeOfInactivity = Optional.of(node);
            }
        }
        return nodeWithLongestTimeOfInactivity;
    }

    protected boolean hasAnyNodeBeenActiveForTooLong()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            ServerLevel dimension = node.getDimension();
            // This is likely due to an old world that was created before multi-dimensional support was added.
            if(dimension == null)
            {
                ModSavedData.NodeEntry nodeToRemove = node;
                SculkHorde.LOGGER.warn("Removing Node at: " + nodeToRemove.getPosition().toString() + " due to it being in a null dimension.");
                getNodes().remove(nodeToRemove);
                continue;

            }

            long currentNodeDurationOfInactivity =  node.getDimension().getGameTime() - node.getActivationTimeStamp();
            if(currentNodeDurationOfInactivity > TickUnits.convertHoursToTicks(1) && node.isActive())
            {
                return true;
            }
        }
        return false;
    }

    protected boolean areAllNodesInactive()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            if(node.isActive())
            {
                return false;
            }
        }
        return true;
    }


    public void ActivateNodeWithLongestDurationOfInactivity()
    {
        Optional<ModSavedData.NodeEntry> nodeWithLongestTimeOfInactivity = getNodeWithLongestTimeOfInactivity();

        if(nodeWithLongestTimeOfInactivity.isEmpty())
        {
            return;
        }


        if(!nodeWithLongestTimeOfInactivity.get().isEntryValid()) { return; }
        nodeWithLongestTimeOfInactivity.get().setActive(true);
        nodeWithLongestTimeOfInactivity.get().setActivationTimeStamp(nodeWithLongestTimeOfInactivity.get().getDimension().getGameTime());
        DebuggerSystem.eventDebuggerModule.logInfo("Activating Node at: " + nodeWithLongestTimeOfInactivity.get().getPosition().toString());

        SpawnPhantomsEvent phantomEvent = new SpawnPhantomsEvent(nodeWithLongestTimeOfInactivity.get().getDimension().dimension());
        phantomEvent.setEventLocation(nodeWithLongestTimeOfInactivity.get().getPosition());
        SculkHorde.eventSystem.addEvent(phantomEvent);
    }

    public void DeactivateAllNodes()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            ServerLevel dimension = node.getDimension();
            // This is likely due to an old world that was created before multi-dimensional support was added.
            if(dimension == null)
            {
                ModSavedData.NodeEntry nodeToRemove = node;
                SculkHorde.LOGGER.warn("Removing Node at: " + nodeToRemove.getPosition().toString() + " due to it being in a null dimension.");
                getNodes().remove(nodeToRemove);
                continue;

            }

            if(!node.isActive()) { continue; }
            node.setActive(false);
            node.setLastTimeWasActive(node.getDimension().getGameTime());
            DebuggerSystem.eventDebuggerModule.logInfo("Deactivating Node at: " + node.getPosition().toString());
        }
    }

    public void ActivateAllNodes()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            ServerLevel dimension = node.getDimension();
            // This is likely due to an old world that was created before multi-dimensional support was added.
            if(dimension == null)
            {
                ModSavedData.NodeEntry nodeToRemove = node;
                SculkHorde.LOGGER.warn("Removing Node at: " + nodeToRemove.getPosition().toString() + " due to it being in a null dimension.");
                getNodes().remove(nodeToRemove);
                continue;

            }

            if(!node.isActive()) { continue; }
            node.setActive(true);
            node.setLastTimeWasActive(node.getDimension().getGameTime());
            DebuggerSystem.eventDebuggerModule.logInfo("Deactivating Node at: " + node.getPosition().toString());
        }
    }

    public void tick()
    {
        boolean isSculkNodeHandlerNotActive = !isActive();
        boolean isSaveDataNull = ModSavedData.getSaveData() == null;
        long timeElapsedSinceLastTick = getLevel().getGameTime() - lastTimeSinceTick;
        boolean isCooldownStillActive = timeElapsedSinceLastTick < TICK_COOLDOWN && !cleanUpRequired;
        boolean areThereNoNodes = getNodes().isEmpty();
        boolean isHordeDeactivated = !ModSavedData.getSaveData().isHordeActive();

        if(isHordeDeactivated || isSculkNodeHandlerNotActive || isSaveDataNull || areThereNoNodes || isCooldownStillActive)
        {
            return;
        }

        lastTimeSinceTick = getLevel().getGameTime();

        // If it is time to clean up, clean.
        if(TickUnits.hasTicksPassed(timeOfLastValidation, getLevel(), VALIDATION_INTERVAL) || cleanUpRequired)
        {
            ModSavedData.getSaveData().cleanUpNodeEntries();
            timeOfLastValidation = getLevel().getGameTime();
            cleanUpRequired = false;
        }

        boolean isThereMoreNodesThanMaxActiveNodes = getNodes().size() > ModConfig.SERVER.max_nodes_active.get();

        boolean hasAnyNodeBeenActiveForTooLong = hasAnyNodeBeenActiveForTooLong();

        boolean areAllNodesInactive = areAllNodesInactive();

        if((hasAnyNodeBeenActiveForTooLong && isThereMoreNodesThanMaxActiveNodes) || areAllNodesInactive)
        {
            DeactivateAllNodes();
            for(int i = 0; i < SculkHorde.autoPerformanceSystem.getMaxNodesActive(); i++)
            {
                ActivateNodeWithLongestDurationOfInactivity();
            }
        }
    }

}
