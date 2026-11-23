package nl.smartstreamlabs.sbwdroneconfig;
import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.toml.*;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.*;
import java.util.function.UnaryOperator;

/** Versioned, repeatable preparation on a detached config. Persistence is a separate atomic operation. */
public final class ConfigMigrations {
 public record Alias(List<String> oldPath,List<String> newPath,UnaryOperator<Object> convert){public Alias{oldPath=List.copyOf(oldPath);newPath=List.copyOf(newPath);}}
 public record Result(CommentedConfig config,boolean changed,List<String> notices){public Result{notices=List.copyOf(notices);}}
 private ConfigMigrations(){}
 /** Copies only paths declared by the destination spec, once, without changing the legacy document. */
 public static int seedLegacy(ModConfigSpec spec,CommentedConfig destination,UnmodifiableConfig legacy){
  Object version=destination.get(List.of(ConfigSchema.VERSION));
  if(version instanceof Number n&&n.longValue()>=1)return 0;
  var detached=new TomlParser().parse(new TomlWriter().writeToString(legacy));
  return seedNode(spec.getValues(),List.of(),destination,detached);
 }
 private static int seedNode(UnmodifiableConfig node,List<String> parent,CommentedConfig out,UnmodifiableConfig source){
  int count=0;
  for(var entry:node.entrySet()){
   var path=new ArrayList<>(parent);path.add(entry.getKey());
   if(entry.getValue() instanceof UnmodifiableConfig child)count+=seedNode(child,path,out,source);
   else if(!path.equals(List.of(ConfigSchema.VERSION))&&!out.contains(path)&&source.contains(path)){out.set(path,source.get(path));count++;}
  }
  return count;
 }
 public static Result prepare(String family,ModConfigSpec spec,CommentedConfig input,List<Alias> aliases){
  if(!spec.getSpec().contains(List.of(ConfigSchema.VERSION)))throw new IllegalArgumentException("Missing schema definition for "+family);
  var writer=new TomlWriter();String before=writer.writeToString(input);var out=new TomlParser().parse(before);var notes=new ArrayList<String>();
  Object raw=out.get(List.of(ConfigSchema.VERSION));int version=0;
  if(raw instanceof Number n&&(raw instanceof Integer||raw instanceof Long)&&n.longValue()>=0&&n.longValue()<=Integer.MAX_VALUE)version=n.intValue();
  else if(raw!=null)notes.add(family+": invalid schema version; treating file as legacy");
  if(version>ConfigSchema.CURRENT)throw new IllegalArgumentException("Config schema is newer than this mod: "+family+" v"+version);
  if(version<1){
   for(var a:aliases)if(out.contains(a.oldPath())){
    if(!out.contains(a.newPath())){
     try{out.set(a.newPath(),a.convert().apply(out.get(a.oldPath())));notes.add(family+": renamed "+a.oldPath()+" to "+a.newPath());}
     catch(RuntimeException e){notes.add(family+": invalid legacy value at "+a.oldPath()+"; destination will use its validated default");}
    }else notes.add(family+": kept explicit destination "+a.newPath()+" over obsolete "+a.oldPath());
    if(!a.oldPath().equals(a.newPath()))out.remove(a.oldPath());
   }
   out.set(List.of(ConfigSchema.VERSION),ConfigSchema.CURRENT);notes.add(family+": upgraded schema "+version+" to "+ConfigSchema.CURRENT);
  }
  // Only the server gameplay spec owns this marker. Client preferences and other families are untouched.
  var rangePath=List.of("drone","droneMaxRange");
  var rangeVersion=List.of("drone","_manualRangeDefaultsVersion");
  if(family.equals("AddonConfig")&&spec.getSpec().contains(rangeVersion)){
   Object marker=out.get(rangeVersion);
   if(!(marker instanceof Number n)||n.intValue()<1){
    Object range=out.get(rangePath);
    if(range instanceof Number n&&n.doubleValue()==100d){
     out.set(rangePath,10000);
     notes.add("AddonConfig: upgraded obsolete 100-block manual range to 10000 (one-time migration)");
    }
    out.set(rangeVersion,1);
   }
  }
  spec.correct(out,(action,path,oldValue,newValue)->notes.add(family+": "+action+" "+String.join(".",path)),(action,path,oldValue,newValue)->{});
  if(!spec.isCorrect(out))throw new IllegalStateException("Migration did not produce a valid config: "+family);
  return new Result(out,!before.equals(writer.writeToString(out)),notes);
 }
}
