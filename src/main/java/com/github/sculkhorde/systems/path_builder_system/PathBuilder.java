package com.github.sculkhorde.systems.path_builder_system;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class PathBuilder {
    private final PriorityQueue<BlockPos> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(this::getHeuristic));
    private final Set<Long> visitedPositions = new HashSet<>();
    private final Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
    private boolean debugMode = true;
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

    public void enableDebugMode() {
        debugMode = true;
    }

    protected Optional<PathBuilderRequest> getCurrentRequest()
    {
        return request;
    }

    protected float getHeuristic(BlockPos pos) {

        if(request.isEmpty())
        {
            throw new IllegalStateException("PathBuilder | Attempted to getHeuristic for non-existent request.");
        }

        PathBuilderRequest current = request.get();
        BlockPos dest = current.getDesiredDestination();

        // Manhattan distance (sum of absolute differences) computed correctly for X, Y, Z
        float heuristic = Math.abs(pos.getX() - dest.getX())
                + Math.abs(pos.getY() - dest.getY())
                + Math.abs(pos.getZ() - dest.getZ());

        // Modifier for preference above ground. Penalize nodes that are too close to ground
        ServerLevel level = current.getLevel(); // Assuming PathBuilderRequest has getLevel()
        /*
        BlockPos groundPos = BlockAlgorithms.getGroundBlockPos(level, pos, pos.getY());
        int heightOffTheGround = pos.getY() - groundPos.getY();

        // Previously this set heuristic to 0 which biased the queue incorrectly. Add a penalty instead.
        if(heightOffTheGround < 10 && BlockAlgorithms.getBlockDistance(dest, pos) > 10)
        {
            heuristic += 50.0f; // penalty value; tune as necessary
        }

         */

        return Math.max(0, heuristic);

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

    /**
     * Checks if a cube of blocks defined by an origin and length contains any obstructed blocks.
     *
     * @param level The Level (or World) instance to check blocks in.
     * @param origin The BlockPos representing the center of the cube.
     *               - If 'length' is odd (e.g., 3), 'origin' is the exact center block.
     *                 The cube extends (length-1)/2 blocks in both positive and negative directions from origin.
     *                 (e.g., for length 3, offsets are -1, 0, +1 from origin's coordinates).
     *               - If 'length' is even (e.g., 2), 'origin' is one of the conceptual central blocks.
     *                 The cube extends 'length/2' blocks in the negative direction and '(length/2)-1' blocks
     *                 in the positive direction from origin's coordinates.
     *                 (e.g., for length 2, offsets are -1, 0 from origin's coordinates).
     * @param length The side length of the cube. For example, a length of 1 checks only the origin block.
     *               A length of 2 checks a 2x2x2 cube. A length of 3 checks a 3x3x3 cube.
     * @return {@code true} if any block in the cube is obstructed (non-replaceable), {@code false} if all blocks are replaceable.
     */
    public static boolean isCubeObstructed(ServerLevel level, BlockPos origin, int length) {
        // Handle invalid length: an empty or negatively sized cube could be considered "not obstructed".
        // Adjust this behavior if needed (e.g., throw IllegalArgumentException).
        if (length <= 0) {
            return true; // treat invalid length as obstructed for safety
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Calculate the starting coordinate (minimum corner) for iteration based on the center 'origin'.
        int extentNegativeDir = length / 2; // Integer division handles odd/even cases appropriately

        int minX = origin.getX() - extentNegativeDir;
        int minY = origin.getY() - extentNegativeDir;
        int minZ = origin.getZ() - extentNegativeDir;

        int maxX = minX + length - 1;
        int maxY = minY + length - 1;
        int maxZ = minZ + length - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Set the mutable BlockPos to the current position in the cube
                    mutablePos.set(x, y, z);

                    // If any block in the cube is not replaceable then the cube is obstructed
                    if (!BlockAlgorithms.isReplaceable(level.getBlockState(mutablePos))) {
                        return true;
                    }
                }
            }
        }

        // No obstructions found
        return false;
    }

    protected void initializationTick()
    {
        // Ensure any previous search state is cleared when starting a new request
        priorityQueue.clear();
        visitedPositions.clear();
        cameFrom.clear();
        foundTarget = false;
        nodesSearched = 0;

        PathBuilderRequest currentRequest = request.get();
        currentRequest.hasPathBuildingStarted = true;
        currentRequest.isPathBuildingInProgress = true;
        currentRequest.isSearching = true;
        priorityQueue.add(currentRequest.startLocation);
        visitedPositions.add(currentRequest.startLocation.asLong());
        SculkHorde.LOGGER.debug("PathBuilder | Path Builder Initialized at " + currentRequest.startLocation.toShortString());
    }

    protected void processingTick()
    {
        PathBuilderRequest currentRequest = request.get();

        if (priorityQueue.isEmpty()) {

            if(debugMode)
            {
                SculkHorde.LOGGER.debug("PathBuilder | Queue is Empty. No more blocks to search.");
            }

            currentRequest.isSearching = false;
            return;
        }

        // Safety check: abort if we've searched too many nodes
        if (nodesSearched >= MAX_SEARCH_NODES) {
            if (debugMode) {
                SculkHorde.LOGGER.debug("PathBuilder | Max search nodes ({}) exceeded. Aborting.", MAX_SEARCH_NODES);
            }
            currentRequest.isSearching = false;
            return;
        }

        // Spawn Debug Stand if Necessary
        if(debugStand == null && debugMode)
        {
            debugStand = new ArmorStand(currentRequest.getLevel(), currentRequest.getStartLocation().getX(), currentRequest.getStartLocation().getY(), currentRequest.getStartLocation().getZ());
            debugStand.setInvisible(true);
            debugStand.setNoGravity(true);
            debugStand.addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 3));
            currentRequest.getLevel().addFreshEntity(debugStand);
        }

        BlockPos currentPos = priorityQueue.poll();
        nodesSearched++;

        if(debugMode && debugStand != null)
        {
            debugStand.teleportTo(currentPos.getX() + 0.5, currentPos.getY(), currentPos.getZ() + 0.5);
        }

        // Check if we've reached the target, validating with the request's isValidTarget predicate
        if(BlockAlgorithms.getBlockDistance(currentPos, currentRequest.getDesiredDestination()) <= currentRequest.requiredProximityToDesiredLocation)
        {
            // Validate the target position using the request's predicate
            if (currentRequest.isValidTargetBlock != null && !currentRequest.isValidTargetBlock.test(currentPos)) {
                if (debugMode) {
                    SculkHorde.LOGGER.debug("PathBuilder | Proximity reached but target invalid at {}", currentPos.toShortString());
                }
                // Continue searching; don't return
            } else {
                if(debugMode)
                {
                    SculkHorde.LOGGER.debug("PathBuilder | Found Target Block at {}", currentPos.toShortString());
                }
                currentRequest.setPath(getPath(currentPos));
                currentRequest.isPathBuildSuccessful = true;
                currentRequest.isSearching = false;
                return;
            }
        }

        for (BlockPos neighbor : BlockAlgorithms.getNeighborsCube(currentPos, false)) {
            if (visitedPositions.contains(neighbor.asLong())) {
                continue;
            }

            // Check obstruction using the request's predicate if available, otherwise use cube check
            if (currentRequest.isObstructed != null && currentRequest.isObstructed.test(neighbor)) {
                continue;
            }

            if (isCubeObstructed(currentRequest.getLevel(), neighbor, 5)) {
                continue;
            }

            if(neighbor.distManhattan(currentRequest.getStartLocation()) > MAX_DISTANCE)
            {
                continue;
            }

            priorityQueue.add(neighbor);
            visitedPositions.add(neighbor.asLong());
            cameFrom.put(neighbor, currentPos);
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
            if(debugMode)
            {
                for(BlockPos pos : cameFrom.values())
                {
                    request.get().getLevel().setBlockAndUpdate(pos, Blocks.GREEN_STAINED_GLASS.defaultBlockState());
                }
            }

            SculkHorde.LOGGER.info("PathBuilder | Path Built Successfully");
            return;
        }
        SculkHorde.LOGGER.info("PathBuilder | Path Not Built");
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

    private BuiltPath getPath(BlockPos current) {
        List<BlockPos> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return new BuiltPath(path);
    }

    public void setMaxDistance(int value) {
        MAX_DISTANCE = value;
    }

}
