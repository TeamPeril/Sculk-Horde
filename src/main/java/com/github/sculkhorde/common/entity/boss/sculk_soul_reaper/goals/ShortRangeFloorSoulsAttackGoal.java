package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

public class ShortRangeFloorSoulsAttackGoal extends Goal
{
    private final SculkSoulReaperEntity mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected long lastTimeOfExecution;
    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;


    public ShortRangeFloorSoulsAttackGoal(SculkSoulReaperEntity mob, int minDifficulty, int maxDifficulty) {
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

        if(!mob.closerThan(mob.getTarget(), 10.0F))
        {
            return false;
        }

        if(!mob.getSensing().hasLineOfSight(mob.getTarget()))
        {
            return false;
        }

        if(mob.getMobDifficultyLevel() < minDifficulty || mob.getMobDifficultyLevel() > maxDifficulty)
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
        spawnSoulSuckersOnFloorInCircle(0, 1);
        spawnSoulSuckersOnFloorInCircle(6, 6);
        //spawnSoulSuckersOnFloorInCircle(12, 12);
        lastTimeOfExecution = mob.level().getGameTime();
    }


    public void spawnSoulSuckersOnFloorInCircle(int radius, int amount)
    {
        ArrayList<Vec3> pos = BlockAlgorithms.getPointsOnCircumferenceVec3(mob.position(), radius, amount);

        for(Vec3 position: pos)
        {
            AreaEffectCloud cloud = new AreaEffectCloud(mob.level(), position.x, position.y, position.z);
            cloud.setOwner(mob);
            cloud.setRadius(3);
            cloud.setDuration(TickUnits.convertSecondsToTicks(20));
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM));
            cloud.setParticle(ParticleTypes.SCULK_SOUL);
            mob.level().addFreshEntity(cloud);
        }
    }

}
