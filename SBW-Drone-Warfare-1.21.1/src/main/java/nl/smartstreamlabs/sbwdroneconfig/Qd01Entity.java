package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import net.minecraft.network.syncher.*;import net.minecraft.util.Mth;import net.minecraft.server.level.ServerPlayer;

/** Native SBW tracked drive/gun/inventory. No passenger, fake player, AI target selection or client teleport drive. */
public class Qd01Entity extends VatagEntity {
 private static final EntityDataAccessor<Float> STANCE=SynchedEntityData.defineId(Qd01Entity.class,EntityDataSerializers.FLOAT),PHASE=SynchedEntityData.defineId(Qd01Entity.class,EntityDataSerializers.FLOAT);
 private final Qd01PoseClock poseClock=new Qd01PoseClock();private float oldStance,stance,phase;private Vec3 last;private float lastYaw;private Qd01Gait gait;
 public Qd01Entity(EntityType<? extends Qd01Entity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STANCE,0f);b.define(PHASE,0f);}
 @Override public boolean canAddPassenger(Entity e){return false;}
 /** A monitor operator is not a native local passenger. Client prediction must not fight the authoritative native track solver. */
 @Override public void travel(){if(!level().isClientSide)super.travel();}
 @Override public void move(MoverType type,Vec3 delta){if(!level().isClientSide)super.move(type,delta);}
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps){super.lerpTo(x,y,z,yaw,pitch,steps);if(level().isClientSide)setInterpolationSteps(Math.max(1,Math.min(3,steps)));}
 @Override public void handleClientSync(){int steps=getInterpolationSteps();float yaw=getYRot(),pitch=getXRot();super.handleClientSync();if(level().isClientSide&&steps>0){setYRot(Mth.rotLerp(1f/steps,yaw,getServerYaw()));setXRot(Mth.rotLerp(1f/steps,pitch,getServerPitch()));}}
 @Override public double getTargetSpeed(){return super.getTargetSpeed()*Qd01Config.SPEED.get();}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){super.input(p,getEnergy()>0?flags:flags&~48,yaw,pitch);}
 public float stance(float partial){return level().isClientSide&&poseClock!=null?poseClock.sample(level().getGameTime(),entityData.get(STANCE),partial):Mth.lerp(partial,oldStance,stance);}public float gaitPhase(){return entityData.get(PHASE);}
 public Qd01Gait gait(){if(gait==null)gait=new Qd01Gait();return gait;}
 public org.joml.Matrix4d bodyFrame(float partial){return new org.joml.Matrix4d(getVehicleTransform(partial)).translate(0,stance(partial),0);}
 public org.joml.Matrix4d nativeFrame(float partial){return bodyFrame(partial).rotateY(Math.PI);}
 @Override public org.joml.Matrix4d getTurretTransform(float partial){return bodyFrame(partial).translate(0,1.08,0).rotateY(Mth.rotLerp(partial,getTurretYRotO(),getTurretYRot())*Mth.DEG_TO_RAD);}
 @Override public org.joml.Matrix4d getBarrelTransform(float partial){return getTurretTransform(partial).translate(0,.35,0).rotateX(Mth.lerp(partial,getTurretXRotO(),getTurretXRot())*Mth.DEG_TO_RAD);}
 public org.joml.Matrix4d cameraFrame(boolean aim,float partial){return aim?getBarrelTransform(partial).translate(-.26,.24,.32):bodyFrame(partial).translate(0,.86,.87);}
 @Override public Vec3 camera(boolean aim,float partial){var p=cameraFrame(aim,partial).transformPosition(new org.joml.Vector3d());return new Vec3(p.x,p.y,p.z);}
 @Override public Vec3 getShootPos(String name,float partial){var p=getBarrelTransform(partial).transformPosition(new org.joml.Vector3d(0,.10,1.26));return new Vec3(p.x,p.y,p.z);}
 @Override protected boolean remoteFireAllowed(){
  if(operator()==null||getEnergy()<=0||!isAlive()||isWreck()||!onGround())return false;
  var b=getBarrelTransform(1).transformPosition(new org.joml.Vector3d());Vec3 breech=new Vec3(b.x,b.y,b.z),muzzle=getShootPos("Cannon",1);if(!level().hasChunkAt(net.minecraft.core.BlockPos.containing(muzzle)))return false;
  var hit=level().clip(new ClipContext(breech,muzzle,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.MISS)return false;
  // Body-space guard independent of the aim-camera or operator's position.
  var inv=new org.joml.Matrix4d(bodyFrame(1)).invert();var a=inv.transformPosition(new org.joml.Vector3d(breech.x,breech.y,breech.z));var z=inv.transformPosition(new org.joml.Vector3d(muzzle.x,muzzle.y,muzzle.z));
  return new AABB(-.44,.59,-.78,.44,1.06,.78).clip(new Vec3(a.x,a.y,a.z),new Vec3(z.x,z.y,z.z)).isEmpty();
 }
 public void beforeTrack(){if(!remotelyControlled()||getEnergy()<=0||isWreck()||upInputDown()){
  setForwardInputDown(false);setBackInputDown(false);setLeftInputDown(false);setRightInputDown(false);setPower(Mth.approach(getPower(),0,.07f));setDeltaRot(getDeltaRot()*.65f);setDeltaMovement(getDeltaMovement().multiply(.78,1,.78));
 }}
 @Override public void baseTick(){super.baseTick();oldStance=stance;
  if(level().isClientSide){stance=entityData.get(STANCE);return;}
  if(last!=null){var movement=position().subtract(last);if(onGround()&&movement.y>.15&&movement.y<.65){stance=(float)Mth.clamp(stance-movement.y,-.45,.06);entityData.set(STANCE,stance);}var f=getVehicleTransform(1).transformDirection(new org.joml.Vector3d(0,0,1));double along=movement.x*f.x+movement.z*f.z,turn=Math.abs(Mth.wrapDegrees(getYRot()-lastYaw))*Mth.DEG_TO_RAD*.64,travel=movement.horizontalDistance();if(travel+turn>.001){phase+=(float)((travel+turn)/.48);phase-=Mth.floor(phase);if(tickCount%2==0)entityData.set(PHASE,phase);}}
  last=position();lastYaw=getYRot();if(tickCount%2==0)groundPose();if(remotelyControlled()&&tickCount%10==0&&getEnergy()>0)consumeEnergy(2);
 }
 private void groundPose(){
  if(!onGround()){stance=Mth.approach(stance,0,.025f);entityData.set(STANCE,stance);return;}
  double[] h=new double[4];int count=0;double total=0,low=Double.POSITIVE_INFINITY,high=Double.NEGATIVE_INFINITY;
  // Upright yaw-only reference keeps support independent of the previous slope correction.
  var m=new org.joml.Matrix4d().translation(getX(),getY(),getZ()).rotateY(-getYRot()*Mth.DEG_TO_RAD+Math.PI);
  for(int i=0;i<4;i++){var p=m.transformPosition(new org.joml.Vector3d(Qd01Ground.x(i),0,Qd01Ground.z(i)));var hit=Qd01Ground.contact(level(),this,new Vec3(p.x,p.y,p.z),.65,.8);h[i]=hit==null?Double.NaN:hit.y;if(hit!=null){total+=hit.y;low=Math.min(low,hit.y);high=Math.max(high,hit.y);count++;}}
  if(count>=3){float offset=(float)Mth.clamp(total/count-getY()-Math.max(0,high-low-.05)*.25,-.35,.06);stance=Mth.approach(stance,offset,.045f);float pitch=Double.isFinite(h[0]+h[1]+h[2]+h[3])?(float)Math.toDegrees(Math.atan2((h[2]+h[3]-h[0]-h[1])*.5,1.03)):0;float roll=Double.isFinite(h[0]+h[1]+h[2]+h[3])?(float)Math.toDegrees(Math.atan2((h[0]+h[2]-h[1]-h[3])*.5,1.28)):0;setXRot(Mth.approachDegrees(getXRot(),Mth.clamp(pitch,-10,10),.8f));setZRot(Mth.approachDegrees(getRoll(),Mth.clamp(roll,-10,10),.8f));}
  entityData.set(STANCE,stance);
 }
 @Override public boolean canBeCollidedWith(){return false;} // precise nearby components replace an enclosing solid query box
 @Override protected AABB makeBoundingBox(){double a=getYRot()*Mth.DEG_TO_RAD,x=Math.abs(Math.cos(a))*.705+Math.abs(Math.sin(a))*.87,z=Math.abs(Math.sin(a))*.705+Math.abs(Math.cos(a))*.87;return new AABB(getX()-x,getY(),getZ()-z,getX()+x,getY()+1.77,getZ()+z);}
 @Override public AABB getBoundingBoxForCulling(){return new AABB(getX()-1.5,getY()-.4,getZ()-1.5,getX()+1.5,getY()+2.4,getZ()+1.5);}
}
