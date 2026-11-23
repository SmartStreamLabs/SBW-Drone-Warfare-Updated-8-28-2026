package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.ModList;
import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.*;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

/** One loader-owned file per atomic commit. Authority and revision validation precede this adapter. */
public final class NativeConfigIo {
 private NativeConfigIo(){}
 private static CommentedConfig data(ModConfig config){return config.getConfigData();}
 public static void commit(Map<ConfigCatalog.Definition,Object> changes)throws IOException {
  if(changes.isEmpty())return;
  var first=changes.keySet().iterator().next();var config=ConfigLifecycle.config(first.owner());
  commitKnown(config,changes);
 }
 /** Called only with a loader registry object, never a network-supplied path. The UI rechecks local scope. */
 public static void commitKnown(ModConfig config,Map<ConfigCatalog.Definition,Object> changes)throws IOException {
  if(changes.isEmpty())return;var first=changes.keySet().iterator().next();
  if(config==null||!first.loaded())throw new IllegalStateException("Configuration is not loaded in this context");
  if(config.getSpec()!=first.owner())throw new IllegalArgumentException("Configuration owner mismatch");
  var target=config.getFullPath();
  if(target==null||!java.nio.file.Files.isRegularFile(target))throw new IllegalStateException("No authoritative configuration file in this context");
  if(!(data(config) instanceof CommentedFileConfig))throw new IllegalStateException("Remote synchronized values cannot be saved locally");
  for(var e:changes.entrySet()){
   if(e.getKey().owner()!=first.owner())throw new IllegalArgumentException("A commit must address one configuration file");
   if(!e.getKey().rules().test(e.getValue()))throw new IllegalArgumentException("Invalid value: "+e.getKey().id());
  }
  // Round-tripping a snapshot deep-copies nested sections and preserves TOML comments.
  var writer=new TomlWriter();var expected=new TomlParser().parse(writer.writeToString(data(config)));
  byte[] before=java.nio.file.Files.readAllBytes(target);
  var prepared=new TomlParser().parse(new String(before,StandardCharsets.UTF_8));
  if(!values(expected).equals(values(prepared)))throw new ConcurrentModificationException("Configuration changed on disk; wait for native reload and reopen before applying");
  for(var e:changes.entrySet())prepared.set(e.getKey().path(),e.getValue() instanceof Enum<?> n?n.name():e.getValue());
  byte[] bytes=writer.writeToString(prepared).getBytes(StandardCharsets.UTF_8);
  if(!Arrays.equals(before,java.nio.file.Files.readAllBytes(target)))throw new ConcurrentModificationException("Configuration changed on disk during preparation");
  if(!AtomicConfigFiles.replace(target,bytes,false))return;
  try{reload(config,prepared);}catch(RuntimeException failure){
   // Do not roll back somebody else's subsequent disk edit.
   if(Arrays.equals(bytes,java.nio.file.Files.readAllBytes(target))){
    AtomicConfigFiles.replace(target,before,false);
    try{reload(config,expected);}catch(RuntimeException rollback){failure.addSuppressed(rollback);}
   }
   throw new IOException("Native configuration reload failed; committed bytes were restored when still owned by this transaction",failure);
  }
 }
 private static Map<List<String>,Object> values(UnmodifiableConfig config){var out=new LinkedHashMap<List<String>,Object>();flatten(config,List.of(),out);return out;}
 private static void flatten(UnmodifiableConfig node,List<String> parent,Map<List<String>,Object> out){for(var e:node.entrySet()){var path=new ArrayList<>(parent);path.add(e.getKey());Object value=e.getValue();if(value instanceof UnmodifiableConfig section)flatten(section,path,out);else out.put(List.copyOf(path),value);}}
 private static void reload(ModConfig config,CommentedConfig prepared){
  var actual=data(config);if(!(actual instanceof CommentedFileConfig file))throw new IllegalStateException("Expected loader-owned file configuration");file.load();
  config.getSpec().afterReload();ModList.get().getModContainerById(config.getModId()).orElseThrow().dispatchConfigEvent(new ModConfigEvent.Reloading(config));
 }
}
