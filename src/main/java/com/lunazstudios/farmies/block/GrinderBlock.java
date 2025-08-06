package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.block.entity.GrinderBlockEntity;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.util.ShapeUtil;
import com.mojang.datafixers.kinds.IdF;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
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

public class GrinderBlock extends BaseEntityBlock {
    public static final MapCodec<GrinderBlock> CODEC = simpleCodec(GrinderBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public GrinderBlock(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(3, 14, 3, 13, 16, 13),
            Block.box(1, 0, 1, 15, 3, 15),
            Block.box(0, 3, 0, 16, 5, 16),
            Block.box(2, 5, 2, 14, 10, 14),
            Block.box(4, 10, 4, 12, 14, 12)
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
        return new GrinderBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        return createTickerHelper(type, FBlockEntities.GRINDER_BE.get(),
                level.isClientSide
                        ? (lvl, pos, st, be) -> ((GrinderBlockEntity) be).clientTick(lvl, pos, st)
                        : (lvl, pos, st, be) -> ((GrinderBlockEntity) be).tick(lvl, pos, st)
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
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
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
            if (be instanceof GrinderBlockEntity grinderBlockEntity) {
                grinderBlockEntity.drops();
            }
        }

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

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }

        double xPos = pos.getX() + 0.5;
        double yPos = pos.getY() + 1.0;
        double zPos = pos.getZ() + 0.5;

        if (level.getBlockEntity(pos) instanceof GrinderBlockEntity grinderBE &&
                !grinderBE.itemHandler.getStackInSlot(0).isEmpty()) {

            if (random.nextDouble() < 0.15) {
                level.playLocalSound(
                        xPos, pos.getY(), zPos,
                        SoundEvents.BONE_BLOCK_BREAK, SoundSource.BLOCKS,
                        1.0f, 1.0f, false
                );
            }

            double xOffset = (random.nextDouble() - 0.5) * 0.2;
            double zOffset = (random.nextDouble() - 0.5) * 0.2;

            level.addParticle(
                    new ItemParticleOption(ParticleTypes.ITEM, grinderBE.itemHandler.getStackInSlot(0)),
                    xPos + xOffset, yPos, zPos + zOffset,
                    0.0, 0.25, 0.0
            );
        }
    }
}
