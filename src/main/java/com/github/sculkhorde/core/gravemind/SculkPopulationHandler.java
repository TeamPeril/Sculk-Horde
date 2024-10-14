package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;

public class SculkPopulationHandler {

    Collection<ISculkSmartEntity> population = new ArrayList<>();

    private long lastTimeOfPopulationRecount = 0;
    private int populationRecountInterval = TickUnits.convertSecondsToTicks(30);

    public SculkPopulationHandler()
    {

    }


    public void serverTick()
    {
        long currentTime = ServerLifecycleHooks.getCurrentServer().overworld().getGameTime();

        // I saw a weird bug where the lastTimeOfPopulationRecount was bigger than currentTime. No Idea why.
        // Therefore I will use math.abs
        if(Math.abs(currentTime - lastTimeOfPopulationRecount) >= populationRecountInterval)
        {
            lastTimeOfPopulationRecount = currentTime;
            updatePopulationCollection();
        }
    }

    public int getPopulationSize()
    {
        return population.size();
    }

    public int getMaxPopulation()
    {
        return ModConfig.SERVER.max_unit_population.get();
    }

    public boolean isPopulationAtMax()
    {
        return population.size() >= getMaxPopulation();
    }

    public void updatePopulationCollection()
    {
        population.clear();

        ServerLifecycleHooks.getCurrentServer().getAllLevels().forEach( level -> {
            Iterable<Entity> listOfEntities = level.getEntities().getAll();

            for(Entity entity : listOfEntities)
            {
                if(! (entity instanceof LivingEntity))
                {
                    continue;
                }

                if(!EntityAlgorithms.isSculkLivingEntity.test((LivingEntity) entity))
                {
                    continue;
                }

                if(entity instanceof SculkBeeHarvesterEntity || entity instanceof SculkPhantomCorpseEntity)
                {
                    continue;
                }

                population.add((ISculkSmartEntity) entity);
            }
        });


        if(SculkHorde.isDebugMode() && isPopulationAtMax()) { SculkHorde.LOGGER.info("Sculk Horde has reached maximum population. Killing Idle Mobs"); }

        if(isPopulationAtMax()) { despawnIdleMobs(); }
    }

    public void despawnIdleMobs()
    {
        for(ISculkSmartEntity entity : population)
        {
            // We don't want raid entities being killed if raid is active.
            if(entity.isIdle() && (!entity.isParticipatingInRaid() && !SculkHorde.raidHandler.isRaidInactive()))
            {
                ((LivingEntity) entity).discard();
                SculkHorde.savedData.addSculkAccumulatedMass((int) ((LivingEntity) entity).getHealth());
            }
        }
    }
}

