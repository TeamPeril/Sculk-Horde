package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.*;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class ShootElementalSoulProjectilesGoal extends ReaperCastSpellGoal
{
    private final AngelOfReapingEntity mob;
    protected int maxAttackDuration = TickUnits.convertSecondsToTicks(10);
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;
    protected int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.45F);
    protected int attackkIntervalCooldown = 0;
    protected int projectileType = 0;
    protected int minDifficulty = 0;
    protected int maxDifficulty = 0;

    public ShootElementalSoulProjectilesGoal(AngelOfReapingEntity mob) {
        super(mob);
        this.mob = mob;
    }

    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start()
    {
        super.start();
        projectileType = mob.level().getRandom().nextInt(4);
    }

    @Override
    protected void doAttackTick() {
        elapsedAttackDuration++;
        spawnSoulAndShootAtTarget(5);

        if(!mob.isShootingElementals())
        {
            mob.setFlagIsShootingElementals(true);
        }

        if(elapsedAttackDuration >= maxAttackDuration)
        {
            mob.setFlagIsShootingElementals(false);
            setPostAttack(true);
        }
    }

    @Override
    public void stop()
    {
        super.stop();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
        mob.setFlagIsShootingElementals(false);

    }

    public double getRandomDoubleInRange(double min, double max)
    {
        return min + (mob.getRandom().nextFloat() * (max + min));
    }

    public AbstractProjectileEntity getProjectile()
    {
        return switch (projectileType) {
            case 0 -> new SoulFireProjectileAttackEntity(mob.level(), mob, 2.5F);
            case 1 -> new SoulPoisonProjectileAttackEntity(mob.level(), mob, 2.5F);
            case 2 -> new SoulIceProjectileAttackEntity(mob.level(), mob, 2.5F);
            case 3 -> new SoulBreezeProjectileAttackEntity(mob.level(), mob, 2.5F);
            default -> new SoulFireProjectileAttackEntity(mob.level(), mob, 2.5F);
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

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(1.44F);
    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ELEMENTAL_PROJECTILE_SPELL_CHARGE_ID);
    }

    @Override
    protected void playAttackAnimation() {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ELEMENTAL_PROJECTILE_SPELL_SHOOT_ID);
    }
}
//projectileEntity.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - mob.level().getDifficulty().getId() * 4));