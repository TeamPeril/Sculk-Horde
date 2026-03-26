package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulFlySwatterProjectileAttackEntity;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class ShootSoulFlySwatterAttackGoal extends ReaperCastSpellGoal
{
    protected final int baseCastingTime = TickUnits.convertSecondsToTicks(3);
    protected int castingTime = 0;
    boolean spellCasted = false;

    public ShootSoulFlySwatterAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public boolean canUse()
    {
        if(!super.canUse())
        {
            return false;
        }

        if(EntityAlgorithms.getHeightOffGround(mob.getTarget()) <= 2)
        {
            return false;
        }

        return true;
    }


    @Override
    protected void doAttackTick() {
        super.doAttackTick();
        shootProjectileAtTarget();
        setPostAttack(true);
    }


    public double getRandomDoubleInRange(double min, double max)
    {
        return min + (mob.getRandom().nextFloat() * (max + min));
    }
    public void shootProjectileAtTarget()
    {

        if(mob.getTarget() == null)
        {
            return;
        }

        AbstractProjectileEntity projectile =  new SoulFlySwatterProjectileAttackEntity(mob.level(), mob, 10F);
        projectile.setPos(mob.position().add(0, mob.getEyeHeight() - projectile.getBoundingBox().getYsize() * .5f, 0));

        double spawnPosX = mob.getX();
        double spawnPosY = mob.getY() + mob.getEyeHeight();
        double spawnPosZ = mob.getZ();

        double targetPosX = mob.getTarget().getX() - spawnPosX  + getRandomDoubleInRange(0, 1);
        double targetPosY = mob.getTarget().getY() - spawnPosY + getRandomDoubleInRange(0, 1);
        double targetPosZ = mob.getTarget().getZ() - spawnPosZ + getRandomDoubleInRange(0, 1);

        // Create a vector for the direction
        Vec3 direction = new Vec3(targetPosX, targetPosY, targetPosZ).normalize();

        // Shoot the projectile in the direction vector
        projectile.shoot(direction);

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(projectile);

    }
}