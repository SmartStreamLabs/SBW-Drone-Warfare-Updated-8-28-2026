package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.UUID;

final class CubedFpvReferenceGuiTelemetry {
    private static UUID sessionDroneId;
    private static int sessionTicks;

    private CubedFpvReferenceGuiTelemetry() {
    }

    static void onClientTick() {
        CubedFpvDroneEntity drone = DroneClientViewContext.activeCubedFpvDrone(Minecraft.getInstance());
        if (drone instanceof DeliveryDroneEntity || !DroneClientViewContext.isLocalFpvViewOf(drone)) {
            sessionDroneId = null;
            sessionTicks = 0;
            return;
        }

        UUID droneId = drone.getUUID();
        if (!droneId.equals(sessionDroneId)) {
            sessionDroneId = droneId;
            sessionTicks = 0;
        }
        sessionTicks++;
    }

    static Snapshot collect(Minecraft minecraft, float partialTick) {
        CubedFpvDroneEntity drone = DroneClientViewContext.activeCubedFpvDrone(minecraft);
        if (drone instanceof DeliveryDroneEntity || !DroneClientViewContext.isLocalFpvViewOf(drone)) {
            return null;
        }

        Vec3 velocity = drone.getDeltaMovement();
        Vec3 position = drone.getPosition(partialTick);
        double horizontalSpeed = Math.sqrt((velocity.x * velocity.x) + (velocity.z * velocity.z)) * 72.0D;
        float heading = normalizeDegrees(Mth.rotLerp(partialTick, drone.yRotO, drone.getYRot()));
        float cameraAngle = Mth.rotLerp(partialTick, drone.xRotO, drone.getXRot());
        boolean fiberActive = DroneFiberOpticClient.isFiberHudActiveFor(drone.getUUID());
        double batteryVoltage = DroneEnergyClient.getBatteryVoltageFor(drone.getUUID());
        double batteryPercent = DroneEnergyClient.getBatteryPercentFor(drone.getUUID());
        float jamProgress = DroneJamOverlayClient.getJamProgressFor(drone.getUUID());
        int linkQuality = Mth.clamp(Math.round((1.0F - jamProgress) * 100.0F), 0, 100);
        boolean hardJammed = DroneJamOverlayClient.isHardJammed(drone.getUUID());
        String callsign = drone.hasCustomName() ? spacedCallsign(drone.getName().getString()) : "C U B E D   F P V";

        return new Snapshot(
                drone.getUUID(),
                heading,
                drone.engineRunning(),
                Math.max(0, sessionTicks / 20),
                horizontalSpeed,
                position.y,
                cameraAngle,
                batteryVoltage,
                batteryPercent,
                fiberActive,
                linkQuality,
                hardJammed,
                CubedFpvDroneRenderer.hasMountedPayload(drone),
                callsign
        );
    }

    private static float normalizeDegrees(float degrees) {
        double normalizedHeading = ((degrees % 360.0) + 360.0) % 360.0;
        return (float) normalizedHeading;
    }

    private static String spacedCallsign(String value) {
        String compact = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (compact.isBlank()) {
            return "C U B E D   F P V";
        }
        compact = compact.length() > 16 ? compact.substring(0, 16) : compact;

        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < compact.length(); index++) {
            char character = compact.charAt(index);
            if (Character.isWhitespace(character)) {
                builder.append("   ");
            } else {
                if (!builder.isEmpty() && builder.charAt(builder.length() - 1) != ' ') {
                    builder.append(' ');
                }
                builder.append(character);
            }
        }
        return builder.toString();
    }

    record Snapshot(
            UUID droneId,
            float heading,
            boolean armed,
            int sessionSeconds,
            double horizontalSpeed,
            double altitude,
            double cameraAngle,
            double batteryVoltage,
            double batteryPercent,
            boolean fiberActive,
            int linkQuality,
            boolean hardJammed,
            boolean payloadMounted,
            String callsign
    ) {
        boolean hasBatteryVoltage() {
            return !Double.isNaN(batteryVoltage);
        }

        boolean hasBatteryPercent() {
            return !Double.isNaN(batteryPercent);
        }

        String armText() {
            return armed ? "ARM 1" : "ARM 0";
        }

        String timerText() {
            int minutes = sessionSeconds / 60;
            int seconds = sessionSeconds % 60;
            return String.format(Locale.ROOT, "TMR %02d:%02d", minutes, seconds);
        }

        String headingText() {
            double normalizedHeading = ((heading % 360.0) + 360.0) % 360.0;
            int displayHeading = (int) Math.floor(normalizedHeading + 0.5) % 360;
            return String.format(Locale.ROOT, "%03d", displayHeading);
        }

        String speedText() {
            return String.format(Locale.ROOT, "S %03d", Mth.clamp((int) Math.round(horizontalSpeed), 0, 999));
        }

        String altitudeText() {
            return String.format(Locale.ROOT, "H %03d", Mth.clamp((int) Math.round(altitude), 0, 999));
        }

        String angleText() {
            return String.format(Locale.ROOT, "A %.1f\u00B0", cameraAngle);
        }

        String batteryText() {
            return hasBatteryVoltage() ? String.format(Locale.ROOT, "%.1fV", batteryVoltage) : "--.-V";
        }

        String secondaryBatteryText() {
            return hasBatteryPercent() ? String.format(Locale.ROOT, "%02d%%", Mth.clamp((int) Math.round(batteryPercent), 0, 100)) : "--%";
        }

        int batteryColor() {
            if (!hasBatteryVoltage()) {
                return CubedFpvReferenceGuiTextRenderer.WHITE;
            }
            if (batteryVoltage <= 14.2D) {
                return CubedFpvReferenceGuiTextRenderer.CRITICAL;
            }
            if (batteryVoltage <= 14.7D) {
                return CubedFpvReferenceGuiTextRenderer.CAUTION;
            }
            return CubedFpvReferenceGuiTextRenderer.WHITE;
        }

        String linkModeText() {
            return fiberActive ? "F I B E R" : "W I R E L E S S";
        }

        String linkQualityText() {
            return String.format(Locale.ROOT, "LQ %03d", linkQuality);
        }

        String rssiText() {
            return fiberActive ? "FIBER" : "RSSI --";
        }

        String latencyText() {
            return "LAT --";
        }

        int linkColor() {
            if (hardJammed || linkQuality <= 15) {
                return CubedFpvReferenceGuiTextRenderer.CRITICAL;
            }
            if (linkQuality <= 45) {
                return CubedFpvReferenceGuiTextRenderer.CAUTION;
            }
            return CubedFpvReferenceGuiTextRenderer.WHITE;
        }

        String flightModeText() {
            return "A C R O";
        }

        String payloadText() {
            return payloadMounted ? "P A Y L O A D" : "";
        }
    }
}
