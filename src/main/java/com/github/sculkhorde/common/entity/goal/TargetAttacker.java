package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class TargetAttacker extends TargetGoal {

    private static final TargetingConditions HURT_BY_TARGETING = (TargetingConditions.forCombat()).ignoreInvisibilityTesting();
    private boolean alertSameType;
    /** Store the previous revengeTimer value */
    private int timestamp;
    private final Class<?>[] toIgnoreDamage;
    private Class<?>[] toIgnoreAlert;

    public TargetAttacker(PathfinderMob sourceEntity, Class<?>... p_i50317_2_) {
        super(sourceEntity, true);
        this.toIgnoreDamage = p_i50317_2_;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public TargetAttacker setAlertAllies(Class<?>... pReinforcementTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = pReinforcementTypes;
        return this;
    }


    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse() {
        int i = this.mob.getLastHurtByMobTimestamp(); //Get the timestamp of when we were last attacked
        LivingEntity livingentity = this.mob.getLastHurtByMob(); //Get the mob that last attacked us

        //Do not allow this behavior to execute if what attacked us was a sculk mob
        if(EntityAlgorithms.isSculkLivingEntity.test(livingentity)) {return false;}

        //if ??? and living entity is not null
        if (i != this.timestamp && livingentity != null)
        {
            //If the thing that attacked us was the player and universal anger is enabled.
            if (livingentity.getType() == EntityType.PLAYER && this.mob.level.getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER))
            {
                return false;
            }
            else
            {
                //If we are told to ignore damage.
                for(Class<?> oclass : this.toIgnoreDamage)
                {
                    if (oclass.isAssignableFrom(livingentity.getClass()))
                    {
                        return false;
                    }
                }

                return this.canAttack(livingentity, HURT_BY_TARGETING);
            }
        }
        else
        {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start()
    {
        this.mob.setTarget(this.mob.getLastHurtByMob());
        this.targetMob = this.mob.getTarget();
        this.timestamp = this.mob.getLastHurtByMobTimestamp();
        this.unseenMemoryTicks = 60;

        //If a mob isnt already a confirmed hostile, make it one.
        if(EntityAlgorithms.isSculkLivingEntity.test(this.mob.getLastHurtByMob()) == false
                && this.mob.getLastHurtByMob() != null)
        {
            //TODO PORT THIS TO THE NEW MEMORY SYSTEM
            //SculkHorde.gravemind.getGravemindMemory().addHostileToMemory(mob.getLastHurtByMob(), (ServerLevel) this.mob.level);
        }

        if (this.alertSameType) {
            this.alertSculkLivingEntities();
        }

        super.start();
    }

    protected void alertSculkLivingEntities()
    {
        boolean DEBUG_THIS = false;
        double d0 = this.getFollowDistance();
        AABB axisalignedbb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(d0, 10.0D, d0);
        List<Mob> list = this.mob.level.getEntitiesOfClass(Mob.class, axisalignedbb);
        Iterator iterator = list.iterator();

        while(true)
        {
            Mob mobentity;

            while(true)
            {
                //Exit if we reach end of list
                if (!iterator.hasNext())
                {
                    return;
                }

                mobentity = (Mob)iterator.next();//Get Next Mob

                boolean isAlertingSelf = this.mob == mobentity;
                boolean hasTargetAlready = mobentity.getTarget() != null;
                boolean isProtector = EntityAlgorithms.isSculkLivingEntity.test(mobentity);

                if(DEBUG_THIS)
                {
                    System.out.println("Attempting to Call Protectors");
                    System.out.println("[ isAlertingSelf? = " + isAlertingSelf
                            + " hasTargetAlready? =" + hasTargetAlready
                            + " isProtector? =" + isProtector + "]");
                }

                //If we arent trying to alert ourself & if protectors dont already have a target
                if (!isAlertingSelf && !hasTargetAlready && isProtector)
                {
                    this.alertOther(mobentity, this.mob.getLastHurtByMob());
                }
            }
        }
    }

    protected void alertOther(Mob pMob, LivingEntity pTarget) {
        pMob.setTarget(pTarget);
    }
}
