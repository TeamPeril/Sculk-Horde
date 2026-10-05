package com.github.sculkhorde.core;

import com.github.sculkhorde.common.recipe.SoulHarvestingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, SculkHorde.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, SoulHarvestingRecipe.Serializer> SOUL_HARVESTING = SERIALIZERS.register("soul_harvesting", () -> SoulHarvestingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
