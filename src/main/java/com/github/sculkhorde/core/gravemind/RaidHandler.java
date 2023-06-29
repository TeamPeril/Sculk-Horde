package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockSearcher;
import com.github.sculkhorde.util.ChunkLoaderHelper;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidHandler {

    public static int COOLDOWN_BETWEEN_RAIDS = TickUnits.convertMinutesToTicks(5); // TODO INCREASE COOLDOWN
    protected int MAX_WAVE_DURATION = TickUnits.convertMinutesToTicks(5);
    protected int waveDuration = 0;

    // Raid Variables
    protected ServerLevel level;
    protected BlockPos raidLocation = BlockPos.ZERO;
    protected BlockPos objectiveLocation = BlockPos.ZERO;
    protected BlockPos objectiveLocationAtStartOfWave = objectiveLocation; // We use this to make sure we move on to next objective
    protected BlockPos raidCenter = BlockPos.ZERO; // Used for calculation purposes
    protected int MINIMUM_RAID_RADIUS = 200;
    protected int currentRaidRadius = MINIMUM_RAID_RADIUS;
    protected int MAXIMUM_RAID_RADIUS = 500;
    // The Mobs that spawn during waves
    protected ArrayList<ISculkSmartEntity> waveParticipants = new ArrayList<>();

    // The current status of the raid
    public enum RaidState {
        INACTIVE,
        INVESTIGATING_LOCATION,
        ENDERMAN_SCOUTING,
        INITIALIZING_RAID,
        INITIALIZING_WAVE,
        ACTIVE_WAVE,
        COMPLETE,
        FAILED
    }
    private RaidState raidState = RaidState.INACTIVE;

    protected enum failureType {
        NONE,
        FAILED_INITIALIZATION,
        ENDERMAN_DEFEATED,
        FAILED_OBJECTIVE_COMPLETION
    }

    protected failureType failure = failureType.NONE;

    // Enderman Scouting
    private SculkEndermanEntity scoutEnderman = null;
    private int timeElapsedScouting = 0;
    private final int SCOUTING_DURATION = TickUnits.convertMinutesToTicks(1);

    // Waves
    protected EntityFactory.StrategicValues[] currentWavePattern;
    private int maxWaves = 2;
    private int currentWave = 1;
    private int remainingWaveParticipants = 0;

    protected ModSavedData.AreaofInterestEntry areaOfInterestEntry;

    // Targets
    private static final ArrayList<BlockPos> high_priority_targets = new ArrayList<>();
    private static final ArrayList<BlockPos> medium_priority_targets = new ArrayList<>();
    private static final ArrayList<BlockPos> low_priority_targets = new ArrayList<>();

    // Block Searcher
    private BlockSearcher blockSearcher;

    /**
     * This is used to determine if a block is obstructed when searching for a spawn location
     */
    private final Predicate<BlockPos> isSpawnObstructed = (blockPos) ->
    {
        if(Math.abs(blockPos.getY() - raidLocation.getY()) > 15)
        {
            return true;
        }

        // If block isn't solid, its obstructed
        if(level.getBlockState(blockPos).isAir() || level.getBlockState(blockPos).is(Blocks.WATER) || level.getBlockState(blockPos).is(Blocks.LAVA))
        {
            return true;
        }
        // If block above is not
        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above()).is(Blocks.WATER) || level.getBlockState(blockPos.above()).is(Blocks.LAVA))
        {
            return true;
        }

        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above(1)).is(Blocks.WATER) || level.getBlockState(blockPos.above(1)).is(Blocks.LAVA))
        {
            return true;
        }

        return !level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above(2)).is(Blocks.WATER) || level.getBlockState(blockPos.above(2)).is(Blocks.LAVA);
    };

    /**
     * This is used to determine if a block is a valid spawn location for a mob
     */
    private final Predicate<BlockPos> isSpawnTarget = (blockPos) ->
    {
        return BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (getCurrentRaidRadius() * 0.75) && BlockAlgorithms.isAreaFlat(level, blockPos, 2);
    };

    public RaidHandler(ServerLevel levelIn)
    {
        setLevel(levelIn);
    }

    // Accessors & Modifiers

    public ServerLevel getLevel() {
        return level;
    }

    public void setLevel(ServerLevel levelIn) {
        level = levelIn;
    }

    public BlockPos getRaidLocation() {
        return raidLocation;
    }

    public Vec3 getRaidLocationVec3() {
        return new Vec3(raidLocation.getX(), raidLocation.getY(), raidLocation.getZ());
    }

    public void setRaidLocation(BlockPos raidLocationIn) {
        raidLocation = raidLocationIn;
    }

    public boolean canRaidStart()
    {
        if(!SculkHorde.isDebugMode())
        {
            return false;
        }

        if(gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped)
        {
            return false;
        }

        if(!SculkHorde.savedData.isRaidCooldownOver())
        {
            return false;
        }

        return !SculkHorde.savedData.getAreasOfInterestEntries().isEmpty();
    }

    /**
     * Gets the raid state
     * @return the raid state
     */
    public boolean isRaidActive() {
        return raidState == RaidState.ACTIVE_WAVE;
    }

    /**
     * Sets the raid State
     * @param raidStateIn the raid state
     */
    public void setRaidState(RaidState raidStateIn) {
        raidState = raidStateIn;
    }

    /**
     * Sets the raid State to Failed
     * @param failureTypeIn the type of failure
     */
    public void setRaidStateToFailure(failureType failureTypeIn) {
        failure = failureTypeIn;
    }

    /**
     * Gets the raid radius
     * @return the raid radius
     */
    public int getCurrentRaidRadius() {
        return currentRaidRadius;
    }

    /**
     * Sets the raid radius
     * @param raidRadiusIn the raid radius
     */
    public void setCurrentRaidRadius(int raidRadiusIn) {
        currentRaidRadius = raidRadiusIn;
    }

    /**
     * Checks if all raid participants are alive
     * @return true if all raid participants are alive, false otherwise
     */
    public boolean areWaveParticipantsDead() {
        return remainingWaveParticipants <= 0;
    }

    private void announceToPlayersInRange(Component message, int range)
    {
        level.players().forEach((player) -> {
            if(BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= range)
            {
                player.displayClientMessage(message, false);
            }
        });
    }

    public void announceToAllPlayers(Component message)
    {
        level.players().forEach((player) -> player.displayClientMessage(message, false));
    }

    private void updateRemainingWaveParticipantsAmount()
    {
        remainingWaveParticipants = 0;
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                remainingWaveParticipants++;
            }
        }
    }

    /**
     * Just removes them from the waveParticipantsList. Does not kill them.
     * Will also removing the glowing effect.
     */
    protected void removeWaveParticipantsFromList()
    {
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                ((Mob) entity).removeAllEffects();
            }
            else
            {
                ((Mob) entity).discard();
            }
        }
        waveParticipants.clear();
    }

    public BlockPos getObjectiveLocation()
    {
           return objectiveLocation;
    }

    public Vec3 getObjectiveLocationVec3()
    {
        return new Vec3(objectiveLocation.getX(), objectiveLocation.getY(), objectiveLocation.getZ());
    }

    public void setObjectiveLocation(BlockPos objectiveLocationIn)
    {
        objectiveLocation = objectiveLocationIn;
    }

    public Optional<BlockPos> popObjectiveLocation()
    {
        Optional<BlockPos> objective = Optional.empty();
        if(!high_priority_targets.isEmpty())
        {
            objective = Optional.of(high_priority_targets.get(0));
            high_priority_targets.remove(0);
        }
        else if(!medium_priority_targets.isEmpty())
        {
            objective = Optional.of(medium_priority_targets.get(0));
            medium_priority_targets.remove(0);
        }
        else if(!low_priority_targets.isEmpty())
        {
            objective = Optional.of(low_priority_targets.get(0));
            low_priority_targets.remove(0);
        }
        return objective;
    }

    /**
     * Will Pop Next Objective Location and set it as the objective location
     */
    public void setNextObjectiveLocation()
    {
        Optional<BlockPos> objectiveOptional = popObjectiveLocation();
        if(objectiveOptional.isPresent())
        {
            objectiveLocation = objectiveOptional.get();
        }
        else
        {
            announceToAllPlayers(Component.literal("The Sculk Horde has Successfully destroyed all objectives!"));

            //level.players().forEach((player) -> level.playSound(null, player.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.AMBIENT, 1.0F, 1.0F));

            setRaidState(RaidState.COMPLETE);
        }
    }

    public boolean isCurrentObjectiveCompleted()
    {
        if(level.getBlockState(objectiveLocation).is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return false;
        }
        else if(level.getBlockState(objectiveLocation).is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return false;
        }
        else return !level.getBlockState(objectiveLocation).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY);
    }

    public Optional<BlockPos> getNextObjectiveLocation()
    {
        Optional<BlockPos> objective = Optional.empty();
        if(!high_priority_targets.isEmpty())
        {
            objective = Optional.of(high_priority_targets.get(0));
        }
        else if(!medium_priority_targets.isEmpty())
        {
            objective = Optional.of(medium_priority_targets.get(0));
        }
        else if(!low_priority_targets.isEmpty())
        {
            objective = Optional.of(low_priority_targets.get(0));
        }
        return objective;
    }

    /**
     * Gets the distance of the furthest objective
     * @return the distance of the furthest objective
     */
    public int getDistanceOfFurthestObjective()
    {
        int distance = 0;
        for(BlockPos pos : high_priority_targets)
        {
            int tempDistance = (int) BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), pos);
            if(tempDistance > distance)
            {
                distance = tempDistance;
            }
        }
        for(BlockPos pos : medium_priority_targets)
        {
            int tempDistance = (int) BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), pos);
            if(tempDistance > distance)
            {
                distance = tempDistance;
            }
        }
        for(BlockPos pos : low_priority_targets)
        {
            int tempDistance = (int) BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), pos);
            if(tempDistance > distance)
            {
                distance = tempDistance;
            }
        }
        return distance;
    }

    /**
     * Resets all variables related to raid.
     */
    public void reset()
    {
        SculkHorde.savedData.removeAreaOfInterestFromMemory(areaOfInterestEntry.getPosition());
        ChunkLoaderHelper.unloadChunksInRadius(level, getRaidLocation(), getRaidLocation().getX() >> 4, getRaidLocation().getZ() >> 4, 5);
        blockSearcher = null;
        setRaidState(RaidState.INACTIVE);
        setRaidLocation(BlockPos.ZERO);
        setObjectiveLocation(BlockPos.ZERO);
        waveParticipants.clear();
        remainingWaveParticipants = 0;
        currentWave = 0;
        scoutEnderman = null;
        timeElapsedScouting = 0;
    }

    // Events

    public void raidTick()
    {
        switch (raidState)
        {
            case INACTIVE:
                inactiveRaidTick();
                break;
            case INVESTIGATING_LOCATION:
                investigatingLocationTick();
                break;
            case ENDERMAN_SCOUTING:
                endermanScoutingTick();
                break;
            case INITIALIZING_RAID:
                initializingRaidTick();
                break;
            case INITIALIZING_WAVE:
                initializingWaveTick();
                break;
            case ACTIVE_WAVE:
                activeWaveTick();
                break;
            case COMPLETE:
                completeRaidTick();
                break;
            case FAILED:
                failureRaidTick();
        }
    }

    private void inactiveRaidTick()
    {
        SculkHorde.savedData.incrementTicksSinceLastRaid();
        if(canRaidStart())
        {
            setRaidState(RaidState.INVESTIGATING_LOCATION);
        }
    }

    private void initializeBlockSearcher(int searchIterationsPerTick, int maxTargets)
    {
        areaOfInterestEntry = SculkHorde.savedData.getAreasOfInterestEntries().get(0);
        blockSearcher = new BlockSearcher(level, areaOfInterestEntry.getPosition());
        blockSearcher.setMaxDistance(getCurrentRaidRadius());
        blockSearcher.setDebugMode(SculkHorde.isDebugMode());
        blockSearcher.searchIterationsPerTick = searchIterationsPerTick;
        blockSearcher.ignoreBlocksNearTargets = true;

        // What is the target?
        blockSearcher.setTargetBlockPredicate(blockPos -> {
            boolean isTarget = level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY)
                    || level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY)
                    || level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);

            // If the target is outside of the current raid radius, increase the raid radius
            if (isTarget && BlockAlgorithms.getBlockDistance(areaOfInterestEntry.getPosition(), blockPos) > getCurrentRaidRadius()) {
                currentRaidRadius = (int) BlockAlgorithms.getBlockDistance(getRaidLocation(), blockPos);
                SculkHorde.LOGGER.debug("Raid Radius is now " + getCurrentRaidRadius() + " blocks.");
            }

            return isTarget;
        });

        // What is obstructed?
        blockSearcher.setObstructionPredicate(blockPos -> {
            if(blockSearcher.foundTargets.size() == 0 && BlockAlgorithms.getBlockDistance(areaOfInterestEntry.getPosition(), blockPos) > MAXIMUM_RAID_RADIUS)
            {
                return true;
            }

            if(blockSearcher.foundTargets.size() > 0 && !blockSearcher.isAnyTargetCloserThan(blockPos, 50))
            {
                return true;
            }

            if(level.getBlockState(blockPos).is(Blocks.AIR))
            {
                return true;
            }
            return !BlockAlgorithms.isExposedToAir(level, blockPos);
        });

        blockSearcher.MAX_TARGETS = maxTargets;
    }

    private void getFoundTargetsFromBlockSearcher(ArrayList<BlockPos> foundTargets)
    {
        high_priority_targets.clear();
        medium_priority_targets.clear();
        low_priority_targets.clear();

        for (BlockPos blockPos : foundTargets)
        {
            if (level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY))
            {
                high_priority_targets.add(blockPos);
            }
            else if (level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY))
            {
                medium_priority_targets.add(blockPos);
            }
            else if (level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY))
            {
                low_priority_targets.add(blockPos);
            }
        }

        // Sort the targets by distance to origin
        high_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation())));

        // Sort the targets by distance to origin
        medium_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation())));

        // Sort the targets by distance to origin
        low_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation())));
    }

    private void investigatingLocationTick()
    {
        if(SculkHorde.savedData.getAreasOfInterestEntries().isEmpty())
        {
            setRaidStateToFailure(failureType.FAILED_INITIALIZATION);
            return;
        }

        // Initialize Block Searcher if null
        if(blockSearcher == null)
        {
            initializeBlockSearcher(100, 30);
        }

        // Tick Block Searcher
        blockSearcher.tick();

        // If the block searcher is not finished, return.
        if(!blockSearcher.isFinished) { return; }

        // If we find block targets, store them.
        if(blockSearcher.isSuccessful)
        {
            getFoundTargetsFromBlockSearcher(blockSearcher.foundTargets);
            maxWaves = 10;
            setRaidLocation(areaOfInterestEntry.getPosition());
            SculkHorde.LOGGER.debug("RaidHandler | Found " + (high_priority_targets.size() + medium_priority_targets.size() + low_priority_targets.size()) + " objective targets.");
            setRaidState(RaidState.ENDERMAN_SCOUTING);
        }
        else
        {
            setRaidStateToFailure(failureType.FAILED_INITIALIZATION);
            SculkHorde.LOGGER.debug("RaidHandler | Found no objective targets. Not Initializing Raid.");
        }
        blockSearcher = null;
    }

    private void endermanScoutingTick()
    {
        timeElapsedScouting++;

        if(scoutEnderman == null)
        {
            scoutEnderman = new SculkEndermanEntity(level, areaOfInterestEntry.getPosition());
            level.addFreshEntity(scoutEnderman);
            scoutEnderman.setInvestigatingPossibleRaidLocation(true);
            announceToPlayersInRange(Component.literal("A Sculk Infested Enderman is scouting out a possible raid location. Keep an eye out."), getCurrentRaidRadius() * 8);
        }

        if(!scoutEnderman.isAlive())
        {
            setRaidStateToFailure(failureType.ENDERMAN_DEFEATED);
            return;
        }

        if(timeElapsedScouting >= SCOUTING_DURATION)
        {
            setRaidState(RaidState.INITIALIZING_RAID);
            scoutEnderman.discard();
            scoutEnderman = null;
        }
    }

    /**
     * This function gets called when the raid is initialized.
     * It calculates the center of the raid, finds a spawn point
     * for the raid, and then chuckloads it.
     */
    private void initializingRaidTick()
    {
        SculkHorde.savedData.setTicksSinceLastRaid(0);
        int MAX_SEARCH_DISTANCE = getCurrentRaidRadius();

        if(blockSearcher == null)
        {
            // Send message to all players

            // Calculate centroid of all targets
            ArrayList<BlockPos> allTargets = new ArrayList<>();
            allTargets.addAll(high_priority_targets);
            allTargets.addAll(medium_priority_targets);
            allTargets.addAll(low_priority_targets);

            //
            if(allTargets.size() == 0)
            {
                setRaidState(RaidState.INITIALIZING_RAID);
                return;
            }
            raidCenter = BlockAlgorithms.getCentroid(allTargets);
            allTargets.clear();

            blockSearcher = new BlockSearcher(level, getRaidLocation());
            blockSearcher.setMaxDistance(MAX_SEARCH_DISTANCE);
            blockSearcher.setTargetBlockPredicate(isSpawnTarget);
            blockSearcher.setObstructionPredicate(isSpawnObstructed);
            blockSearcher.setMaxTargets(1);
            blockSearcher.setPositionToMoveAwayFrom(raidCenter);
            blockSearcher.setDebugMode(SculkHorde.isDebugMode());


            // Load chunks
            ChunkLoaderHelper.forceLoadChunksInRadius(level, getRaidLocation(), getRaidLocation().getX() >> 4, getRaidLocation().getZ() >> 4, currentRaidRadius /16 + 1);

        }

        // Tick the Block Searcher
        blockSearcher.tick();

        // If Completed, and was successful
        if(blockSearcher.isFinished && blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.INITIALIZING_WAVE);
            SculkHorde.LOGGER.debug("RaidHandler | Found Spawn Location. Initializing Raid.");

            setNextObjectiveLocation();

            announceToPlayersInRange(Component.literal("Sculk Raid Commencing at: " + getRaidLocation()), getCurrentRaidRadius() * 8);

        }
        // If Completed, and was not successful
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.FAILED);
            SculkHorde.LOGGER.debug("RaidHandler | Unable to Find Spawn Location. Not Initializing Raid.");
        }
    }

    private void playSoundForEachPlayerInRange(SoundEvent soundEvent, float volume, float pitch, int range)
    {
        // Play sound for each player
        level.players().forEach(player ->
        {
            if (BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= range || SculkHorde.isDebugMode())
            {
                level.playSound(null, player.blockPosition(), soundEvent, SoundSource.HOSTILE, volume, pitch);
            }
        });
    }

    private void spawnWaveParticipants(BlockPos spawnLocation)
    {
        waveParticipants.forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            level.addFreshEntity((Entity) raidParticipant);
            ((Mob) raidParticipant).addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0));
        });
    }

    private void initializingWaveTick()
    {
        waveDuration = 0;
        currentWavePattern = getWavePattern();


        BlockPos spawnLocation = blockSearcher.foundTargets.get(0);

        populateRaidParticipants(spawnLocation);

        announceToPlayersInRange(Component.literal(" Starting Wave " + currentWave + " out of " + maxWaves + "."), getCurrentRaidRadius() * 8);

        spawnWaveParticipants(spawnLocation);

        playSoundForEachPlayerInRange(SoundRegistry.RAID_START_SOUND.get(), 1.0F, 1.0F, getCurrentRaidRadius() * 4);

        if(objectiveLocationAtStartOfWave.equals(objectiveLocation))
        {
            setNextObjectiveLocation();
        }
        objectiveLocationAtStartOfWave = objectiveLocation;
        SculkHorde.LOGGER.debug("RaidHandler | Spawning mobs at: " + spawnLocation);
        setRaidState(RaidState.ACTIVE_WAVE);
    }

    /**
     * If on last wave, end raid. Otherwise, go to next wave.
     */
    protected void endWave()
    {
        // Otherwise, go to next wave
        currentWave++;

        // If we are on last wave, end raid
        if(currentWave >= maxWaves)
        {
            setRaidStateToFailure(failureType.FAILED_OBJECTIVE_COMPLETION);

            announceToPlayersInRange(Component.literal("Final Wave Complete."), getCurrentRaidRadius() * 8);
        }

        announceToPlayersInRange(Component.literal("Wave " + (currentWave-1) + " complete."), getCurrentRaidRadius() * 8);

        setRaidState(RaidState.INITIALIZING_WAVE);
    }

    protected void activeWaveTick()
    {
        updateRemainingWaveParticipantsAmount();
        waveDuration++;

        // If wave has been going on for too long, end it
        if(waveDuration >= MAX_WAVE_DURATION)
        {
            endWave();
            removeWaveParticipantsFromList();
        }

        // End Wave if all participants are dead
        if(areWaveParticipantsDead())
        {
            endWave();
        }

        if(isCurrentObjectiveCompleted())
        {
            setNextObjectiveLocation();

            announceToAllPlayers(Component.literal("The Sculk Horde has Successfully Destroyed an Objective!"));

            level.players().forEach((player) -> level.playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 1.0F));
        }
    }

    private void completeRaidTick()
    {
        announceToAllPlayers(Component.literal("The Sculk Horde's raid was successful!"));
        // Summon Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(EntityRegistry.SCULK_SPORE_SPEWER.get(), level);
        sporeSpewer.setPos(getRaidLocation().getX(), getRaidLocation().getY(), getRaidLocation().getZ());
        level.addFreshEntity(sporeSpewer);
        reset();
    }

    private void failureRaidTick()
    {
        // Switch Statement for Failure Type
        switch (failure)
        {
            case FAILED_OBJECTIVE_COMPLETION:
                announceToAllPlayers(Component.literal("The Sculk Horde has failed to destroy all objectives!"));
                level.players().forEach((player) -> level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 1.0F));
                break;
            case ENDERMAN_DEFEATED:
                announceToAllPlayers(Component.literal("The Sculk Horde has failed to scout out a potential raid location. Raid Prevented!"));
                level.players().forEach((player) -> level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 1.0F));
                break;
        }

        reset();
    }

    private Predicate<EntityFactoryEntry> isValidRaidParticipant(EntityFactory.StrategicValues strategicValue)
    {
        return (entityFactoryEntry) -> entityFactoryEntry.getCategory() == strategicValue;
    }

    public EntityFactory.StrategicValues[] getWavePattern()
    {
        EntityFactory.StrategicValues[][] possibleWavePatterns = {DefaultRaidWavePatterns.FIVE_RANGED_FIVE_MELEE, DefaultRaidWavePatterns.TEN_RANGED, DefaultRaidWavePatterns.TEN_MELEE};
        Random random = new Random();
        return possibleWavePatterns[random.nextInt(possibleWavePatterns.length)];
    }

    private void populateRaidParticipants(BlockPos spawnLocation)
    {
        for(int i = 0; i < getWavePattern().length; i++)
        {
            Optional<EntityFactoryEntry> randomEntry = EntityFactory.getRandomEntry(isValidRaidParticipant(getWavePattern()[i]));
            if(randomEntry.isEmpty())
            {
                SculkHorde.LOGGER.debug("RaidHandler | Unable to find valid entity for raid.");
                setRaidState(RaidState.INITIALIZING_RAID);
                return;
            }
            waveParticipants.add((ISculkSmartEntity) randomEntry.get().createEntity(level, spawnLocation));
        }

        // Add 5 Creepers
        for(int i = 0; i < 6; i++)
        {
            SculkCreeperEntity creeper = EntityRegistry.SCULK_CREEPER.get().create(level);
            creeper.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            waveParticipants.add(creeper);
        }

        if(currentWave == maxWaves)
        {
            Mob boss = EntityRegistry.SCULK_ENDERMAN.get().create(level);
            boss.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            waveParticipants.add((ISculkSmartEntity) boss);
        }
    }
}
