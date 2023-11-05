package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class SculkNodesHandler {

    protected boolean isActive = false;

    //protected long TICK_COOLDOWN = TickUnits.convertMinutesToTicks(5);
    protected long TICK_COOLDOWN = TickUnits.convertSecondsToTicks(10);
    protected long lastTimeSinceTick = 0;


    public SculkNodesHandler() {
        isActive = true;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public boolean isActive() {
        return this.isActive;
    }

    protected ServerLevel getLevel() {
        return getSavedData().level;
    }

    protected ModSavedData getSavedData() {
        return SculkHorde.savedData;
    }

    protected ArrayList<ModSavedData.NodeEntry> getNodes() {
        return getSavedData().getNodeEntries();
    }

    protected ModSavedData.NodeEntry getNodeWithLongestTimeOfInactivity()
    {
        ModSavedData.NodeEntry nodeWithLongestTimeOfInactivity = null;
        for(ModSavedData.NodeEntry node : getNodes())
        {
            long currentTime = node.getDimension().getGameTime();
            long currentNodeDurationOfInactivity =  currentTime - node.getLastTimeWasActive();
            long nodeWithLongestTimeOfInactivityDuration = nodeWithLongestTimeOfInactivity == null ? 0 : currentTime - nodeWithLongestTimeOfInactivity.getLastTimeWasActive();

            boolean hasCurrentNodeBeenInactiveForLonger = currentNodeDurationOfInactivity > nodeWithLongestTimeOfInactivityDuration;
            boolean hasCurrentNodeNeverBeenActive = node.getLastTimeWasActive() == 0;

            if(nodeWithLongestTimeOfInactivity == null)
            {
                nodeWithLongestTimeOfInactivity = node;
            }
            else if((hasCurrentNodeBeenInactiveForLonger || hasCurrentNodeNeverBeenActive) && !node.isActive())
            {
                nodeWithLongestTimeOfInactivity = node;
            }
        }
        return nodeWithLongestTimeOfInactivity;
    }

    protected boolean hasAnyNodeBeenActiveForTooLong()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
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


    protected void ActivateNodeWithLongestDurationOfInactivity()
    {
        ModSavedData.NodeEntry nodeWithLongestTimeOfInactivity = getNodeWithLongestTimeOfInactivity();
        nodeWithLongestTimeOfInactivity.setActive(true);
        nodeWithLongestTimeOfInactivity.setActivationTimeStamp(nodeWithLongestTimeOfInactivity.getDimension().getGameTime());
        SculkHorde.LOGGER.info("Activating Node at: " + nodeWithLongestTimeOfInactivity.getPosition().toString());
    }

    protected void DeactivateAllNodes()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            if(!node.isActive()) { continue; }
            node.setActive(false);
            node.setLastTimeWasActive(node.getDimension().getGameTime());
            SculkHorde.LOGGER.info("Deactivating Node at: " + node.getPosition().toString());
        }
    }

    public void tick()
    {
        boolean isSculkNodeHandlerNotActive = !isActive();
        boolean isSaveDataNull = getSavedData() == null;
        long timeElapsedSinceLastTick = getLevel().getGameTime() - lastTimeSinceTick;
        boolean isCooldownStillActive = timeElapsedSinceLastTick < TICK_COOLDOWN;
        boolean areThereNoNodes = getNodes().isEmpty();

        if(isSculkNodeHandlerNotActive || isSaveDataNull || areThereNoNodes || isCooldownStillActive)
        {
            return;
        }
        lastTimeSinceTick = getLevel().getGameTime();

        int maxActiveNodes = 1;

        boolean isThereMoreNodesThanMaxActiveNodes = getNodes().size() > maxActiveNodes;

        boolean hasAnyNodeBeenActiveForTooLong = hasAnyNodeBeenActiveForTooLong();

        boolean areAllNodesInactive = areAllNodesInactive();

        if((hasAnyNodeBeenActiveForTooLong && isThereMoreNodesThanMaxActiveNodes) || areAllNodesInactive)
        {
            for(int i = 0; i < maxActiveNodes; i++)
            {
                DeactivateAllNodes();
                ActivateNodeWithLongestDurationOfInactivity();
            }
        }
    }

}
