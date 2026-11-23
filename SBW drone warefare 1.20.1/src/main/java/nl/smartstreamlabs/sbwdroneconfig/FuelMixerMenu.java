package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class FuelMixerMenu extends AbstractContainerMenu {
    private final Container container;
    private final ContainerData data;

    public FuelMixerMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory, getBlockEntity(inventory, buffer));
    }

    private FuelMixerMenu(int containerId, Inventory inventory, FuelMixerBlockEntity blockEntity) {
        this(containerId, inventory, blockEntity, blockEntity.getContainerData());
    }

    public FuelMixerMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(AddonMenus.FUEL_MIXER.get(), containerId);
        checkContainerSize(container, FuelMixerBlockEntity.CONTAINER_SIZE);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        this.container.startOpen(inventory.player);

        addSlot(new IngredientSlot(container, FuelMixerBlockEntity.SLOT_COAL, 44, 26, FuelMixerBlockEntity::isCoalIngredient));
        addSlot(new IngredientSlot(container, FuelMixerBlockEntity.SLOT_BLAZE_POWDER, 44, 48, FuelMixerBlockEntity::isBlazePowderIngredient));
        addSlot(new IngredientSlot(container, FuelMixerBlockEntity.SLOT_REDSTONE, 44, 70, FuelMixerBlockEntity::isRedstoneIngredient));
        addSlot(new FuelMixerOutputSlot(inventory.player, container, FuelMixerBlockEntity.SLOT_OUTPUT, 130, 48));

        addPlayerInventory(inventory);
        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getProcessTime() {
        return data.get(1);
    }

    public int getScaledProgress(int width) {
        int processTime = Math.max(1, getProcessTime());
        return Math.min(width, Math.max(0, (getProgress() * width) / processTime));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack moved = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return moved;
        }

        ItemStack stack = slot.getItem();
        moved = stack.copy();
        if (index < FuelMixerBlockEntity.CONTAINER_SIZE) {
            if (!moveItemStackTo(stack, FuelMixerBlockEntity.CONTAINER_SIZE, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (FuelMixerBlockEntity.isCoalIngredient(stack)) {
            if (!moveItemStackTo(stack, FuelMixerBlockEntity.SLOT_COAL, FuelMixerBlockEntity.SLOT_COAL + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (FuelMixerBlockEntity.isBlazePowderIngredient(stack)) {
            if (!moveItemStackTo(stack, FuelMixerBlockEntity.SLOT_BLAZE_POWDER, FuelMixerBlockEntity.SLOT_BLAZE_POWDER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (FuelMixerBlockEntity.isRedstoneIngredient(stack)) {
            if (!moveItemStackTo(stack, FuelMixerBlockEntity.SLOT_REDSTONE, FuelMixerBlockEntity.SLOT_REDSTONE + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return moved;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 98 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 156));
        }
    }

    private static FuelMixerBlockEntity getBlockEntity(Inventory inventory, FriendlyByteBuf buffer) {
        BlockPos pos = buffer.readBlockPos();
        BlockEntity blockEntity = inventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof FuelMixerBlockEntity mixer) {
            return mixer;
        }
        throw new IllegalStateException("Fuel Mixer block entity missing at " + pos);
    }

    private static final class IngredientSlot extends Slot {
        private final Predicate<ItemStack> ingredientPredicate;

        private IngredientSlot(Container container, int slot, int x, int y, Predicate<ItemStack> ingredientPredicate) {
            super(container, slot, x, y);
            this.ingredientPredicate = ingredientPredicate;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ingredientPredicate.test(stack);
        }
    }

    private static final class FuelMixerOutputSlot extends Slot {
        private final Player player;

        private FuelMixerOutputSlot(Player player, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            this.player.awardStat(net.minecraft.stats.Stats.ITEM_CRAFTED.get(AddonItems.GASOLINE_CANISTER.get()));
        }
    }
}
