package nl.smartstreamlabs.sbwdroneconfig;

public final class FiberOpticLinkMath {
    private FiberOpticLinkMath() {
    }

    public static float updateTension(float currentTension, double cableLength, int maxCableLength, int breakDelayTicks) {
        float clampedCurrent = clampTension(currentTension);
        float step = computeTensionStep(breakDelayTicks);

        if (cableLength > Math.max(0, maxCableLength)) {
            return clampTension(clampedCurrent + step);
        }

        return clampTension(clampedCurrent - step);
    }

    public static float computeTensionStep(int breakDelayTicks) {
        return 1.0F / Math.max(1, breakDelayTicks);
    }

    public static float clampTension(float tension) {
        if (tension < 0.0F) {
            return 0.0F;
        }
        if (tension > 1.0F) {
            return 1.0F;
        }
        return tension;
    }

    public static double computeCableLength(double anchorX, double anchorY, double anchorZ, double droneX, double droneY, double droneZ) {
        double dx = droneX - anchorX;
        double dy = droneY - anchorY;
        double dz = droneZ - anchorZ;
        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }
}
