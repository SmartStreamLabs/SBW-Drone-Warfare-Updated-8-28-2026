package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DroneJamProgressMathTest {
    @Test
    void closerJammerBuildsProgressFasterThanEdgeJammer() {
        float closeInfluence = DroneJamProgressMath.computeInfluence(2.0D, 16.0D);
        float edgeInfluence = DroneJamProgressMath.computeInfluence(14.0D, 16.0D);

        float closeStep = DroneJamProgressMath.computeBuildStep(closeInfluence, 80);
        float edgeStep = DroneJamProgressMath.computeBuildStep(edgeInfluence, 80);

        assertTrue(closeInfluence > edgeInfluence, "Closer jammers should apply a stronger influence.");
        assertTrue(closeStep > edgeStep, "Closer jammers should build jam progress faster.");
    }

    @Test
    void recoveryStepAlwaysReducesProgress() {
        float progress = 0.72F;
        float recovery = DroneJamProgressMath.computeRecoveryStep(120);

        float recovered = DroneJamProgressMath.clampProgress(progress - recovery);

        assertTrue(recovery > 0.0F, "Recovery should always be positive.");
        assertTrue(recovered < progress, "Progress should decay when no jammer is present.");
    }

    @Test
    void progressRemainsClampedBetweenZeroAndOne() {
        assertEquals(0.0F, DroneJamProgressMath.clampProgress(-0.25F));
        assertEquals(1.0F, DroneJamProgressMath.clampProgress(1.25F));
        assertEquals(0.45F, DroneJamProgressMath.clampProgress(0.45F));
    }

    @Test
    void influenceDropsToZeroOutsideJammerRange() {
        assertEquals(0.0F, DroneJamProgressMath.computeInfluence(20.0D, 16.0D));
        assertEquals(0.0F, DroneJamProgressMath.computeInfluence(16.0D, 16.0D));
        assertTrue(DroneJamProgressMath.computeInfluence(4.0D, 16.0D) > 0.0F);
    }
}
