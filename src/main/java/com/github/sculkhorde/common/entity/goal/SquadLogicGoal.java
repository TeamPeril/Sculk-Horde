package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.Optional;
import java.util.UUID;

public class SquadLogicGoal extends Goal {

    private final LivingEntity mob; // We use this to retrieve the mob that is using this goal.
    private long timeOfLastSquadUpdate = 0L;
    private final long SQUAD_UPDATE_DELAY = TickUnits.convertSecondsToTicks(5);
    protected int squadSearchAttempts = 0;
    protected final int MAX_SQUAD_SEARCHES = 5;

    public SquadLogicGoal(LivingEntity mob)
    {
        super();
        this.mob = mob;
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
        return !SculkHorde.gravemind.getEvolutionState().equals(Gravemind.evolution_states.Immature) || getMob().level().getGameTime() - timeOfLastSquadUpdate > SQUAD_UPDATE_DELAY;
    }

    @Override
    public void start()
    {
        timeOfLastSquadUpdate = getMob().level().getGameTime();
    }

    @Override
    public void tick() {
        super.tick();

        if(mob.isVehicle())
        {
            return;
        }

        if(SquadSystem.getSquadIdForMember(mob).isEmpty())
        {
            // Try to join squad, if this fails. Make one
            if(!tryToJoinNearBySquad())
            {
                squadSearchAttempts++;

                if(squadSearchAttempts >= MAX_SQUAD_SEARCHES)
                {
                    SquadSystem.createSquad(mob);
                    squadSearchAttempts = 0;
                }

            }
            else
            {
                squadSearchAttempts = 0;
            }
            return;
        }

        UUID squadUUID = SquadSystem.getSquadIdForMember(mob).get();
        Optional<Squad> squad = SquadSystem.getSquad(squadUUID);

        if(squad.isEmpty()) {
            return;
        }

        if(squad.get().isLeader(mob.getUUID()) && SculkHorde.isDebugMode())
        {
            MobEffectInstance effect = new MobEffectInstance(MobEffects.GLOWING, TickUnits.convertSecondsToTicks(10), 0, false, false);
            getMob().addEffect(effect);
        }
    }

    protected boolean tryToJoinNearBySquad()
    {
        Optional<Squad> squad = SquadSystem.getSquadNearPos(mob.blockPosition());
        if(squad.isPresent())
        {
            return squad.get().attemptAddMember(mob);
        }

        return false;
    }

    @Override
    public boolean canContinueToUse()
    {
        return false;
    }
}
