package com.github.sculkhorde.common.entity.attack;

import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class AcidAttack extends RangedAttack{

    public AcidAttack(Mob thisMob) {
        super(thisMob);
    }

    /**
     * NOTE: FOR WHATEVER REASON THIS DOES NOT WORK CORRECTLY.
     * STILL SPAWNS DEFAULT CUSTOM ITEM PROJECTILE
     * @param worldIn The world to spawn this projectile in
     * @return
     */
    public SculkAcidicProjectileEntity getProjectile(Level worldIn, LivingEntity shooterIn, float damageIn) {
        return new SculkAcidicProjectileEntity(worldIn, shooterIn, damageIn);


    }
}
