package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToggleSpotlightMessage() {
    public static void encode(ToggleSpotlightMessage message, FriendlyByteBuf buffer) {
    }

    public static ToggleSpotlightMessage decode(FriendlyByteBuf buffer) {
        return new ToggleSpotlightMessage();
    }

    public static void handle(ToggleSpotlightMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                DroneSpotlightSystem.toggleSpotlight(player);
            }
        });
        context.setPacketHandled(true);
    }
}
