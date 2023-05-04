package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.block.SculkBeeNestBlock;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;
import static com.github.sculkhorde.util.BlockAlgorithms.getBlockDistance;

/**
 * This class handels all data that gets saved to and loaded from the world. <br>
 * Learned World Data mechanics from: https://www.youtube.com/watch?v=tyTsdCzVz6w
 */
public class ModSavedData extends SavedData {
    private static final Map<String, HostileEntry> hostileEntries = new HashMap<>();
    //The world
    private final ServerLevel level;

    //List of all known positions of nodes.
    private final ArrayList<NodeEntry> nodeEntries = new ArrayList<>();

    //List of all known positions of bee nests
    private final ArrayList<BeeNestEntry> beeNestEntries = new ArrayList<>();

    // the amount of mass that the sculk hoard has accumulated.
    private int sculkAccumulatedMass = 0;
    // used to write/read nbt data to/from the world.
    private static final String sculkAccumulatedMassIdentifier = "sculkAccumulatedMass";


    private int ticksSinceSculkNodeDestruction = Gravemind.TICKS_BETWEEN_NODE_SPAWNS;
    private static final String ticksSinceSculkNodeDestructionIdentifier = "ticksSinceSculkNodeDestruction";

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
     * Accessors
     **/

    public boolean isSculkNodeCooldownOver() {
        return ticksSinceSculkNodeDestruction >= Gravemind.TICKS_BETWEEN_NODE_SPAWNS;
    }

    public int getTicksSinceSculkNodeDestruction() {
        setDirty();
        return ticksSinceSculkNodeDestruction;
    }

    public void setTicksSinceSculkNodeDestruction(int ticksSinceSculkNodeDestruction) {
        this.ticksSinceSculkNodeDestruction = ticksSinceSculkNodeDestruction;
        setDirty();
    }

    public void incrementTicksSinceSculkNodeDestruction() {
        this.ticksSinceSculkNodeDestruction++;
        setDirty();
    }

    public void resetTicksSinceSculkNodeDestruction() {
        //Send message to all players that node has spawned
        level.players().forEach(player -> player.displayClientMessage(Component.literal("A Sculk Node has been Destroyed!"), true));
        // Play sound for each player
        level.players().forEach(player -> level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.HOSTILE, 1.0F, 1.0F));


        this.ticksSinceSculkNodeDestruction = 0;
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
     * Returns a list of known node positions
     *
     * @return An ArrayList of all node entries positions
     */
    public ArrayList<NodeEntry> getNodeEntries() {
        return nodeEntries;
    }


    /**
     * Returns a list of known node positions
     *
     * @return The Closest TreeNode
     */
    public NodeEntry getClosestNodeEntry(BlockPos pos) {
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


    public boolean isInRangeOfNode(BlockPos pos, int distance) {
        if (getNodeEntries().isEmpty()) {
            return false;
        }

        return getBlockDistance(getClosestNodeEntry(pos).position, pos) <= distance;

    }

    /**
     * Returns a list of known bee nest positions
     *
     * @return An ArrayList of all know bee nest positions
     */
    public ArrayList<BeeNestEntry> getBeeNestEntries() {
        return beeNestEntries;
    }

    /**
     * Returns the map of known hostiles
     *
     * @return An HashMap with all known hostile entities
     */
    public Map<String, HostileEntry> getHostileEntries() {
        return hostileEntries;
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
     * @param position The position to cross reference
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

    // ######## Modifiers ########


    /**
     * Adds to the sculk accumulated mass
     *
     * @param amount The amount you want to add
     */
    public void addSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass += amount;
    }


    /**
     * Subtracts from the Sculk Accumulate Mass
     *
     * @param amount The amount to substract
     */
    public void subtractSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass -= amount;
    }

    /**
     * Sets the value of sculk accumulate mass.
     *
     * @param amount The amount to set it to.
     */
    public void setSculkAccumulatedMass(int amount) {
        setDirty();
        sculkAccumulatedMass = amount;
    }

    /**
     * Adds a position to the list if it does not already exist
     *
     * @param positionIn The Posoition to add
     */
    public void addNodeToMemory(BlockPos positionIn) {
        if (!isNodePositionInMemory(positionIn) && getNodeEntries() != null) {
            getNodeEntries().add(new NodeEntry(positionIn));
            setDirty();
        } else if (DEBUG_MODE) System.out.println("Attempted to Add TreeNode To Memory but failed.");
    }

    /**
     * Adds a position to the list if it does not already exist
     *
     * @param positionIn The Position to add
     */
    public void addBeeNestToMemory(BlockPos positionIn) {
        if (!isBeeNestPositionInMemory(positionIn) && getBeeNestEntries() != null) {
            getBeeNestEntries().add(new BeeNestEntry(positionIn));
            setDirty();
        }
        // TODO For some reason this continously gets called, find out why
        //else if(DEBUG_MODE) System.out.println("Attempted to Add Nest To Memory but failed.");
    }

    /**
     * Translate entities to string to make an identifier. <br>
     * This identifier is then stored in memory in a map.
     *
     * @param entityIn The Entity
     */
    public void addHostileToMemory(LivingEntity entityIn) {
        if (entityIn == null || EntityAlgorithms.isSculkLivingEntity.test(entityIn) || entityIn instanceof Creeper) {
            //if(DEBUG_MODE) System.out.println("Attempted to Add Hostile To Memory but failed.");
            return;
        }

        String identifier = entityIn.getType().toString();
        if (!identifier.isEmpty()) {
            getHostileEntries().putIfAbsent(identifier, new HostileEntry(identifier));
            setDirty();
        }
    }


    // ######## Events #########

    /**
     * Will verify all enties to see if they exist in the world.
     * If not, they will be removed. <br>
     * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
     *
     * @param worldIn The World
     */
    public void validateNodeEntries(ServerLevel worldIn) {
        long startTime = System.nanoTime();
        for (int index = 0; index < nodeEntries.size(); index++) {
            //TODO: Figure out if not being in the overworld can mess this up
            if (!getNodeEntries().get(index).isEntryValid(worldIn)) {
                resetTicksSinceSculkNodeDestruction();
                getNodeEntries().remove(index);
                index--;
                setDirty();
            }
        }
        long endTime = System.nanoTime();
        if (DEBUG_MODE)
            System.out.println("TreeNode Validation Took " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " milliseconds");
    }


    /**
     * Will verify all enties to see if they exist in the world.
     * Will also reasses the parentNode for each one.
     * If not, they will be removed. <br>
     * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
     *
     * @param worldIn The World
     */
    public void validateBeeNestEntries(ServerLevel worldIn) {
        long startTime = System.nanoTime();
        for (int index = 0; index < getBeeNestEntries().size(); index++) {
            getBeeNestEntries().get(index).setParentNodeToClosest();
            //TODO: Figure out if not being in the overworld can mess this up
            if (!getBeeNestEntries().get(index).isEntryValid(worldIn)) {
                getBeeNestEntries().remove(index);
                index--;
                setDirty();
            }
        }
        long endTime = System.nanoTime();
        if (DEBUG_MODE)
            System.out.println("Bee Nest Validation Took " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " milliseconds");
    }

    /**
     * This method gets called every time the world loads data from memory.
     * We extract data from the memory and store it in variables.
     *
     * @param nbt The memory where data is stored
     */
    public static ModSavedData load(CompoundTag nbt) {

        CompoundTag gravemindData = nbt.getCompound("gravemindData");

        SculkHorde.savedData.getNodeEntries().clear();
        SculkHorde.savedData.getBeeNestEntries().clear();
        SculkHorde.savedData.getHostileEntries().clear();

        SculkHorde.savedData.setSculkAccumulatedMass(nbt.getInt(sculkAccumulatedMassIdentifier));
        SculkHorde.savedData.setTicksSinceSculkNodeDestruction(nbt.getInt(ticksSinceSculkNodeDestructionIdentifier));

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

        System.out.print("");

        return getGravemindMemory();

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

        nbt.putInt(sculkAccumulatedMassIdentifier, sculkAccumulatedMass);
        nbt.putInt(ticksSinceSculkNodeDestructionIdentifier, ticksSinceSculkNodeDestruction);

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

        nbt.put("gravemindData", gravemindData);
        return nbt;
    }


    /**
     * This class is a representation of the actual
     * Sculk Nodes in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    static class NodeEntry
    {
        private final BlockPos position; //The Location in the world where the node is
        private long lastTimeWasActive; //The Last Time A node was active and working

        /**
         * Default Constructor
         * @param positionIn The physical location
         */
        public NodeEntry(BlockPos positionIn)
        {
            position = positionIn;
            lastTimeWasActive = System.nanoTime();
        }

        public BlockPos getPosition()
        {
            return position;
        }

        public long getLastTimeWasActive()
        {
            return lastTimeWasActive;
        }

        public void setLastTimeWasActive(long lastTimeWasActiveIn)
        {
            lastTimeWasActive = lastTimeWasActiveIn;
        }



        /**
         * Checks the world to see if the node is still there.
         * @param worldIn The world to check
         * @return True if in the world at location, false otherwise
         */
        public boolean isEntryValid(ServerLevel worldIn)
        {
            return worldIn.getBlockState(position).getBlock().equals(BlockRegistry.SCULK_NODE_BLOCK.get());
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
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static NodeEntry serialize(CompoundTag nbt)
        {
            return new NodeEntry(BlockPos.of(nbt.getLong("position")));
        }

    }

    /**
     * This class is a representation of the actual
     * Bee Nests in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    static class BeeNestEntry
    {
        private final BlockPos position; //The location in the world where the node is
        private BlockPos parentNodePosition; //The location of the Sculk TreeNode that this Nest belongs to

        /**
         * Default Constructor
         * @param positionIn The Position of this Nest
         */
        public BeeNestEntry(BlockPos positionIn)
        {
            position = positionIn;
        }

        public BeeNestEntry(BlockPos positionIn, BlockPos parentPositionIn)
        {
            position = positionIn;
            parentNodePosition = parentPositionIn;
        }

        /**
         * Checks if the block does still exist in the world.
         * @param worldIn The world to check
         * @return True if valid, false otherwise.
         */
        public boolean isEntryValid(ServerLevel worldIn)
        {
            return worldIn.getBlockState(position).getBlock().equals(BlockRegistry.SCULK_BEE_NEST_BLOCK.get());
        }


        /**
         * is Hive enabled?
         * @return True if enabled, false otherwise
         */
        public boolean isOccupantsExistingDisabled(ServerLevel worldIn)
        {
            return SculkBeeNestBlock.isNestClosed(worldIn.getBlockState(position));
        }

        /**
         * Sets Hive to deny bees leaving
         */
        public void disableOccupantsExiting(ServerLevel world)
        {
            SculkBeeNestBlock.setNestClosed(world, world.getBlockState(position), position);
        }


        /**
         * Sets Hive to allow bees leaving
         */
        public void enableOccupantsExiting(ServerLevel world)
        {
            SculkBeeNestBlock.setNestOpen(world, world.getBlockState(position), position);
        }

        /**
         * Checks list of node entries and finds the closest one.
         * It then sets the parentNodePosition to be the position of
         * the closest node.
         */
        public void setParentNodeToClosest()
        {
            //Make sure nodeEntries isnt null and nodeEntries isnt empty
            if(getGravemindMemory().getNodeEntries() != null && !getGravemindMemory().getNodeEntries().isEmpty())
            {
                NodeEntry closestEntry = getGravemindMemory().getNodeEntries().get(0);
                for(NodeEntry entry : getGravemindMemory().getNodeEntries())
                {
                    //If entry is closer than our current closest entry
                    if(getBlockDistance(position, entry.position) < getBlockDistance(position, closestEntry.position))
                    {
                        closestEntry = entry;
                    }
                }
                parentNodePosition = closestEntry.position;
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
            if(parentNodePosition != null) nbt.putLong("parentNodePosition", parentNodePosition.asLong());
            return nbt;
        }


        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static BeeNestEntry serialize(CompoundTag nbt)
        {
            return new BeeNestEntry(BlockPos.of(nbt.getLong("position")), BlockPos.of(nbt.getLong("parentNodePosition")));
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
}
