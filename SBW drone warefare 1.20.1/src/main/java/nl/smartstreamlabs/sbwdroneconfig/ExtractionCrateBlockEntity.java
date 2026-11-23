package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ExtractionCrateBlockEntity extends RandomizableContainerBlockEntity {
    private static final String TAG_TRANSFER_COOLDOWN = "sbwdroneconfigTransferCooldown";
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    public ExtractionCrateBlockEntity(BlockPos pos, BlockState blockState) {
        super(AddonBlockEntities.EXTRACTION_CRATE.get(), pos, blockState);
    }

    public boolean extractAllFromDrone(DroneInventoryContainer droneInventory) {
        boolean changed = false;
        for (int slot = 0; slot < droneInventory.getContainerSize(); slot++) {
            ItemStack stack = droneInventory.getItem(slot);
            if (stack.isEmpty() || SbwCompat.isSbwBattery(stack)) {
                continue;
            }

            ItemStack remaining = stack.copy();
            for (int targetSlot = 0; targetSlot < getContainerSize() && !remaining.isEmpty(); targetSlot++) {
                remaining = insertIntoSlot(targetSlot, remaining);
            }

            int transferred = stack.getCount() - remaining.getCount();
            if (transferred > 0) {
                changed = true;
                if (remaining.isEmpty()) {
                    droneInventory.setItem(slot, ItemStack.EMPTY);
                } else {
                    droneInventory.setItem(slot, remaining);
                }
            }
        }

        if (changed) {
            setChanged();
        }
        return changed;
    }

    public CompoundTag saveContentsOnly() {
        CompoundTag tag = new CompoundTag();
        ContainerHelper.saveAllItems(tag, this.items);
        return tag;
    }

    public void loadContentsOnly(CompoundTag tag) {
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (tag != null && !tag.isEmpty()) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
        setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ExtractionCrateBlockEntity crate) {
        if (level.isClientSide()) {
            return;
        }

        int cooldown = crate.getPersistentData().getInt(TAG_TRANSFER_COOLDOWN);
        if (cooldown > 0) {
            crate.getPersistentData().putInt(TAG_TRANSFER_COOLDOWN, cooldown - 1);
            return;
        }

        AABB scanBox = new AABB(
                pos.getX() - 0.25D, pos.getY(), pos.getZ() - 0.25D,
                pos.getX() + 1.25D, pos.getY() + 2.8D, pos.getZ() + 1.25D
        );

        for (var entity : level.getEntities((net.minecraft.world.entity.Entity) null, scanBox, SbwCompat::isDrone)) {
            DroneInventoryContainer droneInventory = new DroneInventoryContainer(entity);
            if (droneInventory.isEmpty()) {
                continue;
            }

            if (crate.extractAllFromDrone(droneInventory)) {
                droneInventory.saveToDrone();
                crate.getPersistentData().putInt(TAG_TRANSFER_COOLDOWN, 8);
                break;
            }
        }
    }

    private ItemStack insertIntoSlot(int slot, ItemStack stack) {
        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) {
            this.items.set(slot, stack.copy());
            return ItemStack.EMPTY;
        }

        if (!ItemStack.isSameItemSameTags(existing, stack) || existing.getCount() >= existing.getMaxStackSize()) {
            return stack;
        }

        int move = Math.min(existing.getMaxStackSize() - existing.getCount(), stack.getCount());
        if (move <= 0) {
            return stack;
        }

        existing.grow(move);
        ItemStack remaining = stack.copy();
        remaining.shrink(move);
        return remaining;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.sbwdroneconfig.extraction_crate");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.threeRows(containerId, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }
}
