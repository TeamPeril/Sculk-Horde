package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;

public class FollowSquadLeader extends Goal {
    private final ISculkSmartEntity sculkSmartEntity; // the skeleton mob
    private int timeToRecalcPath;

    private final int FOLLOW_RANGE = 7;

    public FollowSquadLeader(ISculkSmartEntity mob) {
        this.sculkSmartEntity = mob;
    }

    private Mob getMob() {
        return (Mob) this.sculkSmartEntity;
    }

    @Override
    public boolean canUse() {



        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(getMob());

        if(squad.isEmpty() || squad.get().getLeader().isEmpty() || squad.get().isLeader(getMob().getUUID()))
        {
            return false;
        }

        boolean doWeHaveTarget = getMob().getTarget() != null;
        boolean isBeingRidden = getMob().isVehicle();

        if (isBeingRidden || doWeHaveTarget || squad.get().isLeaderDead()) {
            return false;
        }

        if(EntityAlgorithms.getDistanceBetweenEntities(squad.get().getLeader().get(), getMob()) <= FOLLOW_RANGE)
        {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void tick()
    {
        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(getMob());

        if(squad.isEmpty() || squad.get().getLeader().isEmpty() || squad.get().isLeaderDead())
        {
            return;
        }

        if (EntityAlgorithms.getDistanceBetweenEntities(squad.get().getLeader().get(), getMob()) <= FOLLOW_RANGE) {
            getMob().getNavigation().stop();
        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(20);
            this.getMob().getNavigation().moveTo(squad.get().getLeader().get(), 1.0);
        }
    }
}