package com.github.sculkhorde.core.gravemind;


import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.*;

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
    //This is a list of all known positions of sculkNodes.
    //We do not want to put them too close to each other.
    private static final int MINIMUM_DISTANCE_BETWEEN_NODES = 300;
    private final int SCULK_NODE_INFECT_RADIUS_UNDEVELOPED = 10;

    //Determines the range which a sculk node can infect land around it
    public int sculk_node_infect_radius = SCULK_NODE_INFECT_RADIUS_UNDEVELOPED;
    public int sculk_node_limit = 1;

    public static int TICKS_BETWEEN_NODE_SPAWNS = TickUnits.convertHoursToTicks(1);

    /**
     * Default Constructor <br>
     * Called in ForgeEventSubscriber.java in world load event. <br>
     * WARNING: DO NOT CALL THIS FUNCTION UNLESS THE WORLD IS LOADED
     */
    public Gravemind()
    {
        evolution_state = evolution_states.Undeveloped;
        entityFactory = SculkHorde.entityFactory;
        calulateCurrentState();
    }

    public evolution_states getEvolutionState()
    {
        return evolution_state;
    }



    // Accessors

    /**
     * Used to figure out what state the gravemind is in. Called periodically. <br>
     * Useful for when world is loaded in because we dont store the state.
     */
    public void calulateCurrentState()
    {

        //This is how much mass is needed to go from undeveloped to immature
        int MASS_GOAL_FOR_IMMATURE = 5000;
        //This is how much mass is needed to go from immature to mature
        int MASS_GOAL_FOR_MATURE = 100000000;
        if(SculkHorde.savedData.getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
        {
            //The radius that sculk nodes can infect in the immature state
            sculk_node_infect_radius = 20;
            evolution_state = evolution_states.Immature;
        }
        else if(SculkHorde.savedData.getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
        {
            //The radius that sculk nodes can infect in the mature state
            sculk_node_infect_radius = 50;
            evolution_state = evolution_states.Mature;
            sculk_node_limit = 2;
        }
    }

    public void enableAmountOfBeeHives(ServerLevel worldIn, int amount)
    {
        if(SculkHorde.savedData.getBeeNestEntries().size() <= 0) { return; }

        int lastEnabledIndex = -1;
        for (int i = 0; i < SculkHorde.savedData.getBeeNestEntries().size(); i++)
        {
            ModSavedData.BeeNestEntry entry = SculkHorde.savedData.getBeeNestEntries().get(i);

            if(!entry.isEntryValid(worldIn)) { continue; }

            if (!entry.isOccupantsExistingDisabled(worldIn))
            {
                entry.disableOccupantsExiting(worldIn);
                lastEnabledIndex = i;
            }
        }
        int startIndex = lastEnabledIndex + 1;
        if (startIndex >= SculkHorde.savedData.getBeeNestEntries().size())
        {
            startIndex = 0;
        }
        for (int i = startIndex; i < startIndex + amount; i++)
        {
            int index = i % SculkHorde.savedData.getBeeNestEntries().size();

            if(!SculkHorde.savedData.getBeeNestEntries().get(index).isEntryValid(worldIn)) { continue; }

            SculkHorde.savedData.getBeeNestEntries().get(index).enableOccupantsExiting(worldIn);
        }
    }

    public void processReinforcementRequest(ReinforcementRequest context)
    {
        context.isRequestViewed = true;

        //Auto approve is this reinforcement is requested by a developer or sculk mass
        if(context.sender == ReinforcementRequest.senderType.Developer || context.sender == ReinforcementRequest.senderType.SculkMass)
        {
            context.isRequestApproved = true;
        }

        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            return;
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
    public void placeSculkNode(ServerLevel worldIn, BlockPos targetPos, boolean enableChance)
    {
        final int SPAWN_NODE_COST = 3000;
        final int SPAWN_NODE_BUFFER = 1000;

        //Random Chance to Place TreeNode
        if(new Random().nextInt(1000) > 1 && enableChance) { return; }

        if(!SculkHorde.savedData.isSculkNodeCooldownOver())
        {
            return;
        }

        //If we are too close to another node, do not create one
        if(!SculkHorde.gravemind.isValidPositionForSculkNode(worldIn, targetPos)) { return; }


        if(SculkHorde.savedData.getSculkAccumulatedMass() < SPAWN_NODE_COST + SPAWN_NODE_BUFFER)
        {
            return;
        }

        SculkNodeBlock.FindAreaAndPlaceNode(worldIn, targetPos);
        SculkHorde.savedData.subtractSculkAccumulatedMass(SPAWN_NODE_COST);

    }


    /**
     * Will check each known node location in {@link ModSavedData}
     * to see if there is one too close.
     * @param positionIn The potential location of a new node
     * @return true if creation of new node is approved, false otherwise.
     */
    public boolean isValidPositionForSculkNode(ServerLevel worldIn, BlockPos positionIn)
    {
        if(worldIn.canSeeSky(positionIn))
        {
            return false;
        }

        if(SculkHorde.savedData.getNodeEntries().size() >= SculkHorde.gravemind.sculk_node_limit)
        {
            return false;
        }

        for (ModSavedData.NodeEntry entry : SculkHorde.savedData.getNodeEntries())
        {
            //Get Distance from our potential location to the current index node position
            int distanceFromPotentialToCurrentNode = (int) getBlockDistance(positionIn, entry.getPosition());

            //if we find a single node that is too close, disapprove of creating a new one
            if (distanceFromPotentialToCurrentNode < MINIMUM_DISTANCE_BETWEEN_NODES)
            {
                return false;
            }
        }
        return true;
    }


    /** ######## Classes ######## **/

    /** ######## CLASSES ######### **/


}
