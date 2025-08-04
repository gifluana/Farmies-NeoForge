package com.lunazstudios.farmies.registry;

import net.minecraft.world.food.FoodProperties;

public class FFoodProperties {
    public static final FoodProperties TOMATO = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.4f)
            .build();

    public static final FoodProperties LETTUCE = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.2f)
            .build();

    public static final FoodProperties ONION = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.3f)
            .build();

    public static final FoodProperties GARLIC = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1f)
            .build();

    public static final FoodProperties CORN = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.5f)
            .build();

    public static final FoodProperties BELLPEPPER = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.4f)
            .build();

    public static final FoodProperties EGGPLANT = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.35f)
            .build();

    public static final FoodProperties STRAWBERRY = new FoodProperties.Builder()
            .nutrition(3)
            .saturationModifier(0.4f)
            .build();

    public static final FoodProperties CABBAGE = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1f)
            .build();
}
