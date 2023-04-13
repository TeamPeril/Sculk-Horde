package com.github.sculkhorde.common.world.gen;

import com.github.sculkhorde.common.entity.SculkMiteEntity;
import com.github.sculkhorde.core.EntityRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.Arrays;
import java.util.List;

public class ModEntityGen {
    /*
    public static void onEntitySpawn(final BiomeLoadingEvent event) {

        addEntityToAllBiomesExceptThese(
                event,
                EntityRegistry.SCULK_ZOMBIE.get(),
                SculkZombieEntity.SPAWN_WEIGHT,
                SculkZombieEntity.SPAWN_MIN,
                SculkZombieEntity.SPAWN_MAX,
                Biomes.OCEAN,
                Biomes.COLD_OCEAN);

        addEntityToAllBiomesExceptThese(
                event,
                EntityRegistry.SCULK_MITE,
                SculkMiteEntity.SPAWN_WEIGHT,
                SculkMiteEntity.SPAWN_MIN,
                SculkMiteEntity.SPAWN_MAX,
                Biomes.OCEAN,
                Biomes.COLD_OCEAN);


    }

    private static void addEntityToAllBiomesExceptThese(BiomeLoadingEvent event, EntityType<?> type,
                                                        int weight, int minCount, int maxCount, ResourceKey<Biome>... biomes) {
        // Goes through each entry in the biomes and sees if it matches the current biome we are loading
        boolean isBiomeSelected = Arrays.stream(biomes).map(ResourceKey::location)
                .map(Object::toString).anyMatch(s -> s.equals(event.getName().toString()));

        if(!isBiomeSelected) {
            addEntityToAllBiomes(event.getSpawns(), type, weight, minCount, maxCount);
        }
    }

    private static void addEntityToSpecificBiomes(BiomeLoadingEvent event, EntityType<?> type,
                                                  int weight, int minCount, int maxCount, ResourceKey<Biome>... biomes) {
        // Goes through each entry in the biomes and sees if it matches the current biome we are loading
        boolean isBiomeSelected = Arrays.stream(biomes).map(ResourceKey::location)
                .map(Object::toString).anyMatch(s -> s.equals(event.getName().toString()));

        if(isBiomeSelected) {
            addEntityToAllBiomes(event.getSpawns(), type, weight, minCount, maxCount);
        }
    }

    private static void addEntityToAllBiomes(MobSpawnInfoBuilder spawns, EntityType<?> type,
                                             int weight, int minCount, int maxCount) {
        List<MobSpawnSettings.SpawnerData> base = spawns.getSpawner(type.getCategory());
        base.add(new MobSpawnSettings.SpawnerData(type,weight, minCount, maxCount));
    }
    */
}
