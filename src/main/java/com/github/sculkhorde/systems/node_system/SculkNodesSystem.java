package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.event_system.events.SpawnPhantomsEvent;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.server.ServerLifecycleHooks;

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

    protected Optional<ModSavedData.NodeEntry> getNodeWithLongestTimeOfInactivity() {
        ModSavedData.NodeEntry bestCandidate = null;
        long longestInactivity = -1L;

        for (ModSavedData.NodeEntry node : getNodes()) {
            if (node.isActive() || node.getDimension() == null || !node.isEntryValid()) {
                continue;
            }

            // Treat never-activated nodes (lastTimeWasActive == 0) as maximum priority
            if (node.getLastTimeWasActive() == 0) {
                return Optional.of(node);
            }

            long inactivity = node.getDimension().getGameTime() - node.getLastTimeWasActive();
            if (inactivity > longestInactivity) {
                longestInactivity = inactivity;
                bestCandidate = node;
            }
        }

        return Optional.ofNullable(bestCandidate);
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


    public void activateNodeWithLongestDurationOfInactivity()
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

    public void deactivateAllNodes()
    {
        getNodes().removeIf(node -> {
            if(node.getDimension() == null)
            {
                SculkHorde.LOGGER.warn("Removing Node at: " + node.getPosition().toString() + " due to it being in a null dimension.");
                return true;
            }

            return false;
        });

        for(ModSavedData.NodeEntry node : getNodes())
        {
            if(!node.isActive()) { continue; }
            node.setActive(false);
            node.setLastTimeWasActive(node.getDimension().getGameTime());
            DebuggerSystem.eventDebuggerModule.logInfo("Deactivating Node at: " + node.getPosition().toString());
        }
    }

    public void activateAllNodes()
    {
        getNodes().removeIf(node -> {
            if(node.getDimension() == null)
            {
                SculkHorde.LOGGER.warn("Removing Node at: " + node.getPosition().toString() + " due to it being in a null dimension.");
                return true;
            }

            return false;
        });

        for(ModSavedData.NodeEntry node : getNodes())
        {
            if(node.isActive()) { continue; }
            node.setActive(true);
            node.setLastTimeWasActive(node.getDimension().getGameTime());
            DebuggerSystem.eventDebuggerModule.logInfo("Activating Node at: " + node.getPosition().toString());
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

        boolean isThereMoreNodesThanMaxActiveNodes = getNodes().size() > SculkHorde.autoPerformanceSystem.getMaxNodesActive();

        boolean hasAnyNodeBeenActiveForTooLong = hasAnyNodeBeenActiveForTooLong();

        boolean areAllNodesInactive = areAllNodesInactive();

        if((hasAnyNodeBeenActiveForTooLong && isThereMoreNodesThanMaxActiveNodes) || areAllNodesInactive)
        {
            deactivateAllNodes();
            for(int i = 0; i < SculkHorde.autoPerformanceSystem.getMaxNodesActive(); i++)
            {
                activateNodeWithLongestDurationOfInactivity();
            }
        }
    }

}
