package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SculkHordeTargetGoal<T extends LivingEntity> extends net.minecraft.world.entity.ai.goal.target.TargetGoal {

    //protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;

    long lastTimeSinceTargetSearch = 0;
    long targetSearchInterval = TickUnits.convertSecondsToTicks(2);

    public SculkHordeTargetGoal(Mob mobEntity, boolean mustSee, boolean mustReach)
    {
        this(mobEntity, false, false, null);
    }

    public SculkHordeTargetGoal(Mob mobEntity, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> predicate)
    {
        super(mobEntity, false, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
        //this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(predicate);
    }

    /** Functionality **/
    @Override
    public boolean canUse()
    {
        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(mob);
        if(squad.isPresent()) {

            boolean isLeaderOfSquad = squad.get().isLeader(mob.getUUID());
            if (!isLeaderOfSquad) {
                return false;
            }
        }

        boolean canWeUse = !((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
        // If the mob is already targeting something valid, don't bother
        return canWeUse;
    }

    protected AABB getTargetSearchArea(double range)
    {
        return this.mob.getBoundingBox().inflate(range, this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), range);
    }

    protected void findTarget()
    {
        if(this.mob.level().getGameTime() - lastTimeSinceTargetSearch < targetSearchInterval)
        {
            return;
        }

        lastTimeSinceTargetSearch = this.mob.level().getGameTime();

        possibleTargets =
                this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getTargetSearchArea(this.getFollowDistance()),
                        ((ISculkSmartEntity)this.mob).getTargetParameters().isPossibleNewTargetValid);

        //If there is available targets
        if(possibleTargets.size() <= 0)
        {
            return;
        }

        LivingEntity closestLivingEntity = possibleTargets.get(0);

        //Return nearest Mob
        for(LivingEntity e : possibleTargets)
        {
            if(e.distanceTo(this.mob) < closestLivingEntity.distanceTo(this.mob))
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
