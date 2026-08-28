package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticProjectileFallbackTest {
    @Test
    void projectileFallbackIgnoresStationaryProjectiles() {
        assertFalse(
                FiberOpticLinkSystem.shouldProjectileFallbackBreak(0.0D),
                "Stationary projectiles should not sever the fiber cable through the fallback scan."
        );
        assertFalse(
                FiberOpticLinkSystem.shouldProjectileFallbackBreak(1.0E-4D),
                "Nearly motionless projectiles should be ignored to avoid random cable severing from stuck arrows or stale bullets."
        );
    }

    @Test
    void projectileFallbackStillAllowsRealInFlightProjectiles() {
        assertTrue(
                FiberOpticLinkSystem.shouldProjectileFallbackBreak(0.01D),
                "Real moving projectiles should still sever the cable through the fallback scan."
        );
    }
}
