package nl.smartstreamlabs.sbwdroneconfig;
import com.google.gson.*;
import java.util.*;

/** Bounded transport, separate from the native numeric ranges (including exact 64-bit capacities). */
public final class ConfigWire {
 public static final int PIECE=4096,MAX_PARTS=256;
 private ConfigWire(){}
 public static String text(Object value){return value instanceof List<?>?new Gson().toJson(value):value instanceof Enum<?> e?e.name():value.toString();}
 public static List<String> chunks(String text){var out=new ArrayList<String>();for(int i=0;i<text.length();i+=PIECE)out.add(text.substring(i,Math.min(text.length(),i+PIECE)));if(out.isEmpty())out.add("");if(out.size()>MAX_PARTS)throw new IllegalArgumentException("Configuration transaction exceeds the network transfer budget; edit the server file instead");return List.copyOf(out);}
 public static Map<String,String> strings(JsonObject object){var values=new LinkedHashMap<String,String>();if(object.size()>2048)throw new IllegalArgumentException("Too many setting IDs");for(var e:object.entrySet()){if(e.getKey().length()>256||!e.getValue().isJsonPrimitive()||!e.getValue().getAsJsonPrimitive().isString())throw new IllegalArgumentException("Expected known setting IDs with string values");values.put(e.getKey(),e.getValue().getAsString());}return values;}
 public static String snapshot(List<ConfigCatalog.Definition> definitions){var out=new JsonObject();for(var d:definitions)if(!d.managed()&&ConfigAvailability.active(d))out.addProperty(d.id(),text(d.current()));return out.toString();}
 public static final class Assembly {
  private final String[] pieces;private int size;private final long created;
  public Assembly(int count,long created){if(count<1||count>MAX_PARTS)throw new IllegalArgumentException("Invalid config part count");pieces=new String[count];this.created=created;}
  public boolean expired(long now){return now-created>200;}
  public void add(int index,int count,String value){if(count!=pieces.length||index<0||index>=pieces.length||value.length()>PIECE)throw new IllegalArgumentException("Invalid config transfer part");if(pieces[index]!=null&&!pieces[index].equals(value))throw new IllegalArgumentException("Conflicting duplicate config part");if(pieces[index]==null){pieces[index]=value;size++;}}
  public boolean complete(){return size==pieces.length;}
  public String finish(){if(!complete())throw new IllegalStateException("Incomplete transfer");return String.join("",pieces);}
 }
}
