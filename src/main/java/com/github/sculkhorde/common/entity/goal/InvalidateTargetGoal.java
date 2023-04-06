package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;

public class InvalidateTargetGoal extends Goal {

    private final ISculkSmartEntity mob; // We use this to retrieve the mob that is using this goal.

    public InvalidateTargetGoal(ISculkSmartEntity mob)
    {
        super();
        this.mob = mob;
    }

    public MobEntity getMob()
    {
        return (MobEntity) this.mob;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        LivingEntity target = ((MobEntity)this.mob).getTarget();
        TargetParameters targetParameters = mob.getTargetParameters();
        boolean result = !targetParameters.isEntityValidTarget(target);
        return result;
    }

    @Override
    public void start()
    {
        getMob().setTarget((LivingEntity)null);
        stop();
    }
}
