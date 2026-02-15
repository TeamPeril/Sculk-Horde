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
import java.util.function.Predicate;

public class PathBuilder {
    private final PriorityQueue<BlockPos> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(this::getHeuristic));
    private final Map<Long, Boolean> visitedPositions = new HashMap<>();
    private final Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
    private boolean debugMode = true;
    private ArmorStand debugStand;
    private int MAX_DISTANCE = 150;

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
            SculkHorde.LOGGER.error("PathBuilderSystem | Attempted to getHeuristic for non-existent request.");
            return -1;
        }

        float heuristic = Math.abs(pos.getX() - request.get().getDesiredDestination().getX())
                + Math.abs(pos.getY() - request.get().getDesiredDestination().getY()
                +Math.abs(pos.getZ() - request.get().getDesiredDestination().getZ()));

        // Modifier for preference above ground
        ServerLevel level = getCurrentRequest().get().getLevel(); // Assuming PathBuilderRequest has getLevel()

        BlockPos groundPos = BlockAlgorithms.getGroundBlockPos(level, pos, pos.getY());
        int heightOffTheGround = pos.getY() - groundPos.getY();

        if(heightOffTheGround < 10 && BlockAlgorithms.getBlockDistance(request.get().desiredDestination, pos) > 10)
        {
            heuristic = 0;
        }

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
     * @return {@code true} if all blocks in the cube are unobstructed, {@code false} if any block is obstructed.
     */
    public static boolean isCubeObstructed(ServerLevel level, BlockPos origin, int length) {
        // Handle invalid length: an empty or negatively sized cube could be considered "not obstructed".
        // Adjust this behavior if needed (e.g., throw IllegalArgumentException).
        if (length <= 0) {
            return true; // Or false, or throw exception, depending on desired behavior for invalid input
        }

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Calculate the starting coordinate (minimum corner) for iteration based on the center 'origin'.
        // 'extentNegativeDir' is how many blocks the cube extends from the origin's coordinate
        // in the negative direction along an axis.
        // For length 3: extentNegativeDir = 3/2 = 1. Min coord = origin.coord - 1.
        // For length 2: extentNegativeDir = 2/2 = 1. Min coord = origin.coord - 1.
        int extentNegativeDir = length / 2; // Integer division handles odd/even cases appropriately

        int minX = origin.getX() - extentNegativeDir;
        int minY = origin.getY() - extentNegativeDir;
        int minZ = origin.getZ() - extentNegativeDir;

        // The loop for each axis will run 'length' times.
        // So, the maximum coordinate is min_coord + length - 1.
        // For length 3 (minX = originX - 1): maxX = (originX - 1) + 3 - 1 = originX + 1. Iterates originX-1, originX, originX+1.
        // For length 2 (minX = originX - 1): maxX = (originX - 1) + 2 - 1 = originX. Iterates originX-1, originX.
        int maxX = minX + length - 1;
        int maxY = minY + length - 1;
        int maxZ = minZ + length - 1;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    // Set the mutable BlockPos to the current position in the cube
                    mutablePos.set(x, y, z);

                    // Check if the block at the current position is unobstructed
                    if (!BlockAlgorithms.isReplaceable(level.getBlockState(mutablePos))) {
                        return true;
                    }
                }
            }
        }

        // If the loops complete, it means all blocks checked were air.
        return false; // No obstructions found, the cube is clear.
    }

    public final Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        if(isCubeObstructed(request.get().level, blockPos, 5))
        {
            return true;
        }

        return false;
    };
    protected Predicate<BlockPos> isValidTargetBlock  = (blockPos) ->
    {
        if(BlockAlgorithms.getBlockDistance(blockPos, request.get().desiredDestination) <= request.get().requiredProximityToDesiredLocation)
        {
            return true;
        }

        return false;
    };

    protected void initializationTick()
    {
        PathBuilderRequest currentRequest = request.get();
        currentRequest.hasPathBuildingStarted = true;
        currentRequest.isPathBuildingInProgress = true;
        currentRequest.isSearching = true;
        priorityQueue.add(request.get().startLocation);
        visitedPositions.put(request.get().startLocation.asLong(), true);
        SculkHorde.LOGGER.debug("PathBuilder | Path Builder Initialized.");
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

        if(debugMode)
        {
            debugStand.teleportTo(currentPos.getX() + 0.5, currentPos.getY(), currentPos.getZ() + 0.5);
        }

        if (isValidTargetBlock.test(currentPos))
        {
            if(debugMode)
            {
                SculkHorde.LOGGER.debug("PathBuilder | Found Target Block");
            }
            currentRequest.setPath(reconstructPath(currentPos));
            currentRequest.isPathBuildSuccessful = true;
            currentRequest.isSearching = false;
            return;
        }

        for (BlockPos neighbor : BlockAlgorithms.getNeighborsCube(currentPos, false)) {
            if (visitedPositions.getOrDefault(neighbor.asLong(), false)) {
                continue;
            }

            if (isObstructed.test(neighbor)) {
                continue;
            }

            if(neighbor.distManhattan(currentRequest.getStartLocation()) > MAX_DISTANCE)
            {
                continue;
            }

            priorityQueue.add(neighbor);
            visitedPositions.put(neighbor.asLong(), true);
            cameFrom.put(neighbor, currentPos);
        }
    }

    protected void finishedTick()
    {
        timeOfLastCompletion = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();
        request.get().isPathBuildingInProgress = false;
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

    private List<BlockPos> reconstructPath(BlockPos current) {
        List<BlockPos> path = new ArrayList<>();
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }
        Collections.reverse(path);
        return path;
    }


    public void setTargetBlockPredicate(Predicate<BlockPos> predicate) {
        //isValidTargetBlock = predicate;
    }

    public void setObstructionPredicate(Predicate<BlockPos> predicate) {
        //isObstructed = predicate;
    }

    public void setMaxDistance(int value) {
        MAX_DISTANCE = value;
    }

}