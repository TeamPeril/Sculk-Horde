package com.github.sculkhorde.common.entity.goal;

import java.util.EnumSet;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkMiteEntity;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public class SculkMiteInfectGoal extends MeleeAttackGoal {

    private final SculkMiteEntity mob;

    /**
     * The Constructor
     * @param mob The mob that called this
     * @param speedModifier How fast can they attack?
     * @param followTargetIfNotSeen Should the mob follow their target if they cant see them.
     */
    public SculkMiteInfectGoal(SculkMiteEntity mob, double speedModifier, boolean followTargetIfNotSeen) {
        super(mob, speedModifier, followTargetIfNotSeen);
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.mob = mob;
    }

    @Override
    public boolean canUse()
    {
        boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
        // If the mob is already targeting something valid, don't bother
        return canWeUse;
    }

    @Override
    public boolean canContinueToUse()
    {
        return canUse();
    }

    /**
     * Gets called every tick the attack is active<br>
     * We shouldn't have to check if the target is null since
     * the super class does this. However, something funky is going on that
     * causes a null pointer exception if we dont check this here. This is
     * absolutely some sort of bug that I was unable to figure out. For the
     * time being (assuming I ever fix this), this will have to do.
     */
    @Override
    public void tick()
    {
        if(!canContinueToUse())
        {
            return;
        }

        SculkMiteEntity thisMob = this.mob;
        LivingEntity target = this.mob.getTarget();
        super.tick();
        //Calcualate distance between this mob and target mob in a 3D space
        double mobX = thisMob.getX();
        double mobY = thisMob.getY();
        double mobZ = thisMob.getZ();
        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = thisMob.getTarget().getZ();
        double distance = Math.sqrt(Math.pow(mobX-targetX, 2) + Math.pow(mobY-targetY, 2) + Math.pow(mobZ-targetZ, 2));
        //If in infect range & not client side & current mob health is less than or equal to 50% of max health
        if(distance <= SculkMiteEntity.INFECT_RANGE && !(this.mob.level.isClientSide))
        {
            float targetMobRemainingHealth = target.getHealth() / target.getMaxHealth();
            if(targetMobRemainingHealth <= 0.5 && !target.hasEffect(SculkMiteEntity.INFECT_EFFECT))
            {
                target.addEffect(new MobEffectInstance(SculkMiteEntity.INFECT_EFFECT, SculkMiteEntity.INFECT_DURATION, SculkMiteEntity.INFECT_LEVEL));

                //Kill The Bastard
                /**
                 *  Note:
                 *  Never call thisMob.die(). This is not meant to be used, but is a public method for whatever reason.
                 */
                //thisMob.die(DamageSource.GENERIC);
                thisMob.hurt(thisMob.damageSources().generic(), thisMob.getHealth());
            }
        }
    }
}
