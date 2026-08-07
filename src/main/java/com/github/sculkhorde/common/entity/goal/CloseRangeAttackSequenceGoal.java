package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class CloseRangeAttackSequenceGoal extends AttackSequenceGoal implements IDebuggableGoal
{
    protected float distanceRequired = 0;

    public CloseRangeAttackSequenceGoal(Mob mob, long executionCooldown, float distanceRequired, AttackStepGoal... attacksIn)
    {
        super(mob, executionCooldown, attacksIn);
        this.distanceRequired = distanceRequired;
    }


    @Override
    public boolean canUse() {

        if(!super.canUse() || mob.getTarget() == null)
        {
            return false;
        }
        else if(EntityAlgorithms.getDistanceBetweenEntities(mob, mob.getTarget()) > distanceRequired)
        {
            reasonForNoStart = "Target is too far away.";
            return false;
        }

        return true;
    }

    @Override
    public Optional<String> getGoalName() {
        if(getCurrentGoal() != null)
        {
            return Optional.of("Close Attack Sequence | " + getCurrentGoal().getClass().getSimpleName());
        }

        return Optional.of("Close Attack Sequence");
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof CloseRangeAttackSequenceGoal goal)
        {
            return goal.uuid.equals(uuid);
        }

        return false;
    }
}