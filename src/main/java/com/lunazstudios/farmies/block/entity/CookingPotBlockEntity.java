package com.lunazstudios.farmies.block.entity;

import com.lunazstudios.farmies.block.CookingPotBlock;
import com.lunazstudios.farmies.recipe.CookingPotRecipe;
import com.lunazstudios.farmies.recipe.CookingPotRecipeInput;
import com.lunazstudios.farmies.registry.FBlockEntities;
import com.lunazstudios.farmies.registry.FRecipes;
import com.lunazstudios.farmies.registry.FSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookingPotBlockEntity extends BlockEntity {
    public static final int SLOT_OUTPUT = 9;
    private static final int TOTAL_SLOTS = 10;

    public final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != SLOT_OUTPUT;
        }
    };

    private final IItemHandler universalHandler = new IItemHandler() {
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
            if (slot == SLOT_OUTPUT || stack.isEmpty()) return stack;

            if (!itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty()) return stack;

            ItemStack remaining = stack.copy();

            for (int i = 0; i < SLOT_OUTPUT; i++) {
                if (!isItemValid(i, remaining)) continue;
                remaining = itemHandler.insertItem(i, remaining, simulate);
                if (remaining.isEmpty()) break;
            }

            return remaining;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return (slot == SLOT_OUTPUT)
                    ? itemHandler.extractItem(slot, amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return itemHandler.getSlotLimit(slot);
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot != SLOT_OUTPUT && itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty();
        }
    };

    private final ContainerData data;
    private int progress = 0;
    private int maxProgress = 50;
    private final int DEFAULT_MAX_PROGRESS = 50;

    private boolean crafting = false;

    public CookingPotBlockEntity(BlockPos pos, BlockState state) {
        super(FBlockEntities.COOKING_POT_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> CookingPotBlockEntity.this.progress;
                    case 1 -> CookingPotBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int j) {
                switch (i) {
                    case 0: CookingPotBlockEntity.this.progress = j;
                    case 1: CookingPotBlockEntity.this.maxProgress = j;
                };
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public IItemHandler getItemHandler(@Nullable Direction direction) {
        return universalHandler;
    }

    public boolean isCrafting() {
        return crafting;
    }

    public void startCrafting() {
        this.crafting = true;
        this.progress = 0;
        setChanged();
        if (!level.isClientSide) {
            level.blockEvent(worldPosition, getBlockState().getBlock(), 1, 0);
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == 1) {

            if (level != null && level.isClientSide) {
                level.playLocalSound(
                        worldPosition.getX() + 0.5,
                        worldPosition.getY() + 0.1,
                        worldPosition.getZ() + 0.5,
                        FSounds.FRYING_PAN_FRYING.get(),
                        SoundSource.BLOCKS,
                        1.0f, 1.0f, false
                );
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public int getComparatorOutput() {
        int occupiedSlots = 0;
        for (int i = 0; i < SLOT_OUTPUT; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                occupiedSlots++;
            }
        }
        return occupiedSlots;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (crafting && hasRecipe() && isOutputSlotEmptyOrReceivable()) {
            progress++;
            setChanged(level, pos, state);

            if (progress >= maxProgress) {
                craftItem();
                resetProgress();
                crafting = false;
                level.setBlock(pos, state.setValue(CookingPotBlock.LIT, false), 3);
            }
        }
    }

    public ItemStack addItem(ItemStack stack) {
        for (int i = 0; i < SLOT_OUTPUT; i++) {
            if (itemHandler.getStackInSlot(i).isEmpty()) {
                itemHandler.setStackInSlot(i, stack);
                setChanged();
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }

    private void resetProgress() {
        this.progress = 0;
        this.maxProgress = DEFAULT_MAX_PROGRESS;

        for (int i = 0; i < SLOT_OUTPUT; i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private void craftItem() {
        Optional<RecipeHolder<CookingPotRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return;
        CookingPotRecipe r = recipe.get().value();

        for (int i = 0; i < SLOT_OUTPUT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    itemHandler.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }

        ItemStack result = r.outputItem().copy();
        ItemStack output = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, result);
        } else if (ItemStack.isSameItemSameComponents(output, result)) {
            output.grow(result.getCount());
        }
    }

    private boolean isOutputSlotEmptyOrReceivable() {
        ItemStack outputStack = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return outputStack.isEmpty() || outputStack.getCount() < outputStack.getMaxStackSize();
    }

    public boolean hasRecipe() {
        Optional<RecipeHolder<CookingPotRecipe>> recipe = getCurrentRecipe();
        if (recipe.isEmpty()) return false;

        ItemStack output = recipe.get().value().getResultItem(null);
        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeHolder<CookingPotRecipe>> getCurrentRecipe() {
        List<ItemStack> inputs = new ArrayList<>();
        for (int i = 0; i < SLOT_OUTPUT; i++) {
            inputs.add(itemHandler.getStackInSlot(i));
        }
        return level.getRecipeManager().getRecipeFor(
                FRecipes.COOKING_POT_TYPE.get(),
                new CookingPotRecipeInput(inputs),
                level
        );
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        ItemStack current = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return current.isEmpty() || current.getItem() == output.getItem();
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        ItemStack current = itemHandler.getStackInSlot(SLOT_OUTPUT);
        int maxCount = current.isEmpty() ? 64 : current.getMaxStackSize();
        return maxCount >= current.getCount() + count;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.progress = tag.getInt("cooking_pot.progress");
        this.maxProgress = tag.getInt("cooking_pot.max_progress");
        this.itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.putInt("cooking_pot.progress", this.progress);
        tag.putInt("cooking_pot.max_progress", this.maxProgress);
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
}
