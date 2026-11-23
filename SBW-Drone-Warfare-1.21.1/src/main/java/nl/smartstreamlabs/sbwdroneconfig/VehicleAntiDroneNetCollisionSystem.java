package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import org.joml.Matrix4d;
import java.util.*;

/** One server authority for the existing inventory attachment; no extra world entities/NBT. */
public final class VehicleAntiDroneNetCollisionSystem {
    private record Pose(long tick,List<Matrix4d> branches) {}
    private record DronePose(long tick,Vec3 pos,List<AABB> parts) {}
    private static final Map<Entity,Pose> PREVIOUS=new WeakHashMap<>();
    private static final Map<Entity,DronePose> DRONES=new WeakHashMap<>();
    private VehicleAntiDroneNetCollisionSystem() {}

    public static boolean tryJamVehicleNet(ServerLevel level,Entity drone) {
        if(!enabled(drone)||AntiDroneNetEffectSystem.isOnCooldown(level,drone))return false;
        var old=DRONES.get(drone); Vec3 movement=old==null?Vec3.ZERO:drone.position().subtract(old.pos);
        AABB search=drone.getBoundingBox().expandTowards(movement.scale(-1)).inflate(48);
        for(Entity vehicle:level.getEntities((Entity)null,search,SbwCompat::hasVehicleAntiDroneNetUpgrade))
            {track(vehicle);if(test(level,drone,vehicle,old))return true;}
        DRONES.put(drone,new DronePose(level.getGameTime(),drone.position(),List.copyOf(NtCollision.parts(drone))));
        return false;
    }
    public static java.util.List<Matrix4d> previousPoses(Entity vehicle){track(vehicle);var pose=PREVIOUS.get(vehicle);return pose==null?null:pose.branches();}
    public static java.util.Set<Entity> candidates(ServerLevel level,AABB region){var result=new java.util.HashSet<Entity>(level.getEntities((Entity)null,region.inflate(48),SbwCompat::hasVehicleAntiDroneNetUpgrade));for(Entity e:new java.util.ArrayList<>(PREVIOUS.keySet()))if(e.level()==level&&SbwCompat.hasVehicleAntiDroneNetUpgrade(e)){var p=Vc01Geometry.profile(e);if(p!=null)for(var b:p.branches())if(Vc01Geometry.bounds(b,Vc01Geometry.matrix(e,b,1)).intersects(region)){result.add(e);break;}}return result;}
    public static void observe(Entity e){if(e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity&&e.level() instanceof ServerLevel&&enabled(e))DRONES.computeIfAbsent(e,k->new DronePose(e.level().getGameTime(),e.position(),List.copyOf(NtCollision.parts(e))));}
    public static void track(Entity vehicle){if(Vc01Geometry.profile(vehicle)==null||!SbwCompat.hasVehicleAntiDroneNetUpgrade(vehicle))return;PREVIOUS.computeIfAbsent(vehicle,e->{List<Matrix4d> matrices=new ArrayList<>();for(var b:Vc01Geometry.profile(e).branches())matrices.add(Vc01Geometry.matrix(e,b,1));return new Pose(e.level().getGameTime(),List.copyOf(matrices));});}
    private static boolean enabled(Entity e){return e!=null&&e.isAlive()&&AddonConfig.vehicleAntiDroneNetEnabled()&&Vc01Settings.ENABLED.get()&&AddonConfig.vehicleAntiDroneNetStopsDrones()&&SbwCompat.isDrone(e);}

    /** Every level END tick: also catches a moving cage against a stationary drone. */
    public static void endTick(ServerLevel level) {
        List<Entity> drones=new ArrayList<>(),vehicles=new ArrayList<>();
        for(Entity e:new ArrayList<>(DRONES.keySet()))if(e.level()==level&&enabled(e))drones.add(e);
        for(Entity e:new ArrayList<>(PREVIOUS.keySet()))if(e.level()==level&&e.isAlive()&&SbwCompat.hasVehicleAntiDroneNetUpgrade(e))vehicles.add(e);
        for(Entity vehicle:vehicles) for(Entity drone:drones) {
            if(!AntiDroneNetEffectSystem.isOnCooldown(level,drone))test(level,drone,vehicle,DRONES.get(drone));
        }
        long tick=level.getGameTime();
        for(Entity vehicle:vehicles){var p=Vc01Geometry.profile(vehicle);List<Matrix4d> matrices=new ArrayList<>();for(var b:p.branches())matrices.add(Vc01Geometry.matrix(vehicle,b,1));PREVIOUS.put(vehicle,new Pose(tick,List.copyOf(matrices)));}
        for(Entity drone:drones)DRONES.put(drone,new DronePose(tick,drone.position(),List.copyOf(NtCollision.parts(drone))));
        PREVIOUS.keySet().removeIf(e->e.isRemoved()||e.level()==level&&!vehicles.contains(e));
        DRONES.keySet().removeIf(e->e.isRemoved()||e.level()==level&&!drones.contains(e));
    }
    private static boolean test(ServerLevel level,Entity drone,Entity vehicle,DronePose oldDrone) {
        var profile=Vc01Geometry.profile(vehicle);if(profile==null)return false;
        var old=PREVIOUS.get(vehicle);long tick=level.getGameTime();
        boolean continuous=oldDrone!=null&&oldDrone.tick>=tick-1&&oldDrone.tick<=tick;
        Vec3 movement=continuous?drone.position().subtract(oldDrone.pos):Vec3.ZERO;
        if(movement.lengthSqr()>128*128){continuous=false;movement=Vec3.ZERO;}
        var parts=continuous?oldDrone.parts:NtCollision.parts(drone);
        var nativeVehicle=(com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)vehicle;
        Vec3 pivot=nativeVehicle.getTurretPos();
        double outer=profile.radius()+(pivot==null?0:pivot.length())+2*nativeVehicle.getRotateOffsetHeight();
        AABB vehiclePath=new AABB(Math.min(vehicle.xo,vehicle.getX()),Math.min(vehicle.yo,vehicle.getY()),Math.min(vehicle.zo,vehicle.getZ()),Math.max(vehicle.xo,vehicle.getX()),Math.max(vehicle.yo,vehicle.getY()),Math.max(vehicle.zo,vehicle.getZ())).inflate(outer);
        boolean candidate=false;for(AABB box:parts)if(vehiclePath.intersects(box.expandTowards(movement))){candidate=true;break;}
        if(!candidate)return false;
        double best=Double.POSITIVE_INFINITY;Vec3 contact=null;String panelId=null;
        for(int index=0;index<profile.branches().size();index++) {
            var branch=profile.branches().get(index);Matrix4d now=Vc01Geometry.matrix(vehicle,branch,1);
            Matrix4d before=old!=null&&old.tick>=tick-1?old.branches.get(index):now;
            if(Vc01Geometry.transform(before,Vec3.ZERO).distanceToSqr(Vc01Geometry.transform(now,Vec3.ZERO))>128*128)before=now;
            AABB broad=Vc01Geometry.bounds(branch,before).minmax(Vc01Geometry.bounds(branch,now));
            Vec3 centre=Vc01Geometry.transform(now,Vec3.ZERO);double r=profile.radius();
            broad=broad.minmax(new AABB(centre.x-r,centre.y-r,centre.z-r,centre.x+r,centre.y+r,centre.z+r));
            for(AABB box:parts) {
                if(!broad.intersects(box.expandTowards(movement)))continue;
                for(int panelIndex=0;panelIndex<branch.panels().size();panelIndex++) {
                    var panel=branch.panels().get(panelIndex);
                    if(Vc01Panels.health(vehicle,Vc01Panels.id(index,branch.panelIds().get(panelIndex))).fraction()<=0)continue;
                    double hit=Vc01Sweep.hit(box,movement,panel,before,now);
                    if(Double.isFinite(hit)&&hit<best){best=hit;panelId=Vc01Panels.id(index,branch.panelIds().get(panelIndex));var atHit=Vc01Geometry.triangle(panel,Vc01Sweep.pose(before,now,hit));contact=closest(atHit,box.getCenter().add(movement.scale(hit)));}
                }
            }
        }
        if(!Double.isFinite(best))return false;
        Vec3 at=continuous?oldDrone.pos.add(movement.scale(Math.max(0,best-.002))):drone.position();
        drone.setPos(at.x,at.y,at.z);
        return Vc01Intercept.hit(level,drone,contact==null?at:contact,"VC-01 vehicle "+vehicle.getStringUUID(),vehicle,panelId);
    }
    public static Vec3 closest(NtGeometry.Triangle t,Vec3 p){
        Vec3 a=t.a(),b=t.b(),c=t.c(),ab=b.subtract(a),ac=c.subtract(a),ap=p.subtract(a);double d1=ab.dot(ap),d2=ac.dot(ap);if(d1<=0&&d2<=0)return a;
        Vec3 bp=p.subtract(b);double d3=ab.dot(bp),d4=ac.dot(bp);if(d3>=0&&d4<=d3)return b;double vc=d1*d4-d3*d2;if(vc<=0&&d1>=0&&d3<=0)return a.add(ab.scale(d1/(d1-d3)));
        Vec3 cp=p.subtract(c);double d5=ab.dot(cp),d6=ac.dot(cp);if(d6>=0&&d5<=d6)return c;double vb=d5*d2-d1*d6;if(vb<=0&&d2>=0&&d6<=0)return a.add(ac.scale(d2/(d2-d6)));double va=d3*d6-d5*d4;if(va<=0&&(d4-d3)>=0&&(d5-d6)>=0)return b.add(c.subtract(b).scale((d4-d3)/((d4-d3)+(d5-d6))));double inv=1/(va+vb+vc);return a.add(ab.scale(vb*inv)).add(ac.scale(vc*inv));
    }

}
