package com.lunazstudios.farmies.client.renderer;

import com.lunazstudios.farmies.block.entity.TestBlockEntity;
import com.lunazstudios.farmies.client.animation.AnimationMachine;
import com.lunazstudios.farmies.client.runtime.BBModel;
import com.lunazstudios.farmies.client.runtime.BBRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class TestBlockRenderer implements BlockEntityRenderer<TestBlockEntity> {
    private final BBModel model;
    private final ResourceLocation texture;
    private final Map<BlockPos, AnimationMachine> machines = new HashMap<>();

    public TestBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        var json = ResourceLocation.fromNamespaceAndPath("farmies", "models/runtime/my_model.bbs.json");
        this.model = BBModel.load(json);
        this.texture = ResourceLocation.fromNamespaceAndPath("farmies", "textures/runtime/my_model.png");
    }

    private AnimationMachine getOrCreateMachine(TestBlockEntity be) {
        return machines.computeIfAbsent(be.getBlockPos(), p -> {
            AnimationMachine m = new AnimationMachine(model)
                    .state("closed", "closed")
                    .state("open",   "open")
                    .transition("closed","open","opening")
                    .transition("open","closed","closing");
            long now = be.getLevel() != null ? be.getLevel().getGameTime() : 0L;
            m.initAtState(be.getTargetState(), now);
            return m;
        });
    }

    @Override
    public void render(TestBlockEntity be, float partialTicks, PoseStack ps,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {

        var lvl = be.getLevel();
        if (lvl == null) return;
        long now = lvl.getGameTime();

        AnimationMachine m = getOrCreateMachine(be);

        if (!be.getTargetState().equals(m.getCurrentState())) {
            m.requestState(be.getTargetState(), now);
        }

        var pb = m.sample(now, partialTicks);

        var opts = BBRenderer.Opts.defaults()
                .clip(pb.clip)
                .loop(pb.loop)
                .center(true)
                .autoLight(true)
                .time(pb.timeSec);

        BBRenderer.render(model, be, texture, ps, buffers, partialTicks, packedOverlay, opts);
    }
}