package nl.smartstreamlabs.sbwdroneconfig;
import java.util.Set;
/** Retained file/API compatibility fields with no runtime consumer in any maintained implementation. */
public final class ConfigAvailability {
 private static final boolean PORTABLE_DETECTOR_HUD_PARAMETERS=false;
 private static final Set<String> RETIRED=Set.of(
  "antiDroneNetPanelRequiresSupport",
  "antiDroneNetPanelTriggerThickness",
  "antiDroneNetPanelVisualThickness",
  "cagePitch",
  "cageRoll",
  "cageScale",
  "cageYaw",
  "debugDroneChunkLoading",
  "debugDroneRadar",
  "debugRadarTowerMount",
  "droneProactiveLoadRadius",
  "droneRadarCompactMode",
  "droneRadarDetectionRange",
  "droneRadarHudScale",
  "droneRadarHudXOffset",
  "droneRadarHudYOffset",
  "droneRadarMaxBeepIntervalTicks",
  "droneRadarMinBeepIntervalTicks",
  "droneRadarOnlyBeepWhenHeld",
  "enableFpvFisheyeEffect",
  "fiberCableSegmentSpacing",
  "fiberCableTargetIndicator",
  "fpvPayloadGrenadeScale",
  "fpvPayloadSwingDamping",
  "fpvPayloadSwingMaxAngle",
  "jammerMode",
  "lucasCanHover",
  "maxFiberCableSegmentsPerDrone",
  "rainVisibilityMultiplier",
  "renderLegacyFiberLine",
  "roofAnchorX",
  "roofAnchorY",
  "roofAnchorZ",
  "solarChargeRate",
  "stormInstabilityStrength",
  "thermalHighlightPlayers",
  "thermalVisionHighlightAnimals",
  "thermalVisionHighlightHostileMobs",
  "thermalVisionNoiseEffect",
  "thermalVisionRange",
  "vehicleAntiDroneNetHorizontalPadding",
  "vehicleAntiDroneNetOpacity",
  "vehicleAntiDroneNetTopPadding"
 );
 private ConfigAvailability(){}
 public static boolean activeKey(String key){return !RETIRED.contains(key)&&(PORTABLE_DETECTOR_HUD_PARAMETERS||!Set.of("portableFpvDetectorHudEnabled","portableFpvDetectorShowSignalPercent").contains(key));}
 public static boolean active(ConfigCatalog.Definition definition){return !definition.family().equals("AddonConfig")||activeKey(definition.path().get(definition.path().size()-1));}
 public static String reason(ConfigCatalog.Definition definition){return active(definition)?"":"Legacy compatibility value: no runtime consumer in the maintained implementation";}
}
