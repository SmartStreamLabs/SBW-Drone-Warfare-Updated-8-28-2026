package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.core.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import net.minecraft.network.syncher.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.util.Mth;import net.minecraft.world.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;import net.minecraft.world.phys.Vec3;import java.util.*;
public class VatagEntity extends GeoVehicleEntity {
 private static final EntityDataAccessor<String> OP=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.STRING);
 private static final EntityDataAccessor<Float> YAW=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.FLOAT),PITCH=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Integer> AMMO=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.INT),ENERGY=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> RELOAD=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Long> SHOT=SynchedEntityData.defineId(VatagEntity.class,EntityDataSerializers.LONG);
 private int lastObservedShootTimer; private boolean observedGun; private long lastClientShot=-100; private final float[] suspension=new float[8],oldSuspension=new float[8];
 private UUID owner;private ServerPlayer operator;private long lastInput;private int buttons;private float desiredYaw,desiredPitch;
 public VatagEntity(EntityType<? extends VatagEntity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(OP,"");entityData.define(YAW,0f);entityData.define(PITCH,0f);entityData.define(AMMO,0);entityData.define(ENERGY,0);entityData.define(RELOAD,false);entityData.define(SHOT,-100L);}
 public boolean permitted(ServerPlayer p){return owner==null||owner.equals(p.getUUID())||p.hasPermissions(2);}
 public void claimOwner(ServerPlayer p){if(owner==null&&permitted(p))owner=p.getUUID();}
 public ServerPlayer operator(){return operator;}
 public boolean remotelyControlled(){return !entityData.get(OP).isEmpty();}
 public boolean takeControl(ServerPlayer p){if(isPassenger()||!permitted(p)||isWreck()||!isAlive()||operator!=null&&operator!=p)return false;if(owner==null)owner=p.getUUID();operator=p;entityData.set(OP,p.getUUID().toString());lastInput=level().getGameTime();holdAimPose();buttons=64;return true;}
 private void holdAimPose(){var d=getBarrelTransform(1).transformDirection(new org.joml.Vector3d(0,0,1)).normalize();desiredYaw=(float)Math.toDegrees(-Math.atan2(d.x,d.z));desiredPitch=(float)Math.toDegrees(-Math.asin(Mth.clamp(d.y,-1,1)));}
 public float aimYaw(){return desiredYaw;}public float aimPitch(){return desiredPitch;}
 /** Neutral keepalive while the authorized client awaits the tracked entity. */
 public void loadingHeartbeat(ServerPlayer p){if(operator!=p)return;lastInput=level().getGameTime();buttons=64;holdAimPose();}
 public void releaseControl(){operator=null;buttons=0;entityData.set(OP,"");setForwardInputDown(false);setBackInputDown(false);setLeftInputDown(false);setRightInputDown(false);setFireInputDown(false);setUpInputDown(true);}
 public void input(ServerPlayer p,int flags,float yaw,float pitch){if(operator!=p||!Float.isFinite(yaw)||!Float.isFinite(pitch))return;lastInput=level().getGameTime();buttons=flags&127;desiredYaw=Mth.wrapDegrees(yaw);desiredPitch=Mth.clamp(pitch,-85,85);if((flags&32)!=0){var gun=getGunData("Cannon");if(gun!=null&&!gun.reloading())gun.startReload();}}
 protected InteractionResult crewInteract(Player p,InteractionHand hand){return super.interact(p,hand);}
 protected boolean nativeCrewControls(){return false;}
 protected boolean remoteAimAllowed(){return true;}
 @Override public void travel(){
  if(!level().isClientSide&&!nativeCrewControls()){boolean active=operator!=null&&operator.isAlive()&&!operator.hasDisconnected()&&operator.level()==level()&&!isWreck()&&level().getGameTime()-lastInput<=20;
   if(!active&&operator!=null)VatagSessions.stop(operator);
   boolean terrain=terrainReady();setForwardInputDown(active&&terrain&&(buttons&1)!=0);setBackInputDown(active&&terrain&&(buttons&2)!=0);setLeftInputDown(active&&(buttons&4)!=0);setRightInputDown(active&&(buttons&8)!=0);setUpInputDown(!active||!terrain||(buttons&64)!=0);setFireInputDown(false);
   if(active&&remoteAimAllowed()){var target=VatagAim.local(remoteAimFrame(),desiredYaw,desiredPitch);setTurretYRot(Mth.approachDegrees(getTurretYRot(),target.x,12));setTurretXRot(Mth.approach(getTurretXRot(),Mth.clamp(target.y,minimumAimPitch(),maximumAimPitch()),8));}
  }super.travel();
 }
 @Override public void baseTick(){super.baseTick();
  if(level().isClientSide){if(!nativeCrewControls()){setTurretYRot(entityData.get(YAW));setTurretXRot(entityData.get(PITCH));}if(!(this instanceof Adv01Entity)&&!(this instanceof Sd01Entity)&&!(this instanceof Gad01Entity)&&!(this instanceof Hgd01Entity)&&!(this instanceof Qd01Entity)&&!(this instanceof Wd01Entity)&&!(this instanceof Dc01Entity)&&!(this instanceof SupplyRoverEntity)&&!(this instanceof MineRoverEntity))updateSuspension();long shot=entityData.get(SHOT);if(shot!=lastClientShot){lastClientShot=shot;if(level().getGameTime()-shot<=3)VatagClient.shot(this);}return;}
  if(operator!=null&&remoteFireAllowed()){var gun=getGunData("Cannon");if(gun!=null&&(buttons&16)!=0&&gun.canShoot(this)){VatagNative.fire(this,operator);}}
  entityData.set(YAW,getTurretYRot());entityData.set(PITCH,getTurretXRot());entityData.set(ENERGY,getEnergy());var gun=getGunData("Cannon");if(gun!=null){int timer=gun.shootTimer.get();if(observedGun&&timer>Math.max(0,lastObservedShootTimer-1))entityData.set(SHOT,level().getGameTime());lastObservedShootTimer=timer;observedGun=true;entityData.set(AMMO,gun.ammo.get());entityData.set(RELOAD,gun.reloading());}
 }
 /** Only samples already loaded terrain. SBW remains responsible for all movement and collisions. */
 private boolean terrainReady(){Vec3 d=getDeltaMovement();if(d.horizontalDistanceSqr()<.0001)return true;d=d.normalize().scale(16);return level().hasChunkAt(blockPosition().offset((int)d.x,0,(int)d.z));}
 private void updateSuspension(){System.arraycopy(suspension,0,oldSuspension,0,8);for(int k=0;k<8;k++){double x=k<4?1.25:-1.25,z=1.6875-(k%4)*1.125;var t=transformPosition(getVehicleTransform(1),x,.4859416256,z);Vec3 center=new Vec3(t.x,t.y,t.z);float offset=0;if(level().hasChunkAt(BlockPos.containing(center))){var hit=level().clip(new net.minecraft.world.level.ClipContext(center.add(0,.25,0),center.add(0,-.75,0),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)offset=Mth.clamp((float)(hit.getLocation().y+.4859416256-center.y),-.15f,.2f);}suspension[k]=Mth.lerp(.4f,suspension[k],offset);}}
 public float suspension(int wheel,float partial){return Mth.lerp(partial,oldSuspension[wheel],suspension[wheel]);}
 /** Native sounds for observers; sender exclusion reserves one close cannon sound for the remote operator. */
 @Override public void playShootSound3p(net.minecraft.world.entity.LivingEntity living,String name){
  if(nativeCrewControls()){super.playShootSound3p(living,name);return;}
  var gun=getGunData(name);if(!(level() instanceof net.minecraft.server.level.ServerLevel l)||gun==null)return;
  var info=gun.get(com.atsuishio.superbwarfare.data.gun.GunProp.SOUND_INFO);float radius=gun.get(com.atsuishio.superbwarfare.data.gun.GunProp.SOUND_RADIUS).floatValue();Vec3 pos=getShootPos(name,1);
  if(info.fire3P!=null)com.atsuishio.superbwarfare.tools.SoundTool.playDistantSound(l,info.fire3P,pos,radius*.4f,1,operator);
  if(info.fire3PFar!=null)com.atsuishio.superbwarfare.tools.SoundTool.playDistantSound(l,info.fire3PFar,pos,radius*.7f,1,operator);
  if(info.getFire3PVeryFar()!=null)com.atsuishio.superbwarfare.tools.SoundTool.playDistantSound(l,info.getFire3PVeryFar(),pos,radius,1,operator);
 }
 @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){return super.getBoundingBoxForCulling().inflate(3,1,3);}
 protected org.joml.Matrix4d remoteAimFrame(){return getVehicleTransformWithCustomPitch(1);}
 protected float minimumAimPitch(){return -35;}protected float maximumAimPitch(){return 12;}
 protected boolean remoteFireAllowed(){return true;}
 public int magazine(){return entityData.get(AMMO);}public boolean reloading(){return entityData.get(RELOAD);}public int displayEnergy(){return entityData.get(ENERGY);}
 public float recoil(float partial){float t=(float)(level().getGameTime()+partial-entityData.get(SHOT));return t<0||t>5?0:.08f*(1-t/5);}
 @Override public Vec3 getShootPos(String weapon,float partial){var v=transformPosition(getBarrelTransform(partial),0,0,2.821875-recoil(partial));return new Vec3(v.x,v.y,v.z);}
 public Vec3 camera(boolean aim,float partial){var t=aim?getBarrelTransform(partial):getVehicleTransform(partial);var v=aim?transformPosition(t,-.46875,.178125,.40625):transformPosition(t,0,1.2140666256375665,2.534375);return new Vec3(v.x,v.y,v.z);}
 @Override public InteractionResult interact(Player p,InteractionHand hand){if(isPassenger()&&getVehicle() instanceof DeliveryDroneEntity)return InteractionResult.FAIL;
  if(isRemoved())return InteractionResult.FAIL;
  if(SbwCompat.isMonitor(p.getItemInHand(hand))){if(p instanceof ServerPlayer sp){if(permitted(sp)){Ndc01Monitor.clear(p.getItemInHand(hand));if(owner==null)owner=sp.getUUID();Hgd01Jam.mark(p.getItemInHand(hand),this instanceof Hgd01Entity);Wd01Entity.markMonitor(p.getItemInHand(hand),this instanceof Wd01Entity);Sd01Entity.mark(p.getItemInHand(hand),this instanceof Sd01Entity);Dc01Docking.markMonitor(p.getItemInHand(hand),this instanceof Dc01Entity);VatagMonitorTag.link(p.getItemInHand(hand),getUUID(),blockPosition(),level().dimension().location().toString());if(CarrierFleet.carrier(this))CarrierFleet.bind(sp,this);sp.displayClientMessage(Component.literal(getName().getString()+" linked. Use this monitor to connect."),true);}else sp.displayClientMessage(Component.literal("VATAG: access denied."),true);}return InteractionResult.sidedSuccess(level().isClientSide);}
  if(p instanceof ServerPlayer sp&&!permitted(sp))return InteractionResult.FAIL;
  if(p.isShiftKeyDown()&&p.getItemInHand(hand).is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)){if(operator!=null)VatagSessions.stop(operator);if(getDeltaMovement().horizontalDistance()>.015){if(!level().isClientSide)p.displayClientMessage(Component.literal("VATAG: wait until stopped."),true);return InteractionResult.FAIL;}return super.interact(p,hand);}
  return p.isShiftKeyDown()?super.interact(p,hand):InteractionResult.PASS;
 }
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("VatagOwner",owner);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("VatagOwner")?n.getUUID("VatagOwner"):null;observedGun=false;releaseControl();}
}
