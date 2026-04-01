package com.github.sculkhorde.common.entity.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class CustomMeleeAttackGoal2 extends CustomAttackGoal2 {


    public CustomMeleeAttackGoal2(Mob mob, float maxDistanceForAttackIn, long preAttackDelay, long postAttackDelay) {
        super(mob, maxDistanceForAttackIn, preAttackDelay, postAttackDelay);
    }


    protected void doAttack() {

        hurtTarget(mob, mob.getTarget());

    }

    public void hurtTarget(Mob damageDealer, LivingEntity damageReceiver)
    {
        damageDealer.doHurtTarget(damageReceiver);
    }

}
