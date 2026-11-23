package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import java.util.*;
import com.google.gson.*;

/** Server-owned edits, permission rechecks, file revisions, and a shared eight-packet/tick send budget. */
@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class ConfigServer {
 private record Upload(int request,String file,long revision,ConfigWire.Assembly assembly){}
 private record Snapshot(long revision,List<String> chunks){}
 private static final class Pending {final int request;final String file,status;final Snapshot snapshot;int next;Pending(int request,String file,String status,Snapshot snapshot){this.request=request;this.file=file;this.status=status;this.snapshot=snapshot;}}
 private static final Map<UUID,Upload> UPLOADS=new HashMap<>();
 private static final Map<UUID,Map<String,Long>> REQUEST_TIMES=new HashMap<>();
 private static final Map<UUID,LinkedHashMap<String,Pending>> OUTBOUND=new LinkedHashMap<>();
 private static final Map<String,Snapshot> SNAPSHOTS=new HashMap<>();
 private static Map<String,List<ConfigCatalog.Definition>> files;private static long checked=-1;private static int turn;
 private ConfigServer(){}
 private static Map<String,List<ConfigCatalog.Definition>> files(){if(files==null){var map=new LinkedHashMap<String,List<ConfigCatalog.Definition>>();for(var d:ConfigCatalog.definitions())if(d.scope()==ModConfig.Type.SERVER)map.computeIfAbsent(d.file(),s->new ArrayList<>()).add(d);map.replaceAll((s,v)->List.copyOf(v));files=Collections.unmodifiableMap(map);}return files;}
 public static boolean permitted(ServerPlayer p){return !p.hasDisconnected()&&p.server.getPlayerList().getPlayer(p.getUUID())==p&&(p.hasPermissions(2)||!p.server.isDedicatedServer()&&p.server.isSingleplayerOwner(p.getGameProfile()));}
 public static void request(ServerPlayer player,ConfigRequest m){
  long now=player.server.overworld().getGameTime();UUID id=player.getUUID();
  try{
   if(m.file().isEmpty()){
    if(m.parts()!=1||m.part()!=0||!m.json().isEmpty())throw new IllegalArgumentException("Invalid snapshot request");
    var times=REQUEST_TIMES.computeIfAbsent(id,u->new HashMap<>());if(now-times.getOrDefault("",Long.MIN_VALUE/2)<10)return;times.put("",now);
    for(String file:files().keySet())queue(player,m.request(),file,"snapshot");return;
   }
   var defs=files().get(m.file());if(defs==null)throw new IllegalArgumentException("Unknown server configuration");
   if(!permitted(player))throw new SecurityException("Only server operators may change server settings");
   var upload=UPLOADS.get(id);
   if(upload==null||upload.request()!=m.request()||upload.assembly().expired(now)){
    if(m.part()!=0)throw new IllegalArgumentException("Missing initial transfer part");
    var times=REQUEST_TIMES.computeIfAbsent(id,u->new HashMap<>());if(now-times.getOrDefault(m.file(),Long.MIN_VALUE/2)<10)throw new IllegalArgumentException("Wait before submitting another configuration transaction");
    times.put(m.file(),now);upload=new Upload(m.request(),m.file(),m.revision(),new ConfigWire.Assembly(m.parts(),now));UPLOADS.put(id,upload);
   }
   if(!upload.file().equals(m.file())||upload.revision()!=m.revision())throw new IllegalArgumentException("Configuration transfer changed destination");
   upload.assembly().add(m.part(),m.parts(),m.json());if(!upload.assembly().complete())return;UPLOADS.remove(id);
   var root=JsonParser.parseString(upload.assembly().finish()).getAsJsonObject();if(!root.keySet().equals(Set.of("edits","originals")))throw new IllegalArgumentException("Invalid transaction fields");
   var edits=ConfigWire.strings(root.getAsJsonObject("edits"));var originals=ConfigWire.strings(root.getAsJsonObject("originals"));
   var owner=defs.get(0).owner();var authorize=(java.util.function.Predicate<ConfigCatalog.Definition>)(d->permitted(player)&&d.scope()==ModConfig.Type.SERVER&&d.owner()==owner);
   synchronized(ConfigLifecycle.COMMIT_LOCK){var plan=ConfigCommitPlan.prepare(m.revision(),ConfigLifecycle.revision(owner),edits,originals,authorize);plan.execute(authorize);}
   for(var other:player.server.getPlayerList().getPlayers())queue(other,0,m.file(),"updated");
   queue(player,m.request(),m.file(),"applied");
   SbwDroneRangeConfig.LOGGER.info("{} applied {} Drone Warfare settings in {}",player.getGameProfile().getName(),edits.size(),m.file());
  }catch(Exception e){UPLOADS.remove(id);String status=(e instanceof ConcurrentModificationException?"conflict: ":e instanceof SecurityException?"denied: ":"error: ")+Objects.toString(e.getMessage(),e.getClass().getSimpleName());status=status.substring(0,Math.min(status.length(),512));if(files().containsKey(m.file()))queue(player,m.request(),m.file(),status);else AddonNetwork.configReply(player,new ConfigReply(m.request(),"",0,false,0,1,status,"{}"));}
 }
 private static Snapshot snapshot(String file){var defs=files().get(file);long revision=ConfigLifecycle.revision(defs.get(0).owner());var previous=SNAPSHOTS.get(file);if(previous!=null&&previous.revision()==revision)return previous;var state=new Snapshot(revision,ConfigWire.chunks(ConfigWire.snapshot(defs)));SNAPSHOTS.put(file,state);return state;}
 private static void queue(ServerPlayer p,int request,String file,String status){OUTBOUND.computeIfAbsent(p.getUUID(),id->new LinkedHashMap<>()).put(file,new Pending(request,file,status,snapshot(file)));}
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ServerTickEvent e){if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;var server=net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();if(server==null)return;tick(server);}
 private static void tick(MinecraftServer server){
  long now=server.overworld().getGameTime();UPLOADS.values().removeIf(u->u.assembly().expired(now));
  if(checked!=ConfigLifecycle.revision()){
   checked=ConfigLifecycle.revision();for(var entry:files().entrySet()){
    var old=SNAPSHOTS.get(entry.getKey());long revision=ConfigLifecycle.revision(entry.getValue().get(0).owner());
    if(old==null)snapshot(entry.getKey());
    else if(old.revision()!=revision){SNAPSHOTS.remove(entry.getKey());for(var p:server.getPlayerList().getPlayers())queue(p,0,entry.getKey(),"updated");}
   }
  }
  var ids=new ArrayList<>(OUTBOUND.keySet());if(ids.isEmpty())return;
  for(int n=0;n<8;n++){
   if(turn>=ids.size())turn=0;UUID id=ids.get(turn++);var p=server.getPlayerList().getPlayer(id);var queue=OUTBOUND.get(id);
   if(p==null||queue==null||queue.isEmpty()){OUTBOUND.remove(id);continue;}
   var it=queue.entrySet().iterator();var pending=it.next().getValue();var s=pending.snapshot;int index=pending.next++;
   AddonNetwork.configReply(p,new ConfigReply(pending.request,pending.file,s.revision(),permitted(p),index,s.chunks().size(),pending.status,s.chunks().get(index)));
   if(pending.next==s.chunks().size())it.remove();if(queue.isEmpty())OUTBOUND.remove(id);
  }
 }
 @SubscribeEvent public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){var id=e.getEntity().getUUID();UPLOADS.remove(id);REQUEST_TIMES.remove(id);OUTBOUND.remove(id);}
 @SubscribeEvent public static void stop(net.minecraftforge.event.server.ServerStoppedEvent e){UPLOADS.clear();REQUEST_TIMES.clear();OUTBOUND.clear();SNAPSHOTS.clear();checked=-1;turn=0;}
}
