package com.lunazstudios.farmies.block.entity;

import com.lunazstudios.farmies.registry.FBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends BlockEntity {
    private String activeClip = "idle";
    private boolean loop = true;
    private long clipStartGameTime = 0L;

    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(FBlockEntities.TEST_BLOCK_BE.get(), pos, blockState);
    }

    public String getActiveClip() { return activeClip; }
    public boolean isLooping() { return loop; }
    public long getClipStartGameTime() { return clipStartGameTime; }

    public void setClip(String clip, boolean loop) {
        this.activeClip = clip;
        this.loop = loop;
        this.clipStartGameTime = (level != null) ? level.getGameTime() : 0L;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void toggleClip() {
        if ("idle".equals(activeClip)) setClip("on", true);
        else setClip("idle", true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Clip", activeClip);
        tag.putBoolean("Loop", loop);
        tag.putLong("ClipStartGT", clipStartGameTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        activeClip = tag.getString("Clip");
        loop = tag.getBoolean("Loop");
        clipStartGameTime = tag.getLong("ClipStartGT");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
