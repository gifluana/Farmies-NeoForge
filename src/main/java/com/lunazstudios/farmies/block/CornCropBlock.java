package com.lunazstudios.farmies.block;

import com.lunazstudios.farmies.registry.FItems;
import com.lunazstudios.farmies.util.FarmiesHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CornCropBlock extends CropBlock {
    public static final int MAX_AGE = 5;
    public static final int DOUBLE_AGE = 3;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape FULL_BOTTOM = Block.box(0, 0, 0, 16, 16, 16);
    private static final VoxelShape[] SHAPES_BOTTOM = new VoxelShape[] {
            Block.box(0, 0, 0, 16, 9, 16),
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM
    };

    private static final VoxelShape[] SHAPES_TOP = new VoxelShape[] {
            FULL_BOTTOM,
            FULL_BOTTOM,
            FULL_BOTTOM,
            Block.box(0, 0, 0, 16, 11, 16),
            Block.box(0, 0, 0, 16, 11, 16),
            FULL_BOTTOM
    };

    public CornCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState state) {
        return !this.isMaxAge(state);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource randomSource, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        int increment = this.getBonemealAgeIncrease(level);
        growCropBy(level, pos, state, increment);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        int age = state.getValue(AGE);
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return SHAPES_TOP[Math.min(age, SHAPES_TOP.length - 1)];
        } else {
            return SHAPES_BOTTOM[Math.min(age, SHAPES_BOTTOM.length - 1)];
        }
    }

    @Override
    protected @NotNull ItemLike getBaseSeedId() {
        return FItems.CORN_SEEDS.get();
    }

    @Override
    protected @NotNull IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) return;

        if (level.getRawBrightness(pos, 0) >= 9 && !this.isMaxAge(state)) {
            float f = FarmiesHelper.getGrowthSpeed(state, level, pos);
            if (random.nextInt((int) (25.0F / f) + 1) == 0) {
                growCropBy(level, pos, state, 1);
            }
        }
    }

    public void growCropBy(Level level, BlockPos pos, BlockState state, int increment) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
        }

        int newAge = Math.min(getAge(state) + increment, getMaxAge());
        level.setBlock(pos, getStateForAge(newAge).setValue(HALF, DoubleBlockHalf.LOWER), 2);

        if (newAge >= DOUBLE_AGE) {
            level.setBlock(pos.above(), getStateForAge(newAge).setValue(HALF, DoubleBlockHalf.UPPER), 2);
        } else {
            BlockState above = level.getBlockState(pos.above());
            if (above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER) {
                level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = world.getBlockState(pos.below());
            return below.is(this)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER
                    && this.getAge(below) >= DOUBLE_AGE;
        } else {
            return super.canSurvive(state, world, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState neighbor, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (dir.getAxis() == Direction.Axis.Y) {
            if (half == DoubleBlockHalf.UPPER && dir == Direction.DOWN) {
                if (!neighbor.is(this) || neighbor.getValue(HALF) != DoubleBlockHalf.LOWER) {
                    return Blocks.AIR.defaultBlockState();
                }
            }

            if (half == DoubleBlockHalf.LOWER && dir == Direction.UP) {
                if (!neighbor.is(this) || neighbor.getValue(HALF) != DoubleBlockHalf.UPPER) {
                    world.destroyBlock(pos, false);
                }
            }
        }

        return super.updateShape(state, dir, neighbor, world, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos mainPos = (half == DoubleBlockHalf.UPPER) ? pos.below() : pos;
            BlockPos otherPos = (half == DoubleBlockHalf.UPPER) ? pos : pos.above();

            BlockState mainState = level.getBlockState(mainPos);

            Block.dropResources(mainState, level, mainPos, null, player, player.getMainHandItem());

            level.removeBlock(mainPos, false);
            if (level.getBlockState(otherPos).is(this)) {
                level.removeBlock(otherPos, false);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }
}
