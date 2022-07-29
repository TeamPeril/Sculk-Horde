package com.github.sculkhoard.common.entity.goal;

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

import static com.github.sculkhoard.common.entity.EntityAlgorithms.filterOutFriendlies;
import static com.github.sculkhoard.common.entity.EntityAlgorithms.filterOutNonHostiles;

public class NearestAttackableHostileTargetGoal<T extends LivingEntity> extends TargetGoal {

    //TODO: Update how this class works so that we can dynamically add and remove mobs from being targets.

    private final int ticksPerSecond = 20;
    private final int ticksIdleThreshold = 60;
    private int ticksSinceIdle = 0;
    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;
    boolean despawnWhenIdle = false;

    public NearestAttackableHostileTargetGoal(MobEntity mobEntity, Class<T> targetClass, boolean mustSee)
    {
        this(mobEntity, targetClass, mustSee, false);
    }

    public NearestAttackableHostileTargetGoal(MobEntity mobEntity, Class<T> targetClass, boolean mustSee, boolean mustReach)
    {
        this(mobEntity, targetClass, 10, mustSee, mustReach, (Predicate<LivingEntity>)null);
    }

    public NearestAttackableHostileTargetGoal(MobEntity mobEntity, Class<T> targetClass, int interval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate)
    {
        super(mobEntity, mustSee, mustReach);
        this.targetType = targetClass;
        this.randomInterval = interval;
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    public NearestAttackableHostileTargetGoal enableDespawnWhenIdle()
    {
        despawnWhenIdle = true;
        return this;
    }

    public boolean canUse()
    {
        /**
         * I shouldn't have to do this, but im doing this here.
         * I cannot figure out how vanilla handles this.
         * This targeting system is put together with tape and glue.
         */
        if(this.target != null && this.target.isDeadOrDying())
        {
            this.target = null;
        }

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


        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }


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

            //Remove Any Sculk Entities or entities already infected
            filterOutNonHostiles(possibleTargets);
        }
        else //if targetType is player
        {
            this.target = this.mob.level.getNearestPlayer(
                    this.targetConditions,
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            );
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
            this.target = closestLivingEntity; //Return target
        }
    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity p_234054_1_) {
        this.target = p_234054_1_;
    }

}
