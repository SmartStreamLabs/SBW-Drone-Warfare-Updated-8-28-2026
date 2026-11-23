package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;

public final class DroneInventorySystem {
    private static final String TAG_HOPPER_TRANSFER_COOLDOWN = "sbwdroneconfigDroneHopperCooldown";

    private DroneInventorySystem() {
    }

    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onDroneInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();if(PlatformInventory.editingDrone(target)){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);return;}if(target instanceof Gad01Entity || target instanceof Sd01Entity)return;

        if (event.getHand() != InteractionHand.MAIN_HAND || player.level().isClientSide() || !SbwCompat.isDrone(target)) {
            return;
        }

        if (target instanceof DeliveryDroneEntity deliveryDrone
                && DeliveryDroneEntity.isCargoAction(player)
                && deliveryDrone.tryDeployMountedExtractionCrate(player)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(false));
            return;
        }

        if (player.isCrouching() || player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = player.getItemInHand(event.getHand());
        if (!stack.isEmpty() || SbwCompat.isMonitor(stack) || SbwCompat.isSbwBattery(stack)) {
            return;
        }

        DroneInventoryContainer container = new DroneInventoryContainer(target);
        MenuProvider provider = new SimpleMenuProvider(
                (windowId, playerInventory, ignored) -> ChestMenu.threeRows(windowId, playerInventory, container),
                inventoryTitleFor(target)
        );

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(provider);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(false));
        }
    }

    public static void onDroneBaseTick(Entity drone) {if(PlatformInventory.editingDrone(drone))return;if(drone instanceof Gad01Entity || drone instanceof Sd01Entity)return;
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone)) {
            return;
        }

        int cooldown = drone.getPersistentData().getInt(TAG_HOPPER_TRANSFER_COOLDOWN);
        if (cooldown > 0) {
            drone.getPersistentData().putInt(TAG_HOPPER_TRANSFER_COOLDOWN, cooldown - 1);
            return;
        }

        ExtractionCrateBlockEntity crate = findExtractionCrateBelow(serverLevel, drone);
        if (crate != null) {
            DroneInventoryContainer container = new DroneInventoryContainer(drone);
            if (!container.isEmpty() && crate.extractAllFromDrone(container)) {
                container.saveToDrone();
                drone.getPersistentData().putInt(TAG_HOPPER_TRANSFER_COOLDOWN, 8);
                return;
            }
        }

        HopperBlockEntity hopper = findHopperBelow(serverLevel, drone);
        if (hopper == null) {
            return;
        }

        DroneInventoryContainer container = new DroneInventoryContainer(drone);
        if (container.isEmpty()) {
            return;
        }

        if (transferOneItemToHopper(container, hopper)) {
            container.saveToDrone();
            drone.getPersistentData().putInt(TAG_HOPPER_TRANSFER_COOLDOWN, 8);
        }
    }

    private static ExtractionCrateBlockEntity findExtractionCrateBelow(ServerLevel level, Entity drone) {
        double minX = drone.getBoundingBox().minX - 0.35D;
        double maxX = drone.getBoundingBox().maxX + 0.35D;
        double minZ = drone.getBoundingBox().minZ - 0.35D;
        double maxZ = drone.getBoundingBox().maxZ + 0.35D;
        int startX = BlockPos.containing(minX, 0.0D, 0.0D).getX();
        int endX = BlockPos.containing(maxX, 0.0D, 0.0D).getX();
        int startZ = BlockPos.containing(0.0D, 0.0D, minZ).getZ();
        int endZ = BlockPos.containing(0.0D, 0.0D, maxZ).getZ();
        int footY = BlockPos.containing(drone.getX(), drone.getBoundingBox().minY, drone.getZ()).getY();
        int topY = footY;
        int bottomY = footY - 3;

        for (int y = topY; y >= bottomY; y--) {
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));
                    if (blockEntity instanceof ExtractionCrateBlockEntity crate) {
                        return crate;
                    }
                }
            }
        }
        return null;
    }

    private static HopperBlockEntity findHopperBelow(ServerLevel level, Entity drone) {
        double minX = drone.getBoundingBox().minX - 0.35D;
        double maxX = drone.getBoundingBox().maxX + 0.35D;
        double minZ = drone.getBoundingBox().minZ - 0.35D;
        double maxZ = drone.getBoundingBox().maxZ + 0.35D;
        int startX = BlockPos.containing(minX, 0.0D, 0.0D).getX();
        int endX = BlockPos.containing(maxX, 0.0D, 0.0D).getX();
        int startZ = BlockPos.containing(0.0D, 0.0D, minZ).getZ();
        int endZ = BlockPos.containing(0.0D, 0.0D, maxZ).getZ();
        int footY = BlockPos.containing(drone.getX(), drone.getBoundingBox().minY, drone.getZ()).getY();
        int topY = footY;
        int bottomY = footY - 3;

        for (int y = topY; y >= bottomY; y--) {
            for (int x = startX; x <= endX; x++) {
                for (int z = startZ; z <= endZ; z++) {
                    BlockEntity blockEntity = level.getBlockEntity(new BlockPos(x, y, z));
                    if (blockEntity instanceof HopperBlockEntity hopper) {
                        return hopper;
                    }
                }
            }
        }
        return null;
    }

    private static boolean transferOneItemToHopper(DroneInventoryContainer container, HopperBlockEntity hopper) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (insertOneItemIntoHopper(hopper, stack.copyWithCount(1))) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    container.setItem(slot, ItemStack.EMPTY);
                } else {
                    container.setItem(slot, stack);
                }
                return true;
            }
        }

        return false;
    }

    private static boolean insertOneItemIntoHopper(HopperBlockEntity hopper, ItemStack singleItem) {
        for (int slot = 0; slot < hopper.getContainerSize(); slot++) {
            ItemStack hopperStack = hopper.getItem(slot);
            if (!canPlaceInHopper(hopper, slot, singleItem)) {
                continue;
            }

            if (hopperStack.isEmpty()) {
                hopper.setItem(slot, singleItem.copy());
                return true;
            }

            if (ItemStack.isSameItemSameComponents(hopperStack, singleItem) && hopperStack.getCount() < hopperStack.getMaxStackSize()) {
                hopperStack.grow(1);
                hopper.setItem(slot, hopperStack);
                return true;
            }
        }
        return false;
    }

    private static boolean canPlaceInHopper(HopperBlockEntity hopper, int slot, ItemStack stack) {
        return hopper.canPlaceItem(slot, stack);
    }

    private static Component inventoryTitleFor(Entity target) {
        if (target instanceof DeliveryDroneEntity) {
            return Component.translatable("screen.sbwdroneconfig.delivery_drone_inventory");
        }
        if (target instanceof HeavyBomberDroneEntity) {
            return Component.translatable("screen.sbwdroneconfig.heavy_bomber_drone_inventory");
        }
        if (target instanceof Geranium2Entity) {
            return Component.translatable("screen.sbwdroneconfig.geranium_2_inventory");
        }
        if (target instanceof BayraktarTb2Entity) {
            return Component.translatable("screen.sbwdroneconfig.bayraktar_tb2_inventory");
        }
        if (target instanceof LucasDroneEntity) {
            return Component.translatable("screen.sbwdroneconfig.lucas_drone_inventory");
        }
        return Component.translatable("screen.sbwdroneconfig.drone_inventory");
    }
}
