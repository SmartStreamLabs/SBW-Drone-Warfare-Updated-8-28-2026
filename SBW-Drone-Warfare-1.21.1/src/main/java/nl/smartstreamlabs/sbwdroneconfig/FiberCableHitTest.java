package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

final class FiberCableHitTest {
    private static final double EPSILON = 1.0E-8D;

    private FiberCableHitTest() {
    }

    static Optional<Result> closestHit(Vec3 rayOrigin, Vec3 rayDirection, double reach, double radius, List<Vec3> polyline) {
        return closestHit(rayOrigin, rayDirection, reach, radius, polyline, -1, 0);
    }

    static Optional<Result> closestHit(
            Vec3 rayOrigin,
            Vec3 rayDirection,
            double reach,
            double radius,
            List<Vec3> polyline,
            int preferredSegmentIndex,
            int preferredWindow
    ) {
        if (!isFinite(rayOrigin) || !isFinite(rayDirection) || polyline == null || polyline.size() < 2) {
            return Optional.empty();
        }

        double clampedReach = Math.max(0.0D, reach);
        double clampedRadius = Math.max(0.0D, radius);
        if (clampedReach <= EPSILON || clampedRadius <= EPSILON || rayDirection.lengthSqr() <= EPSILON) {
            return Optional.empty();
        }

        Vec3 rayEnd = rayOrigin.add(rayDirection.normalize().scale(clampedReach));
        Optional<Result> preferred = closestHitInRange(
                rayOrigin,
                rayEnd,
                clampedReach,
                clampedRadius,
                polyline,
                Math.max(0, preferredSegmentIndex - Math.max(0, preferredWindow)),
                Math.min(polyline.size() - 2, preferredSegmentIndex + Math.max(0, preferredWindow))
        );
        Optional<Result> full = closestHitInRange(rayOrigin, rayEnd, clampedReach, clampedRadius, polyline, 0, polyline.size() - 2);
        return full.isPresent() ? full : preferred;
    }

    private static Optional<Result> closestHitInRange(
            Vec3 rayOrigin,
            Vec3 rayEnd,
            double reach,
            double radius,
            List<Vec3> polyline,
            int firstSegment,
            int lastSegment
    ) {
        if (firstSegment > lastSegment || firstSegment < 0 || lastSegment >= polyline.size() - 1) {
            return Optional.empty();
        }

        Result best = null;
        double radiusSqr = radius * radius;
        double reachWithTolerance = reach + radius;
        double reachWithToleranceSqr = reachWithTolerance * reachWithTolerance;

        for (int segmentIndex = firstSegment; segmentIndex <= lastSegment; segmentIndex++) {
            Vec3 start = polyline.get(segmentIndex);
            Vec3 end = polyline.get(segmentIndex + 1);
            if (!isFinite(start) || !isFinite(end) || start.distanceToSqr(end) <= EPSILON) {
                continue;
            }

            ClosestPoints closestPoints = closestPointsBetweenSegments(rayOrigin, rayEnd, start, end);
            double distanceSqr = closestPoints.pointOnRay().distanceToSqr(closestPoints.pointOnCable());
            if (distanceSqr > radiusSqr || rayOrigin.distanceToSqr(closestPoints.pointOnCable()) > reachWithToleranceSqr) {
                continue;
            }

            double distanceFromRay = Math.sqrt(distanceSqr);
            double distanceFromOrigin = rayOrigin.distanceTo(closestPoints.pointOnCable());
            Result candidate = new Result(
                    segmentIndex,
                    closestPoints.pointOnCable(),
                    closestPoints.pointOnRay(),
                    distanceFromRay,
                    distanceFromOrigin
            );
            if (best == null
                    || candidate.distanceFromRay() < best.distanceFromRay()
                    || (candidate.distanceFromRay() == best.distanceFromRay()
                    && candidate.distanceFromOrigin() < best.distanceFromOrigin())) {
                best = candidate;
            }
        }

        return Optional.ofNullable(best);
    }

    private static ClosestPoints closestPointsBetweenSegments(Vec3 firstStart, Vec3 firstEnd, Vec3 secondStart, Vec3 secondEnd) {
        Vec3 u = firstEnd.subtract(firstStart);
        Vec3 v = secondEnd.subtract(secondStart);
        Vec3 w = firstStart.subtract(secondStart);

        double a = u.dot(u);
        double b = u.dot(v);
        double c = v.dot(v);
        double d = u.dot(w);
        double e = v.dot(w);
        double denominator = (a * c) - (b * b);

        double sNumerator;
        double sDenominator = denominator;
        double tNumerator;
        double tDenominator = denominator;

        if (denominator < EPSILON) {
            sNumerator = 0.0D;
            sDenominator = 1.0D;
            tNumerator = e;
            tDenominator = c;
        } else {
            sNumerator = (b * e) - (c * d);
            tNumerator = (a * e) - (b * d);
            if (sNumerator < 0.0D) {
                sNumerator = 0.0D;
                tNumerator = e;
                tDenominator = c;
            } else if (sNumerator > sDenominator) {
                sNumerator = sDenominator;
                tNumerator = e + b;
                tDenominator = c;
            }
        }

        if (tNumerator < 0.0D) {
            tNumerator = 0.0D;
            if (-d < 0.0D) {
                sNumerator = 0.0D;
            } else if (-d > a) {
                sNumerator = sDenominator;
            } else {
                sNumerator = -d;
                sDenominator = a;
            }
        } else if (tNumerator > tDenominator) {
            tNumerator = tDenominator;
            if ((-d + b) < 0.0D) {
                sNumerator = 0.0D;
            } else if ((-d + b) > a) {
                sNumerator = sDenominator;
            } else {
                sNumerator = -d + b;
                sDenominator = a;
            }
        }

        double s = Math.abs(sNumerator) < EPSILON ? 0.0D : sNumerator / sDenominator;
        double t = Math.abs(tNumerator) < EPSILON ? 0.0D : tNumerator / tDenominator;

        return new ClosestPoints(firstStart.add(u.scale(s)), secondStart.add(v.scale(t)));
    }

    static boolean isFinite(Vec3 point) {
        return point != null
                && Double.isFinite(point.x)
                && Double.isFinite(point.y)
                && Double.isFinite(point.z);
    }

    record Result(int segmentIndex, Vec3 closestPointOnCable, Vec3 closestPointOnRay, double distanceFromRay, double distanceFromOrigin) {
    }

    private record ClosestPoints(Vec3 pointOnRay, Vec3 pointOnCable) {
    }
}
