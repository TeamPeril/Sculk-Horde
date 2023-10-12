package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;

public class SculkNodesHandler {

    protected boolean isActive = false;

    //protected long TICK_COOLDOWN = TickUnits.convertMinutesToTicks(5);
    protected long TICK_COOLDOWN = TickUnits.convertMinutesToTicks(1);
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


    protected ModSavedData getSavedData() {
        return SculkHorde.savedData;
    }

    protected ServerLevel getLevel() {
        return getSavedData().level;
    }

    protected ArrayList<ModSavedData.NodeEntry> getNodes() {
        return getSavedData().getNodeEntries();
    }

    protected ModSavedData.NodeEntry getNodeWithLongestTimeOfInactivity()
    {
        ModSavedData.NodeEntry nodeWithLongestTimeOfInactivity = null;
        for(ModSavedData.NodeEntry node : getNodes())
        {
            long currentTime = getLevel().getGameTime();
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
            long currentNodeDurationOfInactivity =  getLevel().getGameTime() - node.getActivationTimeStamp();
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
        nodeWithLongestTimeOfInactivity.setActivationTimeStamp(getLevel().getGameTime());
        SculkHorde.LOGGER.info("Activating Node at: " + nodeWithLongestTimeOfInactivity.getPosition().toString());
    }

    protected void DeactivateAllNodes()
    {
        for(ModSavedData.NodeEntry node : getNodes())
        {
            if(!node.isActive()) { continue; }
            node.setActive(false);
            node.setLastTimeWasActive(getLevel().getGameTime());
            SculkHorde.LOGGER.info("Deactivating Node at: " + node.getPosition().toString());
        }
    }

    public void tick()
    {
        boolean isSaveDataNull = getSavedData() == null;
        if(!isActive() || isSaveDataNull || getNodes().isEmpty() || getLevel().getGameTime() - lastTimeSinceTick < TICK_COOLDOWN)
        {
            return;
        }
        lastTimeSinceTick = getLevel().getGameTime();

        int maxActiveNodes = 1;

        if((hasAnyNodeBeenActiveForTooLong() && getNodes().size() > maxActiveNodes) || (areAllNodesInactive() && getNodes().size() >= maxActiveNodes))
        {
            for(int i = 0; i < maxActiveNodes; i++)
            {
                DeactivateAllNodes();
                ActivateNodeWithLongestDurationOfInactivity();
            }
        }
    }

}
