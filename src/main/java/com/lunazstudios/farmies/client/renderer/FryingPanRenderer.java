package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.FryingPanBlock;
import com.lunazstudios.farmies.block.entity.FryingPanBlockEntity;
import com.lunazstudios.farmies.client.animation.FryingPanAnimation;
import com.lunazstudios.farmies.client.util.Interp;
import com.lunazstudios.farmies.client.util.Keyframe;
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
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

import java.util.List;

public class FryingPanRenderer implements BlockEntityRenderer<FryingPanBlockEntity> {

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

    public FryingPanRenderer(BlockEntityRendererProvider.Context ctx) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(FryingPanBlockEntity fryingPan, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = fryingPan.getLevel();
        if (level == null) return;

        BlockPos pos = fryingPan.getBlockPos();
        int light = getBetterLight(level, pos);

        float animProgress = 0f;
        if (fryingPan.isAnimating()) {
            animProgress = (fryingPan.animationTime + partialTicks) / FryingPanAnimation.length;
            animProgress = Mth.clamp(animProgress, 0f, 1f);
        }

        poseStack.pushPose();

        Direction facing = fryingPan.getBlockState().getValue(FryingPanBlock.FACING);
        poseStack.translate(0.5, 0, 0.5);
        float rotation = switch (facing) {
            case SOUTH -> 180f;
            case WEST -> 90f;
            case EAST -> -90f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.translate(-0.5, 0, -0.5);

        if (animProgress > 0f && animProgress <= 1f) {
            float animTime = animProgress * FryingPanAnimation.length;
            Keyframe posFrame = interpolate(FryingPanAnimation.positionFrames, animTime);
            Keyframe rotFrame = interpolate(FryingPanAnimation.rotationFrames, animTime);

            poseStack.translate(posFrame.x() / 16f, posFrame.y() / 16f, posFrame.z() / 16f);
            poseStack.mulPose(Axis.XP.rotationDegrees(rotFrame.x()));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotFrame.y()));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotFrame.z()));
        }

        var blockRenderer = Minecraft.getInstance().getBlockRenderer();
        var model = blockRenderer.getBlockModel(fryingPan.getBlockState());
        blockRenderer.getModelRenderer().tesselateBlock(
                level,
                model,
                fryingPan.getBlockState(),
                pos,
                poseStack,
                bufferSource.getBuffer(net.minecraft.client.renderer.RenderType.cutout()),
                false,
                level.random,
                fryingPan.getBlockState().getSeed(pos),
                packedOverlay
        );

        ItemStack outputStack = fryingPan.itemHandler.getStackInSlot(9);
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
            ItemStack stack = fryingPan.itemHandler.getStackInSlot(slot);
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

    private Keyframe interpolate(List<Keyframe> frames, float time) {
        if (frames.isEmpty()) return new Keyframe(0,0,0,0, Interp.LINEAR);

        Keyframe prev = frames.get(0);
        Keyframe next = frames.get(frames.size() - 1);

        for (int i = 0; i < frames.size() - 1; i++) {
            Keyframe k1 = frames.get(i);
            Keyframe k2 = frames.get(i+1);
            if (time >= k1.time() && time <= k2.time()) {
                prev = k1;
                next = k2;
                break;
            }
        }

        float segmentLength = next.time() - prev.time();
        float progress = segmentLength > 0 ? (time - prev.time()) / segmentLength : 0f;

        float x = Mth.lerp(progress, prev.x(), next.x());
        float y = Mth.lerp(progress, prev.y(), next.y());
        float z = Mth.lerp(progress, prev.z(), next.z());
        return new Keyframe(time, x, y, z, Interp.LINEAR);
    }
}