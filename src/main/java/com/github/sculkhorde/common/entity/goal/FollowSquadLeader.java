package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class FollowSquadLeader extends Goal {
    private final ISculkSmartEntity sculkSmartEntity; // the skeleton mob
    private int timeToRecalcPath;

    private final int FOLLOW_RANGE = 20;

    public FollowSquadLeader(ISculkSmartEntity mob) {
        this.sculkSmartEntity = mob;
    }

    private Mob getMob() {
        return (Mob) this.sculkSmartEntity;
    }

    @Override
    public boolean canUse() {

        // check if the entity is riding something
        boolean isRiding = getMob().isPassenger();
        boolean isSquadLeaderNull = sculkSmartEntity.getSquad().leaderUUID.isEmpty();
        boolean isSquadNull = sculkSmartEntity.getSquad() == null;
        boolean isSquadLeaderDead = sculkSmartEntity.getSquad().isLeaderDead();
        boolean areWeTheSquadLeader = sculkSmartEntity.getSquad().isLeader();
        boolean doWeHaveTarget = getMob().getTarget() != null;

        if (isRiding || isSquadLeaderNull || isSquadNull || isSquadLeaderDead || areWeTheSquadLeader || doWeHaveTarget) {
            return false;
        }

        if(!sculkSmartEntity.getSquad().isLeaderDead() && getMob().distanceToSqr((Entity) sculkSmartEntity.getSquad().leaderUUID.get()) < FOLLOW_RANGE)
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
    public void tick() {
        boolean isSquadNull = sculkSmartEntity.getSquad() == null;
        boolean isSquadLeaderNull = sculkSmartEntity.getSquad().leaderUUID.isEmpty();

        if(isSquadNull || isSquadLeaderNull)
        {
            return;
        }

        if (getMob().distanceToSqr((Entity) sculkSmartEntity.getSquad().leaderUUID.get()) < FOLLOW_RANGE) {
            // stop the navigation
            getMob().getNavigation().stop();

        }

        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(20);
            this.getMob().getNavigation().moveTo((Entity) sculkSmartEntity.getSquad().leaderUUID.get(), 1.0);
        }
    }
}