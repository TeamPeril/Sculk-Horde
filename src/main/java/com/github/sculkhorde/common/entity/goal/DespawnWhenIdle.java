package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.concurrent.TimeUnit;

public class DespawnWhenIdle extends Goal {

    long lastTimeSinceNotIdle = System.nanoTime();
    long timeElapsed = 0;
    long secondsIdleThreshold;
    ISculkSmartEntity mob;

    public DespawnWhenIdle(ISculkSmartEntity mob, long secondsIdleThreshold)
    {
        super();
        this.mob = mob;
        this.secondsIdleThreshold = secondsIdleThreshold;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        if(!mob.isIdle() || mob.isParticipatingInRaid())
        {
            lastTimeSinceNotIdle = System.nanoTime();
        }

        timeElapsed = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastTimeSinceNotIdle);
        return timeElapsed > secondsIdleThreshold;
    }

    @Override
    public void start()
    {
        ((Mob)mob).remove(Entity.RemovalReason.DISCARDED);
        if(SculkHorde.savedData != null) { SculkHorde.savedData.addSculkAccumulatedMass((int) ((Mob) mob).getHealth()); }
    }
}
