package com.github.sculkhorde.compat;

import com.github.sculkhorde.common.recipe.SoulHarvestingRecipe;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.SculkHorde;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class SoulHarvestingCategory implements IRecipeCategory<SoulHarvestingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(SculkHorde.MOD_ID, "soul_harvesting");
    public static final ResourceLocation TEXTURE = new ResourceLocation(SculkHorde.MOD_ID,
            "textures/gui/soul_harvester_gui.png");

    public static final RecipeType<SoulHarvestingRecipe> SOUL_HARVESTING_TYPE =
            new RecipeType<>(UID, SoulHarvestingRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public SoulHarvestingCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 83);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.SOUL_HARVESTER_BLOCK.get()));
    }

    @Override
    public RecipeType<SoulHarvestingRecipe> getRecipeType() {
        return SOUL_HARVESTING_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.sculkhorde.soul_harvester");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SoulHarvestingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 11).addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 59).addItemStack(recipe.getResultItem(null));
    }
}
