package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Lightweight chest-like storage bound directly to the drone entity.
 * The contents are persisted in the drone's PersistentData so the menu can
 * reuse vanilla chest screens without adding a custom block or screen.
 */
public class DroneInventoryContainer extends SimpleContainer {
    public static final int SIZE = 27;
    private static final String TAG_DRONE_INVENTORY = "sbwdroneconfigDroneInventory";

    private final Entity drone;
    private boolean loading;

    public DroneInventoryContainer(Entity drone) {
        super(SIZE);
        this.drone = drone;
        loadFromDrone();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) {
            saveToDrone();
        }
    }

    public void loadFromDrone() {
        CompoundTag stored = drone.getPersistentData().getCompound(TAG_DRONE_INVENTORY);
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(stored, items);

        loading = true;
        try {
            clearContent();
            for (int i = 0; i < items.size(); i++) {
                setItem(i, items.get(i));
            }
        } finally {
            loading = false;
        }
    }

    public void saveToDrone() {
        NonNullList<ItemStack> items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, getItem(i));
        }

        CompoundTag stored = new CompoundTag();
        ContainerHelper.saveAllItems(stored, items);
        drone.getPersistentData().put(TAG_DRONE_INVENTORY, stored);
        FiberOpticLinkSystem.syncVisualStateForTrackingPlayers(drone);
    }
}
