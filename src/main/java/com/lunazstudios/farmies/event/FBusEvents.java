package com.lunazstudios.farmies.event;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.block.entity.*;
import com.lunazstudios.farmies.registry.FBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = Farmies.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class FBusEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FBlockEntities.GRINDER_BE.get(), GrinderBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FBlockEntities.DEHYDRATOR_BE.get(), DehydratorBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FBlockEntities.FRYING_PAN_BE.get(), FryingPanBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FBlockEntities.COOKING_POT_BE.get(), CookingPotBlockEntity::getItemHandler);

        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FBlockEntities.COAL_GENERATOR_BE.get(), CoalGeneratorBlockEntity::getEnergyStorage);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FBlockEntities.GRINDER_BE.get(), GrinderBlockEntity::getEnergyStorage);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, FBlockEntities.DEHYDRATOR_BE.get(), DehydratorBlockEntity::getEnergyStorage);
    }
}
