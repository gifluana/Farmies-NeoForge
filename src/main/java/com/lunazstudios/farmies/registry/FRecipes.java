package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.recipe.CookingPotRecipe;
import com.lunazstudios.farmies.recipe.DehydratorRecipe;
import com.lunazstudios.farmies.recipe.FryingPanRecipe;
import com.lunazstudios.farmies.recipe.GrinderRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Farmies.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Farmies.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrinderRecipe>> GRINDER_SERIALIZER =
            SERIALIZERS.register("grinding", GrinderRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrinderRecipe>> GRINDER_TYPE =
            TYPES.register("grinding", () -> new RecipeType<GrinderRecipe>() {
                @Override
                public String toString() {
                    return "grinding";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DehydratorRecipe>> DEHYDRATOR_SERIALIZER =
            SERIALIZERS.register("dehydration", DehydratorRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<DehydratorRecipe>> DEHYDRATOR_TYPE =
            TYPES.register("dehydration", () -> new RecipeType<DehydratorRecipe>() {
                @Override
                public String toString() {
                    return "dehydration";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FryingPanRecipe>> FRYING_PAN_SERIALIZER =
            SERIALIZERS.register("frying", FryingPanRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FryingPanRecipe>> FRYING_PAN_TYPE =
            TYPES.register("frying", () -> new RecipeType<FryingPanRecipe>() {
                @Override
                public String toString() {
                    return "frying";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CookingPotRecipe>> COOKING_POT_SERIALIZER =
            SERIALIZERS.register("cooking", CookingPotRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CookingPotRecipe>> COOKING_POT_TYPE =
            TYPES.register("cooking", () -> new RecipeType<CookingPotRecipe>() {
                @Override
                public String toString() {
                    return "cooking";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
