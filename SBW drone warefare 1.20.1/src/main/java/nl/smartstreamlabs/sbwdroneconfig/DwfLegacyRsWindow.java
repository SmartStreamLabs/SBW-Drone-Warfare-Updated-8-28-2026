package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.math.BigInteger;

/** One legacy RS view window per NETWORK and exact variant, never per chest.
 * This ledger does not own or mutate physical stock. BigInteger also prevents
 * overflow when summing multiple individually valid 64-bit chest quantities. */
public final class DwfLegacyRsWindow<K>{
 private static final BigInteger LIMIT=BigInteger.valueOf(Integer.MAX_VALUE);
 private static final class Network<K>{
  final Map<K,BigInteger>totals=new HashMap<>();int sources,retiring,limitedVariants;boolean dropped;
 }
 private static final class Source<K>{
  final Object route,networkId;final Network<K>network;final Map<K,Long>counts=new HashMap<>();
  Source(Object route,Object networkId,Network<K>network){this.route=route;this.networkId=networkId;this.network=network;}
 }
 private final Map<Object,Network<K>>networks=new IdentityHashMap<>();
 private final Map<Object,Source<K>>sources=new HashMap<>();
 /** A physical chest may expose stock through exactly one route at a time. */
 public boolean bind(Object chest,Object route,Object network){
  Objects.requireNonNull(chest);Objects.requireNonNull(route);Objects.requireNonNull(network);
  var existing=sources.get(chest);if(existing!=null)return existing.route==route&&existing.networkId==network;
  var target=networks.computeIfAbsent(network,k->new Network<>());target.sources++;sources.put(chest,new Source<>(route,network,target));return true;
 }
 public void update(Object chest,Object route,K variant,long actualCount){
  if(actualCount<0)throw new IllegalArgumentException("Negative physical count");Objects.requireNonNull(variant);
  var source=sources.get(chest);if(source==null||source.route!=route)throw new IllegalStateException("Unbound or duplicate storage route");
  long previous=source.counts.getOrDefault(variant,0L);if(actualCount==0)source.counts.remove(variant);else source.counts.put(variant,actualCount);
  change(source.network,variant,BigInteger.valueOf(actualCount).subtract(BigInteger.valueOf(previous)));
 }
 private static <K>void change(Network<K>network,K key,BigInteger delta){
  BigInteger old=network.totals.getOrDefault(key,BigInteger.ZERO);BigInteger next=old.add(delta);if(old.compareTo(LIMIT)<=0&&next.compareTo(LIMIT)>0)network.limitedVariants++;else if(old.compareTo(LIMIT)>0&&next.compareTo(LIMIT)<=0)network.limitedVariants--;if(next.signum()<0)throw new IllegalStateException("Storage view ledger underflow");if(next.signum()==0)network.totals.remove(key);else network.totals.put(key,next);
 }
 public BigInteger tracked(Object network,K variant){var n=networks.get(network);return n==null?BigInteger.ZERO:n.totals.getOrDefault(variant,BigInteger.ZERO);}
 public boolean limited(Object network){var n=networks.get(network);return n!=null&&n.limitedVariants>0;}
 public boolean limited(Object network,K variant){return tracked(network,variant).compareTo(LIMIT)>0;}
 /** Read-only planning: simulation must not consume a view allowance or stock. */
 public int refill(Object network,K variant,int currentlyVisible){
  if(currentlyVisible<0)throw new IllegalArgumentException("Invalid legacy view count");var n=networks.get(network);if(n==null||n.retiring>0)return 0;
  int floor=tracked(network,variant).min(LIMIT).intValue();return Math.max(0,floor-currentlyVisible);
 }
 public boolean updating(Object network){var n=networks.get(network);return n!=null&&n.retiring>0;}
 /** Reconciliation must first let RS apply ordinary deltas, then fill only the
  * missing part of its bounded window. Never add a full chest total again. */
 public DwfWorkBudget.Job unbind(Object chest,Object route){
  var source=sources.get(chest);if(source==null||source.route!=route)return null;sources.remove(chest);var n=source.network;n.sources--;
  if(n.sources==0){n.dropped=true;networks.remove(source.networkId);return null;}
  n.retiring++;var it=source.counts.entrySet().iterator();
  return new DwfWorkBudget.Job(){
   boolean finished;
   public DwfWorkBudget.Kind kind(){return DwfWorkBudget.Kind.TRANSPORT;}
   public boolean step(){if(finished||n.dropped){finished=true;return false;}int count=0;while(count++<256&&it.hasNext()){var e=it.next();change(n,e.getKey(),BigInteger.valueOf(e.getValue()).negate());it.remove();}if(!it.hasNext()){n.retiring--;finished=true;return false;}return true;}
  };
 }
 public int networkCount(){return networks.size();}public int sourceCount(){return sources.size();}
 public void clear(){for(var n:networks.values())n.dropped=true;sources.clear();networks.clear();}
}
