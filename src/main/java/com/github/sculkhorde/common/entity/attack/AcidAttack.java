package com.github.sculkhorde.common.entity.attack;

import com.github.sculkhorde.common.entity.SculkLivingEntity;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

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
