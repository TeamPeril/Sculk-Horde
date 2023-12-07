package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.util.SquadHandler;
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

        boolean squadDoesntExist = !SquadHandler.doesSquadExist(((ISculkSmartEntity)this.mob).getSquad());
        boolean isSquadLeader = sculkSmartEntity.getSquad().isSquadLeader();
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
        SquadHandler squad = ((ISculkSmartEntity)this.mob).getSquad();
        if(squad == null)
        {
            return;
        }
        boolean doesSquadExist = SquadHandler.doesSquadExist(squad);
        boolean isSquadLeader = squad.isSquadLeader();
        boolean isSquadLeaderNullOrDead = squad.isSquadLeaderDead();
        if(!doesSquadExist || isSquadLeader || isSquadLeaderNullOrDead)
        {
            return;
        }

        this.mob.setTarget(squad.getSquadTarget());
    }

}
