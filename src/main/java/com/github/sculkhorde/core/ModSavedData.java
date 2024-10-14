package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.SculkBeeNestBlock;
import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.RaidData;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.core.gravemind.events.EventHandler;
import com.github.sculkhorde.misc.StatisticsData;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.github.sculkhorde.util.BlockAlgorithms.getBlockDistance;

/**
 * This class handels all data that gets saved to and loaded from the world. <br>
 * Learned World Data mechanics from: <a href="https://www.youtube.com/watch?v=tyTsdCzVz6w">...</a>
 */
public class ModSavedData extends SavedData {

    // identifier for debugmode
    private static final String debugModeIdentifier = "debugMode";

    //The world
    public final ServerLevel level;

    public static enum HordeState {
        UNACTIVATED,
        ACTIVE,
        DEFEATED
    }
    HordeState hordeState = HordeState.UNACTIVATED;

    public boolean isHordeUnactivated() {
        return hordeState == HordeState.UNACTIVATED;
    }

    public boolean isHordeActive() {
        return hordeState == HordeState.ACTIVE;
    }

    public boolean isHordeDefeated() {
        return hordeState == HordeState.DEFEATED || (ModConfig.SERVER.disable_sculk_horde_unless_activated.get() && hordeState == HordeState.UNACTIVATED);
    }

    public HordeState getHordeState() {
        return hordeState;
    }

    public void setHordeState(HordeState hordeState) {
        this.hordeState = hordeState;
        setDirty();
    }

    private final ArrayList<NodeEntry> nodeEntries = new ArrayList<>();
    private final ArrayList<BeeNestEntry> beeNestEntries = new ArrayList<>();
    private final Map<String, HostileEntry> hostileEntries = new HashMap<>();
    private final ArrayList<PriorityBlockEntry> priorityBlockEntries = new ArrayList<>();
    private final ArrayList<DeathAreaEntry> deathAreaEntries = new ArrayList<>();
    private final ArrayList<AreaOfInterestEntry> areasOfInterestEntries = new ArrayList<>();
    private final ArrayList<NoRaidZoneEntry> noRaidZoneEntries = new ArrayList<>();
    private final ArrayList<PlayerProfileEntry> playerProfileEntries = new ArrayList<>();

    private int sculkAccumulatedMass = 0;
    private static final String sculkAccumulatedMassIdentifier = "sculkAccumulatedMass";
    private int noNodeSpawningTicksElapsed = Gravemind.TICKS_BETWEEN_NODE_SPAWNS;
    private static final String ticksSinceSculkNodeDestructionIdentifier = "ticksSinceSculkNodeDestruction";
    private int ticksSinceLastRaid = TickUnits.convertHoursToTicks(8);
    private static final String ticksSinceLastRaidIdentifier = "ticksSinceLastRaid";

    /**
     * Default Constructor
     */
    public ModSavedData()
    {
        level = ServerLifecycleHooks.getCurrentServer().overworld();
    }

    /**
     * Get the memory object that stores all the data
     * @return The GravemindMemory
     */
    private static @NotNull ModSavedData getGravemindMemory()
    {
        return SculkHorde.savedData;
    }


    /**
     * This method gets called every time the world loads data from memory.
     * We extract data from the memory and store it in variables.
     *
     * @param nbt The memory where data is stored
     */
    public static ModSavedData load(CompoundTag nbt) {

        CompoundTag gravemindData = nbt.getCompound("gravemindData");

        SculkHorde.savedData = new ModSavedData();

        SculkHorde.savedData.getNodeEntries().clear();
        SculkHorde.savedData.getBeeNestEntries().clear();
        SculkHorde.savedData.getHostileEntries().clear();
        SculkHorde.savedData.getPriorityBlockEntries().clear();
        SculkHorde.savedData.getDeathAreaEntries().clear();
        SculkHorde.savedData.getAreasOfInterestEntries().clear();

        SculkHorde.savedData.setHordeState(HordeState.values()[nbt.getInt("hordeState")]);
        SculkHorde.savedData.setSculkAccumulatedMass(nbt.getInt(sculkAccumulatedMassIdentifier));
        SculkHorde.savedData.setNoNodeSpawningTicksElapsed(nbt.getInt(ticksSinceSculkNodeDestructionIdentifier));

        SculkHorde.savedData.setTicksSinceLastRaid(nbt.getInt(ticksSinceLastRaidIdentifier));

        SculkHorde.setDebugMode(nbt.getBoolean(debugModeIdentifier));

        for (int i = 0; gravemindData.contains("node_entry" + i); i++) {
            SculkHorde.savedData.getNodeEntries().add(NodeEntry.serialize(gravemindData.getCompound("node_entry" + i)));
        }

        for (int i = 0; gravemindData.contains("bee_nest_entry" + i); i++) {
            SculkHorde.savedData.getBeeNestEntries().add(BeeNestEntry.serialize(gravemindData.getCompound("bee_nest_entry" + i)));
        }

        for (int i = 0; gravemindData.contains("hostile_entry" + i); i++) {
            HostileEntry hostileEntry = HostileEntry.serialize(gravemindData.getCompound("hostile_entry" + i));
            SculkHorde.savedData.getHostileEntries().putIfAbsent(hostileEntry.identifier, hostileEntry);
        }

        for (int i = 0; gravemindData.contains("priority_block_entry" + i); i++) {
            SculkHorde.savedData.getPriorityBlockEntries().add(PriorityBlockEntry.serialize(gravemindData.getCompound("priority_block_entry" + i)));
        }

        for (int i = 0; gravemindData.contains("death_area_entry" + i); i++) {
            SculkHorde.savedData.getDeathAreaEntries().add(DeathAreaEntry.serialize(gravemindData.getCompound("death_area_entry" + i)));
        }

        for (int i = 0; gravemindData.contains("area_of_interest_entry" + i); i++) {
            SculkHorde.savedData.getAreasOfInterestEntries().add(AreaOfInterestEntry.serialize(gravemindData.getCompound("area_of_interest_entry" + i)));
        }

        for(int i = 0; gravemindData.contains("no_raid_zone_entry" + i); i++) {
            SculkHorde.savedData.getNoRaidZoneEntries().add(NoRaidZoneEntry.serialize(gravemindData.getCompound("no_raid_zone_entry" + i)));
        }

        for(int i = 0; gravemindData.contains("player_profile_entry" + i); i++) {
            SculkHorde.savedData.getPlayerProfileEntries().add(PlayerProfileEntry.serialize(gravemindData.getCompound("player_profile_entry" + i)));
        }

        if(RaidHandler.raidData == null)
        {
            RaidHandler.raidData = new RaidData();
        }

        if(SculkHorde.statisticsData == null)
        {
            SculkHorde.statisticsData = new StatisticsData();
        }

        StatisticsData.load(nbt);
        RaidData.load(nbt);
        BlockEntityChunkLoaderHelper.load(nbt);
        EntityChunkLoaderHelper.load(nbt);
        EventHandler.load(nbt);

        return getGravemindMemory();

    }

    public ArrayList<NoRaidZoneEntry> getNoRaidZoneEntries() {
        return noRaidZoneEntries;
    }

    /**
     * This method gets called every time the world saves data from memory.
     * We take the data in our variables and store it to memory.
     *
     * @param nbt The memory where data is stored
     */
    @Override
    public @NotNull CompoundTag save(CompoundTag nbt) {
        CompoundTag gravemindData = new CompoundTag();

        nbt.putInt("hordeState", hordeState.ordinal());
        nbt.putInt(sculkAccumulatedMassIdentifier, sculkAccumulatedMass);
        nbt.putInt(ticksSinceSculkNodeDestructionIdentifier, noNodeSpawningTicksElapsed);
        nbt.putInt(ticksSinceLastRaidIdentifier, ticksSinceLastRaid);
        nbt.putBoolean(debugModeIdentifier, SculkHorde.isDebugMode());

        for (ListIterator<NodeEntry> iterator = getNodeEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("node_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        for (ListIterator<BeeNestEntry> iterator = getBeeNestEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("bee_nest_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        int hostileIndex = 0;
        for (Map.Entry<String, HostileEntry> entry : getHostileEntries().entrySet()) {
            gravemindData.put("hostile_entry" + hostileIndex, entry.getValue().deserialize());
            hostileIndex++;
        }

        for (ListIterator<PriorityBlockEntry> iterator = getPriorityBlockEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("priority_block_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        for (ListIterator<DeathAreaEntry> iterator = getDeathAreaEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("death_area_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        for (ListIterator<AreaOfInterestEntry> iterator = getAreasOfInterestEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("area_of_interest_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        for (ListIterator<NoRaidZoneEntry> iterator = getNoRaidZoneEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("no_raid_zone_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        for (ListIterator<PlayerProfileEntry> iterator = getPlayerProfileEntries().listIterator(); iterator.hasNext(); ) {
            gravemindData.put("player_profile_entry" + iterator.nextIndex(), iterator.next().deserialize());
        }

        nbt.put("gravemindData", gravemindData);

        RaidData.save(nbt);
        StatisticsData.save(nbt);
        BlockEntityChunkLoaderHelper.save(nbt);
        EntityChunkLoaderHelper.save(nbt);
        EventHandler.save(nbt);

        return nbt;
    }



    /**
     * Accessors
     **/

    public boolean isRaidCooldownOver() {
        return getTicksSinceLastRaid() >= TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_global_cooldown_between_raids_minutes.get());
    }

    public int getTicksSinceLastRaid() {
        setDirty();
        return ticksSinceLastRaid;
    }

    public void setTicksSinceLastRaid(int ticksSinceLastRaid) {
        this.ticksSinceLastRaid = ticksSinceLastRaid;
        setDirty();
    }

    public void incrementTicksSinceLastRaid() {
        this.ticksSinceLastRaid++;
        setDirty();
    }

    public boolean isNodeSpawnCooldownOver() {
        long ticksElapsed = getTicksElapsedForNodeSpawningCooldown();
        long ticksNeeded = Gravemind.TICKS_BETWEEN_NODE_SPAWNS;
        boolean result = ticksElapsed >= ticksNeeded;
        return result;
    }

    public long getMinutesRemainingUntilNodeSpawn()
    {
        long ticksElapsed = getTicksElapsedForNodeSpawningCooldown();
        long ticksNeeded = Gravemind.TICKS_BETWEEN_NODE_SPAWNS;
        long result = Math.max(0, ticksNeeded - ticksElapsed);
        return TickUnits.convertTicksToMinutes(result);
    }

    public int getTicksElapsedForNodeSpawningCooldown() {
        setDirty();
        return noNodeSpawningTicksElapsed;
    }

    public void setNoNodeSpawningTicksElapsed(int noNodeSpawningTicksElapsed) {
        this.noNodeSpawningTicksElapsed = noNodeSpawningTicksElapsed;
        setDirty();
    }

    public void incrementNoNodeSpawningTicksElapsed() {
        this.noNodeSpawningTicksElapsed++;
        setDirty();
    }

    public void resetNoNodeSpawningTicksElapsed() {
        //Send message to all players that node has spawned
        this.noNodeSpawningTicksElapsed = 0;
        setDirty();
    }

    /**
     * Gets how much Sculk mass the Sculk horde has.
     *
     * @return An integer representing all Sculk mass accumulated.
     */
    public int getSculkAccumulatedMass() {
        setDirty();
        return sculkAccumulatedMass;
    }

    /**
     * Adds to the sculk accumulated mass
     *
     * @param amount The amount you want to add
     */
    public int addSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass += amount;
        return sculkAccumulatedMass;
    }

    /**
     * Subtracts from the Sculk Accumulate Mass
     *
     * @param amount The amount to substract
     */
    public int subtractSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass -= amount;
        return sculkAccumulatedMass;
    }

    /**
     * Sets the value of sculk accumulate mass.
     *
     * @param amount The amount to set it to.
     */
    public int setSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass = amount;
        return sculkAccumulatedMass;
    }

    public ArrayList<NodeEntry> getNodeEntries() {
        return nodeEntries;
    }

    public ArrayList<BeeNestEntry> getBeeNestEntries() {
        return beeNestEntries;
    }

    public Map<String, HostileEntry> getHostileEntries() {
        return hostileEntries;
    }

    public ArrayList<PriorityBlockEntry> getPriorityBlockEntries() {
        return priorityBlockEntries;
    }

    public ArrayList<DeathAreaEntry> getDeathAreaEntries() {
        return deathAreaEntries;
    }

    public ArrayList<AreaOfInterestEntry> getAreasOfInterestEntries() {
        return areasOfInterestEntries;
    }

    /**
     * Adds a position to the list if it does not already exist
     *
     * @param positionIn The Posoition to add
     */
    public void addNodeToMemory(ServerLevel level, BlockPos positionIn)
    {
        if (!isNodePositionInMemory(positionIn) && getNodeEntries() != null)
        {
            getNodeEntries().add(new NodeEntry(level, positionIn));
            setDirty();
        }
    }

    /**
     * Adds a position to the list if it does not already exist
     *
     * @param positionIn The Position to add
     */
    public void addBeeNestToMemory(ServerLevel level, BlockPos positionIn)
    {
        if (!isBeeNestPositionInMemory(positionIn) && getBeeNestEntries() != null)
        {
            getBeeNestEntries().add(new BeeNestEntry(level, positionIn));
            setDirty();
        }
        // TODO For some reason this continously gets called, find out why
        //else if(DEBUG_MODE) System.out.println("Attempted to Add Nest To Memory but failed.");
    }

    public void addHostileToMemory(LivingEntity entityIn)
    {
        if (entityIn == null || EntityAlgorithms.isSculkLivingEntity.test(entityIn) || entityIn instanceof Creeper)
        {
            return;
        }

        String identifier = entityIn.getType().toString();
        if (!identifier.isEmpty())
        {
            getHostileEntries().putIfAbsent(identifier, new HostileEntry(identifier));
            setDirty();
        }
    }

    public void addPriorityBlockToMemory(BlockPos positionIn) {

        // If the list is null, dont even try
        if (getPriorityBlockEntries() == null)
        {
            return;
        }

        // If the block is already in the list, dont add it again
        for (PriorityBlockEntry entry : getPriorityBlockEntries())
        {
            if (entry.position == positionIn)
            {
                return;
            }
        }

        int priority;

        // Determine the priority of the block
        if(level.getBlockState(positionIn).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY))
        {
            priority = 2;
        }
        else if(level.getBlockState(positionIn).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY))
        {
            priority = 1;
        }
        else if (level.getBlockState(positionIn).is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY))
        {
            priority = 0;
        }
        else
        {
            SculkHorde.LOGGER.warn("Attempted to add a block to the priority list that was not a priority block");
            return;
        }

        getPriorityBlockEntries().add(new PriorityBlockEntry(positionIn, priority));
        setDirty();

    }

    public void addDeathAreaToMemory(ServerLevel dimension, BlockPos positionIn)
    {
        if(getDeathAreaEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to add a death area to memory but the list was null");
            return;
        }

        // If already exists in memory, dont add it again
        for(int i = 0; i < getDeathAreaEntries().size(); i++)
        {
            if(getDeathAreaEntries().get(i).position == positionIn)
            {
                return;
            }
        }

        SculkHorde.LOGGER.info("Adding Death Area in " + dimension.dimension() + " at " + positionIn + " to memory");
        getDeathAreaEntries().add(new DeathAreaEntry(dimension, positionIn));
        setDirty();
    }

    public Optional<AreaOfInterestEntry> addAreaOfInterestToMemory(ServerLevel dimension, BlockPos positionIn) {
        if(getAreasOfInterestEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to add an area of interest to memory but the list was null");
            return Optional.empty();
        }

        // If already exists in memory, dont add it again
        for(int i = 0; i < getAreasOfInterestEntries().size(); i++)
        {
            if(getAreasOfInterestEntries().get(i).position == positionIn || getAreasOfInterestEntries().get(i).position.closerThan(positionIn, 100))
            {
                return Optional.empty();
            }
        }

        SculkHorde.LOGGER.info("Adding Area of Interest at " + dimension.dimension() + " at " + positionIn + " to memory");
        AreaOfInterestEntry entry = new AreaOfInterestEntry(dimension, positionIn);
        getAreasOfInterestEntries().add(entry);
        setDirty();
        return Optional.of(entry);
    }

    public void addNoRaidZoneToMemory(ServerLevel dimension, BlockPos positionIn) {
        if(getNoRaidZoneEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to add a no raid zone to memory but the list was null");
            return;
        }

        // If already exists in memory, dont add it again
        for(int i = 0; i < getNoRaidZoneEntries().size(); i++)
        {
            boolean areInSameDimension = BlockAlgorithms.areTheseDimensionsEqual(getNoRaidZoneEntries().get(i).dimension, dimension.dimension());
            boolean arePositionsEqual = getNoRaidZoneEntries().get(i).position.equals(positionIn);
            boolean isCloserThan100BlocksFromPosition = getNoRaidZoneEntries().get(i).position.closerThan(positionIn, 100);

            if((areInSameDimension && arePositionsEqual) || (areInSameDimension && isCloserThan100BlocksFromPosition))
            {
                if(isCloserThan100BlocksFromPosition) { SculkHorde.LOGGER.debug("Attempted to add a no raid zone to memory but it was too close to another no raid zone"); }
                else if(arePositionsEqual) { SculkHorde.LOGGER.debug("Attempted to add a no raid zone to memory but it already existed"); }

                return;
            }
        }

        SculkHorde.LOGGER.info("Adding No Raid Zone at " + positionIn + " in " + dimension.dimension() + " to memory");
        getNoRaidZoneEntries().add(new NoRaidZoneEntry(dimension, positionIn, 1000, level.getGameTime(), TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_no_raid_zone_duration_minutes.get())));
        setDirty();
    }

    private Optional<DeathAreaEntry> getDeathAreaWithinRange(BlockPos positionIn, int range)
    {
        if(getDeathAreaEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to get a death area from memory but the list was null");
            return Optional.empty();
        }

        for(int i = 0; i < getDeathAreaEntries().size(); i++)
        {
            if(getDeathAreaEntries().get(i).position.closerThan(positionIn, range))
            {
                return Optional.of(getDeathAreaEntries().get(i));
            }
        }
        return Optional.empty();
    }

    public Optional<DeathAreaEntry> getDeathAreaWithHighestDeaths()
    {
        if(getDeathAreaEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to get a death area from memory but the list was null");
            return Optional.empty();
        }

        int highestDeathCount = 0;
        DeathAreaEntry highestDeathArea = null;

        for(int i = 0; i < getDeathAreaEntries().size(); i++)
        {
            if(getDeathAreaEntries().get(i).deathCount > highestDeathCount)
            {
                highestDeathCount = getDeathAreaEntries().get(i).deathCount;
                highestDeathArea = getDeathAreaEntries().get(i);
            }
        }

        if(highestDeathArea == null)
        {
            return Optional.empty();
        }

        return Optional.of(highestDeathArea);
    }

    /**
     * Will try to return an Area of Interest Entry that is not in a no raid zone.
     * @return Optional<AreaofInterestEntry> - The area of interest entry that is not in a no raid zone
     */
    public Optional<AreaOfInterestEntry> getAreaOfInterestEntryNotInNoRaidZone()
    {
        if(getAreasOfInterestEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to get an area of interest from memory but the list was null");
            return null;
        }

        for(int i = 0; i < getAreasOfInterestEntries().size(); i++)
        {
            // If the area of interest is not in a no raid zone, return it
            if(!getAreasOfInterestEntries().get(i).isInNoRaidZone())
            {
                return Optional.of(getAreasOfInterestEntries().get(i));
            }
        }
        return Optional.empty();
    }

    /**
     * Will verify all enties to see if they exist in the world.
     * If not, they will be removed. <br>
     * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
     */
    public void validateNodeEntries() {
        long startTime = System.nanoTime();
        Iterator<NodeEntry> iterator = getNodeEntries().iterator();
        while (iterator.hasNext()) {
            NodeEntry entry = iterator.next();
            if (!entry.isEntryValid()) {
                resetNoNodeSpawningTicksElapsed();
                iterator.remove();
                setDirty();
            }
        }
        long endTime = System.nanoTime();
        if (SculkHorde.isDebugMode()) {
            System.out.println("Node Validation Took " +
                    TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) +
                    " milliseconds");
        }
    }


    /**
     * Will verify all enties to see if they exist in the world.
     * Will also reasses the parentNode for each one.
     * If not, they will be removed. <br>
     * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
     */
    public void validateBeeNestEntries() {
        long startTime = System.nanoTime();
        List<BeeNestEntry> toRemove = new ArrayList<>();
        Iterator<BeeNestEntry> iterator = getBeeNestEntries().iterator();
        while (iterator.hasNext()) {
            BeeNestEntry entry = iterator.next();
            entry.setParentNodeToClosest();
            if (!entry.isEntryValid()) {
                toRemove.add(entry);
            }
        }
        getBeeNestEntries().removeAll(toRemove);
        setDirty();
        long endTime = System.nanoTime();
        if (SculkHorde.isDebugMode()) {
            System.out.println("Bee Nest Validation Took " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " milliseconds");
        }
    }

    public void validateAreasOfInterest() {
        long startTime = System.currentTimeMillis();
        Iterator<AreaOfInterestEntry> iterator = getAreasOfInterestEntries().iterator();
        while (iterator.hasNext()) {
            AreaOfInterestEntry entry = iterator.next();
            if (!entry.isInNoRaidZone()) {
                SculkHorde.LOGGER.info("Area of Interest at " + entry.position + " is on no raid zone. Removing from memory.");
                iterator.remove();
                setDirty();
            }
        }
        long endTime = System.currentTimeMillis();
        if (SculkHorde.isDebugMode()) {
            SculkHorde.LOGGER.info("Area Of Interest Validation Took " + (endTime - startTime) + " milliseconds");
        }
    }

    public void validateNoRaidZoneEntries() {
        long startTime = System.currentTimeMillis();
        Iterator<NoRaidZoneEntry> iterator = getNoRaidZoneEntries().iterator();
        while (iterator.hasNext()) {
            NoRaidZoneEntry entry = iterator.next();
            if (entry.isExpired(level.getGameTime())) {
                SculkHorde.LOGGER.info("No Raid Zone Entry at " + entry.position + " has expired. Removing from memory.");
                iterator.remove();
                setDirty();
            }
        }
        long endTime = System.currentTimeMillis();
        if (SculkHorde.isDebugMode()) {
            SculkHorde.LOGGER.info("No Raid Zone Validation Took " + (endTime - startTime) + " milliseconds");
        }
    }


    /**
     * Will check the positons of all entries to see
     * if they match the parameter.
     *
     * @param position The position to cross reference
     * @return true if in memory, false otherwise
     */
    public boolean isBeeNestPositionInMemory(BlockPos position) {
        for (BeeNestEntry entry : getBeeNestEntries()) {
            if (entry.position == position) {
                return true;
            }
        }
        return false;
    }


    /**
     * Will check the positons of all entries to see
     * if they match the parameter.
     *
     * @param position The position to cross-reference
     * @return true if in memory, false otherwise
     */
    public boolean isNodePositionInMemory(BlockPos position) {
        for (NodeEntry entry : getNodeEntries()) {
            if (entry.position.equals(position)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a list of known node positions
     *
     * @return The Closest TreeNode
     */
    public NodeEntry getClosestNodeEntry(ServerLevel dimension, BlockPos pos) {
        NodeEntry closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        for (NodeEntry node : getNodeEntries()) {
            if (pos.distSqr(node.position) < closestDistance) {
                closestNode = node;
                closestDistance = pos.distSqr(node.position);
            }
        }
        return closestNode;
    }

    public void removeNodeFromMemory(BlockPos positionIn)
    {
        if(getNodeEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to remove an area of interest from memory but the list was null");
            return;
        }

        for(int i = 0; i < getNodeEntries().size(); i++)
        {
            if(getNodeEntries().get(i).position.equals(positionIn))
            {
                getNodeEntries().remove(i);
                setDirty();
                resetNoNodeSpawningTicksElapsed();
                return;
            }
        }
        setDirty();
    }

    public void removeDeathAreaFromMemory(BlockPos positionIn)
    {
        if(getDeathAreaEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to remove a death area from memory but the list was null");
            return;
        }

        for(int i = 0; i < getDeathAreaEntries().size(); i++)
        {
            if(getDeathAreaEntries().get(i).position == positionIn)
            {
                getDeathAreaEntries().remove(i);
                setDirty();
                return;
            }
        }
        setDirty();
    }

    public void removeAreaOfInterestFromMemory(BlockPos positionIn)
    {
        if(getAreasOfInterestEntries() == null)
        {
            SculkHorde.LOGGER.warn("Attempted to remove an area of interest from memory but the list was null");
            return;
        }

        for(int i = 0; i < getAreasOfInterestEntries().size(); i++)
        {
            if(getAreasOfInterestEntries().get(i).position == positionIn)
            {
                getAreasOfInterestEntries().remove(i);
                setDirty();
                return;
            }
        }
        setDirty();
    }

    /**
     * This method gets called every time a sculk mob dies.
     * We check if the death happened in a death area.
     * If it did, we iterate the death count of that area.
     * If it did not, we create a new death area.
     *
     * @param deathPosition The position where the player died
     */
    public void reportDeath(ServerLevel level, BlockPos deathPosition)
    {
        // If a death area already exist close to this location, iterate the death count
        Optional<DeathAreaEntry> deathArea = getDeathAreaWithinRange(deathPosition, 100);
        if(deathArea.isPresent())
        {
            deathArea.get().iterateDeathCount();
            setDirty();
            return;
        }

        // If the death area does not exist, create a new one
        addDeathAreaToMemory(level, deathPosition);
    }

    public static class PriorityBlockEntry
    {
        private final BlockPos position;
        private final int priority;

        public PriorityBlockEntry(BlockPos positionIn, int priorityIn)
        {
            position = positionIn;
            priority = priorityIn;
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public int getPriority()
        {
            return priority;
        }

        public boolean isEntryValid(ServerLevel level)
        {
            BlockState blockState = level.getBlockState(position);

            if (blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_HIGH_PRIORITY) && priority == 2)
            {
                return true;
            }
            else if (blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_MEDIUM_PRIORITY) && priority == 1)
            {
                return true;
            }
            else if (blockState.is(ModBlocks.BlockTags.SCULK_RAID_TARGET_LOW_PRIORITY) && priority == 0)
            {
                return true;
            }

            return false;

        }


        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            nbt.putLong("priority", priority);
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static PriorityBlockEntry serialize(CompoundTag nbt)
        {
            return new PriorityBlockEntry(BlockPos.of(nbt.getLong("position")), nbt.getInt("priority"));
        }


    }

    /**
     * This class is a representation of the actual
     * Sculk Nodes in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    public static class NodeEntry
    {
        private final BlockPos position; //The Location in the world where the node is
        private long lastTimeWasActive;
        private long activationTimeStamp;
        private boolean IsActive;
        private ResourceKey<Level> dimension;


        /**
         * Default Constructor
         * @param positionIn The physical location
         */
        public NodeEntry(ServerLevel level, BlockPos positionIn)
        {
            position = positionIn;
            lastTimeWasActive = SculkHorde.savedData.level.getGameTime();

            this.dimension = level.dimension();
        }

        /**
         * Default Constructor
         * @param positionIn The physical location
         */
        public NodeEntry(ResourceKey<Level> dimensionResource, BlockPos positionIn)
        {
            position = positionIn;
            lastTimeWasActive = SculkHorde.savedData.level.getGameTime();

            this.dimension = dimensionResource;
        }
        public ServerLevel getDimension()
        {
            return SculkHorde.savedData.level.getServer().getLevel(dimension);
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public boolean isActive()
        {
            return IsActive;
        }

        public void setActive(boolean activeIn)
        {
            if(getDimension() == null)
            {
                SculkHorde.LOGGER.error("Failed To Set Node Active. Dimension was null.");
                return;
            }
            else if(getDimension().getBlockEntity(position) == null)
            {
                SculkHorde.LOGGER.error("Failed To Set Node Active. Block Entity was null.");
                return;
            }
            else if(!(getDimension().getBlockEntity(position) instanceof SculkNodeBlockEntity))
            {
                SculkHorde.LOGGER.error("Failed To Set Node Active. Block Entity was not instance of Sculk Node Block Entity.");
                return;
            }

            IsActive = activeIn;
            SculkNodeBlockEntity sculkNodeBlockEntity = (SculkNodeBlockEntity) getDimension().getBlockEntity(position);
            sculkNodeBlockEntity.setActive(activeIn);
        }

        public long getLastTimeWasActive()
        {
            return lastTimeWasActive;
        }

        public void setLastTimeWasActive(long lastTimeWasActiveIn)
        {
            lastTimeWasActive = lastTimeWasActiveIn;
        }

        public void setActivationTimeStamp(long activationTimeStampIn)
        {
            activationTimeStamp = activationTimeStampIn;
        }

        public long getActivationTimeStamp()
        {
            return activationTimeStamp;
        }

        /**
         * Checks the world to see if the node is still there.
         * @return True if in the world at location, false otherwise
         */
        public boolean isEntryValid()
        {
            if(getDimension() == null) { return false; }
            return getDimension().getBlockState(position).getBlock().equals(ModBlocks.SCULK_NODE_BLOCK.get());
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            nbt.putLong("lastTimeWasActive", lastTimeWasActive);
            nbt.putLong("activationTimeStamp", activationTimeStamp);
            nbt.putBoolean("IsActive", IsActive);
            // Put Dimension ID
            nbt.putString("dimension", dimension.location().toString());
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static NodeEntry serialize(CompoundTag nbt)
        {
            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));
            NodeEntry entry = new NodeEntry(dimensionResourceKey, BlockPos.of(nbt.getLong("position")));
            entry.setLastTimeWasActive(nbt.getLong("lastTimeWasActive"));
            entry.setActivationTimeStamp(nbt.getLong("activationTimeStamp"));
            entry.setActive(nbt.getBoolean("IsActive"));
            // Get Dimension

            return entry;
        }

    }

    /**
     * This class is a representation of the actual
     * Bee Nests in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    public static class BeeNestEntry
    {
        private final BlockPos position; //The location in the world where the node is
        private BlockPos parentNodePosition; //The location of the Sculk TreeNode that this Nest belongs to

        private ResourceKey<Level> dimension;

        /**
         * Default Constructor
         * @param positionIn The Position of this Nest
         */
        public BeeNestEntry(ServerLevel dimension, BlockPos positionIn)
        {
            this.dimension = dimension.dimension();
            position = positionIn;
        }

        public BeeNestEntry(ServerLevel dimension, BlockPos positionIn, BlockPos parentPositionIn)
        {
            position = positionIn;
            parentNodePosition = parentPositionIn;
            this.dimension = dimension.dimension();
        }

        public BeeNestEntry(ResourceKey<Level> dimension, BlockPos positionIn, BlockPos parentPositionIn)
        {
            position = positionIn;
            parentNodePosition = parentPositionIn;
            this.dimension = dimension;
        }

        public ServerLevel getDimension()
        {
            return SculkHorde.savedData.level.getServer().getLevel(dimension);
        }

        /**
         * Checks if the block does still exist in the world.
         * @return True if valid, false otherwise.
         */
        public boolean isEntryValid()
        {
            ServerLevel dimension = getDimension();

            if(dimension == null)
            {
                SculkHorde.LOGGER.error("Failed To Validate Bee Nest Entry. Dimension was null.");
                return false;
            }
            else if(dimension.getBlockEntity(position) == null)
            {
                SculkHorde.LOGGER.error("Failed To Validate Bee Nest Entry. Block Entity was null.");
                return false;
            }

            return dimension.getBlockState(position).getBlock().equals(ModBlocks.SCULK_BEE_NEST_BLOCK.get());
        }


        /**
         * is Hive enabled?
         * @return True if enabled, false otherwise
         */
        public boolean isOccupantsExistingDisabled()
        {
            if(getDimension() == null) { return true; }
            return SculkBeeNestBlock.isNestClosed(getDimension().getBlockState(position));
        }

        /**
         * Sets Hive to deny bees leaving
         */
        public void disableOccupantsExiting()
        {
            if(getDimension() == null) { return; }
            SculkBeeNestBlock.setNestClosed(getDimension(), getDimension().getBlockState(position), position);
        }


        /**
         * Sets Hive to allow bees leaving
         */
        public void enableOccupantsExiting()
        {
            if(getDimension() == null) { return; }
            SculkBeeNestBlock.setNestOpen(getDimension(), getDimension().getBlockState(position), position);
        }


        public Optional<NodeEntry> getClosestNode(BlockPos pos)
        {
            Optional<NodeEntry> closestEntry = Optional.empty();
            for(NodeEntry entry : getGravemindMemory().getNodeEntries())
            {
                // If we are not in the same dimension
                if(!entry.dimension.equals(dimension))
                {
                    continue;
                }

                if(closestEntry.isEmpty())
                {
                    closestEntry = Optional.of(entry);
                }
                //If entry is closer than our current closest entry
                else if(getBlockDistance(pos, entry.position) < getBlockDistance(pos, closestEntry.get().position))
                {
                    closestEntry = Optional.of(entry);
                }
            }
            return closestEntry;
        }

        /**
         * Checks list of node entries and finds the closest one.
         * It then sets the parentNodePosition to be the position of
         * the closest node.
         */
        public void setParentNodeToClosest()
        {
            //Make sure nodeEntries isn't null and nodeEntries isn't empty
            if(getGravemindMemory().getNodeEntries() != null && !getGravemindMemory().getNodeEntries().isEmpty())
            {
                Optional<NodeEntry> closestEntry = Optional.empty();
                for(NodeEntry entry : getGravemindMemory().getNodeEntries())
                {
                    // If we are not in the same dimension
                    if(!entry.dimension.equals(dimension))
                    {
                        continue;
                    }

                    if(Optional.of(entry).isEmpty())
                    {
                        SculkHorde.LOGGER.error("Failed To Set Parent Node To Closest. Node Entry was null.");
                        continue;
                    }

                    if(closestEntry.isEmpty())
                    {
                        closestEntry = Optional.of(entry);
                    }
                    //If entry is closer than our current closest entry
                    // NOTE: We shouldnt need the isPresent() check here but it was throwing an exception
                    else if(closestEntry.isPresent() && getBlockDistance(position, entry.position) < getBlockDistance(position, closestEntry.get().position))
                    {
                        closestEntry = Optional.of(entry);
                    }
                }

                if(closestEntry.isPresent() && closestEntry.get().getPosition() != null) { parentNodePosition = closestEntry.get().getPosition(); }
            }
        }


        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            if(dimension != null) nbt.putString("dimension", dimension.location().toString());
            if(parentNodePosition != null) nbt.putLong("parentNodePosition", parentNodePosition.asLong());
            return nbt;
        }


        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static BeeNestEntry serialize(CompoundTag nbt)
        {
            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));

            return new BeeNestEntry(dimensionResourceKey, BlockPos.of(nbt.getLong("position")), BlockPos.of(nbt.getLong("parentNodePosition")));
        }
    }

    /**
     * This class is a representation of the actual
     * Sculk Nodes in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    private static class HostileEntry
    {
        private final String identifier; //The String that is the class name identifier of the mob. Example: class net.minecraft.entity.monster.SpiderEntity

        /**
         * Default Constructor
         * @param identifierIn The String that is the class name identifier of the mob. <br>
         * Example: class net.minecraft.entity.monster.SpiderEntity
         */
        public HostileEntry(String identifierIn)
        {
            identifier = identifierIn;
        }


        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("identifier", identifier);
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static HostileEntry serialize(CompoundTag nbt)
        {
            return new HostileEntry(nbt.getString("identifier"));
        }

    }

    public static class DeathAreaEntry
    {
        private final BlockPos position; // The Location of the Death Area
        private int deathCount; // The number of deaths that have occurred in this area

        private ResourceKey<Level> dimension;

        public DeathAreaEntry(ServerLevel dimension, BlockPos positionIn)
        {
            position = positionIn;
            deathCount = 1;
            this.dimension = dimension.dimension();
        }

        public DeathAreaEntry(ResourceKey<Level> dimension, BlockPos positionIn, int deathCountIn)
        {
            position = positionIn;
            deathCount = deathCountIn;
            this.dimension = dimension;
        }

        public ServerLevel getDimension()
        {
            return SculkHorde.savedData.level.getServer().getLevel(dimension);
        }

        public void setDeathCount(int deathCountIn)
        {
            deathCount = deathCountIn;
        }

        public int getDeathCount()
        {
            return deathCount;
        }

        public void iterateDeathCount()
        {
            deathCount++;
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public boolean isValid()
        {
            boolean isDimensionValid = getDimension() != null;
            boolean isPositionValid = getPosition() != null;

            return isDimensionValid && isPositionValid;
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            nbt.putInt("deathCount", deathCount);
            if(dimension != null) nbt.putString("dimension", dimension.location().toString());
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static DeathAreaEntry serialize(CompoundTag nbt) {

            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));
            return new DeathAreaEntry(dimensionResourceKey, BlockPos.of(nbt.getLong("position")), nbt.getInt("deathCount"));
        }
    }

    public static class AreaOfInterestEntry
    {
        private final BlockPos position; // The Location of the Death Area
        private final ResourceKey<Level> dimension;
        private long ticksSinceLastRaid;

        public AreaOfInterestEntry(ServerLevel dimension, BlockPos positionIn)
        {
            this.dimension = dimension.dimension();
            position = positionIn;
        }

        public AreaOfInterestEntry(ResourceKey<Level> dimension, BlockPos positionIn, long ticksSinceLastRaidIn)
        {
            this.dimension = dimension;
            position = positionIn;
            ticksSinceLastRaid = ticksSinceLastRaidIn;
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public ServerLevel getDimension()
        {
            return SculkHorde.savedData.level.getServer().getLevel(dimension);
        }

        public boolean isInNoRaidZone()
        {
            for(NoRaidZoneEntry entry : SculkHorde.savedData.getNoRaidZoneEntries())
            {
                if(entry.isBlockPosInRadius(entry.getDimension(), getPosition()))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean isEntryValid()
        {
            boolean isDimensionValid = getDimension() != null;
            boolean isPositionValid = getPosition() != null;

            return isDimensionValid && isPositionValid;
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            nbt.putLong("ticksSinceLastRaid", ticksSinceLastRaid);
            nbt.putString("dimension", dimension.location().toString());
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static AreaOfInterestEntry serialize(CompoundTag nbt) {
            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));
            return new AreaOfInterestEntry(dimensionResourceKey, BlockPos.of(nbt.getLong("position")), nbt.getLong("ticksSinceLastRaid"));
        }
    }

    public static class NoRaidZoneEntry
    {
        private final BlockPos position; // The Location
        private final int radius;
        private final long timeOfCreation; // this.level.getGameTime();
        private long durationInTicksUntilExpiration;

        private final ResourceKey<Level> dimension;

        public NoRaidZoneEntry(ServerLevel dimension, BlockPos positionIn, int radiusIn, long gameTimeStampIn, long durationUntilExpirationIn)
        {
            this.dimension = dimension.dimension();
            position = positionIn;
            radius = radiusIn;
            timeOfCreation = gameTimeStampIn;
            durationInTicksUntilExpiration = durationUntilExpirationIn;
        }

        public NoRaidZoneEntry(ResourceKey<Level> dimension, BlockPos positionIn, int radiusIn, long gameTimeStampIn, long durationUntilExpirationIn)
        {
            this.dimension = dimension;
            position = positionIn;
            radius = radiusIn;
            timeOfCreation = gameTimeStampIn;
            durationInTicksUntilExpiration = durationUntilExpirationIn;
        }

        public ServerLevel getDimension()
        {
            return SculkHorde.savedData.level.getServer().getLevel(dimension);
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public int getRadius()
        {
            return radius;
        }

        public long getTimeOfCreation()
        {
            return timeOfCreation;
        }

        public long getDurationInTicksUntilExpiration()
        {
            return durationInTicksUntilExpiration;
        }

        public boolean isExpired(long currentTimeStamp)
        {
            long defaultTicksUntilExpiration = TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_no_raid_zone_duration_minutes.get());
            long ticksUntilThisNoRaidZoneExpires = getDurationInTicksUntilExpiration();
            // If the user has set a lower duration in the config, we will use that instead
            if(ticksUntilThisNoRaidZoneExpires > defaultTicksUntilExpiration)
            {
                durationInTicksUntilExpiration = defaultTicksUntilExpiration;
            }

            return (currentTimeStamp - getTimeOfCreation()) > getDurationInTicksUntilExpiration();
        }

        public boolean isBlockPosInRadius(ServerLevel level, BlockPos blockPosIn)
        {

            return position.closerThan(blockPosIn, radius) && BlockAlgorithms.areTheseDimensionsEqual(level, getDimension());
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putLong("position", position.asLong());
            nbt.putInt("radius", radius);
            nbt.putLong("gameTimeStamp", timeOfCreation);
            nbt.putLong("durationUntilExpiration", durationInTicksUntilExpiration);
            nbt.putString("dimension", dimension.location().toString());
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static NoRaidZoneEntry serialize(CompoundTag nbt)
        {
            ResourceKey<Level> dimensionResourceKey = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("dimension")));
            return new NoRaidZoneEntry(dimensionResourceKey, BlockPos.of(nbt.getLong("position")), nbt.getInt("radius"), nbt.getLong("gameTimeStamp"), nbt.getLong("durationUntilExpiration"));
        }
    }

    // ###### Player Profile Entries ######

    public ArrayList<PlayerProfileEntry> getPlayerProfileEntries() {
        return playerProfileEntries;
    }

    public static class PlayerProfileEntry
    {
        private final UUID playerUUID; // The Location
        private int relationshipToTheHorde;

        private boolean isVessel = false;
        private boolean isActiveVessel = false;

        private static final int MAX_RELATIONSHIP_VALUE = 1000;
        private static final int MIN_RELATIONSHIP_VALUE = -1;

        public PlayerProfileEntry(Player playerIn)
        {
            this.playerUUID = playerIn.getUUID();
        }

        public PlayerProfileEntry(UUID playerIn, int relationshipToTheHordeIn, boolean isVesselIn, boolean isActiveVesselIn)
        {
            this.playerUUID = playerIn;
            this.relationshipToTheHorde = relationshipToTheHordeIn;
            this.isVessel = isVesselIn;
            this.isActiveVessel = isActiveVesselIn;
        }

        public Optional<Player> getPlayer()
        {
            return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(playerUUID));
        }

        public UUID getPlayerUUID()
        {
            return playerUUID;
        }

        public int getRelationshipToTheHorde()
        {
            return relationshipToTheHorde;
        }

        public void setRelationshipToTheHorde(int value)
        {
            if(value < 0)
            {
                relationshipToTheHorde = Math.max(MIN_RELATIONSHIP_VALUE, value);
            }
            else
            {
                relationshipToTheHorde = Math.min(MAX_RELATIONSHIP_VALUE, value);
            }
        }

        public void increaseOrDecreaseRelationshipToHorde(int value)
        {
            setRelationshipToTheHorde(getRelationshipToTheHorde() + value);
        }

        public void setVessel(boolean value)
        {
            isVessel = value;
        }

        public boolean isVessel()
        {
            return isVessel;
        }

        public void setActiveVessel(boolean value)
        {
            isActiveVessel = value;
        }

        public boolean isActiveVessel()
        {
            return isVessel && isActiveVessel;
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundTag deserialize()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("playerUUID", playerUUID);
            nbt.putInt("relationshipToTheHorde", relationshipToTheHorde);
            nbt.putBoolean("isVessel", isVessel);
            nbt.putBoolean("isActiveVessel", isActiveVessel);
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static PlayerProfileEntry serialize(CompoundTag nbt)
        {
            return new PlayerProfileEntry(nbt.getUUID("playerUUID"), nbt.getInt("relationshipToTheHorde"), nbt.getBoolean("isVessel"), nbt.getBoolean("isActiveVessel"));
        }
    }
}
