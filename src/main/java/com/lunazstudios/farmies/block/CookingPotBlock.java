package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.block.entity.CookingPotBlockEntity;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.util.ShapeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class CookingPotBlock extends BaseEntityBlock {
    public static final MapCodec<CookingPotBlock> CODEC = simpleCodec(CookingPotBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty HAS_SUPPORT = BooleanProperty.create("has_support");
    public CookingPotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(LIT, false)
                        .setValue(HAS_SUPPORT, false)
        );
    }

    protected static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(15, 7, 6, 17, 8, 7),
            Block.box(1, 0, 1, 15, 9, 15),
            Block.box(-1, 7, 9, 1, 8, 10),
            Block.box(-1, 7, 6, 1, 8, 7),
            Block.box(-1, 7, 7, 0, 8, 9),
            Block.box(15, 7, 9, 17, 8, 10),
            Block.box(16, 7, 7, 17, 8, 9)
    );

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
        return new CookingPotBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        return createTickerHelper(type, FBlockEntities.COOKING_POT_BE.get(),
                (lvl, pos, st, be) -> ((CookingPotBlockEntity) be).tick(lvl, pos, st)
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
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos below = context.getClickedPos().below();
        BlockState belowState = level.getBlockState(below);

        boolean hasSupport = belowState.getBlock() instanceof CampfireBlock;

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false)
                .setValue(HAS_SUPPORT, hasSupport);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, HAS_SUPPORT);
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState.getBlock() != blockState2.getBlock()) {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof CookingPotBlockEntity cookingPotBlockEntity) {
                cookingPotBlockEntity.drops();
            }
        }

        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide) return;

        if (neighborPos.equals(pos.below())) {
            boolean hasSupport = level.getBlockState(pos.below()).getBlock() instanceof CampfireBlock;
            if (state.getValue(HAS_SUPPORT) != hasSupport) {
                level.setBlock(pos, state.setValue(HAS_SUPPORT, hasSupport), 3);
            }
        }

        boolean powered = level.hasNeighborSignal(pos);
        if (!powered) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CookingPotBlockEntity cookingPot)) return;

        if (!cookingPot.isCrafting() && cookingPot.hasRecipe()) {
            cookingPot.startCrafting();
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
        if (be instanceof CookingPotBlockEntity cookingPotBlock) {
            return cookingPotBlock.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) return ItemInteractionResult.CONSUME;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CookingPotBlockEntity cookingPot)) {
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty() && player.isShiftKeyDown() && !cookingPot.isCrafting()) {
            for (int i = CookingPotBlockEntity.SLOT_OUTPUT - 1; i >= 0; i--) {
                ItemStack stored = cookingPot.itemHandler.getStackInSlot(i);
                if (!stored.isEmpty()) {
                    ItemStack extracted = cookingPot.itemHandler.extractItem(i, stored.getCount(), false);
                    if (!extracted.isEmpty()) {
                        player.getInventory().placeItemBackInInventory(extracted);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty()) {
            ItemStack output = cookingPot.itemHandler.getStackInSlot(CookingPotBlockEntity.SLOT_OUTPUT);
            if (!output.isEmpty()) {
                ItemStack taken = cookingPot.itemHandler.extractItem(CookingPotBlockEntity.SLOT_OUTPUT, output.getCount(), false);
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

        if (!stack.isEmpty() && !cookingPot.isCrafting()) {
            ItemStack remainder = cookingPot.addItem(stack.copyWithCount(1));
            if (remainder.isEmpty()) {
                stack.shrink(1);
                return ItemInteractionResult.SUCCESS;
            }
            return ItemInteractionResult.CONSUME;
        }

        if (stack.isEmpty() && !cookingPot.isCrafting() && cookingPot.hasRecipe()) {
            cookingPot.startCrafting();
            level.setBlock(pos, state.setValue(CookingPotBlock.LIT, true), 3);
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
