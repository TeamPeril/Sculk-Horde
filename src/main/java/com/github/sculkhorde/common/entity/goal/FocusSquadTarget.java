package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

public class FocusSquadTarget extends TargetGoal {

    public FocusSquadTarget(Mob sourceEntity) {
        super(sourceEntity, true);
        this.setFlags(EnumSet.of(Flag.TARGET));
    }


    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    public boolean canUse()
    {
        Optional<UUID> squadUUID = SquadSystem.getSquadIdForMember(mob);

        if(squadUUID.isEmpty())
        {
            return false;
        }

        Optional<Squad> squad = SquadSystem.getSquad(squadUUID.get());

        if(squad.isEmpty())
        {
            return false;
        }

        if(squad.get().isLeader(mob.getUUID()))
        {
            return false;
        }

        return true;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start()
    {
        Optional<UUID> squadUUID = SquadSystem.getSquadIdForMember(mob);

        if(squadUUID.isEmpty())
        {
            return;
        }

        Optional<Squad> squad = SquadSystem.getSquad(squadUUID.get());

        if(squad.isEmpty())
        {
            return;
        }

        if(squad.get().isLeader(mob.getUUID()))
        {
            return;
        }

        this.mob.setTarget(squad.get().getSquadTarget());
    }

}
