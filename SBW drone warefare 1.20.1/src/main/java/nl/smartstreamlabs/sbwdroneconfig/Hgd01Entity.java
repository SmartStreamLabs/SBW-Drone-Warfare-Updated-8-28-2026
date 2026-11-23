package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import net.minecraft.world.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.network.syncher.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.util.Mth;import net.minecraft.network.chat.Component;import java.util.*;
/** Native SBW tracked vehicle with one remote operator and four independent native passenger slots. */
public class Hgd01Entity extends VatagEntity {
 private static final EntityDataAccessor<Integer> WEAPON=SynchedEntityData.defineId(Hgd01Entity.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Boolean> RAMP=SynchedEntityData.defineId(Hgd01Entity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<CompoundTag> BEAMS=SynchedEntityData.defineId(Hgd01Entity.class,EntityDataSerializers.COMPOUND_TAG);
 private int fireKeys,rampTicks;private long inputTick,nextPulse;private boolean retrieving;private float ramp,oldRamp;private UUID ownerId;
 private Vec3 visualLast;private float visualYaw;private double leftTravel,rightTravel,oldLeftTravel,oldRightTravel;
 private final LinkedHashMap<UUID,Long> boarding=new LinkedHashMap<>();
 public Hgd01Entity(EntityType<? extends Hgd01Entity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(WEAPON,0);entityData.define(RAMP,false);entityData.define(BEAMS,new CompoundTag());}
 public double trackTravel(boolean left,float p){return left?oldLeftTravel+(leftTravel-oldLeftTravel)*p:oldRightTravel+(rightTravel-oldRightTravel)*p;}
 public int weapon(){return entityData.get(WEAPON);}public String weaponName(){return weapon()==0?"TWIN LASER":"20 MM HE";}public UUID ownerId(){return ownerId;}
 public void selectWeapon(ServerPlayer p){if(operator()!=p)return;entityData.set(WEAPON,1-weapon());fireKeys=0;clearBeams();super.input(p,64,aimYaw(),aimPitch());p.displayClientMessage(Component.literal("HGD-01: "+weaponName()),true);}
 @Override public boolean takeControl(ServerPlayer p){if(DroneJamStateManager.isHardJammed(this)||!super.takeControl(p))return false;if(ownerId==null)ownerId=p.getUUID();return true;}
 @Override public void input(ServerPlayer p,int keys,float yaw,float pitch){if(operator()!=p)return;fireKeys=keys&127;inputTick=level().getGameTime();super.input(p,weapon()==0?keys&~48:keys,yaw,pitch);}
 @Override public void loadingHeartbeat(ServerPlayer p){if(operator()==p){fireKeys=0;clearBeams();}super.loadingHeartbeat(p);}
 @Override public void releaseControl(){super.releaseControl();fireKeys=0;clearBeams();}
 private void clearBeams(){if(!entityData.get(BEAMS).isEmpty())entityData.set(BEAMS,new CompoundTag());}
 @Override protected org.joml.Matrix4d remoteAimFrame(){return getVehicleTransform(1);}
 @Override protected float minimumAimPitch(){return -68;}@Override protected float maximumAimPitch(){return Hgd01Frames.maximumPitch(getTurretYRot());}
 @Override public int getTurretControllerIndex(){return -1;}
 @Override public void mouseInput(double x,double y){}
 @Override public void processInput(short keys){} // physical passengers never own the drive or weapons
 @Override public LivingEntity getControllingPassenger(){return null;}
 @Override public void travel(){if(!level().isClientSide)super.travel();}
 @Override public void move(MoverType type,Vec3 movement){if(!level().isClientSide)super.move(type,movement);}
 @Override public double getTargetSpeed(){return super.getTargetSpeed()*Hgd01Config.SPEED.get();}
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){super.lerpTo(x,y,z,yaw,pitch,steps,teleport);if(level().isClientSide)setInterpolationSteps(Math.max(1,Math.min(3,steps)));}
 @Override public org.joml.Matrix4d getTurretTransform(float p){return Hgd01Frames.turret(getVehicleTransform(p),Mth.rotLerp(p,getTurretYRotO(),getTurretYRot()));}
 @Override public org.joml.Matrix4d getBarrelTransform(float p){return Hgd01Frames.gun(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot()));}
 public org.joml.Matrix4d cameraFrame(boolean aim,float p){return aim?Hgd01Frames.sensor(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot())):new org.joml.Matrix4d(getVehicleTransform(p)).translate(.46,1.74,3.64);}
 static Vec3 vec(org.joml.Vector3d p){return new Vec3(p.x,p.y,p.z);}
 @Override public Vec3 camera(boolean aim,float p){return vec(cameraFrame(aim,p).transformPosition(new org.joml.Vector3d()));}
 @Override public Vec3 getShootPos(String name,float p){return vec(Hgd01Frames.point(getBarrelTransform(p),0,0,3.42));}
 public Vec3 laserPosition(int side,float p){return vec(Hgd01Frames.point(Hgd01Frames.laser(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot()),side),0,0,1.4));}
 public Vec3 laserDirection(int side,float p){return vec(Hgd01Frames.laser(getTurretTransform(p),Mth.lerp(p,getTurretXRotO(),getTurretXRot()),side).transformDirection(new org.joml.Vector3d(0,0,1)).normalize());}
 @Override protected boolean remoteFireAllowed(){if(weapon()!=1||getEnergy()<=0||isWreck()||operator()==null||level().getGameTime()-inputTick>10)return false;Vec3 breech=vec(getBarrelTransform(1).transformPosition(new org.joml.Vector3d())),muzzle=getShootPos("Cannon",1);return barrelClear(breech,muzzle);}
 public boolean barrelClear(Vec3 breech,Vec3 muzzle){if(!(level()instanceof net.minecraft.server.level.ServerLevel l)||!Hgd01Lasers.loaded(l,breech,muzzle))return false;return l.clip(new ClipContext(breech,muzzle,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
 @Override public void baseTick(){super.baseTick();oldRamp=ramp;ramp=Mth.approach(ramp,entityData.get(RAMP)?1:0,1f/12);
  if(level().isClientSide){oldLeftTravel=leftTravel;oldRightTravel=rightTravel;if(visualLast!=null){Vec3 delta=position().subtract(visualLast);if(delta.lengthSqr()<16){var f=getVehicleTransform(1).transformDirection(new org.joml.Vector3d(0,0,1)).normalize();double along=delta.dot(new Vec3(f.x,f.y,f.z)),turn=Mth.wrapDegrees(getYRot()-visualYaw)*Mth.DEG_TO_RAD*1.4;leftTravel+=along+turn;rightTravel+=along-turn;}}visualLast=position();visualYaw=getYRot();return;}
  if(isWreck()||!isAlive()){if(operator()!=null)VatagSessions.stop(operator());ejectPassengers();boarding.clear();clearBeams();return;}
  boolean firing=operator()!=null&&operator().isAlive()&&!operator().hasDisconnected()&&level().getGameTime()-inputTick<=10&&(fireKeys&16)!=0&&weapon()==0&&getEnergy()>=Hgd01Config.LASER_COST.get()*2;
  if(!firing)clearBeams();else if(tickCount%2==0){boolean damage=level().getGameTime()>=nextPulse;CompoundTag beams=new CompoundTag();for(int side=0;side<2;side++){Vec3 start=laserPosition(side,1);var origin=vec(Hgd01Frames.laser(getTurretTransform(1),getTurretXRot(),side).transformPosition(new org.joml.Vector3d()));if(!barrelClear(origin,start))continue;var ray=Hgd01Lasers.trace(this,side);beams.putDouble("X"+side,ray.end().x);beams.putDouble("Y"+side,ray.end().y);beams.putDouble("Z"+side,ray.end().z);beams.putLong("Tick",level().getGameTime());if(damage){consumeEnergy(Hgd01Config.LASER_COST.get());if(ray.hit()!=null&&Hgd01Lasers.hostile(operator(),ray.hit()))ray.hit().hurt(new net.minecraft.world.damagesource.DamageSource(level().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(com.atsuishio.superbwarfare.init.ModDamageTypes.LASER),this,operator()),Hgd01Config.LASER_DAMAGE.get().floatValue());}}entityData.set(BEAMS,beams);if(damage)nextPulse=level().getGameTime()+Hgd01Config.LASER_INTERVAL.get();}
  if(firing&&tickCount%8==0)hgdSound("laser_tower_shoot",.5f);if(operator()!=null&&tickCount%8==0&&(Math.abs(Mth.wrapDegrees(getTurretYRot()-getTurretYRotO()))>.5||Math.abs(getTurretXRot()-getTurretXRotO())>.5))hgdSound("turret_turn",.25f);
  tickBoarding();entityData.set(RAMP,rampTicks-- >0||!boarding.isEmpty());
 }
 private void hgdSound(String name,float volume){var sound=net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(net.minecraft.resources.ResourceLocation.tryParse("superbwarfare:"+name));if(sound!=null)level().playSound(null,blockPosition(),sound,net.minecraft.sounds.SoundSource.BLOCKS,volume,1);}
 public Vec3 beamEnd(int side){var n=entityData.get(BEAMS);return n.contains("X"+side)&&level().getGameTime()-n.getLong("Tick")<8?new Vec3(n.getDouble("X"+side),n.getDouble("Y"+side),n.getDouble("Z"+side)):null;}
 public float ramp(float p){return Mth.lerp(p,oldRamp,ramp);}
 @Override public boolean canAddPassenger(Entity e){return e instanceof Player&&!isWreck()&&getPassengers().size()<4&&super.canAddPassenger(e);}
 @Override public InteractionResult interact(Player p,InteractionHand hand){
  var held=p.getItemInHand(hand);if(isRemoved()||retrieving)return InteractionResult.FAIL;
  if(SbwCompat.isMonitor(held)){var r=super.interact(p,hand);if(p instanceof ServerPlayer sp&&permitted(sp)){if(ownerId==null)ownerId=p.getUUID();Hgd01Jam.mark(held,true);}return r;}
  if(p.isShiftKeyDown()){if(held.is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR)&&(!getPassengers().isEmpty()||!boarding.isEmpty())){if(!level().isClientSide)p.displayClientMessage(Component.literal("HGD-01: passengers must leave before packing."),true);return InteractionResult.FAIL;}if(p instanceof ServerPlayer&&!permitted((ServerPlayer)p))return InteractionResult.FAIL;retrieving=true;try{return super.interact(p,hand);}finally{retrieving=false;}}
  if(!held.isEmpty())return InteractionResult.PASS;
  if(level().isClientSide)return InteractionResult.SUCCESS;
  if(!(p instanceof ServerPlayer sp)||p.isPassenger()||p.isSpectator()||!p.isAlive()||isWreck())return InteractionResult.FAIL;
  var local=new org.joml.Matrix4d(getVehicleTransform(1)).invert().transformPosition(new org.joml.Vector3d(p.getX(),p.getY(),p.getZ()));
  if(local.z> -2.25||Math.abs(local.x)>2||getDeltaMovement().horizontalDistance()>.04||getPassengers().size()+boarding.size()>=4){p.displayClientMessage(Component.literal("HGD-01: enter at the rear while stopped; four passenger seats."),true);return InteractionResult.FAIL;}
  rampTicks=60;boarding.put(sp.getUUID(),level().getGameTime());return InteractionResult.CONSUME;
 }
 private void tickBoarding(){var l=(net.minecraft.server.level.ServerLevel)level();var it=boarding.entrySet().iterator();while(it.hasNext()){var entry=it.next();var p=l.getServer().getPlayerList().getPlayer(entry.getKey());if(p==null||!p.isAlive()||p.level()!=level()||p.distanceToSqr(this)>49||p.isPassenger()||level().getGameTime()-entry.getValue()>80||getDeltaMovement().horizontalDistance()>.04){it.remove();continue;}if(ramp>.85){p.startRiding(this);it.remove();}}}
 @Override public Vec3 getDismountLocationForIndex(LivingEntity p,int seat){return getDismountLocationForPassenger(p);}
 @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger){rampTicks=60;for(double x:new double[]{0,.85,-.85,1.6,-1.6})for(double z:new double[]{-4.5,-5.5,-6.5}){var p=vec(Hgd01Frames.point(getVehicleTransform(1),x,0,z));for(int y=0;y<=2;y++){Vec3 at=p.add(0,y+.05,0);var box=passenger.getDimensions(Pose.STANDING).makeBoundingBox(at);if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(at))&&level().noCollision(passenger,box)&&!level().noCollision(passenger,box.move(0,-.2,0)))return at;}}return super.dismount(passenger);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(ownerId!=null)n.putUUID("Hgd01Owner",ownerId);n.putInt("Hgd01Weapon",weapon());}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);ownerId=n.hasUUID("Hgd01Owner")?n.getUUID("Hgd01Owner"):null;entityData.set(WEAPON,Math.max(0,Math.min(1,n.getInt("Hgd01Weapon"))));entityData.set(RAMP,false);boarding.clear();fireKeys=0;clearBeams();}
 @Override public void remove(RemovalReason why){if(!level().isClientSide&&operator()!=null)VatagSessions.stop(operator());super.remove(why);}
 @Override protected AABB makeBoundingBox(){double a=getYRot()*Mth.DEG_TO_RAD,x=Math.abs(Math.cos(a))*1.74+Math.abs(Math.sin(a))*3.7,z=Math.abs(Math.sin(a))*1.74+Math.abs(Math.cos(a))*3.7;return new AABB(getX()-x,getY(),getZ()-z,getX()+x,getY()+3.14,getZ()+z);}
 @Override public AABB getBoundingBoxForCulling(){return new AABB(getX()-6,getY()-.5,getZ()-6,getX()+6,getY()+7,getZ()+6);}
}
