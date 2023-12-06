package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import com.github.sculkhorde.common.entity.SculkZombieEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal.Flag;

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
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public void start()
    {
        super.start();
        ((SculkZombieEntity)mob).triggerAnim("attack_controller", "attack_animation");
    }

    @Override
    public boolean canUse()
    {
        boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
        // If the mob is already targeting something valid, don't bother
        return canWeUse;
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse();
    }
}
