package com.github.sculkhoard.common.entity.goal;

import com.github.sculkhoard.common.entity.SculkMiteAggressorEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class SculkMiteAggressorAttackGoal extends MeleeAttackGoal {

    private final SculkMiteAggressorEntity thisMob;

    /**
     * The Constructor
     * @param mob The mob that called this
     * @param speedModifier How fast can they attack?
     * @param followTargetIfNotSeen Should the mob follow their target if they cant see them.
     */
    public SculkMiteAggressorAttackGoal(SculkMiteAggressorEntity mob, double speedModifier, boolean followTargetIfNotSeen) {
        super(mob, speedModifier, followTargetIfNotSeen);
        this.thisMob = mob;
    }


    /**
     * Starts the attack Sequence<br>
     * We shouldn't have to check if the target is null since
     * the super class does this. However, something funky is going on that
     * causes a null pointer exception if we dont check this in tick(). I put
     * it here aswell just in case.
     */
    public void start()
    {
        if(this.thisMob.getTarget() != null)
        {
            super.start();
        }
    }

    /**
     * Stops the attack sequence.
     */
    public void stop()
    {
        super.stop();
    }

    /**
     * Gets called every tick the attack is active<br>
     * We shouldn't have to check if the target is null since
     * the super class does this. However, something funky is going on that
     * causes a null pointer exception if we dont check this here. This is
     * absolutely some sort of bug that I was unable to figure out. For the
     * time being (assuming I ever fix this), this will have to do.
     */
    public void tick()
    {
        if(this.thisMob.getTarget() == null)
        {
            stop();
        }
        else
        {
            super.tick();
        }
    }
}
