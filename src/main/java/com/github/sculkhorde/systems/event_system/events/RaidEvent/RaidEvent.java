package com.github.sculkhorde.systems.event_system.events.RaidEvent;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactory;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

public class RaidEvent extends Event {


    protected Optional<BlockSearcher> blockSearcher = Optional.empty();
    protected Optional<BlockPos> scoutingLocation = Optional.empty();

    public enum State {
        EVENT_INITIALIZATION,
        SCOUTING,
        RAID_INITIALIZATION,
        WAVE_INITIALIZATION,
        WAVE_ACTIVE,
        SUCCESS,
        FAILURE,
        FINISHED
    }
    
    public enum failureType {
        NONE,
        FAILED_INITIALIZATION,
        ENDERMAN_DEFEATED,
        FAILED_OBJECTIVE_COMPLETION,

        FAILED_TO_LOAD_CHUNKS
    }

    protected long MINIMUM_WAVE_LENGTH_TICKS = TickUnits.convertMinutesToTicks(2);

    // Timing Variables
    protected int MAX_WAVE_DURATION = TickUnits.convertMinutesToTicks(5);
    protected int waveDuration = 0;
    private int timeElapsedScouting = 0;
    protected long timeOfScoutingStart = -1;

    // Raid Variables
    protected BlockPos spawnLocation = BlockPos.ZERO;
    protected BlockPos raidLocation = BlockPos.ZERO;
    protected BlockPos objectiveLocation = BlockPos.ZERO;
    protected BlockPos objectiveLocationAtStartOfWave = objectiveLocation; // We use this to make sure we move on to next objective
    protected BlockPos raidCenter = BlockPos.ZERO; // Used for calculation purposes
    protected static int MINIMUM_RAID_RADIUS = 200;
    protected int currentRaidRadius = MINIMUM_RAID_RADIUS;
    protected static int MAXIMUM_RAID_RADIUS = 500;
    // The Mobs that spawn during waves
    protected ArrayList<ISculkSmartEntity> waveParticipants = new ArrayList<>();
    private State currentState = State.EVENT_INITIALIZATION;
    protected failureType failure = failureType.NONE;


    // Enderman Scouting
    private SculkEndermanEntity scoutEnderman = null;


    protected ServerBossEvent bossEvent;

    // Waves
    protected EntityFactoryEntry.StrategicValues[] currentWavePattern;
    private int maxWaves = 2;
    private int currentWave = 1;
    private int remainingWaveParticipants = 0;

    protected ModSavedData.AreaOfInterestEntry areaOfInterestEntry;

    // Targets
    private static final ArrayList<BlockPos> high_priority_targets = new ArrayList<>();
    private static final ArrayList<BlockPos> medium_priority_targets = new ArrayList<>();

    // Block Searcher
    protected int ticksSpentTryingToChunkLoad;

    public int MAX_TICKS_SPENT_TRYING_TO_CHUNK_LOAD = TickUnits.convertMinutesToTicks(15);


    /**
     * Constructor
     * @param dimension
     */
    public RaidEvent(ResourceKey<Level> dimension) {
        super(dimension);
        setEventCost(300);
        setState(State.EVENT_INITIALIZATION);
        setMinimumDifficulty(Difficulty.NORMAL);
        setEventActive(true);
    }


    // #### Getters ####

    public ArrayList<BlockPos> getHighPriorityTargets()
    {
        return high_priority_targets;
    }

    public ArrayList<BlockPos> getMediumPriorityTargets()
    {
        return medium_priority_targets;
    }

    public ServerLevel getDimension() {
        return ServerLifecycleHooks.getCurrentServer().getLevel(dimension);
    }

    public ResourceKey<Level> getDimensionResourceKey() {
        return dimension;
    }

    public void getFoundTargetsFromBlockSearcher(ArrayList<BlockPos> foundTargets)
    {
        high_priority_targets.clear();
        medium_priority_targets.clear();

        for (BlockPos blockPos : foundTargets)
        {
            if (getDimension().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
            {
                high_priority_targets.add(blockPos);
            }
            else if (getDimension().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY))
            {
                medium_priority_targets.add(blockPos);
            }
        }

        // Sort the targets by distance to origin
        high_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.get().origin) - t1.distSqr(getRaidLocation())));

        // Sort the targets by distance to origin
        medium_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.get().origin) - t1.distSqr(getRaidLocation())));

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
        return objective;
    }

    public void removeNoRaidZoneAtBlockPos(ServerLevel level, BlockPos pos)
    {
        ModSavedData.getSaveData().getNoRaidZoneEntries().removeIf(entry -> entry.isBlockPosInRadius(level, pos));
    }

    public int getTicksSpentTryingToChunkLoad()
    {
        return ticksSpentTryingToChunkLoad;
    }

    public void incrementTicksSpentTryingToChunkLoad()
    {
        ticksSpentTryingToChunkLoad++;

    }

    public void resetTicksSpentTryingToChunkLoad()
    {
        ticksSpentTryingToChunkLoad = 0;

    }

    public int getMAX_WAVE_DURATION() {
        return MAX_WAVE_DURATION;
    }

    public void setMAX_WAVE_DURATION(int MAX_WAVE_DURATION) {
        this.MAX_WAVE_DURATION = MAX_WAVE_DURATION;

    }

    public int getWaveDuration() {
        return waveDuration;
    }

    public void setWaveDuration(int waveDuration) {
        this.waveDuration = waveDuration;

    }

    public void incrementWaveDuration() {
        waveDuration++;

    }

    public int getTimeElapsedScouting() {
        return timeElapsedScouting;
    }

    public void incrementTimeElapsedScouting() {
        timeElapsedScouting++;

    }

    public void setTimeElapsedScouting(int timeElapsedScouting) {
        this.timeElapsedScouting = timeElapsedScouting;

    }

    public BlockPos getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(BlockPos spawnLocation) {
        this.spawnLocation = spawnLocation;

    }

    public BlockPos getRaidLocation() {
        return raidLocation;
    }

    public void setRaidLocation(BlockPos raidLocation) {
        this.raidLocation = raidLocation;

    }

    public Vec3 getObjectiveLocationVec3()
    {
        return new Vec3(objectiveLocation.getX(), objectiveLocation.getY(), objectiveLocation.getZ());
    }

    public BlockPos getObjectiveLocation() {
        return objectiveLocation;
    }

    public void setObjectiveLocation(BlockPos objectiveLocation) {
        this.objectiveLocation = objectiveLocation;

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
        return distance;
    }

    /**
     * Will Pop Next Objective Location and set it as the objective location
     */
    public void advanceToNextObjective()
    {
        getDimension().players().forEach((player) -> getDimension().playSound(null, player.blockPosition(), SoundEvents.BELL_RESONATE, SoundSource.AMBIENT, 1.0F, 1.0F));
        Optional<BlockPos> objectiveOptional = popObjectiveLocation();
        if(objectiveOptional.isPresent())
        {
            setObjectiveLocation(objectiveOptional.get());
        }
        else if(currentState != State.SUCCESS && currentState != State.FINISHED)
        {
            setState(State.SUCCESS);
        }
    }

    public BlockPos getObjectiveLocationAtStartOfWave() {
        return objectiveLocationAtStartOfWave;
    }

    public void setObjectiveLocationAtStartOfWave(BlockPos objectiveLocationAtStartOfWave) {
        this.objectiveLocationAtStartOfWave = objectiveLocationAtStartOfWave;

    }

    public BlockPos getRaidCenter() {
        return raidCenter;
    }

    public void setRaidCenter(BlockPos raidCenter) {
        this.raidCenter = raidCenter;

    }

    public int getMINIMUM_RAID_RADIUS() {
        return MINIMUM_RAID_RADIUS;
    }

    public int getCurrentRaidRadius() {
        return currentRaidRadius;
    }

    public void setCurrentRaidRadius(int currentRaidRadius) {
        this.currentRaidRadius = currentRaidRadius;

    }

    public int getMAXIMUM_RAID_RADIUS() {
        return MAXIMUM_RAID_RADIUS;
    }

    public ArrayList<ISculkSmartEntity> getWaveParticipants() {
        return waveParticipants;
    }

    public void setWaveParticipants(ArrayList<ISculkSmartEntity> waveParticipants) {
        this.waveParticipants = waveParticipants;

    }

    /**
     * Gets the raid state
     * @return the raid state
     */
    public boolean isRaidActive() {
        return currentState == State.WAVE_ACTIVE;
    }

    public State getState() {
        return currentState;
    }

    public failureType getFailure() {
        return failure;
    }

    public void setFailure(failureType failure) {
        setState(State.FAILURE);
        this.failure = failure;

    }

    public SculkEndermanEntity getScoutEnderman() {
        return scoutEnderman;
    }

    public void setScoutEnderman(SculkEndermanEntity scoutEnderman) {
        this.scoutEnderman = scoutEnderman;

    }

    public ServerBossEvent getBossEvent() {
        return bossEvent;
    }

    public void setBossEvent(ServerBossEvent bossEvent) {
        this.bossEvent = bossEvent;

    }

    public EntityFactoryEntry.StrategicValues[] getCurrentWavePattern() {
        return currentWavePattern;
    }

    public void setCurrentWavePattern(EntityFactoryEntry.StrategicValues[] currentWavePattern) {
        this.currentWavePattern = currentWavePattern;

    }

    public int getMaxWaves() {
        return maxWaves;
    }

    public void setMaxWaves(int maxWaves) {
        this.maxWaves = maxWaves;

    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;

    }

    public void incrementCurrentWave()
    {
        currentWave++;

    }

    /**
     * Checks if all raid participants are alive
     * @return true if all raid participants are alive, false otherwise
     */
    public boolean areWaveParticipantsDead() {
        return remainingWaveParticipants <= 0;
    }

    public int getRemainingWaveParticipants() {
        return remainingWaveParticipants;
    }

    public float getWaveProgress() {
        float progress = 0;
        float maxProgress = 0;
        for(ISculkSmartEntity entity : waveParticipants)
        {
            progress += (int) ((Mob) entity).getHealth();
            maxProgress += (int) ((Mob) entity).getMaxHealth();
        }

        return progress / maxProgress;
    }

    public void updateRemainingWaveParticipantsAmount()
    {
        setRemainingWaveParticipants(0);
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                setRemainingWaveParticipants(getRemainingWaveParticipants() + 1);
            }
        }

    }

    public void setRemainingWaveParticipants(int remainingWaveParticipants) {
        this.remainingWaveParticipants = remainingWaveParticipants;

    }

    /**
     * Just removes them from the waveParticipantsList. Does not kill them.
     * Will also removing the glowing effect.
     */
    public void removeWaveParticipantsFromList()
    {
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                ((Mob) entity).removeAllEffects();
                entity.setParticipatingInRaid(false);
            }
            else
            {
                ((Mob) entity).discard();
            }
        }
        waveParticipants.clear();

    }

    public ModSavedData.AreaOfInterestEntry getAreaOfInterestEntry() {
        return areaOfInterestEntry;
    }

    public void setAreaOfInterestEntry(ModSavedData.AreaOfInterestEntry areaOfInterestEntry) {
        this.areaOfInterestEntry = areaOfInterestEntry;

    }

    public Optional<BlockSearcher> getBlockSearcher() {
        return blockSearcher;
    }

    public void setBlockSearcher(BlockSearcher blockSearcher) {
        this.blockSearcher = Optional.ofNullable(blockSearcher);

    }

    /**
     * This is used to determine if a block is obstructed when searching for a spawn location
     */
    public final Predicate<BlockPos> isSpawnObstructed = (blockPos) ->
    {
        if(Math.abs(blockPos.getY() - raidLocation.getY()) > 15)
        {
            return true;
        }

        // If block isn't solid, its obstructed
        if(getDimension().getBlockState(blockPos).isAir() || getDimension().getBlockState(blockPos).is(Blocks.WATER) || getDimension().getBlockState(blockPos).is(Blocks.LAVA))
        {
            return true;
        }
        // If block above is not
        if(!getDimension().getBlockState(blockPos.above()).canBeReplaced() || getDimension().getBlockState(blockPos.above()).is(Blocks.WATER) || getDimension().getBlockState(blockPos.above()).is(Blocks.LAVA))
        {
            return true;
        }

        if(!getDimension().getBlockState(blockPos.above()).canBeReplaced() || getDimension().getBlockState(blockPos.above(1)).is(Blocks.WATER) || getDimension().getBlockState(blockPos.above(1)).is(Blocks.LAVA))
        {
            return true;
        }

        return !getDimension().getBlockState(blockPos.above()).canBeReplaced() || getDimension().getBlockState(blockPos.above(2)).is(Blocks.WATER) || getDimension().getBlockState(blockPos.above(2)).is(Blocks.LAVA);
    };

    /**
     * This is used to determine if a block is a valid spawn location for a mob
     */
    public final Predicate<BlockPos> isSpawnTarget = (blockPos) ->
    {
        return BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (getCurrentRaidRadius() * 0.75) && BlockAlgorithms.isAreaFlat(getDimension(), blockPos, 2);
    };

    public final Predicate<BlockPos> isObstructedInvestigateLocationState = (blockPos) ->
    {
        if(blockSearcher.get().foundTargets.isEmpty() && BlockAlgorithms.getBlockDistance(areaOfInterestEntry.getPosition(), blockPos) > MAXIMUM_RAID_RADIUS)
        {
            return true;
        }

        if(getDimension().getBlockState(blockPos).is(Blocks.AIR))
        {
            return true;
        }

        if(!blockSearcher.get().foundTargets.isEmpty() && !blockSearcher.get().isAnyTargetCloserThan(blockPos, 25))
        {
            return true;
        }

        return !BlockAlgorithms.isExposedToAir(getDimension(), blockPos);
    };

    public final Predicate<BlockPos> isTargetInvestigateLocationState = (blockPos) ->
    {
        boolean isTarget = getDimension().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY)
                || getDimension().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY)
                || getDimension().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);

        // If the target is outside of the current raid radius, increase the raid radius
        if (isTarget && BlockAlgorithms.getBlockDistance(getAreaOfInterestEntry().getPosition(), blockPos) > getCurrentRaidRadius()) {
            setCurrentRaidRadius((int) BlockAlgorithms.getBlockDistance(getRaidLocation(), blockPos));
            SculkHorde.LOGGER.debug("Raid Radius is now " + getCurrentRaidRadius() + " blocks.");
        }

        if(isTarget && !blockSearcher.get().foundTargets.isEmpty() && blockSearcher.get().isAnyTargetCloserThan(blockPos, 5))
        {
            return false;
        }

        return isTarget;
    };


    public boolean canContinue()
    {
        if(!ModConfig.SERVER.sculk_raid_enabled.get())
        {
            return false;
        }
        else if(currentState.equals(State.FINISHED))
        {
            return false;
        }

        return isEventActive;
    }

    @Override
    public boolean canStart() {

        if(!super.canStart())
        {
            return false;
        }

        boolean areRaidsDisabled = !ModConfig.SERVER.sculk_raid_enabled.get();
        boolean isTheHordeDefeated = ModSavedData.getSaveData().isHordeDefeated();
        boolean isRaidCooldownNotOver = !ModSavedData.getSaveData().isRaidCooldownOver();
        boolean isTheGravemindInUndevelopedState = gravemind.getEvolutionState() == Gravemind.evolution_states.Undeveloped;
        boolean areThereNoAreasOfInterest = ModSavedData.getSaveData().getAreasOfInterestEntries().isEmpty();
        boolean areThereNoAreasOfInterestNotInNoRaidZone = ModSavedData.getSaveData().getAreaOfInterestEntryNotInNoRaidZone().isEmpty();
        boolean areThereNoPlayersOnServer = ServerLifecycleHooks.getCurrentServer().getPlayerCount() <= 0;

        if(areRaidsDisabled || isTheHordeDefeated || isRaidCooldownNotOver || isTheGravemindInUndevelopedState || areThereNoAreasOfInterest || areThereNoAreasOfInterestNotInNoRaidZone || areThereNoPlayersOnServer)
        {
            return false;
        }
        return true;
    }



    public void bossBarTick(){
        if(getState() == State.EVENT_INITIALIZATION || getState() == State.SCOUTING || getState() == State.SUCCESS || getState() == State.FAILURE)
        {
            return;
        }

        if(getBossEvent() == null)
        {
            setBossEvent(new ServerBossEvent(Component.literal("Sculk Raid Wave " + getCurrentWave() + " / " + getMaxWaves()), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS));
            getBossEvent().setCreateWorldFog(true);
            getBossEvent().setDarkenScreen(true);
        }

        Iterator<ServerPlayer> iterator = getDimension().players().iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            boolean isPlayerInListAlready = getBossEvent().getPlayers().contains(player);
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= Math.max(100, getCurrentRaidRadius() * 2);
            if (!isPlayerInListAlready && isPlayerInRangeOfRaid) {
                getBossEvent().addPlayer(player);
            }
        }

        // Remove players from event as necessary
        iterator = getBossEvent().getPlayers().iterator();
        while (iterator.hasNext()) {
            ServerPlayer player = iterator.next();
            boolean isPlayerInRangeOfRaid = BlockAlgorithms.getBlockDistanceXZ(getRaidLocation(), player.blockPosition()) <= Math.max(100, getCurrentRaidRadius() * 2);
            if (!isPlayerInRangeOfRaid) {
                getBossEvent().removePlayer(player);
                break;
            }
        }


        if(getState() == State.WAVE_INITIALIZATION)
        {
            getBossEvent().setProgress(0.0F);
            getBossEvent().setName(Component.literal("Sculk Raid Wave " + getCurrentWave() + " / " + getMaxWaves()));
        }
        else
        {
            getBossEvent().setProgress(getWaveProgress());
        }
    }

    @Override
    public void serverTick() {

        if(getTicksSpentTryingToChunkLoad() > MAX_TICKS_SPENT_TRYING_TO_CHUNK_LOAD)
        {
            setFailure(failureType.FAILED_TO_LOAD_CHUNKS);
            return;
        }


        bossBarTick();

        if(currentState == State.EVENT_INITIALIZATION)
        {
            initializationTick();
        }
        else if(currentState == State.SCOUTING)
        {
            scoutingTick();
        }
        else if(currentState == State.RAID_INITIALIZATION)
        {
            raidInitializationTick();
        }
        else if(currentState == State.WAVE_INITIALIZATION)
        {
            waveInitializationTick();
        }
        else if(currentState == State.WAVE_ACTIVE)
        {
            waveActiveTick();
        }
        else if(currentState == State.SUCCESS)
        {
            successTick();
        }
        else if(currentState == State.FAILURE)
        {
            failureTick();
        }

    }

    public void setState(State state)
    {
        this.currentState = state;
        SculkHorde.LOGGER.info(getClass().getSimpleName() + "State: " + state.toString());
    }


    protected void initializeBlockSearcherForInvestigateLocation(int searchIterationsPerTick, int maxTargets)
    {
        if(getAreaOfInterestEntry() == null)
        {
            Optional<ModSavedData.AreaOfInterestEntry> possibleEntry = ModSavedData.getSaveData().getAreaOfInterestEntryNotInNoRaidZone();
            if(possibleEntry.isEmpty())
            {
                setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            if(!possibleEntry.get().isEntryValid())
            {
                setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            setAreaOfInterestEntry(possibleEntry.get());

        }

        ModSavedData.AreaOfInterestEntry areaOfInterestEntry = getAreaOfInterestEntry();
        ServerLevel dimension = areaOfInterestEntry.getDimension();
        ResourceKey<Level> dimensionResourceKey = dimension.dimension();
        setDimension(dimensionResourceKey);

        SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Investigating Location at: " + areaOfInterestEntry.getPosition().toShortString() + " in dimension " + getFormattedDimension(dimensionResourceKey) + ".");

        setBlockSearcher(new BlockSearcher(dimension, areaOfInterestEntry.getPosition()));

        if(getBlockSearcher().isEmpty())
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | BlockSearcher Failed to Initialize");
            setFailure(failureType.FAILED_INITIALIZATION);
            return;
        }

        BlockSearcher blockSearcher = getBlockSearcher().get();

        blockSearcher.setMaxDistance(getCurrentRaidRadius());
        //getBlockSearcher().setDebugMode(SculkHorde.isDebugMode());
        blockSearcher.searchIterationsPerTick = searchIterationsPerTick;
        blockSearcher.ignoreBlocksNearTargets = true;

        // What is the target?
        blockSearcher.setTargetBlockPredicate(isTargetInvestigateLocationState);

        // What is obstructed?
        blockSearcher.setObstructionPredicate(isObstructedInvestigateLocationState);

        blockSearcher.MAX_TARGETS = maxTargets;
    }

    protected void initializationTick()
    {
        ModSavedData.getSaveData().incrementTicksSinceLastRaid();

        // Initialize Block Searcher if null
        if(blockSearcher.isEmpty())
        {
            initializeBlockSearcherForInvestigateLocation(100, 30);
        }

        if(blockSearcher.isEmpty())
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | BlockSearcher Failed to Initialize");
            setFailure(failureType.FAILED_INITIALIZATION);
            return;
        }


        // Tick Block Searcher
        blockSearcher.get().tick();

        // If the block searcher is not finished, return.
        if(!blockSearcher.get().isFinished) { return; }

        // If we find block targets, store them.
        if(blockSearcher.get().isSuccessful)
        {
            getFoundTargetsFromBlockSearcher(blockSearcher.get().foundTargets);
            setMaxWaves(10);
            setRaidLocation(getAreaOfInterestEntry().getPosition());
            SculkHorde.LOGGER.debug(getClass().getSimpleName() + " | Found " + (getHighPriorityTargets().size() + getMediumPriorityTargets().size()) + " objective targets in " + getAreaOfInterestEntry().getPosition() + " in dimension " + getDimension().dimension());
            setState(State.SCOUTING);
        }
        else
        {
            setFailure(failureType.FAILED_INITIALIZATION);
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Found no objective targets in dimension" + getDimensionResourceKey() +". Not Initializing Raid.");
        }
        setBlockSearcher(null);


    }

    protected void scoutingTick()
    {
        if(scoutingLocation.isEmpty())
        {
            setEventLocation(getAreaOfInterestEntry().getPosition());
            scoutingLocation = Optional.of(getAreaOfInterestEntry().getPosition());

            loadScoutingChunks();
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Scouting Location: " + scoutingLocation.get().toShortString());
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Scouting Location Loaded: " + isScoutingLocationLoaded());
            timeOfScoutingStart = getDimension().getGameTime();

        }

        if(!isScoutingLocationLoaded())
        {
            loadScoutingChunks();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        incrementTimeElapsedScouting();

        if(getScoutEnderman() == null)
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Scouting Location Is Loaded. Continuing.");

            setScoutEnderman(new SculkEndermanEntity(getDimension(), scoutingLocation.get()));
            getDimension().addFreshEntity(getScoutEnderman());
            getScoutEnderman().setScouting(true);
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Sculk Enderman Scouting at " + getAreaOfInterestEntry().getPosition().toShortString() + " in the " + getDimensionResourceKey() + " for " + ModConfig.SERVER.sculk_raid_enderman_scouting_duration_minutes.get() + " minutes");
            announceToPlayersInRange(Component.literal("A Sculk Infested Enderman is scouting out a possible raid location at " + areaOfInterestEntry.getPosition().toShortString() + " in the " + getFormattedDimension(getDimensionResourceKey()) +  ". Kill it to stop the raid from happening!"), getCurrentRaidRadius() * 8);
            EntityAlgorithms.applyEffectToTarget(getScoutEnderman(), MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0);
            SoundUtil.playSoundForEveryPlayer(getDimension(), ModSounds.RAID_SCOUT_SOUND.get());

            //Spawn Sculk Phantoms
            if (ModConfig.SERVER.should_sculk_nodes_and_raids_spawn_phantoms.get()) {
                spawnPhantomsAtTopOfWorld(getDimension(), getAreaOfInterestEntry().getPosition(), 5);
            }
        }

        if(!getScoutEnderman().isAlive())
        {
            setFailure(failureType.ENDERMAN_DEFEATED);
            return;
        }

        if(getDimension().getGameTime() - timeOfScoutingStart >= TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_enderman_scouting_duration_minutes.get()))
        {
            setState(State.RAID_INITIALIZATION);
            getScoutEnderman().discard();
            setScoutEnderman(null);
            setBlockSearcher(null);
        }
    }

    protected void raidInitializationTick()
    {
        ModSavedData.getSaveData().setTicksSinceLastRaid(0);

        if(getBlockSearcher().isEmpty())
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Scouting Location Loaded: " + isScoutingLocationLoaded());


            if(getHighPriorityTargets().size() + getMediumPriorityTargets().size() <= 0)
            {
                setFailure(failureType.FAILED_INITIALIZATION);
                return;
            }

            setRaidCenterToCentroidOfAllTargets();

            // Initialize Block Searcher
            initializeBlockSearcherForSpawnSearch(100, 1);


            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Initializing Block Searcher");
        }

        //This is just in case we load in the middle of the raid
        scoutingLocation = Optional.of(getAreaOfInterestEntry().getPosition());

        if(!isScoutingLocationLoaded())
        {
            loadScoutingChunks();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        if(getBlockSearcher().isEmpty())
        {
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | BlockSearcher Failed to Initialize");
            setFailure(failureType.FAILED_INITIALIZATION);
            return;
        }

        BlockSearcher blockSearcher = getBlockSearcher().get();

        // Tick the Block Searcher
        blockSearcher.tick();

        if(!blockSearcher.isFinished)
        {
            return;
        }

        // If successful
        if(blockSearcher.isSuccessful)
        {
            setState(State.WAVE_INITIALIZATION);
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Found Spawn Location at " + getSpawnLocation().toShortString() + " in " + blockSearcher.getDimension().dimension() + ". Initializing Raid.");

            advanceToNextObjective();
            setSpawnLocation(blockSearcher.foundTargets.get(0));

            setCurrentRaidRadius(getDistanceOfFurthestObjective());
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Current Raid Radius: " + getCurrentRaidRadius());

            loadRaidChunksCenter();

            announceToPlayersInRange(Component.literal("The Sculk Horde is Raiding " + getRaidLocation().toShortString() + " in the " + getFormattedDimension(getDimensionResourceKey()) + "!"), getCurrentRaidRadius() * 8);

        }
        // If not successful
        else
        {
            setState(State.FAILURE);
            SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Unable to Find Spawn Location. Not Initializing Raid.");
        }
    }

    protected void waveInitializationTick()
    {
        setWaveDuration(0);
        setCurrentWavePattern(getWavePattern());

        if(!isSpawningLocationLoaded())
        {
            loadSpawningChunks();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        if(!isRaidCenterLocationLoaded())
        {
            loadRaidChunksCenter();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        populateRaidParticipants(getSpawnLocation());

        announceToPlayersInRange(Component.literal(" Starting Wave " + getCurrentWave() + " out of " + getMaxWaves() + "."), getCurrentRaidRadius() * 8);

        spawnWaveParticipants(getSpawnLocation());

        SoundUtil.playSoundForEveryPlayer(getDimension(), ModSounds.RAID_START_SOUND.get());

        if(getObjectiveLocationAtStartOfWave().equals(getObjectiveLocation()))
        {
            advanceToNextObjective();
        }
        setObjectiveLocationAtStartOfWave(getObjectiveLocation());
        SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Spawning mobs at: " + getSpawnLocation());
        setState(State.WAVE_ACTIVE);
    }

    protected void waveActiveTick()
    {
        if(!isSpawningLocationLoaded())
        {
            loadSpawningChunks();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        if(!isRaidCenterLocationLoaded())
        {
            loadRaidChunksCenter();
            incrementTicksSpentTryingToChunkLoad();
            return;
        }

        updateRemainingWaveParticipantsAmount();

        incrementWaveDuration();

        // If wave has been going on for too long, end it
        if(getWaveDuration() >= getMAX_WAVE_DURATION() && getWaveDuration() >= MINIMUM_WAVE_LENGTH_TICKS)
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

    protected void successTick()
    {
        ModSavedData.getSaveData().addNoRaidZoneToMemory(getDimension(), getRaidLocation());
        SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Raid Complete.");
        announceToPlayersInRange(Component.literal("The Sculk Horde's raid was successful!"), getCurrentRaidRadius() * 8);
        // Summon Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(ModEntities.SCULK_SPORE_SPEWER.get(), getDimension());
        sporeSpewer.setPos(getRaidLocation().getX(), getRaidLocation().getY(), getRaidLocation().getZ());
        getDimension().addFreshEntity(sporeSpewer);
        setState(State.FINISHED);
        bossEvent.removeAllPlayers();
    }

    protected void failureTick()
    {
        // Switch Statement for Failure Type
        switch (getFailure())
        {
            case FAILED_OBJECTIVE_COMPLETION:
                SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Raid Failed. Objectives Not Destroyed.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to destroy all objectives!"), getCurrentRaidRadius() * 8);
                getDimension().players().forEach((player) -> getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case ENDERMAN_DEFEATED:
                SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Raid Failed. Sculk Enderman Defeated.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to scout out a potential raid location. Raid Prevented!"), getCurrentRaidRadius() * 8);
                getDimension().players().forEach((player) -> getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case FAILED_INITIALIZATION:
                SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Raid Failed. Unable to Initialize.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to find a suitable way to raid the location. Raid Prevented!"), getCurrentRaidRadius() * 8);
                getDimension().players().forEach((player) -> getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case FAILED_TO_LOAD_CHUNKS:
                SculkHorde.LOGGER.info(getClass().getSimpleName() + " | Raid Failed. Unable to Load Chunks.");
                announceToPlayersInRange(Component.literal("The Sculk Horde has failed to load the chunks required to raid the location. Raid Prevented!"), getCurrentRaidRadius() * 8);
                getDimension().players().forEach((player) -> getDimension().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.AMBIENT, 1.0F, 7.0F));
                break;
            case NONE:
                SculkHorde.LOGGER.error(getClass().getSimpleName() + " | Raid Failed. Unknown Reason.");
                break;
        }

        if(getRaidLocation() != null && getRaidLocation() != BlockPos.ZERO)
        {
            ModSavedData.getSaveData().addNoRaidZoneToMemory(getDimension(), getRaidLocation());
        }

        bossEvent.removeAllPlayers();
        setState(State.FINISHED);
    }


    public boolean isScoutingLocationLoaded()
    {
        return getDimension().isAreaLoaded(scoutingLocation.get(), 3);
    }

    public boolean isRaidCenterLocationLoaded()
    {
        return getDimension().isAreaLoaded(getRaidCenter(), 5);
    }

    public boolean isSpawningLocationLoaded()
    {
        return getDimension().isAreaLoaded(getSpawnLocation(), 1);
    }

    public void loadRaidChunksCenter()
    {
        int distanceXBetweenRaidCenterAndSpawnPos = Math.abs(getRaidCenter().getX() - getSpawnLocation().getX());
        int distanceZBetweenRaidCenterAndSpawnPos = Math.abs(getRaidCenter().getZ() - getSpawnLocation().getZ());
        int lengthInBlocks = Math.max(distanceXBetweenRaidCenterAndSpawnPos, distanceZBetweenRaidCenterAndSpawnPos) * 2;
        int chunkLength = BlockAlgorithms.convertBlockLengthToChunkLength(lengthInBlocks);

        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(getRaidCenter(), getDimension());
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(getDimension(), getRaidCenter(), chunkLength, 2, TickUnits.convertHoursToTicks(1));
    }

    public void loadScoutingChunks()
    {
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(getDimension(), scoutingLocation.get(), 3, 2, TickUnits.convertHoursToTicks(1));
    }

    public void loadSpawningChunks()
    {
        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(getDimension(), getSpawnLocation(), 5, 2, TickUnits.convertHoursToTicks(1));
    }

    protected String getFormattedDimension(ResourceKey<Level> dimension)
    {
        String languageKey = dimension.location().toShortLanguageKey();
        return Component.translatable(languageKey).getString();
    }

    protected static void spawnPhantomsAtTopOfWorld(ServerLevel level, BlockPos origin, int amount)
    {
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        for(int i = 0; i < amount; i++)
        {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int y = level.getMaxBuildHeight();
            BlockPos spawnPosition = new BlockPos(origin.getX() + x, y, origin.getZ() + z);

            SculkPhantomEntity.spawnPhantom(level, spawnPosition, true);

        }
    }

    protected void setRaidCenterToCentroidOfAllTargets()
    {
        // Calculate centroid of all targets
        ArrayList<BlockPos> allTargets = new ArrayList<>();
        allTargets.addAll(getHighPriorityTargets());
        allTargets.addAll(getMediumPriorityTargets());
        setRaidCenter(BlockAlgorithms.getCentroid(allTargets));
    }

    protected void initializeBlockSearcherForSpawnSearch(int searchIterationsPerTick, int maxTargets)
    {
        setBlockSearcher(new BlockSearcher(getDimension(), getRaidLocation()));

        if(getBlockSearcher().isEmpty())
        {
            SculkHorde.LOGGER.info("RaidHandler | BlockSearcher Failed to Initialize");
            setFailure(failureType.FAILED_INITIALIZATION);
            return;
        }

        BlockSearcher blockSearcher = getBlockSearcher().get();

        blockSearcher.setMaxDistance(getCurrentRaidRadius());
        blockSearcher.setTargetBlockPredicate(isSpawnTarget);
        blockSearcher.setObstructionPredicate(isSpawnObstructed);
        blockSearcher.setMaxTargets(1);
        blockSearcher.setPositionToMoveAwayFrom(getRaidCenter());
        // getBlockSearcher().setDebugMode(SculkHorde.isDebugMode());
        blockSearcher.searchIterationsPerTick = searchIterationsPerTick;
        blockSearcher.MAX_TARGETS = maxTargets;
    }

    protected boolean isLastWave(int offset)
    {
        return getCurrentWave() >= getMaxWaves() + offset;
    }

    public boolean isCurrentObjectiveCompleted()
    {
        if(getDimension().getBlockState(getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            return false;
        }
        else if(getDimension().getBlockState(getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            return false;
        }
        else return !getDimension().getBlockState(getObjectiveLocation()).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY);
    }

    private Predicate<EntityFactoryEntry> isValidRaidParticipant(EntityFactoryEntry.StrategicValues strategicValue)
    {
        return (entityFactoryEntry) -> entityFactoryEntry.doesEntityContainNeededStrategicValue(strategicValue);
    }

    private void spawnWaveParticipants(BlockPos spawnLocation)
    {
        // Spawn Sculk Spore Spewer
        SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(ModEntities.SCULK_SPORE_SPEWER.get(), getDimension());
        sporeSpewer.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
        getDimension().addFreshEntity(sporeSpewer);

        getWaveParticipants().forEach((raidParticipant) ->
        {
            raidParticipant.setParticipatingInRaid(true);
            ((Mob)raidParticipant).setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            getDimension().addFreshEntity((Entity) raidParticipant);
            EntityAlgorithms.applyEffectToTarget(((Mob) raidParticipant), MobEffects.GLOWING, TickUnits.convertMinutesToTicks(15), 0);
        });
    }

    public EntityFactoryEntry.StrategicValues[] getWavePattern()
    {
        EntityFactoryEntry.StrategicValues[][] possibleWavePatterns = {DefaultRaidWavePatterns.FIVE_RANGED_FIVE_MELEE, DefaultRaidWavePatterns.TEN_RANGED, DefaultRaidWavePatterns.TEN_MELEE};
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
                setState(State.RAID_INITIALIZATION);
                return;
            }
            getWaveParticipants().add((ISculkSmartEntity) randomEntry.get().spawnEntity(getDimension(), spawnLocation));
        }

        // Add 5 Creepers
        for(int i = 0; i < 6; i++)
        {
            SculkCreeperEntity creeper = ModEntities.SCULK_CREEPER.get().create(getDimension());
            creeper.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            getWaveParticipants().add(creeper);
        }

        if(isLastWave(0))
        {
            Mob boss = ModEntities.SCULK_ENDERMAN.get().create(getDimension());
            boss.setPos(spawnLocation.getX(), spawnLocation.getY() + 1, spawnLocation.getZ());
            getWaveParticipants().add((ISculkSmartEntity) boss);
        }
    }

    protected void endWave()
    {
        // Otherwise, go to next wave
        incrementCurrentWave();
        getWaveParticipants().clear();

        // If we are on last wave, end raid
        if(isLastWave(1))
        {
            setFailure(failureType.FAILED_OBJECTIVE_COMPLETION);

            announceToPlayersInRange(Component.literal("Final Wave Complete."), getCurrentRaidRadius() * 8);
            return;
        }

        announceToPlayersInRange(Component.literal("Wave " + (getCurrentWave() - 1) + " complete."), getCurrentRaidRadius() * 8);

        setState(State.WAVE_INITIALIZATION);
    }

    private static int StateToInt(State state) {
        SculkHorde.LOGGER.debug("Saving Raid State: " + state.name() + " as " + state.ordinal() + ".");
        return state.ordinal();
    }

    private static State intToState(int state) {
        SculkHorde.LOGGER.debug("Loading Raid State: " + state + " as " + State.values()[state].name() + ".");
        return State.values()[state];
    }

    @Override
    public void loadAdditional(CompoundTag tag) {

        setState(intToState(tag.getInt("State")));
        setWaveDuration(tag.getInt("waveDuration"));
        setTimeElapsedScouting(tag.getInt("timeElapsedScouting"));
        setSpawnLocation(BlockPos.of(tag.getLong("spawnLocation")));
        setRaidLocation(BlockPos.of(tag.getLong("raidLocation")));
        setObjectiveLocation(BlockPos.of(tag.getLong("objectiveLocation")));
        setObjectiveLocationAtStartOfWave(BlockPos.of(tag.getLong("objectiveLocationAtStartOfWave")));
        setRaidCenter(BlockPos.of(tag.getLong("raidCenter")));
        setMaxWaves(tag.getInt("maxWaves"));
        setCurrentWave(tag.getInt("currentWave"));
        setRemainingWaveParticipants(tag.getInt("remainingWaveParticipants"));

        ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")));
        setDimension(dimensionResourceKey);

        // Load the wave participants from a list of UUIDs
        getWaveParticipants().clear();
        ListTag waveParticipantsTag = tag.getList("waveParticipants", 11);
        for (Tag t : waveParticipantsTag) {
            if (t instanceof IntArrayTag) {
                UUID uuid = NbtUtils.loadUUID((IntArrayTag) t);
                Entity entity = ServerLifecycleHooks.getCurrentServer().getLevel(dimensionResourceKey).getEntity(uuid);
                if (entity instanceof ISculkSmartEntity) {
                    getWaveParticipants().add((ISculkSmartEntity) entity);
                    ((ISculkSmartEntity) entity).setParticipatingInRaid(true);
                }
            }
        }

        // Enderman Scouting
        if (tag.hasUUID("scoutEnderman")) {
            UUID uuid = tag.getUUID("scoutEnderman");
            Entity entity = ServerLifecycleHooks.getCurrentServer().getLevel(dimensionResourceKey).getEntity(uuid);
            if (entity instanceof SculkEndermanEntity) {
                setScoutEnderman((SculkEndermanEntity) entity);
            }
        }

        // Waves
        if (tag.contains("currentWavePattern")) {
            // Load the current wave pattern from a list of strings
            ListTag currentWavePatternTag = tag.getList("currentWavePattern", 8);
            setCurrentWavePattern(new EntityFactoryEntry.StrategicValues[currentWavePatternTag.size()]);
            for (int i = 0; i < currentWavePatternTag.size(); i++) {
                Tag t = currentWavePatternTag.get(i);
                if (t instanceof StringTag) {
                    String s = ((StringTag) t).getAsString();
                    getCurrentWavePattern()[i] = EntityFactoryEntry.StrategicValues.valueOf(s);
                }
            }
        }

        // Area of Interest Entry
        if (tag.contains("areaOfInterestEntry")) {
            CompoundTag areaOfInterestEntryTag = tag.getCompound("areaOfInterestEntry");
            setAreaOfInterestEntry(ModSavedData.AreaOfInterestEntry.serialize(areaOfInterestEntryTag));
        }

        // Targets
        // Load the high priority targets from a list of longs
        getHighPriorityTargets().clear();
        ListTag highPriorityTargetsTag = tag.getList("highPriorityTargets", 4);
        for (Tag t : highPriorityTargetsTag) {
            if (t instanceof LongTag) {
                long l = ((LongTag) t).getAsLong();
                getHighPriorityTargets().add(BlockPos.of(l));
            }
        }

        // Load the medium priority targets from a list of longs
        getMediumPriorityTargets().clear();
        ListTag mediumPriorityTargetsTag = tag.getList("mediumPriorityTargets", 4);
        for (Tag t : mediumPriorityTargetsTag) {
            if (t instanceof LongTag) {
                long l = ((LongTag) t).getAsLong();
                getMediumPriorityTargets().add(BlockPos.of(l));
            }
        }

    }

    @Override
    public void saveAdditional(CompoundTag tag) {

        tag.putInt("State", StateToInt(getState()));
        tag.putInt("waveDuration", getWaveDuration());
        tag.putInt("timeElapsedScouting", getTimeElapsedScouting());
        tag.putLong("spawnLocation", getSpawnLocation().asLong());
        tag.putLong("raidLocation", getRaidLocation().asLong());
        tag.putLong("objectiveLocation", getObjectiveLocation().asLong());
        tag.putLong("objectiveLocationAtStartOfWave", getObjectiveLocationAtStartOfWave().asLong());
        tag.putLong("raidCenter", getRaidCenter().asLong());
        tag.putLong("raidCenter", getRaidCenter().asLong());
        tag.putInt("currentRaidRadius", getCurrentRaidRadius());
        tag.putInt("maxWaves", getMaxWaves());
        tag.putInt("currentWave", getCurrentWave());
        if(getDimensionResourceKey() != null){
            tag.putString("dimension", getDimensionResourceKey().location().toString());
        }
        tag.putInt("remainingWaveParticipants", getRemainingWaveParticipants());        // Save the wave participants as a list of UUIDs
        ListTag waveParticipantsTag = new ListTag();
        for (ISculkSmartEntity entity : getWaveParticipants()) {
            if (entity instanceof Entity) {
                UUID uuid = ((Entity) entity).getUUID();
                waveParticipantsTag.add(NbtUtils.createUUID(uuid));
            }
        }
        tag.put("waveParticipants", waveParticipantsTag);

        // Enderman Scouting
        if (getScoutEnderman() != null) {
            tag.putUUID("scoutEnderman", getScoutEnderman().getUUID());
        }

        // Waves
        if (getCurrentWavePattern() != null) {
            // Save the current wave pattern as a list of strings
            ListTag currentWavePatternTag = new ListTag();
            for (EntityFactoryEntry.StrategicValues value : getCurrentWavePattern()) {
                currentWavePatternTag.add(StringTag.valueOf(value.name()));
            }
            tag.put("currentWavePattern", currentWavePatternTag);
        }

        // Area of Interest Entry
        if (getAreaOfInterestEntry() != null) {
            CompoundTag areaOfInterestEntryTag = getAreaOfInterestEntry().deserialize();
            tag.put("areaOfInterestEntry", areaOfInterestEntryTag);
        }

        // Targets
        // Save the high priority targets as a list of longs
        ListTag highPriorityTargetsTag = new ListTag();
        for (BlockPos pos : getHighPriorityTargets()) {
            highPriorityTargetsTag.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("highPriorityTargets", highPriorityTargetsTag);

        // Save the medium priority targets as a list of longs
        ListTag mediumPriorityTargetsTag = new ListTag();
        for (BlockPos pos : getMediumPriorityTargets()) {
            mediumPriorityTargetsTag.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("mediumPriorityTargets", mediumPriorityTargetsTag);

    }

    /**
     * Sends a message to all players within a specified radius of this entity.
     * * @param message The text component to send.
     * @param radius The radius (in blocks) within which players will receive the message.
     */
    protected void announceToPlayersInRange(Component message, double radius) {
        // Check if the entity is in a valid world/level
        if (getDimension() == null || getDimension().isClientSide) {
            return;
        }

        // Define the squared radius for faster calculation (avoiding Math.sqrt in the loop)
        double radiusSqr = radius * radius;

        // Iterate through all players in the level
        for (Player player : getDimension().players()) {

            // 1. Check if the player is in the same dimension
            if (player.level().dimension() != getDimension().dimension()) {
                continue;
            }

            // 2. Check the distance
            // The distanceSq() method returns the squared distance, matching radiusSqr
            if (BlockAlgorithms.getBlockDistanceXZ(player.blockPosition(), getEventLocation()) <= radiusSqr) {

                // 3. Send the message
                // Use ChatType.SYSTEM for a non-chat message that cannot be disabled easily
                player.sendSystemMessage(message);
            }
        }
    }
}
