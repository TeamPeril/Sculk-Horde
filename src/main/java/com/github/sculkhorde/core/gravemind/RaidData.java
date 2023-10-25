package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class RaidData {

    // Timing Variables
    protected int MAX_WAVE_DURATION = TickUnits.convertMinutesToTicks(5);
    protected int waveDuration = 0;
    private int timeElapsedScouting = 0;
    private final int SCOUTING_DURATION = TickUnits.convertMinutesToTicks(8);

    // Raid Variables
    private ServerLevel level;
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
    private RaidHandler.RaidState raidState = RaidHandler.RaidState.INACTIVE;
    protected RaidHandler.failureType failure = RaidHandler.failureType.NONE;


    // Enderman Scouting
    private SculkEndermanEntity scoutEnderman = null;


    protected ServerBossEvent bossEvent;

    // Waves
    protected EntityFactory.StrategicValues[] currentWavePattern;
    private int maxWaves = 2;
    private int currentWave = 1;
    private int remainingWaveParticipants = 0;

    protected ModSavedData.AreaofInterestEntry areaOfInterestEntry;

    // Targets
    private static final ArrayList<BlockPos> high_priority_targets = new ArrayList<>();
    private static final ArrayList<BlockPos> medium_priority_targets = new ArrayList<>();

    // Block Searcher
    private BlockSearcher blockSearcher;

    public ArrayList<BlockPos> getHighPriorityTargets()
    {
        return high_priority_targets;
    }

    public ArrayList<BlockPos> getMediumPriorityTargets()
    {
        return medium_priority_targets;
    }

    public void getFoundTargetsFromBlockSearcher(ArrayList<BlockPos> foundTargets)
    {
        high_priority_targets.clear();
        medium_priority_targets.clear();

        for (BlockPos blockPos : foundTargets)
        {
            if (level.getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
            {
                high_priority_targets.add(blockPos);
            }
            else if (level.getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY))
            {
                medium_priority_targets.add(blockPos);
            }
        }

        // Sort the targets by distance to origin
        high_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation())));

        // Sort the targets by distance to origin
        medium_priority_targets.sort((blockPos, t1) -> (int) (blockPos.distSqr(blockSearcher.origin) - t1.distSqr(getRaidLocation())));

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


    /**
     * Resets all variables related to raid.
     */
    public void reset()
    {
        if(areaOfInterestEntry != null) { SculkHorde.savedData.removeAreaOfInterestFromMemory(areaOfInterestEntry.getPosition()); }
        areaOfInterestEntry = null;
        if(getRaidLocation() != null)
        {
            BlockEntityChunkLoaderHelper.getChunkLoaderHelper().removeRequestsWithOwner(getRaidLocation(), level);
        }
        setBlockSearcher(null);
        setRaidState(RaidHandler.RaidState.INACTIVE);
        setRaidLocation(BlockPos.ZERO);
        setObjectiveLocation(BlockPos.ZERO);
        setSpawnLocation(BlockPos.ZERO);
        waveParticipants.clear();
        setRemainingWaveParticipants(0);
        setCurrentWave(1);
        setScoutEnderman(null);
        setTimeElapsedScouting(0);
        setCurrentRaidRadius(MINIMUM_RAID_RADIUS);
        SculkHorde.savedData.setDirty();
        if(bossEvent != null ) bossEvent.removeAllPlayers();
        bossEvent = null;
    }

    public void startRaidArtificially(BlockPos raidLocationIn)
    {
        SculkHorde.setDebugMode(!SculkHorde.isDebugMode());
        EntityAlgorithms.announceToAllPlayers(level, Component.literal("Debug Mode is now: " + SculkHorde.isDebugMode()));
        ModConfig.SERVER.experimental_features_enabled.set(true);
        EntityAlgorithms.announceToAllPlayers(level, Component.literal("Experimental Features is now: " + ModConfig.SERVER.experimental_features_enabled.get()));
        SculkHorde.savedData.setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get() + 1000);
        EntityAlgorithms.announceToAllPlayers(level, Component.literal("Mass is now: " + SculkHorde.savedData.getSculkAccumulatedMass()));
        SculkHorde.gravemind.calulateCurrentState();
        EntityAlgorithms.announceToAllPlayers(level, Component.literal("Gravemind is now in state: " + SculkHorde.gravemind.getEvolutionState()));

        Optional<ModSavedData.AreaofInterestEntry> possibleAreaOfInterestEntry = SculkHorde.savedData.addAreaOfInterestToMemory(raidLocationIn);
        if(possibleAreaOfInterestEntry.isPresent())
        {
            setAreaOfInterestEntry(possibleAreaOfInterestEntry.get());
            setRaidState(RaidHandler.RaidState.INVESTIGATING_LOCATION);
            SculkHorde.savedData.setTicksSinceLastRaid(TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_global_cooldown_between_raids_minutes.get()));
        }
        else
        {
            reset();
        }
    }

    public int getMAX_WAVE_DURATION() {
        return MAX_WAVE_DURATION;
    }

    public void setMAX_WAVE_DURATION(int MAX_WAVE_DURATION) {
        this.MAX_WAVE_DURATION = MAX_WAVE_DURATION;
        SculkHorde.savedData.setDirty();
    }

    public int getWaveDuration() {
        return waveDuration;
    }

    public void setWaveDuration(int waveDuration) {
        this.waveDuration = waveDuration;
        SculkHorde.savedData.setDirty();
    }

    public void incrementWaveDuration() {
        waveDuration++;
        SculkHorde.savedData.setDirty();
    }

    public int getTimeElapsedScouting() {
        return timeElapsedScouting;
    }

    public void incrementTimeElapsedScouting() {
        timeElapsedScouting++;
        SculkHorde.savedData.setDirty();
    }

    public void setTimeElapsedScouting(int timeElapsedScouting) {
        this.timeElapsedScouting = timeElapsedScouting;
        SculkHorde.savedData.setDirty();
    }

    public int getSCOUTING_DURATION() {
        return SCOUTING_DURATION;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
    }

    public BlockPos getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(BlockPos spawnLocation) {
        this.spawnLocation = spawnLocation;
        SculkHorde.savedData.setDirty();
    }

    public BlockPos getRaidLocation() {
        return raidLocation;
    }

    public void setRaidLocation(BlockPos raidLocation) {
        this.raidLocation = raidLocation;
        SculkHorde.savedData.setDirty();
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
        SculkHorde.savedData.setDirty();
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
            setObjectiveLocation(objectiveOptional.get());
        }
        else
        {
            setRaidState(RaidHandler.RaidState.COMPLETE);
        }
    }

    public BlockPos getObjectiveLocationAtStartOfWave() {
        return objectiveLocationAtStartOfWave;
    }

    public void setObjectiveLocationAtStartOfWave(BlockPos objectiveLocationAtStartOfWave) {
        this.objectiveLocationAtStartOfWave = objectiveLocationAtStartOfWave;
        SculkHorde.savedData.setDirty();
    }

    public BlockPos getRaidCenter() {
        return raidCenter;
    }

    public void setRaidCenter(BlockPos raidCenter) {
        this.raidCenter = raidCenter;
        SculkHorde.savedData.setDirty();
    }

    public int getMINIMUM_RAID_RADIUS() {
        return MINIMUM_RAID_RADIUS;
    }

    public int getCurrentRaidRadius() {
        return currentRaidRadius;
    }

    public void setCurrentRaidRadius(int currentRaidRadius) {
        this.currentRaidRadius = currentRaidRadius;
        SculkHorde.savedData.setDirty();
    }

    public int getMAXIMUM_RAID_RADIUS() {
        return MAXIMUM_RAID_RADIUS;
    }

    public ArrayList<ISculkSmartEntity> getWaveParticipants() {
        return waveParticipants;
    }

    public void setWaveParticipants(ArrayList<ISculkSmartEntity> waveParticipants) {
        this.waveParticipants = waveParticipants;
        SculkHorde.savedData.setDirty();
    }

    /**
     * Gets the raid state
     * @return the raid state
     */
    public boolean isRaidActive() {
        return raidState == RaidHandler.RaidState.ACTIVE_WAVE;
    }

    public RaidHandler.RaidState getRaidState() {
        return raidState;
    }

    public void setRaidState(RaidHandler.RaidState raidState) {
        this.raidState = raidState;
        SculkHorde.savedData.setDirty();
    }

    public RaidHandler.failureType getFailure() {
        return failure;
    }

    public void setFailure(RaidHandler.failureType failure) {
        setRaidState(RaidHandler.RaidState.FAILED);
        this.failure = failure;
        SculkHorde.savedData.setDirty();
    }

    public SculkEndermanEntity getScoutEnderman() {
        return scoutEnderman;
    }

    public void setScoutEnderman(SculkEndermanEntity scoutEnderman) {
        this.scoutEnderman = scoutEnderman;
        SculkHorde.savedData.setDirty();
    }

    public ServerBossEvent getBossEvent() {
        return bossEvent;
    }

    public void setBossEvent(ServerBossEvent bossEvent) {
        this.bossEvent = bossEvent;
        SculkHorde.savedData.setDirty();
    }

    public EntityFactory.StrategicValues[] getCurrentWavePattern() {
        return currentWavePattern;
    }

    public void setCurrentWavePattern(EntityFactory.StrategicValues[] currentWavePattern) {
        this.currentWavePattern = currentWavePattern;
        SculkHorde.savedData.setDirty();
    }

    public int getMaxWaves() {
        return maxWaves;
    }

    public void setMaxWaves(int maxWaves) {
        this.maxWaves = maxWaves;
        SculkHorde.savedData.setDirty();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public void setCurrentWave(int currentWave) {
        this.currentWave = currentWave;
        SculkHorde.savedData.setDirty();
    }

    public void incrementCurrentWave()
    {
        currentWave++;
        SculkHorde.savedData.setDirty();
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
        int aliveWaveParticipants = 0;
        int deadWaveParticipants = 0;
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(entity == null)
            {
                continue;
            }

            if(((Mob) entity).isAlive())
            {
                aliveWaveParticipants++;
            }
            else
            {
                deadWaveParticipants++;
            }
        }

        return (float) remainingWaveParticipants / (float) getWaveParticipants().size();
    }

    protected void updateRemainingWaveParticipantsAmount()
    {
        setRemainingWaveParticipants(0);
        for(ISculkSmartEntity entity : waveParticipants)
        {
            if(((Mob) entity).isAlive())
            {
                setRemainingWaveParticipants(getRemainingWaveParticipants() + 1);
            }
        }
        SculkHorde.savedData.setDirty();
    }

    public void setRemainingWaveParticipants(int remainingWaveParticipants) {
        this.remainingWaveParticipants = remainingWaveParticipants;
        SculkHorde.savedData.setDirty();
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
                entity.setParticipatingInRaid(false);
            }
            else
            {
                ((Mob) entity).discard();
            }
        }
        waveParticipants.clear();
        SculkHorde.savedData.setDirty();
    }

    public ModSavedData.AreaofInterestEntry getAreaOfInterestEntry() {
        return areaOfInterestEntry;
    }

    public void setAreaOfInterestEntry(ModSavedData.AreaofInterestEntry areaOfInterestEntry) {
        this.areaOfInterestEntry = areaOfInterestEntry;
        SculkHorde.savedData.setDirty();
    }

    public BlockSearcher getBlockSearcher() {
        return blockSearcher;
    }

    public void setBlockSearcher(BlockSearcher blockSearcher) {
        this.blockSearcher = blockSearcher;
        SculkHorde.savedData.setDirty();
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
    public final Predicate<BlockPos> isSpawnTarget = (blockPos) ->
    {
        return BlockAlgorithms.getBlockDistance(blockPos, raidLocation) > (getCurrentRaidRadius() * 0.75) && BlockAlgorithms.isAreaFlat(level, blockPos, 2);
    };

    public final Predicate<BlockPos> isObstructedInvestigateLocationState = (blockPos) ->
    {
        if(blockSearcher.foundTargets.isEmpty() && BlockAlgorithms.getBlockDistance(areaOfInterestEntry.getPosition(), blockPos) > MAXIMUM_RAID_RADIUS)
        {
            return true;
        }

        if(level.getBlockState(blockPos).is(Blocks.AIR))
        {
            return true;
        }

        if(!blockSearcher.foundTargets.isEmpty() && !blockSearcher.isAnyTargetCloserThan(blockPos, 25))
        {
            return true;
        }

        return !BlockAlgorithms.isExposedToAir(level, blockPos);
    };

    public final Predicate<BlockPos> isTargetInvestigateLocationState = (blockPos) ->
    {
        boolean isTarget = getLevel().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY)
                || getLevel().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY)
                || getLevel().getBlockState(blockPos).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY);

        // If the target is outside of the current raid radius, increase the raid radius
        if (isTarget && BlockAlgorithms.getBlockDistance(getAreaOfInterestEntry().getPosition(), blockPos) > getCurrentRaidRadius()) {
            setCurrentRaidRadius((int) BlockAlgorithms.getBlockDistance(getRaidLocation(), blockPos));
            SculkHorde.LOGGER.debug("Raid Radius is now " + getCurrentRaidRadius() + " blocks.");
        }

        if(isTarget && !blockSearcher.foundTargets.isEmpty() && blockSearcher.isAnyTargetCloserThan(blockPos, 5))
        {
            return false;
        }

        return isTarget;
    };

    // Save the variables to a CompoundTag
    public static void save(CompoundTag tag) {
        // Timing Variables
        tag.putInt("raidState", raidStateToInt(RaidHandler.raidData.getRaidState()));
        tag.putInt("waveDuration", RaidHandler.raidData.getWaveDuration());
        tag.putInt("timeElapsedScouting", RaidHandler.raidData.getTimeElapsedScouting());
        tag.putLong("spawnLocation", RaidHandler.raidData.getSpawnLocation().asLong());
        tag.putLong("raidLocation", RaidHandler.raidData.getRaidLocation().asLong());
        tag.putLong("objectiveLocation", RaidHandler.raidData.getObjectiveLocation().asLong());
        tag.putLong("objectiveLocationAtStartOfWave", RaidHandler.raidData.getObjectiveLocationAtStartOfWave().asLong());
        tag.putLong("raidCenter", RaidHandler.raidData.getRaidCenter().asLong());
        tag.putLong("raidCenter", RaidHandler.raidData.getRaidCenter().asLong());
        tag.putInt("currentRaidRadius", RaidHandler.raidData.getCurrentRaidRadius());
        tag.putInt("maxWaves", RaidHandler.raidData.getMaxWaves());
        tag.putInt("currentWave", RaidHandler.raidData.getCurrentWave());
        tag.putInt("remainingWaveParticipants", RaidHandler.raidData.getRemainingWaveParticipants());        // Save the wave participants as a list of UUIDs
        ListTag waveParticipantsTag = new ListTag();
        for (ISculkSmartEntity entity : RaidHandler.raidData.getWaveParticipants()) {
            if (entity instanceof Entity) {
                UUID uuid = ((Entity) entity).getUUID();
                waveParticipantsTag.add(NbtUtils.createUUID(uuid));
            }
        }
        tag.put("waveParticipants", waveParticipantsTag);

        // Enderman Scouting
        if (RaidHandler.raidData.getScoutEnderman() != null) {
            tag.putUUID("scoutEnderman", RaidHandler.raidData.getScoutEnderman().getUUID());
        }

        // Waves
        if (RaidHandler.raidData.getCurrentWavePattern() != null) {
            // Save the current wave pattern as a list of strings
            ListTag currentWavePatternTag = new ListTag();
            for (EntityFactory.StrategicValues value : RaidHandler.raidData.getCurrentWavePattern()) {
                currentWavePatternTag.add(StringTag.valueOf(value.name()));
            }
            tag.put("currentWavePattern", currentWavePatternTag);
        }

        // Area of Interest Entry
        if (RaidHandler.raidData.getAreaOfInterestEntry() != null) {
            CompoundTag areaOfInterestEntryTag = RaidHandler.raidData.getAreaOfInterestEntry().deserialize();
            tag.put("areaOfInterestEntry", areaOfInterestEntryTag);
        }

        // Targets
        // Save the high priority targets as a list of longs
        ListTag highPriorityTargetsTag = new ListTag();
        for (BlockPos pos : RaidHandler.raidData.getHighPriorityTargets()) {
            highPriorityTargetsTag.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("highPriorityTargets", highPriorityTargetsTag);

        // Save the medium priority targets as a list of longs
        ListTag mediumPriorityTargetsTag = new ListTag();
        for (BlockPos pos : RaidHandler.raidData.getMediumPriorityTargets()) {
            mediumPriorityTargetsTag.add(LongTag.valueOf(pos.asLong()));
        }
        tag.put("mediumPriorityTargets", mediumPriorityTargetsTag);
    }

    private static int raidStateToInt(RaidHandler.RaidState state) {
        SculkHorde.LOGGER.debug("Saving Raid State: " + state.name() + " as " + state.ordinal() + ".");
        return state.ordinal();
    }

    private static RaidHandler.RaidState intToRaidState(int state) {
        SculkHorde.LOGGER.debug("Loading Raid State: " + state + " as " + RaidHandler.RaidState.values()[state].name() + ".");
        return RaidHandler.RaidState.values()[state];
    }

    // Load the variables from a CompoundTag
    public static void load(CompoundTag tag) {

        // Timing Variables
        RaidHandler.raidData.setRaidState(intToRaidState(tag.getInt("raidState")));
        RaidHandler.raidData.setWaveDuration(tag.getInt("waveDuration"));
        RaidHandler.raidData.setTimeElapsedScouting(tag.getInt("timeElapsedScouting"));
        RaidHandler.raidData.setSpawnLocation(BlockPos.of(tag.getLong("spawnLocation")));
        RaidHandler.raidData.setRaidLocation(BlockPos.of(tag.getLong("raidLocation")));
        RaidHandler.raidData.setObjectiveLocation(BlockPos.of(tag.getLong("objectiveLocation")));
        RaidHandler.raidData.setObjectiveLocationAtStartOfWave(BlockPos.of(tag.getLong("objectiveLocationAtStartOfWave")));
        RaidHandler.raidData.setRaidCenter(BlockPos.of(tag.getLong("raidCenter")));
        RaidHandler.raidData.setMaxWaves(tag.getInt("maxWaves"));
        RaidHandler.raidData.setCurrentWave(tag.getInt("currentWave"));
        RaidHandler.raidData.setRemainingWaveParticipants(tag.getInt("remainingWaveParticipants"));

        // Load the wave participants from a list of UUIDs
        RaidHandler.raidData.getWaveParticipants().clear();
        ListTag waveParticipantsTag = tag.getList("waveParticipants", 11);
        for (Tag t : waveParticipantsTag) {
            if (t instanceof IntArrayTag) {
                UUID uuid = NbtUtils.loadUUID((IntArrayTag) t);
                Entity entity = SculkHorde.savedData.level.getEntity(uuid);
                if (entity instanceof ISculkSmartEntity) {
                    RaidHandler.raidData.getWaveParticipants().add((ISculkSmartEntity) entity);
                    ((ISculkSmartEntity) entity).setParticipatingInRaid(true);
                }
            }
        }

        // Enderman Scouting
        if (tag.hasUUID("scoutEnderman")) {
            UUID uuid = tag.getUUID("scoutEnderman");
            Entity entity = SculkHorde.savedData.level.getEntity(uuid);
            if (entity instanceof SculkEndermanEntity) {
                RaidHandler.raidData.setScoutEnderman((SculkEndermanEntity) entity);
            }
        }

        // Waves
        if (tag.contains("currentWavePattern")) {
            // Load the current wave pattern from a list of strings
            ListTag currentWavePatternTag = tag.getList("currentWavePattern", 8);
            RaidHandler.raidData.setCurrentWavePattern(new EntityFactory.StrategicValues[currentWavePatternTag.size()]);
            for (int i = 0; i < currentWavePatternTag.size(); i++) {
                Tag t = currentWavePatternTag.get(i);
                if (t instanceof StringTag) {
                    String s = ((StringTag) t).getAsString();
                    RaidHandler.raidData.getCurrentWavePattern()[i] = EntityFactory.StrategicValues.valueOf(s);
                }
            }
        }

        // Area of Interest Entry
        if (tag.contains("areaOfInterestEntry")) {
            CompoundTag areaOfInterestEntryTag = tag.getCompound("areaOfInterestEntry");
            RaidHandler.raidData.setAreaOfInterestEntry(ModSavedData.AreaofInterestEntry.serialize(areaOfInterestEntryTag));
        }

        // Targets
        // Load the high priority targets from a list of longs
        RaidHandler.raidData.getHighPriorityTargets().clear();
        ListTag highPriorityTargetsTag = tag.getList("highPriorityTargets", 4);
        for (Tag t : highPriorityTargetsTag) {
            if (t instanceof LongTag) {
                long l = ((LongTag) t).getAsLong();
                RaidHandler.raidData.getHighPriorityTargets().add(BlockPos.of(l));
            }
        }

        // Load the medium priority targets from a list of longs
        RaidHandler.raidData.getMediumPriorityTargets().clear();
        ListTag mediumPriorityTargetsTag = tag.getList("mediumPriorityTargets", 4);
        for (Tag t : mediumPriorityTargetsTag) {
            if (t instanceof LongTag) {
                long l = ((LongTag) t).getAsLong();
                RaidHandler.raidData.getMediumPriorityTargets().add(BlockPos.of(l));
            }
        }
    }
}
