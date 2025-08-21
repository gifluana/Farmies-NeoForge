package com.lunazstudios.farmies.block.entity;

import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TestBlockEntity extends BlockEntity {
    private String targetState = "closed";

    public TestBlockEntity(BlockPos pos, BlockState blockState) {
        super(FBlockEntities.TEST_BLOCK_BE.get(), pos, blockState);
    }

    public String getTargetState() { return targetState; }

    public void toggleServer(long nowGT) {
        boolean opening = targetState.equals("closed");

        targetState = opening ? "open" : "closed";
        setChanged();

        if (!level.isClientSide) {
            if (opening) {
                level.playSound(
                        null,
                        worldPosition,
                        FSounds.WOODEN_DOOR_OPENING.get(),
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                );
            } else {
                level.playSound(
                        null,
                        worldPosition,
                        FSounds.WOODEN_DOOR_CLOSING.get(),
                        SoundSource.BLOCKS,
                        1.0f,
                        1.0f
                );
            }

            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.saveAdditional(tag, r);
        tag.putString("TargetState", targetState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider r) {
        super.loadAdditional(tag, r);
        targetState = tag.getString("TargetState");
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
