package com.github.sculkhoard.common.entity.attack;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhoard.common.item.CustomItemProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import static java.lang.Math.random;

public abstract class RangedAttack {

    public SculkLivingEntity thisMob; //The mob calling this attack
    public double xOffSetModifier = 2;
    public double entityHeightFraction = 0.5;
    public double zOffSetModifier = 2;
    public float damage = 1;
    public double accuracy = 0.95;

    public RangedAttack(SculkLivingEntity parentEntity) {
        this.thisMob = parentEntity;
    }

    public RangedAttack(SculkLivingEntity parentEntity, double xOffSetModifier, double entityHeightFraction,
                                double zOffSetModifier, float damage) {
        this.thisMob = parentEntity;
        this.xOffSetModifier = xOffSetModifier;
        this.entityHeightFraction = entityHeightFraction;
        this.zOffSetModifier = zOffSetModifier;
        this.damage = damage;
    }

    public abstract CustomItemProjectileEntity getProjectile(World worldIn, LivingEntity shooterIn, float damageIn);

    public RangedAttack setProjectileOriginOffset(double x, double entityHeightFraction, double z) {
        xOffSetModifier = x;
        this.entityHeightFraction = entityHeightFraction;
        zOffSetModifier = z;
        return this;
    }

    public RangedAttack setDamage(float damage) {
        this.damage = damage;
        return this;
    }

    public RangedAttack setAccuracy(double accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public double rollAccuracy(double directional) {
        return directional + (1.0D - accuracy) * directional * this.thisMob.getRandom().nextGaussian();
    }

    public void shoot()
    {
        LivingEntity targetEntity = this.thisMob.getTarget();
        CustomItemProjectileEntity projectile = getProjectile(this.thisMob.level, this.thisMob, this.damage);

        //Math Stuff
        double d0 = (targetEntity.getEyeY()- targetEntity.getY()) / 2 + targetEntity.getY();
        double d1 = targetEntity.getX() - this.thisMob.getX();
        double d2 = d0 - projectile.getY();
        double d3 = targetEntity.getZ() - this.thisMob.getZ();
        float f = MathHelper.sqrt(d1 * d1 + d3 * d3) * 0.2F;

        //Create and shoot projectile
        projectile.shoot(d1, d2 + (double)f, d3, 1.6F, 12.0F);
        float rng = (float) random();
        this.thisMob.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.4F / (rng * 0.4F + 0.8F));
        this.thisMob.level.addFreshEntity(projectile);

        //Shoot in a straight line if projectile is not affected by gravity

        /*
        LivingEntity targetEntity = this.thisMob.getTarget(); //Get the target
        World world = this.thisMob.getCommandSenderWorld(); //get the world
        Vector3d vector3d = this.thisMob.getViewVector(1.0F); //Calculate the pov vector

        //Math Stuff
        double d2 = targetEntity.getX() - (this.thisMob.getX() + vector3d.x * xOffSetModifier);
        double d3 = targetEntity.getY(0.5D) - (this.thisMob.getY(entityHeightFraction));
        double d4 = targetEntity.getZ() - (this.thisMob.getZ() + vector3d.z * zOffSetModifier);

        //Create projectile
        ProjectileEntity projectile = getProjectile(world, rollAccuracy(d2), rollAccuracy(d3), rollAccuracy(d4));
        projectile.setPos(this.thisMob.getX() + vector3d.x * xOffSetModifier,
                this.thisMob.getY(entityHeightFraction), this.thisMob.getZ() + vector3d.z * zOffSetModifier);
        world.addFreshEntity(projectile);
         */

    }
}
