package nl.smartstreamlabs.sbwdroneconfig;
import java.io.*;import java.nio.file.*;import java.util.*;import java.util.concurrent.*;

/** Per-server disk service. Never sees levels, block entities or live inventories. */
public final class DwfIo<K> implements AutoCloseable {
    public record Record(int chest,byte[] variant,long amount){}
    public static final class Load{
        private final ArrayBlockingQueue<Record> records=new ArrayBlockingQueue<>(512);
        private final DwfIo<?>service;private Path path;private DwfArchive.Source source;private boolean scheduled;
        public volatile byte[] state;public volatile boolean complete,cancelled;public volatile Throwable failure;
        private Load(DwfIo<?>service){this.service=service;}
        public boolean hasRecords(){return !records.isEmpty();}
        public Record poll(){var r=records.poll();if(r!=null)service.released(r.variant().length);service.schedule(this);if(complete&&records.isEmpty())service.drained(this);return r;}
        public boolean drained(){boolean done=complete&&records.isEmpty();if(done)service.drained(this);return done;}
        public void cancel(){cancelled=true;Record r;while((r=records.poll())!=null)service.released(r.variant().length);service.schedule(this);}
    }
    private record Save<K>(Path path,DwfBulkStore.Cursor<K> input,DwfBulkStore.Cursor<K> output,byte[] state){}
    private final ExecutorService pool=Executors.newFixedThreadPool(2,r->{var t=new Thread(r,"DWF checkpoint IO");t.setDaemon(true);return t;});
    private final Map<UUID,Save<K>> waiting=new HashMap<>();private final Set<UUID> active=new HashSet<>();
    private record PendingLoad(Path path,Load load){}
    private final Map<UUID,List<PendingLoad>> pendingLoads=new HashMap<>();
    private final Semaphore loadBytes=new Semaphore(32*1024*1024);private final LinkedHashSet<Load>waitingReaders=new LinkedHashSet<>();
    private final Set<Load> loads=Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<UUID,Throwable> failures=new ConcurrentHashMap<>();
    private final DwfArchive.Encoder<K> encoder;private final DwfArchive.Limits limits;
    private boolean closed;
    public DwfIo(DwfArchive.Encoder<K> encoder,DwfArchive.Limits limits){this.encoder=encoder;this.limits=limits;}
    public synchronized void save(UUID id,Path path,DwfBulkStore.Cursor<K> input,DwfBulkStore.Cursor<K> output,byte[] state){
        if(closed)throw new IllegalStateException("Disk service closed");
        waiting.put(id,new Save<>(path,input,output,state.clone()));
        if(active.add(id))pool.execute(()->write(id));
    }
    private void write(UUID id){
        Save<K> request;synchronized(this){request=waiting.remove(id);}
        try{if(request!=null){DwfArchive.save(request.path(),request.input(),request.output(),request.state(),encoder,limits);failures.remove(id);}}
        catch(Throwable failure){failures.put(id,failure);}
        synchronized(this){
            if(waiting.containsKey(id)&&!closed)pool.execute(()->write(id));
            else {active.remove(id);var readers=pendingLoads.remove(id);if(readers!=null)for(var reader:readers)read(reader.path(),reader.load());}
            notifyAll();
        }
    }
    public synchronized Load load(UUID id,Path path){
        if(closed)throw new IllegalStateException("Disk service closed");var load=new Load(this);loads.add(load);
        if(active.contains(id))pendingLoads.computeIfAbsent(id,k->new ArrayList<>()).add(new PendingLoad(path,load));else read(path,load);
        return load;
    }
    private synchronized void read(Path path,Load load){load.path=path;schedule(load);}
    private synchronized void drained(Load load){if(load.complete&&load.records.isEmpty())loads.remove(load);}
    private synchronized void released(int bytes){loadBytes.release(bytes);if(!waitingReaders.isEmpty()){var it=waitingReaders.iterator();var next=it.next();it.remove();schedule(next);}}
    private synchronized void schedule(Load load){
        if(closed||load.scheduled||load.complete||load.path==null)return;
        if(!load.cancelled&&(load.records.remainingCapacity()<128||loadBytes.availablePermits()<limits.variantBytes())){waitingReaders.add(load);return;}
        waitingReaders.remove(load);load.scheduled=true;pool.execute(()->readChunk(load));
    }
    private void readChunk(Load load){
        try{
            if(load.cancelled){finish(load);return;}
            if(load.source==null){if(!Files.exists(load.path)){load.complete=true;return;}load.source=new DwfArchive.Source(load.path,limits);load.state=load.source.state();}
            for(int i=0;i<128&&!load.source.done()&&!load.cancelled;i++){
                int reservation=limits.variantBytes();if(!loadBytes.tryAcquire(reservation))break;boolean[]kept={false};
                try{load.source.next(new DwfArchive.Reader(){public void state(byte[]data){}public void entry(int chest,byte[]key,long count)throws IOException{if(load.cancelled)throw new IOException("Load cancelled");if(!load.records.offer(new Record(chest,key,count)))throw new IOException("Load queue overflow");kept[0]=true;loadBytes.release(reservation-key.length);}},1);}finally{if(!kept[0])loadBytes.release(reservation);}
            }
            if(load.cancelled||load.source.done())finish(load);
        }catch(Throwable failure){if(!load.cancelled)load.failure=failure;finish(load);}
        finally{synchronized(this){load.scheduled=false;if(load.complete&&load.records.isEmpty())loads.remove(load);else schedule(load);}}
    }
    private void finish(Load load){if(load.source!=null)try{load.source.close();}catch(IOException failure){if(load.failure==null&&!load.cancelled)load.failure=failure;}load.source=null;load.complete=true;}
    public Throwable failure(UUID id){return failures.get(id);}
    @Override public void close()throws IOException{
        synchronized(this){
            for(var load:List.copyOf(loads))load.cancel();
            // Stop accepting work, but allow queued latest snapshots to drain first.
            long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(60);
            while(!active.isEmpty()&&System.nanoTime()<until)try{wait(100);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException(e);}
            if(!active.isEmpty())throw new IOException("Fabricator checkpoint queue did not drain within 60 seconds");
            closed=true;pool.shutdown();
        }
        try{if(!pool.awaitTermination(30,TimeUnit.SECONDS))throw new IOException("Fabricator disk loads did not stop");}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new IOException(e);}
        if(!failures.isEmpty())throw new IOException("Fabricator checkpoints failed: "+failures.size(),failures.values().iterator().next());
    }
}
