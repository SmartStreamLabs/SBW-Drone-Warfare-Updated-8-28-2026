package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.server.level.ServerPlayer;import net.minecraft.world.entity.Entity;import net.minecraft.world.item.ItemStack;import net.minecraft.core.BlockPos;import net.minecraft.network.chat.Component;import com.atsuishio.superbwarfare.entity.vehicle.DroneEntity;
/** Server-owned handoff from a specific stored load to its one native flying entity. */
public final class PlatformMonitor {
 private static final Map<ServerPlayer,Long> RATES=new WeakHashMap<>();
 public static boolean eligible(Entity e){return e instanceof Fl01Entity||e instanceof LucasDroneEntity||e instanceof Geranium2Entity||e instanceof GlideBombEntity||e instanceof Hx2Entity;}
 static net.minecraft.nbt.CompoundTag tag(ItemStack s){return s.getOrCreateTag().copy();}
 static void save(ItemStack s,net.minecraft.nbt.CompoundTag n){s.setTag(n);}
 public static void message(net.minecraft.world.entity.player.Player p,String text){p.displayClientMessage(Component.literal(text),true);}
 public static boolean bind(MissionPlatform pad,net.minecraft.world.entity.player.Player p,ItemStack s){
  if(!SbwCompat.isMonitor(s))return false;if(!pad.canUse(p)||pad.loadedId()==null){message(p,"Cannot link: platform is empty or access denied.");return true;}
  Ndc01Monitor.clear(s);var n=tag(s);for(String k:new String[]{"VatagUUID","Rd01Linked","Id01Linked",SbwCompat.TAG_LINKED_DRONE})n.remove(k);n.putBoolean(SbwCompat.TAG_LINKED,false);n.putBoolean(SbwCompat.TAG_USING,false);n.putUUID("PadLoad",pad.loadedId());n.putLong("PadPosition",pad.block().getBlockPos().asLong());n.putString("PadDimension",pad.block().getLevel().dimension().location().toString());save(s,n);message(p,"Loaded drone linked. Hold this monitor and press the Launch / Pilot key (default C).");return true;
 }
 public static boolean held(ServerPlayer p,UUID id){var s=p.getMainHandItem();return id!=null&&SbwCompat.isMonitor(s)&&tag(s).hasUUID("PadLoad")&&id.equals(tag(s).getUUID("PadLoad"));}
 public static boolean ready(ServerPlayer p,UUID id){return p!=null&&p.isAlive()&&!p.isSpectator()&&!InjuryHooks.downed(p)&&SbwCompat.hasEquippedFpvGoggles(p)&&held(p,id);}
 public static void request(ServerPlayer p){request(p,false);}
 public static void request(ServerPlayer p,boolean navalReturn){
  if(p==null||!p.isAlive()||p.isSpectator()||InjuryHooks.downed(p))return;long now=p.serverLevel().getGameTime();Long old=RATES.put(p,now);if(old!=null&&now-old<5)return;
  if(Ndc01Monitor.request(p,navalReturn))return;var s=p.getMainHandItem();if(!SbwCompat.isMonitor(s)||SbwCompat.isUsingLinkedMonitor(s))return;
  if(!SbwCompat.hasEquippedFpvGoggles(p)){message(p,"Equip your FPV goggles before launching or piloting.");return;}
  var n=tag(s);if(n.hasUUID("PadLoad")&&n.contains(SbwCompat.TAG_LINKED_DRONE)&&!n.getString(SbwCompat.TAG_LINKED_DRONE).equals(n.getUUID("PadLoad").toString())){n.remove("PadLoad");save(s,n);}
  UUID id=n.hasUUID("PadLoad")?n.getUUID("PadLoad"):null;
  if(SbwCompat.isLinkedMonitor(s)){var linked=SbwCompat.findLinkedDrone(p,s);if(eligible(linked)){if(linked instanceof Hx2Entity h&&h.mount()!=null){if(p.serverLevel().hasChunkAt(h.mount())&&p.serverLevel().getBlockEntity(h.mount()) instanceof HxPlatformBlockEntity pad)pad.manualLaunch(p);return;}activate(p,linked);return;}message(p,"Linked drone is unavailable.");return;}
  if(id==null||!n.getString("PadDimension").equals(p.serverLevel().dimension().location().toString())){message(p,"No valid platform drone link in this dimension.");return;}
  Entity flying=p.serverLevel().getEntity(id);if(flying!=null){activate(p,flying);return;}
  BlockPos pos=BlockPos.of(n.getLong("PadPosition"));if(!p.serverLevel().hasChunkAt(pos)){message(p,"Linked platform chunk is not loaded.");return;}
  if(p.serverLevel().getBlockEntity(pos) instanceof MissionPlatform pad&&id.equals(pad.loadedId())){if(!pad.canUse(p)){message(p,"Move closer to the linked launch platform, or check its access permissions. Link retained.");return;}pad.requestManualLaunch(p);return;}
  message(p,"Link expired: the drone was unloaded, replaced, or the platform removed. Link again.");
 }
 public static void activate(ServerPlayer p,Entity e){
  if(!SbwCompat.hasEquippedFpvGoggles(p)||InjuryHooks.downed(p)||!eligible(e)||!e.isAlive()||e.isRemoved()||e.level()!=p.level())return;
  UUID owner=SbwCompat.getStoredDroneOwnerUuid(e);if(owner!=null&&!owner.equals(p.getUUID())&&!p.hasPermissions(2)){message(p,"Access denied: another player owns this drone.");return;}
  for(var pilot:p.serverLevel().getServer().getPlayerList().getPlayers())if(pilot!=p&&SbwCompat.isUsingLinkedMonitorForDrone(pilot,e)){message(p,"Another pilot is controlling this drone.");return;}
  if(p.distanceToSqr(e)>(double)AddonConfig.droneMaxRange()*AddonConfig.droneMaxRange()||DroneJamStateManager.isHardJammed(e)){message(p,"Drone is outside control range or its signal is jammed.");return;}
  var s=p.getMainHandItem();if(!SbwCompat.isMonitor(s))return;
  if(MissionManager.owns(e)){if(!(e instanceof Fl01Entity)||!MissionManager.get(p.serverLevel().getServer()).cancel(e.getUUID(),p)){message(p,"Coordinate autopilot is active. Cancel that mission before taking manual control.");return;}}
  if(e instanceof Fl01Entity fl)fl.enableManual();Rd01Sessions.stop(p);Id01Sessions.stop(p);VatagSessions.stop(p);SbwCompat.ensureDroneOwner(e,p);e.getEntityData().set(DroneEntity.LINKED,true);e.getEntityData().set(DroneEntity.CONTROLLER,p.getStringUUID());SbwCompat.linkMonitorToDrone(s,e,true);var session=tag(s);session.putUUID("PadPilotSession",UUID.randomUUID());save(s,session);p.getInventory().setChanged();p.containerMenu.broadcastChanges();
 }
 private PlatformMonitor(){}
}
