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

import java.util.ArrayList;
import java.util.List;

public record CookingPotRecipe(
        NonNullList<Ingredient> ingredients,
        ItemStack outputItem
) implements Recipe<CookingPotRecipeInput> {

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    @Override
    public boolean matches(@NotNull CookingPotRecipeInput input, Level level) {
        if (level.isClientSide()) return false;

        List<Ingredient> required = new ArrayList<>(ingredients);

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;

            boolean matched = false;
            for (int j = 0; j < required.size(); j++) {
                if (required.get(j).test(stack)) {
                    required.remove(j);
                    matched = true;
                    break;
                }
            }

            if (!matched) return false;
        }

        return required.isEmpty();
    }

    @Override
    public ItemStack assemble(CookingPotRecipeInput input, HolderLookup.Provider provider) {
        return outputItem.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int j) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return outputItem;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FRecipes.COOKING_POT_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FRecipes.COOKING_POT_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CookingPotRecipe> {

        public static final MapCodec<CookingPotRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").xmap(
                        list -> NonNullList.of(Ingredient.EMPTY, list.toArray(new Ingredient[0])),
                        ArrayList::new
                ).forGetter(CookingPotRecipe::ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(CookingPotRecipe::outputItem)
        ).apply(inst, CookingPotRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> STREAM_CODEC =
                StreamCodec.of(
                        (buf, recipe) -> {
                            buf.writeVarInt(recipe.ingredients().size());
                            for (Ingredient ing : recipe.ingredients()) {
                                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
                            }
                            ItemStack.STREAM_CODEC.encode(buf, recipe.outputItem());
                        },

                        buf -> {
                            int size = buf.readVarInt();
                            NonNullList<Ingredient> ingredients = NonNullList.create();
                            for (int i = 0; i < size; i++) {
                                ingredients.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                            }
                            ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                            return new CookingPotRecipe(ingredients, output);
                        }
                );

        @Override
        public MapCodec<CookingPotRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}