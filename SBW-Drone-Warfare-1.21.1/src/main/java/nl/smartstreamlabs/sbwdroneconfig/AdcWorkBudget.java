package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.lang.ref.WeakReference;import net.minecraft.server.MinecraftServer;
/** Shared fair queues. Deferred aim/scan work never accumulates catch-up executions. */
final class AdcWorkBudget {
 private record Node(UUID id,WeakReference<AdcBlockEntity> block){}
 private static final class Queue {final ArrayDeque<Node> pending=new ArrayDeque<>();final Set<UUID> ids=new HashSet<>();long tick=Long.MIN_VALUE,nanos;int calls;}
 private static final class State {final Queue scans=new Queue(),aim=new Queue();}
 private static final Map<MinecraftServer,State> STATES=new WeakHashMap<>();
 static boolean run(AdcBlockEntity block,boolean scan,Runnable work){var state=STATES.computeIfAbsent(block.defenseLevel().getServer(),s->new State());var q=scan?state.scans:state.aim;long now=block.defenseLevel().getServer().getTickCount();if(q.tick!=now){q.tick=now;q.calls=0;q.nanos=0;}
  while(!q.pending.isEmpty()){var head=q.pending.peek();var b=head.block.get();if(b!=null&&!b.isRemoved()&&b.enabled&&b.automatic&&(!scan||b.scanPending))break;q.pending.remove();q.ids.remove(head.id);}
  if(!q.ids.contains(block.id)){if(q.pending.size()>=4096)return false;q.ids.add(block.id);q.pending.add(new Node(block.id,new WeakReference<>(block)));}
  if(q.pending.peek().block.get()!=block||q.calls>=(scan?4:64)||q.nanos>=(scan?1_500_000:2_000_000))return false;
  q.ids.remove(q.pending.remove().id);q.calls++;long start=System.nanoTime();try{work.run();return true;}finally{q.nanos+=System.nanoTime()-start;}
 }
 private AdcWorkBudget(){}
}
