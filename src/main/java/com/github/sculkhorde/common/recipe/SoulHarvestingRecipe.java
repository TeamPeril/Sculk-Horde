package com.github.sculkhorde.common.recipe;

import com.github.sculkhorde.core.SculkHorde;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SoulHarvestingRecipe implements Recipe<SimpleContainer> {

    private final NonNullList<Ingredient> inputItems;
    private final ItemStack output;
    private final ResourceLocation id;
    private final int healthRequired;

    public SoulHarvestingRecipe(NonNullList<Ingredient> inputItems, ItemStack outout, ResourceLocation id, int healthRequired) {
        this.inputItems = inputItems;
        this.output = outout;
        this.id = id;
        this.healthRequired = healthRequired;
    }

    @Override
    public boolean matches(SimpleContainer containerIn, Level levelIn) {
        if(levelIn.isClientSide()) { return false; }

        return inputItems.get(0).test(containerIn.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return inputItems;
    }

    @Override
    public ItemStack assemble(SimpleContainer p_44001_, RegistryAccess p_267165_) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return output.copy();
    }

    public ResourceLocation getId() {
        return id;
    }

    public int getHealthRequired() {
        return healthRequired;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;

    }

    public static class Type implements RecipeType<SoulHarvestingRecipe> {
        public static final Type INSTANCE = new Type();

        public static final String ID = "sculkhorde:soul_harvesting";
    }

    public static class Serializer implements RecipeSerializer<SoulHarvestingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "soul_harvesting");


        @Override
        public SoulHarvestingRecipe fromJson(ResourceLocation recipeIDIn, JsonObject serializedRecipeIn) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(serializedRecipeIn, "output"));

            JsonArray ingredients = GsonHelper.getAsJsonArray(serializedRecipeIn, "ingredients");
            NonNullList<Ingredient> inputItems = NonNullList.withSize(1, Ingredient.EMPTY);

            for(int i = 0; i < ingredients.size(); i++) {
                inputItems.set(i, Ingredient.fromJson(ingredients.get(i)));
            }

            int healthRequired = GsonHelper.getAsInt(serializedRecipeIn, "healthRequired", 0);

            return new SoulHarvestingRecipe(inputItems, output, recipeIDIn, healthRequired);
        }

        @Override
        public @Nullable SoulHarvestingRecipe fromNetwork(ResourceLocation recipeIDIn, FriendlyByteBuf bufferIn) {
            NonNullList<Ingredient> inputItems = NonNullList.withSize(bufferIn.readInt(), Ingredient.EMPTY);

            for (int i = 0; i < inputItems.size(); i++) {
                inputItems.set(i, Ingredient.fromNetwork(bufferIn));
            }

            ItemStack output = bufferIn.readItem();

            int healthRequired = bufferIn.readInt();

            // Log the size of the data being read
            System.out.println("SoulHarvesterRecipe | Data size being read: " + bufferIn.readableBytes());

            return new SoulHarvestingRecipe(inputItems, output, recipeIDIn, healthRequired);
        }

        @Override
        public void toNetwork(FriendlyByteBuf bufferIn, SoulHarvestingRecipe recipeIn) {
            int initialIndex = bufferIn.writerIndex(); // Capture starting position
            bufferIn.writeInt(recipeIn.inputItems.size());

            for (Ingredient ingredient : recipeIn.getIngredients()) {
                ingredient.toNetwork(bufferIn);
            }

            // --- CRITICAL FIX ---
            // Copy of the output stack.
            ItemStack clientOutput = recipeIn.getResultItem(null);

            // ONLY KEEP NBT DATA THAT THE CLIENT NEEDS.
            // If the client doesn't need any NBT, clear it to prevent the crash.
            if (clientOutput.hasTag()) {
                // Option A: Clear all NBT (safest)
                clientOutput = clientOutput.copy();
                clientOutput.setTag(null);
            }

            // 3. Write the (now smaller) ItemStack to the network.
            bufferIn.writeItemStack(clientOutput, false);

            bufferIn.writeInt(recipeIn.getHealthRequired());

            // Log the size of the data being written
            System.out.printf("SoulHarvestingRecipe | Wrote recipe '%s'. Total bytes: %d, Starting index: %d%n",
                    recipeIn.getId().toString(),
                    bufferIn.writerIndex() - initialIndex,
                    initialIndex);
        }
    }
}
