package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class SculkHordeTargetGoal<T extends LivingEntity> extends net.minecraft.world.entity.ai.goal.target.TargetGoal {

    protected long lastTimeSinceTargetSearch = 0;
    protected long targetSearchInterval = TickUnits.convertSecondsToTicks(2);

    public SculkHordeTargetGoal(Mob mobEntity)
    {
        super(mobEntity, false, false);
        this.setFlags(EnumSet.of(Flag.TARGET));
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

        TargetParameters params = ((ISculkSmartEntity)this.mob).getTargetParameters();

        // If we don't have a primary target, we can use this goal to find one.
        if (this.mob.getTarget() == null)
        {
            return true;
        }

        // If our current target is invalid, we can use this goal to find a new one.
        if (!params.isEntityValidSculkHordeTarget(this.mob.getTarget()))
        {
            return true;
        }

        // If we have available slots for secondary targets, we can use this goal to find them.
        if (params.getTargetStack().hasSecondarySlotsAvailable())
        {
            return true;
        }

        return false;
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

        TargetParameters params = ((ISculkSmartEntity)this.mob).getTargetParameters();

        List<LivingEntity> possibleTargets =
                this.mob.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getTargetSearchArea(this.getFollowDistance()),
                        params.isPossibleNewTargetValid);

        // If there are no available targets
        if(possibleTargets.isEmpty())
        {
            return;
        }

        // Sort by distance
        possibleTargets.sort((e1, e2) -> Double.compare(this.mob.distanceToSqr(e1), this.mob.distanceToSqr(e2)));

        for (LivingEntity target : possibleTargets)
        {
            // If the primary target is null or invalid, set this as the primary target
            if (this.mob.getTarget() == null || !params.isEntityValidSculkHordeTarget(this.mob.getTarget()))
            {
                this.mob.setTarget(target);
            }
            // Otherwise, try to add it as a secondary target if we have slots
            else if (params.getTargetStack().hasSecondarySlotsAvailable())
            {
                params.addSecondaryTarget(target);
            }
            else
            {
                // We've filled our slots or can't add more
                break;
            }
        }
    }

    public void start()
    {
        this.findTarget();
        super.start();
    }


}
