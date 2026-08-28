package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.loading.json.raw.Bone;
import software.bernie.geckolib.loading.json.raw.Cube;
import software.bernie.geckolib.loading.json.raw.ModelProperties;
import software.bernie.geckolib.loading.json.raw.PolyMesh;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.BoneStructure;
import software.bernie.geckolib.loading.object.GeometryTree;

import java.util.ArrayList;
import java.util.List;

public final class SbwDronePolyMeshFactory implements BakedModelFactory {
    public static final SbwDronePolyMeshFactory INSTANCE = new SbwDronePolyMeshFactory();

    private static final BakedModelFactory FALLBACK = BakedModelFactory.DEFAULT_FACTORY;
    private static final double MODEL_SCALE = 16.0D;

    private SbwDronePolyMeshFactory() {
    }

    public static void registerForModNamespace() {
        BakedModelFactory.register(SbwDroneRangeConfig.MOD_ID, INSTANCE);
    }

    @Override
    public BakedGeoModel constructGeoModel(GeometryTree geometryTree) {
        List<GeoBone> topLevelBones = new ArrayList<>();

        for (BoneStructure topLevelBone : geometryTree.topLevelBones().values()) {
            topLevelBones.add(constructBone(topLevelBone, geometryTree.properties(), null));
        }

        return new BakedGeoModel(topLevelBones, geometryTree.properties());
    }

    @Override
    public GeoBone constructBone(BoneStructure boneStructure, ModelProperties properties, GeoBone parent) {
        Bone bone = boneStructure.self();
        GeoBone geoBone = new GeoBone(parent, bone.name(), bone.mirror(), bone.inflate(), bone.neverRender(), bone.reset());

        Vec3 rotation = toVec(bone.rotation());
        Vec3 pivot = toVec(bone.pivot());
        geoBone.setRotX((float) Math.toRadians(-rotation.x));
        geoBone.setRotY((float) Math.toRadians(-rotation.y));
        geoBone.setRotZ((float) Math.toRadians(rotation.z));
        geoBone.setPivotX((float) -pivot.x);
        geoBone.setPivotY((float) pivot.y);
        geoBone.setPivotZ((float) pivot.z);

        Cube[] cubes = bone.cubes();
        if (cubes != null) {
            for (Cube cube : cubes) {
                geoBone.getCubes().add(constructCube(cube, properties, geoBone));
            }
        }

        if (bone.polyMesh() != null) {
            geoBone.getCubes().add(constructPolyMeshCube(bone.polyMesh(), properties));
        }

        for (BoneStructure child : boneStructure.children().values()) {
            geoBone.getChildBones().add(constructBone(child, properties, geoBone));
        }

        return geoBone;
    }

    @Override
    public GeoCube constructCube(Cube cube, ModelProperties properties, GeoBone bone) {
        return FALLBACK.constructCube(cube, properties, bone);
    }

    private static GeoCube constructPolyMeshCube(PolyMesh polyMesh, ModelProperties properties) {
        double[] positions = polyMesh.positions();
        double[] normals = polyMesh.normals();
        double[] uvs = polyMesh.uvs();
        double[][][] polys = polyMesh.polysUnion() == null ? new double[0][][] : polyMesh.polysUnion().union();

        List<GeoQuad> quads = new ArrayList<>();
        Bounds bounds = new Bounds();

        for (double[][] poly : polys) {
            appendPolyQuads(quads, bounds, poly, positions, normals, uvs, Boolean.TRUE.equals(polyMesh.normalizedUVs()), properties);
        }

        return new GeoCube(
                quads.toArray(GeoQuad[]::new),
                Vec3.ZERO,
                Vec3.ZERO,
                bounds.toSizeVec(),
                0.0D,
                false
        );
    }

    private static void appendPolyQuads(List<GeoQuad> target, Bounds bounds, double[][] poly, double[] positions, double[] normals, double[] uvs,
                                        boolean normalizedUvs, ModelProperties properties) {
        if (poly == null || poly.length < 3) {
            return;
        }

        if (poly.length == 4) {
            GeoQuad quad = buildQuad(poly, bounds, positions, normals, uvs, normalizedUvs, properties);
            if (quad != null) {
                target.add(quad);
            }
            return;
        }

        for (int index = 1; index < poly.length - 1; index++) {
            GeoQuad quad = buildQuad(new double[][]{poly[0], poly[index], poly[index + 1]}, bounds, positions, normals, uvs, normalizedUvs, properties);
            if (quad != null) {
                target.add(quad);
            }
        }
    }

    private static GeoQuad buildQuad(double[][] poly, Bounds bounds, double[] positions, double[] normals, double[] uvs,
                                     boolean normalizedUvs, ModelProperties properties) {
        if (poly.length < 3) {
            return null;
        }

        GeoVertex first = buildVertex(poly[0], bounds, positions, uvs, normalizedUvs, properties);
        GeoVertex second = buildVertex(poly[1], bounds, positions, uvs, normalizedUvs, properties);
        GeoVertex third = buildVertex(poly[2], bounds, positions, uvs, normalizedUvs, properties);
        GeoVertex fourth = poly.length > 3 ? buildVertex(poly[3], bounds, positions, uvs, normalizedUvs, properties) : third;

        if (first == null || second == null || third == null || fourth == null) {
            return null;
        }

        Vector3f normal = buildNormal(poly, normals, first, second, third);
        return new GeoQuad(new GeoVertex[]{first, second, third, fourth}, normal, nearestDirection(normal));
    }

    private static GeoVertex buildVertex(double[] polyVertex, Bounds bounds, double[] positions, double[] uvs,
                                         boolean normalizedUvs, ModelProperties properties) {
        int positionIndex = indexFrom(polyVertex, 0);
        if (positionIndex < 0 || positionIndex * 3 + 2 >= positions.length) {
            return null;
        }

        double geoX = positions[positionIndex * 3];
        double geoY = positions[positionIndex * 3 + 1];
        double geoZ = positions[positionIndex * 3 + 2];

        bounds.include(geoX, geoY, geoZ);

        int uvIndex = indexFrom(polyVertex, 2);
        float u = 0.0F;
        float v = 0.0F;
        if (uvIndex >= 0 && uvIndex * 2 + 1 < uvs.length) {
            u = (float) uvs[uvIndex * 2];
            v = (float) uvs[uvIndex * 2 + 1];
            if (!normalizedUvs) {
                u /= (float) properties.textureWidth();
                v /= (float) properties.textureHeight();
            }
            v = 1.0F - v;
        }

        return new GeoVertex(-geoX / MODEL_SCALE, geoY / MODEL_SCALE, geoZ / MODEL_SCALE).withUVs(u, v);
    }

    private static Vector3f buildNormal(double[][] poly, double[] normals, GeoVertex first, GeoVertex second, GeoVertex third) {
        Vector3f averaged = new Vector3f();
        int counted = 0;

        for (double[] polyVertex : poly) {
            int normalIndex = indexFrom(polyVertex, 1);
            if (normalIndex >= 0 && normalIndex * 3 + 2 < normals.length) {
                averaged.add(
                        (float) -normals[normalIndex * 3],
                        (float) normals[normalIndex * 3 + 1],
                        (float) normals[normalIndex * 3 + 2]
                );
                counted++;
            }
        }

        if (counted > 0 && averaged.lengthSquared() > 1.0E-6F) {
            return averaged.normalize();
        }

        Vector3f edgeA = new Vector3f(second.position()).sub(first.position());
        Vector3f edgeB = new Vector3f(third.position()).sub(first.position());
        Vector3f derived = edgeA.cross(edgeB);

        if (derived.lengthSquared() <= 1.0E-6F) {
            return new Vector3f(0.0F, 1.0F, 0.0F);
        }

        return derived.normalize();
    }

    private static int indexFrom(double[] polyVertex, int slot) {
        if (polyVertex == null || polyVertex.length <= slot) {
            return -1;
        }

        return (int) polyVertex[slot];
    }

    private static Vec3 toVec(double[] values) {
        if (values == null || values.length < 3) {
            return Vec3.ZERO;
        }

        return new Vec3(values[0], values[1], values[2]);
    }

    private static Direction nearestDirection(Vector3f normal) {
        float absX = Math.abs(normal.x);
        float absY = Math.abs(normal.y);
        float absZ = Math.abs(normal.z);

        if (absY >= absX && absY >= absZ) {
            return normal.y >= 0.0F ? Direction.UP : Direction.DOWN;
        }
        if (absX >= absZ) {
            return normal.x >= 0.0F ? Direction.EAST : Direction.WEST;
        }

        return normal.z >= 0.0F ? Direction.SOUTH : Direction.NORTH;
    }

    private static final class Bounds {
        private double minX = Double.POSITIVE_INFINITY;
        private double minY = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxY = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        private void include(double x, double y, double z) {
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }

        private Vec3 toSizeVec() {
            if (!Double.isFinite(minX)) {
                return Vec3.ZERO;
            }

            return new Vec3(maxX - minX, maxY - minY, maxZ - minZ);
        }
    }
}
