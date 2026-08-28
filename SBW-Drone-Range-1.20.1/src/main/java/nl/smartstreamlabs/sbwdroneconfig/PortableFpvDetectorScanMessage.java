package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PortableFpvDetectorScanMessage(
        int range,
        List<PortableFpvDetectorTarget> targets,
        UUID selectedTargetId,
        boolean detectorActive,
        boolean videoEnabled
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortableFpvDetectorScanMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "portable_fpv_detector_scan"));

    public PortableFpvDetectorScanMessage {
        targets = targets == null ? List.of() : List.copyOf(targets);
    }

    public static PortableFpvDetectorScanMessage empty(boolean detectorActive) {
        return new PortableFpvDetectorScanMessage(0, List.of(), null, detectorActive, AddonConfig.portableFpvDetectorVideoEnabled());
    }

    public static void encode(PortableFpvDetectorScanMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.range());
        buffer.writeVarInt(message.targets().size());
        for (PortableFpvDetectorTarget target : message.targets()) {
            PortableFpvDetectorTarget.encode(target, buffer);
        }
        buffer.writeBoolean(message.selectedTargetId() != null);
        if (message.selectedTargetId() != null) {
            buffer.writeUUID(message.selectedTargetId());
        }
        buffer.writeBoolean(message.detectorActive());
        buffer.writeBoolean(message.videoEnabled());
    }

    public static PortableFpvDetectorScanMessage decode(FriendlyByteBuf buffer) {
        int range = buffer.readVarInt();
        int size = Math.max(0, Math.min(64, buffer.readVarInt()));
        List<PortableFpvDetectorTarget> targets = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            targets.add(PortableFpvDetectorTarget.decode(buffer));
        }
        UUID selectedTargetId = buffer.readBoolean() ? buffer.readUUID() : null;
        boolean detectorActive = buffer.readBoolean();
        boolean videoEnabled = buffer.readBoolean();
        return new PortableFpvDetectorScanMessage(range, targets, selectedTargetId, detectorActive, videoEnabled);
    }

    public static void handle(PortableFpvDetectorScanMessage message, IPayloadContext context) {
        context.enqueueWork(() -> DroneJammerClient.handleDetectorScan(message));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
