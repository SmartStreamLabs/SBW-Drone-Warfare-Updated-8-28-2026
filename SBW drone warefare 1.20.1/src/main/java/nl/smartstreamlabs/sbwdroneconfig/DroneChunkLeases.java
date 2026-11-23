package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
/** Bounded, shared, non-persistent extra tickets; server thread only. */
final class DroneChunkLeases<O> {
    interface Backend { void add(long chunk); void remove(long chunk); boolean ready(long chunk); }
    private static final class Lease {
        final Set<Long> chunks; long expires;
        Lease(Set<Long> chunks,long expires) { this.chunks=chunks; this.expires=expires; }
    }
    private final Map<O, Lease> owners = new LinkedHashMap<>();
    private final Map<Long, Long> active = new LinkedHashMap<>();
    private final int limit, ownerLimit;
    private Set<Long> desired=Set.of();
    private boolean dirty;
    DroneChunkLeases(int limit, int ownerLimit) { this.limit=limit; this.ownerLimit=ownerLimit; }
    void touch(O owner, Set<Long> chunks, long now) {
        if (chunks.size()>81) throw new IllegalArgumentException("Extra ticket radius exceeds four chunks");
        if (!owners.containsKey(owner) && owners.size()>=ownerLimit) return;
        Lease previous=owners.get(owner);
        if(previous!=null && previous.chunks.equals(chunks)) { previous.expires=now+10; return; }
        owners.put(owner,new Lease(Collections.unmodifiableSet(new LinkedHashSet<>(chunks)),now+10));
        dirty=true;
    }
    void release(O owner) { if(owners.remove(owner)!=null) dirty=true; }
    int tick(long now, int starts, int pendingLimit, Backend backend) {
        int added=0;
        if(owners.values().removeIf(lease -> now>lease.expires)) dirty=true;
        if(dirty) {
            desired=new LinkedHashSet<>();
            for(Lease lease:owners.values()) desired.addAll(lease.chunks);
            dirty=false;
        }
        Iterator<Map.Entry<Long,Long>> iterator=active.entrySet().iterator();
        int pending=0;
        while(iterator.hasNext()) {
            Map.Entry<Long,Long> entry=iterator.next();
            if(desired.contains(entry.getKey())) entry.setValue(now);
            // A bounded tail avoids border churn and protects the body on return.
            if(now-entry.getValue()>20 || (!desired.contains(entry.getKey()) && !backend.ready(entry.getKey()))) {
                backend.remove(entry.getKey()); iterator.remove();
            }
            else if(!backend.ready(entry.getKey())) pending++;
        }
        for(long chunk:desired) {
            if(starts<=0 || active.size()>=limit || pending>=pendingLimit) break;
            if(active.containsKey(chunk)) continue;
            backend.add(chunk); active.put(chunk,now); starts--; added++;
            if(!backend.ready(chunk)) pending++;
        }
        return added;
    }
    int pending(Backend backend) { return (int)active.keySet().stream().filter(chunk -> !backend.ready(chunk)).count(); }
    void clear(Backend backend) { active.keySet().forEach(backend::remove); active.clear(); owners.clear(); desired=Set.of(); dirty=false; }
    int tickets() { return active.size(); }
    int owners() { return owners.size(); }
}
