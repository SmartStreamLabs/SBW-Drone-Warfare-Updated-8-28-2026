package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.phys.*;import net.minecraft.world.entity.Entity;import net.minecraft.world.phys.shapes.*;import java.util.*;
/** Native SBW OBBs remain authoritative. The enclosing interaction query box is never a solid wall. */
public final class Qd01Geometry {
 public static boolean ray(Qd01Entity e,Vec3 a,Vec3 b){return e.getOBBs().stream().anyMatch(o->o.contains(a)||o.clip(new org.joml.Vector3d(a.x,a.y,a.z),new org.joml.Vector3d(b.x,b.y,b.z)).isPresent());}
 public static List<VoxelShape> addCollisions(net.minecraft.world.level.Level l,Entity mover,AABB area,List<VoxelShape> original){if(mover==null||mover.isSpectator())return original;var q=l.getEntities(mover,area,e->e instanceof Qd01Entity&&!e.isRemoved());if(q.isEmpty())return original;var result=new ArrayList<>(original);for(var en:q)for(var part:((Qd01Entity)en).getOBBs()){var box=worldBounds(part);if(box.intersects(area))result.add(Shapes.create(box));}return result;}
// Legacy SBW exposes the world-space corners rather than getWorldAABB.
 private static AABB worldBounds(com.atsuishio.superbwarfare.tools.OBB part){
  var points=part.getVertices();double x=Double.POSITIVE_INFINITY,y=x,z=x,X=Double.NEGATIVE_INFINITY,Y=X,Z=X;
  for(var point:points){x=Math.min(x,point.x);y=Math.min(y,point.y);z=Math.min(z,point.z);X=Math.max(X,point.x);Y=Math.max(Y,point.y);Z=Math.max(Z,point.z);}
  return new AABB(x,y,z,X,Y,Z);
 }
}
