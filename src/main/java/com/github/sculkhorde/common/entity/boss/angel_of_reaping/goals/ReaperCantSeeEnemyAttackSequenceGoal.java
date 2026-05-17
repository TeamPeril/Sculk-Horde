package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.goal.AttackStepGoal;
import net.minecraft.world.entity.Mob;

public class ReaperCantSeeEnemyAttackSequenceGoal extends ReaperAttackSequenceGoal {

    public ReaperCantSeeEnemyAttackSequenceGoal(Mob mob, long executionCooldown, int minDifficulty, int maxDifficulty, AttackStepGoal... attacksIn) {
        super(mob, executionCooldown, minDifficulty, maxDifficulty, attacksIn);
    }
    @Override
    public boolean canUse() {
        if(!super.canUse())
        {
            return false;
        }

        if(getReaper().getTarget() == null)
        {
            return false;
        }

        if(getReaper().getSensing().hasLineOfSight(getReaper().getTarget()))
        {
            reasonForNoStart = "Can see entity";
            return false;
        }

        return true;
    }
}
