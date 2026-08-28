package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleSpotlightMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ToggleSpotlightMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "toggle_spotlight"));

    public static void encode(ToggleSpotlightMessage message, FriendlyByteBuf buffer) {
    }

    public static ToggleSpotlightMessage decode(FriendlyByteBuf buffer) {
        return new ToggleSpotlightMessage();
    }

    public static void handle(ToggleSpotlightMessage message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            DroneSpotlightSystem.toggleSpotlight(player);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
