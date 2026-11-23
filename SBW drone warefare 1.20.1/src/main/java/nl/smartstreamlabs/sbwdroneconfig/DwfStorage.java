package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.io.*;import net.minecraft.world.item.*;

/** Server-only bridge: count mutations, indices and change generations are bounded
 * per delta. No inventory-wide scan is necessary after an ordinary transfer. */
public final class DwfStorage{
 private DwfWorkBudget work;
 public void useBudget(DwfWorkBudget work){if(this.work!=null&&this.work!=work)throw new IllegalStateException("Storage already attached");this.work=Objects.requireNonNull(work);}
 private final DwfWorkBudget.Job changesJob=new DwfWorkBudget.Job(){public DwfWorkBudget.Kind kind(){return DwfWorkBudget.Kind.TRANSPORT;}public boolean available(){return counts.hasChanges()&&!loading;}public boolean step(){flushNow();return counts.hasChanges();}};
 public final DwfBulkStore<DwfItemKey> counts;
 public DwfIndex<DwfItemKey> index=new DwfIndex<>(DwfItemKey::item);
 private final DwfVariantPool pool;private boolean loading;private java.util.concurrent.CompletableFuture<Built>loadedIndex;
 private record Built(DwfIndex<DwfItemKey>index,Map<Item,Long>totals){}
 public void beginLoad(){loading=true;}private final Set<java.util.function.Consumer<List<DwfBulkStore.Entry<DwfItemKey>>>>listeners=Collections.newSetFromMap(new IdentityHashMap<>());
 public void addListener(java.util.function.Consumer<List<DwfBulkStore.Entry<DwfItemKey>>> listener){listeners.add(listener);}
 public void removeListener(java.util.function.Consumer<List<DwfBulkStore.Entry<DwfItemKey>>> listener){listeners.remove(listener);}
 private Map<Item,Long> itemTotals=new IdentityHashMap<>();private final Map<Item,java.math.BigInteger>overflowTotals=new IdentityHashMap<>();
 public long available(net.minecraft.world.item.crafting.Ingredient ingredient){if(!ingredient.isSimple())return -1;var seen=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());long total=0;for(var stack:ingredient.getItems())if(seen.add(stack.getItem())){long count=itemTotals.getOrDefault(stack.getItem(),0L);if(count>Long.MAX_VALUE-total)return Long.MAX_VALUE;total+=count;}return total;}
 private final Map<Item,Long> itemRevisions=new IdentityHashMap<>();private final java.util.function.Consumer<Set<Item>> dirty;
 public DwfStorage(long capacity,int variants,Runnable dirty){this(capacity,variants,items->dirty.run());}
 public DwfStorage(long capacity,int variants,java.util.function.Consumer<Set<Item>>dirty){this(capacity,variants,dirty,new DwfVariantPool());}
 public DwfStorage(long capacity,int variants,java.util.function.Consumer<Set<Item>>dirty,DwfVariantPool pool){counts=new DwfBulkStore<>(capacity,variants);this.dirty=dirty;this.pool=pool;}
 public long itemRevision(Item item){return itemRevisions.getOrDefault(item,0L);}
 public long insert(ItemStack stack,long amount,boolean simulate){
  if(stack.isEmpty())return 0;if(amount<0)throw new IllegalArgumentException();var probe=DwfItemKey.probe(stack);var key=counts.canonical(probe);long accepted=counts.insert(key==null?probe:key,amount,true);
  if(accepted>0&&!simulate){counts.insert(key==null?pool.intern(stack):key,accepted,false);flush();}return accepted;
 }
 public ItemStack extractStack(DwfItemKey key,int amount,boolean simulate){
  if(amount<=0)return ItemStack.EMPTY;long accepted=counts.extract(key,Math.min(key.maxStackSize(),amount),simulate);if(accepted>0&&!simulate)flush();return accepted==0?ItemStack.EMPTY:key.stack((int)accepted);
 }
 public void flush(){if(work==null||loading)flushNow();else if(counts.hasChanges())work.add(changesJob);}
 private void flushNow(){
  var changes=counts.takeChanges(256);if(loading)return;var changed=Collections.newSetFromMap(new IdentityHashMap<Item,Boolean>());for(var change:changes){var key=change.key();long delta=change.count()-index.count(key);changeItemTotal(key.item(),delta);index.update(key,change.count(),key.label(),key.category());itemRevisions.merge(key.item(),1L,Long::sum);changed.add(key.item());}if(!changes.isEmpty()){dirty.accept(Set.copyOf(changed));for(var listener:List.copyOf(listeners))listener.accept(changes);}
 }
 /** A bounded delta batch can temporarily contain an insert before the
  * corresponding removal. Keep that transient index sum exact without changing
  * physical 64-bit stock or wrapping material-availability counters. */
 private void changeItemTotal(Item item,long delta){var overflow=overflowTotals.get(item);if(overflow==null){long old=itemTotals.getOrDefault(item,0L);try{long next=Math.addExact(old,delta);if(next<0)throw new IllegalStateException("Negative material index");if(next==0)itemTotals.remove(item);else itemTotals.put(item,next);return;}catch(ArithmeticException tooLarge){overflow=java.math.BigInteger.valueOf(old);}}var next=overflow.add(java.math.BigInteger.valueOf(delta));if(next.signum()<0)throw new IllegalStateException("Negative material index");if(next.compareTo(java.math.BigInteger.valueOf(Long.MAX_VALUE))>0){overflowTotals.put(item,next);itemTotals.put(item,Long.MAX_VALUE);}else{overflowTotals.remove(item);long count=next.longValueExact();if(count==0)itemTotals.remove(item);else itemTotals.put(item,count);}}
 public void restore(byte[] encoded,long count)throws IOException{counts.restore(pool.decode(encoded),count);}
 public void restoreBatch(List<DwfIo.Record>records)throws IOException{var entries=new ArrayList<DwfBulkStore.Entry<DwfItemKey>>(records.size());for(var r:records)entries.add(new DwfBulkStore.Entry<>(pool.decode(r.variant()),r.amount()));counts.restoreBatch(entries);}
 /** Build detached immutable-tree data from frozen metadata only. No worker
  * accesses Minecraft world data, ItemStacks, capabilities or live counts. */
 public void prepareIndex(java.util.concurrent.Executor executor){
  if(loadedIndex!=null)return;var cursor=counts.snapshot();loadedIndex=new java.util.concurrent.CompletableFuture<>();var future=loadedIndex;
  class Build implements Runnable{final DwfIndex<DwfItemKey>index=new DwfIndex<>(DwfItemKey::item);final Map<Item,Long>totals=new IdentityHashMap<>();public void run(){if(future.isCancelled())return;try{for(var e:cursor.next(256)){var key=e.key();index.update(key,e.count(),key.label(),key.category());totals.merge(key.item(),e.count(),Math::addExact);}if(cursor.done())future.complete(new Built(index,totals));else executor.execute(this);}catch(Throwable failure){future.completeExceptionally(failure);}}}executor.execute(new Build());
 }
 public boolean indexAvailable(){return loadedIndex==null||loadedIndex.isDone();}
 public boolean adoptIndex(){if(loadedIndex==null||!loadedIndex.isDone())return false;var built=loadedIndex.join();index=built.index();itemTotals=built.totals();loading=false;return true;}
 public void cancelLoad(){if(loadedIndex!=null)loadedIndex.cancel(false);if(work!=null)work.remove(changesJob);listeners.clear();}
 public DwfItemHandler handler(java.util.function.BooleanSupplier insert,java.util.function.BooleanSupplier extract,java.util.function.BooleanSupplier ready){return new DwfItemHandler(counts,insert,extract,ready,this::flush,pool);}
}
