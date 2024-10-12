package com.github.sculkhorde.datagen;

import com.github.sculkhorde.common.loot.AddItemModifier;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output) {
        super(output, SculkHorde.MOD_ID);
    }

    @Override
    protected void start() {
        add("deep_green_music_disc_from_ancient_city", new AddItemModifier(new LootItemCondition[]{
                new AllOfCondition.Builder(
                        new LootTableIdCondition.Builder(new ResourceLocation("chests/ancient_city")),
                        LootItemRandomChanceCondition.randomChance(0.1F)
                ).build()
        }, ModItems.DEEP_GREEN_MUSIC_DISC.get()));
    }
}
