package com.github.sculkhoard.common.entity.attack;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.common.entity.projectile.AcidBallEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;

public class AcidAttack extends RangedAttack{


    public AcidAttack(SculkLivingEntity thisMob, double xOffSetModifier,
                      double entityHeightFraction, double zOffSetModifier, float damage) {
        super(thisMob, xOffSetModifier, entityHeightFraction, zOffSetModifier, damage);
    }

    public AcidAttack(SculkLivingEntity thisMob) {
        super(thisMob);
    }

    @Override
    public ProjectileEntity getProjectile(World world, double d2, double d3, double d4) {
        return new AcidBallEntity(world, this.thisMob, d2, d3, d4, damage);

    }
}
