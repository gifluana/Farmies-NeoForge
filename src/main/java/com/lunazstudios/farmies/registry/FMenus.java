package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.screen.CoalGeneratorMenu;
import com.lunazstudios.farmies.screen.DehydratorMenu;
import com.lunazstudios.farmies.screen.GrinderMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class FMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Farmies.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<GrinderMenu>> GRINDER_MENU =
            registerMenuType("grinder_menu", GrinderMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<DehydratorMenu>> DEHYDRATOR_MENU =
            registerMenuType("dehydrator_menu", DehydratorMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<CoalGeneratorMenu>> COAL_GENERATOR_MENU =
            registerMenuType("coal_generator_menu", CoalGeneratorMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
