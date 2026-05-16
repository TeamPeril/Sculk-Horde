package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public class DespawnAfterTime extends Goal {
    protected long ticksThreshold;
    protected ISculkSmartEntity mob;
    protected long creationTime;
    protected Level level;

    public DespawnAfterTime(ISculkSmartEntity mob, int ticksThreshold)
    {
        super();
        this.mob = mob;
        this.ticksThreshold = ticksThreshold;
        this.level = ((Mob) mob).level();
        this.creationTime = level.getGameTime();
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        boolean mobHasBeenNameTagged = ((Mob) mob).hasCustomName();
        if(level.getGameTime() - creationTime > ticksThreshold && !mob.isParticipatingInRaid() && !mobHasBeenNameTagged)
        {
            return true;
        }

        if(DifficultyUtil.getCurrentDifficulty().equals(Difficulty.PEACEFUL) && !mobHasBeenNameTagged)
        {
            return true;
        }
        return false;
    }

    @Override
    public void start()
    {
        Mob mobEntity = ((Mob)mob);
        BlockInfestationSystem.tryToInfestBlock((ServerLevel) mobEntity.level(), mobEntity.blockPosition().below());
        ParticleUtil.spawnDespawnParticles(mobEntity);

        mobEntity.remove(Entity.RemovalReason.DISCARDED);
        ModSavedData.getSaveData().addSculkAccumulatedMass((int) mobEntity.getHealth());
        SculkHorde.statisticsData.addTotalMassFromDespawns((int) mobEntity.getHealth());
    }
}
