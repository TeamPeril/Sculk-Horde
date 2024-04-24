package com.github.sculkhorde.core.gravemind;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
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
        if(currentTime - lastTimeOfPopulationRecount >= populationRecountInterval)
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
        return ModConfig.SERVER.maximum_sculk_population.get();
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

                if(entity instanceof SculkBeeHarvesterEntity)
                {
                    continue;
                }

                population.add((ISculkSmartEntity) entity);
            }
        });


        if(SculkHorde.isDebugMode() && isPopulationAtMax()) { SculkHorde.LOGGER.info("Sculk Horde has reached maximum population."); }
        if(SculkHorde.isDebugMode() && isPopulationAtMax()) { SculkHorde.LOGGER.info("Sculk Horde has calculated population to be " + population.size() + "."); }
    }
}

