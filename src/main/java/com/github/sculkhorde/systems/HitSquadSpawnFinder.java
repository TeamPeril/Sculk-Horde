package com.github.sculkhorde.systems;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.util.*;
import java.util.function.Predicate;

public class HitSquadSpawnFinder {
    private final ServerLevel level;
    private final BlockPos origin;
    private final BlockPos target;
    private final PriorityQueue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingInt(this::heuristic));
    private final Map<Long, Boolean> visitedPositions = new HashMap<>();
    private final Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
    private boolean debugMode = false;
    private ArmorStand debugStand;
    private boolean pathFound = false;

    private boolean isFinished = false;
    private List<BlockPos> path = new ArrayList<>();

    private int MAX_DISTANCE = 150;

    protected Predicate<BlockPos> isObstructed;
    protected Predicate<BlockPos> isValidTargetBlock;

    protected BlockPos foundBlock;

    public HitSquadSpawnFinder(ServerLevel level, BlockPos origin, BlockPos target) {
        this.level = level;
        this.origin = origin;
        this.target = target;
        queue.add(origin);
    }

    public void enableDebugMode() {
        debugMode = true;
    }

    private int heuristic(BlockPos pos) {
        // Only consider x and z coordinates
        return Math.abs(pos.getX() - target.getX()) + Math.abs(pos.getZ() - target.getZ());
    }

    public void tick() {
        if (pathFound || queue.isEmpty()) {

            if(pathFound && debugMode)
            {
                SculkHorde.LOGGER.info("HitSquadSpawnFinder | Found Target Block at" + foundBlock.toShortString());
            }
            else if(debugMode)
            {
                SculkHorde.LOGGER.info("HitSquadSpawnFinder | Did Not Target Block");
            }

            isFinished = true;
            return;
        }

        // Spawn Debug Stand if Necessary
        if(debugStand == null && debugMode)
        {
            debugStand = new ArmorStand(level, origin.getX(), origin.getY(), origin.getZ());
            debugStand.setInvisible(true);
            debugStand.setNoGravity(true);
            debugStand.addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 3));
            level.addFreshEntity(debugStand);
        }

        BlockPos current = queue.poll();

        if(debugMode)
        {
            debugStand.teleportTo(current.getX() + 0.5, current.getY(), current.getZ() + 0.5);
        }

        if (isValidTargetBlock.test(current)) {
            path = reconstructPath(current);
            pathFound = true;
            foundBlock = current;
            return;
        }

        for (BlockPos neighbor : BlockAlgorithms.getNeighborsCube(current, false)) {
            if (visitedPositions.getOrDefault(neighbor.asLong(), false)) {
                continue;
            }

            if (isObstructed.test(neighbor)) {
                continue;
            }

            if(neighbor.distManhattan(origin) > MAX_DISTANCE)
            {
                continue;
            }

            queue.add(neighbor);
            visitedPositions.put(neighbor.asLong(), true);
            cameFrom.put(neighbor, current);

            if (debugMode) {
                //level.setBlockAndUpdate(neighbor, Blocks.GREEN_STAINED_GLASS.defaultBlockState());
            }
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

    public List<BlockPos> getPath() {
        return path;
    }

    public boolean isPathFound() {
        return pathFound;
    }

    public void setTargetBlockPredicate(Predicate<BlockPos> predicate) {
        isValidTargetBlock = predicate;
    }

    public void setObstructionPredicate(Predicate<BlockPos> predicate) {
        isObstructed = predicate;
    }

    public void setMaxDistance(int value) {
        MAX_DISTANCE = value;
    }

    public boolean isFinished()
    {
        return isFinished;
    }

    public BlockPos getFoundBlock()
    {
        return foundBlock;
    }
}