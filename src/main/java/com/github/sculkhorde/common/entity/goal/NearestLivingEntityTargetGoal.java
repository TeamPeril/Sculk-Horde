package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.EntityAlgorithms.filterOutNonTargets;

public class NearestLivingEntityTargetGoal<T extends LivingEntity> extends TargetGoal {


    //flags to modify behavior
    private boolean targetHostiles = false; //Should we attack hostiles?
    private boolean targetPassives = false; //Should we target passives?
    private boolean targetInfected = false;//If a passive or hostile is infected, should we attack it?
    private boolean targetBelow50PercentHealth = true; //Should we target entities below 50% health?
    boolean despawnWhenIdle = false; //Should we despawn after not having a target for a while?

    private final int ticksPerSecond = 20;
    private final int ticksIdleThreshold = 60;
    private int ticksSinceIdle = 0;
    protected final Class<T> targetType;
    protected LivingEntity target;
    protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;

    public NearestLivingEntityTargetGoal(MobEntity mobEntity, boolean mustSee, boolean mustReach)
    {
        this(mobEntity, mustSee, mustReach, (Predicate<LivingEntity>)null);
    }

    public NearestLivingEntityTargetGoal(MobEntity mobEntity, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate)
    {
        super(mobEntity, mustSee, mustReach);
        this.targetType = (Class<T>) LivingEntity.class;
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    /** Options **/

    public NearestLivingEntityTargetGoal enableDespawnWhenIdle()
    {
        despawnWhenIdle = true;
        return this;
    }

    public NearestLivingEntityTargetGoal enableTargetHostiles()
    {
        targetHostiles = true;
        return this;
    }

    public NearestLivingEntityTargetGoal enableTargetPassives()
    {
        targetPassives = true;
        return this;
    }

    public NearestLivingEntityTargetGoal enableTargetInfected()
    {
        targetInfected = true;
        return this;
    }

    public NearestLivingEntityTargetGoal ignoreTargetBelow50PercentHealth()
    {
        targetBelow50PercentHealth = false;
        return this;
    }

    /** Functionality **/

    public boolean canUse()
    {

        //Despawn the mob if it has no target for too long
        if(despawnWhenIdle)
        {
            this.ticksSinceIdle++;
            //If mob is idle for too long, destroy it
            if(despawnWhenIdle && ticksSinceIdle >= ticksPerSecond * ticksIdleThreshold)
            {
                this.mob.remove();
            }
        }

        // If the mob is already targeting something valid, don't bother
        return !EntityAlgorithms.isLivingEntityValidTarget(this.target, targetHostiles, targetPassives, targetInfected, targetBelow50PercentHealth);
    }

    protected AxisAlignedBB getTargetSearchArea(double range)
    {
        return this.mob.getBoundingBox().inflate(range, 4.0D, range);
    }

    protected void findTarget()
    {

        //If targetType is not player, Get all possible targets, filter out sculk or infected mobs
        if (this.targetType != PlayerEntity.class && this.targetType != ServerPlayerEntity.class)
        {
            possibleTargets =
                    this.mob.level.getLoadedEntitiesOfClass(
                    this.targetType,
                    this.getTargetSearchArea(this.getFollowDistance()),
                    (Predicate<? super LivingEntity>) null);

            // Remove Any Sculk Entities or entities already infected
            filterOutNonTargets(possibleTargets, targetHostiles, targetPassives, targetInfected, targetBelow50PercentHealth);
        }
        else //if targetType is player
        {
            setTarget(this.mob.level.getNearestPlayer(
                    this.targetConditions,
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            ));
        }

        //If there is available targets
        if(possibleTargets.size() > 0)
        {
            this.ticksSinceIdle = 0;
            LivingEntity closestLivingEntity = possibleTargets.get(0);

            //Return nearest Mob
            for(LivingEntity e : possibleTargets)
            {
                if(e.distanceTo(this.mob) < e.distanceTo(closestLivingEntity))
                {
                    closestLivingEntity = e;
                }
            }
            setTarget(closestLivingEntity); //Return target
        }
    }

    public void start()
    {
        this.findTarget();
        this.mob.setTarget(this.target);
        super.start();
    }

    @Override
    public void stop()
    {
        // I have to comment out these lines of code because there is some weird bug
        // that when this gets set to null, it causes the look at goal to crash.
        // NullPointerException at
        // net.minecraft.entity.ai.controller.LookController.setLookAt(LookController.java:30)
        //
        // Has something to do with the attack goal, because the attack goal calls the look at
        // goal. I don't know why this is happening, but I'm going to leave it commented out
        // Theoretically I could fix this by adding a null check to attack functions, but
        // vanilla minecraft doesnt even do this. So wtf why does it only work with vanilla.g

        // this.target = null;
        // this.mob.setTarget(null);
    }

    public void setTarget(@Nullable LivingEntity targetIn) {
        this.target = targetIn;
    }

}
