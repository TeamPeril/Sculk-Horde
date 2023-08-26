package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
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
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidHandler {

    public static RaidData raidData;

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

    protected enum failureType {
        NONE,
        FAILED_INITIALIZATION,
        ENDERMAN_DEFEATED,
        FAILED_OBJECTIVE_COMPLETION
    }

    public RaidHandler(ServerLevel levelIn)
    {
        if(raidData == null) { raidData = new RaidData(); }
        raidData.setLevel(levelIn);
    }

    public boolean canRaidStart()
    {
        if(!ModConfig.SERVER.experimental_features_enabled.get())
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

        if(SculkHorde.savedData.getAreasOfInterestEntries().isEmpty())
        {
            return false;
        }

        if(SculkHorde.savedData.getAreaOfInterestEntryNotInNoRaidZone().isEmpty())
        {
            return false;
        }

        return true;
    }



    private void announceToPlayersInRange(Component message, int range)
    {
        raidData.getLevel().players().forEach((player) -> {
            if(BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= range)
            {
                player.displayClientMessage(message, false);
            }
        });
    }

    public void announceToAllPlayers(Component message)
    {
        raidData.getLevel().players().forEach((player) -> player.displayClientMessage(message, false));
    }




    public boolean isCurrentObjectiveCompleted()
    {
        if(raidData.getLevel().getBlockState(raidData.getObjectiveLocation()).is(BlockRegistry.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return false;
        }
        else if(raidData.getLevel().getBlockState(raidData.getObjectiveLocation()).is(BlockRegistry.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return false;
        }
        else return !raidData.getLevel().getBlockState(raidData.getObjectiveLocation()).is(BlockRegistry.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY);
    }


    // Events

    public void bossBarTick(){
        if(raidData.getRaidState() != RaidState.ACTIVE_WAVE && raidData.getRaidState() != RaidState.INITIALIZING_WAVE)
        {
            return;
        }

        if(raidData.getBossEvent() == null)
        {
            raidData.setBossEvent(new ServerBossEvent(Component.literal("Sculk Raid Wave " + raidData.getCurrentWave() + " / " + raidData.getMaxWaves()), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS));
            raidData.getBossEvent().setCreateWorldFog(true);
            raidData.getBossEvent().setDarkenScreen(true);
        }

        //Add players to event as necessary
        raidData.getLevel().players().forEach((player) -> {

            boolean isPlayerInListAlready = raidData.getBossEvent().getPlayers().contains(player);
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= raidData.getCurrentRaidRadius() * 2;

            if(!isPlayerInListAlready && isPlayerInRangeOfRaid)
            {

                raidData.getBossEvent().addPlayer(player);
            }
        });

        // Remove players from event as necessary
        raidData.getBossEvent().getPlayers().forEach((player) -> {
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= raidData.getCurrentRaidRadius() * 2;
            if(!isPlayerInRangeOfRaid)
            {
                raidData.getBossEvent().removePlayer(player);
            }
        });


        if(raidData.getRaidState() == RaidState.INITIALIZING_WAVE)
        {
            raidData.getBossEvent().setProgress(0.0F);
            raidData.getBossEvent().setName(Component.literal("Sculk Raid Wave " + raidData.getCurrentWave() + " / " + raidData.getMaxWaves()));
        }
        else
        {
            raidData.getBossEvent().setProgress(raidData.getWaveProgress());
        }
    }

    public void raidTick()
    {
        bossBarTick();
        switch (raidData.getRaidState())
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
            raidData.setRaidState(RaidState.INVESTIGATING_LOCATION);
        }
    }

    private void initializeBlockSearcherForInvestigateLocation(int searchIterationsPerTick, int maxTargets)
    {
        if(raidData.getAreaOfInterestEntry() == null)
        {
            Optional<ModSavedData.AreaofInterestEntry> possibleEntry = SculkHorde.savedData.getAreaOfInterestEntryNotInNoRaidZone();
            if(possibleEntry.isEmpty())
            {
                raidData.setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            raidData.setAreaOfInterestEntry(possibleEntry.get());
        }
        raidData.setBlockSearcher(new BlockSearcher(raidData.getLevel(), raidData.getAreaOfInterestEntry().getPosition()));
        raidData.getBlockSearcher().setMaxDistance(raidData.getCurrentRaidRadius());
        //raidData.getBlockSearcher().setDebugMode(SculkHorde.isDebugMode());
        raidData.getBlockSearcher().searchIterationsPerTick = searchIterationsPerTick;
        raidData.getBlockSearcher().ignoreBlocksNearTargets = true;

        // What is the target?
        raidData.getBlockSearcher().setTargetBlockPredicate(raidData.isTargetInvestigateLocationState);

        // What is obstructed?
        raidData.getBlockSearcher().setObstructionPredicate(raidData.isObstructedInvestigateLocationState);

        raidData.getBlockSearcher().MAX_TARGETS = maxTargets;
    }

    private void initializeBlockSearcherForSpawnSearch(int searchIterationsPerTick, int maxTargets)
    {
        raidData.setBlockSearcher(new BlockSearcher(raidData.getLevel(), raidData.getRaidLocation()));
        raidData.getBlockSearcher().setMaxDistance(raidData.getCurrentRaidRadius());
        raidData.getBlockSearcher().setTargetBlockPredicate(raidData.isSpawnTarget);
        raidData.getBlockSearcher().setObstructionPredicate(raidData.isSpawnObstructed);
        raidData.getBlockSearcher().setMaxTargets(1);
        raidData.getBlockSearcher().setPositionToMoveAwayFrom(raidData.getRaidCenter());
       // raidData.getBlockSearcher().setDebugMode(SculkHorde.isDebugMode());
        raidData.getBlockSearcher().searchIterationsPerTick = searchIterationsPerTick;
        raidData.getBlockSearcher().MAX_TARGETS = maxTargets;
    }

    private void investigatingLocationTick()
    {
        // Initialize Block Searcher if null
        if(raidData.getBlockSearcher() == null)
        {
            initializeBlockSearcherForInvestigateLocation(100, 30);
        }

        // Tick Block Searcher
        raidData.getBlockSearcher().tick();

        // If the block searcher is not finished, return.
        if(!raidData.getBlockSearcher().isFinished) { return; }

        // If we find block targets, store them.
        if(raidData.getBlockSearcher().isSuccessful)
        {
            raidData.getFoundTargetsFromBlockSearcher(raidData.getBlockSearcher().foundTargets);
            raidData.setMaxWaves(10);
            raidData.setRaidLocation(raidData.getAreaOfInterestEntry().getPosition());
            SculkHorde.LOGGER.debug("RaidHandler | Found " + (raidData.getHighPriorityTargets().size() + raidData.getMediumPriorityTargets().size() + raidData.getLowPriorityTargets().size()) + " objective targets.");
            raidData.setRaidState(RaidState.ENDERMAN_SCOUTING);
        }
        else
        {
            raidData.setFailure(failureType.FAILED_INITIALIZATION);
            SculkHorde.LOGGER.debug("RaidHandler | Found no objective targets. Not Initializing Raid.");
        }
        raidData.setBlockSearcher(null);
    }

    private void endermanScoutingTick()
    {
        raidData.incrementTimeElapsedScouting();

        if(raidData.getScoutEnderman() == null)
        {
            raidData.setScoutEnderman(new SculkEndermanEntity(raidData.getLevel(), raidData.getAreaOfInterestEntry().getPosition()));
            raidData.getLevel().addFreshEntity(raidData.getScoutEnderman());
            raidData.getScoutEnderman().setInvestigatingPossibleRaidLocation(true);
            SculkHorde.LOGGER.info("RaidHandler | Sculk Enderman Scouting at " + raidData.getAreaOfInterestEntry().getPosition() + " for " + raidData.getSCOUTING_DURATION() + " minutes");
            announceToPlayersInRange(Component.literal("A Sculk Infested Enderman is scouting out a possible raid location. Keep an eye out."), raidData.getCurrentRaidRadius() * 8);
        }

        if(!raidData.getScoutEnderman().isAlive())
        {
            raidData.setFailure(failureType.ENDERMAN_DEFEATED);
            return;
        }

        if(raidData.getTimeElapsedScouting() >= raidData.getSCOUTING_DURATION())
        {
            raidData.setRaidState(RaidState.INITIALIZING_RAID);
            raidData.getScoutEnderman().discard();
            raidData.setScoutEnderman(null);
        }
    }

    private void setRaidCenterToCentroidOfAllTargets()
    {
        // Calculate centroid of all targets
        ArrayList<BlockPos> allTargets = new ArrayList<>();
        allTargets.addAll(raidData.getHighPriorityTargets());
        allTargets.addAll(raidData.getMediumPriorityTargets());
        allTargets.addAll(raidData.getLowPriorityTargets());
        raidData.setRaidCenter(BlockAlgorithms.getCentroid(allTargets));
    }


    /**
     * This function gets called when the raid is initialized.
     * It calculates the center of the raid, finds a spawn point
     * for the raid, and then chuckloads it.
     */
    private void initializingRaidTick()
    {
        SculkHorde.savedData.setTicksSinceLastRaid(0);


        if(raidData.getBlockSearcher() == null)
        {

            if(raidData.getHighPriorityTargets().size() + raidData.getMediumPriorityTargets().size() + raidData.getLowPriorityTargets().size() <= 0)
            {
                raidData.setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            setRaidCenterToCentroidOfAllTargets();

            // Initialize Block Searcher
            initializeBlockSearcherForSpawnSearch(100, 1);

            // Load chunks
            ChunkLoaderHelper.forceLoadChunksInRadius(raidData.getLevel(), raidData.getRaidLocation(), raidData.getRaidLocation().getX() >> 4, raidData.getRaidLocation().getZ() >> 4, raidData.getCurrentRaidRadius() /16 + 1);

        }

        // Tick the Block Searcher
        raidData.getBlockSearcher().tick();

        if(!raidData.getBlockSearcher().isFinished)
        {
            return;
        }

        // If successful
        if(raidData.getBlockSearcher().isSuccessful)
        {
            raidData.setRaidState(RaidState.INITIALIZING_WAVE);
            SculkHorde.LOGGER.info("RaidHandler | Found Spawn Location. Initializing Raid.");

            raidData.setNextObjectiveLocation();
            raidData.setSpawnLocation(raidData.getBlockSearcher().foundTargets.get(0));

            raidData.setCurrentRaidRadius(raidData.getDistanceOfFurthestObjective());
            SculkHorde.LOGGER.debug("RaidHandler | Current Raid Radius: " + raidData.getCurrentRaidRadius());

            announceToPlayersInRange(Component.literal("Sculk Raid Commencing at: " + raidData.getRaidLocation()), raidData.getCurrentRaidRadius() * 8);

        }
        // If not successful
        else
        {
            raidData.setRaidState(RaidState.FAILED);
            SculkHorde.LOGGER.debug("RaidHandler | Unable to Find Spawn Location. Not Initializing Raid.");
        }
    }

    private void playSoundForEachPlayerInRange(SoundEvent soundEvent, float volume, float pitch, int range)
    {
        // Play sound for each player
        raidData.getLevel().players().forEach(player ->
        {
            if (BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= range || SculkHorde.isDebugMode())
            {
                raidData.getLevel().playSound(null, player.blockPosition(), soundEvent, SoundSource.HOSTILE, volume, pitch);
            }
        });
    }

    private void spawnWaveParticipants(BlockPos spawnLocation)
    {
        raidData.getWaveParticipants().forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getLevel().addFreshEntity((Entity) raidParticipant);
            ((Mob) raidParticipant).addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0));
        });
    }

    private void initializingWaveTick()
    {
        raidData.setWaveDuration(0);
        raidData.setCurrentWavePattern(getWavePattern());

        raidData.getWaveParticipants().clear();

        populateRaidParticipants(raidData.getSpawnLocation());

        announceToPlayersInRange(Component.literal(" Starting Wave " + raidData.getCurrentWave() + " out of " + raidData.getMaxWaves() + "."), raidData.getCurrentRaidRadius() * 8);

        spawnWaveParticipants(raidData.getSpawnLocation());

        playSoundForEachPlayerInRange(SoundRegistry.RAID_START_SOUND.get(), 1.0F, 1.0F, raidData.getCurrentRaidRadius() * 4);

        if(raidData.getObjectiveLocationAtStartOfWave().equals(raidData.getObjectiveLocation()))
        {
            raidData.setNextObjectiveLocation();
        }
        raidData.setObjectiveLocationAtStartOfWave(raidData.getObjectiveLocation());
        SculkHorde.LOGGER.debug("RaidHandler | Spawning mobs at: " + raidData.getSpawnLocation());
        raidData.setRaidState(RaidState.ACTIVE_WAVE);
    }

    protected boolean isLastWave(int offset)
    {
        return raidData.getCurrentWave() >= raidData.getMaxWaves() + offset;
    }

    /**
     * If on last wave, end raid. Otherwise, go to next wave.
     */
    protected void endWave()
    {
        // Otherwise, go to next wave
        raidData.incrementCurrentWave();

        // If we are on last wave, end raid
        if(isLastWave(1))
        {
            raidData.setFailure(failureType.FAILED_OBJECTIVE_COMPLETION);

            announceToPlayersInRange(Component.literal("Final Wave Complete."), raidData.getCurrentRaidRadius() * 8);
            return;
        }

        announceToPlayersInRange(Component.literal("Wave " + (raidData.getCurrentWave()) + " complete."), raidData.getCurrentRaidRadius() * 8);

        raidData.setRaidState(RaidState.INITIALIZING_WAVE);
    }

    protected void activeWaveTick()
    {
        raidData.updateRemainingWaveParticipantsAmount();

        raidData.incrementWaveDuration();

        // If wave has been going on for too long, end it
        if(raidData.getWaveDuration() >= raidData.getMAX_WAVE_DURATION())
        {
            endWave();
            raidData.removeWaveParticipantsFromList();
        }

        // End Wave if all participants are dead
        if(raidData.areWaveParticipantsDead())
        {
            endWave();
        }

        if(isCurrentObjectiveCompleted())
        {
            raidData.setNextObjectiveLocation();

            announceToAllPlayers(Component.literal("The Sculk Horde has Successfully Destroyed an Objective!"));

            raidData.getLevel().players().forEach((player) -> raidData.getLevel().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 1.0F));
        }
    }

    private void completeRaidTick()
    {
        SculkHorde.savedData.addNoRaidZoneToMemory(raidData.getRaidLocation());
        SculkHorde.LOGGER.info("RaidHandler | Raid Complete.");
        announceToAllPlayers(Component.literal("The Sculk Horde's raid was successful!"));
        // Summon Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(EntityRegistry.SCULK_SPORE_SPEWER.get(), raidData.getLevel());
        sporeSpewer.setPos(raidData.getRaidLocation().getX(), raidData.getRaidLocation().getY(), raidData.getRaidLocation().getZ());
        raidData.getLevel().addFreshEntity(sporeSpewer);
        raidData.reset();
    }

    private void failureRaidTick()
    {
        // Switch Statement for Failure Type
        switch (raidData.getFailure())
        {
            case FAILED_OBJECTIVE_COMPLETION:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Objectives Not Destroyed.");
                announceToAllPlayers(Component.literal("The Sculk Horde has failed to destroy all objectives!"));
                raidData.getLevel().players().forEach((player) -> raidData.getLevel().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 1.0F));
                break;
            case ENDERMAN_DEFEATED:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Sculk Enderman Defeated.");
                announceToAllPlayers(Component.literal("The Sculk Horde has failed to scout out a potential raid location. Raid Prevented!"));
                raidData.getLevel().players().forEach((player) -> raidData.getLevel().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 1.0F));
                break;
        }

        if(raidData.getRaidLocation() != null && raidData.getRaidLocation() != BlockPos.ZERO && raidData.getRaidLocation() != null)
        {
            SculkHorde.savedData.addNoRaidZoneToMemory(raidData.getRaidLocation());
        }

        raidData.reset();
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
                raidData.setRaidState(RaidState.INITIALIZING_RAID);
                return;
            }
            raidData.getWaveParticipants().add((ISculkSmartEntity) randomEntry.get().createEntity(raidData.getLevel(), spawnLocation));
        }

        // Add 5 Creepers
        for(int i = 0; i < 6; i++)
        {
            SculkCreeperEntity creeper = EntityRegistry.SCULK_CREEPER.get().create(raidData.getLevel());
            creeper.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getWaveParticipants().add(creeper);
        }

        if(isLastWave(0))
        {
            Mob boss = EntityRegistry.SCULK_ENDERMAN.get().create(raidData.getLevel());
            boss.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getWaveParticipants().add((ISculkSmartEntity) boss);
        }
    }
}
