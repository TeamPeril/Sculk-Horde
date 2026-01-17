package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;

public class AttackStepGoal extends Goal implements IDebuggableGoal {
    protected Mob mob;
    protected boolean isReadyForNextAttackStep = false;
    protected boolean isPreAttack = true;
    protected boolean isPostAttack = false;

    protected int preAttackDelayRemaining = 0;
    protected int postAttackDelayRemaining = 0;

    protected boolean hasPlayedPreAttackAnimation = false;
    protected boolean hasPlayedAttackAnimation = false;
    protected boolean hasPlayedPostAttackAnimation = false;
    protected String lastReasonOfNoStart = "None";
    protected AttackSequenceGoal sequenceParent;

    public AttackStepGoal(Mob mob)
    {
        this.mob = mob;

    }

    protected AttackSequenceGoal getSequenceParent()
    {
        return sequenceParent;
    }

    protected void setSequenceParent(AttackSequenceGoal parent)
    {
        sequenceParent = parent;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return !isReadyForNextAttackStep();
    }

    @Override
    public Optional<String> getLastReasonForGoalNoStart() {
        return Optional.of(lastReasonOfNoStart);
    }

    @Override
    public Optional<String> getGoalName() {
        return Optional.empty();
    }

    @Override
    public long getLastTimeOfGoalExecution() {
        return -1;
    }

    @Override
    public long getTimeRemainingBeforeCooldownOver() {
        return -1;
    }

    public boolean isAttackTickComplete() {
        return isPostAttack;
    }

    public void setAttackTickComplete()
    {
        isPostAttack = true;
    }

    public boolean isPostAttack() {
        return isPostAttack;
    }

    public boolean isPreAttack() { return isPreAttack; }

    public boolean isReadyForNextAttackStep() {
        return isReadyForNextAttackStep;
    }

    protected int getPreAttackDelay() { return TickUnits.convertSecondsToTicks(1);}
    protected int getPreAttackDelayRemaining()
    {
        return preAttackDelayRemaining;
    }

    public void setPreAttackDelayRemaining(int preAttackDelayRemaining) {
        this.preAttackDelayRemaining = preAttackDelayRemaining;
    }

    public void setPreAttack(boolean preAttack) {
        isPreAttack = preAttack;
    }

    protected int getPostAttackDelay() { return TickUnits.convertSecondsToTicks(3);}
    protected int getPostAttackDelayRemaining() { return postAttackDelayRemaining; }

    public void setPostAttackDelayRemaining(int postAttackDelayRemaining) {
        this.postAttackDelayRemaining = postAttackDelayRemaining;
    }

    public void setPostAttack(boolean postAttack) {
        isPostAttack = postAttack;
    }

    public void setReadyForNextAttackStep(boolean readyForNextAttackStep) {
        isReadyForNextAttackStep = readyForNextAttackStep;
    }

    @Override
    public void stop() {
        super.stop();
        setPostAttack(false);
        setPreAttack(true);

        hasPlayedPreAttackAnimation = false;
        hasPlayedAttackAnimation = false;
        hasPlayedPostAttackAnimation = false;
        isReadyForNextAttackStep = false;
    }

    @Override
    public void start() {
        super.start();
        setPreAttackDelayRemaining(getPreAttackDelay());
        setPostAttackDelayRemaining(getPostAttackDelay());
        isReadyForNextAttackStep = false;

        playPreAttackAnimation();
        hasPlayedPreAttackAnimation = true;

    }

    @Override
    public void tick()
    {
        super.tick();

        if(isReadyForNextAttackStep())
        {
            return;
        }

        if(isPreAttack())
        {
            setPreAttackDelayRemaining(getPreAttackDelayRemaining() - 1);
            doPreAttackTick();

            if(getPreAttackDelayRemaining() <= 0)
            {
                setPreAttack(false);
            }
        }
        else if(isPostAttack())
        {
            if(!hasPlayedPostAttackAnimation)
            {
                playPostAttackAnimation();
                hasPlayedPostAttackAnimation = true;
            }

            setPostAttackDelayRemaining(getPostAttackDelayRemaining() - 1);
            doPostAttackTick();

            if(getPostAttackDelayRemaining() <= 0)
            {
                setReadyForNextAttackStep(true);
            }
        }
        else
        {
            if(!hasPlayedAttackAnimation)
            {
                playAttackAnimation();
                hasPlayedAttackAnimation = true;
            }

            doAttackTick();

            if(isAttackTickComplete())
            {
                setPostAttack(true);
            }
        }


    }

    protected void doPreAttackTick()
    {

    }

    protected void doAttackTick()
    {

    }

    protected void doPostAttackTick()
    {

    }

    protected void playPreAttackAnimation()
    {

    }

    protected void playAttackAnimation()
    {

    }

    protected void playPostAttackAnimation()
    {

    }
}
