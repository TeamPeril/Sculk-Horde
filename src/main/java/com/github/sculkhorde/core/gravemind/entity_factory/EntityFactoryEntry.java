package com.github.sculkhorde.core.gravemind.entity_factory;

import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import java.util.ArrayList;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    public enum StrategicValues {Combat, Infector, Melee, Ranged, Boss, Support, Tank, EffectiveInSkies, Aquatic, EffectiveOnGround}

    private int orderCost = 0;
    private EntityType entity = null;
    private int limit = Integer.MAX_VALUE; // The limit of how many of this entity can be spawned
    private  StrategicValues[] strategicValues = new StrategicValues[]{};

    private ReinforcementRequest.senderType explicitDeniedSenders[] = new ReinforcementRequest.senderType[]{};
    private Gravemind.evolution_states minEvolutionRequired = Gravemind.evolution_states.Undeveloped;

    public EntityFactoryEntry(EntityType entity)
    {
        this.entity = entity;
    }

    public EntityType<Mob> getEntity()
    {
        return entity;
    }

    // Getters and Setters
    public EntityFactoryEntry setCost(int cost)
    {
        orderCost = cost;
        return this;
    }

    public int getCost()
    {
        return orderCost;
    }

    public EntityFactoryEntry setLimit(int limit)
    {
        this.limit = limit;
        return this;
    }

    public int getLimit()
    {
        return limit;
    }

    public EntityFactoryEntry addStrategicValues(StrategicValues... values)
    {
        strategicValues = values;
        return this;
    }

    public StrategicValues[] getStrategicValues()
    {
        return strategicValues;
    }

    public StrategicValues getFirstStrategicValue()
    {
        return strategicValues[0];
    }

    public EntityFactoryEntry setExplicitlyDeniedSenders(ReinforcementRequest.senderType... deniedSenders)
    {
        explicitDeniedSenders = deniedSenders;
        return this;
    }

    public ReinforcementRequest.senderType[] getExplicitDeniedSenders()
    {
        return explicitDeniedSenders;
    }

    public EntityFactoryEntry setMinEvolutionRequired(Gravemind.evolution_states minEvolutionRequired)
    {
        this.minEvolutionRequired = minEvolutionRequired;
        return this;
    }

    public Gravemind.evolution_states getMinEvolutionRequired()
    {
        return minEvolutionRequired;
    }

    public boolean isSenderExplicitlyDenied(ReinforcementRequest.senderType sender)
    {
        for(ReinforcementRequest.senderType deniedSender : explicitDeniedSenders)
        {
            if(deniedSender == sender)
            {
                return true;
            }
        }

        return false;
    }

    public boolean isEntryAppropriateMinimalCheck()
    {
        if(getCost() > SculkHorde.savedData.getSculkAccumulatedMass())
        {
            return false;
        }
        else if(!SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired))
        {
            return false;
        }

        return true;
    }

    public boolean doesEntityContainNeededStrategicValue(StrategicValues requiredValue)
    {
        for(StrategicValues entityValue : strategicValues)
        {
            if(entityValue == requiredValue)
            {
                return true;
            }
        }

        return false;
    }

    public boolean doesEntityContainNeededStrategicValues(ArrayList<StrategicValues> requiredValues)
    {
        int amountOfValuesNeeded = requiredValues.size();
        int amountOfValuesUnitHasFromRequirement = 0;

        for(StrategicValues value : requiredValues)
        {
            for(StrategicValues entityValue : strategicValues)
            {
                if(entityValue == value)
                {
                    amountOfValuesUnitHasFromRequirement++;
                }
            }
        }

        return amountOfValuesNeeded == amountOfValuesUnitHasFromRequirement;
    }

    public boolean doesEntityContainAnyDeniedStrategicValues(StrategicValues[] deniedValues)
    {
        for(StrategicValues value : deniedValues)
        {
            for(StrategicValues entityValue : strategicValues)
            {
                if(entityValue == value)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isEntryAppropriate(ReinforcementRequest context)
    {
        if(context == null)
        {
            return false;
        }

        boolean isOverBudget = getCost() > context.budget && context.budget != -1;
        boolean doesHordeNotHaveEnoughMass = getCost() >= SculkHorde.savedData.getSculkAccumulatedMass();
        boolean isSenderExplicitlyDenied = isSenderExplicitlyDenied(context.sender);
        boolean isEvolutionStateNotMet = !SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired);
        boolean doesEntityNotContainNeededStrategicValues = !doesEntityContainNeededStrategicValues(context.approvedStrategicValues);
        boolean doesRequestSpecifyAnyApprovedMobTypes = !context.approvedStrategicValues.isEmpty();

        if(doesHordeNotHaveEnoughMass || isOverBudget)
        {
            return false;
        }
        else if(doesEntityNotContainNeededStrategicValues && doesRequestSpecifyAnyApprovedMobTypes)
        {
            return false;
        }
        else if(isEvolutionStateNotMet)
        {
            return false;
        }
        else if(isSenderExplicitlyDenied)
        {
            return false;
        }

        return true;
    }

    /**
     * Will spawn entity and subtract the cost of spawning it.
     * @param level The level to spawn the entity in
     * @param pos The position to spawn the entity at
     */
    public Mob spawnEntity(ServerLevel level, BlockPos pos)
    {
        SculkHorde.savedData.subtractSculkAccumulatedMass(getCost());
        SculkHorde.statisticsData.incrementTotalUnitsSpawned();
        return getEntity().spawn(level, pos, MobSpawnType.EVENT);
    }
}
