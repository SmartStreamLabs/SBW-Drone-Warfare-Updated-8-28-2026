package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.util.Mth;

final class LucasFlightPhysicsMath {
    private static final double GROUND_DRAG_IDLE = 0.91D;
    private static final double GROUND_DRAG_RUNWAY = 0.985D;
    private static final double BRAKE_DRAG_AIR = 0.72D;
    private static final double BRAKE_DRAG_GROUND = 0.58D;

    private LucasFlightPhysicsMath() {
    }

    static float clampThrottleNormalized(float throttle) {
        return Mth.clamp(throttle, 0.0F, 1.0F);
    }

    static double resolveTargetSpeed(float throttleNormalized, double minSpeed, double maxSpeed) {
        float throttle = clampThrottleNormalized(throttleNormalized);
        if (throttle <= 0.0F) {
            return 0.0D;
        }
        return Mth.lerp(throttle, minSpeed, Math.max(minSpeed, maxSpeed));
    }

    static float computeStallSeverity(double speed, double minLiftSpeed) {
        if (minLiftSpeed <= 0.0D || speed >= minLiftSpeed) {
            return 0.0F;
        }
        return Mth.clamp((float) ((minLiftSpeed - speed) / minLiftSpeed), 0.0F, 1.0F);
    }

    static float computeKeyboardPitchAssist(boolean forwardInputDown, boolean backInputDown) {
        if (forwardInputDown == backInputDown) {
            return 0.0F;
        }
        return forwardInputDown ? -1.0F : 1.0F;
    }

    static double computeGroundDragForThrottle(float throttleNormalized, boolean onGround) {
        if (!onGround) {
            return 1.0D;
        }
        return Mth.lerp(clampThrottleNormalized(throttleNormalized), GROUND_DRAG_IDLE, GROUND_DRAG_RUNWAY);
    }

    static double computeBrakeDrag(boolean braking, boolean onGround) {
        if (!braking) {
            return 1.0D;
        }
        return onGround ? BRAKE_DRAG_GROUND : BRAKE_DRAG_AIR;
    }

    static double computeThrottleSink(float throttleNormalized) {
        float throttle = clampThrottleNormalized(throttleNormalized);
        if (throttle <= 0.0F) {
            return 0.030D;
        }
        if (throttle < 0.25F) {
            return Mth.lerp(throttle / 0.25F, 0.024D, 0.010D);
        }
        return 0.0D;
    }

    static double computeSteadyStateSpeed(double targetSpeed, double response, double drag) {
        double coefficient = drag * (1.0D - response);
        double gain = drag * response;
        double denominator = 1.0D - coefficient;
        if (Math.abs(denominator) < 1.0E-6D) {
            return targetSpeed;
        }
        return (gain * targetSpeed) / denominator;
    }
}
