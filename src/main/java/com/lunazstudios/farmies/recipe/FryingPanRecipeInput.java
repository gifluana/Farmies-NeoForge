package com.lunazstudios.farmies.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;


public record FryingPanRecipeInput(List<ItemStack> inputs) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < inputs.size() ? inputs.get(index) : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return inputs.size();
    }
}