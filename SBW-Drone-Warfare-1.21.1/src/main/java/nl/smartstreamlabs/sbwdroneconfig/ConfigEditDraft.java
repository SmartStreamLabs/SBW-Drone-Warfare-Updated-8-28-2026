package nl.smartstreamlabs.sbwdroneconfig;

import com.google.gson.*;
import java.util.*;

/** An edit buffer only: typing, resetting and importing never mutate live loader values or files. */
public final class ConfigEditDraft {
 private final Map<String,ConfigCatalog.Definition> definitions;
 private final Map<String,Object> baseline=new LinkedHashMap<>(),edits=new LinkedHashMap<>();
 public ConfigEditDraft(List<ConfigCatalog.Definition> definitions){
  var map=new LinkedHashMap<String,ConfigCatalog.Definition>();
  for(var d:definitions){if(map.putIfAbsent(d.id(),d)!=null)throw new IllegalArgumentException("Duplicate setting "+d.id());baseline.put(d.id(),d.current());}
  this.definitions=Collections.unmodifiableMap(map);
 }
 public Object current(String id){require(id);return edits.containsKey(id)?edits.get(id):baseline.get(id);}
 public Object original(String id){require(id);return baseline.get(id);}
 public boolean changed(String id){return edits.containsKey(id);}
 public int changes(){return edits.size();}
 public void set(String id,String text){var d=requireEditable(id);store(id,d.parse(text));}
 public void reset(String id){var d=requireEditable(id);store(id,d.defaults());}
 public void undo(String id){require(id);edits.remove(id);}
 public void cancel(){edits.clear();}
 /** Preserve edits across remote updates; accept only explicitly acknowledged matching edits. */
 public void observe(String id,Object actual,boolean acknowledged){require(id);if(edits.containsKey(id)){if(!acknowledged||!Objects.equals(edits.get(id),actual))return;edits.remove(id);}baseline.put(id,actual);}
 public Map<String,Object> pending(){return Collections.unmodifiableMap(new LinkedHashMap<>(edits));}
 public Map<String,Object> expected(){var values=new LinkedHashMap<String,Object>();for(String id:edits.keySet())values.put(id,baseline.get(id));return Collections.unmodifiableMap(values);}
 private void store(String id,Object value){if(Objects.equals(value,baseline.get(id)))edits.remove(id);else edits.put(id,value);}
 private ConfigCatalog.Definition require(String id){var d=definitions.get(id);if(d==null)throw new IllegalArgumentException("Unknown setting "+id);return d;}
 private ConfigCatalog.Definition requireEditable(String id){var d=require(id);if(d.managed())throw new IllegalArgumentException("Setting is managed by config migrations: "+id);if(!ConfigAvailability.active(d))throw new IllegalArgumentException(ConfigAvailability.reason(d)+": "+id);return d;}
 public String exportPreset(boolean onlyChanged){
  var root=new JsonObject();root.addProperty("schema",1);var values=new JsonObject();var gson=new Gson();
  for(var d:definitions.values())if(!d.managed()&&ConfigAvailability.active(d)&&(!onlyChanged||changed(d.id())))values.add(d.id(),gson.toJsonTree(current(d.id())));
  root.add("values",values);return new GsonBuilder().setPrettyPrinting().create().toJson(root);
 }
 /** Validate the entire preset before touching even the unsaved draft. Unknown keys fail explicitly. */
 public void importPreset(String json){
  var root=JsonParser.parseString(json).getAsJsonObject();
  if(!root.has("schema")||!root.get("schema").isJsonPrimitive()||!root.get("schema").getAsString().equals("1"))throw new IllegalArgumentException("Unsupported preset schema");
  if(!root.has("values")||!root.get("values").isJsonObject())throw new IllegalArgumentException("Missing preset values");
  var prepared=new LinkedHashMap<String,Object>();
  for(var entry:root.getAsJsonObject("values").entrySet()){
   var d=requireEditable(entry.getKey());var v=entry.getValue();Object def=d.defaults();String text;
   if(def instanceof List<?>)text=v.toString();
   else {if(!v.isJsonPrimitive())throw new IllegalArgumentException("Invalid value type for "+d.id());var p=v.getAsJsonPrimitive();
    if(def instanceof Boolean&&!p.isBoolean()||def instanceof Number&&!p.isNumber()||def instanceof String&&!p.isString()||def instanceof Enum<?>&&!p.isString())throw new IllegalArgumentException("Invalid value type for "+d.id());text=v.getAsString();}
   prepared.put(d.id(),d.parse(text));
  }
  prepared.forEach(this::store);
 }
}
