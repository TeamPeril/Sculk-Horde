package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class NearestLivingEntityTargetGoal<T extends LivingEntity> extends TargetGoal {

    protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;

    public NearestLivingEntityTargetGoal(MobEntity mobEntity, boolean mustSee, boolean mustReach)
    {
        this(mobEntity, mustSee, mustReach, null);
    }

    public NearestLivingEntityTargetGoal(MobEntity mobEntity, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate)
    {
        super(mobEntity, mustSee, mustReach);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    /** Functionality **/
    @Override
    public boolean canUse()
    {

        boolean canWeUse = !((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
        // If the mob is already targeting something valid, don't bother
        return canWeUse;
    }

    protected AxisAlignedBB getTargetSearchArea(double range)
    {
        return this.mob.getBoundingBox().inflate(range, 4.0D, range);
    }

    protected void findTarget()
    {
        possibleTargets =
                this.mob.level.getLoadedEntitiesOfClass(
                LivingEntity.class,
                this.getTargetSearchArea(this.getFollowDistance()),
                        ((ISculkSmartEntity)this.mob).getTargetParameters().isPossibleNewTargetValid);

        // Use java removeif function to filter out non targets
        //possibleTargets.removeIf(e -> (!((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(e)));

        //If there is available targets
        if(possibleTargets.size() <= 0)
        {
            return;
        }

        LivingEntity closestLivingEntity = possibleTargets.get(0);

        //Return nearest Mob
        for(LivingEntity e : possibleTargets)
        {
            if(e.distanceTo(this.mob) < e.distanceTo(closestLivingEntity))
            {
                closestLivingEntity = e;
            }
        }
        setTargetMob(closestLivingEntity); //Return target

    }

    public void start()
    {
        this.findTarget();
        this.mob.setTarget(getTargetMob());
        super.start();
    }

    public void setTargetMob(@Nullable LivingEntity targetIn) {
        this.targetMob = targetIn;
    }

    public LivingEntity getTargetMob() {
        return this.targetMob;
    }

}
