package com.github.sculkhorde.common.entity.attack;

import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AcidAttack extends RangedAttack{

    public AcidAttack(SculkSpitterEntity thisMob) {
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

    @Override
    public void triggerAttackAnimation()
    {
        // TODO PORT TO 1.19.2
        //((SculkSpitterEntity)thisMob).triggerAnim("attack_controller", "attack_animation");
    }
}
