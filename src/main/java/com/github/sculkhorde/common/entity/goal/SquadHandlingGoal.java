package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class SquadHandlingGoal extends Goal {

    private final ISculkSmartEntity mob; // We use this to retrieve the mob that is using this goal.
    private long timeOfLastSquadUpdate = 0L;
    private final long SQUAD_UPDATE_DELAY = TickUnits.convertSecondsToTicks(5);

    public SquadHandlingGoal(ISculkSmartEntity mob)
    {
        super();
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    public Mob getMob()
    {
        return (Mob) this.mob;
    }


    /**
     * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
     * method as well.
     */
    @Override
    public boolean canUse()
    {
        return getMob().level().getGameTime() - timeOfLastSquadUpdate > SQUAD_UPDATE_DELAY;
    }

    @Override
    public void start()
    {
        timeOfLastSquadUpdate = getMob().level().getGameTime();
    }

    @Override
    public void tick() {
        super.tick();

        SquadHandler squad = mob.getSquad();

        if(SquadHandler.doesSquadExist(squad))
        {
            if(squad.isSquadLeaderDead())
            {
                squad.disbandSquad();
            }

            if(squad.isSquadLeader())
            {
                MobEffectInstance effect = new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertSecondsToTicks(10), 0, false, false);
                getMob().addEffect(effect);
            }

            return;
        }

        // If we fail to join a squad, create one and make us leader.
        if(!tryToJoinNearBySquad())
        {
            squad.createSquad();
        }
    }

    protected boolean tryToJoinNearBySquad()
    {
        AABB boundingBox = getMob().getBoundingBox().inflate(32.0D, 8.0D, 32.0D);
        // Get list of mobs in range
        List<? extends Mob> list = getMob().level().getEntitiesOfClass(Mob.class, boundingBox);


        // Early exit if list is empty
        if (list.isEmpty()) {
            return false;
        }

        // Use streams to filter out non-iSculkSmartEntities that are squad leaders, and exclude this mob.
        Mob bestMob = list.stream()
                .filter(mob -> mob instanceof ISculkSmartEntity && ((ISculkSmartEntity) mob).getSquad() != null && ((ISculkSmartEntity) mob).getSquad().isSquadLeader() && mob != getMob())
                .min(Comparator.comparingDouble(getMob()::distanceToSqr))
                .orElse(null);

        if(bestMob == null)
        {
            return false;
        }

        return ((ISculkSmartEntity) bestMob).getSquad().tryToAcceptMemberIntoSquad(mob);

    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
