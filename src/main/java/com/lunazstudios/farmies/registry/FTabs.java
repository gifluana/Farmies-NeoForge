package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Farmies.MOD_ID);

    public static final Supplier<CreativeModeTab> PRODUCES_TAB =
            CREATIVE_MODE_TABS.register("produces", () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + Farmies.MOD_ID + ".produces"))
                    .icon(() -> new ItemStack(FItems.TOMATO.get()))
                    .displayItems((pParameters, output) -> {
                        output.accept(FItems.TOMATO.get());
                        output.accept(FItems.LETTUCE.get());
                        output.accept(FItems.ONION.get());
                        output.accept(FItems.GARLIC.get());
                        output.accept(FItems.CORN.get());
                        output.accept(FItems.GREEN_BELLPEPPER.get());
                        output.accept(FItems.YELLOW_BELLPEPPER.get());
                        output.accept(FItems.RED_BELLPEPPER.get());
                        output.accept(FItems.EGGPLANT.get());
                        output.accept(FItems.STRAWBERRY.get());
                        output.accept(FItems.CABBAGE.get());
                    }).build());

    public static final Supplier<CreativeModeTab> SEEDS_TAB =
            CREATIVE_MODE_TABS.register("seeds", () -> CreativeModeTab.builder()
                    .title(Component.translatable("item_group." + Farmies.MOD_ID + ".seeds"))
                    .icon(() -> new ItemStack(FItems.TOMATO_SEEDS.get()))
                    .displayItems((pParameters, output) -> {
                        output.accept(FItems.TOMATO_SEEDS.get());
                        output.accept(FItems.LETTUCE_SEEDS.get());
                        output.accept(FItems.ONION_SEEDS.get());
                        output.accept(FItems.GARLIC_SEEDS.get());
                        output.accept(FItems.CORN_SEEDS.get());
                        output.accept(FItems.BELLPEPPER_SEEDS.get());
                        output.accept(FItems.EGGPLANT_SEEDS.get());
                        output.accept(FItems.STRAWBERRY_SEEDS.get());
                        output.accept(FItems.CABBAGE_SEEDS.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
