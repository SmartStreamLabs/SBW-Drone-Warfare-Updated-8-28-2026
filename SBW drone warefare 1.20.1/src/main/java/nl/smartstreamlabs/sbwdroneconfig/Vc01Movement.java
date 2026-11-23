package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import org.joml.Matrix4d;
import java.util.*;
/** Runs inside SBW/vanilla movement, before the caller can process a hull impact. */
public final class Vc01Movement {
 public record Contact(Vec3 movement,Vec3 end,Vec3 point,Entity vehicle,String panel,List<Matrix4d> previous){}
 public static Contact before(Entity e,Vec3 movement){
  if(!(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)||!(e.level() instanceof ServerLevel level)||!Vc01Settings.ENABLED.get()||!AddonConfig.vehicleAntiDroneNetEnabled()||!AddonConfig.vehicleAntiDroneNetStopsDrones())return null;
  if(SbwCompat.hasVehicleAntiDroneNetUpgrade(e)){
   var profile=Vc01Geometry.profile(e);if(profile==null)return null;List<Matrix4d> poses=VehicleAntiDroneNetCollisionSystem.previousPoses(e);if(poses==null)return null;
   return new Contact(movement,e.position().add(movement),null,e,null,List.copyOf(poses));
  }
  if(!SbwCompat.isDrone(e)||e.isRemoved()||AntiDroneNetEffectSystem.isOnCooldown(level,e))return null;
  double best=Double.POSITIVE_INFINITY;Entity target=null;String key=null;Vec3 point=null;
  for(Entity vehicle:VehicleAntiDroneNetCollisionSystem.candidates(level,e.getBoundingBox().expandTowards(movement).inflate(.01))){
   var profile=Vc01Geometry.profile(vehicle);if(profile==null)continue;
   for(int b=0;b<profile.branches().size();b++){var branch=profile.branches().get(b);var pose=Vc01Geometry.matrix(vehicle,branch,1);
    if(!Vc01Geometry.bounds(branch,pose).intersects(e.getBoundingBox().expandTowards(movement).inflate(.01)))continue;
    for(int p=0;p<branch.panels().size();p++){String id=Vc01Panels.id(b,branch.panelIds().get(p));if(Vc01Panels.health(vehicle,id).fraction()<=0)continue;var tri=Vc01Geometry.triangle(branch.panels().get(p),pose);
     for(AABB part:NtCollision.parts(e)){double t=NtGeometry.sweep(part,movement,tri);if(Double.isFinite(t)&&t<best){best=t;target=vehicle;key=id;point=VehicleAntiDroneNetCollisionSystem.closest(tri,part.getCenter().add(movement.scale(t)));}}
    }
   }
  }
  if(target==null)return null;
  Vec3 allowed=movement.scale(Math.max(0,best-.03/Math.max(.03,movement.length())));
  return new Contact(allowed,e.position().add(allowed),point,target,key,null);
 }
 public static void after(Entity e,Contact hit){
  if(hit==null||!(e.level() instanceof ServerLevel level)||e.isRemoved())return;
  if(hit.previous()!=null){movingVehicle(level,e,hit);return;}
  // Terrain before the panel must win; the projected route alone is not a confirmed contact.
  if(e.position().distanceToSqr(hit.end())>.08*.08||AntiDroneNetEffectSystem.isOnCooldown(level,e))return;
  Vc01Intercept.hit(level,e,hit.point(),"VC-01 movement contact",hit.vehicle(),hit.panel());
 }
 private static void movingVehicle(ServerLevel level,Entity vehicle,Contact motion){
  if(!SbwCompat.hasVehicleAntiDroneNetUpgrade(vehicle))return;var profile=Vc01Geometry.profile(vehicle);if(profile==null)return;
  AABB region=null;boolean changed=false;
  for(int b=0;b<profile.branches().size();b++){var before=motion.previous().get(b);var now=Vc01Geometry.matrix(vehicle,profile.branches().get(b),1);if(!before.equals(now,1e-8))changed=true;Vec3 start=Vc01Geometry.transform(before,Vec3.ZERO),end=Vc01Geometry.transform(now,Vec3.ZERO);AABB swept=new AABB(Math.min(start.x,end.x),Math.min(start.y,end.y),Math.min(start.z,end.z),Math.max(start.x,end.x),Math.max(start.y,end.y),Math.max(start.z,end.z)).inflate(profile.radius()+.01);region=region==null?swept:region.minmax(swept);}
  if(!changed||region==null)return;
  for(Entity drone:level.getEntities(vehicle,region,SbwCompat::isDrone)){
   if(!drone.isAlive()||AntiDroneNetEffectSystem.isOnCooldown(level,drone))continue;
   double best=Double.POSITIVE_INFINITY;Vec3 point=null;String key=null;
   for(int b=0;b<profile.branches().size();b++){var branch=profile.branches().get(b);var old=motion.previous().get(b);var now=Vc01Geometry.matrix(vehicle,branch,1);
    for(int p=0;p<branch.panels().size();p++){String id=Vc01Panels.id(b,branch.panelIds().get(p));if(Vc01Panels.health(vehicle,id).fraction()<=0)continue;
     for(AABB part:NtCollision.parts(drone)){double t=Vc01Sweep.hit(part,Vec3.ZERO,branch.panels().get(p),old,now);if(Double.isFinite(t)&&t<best){best=t;key=id;var tri=Vc01Geometry.triangle(branch.panels().get(p),Vc01Sweep.pose(old,now,t));point=VehicleAntiDroneNetCollisionSystem.closest(tri,part.getCenter());}}
    }
   }
   if(point!=null)Vc01Intercept.hit(level,drone,point,"VC-01 moving vehicle",vehicle,key);
  }
 }
}
