package com.github.sculkhoard.common.entity.goal;

import com.github.sculkhoard.common.entity.SculkZombieEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;

public class SculkZombieAttackGoal extends MeleeAttackGoal {

    private final SculkZombieEntity sculkZombie;

    public SculkZombieAttackGoal(SculkZombieEntity mob, double speedModifier, boolean followTargetIfNotSeen) {
        super(mob, speedModifier, followTargetIfNotSeen);
        this.sculkZombie = mob;
    }

    public void start()
    {
        if(this.sculkZombie.getTarget() != null)
        {
            super.start();
        }
    }

    public void stop()
    {
        super.stop();
    }

    public void tick()
    {
        super.tick();
    }
}
