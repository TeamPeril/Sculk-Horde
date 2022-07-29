package com.github.sculkhoard.common.entity.goal;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

import static com.github.sculkhoard.common.entity.EntityAlgorithms.filterOutDoNotInteractMobs;
import static com.github.sculkhoard.common.entity.EntityAlgorithms.filterOutFriendlies;

public class NearestAttackableNonSculkTargetGoal<T extends LivingEntity> extends TargetGoal {

    //TODO: Update how this class works so that we can dynamically add and remove mobs from being targets.

    private final int ticksPerSecond = 20;
    private final int ticksIdleThreshold = 20;
    private int ticksSinceIdle = 0;
    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;
    boolean despawnWhenIdle = false;
    private boolean mustReach;

    public NearestAttackableNonSculkTargetGoal(MobEntity mobEntity, Class<T> targetClass, boolean mustSee) {
        this(mobEntity, targetClass, mustSee, false);
    }

    public NearestAttackableNonSculkTargetGoal(MobEntity mobEntity, Class<T> targetClass, boolean mustSee, boolean mustReach) {
        this(mobEntity, targetClass, 10, mustSee, mustReach, (Predicate<LivingEntity>)null);
    }

    public NearestAttackableNonSculkTargetGoal(MobEntity mobEntity, Class<T> targetClass, int interval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate) {
        super(mobEntity, mustSee, mustReach);
        this.mustReach = mustReach;
        this.targetType = targetClass;
        this.randomInterval = interval;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    public NearestAttackableNonSculkTargetGoal enableDespawnWhenIdle()
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



        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0)
        {
            return false;
        }
        else
        {
            this.findTarget();
            return this.target != null;
        }


    }

    protected AxisAlignedBB getTargetSearchArea(double range) {
        return this.mob.getBoundingBox().inflate(range, 4.0D, range);
    }

    protected void findTarget() {

        //TODO filtering should be done in one loop, not multiple loops

        //If targetType is not player, Get all possible targets, filter out sculk or infected mobs
        if (this.targetType != PlayerEntity.class && this.targetType != ServerPlayerEntity.class)
        {
            possibleTargets =
                    this.mob.level.getLoadedEntitiesOfClass(
                    this.targetType,
                    this.getTargetSearchArea(this.getFollowDistance()),
                    (Predicate<? super LivingEntity>) null);

            //Remove Any Sculk Entities or entities already infected
            filterOutFriendlies(possibleTargets);
            filterOutDoNotInteractMobs(possibleTargets);
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

        //Filter out targets we cannot see
        if(this.mustSee || this.mustReach)
        {
            for(int index = 0; index < possibleTargets.size(); index++)
            {
                //If must see and cant see, remove.
                if(this.mustSee && !this.mob.getSensing().canSee(possibleTargets.get(index)))
                {
                    possibleTargets.remove(possibleTargets.get(index));
                    index--;
                }
                //if must reach but cannot reach, remove
                else if(this.mustReach && !canReach(possibleTargets.get(index)))
                {
                    possibleTargets.remove(possibleTargets.get(index));
                    index--;
                }

            }
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

    public void start()
    {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity p_234054_1_) {
        this.target = p_234054_1_;
    }


    /**
     * Checks to see if this entity can find a short path to the given target.
     */
    private boolean canReach(LivingEntity pTarget)
    {
        Path path = this.mob.getNavigation().createPath(pTarget, 0);
        if (path == null)
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = path.getEndNode();
            if (pathpoint == null)
            {
                return false;
            }
            else
            {
                int i = pathpoint.x - MathHelper.floor(pTarget.getX());
                int j = pathpoint.z - MathHelper.floor(pTarget.getZ());
                return (double)(i * i + j * j) <= 2.25D;
            }
        }
    }

}
