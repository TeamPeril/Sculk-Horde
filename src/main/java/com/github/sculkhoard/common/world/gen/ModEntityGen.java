package com.github.sculkhoard.common.world.gen;

import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.world.BiomeLoadingEvent;

import java.util.Arrays;
import java.util.List;

public class ModEntityGen {

    public static void onEntitySpawn(final BiomeLoadingEvent event) {
        addEntityToAllBiomesExceptThese(
                event,
                EntityRegistry.SCULK_ZOMBIE.get(),
                SculkZombieEntity.SPAWN_WEIGHT,
                SculkZombieEntity.SPAWN_MIN,
                SculkZombieEntity.SPAWN_MAX,
                Biomes.OCEAN,
                Biomes.COLD_OCEAN);
    }

    private static void addEntityToAllBiomesExceptThese(BiomeLoadingEvent event, EntityType<?> type,
                                                        int weight, int minCount, int maxCount, RegistryKey<Biome>... biomes) {
        // Goes through each entry in the biomes and sees if it matches the current biome we are loading
        boolean isBiomeSelected = Arrays.stream(biomes).map(RegistryKey::location)
                .map(Object::toString).anyMatch(s -> s.equals(event.getName().toString()));

        if(!isBiomeSelected) {
            addEntityToAllBiomes(event.getSpawns(), type, weight, minCount, maxCount);
        }
    }

    private static void addEntityToSpecificBiomes(BiomeLoadingEvent event, EntityType<?> type,
                                                  int weight, int minCount, int maxCount, RegistryKey<Biome>... biomes) {
        // Goes through each entry in the biomes and sees if it matches the current biome we are loading
        boolean isBiomeSelected = Arrays.stream(biomes).map(RegistryKey::location)
                .map(Object::toString).anyMatch(s -> s.equals(event.getName().toString()));

        if(isBiomeSelected) {
            addEntityToAllBiomes(event.getSpawns(), type, weight, minCount, maxCount);
        }
    }

    private static void addEntityToAllBiomes(MobSpawnInfoBuilder spawns, EntityType<?> type,
                                             int weight, int minCount, int maxCount) {
        List<MobSpawnInfo.Spawners> base = spawns.getSpawner(type.getCategory());
        base.add(new MobSpawnInfo.Spawners(type,weight, minCount, maxCount));
    }
}
