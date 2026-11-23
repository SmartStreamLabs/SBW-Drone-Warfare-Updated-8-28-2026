package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.world.phys.*;import net.minecraft.world.level.*;
public final class Qd01Ground {
 public static final String[] LEGS={"fl","fr","rl","rr"};
 public static double x(int i){return i%2==0?-.64:.64;}public static double z(int i){return i<2?-.565:.465;}
 public static Vec3 contact(Level l,net.minecraft.world.entity.Entity e,Vec3 at,double up,double down){
  if(!l.hasChunkAt(BlockPos.containing(at)))return null;
  var hit=l.clip(new ClipContext(at.add(0,up,0),at.add(0,-down,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e));return hit.getType()==HitResult.Type.MISS?null:hit.getLocation();
 }
 private Qd01Ground(){}
 /** Footprint support catches a riser's edge before the sole enters it; bounded to a tiny loaded volume. */
 public static double sole(net.minecraft.world.level.Level l,net.minecraft.world.entity.Entity e,Vec3 p){
  double result=Double.NEGATIVE_INFINITY;var box=new net.minecraft.world.phys.AABB(p.x-.105,p.y-.65,p.z-.105,p.x+.105,p.y+.62,p.z+.105);
  for(double x:new double[]{box.minX,box.maxX})for(double z:new double[]{box.minZ,box.maxZ})if(!l.hasChunkAt(net.minecraft.core.BlockPos.containing(x,p.y,z)))return result;
  for(var shape:l.getBlockCollisions(e,box))for(var b:shape.toAabbs())if(b.maxY<=p.y+.61&&b.maxY>=p.y-.65)result=Math.max(result,b.maxY);
  return result;
 }
}
