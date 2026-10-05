package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        //client
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModBlockModelsProvider>) output -> new ModBlockModelsProvider(output, SculkHorde.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModBlockStateProvider>) output -> new ModBlockStateProvider(output, SculkHorde.MOD_ID, existingFileHelper));

        //server
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(
                output,
                ModBlocks.BLOCKS_TO_DATAGEN.stream().map(pair -> ResourceKey.create(Registries.LOOT_TABLE, pair.getA().getId().withPrefix("blocks/"))).collect(Collectors.toUnmodifiableSet()),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootTableSubProvider::new,
                        LootContextParamSets.BLOCK
                )),
                lookupProvider
        ));
    }
}
