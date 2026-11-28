package com.github.sculkhorde.systems.gravemind_system.entity_factory;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.util.DifficultyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class is only used in the EntityFactory class which stores a list
 * of these entries. It is simply to store an EntityType and how much
 * sculk mass is required to spawn it.
 */
public class EntityFactoryEntry {

    Random rng = new Random();

    public enum StrategicValues {Combat, Infector, Melee, Ranged, Boss, Support, Tank, EffectiveInSkies, Aquatic, EffectiveOnGround}

    protected int orderCost;
    protected EntityType<Mob> entity;
    protected int squad_limit = Integer.MAX_VALUE;

    protected float chanceToSpawn = 1.0F;

    protected StrategicValues[] strategicValues = new StrategicValues[]{};

    protected ReinforcementRequest.senderType explicitDeniedSenders[] = new ReinforcementRequest.senderType[]{};
    protected Gravemind.evolution_states minEvolutionRequired = Gravemind.evolution_states.Undeveloped;

    boolean experimentalMode = false;
    protected ModConfigSpec.ConfigValue<Boolean> requiredConfig = ModConfig.SERVER.experimental_features_enabled;

    public EntityFactoryEntry(EntityType entity)
    {
        this.entity = entity;
    }

    public EntityType<Mob> getEntity()
    {
        return entity;
    }

    protected Difficulty minimumDifficulty = Difficulty.EASY;

    // Getters and Setters
    public EntityFactoryEntry setCost(int cost)
    {
        orderCost = cost;
        return this;
    }


    public boolean meetsRequiredDifficulty()
    {
        return DifficultyUtil.isCurrentDifficultyEqualToOrGreaterThan(minimumDifficulty);
    }


    public int getCost()
    {
        return orderCost;
    }

    public boolean getChanceToSpawn()
    {
        return rng.nextFloat() <= chanceToSpawn;
    }

    public EntityFactoryEntry setMinimumDifficulty(Difficulty difficulty)
    {
        minimumDifficulty = difficulty;
        return this;
    }

    public EntityFactoryEntry setChanceToSpawn(float value)
    {
        chanceToSpawn = value;
        return this;
    }

    public EntityFactoryEntry setLimit(int limit)
    {
        this.squad_limit = limit;
        return this;
    }

    public EntityFactoryEntry enableExperimentalMode(ModConfigSpec.ConfigValue<Boolean> configOptionThatNeedsToBeTrue)
    {
        experimentalMode = true;
        requiredConfig = configOptionThatNeedsToBeTrue;
        return this;
    }

    public int getLimit()
    {
        return squad_limit;
    }

    public EntityFactoryEntry addStrategicValues(StrategicValues... values)
    {
        strategicValues = values;
        return this;
    }


    public EntityFactoryEntry setExplicitlyDeniedSenders(ReinforcementRequest.senderType... deniedSenders)
    {
        explicitDeniedSenders = deniedSenders;
        return this;
    }


    public EntityFactoryEntry setMinEvolutionRequired(Gravemind.evolution_states minEvolutionRequired)
    {
        this.minEvolutionRequired = minEvolutionRequired;
        return this;
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
        if(getCost() > ModSavedData.getSaveData().getSculkAccumulatedMass())
        {
            return false;
        }
        else if(!SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired))
        {
            return false;
        }
        else if(!getChanceToSpawn())
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

    public boolean containsAll(ArrayList<StrategicValues> requiredValues)
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

    public boolean containsAny(ArrayList<StrategicValues>  deniedValues)
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
        boolean doesHordeNotHaveEnoughMass = getCost() >= ModSavedData.getSaveData().getSculkAccumulatedMass();
        boolean isSenderExplicitlyDenied = isSenderExplicitlyDenied(context.sender);
        boolean isEvolutionStateNotMet = !SculkHorde.gravemind.isEvolutionStateEqualOrLessThanCurrent(minEvolutionRequired);
        boolean doesEntityNotContainNeededStrategicValues = !containsAll(context.approvedStrategicValues);
        boolean doesEntityContainBannedStrategicValues = containsAny(context.deniedStrategicValues);
        boolean doesRequestSpecifyAnyApprovedMobTypes = !context.approvedStrategicValues.isEmpty();

        if(doesHordeNotHaveEnoughMass || isOverBudget)
        {
            return false;
        }
        else if(experimentalMode && (!ModConfig.SERVER.experimental_features_enabled.get() || !requiredConfig.get()))
        {
            return false;
        }
        else if(doesEntityNotContainNeededStrategicValues && doesRequestSpecifyAnyApprovedMobTypes)
        {
            return false;
        }
        else if(doesEntityContainBannedStrategicValues)
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
        else if(!getChanceToSpawn())
        {
            return false;
        }
        else if(!meetsRequiredDifficulty())
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
        ModSavedData.getSaveData().subtractSculkAccumulatedMass(getCost());
        SculkHorde.statisticsData.incrementTotalUnitsSpawned();
        return getEntity().spawn(level, pos, MobSpawnType.EVENT);
    }
}
