package com.lunazstudios.farmies.registry;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.block.entity.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Farmies.MOD_ID);

    public static final Supplier<BlockEntityType<GrinderBlockEntity>> GRINDER_BE =
            BLOCK_ENTITIES.register("grinder_be", () -> BlockEntityType.Builder.of(
                    GrinderBlockEntity::new, FBlocks.GRINDER.get()).build(null));

    public static final Supplier<BlockEntityType<DehydratorBlockEntity>> DEHYDRATOR_BE =
            BLOCK_ENTITIES.register("dehydrator_be", () -> BlockEntityType.Builder.of(
                    DehydratorBlockEntity::new, FBlocks.DEHYDRATOR.get()).build(null));

    public static final Supplier<BlockEntityType<FryingPanBlockEntity>> FRYING_PAN_BE =
            BLOCK_ENTITIES.register("frying_pan_be", () -> BlockEntityType.Builder.of(
                    FryingPanBlockEntity::new, FBlocks.FRYING_PAN.get()).build(null));

    public static final Supplier<BlockEntityType<CookingPotBlockEntity>> COOKING_POT_BE =
            BLOCK_ENTITIES.register("cooking_pot_be", () -> BlockEntityType.Builder.of(
                    CookingPotBlockEntity::new, FBlocks.COOKING_POT.get()).build(null));

    public static final Supplier<BlockEntityType<CoalGeneratorBlockEntity>> COAL_GENERATOR_BE =
            BLOCK_ENTITIES.register("coal_generator_be", () -> BlockEntityType.Builder.of(
                    CoalGeneratorBlockEntity::new, FBlocks.COAL_GENERATOR.get()).build(null));

    public static final Supplier<BlockEntityType<TestBlockEntity>> TEST_BLOCK_BE =
            BLOCK_ENTITIES.register("test_block_be", () -> BlockEntityType.Builder.of(
                    TestBlockEntity::new, FBlocks.TEST_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
