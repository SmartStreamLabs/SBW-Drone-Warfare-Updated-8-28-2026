package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import java.util.function.LongSupplier;

/** One queue per SERVER, shared across dimensions and machines. A job performs
 * ONE bounded unit. Fair rounds may use spare shared capacity; per-kind and
 * elapsed-time caps span all rounds. Production never gains extra elapsed ticks. */
public final class DwfWorkBudget {
    public enum Kind{RECIPE,TRANSPORT,COMPLETION,INDEX,PERSISTENCE}
    public interface Job { Kind kind(); boolean step(); default boolean available(){return true;} }
    public record Measurement(int operations,long nanos,int waiting){}
    private record Queued(Job job,long generation){}
    private final ArrayDeque<Queued> queue=new ArrayDeque<>();
    private final Map<Job,Long> scheduled=new IdentityHashMap<>();private long generation;
    private final LongSupplier clock;
    public DwfWorkBudget(){this(System::nanoTime);}DwfWorkBudget(LongSupplier clock){this.clock=clock;}
    public void add(Job job){Objects.requireNonNull(job);if(!scheduled.containsKey(job)){long token=++generation;scheduled.put(job,token);queue.addLast(new Queued(job,token));}}
    public void remove(Job job){if(scheduled.remove(job)!=null)queue.removeIf(j->j.job()==job);}
    public int size(){return queue.size();}public void clear(){queue.clear();scheduled.clear();}
    public Measurement tick(int maxOperations,long maxNanos,Map<Kind,Integer> limits,boolean overloaded){
        if(maxOperations<1||maxNanos<1)throw new IllegalArgumentException("Positive global budget required");
        long start=clock.getAsLong(),roundGeneration=generation;var blocked=new ArrayDeque<Queued>();var serviced=new ArrayDeque<Queued>();var added=new ArrayDeque<Queued>();int operations=0;var used=new EnumMap<Kind,Integer>(Kind.class);
        int previous;do{previous=operations;int visits=queue.size();blocked.clear();serviced.clear();added.clear();
        try{while(visits-->0&&!queue.isEmpty()&&operations<maxOperations&&clock.getAsLong()-start<maxNanos){
            Queued current=queue.removeFirst();Job job=current.job();if(!Objects.equals(scheduled.get(job),current.generation()))continue;if(current.generation()>roundGeneration){added.addLast(current);continue;}if(!job.available()){blocked.addLast(current);continue;}Kind kind=job.kind();int limit=limits.getOrDefault(kind,maxOperations);
            if(overloaded&&(kind==Kind.INDEX||kind==Kind.TRANSPORT))limit=Math.max(1,limit/4);
            if(used.getOrDefault(kind,0)>=limit){blocked.addLast(current);continue;}
            used.merge(kind,1,Integer::sum);operations++;
            boolean more;
            try{more=job.step();}catch(RuntimeException failure){scheduled.remove(job);throw failure;}
            if(Objects.equals(scheduled.get(job),current.generation())){if(more)serviced.addLast(current);else scheduled.remove(job);}
        }
        }finally{
        // Keep unserved capped jobs before serviced jobs. Rotating a complete
        // round back into the same queue would restore the original order and
        // starve every machine beyond the per-kind allowance indefinitely.
        for(var deferred:List.of(blocked,serviced,added))for(var entry:deferred)if(Objects.equals(scheduled.get(entry.job()),entry.generation()))queue.addLast(entry);
        }
        }while(operations>previous&&operations<maxOperations&&clock.getAsLong()-start<maxNanos&&!queue.isEmpty());
        return new Measurement(operations,clock.getAsLong()-start,queue.size());
    }
}
