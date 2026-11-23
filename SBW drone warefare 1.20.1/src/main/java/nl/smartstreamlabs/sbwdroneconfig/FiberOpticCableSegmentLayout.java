package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds small physical cable pieces along the same slightly sagging curve used
 * by the FPV fiber optic cable. The server uses these poses to position real,
 * damageable cable segment entities instead of one purely client-side line.
 */
public final class FiberOpticCableSegmentLayout {
    private FiberOpticCableSegmentLayout() {
    }

    public static List<SegmentPose> buildSegmentPoses(Vec3 anchor, Vec3 droneAttachment, double spacing, int maxSegments) {
        int segmentCount = computeSegmentCount(anchor, droneAttachment, spacing, maxSegments);
        Vec3[] curvePoints = FiberOpticCableRenderMath.buildCableCurvePoints(anchor, droneAttachment, segmentCount);
        List<SegmentPose> poses = new ArrayList<>(segmentCount);

        for (int index = 0; index < segmentCount; index++) {
            Vec3 start = curvePoints[index];
            Vec3 end = curvePoints[index + 1];
            Vec3 delta = end.subtract(start);
            double distance = delta.length();
            Vec3 direction = distance > 1.0E-6D ? delta.scale(1.0D / distance) : new Vec3(0.0D, 0.0D, 1.0D);
            Vec3 center = start.lerp(end, 0.5D);
            float length = (float) Math.max(0.18D, distance);
            poses.add(new SegmentPose(center, direction, length));
        }

        return List.copyOf(poses);
    }

    public static int computeSegmentCount(Vec3 anchor, Vec3 droneAttachment, double spacing, int maxSegments) {
        double cableLength = anchor.distanceTo(droneAttachment);
        double safeSpacing = Math.max(0.25D, spacing);
        int safeMaxSegments = Math.max(1, maxSegments);

        if (cableLength <= safeSpacing) {
            return 1;
        }

        return Math.max(1, Math.min(safeMaxSegments, (int) Math.ceil(cableLength / safeSpacing)));
    }

    public record SegmentPose(Vec3 center, Vec3 direction, float length) {
    }
}
