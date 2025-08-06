package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.block.entity.CoalGeneratorBlockEntity;
import com.lunazstudios.farmies.block.entity.GrinderBlockEntity;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.util.ShapeUtil;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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

public class CoalGeneratorBlock extends BaseEntityBlock {
    public static final MapCodec<CoalGeneratorBlock> CODEC = simpleCodec(CoalGeneratorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public CoalGeneratorBlock(Properties properties) {
        super(properties);
    }

    protected static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0, 0, 0, 3, 1, 3),
            Block.box(13, 0, 0, 16, 1, 3),
            Block.box(0, 0, 13, 3, 1, 16),
            Block.box(13, 0, 13, 16, 1, 16),
            Block.box(0, 1, 0, 16, 4, 16),
            Block.box(5, 4, 1, 15, 14, 11),
            Block.box(6, 5, 0, 14, 13, 1),
            Block.box(2, 6, 2, 5, 8, 4),
            Block.box(2, 6, 4, 4, 8, 11),
            Block.box(1, 4, 11, 15, 9, 16),
            Block.box(2, 9, 12.5, 4, 12, 14.5),
            Block.box(12, 10, 10.5, 14, 12, 12.5),
            Block.box(4, 10, 12.5, 14, 12, 14.5),
            Block.box(8, 14, 4, 12, 16, 8),
            Block.box(7, 16, 3, 13, 18, 9)
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
                coalGeneratorBlockEntity.drops();
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CoalGeneratorBlockEntity(blockPos, blockState);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if(entity instanceof CoalGeneratorBlockEntity coalGeneratorBlockEntity) {
                pPlayer.openMenu(new SimpleMenuProvider(coalGeneratorBlockEntity, Component.literal("Coal Generator")), pPos);
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }

        return ItemInteractionResult.sidedSuccess(pLevel.isClientSide());
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if(level.isClientSide()) {
            return null;
        }

        return createTickerHelper(blockEntityType, FBlockEntities.COAL_GENERATOR_BE.get(),
                ((level1, blockPos, blockState, coalGeneratorBlockEntity) -> coalGeneratorBlockEntity.tick(level1, blockPos, blockState)));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) return;

        double baseX = pos.getX() + 0.5;
        double baseY = pos.getY() + 1.2;
        double baseZ = pos.getZ() + 0.5;

        double offsetX = 0.1;
        double offsetZ = -0.1;

        Direction facing = state.getValue(FACING);
        double rotX = offsetX;
        double rotZ = offsetZ;

        switch (facing) {
            case NORTH -> { rotX = offsetX; rotZ = offsetZ; }
            case SOUTH -> { rotX = -offsetX; rotZ = -offsetZ; }
            case EAST  -> { rotX = offsetZ; rotZ = -offsetX; }
            case WEST  -> { rotX = -offsetZ; rotZ = offsetX; }
        }

        double particleX = baseX + rotX;
        double particleY = baseY;
        double particleZ = baseZ + rotZ;

        if (random.nextDouble() < 0.7) {
            level.addParticle(
                    ParticleTypes.SMOKE,
                    particleX + (random.nextDouble() - 0.5) * 0.05,
                    particleY,
                    particleZ + (random.nextDouble() - 0.5) * 0.05,
                    0.0, 0.05, 0.0
            );
        }

        if (random.nextDouble() < 0.3) {
            level.addParticle(
                    ParticleTypes.FLAME,
                    particleX,
                    particleY - 0.75,
                    particleZ,
                    0.0, 0.01, 0.0
            );
        }

        if (random.nextDouble() < 0.1) {
            level.playLocalSound(
                    particleX, pos.getY(), particleZ,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS,
                    0.3f, 1.0f, false
            );
        }
    }

}
