package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.block.entity.FryingPanBlockEntity;
import com.lunazstudios.farmies.block.entity.FryingPanBlockEntity;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FSounds;
import com.lunazstudios.farmies.util.ShapeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FryingPanBlock extends BaseEntityBlock {
    public static final MapCodec<FryingPanBlock> CODEC = simpleCodec(FryingPanBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public FryingPanBlock(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE_NORTH = Block.box(1, 0, 1, 15, 3, 15);

    protected static final VoxelShape SHAPE_EAST = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.EAST);
    protected static final VoxelShape SHAPE_SOUTH = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.SOUTH);
    protected static final VoxelShape SHAPE_WEST = ShapeUtil.rotateShape(SHAPE_NORTH, Direction.WEST);

    protected static final VoxelShape[] SHAPES = new VoxelShape[] {
            SHAPE_SOUTH, SHAPE_WEST, SHAPE_NORTH, SHAPE_EAST
    };

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new FryingPanBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        return createTickerHelper(type, FBlockEntities.FRYING_PAN_BE.get(),
                level.isClientSide
                        ? (lvl, pos, st, be) -> ((FryingPanBlockEntity) be).clientTick(lvl, pos, st)
                        : (lvl, pos, st, be) -> ((FryingPanBlockEntity) be).tick(lvl, pos, st)
        );
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        int shape = state.getValue(FACING).get2DDataValue();
        return SHAPES[shape];
    }


    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getBlock() != blockState2.getBlock()) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof FryingPanBlockEntity fryingPanBlockEntity) {
                fryingPanBlockEntity.drops();
            }
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);
        if (!powered) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FryingPanBlockEntity fryingPan)) return;

        if (!fryingPan.isCrafting() && fryingPan.hasRecipe()) {
            fryingPan.startCrafting();
            level.setBlock(pos, state.setValue(LIT, true), 3);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof FryingPanBlockEntity fryingPan) {
            return fryingPan.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.CONSUME;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FryingPanBlockEntity fryingPan)) {
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty() && player.isShiftKeyDown() && !fryingPan.isCrafting()) {
            for (int i = FryingPanBlockEntity.SLOT_OUTPUT - 1; i >= 0; i--) {
                ItemStack stored = fryingPan.itemHandler.getStackInSlot(i);
                if (!stored.isEmpty()) {
                    ItemStack extracted = fryingPan.itemHandler.extractItem(i, stored.getCount(), false);
                    if (!extracted.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(extracted);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty()) {
            ItemStack output = fryingPan.itemHandler.getStackInSlot(FryingPanBlockEntity.SLOT_OUTPUT);
            if (!output.isEmpty()) {
                ItemStack taken = fryingPan.itemHandler.extractItem(FryingPanBlockEntity.SLOT_OUTPUT, output.getCount(), false);
                player.getInventory().placeItemBackInInventory(taken);
                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ITEM_PICKUP,
                        SoundSource.PLAYERS,
                        0.3f,
                        1.0f
                );
                return ItemInteractionResult.SUCCESS;
            }
        }

        if (!stack.isEmpty() && !fryingPan.isCrafting()) {
            ItemStack remainder = fryingPan.addItem(stack.copyWithCount(1));
            if (remainder.isEmpty()) {
                stack.shrink(1);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty() && !fryingPan.isCrafting() && fryingPan.hasRecipe()) {
            fryingPan.startCrafting();
            level.setBlock(pos, state.setValue(FryingPanBlock.LIT, true), 3);
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.CONSUME;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

    }
}
