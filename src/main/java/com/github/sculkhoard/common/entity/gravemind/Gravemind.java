package com.github.sculkhoard.common.entity.gravemind;


import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.ReinforcementContext;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.LivingEntity;

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

    private attack_states attack_state;

    public EntityFactory entityFactory;

    private int MASS_GOAL_FOR_IMMATURE = 500;
    private int MASS_GOAL_FOR_MATURE = 1000;


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
        deductCurrentState();
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
    public void deductCurrentState()
    {
        if(SculkHoard.entityFactory.getSculkAccumulatedMass() >= MASS_GOAL_FOR_IMMATURE)
            evolution_state = evolution_states.Immature;
        else if(SculkHoard.entityFactory.getSculkAccumulatedMass() >= MASS_GOAL_FOR_MATURE)
            evolution_state = evolution_states.Mature;

        if(DEBUG_THIS) System.out.println("Gravemind deduced the current state as: " + evolution_state);
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
}
