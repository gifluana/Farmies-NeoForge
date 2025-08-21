package com.lunazstudios.farmies;

import com.lunazstudios.farmies.client.model.GrinderCogModel;
import com.lunazstudios.farmies.client.model.FarmiesModelGeometry;
import com.lunazstudios.farmies.client.renderer.*;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FMenus;
import com.lunazstudios.farmies.screen.CoalGeneratorScreen;
import com.lunazstudios.farmies.screen.DehydratorScreen;
import com.lunazstudios.farmies.screen.GrinderScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
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
        event.register(FMenus.DEHYDRATOR_MENU.get(), DehydratorScreen::new);
        event.register(FMenus.COAL_GENERATOR_MENU.get(), CoalGeneratorScreen::new);
    }

    @SubscribeEvent
    static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FBlockEntities.GRINDER_BE.get(), GrinderRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.FRYING_PAN_BE.get(), FryingPanRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.COOKING_POT_BE.get(), CookingPotRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.TEST_BLOCK_BE.get(), TestBlockRenderer::new);
        event.registerBlockEntityRenderer(FBlockEntities.CABINET_BE.get(), CabinetRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(ResourceLocation.fromNamespaceAndPath(Farmies.MOD_ID, "farmies_model"), new FarmiesModelGeometry.Loader());
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(GrinderCogModel.LAYER_LOCATION, GrinderCogModel::createBodyLayer);
    }
}
