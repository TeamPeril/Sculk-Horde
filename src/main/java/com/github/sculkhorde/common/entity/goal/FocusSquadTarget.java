package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.squad_system.Squad;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

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
        ISculkSmartEntity sculkSmartEntity = (ISculkSmartEntity) this.mob;

        boolean squadDoesntExist = !Squad.doesSquadExist(((ISculkSmartEntity)this.mob).getSquad());
        boolean isSquadLeader = sculkSmartEntity.getSquad().isLeader();
        if(squadDoesntExist || isSquadLeader)
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
        Squad squad = ((ISculkSmartEntity)this.mob).getSquad();
        if(squad == null)
        {
            return;
        }
        boolean doesSquadExist = Squad.doesSquadExist(squad);
        boolean isSquadLeader = squad.isLeader();
        boolean isSquadLeaderNullOrDead = squad.isLeaderDead();
        if(!doesSquadExist || isSquadLeader || isSquadLeaderNullOrDead)
        {
            return;
        }

        this.mob.setTarget(squad.getSquadTarget());
    }

}
