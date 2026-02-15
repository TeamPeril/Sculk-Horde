package com.github.sculkhorde.systems.event_system.events;

import com.github.sculkhorde.common.entity.SculkGhastEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.systems.path_builder_system.PathBuilderRequest;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.Predicate;

/**
 * Will find a path, spawn a sculk ghast, send the ghast to the location, the ghast will then engage the enemy.
 * 
 */
public class GhastDeploymentEvent extends Event {
    protected final int MAX_DISTANCE_FROM_PLAYER = 150;

    protected SculkGhastEntity ghast;
        protected UUID ghastUUID;

    Optional<ModSavedData.NodeEntry> cloestNode = Optional.empty();
    Optional<BlockPos> potentialSpawnPoint = Optional.empty();
    PathBuilderRequest pathRequest;


    protected enum State {
        INITIALIZATION,
        PURSUIT,
        ENGAGING,
        SUCCESS,
        FAILURE
    }

    protected State state;
    protected boolean isEventOver = false;

    protected Optional<SculkGhastSpawnFinder> spawnFinder = Optional.empty();

    protected Optional<BlockPos> desiredSpawnPos = Optional.empty();

    public GhastDeploymentEvent(ResourceKey<Level> dimension, BlockPos targetLocation) {
        this(dimension);
        this.eventLocation = targetLocation;
    }

    public GhastDeploymentEvent(ResourceKey<Level> dimension) {
        super(dimension);
        setEventCost(100);
        setState(State.INITIALIZATION);
    }

    public Optional<SculkGhastEntity> getGhast()
    {
        return Optional.ofNullable(ghast);
    }

    public boolean canContinue()
    {
        return !isEventOver;
    }

    @Override
    public void serverTick() {

        // Debug: report tick and current state
        //SculkHorde.LOGGER.debug("GhastDeploymentEvent | serverTick state: " + (state == null ? "NULL" : state.toString()));

        if(state == State.INITIALIZATION)
        {
            initializationTick();
        }
        else if(state == State.PURSUIT)
        {
            pursuitTick();
        }
        else if(state == State.ENGAGING)
        {
            engagingTick();
        }
        else if(state == State.SUCCESS)
        {
            successTick();
        }
        else if(state == State.FAILURE)
        {
            failureTick();
        }

    }

    protected void setState(State state)
    {
        this.state = state;
        SculkHorde.LOGGER.info("GhastDeploymentEvent | " + "State: " + state.toString());
    }


    public Optional<BlockPos> findValidSpawnPosition(int cubeLength) {
        // Calculate the bounds of the cube
        int halfLength = cubeLength / 2;
        int minX = getEventLocation().getX() - halfLength;
        int minY = getEventLocation().getY() - halfLength;
        int minZ = getEventLocation().getZ() - halfLength;
        int maxX = getEventLocation().getX() + halfLength;
        int maxY = getEventLocation().getY() + halfLength;
        int maxZ = getEventLocation().getZ() + halfLength;

        // Create a mutable block position to avoid creating new objects in the loop
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Iterate through each block position in the cube
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);

                    // Check if the block is air
                    if (BlockAlgorithms.isReplaceable(getDimension().getBlockState(mutablePos))) {
                        // Check if the position is a valid spawn position
                        if (isValidSpawnPos(mutablePos)) {
                            return Optional.of(mutablePos.immutable());
                        }
                    }
                }
            }
        }

        // If no valid spawn position is found, return an empty Optional
        return Optional.empty();
    }

    public boolean isValidSpawnPos(BlockPos.MutableBlockPos pos)
    {
        if(pos == null) { return false; }

        if(!BlockAlgorithms.isAir(getDimension().getBlockState(pos)))
        {
            return false;
        }

        return true;
    };

    public final Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        boolean isBlockNotAir = !getDimension().getBlockState(blockPos).is(Blocks.AIR);

        return isBlockNotAir;
    };


    protected void initializationTick()
    {
        //SculkHorde.LOGGER.debug("GhastDeploymentEvent | initializationTick start");

        if(potentialSpawnPoint.isEmpty())
        {
            if(cloestNode.isEmpty())
            {
                cloestNode = ModSavedData.getSaveData().getClosestNodeEntry(getDimension(), getEventLocation());
                if(cloestNode.isEmpty())
                {
                    setState(State.FAILURE);
                    SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: Could not find closest node.");
                    return;
                }
                else
                {
                    SculkHorde.LOGGER.debug("GhastDeploymentEvent | Closest node found at: " + cloestNode.get().getPosition().toShortString());
                }
            }


            potentialSpawnPoint = BlockAlgorithms.getLargestAreaAboveBlock(getDimension(), cloestNode.get().getPosition());

            if(potentialSpawnPoint.isEmpty())
            {
                setState(State.FAILURE);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: Could not find place to spawn above closest node.");
                return;
            }
            else
            {
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Found Spawn Point at: " + potentialSpawnPoint.get().toShortString());
            }

            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Found Spawn Point.");
        }

        if(pathRequest == null)
        {
            // Define an obstruction predicate: true when the block is non-air
            Predicate<BlockPos> obstructionPredicate = (pos) -> {
                return !BlockAlgorithms.isAir(getDimension().getBlockState(pos));
            };

            // Define a valid target predicate:
            // - target block must be air
            // - the two blocks above must also be air (room for the entity)
            // - the block below must NOT be air (so there is ground beneath)
            Predicate<BlockPos> validTargetPredicate = (pos) -> {
                if (!BlockAlgorithms.isReplaceableByWater(getDimension().getBlockState(pos)))
                {
                    return false;
                }

                return true;
            };

            pathRequest = new PathBuilderRequest(getDimension(), eventLocation, potentialSpawnPoint.get(), 20, obstructionPredicate, validTargetPredicate);
             SculkHorde.pathBuilderSystem.addPathBuilderRequest(pathRequest);
             SculkHorde.LOGGER.debug("GhastDeploymentEvent | Created path request.");
         }

        if(pathRequest.isPathBuildingInProgress() || !pathRequest.hasPathBuildStarted())
        {
            return;
        }

        if(!pathRequest.isPathBuildSuccessful())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: Could not find path to target.");
            return;
        }

        // Spawn ghast if not spawned yet
        if(ghast == null)
        {
            BlockPos spawnAt = potentialSpawnPoint.orElse(cloestNode.get().getPosition().above(5));
            SculkGhastEntity created = com.github.sculkhorde.core.ModEntities.SCULK_GHAST.get().create(getDimension());
            if(created == null)
            {
                setState(State.FAILURE);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: Could not create ghast entity.");
                return;
            }
            created.setPos(spawnAt.getX() + 0.5, spawnAt.getY() + 0.5, spawnAt.getZ() + 0.5);
            created.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TickUnits.convertSecondsToTicks(10), 0));
            created.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(10), 0));
            getDimension().addFreshEntity(created);
            ghast = created;
            ghastUUID = created.getUUID();

            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Spawned ghast UUID: " + ghastUUID + " at " + spawnAt.toShortString());
        }

        setState(State.PURSUIT);

    }

    protected void pursuitTick()
    {
        SculkHorde.LOGGER.debug("GhastDeploymentEvent | pursuitTick start");

        // Reattach ghast if needed
        if(ghast == null && ghastUUID != null)
        {
            ghast = (SculkGhastEntity) getDimension().getEntity(ghastUUID);
            if(ghast != null)
            {
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Reattached ghast UUID: " + ghastUUID);
            }
        }

        if(ghast == null)
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: ghast is null during pursuit.");
            return;
        }

        // Make sure we keep chunks loaded around the ghast while moving
        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(ghast,3, 3, TickUnits.convertMinutesToTicks(1));
        SculkHorde.LOGGER.debug("GhastDeploymentEvent | Ensured chunk loading around ghast.");

        if(pathRequest == null || !pathRequest.hasPath())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: pathRequest missing or has no path.");
            return;
        }

        // If already close enough to target, start engaging
        if(BlockAlgorithms.getBlockDistance(ghast.blockPosition(), pathRequest.getDesiredDestination()) <= pathRequest.getRequiredProximityToDesiredLocation())
        {
            setState(State.ENGAGING);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Reached proximity to desired destination; switching to ENGAGING.");
            return;
        }

        // Follow next step
        pathRequest.getNextStep().ifPresentOrElse(next -> {
            double dist = BlockAlgorithms.getBlockDistance(ghast.blockPosition(), next);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Next step: " + next.toShortString() + " distance: " + dist);
            if(dist <= 2)
            {
                pathRequest.advanceToNextStep();
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Advanced to next path step.");
            }
            else
            {
                ghast.getNavigation().moveTo(next.getX() + 0.5, next.getY() + 0.5, next.getZ() + 0.5, 1.2F);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Moving ghast towards next step.");
            }
        }, () -> {
            // No next step; if path is complete, engage, else fail
            if(pathRequest.isPathComplete())
            {
                setState(State.ENGAGING);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Path complete; switching to ENGAGING.");
            }
            else
            {
                setState(State.FAILURE);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | No next step and path not complete; failing.");
            }
        });
    }

    protected void engagingTick()
    {
        //SculkHorde.LOGGER.debug("GhastDeploymentEvent | engagingTick start");

        // Reattach ghast if needed
        if(ghast == null && ghastUUID != null)
        {
            ghast = (SculkGhastEntity) getDimension().getEntity(ghastUUID);
            if(ghast != null)
            {
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Reattached ghast UUID: " + ghastUUID + " in engagingTick.");
            }
        }

        if(ghast == null || ghast.isRemoved() || ghast.isDeadOrDying())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: ghast missing or dead during engaging.");
            return;
        }

        // Maintain chunkloading around ghast
        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(ghast,3, 3, TickUnits.convertMinutesToTicks(1));
        SculkHorde.LOGGER.debug("GhastDeploymentEvent | Maintained chunk loading during engaging.");

        // Check for arrival at destination
        if(pathRequest != null)
        {
            int proximity = pathRequest.getRequiredProximityToDesiredLocation();
            if(BlockAlgorithms.getBlockDistance(ghast.blockPosition(), pathRequest.getDesiredDestination()) <= proximity)
            {
                setState(State.SUCCESS);
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Arrived at destination; SUCCESS.");
                return;
            }

            // Otherwise, ensure it keeps moving to destination
            BlockPos dest = pathRequest.getDesiredDestination();
            ghast.getNavigation().moveTo(dest.getX() + 0.5, dest.getY() + 0.5, dest.getZ() + 0.5, 1.2F);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Moving ghast towards destination: " + dest.toShortString());
        }
    }

    protected void successTick()
    {
        isEventOver = true;
    }

    protected void failureTick()
    {
        isEventOver = true;
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        if(tag.contains("state"))
        {
            try {
                this.state = State.valueOf(tag.getString("state"));
            }
            catch (IllegalArgumentException ex)
            {
                this.state = State.INITIALIZATION;
            }
        }
        if(tag.contains("ghastUUID"))
        {
            try {
                this.ghastUUID = tag.getUUID("ghastUUID");
            }
            catch (Exception ignored) {}
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        if(this.state != null)
        {
            tag.putString("state", this.state.name());
        }
        if(this.ghast != null)
        {
            tag.putUUID("ghastUUID", this.ghast.getUUID());
        }
        else if(this.ghastUUID != null)
        {
            tag.putUUID("ghastUUID", this.ghastUUID);
        }
    }

    public class SculkGhastSpawnFinder {
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

        public SculkGhastSpawnFinder(ServerLevel level, BlockPos origin, BlockPos target) {
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

            // Debug: report current polled node and remaining queue size
            SculkHorde.LOGGER.debug("GhastSpawnFinder | Polled node: " + (current == null ? "null" : current.toShortString()) + " queueSize: " + queue.size());

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
}
