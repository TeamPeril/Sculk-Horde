package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.gravemind.RaidHandler;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

public class PathFindToRaidLocation<T extends ISculkSmartEntity> extends Goal {

    private final T mob;

    private boolean hasReachedLocationOnce = false;

    public PathFindToRaidLocation(T mobIn) {
        this.mob = mobIn;
    }

    private PathfinderMob getPathFinderMob()
    {
        return (PathfinderMob) this.mob;
    }

    private ISculkSmartEntity getSculkSmartEntity()
    {
        return (ISculkSmartEntity) this.mob;
    }

    public boolean canUse()
    {
        if(hasReachedLocationOnce)
        {
            return false;
        }
        else if(getPathFinderMob().isVehicle())
        {
            return false;
        }
        else if(!getSculkSmartEntity().canParticipatingInRaid())
        {
            return false;
        }
        else if(isCloseEnoughToObjective())
        {
            return false;
        }
         return true;
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void tick()
    {
        if (!getPathFinderMob().isPathFinding())
        {
            getPathFinderMob().getNavigation().moveTo(RaidHandler.raidData.getObjectiveLocationVec3().x, RaidHandler.raidData.getObjectiveLocationVec3().y, RaidHandler.raidData.getObjectiveLocationVec3().z, 1.5D);
        }
    }

    private boolean isCloseEnoughToObjective()
    {
        if(RaidHandler.raidData.getObjectiveLocation().closerThan(getPathFinderMob().blockPosition(), 7))
        {
            hasReachedLocationOnce = true;
            return true;
        }
        return false;
    }
}
