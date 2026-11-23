package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.core.*;import net.minecraft.nbt.CompoundTag;import net.minecraft.network.chat.Component;import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;import net.minecraft.server.level.*;import net.minecraft.world.entity.player.Player;import net.minecraft.world.item.ItemStack;import net.minecraft.world.level.*;import net.minecraft.world.level.block.entity.BlockEntity;import net.minecraft.world.level.block.state.BlockState;import net.minecraft.world.phys.*;import software.bernie.geckolib.animatable.GeoBlockEntity;import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;import software.bernie.geckolib.core.animation.AnimatableManager;import software.bernie.geckolib.util.GeckoLibUtil;import java.util.*;
/** The platform stores only the actual entity UUID, never a second drone inventory. */
public class HxPlatformBlockEntity extends BlockEntity implements GeoBlockEntity {
 public enum Phase {EMPTY,LOADED,READY,LAUNCHING,BLOCKED}
 private final AnimatableInstanceCache cache=GeckoLibUtil.createInstanceCache(this);
 private UUID drone,owner;private UUID manualPilot;
 public void manualLaunch(Player p){if(!canUse(p)||drone()==null)return;var s=p.getMainHandItem();if(!SbwCompat.isLinkedMonitorForDrone(s,drone()))return;if(launch(p))manualPilot=p.getUUID();}private Phase phase=Phase.EMPTY;private long phaseStart;private int missingTicks;private boolean recovering;
 public HxPlatformBlockEntity(BlockPos p,BlockState s){super(AddonBlockEntities.HX_PLATFORM.get(),p,s);}
 public UUID droneId(){return drone;}public Phase phase(){return phase;}public Direction facing(){return getBlockState().getValue(HxPlatformBlock.FACING);}
 public boolean canUse(Player p){return p!=null&&p.level()==level&&p.distanceToSqr(Vec3.atCenterOf(worldPosition))<=64&&(owner==null||owner.equals(p.getUUID())||p.hasPermissions(2));}
 public void setOwnerUuid(UUID id){owner=id;setChanged();}
 public double age(float partial){return level==null?0:Math.max(0,level.getGameTime()-phaseStart+partial);}
 public double carriage(float partial){return phase==Phase.LAUNCHING?Hx2Pose.carriage(age(partial)):0;}
 public float latches(float partial){return phase==Phase.LAUNCHING?Hx2Pose.latches(age(partial)):0;}
 public Hx2Entity drone(){if(!(level instanceof ServerLevel s)||drone==null)return null;var e=s.getEntity(drone);return e instanceof Hx2Entity h?h:null;}
 private void message(Player p,String key){p.displayClientMessage(Component.translatable("hx2."+key),true);}
 private ItemStack monitor(Player p){if(SbwCompat.isMonitor(p.getMainHandItem()))return p.getMainHandItem();if(SbwCompat.isMonitor(p.getOffhandItem()))return p.getOffhandItem();return p.getInventory().items.stream().filter(SbwCompat::isMonitor).findFirst().orElse(ItemStack.EMPTY);}
 public boolean loadDrone(Player p,ItemStack held){if(PlatformInventory.guard(this,p))return false;
  if(Boolean.getBoolean("hx2.trace"))System.out.println("HX2_LOAD pos="+worldPosition+" permitted="+canUse(p)+" drone="+drone+" phase="+phase+" held="+held);
  if(!canUse(p)){message(p,"owner");return false;}if(drone!=null||phase==Phase.LAUNCHING){message(p,"busy");return false;}if(!held.is(AddonItems.HX2.get())||!(level instanceof ServerLevel s))return false;
  if(!clear(false)){if(Boolean.getBoolean("hx2.trace"))System.out.println("HX2_LOAD_CLEARANCE_FAILED pos="+worldPosition);phase=Phase.BLOCKED;sync();message(p,"blocked");return false;}
  var h=AddonEntities.HX2.get().create(s);if(h==null)return false;
  var saved=held.getTagElement("EntityTag");if(saved!=null){var data=saved.copy();for(String k:new String[]{"UUID","Pos","Motion","Rotation","Passengers","Leash","HxMount","HxFlight"})data.remove(k);var defaults=new CompoundTag();h.saveWithoutId(defaults);defaults.merge(data);if(defaults.contains("Item",10)&&defaults.getCompound("Item").isEmpty())defaults.remove("Item");h.load(defaults);}
  if(!h.permitted(p)){message(p,"owner");return false;}
  if(held.hasCustomHoverName())h.setCustomName(held.getHoverName());
  // Commit identity before native entity tracking. A failed add rolls back; no item is consumed.
  drone=h.getUUID();h.dock(worldPosition);Vec3 at=Hx2Pose.point(Hx2Pose.dock(worldPosition,facing(),0),Vec3.ZERO);h.moveTo(at.x,at.y,at.z,facing().toYRot(),-35);h.xRotO=-35;h.yRotO=h.getYRot();SbwCompat.ensureDroneOwner(h,p);
  if(!s.addFreshEntity(h)){if(Boolean.getBoolean("hx2.trace"))System.out.println("HX2_SPAWN_REJECTED uuid="+h.getUUID());drone=null;return false;}if(Boolean.getBoolean("hx2.trace"))System.out.println("HX2_SPAWN_ADDED uuid="+h.getUUID()+" accessible="+(s.getEntity(h.getUUID())!=null));owner=p.getUUID();if(!p.getAbilities().instabuild)held.shrink(1);if(!monitor(p).isEmpty())h.bindMonitor(p,monitor(p),false);phase=Phase.LOADED;missingTicks=0;sync();return true;
 }
 public boolean unload(Player p){if(PlatformInventory.guard(this,p))return false;if(!canUse(p)){message(p,"owner");return false;}if(phase==Phase.LAUNCHING)return false;Hx2Entity h=drone();if(h==null)return false;
  ItemStack packed=h.packed();if(!PlatformInventory.canReceive(p,packed)||!p.getInventory().add(packed)){message(p,"inventory_full");return false;}
  SbwCompat.disconnectControllersForDrone((ServerLevel)level,h);ChunkTicketManager.releaseDrone(h);h.discard();drone=null;phase=Phase.EMPTY;sync();return true;
 }
 public boolean launch(Player p){if(PlatformInventory.guard(this,p))return false;
  if(!canUse(p)){message(p,"owner");return false;}var h=drone();if(h==null||phase==Phase.LAUNCHING){message(p,"busy");return false;}if(h.energy()<=0){message(p,"energy");return false;}if(monitor(p).isEmpty()){message(p,"monitor");return false;}
  if(!clear(true)){phase=Phase.BLOCKED;sync();message(p,"blocked");return false;}
  manualPilot=null;h.bindMonitor(p,monitor(p),SbwCompat.isUsingLinkedMonitorForDrone(p,h));phase=Phase.LAUNCHING;phaseStart=level.getGameTime();sync();return true;
 }
 public boolean clear(boolean corridor){if(level==null)return false;
  var m=Hx2Pose.dock(worldPosition,facing(),0);Vec3 p=Hx2Pose.point(m,Vec3.ZERO),end=corridor?p.add(Hx2Pose.direction(m).scale(4)):p;
  AABB box=Hx2Geometry.dockedBounds(worldPosition,facing()).expandTowards(end.subtract(p)).inflate(.08);
  if(!level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ)))return false;
  for(BlockPos cell:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){var state=level.getBlockState(cell);if(state.getBlock() instanceof HxPlatformBlock&&HxPlatformBlock.root(cell,state).equals(worldPosition))continue;var shape=state.getCollisionShape(level,cell);if(LaunchClearance.intersects(shape,cell,box))return false;}
  return level.getEntities((net.minecraft.world.entity.Entity)null,box,e->e.isAlive()&&!e.isSpectator()&&e.isPickable()&&(drone==null||!drone.equals(e.getUUID()))).isEmpty();
 }
 public static void serverTick(Level l,BlockPos p,BlockState s,HxPlatformBlockEntity b){b.tickServer();}
 private void tickServer(){
  var h=drone();if(recovering){recovering=false;if(phase==Phase.LAUNCHING){phase=drone==null?Phase.EMPTY:Phase.LOADED;phaseStart=level.getGameTime();sync();}}
  if(drone!=null&&h==null){if(++missingTicks>120&&phase!=Phase.BLOCKED){phase=Phase.BLOCKED;sync();}return;}missingTicks=0;
  if(h!=null&&h.mount()==null){drone=null;if(phase!=Phase.LAUNCHING)phase=Phase.EMPTY;sync();}
  if(phase==Phase.LAUNCHING){double age=age(0);
   if(age<20&&h!=null&&!clear(true)){phase=Phase.BLOCKED;sync();return;}
   if(age>=20&&h!=null&&h.mount()!=null){h.release(Hx2Pose.dock(worldPosition,facing(),-.5));if(manualPilot!=null&&level.getPlayerByUUID(manualPilot) instanceof ServerPlayer pilot&&SbwCompat.isLinkedMonitorForDrone(pilot.getMainHandItem(),h))PlatformMonitor.activate(pilot,h);manualPilot=null;drone=null;sync();}
   if(age>=40){phase=Phase.EMPTY;sync();}return;
  }
  if(level.getGameTime()%10==0){Phase next=h==null?Phase.EMPTY:h.energy()>0&&clear(true)?Phase.READY:Phase.BLOCKED;if(phase!=next){phase=next;sync();}}
 }
 public void consumed(UUID id){if(id!=null&&id.equals(drone)){drone=null;phase=Phase.EMPTY;sync();}}
 public void dropContents(){PlatformInventory.closeFor(this);var h=drone();if(h!=null){var item=h.packed();SbwCompat.disconnectControllersForDrone((ServerLevel)level,h);ChunkTicketManager.releaseDrone(h);h.discard();net.minecraft.world.Containers.dropItemStack(level,worldPosition.getX()+.5,worldPosition.getY()+1,worldPosition.getZ()+.5,item);}drone=null;setChanged();}
 @Override public void setRemoved(){PlatformInventory.closeFor(this);super.setRemoved();}
 private void sync(){setChanged();if(level!=null&&!level.isClientSide)level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);}
 private void write(CompoundTag t){if(drone!=null)t.putUUID("Drone",drone);if(owner!=null)t.putUUID("Owner",owner);t.putInt("Phase",phase.ordinal());t.putLong("PhaseStart",phaseStart);}
 @Override protected void saveAdditional(CompoundTag t){super.saveAdditional(t);write(t);}
 @Override public void load(CompoundTag t){super.load(t);drone=t.hasUUID("Drone")?t.getUUID("Drone"):null;owner=t.hasUUID("Owner")?t.getUUID("Owner"):null;phase=Phase.values()[Math.max(0,Math.min(4,t.getInt("Phase")))];phaseStart=t.getLong("PhaseStart");recovering=true;}
 @Override public void handleUpdateTag(CompoundTag t){load(t);recovering=false;}
 @Override public void onDataPacket(net.minecraft.network.Connection c,ClientboundBlockEntityDataPacket p){handleUpdateTag(p.getTag());}
 @Override public CompoundTag getUpdateTag(){var t=new CompoundTag();write(t);return t;}
 @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){return ClientboundBlockEntityDataPacket.create(this);}
 @Override public AABB getRenderBoundingBox(){return new AABB(worldPosition).inflate(3,2,3);}
 @Override public void registerControllers(AnimatableManager.ControllerRegistrar c){}@Override public AnimatableInstanceCache getAnimatableInstanceCache(){return cache;}
}
