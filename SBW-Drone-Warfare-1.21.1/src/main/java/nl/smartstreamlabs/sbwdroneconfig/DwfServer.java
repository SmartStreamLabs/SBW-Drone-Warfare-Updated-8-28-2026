package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.event.tick.ServerTickEvent;import net.neoforged.neoforge.event.server.ServerStoppingEvent;import net.neoforged.bus.api.*;
/** The sole server work dispatcher. Block tickers must not run their own work loop. */
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID)
public final class DwfServer{
 private static final Map<MinecraftServer,Context> SERVERS=new IdentityHashMap<>();
 public static final class Context{
  public final MinecraftServer server;
  public final DwfWorkBudget work=new DwfWorkBudget();public final DwfVariantPool variants=new DwfVariantPool();
  public final java.nio.file.Path directory;public final DwfIo<DwfItemKey>disk=new DwfIo<>(DwfItemKey::encoded,new DwfArchive.Limits(Long.MAX_VALUE,1048576,2097152,65536));
  public final java.util.concurrent.ExecutorService calculations=java.util.concurrent.Executors.newFixedThreadPool(2,r->{var t=new Thread(r,"DWF recipe snapshots");t.setDaemon(true);return t;});
  final Set<DwfMachine>machines=Collections.newSetFromMap(new IdentityHashMap<>());private DwfRecipes.Catalog recipes;private final Map<Integer,List<DwfRecipes.Entry>>catalogOrders=new HashMap<>();
  Context(MinecraftServer server){this.server=server;directory=server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("data/dwf01");}
  private void reload(){recipes=DwfRecipes.build(server);catalogOrders.clear();for(var machine:List.copyOf(machines))if(machine.ready())machine.catalogChanged();}
  public DwfRecipes.Catalog catalog(){if(recipes==null)recipes=DwfRecipes.build(server);return recipes;}
  private void close(){if(net.neoforged.fml.ModList.get().isLoaded("refinedstorage"))DwfRs2.stop(server);for(var machine:List.copyOf(machines))machine.close();work.clear();calculations.shutdownNow();try{disk.close();}catch(java.io.IOException failure){SbwDroneRangeConfig.LOGGER.error("DWF checkpoint failure at server stop",failure);}variants.clear();}
  public List<DwfRecipes.Entry>catalogOrder(int sort){return catalogOrders.computeIfAbsent(sort,k->{var rows=new ArrayList<>(catalog().entries());var names=Comparator.<DwfRecipes.Entry,String>comparing(e->e.output().getHoverName().getString(),String.CASE_INSENSITIVE_ORDER).thenComparing(e->e.id().toString());rows.sort(k==2?Comparator.<DwfRecipes.Entry,String>comparing(DwfRecipes.Entry::category).thenComparing(names):k==1?Comparator.<DwfRecipes.Entry>comparingInt(e->e.output().getCount()).reversed().thenComparing(names):names);return List.copyOf(rows);});}
  private final Map<DwfWorkBudget.Kind,Integer> limits=new EnumMap<>(DwfWorkBudget.Kind.class);
  private long started;private double lastMs;
  private DwfWorkBudget.Measurement measurement=new DwfWorkBudget.Measurement(0,0,0);
  public DwfWorkBudget.Measurement measurement(){return measurement;}
  private void tick(){
   limits.put(DwfWorkBudget.Kind.RECIPE,DwfConfig.RECIPE_CHECKS.get());limits.put(DwfWorkBudget.Kind.TRANSPORT,DwfConfig.TRANSPORT_UPDATES.get());limits.put(DwfWorkBudget.Kind.COMPLETION,DwfConfig.COMPLETIONS.get());
   measurement=work.tick(DwfConfig.WORK_UNITS.get(),DwfConfig.WORK_MICROS.get()*1000L,limits,lastMs>50);
   lastMs=(System.nanoTime()-started)/1e6;
  }
 }
 public static Context existing(MinecraftServer server){return SERVERS.get(server);}
 public static Context context(MinecraftServer server){return SERVERS.computeIfAbsent(server,k->new Context(k));}
 @SubscribeEvent(priority=EventPriority.LOWEST)public static void tick(ServerTickEvent.Post e){
  var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();var c=SERVERS.get(server);if(c==null)return;
  c.tick();
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)public static void begin(ServerTickEvent.Pre e){var c=SERVERS.get(e.getServer());if(c!=null)c.started=System.nanoTime();}
 @SubscribeEvent public static void stop(ServerStoppingEvent e){var c=SERVERS.remove(e.getServer());if(c!=null)c.close();}
 @SubscribeEvent public static void save(net.neoforged.neoforge.event.level.LevelEvent.Save e){if(e.getLevel()instanceof net.minecraft.server.level.ServerLevel level){var c=SERVERS.get(level.getServer());if(c!=null)for(var machine:List.copyOf(c.machines))if(machine.block.getLevel()==level)machine.checkpoint();}}
 @SubscribeEvent public static void reload(net.neoforged.neoforge.event.OnDatapackSyncEvent e){if(e.getPlayer()==null){var c=SERVERS.get(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());if(c!=null)c.reload();}}
 private DwfServer(){}
}
