package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;import net.minecraft.world.entity.Entity;import net.minecraft.core.Direction;import net.minecraft.world.phys.shapes.*;import java.util.*;
/** Physical parts and exact ray triangles derived from visible R3 meshes only, in blocks. */
public final class Hx2Geometry {
 private static final double[][] BOXES,TRIANGLES;
 static {try(var in=Hx2Geometry.class.getResourceAsStream("/assets/sbwdroneconfig/hx2-collision.json")){var data=new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(java.util.Objects.requireNonNull(in),java.nio.charset.StandardCharsets.UTF_8),com.google.gson.JsonObject.class);BOXES=new com.google.gson.Gson().fromJson(data.get("boxes"),double[][].class);TRIANGLES=new com.google.gson.Gson().fromJson(data.get("triangles"),double[][].class);}catch(java.io.IOException ex){throw new ExceptionInInitializerError(ex);}}
 private record Cache(org.joml.Matrix4d frame,List<com.atsuishio.superbwarfare.tools.OBB> obbs,List<AABB> parts){}
 private static final java.util.Map<Hx2Entity,Cache> CACHE=new java.util.WeakHashMap<>();
 private static synchronized Cache cache(Hx2Entity e){var frame=Hx2Pose.frame(e,1);var old=CACHE.get(e);if(old!=null&&old.frame.equals(frame,1e-9))return old;var rot=frame.getNormalizedRotation(new org.joml.Quaterniond());var obbs=new java.util.ArrayList<com.atsuishio.superbwarfare.tools.OBB>();var parts=new java.util.ArrayList<AABB>();for(var b:BOXES){var center=frame.transformPosition(new org.joml.Vector3d((b[0]+b[3])/2,(b[1]+b[4])/2,(b[2]+b[5])/2));var obb=new com.atsuishio.superbwarfare.tools.OBB(center,new org.joml.Vector3d(Math.max(.005,(b[3]-b[0])/2),Math.max(.005,(b[4]-b[1])/2),Math.max(.005,(b[5]-b[2])/2)),new org.joml.Quaterniond(rot),com.atsuishio.superbwarfare.tools.OBB.Part.BODY);obbs.add(obb);AABB world=null;for(int i=0;i<8;i++){var at=Hx2Pose.point(frame,new Vec3(b[(i&1)==0?0:3],b[(i&2)==0?1:4],b[(i&4)==0?2:5]));var corner=new AABB(at,at);world=world==null?corner:world.minmax(corner);}parts.add(world);}var value=new Cache(frame,List.copyOf(obbs),List.copyOf(parts));CACHE.put(e,value);return value;}
 public static List<com.atsuishio.superbwarfare.tools.OBB> obbs(Hx2Entity e){return cache(e).obbs;}
 public static List<AABB> parts(Hx2Entity e){return cache(e).parts;}
 public static AABB bounds(Hx2Entity e){AABB all=null;for(var b:parts(e))all=all==null?b:all.minmax(b);return all;}
 private static final java.util.Map<net.minecraft.core.Direction,AABB> DOCK_BOUNDS=new java.util.EnumMap<>(net.minecraft.core.Direction.class);
 public static synchronized AABB dockedBounds(net.minecraft.core.BlockPos p,net.minecraft.core.Direction facing){var box=DOCK_BOUNDS.get(facing);if(box==null){var frame=Hx2Pose.dock(net.minecraft.core.BlockPos.ZERO,facing,0);for(var b:BOXES)for(int i=0;i<8;i++){var point=Hx2Pose.point(frame,new Vec3(b[(i&1)==0?0:3],b[(i&2)==0?1:4],b[(i&4)==0?2:5]));var corner=new AABB(point,point);box=box==null?corner:box.minmax(corner);}DOCK_BOUNDS.put(facing,box);}return box.move(p);}
 public static boolean ray(Hx2Entity e,Vec3 start,Vec3 end){
  Vec3 a=local(e,start),b=local(e,end),d=b.subtract(a);
  for(var t:TRIANGLES){Vec3 v=new Vec3(t[0],t[1],t[2]),u=new Vec3(t[3],t[4],t[5]).subtract(v),w=new Vec3(t[6],t[7],t[8]).subtract(v),p=d.cross(w);double det=u.dot(p);if(Math.abs(det)<1e-10)continue;double inv=1/det;Vec3 s=a.subtract(v);double x=s.dot(p)*inv;if(x<0||x>1)continue;Vec3 c=s.cross(u);double y=d.dot(c)*inv;if(y<0||x+y>1)continue;double distance=w.dot(c)*inv;if(distance>=0&&distance<=1)return true;}return false;
 }
 private static Vec3 local(Hx2Entity e,Vec3 world){var p=new org.joml.Matrix4d(Hx2Pose.frame(e,1)).invert().transformPosition(new org.joml.Vector3d(world.x,world.y,world.z));return new Vec3(p.x,p.y,p.z);}
 public static List<VoxelShape> entityCollisions(net.minecraft.world.level.Level level,Entity mover,AABB area){
  var original=level.getEntityCollisions(mover,area);if(mover==null||mover.isSpectator())return original;
  var ids=level.getEntities(mover,area,e->e instanceof Hx2Entity&&!e.isSpectator()&&!e.isRemoved());if(ids.isEmpty())return original;
  var shapes=new ArrayList<>(original);for(var entity:ids)for(var part:parts((Hx2Entity)entity))if(part.intersects(area))shapes.add(Shapes.create(part));return shapes;
 }
 public static List<VoxelShape> addCollisions(net.minecraft.world.level.Level level,Entity mover,AABB area,List<VoxelShape> original){if(mover==null||mover.isSpectator())return original;var list=level.getEntities(mover,area,e->e instanceof Hx2Entity&&!e.isRemoved());if(list.isEmpty())return original;var result=new ArrayList<>(original);for(var e:list)for(var b:parts((Hx2Entity)e))if(b.intersects(area))result.add(Shapes.create(b));return result;}
 public static Vec3 collide(Hx2Entity e,Vec3 requested){
  AABB area=bounds(e).expandTowards(requested).inflate(.01);List<VoxelShape> shapes=new ArrayList<>();for(var cell:net.minecraft.core.BlockPos.betweenClosed(net.minecraft.core.BlockPos.containing(area.minX,area.minY,area.minZ),net.minecraft.core.BlockPos.containing(area.maxX,area.maxY,area.maxZ))){var state=e.level().getBlockState(cell);if(e.ignoresLaunchBlock(cell,state))continue;var shape=state.getCollisionShape(e.level(),cell,net.minecraft.world.phys.shapes.CollisionContext.of(e));if(!shape.isEmpty())shapes.add(shape.move(cell.getX(),cell.getY(),cell.getZ()));}shapes.addAll(entityCollisions(e.level(),e,area));var border=e.level().getWorldBorder();if(!border.isWithinBounds(area))shapes.add(border.getCollisionShape());
  if(shapes.isEmpty())return requested;double y=requested.y,x=requested.x,z=requested.z;var parts=parts(e);for(var box:parts)y=Shapes.collide(Direction.Axis.Y,box,shapes,y);for(var box:parts)x=Shapes.collide(Direction.Axis.X,box.move(0,y,0),shapes,x);for(var box:parts)z=Shapes.collide(Direction.Axis.Z,box.move(x,y,0),shapes,z);return new Vec3(x,y,z);
 }
}
