package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class SculkZombieAttackGoal extends MeleeAttackGoal
{

    /**
     * The Constructor
     * @param mob The mob that called this
     * @param speedModifier How fast can they attack?
     * @param followTargetIfNotSeen Should the mob follow their target if they cant see them.
     */
    public SculkZombieAttackGoal(SculkZombieEntity mob, double speedModifier, boolean followTargetIfNotSeen) {
        super(mob, speedModifier, followTargetIfNotSeen);
    }

    @Override
    public boolean canUse()
    {
        if(!((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget()))
        {
            return false;
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse()
    {
        if(!((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget()))
        {
            return false;
        }

        return super.canContinueToUse();
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
       super.start();
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
        if(!canContinueToUse())
        {
            stop();
            return;
        }
        super.tick();
    }
}
