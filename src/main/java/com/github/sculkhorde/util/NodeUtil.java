package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
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

    public static List<ModSavedData.NodeEntry> getInactiveNodes()
    {
        List<ModSavedData.NodeEntry> nodes = new ArrayList<>();
        for (ModSavedData.NodeEntry node : ModSavedData.getSaveData().getNodeEntries()) {
            if(node.isEntryValid() && !node.isActive())
            {
                nodes.add(node);
            }
        }
        return nodes;
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

    public static Optional<SculkNodeBlockEntity> getNodeBlockEntity(ModSavedData.NodeEntry nodeEntry)
    {
        return getNodeBlockEntity(nodeEntry.getDimension(), nodeEntry.getPosition());
    }

    public static Optional<SculkNodeBlockEntity> getNodeBlockEntity(ServerLevel level, BlockPos pos)
    {
        return level.getBlockEntity(pos, ModBlockEntities.SCULK_NODE_BLOCK_ENTITY.get());
    }

    public static long getNodeAgeTicks(ModSavedData.NodeEntry nodeEntry)
    {
        long currentTime = nodeEntry.getDimension().getGameTime();
        Optional<SculkNodeBlockEntity> blockEntity = getNodeBlockEntity(nodeEntry);

        if(!nodeEntry.isEntryValid() || blockEntity.isEmpty())
        {
            SculkHorde.LOGGER.error("getNodeAgeTicks | Node was invalid at " + nodeEntry.getDimension().toString() + " | " + nodeEntry.getPosition().toShortString());
            return 0;
        }

        long nodeCreationTime = blockEntity.get().getCreationTime();
        long ageTicks = currentTime - nodeCreationTime;
        return ageTicks;
    }

    public static boolean canMoveNode(ModSavedData.NodeEntry node)
    {
        if(!node.isEntryValid())
        {
            return false;
        }
        else if(getNodeBlockEntity(node).isEmpty())
        {
            return false;
        }
        else if(getNodeBlockEntity(node).get().isActive())
        {
            return false;
        }
        else if(node.getLastTimeWasActive() == 0)
        {
            return false;
        }
        else if(getNodeAgeTicks(node) <= TickUnits.convertHoursToTicks(2))
        {
            return false;
        }

        return true;
    }

    public static Optional<ModSavedData.NodeEntry> getNextNodeToMove(boolean ignoreRequirements)
    {
        if(getInactiveNodes().isEmpty())
        {
            return Optional.empty();
        }

        ModSavedData.NodeEntry oldest = null;

        // Get oldest notest
        for(ModSavedData.NodeEntry node : getInactiveNodes())
        {
            if((oldest == null || getNodeAgeTicks(node) > getNodeAgeTicks(oldest)) && (canMoveNode(node) || ignoreRequirements))
            {
                oldest = node;
            }
        }

        if(oldest == null)
        {
            return Optional.empty();
        }

        return Optional.of(oldest);
    }

    public static void tryMoveOldestNodeTo(ServerLevel level, BlockPos pos, boolean ignoreRequirements)
    {

        if(!ignoreRequirements && !TickUnits.hasTicksPassed(SculkHorde.sculkNodesSystem.timeOfLastNodeMove, level, SculkHorde.sculkNodesSystem.NODE_RELOCATION_COOLDOWN))
        {
            return;
        }

        if(!ModConfig.SERVER.enable_node_relocation.get())
        {
            return;
        }

        Optional<ModSavedData.NodeEntry> nodeToMove = getNextNodeToMove(ignoreRequirements);

        if(nodeToMove.isEmpty() || getNodeBlockEntity(nodeToMove.get()).isEmpty() || (!SculkNodeBlock.isValidPositionForSculkNode(level, pos) && !ignoreRequirements))
        {
            return;
        }

        SculkHorde.sculkNodesSystem.timeOfLastNodeMove = level.getGameTime();
        getNodeBlockEntity(nodeToMove.get()).get().isBeingMoved = true;
        level.destroyBlock(nodeToMove.get().getPosition(), false);

        SculkNodeBlock.PlaceNode(level, pos, true);
    }

}
