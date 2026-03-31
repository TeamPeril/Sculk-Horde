package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulSpearProjectileAttackEntity;
import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class ShootSoulSpearAttackGoal extends ReaperCastSpellGoal
{
    public ShootSoulSpearAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public void start()
    {
        super.start();
        if(mob.level().isClientSide())
        {
            return;
        }

        this.mob.getNavigation().stop();
        //EntityType.LIGHTNING_BOLT.spawn((ServerLevel) mob.level(), mob.blockPosition().above(50), MobSpawnType.SPAWNER);
    }

    @Override
    protected void doAttackTick() {
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

        AbstractProjectileEntity projectile =  new SoulSpearProjectileAttackEntity(mob.level(), mob, 20F);
        projectile.setPos(mob.position().add(0, mob.getEyeHeight() - projectile.getBoundingBox().getYsize() * .5f, 0));

        double spawnPosX = mob.getX();
        double spawnPosY = mob.getY() + mob.getEyeHeight();
        double spawnPosZ = mob.getZ();

        double targetPosX = mob.getTarget().getX() - spawnPosX  + getRandomDoubleInRange(0, 1);
        double targetPosY = mob.getTarget().getEyePosition().y() - spawnPosY + getRandomDoubleInRange(0, 1);
        double targetPosZ = mob.getTarget().getZ() - spawnPosZ + getRandomDoubleInRange(0, 1);

        // Create a vector for the direction
        Vec3 direction = new Vec3(targetPosX, targetPosY, targetPosZ).normalize();

        // Shoot the projectile in the direction vector
        projectile.shoot(direction);

        mob.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level().addFreshEntity(projectile);

    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(0.52F);
    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.SOUL_SPEAR_SPELL_USE_ID);
    }
}