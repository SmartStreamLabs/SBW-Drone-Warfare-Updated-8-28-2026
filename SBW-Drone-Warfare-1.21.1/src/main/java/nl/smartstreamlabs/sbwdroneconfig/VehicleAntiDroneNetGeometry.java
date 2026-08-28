package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public final class VehicleAntiDroneNetGeometry {
    public static final double SHELL_THICKNESS = 0.10D;
    public static final double TILE_SIZE = 0.75D;
    private static final double MIN_HALF_HORIZONTAL = 0.75D;
    private static final double MIN_HEIGHT = 0.75D;
    private static final double FALLBACK_BODY_BOTTOM_FACTOR = 0.25D;

    private VehicleAntiDroneNetGeometry() {
    }

    public static NetBox createBox(Entity vehicle, double horizontalPadding, double topPadding) {
        VehicleBodyBounds bodyBounds = resolveBodyBounds(vehicle);
        double padding = Math.max(0.0D, horizontalPadding);
        double minX = bodyBounds.minX() - padding;
        double maxX = bodyBounds.maxX() + padding;
        double minZ = bodyBounds.minZ() - padding;
        double maxZ = bodyBounds.maxZ() + padding;
        double minY = bodyBounds.minY();
        double maxY = bodyBounds.maxY() + Math.max(0.0D, topPadding);

        if (maxX - minX < MIN_HALF_HORIZONTAL * 2.0D) {
            double centerX = (minX + maxX) * 0.5D;
            minX = centerX - MIN_HALF_HORIZONTAL;
            maxX = centerX + MIN_HALF_HORIZONTAL;
        }
        if (maxZ - minZ < MIN_HALF_HORIZONTAL * 2.0D) {
            double centerZ = (minZ + maxZ) * 0.5D;
            minZ = centerZ - MIN_HALF_HORIZONTAL;
            maxZ = centerZ + MIN_HALF_HORIZONTAL;
        }
        if (maxY - minY < MIN_HEIGHT) {
            maxY = minY + MIN_HEIGHT;
        }

        return new NetBox(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static NetBox createVisualBox(Entity vehicle, double horizontalPadding, double topPadding) {
        return createBox(vehicle, horizontalPadding, topPadding);
    }

    public static Basis basis(Entity vehicle, float partialTick) {
        Vec3 right = SbwCompat.getVehicleRightVector(vehicle, partialTick);
        Vec3 up = SbwCompat.getVehicleUpVector(vehicle, partialTick);
        Vec3 forward = SbwCompat.getVehicleForwardVector(vehicle, partialTick);

        if (right.lengthSqr() < 1.0E-6D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        if (up.lengthSqr() < 1.0E-6D) {
            up = new Vec3(0.0D, 1.0D, 0.0D);
        }
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = right.cross(up);
        }
        if (forward.lengthSqr() < 1.0E-6D) {
            forward = new Vec3(0.0D, 0.0D, 1.0D);
        }

        Vec3 origin = vehicle.getPosition(partialTick);
        return new Basis(origin, right.normalize(), up.normalize(), forward.normalize());
    }

    public static double searchRadius(NetBox box) {
        double maxX = Math.max(Math.abs(box.minX()), Math.abs(box.maxX()));
        double maxZ = Math.max(Math.abs(box.minZ()), Math.abs(box.maxZ()));
        return Math.sqrt((maxX * maxX) + (maxZ * maxZ))
                + box.height()
                + 2.0D;
    }

    public static boolean intersectsSweptShell(NetBox box, LocalPoint previous, LocalPoint current, double radius) {
        double expanded = Math.max(0.0D, radius);
        return intersectsLocalAabb(previous, current,
                box.minX() - expanded,
                box.maxX() + expanded,
                box.maxY() - SHELL_THICKNESS - expanded,
                box.maxY() + SHELL_THICKNESS + expanded,
                box.minZ() - expanded,
                box.maxZ() + expanded)
                || intersectsLocalAabb(previous, current,
                box.minX() - SHELL_THICKNESS - expanded,
                box.minX() + SHELL_THICKNESS + expanded,
                box.minY() - expanded,
                box.maxY() + expanded,
                box.minZ() - expanded,
                box.maxZ() + expanded)
                || intersectsLocalAabb(previous, current,
                box.maxX() - SHELL_THICKNESS - expanded,
                box.maxX() + SHELL_THICKNESS + expanded,
                box.minY() - expanded,
                box.maxY() + expanded,
                box.minZ() - expanded,
                box.maxZ() + expanded)
                || intersectsLocalAabb(previous, current,
                box.minX() - expanded,
                box.maxX() + expanded,
                box.minY() - expanded,
                box.maxY() + expanded,
                box.minZ() - SHELL_THICKNESS - expanded,
                box.minZ() + SHELL_THICKNESS + expanded)
                || intersectsLocalAabb(previous, current,
                box.minX() - expanded,
                box.maxX() + expanded,
                box.minY() - expanded,
                box.maxY() + expanded,
                box.maxZ() - SHELL_THICKNESS - expanded,
                box.maxZ() + SHELL_THICKNESS + expanded);
    }

    private static VehicleBodyBounds resolveBodyBounds(Entity vehicle) {
        VehicleBodyBounds obbBounds = resolveObbBodyBounds(vehicle);
        return obbBounds != null ? obbBounds : fallbackBodyBounds(vehicle);
    }

    private static VehicleBodyBounds resolveObbBodyBounds(Entity vehicle) {
        try {
            Method getObbs = vehicle.getClass().getMethod("getOBBs");
            Object result = getObbs.invoke(vehicle);
            if (!(result instanceof Iterable<?> obbs)) {
                return null;
            }

            Basis vehicleBasis = basis(vehicle, 1.0F);
            MutableBounds hull = new MutableBounds();
            double turretTopY = Double.NEGATIVE_INFINITY;

            for (Object obb : obbs) {
                String partName = getObbPartName(obb);
                if (shouldExcludePart(partName)) {
                    continue;
                }

                Vector3d[] vertices = getObbVertices(obb);
                if (vertices.length == 0) {
                    continue;
                }

                if (isTurretPart(partName)) {
                    for (Vector3d vertex : vertices) {
                        turretTopY = Math.max(turretTopY, vehicleBasis.toLocal(toVec3(vertex)).y());
                    }
                    continue;
                }

                if (isHullPart(partName)) {
                    for (Vector3d vertex : vertices) {
                        hull.include(vehicleBasis.toLocal(toVec3(vertex)));
                    }
                }
            }

            if (!hull.hasValue()) {
                return null;
            }

            if (Double.isFinite(turretTopY)) {
                hull.includeY(turretTopY);
            }
            return hull.toBounds();
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }

    private static Vector3d[] getObbVertices(Object obb) throws ReflectiveOperationException {
        Method getVertices = obb.getClass().getMethod("getVertices");
        Object vertices = getVertices.invoke(obb);
        return vertices instanceof Vector3d[] typed ? typed : new Vector3d[0];
    }

    private static String getObbPartName(Object obb) {
        try {
            Field partField = obb.getClass().getField("part");
            Object part = partField.get(obb);
            return part == null ? "BODY" : part.toString().toUpperCase(Locale.ROOT);
        } catch (ReflectiveOperationException ignored) {
            return "BODY";
        }
    }

    private static boolean isHullPart(String partName) {
        return "BODY".equals(partName) || "MAIN_ENGINE".equals(partName) || "SUB_ENGINE".equals(partName);
    }

    private static boolean isTurretPart(String partName) {
        return "TURRET".equals(partName);
    }

    private static boolean shouldExcludePart(String partName) {
        return partName.contains("WHEEL")
                || "INTERACTIVE".equals(partName)
                || "EMPTY".equals(partName)
                || partName.contains("BARREL");
    }

    private static VehicleBodyBounds fallbackBodyBounds(Entity vehicle) {
        AABB bounds = vehicle.getBoundingBox();
        Basis basis = basis(vehicle, 1.0F);
        MutableBounds localBounds = new MutableBounds();
        includeAabbCorners(localBounds, basis, bounds);
        VehicleBodyBounds raw = localBounds.hasValue() ? localBounds.toBounds() : basicEntityBounds(vehicle);
        double height = Math.max(MIN_HEIGHT, raw.maxY() - raw.minY());
        double fallbackBottom = raw.minY() + height * FALLBACK_BODY_BOTTOM_FACTOR;
        return new VehicleBodyBounds(
                raw.minX(),
                raw.maxX(),
                Math.min(fallbackBottom, raw.maxY() - MIN_HEIGHT),
                raw.maxY(),
                raw.minZ(),
                raw.maxZ()
        );
    }

    private static VehicleBodyBounds basicEntityBounds(Entity vehicle) {
        double halfWidth = Math.max(MIN_HALF_HORIZONTAL, vehicle.getBbWidth() * 0.5D);
        double height = Math.max(MIN_HEIGHT, vehicle.getBbHeight());
        return new VehicleBodyBounds(-halfWidth, halfWidth, 0.0D, height, -halfWidth, halfWidth);
    }

    private static void includeAabbCorners(MutableBounds bounds, Basis basis, AABB box) {
        includeCorner(bounds, basis, box.minX, box.minY, box.minZ);
        includeCorner(bounds, basis, box.maxX, box.minY, box.minZ);
        includeCorner(bounds, basis, box.minX, box.maxY, box.minZ);
        includeCorner(bounds, basis, box.maxX, box.maxY, box.minZ);
        includeCorner(bounds, basis, box.minX, box.minY, box.maxZ);
        includeCorner(bounds, basis, box.maxX, box.minY, box.maxZ);
        includeCorner(bounds, basis, box.minX, box.maxY, box.maxZ);
        includeCorner(bounds, basis, box.maxX, box.maxY, box.maxZ);
    }

    private static void includeCorner(MutableBounds bounds, Basis basis, double x, double y, double z) {
        bounds.include(basis.toLocal(new Vec3(x, y, z)));
    }

    private static Vec3 toVec3(Vector3d vector) {
        return new Vec3(vector.x, vector.y, vector.z);
    }

    private static boolean intersectsLocalAabb(
            LocalPoint previous,
            LocalPoint current,
            double minX,
            double maxX,
            double minY,
            double maxY,
            double minZ,
            double maxZ
    ) {
        double dx = current.x() - previous.x();
        double dy = current.y() - previous.y();
        double dz = current.z() - previous.z();
        double enter = 0.0D;
        double exit = 1.0D;

        double[] result = clipAxis(previous.x(), dx, minX, maxX, enter, exit);
        if (result == null) {
            return false;
        }
        enter = result[0];
        exit = result[1];

        result = clipAxis(previous.y(), dy, minY, maxY, enter, exit);
        if (result == null) {
            return false;
        }
        enter = result[0];
        exit = result[1];

        result = clipAxis(previous.z(), dz, minZ, maxZ, enter, exit);
        return result != null;
    }

    private static double[] clipAxis(double start, double delta, double min, double max, double enter, double exit) {
        if (Math.abs(delta) < 1.0E-7D) {
            return start >= min && start <= max ? new double[]{enter, exit} : null;
        }

        double near = (min - start) / delta;
        double far = (max - start) / delta;
        if (near > far) {
            double swap = near;
            near = far;
            far = swap;
        }
        enter = Math.max(enter, near);
        exit = Math.min(exit, far);
        return enter <= exit ? new double[]{enter, exit} : null;
    }

    public record NetBox(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        public double width() {
            return maxX - minX;
        }

        public double length() {
            return maxZ - minZ;
        }

        public double height() {
            return maxY - minY;
        }
    }

    public record VehicleBodyBounds(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    }

    public record Basis(Vec3 origin, Vec3 right, Vec3 up, Vec3 forward) {
        public Vec3 toWorld(double x, double y, double z) {
            return origin
                    .add(right.scale(x))
                    .add(up.scale(y))
                    .add(forward.scale(z));
        }

        public LocalPoint toLocal(Vec3 world) {
            Vec3 rel = world.subtract(origin);
            return new LocalPoint(rel.dot(right), rel.dot(up), rel.dot(forward));
        }
    }

    public record LocalPoint(double x, double y, double z) {
        public static LocalPoint lerp(LocalPoint from, LocalPoint to, double progress) {
            return new LocalPoint(
                    Mth.lerp(progress, from.x, to.x),
                    Mth.lerp(progress, from.y, to.y),
                    Mth.lerp(progress, from.z, to.z)
            );
        }
    }

    private static final class MutableBounds {
        private double minX = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        void include(LocalPoint point) {
            minX = Math.min(minX, point.x());
            maxX = Math.max(maxX, point.x());
            minY = Math.min(minY, point.y());
            maxY = Math.max(maxY, point.y());
            minZ = Math.min(minZ, point.z());
            maxZ = Math.max(maxZ, point.z());
        }

        void includeY(double y) {
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        boolean hasValue() {
            return Double.isFinite(minX)
                    && Double.isFinite(maxX)
                    && Double.isFinite(minY)
                    && Double.isFinite(maxY)
                    && Double.isFinite(minZ)
                    && Double.isFinite(maxZ);
        }

        VehicleBodyBounds toBounds() {
            return new VehicleBodyBounds(minX, maxX, minY, maxY, minZ, maxZ);
        }
    }
}
