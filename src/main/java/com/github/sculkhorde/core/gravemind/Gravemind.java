package com.github.sculkhorde.core.gravemind;


import com.github.sculkhorde.common.block.SculkBeeNestBlock;
import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.entity.SculkLivingEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.github.sculkhorde.core.SculkHorde.DEBUG_MODE;
import static com.github.sculkhorde.util.BlockAlgorithms.getBlockDistance;

/**
 * This class represents the logistics for the Gravemind and is SEPARATE from the physical version.
 * The gravemind is a state machine that is used to coordinate the sculk hoard.
 * Right now only controls the reinforcement system.
 *
 * Future Plans:
 * -Controls Sculk Raids
 * -Coordinate Defense
 * -Make Coordination of Reinforcements more advanced
 */
public class Gravemind
{
    public enum evolution_states {Undeveloped, Immature, Mature}

    private evolution_states evolution_state;

    //This controls the reinforcement system.
    public static EntityFactory entityFactory;

    public GravemindMemory gravemindMemory;



    //This is a list of all known positions of sculkNodes.
    //We do not want to put them too close to each other.
    private static final int MINIMUM_DISTANCE_BETWEEN_NODES = 300;

    private final int SCULK_NODE_INFECT_RADIUS_UNDEVELOPED = 10;


    //Determines the range which a sculk node can infect land around it
    public int sculk_node_infect_radius = SCULK_NODE_INFECT_RADIUS_UNDEVELOPED;
    public int sculk_node_limit = 1;

    /**
     * Default Constructor <br>
     * Called in ForgeEventSubscriber.java in world load event. <br>
     * WARNING: DO NOT CALL THIS FUNCTION UNLESS THE WORLD IS LOADED
     */
    public Gravemind()
    {
        evolution_state = evolution_states.Undeveloped;
        entityFactory = SculkHorde.entityFactory;

        gravemindMemory = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage().computeIfAbsent(GravemindMemory::new, SculkHorde.SAVE_DATA_ID);
        calulateCurrentState();
    }

    public evolution_states getEvolutionState()
    {
        return evolution_state;
    }



    /** Accessors **/

    /**
     * Get the memory object that stores all the data
     * @return The GravemindMemory
     */
    @Nullable
    public GravemindMemory getGravemindMemory()
    {
        if(gravemindMemory == null)
        {
            if(ServerLifecycleHooks.getCurrentServer() == null)
                return null;

            DimensionSavedDataManager savedData = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage();
            gravemindMemory = savedData.computeIfAbsent(GravemindMemory::new, SculkHorde.SAVE_DATA_ID);
        }
        return gravemindMemory;
    }

    /**
     * Used to figure out what state the gravemind is in. Called periodically. <br>
     * Useful for when world is loaded in because we dont store the state.
     */
    public void calulateCurrentState()
    {
        //This is how much mass is needed to go from undeveloped to immature
        int MASS_GOAL_FOR_IMMATURE = 500;
        //This is how much mass is needed to go from immature to mature
        int MASS_GOAL_FOR_MATURE = 100000000;
        if(getGravemindMemory().getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
        {
            //The radius that sculk nodes can infect in the immature state
            sculk_node_infect_radius = 20;
            evolution_state = evolution_states.Immature;
        }
        else if(getGravemindMemory().getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
        {
            //The radius that sculk nodes can infect in the mature state
            sculk_node_infect_radius = 50;
            evolution_state = evolution_states.Mature;
            sculk_node_limit = 2;
        }
    }

    public void enableAmountOfBeeHives(ServerWorld worldIn, int amount)
    {
        if(getGravemindMemory().getBeeNestEntries().size() <= 0) { return; }

        int lastEnabledIndex = -1;
        for (int i = 0; i < getGravemindMemory().getBeeNestEntries().size(); i++)
        {
            BeeNestEntry entry = getGravemindMemory().getBeeNestEntries().get(i);

            if(!entry.isEntryValid(worldIn)) { continue; }

            if (!entry.isOccupantsExistingDisabled(worldIn))
            {
                entry.disableOccupantsExiting(worldIn);
                lastEnabledIndex = i;
            }
        }
        int startIndex = lastEnabledIndex + 1;
        if (startIndex >= getGravemindMemory().getBeeNestEntries().size())
        {
            startIndex = 0;
        }
        for (int i = startIndex; i < startIndex + amount; i++)
        {
            int index = i % getGravemindMemory().getBeeNestEntries().size();

            if(!getGravemindMemory().getBeeNestEntries().get(index).isEntryValid(worldIn)) { continue; }

            getGravemindMemory().getBeeNestEntries().get(index).enableOccupantsExiting(worldIn);
        }
    }

    public boolean processReinforcementRequest(ReinforcementRequest context)
    {
        context.isRequestViewed = true;

        //Auto approve is this reinforcement is requested by a developer or sculk mass
        if(context.sender == ReinforcementRequest.senderType.Developer || context.sender == ReinforcementRequest.senderType.SculkMass)
        {
            context.isRequestApproved = true;
        }

        if(SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0)
        {
            return false;
        }


        //If gravemind is undeveloped, just auto approve all requests
        if(evolution_state == evolution_states.Undeveloped)
        {
            context.isRequestApproved = true;
        }
        else if(evolution_state == evolution_states.Immature)
        {
            //Spawn Combat Mobs to deal with player
            if(context.is_aggressor_nearby)
            {
                context.approvedMobTypes.add(EntityFactory.StrategicValues.Melee);
                context.approvedMobTypes.add(EntityFactory.StrategicValues.Ranged);
                context.isRequestApproved = true;
            }
            //Spawn infector mobs to infect
            //NOTE: I turned this into an else if because if both aggressors and passives are present,
            //it will choose from both combat and infector units. I think its better we prioritize
            //spawning aggressors if both are present
            else if(context.is_non_sculk_mob_nearby)
            {
                context.approvedMobTypes.add(EntityFactory.StrategicValues.Infector);
                context.isRequestApproved = true;
            }
        }
        //TODO: Add functionality for mature state


        return context.isRequestApproved;
    }

    /**
     * Determines if a given evolution state is equal to or below the current evolution state.
     * @param stateIn The given state to check
     * @return True if the given state is equal to or less than current evolution state.
     */
    public boolean isEvolutionStateEqualOrLessThanCurrent(evolution_states stateIn)
    {
        if(evolution_state == evolution_states.Undeveloped)
        {
            return (stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Immature)
        {
            return (stateIn == evolution_states.Immature || stateIn == evolution_states.Undeveloped);
        }
        else if(evolution_state == evolution_states.Mature)
        {
            return(stateIn == evolution_states.Undeveloped
                || stateIn == evolution_states.Immature
                || stateIn == evolution_states.Mature);
        }
        return false;
    }


    /**
     * Will only place sculk nodes if sky is visible
     * @param worldIn The World to place it in
     * @param targetPos The position to place it in
     */
    public void placeSculkNode(ServerWorld worldIn, BlockPos targetPos, boolean enableChance)
    {
        //Random Chance to Place TreeNode
        if(new Random().nextInt(10000) > 1 && enableChance) { return; }

        //If we are too close to another node, do not create one
        if(!SculkHorde.gravemind.isValidPositionForSculkNode(worldIn, targetPos)) { return; }

        SculkNodeBlock.FindAreaAndPlaceNode(worldIn, targetPos);

    }


    /**
     * Will check each known node location in {@link GravemindMemory#nodeEntries}
     * to see if there is one too close.
     * @param positionIn The potential location of a new node
     * @return true if creation of new node is approved, false otherwise.
     */
    public boolean isValidPositionForSculkNode(ServerWorld worldIn, BlockPos positionIn)
    {
        if(worldIn.canSeeSky(positionIn))
        {
            return false;
        }

        if(SculkHorde.gravemind.getGravemindMemory().getNodeEntries().size() >= SculkHorde.gravemind.sculk_node_limit)
        {
            return false;
        }

        for (NodeEntry entry : getGravemindMemory().getNodeEntries())
        {
            //Get Distance from our potential location to the current index node position
            int distanceFromPotentialToCurrentNode = (int) getBlockDistance(positionIn, entry.position);

            //if we find a single node that is too close, disapprove of creating a new one
            if (distanceFromPotentialToCurrentNode < MINIMUM_DISTANCE_BETWEEN_NODES)
            {
                return false;
            }
        }
        return true;
    }


    /** ######## Classes ######## **/

    /**
     * This class handels all data that gets saved to and loaded from the world. <br>
     * Learned World Data mechanics from: https://www.youtube.com/watch?v=tyTsdCzVz6w
     */
    public class GravemindMemory extends WorldSavedData
    {
        //The world
        public ServerWorld world;

        //Map<The Name of Mob, IsHostile?>
        public Map<String, HostileEntry> hostileEntries;

        //List of all known positions of nodes.
        private ArrayList<NodeEntry> nodeEntries;

        //List of all known positions of bee nests
        private ArrayList<BeeNestEntry> beeNestEntries;

        // the amount of mass that the sculk hoard has accumulated.
        private int sculkAccumulatedMass = 0;

        // used to write/read nbt data to/from the world.
        private final String sculkAccumulatedMassIdentifier = "sculkAccumulatedMass";

        /**
         * Default Constructor
         */
        public GravemindMemory()
        {
            super(SculkHorde.SAVE_DATA_ID);
            nodeEntries = new ArrayList<>();
            beeNestEntries = new ArrayList<>();
            hostileEntries = new HashMap<>();
        }

        /** Accessors **/

        /**
         * Gets how much Sculk mass the Sculk horde has.
         * @return An integer representing all Sculk mass accumulated.
         */
        public int getSculkAccumulatedMass()
        {
            setDirty();
            return sculkAccumulatedMass;
        }

        /**
         * Returns a list of known node positions
         * @return An ArrayList of all node entries positions
         */
        public ArrayList<NodeEntry> getNodeEntries() { return nodeEntries; }


        /**
         * Returns a list of known node positions
         * @return The Closest TreeNode
         */
        public NodeEntry getClosestNodeEntry(BlockPos pos) {
            NodeEntry closestNode = null;
            double closestDistance = Double.MAX_VALUE;
            for (NodeEntry node : getNodeEntries())
            {
                if (pos.distSqr(node.position) < closestDistance)
                {
                    closestNode = node;
                    closestDistance = pos.distSqr(node.position);
                }
            }
            return closestNode;
        }


        public boolean isInRangeOfNode(BlockPos pos, int distance)
        {
            if(getNodeEntries().isEmpty()) {return false;}

            return getBlockDistance(getClosestNodeEntry(pos).position, pos) <= distance;

        }

        /**
         * Returns a list of known bee nest positions
         * @return An ArrayList of all know bee nest positions
         */
        public ArrayList<BeeNestEntry> getBeeNestEntries() { return beeNestEntries; }

        /**
         * Returns the map of known hostiles
         * @return An HashMap with all known hostile entities
         */
        public Map<String, HostileEntry> getHostileEntries() { return hostileEntries; }

        /**
         * Will check the positons of all entries to see
         * if they match the parameter.
         * @param position The position to cross reference
         * @return true if in memory, false otherwise
         */
        public boolean isBeeNestPositionInMemory(BlockPos position)
        {
            for(BeeNestEntry entry : getBeeNestEntries())
            {
                if(entry.position == position)
                {
                    return true;
                }
            }
            return false;
        }

        /**
         * Will check the positons of all entries to see
         * if they match the parameter.
         * @param position The position to cross reference
         * @return true if in memory, false otherwise
         */
        public boolean isNodePositionInMemory(BlockPos position)
        {
            for(NodeEntry entry : getNodeEntries())
            {
                if(entry.position.equals(position))
                {
                    return true;
                }
            }
            return false;
        }

        /** ######## Modifiers ######## **/


        /**
         * Adds to the sculk accumulated mass
         * @param amount The amount you want to add
         */
        public void addSculkAccumulatedMass(int amount)
        {
            setDirty();
            sculkAccumulatedMass += amount;
        }


        /**
         * Subtracts from the Sculk Accumulate Mass
         * @param amount The amount to substract
         */
        public void subtractSculkAccumulatedMass(int amount)
        {
            setDirty();
            sculkAccumulatedMass -= amount;
        }

        /**
         * Sets the value of sculk accumulate mass.
         * @param amount The amount to set it to.
         */
        public void setSculkAccumulatedMass(int amount)
        {
            setDirty();
            sculkAccumulatedMass = amount;
        }

        /**
         * Adds a position to the list if it does not already exist
         * @param positionIn
         */
        public void addNodeToMemory(BlockPos positionIn)
        {
            if(!isNodePositionInMemory(positionIn) && getNodeEntries() != null)
            {
                getNodeEntries().add(new NodeEntry(positionIn));
                setDirty();
            }
            else if(DEBUG_MODE) System.out.println("Attempted to Add TreeNode To Memory but failed.");
        }

        /**
         * Adds a position to the list if it does not already exist
         * @param positionIn
         */
        public void addBeeNestToMemory(BlockPos positionIn)
        {
            if(!isBeeNestPositionInMemory(positionIn) && getBeeNestEntries() != null)
            {
                getBeeNestEntries().add(new BeeNestEntry(positionIn));
                setDirty();
            }
            // TODO For some reason this continously gets called, find out why
            //else if(DEBUG_MODE) System.out.println("Attempted to Add Nest To Memory but failed.");
        }

        /**
         * Translate entities to string to make an identifier. <br>
         * This identifier is then stored in memory in a map.
         * @param entityIn The Entity
         * @param worldIn The World
         */
        public void addHostileToMemory(LivingEntity entityIn, ServerWorld worldIn)
        {
            if(entityIn == null || EntityAlgorithms.isSculkLivingEntity.test(entityIn) || entityIn instanceof CreeperEntity)
            {
                //if(DEBUG_MODE) System.out.println("Attempted to Add Hostile To Memory but failed.");
                return;
            }

            String identifier = entityIn.getType().toString();
            if(!identifier.isEmpty())
            {
                GravemindMemory memory = worldIn.getDataStorage().computeIfAbsent(GravemindMemory::new, SculkHorde.SAVE_DATA_ID);
                memory.getHostileEntries().putIfAbsent(identifier, new HostileEntry(identifier));
                memory.setDirty();
            }
        }


        /** ######## Events ######### **/

        /**
         * Will verify all enties to see if they exist in the world.
         * If not, they will be removed. <br>
         * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
         * @param worldIn The World
         */
        public void validateNodeEntries(ServerWorld worldIn)
        {
            long startTime = System.nanoTime();
            for(int index = 0; index < nodeEntries.size(); index++)
            {
                //TODO: Figure out if not being in the overworld can mess this up
                if(!getNodeEntries().get(index).isEntryValid(worldIn))
                {
                    getGravemindMemory().getNodeEntries().remove(index);
                    index--;
                    worldIn.getDataStorage().computeIfAbsent(GravemindMemory::new, SculkHorde.SAVE_DATA_ID).setDirty();
                }
            }
            long endTime = System.nanoTime();
            if(DEBUG_MODE) System.out.println("TreeNode Validation Took " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " milliseconds");
        }


        /**
         * Will verify all enties to see if they exist in the world.
         * Will also reasses the parentNode for each one.
         * If not, they will be removed. <br>
         * Gets called in {@link com.github.sculkhorde.util.ForgeEventSubscriber#WorldTickEvent}
         * @param worldIn The World
         */
        public void validateBeeNestEntries(ServerWorld worldIn)
        {
            long startTime = System.nanoTime();
            for(int index = 0; index < getBeeNestEntries().size(); index++)
            {
                getBeeNestEntries().get(index).setParentNodeToClosest();
                //TODO: Figure out if not being in the overworld can mess this up
                if(!getBeeNestEntries().get(index).isEntryValid(worldIn))
                {
                    getBeeNestEntries().remove(index);
                    index--;
                    worldIn.getDataStorage().computeIfAbsent(GravemindMemory::new, SculkHorde.SAVE_DATA_ID).setDirty();
                }
            }
            long endTime = System.nanoTime();
            if(DEBUG_MODE) System.out.println("Bee Nest Validation Took " + TimeUnit.MILLISECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " milliseconds");
        }

        /**
         * This method gets called every time the world loads data from memory.
         * We extract data from the memory and store it in variables.
         * @param nbt The memory where data is stored
         */
        @Override
        public void load(CompoundNBT nbt)
        {

            CompoundNBT gravemindData = nbt.getCompound("gravemindData");

            getNodeEntries().clear();
            getBeeNestEntries().clear();
            getHostileEntries().clear();

            this.sculkAccumulatedMass = nbt.getInt(sculkAccumulatedMassIdentifier);

            for (int i = 0; gravemindData.contains("node_entry" + i); i++) {
                getNodeEntries().add(NodeEntry.serialize(gravemindData.getCompound("node_entry" + i)));
            }

            for (int i = 0; gravemindData.contains("bee_nest_entry" + i); i++) {
                getBeeNestEntries().add(BeeNestEntry.serialize(gravemindData.getCompound("bee_nest_entry" + i)));
            }

            for (int i = 0; gravemindData.contains("hostile_entry" + i); i++) {
                //getHostileEntries().add(BeeNestEntry.serialize(gravemindData.getCompound("hostile_entry" + i)));
                HostileEntry hostileEntry = HostileEntry.serialize(gravemindData.getCompound("hostile_entry" + i));
                getHostileEntries().putIfAbsent(hostileEntry.identifier, hostileEntry);
            }

            System.out.print("");

        }

        /**
         * This method gets called every time the world saves data from memory.
         * We take the data in our variables and store it to memory.
         * @param nbt The memory where data is stored
         */
        @Override
        public CompoundNBT save(CompoundNBT nbt)
        {
            CompoundNBT gravemindData = new CompoundNBT();

            nbt.putInt(sculkAccumulatedMassIdentifier, sculkAccumulatedMass);

            for(ListIterator<NodeEntry> iterator = getNodeEntries().listIterator(); iterator.hasNext();)
            {
                gravemindData.put("node_entry" + iterator.nextIndex(),iterator.next().deserialize());
            }

            for(ListIterator<BeeNestEntry> iterator = getBeeNestEntries().listIterator(); iterator.hasNext();)
            {
                gravemindData.put("bee_nest_entry" + iterator.nextIndex(),iterator.next().deserialize());
            }

            int hostileIndex = 0;
            for(Map.Entry<String, HostileEntry> entry : getHostileEntries().entrySet())
            {
                gravemindData.put("hostile_entry" + hostileIndex, entry.getValue().deserialize());
                hostileIndex++;
            }

            nbt.put("gravemindData", gravemindData);
            return nbt;
        }


    }

    /** ######## CLASSES ######### **/

    /**
     * This class is a representation of the actual
     * Sculk Nodes in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    private static class NodeEntry
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

        /**
         * Checks the world to see if the node is still there.
         * @param worldIn The world to check
         * @return True if in the world at location, false otherwise
         */
        public boolean isEntryValid(ServerWorld worldIn)
        {
            return worldIn.getBlockState(position).getBlock().is(BlockRegistry.SCULK_NODE_BLOCK.get());
        }

        /**
         * Making nbt to be stored in memory
         * @return The nbt with our data
         */
        public CompoundNBT deserialize()
        {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putLong("position", position.asLong());
            nbt.putLong("lastTimeWasActive", lastTimeWasActive);
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static NodeEntry serialize(CompoundNBT nbt)
        {
            return new NodeEntry(BlockPos.of(nbt.getLong("position")));
        }

    }

    /**
     * This class is a representation of the actual
     * Bee Nests in the world that the horde has access
     * to. It allows the gravemind to keep track of all.
     */
    private static class BeeNestEntry
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
        public boolean isEntryValid(ServerWorld worldIn)
        {
            return worldIn.getBlockState(position).getBlock().is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get());
        }


        /**
         * is Hive enabled?
         * @return
         */
        public boolean isOccupantsExistingDisabled(ServerWorld worldIn)
        {
            return SculkBeeNestBlock.isNestClosed(worldIn.getBlockState(position));
        }

        /**
         * Sets Hive to deny bees leaving
         */
        public void disableOccupantsExiting(ServerWorld world)
        {
            SculkBeeNestBlock.setNestClosed(world, world.getBlockState(position), position);
        }


        /**
         * Sets Hive to allow bees leaving
         */
        public void enableOccupantsExiting(ServerWorld world)
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
            if(SculkHorde.gravemind.getGravemindMemory().getNodeEntries() != null && !SculkHorde.gravemind.getGravemindMemory().getNodeEntries().isEmpty())
            {
                NodeEntry closestEntry = SculkHorde.gravemind.getGravemindMemory().getNodeEntries().get(0);
                for(NodeEntry entry : SculkHorde.gravemind.getGravemindMemory().getNodeEntries())
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
        public CompoundNBT deserialize()
        {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putLong("position", position.asLong());
            if(parentNodePosition != null) nbt.putLong("parentNodePosition", parentNodePosition.asLong());
            return nbt;
        }


        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static BeeNestEntry serialize(CompoundNBT nbt)
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
        public CompoundNBT deserialize()
        {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putString("identifier", identifier);
            return nbt;
        }

        /**
         * Extracting our data from the nbt.
         * @return The nbt with our data
         */
        public static HostileEntry serialize(CompoundNBT nbt)
        {
            return new HostileEntry(nbt.getString("identifier"));
        }

    }

}
