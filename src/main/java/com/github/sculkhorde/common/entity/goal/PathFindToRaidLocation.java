package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class PathFindToRaidLocation<T extends ISculkSmartEntity> extends Goal {

    private final T mob;

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
        if(getPathFinderMob().isVehicle())
        {
            return false;
        }
        else if(!getSculkSmartEntity().canParticipatingInRaid())
        {
            return false;
        }
        else if(isInRadius())
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
            getPathFinderMob().getNavigation().moveTo(SculkHorde.raidHandler.getObjectiveLocationVec3().x, SculkHorde.raidHandler.getObjectiveLocationVec3().y, SculkHorde.raidHandler.getObjectiveLocationVec3().z, 1.5D);
        }
    }

    private boolean isInRadius() {
        return SculkHorde.raidHandler.getObjectiveLocation().closerThan(getPathFinderMob().blockPosition(), 10);
    }
}
