package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulFireProjectileEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulPoisonProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ShootSoulsAttackGoal extends Goal
{
    private final Mob mob;
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;
    protected int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.2F);
    protected int attackkIntervalCooldown = 0;

    protected int projectileType = 0;


    public ShootSoulsAttackGoal(PathfinderMob mob, int durationInTicks) {
        this.mob = mob;
        maxAttackDuration = durationInTicks;
        this.setFlags(EnumSet.of(Flag.LOOK));
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

        if(!mob.getSensing().hasLineOfSight(mob.getTarget()))
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
        projectileType = getRandomIntInRange(0,1);
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

    public double getRandomDoubleInRange(double min, double max)
    {
        return min + (mob.getRandom().nextFloat() * (max + min));
    }
    public int getRandomIntInRange(int min, int max)
    {
        return min + (mob.getRandom().nextInt() * (max + min));
    }

    public AbstractProjectileEntity getProjectile()
    {

        return switch (projectileType) {
            case 0 -> new SoulFireProjectileEntity(mob.level(), mob, 2.5F);
            case 1 -> new SoulPoisonProjectileEntity(mob.level(), mob, 2.5F);
            default -> new SoulFireProjectileEntity(mob.level(), mob, 2.5F);
        };
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

        AbstractProjectileEntity projectile =  getProjectile();

        projectile.setPos(mob.position().add(0, mob.getEyeHeight() - projectile.getBoundingBox().getYsize() * .5f, 0));

        double spawnPosX = mob.getX() + getRandomDoubleInRange(0, 1);
        double spawnPosY = mob.getY() + mob.getEyeHeight() + getRandomDoubleInRange(0, 1);
        double spawnPosZ = mob.getZ() + getRandomDoubleInRange(0, 1);

        double targetPosX = mob.getTarget().getX() - spawnPosX  + getRandomDoubleInRange(0, 1);
        double targetPosY = mob.getTarget().getY(1) - spawnPosY + getRandomDoubleInRange(0, 1);
        double targetPosZ = mob.getTarget().getZ() - spawnPosZ + getRandomDoubleInRange(0, 1);

        // Create a vector for the direction
        Vec3 direction = new Vec3(targetPosX, targetPosY, targetPosZ).normalize();

        // Shoot the projectile in the direction vector
        projectile.shoot(direction);

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(projectile);

        attackkIntervalCooldown = attackIntervalTicks;
    }

}
//projectileEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - mob.level().getDifficulty().getId() * 4));