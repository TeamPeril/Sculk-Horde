package com.github.sculkhoard.common.entity.goal;

import com.github.sculkhoard.common.entity.EntityAlgorithms;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;

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
        this.mob = mob;
    }


    /**
     * Starts the attack Sequence<br>
     * We shouldn't have to check if the target is null since
     * the super class does this. However, something funky is going on that
     * causes a null pointer exception if we dont check this in tick(). I put
     * it here aswell just in case.
     */
    public void start()
    {
        if(this.mob.getTarget() != null && EntityAlgorithms.isLivingEntityInfected(this.mob.getTarget()))
        {
            super.start();
        }
    }

    /**
     * Stops the attack sequence.
     */
    public void stop()
    {
        super.stop();
    }

    /**
     * Gets called every tick the attack is active<br>
     * We shouldn't have to check if the target is null since
     * the super class does this. However, something funky is going on that
     * causes a null pointer exception if we dont check this here. This is
     * absolutely some sort of bug that I was unable to figure out. For the
     * time being (assuming I ever fix this), this will have to do.
     */
    public void tick()
    {
        SculkMiteEntity thisMob = this.mob;
        LivingEntity target = this.mob.getTarget();

        //If entity is null or infected already, do not pursue
        if(this.mob.getTarget() == null || EntityAlgorithms.isLivingEntityInfected(this.mob.getTarget()))
        {
            stop();
        }
        else
        {
            super.tick();
            //Calcualate distance between this mob and target mob in a 3D space
            double mobX = thisMob.getX();
            double mobY = thisMob.getY();
            double mobZ = thisMob.getZ();
            double targetX = target.getX();
            double targetY = target.getY();
            double targetZ = thisMob.getTarget().getZ();
            double distance = Math.sqrt(Math.pow(mobX-targetX, 2) + Math.pow(mobY-targetY, 2) + Math.pow(mobZ-targetZ, 2));
            if(distance <= thisMob.INFECT_RANGE && !(this.mob.level.isClientSide))
            {
                target.addEffect(new EffectInstance(thisMob.INFECT_EFFECT, thisMob.INFECT_DURATION, thisMob.INFECT_LEVEL));

                //Kill The Bastard
                /**
                 *  Note: <br>
                 *  Never call thisMob.die(). This is not meant to be used, but is a public method for whatever reason.
                 */
                //thisMob.die(DamageSource.GENERIC);
                thisMob.hurt(DamageSource.GENERIC, thisMob.getHealth());

            }
        }
    }
}
