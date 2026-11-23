package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import net.minecraft.server.MinecraftServer;import net.minecraft.world.item.ItemStack;import net.minecraft.core.*;import com.refinedmods.refinedstorage.api.resource.*;import com.refinedmods.refinedstorage.api.core.Action;import com.refinedmods.refinedstorage.api.storage.*;import com.refinedmods.refinedstorage.api.storage.external.*;import com.refinedmods.refinedstorage.api.storage.composite.*;import com.refinedmods.refinedstorage.common.support.resource.ItemResource;import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;import com.refinedmods.refinedstorage.common.api.support.network.*;
/** Optional RS 2.0.9 bridge. Quantities stay long; Minecraft stacks are never
 * expanded to represent the bulk amount. All callbacks run on the server thread. */
public final class DwfRs2{
 // Keep RS-typed lambda methods off the unconditional event subscriber.
 public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent e){e.registerBlockEntity(com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi.INSTANCE.getNetworkNodeContainerProviderCapability(),AddonBlockEntities.DWF01.get(),(b,side)->b instanceof DwfPortBlockEntity port?DwfRs2.capability(port):null);}

 public interface Cache{long dwfCount(ResourceKey key);void dwfAdd(ResourceKey key,long amount);void dwfRemove(ResourceKey key,long amount);boolean dwfAttached();void dwfReset();}
 public interface Composite{List<ExternalStorageProvider>dwfProviders();}
 private record Chest(UUID machine,int side){}
 private static final class State{final Map<Chest,Provider>leases=new HashMap<>();}
 private static final Map<MinecraftServer,State>STATES=new IdentityHashMap<>();
 public static Provider find(ExternalStorageProvider provider){if(provider instanceof Provider p)return p;if(provider instanceof Composite c)for(var child:c.dwfProviders()){var p=find(child);if(p!=null)return p;}return null;}
 public static ExternalStorageProvider external(net.minecraft.server.level.ServerLevel level,BlockPos pos){return level.getBlockEntity(pos)instanceof DwfPortBlockEntity port?new Provider(port):null;}
 public static void stop(MinecraftServer server){var state=STATES.remove(server);if(state!=null)for(var p:List.copyOf(state.leases.values()))p.release();}
 public static final class Provider implements ExternalStorageProvider,DwfWorkBudget.Job{
  private final DwfPortBlockEntity port;private final int side;private DwfMachine machine;private DwfStorage store;private DwfServer.Context server;private State state;private Chest identity;private Cache sink;private boolean bound,lastExtract;private int detached;
  private DwfBulkStore.Cursor<DwfItemKey>seed;private final Map<DwfItemKey,Long>pending=new LinkedHashMap<>();
  private final java.util.function.Consumer<List<DwfBulkStore.Entry<DwfItemKey>>>listener=changes->{if(!lastExtract)return;for(var e:changes)pending.put(e.key(),e.count());if(server!=null)server.work.add(this);};
  Provider(DwfPortBlockEntity port){this.port=port;side=DwfGeometry.chest(port.getBlockState().getValue(DwfBlock.PART));}
  public void attach(Cache sink){if(bound&&this.sink!=sink&&!this.sink.dwfAttached())release();if(bound){if(this.sink==sink)server.work.add(this);return;}if(port.isRemoved())return;var rootPosition=DwfGeometry.root(port.getBlockPos(),port.facing(),port.getBlockState().getValue(DwfBlock.PART));if(!port.getLevel().hasChunkAt(rootPosition))return;var root=port.root();if(root==null||root.machine()==null||!root.machine().ready())return;server=DwfServer.existing(port.getLevel().getServer());if(server==null)return;state=STATES.computeIfAbsent(server.server,k->new State());identity=new Chest(root.id(),side);var other=state.leases.putIfAbsent(identity,this);if(other!=null&&other!=this)return;this.sink=sink;machine=root.machine();store=machine.storage(side);store.addListener(listener);lastExtract=extractAllowed();seed=lastExtract?store.counts.snapshot():null;bound=true;detached=0;server.work.add(this);}
  private boolean ready(){return bound&&!port.isRemoved()&&machine.ready();}
  private boolean insertAllowed(){return side==0?DwfConfig.INPUT_INSERT.get():DwfConfig.OUTPUT_INSERT.get();}private boolean extractAllowed(){return side==0?DwfConfig.INPUT_EXTRACT.get():DwfConfig.OUTPUT_EXTRACT.get();}
  public Iterator<ResourceAmount>iterator(){return Collections.emptyIterator();}
  public long insert(ResourceKey resource,long amount,Action action,Actor actor){if(!ready()||!insertAllowed()||amount<=0||!(resource instanceof ItemResource item))return 0;return store.insert(item.toItemStack(),amount,action==Action.SIMULATE);}
  public long extract(ResourceKey resource,long amount,Action action,Actor actor){if(!ready()||!extractAllowed()||amount<=0||!(resource instanceof ItemResource item))return 0;var key=store.counts.canonical(DwfItemKey.probe(item.toItemStack()));if(key==null)return 0;long removed=store.counts.extract(key,amount,action==Action.SIMULATE);if(removed>0&&action==Action.EXECUTE)store.flush();return removed;}
  public DwfWorkBudget.Kind kind(){return DwfWorkBudget.Kind.TRANSPORT;}
  public boolean available(){return !ready()||!sink.dwfAttached()||seed!=null||!pending.isEmpty()||lastExtract!=extractAllowed();}
  public boolean step(){if(!ready()){release();return false;}if(!sink.dwfAttached()){if(++detached>40){release();return false;}return true;}detached=0;if(lastExtract!=extractAllowed()){lastExtract=extractAllowed();seed=store.counts.snapshot();}if(seed!=null){for(var e:seed.next(16))pending.putIfAbsent(e.key(),e.count());if(seed.done())seed=null;}int count=0;var it=pending.entrySet().iterator();while(count++<16&&it.hasNext()){var e=it.next();var resource=ItemResource.ofItemStack(e.getKey().stack(1));long actual=extractAllowed()?store.counts.count(e.getKey()):0,old=sink.dwfCount(resource);if(actual>old)sink.dwfAdd(resource,actual-old);else if(actual<old)sink.dwfRemove(resource,old-actual);it.remove();}return true;}
  public void release(){if(bound){store.removeListener(listener);state.leases.remove(identity,this);server.work.remove(this);bound=false;}if(sink!=null&&!sink.dwfAttached())sink.dwfReset();sink=null;seed=null;pending.clear();machine=null;store=null;}
 }
 private static final com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository NO_TRACKING=new com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository(){public void update(ResourceKey key,Actor actor,long time){}public Optional<com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource>findTrackedResourceByActorType(ResourceKey key,Class<? extends Actor>type){return Optional.empty();}};
 /** Use RS's native configured proxy. A plain nested CompositeStorage does not
  * forward incremental child deltas to the network's root cache. */
 private static final class PortNode extends com.refinedmods.refinedstorage.api.network.impl.node.externalstorage.ExternalStorageNetworkNode{
  final Provider provider;
  PortNode(DwfPortBlockEntity port){super(com.refinedmods.refinedstorage.common.Platform.INSTANCE.getConfig().getExternalStorage().getEnergyUsage(),System::currentTimeMillis);provider=new Provider(port);setTrackingRepository(NO_TRACKING);initialize(provider);}
  @Override public void doWork(){super.doWork();if(isActive())detectChanges();}
 }
 /** RS2 native block entities drive node activeness/work themselves. Our two
  * port cells use the existing SERVER-wide transport scheduler instead of
  * adding independent block tickers. One port gets at most one visit per tick. */
 private static final class Port implements DwfWorkBudget.Job{
  final NetworkNodeContainerProvider provider;final PortNode node;final DwfPortBlockEntity block;long lastTick=Long.MIN_VALUE;boolean registered,removed;
  Port(NetworkNodeContainerProvider provider,PortNode node,DwfPortBlockEntity block){this.provider=provider;this.node=node;this.block=block;}
  NetworkNodeContainerProvider provider(){return provider;}PortNode node(){return node;}
  public DwfWorkBudget.Kind kind(){return DwfWorkBudget.Kind.TRANSPORT;}
  public boolean available(){return block.isRemoved()||block.getLevel()==null||lastTick!=block.getLevel().getGameTime();}
  public boolean step(){
   var level=block.getLevel();if(removed||level==null||block.isRemoved()){if(node.isActive())node.setActive(false);node.provider.release();return false;}
   lastTick=level.getGameTime();var network=node.getNetwork();boolean active=network!=null&&rootReady(block)&&directFaceOpen(block)&&level.hasChunkAt(block.getBlockPos())&&(!RefinedStorageApi.INSTANCE.isEnergyRequired()||network.getComponent(com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent.class).getStored()>=node.getEnergyUsage());
   if(active!=node.isActive())node.setActive(active);
   if(active)node.doWork();return true;
  }
 }
 private static boolean rootReady(DwfPortBlockEntity block){var level=block.getLevel();var rootPosition=DwfGeometry.root(block.getBlockPos(),block.facing(),block.getBlockState().getValue(DwfBlock.PART));if(!level.hasChunkAt(rootPosition))return false;var root=block.root();return root!=null&&!root.isRemoved()&&root.machine()!=null&&root.machine().ready();}
 private static boolean directFaceOpen(DwfPortBlockEntity b){var outward=DwfGeometry.chest(b.getBlockState().getValue(DwfBlock.PART))==0?b.facing().getClockWise():b.facing().getCounterClockWise();var adjacent=b.getBlockPos().relative(outward);if(b.isRemoved()||!b.getLevel().hasChunkAt(adjacent))return false;var state=b.getLevel().getBlockState(adjacent);var type=state.getBlock();return !((type instanceof com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageBlock||type instanceof com.refinedmods.refinedstorage.common.importer.ImporterBlock||type instanceof com.refinedmods.refinedstorage.common.exporter.ExporterBlock)&&com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection(state)==outward.getOpposite());}
 public static Object port(DwfPortBlockEntity b){if(b.rsAttachment instanceof Port p)return p;var node=new PortNode(b);var api=RefinedStorageApi.INSTANCE;var container=api.createNetworkNodeContainer(b,node).name("DWF-01 storage port").connectionStrategy(new ConnectionStrategy(){private Direction outward(){return DwfGeometry.chest(b.getBlockState().getValue(DwfBlock.PART))==0?b.facing().getClockWise():b.facing().getCounterClockWise();}private boolean storageFront(net.minecraft.world.level.block.state.BlockState adjacent,Direction toward){var block=adjacent.getBlock();return(block instanceof com.refinedmods.refinedstorage.common.storage.externalstorage.ExternalStorageBlock||block instanceof com.refinedmods.refinedstorage.common.importer.ImporterBlock||block instanceof com.refinedmods.refinedstorage.common.exporter.ExporterBlock)&&com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection(adjacent)==toward.getOpposite();}public void addOutgoingConnections(ConnectionSink sink){if(b.isRemoved())return;var side=outward();sink.tryConnectInSameDimension(b.getBlockPos().relative(side),side.getOpposite());}public boolean canAcceptIncomingConnection(Direction side,net.minecraft.world.level.block.state.BlockState adjacent){return !b.isRemoved()&&side==outward()&&!storageFront(adjacent,side);}}).keyProvider(()->GlobalPos.of(b.getLevel().dimension(),b.getBlockPos())).build();var provider=api.createNetworkNodeContainerProvider();provider.addContainer(container);var p=new Port(provider,node,b);b.rsAttachment=p;return p;}
 public static NetworkNodeContainerProvider capability(DwfPortBlockEntity b){if(b.isRemoved()||b.rsAttachment instanceof Port existing&&existing.removed)return null;return((Port)port(b)).provider();}
 public static void loaded(DwfPortBlockEntity b){if(!b.getLevel().isClientSide){var p=(Port)port(b);if(!p.registered&&!p.removed){p.provider().initialize(b.getLevel(),()->{});p.registered=true;}if(!p.removed)DwfServer.context(b.getLevel().getServer()).work.add(p);}}
 public static void removed(DwfPortBlockEntity b){if(b.rsAttachment instanceof Port p){p.removed=true;var server=DwfServer.existing(b.getLevel().getServer());if(server!=null)server.work.remove(p);if(p.node().isActive())p.node().setActive(false);if(p.registered){p.registered=false;p.provider().remove(b.getLevel());}p.node().provider.release();b.rsAttachment=null;}}
 private DwfRs2(){}
}
