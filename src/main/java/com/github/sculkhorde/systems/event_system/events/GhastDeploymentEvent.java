package com.github.sculkhorde.systems.event_system.events;

import com.github.sculkhorde.common.entity.SculkGhastEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.systems.path_builder_system.PathBuilderRequest;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.*;
import java.util.function.Predicate;

/**
 * Will find a path, spawn a sculk ghast, send the ghast to the location, the ghast will then engage the enemy.
 * 
 */
public class GhastDeploymentEvent extends Event {

    protected SculkGhastEntity ghast;
    protected UUID ghastUUID;

    Optional<ModSavedData.NodeEntry> cloestNode = Optional.empty();
    Optional<BlockPos> potentialSpawnPoint = Optional.empty();
    PathBuilderRequest pathRequest;

    public static boolean canSendGhastDeployment(LivingEntity entity){


        if(!DifficultyUtil.isCurrentDifficultyGreaterThanEasy() || !SculkHorde.gravemind.isEvolutionInMatureState())
        {
            return false;
        }

        if(EntityAlgorithms.isLivingEntityExplicitDenyTarget(entity))
        {
            return false;
        }

        if(entity instanceof Mob mob)
        {
            // Cooldown of ghast deployment depends on difficulty
            long timeRequired = TickUnits.convertMinutesToTicks(10);
            if(DifficultyUtil.isCurrentDifficultyHard())
            {
                timeRequired = TickUnits.convertMinutesToTicks(5);
            }


            ModSavedData.MobProfileEntry profile = MobProfileUtil.getOrCreateMobProfile(mob);

            long currentTime = entity.level().getGameTime();
            long timeOfLastGhastDeployment = profile.getTimeofLastGhastDeployment();
            long timeSinceLastDeployment = currentTime - timeOfLastGhastDeployment;

            if(timeSinceLastDeployment < timeRequired)
            {
                return false;
            }
            else if(!profile.isHighPriorityTarget() && entity.getMaxHealth() < 50)
            {
                return false;
            }
        }
        else if(entity instanceof Player player)
        {
            // Cooldown of ghast deployment depends on difficulty
            long timeRequired = TickUnits.convertMinutesToTicks(15);
            if(DifficultyUtil.isCurrentDifficultyHard())
            {
                timeRequired = TickUnits.convertMinutesToTicks(10);
            }


            ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);

            long currentTime = entity.level().getGameTime();
            long timeOfLastGhastDeployment = profile.getTimeofLastGhastDeployment();
            long timeSinceLastDeployment = currentTime - timeOfLastGhastDeployment;

            if(timeSinceLastDeployment < timeRequired)
            {
                return false;
            }
            else if(entity.getMaxHealth() < 50)
            {
                return false;
            }
        }

        Optional<ModSavedData.NodeEntry> node = ModSavedData.getSaveData().getClosestNodeEntry((ServerLevel) entity.level(), entity.blockPosition());
        if(node.isEmpty())
        {
            return false;
        }

        return true;

    }

    protected enum State {
        INITIALIZATION,
        TRAVEl,
        ENGAGING,
        SUCCESS,
        FAILURE
    }

    protected State state;
    protected boolean isEventOver = false;

    public static Optional<GhastDeploymentEvent> trySendGhastDepolymentEvent(LivingEntity entity)
    {
        if(entity == null)
        {
            SculkHorde.LOGGER.error("sendGhastDepolymentEvent | Null Target");
            return Optional.empty();
        }

        if(!canSendGhastDeployment(entity))
        {
            return Optional.empty();
        }

        return Optional.of(forceGhastDeploymentEvent(entity));

    }

    public static GhastDeploymentEvent forceGhastDeploymentEvent(LivingEntity entity)
    {
        GhastDeploymentEvent ghastDeploymentEvent = new GhastDeploymentEvent(entity.level().dimension(), entity.blockPosition());
        SculkHorde.eventSystem.addEvent(ghastDeploymentEvent);
        if(entity instanceof Mob mob)
        {
            MobProfileUtil.updateGhastDeploymentTime(mob);
        }
        return ghastDeploymentEvent;
    }

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
        else if(state == State.TRAVEl)
        {
            travelTick();
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
            Predicate<BlockPos> obstructionPredicate = (pos) -> !BlockAlgorithms.isCubeReplaceable(getDimension(), pos, 5);

            pathRequest = new PathBuilderRequest(getDimension(), eventLocation, potentialSpawnPoint.get(), 32, obstructionPredicate, null);
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

        // Assign the built path to the ghast (let the ghast's own goal follow it)
        if(ghast != null && pathRequest != null && pathRequest.hasPath())
        {
            ghast.setBuiltPath(pathRequest.getBuiltPath());
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Assigned built path to ghast UUID: " + ghastUUID);
        }

        setState(State.TRAVEl);

    }

    protected void travelTick()
    {


        // Reattach ghast if needed
        if(ghast == null && ghastUUID != null)
        {
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | travelTick start");
            ghast = (SculkGhastEntity) getDimension().getEntity(ghastUUID);
            if(ghast != null)
            {
                SculkHorde.LOGGER.debug("GhastDeploymentEvent | Reattached ghast UUID: " + ghastUUID);
            }
        }

        if(ghast == null || ghast.isDeadOrDying() || ghast.isRemoved())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: Ghast is dead.");
            return;
        }

        // Make sure we keep chunks loaded around the ghast while moving
        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(ghast,3, 3, TickUnits.convertMinutesToTicks(1));
        //SculkHorde.LOGGER.debug("GhastDeploymentEvent | Ensured chunk loading around ghast.");

        if(pathRequest == null || !pathRequest.hasPath())
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Failure: pathRequest missing or has no path.");
            return;
        }

        if(ghast.hasCompletedAssignedBuiltPath())
        {
            // clear the assigned flag and advance state to engaging (or success)
            ghast.clearCompletedAssignedBuiltPath();
            setState(State.SUCCESS);
            SculkHorde.LOGGER.debug("GhastDeploymentEvent | Ghast completed built path; switching to ENGAGING.");
            return;
        }

        // Otherwise, just wait while the ghast follows the assigned built path.
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
}
