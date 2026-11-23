package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.network.chat.Component;
/** The configuration UI intentionally stays English regardless of Minecraft's selected language. */
public final class ConfigEnglish {
 private static final Map<String,String> TEXT=load();
 private ConfigEnglish(){}
 private static Map<String,String> load(){
  try(var stream=ConfigEnglish.class.getResourceAsStream("/assets/sbwdroneconfig/lang/en_us.json")){
   if(stream==null)throw new IllegalStateException("Missing bundled English configuration labels");
   var json=JsonParser.parseReader(new java.io.InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonObject();var result=new HashMap<String,String>();for(var entry:json.entrySet())result.put(entry.getKey(),entry.getValue().getAsString());return Map.copyOf(result);
  }catch(Exception error){throw new IllegalStateException("Invalid bundled English configuration labels",error);}
 }
 public static boolean has(String key){return TEXT.containsKey(key);}
 public static Component text(String key,Object... arguments){String pattern=TEXT.getOrDefault(key,key);if(arguments.length==0)return Component.literal(pattern);Object[] values=Arrays.stream(arguments).map(value->value instanceof Component component?component.getString():value).toArray();try{return Component.literal(String.format(Locale.ROOT,pattern,values));}catch(IllegalFormatException error){return Component.literal(pattern);}}
}
