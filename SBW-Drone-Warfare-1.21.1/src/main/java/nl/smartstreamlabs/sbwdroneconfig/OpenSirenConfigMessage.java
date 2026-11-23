package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record OpenSirenConfigMessage(BlockPos blockPos, List<String> blacklistedGamertags) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenSirenConfigMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "open_siren_config"));

    public static void encode(OpenSirenConfigMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeCollection(message.blacklistedGamertags, FriendlyByteBuf::writeUtf);
    }

    public static OpenSirenConfigMessage decode(FriendlyByteBuf buffer) {
        return new OpenSirenConfigMessage(buffer.readBlockPos(), buffer.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(OpenSirenConfigMessage message, IPayloadContext context) {
        DroneDetectionSirenScreen.open(message);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
