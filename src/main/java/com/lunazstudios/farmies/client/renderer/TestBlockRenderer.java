package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.entity.TestBlockEntity;
import com.lunazstudios.farmies.client.runtime.BBModel;
import com.lunazstudios.farmies.client.runtime.BBRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class TestBlockRenderer implements BlockEntityRenderer<TestBlockEntity> {
    private final BBModel model;
    private final ResourceLocation texture;

    public TestBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        var json = ResourceLocation.fromNamespaceAndPath("farmies", "models/runtime/my_model.bbs.json");
        this.model = BBModel.load(json);
        this.texture = ResourceLocation.fromNamespaceAndPath("farmies", "textures/runtime/my_model.png");
    }

    @Override
    public void render(TestBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String clip = be.getActiveClip();
        boolean loop = be.isLooping();

        Float timeOverride = null;
        long start = be.getClipStartGameTime();
        var level = be.getLevel();
        if (level != null && start > 0L) {
            float duration = 0f;
            var c = model.clips.get(clip);
            if (c != null) duration = c.durationSec;

            float elapsed = ((level.getGameTime() - start) + partialTicks) / 20f;
            timeOverride = (duration > 0f)
                    ? (loop ? (elapsed % duration) : Math.min(elapsed, duration))
                    : elapsed;
        }

        var opts = BBRenderer.Opts.defaults()
                .clip(clip)
                .loop(loop)
                .center(true)
                .autoLight(true);

        if (timeOverride != null) opts.time(timeOverride);

        BBRenderer.render(
                model, be, texture,
                ps, buffers, partialTicks, packedOverlay,
                opts
        );
    }
}