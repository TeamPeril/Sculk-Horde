package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

public class InvalidateTargetGoal extends Goal {

    private final ISculkSmartEntity mob; // We use this to retrieve the mob that is using this goal.

    public InvalidateTargetGoal(ISculkSmartEntity mob)
    {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public MobEntity getMob()
    {
        return (MobEntity) this.mob;
    }

    public LivingEntity getTarget()
    {
        return getMob().getTarget();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {

        if(getTarget() == null)
        {
            return false;
        }
        TargetParameters targetParameters = mob.getTargetParameters();
        boolean result = !targetParameters.isEntityValidTarget(getTarget(), true);
        return result;
    }

    @Override
    public void start()
    {
        getMob().setTarget(null);
        getMob().getTarget();
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
