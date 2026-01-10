package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;

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
        Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) getPathFinderMob().level(), getPathFinderMob().blockPosition());
        if(nearestRaid.isEmpty())
        {
            return false;
        }
        else if(hasReachedLocationOnce)
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

        // Only Squad leaders can lead to the raid
        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(getPathFinderMob());
        if(squad.isPresent() && squad.get().isLeader(getPathFinderMob().getUUID()))
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
            Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) getPathFinderMob().level(), getPathFinderMob().blockPosition());

            if(nearestRaid.isEmpty())
            {
                return;
            }

            getPathFinderMob().getNavigation().moveTo(nearestRaid.get().getObjectiveLocationVec3().x, nearestRaid.get().getObjectiveLocationVec3().y, nearestRaid.get().getObjectiveLocationVec3().z, 1D);
        }
    }

    private boolean isCloseEnoughToObjective()
    {
        Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) getPathFinderMob().level(), getPathFinderMob().blockPosition());

        if(nearestRaid.isEmpty())
        {
            return true;
        }

        if(nearestRaid.get().getObjectiveLocation().closerThan(getPathFinderMob().blockPosition(), 7))
        {
            hasReachedLocationOnce = true;
            return true;
        }

        return false;

    }
}
