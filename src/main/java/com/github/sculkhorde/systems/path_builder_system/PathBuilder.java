package com.github.sculkhorde.systems.path_builder_system;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.BlockAlgorithms.isCubeReplaceable;

public class PathBuilder {
    // A* node wrapper to track cost information
    private static class AStarNode implements Comparable<AStarNode> {
        BlockPos pos;
        double g; // cost from start
        double h; // heuristic to goal
        double f; // g + h
        AStarNode parent;

        AStarNode(BlockPos pos, double g, double h, AStarNode parent) {
            this.pos = pos;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }

        @Override
        public int compareTo(AStarNode other) {
            return Double.compare(this.f, other.f);
        }
    }

    private final PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
    private final Set<Long> closedSet = new HashSet<>();
    private final Map<Long, AStarNode> allNodes = new HashMap<>();
    private ArmorStand debugStand;
    private int MAX_DISTANCE = 150;
    private int nodesSearched = 0;
    private static final int MAX_SEARCH_NODES = 10000;

    protected Optional<PathBuilderRequest> request = Optional.empty();
    protected boolean foundTarget = false;

    public final UUID uuid;

    protected long timeOfLastCompletion = 0;

    public PathBuilder() {
        uuid = UUID.randomUUID();
    }

    public PathBuilder(UUID uuidIn) {
        uuid = uuidIn;
    }

    protected Optional<PathBuilderRequest> getCurrentRequest()
    {
        return request;
    }

    protected double getHeuristic(BlockPos pos) {
        if(request.isEmpty())
        {
            DebuggerSystem.eventDebuggerModule.logError("PathBuilderSystem | Attempted to getHeuristic for non-existent request.");
            return 0;
        }

        BlockPos goal = request.get().getDesiredDestination();
        double dx = Math.abs(pos.getX() - goal.getX());
        double dy = Math.abs(pos.getY() - goal.getY());
        double dz = Math.abs(pos.getZ() - goal.getZ());

        // Manhattan Distance: dx + dy + dz
        return dx + dy + dz;
    }

    protected boolean isEmpty()
    {
        return request.isEmpty();
    }

    protected boolean isWorking()
    {
        if(isEmpty())
        {
            return false;
        }

        return request.get().isPathBuildingInProgress;
    }

    protected boolean isFinished()
    {
        if(isEmpty())
        {
            return false;
        }

        return request.get().hasPathBuildingStarted && !request.get().isPathBuildingInProgress;
    }

    protected boolean isExpired()
    {
       if(isEmpty()) { return true; }

        return Math.abs(ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - timeOfLastCompletion) >= TickUnits.convertMinutesToTicks(15);
    }

    public final Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        if(isCubeReplaceable(request.get().level, blockPos, 5))
        {
            return true;
        }

        return false;
    };

    protected Predicate<BlockPos> isValidTargetBlock  = (blockPos) ->
    {
        // First check proximity
        if(BlockAlgorithms.getBlockDistance(blockPos, request.get().desiredDestination) > request.get().requiredProximityToDesiredLocation)
        {
            return false;
        }

        // Then validate using the request's custom isValidTarget predicate if provided
        if(request.get().isValidTargetBlock != null && !request.get().isValidTargetBlock.test(blockPos))
        {
            return false;
        }

        return true;
    };

    protected void initializationTick()
    {
        PathBuilderRequest currentRequest = request.get();
        currentRequest.hasPathBuildingStarted = true;
        currentRequest.isPathBuildingInProgress = true;
        currentRequest.isSearching = true;

        // Clear A* data structures
        openSet.clear();
        closedSet.clear();
        allNodes.clear();
        nodesSearched = 0;

        // Initialize start node
        BlockPos startPos = currentRequest.startLocation;
        double startH = getHeuristic(startPos);
        AStarNode startNode = new AStarNode(startPos, 0, startH, null);
        openSet.add(startNode);
        allNodes.put(startPos.asLong(), startNode);

        DebuggerSystem.eventDebuggerModule.logDebug("PathBuilder | A* Path Builder Initialized at " + startPos.toShortString());
        setMaxDistance((int) (BlockAlgorithms.getBlockDistance(currentRequest.startLocation, currentRequest.desiredDestination) * 1.25F));
    }

    protected void processingTick()
    {
        PathBuilderRequest currentRequest = request.get();

        if (openSet.isEmpty()) {

            DebuggerSystem.eventDebuggerModule.logDebug("PathBuilder | Open set is empty. No path found.");
            currentRequest.isSearching = false;
            return;
        }

        // Safety check: abort if we've searched too many nodes
        if (nodesSearched >= MAX_SEARCH_NODES) {
            DebuggerSystem.eventDebuggerModule.logDebug("PathBuilder | Max search nodes (" + MAX_SEARCH_NODES + ") exceeded. Aborting.");
            currentRequest.isSearching = false;
            return;
        }

        // Spawn Debug Stand if Necessary
        if(debugStand == null && DebuggerSystem.eventDebuggerModule.isDebuggingEnabled())
        {
            debugStand = new ArmorStand(currentRequest.getLevel(), currentRequest.getStartLocation().getX(), currentRequest.getStartLocation().getY(), currentRequest.getStartLocation().getZ());
            debugStand.setInvisible(true);
            debugStand.setNoGravity(true);
            debugStand.addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 3));
            currentRequest.getLevel().addFreshEntity(debugStand);
        }

        // Pop best node from open set
        AStarNode current = openSet.poll();
        if (current == null) return;

        closedSet.add(current.pos.asLong());
        nodesSearched++;

        if(DebuggerSystem.eventDebuggerModule.isDebuggingEnabled() && debugStand != null)
        {
            debugStand.teleportTo(current.pos.getX() + 0.5, current.pos.getY(), current.pos.getZ() + 0.5);
        }

        // Check if we reached a valid target
        if (isValidTargetBlock.test(current.pos))
        {

            DebuggerSystem.eventDebuggerModule.logDebug("PathBuilder | Found valid target at " + current.pos.toShortString() + " (g-cost: " + current.g + ")");

            currentRequest.setPath(reconstructPath(current));
            currentRequest.isPathBuildSuccessful = true;
            currentRequest.isSearching = false;
            return;
        }

        // Expand neighbors
        for (BlockPos neighbor : BlockAlgorithms.getNeighborsCube(current.pos, false)) {
            long neighborKey = neighbor.asLong();

            // Skip if already in closed set
            if (closedSet.contains(neighborKey)) {
                continue;
            }

            // Skip if obstructed
            if (isObstructed.test(neighbor)) {
                continue;
            }

            // Skip if too far from start
            if(BlockAlgorithms.getBlockDistance(neighbor, currentRequest.startLocation) > MAX_DISTANCE)
            {
                continue;
            }

            // Calculate g-cost for this neighbor (uniform cost: 1.0 per move)
            double tentativeG = current.g + 1.0;

            // Check if we've seen this neighbor before
            AStarNode existingNode = allNodes.get(neighborKey);

            if (existingNode == null) {
                // New node: create it with the calculated g-cost
                double h = getHeuristic(neighbor);
                AStarNode newNode = new AStarNode(neighbor, tentativeG, h, current);
                allNodes.put(neighborKey, newNode);
                openSet.add(newNode);
            } else if (tentativeG < existingNode.g) {
                // Better path found: update the node
                existingNode.g = tentativeG;
                existingNode.f = tentativeG + existingNode.h;
                existingNode.parent = current;
                // Re-insert to update priority queue ordering
                openSet.remove(existingNode);
                openSet.add(existingNode);
            }
        }
    }

    protected void finishedTick()
    {
        timeOfLastCompletion = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
        request.get().isPathBuildingInProgress = false;

        // Clean up debug stand to prevent memory leak
        if (debugStand != null) {
            debugStand.discard();
            debugStand = null;
        }

        if(request.get().isPathBuildSuccessful())
        {
            DebuggerSystem.eventDebuggerModule.logInfo("PathBuilder | Path Built Successfully (nodes searched: " + nodesSearched + ")");
            return;
        }
        DebuggerSystem.eventDebuggerModule.logInfo("PathBuilder | Path Not Built (nodes searched: " + nodesSearched + ")");
    }

    public void serverTick()
    {
        if(request.isEmpty())
        {
            return;
        }
        else if(!request.get().hasPathBuildStarted())
        {
            initializationTick();
        }
        else if(request.get().isPathBuildingInProgress() && request.get().isSearching())
        {
            processingTick();
        }
        else if(request.get().isPathBuildingInProgress())
        {
            finishedTick();
        }
    }

    private List<BlockPos> reconstructPath(AStarNode endNode) {
        List<BlockPos> path = new ArrayList<>();
        AStarNode current = endNode;
        while (current != null) {
            path.add(0, current.pos);
            current = current.parent;
        }
        return path;
    }

    public void setMaxDistance(int value) {
        MAX_DISTANCE = value;
    }

}

