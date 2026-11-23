package nl.smartstreamlabs.sbwdroneconfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticLinkMathTest {
    @Test
    void tensionReachesFullAfterBreakDelayWhileCableStaysPastLimit() {
        float tension = 0.0F;

        for (int i = 0; i < 60; i++) {
            tension = FiberOpticLinkMath.updateTension(tension, 300.0D, 256, 60);
        }

        assertEquals(1.0F, tension, 1.0E-6F, "A cable that stays beyond its limit for the full delay should snap.");
    }

    @Test
    void tensionBleedsOffAgainWhenDroneReturnsInsideCableLength() {
        float tension = 0.75F;

        for (int i = 0; i < 60; i++) {
            tension = FiberOpticLinkMath.updateTension(tension, 120.0D, 256, 60);
        }

        assertEquals(0.0F, tension, 1.0E-6F, "Returning inside the allowed cable length should fully recover cable tension over time.");
    }

    @Test
    void tensionStaysClampedBetweenZeroAndOne() {
        assertEquals(0.0F, FiberOpticLinkMath.updateTension(-0.5F, 50.0D, 256, 60), 1.0E-6F);
        assertEquals(1.0F, FiberOpticLinkMath.updateTension(1.5F, 500.0D, 256, 60), 1.0E-6F);
    }

    @Test
    void cableLengthUsesStraightLineDistance() {
        double length = FiberOpticLinkMath.computeCableLength(0.0D, 64.0D, 0.0D, 3.0D, 68.0D, 12.0D);

        assertTrue(length > 12.0D, "Diagonal offsets should count toward cable usage.");
        assertEquals(13.0D, length, 1.0E-6D);
    }
}
