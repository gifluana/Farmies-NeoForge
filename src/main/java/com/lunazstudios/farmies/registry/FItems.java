package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class FItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Farmies.MOD_ID);

    public static final DeferredItem<Item> TOMATO_SEEDS = ITEMS.register("tomato_seeds", () -> new ItemNameBlockItem(FBlocks.TOMATOES.get(), new Item.Properties()));
    public static final DeferredItem<Item> TOMATO = ITEMS.register("tomato", () -> new Item(new Item.Properties().food(FFoodProperties.TOMATO)));

    public static final DeferredItem<Item> LETTUCE_SEEDS = ITEMS.register("lettuce_seeds", () -> new ItemNameBlockItem(FBlocks.LETTUCES.get(), new Item.Properties()));
    public static final DeferredItem<Item> LETTUCE = ITEMS.register("lettuce", () -> new Item(new Item.Properties().food(FFoodProperties.LETTUCE)));

    public static final DeferredItem<Item> ONION_SEEDS = ITEMS.register("onion_seeds", () -> new ItemNameBlockItem(FBlocks.ONIONS.get(), new Item.Properties()));
    public static final DeferredItem<Item> ONION = ITEMS.register("onion", () -> new Item(new Item.Properties().food(FFoodProperties.ONION)));

    public static final DeferredItem<Item> GARLIC_SEEDS = ITEMS.register("garlic_seeds", () -> new ItemNameBlockItem(FBlocks.GARLICS.get(), new Item.Properties()));
    public static final DeferredItem<Item> GARLIC = ITEMS.register("garlic", () -> new Item(new Item.Properties().food(FFoodProperties.GARLIC)));

    public static final DeferredItem<Item> CORN_SEEDS = ITEMS.register("corn_seeds", () -> new ItemNameBlockItem(FBlocks.CORNS.get(), new Item.Properties()));
    public static final DeferredItem<Item> CORN = ITEMS.register("corn", () -> new Item(new Item.Properties().food(FFoodProperties.CORN)));

    public static final DeferredItem<Item> BELLPEPPER_SEEDS = ITEMS.register("bellpepper_seeds", () -> new ItemNameBlockItem(FBlocks.BELLPEPPERS.get(), new Item.Properties()));
    public static final DeferredItem<Item> RED_BELLPEPPER = ITEMS.register("red_bellpepper", () -> new Item(new Item.Properties().food(FFoodProperties.BELLPEPPER)));
    public static final DeferredItem<Item> YELLOW_BELLPEPPER = ITEMS.register("yellow_bellpepper", () -> new Item(new Item.Properties().food(FFoodProperties.BELLPEPPER)));
    public static final DeferredItem<Item> GREEN_BELLPEPPER = ITEMS.register("green_bellpepper", () -> new Item(new Item.Properties().food(FFoodProperties.BELLPEPPER)));

    public static final DeferredItem<Item> EGGPLANT_SEEDS = ITEMS.register("eggplant_seeds", () -> new ItemNameBlockItem(FBlocks.EGGPLANTS.get(), new Item.Properties()));
    public static final DeferredItem<Item> EGGPLANT = ITEMS.register("eggplant", () -> new Item(new Item.Properties().food(FFoodProperties.EGGPLANT)));

    public static final DeferredItem<Item> STRAWBERRY_SEEDS = ITEMS.register("strawberry_seeds", () -> new ItemNameBlockItem(FBlocks.STRAWBERRIES.get(), new Item.Properties()));
    public static final DeferredItem<Item> STRAWBERRY = ITEMS.register("strawberry", () -> new Item(new Item.Properties().food(FFoodProperties.STRAWBERRY)));

    public static final DeferredItem<Item> CABBAGE_SEEDS = ITEMS.register("cabbage_seeds", () -> new ItemNameBlockItem(FBlocks.CABBAGES.get(), new Item.Properties()));
    public static final DeferredItem<Item> CABBAGE = ITEMS.register("cabbage", () -> new Item(new Item.Properties().food(FFoodProperties.CABBAGE)));

    public static final DeferredItem<Item> WHEAT_FLOUR = ITEMS.register("wheat_flour", () -> new Item(new Item.Properties()));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
