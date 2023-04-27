package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.gravemind.RaidHandler;
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

    public boolean canUse() {
        return getPathFinderMob().getTarget() == null && !getPathFinderMob().isVehicle() && getSculkSmartEntity().canParticipatingInRaid();
    }

    public boolean canContinueToUse() {
        return canUse();
    }

    public void tick()
    {
        if (!getPathFinderMob().isPathFinding())
        {
            Vec3 vec3 = DefaultRandomPos.getPosTowards(getPathFinderMob(), 15, 4, RaidHandler.getRaidLocationVec3(), (double)((float)Math.PI / 2F));
            if (vec3 != null) {
                getPathFinderMob().getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0D);
            }
        }
    }
}
