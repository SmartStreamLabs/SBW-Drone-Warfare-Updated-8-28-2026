package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberRouteRibbonMathTest {
    @Test
    void stableRibbonOffsetHandlesParallelCameraReference() {
        Vec3 offset = FiberOpticCableRenderer.stableRibbonOffset(
                new Vec3(0.0D, 1.0D, 0.0D),
                new Vec3(0.0D, 4.0D, 0.0D),
                0.015D,
                null
        );

        assertTrue(FiberOpticCableRenderMath.isFinite(offset));
        assertEquals(0.015D, offset.length(), 1.0E-6D);
    }

    @Test
    void stableRibbonOffsetKeepsTheSameSideAcrossSegments() {
        Vec3 previous = new Vec3(0.02D, 0.0D, 0.0D);

        Vec3 offset = FiberOpticCableRenderer.stableRibbonOffset(
                new Vec3(0.0D, 0.0D, 1.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                0.02D,
                previous
        );

        assertTrue(FiberOpticCableRenderMath.isFinite(offset));
        assertEquals(0.02D, offset.length(), 1.0E-6D);
        assertTrue(offset.dot(previous) > 0.0D, "The ribbon should flip back toward the previous side instead of rolling over.");
    }

    @Test
    void stableRibbonOffsetFallsBackCleanlyForMalformedVectors() {
        Vec3 previous = new Vec3(0.0D, 0.0D, 0.02D);

        Vec3 offset = FiberOpticCableRenderer.stableRibbonOffset(
                new Vec3(Double.NaN, 0.0D, 0.0D),
                new Vec3(0.0D, 1.0D, 0.0D),
                0.02D,
                previous
        );

        assertEquals(previous.x, offset.x, 1.0E-6D);
        assertEquals(previous.y, offset.y, 1.0E-6D);
        assertEquals(previous.z, offset.z, 1.0E-6D);
    }
}
