package com.github.sculkhorde.datagen;

import com.github.sculkhorde.common.loot.AddItemModifier;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, SculkHorde.MOD_ID);
    }

    @Override
    protected void start() {
        add("sculk_ancient_city_chest", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(BuiltInLootTables.ANCIENT_CITY.location()).build(),
                LootItemRandomChanceCondition.randomChance(0.5F).build()
        }, ModItems.SCULK_RESIN.get()));

        add("sculk_ancient_city_chest", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(BuiltInLootTables.ANCIENT_CITY.location()).build(),
                LootItemRandomChanceCondition.randomChance(0.3F).build()
        }, ModItems.SOULITE_SHARD.get()));

        add("sculk_ancient_city_chest", new AddItemModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(BuiltInLootTables.ANCIENT_CITY.location()).build(),
                LootItemRandomChanceCondition.randomChance(0.4F).build()
        }, ModItems.ESSENCE_OF_PURITY.get()));
    }
}
