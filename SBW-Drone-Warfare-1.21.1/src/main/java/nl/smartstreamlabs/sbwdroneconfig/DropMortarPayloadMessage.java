package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record DropMortarPayloadMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DropMortarPayloadMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drop_mortar_payload"));

    public static void encode(DropMortarPayloadMessage message, FriendlyByteBuf buffer) {
    }

    public static DropMortarPayloadMessage decode(FriendlyByteBuf buffer) {
        return new DropMortarPayloadMessage();
    }

    public static void handle(DropMortarPayloadMessage message, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            DronePayloadSystem.dropMortarPayload(player);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
