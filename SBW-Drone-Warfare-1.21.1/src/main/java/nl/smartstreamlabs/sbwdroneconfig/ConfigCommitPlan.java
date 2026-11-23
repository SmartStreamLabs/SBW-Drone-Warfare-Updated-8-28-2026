package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;
import java.util.function.Predicate;
import java.io.IOException;

/** Validated optimistic transaction for a single native file. Contains no user-supplied path. */
public record ConfigCommitPlan(long revision,Map<ConfigCatalog.Definition,Object> changes) {
 public ConfigCommitPlan {changes=Collections.unmodifiableMap(new LinkedHashMap<>(changes));}
 public static ConfigCommitPlan prepare(long expectedRevision,long actualRevision,Map<String,String> edits,Map<String,String> originals,Predicate<ConfigCatalog.Definition> authorized){
  if(expectedRevision!=actualRevision)throw new ConcurrentModificationException("Configuration changed; refresh before applying");
  if(!edits.keySet().equals(originals.keySet()))throw new IllegalArgumentException("Missing or unexpected original values");
  var prepared=new LinkedHashMap<ConfigCatalog.Definition,Object>();Object owner=null;
  for(var entry:edits.entrySet()){
   var d=ConfigCatalog.find(entry.getKey());if(d==null)throw new IllegalArgumentException("Unknown setting "+entry.getKey());
   if(d.managed())throw new SecurityException("Schema metadata cannot be edited");
   if(!ConfigAvailability.active(d))throw new IllegalArgumentException(ConfigAvailability.reason(d));
   if(!authorized.test(d))throw new SecurityException("Not permitted to change "+d.id());
   if(owner!=null&&owner!=d.owner())throw new IllegalArgumentException("Submit one config file per transaction");owner=d.owner();
   if(!Objects.equals(d.current(),d.parse(originals.get(d.id()))))throw new ConcurrentModificationException("Effective value changed: "+d.id());
   prepared.put(d,d.parse(entry.getValue()));
  }
  ConfigRules.validate(prepared);return new ConfigCommitPlan(actualRevision,prepared);
 }
 public void execute(Predicate<ConfigCatalog.Definition> authorized)throws IOException {
  synchronized(ConfigLifecycle.COMMIT_LOCK){
  for(var d:changes.keySet())if(!authorized.test(d))throw new SecurityException("Permission changed before commit");
  if(!changes.isEmpty()&&revision!=ConfigLifecycle.revision(changes.keySet().iterator().next().owner()))throw new ConcurrentModificationException("Configuration changed during preparation");
  for(var d:changes.keySet())if(!d.loaded())throw new IllegalStateException("Configuration unloaded before commit");
  ConfigRules.validate(changes);NativeConfigIo.commit(changes);
  }
 }
}
