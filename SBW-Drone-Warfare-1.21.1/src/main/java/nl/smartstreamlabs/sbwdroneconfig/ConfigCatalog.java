package nl.smartstreamlabs.sbwdroneconfig;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.config.ModConfig;
import java.util.*;

/** Registration-backed catalogue. Bounds, defaults and validation always come from the live loader spec. */
public final class ConfigCatalog {
 public record Definition(String id,String family,String file,ModConfig.Type scope,List<String> path,
                          ModConfigSpec owner,ModConfigSpec.ConfigValue<?> value,ModConfigSpec.ValueSpec rules) {
  public Definition {path=List.copyOf(path);}
  public Object defaults(){return immutable(rules.getDefault());}
  public Object current(){return owner.isLoaded()?immutable(value.get()):defaults();}
  public boolean loaded(){return owner.isLoaded();}
  public boolean managed(){return path.equals(List.of(ConfigSchema.VERSION)) || (family.equals("AddonConfig") && path.equals(List.of("drone", "_manualRangeDefaultsVersion")));}
  public String comment(){return Objects.toString(rules.getComment(),"");}
  public String nativeRestart(){return rules.restartType().name();}
  public String minimum(){return bound(rules,"getMin");}
  public String maximum(){return bound(rules,"getMax");}
  public List<String> choices(){Object d=rules.getDefault();return d instanceof Enum<?> e?Arrays.stream(e.getDeclaringClass().getEnumConstants()).map(Enum::name).toList():List.of();}
  public Object parse(String text){
   Object d=rules.getDefault(),v;
   if(d instanceof Boolean){if(!text.equals("true")&&!text.equals("false"))throw new IllegalArgumentException("Expected true or false");v=Boolean.valueOf(text);}
   else if(d instanceof Long)v=Long.valueOf(text);
   else if(d instanceof Integer)v=Integer.valueOf(text);
   else if(d instanceof Double){double n=Double.parseDouble(text);if(!Double.isFinite(n))throw new IllegalArgumentException("Expected finite number");v=n;}
   else if(d instanceof Enum<?>){v=choices().stream().filter(text::equals).findFirst().map(n->enumValue((Enum<?>)d,n)).orElseThrow(()->new IllegalArgumentException("Unknown choice"));}
   else if(d instanceof List<?> sample){var json=com.google.gson.JsonParser.parseString(text);if(!json.isJsonArray())throw new IllegalArgumentException("Expected JSON list");var list=new ArrayList<Object>();for(var e:json.getAsJsonArray())list.add(listElement(e,sample.isEmpty()?null:sample.get(0)));v=list;}
   else if(d instanceof String)v=text;
   else throw new IllegalArgumentException("Unsupported config type: "+d.getClass().getName());
   if(!rules.test(v))throw new IllegalArgumentException("Value outside registered bounds or invalid list");ConfigRules.validateValue(this,v);return immutable(v);
  }
  @SuppressWarnings({"rawtypes","unchecked"})private static Enum<?> enumValue(Enum<?> d,String n){return Enum.valueOf((Class)d.getDeclaringClass(),n);}
  private static Object listElement(com.google.gson.JsonElement json,Object sample){
   if(json.isJsonArray()){var out=new ArrayList<Object>();for(var child:json.getAsJsonArray())out.add(listElement(child,sample instanceof List<?> l&&!l.isEmpty()?l.get(0):null));return out;}
   if(!json.isJsonPrimitive())throw new IllegalArgumentException("Unsupported list element type");var p=json.getAsJsonPrimitive();
   if(p.isBoolean())return p.getAsBoolean();if(p.isString())return sample instanceof Enum<?> e?enumValue(e,p.getAsString()):p.getAsString();
   var n=p.getAsBigDecimal();if(sample instanceof Long)return n.longValueExact();if(sample instanceof Integer)return n.intValueExact();if(sample instanceof Double){double value=n.doubleValue();if(!Double.isFinite(value))throw new IllegalArgumentException("Expected finite list number");return value;}
   try{return n.intValueExact();}catch(ArithmeticException ignored){}try{return n.longValueExact();}catch(ArithmeticException ignored){}double value=n.doubleValue();if(!Double.isFinite(value))throw new IllegalArgumentException("Expected finite list number");return value;
  }
 }
 private static final Map<String,Definition> VALUES=new LinkedHashMap<>();
 private ConfigCatalog(){}
 public static ModConfigSpec capture(String family,ModConfig.Type scope,ModConfigSpec spec,String filename){
  Objects.requireNonNull(family);Objects.requireNonNull(scope);Objects.requireNonNull(spec);
  String file=filename==null?SbwDroneRangeConfig.MOD_ID+"-"+scope.name().toLowerCase(Locale.ROOT)+".toml":filename;
  flatten(family,file,scope,spec,spec.getValues(),new ArrayList<>(),VALUES);return spec;
 }
 /** A detached view of another mod's registered native spec; never accepted by our server-edit protocol. */
 public static List<Definition> describe(String family,String file,ModConfig.Type scope,ModConfigSpec spec){var values=new LinkedHashMap<String,Definition>();flatten(family,file,scope,spec,spec.getValues(),List.of(),values);return List.copyOf(values.values());}
 private static void flatten(String family,String file,ModConfig.Type scope,ModConfigSpec spec,UnmodifiableConfig node,List<String> parent,Map<String,Definition> destination){
  for(var entry:node.entrySet()){
   var path=new ArrayList<>(parent);path.add(entry.getKey());Object obj=entry.getValue();
   if(obj instanceof UnmodifiableConfig child)flatten(family,file,scope,spec,child,path,destination);
   else if(obj instanceof ModConfigSpec.ConfigValue<?> value){
    String id=family+(path.equals(List.of(ConfigSchema.VERSION))?"@"+file:"")+":"+String.join(".",path);ModConfigSpec.ValueSpec rules=spec.getSpec().get(path);
    if(rules==null)throw new IllegalStateException("Missing native validation for "+id);
    Definition d=new Definition(id,family,file,scope,path,spec,value,rules);
    if(destination.putIfAbsent(id,d)!=null)throw new IllegalStateException("Duplicate configuration setting "+id);
   }else throw new IllegalStateException("Unrecognized native config entry: "+family+path);
  }
 }
 public static List<Definition> definitions(){return List.copyOf(VALUES.values());}
 public static Definition find(String id){return VALUES.get(id);}
 private static Object immutable(Object v){return v instanceof List<?> list?List.copyOf(list):v;}
 private static String bound(ModConfigSpec.ValueSpec rules,String method){
  Object range=rules.getRange();if(range==null)return "";
  // Legacy Forge exposes a package-private Range in the development API.
  try{var getter=range.getClass().getMethod(method);getter.setAccessible(true);return Objects.toString(getter.invoke(range));}
  catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot read native config bounds",e);}
 }
}
