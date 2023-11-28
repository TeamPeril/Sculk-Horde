package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockSearcher;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidHandler {

    public static RaidData raidData;

    private BlockPos scoutingLocation;

    private long MINIMUM_WAVE_LENGTH_TICKS = TickUnits.convertMinutesToTicks(2);


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
        FAILED_OBJECTIVE_COMPLETION,

        FAILED_TO_LOAD_CHUNKS
    }

    public RaidHandler(ServerLevel levelIn)
    {
        if(raidData == null) { raidData = new RaidData(); }
        raidData.setDimension(levelIn.dimension());
    }

    // Getters and Setters

    public boolean canRaidStart()
    {
        if(!ModConfig.SERVER.sculk_raid_enabled.get())
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

        //If there are players on server
        if(ServerLifecycleHooks.getCurrentServer().getPlayerCount() <= 0)
        {
            return false;
        }

        return true;
    }

    public boolean isRaidInactive()
    {
        return raidData.getRaidState() == RaidState.INACTIVE;
    }

    private String getFormattedCoordinates(BlockPos pos)
    {
        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }

    private String getFormattedDimension(ResourceKey<Level> dimension)
    {
        String languageKey = dimension.location().toShortLanguageKey();
        return Component.translatable(languageKey).getString();
    }

    private void announceToPlayersInRange(Component message, int range)
    {
        raidData.getDimension().players().forEach((player) ->
        {
            if(BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= range)
            {
                player.displayClientMessage(message, false);
            }
        });
    }
    public boolean isCurrentObjectiveCompleted()
    {
        if(raidData.getDimension().getBlockState(raidData.getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return false;
        }
        else if(raidData.getDimension().getBlockState(raidData.getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return false;
        }
        else return !raidData.getDimension().getBlockState(raidData.getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY);
    }

    // Chunk Loading

    public boolean isScoutingLocationLoaded()
    {
        return raidData.getDimension().isAreaLoaded(scoutingLocation, 3);
    }

    public boolean isRaidCenterLocationLoaded()
    {
        return raidData.getDimension().isAreaLoaded(raidData.getRaidCenter(), 5);
    }

    public boolean isSpawningLocationLoaded()
    {
        return raidData.getDimension().isAreaLoaded(raidData.getSpawnLocation(), 1);
    }

    public void loadRaidChunksCenter()
    {
        int distanceXBetweenRaidCenterAndSpawnPos = Math.abs(raidData.raidCenter.getX() - raidData.spawnLocation.getX());
        int distanceZBetweenRaidCenterAndSpawnPos = Math.abs(raidData.raidCenter.getZ() - raidData.spawnLocation.getZ());
        int lengthInBlocks = Math.max(distanceXBetweenRaidCenterAndSpawnPos, distanceZBetweenRaidCenterAndSpawnPos) * 2;
        int chunkLength = BlockAlgorithms.convertBlockLengthToChunkLength(lengthInBlocks);

        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(raidData.getRaidCenter(), raidData.getDimension());
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(raidData.getDimension(), raidData.getRaidCenter(), chunkLength, 2, TickUnits.convertHoursToTicks(1));
    }

    public void loadScoutingChunks()
    {
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(raidData.getDimension(), scoutingLocation, 3, 2, TickUnits.convertHoursToTicks(1));
    }

    public void loadSpawningChunks()
    {
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(raidData.getDimension(), raidData.getSpawnLocation(), 5, 2, TickUnits.convertHoursToTicks(1));
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

        Iterator<ServerPlayer> iterator = raidData.getDimension().players().iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            boolean isPlayerInListAlready = raidData.getBossEvent().getPlayers().contains(player);
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= Math.max(100, raidData.getCurrentRaidRadius() * 2);
            if (!isPlayerInListAlready && isPlayerInRangeOfRaid) {
                raidData.getBossEvent().addPlayer(player);
            }
        }

        // Remove players from event as necessary
        iterator = raidData.getBossEvent().getPlayers().iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= Math.max(100, raidData.getCurrentRaidRadius() * 2);
            if (!isPlayerInRangeOfRaid) {
                raidData.getBossEvent().removePlayer(player);
                break;
            }
        }


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
        if(raidData.getTicksSpentTryingToChunkLoad() > raidData.MAX_TICKS_SPENT_TRYING_TO_CHUNK_LOAD)
        {
            raidData.setFailure(failureType.FAILED_TO_LOAD_CHUNKS);
            return;
        }


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

            if(!possibleEntry.get().isEntryValid())
            {
                raidData.setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            raidData.setAreaOfInterestEntry(possibleEntry.get());

        }

        ModSavedData.AreaofInterestEntry areaOfInterestEntry = raidData.getAreaOfInterestEntry();
        ServerLevel dimension = areaOfInterestEntry.getDimension();
        ResourceKey<Level> dimensionResourceKey = dimension.dimension();
        raidData.setDimension(dimensionResourceKey);

        SculkHorde.LOGGER.info("RaidHandler | Investigating Location at: " + getFormattedCoordinates(areaOfInterestEntry.getPosition()) + " in dimension " + getFormattedDimension(dimensionResourceKey) + ".");

        raidData.setBlockSearcher(new BlockSearcher(dimension, areaOfInterestEntry.getPosition()));
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
        raidData.setBlockSearcher(new BlockSearcher(raidData.getDimension(), raidData.getRaidLocation()));
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
            SculkHorde.LOGGER.info("RaidHandler | Found " + (raidData.getHighPriorityTargets().size() + raidData.getMediumPriorityTargets().size()) + " objective targets in " + raidData.getAreaOfInterestEntry().getPosition() + " in dimension " + raidData.getDimension().dimension());
            raidData.setRaidState(RaidState.ENDERMAN_SCOUTING);
        }
        else
        {
            raidData.setFailure(failureType.FAILED_INITIALIZATION);
            SculkHorde.LOGGER.info("RaidHandler | Found no objective targets in dimension" + raidData.getDimensionResourceKey() +". Not Initializing Raid.");
        }
        raidData.setBlockSearcher(null);
    }

    private static void spawnSculkPhantomsAtTopOfWorld(ServerLevel level, BlockPos origin, int amount)
    {
        if(!ModConfig.SERVER.sculk_phantoms_enabled.get()) { return; }
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        for(int i = 0; i < amount; i++)
        {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int y = level.getMaxBuildHeight();
            BlockPos spawnPosition = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

            SculkPhantomEntity.spawnPhantom(level, spawnPosition);

        }
    }

    private void endermanScoutingTick()
    {
        if(scoutingLocation == null)
        {
            scoutingLocation = raidData.getAreaOfInterestEntry().getPosition();
            loadScoutingChunks();
            SculkHorde.LOGGER.info("RaidHandler | Scouting Location: " + getFormattedCoordinates(scoutingLocation));
            SculkHorde.LOGGER.info("RaidHandler | Scouting Location Loaded: " + isScoutingLocationLoaded());

        }

        if(!isScoutingLocationLoaded())
        {
            loadScoutingChunks();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
        }

        raidData.incrementTimeElapsedScouting();

        if(raidData.getScoutEnderman() == null)
        {
            SculkHorde.LOGGER.info("RaidHandler | Scouting Location Is Loaded. Continuing.");

            raidData.setScoutEnderman(new SculkEndermanEntity(raidData.getDimension(), scoutingLocation));
            raidData.getDimension().addFreshEntity(raidData.getScoutEnderman());
            raidData.getScoutEnderman().setScouting(true);
            SculkHorde.LOGGER.info("RaidHandler | Sculk Enderman Scouting at " + getFormattedCoordinates(raidData.areaOfInterestEntry.getPosition()) + " in the " + raidData.getDimensionResourceKey() + " for " + ModConfig.SERVER.sculk_raid_enderman_scouting_duration_minutes.get() + " minutes");
            announceToPlayersInRange(Component.literal("A Sculk Infested Enderman is scouting out a possible raid location at " + getFormattedCoordinates(raidData.areaOfInterestEntry.getPosition()) + " in the " + getFormattedDimension(raidData.getDimensionResourceKey()) +  ". Kill it to stop the raid from happening!"), raidData.getCurrentRaidRadius() * 8);
            raidData.getScoutEnderman().addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0));
            playSoundForEveryPlayer(ModSounds.RAID_SCOUT_SOUND.get(), 1.0F, 1.0F);

            //Spawn Sculk Phantoms
            spawnSculkPhantomsAtTopOfWorld(raidData.getDimension(), raidData.getAreaOfInterestEntry().getPosition(), 5);
        }

        if(!raidData.getScoutEnderman().isAlive())
        {
            raidData.setFailure(failureType.ENDERMAN_DEFEATED);
            return;
        }

        if(raidData.getTimeElapsedScouting() >= TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_enderman_scouting_duration_minutes.get()))
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
            SculkHorde.LOGGER.info("RaidHandler | Scouting Location Loaded: " + isScoutingLocationLoaded());


            if(raidData.getHighPriorityTargets().size() + raidData.getMediumPriorityTargets().size() <= 0)
            {
                raidData.setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            setRaidCenterToCentroidOfAllTargets();

            // Initialize Block Searcher
            initializeBlockSearcherForSpawnSearch(100, 1);


            SculkHorde.LOGGER.info("RaidHandler | Initializing Block Searcher");
        }

        //This is just in case we load in the middle of the raid
        scoutingLocation = raidData.getAreaOfInterestEntry().getPosition();

        if(!isScoutingLocationLoaded())
        {
            loadScoutingChunks();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
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
            SculkHorde.LOGGER.info("RaidHandler | Found Spawn Location at " + getFormattedCoordinates(raidData.getSpawnLocation()) + " in " + raidData.getBlockSearcher().getDimension().dimension() + ". Initializing Raid.");

            raidData.setNextObjectiveLocation();
            raidData.setSpawnLocation(raidData.getBlockSearcher().foundTargets.get(0));

            raidData.setCurrentRaidRadius(raidData.getDistanceOfFurthestObjective());
            SculkHorde.LOGGER.info("RaidHandler | Current Raid Radius: " + raidData.getCurrentRaidRadius());

            loadRaidChunksCenter();

            announceToPlayersInRange(Component.literal("The Sculk Horde is Raiding " + getFormattedCoordinates(raidData.getRaidLocation()) + " in the " + getFormattedDimension(raidData.getDimensionResourceKey()) + "!"), raidData.getCurrentRaidRadius() * 8);

        }
        // If not successful
        else
        {
            raidData.setRaidState(RaidState.FAILED);
            SculkHorde.LOGGER.info("RaidHandler | Unable to Find Spawn Location. Not Initializing Raid.");
        }
    }

    private void playSoundForEachPlayerInRange(SoundEvent soundEvent, float volume, float pitch, int range)
    {
        // Play sound for each player
        raidData.getDimension().players().forEach(player ->
        {
            if (BlockAlgorithms.getBlockDistanceXZ(raidData.getRaidLocation(), player.blockPosition()) <= range || SculkHorde.isDebugMode())
            {
                raidData.getDimension().playSound(null, player.blockPosition(), soundEvent, SoundSource.HOSTILE, volume, pitch);
            }
        });
    }

    private void playSoundForEveryPlayer(SoundEvent soundEvent, float volume, float pitch)
    {
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(player -> raidData.getDimension().playSound(null, player.blockPosition(), soundEvent, SoundSource.HOSTILE, volume, pitch));
    }

    private void spawnWaveParticipants(BlockPos spawnLocation)
    {
        // Spawn Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(ModEntities.SCULK_SPORE_SPEWER.get(), raidData.getDimension());
        sporeSpewer.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
        raidData.getDimension().addFreshEntity(sporeSpewer);

        raidData.getWaveParticipants().forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getDimension().addFreshEntity((Entity) raidParticipant);
            ((Mob) raidParticipant).addEffect(new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0));
        });
    }
    private void initializingWaveTick()
    {
        raidData.setWaveDuration(0);
        raidData.setCurrentWavePattern(getWavePattern());

        if(!isSpawningLocationLoaded())
        {
            loadSpawningChunks();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
        }

        if(!isRaidCenterLocationLoaded())
        {
            loadRaidChunksCenter();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
        }

        populateRaidParticipants(raidData.getSpawnLocation());

        announceToPlayersInRange(Component.literal(" Starting Wave " + raidData.getCurrentWave() + " out of " + raidData.getMaxWaves() + "."), raidData.getCurrentRaidRadius() * 8);

        spawnWaveParticipants(raidData.getSpawnLocation());

        playSoundForEachPlayerInRange(ModSounds.RAID_START_SOUND.get(), 1.0F, 1.0F, raidData.getCurrentRaidRadius() * 4);

        if(raidData.getObjectiveLocationAtStartOfWave().equals(raidData.getObjectiveLocation()))
        {
            raidData.setNextObjectiveLocation();
        }
        raidData.setObjectiveLocationAtStartOfWave(raidData.getObjectiveLocation());
        SculkHorde.LOGGER.info("RaidHandler | Spawning mobs at: " + raidData.getSpawnLocation());
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
        if(!isSpawningLocationLoaded())
        {
            loadSpawningChunks();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
        }

        if(!isRaidCenterLocationLoaded())
        {
            loadRaidChunksCenter();
            raidData.incrementTicksSpentTryingToChunkLoad();
            return;
        }

        raidData.updateRemainingWaveParticipantsAmount();

        raidData.incrementWaveDuration();

        // If wave has been going on for too long, end it
        if(raidData.getWaveDuration() >= raidData.getMAX_WAVE_DURATION() && raidData.getWaveDuration() >= MINIMUM_WAVE_LENGTH_TICKS)
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

            raidData.getDimension().players().forEach((player) -> raidData.getDimension().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 1.0F));
        }
    }

    private void completeRaidTick()
    {
        SculkHorde.savedData.addNoRaidZoneToMemory(raidData.getDimension(), raidData.getRaidLocation());
        SculkHorde.LOGGER.info("RaidHandler | Raid Complete.");
        announceToPlayersInRange(Component.literal("The Sculk Horde's raid was successful!"), raidData.getCurrentRaidRadius() * 8);
        // Summon Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(ModEntities.SCULK_SPORE_SPEWER.get(), raidData.getDimension());
        sporeSpewer.setPos(raidData.getRaidLocation().getX(), raidData.getRaidLocation().getY(), raidData.getRaidLocation().getZ());
        raidData.getDimension().addFreshEntity(sporeSpewer);
        raidData.reset();
    }

    private void failureRaidTick()
    {
        // Switch Statement for Failure Type
        switch (raidData.getFailure())
        {
            case FAILED_OBJECTIVE_COMPLETION:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Objectives Not Destroyed.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to destroy all objectives!"), raidData.getCurrentRaidRadius() * 8);
                raidData.getDimension().players().forEach((player) -> raidData.getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case ENDERMAN_DEFEATED:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Sculk Enderman Defeated.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to scout out a potential raid location. Raid Prevented!"), raidData.getCurrentRaidRadius() * 8);
                raidData.getDimension().players().forEach((player) -> raidData.getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case FAILED_INITIALIZATION:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Unable to Initialize.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to find a suitable way to raid the location. Raid Prevented!"), raidData.getCurrentRaidRadius() * 8);
                raidData.getDimension().players().forEach((player) -> raidData.getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case FAILED_TO_LOAD_CHUNKS:
                SculkHorde.LOGGER.info("RaidHandler | Raid Failed. Unable to Load Chunks.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to load the chunks required to raid the location. Raid Prevented!"), raidData.getCurrentRaidRadius() * 8);
                raidData.getDimension().players().forEach((player) -> raidData.getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case NONE:
                SculkHorde.LOGGER.error("RaidHandler | Raid Failed. Unknown Reason.");
                break;
        }

        if(raidData.getRaidLocation() != null && raidData.getRaidLocation() != BlockPos.ZERO && raidData.getRaidLocation() != null)
        {
            SculkHorde.savedData.addNoRaidZoneToMemory(raidData.getDimension(), raidData.getRaidLocation());
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
                SculkHorde.LOGGER.info("RaidHandler | Unable to find valid entity for raid.");
                raidData.setRaidState(RaidState.INITIALIZING_RAID);
                return;
            }
            raidData.getWaveParticipants().add((ISculkSmartEntity) randomEntry.get().spawnEntity(raidData.getDimension(), spawnLocation));
        }

        // Add 5 Creepers
        for(int i = 0; i < 6; i++)
        {
            SculkCreeperEntity creeper = ModEntities.SCULK_CREEPER.get().create(raidData.getDimension());
            creeper.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getWaveParticipants().add(creeper);
        }

        if(isLastWave(0))
        {
            Mob boss = ModEntities.SCULK_ENDERMAN.get().create(raidData.getDimension());
            boss.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            raidData.getWaveParticipants().add((ISculkSmartEntity) boss);
        }
    }
}
