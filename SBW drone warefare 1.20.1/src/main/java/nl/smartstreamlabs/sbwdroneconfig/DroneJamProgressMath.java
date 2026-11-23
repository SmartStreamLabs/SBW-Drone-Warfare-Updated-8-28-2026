package nl.smartstreamlabs.sbwdroneconfig;

public final class DroneJamProgressMath {
    private DroneJamProgressMath() {
    }

    public static float computeInfluence(double distance, double range) {
        if (range <= 0.0D || distance >= range) {
            return 0.0F;
        }

        double normalized = 1.0D - (distance / range);
        return clampProgress((float) normalized);
    }

    public static float computeBuildStep(float influence, int buildUpTicks) {
        if (influence <= 0.0F) {
            return 0.0F;
        }
        return clampProgress(influence) / Math.max(1, buildUpTicks);
    }

    public static float computeRecoveryStep(int recoveryTicks) {
        return 1.0F / Math.max(1, recoveryTicks);
    }

    public static float clampProgress(float progress) {
        if (progress <= 0.0F) {
            return 0.0F;
        }
        if (progress >= 1.0F) {
            return 1.0F;
        }
        return progress;
    }
}
