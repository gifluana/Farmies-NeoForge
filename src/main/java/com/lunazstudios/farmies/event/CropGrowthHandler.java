package com.lunazstudios.farmies.event;

import com.lunazstudios.farmies.Farmies;
import com.lunazstudios.farmies.block.FarmlandWithWormsBlock;
import com.lunazstudios.farmies.block.FertilizedFarmlandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;

@EventBusSubscriber(modid = Farmies.MOD_ID)
public class CropGrowthHandler {
    @SubscribeEvent
    public static void onCropGrowth(CropGrowEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        BlockPos pos = event.getPos();
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        float chance = 0f;

        if (belowState.getBlock() instanceof FarmlandWithWormsBlock &&
                belowState.hasProperty(FarmlandWithWormsBlock.MOISTURE) &&
                belowState.getValue(FarmlandWithWormsBlock.MOISTURE) > 0) {
            chance = 0.5f;
        }

        if (belowState.getBlock() instanceof FertilizedFarmlandBlock &&
                belowState.hasProperty(FertilizedFarmlandBlock.MOISTURE) &&
                belowState.getValue(FertilizedFarmlandBlock.MOISTURE) > 0) {
            chance = 0.75f;
        }

        if (chance > 0 && level.random.nextFloat() < chance) {
            event.setResult(CropGrowEvent.Pre.Result.GROW);
        }
    }
}