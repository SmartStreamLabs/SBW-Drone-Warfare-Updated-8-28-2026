package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

/** Reservation metadata exists ONLY for triggered mines, never for idle mines. World persistence is vanilla scheduled ticks. */
public final class Lm01Budget {
 private static final Map<MinecraftServer,Lm01Budget> SERVERS=new IdentityHashMap<>();
 private final Map<GlobalPos,Long> positions=new HashMap<>();
 private final Map<Long,Integer> reservations=new HashMap<>();
 private long tick=Long.MIN_VALUE,tail;private int used;private long spent;
 private static Lm01Budget of(ServerLevel l){return SERVERS.computeIfAbsent(l.getServer(),s->new Lm01Budget());}
 private void cancel(GlobalPos key){var when=positions.remove(key);if(when!=null)reservations.computeIfPresent(when,(k,n)->n<=1?null:n-1);}
 public static void cancel(ServerLevel l,BlockPos p){var b=SERVERS.get(l.getServer());if(b!=null)b.cancel(GlobalPos.of(l.dimension(),p));}
 public static int reserve(ServerLevel l,BlockPos p){
  var b=of(l);var key=GlobalPos.of(l.dimension(),p.immutable());long now=l.getServer().getTickCount();var old=b.positions.get(key);
  if(old!=null)return(int)Math.max(1,Math.min(Integer.MAX_VALUE,old-now));
  if(b.positions.isEmpty())b.tail=now+1;
  b.tail=Math.max(now+1,b.tail);int limit=Lm01Config.EXPLOSIONS.get();
  while(b.reservations.getOrDefault(b.tail,0)>=limit)b.tail++;
  b.positions.put(key,b.tail);b.reservations.merge(b.tail,1,Integer::sum);
  return(int)Math.min(Integer.MAX_VALUE,b.tail-now);
 }
 public static boolean claim(ServerLevel l,BlockPos p){
  var b=of(l);b.cancel(GlobalPos.of(l.dimension(),p));long now=l.getServer().getTickCount();
  if(b.tick!=now){b.tick=now;b.used=0;b.spent=0;}
  if(b.used>=Lm01Config.EXPLOSIONS.get()||b.used>0&&b.spent>=Lm01Config.MICROS.get()*1000L)return false;
  b.used++;return true;
 }
 public static void elapsed(ServerLevel l,long started){of(l).spent+=System.nanoTime()-started;}
 public static void stop(MinecraftServer s){SERVERS.remove(s);}
 private Lm01Budget(){}
}
