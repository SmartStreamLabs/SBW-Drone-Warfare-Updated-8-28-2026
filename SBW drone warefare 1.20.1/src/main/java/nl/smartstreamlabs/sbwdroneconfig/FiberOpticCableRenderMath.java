package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

final class FiberOpticCableRenderMath {
    private static final double GROUND_OFFSET = 0.12D;
    private static final double MIN_REAR_OFFSET = 0.18D;
    private static final double MAX_REAR_OFFSET = 0.34D;
    private static final double MIN_BODY_ATTACHMENT_HEIGHT = 0.06D;
    private static final double MAX_BODY_ATTACHMENT_HEIGHT = 0.16D;
    private static final double MIN_SAG = 0.08D;
    private static final double MAX_SAG = 1.05D;
    private static final double MAX_GROUND_DIP = 0.35D;

    private FiberOpticCableRenderMath() {
    }

    static Vec3 resolveStoredAnchor(Vec3 controllerFeetPosition, Vec3 capturedLaunchAnchor) {
        return capturedLaunchAnchor != null ? capturedLaunchAnchor : controllerFeetPosition;
    }

    static Vec3 toRenderAnchor(Vec3 storedAnchor) {
        return storedAnchor.add(0.0D, GROUND_OFFSET, 0.0D);
    }

    static Vec3 toRenderEnd(Vec3 dronePosition, float droneYawDegrees, double droneBbWidth, double droneBbHeight) {
        Vec3 forward = horizontalForward(droneYawDegrees);
        double rearOffset = Mth.clamp(droneBbWidth * 0.55D, MIN_REAR_OFFSET, MAX_REAR_OFFSET);
        double attachmentHeight = Mth.clamp(droneBbHeight * 0.14D, MIN_BODY_ATTACHMENT_HEIGHT, MAX_BODY_ATTACHMENT_HEIGHT);

        return new Vec3(
                dronePosition.x - (forward.x * rearOffset),
                dronePosition.y + attachmentHeight,
                dronePosition.z - (forward.z * rearOffset)
        );
    }

    static Vec3 toCubedFpvSpoolAttachment(CubedFpvDroneEntity drone, float partialTicks) {
        return CubedFpvFiberSpoolGeometry.cableExitWorldPosition(drone, partialTicks);
    }

    static Vec3 toCubedFpvSpoolAttachment(CubedFpvDroneEntity drone) {
        return CubedFpvFiberSpoolGeometry.cableExitWorldPosition(drone);
    }

    static Vec3[] buildCableCurvePoints(Vec3 renderStart, Vec3 renderEnd, int segmentCount) {
        if (!isFinite(renderStart) || !isFinite(renderEnd)) {
            return new Vec3[0];
        }
        int segments = Mth.clamp(segmentCount, 1, 256);
        Vec3 control = buildControlPoint(renderStart, renderEnd);
        Vec3[] points = new Vec3[segments + 1];

        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            points[i] = sampleQuadratic(renderStart, control, renderEnd, t);
        }

        return points;
    }

    static Vec3[] buildRouteCurvePoints(List<Vec3> savedRoutePoints, Vec3 dynamicEnd, int sampleBudget, boolean smoothCorners) {
        if (!isFinite(dynamicEnd)) {
            return new Vec3[0];
        }

        int maxPoints = Mth.clamp(sampleBudget, 2, 128);
        int maxSegments = maxPoints - 1;
        List<Vec3> controls = buildRouteControls(savedRoutePoints, dynamicEnd, maxPoints);
        if (controls.size() < 2) {
            return new Vec3[]{dynamicEnd};
        }
        if (controls.size() == 2) {
            return buildCableCurvePoints(controls.get(0), controls.get(1), maxSegments);
        }

        double totalLength = routeLength(controls);
        if (totalLength <= 1.0E-6D) {
            return new Vec3[]{controls.get(0), controls.get(controls.size() - 1)};
        }

        List<Vec3> samples = new ArrayList<>(maxPoints);
        samples.add(controls.get(0));
        int remainingSegmentsToSample = maxSegments;
        for (int segmentIndex = 0; segmentIndex < controls.size() - 1; segmentIndex++) {
            Vec3 start = controls.get(segmentIndex);
            Vec3 end = controls.get(segmentIndex + 1);
            double segmentLength = start.distanceTo(end);
            if (segmentLength <= 1.0E-6D) {
                continue;
            }

            int remainingSegments = (controls.size() - 1) - segmentIndex;
            int reservedSamples = Math.max(0, remainingSegments - 1);
            int segmentSamples = Math.max(1, (int) Math.round(maxSegments * (segmentLength / totalLength)));
            segmentSamples = Math.min(segmentSamples, Math.max(1, remainingSegmentsToSample - reservedSamples));
            for (int step = 1; step <= segmentSamples; step++) {
                double t = step / (double) segmentSamples;
                samples.add(sampleRouteSegment(controls, segmentIndex, t, smoothCorners));
            }
            remainingSegmentsToSample -= segmentSamples;
            if (remainingSegmentsToSample <= 0 || samples.size() >= maxPoints) {
                break;
            }
        }

        Vec3 finalPoint = controls.get(controls.size() - 1);
        if (!samples.get(samples.size() - 1).closerThan(finalPoint, 0.001D)) {
            if (samples.size() >= maxPoints) {
                samples.set(samples.size() - 1, finalPoint);
            } else {
                samples.add(finalPoint);
            }
        }
        return samples.toArray(Vec3[]::new);
    }

    static Vec3[] buildCutInteractionPoints(List<Vec3> savedRoutePoints, Vec3 dynamicEnd) {
        if (!isFinite(dynamicEnd)) {
            return new Vec3[0];
        }

        List<Vec3> controls = buildRouteControls(savedRoutePoints, dynamicEnd, AddonConfig.fiberRouteMaximumPoints() + 1);
        if (controls.size() < 2) {
            return controls.toArray(Vec3[]::new);
        }

        List<Vec3> interactionPoints = new ArrayList<>(controls.size() * 3);
        addDeduplicated(interactionPoints, controls.get(0));
        for (int segmentIndex = 0; segmentIndex < controls.size() - 1; segmentIndex++) {
            Vec3 start = controls.get(segmentIndex);
            Vec3 end = controls.get(segmentIndex + 1);
            if (!isFinite(start) || !isFinite(end) || start.distanceToSqr(end) <= 1.0E-8D) {
                continue;
            }

            double t = 0.5D;
            double segmentLength = start.distanceTo(end);
            boolean dynamicFinalSegment = segmentIndex == controls.size() - 2;
            double maxSag = dynamicFinalSegment ? 0.30D : 0.18D;
            double sag = Mth.clamp((float) (segmentLength * 0.01D), 0.05F, (float) maxSag);
            Vec3 midpoint = start.lerp(end, t).add(0.0D, -Math.sin(Math.PI * t) * sag, 0.0D);

            addDeduplicated(interactionPoints, midpoint);
            addDeduplicated(interactionPoints, end);
        }

        return interactionPoints.toArray(Vec3[]::new);
    }

    private static List<Vec3> buildRouteControls(List<Vec3> savedRoutePoints, Vec3 dynamicEnd, int maxControls) {
        List<Vec3> controls = new ArrayList<>();
        if (savedRoutePoints != null) {
            for (Vec3 point : savedRoutePoints) {
                addDeduplicated(controls, point);
            }
        }
        addDeduplicated(controls, dynamicEnd);

        if (controls.size() <= maxControls) {
            return controls;
        }

        List<Vec3> selected = new ArrayList<>(maxControls);
        selected.add(controls.get(0));
        int lastIndex = controls.size() - 1;
        int newestKeep = Math.min(24, Math.max(2, maxControls / 4));
        int newestStart = Math.max(1, lastIndex - newestKeep);
        int olderBudget = Math.max(0, maxControls - newestKeep - 2);
        int olderEnd = Math.max(1, newestStart);
        int stride = olderBudget <= 0 ? Integer.MAX_VALUE : Math.max(1, olderEnd / olderBudget);

        for (int index = 1; index < newestStart && selected.size() < maxControls - newestKeep - 1; index++) {
            if (isSharpCorner(controls, index) || index % stride == 0) {
                addDeduplicated(selected, controls.get(index));
            }
        }

        for (int index = newestStart; index < controls.size(); index++) {
            addDeduplicated(selected, controls.get(index));
        }

        return selected;
    }

    private static void addDeduplicated(List<Vec3> points, Vec3 point) {
        if (!isFinite(point)) {
            return;
        }
        if (!points.isEmpty() && points.get(points.size() - 1).closerThan(point, 0.02D)) {
            return;
        }
        points.add(point);
    }

    private static Vec3 sampleRouteSegment(List<Vec3> controls, int segmentIndex, double t, boolean smoothCorners) {
        Vec3 p1 = controls.get(segmentIndex);
        Vec3 p2 = controls.get(segmentIndex + 1);
        if (t <= 0.0D) {
            return p1;
        }
        if (t >= 1.0D) {
            return p2;
        }
        Vec3 point = smoothCorners
                ? sampleClampedCatmullRom(
                controls.get(Math.max(0, segmentIndex - 1)),
                p1,
                p2,
                controls.get(Math.min(controls.size() - 1, segmentIndex + 2)),
                t
        )
                : p1.lerp(p2, t);

        double segmentLength = p1.distanceTo(p2);
        boolean dynamicFinalSegment = segmentIndex == controls.size() - 2;
        double maxSag = dynamicFinalSegment ? 0.30D : 0.18D;
        double sag = Mth.clamp((float) (segmentLength * 0.01D), 0.05F, (float) maxSag);
        double sagAmount = Math.sin(Math.PI * t) * sag;
        return point.add(0.0D, -sagAmount, 0.0D);
    }

    private static Vec3 sampleClampedCatmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, double t) {
        if (t <= 0.0D) {
            return p1;
        }
        if (t >= 1.0D) {
            return p2;
        }
        double segmentLength = p1.distanceTo(p2);
        if (segmentLength <= 0.05D || !isFinite(p0) || !isFinite(p1) || !isFinite(p2) || !isFinite(p3)) {
            return p1.lerp(p2, t);
        }

        double t2 = t * t;
        double t3 = t2 * t;
        double x = catmullRom(p0.x, p1.x, p2.x, p3.x, t, t2, t3);
        double y = catmullRom(p0.y, p1.y, p2.y, p3.y, t, t2, t3);
        double z = catmullRom(p0.z, p1.z, p2.z, p3.z, t, t2, t3);
        double expansion = Math.min(0.35D, segmentLength * 0.15D);
        return new Vec3(
                clamp(x, min(p0.x, p1.x, p2.x, p3.x) - expansion, max(p0.x, p1.x, p2.x, p3.x) + expansion),
                clamp(y, min(p0.y, p1.y, p2.y, p3.y) - expansion, max(p0.y, p1.y, p2.y, p3.y) + expansion),
                clamp(z, min(p0.z, p1.z, p2.z, p3.z) - expansion, max(p0.z, p1.z, p2.z, p3.z) + expansion)
        );
    }

    private static double catmullRom(double p0, double p1, double p2, double p3, double t, double t2, double t3) {
        return 0.5D * ((2.0D * p1)
                + ((-p0 + p2) * t)
                + (((2.0D * p0) - (5.0D * p1) + (4.0D * p2) - p3) * t2)
                + ((-p0 + (3.0D * p1) - (3.0D * p2) + p3) * t3));
    }

    private static double routeLength(List<Vec3> controls) {
        double length = 0.0D;
        for (int index = 0; index < controls.size() - 1; index++) {
            length += controls.get(index).distanceTo(controls.get(index + 1));
        }
        return length;
    }

    private static boolean isSharpCorner(List<Vec3> controls, int index) {
        if (index <= 0 || index >= controls.size() - 1) {
            return false;
        }
        Vec3 first = controls.get(index).subtract(controls.get(index - 1));
        Vec3 second = controls.get(index + 1).subtract(controls.get(index));
        double lengthProduct = first.length() * second.length();
        if (lengthProduct <= 1.0E-8D) {
            return false;
        }

        double dot = first.dot(second) / lengthProduct;
        double angle = Math.toDegrees(Math.acos(clamp(dot, -1.0D, 1.0D)));
        return angle >= AddonConfig.fiberRouteTurnAngleThreshold();
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double min(double a, double b, double c, double d) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static double max(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    static boolean isFinite(Vec3 point) {
        return point != null
                && Double.isFinite(point.x)
                && Double.isFinite(point.y)
                && Double.isFinite(point.z);
    }

    private static Vec3 buildControlPoint(Vec3 renderStart, Vec3 renderEnd) {
        double cableLength = renderStart.distanceTo(renderEnd);
        double sag = Mth.clamp((float) (MIN_SAG + (cableLength * 0.0065D)), (float) MIN_SAG, (float) MAX_SAG);
        Vec3 midpoint = renderStart.lerp(renderEnd, 0.5D);
        double floorClamp = Math.min(renderStart.y, renderEnd.y) - Math.min(MAX_GROUND_DIP, sag * 0.45D);
        double controlY = Math.max(midpoint.y - sag, floorClamp);
        return new Vec3(midpoint.x, controlY, midpoint.z);
    }

    private static Vec3 sampleQuadratic(Vec3 p0, Vec3 p1, Vec3 p2, double t) {
        double inverseT = 1.0D - t;
        double x = (inverseT * inverseT * p0.x) + (2.0D * inverseT * t * p1.x) + (t * t * p2.x);
        double y = (inverseT * inverseT * p0.y) + (2.0D * inverseT * t * p1.y) + (t * t * p2.y);
        double z = (inverseT * inverseT * p0.z) + (2.0D * inverseT * t * p1.z) + (t * t * p2.z);
        return new Vec3(x, y, z);
    }

    private static Vec3 horizontalForward(float droneYawDegrees) {
        double yawRadians = Math.toRadians(droneYawDegrees);
        double x = -Math.sin(yawRadians);
        double z = Math.cos(yawRadians);
        Vec3 forward = new Vec3(x, 0.0D, z);
        if (forward.lengthSqr() < 1.0E-6D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return forward.normalize();
    }
}
