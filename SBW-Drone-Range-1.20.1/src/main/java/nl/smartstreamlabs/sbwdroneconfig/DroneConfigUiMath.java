package nl.smartstreamlabs.sbwdroneconfig;

import java.util.Locale;

public final class DroneConfigUiMath {
    private DroneConfigUiMath() {
    }

    public static double normalizedFromInt(int value, int min, int max) {
        if (max <= min) {
            return 0.0D;
        }
        int clamped = Math.max(min, Math.min(max, value));
        return (double) (clamped - min) / (double) (max - min);
    }

    public static int intFromNormalized(double normalized, int min, int max) {
        if (max <= min) {
            return min;
        }
        double clamped = clampNormalized(normalized);
        return (int) Math.round(min + clamped * (max - min));
    }

    public static double normalizedFromDouble(double value, double min, double max) {
        if (max <= min) {
            return 0.0D;
        }
        double clamped = Math.max(min, Math.min(max, value));
        return (clamped - min) / (max - min);
    }

    public static double doubleFromNormalized(double normalized, double min, double max) {
        if (max <= min) {
            return roundToTwoDecimals(min);
        }
        double clamped = clampNormalized(normalized);
        return roundToTwoDecimals(min + clamped * (max - min));
    }

    public static String toPercentLabel(double value) {
        return String.format(Locale.ROOT, "%d%%", Math.round(value * 100.0D));
    }

    private static double clampNormalized(double normalized) {
        return Math.max(0.0D, Math.min(1.0D, normalized));
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}
