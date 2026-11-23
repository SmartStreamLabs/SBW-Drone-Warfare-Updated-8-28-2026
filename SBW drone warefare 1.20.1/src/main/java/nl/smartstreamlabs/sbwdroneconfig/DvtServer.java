package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.core.*;import net.minecraft.server.level.*;import net.minecraft.world.entity.Entity;import net.minecraft.world.level.ChunkPos;import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
/** Supplemental, bounded viewers. Never activates a monitor, changes a controller or moves a player. */
public final class DvtServer {
 private static int roundRobin;
 private static final String ACCESS="sbwdroneconfigDvtViewers";
 private static final Map<UUID,LinkedHashMap<BlockPos,Session>> VIEWERS=new HashMap<>();private static final Map<UUID,long[]> REQUESTS=new HashMap<>();
 static final class Session{final BlockPos pos;long heartbeat;UUID drone;Set<Long> sent=new HashSet<>(),area=Set.of();Session(BlockPos p,long t){pos=p;heartbeat=t;}}
 public static boolean own(ServerPlayer p,Entity e){var o=SbwCompat.resolveDroneOwner(p.serverLevel(),e);return o!=null&&p.getUUID().equals(o.uuid());}
 public static boolean allowed(ServerPlayer p,Entity e){if(e==null||!SbwCompat.isDrone(e)||!e.isAlive()||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck())return false;if(own(p,e))return true;var a=e.getPersistentData().getList(ACCESS,8);for(int i=0;i<a.size();i++)if(p.getUUID().toString().equals(a.getString(i)))return true;return false;}
 private static Dvt01BlockEntity terminal(ServerPlayer p,BlockPos pos){return p.isAlive()&&p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)<=48*48&&p.serverLevel().hasChunkAt(pos)&&p.serverLevel().getBlockEntity(pos)instanceof Dvt01BlockEntity b?b:null;}
 public static void open(ServerPlayer p,BlockPos pos){if(p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64)return;var b=terminal(p,pos);if(b!=null)send(p,b,true,true,"");}
 public static void request(ServerPlayer p,DvtRequest m){
  long t=p.serverLevel().getGameTime();var rate=REQUESTS.computeIfAbsent(p.getUUID(),k->new long[]{t,0});if(t-rate[0]>=10){rate[0]=t;rate[1]=0;}if(++rate[1]>8)return;
  if(m.action()==6){var map=VIEWERS.get(p.getUUID());if(map!=null)map.remove(m.pos());refresh(p);return;}
  var b=terminal(p,m.pos());if(b==null)return;
  if(m.action()==0){var map=VIEWERS.computeIfAbsent(p.getUUID(),k->new LinkedHashMap<>());if(!map.containsKey(m.pos())&&map.size()>=2)return;map.computeIfAbsent(m.pos().immutable(),k->new Session(k,t)).heartbeat=t;return;}
  boolean edit=(b.owner==null||b.owner.equals(p.getUUID())||p.hasPermissions(2))&&p.distanceToSqr(b.getBlockPos().getX()+.5,b.getBlockPos().getY()+.5,b.getBlockPos().getZ()+.5)<=64&&p.serverLevel().mayInteract(p,b.getBlockPos())&&Pb01Dvt.authorized(b,p);String notice=m.action()==1?"Drone list refreshed.":"";
  if(m.action()==2&&edit){if(m.drone()==null)b.drone=null;else{var e=p.serverLevel().getEntity(m.drone());if(!allowed(p,e))return;b.drone=e.getUUID();}b.changed();}
  if(m.action()==3&&edit)b.toggle();
  if(m.action()==4||m.action()==5){var e=m.drone()==null?null:p.serverLevel().getEntity(m.drone());if(e==null||!own(p,e))return;var other=p.server.getPlayerList().getPlayerByName(m.player());if(other==null){notice="Player must be online.";}else{var a=e.getPersistentData().getList(ACCESS,8);String id=other.getUUID().toString();a.removeIf(v->v.getAsString().equals(id));if(m.action()==4&&a.size()<32)a.add(net.minecraft.nbt.StringTag.valueOf(id));e.getPersistentData().put(ACCESS,a);notice=m.action()==4?"Viewing access granted.":"Viewing access revoked.";}}
  send(p,b,false,true,notice);
 }
 private static String status(ServerPlayer p,Dvt01BlockEntity b,Entity e){if(b.drone==null)return "NO DRONE SELECTED";if(e==null||!e.isAlive()||e instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity v&&v.isWreck())return "DRONE OFFLINE";if(!allowed(p,e))return "ACCESS DENIED";if(!b.open)return "CLOSED";if(e instanceof Sd01Entity sd)return sd.getEnergy()<=0?"NO POWER":sd.link(p)?"LIVE":"SIGNAL LOST";if(FiberOpticLinkSystem.isFiberOpticMode(e)&&!FiberOpticLinkSystem.isFiberOpticControlActive(e))return "SIGNAL LOST";var data=DroneOsdServer.collect(p,e,true,1);boolean fiber=FiberOpticLinkSystem.isFiberSessionActive(e);double range=fiber?FiberOpticLinkSystem.getCurrentCableLength(e):data.range();if(fiber&&!FiberOpticLinkSystem.isFiberOpticControlActive(e)||p.distanceTo(e)>range||DroneJamStateManager.isHardJammed(e)&&!FiberOpticLinkSystem.isImmuneToNormalJammers(e))return "SIGNAL LOST";return "LIVE";}
 private static void send(ServerPlayer p,Dvt01BlockEntity b,boolean show,boolean list,String notice){
  Entity e=b.drone==null?null:p.serverLevel().getEntity(b.drone);String status=status(p,b,e);boolean access=allowed(p,e);var data=access?DroneOsdServer.collect(p,e,true,1):null;List<DvtReply.Entry> choices=new ArrayList<>();
  if(list)for(Entity d:p.serverLevel().getAllEntities())if(choices.size()<64&&allowed(p,d))choices.add(new DvtReply.Entry(d.getUUID(),d.getDisplayName().getString(),d.isAlive()?"ONLINE":"OFFLINE",own(p,d)));
  AddonNetwork.dvtReply(p,new DvtReply(b.getBlockPos(),show,b.drone,b.open,status,access?e.getDisplayName().getString():"",access?e.getId():-1,data==null?-1:data.energy(),data==null?0:data.capacity(),access?p.distanceTo(e):0,access&&(!(e instanceof Sd01Entity)||Sd01Config.JAMMERS.get())?DroneJamStateManager.getJamProgress(e):0,access?e.chunkPosition().x:0,access?e.chunkPosition().z:0,choices,notice));
 }
 public static boolean watches(ServerPlayer p,ChunkPos pos){var map=VIEWERS.get(p.getUUID());if(map==null)return false;for(var s:map.values())if(s.area.contains(pos.toLong())&&s.sent.contains(pos.toLong()))return true;return false;}
 public static net.minecraft.world.phys.Vec3 tracking(ServerPlayer p,Entity e,net.minecraft.world.phys.Vec3 normal){if(e!=null&&watches(p,e.chunkPosition()))return e.position();return normal;}
 private static void refresh(ServerPlayer p){((DvtTrackMap)p.serverLevel().getChunkSource().chunkMap).dvt$refresh(p);}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent event){if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;
  var players=new ArrayList<>(VIEWERS.keySet());if(!players.isEmpty())Collections.rotate(players,-(roundRobin++%players.size()));int packets=8;for(UUID id:players){ServerPlayer p=server.getPlayerList().getPlayer(id);var map=VIEWERS.get(id);if(p==null){VIEWERS.remove(id);continue;}long t=p.serverLevel().getGameTime();boolean refresh=false;int budget=Math.min(2,packets);
   var it=map.values().iterator();while(it.hasNext()){var s=it.next();var b=terminal(p,s.pos);if(b==null||t-s.heartbeat>40){it.remove();refresh=true;continue;}var e=b.drone==null?null:p.serverLevel().getEntity(b.drone);boolean live="LIVE".equals(status(p,b,e));Set<Long> area=live?DroneChunkGeometry.square(e.chunkPosition().x,e.chunkPosition().z,2):Set.of();
    if(!Objects.equals(s.drone,b.drone)||!area.equals(s.area)){s.drone=b.drone;s.area=area;s.sent.retainAll(area);refresh=true;if(live)send(p,b,false,false,"");}
    if(live){UUID lease=UUID.nameUUIDFromBytes(("dvt:"+id+":"+b.drone).getBytes(java.nio.charset.StandardCharsets.UTF_8));ChunkTicketManager.touchAuxiliary(p.serverLevel(),lease,area);for(long n:area){if(budget<=0)break;if(s.sent.contains(n))continue;var cp=new ChunkPos(n);var chunk=p.serverLevel().getChunkSource().getChunkNow(cp.x,cp.z);if(chunk==null)continue;if(s.sent.isEmpty())send(p,b,false,false,"");p.connection.send(new ClientboundLevelChunkWithLightPacket(chunk,p.serverLevel().getLightEngine(),null,null));s.sent.add(n);budget--;packets--;refresh=true;}}
    if(t%5==0)send(p,b,false,false,"");
   }if(refresh||t%20==0)refresh(p);if(map.isEmpty())VIEWERS.remove(id);
  }
 }
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppingEvent e){VIEWERS.clear();REQUESTS.clear();}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){VIEWERS.remove(e.getEntity().getUUID());REQUESTS.remove(e.getEntity().getUUID());}
 private DvtServer(){}
}
