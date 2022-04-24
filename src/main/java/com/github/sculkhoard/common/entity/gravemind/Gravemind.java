package com.github.sculkhoard.common.entity.gravemind;


import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.ReinforcementContext;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

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
public class Gravemind {

    private final boolean DEBUG_THIS = DEBUG_MODE && false; //Used to debug the gravemind

    public enum evolution_states {Undeveloped, Immature, Mature}
    private evolution_states evolution_state;

    public enum attack_states {Defensive, Offensive}

    public attack_states attack_state = attack_states.Defensive;

    //This controls the reinforcement system.
    public EntityFactory entityFactory;

    //This is a list of mob types that have attacked the sculk hoard
    public static ArrayList<String> confirmedThreats;

    //This is a list of all known positions of sculkNodes.
    //We do not want to put them too close to each other.
    public static ArrayList<BlockPos> sculkNodePositions;
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
        calulateCurrentState();
        if(DEBUG_THIS) System.out.println("Gravemind Initialized");
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

        if(DEBUG_THIS) System.out.println("Gravemind deduced the current state as: " + evolution_state);
    }

    /**
     * Will check each known node location to see if there is one too close.
     * @param potentialPos The potential location of a new node
     * @return t rue if creation of new node is approved, false otherwise.
     */
    public boolean isValidPositionForSculkNode(BlockPos potentialPos)
    {
        if(sculkNodePositions.isEmpty())
            return true;

        for(BlockPos nodePos : sculkNodePositions)
        {
            //Get Distance from our potential location to the current index node position
            int distanceFromPotentialToCurrentNode = (int)
                    Math.sqrt(
                            Math.pow(potentialPos.getX() - nodePos.getX(),2)
                            + Math.pow(potentialPos.getY() - nodePos.getY(),2)
                            + Math.pow(potentialPos.getZ() - nodePos.getZ(),2)
                    );


            //if we find a single node that is too close, disapprove of creating a new one
            if(distanceFromPotentialToCurrentNode < MINIMUM_DISTANCE_BETWEEN_NODES)
                return false;
        }

        return true;
    }

    public boolean processReinforcementRequest(ReinforcementContext context)
    {
        context.isRequestViewed = true;
        if(DEBUG_THIS)
        {
            System.out.println("Processing the Following Request:");
            System.out.println(context.toString());
        }

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

        if(DEBUG_THIS)
        {
            System.out.println("Request Approved? " + context.isRequestApproved);
            System.out.println(context.toString());
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
            return (stateIn == evolution_states.Undeveloped);
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
     * Checks if a node position is present in {@link Gravemind#sculkNodePositions}
     * @param pos The Position
     * @return True if present, false if not
     */
    public boolean isSculkNodePositionRecorded(BlockPos pos)
    {
        for(BlockPos entry : sculkNodePositions)
        {
            if(entry.getX() == pos.getX() && entry.getZ() == pos.getY() && entry.getZ() == pos.getZ())
                return true;
        }

        return false;
    }
}
