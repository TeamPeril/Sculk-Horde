package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class LegacyCustomAttackGoal extends Goal {
    protected final Mob mob;

    protected long timeOfLastExecution;
    protected long lastCanUseCheck;
    protected int attack_animation_delay;
    protected boolean isAttackInProgress = false;
    protected int ticksUntilAttackExecution = attack_animation_delay;
    protected float maxDistanceForAttack = 0;

    public LegacyCustomAttackGoal(Mob mob, float maxDistanceForAttackIn, int attackDelay) {
        this.mob = mob;
        attack_animation_delay = attackDelay;
        maxDistanceForAttack = maxDistanceForAttackIn;
    }

    public long getCanUseCheckInterval() {
        return 20;
    }

    protected long getExecutionCooldown()
    {
        return TickUnits.convertSecondsToTicks(2);
    }

    public boolean canUse() {
        long gameTime = this.mob.level().getGameTime();
        if (gameTime - this.lastCanUseCheck < getCanUseCheckInterval()) {
            return false;
        }

        this.lastCanUseCheck = gameTime;

        if(!isExecutionCooldownOver())
        {
            return false;
        }

        if(isTargetInvalid())
        {
            return false;
        }

        return true;
    }

    public boolean canContinueToUse() {
        if(!isAttackInProgress)
        {
            return false;
        }
        return !isTargetInvalid();
    }

    public void start() {
        triggerAnimation();
        isAttackInProgress = true;
    }

    public void stop() {

    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }


    public void tick() {
        if (isTargetInvalid() || !isAttackInProgress) {
            return;
        }

        ticksUntilAttackExecution = Math.max(ticksUntilAttackExecution - 1, 0);

        if(ticksUntilAttackExecution > 0)
        {
            return;
        }

        checkAndAttack(mob.getTarget());
        endAttack();

    }

    protected void triggerAnimation() {

    }

    protected boolean isTargetNullOrDead()
    {
        if (mob.getTarget() == null) {
            return true;
        }

        return mob.getTarget().isDeadOrDying();
    }

    protected boolean isTooFarFromTarget()
    {
        if (mob.getTarget() == null) {
            return true;
        }

        return EntityAlgorithms.getDistanceBetweenEntities(mob, mob.getTarget()) > maxDistanceForAttack;
    }

    protected boolean cantSeeTarget()
    {
        if (mob.getTarget() == null) {
            return true;
        }

        return !this.mob.getSensing().hasLineOfSight(mob.getTarget());
    }

    protected boolean isTargetInvalid()
    {
        if(isTargetNullOrDead())
        {
            return true;
        }

        if(isTooFarFromTarget())
        {
            return true;
        }

        if(cantSeeTarget())
        {
            return true;
        }

        return false;
    }


    protected void checkAndAttack(LivingEntity targetMob) {

        if (isTargetInvalid()) {
            return;
        }

        if (!isExecutionCooldownOver()) {
            return;
        }

        mob.doHurtTarget(targetMob);
        onTargetHurt(targetMob);
    }

    protected void endAttack() {
        timeOfLastExecution = mob.level().getGameTime();
        ticksUntilAttackExecution = attack_animation_delay;
        isAttackInProgress = false;
    }

    protected boolean isExecutionCooldownOver() {
        return mob.level().getGameTime() - timeOfLastExecution >= getExecutionCooldown();
    }

    public void onTargetHurt(LivingEntity target) {

    }

}
