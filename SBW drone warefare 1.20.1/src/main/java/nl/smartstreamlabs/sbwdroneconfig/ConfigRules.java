package nl.smartstreamlabs.sbwdroneconfig;
import java.util.*;

/** Cross-field rules match the existing runtime clamps. Legacy files remain readable without changing stored values. */
public final class ConfigRules {
 public record Ordered(String minimum,String maximum){}
 public static final List<Ordered> ORDERED=List.of(
  new Ordered("AddonConfig:lucas.lucasMinSpeed","AddonConfig:lucas.lucasMaxSpeed"),
  new Ordered("Hx2Config:minimumSpeed","Hx2Config:maximumSpeed"),
  new Ordered("Id01Config:minimumForwardSpeed","Id01Config:flightSpeed"),
  new Ordered("AddonConfig:droneJammer.overlayStartThreshold","AddonConfig:droneJammer.controlInterferenceThreshold"),
  new Ordered("AddonConfig:droneJammer.controlInterferenceThreshold","AddonConfig:droneJammer.hardJamThreshold"),
  new Ordered("DistanceWarConfig:minIntervalSeconds","DistanceWarConfig:maxIntervalSeconds"),
  new Ordered("DistanceWarConfig:minSourceDistance","DistanceWarConfig:maxSourceDistance")
 );
 private ConfigRules(){}
 public static void validate(Map<ConfigCatalog.Definition,Object> changes){validate(changes,ConfigCatalog.definitions());}
 public static void validate(Map<ConfigCatalog.Definition,Object> changes,List<ConfigCatalog.Definition> definitions){
  var byId=new HashMap<String,ConfigCatalog.Definition>();for(var d:definitions)byId.put(d.id(),d);
  var edits=new HashMap<String,Object>();for(var e:changes.entrySet()){validateValue(e.getKey(),e.getValue());edits.put(e.getKey().id(),e.getValue());}
  for(var rule:ORDERED){if(!edits.containsKey(rule.minimum())&&!edits.containsKey(rule.maximum()))continue;var min=byId.get(rule.minimum());var max=byId.get(rule.maximum());if(min==null||max==null)continue;var lo=(Number)edits.getOrDefault(min.id(),min.current());var hi=(Number)edits.getOrDefault(max.id(),max.current());if(lo.doubleValue()>hi.doubleValue())throw new IllegalArgumentException(rule.minimum()+" must not exceed "+rule.maximum());}
 }
 public static void validateValue(ConfigCatalog.Definition definition,Object value){
  if(!(value instanceof List<?> list))return;
  boolean damageTypes=definition.family().equals("InjuryConfig")&&Set.of("additionalTypes","excludedTypes").contains(definition.path().get(definition.path().size()-1));
  boolean uuids=definition.family().equals("AddonConfig")&&definition.path().get(definition.path().size()-1).equals("ignoredDroneOwnerUUIDs");
  for(Object item:list){if(damageTypes&&(!(item instanceof String s)||net.minecraft.resources.ResourceLocation.tryParse(s)==null))throw new IllegalArgumentException("Expected a damage-type registry ID, for example minecraft:arrow");if(uuids){if(!(item instanceof String s))throw new IllegalArgumentException("Expected player UUID");try{if(!UUID.fromString(s).toString().equalsIgnoreCase(s))throw new IllegalArgumentException("Expected canonical player UUID");}catch(IllegalArgumentException e){throw new IllegalArgumentException("Expected canonical player UUID: "+s);}}}
 }
 public static Object effective(ConfigCatalog.Definition definition){return effective(definition,ConfigCatalog.definitions());}
 public static Object effective(ConfigCatalog.Definition definition,List<ConfigCatalog.Definition> definitions){
  if(Set.of("VehicleProfilesConfig","VehicleDriveConfig").contains(definition.family())&&definition.path().size()==3&&definition.current() instanceof Number number&&number.doubleValue()==0){String inherited=definition.family()+":defaults."+definition.path().get(2);return definitions.stream().filter(d->d.id().equals(inherited)).map(ConfigCatalog.Definition::current).findFirst().orElse(1d);}
  Object configured=definition.current();for(var rule:ORDERED)if(rule.maximum().equals(definition.id())){
   var lower=definitions.stream().filter(d->d.id().equals(rule.minimum())).findFirst().orElse(null);if(lower==null)return configured;
   double minimum=((Number)effective(lower,definitions)).doubleValue(),result=Math.max(minimum,((Number)configured).doubleValue());
   if(configured instanceof Integer)return (int)result;return result;
  }return configured;
 }
}
