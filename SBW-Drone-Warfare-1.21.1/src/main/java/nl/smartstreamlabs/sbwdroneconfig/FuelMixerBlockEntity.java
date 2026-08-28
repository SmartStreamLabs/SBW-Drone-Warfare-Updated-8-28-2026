package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FuelMixerBlockEntity extends RandomizableContainerBlockEntity {
    public static final int SLOT_COAL = 0;
    public static final int SLOT_BLAZE_POWDER = 1;
    public static final int SLOT_REDSTONE = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int CONTAINER_SIZE = 4;

    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private int progress;
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> FuelMixerBlockEntity.this.progress;
                case 1 -> AddonConfig.fuelMixerProcessTime();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                FuelMixerBlockEntity.this.progress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public FuelMixerBlockEntity(BlockPos pos, BlockState blockState) {
        super(AddonBlockEntities.FUEL_MIXER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FuelMixerBlockEntity mixer) {
        if (level.isClientSide()) {
            return;
        }

        if (!mixer.canProcess()) {
            if (mixer.progress != 0) {
                mixer.progress = 0;
                mixer.setChanged();
            }
            setFuelMixerLit(level, pos, state, false);
            return;
        }

        setFuelMixerLit(level, pos, state, true);
        mixer.progress++;
        if (mixer.progress >= AddonConfig.fuelMixerProcessTime()) {
            mixer.consumeIngredients();
            mixer.produceCanister();
            mixer.progress = 0;
        }
        mixer.setChanged();
    }

    private static void setFuelMixerLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        if (state.hasProperty(FuelMixerBlock.LIT) && state.getValue(FuelMixerBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(FuelMixerBlock.LIT, lit), 3);
        }
    }

    public ContainerData getContainerData() {
        return dataAccess;
    }

    public int getProgress() {
        return progress;
    }

    public int getProcessTime() {
        return AddonConfig.fuelMixerProcessTime();
    }

    public boolean canProcess() {
        return isCoalIngredient(items.get(SLOT_COAL))
                && items.get(SLOT_BLAZE_POWDER).is(Items.BLAZE_POWDER)
                && items.get(SLOT_REDSTONE).is(Items.REDSTONE)
                && canOutputCanister();
    }

    public static boolean isCoalIngredient(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(Items.CHARCOAL);
    }

    public static boolean isBlazePowderIngredient(ItemStack stack) {
        return stack.is(Items.BLAZE_POWDER);
    }

    public static boolean isRedstoneIngredient(ItemStack stack) {
        return stack.is(Items.REDSTONE);
    }

    private boolean canOutputCanister() {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(AddonItems.GASOLINE_CANISTER.get()) && output.getCount() < output.getMaxStackSize();
    }

    private void consumeIngredients() {
        items.get(SLOT_COAL).shrink(1);
        items.get(SLOT_BLAZE_POWDER).shrink(1);
        items.get(SLOT_REDSTONE).shrink(1);
    }

    private void produceCanister() {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.set(SLOT_OUTPUT, new ItemStack(AddonItems.GASOLINE_CANISTER.get()));
        } else if (output.is(AddonItems.GASOLINE_CANISTER.get())) {
            output.grow(1);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_COAL -> isCoalIngredient(stack);
            case SLOT_BLAZE_POWDER -> isBlazePowderIngredient(stack);
            case SLOT_REDSTONE -> isRedstoneIngredient(stack);
            default -> false;
        };
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.sbwdroneconfig.fuel_mixer");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new FuelMixerMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
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
    public boolean stillValid(Player player) {
        return this.level != null && this.level.getBlockEntity(this.worldPosition) == this && player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("Progress", progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.progress = tag.getInt("Progress");
    }
}
