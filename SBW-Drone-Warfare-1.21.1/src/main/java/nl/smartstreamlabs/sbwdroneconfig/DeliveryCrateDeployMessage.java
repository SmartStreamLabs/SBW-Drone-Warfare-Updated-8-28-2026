package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record DeliveryCrateDeployMessage() {
    public static void encode(DeliveryCrateDeployMessage message, FriendlyByteBuf buffer) {
    }

    public static DeliveryCrateDeployMessage decode(FriendlyByteBuf buffer) {
        return new DeliveryCrateDeployMessage();
    }

    public static void handle(DeliveryCrateDeployMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            DeliveryDroneEntity drone = findControlledDeliveryDrone(player);
            if (drone == null) {
                player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_no_drone"), true);
                return;
            }

            if (!drone.hasMountedExtractionCrate()) {
                player.displayClientMessage(Component.translatable("message.sbwdroneconfig.delivery_crate_missing"), true);
                return;
            }

            drone.tryDeployMountedExtractionCrate(player);
        });
        context.setPacketHandled(true);
    }

    private static DeliveryDroneEntity findControlledDeliveryDrone(ServerPlayer player) {
        Entity active = SbwCompat.findActiveLinkedDrone(player);
        if (active instanceof DeliveryDroneEntity deliveryDrone && SbwCompat.isUsingLinkedMonitorForDrone(player, deliveryDrone)) {
            return deliveryDrone;
        }
        return null;
    }
}
