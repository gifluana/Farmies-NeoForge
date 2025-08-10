package com.lunazstudios.farmies.recipe;

import com.lunazstudios.farmies.registry.FRecipes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record DehydratorRecipe(
        Ingredient inputItem,
        ItemStack outputItem
) implements Recipe<DehydratorRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(@NotNull DehydratorRecipeInput dehydratorRecipeInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return inputItem.test(dehydratorRecipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(DehydratorRecipeInput dehydratorRecipeInput, HolderLookup.Provider provider) {
        return outputItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputItem;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FRecipes.DEHYDRATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FRecipes.DEHYDRATOR_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DehydratorRecipe> {

        public static final MapCodec<DehydratorRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(DehydratorRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(DehydratorRecipe::outputItem)
        ).apply(inst, DehydratorRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DehydratorRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, DehydratorRecipe::inputItem,
                        ItemStack.STREAM_CODEC, DehydratorRecipe::outputItem,
                        DehydratorRecipe::new
                );

        @Override
        public MapCodec<DehydratorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DehydratorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
