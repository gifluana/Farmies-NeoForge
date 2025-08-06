package com.lunazstudios.farmies.item;

import com.lunazstudios.farmies.block.CornCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.Objects;

public class ScytheItem extends Item {
    public ScytheItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos clickedPos = context.getClickedPos();
        ServerLevel serverLevel = (ServerLevel) level;

        int radius = 1;
        boolean harvestedAny = false;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                BlockPos pos = clickedPos.offset(dx, 0, dz);
                BlockState state = serverLevel.getBlockState(pos);
                Block block = state.getBlock();

                if (block instanceof CropBlock crop && crop.isMaxAge(state)) {
                    BlockPos basePos = pos;
                    BlockState baseState = state;

                    if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) &&
                            state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                        basePos = pos.below();
                        baseState = serverLevel.getBlockState(basePos);
                    }

                    if (baseState.getBlock() instanceof CornCropBlock corn) {
                        BlockPos topPos = basePos.above();

                        Block.dropResources(baseState, serverLevel, basePos);

                        BlockState topState = serverLevel.getBlockState(topPos);
                        if (topState.getBlock() instanceof CornCropBlock &&
                                topState.getValue(CornCropBlock.HALF) == DoubleBlockHalf.UPPER) {
                            Block.dropResources(topState, serverLevel, topPos);
                        }

                        BlockState newBase = corn.getStateForAge(0).setValue(CornCropBlock.HALF, DoubleBlockHalf.LOWER);
                        serverLevel.setBlock(basePos, newBase, 3);

                        if (topState.getBlock() instanceof CornCropBlock) {
                            serverLevel.setBlock(topPos, Blocks.AIR.defaultBlockState(), 3);
                        }

                        serverLevel.playSound(null, basePos,
                                SoundEvents.CROP_BREAK,
                                SoundSource.BLOCKS,
                                1.0f, 1.0f
                        );

                        harvestedAny = true;
                    } else {
                        Block.dropResources(baseState, serverLevel, basePos);

                        BlockState newBase = crop.getStateForAge(0);
                        serverLevel.setBlock(basePos, newBase, 3);

                        serverLevel.playSound(null, basePos,
                                SoundEvents.CROP_BREAK,
                                SoundSource.BLOCKS,
                                1.0f, 1.0f
                        );

                        harvestedAny = true;
                    }
                }
            }
        }

        if (harvestedAny && context.getItemInHand().isDamageableItem()) {
            context.getItemInHand().hurtAndBreak(1, serverLevel, (ServerPlayer) context.getPlayer(),
                    item -> Objects.requireNonNull(context.getPlayer())
                            .onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
        }

        return harvestedAny ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
