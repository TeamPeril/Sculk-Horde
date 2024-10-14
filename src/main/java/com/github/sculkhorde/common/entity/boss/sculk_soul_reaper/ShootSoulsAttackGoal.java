package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ShootSoulsAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;
    private int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.2F);
    private int attackkIntervalCooldown = 0;


    public ShootSoulsAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
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
        ticksElapsed++;

        if(mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        if(mob.closerThan(mob.getTarget(), 3.0F))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return elapsedAttackDuration < maxAttackDuration;
    }

    @Override
    public void start()
    {
        super.start();
        getEntity().triggerAnim("attack_controller", "fireball_sky_summon_animation");
        getEntity().triggerAnim("twitch_controller", "fireball_sky_twitch_animation");
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        spawnSoulAndShootAtTarget(5);
    }

    @Override
    public void stop()
    {
        super.stop();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
    }

    public double getRandomOffset(double min, double max)
    {
        return (mob.getRandom().nextFloat() * (max + min) - min);
    }

    public void spawnSoulAndShootAtTarget(int range)
    {


        attackkIntervalCooldown--;


        if(attackkIntervalCooldown > 0)
        {
            return;
        }

        if(mob.getTarget() == null)
        {
            return;
        }

        SculkAcidicProjectileEntity projectileEntity = new SculkAcidicProjectileEntity(mob.level(), mob, 1);
        double spawnX = mob.getX() + getRandomOffset(0, 1);
        double spawnY = mob.getY() + mob.getEyeHeight() + getRandomOffset(0, 1);
        double spawnZ = mob.getZ() + getRandomOffset(0, 1);

        double d0 = mob.getTarget().getX() - spawnX;
        double d1 = mob.getTarget().getY(0.3333333333333333D) - spawnY;
        double d2 = mob.getTarget().getZ() - spawnZ;
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        projectileEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - mob.level().getDifficulty().getId() * 4));
        mob.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(projectileEntity);

        attackkIntervalCooldown = attackIntervalTicks;
    }

}
