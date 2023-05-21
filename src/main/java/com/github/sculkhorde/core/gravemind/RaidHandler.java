package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;
import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidHandler {

    public static int COOLDOWN_BETWEEN_RAIDS = TickUnits.convertMinutesToTicks(1); // TODO INCREASE COOLDOWN
    protected int MAX_WAVE_DURATION = TickUnits.convertMinutesToTicks(1);
    protected int waveDuration = 0;

    // Raid Variables
    protected ServerLevel level;
    protected BlockPos raidLocation = BlockPos.ZERO;
    protected BlockPos objectiveLocation = BlockPos.ZERO;
    protected BlockPos raidCenter = BlockPos.ZERO;
    protected int raidRadius = 150;
    protected ArrayList<ISculkSmartEntity> waveParticipants = new ArrayList<>();
    public enum RaidState {
        INACTIVE,
        INVESTIGATING_LOCATION,
        INITIALIZING_RAID,
        INITIALIZING_WAVE,
        ACTIVE_WAVE,
        COMPLETE,
        FAILED
    }
    private RaidState raidState = RaidState.INACTIVE;

    // Waves
    private EntityFactory.StrategicValues[] currentWavePattern;
    private int maxWaves = 2;
    private int currentWave = 0;
    private int remainingWaveParticipants = 0;

    protected ModSavedData.AreaofInterestEntry areaOfInterestEntry;

    // Targets
    private static ArrayList<BlockPos> high_priority_targets = new ArrayList();
    private static ArrayList<BlockPos> medium_priority_targets = new ArrayList();
    private static ArrayList<BlockPos> low_priority_targets = new ArrayList();

    // Block Searcher
    private BlockSearcher blockSearcher;
    private Predicate<BlockPos> isSpawnObstructed = (blockPos) ->
    {
        // If block isnt solid, its obstructed
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

        if(!level.getBlockState(blockPos.above()).canBeReplaced() || level.getBlockState(blockPos.above(2)).is(Blocks.WATER) || level.getBlockState(blockPos.above(2)).is(Blocks.LAVA))
        {
            return true;
        }
        return false;
    };

    private Predicate<BlockPos> isSpawnTarget = (blockPos) ->
    {
        if( BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (raidRadius * 0.75) )
        {
            return true;
        }

        return false;
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
        if(gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped)
        {
            return false;
        }

        if(!SculkHorde.savedData.isRaidCooldownOver())
        {
            return false;
        }

        if(SculkHorde.savedData.getAreasOfInterestEntries().isEmpty())
        {
            return false;
        }

        return true;
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
     * Gets the raid radius
     * @return the raid radius
     */
    public int getRaidRadius() {
        return raidRadius;
    }

    /**
     * Sets the raid radius
     * @param raidRadiusIn the raid radius
     */
    public void setRaidRadius(int raidRadiusIn) {
        raidRadius = raidRadiusIn;
    }

    /**
     * Checks if all raid participants are alive
     * @return true if all raid participants are alive, false otherwise
     */
    public boolean areWaveParticipantsDead() {
        return remainingWaveParticipants <= 0;
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

    private void investigatingLocationTick()
    {
        if(SculkHorde.savedData.getAreasOfInterestEntries().isEmpty())
        {
            setRaidState(RaidState.FAILED);
            return;
        }

        // Initialize Block Searcher
        if(blockSearcher == null)
        {
            areaOfInterestEntry = SculkHorde.savedData.getAreasOfInterestEntries().get(0);
            blockSearcher = new BlockSearcher(level, areaOfInterestEntry.getPosition());
            blockSearcher.setMaxDistance(getRaidRadius());
            blockSearcher.setDebugMode(DEBUG_MODE);
            blockSearcher.searchIterationsPerTick = 100;
            blockSearcher.ignoreBlocksNearTargets = true;

            blockSearcher.setTargetBlockPredicate(new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos blockPos) {
                    return level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_HIGH_PRIORITY)
                            || level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_LOW_PRIORITY)
                            || level.getBlockState(blockPos).is(BlockRegistry.Tags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);
                }
            });

            blockSearcher.setObstructionPredicate(new Predicate<BlockPos>()
            {
                @Override
                public boolean test(BlockPos blockPos)
                {
                    if(level.getBlockState(blockPos).is(Blocks.AIR))
                    {
                        return true;
                    }
                    if(!BlockAlgorithms.isExposedToAir(level, blockPos))
                    {
                        return true;
                    }
                    return false;
                }
            });

            blockSearcher.MAX_TARGETS = 10;
        }

        // Tick Block Searcher
        blockSearcher.tick();

        // If the block searcher is not finished, return.
        if(!blockSearcher.isFinished) { return; }

        // If we find block targets, store them.
        if(blockSearcher.isSuccessful)
        {
            high_priority_targets.clear();
            medium_priority_targets.clear();
            low_priority_targets.clear();

            for (BlockPos blockPos : blockSearcher.foundTargets)
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
            high_priority_targets.sort(new Comparator<BlockPos>()
            {
                @Override
                public int compare(BlockPos blockPos, BlockPos t1)
                {
                    return (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation()));
                }
            });

            // Sort the targets by distance to origin
            medium_priority_targets.sort(new Comparator<BlockPos>()
            {
                @Override
                public int compare(BlockPos blockPos, BlockPos t1)
                {
                    return (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation()));
                }
            });

            // Sort the targets by distance to origin
            low_priority_targets.sort(new Comparator<BlockPos>()
            {
                @Override
                public int compare(BlockPos blockPos, BlockPos t1)
                {
                    return (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation()));
                }
            });

            maxWaves = high_priority_targets.size() + medium_priority_targets.size() + low_priority_targets.size();
            setRaidLocation(areaOfInterestEntry.getPosition());
            SculkHorde.LOGGER.debug("RaidHandler | Found " + maxWaves + " objective targets.");
            setRaidState(RaidState.INITIALIZING_RAID);
        }
        else
        {
            setRaidState(RaidState.FAILED);
            SculkHorde.LOGGER.debug("RaidHandler | Found no objective targets. Not Initializing Raid.");
        }
        blockSearcher = null;
    }

    private void initializingRaidTick()
    {
        SculkHorde.savedData.setTicksSinceLastRaid(0);
        int MAX_SEARCH_DISTANCE = getRaidRadius();

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
                setRaidState(RaidState.FAILED);
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
            blockSearcher.setDebugMode(DEBUG_MODE);


            // Load chunks
            ChunkLoaderHelper.forceLoadChunksInRadius(level, getRaidLocation(), getRaidLocation().getX() >> 4, getRaidLocation().getZ() >> 4, 5);

        }

        // Tick the Block Searcher
        blockSearcher.tick();

        // If Completed, and was successful
        if(blockSearcher.isFinished && blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.INITIALIZING_WAVE);
            SculkHorde.LOGGER.debug("RaidHandler | Found Spawn Location. Initializing Raid.");

            level.players().forEach((player) -> {
                if(BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= raidRadius * 4 || SculkHorde.DEBUG_MODE)
                {
                    player.displayClientMessage(Component.literal("Sculk Raid Commencing at: " + getRaidLocation()), false);
                }
            });
        }
        // If Completed, and was not successful
        else if(blockSearcher.isFinished && !blockSearcher.isSuccessful)
        {
            setRaidState(RaidState.FAILED);
            SculkHorde.LOGGER.debug("RaidHandler | Unable to Find Spawn Location. Not Initializing Raid.");
        }
    }

    private void initializingWaveTick()
    {
        waveDuration = 0;
        currentWavePattern = getWavePattern();
        Optional<BlockPos> objectiveOptional = popObjectiveLocation();
        if(objectiveOptional.isPresent())
        {
            objectiveLocation = objectiveOptional.get();
        }
        else
        {
            setRaidState(RaidState.FAILED);
        }

        BlockPos spawnLocation = blockSearcher.foundTargets.get(0);

        populateRaidParticipants(spawnLocation);

        level.players().forEach((player) ->
        {
            // If player is close
            if(BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= raidRadius * 4 || SculkHorde.DEBUG_MODE)
            {
                player.displayClientMessage(Component.literal("RaidHandler | Starting Wave " + currentWave + " out of " + maxWaves + "."), false);
            }
        });



        waveParticipants.forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            level.addFreshEntity((Entity) raidParticipant);
            ((Mob) raidParticipant).addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertHoursToTicks(1), 0));
        });

        // Play sound for each player
        level.players().forEach(player ->
        {
            if (BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= raidRadius * 4 || SculkHorde.DEBUG_MODE)
            {
                level.playSound(null, player.blockPosition(), SoundRegistry.RAID_START_SOUND.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
            }
        });

        SculkHorde.LOGGER.debug("RaidHandler | Spawning mobs at: " + blockSearcher.currentPosition);
        setRaidState(RaidState.ACTIVE_WAVE);
    }

    /**
     * If on last wave, end raid. Otherwise, go to next wave.
     */
    protected void endWave()
    {
        // If we are on last wave, end raid
        if(currentWave == maxWaves)
        {
            setRaidState(RaidState.COMPLETE);
            //Send message to all players
            level.players().forEach((player) -> {
                // If player is close
                if(BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= raidRadius * 4 || SculkHorde.DEBUG_MODE)
                {
                    player.displayClientMessage(Component.literal("RaidHandler | Completed Final Wave."), false);
                }
            });
            return;
        }

        // Otherwise, go to next wave
        currentWave++;

        //Send message to all players
        level.players().forEach((player) -> {
            // If player in range
            if(BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= raidRadius * 4 || SculkHorde.DEBUG_MODE)
            {
                player.displayClientMessage(Component.literal("RaidHandler | Wave " + (currentWave-1) + " complete."), false);
            }
        });

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
    }

    private void completeRaidTick()
    {
        reset();
    }

    private void failureRaidTick()
    {
        reset();
    }

    private Predicate<EntityFactoryEntry> isValidRaidParticipant(EntityFactory.StrategicValues strategicValue)
    {
        return (entityFactoryEntry) -> {
            return entityFactoryEntry.getCategory() == strategicValue;
        };
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
            EntityFactoryEntry randomEntry = EntityFactory.getRandomEntry(isValidRaidParticipant(getWavePattern()[i]));
            waveParticipants.add((ISculkSmartEntity) randomEntry.createEntity(level, spawnLocation));
        }
    }
}
