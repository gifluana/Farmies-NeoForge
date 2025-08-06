package com.lunazstudios.farmies.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class GrinderCogModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("farmies", "grinder_cog"), "main");

    private final ModelPart cogs;

    public GrinderCogModel(ModelPart root) {
        this.cogs = root.getChild("cogs");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition cogs = partdefinition.addOrReplaceChild("cogs",
                CubeListBuilder.create()
                        .texOffs(0, 50).addBox(6.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F)
                        .texOffs(8, 54).addBox(7.5F, -2.5F, -2.5F, 3.0F, 5.0F, 5.0F)
                        .texOffs(0, 54).addBox(8.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F)
                        .texOffs(24, 54).addBox(8.0F, -1.0F, -4.0F, 2.0F, 2.0F, 8.0F)
                        .texOffs(0, 50).mirror().addBox(-11.0F, -1.0F, -1.0F, 5.0F, 2.0F, 2.0F).mirror(false)
                        .texOffs(8, 54).mirror().addBox(-10.5F, -2.5F, -2.5F, 3.0F, 5.0F, 5.0F).mirror(false)
                        .texOffs(0, 54).mirror().addBox(-10.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F).mirror(false)
                        .texOffs(24, 54).addBox(-10.0F, -1.0F, -4.0F, 2.0F, 2.0F, 8.0F),
                PartPose.offset(0.0F, 17.0F, 2.5F));

        cogs.addOrReplaceChild("cube_r1",
                CubeListBuilder.create()
                        .texOffs(24, 54).addBox(-10.0F, -1.0F, -4.0F, 2.0F, 2.0F, 8.0F)
                        .texOffs(0, 54).mirror().addBox(-10.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F).mirror(false)
                        .texOffs(24, 54).addBox(8.0F, -1.0F, -4.0F, 2.0F, 2.0F, 8.0F)
                        .texOffs(0, 54).addBox(8.0F, -4.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    /** Render das engrenagens */
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay) {
        cogs.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    /** Rotaciona as cogs (chamar no BER) */
    public void setRotation(float rotation) {
        cogs.xRot = rotation;
    }
}