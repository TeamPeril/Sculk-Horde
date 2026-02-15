package com.github.sculkhorde.systems.path_builder_system;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class PathBuilderSystem {
    private final ConcurrentHashMap<UUID, PathBuilderRequest> pathBuilderRequests;
    private final ConcurrentHashMap<UUID, PathBuilder> pathBuilders;

    private long lastGameTimeOfExecution;
    private final long EXECUTION_COOLDOWN_TICKS;
    private final int MAX_ACTIVE_BUILDERS;
    private final int MAX_QUEUED_REQUESTS;

    public PathBuilderSystem() {
        this(TickUnits.convertSecondsToTicks(0.5F), 3, 100);
    }

    public PathBuilderSystem(long executionCooldownTicks, int maxActiveBuilders, int maxQueuedRequests) {
        this.pathBuilderRequests = new ConcurrentHashMap<>();
        this.pathBuilders = new ConcurrentHashMap<>();
        this.EXECUTION_COOLDOWN_TICKS = executionCooldownTicks;
        this.MAX_ACTIVE_BUILDERS = maxActiveBuilders;
        this.MAX_QUEUED_REQUESTS = maxQueuedRequests;
    }

    public Map<UUID, PathBuilderRequest> getPathBuilderRequests() {
        return pathBuilderRequests;
    }

    public Map<UUID, PathBuilder> getPathBuilders() {
        return pathBuilders;
    }

    public boolean canExecute() {
        boolean isHordeActive = ModSavedData.getSaveData().isHordeActive();
        return isHordeActive && (ServerLifecycleHooks.getCurrentServer().overworld().getGameTime() - lastGameTimeOfExecution) > EXECUTION_COOLDOWN_TICKS;
    }

    public PathBuilderRequest getPathBuilderRequest(UUID uuid) {
        return pathBuilderRequests.get(uuid);
    }

    public PathBuilder getPathBuilder(UUID uuid) {
        return pathBuilders.get(uuid);
    }

    public boolean hasPathBuilderRequest(UUID uuid) {
        return pathBuilderRequests.containsKey(uuid);
    }

    public boolean hasPathBuilder(UUID uuid) {
        return pathBuilders.containsKey(uuid);
    }

    public void addPathBuilder(PathBuilder pathBuilder) {
        if (pathBuilder == null) {
            SculkHorde.LOGGER.warn("Attempted to add null PathBuilder to system");
            return;
        }
        if (!pathBuilders.containsKey(pathBuilder.uuid)) {
            pathBuilders.put(pathBuilder.uuid, pathBuilder);
            SculkHorde.LOGGER.debug("Added pathBuilder {} with ID: {}", pathBuilder.getClass().getSimpleName(), pathBuilder.uuid);
        }
    }

    public void addPathBuilderRequest(PathBuilderRequest pathBuilderRequest) {
        if (pathBuilderRequest == null) {
            SculkHorde.LOGGER.warn("Attempted to add null PathBuilderRequest to system");
            return;
        }
        // Enforce request queue size limit to prevent unbounded growth
        if (pathBuilderRequests.size() >= MAX_QUEUED_REQUESTS) {
            SculkHorde.LOGGER.warn("PathBuilderRequest queue at max capacity ({}). Dropping oldest request.", MAX_QUEUED_REQUESTS);
            // Remove the first entry (oldest) from the concurrent map
            pathBuilderRequests.keySet().stream().findFirst().ifPresent(pathBuilderRequests::remove);
        }
        if (!pathBuilderRequests.containsKey(pathBuilderRequest.uuid)) {
            pathBuilderRequests.put(pathBuilderRequest.uuid, pathBuilderRequest);
            SculkHorde.LOGGER.debug("Added pathBuilderRequest {} with ID: {}", pathBuilderRequest.getClass().getSimpleName(), pathBuilderRequest.uuid);
        }
    }

    public void removePathBuilder(UUID uuid) {
        PathBuilder removed = pathBuilders.remove(uuid);
        if (removed != null) {
            SculkHorde.LOGGER.debug("Removed pathBuilder with ID: {}", uuid);
        }
    }

    public void removePathBuilderRequest(UUID uuid) {
        PathBuilderRequest removed = pathBuilderRequests.remove(uuid);
        if (removed != null) {
            SculkHorde.LOGGER.debug("Removed pathBuilderRequest with ID: {}", uuid);
        }
    }

    public boolean isActivePathBuildersAtMax() {
        return getActivePathBuilders() >= MAX_ACTIVE_BUILDERS;
    }

    public int getActivePathBuilders() {
        int count = 0;
        for (PathBuilder pathBuilder : pathBuilders.values()) {
            if (pathBuilder.isWorking()) {
                count++;
            }
        }
        return count;
    }

    public Optional<PathBuilderRequest> popNextPathBuilderRequest() {
        if (pathBuilderRequests.isEmpty()) {
            return Optional.empty();
        }
        UUID firstKey = pathBuilderRequests.keySet().iterator().next();
        PathBuilderRequest request = pathBuilderRequests.remove(firstKey);
        return Optional.ofNullable(request);
    }

    public void serverTick() {
        if (!canExecute()) {
            return;
        }

        lastGameTimeOfExecution = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();

        // Tick active builders and remove finished ones immediately to free slots
        Iterator<PathBuilder> iterator = pathBuilders.values().iterator();
        while (iterator.hasNext()) {
            PathBuilder currentPathBuilder = iterator.next();

            if (currentPathBuilder.isFinished()) {
                // Remove finished builders immediately, regardless of expiration
                if (currentPathBuilder.isExpired()) {
                    iterator.remove();
                }
                continue;
            }

            currentPathBuilder.serverTick();
        }

        // If there is no more room for PathBuilders or no requests queued, return
        if (isActivePathBuildersAtMax() || pathBuilderRequests.isEmpty()) {
            return;
        }

        // Create path builder if we have room
        Optional<PathBuilderRequest> nextRequest = popNextPathBuilderRequest();
        if (nextRequest.isPresent()) {
            PathBuilder pathBuilder = new PathBuilder(nextRequest.get().uuid);
            addPathBuilder(pathBuilder);
        }
    }

    public static void save(CompoundTag tag) {
        //SculkHorde.LOGGER.info("Saving " + SculkHorde.eventSystem.getEvents().size() + " events.");
        //CompoundTag eventsTag = new CompoundTag();
        //long startTime = System.currentTimeMillis();
        //tag.put("events", eventsTag);
        //SculkHorde.LOGGER.info("Saved Path Builder System. Took " + (System.currentTimeMillis() - startTime) + " Milliseconds.");
    }

    public static void load(CompoundTag tag) {

        SculkHorde.pathBuilderSystem = new PathBuilderSystem();
        SculkHorde.LOGGER.info("Loading Path Builder System.");
        SculkHorde.LOGGER.info("Loaded Path Builder System.");
    }

    /**
     * A* pathfinding routine.
     *
     * @param level           the world level (nullable if not needed)
     * @param start           start block position (inclusive)
     * @param goal            goal block position (inclusive)
     * @param isObstructed    predicate returning true if the given BlockPos is obstructed (cannot be traversed)
     * @param isValidTarget   predicate returning true if a node is a valid target (used to validate goal or candidate final nodes)
     * @param maxSearchNodes  maximum nodes to expand before giving up (safeguard)
     * @return ordered list of BlockPos from start to goal (inclusive), or empty list if no path found
     */
    public static List<BlockPos> findPathAStar(Level level,
                                               BlockPos start,
                                               BlockPos goal,
                                               Predicate<BlockPos> isObstructed,
                                               Predicate<BlockPos> isValidTarget,
                                               int maxSearchNodes) {
        // Quick checks
        if (start == null || goal == null) return new ArrayList<>();
        if (!isValidTarget.test(goal)) return new ArrayList<>();

        // Node structure for A*
        class Node {
            final BlockPos pos;
            double g; // cost from start
            double f; // g + heuristic
            Node parent;

            Node(BlockPos pos, double g, double f, Node parent) {
                this.pos = pos;
                this.g = g;
                this.f = f;
                this.parent = parent;
            }
        }

        // Heuristic: Euclidean distance (no need to sqrt for ordering, but we'll use sqrt for clearer numbers)
        java.util.function.BiFunction<BlockPos, BlockPos, Double> heuristic = (a, b) -> {
            double dx = a.getX() - b.getX();
            double dy = a.getY() - b.getY();
            double dz = a.getZ() - b.getZ();
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        };

        Comparator<Node> comparator = Comparator.comparingDouble(n -> n.f);
        PriorityQueue<Node> open = new PriorityQueue<>(comparator);
        HashSet<BlockPos> closed = new HashSet<>();
        HashMap<BlockPos, Node> allNodes = new HashMap<>();

        Node startNode = new Node(start, 0.0, heuristic.apply(start, goal), null);
        open.add(startNode);
        allNodes.put(start, startNode);

        int nodesSearched = 0;

        while (!open.isEmpty() && nodesSearched < maxSearchNodes) {
            Node current = open.poll();
            if (current == null) break;

            // If we reached the goal
            if (current.pos.equals(goal)) {
                // Reconstruct path
                ArrayList<BlockPos> path = new ArrayList<>();
                Node trace = current;
                while (trace != null) {
                    path.add(0, trace.pos); // prepend to build from start->goal
                    trace = trace.parent;
                }
                return path;
            }

            closed.add(current.pos);
            nodesSearched++;

            // Expand neighbors (6-directional)
            for (Direction dir : Direction.values()) {
                // Direction includes null/invalid? Direction.values returns 6 cardinal directions in MC (UP/DOWN/NORTH/SOUTH/EAST/WEST)
                BlockPos neighborPos = current.pos.relative(dir);

                if (closed.contains(neighborPos)) continue;
                if (isObstructed != null && isObstructed.test(neighborPos)) continue;

                // If candidate is not a valid target and it's the goal, skip this neighbor.
                if (neighborPos.equals(goal) && (isValidTarget != null && !isValidTarget.test(neighborPos))) {
                    continue;
                }

                double tentativeG = current.g + 1.0; // uniform cost for adjacent moves

                Node neighborNode = allNodes.get(neighborPos);
                boolean better = false;
                if (neighborNode == null) {
                    double h = heuristic.apply(neighborPos, goal);
                    neighborNode = new Node(neighborPos, tentativeG, tentativeG + h, current);
                    allNodes.put(neighborPos, neighborNode);
                    open.add(neighborNode);
                    better = true;
                } else if (tentativeG < neighborNode.g) {
                    // Found a better path to existing node
                    neighborNode.g = tentativeG;
                    neighborNode.f = tentativeG + heuristic.apply(neighborPos, goal);
                    neighborNode.parent = current;
                    // Reinsert to update priority queue ordering
                    open.remove(neighborNode);
                    open.add(neighborNode);
                    better = true;
                }

                // Optional: if neighbor is the goal and is valid, we could early-return here but the loop will catch it on poll.
                if (better) {
                    // debug trace (low volume)
                    //SculkHorde.LOGGER.debug("A* expanded neighbor {} (g={}, f={})", neighborPos, neighborNode.g, neighborNode.f);
                }
            }
        }

        // No path found within limits
        return new ArrayList<>();
    }

}
