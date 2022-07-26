package com.github.sculkhoard.core.gravemind;


import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhoard.core.gravemind.entity_factory.ReinforcementContext;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

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

    /** State Logic **/
    public enum evolution_states {Undeveloped, Immature, Mature}

    private evolution_states evolution_state;

    public enum attack_states {Defensive, Offensive}

    public attack_states attack_state = attack_states.Defensive;

    //private GravemindState;

    /** Controlable Assets **/

    //This controls the reinforcement system.
    public EntityFactory entityFactory;

    public GravemindMemory gravemindMemory;

    //This is a list of mob types that have attacked the sculk hoard
    public static ArrayList<String> confirmedThreats;

    /** Regular Variables **/
    //This is a list of all known positions of sculkNodes.
    //We do not want to put them too close to each other.
    private static int MINIMUM_DISTANCE_BETWEEN_NODES = 300;

    //This is how much mass is needed to go from undeveloped to immature
    private final int MASS_GOAL_FOR_IMMATURE = 500;
    //This is how much mass is needed to go from immature to mature
    private final int MASS_GOAL_FOR_MATURE = 100000000;

    private final int SCULK_NODE_INFECT_RADIUS_UNDEVELOPED = 10;
    //The radius that sculk nodes can infect in the immature state
    private final int SCULK_NODE_INFECT_RADIUS_IMMATURE = 20;
    //The radius that sculk nodes can infect in the mature state
    private final int SCULK_NODE_INFECT_RADIUS_MATURE = 50;

    public int sculk_node_infect_radius = SCULK_NODE_INFECT_RADIUS_UNDEVELOPED;


    /**
     * Default Constructor <br>
     * Called in ForgeEventSubscriber.java in world load event. <br>
     * WARNING: DO NOT CALL THIS FUNCTION UNLESS THE WORLD IS LOADED
     */
    public Gravemind ()
    {
        evolution_state = evolution_states.Undeveloped;
        attack_state = attack_states.Defensive;
        entityFactory = SculkHoard.entityFactory;
        gravemindMemory = new GravemindMemory();
        calulateCurrentState();
    }

    public evolution_states getEvolutionState()
    {
        return evolution_state;
    }

    public attack_states getAttackState()
    {
        return attack_state;
    }

    /**
     * Used to figure out what state the gravemind is in. Called periodically. <br>
     * Useful for when world is loaded in because we dont store the state.
     */
    public void calulateCurrentState()
    {
        if(SculkHoard.entityFactory.getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
        {
            sculk_node_infect_radius = SCULK_NODE_INFECT_RADIUS_IMMATURE;
            evolution_state = evolution_states.Immature;
        }
        else if(SculkHoard.entityFactory.getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
        {
            sculk_node_infect_radius = SCULK_NODE_INFECT_RADIUS_MATURE;
            evolution_state = evolution_states.Mature;
        }
    }

    public boolean processReinforcementRequest(ReinforcementContext context)
    {
        context.isRequestViewed = true;

        //Auto approve is this reinforcement is requested by a developer or sculk mass
        if(context.sender == ReinforcementContext.senderType.Developer || context.sender == ReinforcementContext.senderType.SculkMass)
        {
            context.isRequestApproved = true;
        }
        //If gravemind is undeveloped, just auto approve all requests
        else if(evolution_state == evolution_states.Undeveloped)
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
            if(context.is_non_sculk_mob_nearby)
            {
                context.approvedMobTypes.add(EntityFactory.StrategicValues.Infector);
                context.isRequestApproved = true;
            }
        }
        else if(evolution_state == evolution_states.Mature)
        {
            //TODO: Add functionality for mature state
        }

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
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void placeSculkNode(ServerWorld world, BlockPos targetPos)
    {
        //If we are too close to another node, do not create one
        if(!SculkHoard.gravemind.isValidPositionForSculkNode(targetPos))
            return;

        //Given random chance and the target location can see the sky, create a sculk node
        if(new Random().nextInt(1000) <= 1 && world.canSeeSky(targetPos))
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_BRAIN.get().defaultBlockState());
            SculkHoard.gravemind.gravemindMemory.addNodeToMemory(targetPos);
            EntityType.LIGHTNING_BOLT.spawn(world, null, null, targetPos, SpawnReason.SPAWNER, true, true);
            if(DEBUG_MODE) System.out.println("New Sculk Node Created at " + targetPos.toString());
        }
    }


    /**
     * Will check each known node location in {@link GravemindMemory#nodeEntries}
     * to see if there is one too close.
     * @param potentialPos The potential location of a new node
     * @return true if creation of new node is approved, false otherwise.
     */
    public boolean isValidPositionForSculkNode(BlockPos potentialPos)
    {
        if(gravemindMemory.nodeEntries.isEmpty())
            return true;

        for(NodeEntry entry : gravemindMemory.nodeEntries)
        {
            //Get Distance from our potential location to the current index node position
            int distanceFromPotentialToCurrentNode = (int) BlockAlgorithms.getBlockDistance(potentialPos, entry.position);

            //if we find a single node that is too close, disapprove of creating a new one
            if(distanceFromPotentialToCurrentNode < MINIMUM_DISTANCE_BETWEEN_NODES)
            {
                return false;
            }
        }
        return true;
    }


    /** ######## Classes ######## **/

    public class GravemindMemory
    {

        //We do not want to put them too close to each other.
        public ArrayList<NodeEntry> nodeEntries;

        public ArrayList<BlockPos> beeHivePositions;

        public ArrayList<BlockPos> spawnerPositions;

        /**
         * Default Constructor
         */
        public GravemindMemory()
        {
            nodeEntries = new ArrayList<>();
        }

        /** Accessors **/

        /**
         * Returns a list of known node positions
         * @return
         */
        public ArrayList<NodeEntry> getNodeEntries()
        {
            return nodeEntries;
        }

        /**
         * Will check the positons of all entries to see
         * if they match the parameter.
         * @param pos The position to cross reference
         * @return true if in memory, false otherwise
         */
        public boolean isNodePositionInMemory(BlockPos position)
        {
            for(NodeEntry entry : nodeEntries)
            {
                if(entry.position == position)
                {
                    return true;
                }
            }
            return false;
        }

        /** ######## Modifiers ######## **/

        /**
         * Adds a position to the list if it does not already exist
         * @param position
         */
        public void addNodeToMemory(BlockPos position)
        {
            if(!isNodePositionInMemory(position) && nodeEntries != null)
            {
                nodeEntries.add(new NodeEntry(position));
            }
        }

        /** ######## Events ######### **/



        /**
         * Will verify all enties to see if they exist in the world.
         * If not, they will be removed.
         * @param worldIn The World
         */
        public void validateNodeEntries(ServerWorld worldIn)
        {
            for(int index = 0; index < nodeEntries.size(); index++)
            {
                //TODO: Figure out if not being in the overworld can mess this up
                if(!nodeEntries.get(index).isEntryValid(worldIn))
                {
                    nodeEntries.remove(index);
                    index--;
                }
            }
        }
    }

    private class NodeEntry
    {
        public BlockPos position;
        public long lastTimeWasActive;

        public NodeEntry(BlockPos positionIn)
        {
            position = positionIn;
            lastTimeWasActive = System.nanoTime();
        }

        public boolean isEntryValid(ServerWorld worldIn)
        {
            if(worldIn.getBlockState(position).getBlock().is(BlockRegistry.SCULK_BRAIN.get()))
            {
                return true;
            }
            return false;
        }

    }
}
