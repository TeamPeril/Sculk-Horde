package com.github.sculkhorde.core.gravemind.entity_factory;

import javax.annotation.Nullable;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    private int orderCost = 0;
    private EntityType entity = null;
    private int limit = Integer.MAX_VALUE; // The limit of how many of this entity can be spawned
    public EntityFactory.StrategicValues strategicValue = EntityFactory.StrategicValues.Melee;
    public Gravemind.evolution_states minEvolutionRequired = Gravemind.evolution_states.Undeveloped;

    public EntityFactoryEntry(EntityType entity, int orderCost, EntityFactory.StrategicValues value, Gravemind.evolution_states minEvolution)
    {
        this.entity = entity;
        this.orderCost = orderCost;
        this.strategicValue = value;
        this.minEvolutionRequired = minEvolution;
    }

    // Getters and Setters

    /**
     * Sets the cost of spawning this entity
     * @return cost
     */
    public int getCost()
    {
        return orderCost;
    }

    /**
     * Sets the limit of how many of this entity can be spawned
     * @param limit The limit of how many of this entity can be spawned
     */
    public void setLimit(int limit)
    {
        this.limit = limit;
    }

    /**
     * Returns the limit of how many of this entity can be spawned
     * @return limit
     */
    public int getLimit()
    {
        return limit;
    }


    /**
     * Returns the entity type
     * @return entity
     */
    @Nullable
    public EntityType getEntity()
    {
        return entity;
    }

    /**
     * Returns the strategic value of this entity
     * @return strategicValue
     */
    public EntityFactory.StrategicValues getCategory()
    {
        return strategicValue;
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

    public boolean isEntryAppropriate(ReinforcementRequest context)
    {
        if(context == null)
        {
            return false;
        }
        else if(context.budget != -1 && context.budget < orderCost)
        {
            return false;
        }
        else if(!context.approvedMobTypes.contains(getCategory()) && !context.approvedMobTypes.isEmpty())
        {
            return false;
        }
        else if(!SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired))
        {
            return false;
        }
        // These sculk spore spewers get spammed to hell if they spawn in sculk masses
        else if(context.sender == ReinforcementRequest.senderType.SculkMass && getEntity() == ModEntities.SCULK_SPORE_SPEWER.get())
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
        return (Mob) getEntity().spawn(level, pos, MobSpawnType.EVENT);
    }
}
