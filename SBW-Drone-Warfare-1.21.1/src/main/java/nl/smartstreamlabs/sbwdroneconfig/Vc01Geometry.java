package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/** Immutable fitted VC-01 meshes. Coordinates are metres, native forward -Z.
 * Profiles are compiled from this distribution's body/model data, NOT entity AABBs.
 * The same faces, yaw hierarchy and SBW matrices are used on client and server.
 */
public final class Vc01Geometry {
    public record Vertex(Vec3 p, float u, float v) {}
    public record Face(boolean net, boolean detail, Vertex a, Vertex b, Vertex c, Vec3 normal,String panel) {}
    public record Branch(boolean turret, List<Face> faces, List<NtGeometry.Triangle> panels, List<String> panelIds,AABB bounds) {}
    public record Profile(String id, List<Branch> branches, double radius) {}
    private static final Map<String, Optional<Profile>> CACHE = new HashMap<>();
    private Vc01Geometry() {}

    public static Profile profile(Entity e) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
        var fitted=profile(id);if(fitted!=null||!(e instanceof VehicleEntity v))return fitted;return fallback(v,id);
    }
    public static synchronized Profile profile(String id) { return CACHE.computeIfAbsent(id, Vc01Geometry::load).orElse(null); }
    private static synchronized Profile fallback(VehicleEntity v,String id){
        var known=CACHE.get(id);if(known!=null&&known.isPresent())return known.get();
        AABB body=null;
        for(var info:v.computed().getObb()){if(!info.getPart().toString().equalsIgnoreCase("BODY"))continue;for(var point:info.getOBB().getVertices()){var box=new AABB(point.x,point.y,point.z,point.x,point.y,point.z);body=body==null?box:body.minmax(box);}}
        if(body==null)return null;
        // Native model OBBs, not the entity's changing world-aligned hitbox. Profile coordinates face -Z.
        var to=new AABB(-body.maxX-.18,body.minY,-body.maxZ-.18,-body.minX+.18,body.maxY+.5,-body.minZ+.18);
        if(to.getXsize()<.1||to.getYsize()<.1||to.getZsize()<.1||to.getXsize()>32||to.getZsize()>32)return null;
        var template=profile("sbwdroneconfig:heavy_ground_drone_hgd01");if(template==null)return null;
        var branches=new ArrayList<Branch>();
        for(var branch:template.branches()){if(branch.turret())continue;var from=branch.bounds();var faces=new ArrayList<Face>();var panels=new ArrayList<NtGeometry.Triangle>();
            for(var f:branch.faces()){var a=fit(f.a(),from,to);var b=fit(f.b(),from,to);var c=fit(f.c(),from,to);faces.add(new Face(f.net(),f.detail(),a,b,c,b.p().subtract(a.p()).cross(c.p().subtract(a.p())).normalize(),f.panel()));}
            for(var t:branch.panels())panels.add(new NtGeometry.Triangle(fit(t.a(),from,to),fit(t.b(),from,to),fit(t.c(),from,to)));
            branches.add(new Branch(false,List.copyOf(faces),List.copyOf(panels),branch.panelIds(),to));
        }
        var result=new Profile(id,List.copyOf(branches),Math.max(to.getCenter().length()+to.getSize(),1));CACHE.put(id,Optional.of(result));return result;
    }
    private static Vertex fit(Vertex v,AABB a,AABB b){return new Vertex(fit(v.p(),a,b),v.u(),v.v());}
    private static Vec3 fit(Vec3 v,AABB a,AABB b){return new Vec3(b.minX+(v.x-a.minX)/Math.max(.001,a.getXsize())*b.getXsize(),b.minY+(v.y-a.minY)/Math.max(.001,a.getYsize())*b.getYsize(),b.minZ+(v.z-a.minZ)/Math.max(.001,a.getZsize())*b.getZsize());}
    private static Optional<Profile> load(String id) {
        String path = "/assets/sbwdroneconfig/vc01/" + id.replace(':', '_') + ".json.gz";
        InputStream raw = Vc01Geometry.class.getResourceAsStream(path);
        if (raw == null) return Optional.empty();
        try (var in = new InputStreamReader(new GZIPInputStream(raw), StandardCharsets.UTF_8)) {
            var root = JsonParser.parseReader(in).getAsJsonObject();
            List<Branch> branches = new ArrayList<>(); double radius = 1;
            for (var value : root.getAsJsonArray("branches")) {
                var b = value.getAsJsonObject(); List<Face> faces = new ArrayList<>();
                List<NtGeometry.Triangle> panels = new ArrayList<>(); List<Vec3> points = new ArrayList<>();
                for (var entry : b.getAsJsonArray("faces")) {
                    var f = entry.getAsJsonArray(); Vertex a = vertex(f.get(2)), c = vertex(f.get(3)), d = vertex(f.get(4));
                    var normal = c.p.subtract(a.p).cross(d.p.subtract(a.p)).normalize();
                    faces.add(new Face(f.get(0).getAsInt() == 1, f.get(1).getAsBoolean(), a, c, d, normal,(f.get(0).getAsInt()==1?Vc01Panels.id(branches.size(),f.get(5).getAsString()):"")));
                    points.add(a.p); points.add(c.p); points.add(d.p);
                }
                for (var entry : b.getAsJsonArray("panels")) {
                    var t = entry.getAsJsonArray(); panels.add(new NtGeometry.Triangle(point(t.get(0)), point(t.get(1)), point(t.get(2))));
                }
                var bounds = NtGeometry.boundsOf(points); boolean turret = b.get("turret").getAsBoolean();
                // Turret position is included by bounds(e,...) below, not baked twice into mesh vertices.
                for (var p : points) radius = Math.max(radius, p.length());
                branches.add(new Branch(turret, List.copyOf(faces), List.copyOf(panels),java.util.Arrays.asList(new Gson().fromJson(b.get("panel_ids"),String[].class)), bounds));
            }
            return Optional.of(new Profile(id, List.copyOf(branches), radius));
        } catch (IOException | JsonParseException | IllegalStateException ex) {
            throw new IllegalStateException("Invalid VC-01 mounting profile " + id, ex);
        }
    }
    private static Vertex vertex(JsonElement value) { var a=value.getAsJsonArray(); return new Vertex(point(a),a.get(3).getAsFloat(),a.get(4).getAsFloat()); }
    private static Vec3 point(JsonElement value) { var a=value.getAsJsonArray(); return new Vec3(a.get(0).getAsDouble(),a.get(1).getAsDouble(),a.get(2).getAsDouble()); }

    public static Matrix4d matrix(Entity e, Branch branch, float partial) {
        var v = (VehicleEntity)e;
        // getTurretTransform includes the real turret pivot and interpolated yaw, NEVER barrel elevation.
        return new Matrix4d(branch.turret ? v.getTurretTransform(partial) : v.getVehicleTransform(partial)).rotateY(Math.PI);
    }
    public static Vec3 transform(Matrix4dc m, Vec3 v) { var p = m.transformPosition(new Vector3d(v.x,v.y,v.z)); return new Vec3(p.x,p.y,p.z); }
    public static AABB bounds(Branch branch, Matrix4dc m) {
        AABB b=branch.bounds; List<Vec3> corners=new ArrayList<>(8);
        for (double x:new double[]{b.minX,b.maxX}) for (double y:new double[]{b.minY,b.maxY}) for (double z:new double[]{b.minZ,b.maxZ}) corners.add(transform(m,new Vec3(x,y,z)));
        return NtGeometry.boundsOf(corners);
    }
    public static NtGeometry.Triangle triangle(NtGeometry.Triangle t, Matrix4dc m) { return new NtGeometry.Triangle(transform(m,t.a()),transform(m,t.b()),transform(m,t.c())); }
}
