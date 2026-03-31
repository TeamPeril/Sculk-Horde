package com.github.sculkhorde.systems.event_system.events;


import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.SculkPopulationSystem;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;
import java.util.Random;

public class SpawnPhantomsEvent extends Event {


    public SpawnPhantomsEvent(ResourceKey<net.minecraft.world.level.Level> dimension) {
        super(dimension);
    }

    private void spawnScoutPhantoms(int amount)
    {
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        Optional<BlockPos> largestSpaceOrigin = BlockAlgorithms.getLargestAreaAboveBlock(getDimension(), getEventLocation());

        if(largestSpaceOrigin.isEmpty())
        {
            for(int i = 0; i < amount; i++)
            {
                int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
                int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
                int y = getDimension().getMaxBuildHeight();
                BlockPos spawnPosition = new BlockPos(getEventLocation().getX() + x, y, getEventLocation().getZ() + z);

                SculkPopulationSystem.trySpawnScoutingPhantom(getDimension(), spawnPosition);
            }
            return;
        }

        for(int i = 0; i < amount; i++)
        {
            SculkPopulationSystem.trySpawnScoutingPhantom(getDimension(), largestSpaceOrigin.get());
        }

    }

    @Override
    public boolean canStart() {

        if(!super.canStart())
        {
            return false;
        }

        if(SculkHorde.populationHandler.isScoutingPhantomPopulationAtMax())
        {
            return false;
        }

        return true;
    }

    @Override
    public void start()
    {
        super.start();
        if (ModConfig.SERVER.should_sculk_nodes_and_raids_spawn_phantoms.get()) {
        	spawnScoutPhantoms(10);
        }
    }
}
