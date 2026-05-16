package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class DespawnWhenIdle extends Goal {

    long lastTimeSinceNotIdle = 0;
    long timeElapsed = 0;
    long ticksIdleThreshold;
    ISculkSmartEntity mob;

    public DespawnWhenIdle(ISculkSmartEntity mob, long ticksIdleThreshold)
    {
        super();
        this.mob = mob;
        this.ticksIdleThreshold = ticksIdleThreshold;
        lastTimeSinceNotIdle = ((Mob) mob).level().getGameTime();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        if(!mob.isIdle() || mob.isParticipatingInRaid() || ((Mob) mob).hasCustomName())
        {
            lastTimeSinceNotIdle = ((Mob) mob).level().getGameTime();
        }

        timeElapsed = ((Mob) mob).level().getGameTime() - lastTimeSinceNotIdle;
        return timeElapsed > ticksIdleThreshold;
    }

    @Override
    public void start()
    {
        Mob mobEntity = ((Mob)mob);
        BlockInfestationSystem.tryToInfestBlock((ServerLevel) mobEntity.level(), mobEntity.blockPosition().below());
        ParticleUtil.spawnDespawnParticles(mobEntity);

        mobEntity.remove(Entity.RemovalReason.DISCARDED);
        if(ModSavedData.getSaveData() != null) {
        	ModSavedData.getSaveData().addSculkAccumulatedMass((int) mobEntity.getHealth());
            SculkHorde.statisticsData.addTotalMassFromDespawns((int) mobEntity.getHealth());
        }
    }
}
