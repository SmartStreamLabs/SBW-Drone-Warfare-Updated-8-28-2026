package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record PortableFpvDetectorTarget(
        UUID droneId,
        int entityId,
        String entityTypeId,
        String displayName,
        String ownerName,
        DroneLinkMode linkMode,
        boolean cameraAvailable,
        boolean active,
        double x,
        double y,
        double z,
        double distance,
        double signalStrength
) {
    public PortableFpvDetectorTarget {
        entityTypeId = sanitize(entityTypeId);
        displayName = sanitize(displayName);
        ownerName = sanitize(ownerName);
        linkMode = linkMode == null ? DroneLinkMode.WIRELESS : linkMode;
        distance = Math.max(0.0D, distance);
        signalStrength = clamp01(signalStrength);
    }

    public static void encode(PortableFpvDetectorTarget target, FriendlyByteBuf buffer) {
        buffer.writeUUID(target.droneId());
        buffer.writeVarInt(target.entityId());
        buffer.writeUtf(target.entityTypeId());
        buffer.writeUtf(target.displayName());
        buffer.writeUtf(target.ownerName());
        buffer.writeEnum(target.linkMode());
        buffer.writeBoolean(target.cameraAvailable());
        buffer.writeBoolean(target.active());
        buffer.writeDouble(target.x());
        buffer.writeDouble(target.y());
        buffer.writeDouble(target.z());
        buffer.writeDouble(target.distance());
        buffer.writeDouble(target.signalStrength());
    }

    public static PortableFpvDetectorTarget decode(FriendlyByteBuf buffer) {
        return new PortableFpvDetectorTarget(
                buffer.readUUID(),
                buffer.readVarInt(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readEnum(DroneLinkMode.class),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble()
        );
    }

    public int signalPercent() {
        return Math.max(0, Math.min(100, (int) Math.round(signalStrength * 100.0D)));
    }

    public String signalLabel() {
        if (signalStrength < 0.20D) {
            return "VERY WEAK";
        }
        if (signalStrength < 0.40D) {
            return "WEAK";
        }
        if (signalStrength < 0.65D) {
            return "MEDIUM";
        }
        if (signalStrength < 0.85D) {
            return "STRONG";
        }
        return "DANGER";
    }

    private static String sanitize(String value) {
        return value == null ? "" : value;
    }

    private static double clamp01(double value) {
        if (!Double.isFinite(value)) {
            return 0.0D;
        }
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
