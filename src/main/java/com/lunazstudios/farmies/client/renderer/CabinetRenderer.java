package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.CabinetBlock;
import com.lunazstudios.farmies.block.entity.CabinetBlockEntity;
import com.lunazstudios.farmies.client.runtime.BBModel;
import com.lunazstudios.farmies.client.runtime.BBRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class CabinetRenderer implements BlockEntityRenderer<CabinetBlockEntity> {
    private final Map<ResourceLocation, BBModel> modelCache = new HashMap<>();

    public CabinetRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(CabinetBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {

        if (be.getLevel() == null) return;

        BlockState st = be.getBlockState();
        CabinetBlock block = (CabinetBlock) st.getBlock();
        String wood = block.getWood().getSerializedName();

        ResourceLocation modelPath = ResourceLocation.fromNamespaceAndPath("farmies",
                "models/block/furniture/bbs/" + wood + "_cabinet.bbs.json");

        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath("farmies",
                "textures/block/furniture/" + wood + "_cabinet.png");

        BBModel model = modelCache.computeIfAbsent(modelPath, BBModel::load);


        String clip = be.getActiveClip();

        float progress = be.getTransitionProgress(partialTicks);

        boolean loop;
        float timeSec;
        switch (clip) {
            case "opening" -> {
                loop = false;
                timeSec = progress * getClipLengthSec("opening");
            }
            case "closing" -> {
                loop = false;
                timeSec = progress * getClipLengthSec("closing");
            }
            case "open" -> {
                loop = false;
                timeSec = 0f;
            }
            case "closed" -> {
                loop = false;
                timeSec = 0f;
            }
            default -> {
                loop = false;
                timeSec = 0f;
            }
        }

        var opts = BBRenderer.Opts.defaults()
                .clip(clip)
                .loop(loop)
                .center(true)
                .autoLight(true)
                .time(timeSec);

        BBRenderer.render(model, be, texture, ps, buffers, partialTicks, packedOverlay, opts);
    }

    private float getClipLengthSec(String clip) {
        return switch (clip) {
            case "opening" -> CabinetBlockEntity.openingDurationSec();
            case "closing" -> CabinetBlockEntity.closingDurationSec();
            default -> 0f;
        };
    }
}
