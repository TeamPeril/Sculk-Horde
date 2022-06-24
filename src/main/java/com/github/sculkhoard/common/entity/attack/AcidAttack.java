package com.github.sculkhoard.common.entity.attack;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhoard.common.entity.projectile.SculkAcidicProjectileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import static java.lang.Math.random;

public class AcidAttack extends RangedAttack{

    public AcidAttack(SculkLivingEntity thisMob) {
        super(thisMob);
    }

    /**
     * NOTE: FOR WHATEVER REASON THIS DOES NOT WORK CORRECTLY.
     * STILL SPAWNS DEFAULT CUSTOM ITEM PROJECTILE
     * @param worldIn The world to spawn this projectile in
     * @return
     */
    public SculkAcidicProjectileEntity getProjectile(World worldIn, LivingEntity shooterIn, float damageIn) {
        return new SculkAcidicProjectileEntity(worldIn, shooterIn, damageIn);

    }
}
