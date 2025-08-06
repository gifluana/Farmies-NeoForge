package com.lunazstudios.farmies.block.entity;

import com.lunazstudios.farmies.energy.FEnergyStorage;
import com.lunazstudios.farmies.recipe.GrinderRecipe;
import com.lunazstudios.farmies.recipe.GrinderRecipeInput;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FRecipes;
import com.lunazstudios.farmies.screen.GrinderMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class GrinderBlockEntity extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INPUT_SLOT;
        }
    };

    private final IItemHandler topHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot == INPUT_SLOT ? itemHandler.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == INPUT_SLOT;
        }
    };

    private final IItemHandler bottomHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot == OUTPUT_SLOT || slot == OUTPUT_EXTRA_SLOT) {
                return itemHandler.extractItem(slot, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;
    private static final int OUTPUT_EXTRA_SLOT = 2;

    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 50;
    private final int DEFAULT_MAX_PROGRESS = 50;

    private static final int ENERGY_CRAFT_AMOUNT = 25;

    private final FEnergyStorage ENERGY_STORAGE = createEnergyStorage();
    private FEnergyStorage createEnergyStorage() {
        return new FEnergyStorage(64000, 320) {
            @Override
            public void onEnergyChanged() {
                setChanged();
                getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        };
    }

    public float cogRotation = 0f;
    public float cogSpeed = 0f;

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.GRINDER_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> GrinderBlockEntity.this.progress;
                    case 1 -> GrinderBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int j) {
                switch (i) {
                    case 0: GrinderBlockEntity.this.progress = j;
                    case 1: GrinderBlockEntity.this.maxProgress = j;
                };
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public boolean isAnimating() {
        return progress > 0 || (hasInput() && ENERGY_STORAGE.getEnergyStored() > 0);
    }

    private boolean hasInput() {
        return !itemHandler.getStackInSlot(INPUT_SLOT).isEmpty();
    }

    public IEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        return this.ENERGY_STORAGE;
    }

    public IItemHandler getItemHandler(@Nullable Direction direction) {
        if (direction == Direction.UP) {
            return topHandler;
        } else if (direction == Direction.DOWN) {
            return bottomHandler;
        } else {
            return null;
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        if (hasRecipe() && isOutputSlotEmptyOrReceivable()) {
            increaseCraftingProgress();
            useEnergyForCrafting();
            setChanged(level, pos, state);

            if (hasCraftingFinished()) {
                craftItem();
                resetProgress();
            }
        } else {
            resetProgress();
        }
    }

    public void clientTick(Level level, BlockPos pos, BlockState state) {
        boolean animating = isAnimating();

        float targetSpeed = animating ? 50f : 0f;
        float accel = 0.5f;

        if (cogSpeed < targetSpeed) {
            cogSpeed = Math.min(cogSpeed + accel, targetSpeed);
        } else if (cogSpeed > targetSpeed) {
            cogSpeed = Math.max(cogSpeed - accel, targetSpeed);
        }

        cogRotation += cogSpeed;
        if (cogRotation > 360f) cogRotation -= 360f;
    }

    private void useEnergyForCrafting() {
        this.ENERGY_STORAGE.extractEnergy(ENERGY_CRAFT_AMOUNT, false);
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;
    }

    private void craftItem() {
        Optional<RecipeHolder<GrinderRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;
        GrinderRecipe r = recipe.get().value();

        itemHandler.extractItem(INPUT_SLOT, 1, false);

        mergeStack(OUTPUT_SLOT, r.outputItem());

        if (!r.extraOutput().isEmpty() && level.random.nextFloat() < r.extraChance()) {
            mergeStack(OUTPUT_EXTRA_SLOT, r.extraOutput());
        }
    }

    private void mergeStack(int slot, ItemStack stackToAdd) {
        ItemStack existing = itemHandler.getStackInSlot(slot);

        if (existing.isEmpty()) {
            itemHandler.setStackInSlot(slot, stackToAdd.copy());
        } else if (ItemStack.isSameItemSameComponents(existing, stackToAdd)) {
            int space = existing.getMaxStackSize() - existing.getCount();
            int toMove = Math.min(space, stackToAdd.getCount());
            if (toMove > 0) {
                existing.grow(toMove);
            }
        }
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean isOutputSlotEmptyOrReceivable() {
        return this.itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                this.itemHandler.getStackInSlot(OUTPUT_SLOT).getCount() < this.itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<GrinderRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) {
            return false;
        }

        ItemStack output = recipe.get().value().getResultItem(null);
        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output) && hasEnoughEnergyToCraft();
    }

    private boolean hasEnoughEnergyToCraft() {
        return this.ENERGY_STORAGE.getEnergyStored() >= ENERGY_CRAFT_AMOUNT * maxProgress;
    }

    private Optional<RecipeHolder<GrinderRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager().getRecipeFor(FRecipes.GRINDER_TYPE.get(), new GrinderRecipeInput(itemHandler.getStackInSlot(INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ||
                itemHandler.getStackInSlot(OUTPUT_SLOT).getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        this.progress = tag.getInt("grinder.progress");
        this.maxProgress = tag.getInt("grinder.max_progress");
        this.itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));

        this.ENERGY_STORAGE.setEnergy(tag.getInt("grinder.energy"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.putInt("grinder.progress", this.progress);
        tag.putInt("grinder.max_progress", this.maxProgress);

        tag.putInt("grinder.energy", ENERGY_STORAGE.getEnergyStored());

        super.saveAdditional(tag, provider);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithFullMetadata(provider);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inv.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("blockentity.farmies.grinder");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new GrinderMenu(i, inventory, this, this.data);
    }
}
