package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;

public class ShortRangeFloorSoulsAttackGoal extends Goal
{
    private final Mob mob;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(20);
    protected long lastTimeOfExecution;


    public ShortRangeFloorSoulsAttackGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
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
            AreaEffectCloud cloud = new AreaEffectCloud(mob.level(), position.x, position.y + 1, position.z);
            cloud.setOwner(mob);
            cloud.setRadius(3);
            cloud.setDuration(TickUnits.convertSecondsToTicks(20));
            cloud.addEffect(new MobEffectInstance(MobEffects.HARM));
            cloud.setParticle(ParticleTypes.SCULK_SOUL);
            mob.level().addFreshEntity(cloud);
        }
    }

}
