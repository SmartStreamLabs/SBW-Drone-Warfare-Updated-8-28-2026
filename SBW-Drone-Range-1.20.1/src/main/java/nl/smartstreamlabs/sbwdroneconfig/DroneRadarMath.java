package nl.smartstreamlabs.sbwdroneconfig;

final class DroneRadarMath {
    private DroneRadarMath() {
    }

    static int signalStrengthPercent(double distance, double range) {
        if (range <= 0.0D) {
            return 0;
        }
        double normalized = clamp01(1.0D - (distance / range));
        return (int) Math.round(normalized * 100.0D);
    }

    static int beepIntervalTicks(double distance, int minIntervalTicks, int maxIntervalTicks) {
        int min = Math.max(1, minIntervalTicks);
        int max = Math.max(min, maxIntervalTicks);
        if (distance < 20.0D) {
            return min;
        }
        if (distance < 50.0D) {
            return clamp(12, min, max);
        }
        if (distance < 200.0D) {
            return clamp(25, min, max);
        }
        return max;
    }

    static float beepPitch(double distance, double range) {
        double closeness = range <= 0.0D ? 0.0D : clamp01(1.0D - (distance / range));
        return (float) (0.80D + (closeness * 0.85D));
    }

    static float beepVolume(double distance, double range, double configuredVolume) {
        double closeness = range <= 0.0D ? 0.0D : clamp01(1.0D - (distance / range));
        return (float) Math.max(0.0D, configuredVolume * (0.45D + (closeness * 0.85D)));
    }

    static RadarMode radarMode(int droneCount, double closestDistance) {
        if (droneCount <= 0) {
            return RadarMode.SCAN;
        }
        return closestDistance < 20.0D ? RadarMode.OVERLOAD : RadarMode.JAM;
    }

    static Blip blip(double offsetX, double offsetZ, double playerYawDegrees, double range, int radius) {
        if (range <= 0.0D || radius <= 0) {
            return new Blip(0, 0, 0.0D, 0.0D);
        }

        double yawRadians = Math.toRadians(playerYawDegrees);
        double forwardX = -Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        double rightX = Math.cos(yawRadians);
        double rightZ = Math.sin(yawRadians);
        double relativeForward = (offsetX * forwardX) + (offsetZ * forwardZ);
        double relativeRight = (offsetX * rightX) + (offsetZ * rightZ);
        double scale = radius / range;
        double x = relativeRight * scale;
        double y = -relativeForward * scale;
        double length = Math.hypot(x, y);
        if (length > radius) {
            double clampScale = radius / length;
            x *= clampScale;
            y *= clampScale;
        }
        return new Blip((int) Math.round(x), (int) Math.round(y), relativeForward, relativeRight);
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    enum RadarMode {
        SCAN,
        JAM,
        OVERLOAD
    }

    record Blip(int x, int y, double relativeForward, double relativeRight) {
    }
}
