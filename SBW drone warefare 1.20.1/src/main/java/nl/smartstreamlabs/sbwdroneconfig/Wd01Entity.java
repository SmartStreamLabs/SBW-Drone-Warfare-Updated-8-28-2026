package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.entity.*;import net.minecraft.world.level.Level;import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;import net.minecraft.util.Mth;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;

/** SBW Ship propulsion/collisions/energy; only surface support and authenticated remote input are WD-specific. */
public class Wd01Entity extends VatagEntity {
 private static final EntityDataAccessor<Boolean> AFLOAT=SynchedEntityData.defineId(Wd01Entity.class,EntityDataSerializers.BOOLEAN);
 private Wd01Water.Support support=new Wd01Water.Support(0,0,0,0);
 private float wantedYaw,wantedPitch,spin,oldSpin;private boolean haveAim;
 public Wd01Entity(EntityType<? extends Wd01Entity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(AFLOAT,false);}
 public boolean floating(){return entityData.get(AFLOAT);}
 @Override public boolean canAddPassenger(Entity e){return false;}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){
  if(operator()!=p||!Float.isFinite(yaw)||!Float.isFinite(pitch))return;
  super.input(p,flags&~48,yaw,pitch);wantedYaw=yaw;wantedPitch=pitch;haveAim=true;
 }
 @Override public void releaseControl(){super.releaseControl();haveAim=false;}
 @Override public boolean takeControl(ServerPlayer p){boolean ok=super.takeControl(p);if(ok){var d=cameraFrame(false,1).transformDirection(new org.joml.Vector3d(0,0,1));wantedYaw=(float)Math.toDegrees(-Math.atan2(d.x,d.z));wantedPitch=(float)Math.toDegrees(-Math.asin(Mth.clamp(d.y,-1,1)));haveAim=true;}return ok;}
 @Override public void travel(){
  float oldYaw=getTurretYRot(),oldPitch=getTurretXRot();super.travel();
  if(!level().isClientSide){
   if(operator()!=null&&haveAim&&getEnergy()>0){var target=VatagAim.local(getVehicleTransform(1),wantedYaw,wantedPitch);setTurretYRot(Mth.approachDegrees(oldYaw,Mth.clamp(target.x,-110,110),6));setTurretXRot(Mth.approach(oldPitch,Mth.clamp(target.y,-50,50),5));}
   else {setTurretYRot(oldYaw);setTurretXRot(oldPitch);}
  }
 }
 /** Called immediately before the native engine evaluates input and thrust, on the server only. */
 public void beforeShip(){
  support=Wd01Water.sample(level(),position(),getYRot());if(level().isClientSide)return;entityData.set(AFLOAT,support.floating());setSprintInputDown(false);
  boolean brake=!remotelyControlled()||upInputDown()||getEnergy()<=0||!support.floating()||isWreck();
  if(brake){setForwardInputDown(false);setBackInputDown(false);setLeftInputDown(false);setRightInputDown(false);setPower(Mth.approach(getPower(),0,.08f));setDeltaMovement(getDeltaMovement().multiply(.84,1,.84));setDeltaRot(getDeltaRot()*.65f);}
 }
 /** A damped buoyancy force, not a position correction; native gravity and collision still apply. */
 public void afterShip(){
  Vec3 v=getDeltaMovement();if(support.floating()){
   double error=support.height()-getY();double lift=Mth.clamp(error*.075-v.y*.48,-.12,.12);
   setDeltaMovement(v.add(0,lift+computed().getGravity(),0));
   setXRot(Mth.approachDegrees(getXRot(),support.pitch(),.65f));setZRot(Mth.approachDegrees(getRoll(),support.roll(),.65f));
  }else {setPower(0);setDeltaMovement(v.multiply(.45,1,.45));setXRot(Mth.approachDegrees(getXRot(),0,.35f));setZRot(Mth.approachDegrees(getRoll(),0,.35f));}
  if(!level().isClientSide&&remotelyControlled()&&tickCount%10==0&&getEnergy()>0)consumeEnergy(2);
 }
 @Override public void baseTick(){super.baseTick();if(level().isClientSide){oldSpin=spin;spin+=floating()?getPower()*1.1f:0;Wd01Client.effects(this);} }
 public float propellerSpin(float partial){return Mth.lerp(partial,oldSpin,spin);}
 /** Model -Z is converted once to SBW +Z. Both gimbal parents precede the exact lens offset. */
 public org.joml.Matrix4d cameraFrame(boolean bow,float partial){
  var m=new org.joml.Matrix4d(getVehicleTransform(partial));if(bow)return m.translate(0,.3,2.184);
  return m.translate(0,.79,-.62).rotateY(Mth.rotLerp(partial,getTurretYRotO(),getTurretYRot())*Mth.DEG_TO_RAD)
   .translate(0,.48,0).rotateX(Mth.lerp(partial,getTurretXRotO(),getTurretXRot())*Mth.DEG_TO_RAD).translate(0,.135,.162);
 }
 @Override public Vec3 camera(boolean bow,float partial){var p=cameraFrame(bow,partial).transformPosition(new org.joml.Vector3d());return new Vec3(p.x,p.y,p.z);}
 @Override public void setYRot(float yaw){super.setYRot(yaw);setBoundingBox(makeBoundingBox());}
 @Override protected AABB makeBoundingBox(){double a=getYRot()*Mth.DEG_TO_RAD,x=Math.abs(Math.cos(a))*.88+Math.abs(Math.sin(a))*2.56,z=Math.abs(Math.sin(a))*.88+Math.abs(Math.cos(a))*2.56;return new AABB(getX()-x,getY()-.45,getZ()-z,getX()+x,getY()+1.0,getZ()+z);}
 @Override public AABB getBoundingBoxForCulling(){return makeBoundingBox().inflate(.2, .65,.2);}
 @Override public InteractionResult interact(Player p,InteractionHand h){var held=p.getItemInHand(h);var result=super.interact(p,h);if(p instanceof ServerPlayer sp&&permitted(sp)&&SbwCompat.isMonitor(held))markMonitor(held,true);return result;}
 public static boolean waterMonitor(ItemStack stack){return stack.hasTag()&&stack.getTag().getBoolean("WD01WaterMonitor");}
 public static void markMonitor(ItemStack stack,boolean value){stack.getOrCreateTag().putBoolean("WD01WaterMonitor",value);}
}
