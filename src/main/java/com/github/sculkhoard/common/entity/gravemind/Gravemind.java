package com.github.sculkhoard.common.entity.gravemind;


import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.ReinforcementContext;
import com.github.sculkhoard.core.SculkHoard;

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

    public enum evolution_states {Undeveloped, Immature, Mature}
    private evolution_states evolution_state;

    public enum attack_states {Defensive, Offensive}

    private attack_states attack_state;

    public EntityFactory entityFactory;

    private int MASS_GOAL_FOR_IMMATURE = 10000;
    private int MASS_GOAL_FOR_MATURE = 100000;

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
    }

    public boolean approveReinforcement(ReinforcementContext context)
    {

        return true;
    }
}
