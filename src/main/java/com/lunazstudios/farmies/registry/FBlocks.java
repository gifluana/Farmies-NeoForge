package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.block.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Farmies.MOD_ID);

    // Farmlands
    public static final DeferredBlock<Block> FERTILIZED_FARMLAND = registerBlock("fertilized_farmland", () -> new FertilizedFarmlandBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)));
    public static final DeferredBlock<Block> FARMLAND_WITH_WORMS = registerBlock("farmland_with_worms", () -> new FarmlandWithWormsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND)));

    // Crops
    public static final DeferredBlock<Block> TOMATOES = registerBlockOnly("tomatoes", () -> new TomatoCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> LETTUCES = registerBlockOnly("lettuces", () -> new LettuceCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> ONIONS = registerBlockOnly("onions", () -> new OnionCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> GARLICS = registerBlockOnly("garlics", () -> new GarlicCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> CORNS = registerBlockOnly("corns", () -> new CornCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> BELLPEPPERS = registerBlockOnly("bellpeppers", () -> new BellPepperCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> EGGPLANTS = registerBlockOnly("eggplants", () -> new EggplantCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> STRAWBERRIES = registerBlockOnly("strawberries", () -> new StrawberryCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));
    public static final DeferredBlock<Block> CABBAGES = registerBlockOnly("cabbages", () -> new CabbageCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)));

    // Machines
    public static final DeferredBlock<Block> GRINDER = registerBlock("grinder", () -> new GrinderBlock(BlockBehaviour.Properties.of().strength(3f).requiresCorrectToolForDrops()));

    // Helper Methods
    private static <T extends Block> DeferredBlock<T> registerBlockOnly(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        FItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
