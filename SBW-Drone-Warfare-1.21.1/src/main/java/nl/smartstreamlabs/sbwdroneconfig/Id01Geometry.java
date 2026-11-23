package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;import net.minecraft.world.entity.Entity;import net.minecraft.core.Direction;import net.minecraft.world.phys.shapes.*;import java.util.*;
/** Physical parts and exact ray triangles derived from visible R3 meshes only, in blocks. */
public final class Id01Geometry {
 private static final double[][] BOXES,TRIANGLES;
 static {try(var in=Id01Geometry.class.getResourceAsStream("/assets/sbwdroneconfig/id01-collision.json")){var data=new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(java.util.Objects.requireNonNull(in),java.nio.charset.StandardCharsets.UTF_8),com.google.gson.JsonObject.class);BOXES=new com.google.gson.Gson().fromJson(data.get("boxes"),double[][].class);TRIANGLES=new com.google.gson.Gson().fromJson(data.get("triangles"),double[][].class);}catch(java.io.IOException ex){throw new ExceptionInInitializerError(ex);}}
 public static List<AABB> parts(Id01Entity e){List<AABB> result=new ArrayList<>(BOXES.length);for(var b:BOXES){AABB box=null;for(int i=0;i<8;i++){Vec3 p=Id01Pose.point(e,1,new Vec3(b[(i&1)==0?0:3],b[(i&2)==0?1:4],b[(i&4)==0?2:5]));box=box==null?new AABB(p,p):box.minmax(new AABB(p,p));}result.add(box);}return result;}
 public static AABB bounds(Id01Entity e){AABB all=null;for(var p:parts(e))all=all==null?p:all.minmax(p);return all;}
 public static boolean ray(Id01Entity e,Vec3 start,Vec3 end){
  Vec3 a=local(e,start),b=local(e,end),d=b.subtract(a);
  for(var t:TRIANGLES){Vec3 v=new Vec3(t[0],t[1],t[2]),u=new Vec3(t[3],t[4],t[5]).subtract(v),w=new Vec3(t[6],t[7],t[8]).subtract(v),p=d.cross(w);double det=u.dot(p);if(Math.abs(det)<1e-10)continue;double inv=1/det;Vec3 s=a.subtract(v);double x=s.dot(p)*inv;if(x<0||x>1)continue;Vec3 c=s.cross(u);double y=d.dot(c)*inv;if(y<0||x+y>1)continue;double distance=w.dot(c)*inv;if(distance>=0&&distance<=1)return true;}return false;
 }
 private static Vec3 local(Id01Entity e,Vec3 world){Vec3 v=world.subtract(e.position().add(0,Id01Pose.ROOT_HEIGHT,0)).yRot((float)Math.toRadians(e.getYRot()));double r=Math.toRadians(e.tilt(1)-e.posePitch(1));return new Vec3(v.x,v.y*Math.cos(r)-v.z*Math.sin(r),v.y*Math.sin(r)+v.z*Math.cos(r));}
 public static List<VoxelShape> entityCollisions(net.minecraft.world.level.Level level,Entity mover,AABB area){
  return addCollisions(level,mover,area,level.getEntityCollisions(mover,area));
 }
 public static List<VoxelShape> addCollisions(net.minecraft.world.level.Level level,Entity mover,AABB area,List<VoxelShape> original){
  if(mover==null||mover.isSpectator())return original;
  var ids=level.getEntities(mover,area,e->e instanceof Id01Entity&&!e.isSpectator()&&!e.isRemoved()&&!Id01Entity.contactPair(mover,e));if(ids.isEmpty())return original;
  var shapes=new ArrayList<>(original);for(var entity:ids)for(var part:parts((Id01Entity)entity))if(part.intersects(area))shapes.add(Shapes.create(part));return shapes;
 }
 public static Vec3 collide(Id01Entity e,Vec3 requested){
  AABB area=bounds(e).expandTowards(requested).inflate(.01);List<VoxelShape> shapes=new ArrayList<>();for(var s:e.level().getBlockCollisions(e,area))shapes.add(s);shapes.addAll(entityCollisions(e.level(),e,area));var border=e.level().getWorldBorder();if(!border.isWithinBounds(area))shapes.add(border.getCollisionShape());
  if(shapes.isEmpty())return requested;double y=requested.y,x=requested.x,z=requested.z;var parts=parts(e);for(var box:parts)y=Shapes.collide(Direction.Axis.Y,box,shapes,y);for(var box:parts)x=Shapes.collide(Direction.Axis.X,box.move(0,y,0),shapes,x);for(var box:parts)z=Shapes.collide(Direction.Axis.Z,box.move(x,y,0),shapes,z);return new Vec3(x,y,z);
 }
}
