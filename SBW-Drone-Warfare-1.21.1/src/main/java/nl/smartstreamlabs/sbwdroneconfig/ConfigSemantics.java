package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;import java.nio.charset.StandardCharsets;import com.google.gson.*;
/** Small shared semantic additions to the authoritative native spec, read once rather than per frame/tick. */
public final class ConfigSemantics {
 public record Details(String unit,String activation,List<String> requires){public Details{requires=List.copyOf(requires);}}
 private static final Details EMPTY=new Details("","NATIVE",List.of());
 private static final Map<String,Details> DETAILS=load();
 private ConfigSemantics(){}
 private static Map<String,Details> load(){
  try(var stream=ConfigSemantics.class.getResourceAsStream("/assets/sbwdroneconfig/configuration/semantics.json")){
   if(stream==null)return Map.of();var root=JsonParser.parseReader(new java.io.InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var values=new LinkedHashMap<String,Details>();
   for(var e:root.entrySet()){var data=e.getValue().getAsJsonObject();var requires=new ArrayList<String>();if(data.has("requires"))for(var r:data.getAsJsonArray("requires"))requires.add(r.getAsString());values.put(e.getKey(),new Details(data.has("unit")?data.get("unit").getAsString():"",data.has("activation")?data.get("activation").getAsString():"NATIVE",requires));}
   return Map.copyOf(values);
  }catch(Exception e){throw new IllegalStateException("Invalid bundled configuration semantics",e);}
 }
 public static Details details(ConfigCatalog.Definition definition){return DETAILS.getOrDefault(definition.id(),EMPTY);}
 public static Set<String> describedIds(){return DETAILS.keySet();}
}
