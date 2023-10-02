package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.concurrent.TimeUnit;

public class DespawnAfterTime extends Goal {


    long timeElapsed = 0;
    long ticksThreshold;
    ISculkSmartEntity mob;
    long creationTime;
    Level level;

    public DespawnAfterTime(ISculkSmartEntity mob, int ticksThreshold)
    {
        super();
        this.mob = mob;
        this.ticksThreshold = ticksThreshold;
        this.level = ((Mob) mob).level;
        this.creationTime = level.getGameTime();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        if(level.getGameTime() - creationTime > ticksThreshold && !mob.isParticipatingInRaid())
        {
            return true;
        }
        return false;
    }

    @Override
    public void start()
    {
        ((Mob)mob).remove(Entity.RemovalReason.DISCARDED);
        SculkHorde.savedData.addSculkAccumulatedMass((int) ((Mob) mob).getHealth());
    }
}
