package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** Tracks loader-owned files without relying on private ConfigTracker maps. */
@Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ConfigLifecycle {
 private static final Map<ForgeConfigSpec,ModConfig> LOADED=new IdentityHashMap<>();
 private static final Map<ForgeConfigSpec,Map<List<String>,Object>> FINGERPRINTS=new IdentityHashMap<>();
 private static final AtomicLong REVISION=new AtomicLong();
 private static final Map<ForgeConfigSpec,Long> FILE_REVISIONS=new IdentityHashMap<>();
 public static final Object COMMIT_LOCK=new Object();
 private ConfigLifecycle(){}
 @SubscribeEvent public static void loading(ModConfigEvent.Loading event){changed(event.getConfig());}
 @SubscribeEvent public static void reloading(ModConfigEvent.Reloading event){changed(event.getConfig());}
 @SubscribeEvent public static void unloading(ModConfigEvent.Unloading event){var config=event.getConfig();if(!config.getModId().equals(SbwDroneRangeConfig.MOD_ID))return;synchronized(COMMIT_LOCK){synchronized(LOADED){LOADED.remove(config.getSpec());FINGERPRINTS.remove(config.getSpec());if(config.getSpec() instanceof ForgeConfigSpec s)FILE_REVISIONS.remove(s);REVISION.incrementAndGet();}}}
 private static void changed(ModConfig config){if(!config.getModId().equals(SbwDroneRangeConfig.MOD_ID))return;if(config.getSpec() instanceof ForgeConfigSpec spec){synchronized(COMMIT_LOCK){var state=new HashMap<List<String>,Object>();flatten(config.getConfigData(),List.of(),state);synchronized(LOADED){var previous=LOADED.put(spec,config);var old=FINGERPRINTS.put(spec,Map.copyOf(state));if(previous!=config||!Objects.equals(old,state)){long revision=REVISION.incrementAndGet();FILE_REVISIONS.put(spec,revision);}}}}}
 private static void flatten(com.electronwill.nightconfig.core.UnmodifiableConfig node,List<String> parent,Map<List<String>,Object> out){for(var e:node.entrySet()){var path=new ArrayList<>(parent);path.add(e.getKey());Object v=e.getValue();if(v instanceof com.electronwill.nightconfig.core.UnmodifiableConfig child)flatten(child,path,out);else out.put(List.copyOf(path),v instanceof List<?> list?List.copyOf(list):v);}}
 public static long revision(){return REVISION.get();}
 public static long revision(ForgeConfigSpec owner){synchronized(LOADED){return FILE_REVISIONS.getOrDefault(owner,0L);}}
 public static ModConfig config(ForgeConfigSpec spec){synchronized(LOADED){return LOADED.get(spec);}}
}
