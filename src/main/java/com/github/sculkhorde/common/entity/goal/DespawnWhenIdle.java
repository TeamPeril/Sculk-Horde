package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.concurrent.TimeUnit;

public class DespawnWhenIdle extends Goal {

    long lastTimeSinceNotIdle = System.nanoTime();
    long timeElapsed = 0;
    long secondsIdleThreshold;
    ISculkSmartEntity mob;

    public DespawnWhenIdle(ISculkSmartEntity mob, int secondsIdleThreshold)
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
        if(!mob.isIdle())
        {
            lastTimeSinceNotIdle = System.nanoTime();
        }

        timeElapsed = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastTimeSinceNotIdle);
        return timeElapsed > secondsIdleThreshold;
    }

    @Override
    public void start()
    {
        mob.remove();
    }
}
