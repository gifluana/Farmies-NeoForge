package com.lunazstudios.farmies;

import com.lunazstudios.farmies.registry.FMenus;
import com.lunazstudios.farmies.screen.GrinderScreen;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Farmies.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Farmies.MOD_ID, value = Dist.CLIENT)
public class FarmiesClient {
    public FarmiesClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {

    }

    @SubscribeEvent
    static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FMenus.GRINDER_MENU.get(), GrinderScreen::new);
    }
}
