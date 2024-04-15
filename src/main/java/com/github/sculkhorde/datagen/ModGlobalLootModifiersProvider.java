package com.github.sculkhorde.datagen;

import com.github.sculkhorde.common.loot.AddItemModifier;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
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
        add("pine_cone_from_creeper", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(new ResourceLocation("entities/creeper")).build() }, ModItems.DEEP_GREEN_MUSIC_DISC.get()));

        add("deep_green_music_disc_from_ancient_city", new AddItemModifier(new LootItemCondition[]{
                new LootTableIdCondition.Builder(new ResourceLocation("chests/ancient_city")).build() }, ModItems.DEEP_GREEN_MUSIC_DISC.get()
        ));
    }
}
