package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import net.minecraft.client.Minecraft;
import com.google.gson.*;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

/** Server snapshots are memory-only. They never overwrite a remote player's config files. */
@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=Dist.CLIENT)
public final class ConfigRemoteClient {
 public record State(int request,long revision,boolean writable,String status,Map<String,String> values){public State{values=Map.copyOf(values);}}
 private record Incoming(int request,long revision,ConfigWire.Assembly assembly){}
 private static final Map<String,State> STATES=new HashMap<>();
 private static final Map<String,Incoming> INCOMING=new HashMap<>();
 private static Object connection;private static int next=1;private static long sequence;private static String notice="";
 private ConfigRemoteClient(){}
 public static long sequence(){return sequence;}
 public static String notice(){return notice;}
 public static State state(String file){return STATES.get(file);}
 public static boolean connected(){return Minecraft.getInstance().getConnection()!=null;}
 public static int refresh(){checkConnection();if(!connected())return 0;int request=next++;AddonNetwork.configRequest(new ConfigRequest(request,"",0,0,1,""));return request;}
 public static int apply(String file,Map<String,String> edits,Map<String,String> originals){
  var state=STATES.get(file);if(state==null||!state.writable())throw new SecurityException("Server settings are read-only");
  var object=new JsonObject();var gson=new Gson();object.add("edits",gson.toJsonTree(edits));object.add("originals",gson.toJsonTree(originals));var chunks=ConfigWire.chunks(object.toString());int request=next++;
  for(int i=0;i<chunks.size();i++)AddonNetwork.configRequest(new ConfigRequest(request,file,state.revision(),i,chunks.size(),chunks.get(i)));
  return request;
 }
 @SuppressWarnings({"rawtypes","unchecked"})public static void receive(ConfigReply m){
  checkConnection();if(!connected())return;
  try{
   var previous=STATES.get(m.file());if(previous!=null&&previous.revision()>m.revision())return;
   var incoming=INCOMING.get(m.file());
   if(incoming==null||incoming.revision()!=m.revision()||incoming.request()!=m.request()){
    if(incoming!=null&&incoming.revision()>m.revision())return;
    incoming=new Incoming(m.request(),m.revision(),new ConfigWire.Assembly(m.parts(),0));INCOMING.put(m.file(),incoming);
   }
   incoming.assembly().add(m.part(),m.parts(),m.json());if(!incoming.assembly().complete())return;INCOMING.remove(m.file());
   var values=ConfigWire.strings(JsonParser.parseString(incoming.assembly().finish()).getAsJsonObject());
   var validated=new LinkedHashMap<ConfigCatalog.Definition,Object>();Object owner=null;
   for(var e:values.entrySet()){
    var d=ConfigCatalog.find(e.getKey());if(d==null||d.managed()||d.scope()!=ModConfig.Type.SERVER||!d.file().equals(m.file()))throw new IllegalArgumentException("Unknown synchronized server setting");
    if(owner!=null&&owner!=d.owner())throw new IllegalArgumentException("Mixed server configuration files");owner=d.owner();validated.put(d,d.parse(e.getValue()));
   }
   // An integrated host already shares the authoritative spec with its server thread.
   boolean changed=validated.entrySet().stream().anyMatch(e->!Objects.equals(e.getKey().current(),e.getValue()));
   if(Minecraft.getInstance().getSingleplayerServer()==null&&changed){
    var first=validated.keySet().iterator().next();var config=ConfigLifecycle.config(first.owner());if(config==null||!first.loaded())throw new IllegalStateException("Server config was not synchronized by the loader");
    for(var e:validated.entrySet())((net.minecraftforge.common.ForgeConfigSpec.ConfigValue)e.getKey().value()).set(e.getValue());
    first.owner().afterReload();net.minecraftforge.fml.ModList.get().getModContainerById(SbwDroneRangeConfig.MOD_ID).orElseThrow().dispatchConfigEvent(new net.minecraftforge.fml.event.config.ModConfigEvent.Reloading(config));
   }
   STATES.put(m.file(),new State(m.request(),m.revision(),m.writable(),m.status(),values));notice=m.status();sequence++;
  }catch(RuntimeException e){notice="error: "+Objects.toString(e.getMessage(),e.getClass().getSimpleName());sequence++;SbwDroneRangeConfig.LOGGER.warn("Rejected configuration snapshot",e);}
 }
 private static void checkConnection(){var current=Minecraft.getInstance().getConnection();if(current!=connection){connection=current;STATES.clear();INCOMING.clear();notice="";sequence++;}}
 @SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){if(e.phase==net.minecraftforge.event.TickEvent.Phase.END)checkConnection();}
}
