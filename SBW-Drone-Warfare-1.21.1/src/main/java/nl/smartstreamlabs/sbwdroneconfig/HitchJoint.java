package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.world.entity.*;import net.minecraft.world.phys.Vec3;import net.minecraft.util.Mth;
/** Collect both native SBW motion proposals, then integrate a single collision-aware articulated pair. */
public final class HitchJoint {
 public static final boolean DEBUG=Boolean.getBoolean("sbwdrone.hitchDebug");
 private static final ThreadLocal<Map<UUID,Frame>> FRAMES=ThreadLocal.withInitial(HashMap::new);private static final ThreadLocal<Boolean> applying=ThreadLocal.withInitial(()->false);
 private static final class Frame {SupplyRoverEntity r;MedicalTrailerEntity t;Vec3 rm,tm;long tick;Frame(SupplyRoverEntity r,MedicalTrailerEntity t){this.r=r;this.t=t;tick=r.level().getGameTime();}}
 public static boolean paired(Entity a,Entity b){return a instanceof RescueVehicle x&&b instanceof RescueVehicle y&&a.level()==b.level()&&a.getUUID().equals(y.partner())&&b.getUUID().equals(x.partner());}
 public static boolean propose(RescueVehicle unit,MoverType type,Vec3 motion){
  if(applying.get()||type!=MoverType.SELF)return false;var other=RescueCoupling.other(unit);if(other==null||!paired(unit.unit(),other.unit()))return false;
  SupplyRoverEntity r=unit instanceof SupplyRoverEntity s?s:(SupplyRoverEntity)other;MedicalTrailerEntity t=unit instanceof MedicalTrailerEntity m?m:(MedicalTrailerEntity)other;
  Frame f=FRAMES.get().computeIfAbsent(r.getUUID(),id->new Frame(r,t));if(f.r!=r||f.t!=t||f.tick!=r.level().getGameTime()){FRAMES.get().remove(r.getUUID());f=new Frame(r,t);FRAMES.get().put(r.getUUID(),f);}
  if(unit==r)f.rm=motion;else f.tm=motion;return true;
 }
 private static Vec3 rolling(MedicalTrailerEntity t,Vec3 motion){if(!t.onGround())return motion;Vec3 axis=t.getViewVector(1).multiply(1,0,1).normalize();double speed=motion.dot(axis);return new Vec3(axis.x*speed,motion.y,axis.z*speed);}
 public static void flush(SupplyRoverEntity r,MedicalTrailerEntity t){Frame f=FRAMES.get().remove(r.getUUID());if(f!=null)integrate(f);}
 private static void integrate(Frame f){
  var r=f.r;var t=f.t;if(r.isRemoved()||t.isRemoved()||!paired(r,t))return;
  Vec3 startR=r.position(),startT=t.position();double before=r.hitch().distanceTo(t.hitch());if(before>7){if(!r.level().isClientSide)RescueCoupling.detach(r);applying.set(true);try{if(f.rm!=null)r.move(MoverType.SELF,f.rm);if(f.tm!=null)t.move(MoverType.SELF,f.tm);}finally{applying.set(false);}return;}
  Vec3 rm=f.rm==null?Vec3.ZERO:f.rm,tm=rolling(t,f.tm==null?Vec3.ZERO:f.tm);
  applying.set(true);try{
   for(int sub=0;sub<4;sub++){
    r.move(MoverType.SELF,rm.scale(.25));t.move(MoverType.SELF,tm.scale(.25));r.terrainCompact(r.computed().getTerrainCompat());t.terrainCompact(t.computed().getTerrainCompat());
    for(int iteration=0;iteration<6;iteration++){
     Vec3 to=r.hitch().subtract(t.position().add(0,t.getRotateOffsetHeight(),0));float yaw=(float)Math.toDegrees(-Math.atan2(to.x,to.z));
     t.setYRot(r.getYRot()+Mth.clamp(Mth.wrapDegrees(yaw-r.getYRot()),-85,85));
     Vec3 old=t.hitch();float pitch=t.getXRot();t.setXRot(pitch+.1f);double derivative=(t.hitch().y-old.y)/.1;t.setXRot(pitch);
     if(Math.abs(derivative)>.01)t.setXRot(Mth.clamp(pitch+(float)((r.hitch().y-old.y)/derivative),-40,40));
     boolean rg=r.onGround(),tg=t.onGround();Vec3 error=r.hitch().subtract(t.hitch());double n=error.length();if(n<.0005)break;if(n>.2)error=error.scale(.2/n);
     // Inverse-mass projection. All displacement goes through native swept collision; no setPos/teleport.
     r.move(MoverType.SELF,new Vec3(-error.x*.16279,0,-error.z*.16279));
     t.move(MoverType.SELF,new Vec3(error.x*.83721,error.y,error.z*.83721));r.setOnGround(rg||r.onGround());t.setOnGround(tg||t.onGround());
    }
   }
   // SBW interpolates yaw linearly. Keep the previous angle in the same 360-degree branch.
   r.yRotO=r.getYRot()-Mth.wrapDegrees(r.getYRot()-r.yRotO);t.yRotO=t.getYRot()-Mth.wrapDegrees(t.getYRot()-t.yRotO);
   Vec3 rv=r.position().subtract(startR),tv=t.position().subtract(startT);
   r.setDeltaMovement(new Vec3(rv.x,r.getDeltaMovement().y,rv.z));t.setDeltaMovement(rolling(t,new Vec3(tv.x,t.getDeltaMovement().y,tv.z)));
   if(!r.level().isClientSide){r.setServerYaw(r.getYRot());r.setServerPitch(r.getXRot());t.setServerYaw(t.getYRot());t.setServerPitch(t.getXRot());}
   for(var patient:t.getPassengers())t.positionRider(patient,Entity::setPos);
   if(DEBUG&&!r.level().isClientSide&&r.tickCount%5==0)SbwDroneRangeConfig.LOGGER.info("HITCH server tick={} before={} after={} R={} T={}",r.tickCount,before,r.hitch().distanceTo(t.hitch()),r.hitch(),t.hitch());
  }finally{applying.set(false);}
 }
 public static void finishClientFrame(net.minecraft.world.level.Level level){for(var f:List.copyOf(FRAMES.get().values())){FRAMES.get().remove(f.r.getUUID());if(f.r.level()==level&&!f.r.isRemoved()&&!f.t.isRemoved())integrate(f);}if(level==null)FRAMES.get().clear();}
 public static void forget(UUID id){FRAMES.get().entrySet().removeIf(e->e.getKey().equals(id)||e.getValue().t.getUUID().equals(id));}
 public static void clear(){FRAMES.get().clear();}
}
