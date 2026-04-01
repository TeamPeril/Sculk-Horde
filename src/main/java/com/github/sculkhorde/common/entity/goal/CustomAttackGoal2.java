package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class CustomAttackGoal2 extends Goal {
    protected final Mob mob;

    protected long timeOfLastExecution;
    protected long lastCanUseCheck;

    protected long preAttackDelay;
    protected long postAttackDelay;
    protected boolean isAttackInProgress = false;
    protected long preAttackStartTime = 0;
    protected long attackStartTime = 0;
    protected long postAttackStartTime = 0;
    protected float maxDistanceForAttack = 0;

    /// 0 = pre attack, 1 = attack, 2 = post attack
    public final int PRE_ATTACK_STATE = 0;
    public final int ATTACK_STATE = 1;
    public final int POST_ATTACK_STATE = 2;
    public int attackState = PRE_ATTACK_STATE;

    public CustomAttackGoal2(Mob mob, float maxDistanceForAttackIn, long preAttackDelay, long postAttackDelay) {
        this.mob = mob;
        this.preAttackDelay = preAttackDelay;
        this.postAttackDelay = postAttackDelay;
        maxDistanceForAttack = maxDistanceForAttackIn;
    }



    protected long getExecutionCooldown()
    {
        return TickUnits.convertSecondsToTicks(2);
    }

    public boolean canUse() {

        if(!TickUnits.hasTicksPassed(lastCanUseCheck, mob.level(), TickUnits.convertSecondsToTicks(1)))
        {
            return false;
        }

        this.lastCanUseCheck = mob.level().getGameTime();

        if(!isExecutionCooldownOver())
        {
            return false;
        }

        if(isTargetInvalid() && attackState != POST_ATTACK_STATE)
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
        isAttackInProgress = true;
        preAttackStartTime = mob.level().getGameTime();
        timeOfLastExecution = preAttackStartTime;

        playPreAttackSound();
        playPreAttackAnimation();
    }

    public void stop() {
        postAttackStartTime = 0;
        attackStartTime = 0;
        preAttackStartTime = 0;
        attackState = 0;
        isAttackInProgress = false;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void preAttackTick()
    {

    }

    public void attackTick()
    {
        if (isTargetInvalid()) {
            return;
        }

        doAttack();
    }

    public void postAttackTick()
    {

    }

    public void customAiTick()
    {

    }

    public void tick() {
        if (isTargetInvalid() || !isAttackInProgress) {
            return;
        }

        customAiTick();

        // State Executions
        if(attackState == PRE_ATTACK_STATE)
        {
            preAttackTick();
            if(TickUnits.hasTicksPassed(preAttackStartTime, mob.level(), preAttackDelay))
            {
                moveToNextState();
            }
        }
        else if(attackState == ATTACK_STATE)
        {
            attackTick();
        }
        else if(attackState == POST_ATTACK_STATE)
        {
            postAttackTick();
            if(TickUnits.hasTicksPassed(postAttackStartTime, mob.level(), postAttackDelay))
            {
                moveToNextState();
            }
        }
        else
        {
            endAttack();
        }
    }

    public void moveToNextState()
    {
        attackState++;

        if(attackState == ATTACK_STATE)
        {
            attackStartTime = mob.level().getGameTime();
            playAttackAnimation();
            playAttackSound();

        }
        else if(attackState == POST_ATTACK_STATE)
        {
            postAttackStartTime = mob.level().getGameTime();
            playPostAttackAnimation();
            playPostAttackSound();
        }
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
        if(EntityAlgorithms.isEntityUntargetable(mob.getTarget()))
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

    protected void playPreAttackAnimation() {

    }

    protected void playPreAttackSound() {

    }

    protected void playAttackAnimation() {

    }

    protected void playAttackSound() {

    }

    protected void playPostAttackAnimation() {

    }

    protected void playPostAttackSound() {

    }


    protected void doAttack() {



    }

    protected void endAttack()
    {
        isAttackInProgress = false;
    }

    protected boolean isExecutionCooldownOver() {
        return TickUnits.hasTicksPassed(timeOfLastExecution, mob.level(), getExecutionCooldown());
    }

}
