package com.github.sculkhorde.core;

import com.github.sculkhorde.common.recipe.SoulHarvestingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SculkHorde.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> SOUL_HARVESTING = SERIALIZERS.register("soul_harvesting", () -> SoulHarvestingRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
