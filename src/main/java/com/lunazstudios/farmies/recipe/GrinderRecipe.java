package com.lunazstudios.farmies.recipe;

import com.lunazstudios.farmies.registry.FRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record GrinderRecipe(
        Ingredient inputItem,
        ItemStack outputItem,
        ItemStack extraOutput,
        float extraChance
) implements Recipe<GrinderRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);
        return list;
    }

    @Override
    public boolean matches(@NotNull GrinderRecipeInput grinderRecipeInput, Level level) {
        if (level.isClientSide()) {
            return false;
        }

        return inputItem.test(grinderRecipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(GrinderRecipeInput grinderRecipeInput, HolderLookup.Provider provider) {
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
        return FRecipes.GRINDER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FRecipes.GRINDER_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<GrinderRecipe> {

        public static final MapCodec<GrinderRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(GrinderRecipe::inputItem),
                ItemStack.CODEC.fieldOf("result").forGetter(GrinderRecipe::outputItem),
                ItemStack.CODEC.optionalFieldOf("extra_result", ItemStack.EMPTY).forGetter(GrinderRecipe::extraOutput),
                Codec.FLOAT.optionalFieldOf("extra_chance", 0.0f).forGetter(GrinderRecipe::extraChance)
        ).apply(inst, GrinderRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, GrinderRecipe::inputItem,
                        ItemStack.STREAM_CODEC, GrinderRecipe::outputItem,
                        ItemStack.STREAM_CODEC, GrinderRecipe::extraOutput,
                        ByteBufCodecs.FLOAT, GrinderRecipe::extraChance,
                        GrinderRecipe::new
                );

        @Override
        public MapCodec<GrinderRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrinderRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
