package com.github.sculkhorde.compat;

import com.github.sculkhorde.common.recipe.SoulHarvestingRecipe;
import com.github.sculkhorde.common.screen.SoulHarvesterScreen;
import com.github.sculkhorde.core.SculkHorde;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.List;

@JeiPlugin
public class JEISculkHordePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(SculkHorde.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new SoulHarvestingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<SoulHarvestingRecipe> soulHarvestingRecipes = recipeManager.getAllRecipesFor(SoulHarvestingRecipe.Type.INSTANCE);
        registration.addRecipes(SoulHarvestingCategory.SOUL_HARVESTING_TYPE, soulHarvestingRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //registration.addRecipeClickArea(SoulHarvesterScreen.class, 60, 30, 20, 30,
                //SoulHarvestingCategory.SOUL_HARVESTING_TYPE);
    }
}
