package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.*;
import org.joml.Matrix4d;
import java.util.*;

/** Aircraft collision is separate from render/cable bounds. Native empty bones contribute nothing. */
public final class Rd01Geometry {
    private static final double[][] BOXES;
    static {
        try(var in=Rd01Geometry.class.getResourceAsStream("/assets/sbwdroneconfig/rd01_collision.json")) {
            BOXES=new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(Objects.requireNonNull(in),java.nio.charset.StandardCharsets.UTF_8),double[][].class);
        }catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}
    }
    static AABB transform(Matrix4d m,double[] box) {
        AABB out=null;
        for(int i=0;i<8;i++){Vec3 v=Rd01Pose.point(m,box[(i&1)==0?0:3],box[(i&2)==0?1:4],box[(i&4)==0?2:5]);AABB b=new AABB(v,v);out=out==null?b:out.minmax(b);}
        return out;
    }
    public static List<AABB> parts(Rd01Entity d) {
        Matrix4d m=Rd01Pose.aircraft(d,1);List<AABB> out=new ArrayList<>(BOXES.length);
        for(double[] b:BOXES)out.add(transform(m,b));return out;
    }
    public static AABB bounds(Rd01Entity d) {AABB b=null;for(AABB p:parts(d))b=b==null?p:b.minmax(p);return b;}
    static boolean grounded(Rd01Entity d) {
        Matrix4d m=Rd01Pose.aircraft(d,1);
        for(double[] b:BOXES)if(b[1]<=27.6&&d.level().getBlockCollisions(d,transform(m,b).move(0,-.03,0)).iterator().hasNext())return true;
        return false;
    }
    public static AABB harnessBounds(Rd01Entity d,float extension,float stow) {
        AABB box=transform(Rd01Pose.harness(d,1,extension,stow),new double[]{-7,8.5,-6,7,37.2,6});
        if(d.occupied())box=box.minmax(transform(Rd01Pose.harness(d,1,extension,stow),new double[]{-5,4,-5,5,30.5,8}));return box;
    }
    public static AABB passengerBounds(Rd01Entity d) {
        return transform(Rd01Pose.harness(d,1),new double[]{-5,4,-5,5,30.5,8});
    }
    public static boolean harnessClear(Rd01Entity d,float ext,Vec3 movement){return harnessClear(d,ext,d.stow(1),movement);}
    static boolean harnessClear(Rd01Entity d,float ext,float stow,Vec3 movement) {
        AABB b=harnessBounds(d,ext,stow).minmax(harnessBounds(d,d.extension(1),d.stow(1))).expandTowards(movement).inflate(.03);
        if(!loaded(d,b)||d.level().getBlockCollisions(d,b).iterator().hasNext())return false;
        // Only the cable's narrow actual segment reserves space, not the whole hull-to-seat volume.
        Vec3 a=Rd01Pose.point(Rd01Pose.aircraft(d,1),0,42.7,1),end=Rd01Pose.point(Rd01Pose.harness(d,1,ext,stow),0,37,1);
        if(d.level().clip(new net.minecraft.world.level.ClipContext(a,end.add(movement),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,d)).getType()!=HitResult.Type.MISS)return false;
        return true;
    }
    private static boolean loaded(Rd01Entity d,AABB b){return d.level().getWorldBorder().isWithinBounds(b)&&d.level().hasChunksAt(BlockPos.containing(b.minX,b.minY,b.minZ),BlockPos.containing(b.maxX,b.maxY,b.maxZ));}
    static boolean routeLoaded(Rd01Entity d,Vec3 v){return loaded(d,bounds(d).expandTowards(v.scale(3)))&&loaded(d,harnessBounds(d,d.extension(1),d.stow(1)).expandTowards(v));}
    public static boolean ray(Rd01Entity d,Vec3 a,Vec3 b){return parts(d).stream().anyMatch(box->box.contains(a)||box.clip(a,b).isPresent());}
    public static List<VoxelShape> addCollisions(net.minecraft.world.level.Level level,Entity mover,AABB area,List<VoxelShape> original) {
        List<VoxelShape> out=null;
        for(Entity e:level.getEntities(mover,area,e->e instanceof Rd01Entity&&!e.isRemoved()&&!e.hasPassenger(mover))) {
            if(out==null)out=new ArrayList<>(original);
            for(AABB b:parts((Rd01Entity)e))if(b.intersects(area))out.add(Shapes.create(b));
        }
        return out==null?original:out;
    }
    static Vec3 collide(Rd01Entity d,Vec3 wanted) {
        List<AABB> parts=new ArrayList<>(parts(d));parts.add(harnessBounds(d,d.extension(1),d.stow(1)));
        AABB area=bounds(d).minmax(parts.get(parts.size()-1)).expandTowards(wanted).inflate(.01);
        List<VoxelShape> shapes=new ArrayList<>();for(var s:d.level().getBlockCollisions(d,area))shapes.add(s);
        shapes.addAll(d.level().getEntityCollisions(d,area));
        if(!d.level().getWorldBorder().isWithinBounds(area))shapes.add(d.level().getWorldBorder().getCollisionShape());
        double y=wanted.y,x=wanted.x,z=wanted.z;
        for(AABB p:parts)y=Shapes.collide(Direction.Axis.Y,p,shapes,y);
        for(AABB p:parts)x=Shapes.collide(Direction.Axis.X,p.move(0,y,0),shapes,x);
        for(AABB p:parts)z=Shapes.collide(Direction.Axis.Z,p.move(x,y,0),shapes,z);
        return new Vec3(x,y,z);
    }
    private Rd01Geometry() {}
}
