package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import java.util.*;
import java.util.function.Function;

/** Queries registered loader objects once when the browser opens. Never scans config directories. */
public final class OtherModsConfigAccess {
 public record Entry(String id,String name,IModInfo info,List<ModConfig> configs,Function<Screen,Screen> ownScreen){public Entry{configs=List.copyOf(configs);}}
 private OtherModsConfigAccess(){}
 public static boolean nativeAllowed(Entry entry){var minecraft=Minecraft.getInstance();return entry.ownScreen()!=null&&(minecraft.getConnection()==null||minecraft.getSingleplayerServer()!=null||!entry.configs().isEmpty()&&entry.configs().stream().allMatch(config->config.getType()==ModConfig.Type.CLIENT));}
 public static Screen nativeScreen(Entry entry,Screen parent){if(!nativeAllowed(entry))throw new IllegalStateException("Use the read-only registered-file view for remote shared/server configurations");var screen=entry.ownScreen().apply(parent);if(screen==null)throw new IllegalStateException("Mod returned no configuration screen");ConfigNativeReturn.begin(parent);return screen;}
 private static Collection<ModConfig> registered(){var out=new ArrayList<ModConfig>();for(var set:ConfigTracker.INSTANCE.configSets().values())out.addAll(set);return List.copyOf(out);}
 public static List<Entry> entries(){
  var byMod=new HashMap<String,List<ModConfig>>();for(var c:registered())byMod.computeIfAbsent(c.getModId(),s->new ArrayList<>()).add(c);
  var result=new ArrayList<Entry>();for(var info:ModList.get().getMods()){
   if(info.getModId().equals(SbwDroneRangeConfig.MOD_ID))continue;
   Function<Screen,Screen> factory=ConfigScreenHandler.getScreenFactoryFor(info).<Function<Screen,Screen>>map(f->parent->f.apply(Minecraft.getInstance(),parent)).orElse(null);
   var configs=byMod.getOrDefault(info.getModId(),List.of());if(factory!=null||!configs.isEmpty())result.add(new Entry(info.getModId(),info.getDisplayName(),info,configs,factory));
  }
  result.sort(Comparator.comparing(Entry::name,String.CASE_INSENSITIVE_ORDER));return List.copyOf(result);
 }
}
