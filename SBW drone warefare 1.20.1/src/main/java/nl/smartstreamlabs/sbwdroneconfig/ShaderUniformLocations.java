package nl.smartstreamlabs.sbwdroneconfig;
import java.util.HashMap;
/** Locations belong to one linked GL program. Includes valid missing-uniform (-1) results. */
public final class ShaderUniformLocations {
 private int program=-1;private final HashMap<String,Integer> locations=new HashMap<>();
 public Integer lookup(int id,String name){select(id);return locations.get(name);}
 public void remember(int id,String name,int location){select(id);locations.put(name,location);}
 private void select(int id){if(program!=id){locations.clear();program=id;}}
 public void clear(){locations.clear();program=-1;}
}
