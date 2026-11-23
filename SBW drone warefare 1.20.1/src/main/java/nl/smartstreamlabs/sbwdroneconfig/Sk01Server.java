package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.*;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;

/** Server-thread authority. One shared fair queue, no entities and no per-remnant tick. */
public final class Sk01Server {
 public static final String DEATH_TAG="Sk01CommittedDeath";
 private static final Map<MinecraftServer,Service>SERVICES=new IdentityHashMap<>();
 public static long lastWorkNanos,lastUnits,lastPackets;
 private abstract static class Job {boolean queued;abstract boolean step();}
 public static final class Service {
  final MinecraftServer server;final Map<ServerLevel,Context>levels=new IdentityHashMap<>();final ArrayDeque<Job>work=new ArrayDeque<>();
  Service(MinecraftServer s){server=s;}
  void enqueue(Job j){if(!j.queued){j.queued=true;work.addLast(j);}}
  Context level(ServerLevel l){return levels.computeIfAbsent(l,k->new Context(this,l));}
 }
 public static final class Context {
  final Service service;public final ServerLevel level;public final Sk01Data data;boolean closed;
  final Map<Long,Journal>journals=new HashMap<>();final Map<UUID,Placement>placements=new HashMap<>();
  final NavigableMap<Long,LinkedHashSet<UUID>>expiry=new TreeMap<>();final Map<UUID,Long>lastClear=new HashMap<>();
  Context(Service s,ServerLevel l){service=s;level=l;data=Sk01Data.get(l);for(var e:data.entries.values()){expiry(e);if(e.pending())place(e);}}
  void expiry(Sk01Entry e){expiry.computeIfAbsent(e.created(),k->new LinkedHashSet<>()).add(e.id());}
  void place(Sk01Entry e){var p=placements.computeIfAbsent(e.id(),k->new Placement(this,e));service.enqueue(p);}
  Journal journal(long key){return journals.computeIfAbsent(key,k->new Journal(this,key));}
  void change(long chunk,UUID id,Sk01Entry e){var j=journals.get(chunk);if(j!=null)j.add(new Sk01Packet.Change(id,e));}
 }
 private static final class Journal {
  final Context c;final long chunk;long revision;final NavigableMap<Long,Sk01Packet.Change>log=new TreeMap<>();final Map<ServerPlayer,Subscription>watchers=new IdentityHashMap<>();
  Journal(Context c,long chunk){this.c=c;this.chunk=chunk;}
  void add(Sk01Packet.Change change){revision++;if(watchers.isEmpty())return;log.put(revision,change);if(log.size()>131072){log.clear();for(var s:watchers.values())s.snapshot=true;}for(var s:watchers.values())c.service.enqueue(s);}
  void trim(){long min=revision;for(var s:watchers.values())min=Math.min(min,s.ack);log.headMap(min,true).clear();}
 }
 private static final class Subscription extends Job {
  final Journal j;final ServerPlayer player;boolean snapshot=true,first=true;long snapshotRevision=-1,ack;UUID cursor;
  Subscription(Journal j,ServerPlayer p){this.j=j;player=p;}
  boolean step(){if(j.c.closed||player.hasDisconnected()||player.serverLevel()!=j.c.level||j.watchers.get(player)!=this)return false;
   var changes=new ArrayList<Sk01Packet.Change>(64);boolean reset=false;
   if(snapshot){if(snapshotRevision!=j.revision){snapshotRevision=j.revision;cursor=null;first=true;}reset=first;first=false;var map=j.c.data.chunk(j.chunk);var it=(cursor==null?map:map.tailMap(cursor,false)).values().iterator();while(it.hasNext()&&changes.size()<64){var e=it.next();cursor=e.id();changes.add(new Sk01Packet.Change(e.id(),e));}if(!it.hasNext()){snapshot=false;ack=snapshotRevision;}}
   else {for(var e:j.log.tailMap(ack,false).entrySet()){changes.add(e.getValue());ack=e.getKey();if(changes.size()==64)break;}}
   if(reset||!changes.isEmpty()){AddonNetwork.sk01(player,new Sk01Packet(j.c.level.dimension().location(),j.chunk,snapshot?snapshotRevision:ack,reset,false,changes));lastPackets++;}
   j.trim();return snapshot||ack<j.revision;
  }
 }
 private static final class Placement extends Job {
  final Context c;final UUID id;final Sk01Placement search;
  Placement(Context c,Sk01Entry e){this.c=c;id=e.id();search=new Sk01Placement(e,c.level);}
  boolean step(){if(c.closed||!c.data.entries.containsKey(id)){c.placements.remove(id);return false;}if(!search.step(c.level)){var old=c.data.entries.get(id);var next=old.settled(search.result);c.data.put(next);if(old.chunk()!=next.chunk())c.change(old.chunk(),id,null);c.change(next.chunk(),id,next);c.placements.remove(id);return false;}return !search.waiting;}
 }
 private static Service service(MinecraftServer s){return SERVICES.computeIfAbsent(s,Service::new);}
 public static Context context(ServerLevel l){return service(l.getServer()).level(l);}
 /** Called only after a death has been confirmed, never for downed players. */
 public static Sk01Entry create(Entity source,ServerLevel physicalLevel,Vec3 at,float yaw){if(!(source instanceof net.minecraft.world.entity.LivingEntity)||source instanceof com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity)return null;if(!Sk01Config.ENABLED.get()||source.getPersistentData().hasUUID(DEATH_TAG))return null;
  var id=UUID.randomUUID();source.getPersistentData().putUUID(DEATH_TAG,id);var e=new Sk01Entry(id,source.getUUID(),at.x,at.y,at.z,yaw,Sk01Config.SCALE.get().floatValue(),Sk01Config.MODEL.get(),physicalLevel.getGameTime(),true);
  var c=context(physicalLevel);c.data.put(e);c.expiry(e);c.change(e.chunk(),id,e);c.place(e);return e;
 }
 public static void watch(ServerPlayer p,ServerLevel l,ChunkPos pos){var c=context(l);var j=c.journal(pos.toLong());var sub=new Subscription(j,p);j.watchers.put(p,sub);c.service.enqueue(sub);}
 public static void unwatch(ServerPlayer p,ServerLevel l,ChunkPos pos){var s=SERVICES.get(l.getServer());if(s==null)return;var c=s.levels.get(l);if(c==null)return;var j=c.journals.get(pos.toLong());if(j==null)return;j.watchers.remove(p);j.trim();if(j.watchers.isEmpty())c.journals.remove(pos.toLong());if(!p.hasDisconnected())AddonNetwork.sk01(p,new Sk01Packet(l.dimension().location(),pos.toLong(),Long.MAX_VALUE,false,true,List.of()));}
 public static void logout(ServerPlayer p){var s=SERVICES.get(p.getServer());if(s==null)return;for(var c:s.levels.values()){c.lastClear.remove(p.getUUID());var it=c.journals.values().iterator();while(it.hasNext()){var j=it.next();j.watchers.remove(p);j.trim();if(j.watchers.isEmpty())it.remove();}}}
 public static void loaded(ServerLevel l,ChunkPos pos){var s=SERVICES.get(l.getServer());if(s==null)return;var c=s.levels.get(l);if(c==null)return;for(var e:c.data.chunk(pos.toLong()).values())if(e.pending())c.place(e);}
 public static void unloaded(ServerLevel l){var s=SERVICES.get(l.getServer());if(s!=null){var c=s.levels.remove(l);if(c!=null)c.closed=true;}}
 public static void stop(MinecraftServer s){var service=SERVICES.remove(s);if(service!=null){service.work.clear();service.levels.clear();}lastWorkNanos=lastUnits=lastPackets=0;}
 public static boolean remove(Context c,UUID id){var e=c.data.remove(id);if(e==null)return false;var set=c.expiry.get(e.created());if(set!=null){set.remove(id);if(set.isEmpty())c.expiry.remove(e.created());}c.placements.remove(id);c.change(e.chunk(),id,null);return true;}
 public static void tick(MinecraftServer server){Sk01Deaths.flush(server);var s=SERVICES.get(server);if(s==null)return;long started=System.nanoTime();int budget=Sk01Config.WORK.get();long deadline=started+Sk01Config.MICROS.get()*1000L;lastUnits=lastPackets=0;
  if(Sk01Config.AUTO_REMOVE.get())for(var c:s.levels.values())while(!c.expiry.isEmpty()&&lastUnits<budget&&System.nanoTime()<deadline){var first=c.expiry.firstEntry();if(first.getKey()>c.level.getGameTime()-Sk01Config.AGE.get()*20L)break;var id=first.getValue().iterator().next();remove(c,id);lastUnits++;}
  while(!s.work.isEmpty()&&lastUnits<budget&&System.nanoTime()<deadline){var job=s.work.removeFirst();job.queued=false;if(job.step())s.enqueue(job);lastUnits++;}lastWorkNanos=System.nanoTime()-started;
 }
 /** Loaded chunks only. The same ray and box selection is recomputed by the server. */
 public static Sk01Entry target(ServerPlayer p){var c=context(p.serverLevel());var eye=p.getEyePosition();var end=eye.add(p.getLookAngle().scale(6));double nearest=eye.distanceToSqr(end);Sk01Entry selected=null;
  int minX=((int)Math.floor(Math.min(eye.x,end.x)-5))>>4,maxX=((int)Math.floor(Math.max(eye.x,end.x)+5))>>4,minZ=((int)Math.floor(Math.min(eye.z,end.z)-5))>>4,maxZ=((int)Math.floor(Math.max(eye.z,end.z)+5))>>4;
  for(int x=minX;x<=maxX;x++)for(int z=minZ;z<=maxZ;z++){if(p.serverLevel().getChunkSource().getChunkNow(x,z)==null)continue;for(var e:c.data.chunk(ChunkPos.asLong(x,z)).values()){if(e.pending()||p.serverLevel().getGameTime()-e.created()<20)continue;var hit=e.bounds().clip(eye,end);if(e.bounds().contains(eye))hit=Optional.of(eye);if(hit.isPresent()){double d=eye.distanceToSqr(hit.get());if(d<nearest){nearest=d;selected=e;}}}}
  if(selected==null)return null;var endHit=eye.add(p.getLookAngle().scale(Math.sqrt(nearest)));for(int x=((int)Math.floor(Math.min(eye.x,endHit.x)))>>4;x<=((int)Math.floor(Math.max(eye.x,endHit.x)))>>4;x++)for(int z=((int)Math.floor(Math.min(eye.z,endHit.z)))>>4;z<=((int)Math.floor(Math.max(eye.z,endHit.z)))>>4;z++)if(p.serverLevel().getChunkSource().getChunkNow(x,z)==null)return null;
  var block=Sk01Placement.view(p.serverLevel()).clip(new ClipContext(eye,endHit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));return block.getType()==HitResult.Type.MISS||block.getLocation().distanceToSqr(eye)+.001>=nearest?selected:null;
 }
 public static boolean clear(ServerPlayer p,UUID id){if(!Sk01Config.SHOVEL.get()||!p.isAlive()||p.isSpectator()||InjuryServer.cannotOperate(p)||!p.getAbilities().mayBuild||!p.getMainHandItem().is(AddonItems.RS01.get()))return false;var c=context(p.serverLevel());long now=p.serverLevel().getGameTime();if(now-c.lastClear.getOrDefault(p.getUUID(),Long.MIN_VALUE/2)<4)return false;c.lastClear.put(p.getUUID(),now);var target=target(p);if(target==null||!target.id().equals(id))return false;var support=BlockPos.containing(target.position().add(0,-.03,0));if(!p.mayUseItemAt(support,Direction.UP,p.getMainHandItem())||!RubbleServer.canClear(p,support)||!remove(c,id))return false;
  int wear=RubbleConfig.DURABILITY.get();if(wear>0&&!p.isCreative())p.getMainHandItem().hurtAndBreak(wear,p,a->a.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);p.displayClientMessage(Component.literal("SK-01 remains cleared."),true);return true;
 }
 public static void commands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>d){d.register(net.minecraft.commands.Commands.literal("remains").requires(s->s.hasPermission(2)).then(net.minecraft.commands.Commands.literal("clear").then(net.minecraft.commands.Commands.argument("radius",com.mojang.brigadier.arguments.IntegerArgumentType.integer(1,256)).executes(cmd->{var source=cmd.getSource();var c=context(source.getLevel());var at=source.getPosition();int radius=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(cmd,"radius");c.service.enqueue(new Job(){int x=((int)Math.floor(at.x-radius))>>4,z=((int)Math.floor(at.z-radius))>>4;final int startZ=z,maxX=((int)Math.floor(at.x+radius))>>4,maxZ=((int)Math.floor(at.z+radius))>>4;UUID cursor;int removed;
   boolean step(){if(c.closed)return false;var map=c.data.chunk(ChunkPos.asLong(x,z));var next=cursor==null?map.firstEntry():map.higherEntry(cursor);if(next!=null){cursor=next.getKey();if(next.getValue().position().distanceToSqr(at)<=radius*(double)radius&&remove(c,cursor))removed++;return true;}cursor=null;if(++z>maxZ){z=startZ;x++;}if(x<=maxX)return true;source.sendSuccess(()->Component.literal("Removed "+removed+" SK-01 remains."),true);return false;}});source.sendSuccess(()->Component.literal("SK-01 cleanup queued within "+radius+" blocks."),false);return 1;}))));}
 private Sk01Server(){}
}
