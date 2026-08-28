package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Basic upgrade/module layer built on top of the existing drone inventory.
 * Modules are considered installed when their item exists anywhere in the
 * drone inventory. This keeps the current drone terminal/inventory flow intact
 * while giving the addon a clean place to add more modules later.
 */
public final class DroneModuleSystem {
    private DroneModuleSystem() {
    }

    public static boolean hasInstalledModule(Entity drone, Item moduleItem) {
        if (drone == null || moduleItem == null || !SbwCompat.isDrone(drone)) {
            return false;
        }

        DroneInventoryContainer inventory = new DroneInventoryContainer(drone);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && stack.is(moduleItem)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasSpotlightModule(Entity drone) {
        return AddonConfig.enableSpotlightModule()
                && (!AddonConfig.spotlightRequiresModule() || hasInstalledModule(drone, AddonItems.SPOTLIGHT_MODULE.get()));
    }

    public static boolean hasFiberOpticSpoolUpgrade(Entity drone) {
        return AddonConfig.enableFiberOpticMode()
                && hasInstalledModule(drone, AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get());
    }
}
