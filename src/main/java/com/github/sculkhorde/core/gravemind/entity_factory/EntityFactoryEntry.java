package com.github.sculkhorde.core.gravemind.entity_factory;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

import javax.annotation.Nullable;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    public enum StrategicValues {Infector, Melee, Ranged, Boss, Support, Tank}

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
    public void setCost(int cost)
    {
        orderCost = cost;
    }

    public int getCost()
    {
        return orderCost;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    public int getLimit()
    {
        return limit;
    }

    public void addStrategicValues(StrategicValues... values)
    {
        strategicValues = values;
    }

    public StrategicValues[] getStrategicValues()
    {
        return strategicValues;
    }

    public StrategicValues getFirstStrategicValue()
    {
        return strategicValues[0];
    }

    public void setExplicitDeniedSenders(ReinforcementRequest.senderType... deniedSenders)
    {
        explicitDeniedSenders = deniedSenders;
    }

    public ReinforcementRequest.senderType[] getExplicitDeniedSenders()
    {
        return explicitDeniedSenders;
    }

    public void setMinEvolutionRequired(Gravemind.evolution_states minEvolutionRequired)
    {
        this.minEvolutionRequired = minEvolutionRequired;
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

    public boolean doesEntityContainAnyRequiredStrategicValues(StrategicValues[] requiredValues)
    {
        for(StrategicValues value : requiredValues)
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

        boolean isOverBudget = getCost() > context.budget;
        boolean doesHordeNotHaveEnoughMass = getCost() <= SculkHorde.savedData.getSculkAccumulatedMass();
        boolean isSenderExplicitlyDenied = isSenderExplicitlyDenied(context.sender);
        boolean isEvolutionStateNotMet = !SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired);
        boolean doesEntityNotContainAnyRequiredStrategicValues = !doesEntityContainAnyRequiredStrategicValues((StrategicValues[]) context.approvedMobTypes.toArray());

        if(doesHordeNotHaveEnoughMass || isOverBudget)
        {
            return false;
        }
        else if(doesEntityNotContainAnyRequiredStrategicValues && !context.approvedMobTypes.isEmpty())
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
