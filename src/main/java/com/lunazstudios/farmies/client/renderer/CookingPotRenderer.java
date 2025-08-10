package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.FryingPanBlock;
import com.lunazstudios.farmies.block.entity.CookingPotBlockEntity;
import com.lunazstudios.farmies.client.runtime.BBRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class CookingPotRenderer implements BlockEntityRenderer<CookingPotBlockEntity> {

    private final ItemRenderer itemRenderer;
    private static final float[][] ITEM_POSITIONS = {
            {  0.00f, 0.0f, 0.00f },  //0 - centro
            {  0.5f, 0.0f, 0.2f },  //1
            {  -0.45f, 0.0f, -0.1f },  //2
            {  0.3f, 0.0f, 0.5f },  //3
            {  -0.3f, 0.05f, -0.3f },  //4
            {  -0.3f, 0.0f, 0.4f },  //5
            {  0.2f, 0.0f, -0.4f },  //6
            {  0.5f, 0.05f, -0.3f },  //7
            {  -0.5f, 0.05f, 0.3f }   //8
    };

    private static final float[][] ITEM_ROTATIONS = {
            { 90f, 0f, 0f },   //0
            { 90f, -2f, 5f },   //1
            { 90f, 2f, -7f },   //2
            { 90f, -2f, -10f },   //3
            { 85f, -5f, 3f },  //4
            { 80f, 2f, -7f },   //5
            { 85f, 0f, 0f },   //6
            { 90f, -5f, -5f },   //7
            { 90f, 0f, -5f }    //8
    };

    public CookingPotRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }


    @Override
    public void render(CookingPotBlockEntity cookingPot, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = cookingPot.getLevel();
        if (level == null) return;

        BlockPos pos = cookingPot.getBlockPos();
        int light = getBetterLight(level, pos);

        poseStack.pushPose();

        Direction facing = cookingPot.getBlockState().getValue(FryingPanBlock.FACING);
        poseStack.translate(0.5, 0.1, 0.5);
        float rotation = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, 0, -0.5);

        ItemStack outputStack = cookingPot.itemHandler.getStackInSlot(9);
        if (!outputStack.isEmpty()) {
            poseStack.pushPose();

            poseStack.translate(0.5, 0.025, 0.5);
            poseStack.scale(0.45f, 0.45f, 0.45f);

            poseStack.mulPose(Axis.XP.rotationDegrees(90));

            itemRenderer.renderStatic(outputStack, ItemDisplayContext.FIXED, light, packedOverlay,
                    poseStack, bufferSource, level, 0);

            poseStack.popPose();
        }

        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = cookingPot.itemHandler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;

            poseStack.pushPose();
            poseStack.translate(0.5, 0.025, 0.5);
            poseStack.scale(0.45f, 0.45f, 0.45f);

            float[] posOffset = ITEM_POSITIONS[slot];
            poseStack.translate(posOffset[0], posOffset[1], posOffset[2]);

            float[] rot = ITEM_ROTATIONS[slot];
            poseStack.mulPose(Axis.XP.rotationDegrees(rot[0]));
            poseStack.mulPose(Axis.YP.rotationDegrees(rot[1]));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rot[2]));

            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, packedOverlay,
                    poseStack, bufferSource, level, 0);

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private int getBetterLight(Level level, BlockPos pos) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, pos);
        int skyLight = level.getBrightness(LightLayer.SKY, pos);

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            blockLight = Math.max(blockLight, level.getBrightness(LightLayer.BLOCK, neighbor));
            skyLight = Math.max(skyLight, level.getBrightness(LightLayer.SKY, neighbor));
        }

        return LightTexture.pack(blockLight, skyLight);
    }
}