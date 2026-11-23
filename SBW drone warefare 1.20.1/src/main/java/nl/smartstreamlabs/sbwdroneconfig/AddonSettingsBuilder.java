package nl.smartstreamlabs.sbwdroneconfig;
import net.minecraftforge.common.ForgeConfigSpec;
import java.util.*;
import java.util.function.Predicate;

/** One definition per legacy setting, routed into its real client/server native spec. The GUI reads those specs. */
public final class AddonSettingsBuilder {
 private static final Set<String> CLIENT=Set.of(
  "hidePlayerOnMinimapDuringDroneControl","showDroneMarkerInstead","lucasHudScale","enableThermalVision","thermalVisionRange","thermalVisionKeybindEnabled","thermalVisionNoiseEffect","thermalHighlightPlayers","thermalVisionHighlightHostileMobs","thermalVisionHighlightAnimals",
  "droneRadarBeepEnabled","droneRadarBeepVolume","droneRadarMinBeepIntervalTicks","droneRadarMaxBeepIntervalTicks","droneRadarOnlyBeepWhenHeld","droneRadarHudScale","droneRadarHudXOffset","droneRadarHudYOffset","droneRadarCompactMode","debugDroneRadar",
  "portableFpvDetectorHudEnabled","portableFpvDetectorShowSignalPercent","antiDroneNetPanelVisualThickness","vehicleAntiDroneNetOpacity",
  "fiberCableMaxRenderDistance","fiberCableVisualThickness","fiberCableVisualAlpha","fiberCableVisualColor","maxVisibleFiberCables","fiberRouteCornerSmoothing","fiberCableTargetIndicator","fiberCableDebugVisualThickness","debugFiberCableRender","renderRedDebugCable","renderLegacyFiberLine",
  "enableDroneScreenshotSound","rainVisibilityMultiplier","fpvCameraFov","fpvCameraTiltDegrees","enableFpvFisheyeEffect","enableFpvCameraVibration",
  "droneEngineVolumeMultiplier","lucasEngineVolumeMultiplier","droneMonitorHumVolumeMultiplier","droneAirborneVolumeMultiplier","cubedFpvExternalVolume","cubedFpvNearVolume","cubedFpvMidVolume","cubedFpvFarVolume","lucasExternalVolume","lucasNearVolume","lucasMidVolume","lucasFarVolume","fpvInternalAudioGain","droneDopplerEnabled","droneDopplerStrength","droneAudioOcclusion","maxAudibleCubedFpvDrones","maxAudibleLucasDrones",
  "debugPayloadMounts","fpvPayloadGrenadeScale","fpvPayloadGrenadeRotationPitch","fpvPayloadGrenadeRotationRoll","fpvPayloadGrenadeRotationYaw","fpvPayloadSwingMaxAngle","fpvPayloadSwingDamping"
 );
 private final ForgeConfigSpec.Builder server=ConfigSchema.builder(),client=ConfigSchema.builder();
 private final Set<ForgeConfigSpec.ConfigValue<?>> locals=Collections.newSetFromMap(new IdentityHashMap<>());
 private String[] comments;
 public AddonSettingsBuilder comment(String... lines){comments=lines.clone();return this;}
 public AddonSettingsBuilder push(String path){if(comments!=null){server.comment(comments);client.comment(comments);comments=null;}server.push(path);client.push(path);return this;}
 public AddonSettingsBuilder pop(){server.pop();client.pop();return this;}
 private ForgeConfigSpec.Builder choose(String key){var b=CLIENT.contains(key)?client:server;if(!ConfigAvailability.activeKey(key)){var text=new ArrayList<String>();if(comments!=null)Collections.addAll(text,comments);text.add("Legacy compatibility value (inactive): retained for old configurations; this implementation does not consume it.");comments=text.toArray(String[]::new);}if(comments!=null){b.comment(comments);comments=null;}return b;}
 private <T extends ForgeConfigSpec.ConfigValue<?>> T track(String key,T value){if(CLIENT.contains(key))locals.add(value);return value;}
 public ForgeConfigSpec.BooleanValue define(String key,boolean value){return track(key,choose(key).define(key,value));}
 public <T> ForgeConfigSpec.ConfigValue<T> define(String key,T value){return track(key,choose(key).define(key,value));}
 public <T> ForgeConfigSpec.ConfigValue<T> define(String key,T value,Predicate<Object> valid){return track(key,choose(key).define(key,value,valid));}
 public ForgeConfigSpec.IntValue defineInRange(String key,int value,int min,int max){return track(key,choose(key).defineInRange(key,value,min,max));}
 public ForgeConfigSpec.LongValue defineInRange(String key,long value,long min,long max){return track(key,choose(key).defineInRange(key,value,min,max));}
 public ForgeConfigSpec.DoubleValue defineInRange(String key,double value,double min,double max){return track(key,choose(key).defineInRange(key,value,min,max));}
 public <T extends Enum<T>> ForgeConfigSpec.EnumValue<T> defineEnum(String key,T value){return track(key,choose(key).defineEnum(key,value));}
 public <T> ForgeConfigSpec.ConfigValue<List<? extends T>> defineListAllowEmpty(String key,List<? extends T> value,Predicate<Object> valid){return track(key,choose(key).defineListAllowEmpty(key,value,valid));}
 public ForgeConfigSpec build(){return server.build();}
 public ForgeConfigSpec buildClient(){return client.build();}
 public Set<ForgeConfigSpec.ConfigValue<?>> localValues(){return Set.copyOf(locals);}
}
