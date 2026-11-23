package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.BlockPos;import net.minecraft.server.level.*;import net.minecraft.world.entity.*;import net.minecraft.world.level.ClipContext;import net.minecraft.world.phys.*;import java.util.*;
/** Physical, independently clipped emitters. Server hits and fixed-rate energy/damage, no jamming or client damage. */
public final class Hgd01Lasers {
 public record Ray(Vec3 end,Entity hit){}
 public static boolean hostile(ServerPlayer user,Entity target){
  if(!SbwCompat.isDrone(target)||!target.isAlive()||target.isRemoved()||target.isAlliedTo(user))return false;
  var owner=SbwCompat.resolveDroneOwner(user.serverLevel(),target);UUID controller=SbwCompat.getDroneControllerUuid(target);
  if(user.getUUID().equals(controller)||owner.hasUuid()&&user.getUUID().equals(owner.uuid()))return false;
  if(owner.hasName()&&user.getTeam()!=null&&user.server.getScoreboard().getPlayersTeam(owner.name())==user.getTeam())return false;
  for(UUID id:new UUID[]{owner.hasUuid()?owner.uuid():null,controller})if(id!=null){var p=user.server.getPlayerList().getPlayer(id);if(p!=null&&user.isAlliedTo(p))return false;}
  return true;
 }
 public static Ray trace(Hgd01Entity vehicle,int side){
  var l=(ServerLevel)vehicle.level();Vec3 start=vehicle.laserPosition(side,1),direction=vehicle.laserDirection(side,1);double distance=Hgd01Config.LASER_RANGE.get();
  Vec3 end=start.add(direction.scale(distance));if(!loaded(l,start,end)){double lo=0,hi=distance;for(int i=0;i<12;i++){double m=(lo+hi)*.5;if(loaded(l,start,start.add(direction.scale(m))))lo=m;else hi=m;}end=start.add(direction.scale(Math.max(0,lo-.02)));}
  if(!loaded(l,start,end))return new Ray(start,null);
  var block=l.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,vehicle));end=block.getLocation();Entity hit=null;double best=start.distanceToSqr(end);
  var entities=l.getEntities(vehicle,new AABB(start,end).inflate(.15),e->e.isPickable()&&!e.isSpectator()&&!vehicle.hasPassenger(e));
  for(Entity e:entities){var p=e.getBoundingBox().clip(start,end);if(p.isPresent()){double d=p.get().distanceToSqr(start);if(d<best){best=d;hit=e;end=p.get();}}}
  return new Ray(end,hit);
 }
 // Exact horizontal chunk traversal, including corner neighbours. This ray never requests chunks.
 static boolean loaded(ServerLevel l,Vec3 a,Vec3 b){
  int x=net.minecraft.util.Mth.floor(a.x)>>4,z=net.minecraft.util.Mth.floor(a.z)>>4,ex=net.minecraft.util.Mth.floor(b.x)>>4,ez=net.minecraft.util.Mth.floor(b.z)>>4;
  double dx=b.x-a.x,dz=b.z-a.z;int sx=Double.compare(dx,0),sz=Double.compare(dz,0);double tx=sx==0?Double.POSITIVE_INFINITY:((x+(sx>0?1:0))*16d-a.x)/dx,tz=sz==0?Double.POSITIVE_INFINITY:((z+(sz>0?1:0))*16d-a.z)/dz,stepX=sx==0?Double.POSITIVE_INFINITY:16/Math.abs(dx),stepZ=sz==0?Double.POSITIVE_INFINITY:16/Math.abs(dz);
  for(int i=0;i<128;i++){if(!l.hasChunk(x,z))return false;if(x==ex&&z==ez)return true;if(Math.abs(tx-tz)<1e-12){if(!l.hasChunk(x+sx,z)||!l.hasChunk(x,z+sz))return false;x+=sx;z+=sz;tx+=stepX;tz+=stepZ;}else if(tx<tz){x+=sx;tx+=stepX;}else{z+=sz;tz+=stepZ;}}return false;
 }
 private Hgd01Lasers(){}
}
