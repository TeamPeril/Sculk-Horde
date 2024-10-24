package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.SculkVexEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ServerLevelAccessor;

public class SummonVexAttackGoal extends Goal
{
    protected final SculkSoulReaperEntity mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(30);
    protected long lastTimeOfExecution;

    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;


    public SummonVexAttackGoal(SculkSoulReaperEntity mob, int minDifficulty, int maxDifficulty) {
        this.mob = mob;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private SculkSoulReaperEntity getEntity()
    {
        return (SculkSoulReaperEntity)this.mob;
    }

    @Override
    public boolean canUse()
    {

        if(mob.level().getGameTime() - lastTimeOfExecution < executionCooldown)
        {
            return false;
        }
        if(mob.getTarget() == null)
        {
            return false;
        }
        if(mob.getMobDifficultyLevel() < minDifficulty)
        {
            return false;
        }

        if(mob.getMobDifficultyLevel() > maxDifficulty && maxDifficulty != -1)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }

    @Override
    public void start()
    {
        super.start();
        performSpellCasting();
        lastTimeOfExecution = mob.level().getGameTime();
    }

    // Performs the spell casting action
    protected void performSpellCasting() {

        for(int i = 0; i < 3; i++)
        {
            SculkVexEntity entity = new SculkVexEntity(mob.level());
            entity.setPos(mob.getX(), mob.getY() + 1, mob.getZ());
            entity.finalizeSpawn((ServerLevelAccessor) mob.level(), mob.level().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, (SpawnGroupData)null, (CompoundTag)null);
            entity.setOwner(mob);
            entity.setBoundOrigin(mob.blockPosition());
            entity.setLimitedLife(TickUnits.convertMinutesToTicks(5));
            entity.setTarget(mob.getTarget());
            mob.level().addFreshEntity(entity);
        }
    }
}
