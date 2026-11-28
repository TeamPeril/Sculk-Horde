package com.github.sculkhorde.systems;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

public class SculkPopulationSystem {

    Collection<ISculkSmartEntity> population = new ArrayList<>();
    Collection<SculkPhantomEntity> scoutingPhantomsPopulation = new ArrayList<>();

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
        return scoutingPhantomsPopulation.size();
    }

    public int getMaxScoutingPhantomsPopulation()
    {
        return 30;
    }

    public void trackScountingPhantom(SculkPhantomEntity entity) { scoutingPhantomsPopulation.add(entity); }

    public boolean isScoutingPhantomPopulationAtMax()
    {
        return getScoutingPhantomsPopulation() >= getMaxScoutingPhantomsPopulation();
    }

    public boolean isScoutingPhantomPopulationTooHigh()
    {
        return getScoutingPhantomsPopulation() > getMaxScoutingPhantomsPopulation();
    }

    public void updatePopulationCollection()
    {
        population.clear();
        scoutingPhantomsPopulation.clear();

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

                if(entity instanceof SculkPhantomEntity phantom && phantom.isScouter())
                {
                    scoutingPhantomsPopulation.add(phantom);
                }

                population.add((ISculkSmartEntity) entity);
            }
        });

        if(isPopulationAtMax()) {
            DebuggerSystem.entityDebuggerModule.logInfo("Sculk Horde has reached maximum population. Killing Idle Mobs");
            despawnIdleMobs();
        }

        if(isScoutingPhantomPopulationTooHigh())
        {
            cullOldestPhantoms();
        }
    }


    public void cullOldestPhantoms() {
        int amountToRemove = getScoutingPhantomsPopulation() - getMaxScoutingPhantomsPopulation();

        if (amountToRemove <= 0 || scoutingPhantomsPopulation.isEmpty()) {
            return;
        }

        DebuggerSystem.entityDebuggerModule.logInfo("Scouting Phantoms has reached maximum population. Culling " + amountToRemove + " excess scouting phantoms.");

        List<SculkPhantomEntity> toRemove = scoutingPhantomsPopulation.stream()
                .sorted(Comparator.comparingLong(SculkPhantomEntity::getTimeOfSpawn)) // Ascending: smallest timestamp = oldest
                .limit(amountToRemove)
                .toList();

        for (SculkPhantomEntity phantom : toRemove) {
            phantom.discard();
        }

        scoutingPhantomsPopulation.removeAll(toRemove);
    }

    public void despawnIdleMobs()
    {
        for(ISculkSmartEntity entity : population)
        {
            LivingEntity livingEntity = (LivingEntity) entity;
            Optional<RaidEvent> nearestRaid = EventSystem.getNearestRaidEvent((ServerLevel) livingEntity.level(), livingEntity.blockPosition());

            if(nearestRaid.isEmpty())
            {
                continue;
            }

            boolean isTooFarFromRaid = BlockAlgorithms.getBlockDistance(nearestRaid.get().getRaidLocation(), livingEntity.blockPosition()) > 300;

            if((entity.isIdle() && !entity.isParticipatingInRaid()) || (entity.isParticipatingInRaid() && isTooFarFromRaid))
            {
                livingEntity.discard();
                ModSavedData.getSaveData().addSculkAccumulatedMass((int) livingEntity.getHealth());
                SculkHorde.statisticsData.addTotalMassFromDespawns((int) livingEntity.getHealth());
            }
        }
    }

    public static void trySpawnScoutingPhantom(Level worldIn, BlockPos spawnPos, boolean force)
    {
        if(SculkHorde.populationHandler.isScoutingPhantomPopulationAtMax() && !force)
        {
            return;
        }

        SculkPhantomEntity phantom = ModEntities.SCULK_PHANTOM.get().create(worldIn);

        if(phantom == null) { return; }

        phantom.setScouter(true);
        phantom.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        phantom.spawnPoint = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        worldIn.addFreshEntity(phantom);
        SculkHorde.populationHandler.trackScountingPhantom(phantom);
        DebuggerSystem.entityDebuggerModule.logInfo("trySpawnScoutingPhantom | Spawned Scouting Phantom at " + spawnPos.toShortString());
    }
}
