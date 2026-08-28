package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

enum NetBulkPlacementKind {
    ANTI_DRONE_NET("anti_drone_net"),
    ANTI_DRONE_NET_PANEL("anti_drone_net_panel"),
    ANTI_DRONE_NET_CARPET("anti_drone_net_carpet");

    private final String id;

    NetBulkPlacementKind(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    ResourceLocation itemId() {
        return new ResourceLocation(SbwDroneRangeConfig.MOD_ID, id);
    }

    Item item() {
        return switch (this) {
            case ANTI_DRONE_NET -> AddonItems.ANTI_DRONE_NET.get();
            case ANTI_DRONE_NET_PANEL -> AddonItems.ANTI_DRONE_NET_PANEL.get();
            case ANTI_DRONE_NET_CARPET -> AddonItems.ANTI_DRONE_NET_CARPET.get();
        };
    }

    Block block() {
        return switch (this) {
            case ANTI_DRONE_NET -> AddonBlocks.ANTI_DRONE_NET.get();
            case ANTI_DRONE_NET_PANEL -> AddonBlocks.ANTI_DRONE_NET_PANEL.get();
            case ANTI_DRONE_NET_CARPET -> AddonBlocks.ANTI_DRONE_NET_CARPET.get();
        };
    }

    boolean matches(ItemStack stack) {
        return stack != null && stack.is(item());
    }

    boolean isCarpet() {
        return this == ANTI_DRONE_NET_CARPET;
    }

    static Optional<NetBulkPlacementKind> fromItemStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        for (NetBulkPlacementKind kind : values()) {
            if (kind.matches(stack)) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }

    static Optional<NetBulkPlacementKind> fromItemId(ResourceLocation itemId) {
        if (itemId == null) {
            return Optional.empty();
        }
        for (NetBulkPlacementKind kind : values()) {
            if (kind.itemId().equals(itemId)) {
                return Optional.of(kind);
            }
        }
        return Optional.empty();
    }
}
