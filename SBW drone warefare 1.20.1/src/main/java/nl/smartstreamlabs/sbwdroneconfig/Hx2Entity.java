package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.core.BlockPos;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import net.minecraft.network.syncher.*;import net.minecraft.server.level.*;import net.minecraft.util.Mth;import net.minecraft.world.*;import net.minecraft.world.entity.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;import java.util.*;
/** SBW owns replication, input, inventory and movement. Only HX-2 flight forces are specialized. */
public class Hx2Entity extends DroneEntity {
 private static final EntityDataAccessor<Optional<BlockPos>> MOUNT=SynchedEntityData.defineId(Hx2Entity.class,EntityDataSerializers.OPTIONAL_BLOCK_POS);
 private static final EntityDataAccessor<Boolean> FLIGHT=SynchedEntityData.defineId(Hx2Entity.class,EntityDataSerializers.BOOLEAN);
 private static final EntityDataAccessor<Float> THROTTLE=SynchedEntityData.defineId(Hx2Entity.class,EntityDataSerializers.FLOAT);
 private static final EntityDataAccessor<Integer> ENERGY=SynchedEntityData.defineId(Hx2Entity.class,EntityDataSerializers.INT);
 private BlockPos releasePad;private int releaseTicks;
 private long lastMouseTick;
 private short heldButtons;
 private boolean geometryReady;
 private boolean spent;private int flightTicks;private Vec3 previous;private float spin,oldSpin;
 public Hx2Entity(EntityType<? extends DroneEntity> type,Level level){super((EntityType<DroneEntity>)(EntityType<?>)type,level);geometryReady=true;setBoundingBox(Hx2Geometry.bounds(this));}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(MOUNT,Optional.empty());entityData.define(FLIGHT,false);entityData.define(THROTTLE,0f);entityData.define(ENERGY,0);}
 @Override public void lerpTo(double x,double y,double z,float yaw,float pitch,int steps,boolean teleport){super.lerpTo(x,y,z,yaw,pitch,steps,teleport);if(level().isClientSide)setInterpolationSteps(Math.max(1,Math.min(3,steps)));}
 @Override public void handleClientSync(){int steps=getInterpolationSteps();float yaw=getYRot(),pitch=getXRot();super.handleClientSync();if(level().isClientSide&&steps>0){setYRot(Mth.rotLerp(1f/steps,yaw,getServerYaw()));setXRot(Mth.rotLerp(1f/steps,pitch,getServerPitch()));}}
 public BlockPos mount(){return entityData.get(MOUNT).orElse(null);}public boolean flying(){return entityData.get(FLIGHT);}public float throttle(){return entityData.get(THROTTLE);}public int energy(){return level().isClientSide?entityData.get(ENERGY):DroneBatterySystem.currentEnergy(this);}
 public boolean motor(){return flying()&&energy()>0&&throttle()>0;}
 public void bindMonitor(Player p,ItemStack monitor,boolean using){if(!level().isClientSide&&permitted(p)&&SbwCompat.isMonitor(monitor)){entityData.set(DroneEntity.LINKED,true);entityData.set(DroneEntity.CONTROLLER,p.getStringUUID());SbwCompat.linkMonitorToDrone(monitor,this,using);}}
 private void clearInputs(){super.processInput((short)0);super.mouseInput(0,0);SbwCompat.setDroneFire(this,false);}
 public boolean permitted(Player p){UUID id=SbwCompat.getStoredDroneOwnerUuid(this);return p!=null&&p.isAlive()&&!p.isRemoved()&&(id==null||id.equals(p.getUUID())||p.hasPermissions(2));}
 public void dock(BlockPos p){entityData.set(MOUNT,Optional.of(p.immutable()));entityData.set(FLIGHT,false);setDeltaMovement(Vec3.ZERO);getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);ChunkTicketManager.releaseDrone(this);}
 public void undock(){entityData.set(MOUNT,Optional.empty());setNoGravity(false);}
 public void release(org.joml.Matrix4d frame){releasePad=mount();releaseTicks=6;Vec3 p=Hx2Pose.point(frame,Vec3.ZERO),direction=Hx2Pose.direction(frame);undock();moveTo(p.x,p.y,p.z,getYRot(),-35);setBodyXRot(-35);entityData.set(FLIGHT,true);entityData.set(THROTTLE,.75f);flightTicks=0;previous=p;getPersistentData().remove(MissionManager.NO_TICKETS);setDeltaMovement(direction.scale(.7));hurtMarked=true;Player launchPilot=SbwCompat.getDroneControllerPlayer(this);if(launchPilot!=null&&SbwCompat.isUsingLinkedMonitorForDrone(launchPilot,this))super.processInput(heldButtons);}
 public float spin(float p){return Mth.lerp(p,oldSpin,spin);}
 @Override public void baseTick(){if(!level().isClientSide&&spent){discard();return;}oldSpin=spin;if(motor())spin+=.9f+throttle()*1.5f;super.baseTick();if(level().isClientSide||isRemoved()||spent)return;
  entityData.set(ENERGY,energy());
  if(mount()!=null){
   if(level().hasChunkAt(mount())){
    if(level().getBlockEntity(mount()) instanceof HxPlatformBlockEntity be&&getUUID().equals(be.droneId())){var m=Hx2Pose.dock(mount(),be.facing(),be.carriage(0));Vec3 p=Hx2Pose.point(m,Vec3.ZERO);moveTo(p.x,p.y,p.z,be.facing().toYRot(),-35);setBodyXRot(-35);setZRot(0);setDeltaMovement(Vec3.ZERO);setOnGround(false);}
    else undock();
   }
   getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);ChunkTicketManager.releaseDrone(this);
  }else if(flying())ChunkTicketManager.touchDrone(this,1);else{getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);ChunkTicketManager.releaseDrone(this);}
 }
 @Override public void travel(){
  if(level().isClientSide)return;
  if(mount()!=null){clearInputs();setDeltaMovement(Vec3.ZERO);return;}
  if(!flying()){setDeltaMovement(0,onGround()?0:Math.max(-.5,getDeltaMovement().y-.04),0);return;}
  if(releaseTicks>0)releaseTicks--;else releasePad=null;
  flightTicks++;DroneBatterySystem.beforeDroneTravel(this);DroneJammerSystem.beforeDroneTravel(this);DroneWeatherEffects.applyStormEffects(this);
  Player controller=SbwCompat.getDroneControllerPlayer(this);boolean control=controller!=null&&permitted(controller)&&SbwCompat.isUsingLinkedMonitorForDrone(controller,this)&&!DroneJammerSystem.isJammed(this);
  if(control&&controller instanceof ServerPlayer sp&&!FiberOpticLinkSystem.isFiberSessionActive(this)){var captured=PlayerDroneAnchorManager.getCapturedOriginalPosition(sp);Vec3 origin=captured==null?sp.position():captured;int range=DroneWeatherEffects.getEffectiveControlRange(sp,this,AddonConfig.droneMaxRange());if(origin.distanceToSqr(position())>(double)range*range){SbwCompat.stopUsingMonitor(SbwCompat.getActiveLinkedMonitor(sp));PlayerDroneAnchorManager.release(sp,true);control=false;}}
  if(!control){heldButtons=0;clearInputs();SbwCompat.resetDroneInput(this);}
  boolean powered=energy()>0&&flightTicks<=Hx2Config.LIFETIME.get();float target=powered?Mth.clamp(throttle()+(control&&forwardInputDown()?.035f:0)-(control&&backInputDown()?.045f:0),.25f,1):0;entityData.set(THROTTLE,target);
  if(level().getGameTime()-lastMouseTick>3)super.mouseInput(0,0);
  float steer=control?(rightInputDown()?1:0)-(leftInputDown()?1:0):0;
  float turn=control?Mth.clamp(steer*Hx2Config.TURN.get().floatValue()+getMouseMoveSpeedX()*.18f,-Hx2Config.TURN.get().floatValue(),Hx2Config.TURN.get().floatValue()):0;
  setYRot(getYRot()+turn);
  float pitchInput=control?Mth.clamp(getMouseMoveSpeedY()*.6f+(downInputDown()?Hx2Config.PITCH.get().floatValue():0)-(upInputDown()?Hx2Config.PITCH.get().floatValue():0),-Hx2Config.PITCH.get().floatValue(),Hx2Config.PITCH.get().floatValue()):0;
  float neutral=powered?(flightTicks<12?-18:0):18;
  float pitchTarget=Math.abs(pitchInput)>.001f?Mth.clamp(getXRot()+pitchInput,-40,55):Mth.approachDegrees(getXRot(),neutral,.8f);
  setXRot(Mth.approachDegrees(getXRot(),pitchTarget,Hx2Config.PITCH.get().floatValue()));setBodyXRot(getXRot());setZRot(Mth.lerp(.18f,getRoll(),-turn*8));
  double max=Math.max(Hx2Config.SPEED.get(),Hx2Config.MIN_SPEED.get());double speed=powered?Mth.lerp(target,Hx2Config.MIN_SPEED.get(),max):Hx2Config.MIN_SPEED.get()*.8;
  Vec3 desired=Vec3.directionFromRotation(getXRot(),getYRot()).scale(speed),current=getDeltaMovement();var next=Hx2Kinematics.accelerate(new double[]{current.x,current.y,current.z},new double[]{desired.x,desired.y,desired.z},Hx2Config.ACCELERATION.get());Vec3 motion=new Vec3(next[0],next[1],next[2]);if(powered&&motion.length()<Hx2Config.MIN_SPEED.get())motion=desired.normalize().scale(Hx2Config.MIN_SPEED.get());
  if(!powered)motion=motion.add(0,-.035,0);
  // This flight controller replaces native travel. HX-2 data sets gravity to zero; an added lift bias would accumulate into an uncommanded climb.
  setDeltaMovement(motion);hurtMarked=true;
  if(!level().hasChunkAt(BlockPos.containing(position().add(motion)))){setDeltaMovement(0,-.02,0);entityData.set(THROTTLE,0f);return;}
  Vec3 start=position(),end=start.add(motion);var hit=level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this){@Override public net.minecraft.world.phys.shapes.VoxelShape getBlockShape(net.minecraft.world.level.block.state.BlockState state,BlockGetter world,BlockPos pos){return ignoresLaunchBlock(pos,state)?net.minecraft.world.phys.shapes.Shapes.empty():super.getBlockShape(state,world,pos);}});
  if(hit.getType()!=HitResult.Type.MISS){if(Vc01Movement.before(this,motion)!=null)return;detonate(hit.getLocation());return;}
  Entity closest=null;Vec3 impact=null;double best=Double.MAX_VALUE;
  for(Entity e:level().getEntities(this,getBoundingBox().expandTowards(motion).inflate(.25),e->e.isAlive()&&!e.isSpectator()&&e.isPickable()&&e!=controller)){
   var point=e.getBoundingBox().inflate(.14).clip(start,end);if(point.isPresent()&&point.get().distanceToSqr(start)<best){best=point.get().distanceToSqr(start);closest=e;impact=point.get();}
  }if(closest!=null){if(Vc01Movement.before(this,motion)!=null)return;detonate(impact);}previous=start;
 }
 public boolean ignoresLaunchBlock(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){return releaseTicks>0&&releasePad!=null&&state.getBlock() instanceof HxPlatformBlock&&HxPlatformBlock.root(pos,state).equals(releasePad);}
 @Override public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers){}
 @Override public void move(MoverType type,Vec3 motion){
  if(mount()!=null||level().isClientSide)return;
  Vec3 allowed=Hx2Geometry.collide(this,motion);boolean prior=noPhysics;noPhysics=true;try{super.move(type,allowed);}finally{noPhysics=prior;}
  horizontalCollision=Math.abs(allowed.x-motion.x)>1e-7||Math.abs(allowed.z-motion.z)>1e-7;verticalCollision=Math.abs(allowed.y-motion.y)>1e-7;setOnGround(verticalCollision&&motion.y<0);
  if(flying()&&!spent&&(horizontalCollision||verticalCollision)&&motion.lengthSqr()>.04)detonate(position());
 }
 @Override public void processInput(short keys){Player p=SbwCompat.getDroneControllerPlayer(this);if(p!=null&&permitted(p)&&SbwCompat.isUsingLinkedMonitorForDrone(p,this)){heldButtons=keys;if(mount()==null)super.processInput(keys);}}
 @Override public void mouseInput(double x,double y){Player p=SbwCompat.getDroneControllerPlayer(this);if(p!=null&&permitted(p)&&SbwCompat.isUsingLinkedMonitorForDrone(p,this)){lastMouseTick=level().getGameTime();super.mouseInput(x,y);}}
 @Override public InteractionResult interact(Player p,InteractionHand hand){if(!permitted(p))return InteractionResult.FAIL;var held=p.getItemInHand(hand);
  if(p.isCrouching()&&held.isEmpty()&&!flying()&&mount()==null){if(!level().isClientSide){var stack=packed();if(p.getInventory().add(stack)){SbwCompat.disconnectControllersForDrone((ServerLevel)level(),this);ChunkTicketManager.releaseDrone(this);discard();}}return InteractionResult.sidedSuccess(level().isClientSide);}
  if(SbwCompat.isMonitor(held)){if(!level().isClientSide)bindMonitor(p,held,false);return InteractionResult.sidedSuccess(level().isClientSide);}
  return InteractionResult.PASS;
 }
 public ItemStack packed(){if(spent)return ItemStack.EMPTY;var item=new ItemStack(AddonItems.HX2.get());var tag=new CompoundTag();saveWithoutId(tag);for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","HxMount","HxFlight"})tag.remove(k);item.getOrCreateTag().put("EntityTag",tag);if(hasCustomName())item.setHoverName(getCustomName());return item;}
 @Override public ItemStack getPickResult(){return new ItemStack(AddonItems.HX2.get());}
 @Override public boolean engineRunning(){return motor();}
 @Override public float getEngineSoundVolume(){return motor()?.35f+.45f*throttle():0;}
 @Override public net.minecraft.sounds.SoundEvent getEngineSound(){return AddonSounds.CUBED_FPV_DRONE_ENGINE.get();}
 @Override public java.util.List<com.atsuishio.superbwarfare.tools.OBB> getOBBs(){return Hx2Geometry.obbs(this);}
 @Override protected AABB makeBoundingBox(){return geometryReady?Hx2Geometry.bounds(this):super.makeBoundingBox();}
 @Override public AABB getBoundingBoxForCulling(){return new AABB(getX()-1.4,getY()-1.4,getZ()-1.4,getX()+1.4,getY()+1.4,getZ()+1.4);}
 @Override public boolean canBeCollidedWith(){return false;}
 private void consumeDock(){var p=mount();if(p!=null&&level().hasChunkAt(p)&&level().getBlockEntity(p) instanceof HxPlatformBlockEntity b)b.consumed(getUUID());}
 @Override public void remove(Entity.RemovalReason reason){if(!level().isClientSide&&reason.shouldDestroy()){getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);consumeDock();ChunkTicketManager.releaseDrone(this);}super.remove(reason);}
 @Override public void destroy(){if(!spent)detonate(position());}
 public void detonate(Vec3 at){if(spent||!(level() instanceof ServerLevel l))return;if(Boolean.getBoolean("hx2.trace"))System.out.println("HX2_DETONATE tick="+flightTicks+" at="+at+" ownGuide="+releaseTicks+" motion="+getDeltaMovement()+" horizontal="+horizontalCollision+" vertical="+verticalCollision);Sk01Deaths.destroyed(this);spent=true;getPersistentData().putBoolean(MissionManager.NO_TICKETS,true);consumeDock();getPersistentData().putBoolean("HxSpent",true);SbwCompat.disconnectControllersForDrone(l,this);SbwCompat.resetDroneInput(this);ChunkTicketManager.releaseDrone(this);float power=Hx2Config.BLAST.get().floatValue();
  try(var attack=DroneAttackDamage.scope(this);var scope=DroneExplosionVfx.explosionScope(l,this,at.x,at.y,at.z,power,true)){DroneExplosionVfx.detonate(l,at,power);if(AddonConfig.enableDroneCrashExplosionDamage())l.explode(this,at.x,at.y,at.z,power,Level.ExplosionInteraction.TNT);}discard();
 }
 @Override public Vec3 getCameraPosition(float partial,Player p,boolean third,boolean front){Vec3 lens=Hx2Pose.camera(this,partial);if(!third)return lens;var dir=Hx2Pose.direction(Hx2Pose.frame(this,partial));Vec3 end=lens.add(dir.scale(front?5:-5)).add(0,2,0);var hit=level().clip(new ClipContext(lens,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));return hit.getType()==HitResult.Type.MISS?end:hit.getLocation().add(lens.subtract(end).normalize().scale(.15));}
 @Override public Vec2 getCameraRotation(float partial,Player p,boolean third,boolean front){return new Vec2(Mth.rotLerp(partial,yRotO,getYRot())+(front?180:0),Mth.lerp(partial,xRotO,getXRot())*(front?-1:1));}
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(mount()!=null)t.putLong("HxMount",mount().asLong());t.putBoolean("HxFlight",flying());t.putFloat("HxThrottle",throttle());t.putInt("HxFlightTicks",flightTicks);t.putBoolean("HxSpent",spent);}
 @Override public void readAdditionalSaveData(CompoundTag t){if(t.contains("Item",10)&&t.getCompound("Item").isEmpty()){t=t.copy();t.remove("Item");}super.readAdditionalSaveData(t);entityData.set(MOUNT,t.contains("HxMount")?Optional.of(BlockPos.of(t.getLong("HxMount"))):Optional.empty());entityData.set(FLIGHT,t.getBoolean("HxFlight"));entityData.set(THROTTLE,Mth.clamp(t.getFloat("HxThrottle"),0,1));flightTicks=Math.max(0,t.getInt("HxFlightTicks"));spent=t.getBoolean("HxSpent");}
}
