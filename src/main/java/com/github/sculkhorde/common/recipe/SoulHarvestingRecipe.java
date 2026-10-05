package com.github.sculkhorde.common.recipe;

import com.github.sculkhorde.core.ModRecipes;
import com.github.sculkhorde.core.SculkHorde;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class SoulHarvestingRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;
    private final int healthRequired;

    public SoulHarvestingRecipe(NonNullList<Ingredient> inputItems, ItemStack output, ResourceLocation id, int healthRequired) {
        this.inputItems = inputItems;
        this.output = output;
        this.id = id;
        this.healthRequired = healthRequired;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return !level.isClientSide() && !inputItems.isEmpty() && input.size() > 0 && inputItems.get(0).test(input.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return inputItems;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getHealthRequired() {
        return healthRequired;
    }

    public static class Type implements RecipeType<SoulHarvestingRecipe> {
        public static final Type INSTANCE = new Type();
        public static final String ID = SculkHorde.MOD_ID + ":soul_harvesting";
    }

    public static class Serializer implements RecipeSerializer<SoulHarvestingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "soul_harvesting");

        private static final MapCodec<SoulHarvestingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> recipe.inputItems),
                ItemStack.CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                com.mojang.serialization.Codec.INT.optionalFieldOf("healthRequired", 0)
                        .forGetter(recipe -> recipe.healthRequired)
        ).apply(instance, (ingredients, output, healthRequired) -> {
            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
            for (int i = 0; i < ingredients.size(); i++) {
                inputs.set(i, ingredients.get(i));
            }
            return new SoulHarvestingRecipe(inputs, output, ID, healthRequired);
        }));

        private static final StreamCodec<RegistryFriendlyByteBuf, SoulHarvestingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.collection(NonNullList::createWithCapacity, Ingredient.CONTENTS_STREAM_CODEC),
                        recipe -> recipe.inputItems,
                        ItemStack.STREAM_CODEC,
                        recipe -> recipe.output,
                        ByteBufCodecs.VAR_INT,
                        recipe -> recipe.healthRequired,
                        (ingredients, output, healthRequired) -> {
                            NonNullList<Ingredient> inputs = NonNullList.withSize(ingredients.size(), Ingredient.EMPTY);
                            for (int i = 0; i < ingredients.size(); i++) {
                                inputs.set(i, ingredients.get(i));
                            }
                            return new SoulHarvestingRecipe(inputs, output, ID, healthRequired);
                        });

        @Override
        public MapCodec<SoulHarvestingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SoulHarvestingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
