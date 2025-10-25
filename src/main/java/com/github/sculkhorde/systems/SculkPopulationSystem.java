package com.github.sculkhorde.systems;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class SculkPopulationSystem {

    Collection<ISculkSmartEntity> population = new ArrayList<>();

    protected int scoutingPhantomsPopulation = 0;

    protected long lastTimeOfPopulationRecount = 0;
    protected int populationRecountInterval = TickUnits.convertSecondsToTicks(30);

    public SculkPopulationSystem()
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
        return SculkHorde.autoPerformanceSystem.getMaxSculkUnitPopulation();
    }

    public boolean isPopulationAtMax()
    {
        return population.size() >= getMaxPopulation();
    }

    public int getScoutingPhantomsPopulation()
    {
        return scoutingPhantomsPopulation;
    }

    public int getMaxScoutingPhantomsPopulation()
    {
        return 30;
    }

    public boolean isScoutingPhantomPopulationAtMax()
    {
        return getScoutingPhantomsPopulation() >= getMaxScoutingPhantomsPopulation();
    }

    public void updatePopulationCollection()
    {
        population.clear();
        scoutingPhantomsPopulation = 0;

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

                if(entity instanceof SculkPhantomEntity phantom)
                {
                    if(phantom.isScouter())
                    {
                        scoutingPhantomsPopulation += 1;
                    }
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
            Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) ((PathfinderMob)entity).level(), ((PathfinderMob)entity).blockPosition());
            boolean isTooFarFromRaid = BlockAlgorithms.getBlockDistance(nearestRaid.get().getRaidLocation(), ((PathfinderMob)entity).blockPosition()) > 300;

            if((entity.isIdle() && !entity.isParticipatingInRaid()) || (entity.isParticipatingInRaid() && isTooFarFromRaid))
            {
                ((LivingEntity) entity).discard();
                ModSavedData.getSaveData().addSculkAccumulatedMass((int) ((LivingEntity) entity).getHealth());
                SculkHorde.statisticsData.addTotalMassFromDespawns((int) ((LivingEntity) entity).getHealth());
            }
        }
    }
}
