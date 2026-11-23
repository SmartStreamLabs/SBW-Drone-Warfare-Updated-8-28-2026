package nl.smartstreamlabs.sbwdroneconfig;
import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
import net.minecraft.world.entity.*;import net.minecraft.world.level.*;import net.minecraft.world.phys.*;
import net.minecraft.world.*;import net.minecraft.world.entity.player.Player;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;import net.minecraft.network.chat.Component;import net.minecraft.network.syncher.*;import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;import net.minecraft.util.Mth;import java.util.*;
/** Native SBW wheel vehicle. Dock contents are real vanilla passengers, never saved item copies. */
public class Dc01Entity extends VatagEntity {
 private static final List<EntityDataAccessor<Integer>> IDS=java.util.stream.IntStream.range(0,3).mapToObj(i->SynchedEntityData.defineId(Dc01Entity.class,EntityDataSerializers.INT)).toList();
 private static final List<EntityDataAccessor<Integer>> TYPES=java.util.stream.IntStream.range(0,3).mapToObj(i->SynchedEntityData.defineId(Dc01Entity.class,EntityDataSerializers.INT)).toList();
 private static final EntityDataAccessor<Integer> READY=SynchedEntityData.defineId(Dc01Entity.class,EntityDataSerializers.INT);
 private final float[] suspension=new float[6],oldSuspension=new float[6];public int lastFailure;private long lastAction=-10;private boolean selectingItem;
 public Dc01Entity(EntityType<? extends Dc01Entity> type,Level level){super(type,level);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);for(var id:IDS)builder.define(id,-1);for(var type:TYPES)builder.define(type,0);builder.define(READY,0);}
 public int dockId(int slot){return entityData.get(IDS.get(slot));}public int dockType(int slot){return entityData.get(TYPES.get(slot));}public int readyMask(){return entityData.get(READY);}
 public int mask(){if(dockType(0)==3)return 7;int m=0;for(int i=0;i<3;i++)if(dockId(i)>=0)m|=1<<i;return m;}
 public int slot(Entity e){if(!level().isClientSide)return e.getVehicle()==this?e.getPersistentData().getInt(Dc01Docking.SLOT):-1;for(int i=0;i<3;i++)if(dockId(i)==e.getId())return i;return -1;}
 public Entity drone(int slot){for(var e:getPassengers())if(slot(e)==slot)return e;return null;}
 public boolean stopped(){return getDeltaMovement().lengthSqr()<.000225;}
 public float suspension(int wheel,float partial){return Mth.lerp(partial,oldSuspension[wheel],suspension[wheel]);}
 @Override public void input(ServerPlayer p,int flags,float yaw,float pitch){super.input(p,flags&~48,yaw,0);}
 @Override public Vec3 camera(boolean aim,float partial){var v=transformPosition(getVehicleTransform(partial),0,1.194977065625,2.058);return new Vec3(v.x,v.y,v.z);}
 @Override public boolean canAddPassenger(Entity e){return (e.getType()==AddonEntities.CUBED_FPV_DRONE.get()||e.getType()==AddonEntities.HEAVY_BOMBER_DRONE.get())&&e instanceof DroneEntity&&Dc01Layout.fits(e.getPersistentData().getInt(Dc01Docking.SLOT),e.getPersistentData().getInt(Dc01Docking.PROFILE),mask());}
 public org.joml.Matrix4d dockMatrix(int slot,float partial){return Dc01Layout.mount(getVehicleTransform(partial),slot,dockType(slot));}
 @Override public void positionRider(Entity e,Entity.MoveFunction move){int s=slot(e);if(s<0)return;var v=dockMatrix(s,1).transformPosition(new org.joml.Vector3d());move.accept(e,v.x,v.y,v.z);e.setYRot(getYRot());e.yRotO=yRotO;e.setXRot(0);e.xRotO=0;}
 private void syncDocks(){for(int s=0;s<3;s++){Entity e=drone(s);entityData.set(IDS.get(s),e==null?-1:e.getId());entityData.set(TYPES.get(s),e==null?0:e.getPersistentData().getInt(Dc01Docking.PROFILE));}}
 @Override public void baseTick(){super.baseTick();if(level().isClientSide){System.arraycopy(suspension,0,oldSuspension,0,6);for(int i=0;i<6;i++){double x=i<3?1.23:-1.23,z=1.28-(i%3)*1.28;var v=transformPosition(getVehicleTransform(1),x,Dc01Layout.WHEEL_RADIUS,z);var at=new Vec3(v.x,v.y,v.z);float off=0;if(level().hasChunkAt(BlockPos.containing(at))){var hit=level().clip(new ClipContext(at.add(0,.25,0),at.add(0,-.8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this));if(hit.getType()!=HitResult.Type.MISS)off=Mth.clamp((float)(hit.getLocation().y+Dc01Layout.WHEEL_RADIUS-at.y),-.10f,.10f);}suspension[i]=Mth.lerp(.4f,suspension[i],off);}return;}
  syncDocks();for(Entity e:getPassengers())Dc01Docking.neutralize(e);if(isWreck()){releaseAll();return;}if(tickCount%10==0){int ready=0;for(int i=0;i<3;i++){Entity e=drone(i);if(e!=null&&stopped()&&e.isAlive()&&(!AddonConfig.enableBatterySystem()||!DroneBatterySystem.isOutOfEnergyState(e))&&clearRoute(e,i))ready|=1<<i;}entityData.set(READY,ready);}}
 public void fail(Player p,int reason){lastFailure=reason;if(!selectingItem)p.displayClientMessage(Component.translatable("dc01.reason."+reason),true);}
 private boolean permittedAction(ServerPlayer p){if(p.isSpectator()||!p.isAlive()||p.hasDisconnected()||p.level()!=level()||!permitted(p)||!isAlive()||isRemoved()||isWreck()){fail(p,1);return false;}if(!stopped()){fail(p,2);return false;}return true;}
 public boolean dock(ServerPlayer p,Entity e,int s){if(PlatformInventory.guard(this,p))return false;if(!permittedAction(p))return false;if(FiberOpticLinkSystem.isFiberSessionActive(e)){fail(p,11);return false;}int profile=Dc01Docking.profile(e);if(profile==0){fail(p,e.getType()==AddonEntities.CUBED_FPV_DRONE.get()&&!SbwCompat.getDroneCurrentItem(e).isEmpty()?12:3);return false;}if(e.level()!=level()||e.isRemoved()||!e.isAlive()||e.isPassenger()||!getBoundingBox().inflate(6).intersects(e.getBoundingBox())){fail(p,4);return false;}if(!Dc01Docking.owned(p,e)){fail(p,1);return false;}if(!Dc01Layout.fits(s,profile,mask())){fail(p,5);return false;}
  if(!clearDeck(e,s,profile)){fail(p,6);return false;}
  SbwCompat.disconnectControllersForDrone(p.serverLevel(),e);Dc01Docking.neutralize(e);var n=e.getPersistentData();n.putInt(Dc01Docking.SLOT,s);n.putInt(Dc01Docking.PROFILE,profile);
  // Explicit server docking has already checked permissions, layout and the single parent.
  // Bypass vanilla dismount cooldown so a recently landed drone can be recovered.
  if(!e.startRiding(this,true)||e.getVehicle()!=this||!getPassengers().contains(e)){e.stopRiding();n.remove(Dc01Docking.SLOT);n.remove(Dc01Docking.PROFILE);fail(p,5);return false;}SbwCompat.ensureDroneOwner(e,p);claimOwner(p);syncDocks();positionRider(e,(entity,x,y,z)->entity.setPos(x,y,z));entityData.set(READY,0);lastFailure=0;return true;}
 public static boolean dockItem(ItemStack stack){return stack.getItem() instanceof CubedFpvDroneItem||stack.getItem() instanceof HeavyBomberDroneItem;}
 public boolean loadItem(ServerPlayer p,ItemStack stack,int requested){if(PlatformInventory.guard(this,p))return false;
  if(!dockItem(stack)||stack.isEmpty()||!permittedAction(p))return false;
  int profile=stack.getItem() instanceof HeavyBomberDroneItem?3:1,slot=requested;
  if(slot<0){for(int candidate:new int[]{0,1,2})if(Dc01Layout.fits(candidate,profile,mask())){slot=candidate;break;}}
  if(!Dc01Layout.fits(slot,profile,mask())){fail(p,5);return false;}
  if(!level().mayInteract(p,blockPosition())||!p.mayUseItemAt(blockPosition(),net.minecraft.core.Direction.UP,stack)){fail(p,1);return false;}
  var type=profile==3?AddonEntities.HEAVY_BOMBER_DRONE.get():AddonEntities.CUBED_FPV_DRONE.get();
  // Use the same native item spawn path as placing the item normally: EntityTag,
  // permissions and saved data are interpreted by Minecraft, not a second inventory.
  var e=type.spawn(p.serverLevel(),stack,p,blockPosition(),MobSpawnType.SPAWN_EGG,false,false);if(e==null){fail(p,4);return false;}
  if(e instanceof HeavyBomberDroneEntity bomber)bomber.loadFromItem(stack);
  SbwCompat.assignDroneOwner(e,p);
  if(!dock(p,e,slot)){e.discard();return false;}
  if(!p.getAbilities().instabuild)stack.shrink(1);p.getInventory().setChanged();return true;
 }
 public void loadNearest(ServerPlayer p,int slot){
  if(!permittedAction(p))return;
  var candidates=level().getEntitiesOfClass(DroneEntity.class,getBoundingBox().inflate(6),e->e.isAlive()&&!e.isRemoved()&&!e.isPassenger());
  var origin=dockMatrix(slot,1).transformPosition(new org.joml.Vector3d());candidates.sort(Comparator.comparingDouble(e->e.distanceToSqr(origin.x,origin.y,origin.z)));
  int reason=4;
  for(var e:candidates){int profile=Dc01Docking.profile(e);if(profile==0){if(reason==4)reason=3;continue;}if(!Dc01Docking.owned(p,e)){if(reason==4||reason==3)reason=1;continue;}if(!Dc01Layout.fits(slot,profile,mask())){reason=5;continue;}if(FiberOpticLinkSystem.isFiberSessionActive(e)){reason=11;continue;}if(!clearDeck(e,slot,profile)){reason=6;continue;}if(dock(p,e,slot))return;reason=lastFailure;}
  // Saved entity data can make an otherwise compatible item undockable (for
  // example an FPV with an underslung payload). Keep looking for a usable item;
  // rejected attempts must not consume it or leave a failure toast on success.
  selectingItem=true;
  try{for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(dockItem(stack)&&Dc01Layout.fits(slot,stack.getItem() instanceof HeavyBomberDroneItem?3:1,mask())){if(loadItem(p,stack,slot))return;reason=lastFailure;}}}
  finally{selectingItem=false;}
  fail(p,reason);
 }
 private boolean clearDeck(Entity candidate,int slot,int profile){var m=Dc01Layout.mount(getVehicleTransform(1),slot,profile);double r=profile==3?1.18:.52,low=Dc01Layout.floor(profile),high=profile==3?1.02:.63;AABB bounds=null;for(int i=0;i<8;i++){var v=m.transformPosition(new org.joml.Vector3d((i&1)==0?-r:r,(i&2)==0?low:high,(i&4)==0?-r:r));var point=new Vec3(v.x,v.y,v.z);var corner=new AABB(point,point);bounds=bounds==null?corner:bounds.minmax(corner);}if(!level().hasChunksAt(BlockPos.containing(bounds.minX,bounds.minY,bounds.minZ),BlockPos.containing(bounds.maxX,bounds.maxY,bounds.maxZ)))return false;for(var shape:level().getBlockCollisions(candidate,bounds))if(!shape.isEmpty())return false;return level().getEntities(candidate,bounds,other->other!=this&&!getPassengers().contains(other)&&other.isPickable()&&!other.isSpectator()).isEmpty();}
 private boolean clearRoute(Entity e,int slot){var at=dockMatrix(slot,1).transformPosition(new org.joml.Vector3d());double r=e instanceof HeavyBomberDroneEntity?1.18:.52;AABB path=new AABB(at.x-r,at.y+.05,at.z-r,at.x+r,at.y+3,at.z+r);if(!level().hasChunksAt(BlockPos.containing(path.minX,path.minY,path.minZ),BlockPos.containing(path.maxX,path.maxY,path.maxZ))||!level().getWorldBorder().isWithinBounds(path))return false;for(var shape:level().getBlockCollisions(e,path))if(!shape.isEmpty())return false;return level().getEntities(e,path,other->other!=this&&!getPassengers().contains(other)&&other.isPickable()&&!other.isSpectator()).isEmpty();}
 public boolean launch(ServerPlayer p,int slot){if(PlatformInventory.guard(this,p))return false;if(!permittedAction(p))return false;Entity e=drone(slot);if(e==null||!e.isAlive()||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck()){fail(p,4);return false;}if(!Dc01Docking.owned(p,e)){fail(p,1);return false;}if(!clearRoute(e,slot)){fail(p,6);return false;}if(AddonConfig.enableBatterySystem()&&DroneBatterySystem.isOutOfEnergyState(e)){fail(p,7);return false;}if(!SbwCompat.isMonitor(p.getMainHandItem())||!SbwCompat.hasEquippedFpvGoggles(p)){fail(p,8);return false;}if(DroneJamStateManager.isHardJammed(e)){fail(p,9);return false;}
  if(!CarrierFleet.prepareLaunch(p,this,e))return false;
  int profile=e.getPersistentData().getInt(Dc01Docking.PROFILE);e.stopRiding();e.getPersistentData().remove(Dc01Docking.SLOT);e.getPersistentData().remove(Dc01Docking.PROFILE);e.setOnGround(false);e.setDeltaMovement(0,.16,0);Dc01Docking.departure(e,this);
  if(!CarrierFleet.launched(p,this,e)){e.getPersistentData().putInt(Dc01Docking.SLOT,slot);e.getPersistentData().putInt(Dc01Docking.PROFILE,profile);e.startRiding(this,true);Dc01Docking.neutralize(e);positionRider(e,(who,x,y,z)->who.setPos(x,y,z));syncDocks();return false;}
  syncDocks();entityData.set(READY,0);lastFailure=0;return true;
 }
 public boolean unload(ServerPlayer p,int slot){if(PlatformInventory.guard(this,p))return false;if(!permittedAction(p))return false;Entity e=drone(slot);if(e==null||!e.isAlive()||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck()){fail(p,4);return false;}if(!Dc01Docking.owned(p,e)){fail(p,1);return false;}for(int side:new int[]{1,-1}){var v=transformPosition(getVehicleTransform(1),side*(e instanceof HeavyBomberDroneEntity?3.2:2.4),2,Dc01Layout.z(slot));Vec3 above=new Vec3(v.x,v.y,v.z);if(!level().hasChunkAt(BlockPos.containing(above)))continue;var hit=level().clip(new ClipContext(above,above.add(0,-5,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,e));if(hit.getType()==HitResult.Type.MISS)continue;Vec3 at=hit.getLocation().add(0,.02,0);AABB box=e.getBoundingBox().move(at.subtract(e.position()));if(!level().noCollision(e,box))continue;e.stopRiding();e.getPersistentData().remove(Dc01Docking.SLOT);e.getPersistentData().remove(Dc01Docking.PROFILE);e.setPos(at);e.setDeltaMovement(Vec3.ZERO);e.setOnGround(false);syncDocks();entityData.set(READY,0);lastFailure=0;return true;}fail(p,6);return false;}
 public void action(ServerPlayer p,int action){if(action==40){CarrierFleet.open(p,0);return;}if(action>=30&&action<33){PlatformInventory.open(this,action-30,p);return;}if(PlatformInventory.guard(this,p))return;if(!permitted(p)||p.level()!=level()||p.distanceToSqr(this)>64&&!VatagSessions.controlling(p,this)&&!CarrierFleet.access(p,this))return;if(level().getGameTime()==lastAction)return;lastAction=level().getGameTime();int s=action%3;if(action<0||action>8)return;if(action<3)loadNearest(p,s);else if(action<6)launch(p,s);else unload(p,s);}
 public void openDocks(ServerPlayer p){if(!permitted(p))return;p.openMenu(new SimpleMenuProvider((id,inv,who)->new Dc01Menu(id,inv,this),Component.translatable("entity.sbwdroneconfig.drone_carrier_dc01")),b->b.writeInt(getId()));}
 @Override public InteractionResult interact(Player p,InteractionHand h){var held=p.getItemInHand(h);if(dockItem(held)){if(p instanceof ServerPlayer sp)loadItem(sp,held,-1);return InteractionResult.sidedSuccess(level().isClientSide);}boolean crowbar=held.is(com.atsuishio.superbwarfare.init.ModTags.Items.TOOLS_CROWBAR);if(crowbar&&p.isShiftKeyDown()){if(p instanceof ServerPlayer sp)return packCarrier(sp)?InteractionResult.CONSUME:InteractionResult.FAIL;return InteractionResult.SUCCESS;}if(crowbar&&p.isShiftKeyDown()&&!getPassengers().isEmpty()){if(!level().isClientSide)fail(p,10);return InteractionResult.FAIL;}if((held.isEmpty()&&!p.isShiftKeyDown())||(SbwCompat.isMonitor(held)&&p.isShiftKeyDown())){if(p instanceof ServerPlayer sp)openDocks(sp);return InteractionResult.sidedSuccess(level().isClientSide);}return super.interact(p,h);}
 private boolean packCarrier(ServerPlayer p){if(PlatformInventory.guard(this,p))return false;if(!permitted(p)||!isAlive()||isWreck())return false;if(!getPassengers().isEmpty()){fail(p,10);return false;}if(!stopped()){fail(p,2);return false;}if(operator()!=null)VatagSessions.stop(operator());var item=com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(this);if(item.isEmpty()||!p.getInventory().add(item)&&p.drop(item,false)==null)return false;discard();return true;}
 private void releaseAll(){PlatformInventory.closeFor(this);for(Entity e:List.copyOf(getPassengers())){e.stopRiding();e.getPersistentData().remove(Dc01Docking.SLOT);e.getPersistentData().remove(Dc01Docking.PROFILE);e.setPos(e.position().add(0,.6,0));e.setDeltaMovement(0,.08,0);e.setOnGround(false);Dc01Docking.departure(e,this);}syncDocks();entityData.set(READY,0);}
 @Override public void destroy(){if(!level().isClientSide)releaseAll();super.destroy();}
 @Override public void remove(RemovalReason why){if(!level().isClientSide)PlatformInventory.closeFor(this);if(!level().isClientSide&&why.shouldDestroy())releaseAll();super.remove(why);}
 @Override public boolean canCollideWith(Entity other){return !Dc01Docking.sameCarrier(this,other)&&super.canCollideWith(other);}
 @Override public net.minecraft.world.phys.AABB getBoundingBoxForCulling(){var m=getVehicleTransform(1);double lx=Double.POSITIVE_INFINITY,ly=lx,lz=lx,hx=Double.NEGATIVE_INFINITY,hy=hx,hz=hx;for(int i=0;i<8;i++){double x=(i&1)==0?-1.4745:1.4745,y=(i&2)==0?0:2.819977065625,z=(i&4)==0?-2.025:2.059;double wx=m.m00()*x+m.m10()*y+m.m20()*z+m.m30(),wy=m.m01()*x+m.m11()*y+m.m21()*z+m.m31(),wz=m.m02()*x+m.m12()*y+m.m22()*z+m.m32();lx=Math.min(lx,wx);ly=Math.min(ly,wy);lz=Math.min(lz,wz);hx=Math.max(hx,wx);hy=Math.max(hy,wy);hz=Math.max(hz,wz);}return new AABB(lx,ly,lz,hx,hy,hz).inflate(.15);}
}
