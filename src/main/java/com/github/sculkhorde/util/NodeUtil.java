package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NodeUtil {

    public static Optional<ModSavedData.NodeEntry> getClosestNode(ServerLevel dimension, BlockPos pos) {
        Optional<ModSavedData.NodeEntry> closestNode = Optional.empty();
        double closestDistance = Double.MAX_VALUE;

        for (ModSavedData.NodeEntry node : ModSavedData.getSaveData().getNodeEntries()) {
            if(!node.isEntryValid())
            {
                continue;
            }

            if (pos.distSqr(node.getPosition()) < closestDistance && node.getDimension().equals(dimension)) {
                closestNode = Optional.of(node);
                closestDistance = pos.distSqr(node.getPosition());
            }
        }
        return closestNode;
    }

    public static List<ModSavedData.NodeEntry> getActiveNodes()
    {
        List<ModSavedData.NodeEntry> activeNodes = new ArrayList<>();
        for (ModSavedData.NodeEntry node : ModSavedData.getSaveData().getNodeEntries()) {
            if(node.isEntryValid() && node.isActive())
            {
                activeNodes.add(node);
            }
        }
        return activeNodes;
    }

    public static Optional<ModSavedData.NodeEntry> getRandomActiveNode(ServerLevel level)
    {
        List<ModSavedData.NodeEntry> activeNodes = getActiveNodes();
        if(activeNodes.isEmpty())
        {
            return Optional.empty();
        }
        else
        {
            int randomIndex = level.getRandom().nextInt(activeNodes.size());
            return Optional.of(activeNodes.get(randomIndex));
        }
    }
}
