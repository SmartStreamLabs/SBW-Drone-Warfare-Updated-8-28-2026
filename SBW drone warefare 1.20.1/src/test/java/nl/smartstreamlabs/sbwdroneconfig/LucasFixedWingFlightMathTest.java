package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LucasFixedWingFlightMathTest {
    @Test
    void resolvesArcadeTargetSpeedBetweenZeroAndConfiguredMax() {
        assertEquals(0.0D, LucasFlightPhysicsMath.resolveTargetSpeed(0.0F, 0.20D, 0.85D), 1.0E-6D);
        assertEquals(0.85D, LucasFlightPhysicsMath.resolveTargetSpeed(1.0F, 0.20D, 0.85D), 1.0E-6D);
        assertEquals(0.525D, LucasFlightPhysicsMath.resolveTargetSpeed(0.5F, 0.20D, 0.85D), 1.0E-6D);
    }

    @Test
    void computesStallSeverityOnlyBelowLiftSpeed() {
        assertEquals(0.0F, LucasFlightPhysicsMath.computeStallSeverity(0.70D, 0.45D), 1.0E-6F);
        assertEquals(0.0F, LucasFlightPhysicsMath.computeStallSeverity(0.45D, 0.45D), 1.0E-6F);
        assertEquals(0.5F, LucasFlightPhysicsMath.computeStallSeverity(0.225D, 0.45D), 1.0E-6F);
        assertEquals(1.0F, LucasFlightPhysicsMath.computeStallSeverity(0.0D, 0.45D), 1.0E-6F);
    }

    @Test
    void clampsThrottleIntoNormalizedFlightRange() {
        assertEquals(0.0F, LucasFlightPhysicsMath.clampThrottleNormalized(-0.25F), 1.0E-6F);
        assertEquals(0.42F, LucasFlightPhysicsMath.clampThrottleNormalized(0.42F), 1.0E-6F);
        assertEquals(1.0F, LucasFlightPhysicsMath.clampThrottleNormalized(1.75F), 1.0E-6F);
    }

    @Test
    void computesKeyboardPitchAssistFromForwardAndBackInputs() {
        assertEquals(-1.0F, LucasFlightPhysicsMath.computeKeyboardPitchAssist(true, false), 1.0E-6F);
        assertEquals(1.0F, LucasFlightPhysicsMath.computeKeyboardPitchAssist(false, true), 1.0E-6F);
        assertEquals(0.0F, LucasFlightPhysicsMath.computeKeyboardPitchAssist(false, false), 1.0E-6F);
        assertEquals(0.0F, LucasFlightPhysicsMath.computeKeyboardPitchAssist(true, true), 1.0E-6F);
    }

    @Test
    void fullThrottleRunwayRollCanReachTakeoffSpeed() {
        double targetSpeed = LucasFlightPhysicsMath.resolveTargetSpeed(1.0F, 0.20D, 0.85D);
        double runwayDrag = LucasFlightPhysicsMath.computeGroundDragForThrottle(1.0F, true);
        double steadySpeed = LucasFlightPhysicsMath.computeSteadyStateSpeed(targetSpeed, 0.025D, runwayDrag);

        assertTrue(steadySpeed > 0.45D,
                "The LUCAS drone must still be able to accelerate past its arcade lift threshold while rolling on the ground.");
    }

    @Test
    void brakeDragStopsLucasDroneMuchFasterThanIdleDrag() {
        assertEquals(1.0D, LucasFlightPhysicsMath.computeBrakeDrag(false, false), 1.0E-6D);
        assertEquals(0.72D, LucasFlightPhysicsMath.computeBrakeDrag(true, false), 1.0E-6D);
        assertEquals(0.58D, LucasFlightPhysicsMath.computeBrakeDrag(true, true), 1.0E-6D);
        assertTrue(LucasFlightPhysicsMath.computeBrakeDrag(true, true)
                        < LucasFlightPhysicsMath.computeGroundDragForThrottle(0.0F, true),
                "Holding S on the ground should brake harder than idle runway drag.");
    }
}
