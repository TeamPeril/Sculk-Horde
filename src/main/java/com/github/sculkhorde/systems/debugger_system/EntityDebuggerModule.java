package com.github.sculkhorde.systems.debugger_system;

import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDebuggerModule extends DebuggerModule {

    public EntityDebuggerModule()
    {
        super();
    }

    List<Mob> entitiesToDebug = new ArrayList<>();
    List<Mob> entitiesToRemoveFromDebugging = new ArrayList<>();
    Map<Mob, LivingEntity> lastKnownTargets = new HashMap<>();

    @Override
    public void serverTick() {
        super.serverTick();
        if(isDebuggingEnabled())
        {
            for(Mob mob : entitiesToDebug)
            {
                if(!mob.isAlive() || mob.isRemoved())
                {
                    entitiesToRemoveFromDebugging.add(mob);
                    continue;
                }
                setMobNameToGoals(mob);
                setMobGlowing(mob);
                checkAndPrintTargetChange(mob);
            }

            if(!entitiesToRemoveFromDebugging.isEmpty()) {
                for (Mob mob : entitiesToRemoveFromDebugging) {
                    cleanMob(mob);
                    entitiesToDebug.remove(mob);
                }
                entitiesToRemoveFromDebugging.clear();
            }
        }
        else if(!entitiesToDebug.isEmpty())
        {
            for(Mob mob : entitiesToDebug)
            {
                cleanMob(mob);
            }
            entitiesToDebug.clear();
        }
    }


    public void addMobToDebug(Mob mob)
    {
        if(!isMobBeingDebugged(mob))
        {
            entitiesToDebug.add(mob);
            setMobGlowing(mob);
        }
    }

    public void removeMobFromDebug(Mob mob)
    {
        if(isMobBeingDebugged(mob))
        {
            entitiesToDebug.remove(mob);
            cleanMob(mob);
        }
    }

    public boolean isMobBeingDebugged(Mob mob)
    {
        return entitiesToDebug.contains(mob);
    }

    public void checkAndPrintTargetChange(Mob mob)
    {
        LivingEntity currentTarget = mob.getTarget();
        LivingEntity lastTarget = lastKnownTargets.get(mob);

        if(currentTarget != lastTarget)
        {
            String mobName = mob.getClass().getSimpleName();
            String targetName = currentTarget == null ? "None" : currentTarget.getClass().getSimpleName();
            Component message = Component.literal("Debugger | Entity: " + mobName + " Target Changed To -> " + targetName);
            mob.level().players().forEach(player -> player.sendSystemMessage(message));
            lastKnownTargets.put(mob, currentTarget);
        }
    }

    public void setMobNameToGoals(Mob mob)
    {
        String customDebugName = "";
        for(WrappedGoal wrappedGoal : mob.goalSelector.getAvailableGoals().stream().filter(WrappedGoal::isRunning).toList())
        {
            Goal goal = wrappedGoal.getGoal();
            if(goal instanceof IDebuggableGoal debugGoal)
            {
                customDebugName += debugGoal.getGoalName().get();
            }
            else
            {
                customDebugName += goal.getClass().getSimpleName();
            }
            customDebugName += " | ";
        }

        mob.setCustomName(Component.literal(customDebugName));
    }

    public void setMobGlowing(Mob mob)
    {
        if(!mob.hasEffect(MobEffects.GLOWING))
        {
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
        }
    }

    public void cleanMob(Mob mob)
    {
        mob.setCustomName(null);
        mob.removeEffect(MobEffects.GLOWING);
        lastKnownTargets.remove(mob);
    }
}
