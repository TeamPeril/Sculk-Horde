package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.goal.AttackSequenceGoal;
import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.world.entity.Mob;

public class ReaperAttackSequenceGoal extends AttackSequenceGoal {


    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;

    public ReaperAttackSequenceGoal(Mob mob, long executionCooldown, int minDifficulty, int maxDifficulty, AttackStepGoal... attacksIn) {
        super(mob, executionCooldown, attacksIn);
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public AngelOfReapingEntity getReaper()
    {
        return(AngelOfReapingEntity) mob;
    }

    @Override
    public boolean canUse() {
        if(!super.canUse())
        {
            return false;
        }

        if(getReaper().isThereAnotherAttackActive(this))
        {
            if(getReaper().getCurrentAttack().getCurrentGoal() == null)
            {
                reasonForNoStart = "There is already an attack going on: null";
                return false;
            }

            reasonForNoStart = "There is already an attack going on: \n   " + getReaper().getCurrentAttack().getCurrentGoal().getClass().getSimpleName();
            return false;
        }

        if(getReaper().getMobDifficultyLevel() < minDifficulty)
        {
            reasonForNoStart = "Incorrect Difficulty";
            return false;
        }

        if(getReaper().getMobDifficultyLevel() > maxDifficulty && maxDifficulty != -1)
        {
            reasonForNoStart = "Incorrect Difficulty";
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {

        if(getReaper().isThereAnotherAttackActive(this))
        {
            return false;
        }

        return super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        getReaper().setCurrentAttack(this);
    }

    @Override
    public void stop() {
        if(finishedAttackSequence || !canContinueToUse())
        {
            getReaper().clearCurrentAttack();
        }

        super.stop();
    }
}
