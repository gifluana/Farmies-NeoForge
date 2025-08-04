package com.lunazstudios.farmies.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FarmiesHelper {
    public static float getGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
        return CropAccessor.callGetGrowthSpeed(state, level, pos);
    }

    private static abstract class CropAccessor extends CropBlock {
        public CropAccessor(Properties properties) {
            super(properties);
        }

        public static float callGetGrowthSpeed(BlockState state, ServerLevel level, BlockPos pos) {
            return CropBlock.getGrowthSpeed(state, level, pos);
        }
    }
}
