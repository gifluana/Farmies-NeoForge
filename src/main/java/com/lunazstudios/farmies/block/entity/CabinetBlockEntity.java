package com.lunazstudios.farmies.block.entity;

import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CabinetBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> items;
    private ContainerOpenersCounter openersCounter;

    public enum AnimState { CLOSED, OPEN, OPENING, CLOSING }
    private static final int OPENING_DURATION_T = 10;
    private static final int CLOSING_DURATION_T = 10;

    public static float openingDurationSec() { return OPENING_DURATION_T / 20f; }
    public static float closingDurationSec() { return CLOSING_DURATION_T / 20f; }

    private AnimState animState = AnimState.CLOSED;
    private long animStartGameTime = 0L;

    public CabinetBlockEntity(BlockPos pos, BlockState blockState) {
        super(FBlockEntities.CABINET_BE.get(), pos, blockState);
        this.items = NonNullList.withSize(27, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {
            protected void onOpen(Level level, BlockPos pos, BlockState state) {
                CabinetBlockEntity.this.playSound(FSounds.WOODEN_DOOR_OPENING.get());
                CabinetBlockEntity.this.beginOpening();
            }

            protected void onClose(Level level, BlockPos pos, BlockState state) {
                CabinetBlockEntity.this.playSound(FSounds.WOODEN_DOOR_CLOSING.get());
                CabinetBlockEntity.this.beginClosing();
            }

            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int count, int openCount) {
            }

            protected boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof ChestMenu) {
                    Container container = ((ChestMenu) player.containerMenu).getContainer();
                    return container == CabinetBlockEntity.this;
                } else {
                    return false;
                }
            }
        };
    }

    public String getActiveClip() {
        return switch (animState) {
            case OPENING -> "opening";
            case CLOSING -> "closing";
            case OPEN    -> "open";
            case CLOSED  -> "closed";
        };
    }

    public float getTransitionProgress(float partialTick) {
        if (level == null) return 1f;
        long now = level.getGameTime();
        float delta;
        if (animState == AnimState.OPENING) {
            delta = (now - animStartGameTime + partialTick) / (float) OPENING_DURATION_T;
            return Math.min(1f, Math.max(0f, delta));
        } else if (animState == AnimState.CLOSING) {
            delta = (now - animStartGameTime + partialTick) / (float) CLOSING_DURATION_T;
            return Math.min(1f, Math.max(0f, delta));
        }
        return 1f;
    }

    public boolean isOpen()   { return animState == AnimState.OPEN; }
    public boolean isClosed() { return animState == AnimState.CLOSED; }

    private void beginOpening() {
        if (level == null) return;
        if (animState == AnimState.OPEN) return;

        animState = AnimState.OPENING;
        animStartGameTime = level.getGameTime();
        // Som "madeira abrindo"
        if (!level.isClientSide) {
            level.playSound(null, worldPosition, FSounds.WOODEN_DOOR_OPENING.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            sync();
        }
    }

    private void beginClosing() {
        if (level == null) return;
        if (animState == AnimState.CLOSED) return;

        animState = AnimState.CLOSING;
        animStartGameTime = level.getGameTime();
        if (!level.isClientSide) {
            level.playSound(null, worldPosition, FSounds.WOODEN_DOOR_CLOSING.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
            sync();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CabinetBlockEntity be) {
        if (level.isClientSide) return;

        long now = level.getGameTime();
        switch (be.animState) {
            case OPENING -> {
                if (now - be.animStartGameTime >= OPENING_DURATION_T) {
                    be.animState = AnimState.OPEN;
                    be.animStartGameTime = now;
                    be.sync();
                }
            }
            case CLOSING -> {
                if (now - be.animStartGameTime >= CLOSING_DURATION_T) {
                    be.animState = AnimState.CLOSED;
                    be.animStartGameTime = now;
                    be.sync();
                }
            }
            default -> {}
        }
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
        tag.putString("AnimState", animState.name());
        tag.putLong("AnimStart", animStartGameTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
        if (tag.contains("AnimState")) {
            try {
                this.animState = AnimState.valueOf(tag.getString("AnimState"));
            } catch (IllegalArgumentException ignored) {
                this.animState = AnimState.CLOSED;
            }
        }
        this.animStartGameTime = tag.getLong("AnimStart");
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> itemStacks) {
        this.items = itemStacks;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.farmies.cabinet");
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    void playSound(SoundEvent sound) {
        if (this.level == null) return;

        double x = (double) this.worldPosition.getX() + 0.5;
        double y = (double) this.worldPosition.getY() + 0.5;
        double z = (double) this.worldPosition.getZ() + 0.5;

        this.level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 1.0F, this.level.random.nextFloat() * 0.1F + 0.9F);
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
