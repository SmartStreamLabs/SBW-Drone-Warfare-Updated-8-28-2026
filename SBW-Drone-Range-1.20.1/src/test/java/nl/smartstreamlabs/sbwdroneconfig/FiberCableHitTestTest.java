package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberCableHitTestTest {
    @Test
    void rayHitsThinCableInsideInteractionRadius() {
        List<Vec3> cable = List.of(new Vec3(0.0D, 0.0D, 3.0D), new Vec3(3.0D, 0.0D, 3.0D));

        Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                5.0D,
                0.12D,
                cable
        );

        assertTrue(hit.isPresent());
        assertEquals(0, hit.get().segmentIndex());
        assertEquals(0.0D, hit.get().distanceFromRay(), 1.0E-6D);
        assertEquals(3.0D, hit.get().distanceFromOrigin(), 1.0E-6D);
    }

    @Test
    void rayMissesCableOutsideInteractionRadius() {
        List<Vec3> cable = List.of(new Vec3(0.25D, 0.0D, 3.0D), new Vec3(3.0D, 0.0D, 3.0D));

        Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                5.0D,
                0.12D,
                cable
        );

        assertTrue(hit.isEmpty());
    }

    @Test
    void rayRejectsCableBeyondReach() {
        List<Vec3> cable = List.of(new Vec3(0.0D, 0.0D, 7.0D), new Vec3(3.0D, 0.0D, 7.0D));

        Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                5.0D,
                0.12D,
                cable
        );

        assertTrue(hit.isEmpty());
    }

    @Test
    void hintedSearchFallsBackToWholePolylineWhenHintMisses() {
        List<Vec3> cable = List.of(
                new Vec3(2.0D, 0.0D, 2.0D),
                new Vec3(3.0D, 0.0D, 2.0D),
                new Vec3(3.0D, 0.0D, 3.0D),
                new Vec3(0.0D, 0.0D, 3.0D)
        );

        Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(
                Vec3.ZERO,
                new Vec3(0.0D, 0.0D, 1.0D),
                5.0D,
                0.12D,
                cable,
                0,
                0
        );

        assertTrue(hit.isPresent());
        assertEquals(2, hit.get().segmentIndex());
    }
}
