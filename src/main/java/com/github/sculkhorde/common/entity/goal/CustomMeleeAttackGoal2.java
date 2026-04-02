package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.EnumSet;

public class CustomMeleeAttackGoal2 extends CustomAttackGoal2 {

    protected int ticksUntilNextPathRecalculation = 0;


    public CustomMeleeAttackGoal2(Mob mob, float maxDistanceForAttackIn, long preAttackDelay, long postAttackDelay) {
        super(mob, maxDistanceForAttackIn, preAttackDelay, postAttackDelay);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }


    protected void doAttack() {

        hurtTarget(mob, mob.getTarget());
        moveToNextState();
    }

    public void hurtTarget(Mob damageDealer, LivingEntity damageReceiver)
    {
        damageDealer.doHurtTarget(damageReceiver);
    }

    @Override
    public void customAiTick() {
        LivingEntity target = mob.getTarget();
        if (target == null) {
            return;
        }

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        double distanceFromTarget = EntityAlgorithms.getDistanceBetweenEntities(mob, target);

        ticksUntilNextPathRecalculation -= 1;

        if (ticksUntilNextPathRecalculation <= 0) {
            this.mob.getNavigation().moveTo(target, 1.0D);

            if (distanceFromTarget < 5) {
                ticksUntilNextPathRecalculation = TickUnits.convertSecondsToTicks(0.5F);
            } else if (distanceFromTarget < 10) {
                ticksUntilNextPathRecalculation = TickUnits.convertSecondsToTicks(1);
            } else if (distanceFromTarget < 20) {
                ticksUntilNextPathRecalculation = TickUnits.convertSecondsToTicks(2);
            } else {
                ticksUntilNextPathRecalculation = TickUnits.convertSecondsToTicks(3);
            }
        }
    }
}
