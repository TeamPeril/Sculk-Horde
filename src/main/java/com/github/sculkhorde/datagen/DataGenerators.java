package com.github.sculkhorde.datagen;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        SculkHorde.LOGGER.info("gatherData");

        //client
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModBlockModelsProvider>) output -> new ModBlockModelsProvider(output, SculkHorde.MOD_ID, existingFileHelper));
        generator.addProvider(event.includeClient(), (DataProvider.Factory<ModBlockStateProvider>) output -> new ModBlockStateProvider(output, SculkHorde.MOD_ID, existingFileHelper));
        //generator.addProvider(event.includeClient(), (DataProvider.Factory<ModItemModelsProvider>) output -> new ModItemModelsProvider(output, SculkHorde.MOD_ID, existingFileHelper));

        //server
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput));
        generator.addProvider(event.includeServer(), (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(
                output,
                ModBlocks.BLOCKS_TO_DATAGEN.stream().map(pair -> pair.getA().getId().withPrefix("blocks/")).collect(Collectors.toUnmodifiableSet()),
                List.of(new LootTableProvider.SubProviderEntry(
                        ModBlockLootTableSubProvider::new,
                        LootContextParamSets.BLOCK
                ))
        ));
    }
}
