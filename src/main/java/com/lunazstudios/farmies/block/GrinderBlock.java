package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.block.entity.GrinderBlockEntity;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class GrinderBlock extends BaseEntityBlock {
    public static final MapCodec<GrinderBlock> CODEC = simpleCodec(GrinderBlock::new);
    public GrinderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GrinderBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, FBlockEntities.GRINDER_BE.get(),
                (level1, blockPos, blockState1, grinderBlockEntity) -> grinderBlockEntity.tick(level1, blockPos, blockState1));
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {

        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(blockPos);
            if (entity instanceof GrinderBlockEntity grinderBlockEntity) {
                (player).openMenu(new SimpleMenuProvider(grinderBlockEntity, Component.literal("Grinder")), blockPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
