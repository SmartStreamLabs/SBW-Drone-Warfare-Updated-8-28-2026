package nl.smartstreamlabs.sbwdroneconfig;

import java.util.*;

/** Sparse server-owned counts. K must be an immutable, complete item-variant identity.
 * One record per present variant; simulation is read-only. No storage-sized tick work. */
public final class DwfBulkStore<K> {
    public record Entry<K>(K key,long count) {}
    private final Thread owner=Thread.currentThread();
    private final LinkedHashMap<K,Long> counts=new LinkedHashMap<>();
    private final Map<K,Long> changed=new LinkedHashMap<>();
    private final Map<K,Integer> slots=new HashMap<>();
    private final ArrayList<K> slotKeys=new ArrayList<>();
    private final ArrayDeque<Integer> freeSlots=new ArrayDeque<>();
    private static final int PAGE=256;
    private List<List<Entry<K>>> pages=List.of();private Map<Integer,List<Entry<K>>>batchPages;
    private final long capacity;private final int variants;
    private long total,revision;
    public DwfBulkStore(long capacity,int variants) {
        if(capacity<1||variants<1)throw new IllegalArgumentException("Invalid storage limits");
        this.capacity=capacity;this.variants=variants;
    }
    private void checkThread(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Storage accessed outside owning server thread");}
    public long capacity(){return capacity;}public int variantLimit(){return variants;}
    public long total(){checkThread();return total;}public int size(){checkThread();return counts.size();}
    public long revision(){checkThread();return revision;}
    public long count(K key){checkThread();return counts.getOrDefault(key,0L);}
    /** Stable O(1) automation slots, allocated only for variants that have existed.
     * Removed slots are reused. One additional virtual slot accepts new variants. */
    public int allocatedSlots(){checkThread();return slotKeys.size();}
    public K keyAt(int slot){checkThread();return slot<0||slot>=slotKeys.size()?null:slotKeys.get(slot);}
    public K canonical(K key){checkThread();Integer slot=slots.get(key);return slot==null?null:slotKeys.get(slot);}
    public long insert(K key,long requested,boolean simulate){
        checkThread();Objects.requireNonNull(key);if(requested<0)throw new IllegalArgumentException("Negative amount");
        long old=counts.getOrDefault(key,0L);
        if(old==0&&counts.size()>=variants)return 0;
        long accepted=Math.min(requested,Math.max(0,capacity-total));
        if(accepted>0&&!simulate)change(key,old+accepted);
        return accepted;
    }
    public long extract(K key,long requested,boolean simulate){
        checkThread();Objects.requireNonNull(key);if(requested<0)throw new IllegalArgumentException("Negative amount");
        long old=counts.getOrDefault(key,0L),accepted=Math.min(old,requested);
        if(accepted>0&&!simulate)change(key,old-accepted);
        return accepted;
    }
    /** Restore validated checkpoint data without discarding stock after an admin
     * lowers a capacity. Existing excess stays extractable; insertion remains blocked. */
    public void restore(K key,long count){
        checkThread();Objects.requireNonNull(key);
        if(count<1||count>Long.MAX_VALUE-total||counts.containsKey(key))throw new IllegalArgumentException("Duplicate or invalid checkpoint record");
        change(key,count);
    }
    /** Only the private loading path uses batching. Preflight first; publish
     * each touched copy-on-write page once, rather than once per restored item. */
    public void restoreBatch(List<Entry<K>> entries){
        checkThread();if(entries.isEmpty())return;long sum=total;var seen=new HashSet<K>();for(var e:entries){Objects.requireNonNull(e.key());if(e.count()<1||e.count()>Long.MAX_VALUE-sum||counts.containsKey(e.key())||!seen.add(e.key()))throw new IllegalArgumentException("Invalid checkpoint batch");sum+=e.count();}
        if(batchPages!=null)throw new IllegalStateException("Nested loading batch");batchPages=new LinkedHashMap<>();
        try{for(var e:entries)change(e.key(),e.count());var outer=new ArrayList<>(pages);for(var e:batchPages.entrySet()){while(outer.size()<=e.getKey())outer.add(Collections.nCopies(PAGE,null));outer.set(e.getKey(),Collections.unmodifiableList(e.getValue()));}pages=List.copyOf(outer);}finally{batchPages=null;}
    }
    private void change(K key,long next){
        long old=counts.getOrDefault(key,0L);total+=next-old;
        if(next==0){counts.remove(key);int slot=slots.remove(key);slotKeys.set(slot,null);freeSlots.addLast(slot);}
        else {counts.put(key,next);if(old==0){int slot;if(freeSlots.isEmpty()){slot=slotKeys.size();slotKeys.add(key);}else{slot=freeSlots.removeFirst();slotKeys.set(slot,key);}slots.put(key,slot);}}
        int slot=next==0?freeSlots.getLast():slots.get(key),page=slot/PAGE,offset=slot%PAGE;
        if(batchPages!=null){var inner=batchPages.computeIfAbsent(page,n->new ArrayList<>(n<pages.size()?pages.get(n):Collections.nCopies(PAGE,null)));inner.set(offset,next==0?null:new Entry<>(key,next));changed.put(key,next);revision++;return;}
        var outer=new ArrayList<>(pages);
        while(outer.size()<=page)outer.add(Collections.nCopies(PAGE,null));
        var inner=new ArrayList<>(outer.get(page));inner.set(offset,next==0?null:new Entry<>(key,next));
        outer.set(page,Collections.unmodifiableList(inner));pages=List.copyOf(outer);
        changed.put(key,next);revision++;
    }
    /** Bounded deltas for indices/listeners; do not send this whole map to GUIs. */
    public boolean hasChanges(){return !changed.isEmpty();}
    public List<Entry<K>> takeChanges(int limit){
        checkThread();if(limit<1)throw new IllegalArgumentException();
        var out=new ArrayList<Entry<K>>(Math.min(limit,changed.size()));var it=changed.entrySet().iterator();
        while(it.hasNext()&&out.size()<limit){var e=it.next();out.add(new Entry<>(e.getKey(),e.getValue()));it.remove();}
        return List.copyOf(out);
    }
    /** O(1), coherent point-in-time snapshot. Copy-on-write pages bound a mutation
     * to 256 records plus at most 256 page references at the default variant cap.
     * Frozen records may be consumed off-thread; K must remain immutable. */
    public Cursor<K> snapshot(){checkThread();return new Cursor<>(this);}
    public static final class Cursor<K>{
        private final DwfBulkStore<K> store;private final long revision;private final List<List<Entry<K>>> pages;
        private int position;
        private boolean done;
        private Cursor(DwfBulkStore<K> s){store=s;revision=s.revision;pages=s.pages;}
        public boolean valid(){store.checkThread();return store.revision==revision;}
        public boolean done(){return done;}
        public List<Entry<K>> next(int limit){
            if(limit<1)throw new IllegalArgumentException();var list=new ArrayList<Entry<K>>(limit);
            int examined=0;while(examined++<limit&&position<pages.size()*PAGE){var e=pages.get(position/PAGE).get(position%PAGE);position++;if(e!=null)list.add(e);}
            done=position>=pages.size()*PAGE;return List.copyOf(list);
        }
    }
    /** Validate everything before committing. Caller supplies an already prepared
     * exact-variant plan, never an untrusted client recipe or output. */
    public static <K> boolean produce(DwfBulkStore<K> input,DwfBulkStore<K> output,Map<K,Long> ingredients,K result,long resultCount){
        input.checkThread();output.checkThread();Objects.requireNonNull(result);
        if(input==output||resultCount<=0)throw new IllegalArgumentException("Separate stores and positive output required");
        for(var e:ingredients.entrySet()){
            Objects.requireNonNull(e.getKey());if(e.getValue()==null||e.getValue()<=0)throw new IllegalArgumentException("Invalid ingredient amount");
            if(input.count(e.getKey())<e.getValue())return false;
        }
        if(output.insert(result,resultCount,true)!=resultCount)return false;
        // All operations are now bounded, allocation-independent arithmetic on the owning thread.
        for(var e:ingredients.entrySet())input.extract(e.getKey(),e.getValue(),false);
        output.insert(result,resultCount,false);return true;
    }
}
