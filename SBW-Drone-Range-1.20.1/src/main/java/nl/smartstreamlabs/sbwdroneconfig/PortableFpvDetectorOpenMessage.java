package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PortableFpvDetectorOpenMessage() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortableFpvDetectorOpenMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "portable_fpv_detector_open"));

    public static void encode(PortableFpvDetectorOpenMessage message, FriendlyByteBuf buffer) {
    }

    public static PortableFpvDetectorOpenMessage decode(FriendlyByteBuf buffer) {
        return new PortableFpvDetectorOpenMessage();
    }

    public static void handle(PortableFpvDetectorOpenMessage message, IPayloadContext context) {
        context.enqueueWork(DroneJammerClient::openDetectorScreen);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
