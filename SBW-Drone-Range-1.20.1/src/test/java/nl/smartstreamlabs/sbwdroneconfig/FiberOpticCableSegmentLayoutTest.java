package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FiberOpticCableSegmentLayoutTest {
    @Test
    void clampsSegmentCountToConfiguredMaximum() {
        List<FiberOpticCableSegmentLayout.SegmentPose> poses = FiberOpticCableSegmentLayout.buildSegmentPoses(
                new Vec3(0.0D, 64.0D, 0.0D),
                new Vec3(40.0D, 68.0D, 0.0D),
                4.0D,
                8
        );

        assertEquals(8, poses.size(), "Long cables should be split into multiple short segments and clamp to the configured cap.");
    }

    @Test
    void keepsShortCableAsSingleSegment() {
        List<FiberOpticCableSegmentLayout.SegmentPose> poses = FiberOpticCableSegmentLayout.buildSegmentPoses(
                new Vec3(10.0D, 64.0D, 10.0D),
                new Vec3(11.25D, 64.4D, 10.5D),
                4.0D,
                96
        );

        assertEquals(1, poses.size(), "Very short fiber cables should stay as one visible segment.");
        assertTrue(poses.get(0).length() > 0.0F, "The cable segment should keep a visible render length.");
    }

    @Test
    void placesSegmentCentersAlongSaggingCableCurve() {
        Vec3 anchor = new Vec3(0.0D, 64.12D, 0.0D);
        Vec3 droneAttachment = new Vec3(18.0D, 67.5D, 2.0D);

        List<FiberOpticCableSegmentLayout.SegmentPose> poses = FiberOpticCableSegmentLayout.buildSegmentPoses(
                anchor,
                droneAttachment,
                4.0D,
                96
        );

        assertTrue(poses.size() >= 4, "A longer cable should create several physical segment entities.");
        assertTrue(poses.get(0).center().distanceTo(anchor) < poses.get(poses.size() - 1).center().distanceTo(anchor),
                "The first segment center should stay closest to the launch anchor.");
        assertTrue(poses.get(poses.size() - 1).center().distanceTo(droneAttachment) < poses.get(0).center().distanceTo(droneAttachment),
                "The last segment center should stay closest to the drone attachment point.");

        double minimumCenterY = Double.POSITIVE_INFINITY;
        for (FiberOpticCableSegmentLayout.SegmentPose pose : poses) {
            minimumCenterY = Math.min(minimumCenterY, pose.center().y);
            assertTrue(pose.length() > 0.0F, "Every physical cable piece should have a positive render length.");
        }

        double straightMidY = (anchor.y + droneAttachment.y) * 0.5D;
        assertTrue(minimumCenterY < straightMidY, "Segment centers should follow the slight cable sag instead of a rigid straight laser line.");
    }

    @Test
    void supportsLongCablesNeedingMoreThanTwentyFourSegments() {
        Vec3 anchor = new Vec3(0.0D, 64.12D, 0.0D);
        Vec3 droneAttachment = new Vec3(120.0D, 78.0D, 0.0D);

        List<FiberOpticCableSegmentLayout.SegmentPose> poses = assertDoesNotThrow(
                () -> FiberOpticCableSegmentLayout.buildSegmentPoses(anchor, droneAttachment, 4.0D, 96),
                "Long fiber cables should not crash when they need more than 24 physical segment entities."
        );

        assertEquals(31, poses.size(), "The layout should preserve the computed segment count for long fiber cables.");
    }
}
