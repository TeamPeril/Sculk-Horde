package com.github.sculkhoard.common.entity.entity_factory;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.entity.EntityType;

import javax.annotation.Nullable;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    private int orderCost = 0;
    private EntityType entity = null;
    public EntityFactory.StrategicValues strategicValue = EntityFactory.StrategicValues.Melee;
    public Gravemind.evolution_states minEvolutionRequired = Gravemind.evolution_states.Undeveloped;

    public EntityFactoryEntry(EntityType entity, int orderCost, EntityFactory.StrategicValues value, Gravemind.evolution_states minEvolution)
    {
        this.entity = entity;
        this.orderCost = orderCost;
        this.strategicValue = value;
        this.minEvolutionRequired = minEvolution;
    }

    public int getCost()
    {
        return orderCost;
    }

    @Nullable
    public EntityType getEntity()
    {
        return entity;
    }

    public boolean isEntryAppropriate(ReinforcementContext context)
    {
        if(context != null)
        {
            if((context.budget == -1 || context.budget >= orderCost)
                    && (context.approvedMobTypes.contains(strategicValue) || context.approvedMobTypes.isEmpty())
                    && SculkHoard.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired))
                return true;
        }
        else
            System.out.println("WARNING context is null");
        return false;
    }

}
