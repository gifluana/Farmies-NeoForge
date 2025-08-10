package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.GrinderBlock;
import com.lunazstudios.farmies.block.entity.GrinderBlockEntity;
import com.lunazstudios.farmies.client.model.GrinderCogModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class GrinderRenderer implements BlockEntityRenderer<GrinderBlockEntity> {
    private final GrinderCogModel cogModel;

    public GrinderRenderer(BlockEntityRendererProvider.Context ctx) {
        ModelPart root = ctx.bakeLayer(GrinderCogModel.LAYER_LOCATION);
        this.cogModel = new GrinderCogModel(root);
    }

    @Override
    public void render(GrinderBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        Level level = blockEntity.getLevel();
        if (level == null) return;

        int light = getBetterLight(level, blockEntity.getBlockPos());

        float rotation = blockEntity.cogRotation + blockEntity.cogSpeed * partialTick;
        cogModel.setRotation((float) Math.toRadians(rotation));

        poseStack.pushPose();

        poseStack.translate(0.5, -0.55, 0.5);

        Direction facing = blockEntity.getBlockState().getValue(GrinderBlock.FACING);

        float yRot = switch (facing) {
            case NORTH -> 0f;
            case EAST -> -90f;
            case SOUTH -> 180f;
            case WEST -> -270f;
            default -> 0f;
        };

        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));

        VertexConsumer vertexConsumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(
                        ResourceLocation.fromNamespaceAndPath("farmies", "textures/block/machines/grinder.png")
                )
        );

        cogModel.renderToBuffer(poseStack, vertexConsumer, light, packedOverlay);
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