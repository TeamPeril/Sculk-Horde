package com.github.sculkhoard.common.entity.attack;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

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

    public abstract ProjectileEntity getProjectile(World world, double d2, double d3, double d4);

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

    public void shoot() {
        System.out.println("Attempting to shoot");
        LivingEntity targetEntity = this.thisMob.getTarget();
        World world = this.thisMob.getCommandSenderWorld();
        Vector3d vector3d = this.thisMob.getViewVector(1.0F);
        double d2 = targetEntity.getX() - (this.thisMob.getX() + vector3d.x * xOffSetModifier);
        double d3 = targetEntity.getY(0.5D) - (this.thisMob.getY(entityHeightFraction));
        double d4 = targetEntity.getZ() - (this.thisMob.getZ() + vector3d.z * zOffSetModifier);
        ProjectileEntity projectile = getProjectile(world, rollAccuracy(d2), rollAccuracy(d3), rollAccuracy(d4));
        projectile.setPos(this.thisMob.getX() + vector3d.x * xOffSetModifier,
                this.thisMob.getY(entityHeightFraction), this.thisMob.getZ() + vector3d.z * zOffSetModifier);
        world.addFreshEntity(projectile);
    }
}
