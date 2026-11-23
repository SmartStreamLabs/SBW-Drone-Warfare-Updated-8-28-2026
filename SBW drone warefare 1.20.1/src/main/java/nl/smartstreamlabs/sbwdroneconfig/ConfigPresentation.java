package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraft.network.chat.Component;
import java.util.*;
/** Presentation is derived from registered definitions. Metadata supplies translations, never a separate GUI list. */
public final class ConfigPresentation {
 private ConfigPresentation(){}
 public static String key(ConfigCatalog.Definition d){return "config.sbwdroneconfig.option."+d.id().replace(':','.');}
 public static Component name(ConfigCatalog.Definition d){String key=key(d)+".name";String nativeKey=d.rules().getTranslationKey();if(nativeKey!=null&&ConfigEnglish.has(nativeKey))return ConfigEnglish.text(nativeKey);return ConfigEnglish.has(key)?ConfigEnglish.text(key):Component.literal(words(d.path().get(d.path().size()-1)));}
 public static Component help(ConfigCatalog.Definition d){String key=key(d)+".help";return ConfigEnglish.has(key)?ConfigEnglish.text(key):Component.literal(d.comment().isBlank()?d.id():d.comment());}
 public static String category(ConfigCatalog.Definition d){return d.family()+(d.path().size()>1?"/"+String.join("/",d.path().subList(0,d.path().size()-1)):"");}
 public static Component categoryName(String category){String key="config.sbwdroneconfig.category."+category.replace('/','.');if(ConfigEnglish.has(key))return ConfigEnglish.text(key);return Component.literal(words(category.replace("Config","").replace("/"," / ")));}
 public static boolean advanced(ConfigCatalog.Definition d){String n=d.path().get(d.path().size()-1).toLowerCase(Locale.ROOT);return n.matches(".*(debug|budget|interval|ticks|lod|offset|damping|smoothing|cache|batch|tickets|variantlimit|workunits).*")||d.defaults() instanceof List<?>;}
 public static String words(String key){return key.replaceAll("([A-Z]+)([A-Z][a-z])","$1 $2").replaceAll("([a-z0-9])([A-Z])","$1 $2").replace('_',' ').trim();}
 public static boolean supported(Object value){return value instanceof String||value instanceof Boolean||value instanceof Integer||value instanceof Long||value instanceof Double||value instanceof Enum<?>||value instanceof List<?> list&&list.stream().allMatch(ConfigPresentation::supported);}
 public static Component unit(ConfigCatalog.Definition definition){String unit=ConfigSemantics.details(definition).unit(),key="config.sbwdroneconfig.unit."+unit;return ConfigEnglish.has(key)?ConfigEnglish.text(key):Component.literal(unit);}
 public static Component formatted(ConfigCatalog.Definition definition,Object value){if(value instanceof Enum<?> choice){String key="config.sbwdroneconfig.choice."+choice.getDeclaringClass().getName().replace('$','.')+"."+choice.name();if(ConfigEnglish.has(key))return ConfigEnglish.text(key);}return Component.literal(ConfigWire.text(value));}
 public static Component dependencies(ConfigCatalog.Definition definition){var text=Component.empty();for(String id:ConfigSemantics.details(definition).requires()){var other=ConfigCatalog.find(id);if(other!=null){if(!text.getString().isEmpty())text.append(", ");text.append(name(other));}}return text;}
 public static String reloadKey(ConfigCatalog.Definition definition){if(definition.scope().name().equals("STARTUP"))return "game_restart";String activation=ConfigSemantics.details(definition).activation();if(activation.equals("QUEUED"))return "queued_live";if(activation.equals("NEW_OBJECTS"))return "new_objects";if(activation.equals("GAME_RESTART"))return "game_restart";if(activation.equals("WORLD_RELOAD"))return "world_restart";return switch(definition.nativeRestart()){case "NONE"->"live";case "GAME"->"game_restart";default->"world_restart";};}
}
