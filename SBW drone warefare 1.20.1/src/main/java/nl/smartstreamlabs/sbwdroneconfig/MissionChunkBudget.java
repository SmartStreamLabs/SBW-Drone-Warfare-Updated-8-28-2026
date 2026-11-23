package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;

/** Loader-independent ownership and scheduling, tested without a running Minecraft world. */
public final class MissionChunkBudget<K> {
    public interface Backend<K>{void add(K chunk);void remove(K chunk);boolean ready(K chunk);}
    private final Backend<K> backend;
    private final LinkedHashMap<UUID,Set<K>> owners=new LinkedHashMap<>();
    private final Set<K> active=new HashSet<>();
    private int cursor;
    public MissionChunkBudget(Backend<K> backend){this.backend=backend;}
    public void follow(UUID owner,Set<K> chunks){if(chunks.size()>9)throw new IllegalArgumentException("Mission window exceeds 3x3");owners.put(owner,new LinkedHashSet<>(chunks));prune();}
    public void release(UUID owner){owners.remove(owner);prune();}
    public void clear(){owners.clear();prune();}
    private void prune(){var wanted=new HashSet<K>();owners.values().forEach(wanted::addAll);var it=active.iterator();while(it.hasNext()){var chunk=it.next();if(!wanted.contains(chunk)){backend.remove(chunk);it.remove();}}}
    public int count(){return active.size();}
    public int owners(){return owners.size();}
    public boolean ready(UUID owner){var chunks=owners.get(owner);return chunks!=null&&!chunks.isEmpty()&&chunks.stream().allMatch(k->active.contains(k)&&backend.ready(k));}
    public void tick(int starts,int pendingLimit,int limit){
        var list=new ArrayList<>(owners.values());if(list.isEmpty())return;
        int pending=(int)active.stream().filter(k->!backend.ready(k)).count();
        for(int n=0;n<list.size();n++)for(var key:list.get((cursor+n)%list.size())){
            if(!active.contains(key)&&starts>0&&pending<pendingLimit&&active.size()<limit){backend.add(key);active.add(key);starts--;pending++;}
        }
        cursor=(cursor+1)%list.size();
    }
}
