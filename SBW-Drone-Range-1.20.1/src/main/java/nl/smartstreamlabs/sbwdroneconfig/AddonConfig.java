package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.config.ModConfigEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class AddonConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue DRONE_MAX_RANGE_VALUE;
    public static final ModConfigSpec.IntValue DRONE_VISUAL_TRACKING_RANGE_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_SPEED_MULTIPLIER_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_CHUNK_LOADING_VALUE;
    public static final ModConfigSpec.IntValue DRONE_CHUNK_LOAD_RADIUS_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_PLAYER_ANCHOR_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE;
    public static final ModConfigSpec.BooleanValue HIDE_PLAYER_ON_MINIMAP_DURING_DRONE_CONTROL_VALUE;
    public static final ModConfigSpec.BooleanValue SHOW_DRONE_MARKER_INSTEAD_VALUE;
    public static final ModConfigSpec.IntValue LUCAS_TAKEOFF_ASSIST_TICKS_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_TAKEOFF_FORWARD_BOOST_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_TAKEOFF_UP_BOOST_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_MIN_SPEED_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_MAX_SPEED_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_ACCELERATION_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_DRAG_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_TURN_RATE_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_PITCH_RATE_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_CLIMB_SPEED_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_DESCEND_SPEED_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_LIFT_STRENGTH_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_ROLL_VISUAL_AMOUNT_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_MIN_LIFT_SPEED_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_STALL_GRAVITY_VALUE;
    public static final ModConfigSpec.BooleanValue LUCAS_STALL_ENABLED_VALUE;
    public static final ModConfigSpec.BooleanValue LUCAS_CAN_HOVER_VALUE;
    public static final ModConfigSpec.IntValue LUCAS_MAX_FUEL_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_IDLE_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_FLYING_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_CLIMB_VALUE;
    public static final ModConfigSpec.IntValue LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE;
    public static final ModConfigSpec.BooleanValue LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_HUD_SCALE_VALUE;
    public static final ModConfigSpec.IntValue FUEL_MIXER_PROCESS_TIME_VALUE;
    public static final ModConfigSpec.IntValue GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_THERMAL_VISION_VALUE;
    public static final ModConfigSpec.IntValue THERMAL_VISION_RANGE_VALUE;
    public static final ModConfigSpec.BooleanValue THERMAL_VISION_KEYBIND_ENABLED_VALUE;
    public static final ModConfigSpec.BooleanValue THERMAL_VISION_NOISE_EFFECT_VALUE;
    public static final ModConfigSpec.BooleanValue THERMAL_HIGHLIGHT_PLAYERS_VALUE;
    public static final ModConfigSpec.BooleanValue THERMAL_HIGHLIGHT_HOSTILE_MOBS_VALUE;
    public static final ModConfigSpec.BooleanValue THERMAL_HIGHLIGHT_ANIMALS_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_BATTERY_SYSTEM_VALUE;
    public static final ModConfigSpec.IntValue MAX_ENERGY_VALUE;
    public static final ModConfigSpec.IntValue ENERGY_USE_PER_TICK_VALUE;
    public static final ModConfigSpec.IntValue IDLE_ENERGY_USE_PER_TICK_VALUE;
    public static final ModConfigSpec.IntValue LOW_ENERGY_THRESHOLD_VALUE;
    public static final ModConfigSpec.BooleanValue RETURN_HOME_ON_EMPTY_VALUE;
    public static final ModConfigSpec.IntValue BATTERY_TRANSFER_RATE_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_SIREN_VALUE;
    public static final ModConfigSpec.IntValue DRONE_SIREN_RANGE_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_SIREN_REDSTONE_OUTPUT_VALUE;
    public static final ModConfigSpec.IntValue DRONE_SIREN_COOLDOWN_TICKS_VALUE;
    public static final ModConfigSpec.BooleanValue DETECT_FRIENDLY_DRONES_VALUE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_OWNER_NAMES_VALUE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_OWNER_UUIDS_VALUE;
    public static final ModConfigSpec.BooleanValue IGNORE_OWNERLESS_DRONES_VALUE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_NAMES_VALUE;
    public static final ModConfigSpec.BooleanValue IGNORE_UNNAMED_DRONES_VALUE;
    public static final ModConfigSpec.BooleanValue BLACKLIST_MATCH_CASE_INSENSITIVE_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_JAMMER_VALUE;
    public static final ModConfigSpec.IntValue JAMMER_RANGE_VALUE;
    public static final ModConfigSpec.IntValue JAMMER_BUILD_UP_TICKS_VALUE;
    public static final ModConfigSpec.IntValue JAMMER_RECOVERY_TICKS_VALUE;
    public static final ModConfigSpec.DoubleValue HARD_JAM_THRESHOLD_VALUE;
    public static final ModConfigSpec.DoubleValue CONTROL_INTERFERENCE_THRESHOLD_VALUE;
    public static final ModConfigSpec.DoubleValue OVERLAY_START_THRESHOLD_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_JAMMER_VALUE;
    public static final ModConfigSpec.EnumValue<DroneJammerMode> JAMMER_MODE_VALUE;
    public static final ModConfigSpec.BooleanValue AFFECT_FRIENDLY_DRONES_VALUE;
    public static final ModConfigSpec.IntValue DRONE_RADAR_DETECTION_RANGE_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_RADAR_BEEP_ENABLED_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_RADAR_BEEP_VOLUME_VALUE;
    public static final ModConfigSpec.IntValue DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE;
    public static final ModConfigSpec.IntValue DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_RADAR_HUD_SCALE_VALUE;
    public static final ModConfigSpec.IntValue DRONE_RADAR_HUD_X_OFFSET_VALUE;
    public static final ModConfigSpec.IntValue DRONE_RADAR_HUD_Y_OFFSET_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_RADAR_COMPACT_MODE_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_DRONE_RADAR_VALUE;
    public static final ModConfigSpec.IntValue PORTABLE_FPV_DETECTOR_RANGE_VALUE;
    public static final ModConfigSpec.IntValue PORTABLE_FPV_DETECTOR_SCAN_INTERVAL_TICKS_VALUE;
    public static final ModConfigSpec.IntValue PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE;
    public static final ModConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE;
    public static final ModConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE;
    public static final ModConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE;
    public static final ModConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE;
    public static final ModConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE;
    public static final ModConfigSpec.IntValue LASER_DEFENSE_TOWER_DRONE_RANGE_VALUE;
    public static final ModConfigSpec.BooleanValue LASER_DEFENSE_TOWER_WHITELIST_ENABLED_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_FIBER_OPTIC_MODE_VALUE;
    public static final ModConfigSpec.IntValue FIBER_OPTIC_CABLE_LENGTH_VALUE;
    public static final ModConfigSpec.IntValue FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE;
    public static final ModConfigSpec.BooleanValue NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE;
    public static final ModConfigSpec.BooleanValue ANTI_DRONE_NET_CUTS_FIBER_VALUE;
    public static final ModConfigSpec.BooleanValue ANTI_DRONE_NET_PANEL_REQUIRES_SUPPORT_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_DRONE_NET_PANEL_TRIGGER_THICKNESS_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_DRONE_NET_PANEL_VISUAL_THICKNESS_VALUE;
    public static final ModConfigSpec.BooleanValue VEHICLE_ANTI_DRONE_NET_ENABLED_VALUE;
    public static final ModConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE;
    public static final ModConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE;
    public static final ModConfigSpec.BooleanValue VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE;
    public static final ModConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE;
    public static final ModConfigSpec.BooleanValue NET_BULK_PLACEMENT_ENABLED_VALUE;
    public static final ModConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_WIDTH_VALUE;
    public static final ModConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_HEIGHT_VALUE;
    public static final ModConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_BLOCKS_VALUE;
    public static final ModConfigSpec.DoubleValue NET_BULK_PLACEMENT_MAXIMUM_REACH_VALUE;
    public static final ModConfigSpec.BooleanValue NET_BULK_PLACEMENT_ALL_OR_NOTHING_VALUE;
    public static final ModConfigSpec.BooleanValue NET_BULK_PLACEMENT_SKIP_BLOCKED_VALUE;
    public static final ModConfigSpec.IntValue NET_BULK_PLACEMENT_BLOCKS_PER_TICK_VALUE;
    public static final ModConfigSpec.IntValue NET_BULK_PLACEMENT_UNDO_SECONDS_VALUE;
    public static final ModConfigSpec.BooleanValue KEEP_DETACHED_FIBER_CABLE_ON_DRONE_DEATH_VALUE;
    public static final ModConfigSpec.IntValue DETACHED_FIBER_CABLE_LIFETIME_TICKS_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_FIBER_OPTIC_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_VISUAL_THICKNESS_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_VISUAL_ALPHA_VALUE;
    public static final ModConfigSpec.ConfigValue<String> FIBER_CABLE_VISUAL_COLOR_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_MAX_RENDER_DISTANCE_VALUE;
    public static final ModConfigSpec.IntValue MAX_VISIBLE_FIBER_CABLES_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_ROUTE_HISTORY_ENABLED_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_ROUTE_POINT_SPACING_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_ROUTE_MINIMUM_TURN_DISTANCE_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_ROUTE_TURN_ANGLE_THRESHOLD_VALUE;
    public static final ModConfigSpec.IntValue FIBER_ROUTE_MAXIMUM_POINTS_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_ROUTE_CORNER_SMOOTHING_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_ROUTE_DEBUG_LOGGING_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_CUT_BY_OWNER_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_CUT_BY_OTHER_PLAYERS_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CUT_REQUIRES_SHEARS_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_CUT_REACH_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_INTERACTION_RADIUS_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CUT_LINE_OF_SIGHT_REQUIRED_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_TARGET_INDICATOR_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_DEBUG_VISUAL_THICKNESS_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_SPACING_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_HEALTH_VALUE;
    public static final ModConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_HITBOX_SIZE_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_PLAYERS_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_PROJECTILES_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_EXPLOSIONS_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_PROJECTILE_RAYCAST_BREAK_VALUE;
    public static final ModConfigSpec.BooleanValue FIBER_CABLE_BREAKS_WHEN_ANY_SEGMENT_DESTROYED_VALUE;
    public static final ModConfigSpec.IntValue MAX_FIBER_CABLE_SEGMENTS_PER_DRONE_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_FIBER_CABLE_RENDER_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_FIBER_CABLE_SEGMENTS_VALUE;
    public static final ModConfigSpec.BooleanValue RENDER_RED_DEBUG_CABLE_VALUE;
    public static final ModConfigSpec.BooleanValue RENDER_LEGACY_FIBER_LINE_VALUE;
    public static final ModConfigSpec.BooleanValue WEATHER_EFFECTS_ENABLED_VALUE;
    public static final ModConfigSpec.DoubleValue RAIN_VISIBILITY_MULTIPLIER_VALUE;
    public static final ModConfigSpec.DoubleValue STORM_INSTABILITY_STRENGTH_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_DRONE_SCREENSHOT_SOUND_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE;
    public static final ModConfigSpec.DoubleValue CUBED_FPV_EXTERNAL_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue CUBED_FPV_NEAR_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue CUBED_FPV_MID_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue CUBED_FPV_FAR_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_EXTERNAL_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_NEAR_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_MID_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue LUCAS_FAR_VOLUME_VALUE;
    public static final ModConfigSpec.DoubleValue FPV_INTERNAL_AUDIO_GAIN_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_DOPPLER_ENABLED_VALUE;
    public static final ModConfigSpec.DoubleValue DRONE_DOPPLER_STRENGTH_VALUE;
    public static final ModConfigSpec.BooleanValue DRONE_AUDIO_OCCLUSION_VALUE;
    public static final ModConfigSpec.IntValue MAX_AUDIBLE_CUBED_FPV_DRONES_VALUE;
    public static final ModConfigSpec.IntValue MAX_AUDIBLE_LUCAS_DRONES_VALUE;
    public static final ModConfigSpec.DoubleValue FPV_CAMERA_FOV_VALUE;
    public static final ModConfigSpec.DoubleValue FPV_CAMERA_TILT_DEGREES_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_FPV_FISHEYE_EFFECT_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_FPV_CAMERA_VIBRATION_VALUE;
    public static final ModConfigSpec.BooleanValue ENABLE_SPOTLIGHT_MODULE_VALUE;
    public static final ModConfigSpec.IntValue SPOTLIGHT_RANGE_VALUE;
    public static final ModConfigSpec.IntValue SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE;
    public static final ModConfigSpec.BooleanValue SPOTLIGHT_REQUIRES_MODULE_VALUE;
    public static final ModConfigSpec.BooleanValue SPOTLIGHT_VISIBLE_TO_OTHER_PLAYERS_VALUE;
    public static final ModConfigSpec.BooleanValue SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE;
    public static final ModConfigSpec.BooleanValue SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE;
    public static final ModConfigSpec.IntValue SPOTLIGHT_LIGHT_RANGE_VALUE;
    public static final ModConfigSpec.IntValue SPOTLIGHT_LIGHT_LEVEL_VALUE;
    public static final ModConfigSpec.IntValue SPOTLIGHT_LIGHT_UPDATE_INTERVAL_TICKS_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_RADAR_TOWER_MOUNT_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_ROOF_ANCHOR_X_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_ROOF_ANCHOR_Y_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_ROOF_ANCHOR_Z_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_CAGE_YAW_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_CAGE_PITCH_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_CAGE_ROLL_VALUE;
    public static final ModConfigSpec.DoubleValue RADAR_TOWER_CAGE_SCALE_VALUE;
    public static final ModConfigSpec.IntValue RADAR_TOWER_MAX_BATTERY_CAPACITY_VALUE;
    public static final ModConfigSpec.IntValue RADAR_TOWER_SOLAR_CHARGE_RATE_VALUE;
    public static final ModConfigSpec.IntValue RADAR_TOWER_CHARGING_STATION_CHARGE_RATE_VALUE;
    public static final ModConfigSpec.IntValue RADAR_TOWER_JAMMER_ENERGY_DRAIN_RATE_VALUE;
    public static final ModConfigSpec.IntValue RADAR_TOWER_MINIMUM_ENERGY_TO_ACTIVATE_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_VEHICLE_MORTAR_VEHICLE_DAMAGE_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_VEHICLE_MORTAR_ENTITY_DAMAGE_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_VEHICLE_MORTAR_EXPLOSION_RADIUS_VALUE;
    public static final ModConfigSpec.DoubleValue ANTI_VEHICLE_MORTAR_BLOCK_DAMAGE_RADIUS_VALUE;
    public static final ModConfigSpec.IntValue ANTI_VEHICLE_MORTAR_PAYLOAD_COOLDOWN_TICKS_VALUE;
    public static final ModConfigSpec.BooleanValue DEBUG_PAYLOAD_MOUNTS_VALUE;

    private static volatile int droneMaxRange;
    private static volatile int droneVisualTrackingRange;
    private static volatile double droneSpeedMultiplier;
    private static volatile boolean enableDroneChunkLoading;
    private static volatile int droneChunkLoadRadius;
    private static volatile boolean enableDronePlayerAnchor;
    private static volatile boolean enableDroneCrashExplosionDamage;
    private static volatile boolean hidePlayerOnMinimapDuringDroneControl;
    private static volatile boolean showDroneMarkerInstead;
    private static volatile int lucasTakeoffAssistTicks;
    private static volatile double lucasTakeoffForwardBoost;
    private static volatile double lucasTakeoffUpBoost;
    private static volatile double lucasMinSpeed;
    private static volatile double lucasMaxSpeed;
    private static volatile double lucasAcceleration;
    private static volatile double lucasDrag;
    private static volatile double lucasTurnRate;
    private static volatile double lucasPitchRate;
    private static volatile double lucasClimbSpeed;
    private static volatile double lucasDescendSpeed;
    private static volatile double lucasLiftStrength;
    private static volatile double lucasRollVisualAmount;
    private static volatile double lucasMinLiftSpeed;
    private static volatile double lucasStallGravity;
    private static volatile boolean lucasStallEnabled;
    private static volatile boolean lucasCanHover;
    private static volatile int lucasMaxFuel;
    private static volatile double lucasFuelDrainIdle;
    private static volatile double lucasFuelDrainFlying;
    private static volatile double lucasFuelDrainClimb;
    private static volatile int lucasLowFuelWarningPercent;
    private static volatile boolean lucasCanFlyWithoutFuel;
    private static volatile double lucasHudScale;
    private static volatile int fuelMixerProcessTime;
    private static volatile int gasolineJerrycanFuelAmount;
    private static volatile boolean enableThermalVision;
    private static volatile int thermalVisionRange;
    private static volatile boolean thermalVisionKeybindEnabled;
    private static volatile boolean thermalVisionNoiseEffect;
    private static volatile boolean thermalHighlightPlayers;
    private static volatile boolean thermalHighlightHostileMobs;
    private static volatile boolean thermalHighlightAnimals;
    private static volatile boolean enableBatterySystem;
    private static volatile int maxEnergy;
    private static volatile int energyUsePerTick;
    private static volatile int idleEnergyUsePerTick;
    private static volatile int lowEnergyThreshold;
    private static volatile boolean returnHomeOnEmpty;
    private static volatile int batteryTransferRate;
    private static volatile boolean enableDroneSiren;
    private static volatile int droneSirenRange;
    private static volatile boolean droneSirenRedstoneOutput;
    private static volatile int droneSirenCooldownTicks;
    private static volatile boolean detectFriendlyDrones;
    private static volatile Set<String> ignoredDroneOwnerNames = Set.of();
    private static volatile Set<UUID> ignoredDroneOwnerUUIDs = Set.of();
    private static volatile boolean ignoreOwnerlessDrones;
    private static volatile Set<String> ignoredDroneNames = Set.of();
    private static volatile boolean ignoreUnnamedDrones;
    private static volatile boolean blacklistMatchCaseInsensitive;
    private static volatile boolean enableDroneJammer;
    private static volatile int jammerRange;
    private static volatile int jammerBuildUpTicks;
    private static volatile int jammerRecoveryTicks;
    private static volatile double hardJamThreshold;
    private static volatile double controlInterferenceThreshold;
    private static volatile double overlayStartThreshold;
    private static volatile boolean debugJammer;
    private static volatile DroneJammerMode jammerMode;
    private static volatile boolean affectFriendlyDrones;
    private static volatile int droneRadarDetectionRange;
    private static volatile boolean droneRadarBeepEnabled;
    private static volatile double droneRadarBeepVolume;
    private static volatile int droneRadarMinBeepIntervalTicks;
    private static volatile int droneRadarMaxBeepIntervalTicks;
    private static volatile boolean droneRadarOnlyBeepWhenHeld;
    private static volatile double droneRadarHudScale;
    private static volatile int droneRadarHudXOffset;
    private static volatile int droneRadarHudYOffset;
    private static volatile boolean droneRadarCompactMode;
    private static volatile boolean debugDroneRadar;
    private static volatile int portableFpvDetectorRange;
    private static volatile int portableFpvDetectorScanIntervalTicks;
    private static volatile int portableFpvDetectorMaximumTargets;
    private static volatile boolean portableFpvDetectorVideoEnabled;
    private static volatile boolean portableFpvDetectorDetectOwnDrones;
    private static volatile boolean portableFpvDetectorRequireLineOfSight;
    private static volatile boolean portableFpvDetectorHudEnabled;
    private static volatile boolean portableFpvDetectorShowSignalPercent;
    private static volatile int laserDefenseTowerDroneRange;
    private static volatile boolean laserDefenseTowerWhitelistEnabled;
    private static volatile boolean enableFiberOpticMode;
    private static volatile int fiberOpticCableLength;
    private static volatile int fiberOpticCableBreakDelayTicks;
    private static volatile double fiberOpticExtraBatteryDrainMultiplier;
    private static volatile boolean normalJammersAffectFiberOptic;
    private static volatile boolean antiDroneNetCutsFiber;
    private static volatile boolean antiDroneNetPanelRequiresSupport;
    private static volatile double antiDroneNetPanelTriggerThickness;
    private static volatile double antiDroneNetPanelVisualThickness;
    private static volatile boolean vehicleAntiDroneNetEnabled;
    private static volatile double vehicleAntiDroneNetHorizontalPadding;
    private static volatile double vehicleAntiDroneNetTopPadding;
    private static volatile boolean vehicleAntiDroneNetStopsDrones;
    private static volatile double vehicleAntiDroneNetOpacity;
    private static volatile boolean netBulkPlacementEnabled;
    private static volatile int netBulkPlacementMaxWidth;
    private static volatile int netBulkPlacementMaxHeight;
    private static volatile int netBulkPlacementMaxBlocks;
    private static volatile double netBulkPlacementMaximumReach;
    private static volatile boolean netBulkPlacementAllOrNothing;
    private static volatile boolean netBulkPlacementSkipBlocked;
    private static volatile int netBulkPlacementBlocksPerTick;
    private static volatile int netBulkPlacementUndoSeconds;
    private static volatile boolean keepDetachedFiberCableOnDroneDeath;
    private static volatile int detachedFiberCableLifetimeTicks;
    private static volatile boolean debugFiberOptic;
    private static volatile double fiberCableVisualThickness;
    private static volatile double fiberCableVisualAlpha;
    private static volatile String fiberCableVisualColor = "charcoal";
    private static volatile double fiberCableMaxRenderDistance;
    private static volatile int maxVisibleFiberCables;
    private static volatile boolean fiberRouteHistoryEnabled = true;
    private static volatile double fiberRoutePointSpacing = 2.5D;
    private static volatile double fiberRouteMinimumTurnDistance = 1.0D;
    private static volatile double fiberRouteTurnAngleThreshold = 18.0D;
    private static volatile int fiberRouteMaximumPoints = 512;
    private static volatile boolean fiberRouteCornerSmoothing = true;
    private static volatile boolean fiberRouteDebugLogging;
    private static volatile boolean fiberCableCanBeCutByOwner = true;
    private static volatile boolean fiberCableCanBeCutByOtherPlayers = true;
    private static volatile boolean fiberCableCutRequiresShears = true;
    private static volatile double fiberCableCutReach = 5.0D;
    private static volatile double fiberCableInteractionRadius = 0.12D;
    private static volatile boolean fiberCableCutLineOfSightRequired = true;
    private static volatile boolean fiberCableTargetIndicator;
    private static volatile double fiberCableDebugVisualThickness;
    private static volatile double fiberCableSegmentSpacing;
    private static volatile double fiberCableSegmentHealth;
    private static volatile double fiberCableSegmentHitboxSize;
    private static volatile boolean fiberCableCanBeDamagedByPlayers;
    private static volatile boolean fiberCableCanBeDamagedByProjectiles;
    private static volatile boolean fiberCableCanBeDamagedByExplosions;
    private static volatile boolean fiberCableProjectileRaycastBreak;
    private static volatile boolean fiberCableBreaksWhenAnySegmentDestroyed;
    private static volatile int maxFiberCableSegmentsPerDrone;
    private static volatile boolean debugFiberCableRender;
    private static volatile boolean debugFiberCableSegments;
    private static volatile boolean renderRedDebugCable;
    private static volatile boolean renderLegacyFiberLine;
    private static volatile boolean weatherEffectsEnabled;
    private static volatile double rainVisibilityMultiplier;
    private static volatile double stormInstabilityStrength;
    private static volatile boolean enableDroneScreenshotSound;
    private static volatile double droneEngineVolumeMultiplier;
    private static volatile double lucasEngineVolumeMultiplier;
    private static volatile double droneMonitorHumVolumeMultiplier;
    private static volatile double droneAirborneVolumeMultiplier;
    private static volatile double cubedFpvExternalVolume = 1.0D;
    private static volatile double cubedFpvNearVolume = 1.0D;
    private static volatile double cubedFpvMidVolume = 0.8D;
    private static volatile double cubedFpvFarVolume = 0.55D;
    private static volatile double lucasExternalVolume = 1.1D;
    private static volatile double lucasNearVolume = 1.0D;
    private static volatile double lucasMidVolume = 0.85D;
    private static volatile double lucasFarVolume = 0.60D;
    private static volatile double fpvInternalAudioGain = 0.65D;
    private static volatile boolean droneDopplerEnabled = true;
    private static volatile double droneDopplerStrength = 0.20D;
    private static volatile boolean droneAudioOcclusion = true;
    private static volatile int maxAudibleCubedFpvDrones = 12;
    private static volatile int maxAudibleLucasDrones = 8;
    private static volatile double fpvCameraFov;
    private static volatile double fpvCameraTiltDegrees;
    private static volatile boolean enableFpvFisheyeEffect;
    private static volatile boolean enableFpvCameraVibration;
    private static volatile boolean enableSpotlightModule;
    private static volatile int spotlightRange;
    private static volatile int spotlightEnergyCostPerSecond;
    private static volatile boolean spotlightRequiresModule;
    private static volatile boolean spotlightVisibleToOtherPlayers;
    private static volatile boolean spotlightAutoOffWhenBatteryEmpty;
    private static volatile boolean spotlightRealLightEnabled;
    private static volatile int spotlightLightRange;
    private static volatile int spotlightLightLevel;
    private static volatile int spotlightLightUpdateIntervalTicks;
    private static volatile boolean debugRadarTowerMount;
    private static volatile double radarTowerRoofAnchorX;
    private static volatile double radarTowerRoofAnchorY;
    private static volatile double radarTowerRoofAnchorZ;
    private static volatile double radarTowerCageYaw;
    private static volatile double radarTowerCagePitch;
    private static volatile double radarTowerCageRoll;
    private static volatile double radarTowerCageScale;
    private static volatile int radarTowerMaxBatteryCapacity;
    private static volatile int radarTowerSolarChargeRate;
    private static volatile int radarTowerChargingStationChargeRate;
    private static volatile int radarTowerJammerEnergyDrainRate;
    private static volatile int radarTowerMinimumEnergyToActivate;
    private static volatile double antiVehicleMortarVehicleDamage;
    private static volatile double antiVehicleMortarEntityDamage;
    private static volatile double antiVehicleMortarExplosionRadius;
    private static volatile double antiVehicleMortarBlockDamageRadius;
    private static volatile int antiVehicleMortarPayloadCooldownTicks;
    private static volatile boolean debugPayloadMounts;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("drone");
        DRONE_MAX_RANGE_VALUE = builder
                .comment("Maximum allowed distance in blocks between the player and a linked Superb Warfare drone.")
                .defineInRange("droneMaxRange", 100, 1, 30000);
        DRONE_VISUAL_TRACKING_RANGE_VALUE = builder
                .comment("Maximum distance in blocks that ordinary nearby players keep a drone entity tracked for visual and audio playback.")
                .defineInRange("droneVisualTrackingRange", 256, 32, 512);
        DRONE_SPEED_MULTIPLIER_VALUE = builder
                .comment("Extra flight speed multiplier applied to FPV drone travel input.")
                .defineInRange("droneSpeedMultiplier", 1.0D,
                        DroneSpeedConfigLimits.MIN_DRONE_SPEED_MULTIPLIER,
                        DroneSpeedConfigLimits.MAX_DRONE_SPEED_MULTIPLIER);
        ENABLE_DRONE_CHUNK_LOADING_VALUE = builder
                .comment("Keep a small area around an actively controlled drone loaded.")
                .define("enableDroneChunkLoading", false);
        DRONE_CHUNK_LOAD_RADIUS_VALUE = builder
                .comment("Chunk radius kept loaded around the drone when chunk loading is enabled.")
                .defineInRange("droneChunkLoadRadius", 1, 0, 4);
        ENABLE_DRONE_PLAYER_ANCHOR_VALUE = builder
                .comment("Move the controlling player with the drone while using the monitor so vanilla chunk loading follows the drone.")
                .define("enableDronePlayerAnchor", true);
        ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE = builder
                .comment("Allow drone crash explosions to damage entities and destroy blocks.")
                .define("enableDroneCrashExplosionDamage", true);
        HIDE_PLAYER_ON_MINIMAP_DURING_DRONE_CONTROL_VALUE = builder
                .comment("Hide the controlling player's normal minimap marker while actively piloting an FPV or LUCAS drone.")
                .define("hidePlayerOnMinimapDuringDroneControl", true);
        SHOW_DRONE_MARKER_INSTEAD_VALUE = builder
                .comment("Prefer showing the active drone as the map target when supported by a minimap integration.")
                .define("showDroneMarkerInstead", true);
        builder.pop();

        builder.push("lucas");
        LUCAS_TAKEOFF_ASSIST_TICKS_VALUE = builder
                .comment("How long the LUCAS drone gets assisted launch thrust after a new control session begins.")
                .defineInRange("lucasTakeoffAssistTicks", 60, 0, 200);
        LUCAS_TAKEOFF_FORWARD_BOOST_VALUE = builder
                .comment("Minimum forward speed injected during the LUCAS drone takeoff assist window.")
                .defineInRange("lucasTakeoffForwardBoost", 1.15D, 0.0D, 4.0D);
        LUCAS_TAKEOFF_UP_BOOST_VALUE = builder
                .comment("Upward lift injected each tick during the LUCAS drone takeoff assist window.")
                .defineInRange("lucasTakeoffUpBoost", 0.24D, 0.0D, 1.0D);
        LUCAS_MIN_SPEED_VALUE = builder
                .comment("Minimum forward cruise speed for the LUCAS fixed-wing drone while throttle is engaged.")
                .defineInRange("lucasMinSpeed", 0.85D, 0.05D, 5.0D);
        LUCAS_MAX_SPEED_VALUE = builder
                .comment("Maximum forward speed for the LUCAS fixed-wing drone.")
                .defineInRange("lucasMaxSpeed", 3.20D, 0.10D, 8.0D);
        LUCAS_ACCELERATION_VALUE = builder
                .comment("How quickly the LUCAS fixed-wing drone blends toward its target forward speed.")
                .defineInRange("lucasAcceleration", 0.085D, 0.001D, 1.0D);
        LUCAS_DRAG_VALUE = builder
                .comment("Drag multiplier applied to LUCAS movement each tick. Lower values bleed speed faster.")
                .defineInRange("lucasDrag", 0.992D, 0.50D, 0.999D);
        LUCAS_TURN_RATE_VALUE = builder
                .comment("Yaw turn rate for the LUCAS fixed-wing drone.")
                .defineInRange("lucasTurnRate", 2.0D, 0.05D, 10.0D);
        LUCAS_PITCH_RATE_VALUE = builder
                .comment("Mouse pitch response rate for the LUCAS fixed-wing drone.")
                .defineInRange("lucasPitchRate", 0.60D, 0.05D, 10.0D);
        LUCAS_CLIMB_SPEED_VALUE = builder
                .comment("Extra upward climb speed while holding Space with the LUCAS drone.")
                .defineInRange("lucasClimbSpeed", 0.08D, 0.0D, 1.0D);
        LUCAS_DESCEND_SPEED_VALUE = builder
                .comment("Extra downward speed while holding Shift with the LUCAS drone.")
                .defineInRange("lucasDescendSpeed", 0.07D, 0.0D, 1.0D);
        LUCAS_LIFT_STRENGTH_VALUE = builder
                .comment("Base arcade lift strength applied when the LUCAS drone has enough throttle to stay airborne.")
                .defineInRange("lucasLiftStrength", 0.060D, 0.0D, 1.0D);
        LUCAS_ROLL_VISUAL_AMOUNT_VALUE = builder
                .comment("Maximum banking roll shown while the LUCAS drone turns.")
                .defineInRange("lucasRollVisualAmount", 25.0D, 0.0D, 90.0D);
        LUCAS_MIN_LIFT_SPEED_VALUE = builder
                .comment("Speed threshold below which the LUCAS drone begins to stall and lose lift.")
                .defineInRange("lucasMinLiftSpeed", 0.45D, 0.05D, 6.0D);
        LUCAS_STALL_GRAVITY_VALUE = builder
                .comment("Extra downward pull applied while the LUCAS drone is stalled.")
                .defineInRange("lucasStallGravity", 0.04D, 0.0D, 1.0D);
        LUCAS_STALL_ENABLED_VALUE = builder
                .comment("Enable harsher fixed-wing stall behavior for the LUCAS drone. Disabled by default for arcade flight.")
                .define("lucasStallEnabled", false);
        LUCAS_CAN_HOVER_VALUE = builder
                .comment("If true, the LUCAS drone can slow to a hover. Intended to stay false for fixed-wing behavior.")
                .define("lucasCanHover", false);
        LUCAS_MAX_FUEL_VALUE = builder
                .comment("Maximum gasoline fuel stored by the LUCAS drone.")
                .defineInRange("lucasMaxFuel", 1000, 1, 100000);
        LUCAS_FUEL_DRAIN_IDLE_VALUE = builder
                .comment("Fuel drained per tick while the LUCAS engine is active on the ground.")
                .defineInRange("lucasFuelDrainIdle", 0.02D, 0.0D, 100.0D);
        LUCAS_FUEL_DRAIN_FLYING_VALUE = builder
                .comment("Fuel drained per tick while the LUCAS drone is flying.")
                .defineInRange("lucasFuelDrainFlying", 0.08D, 0.0D, 100.0D);
        LUCAS_FUEL_DRAIN_CLIMB_VALUE = builder
                .comment("Extra fuel drained per tick while the LUCAS drone climbs or uses takeoff assist.")
                .defineInRange("lucasFuelDrainClimb", 0.14D, 0.0D, 100.0D);
        LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE = builder
                .comment("Fuel percentage at or below which the LUCAS fuel warning appears.")
                .defineInRange("lucasLowFuelWarningPercent", 15, 0, 100);
        LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE = builder
                .comment("If true, LUCAS can keep powered flight with an empty tank. Intended to stay false for gasoline gameplay.")
                .define("lucasCanFlyWithoutFuel", false);
        LUCAS_HUD_SCALE_VALUE = builder
                .comment("Scale multiplier for the LUCAS military UAV camera HUD.")
                .defineInRange("lucasHudScale", 1.0D, 0.60D, 1.50D);
        builder.pop();

        builder.push("fuelMixer");
        FUEL_MIXER_PROCESS_TIME_VALUE = builder
                .comment("Ticks required for the Fuel Mixer to refine coal/charcoal, blaze powder, and redstone into one gasoline canister.")
                .defineInRange("fuelMixerProcessTime", 200, 1, 12000);
        GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE = builder
                .comment("Fuel added to a grounded LUCAS drone by one Gasoline Jerrycan.")
                .defineInRange("gasolineJerrycanFuelAmount", 1000, 1, 100000);
        builder.pop();

        builder.push("thermalVision");
        ENABLE_THERMAL_VISION_VALUE = builder
                .comment("Enable the Thermal Vision drone overlay toggle on the client.")
                .define("enableThermalVision", true);
        THERMAL_VISION_RANGE_VALUE = builder
                .comment("Maximum range in blocks for Thermal Vision entity highlighting while using the drone monitor.")
                .defineInRange("thermalVisionRange", 64, 8, 256);
        THERMAL_VISION_KEYBIND_ENABLED_VALUE = builder
                .comment("Allow toggling Thermal Vision with the client keybind while using the upgraded drone terminal.")
                .define("thermalVisionKeybindEnabled", true);
        THERMAL_VISION_NOISE_EFFECT_VALUE = builder
                .comment("Render subtle scanline and noise effects while Thermal Vision is enabled.")
                .define("thermalVisionNoiseEffect", true);
        THERMAL_HIGHLIGHT_PLAYERS_VALUE = builder
                .comment("Highlight player entities in Thermal Vision.")
                .define("thermalHighlightPlayers", true);
        THERMAL_HIGHLIGHT_HOSTILE_MOBS_VALUE = builder
                .comment("Highlight hostile mobs in Thermal Vision.")
                .define("thermalVisionHighlightHostileMobs", true);
        THERMAL_HIGHLIGHT_ANIMALS_VALUE = builder
                .comment("Highlight animals and passive mobs in Thermal Vision.")
                .define("thermalVisionHighlightAnimals", true);
        builder.pop();

        builder.push("battery");
        ENABLE_BATTERY_SYSTEM_VALUE = builder
                .comment("Enable the drone battery / energy system.")
                .define("enableBatterySystem", true);
        MAX_ENERGY_VALUE = builder
                .comment("Maximum battery capacity for a drone.")
                .defineInRange("maxEnergy", 1200, 1, 1000000);
        ENERGY_USE_PER_TICK_VALUE = builder
                .comment("Energy consumed each server tick while the drone is actively flying or being controlled.")
                .defineInRange("energyUsePerTick", 1, 0, 100000);
        IDLE_ENERGY_USE_PER_TICK_VALUE = builder
                .comment("Energy consumed each server tick while the drone is idle.")
                .defineInRange("idleEnergyUsePerTick", 0, 0, 100000);
        LOW_ENERGY_THRESHOLD_VALUE = builder
                .comment("Threshold where the controller starts receiving low battery warnings.")
                .defineInRange("lowEnergyThreshold", 200, 0, 1000000);
        RETURN_HOME_ON_EMPTY_VALUE = builder
                .comment("If true, an empty drone drifts back toward its controller while descending.")
                .define("returnHomeOnEmpty", false);
        BATTERY_TRANSFER_RATE_VALUE = builder
                .comment("Maximum energy transferred from an SBW battery item to the drone per right-click.")
                .defineInRange("batteryTransferRate", 100, 1, 1000000);
        builder.pop();

        builder.push("droneSiren");
        ENABLE_DRONE_SIREN_VALUE = builder
                .comment("Enable the Drone Detection Siren block.")
                .define("enableDroneSiren", true);
        DRONE_SIREN_RANGE_VALUE = builder
                .comment("Detection radius in blocks for nearby Superb Warfare drones.")
                .defineInRange("droneSirenRange", 32, 4, 256);
        DRONE_SIREN_REDSTONE_OUTPUT_VALUE = builder
                .comment("Emit a redstone signal while the siren is active.")
                .define("droneSirenRedstoneOutput", true);
        DRONE_SIREN_COOLDOWN_TICKS_VALUE = builder
                .comment("Ticks the siren remains active after losing drone contact.")
                .defineInRange("droneSirenCooldownTicks", 20, 1, 1200);
        DETECT_FRIENDLY_DRONES_VALUE = builder
                .comment("If false, sirens ignore drones owned by the player who placed the siren.")
                .define("detectFriendlyDrones", true);
        IGNORED_DRONE_OWNER_NAMES_VALUE = builder
                .comment("Player names whose owned drones should never trigger any siren.")
                .defineListAllowEmpty("ignoredDroneOwnerNames", List.of(), value -> value instanceof String);
        IGNORED_DRONE_OWNER_UUIDS_VALUE = builder
                .comment("Player UUIDs whose owned drones should never trigger any siren.")
                .defineListAllowEmpty("ignoredDroneOwnerUUIDs", List.of(), value -> value instanceof String);
        IGNORE_OWNERLESS_DRONES_VALUE = builder
                .comment("If true, drones without a stored owner are ignored by sirens.")
                .define("ignoreOwnerlessDrones", false);
        IGNORED_DRONE_NAMES_VALUE = builder
                .comment("Custom drone names that should never trigger any siren.")
                .defineListAllowEmpty("ignoredDroneNames", List.of(), value -> value instanceof String);
        IGNORE_UNNAMED_DRONES_VALUE = builder
                .comment("If true, drones without a custom name are ignored by sirens.")
                .define("ignoreUnnamedDrones", false);
        BLACKLIST_MATCH_CASE_INSENSITIVE_VALUE = builder
                .comment("If true, siren drone-name blacklist checks ignore case.")
                .define("blacklistMatchCaseInsensitive", true);
        builder.pop();

        builder.push("droneJammer");
        ENABLE_DRONE_JAMMER_VALUE = builder
                .comment("Enable the portable FPV detector and the existing placeable area jammer.")
                .define("enableDroneJammer", true);
        JAMMER_RANGE_VALUE = builder
                .comment("Disruption radius in blocks for existing placeable area jammers.")
                .defineInRange("jammerRange", 32, 4, 256);
        JAMMER_BUILD_UP_TICKS_VALUE = builder
                .comment("Ticks a drone at maximum jammer strength needs to build from 0 to full jam progress.")
                .defineInRange("jammerBuildUpTicks", 80, 10, 2400);
        JAMMER_RECOVERY_TICKS_VALUE = builder
                .comment("Ticks a drone takes to recover from full jam progress after leaving jammer coverage.")
                .defineInRange("jammerRecoveryTicks", 120, 10, 2400);
        HARD_JAM_THRESHOLD_VALUE = builder
                .comment("Jam progress threshold where a short hard-lock countdown begins.")
                .defineInRange("hardJamThreshold", 0.95D, 0.50D, 1.0D);
        CONTROL_INTERFERENCE_THRESHOLD_VALUE = builder
                .comment("Jam progress threshold where drone controls start stuttering.")
                .defineInRange("controlInterferenceThreshold", 0.50D, 0.0D, 0.99D);
        OVERLAY_START_THRESHOLD_VALUE = builder
                .comment("Jam progress threshold where the FPV static overlay becomes visible.")
                .defineInRange("overlayStartThreshold", 0.15D, 0.0D, 0.99D);
        DEBUG_JAMMER_VALUE = builder
                .comment("Enable verbose progressive jammer debug logging.")
                .define("debugJammer", false);
        JAMMER_MODE_VALUE = builder
                .comment("Legacy instant jammer behavior mode. Progressive jammer logic now ignores this value.")
                .defineEnum("jammerMode", DroneJammerMode.HARD);
        AFFECT_FRIENDLY_DRONES_VALUE = builder
                .comment("If false, jammers ignore drones owned by the player who placed the jammer.")
                .define("affectFriendlyDrones", true);
        DRONE_RADAR_DETECTION_RANGE_VALUE = builder
                .comment("Legacy detection range in blocks for the handheld FPV detector display.")
                .defineInRange("droneRadarDetectionRange", 256, 16, 1024);
        DRONE_RADAR_BEEP_ENABLED_VALUE = builder
                .comment("Play pulsing RF detector beeps when the handheld radar detects drones.")
                .define("droneRadarBeepEnabled", true);
        DRONE_RADAR_BEEP_VOLUME_VALUE = builder
                .comment("Base volume for handheld FPV detector beeps.")
                .defineInRange("droneRadarBeepVolume", 0.45D, 0.0D, 2.0D);
        DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE = builder
                .comment("Fastest allowed handheld radar beep interval in ticks.")
                .defineInRange("droneRadarMinBeepIntervalTicks", 5, 1, 100);
        DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE = builder
                .comment("Slowest allowed handheld radar beep interval in ticks.")
                .defineInRange("droneRadarMaxBeepIntervalTicks", 40, 5, 200);
        DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE = builder
                .comment("If true, only play detector beeps while the active detector item is held in hand.")
                .define("droneRadarOnlyBeepWhenHeld", true);
        DRONE_RADAR_HUD_SCALE_VALUE = builder
                .comment("Scale for the handheld FPV detector HUD.")
                .defineInRange("droneRadarHudScale", 0.55D, 0.25D, 1.25D);
        DRONE_RADAR_HUD_X_OFFSET_VALUE = builder
                .comment("Top-right horizontal offset in pixels for the handheld FPV detector HUD.")
                .defineInRange("droneRadarHudXOffset", 8, 0, 256);
        DRONE_RADAR_HUD_Y_OFFSET_VALUE = builder
                .comment("Top-right vertical offset in pixels for the handheld FPV detector HUD.")
                .defineInRange("droneRadarHudYOffset", 8, 0, 256);
        DRONE_RADAR_COMPACT_MODE_VALUE = builder
                .comment("Use compact short-label layout for the handheld FPV detector HUD.")
                .define("droneRadarCompactMode", true);
        DEBUG_DRONE_RADAR_VALUE = builder
                .comment("Enable verbose handheld FPV detector debug logging.")
                .define("debugDroneRadar", false);
        builder.pop();

        builder.push("portableFpvDetector");
        PORTABLE_FPV_DETECTOR_RANGE_VALUE = builder
                .comment("Maximum wireless FPV detector range in blocks.")
                .defineInRange("portableFpvDetectorRange", 1500, 16, 5000);
        PORTABLE_FPV_DETECTOR_SCAN_INTERVAL_TICKS_VALUE = builder
                .comment("Server-side scan interval in ticks for active held portable FPV detectors.")
                .defineInRange("portableFpvDetectorScanIntervalTicks", 10, 1, 200);
        PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE = builder
                .comment("Maximum detector targets sent to one client per scan.")
                .defineInRange("portableFpvDetectorMaximumTargets", 16, 1, 64);
        PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE = builder
                .comment("Allow read-only intercepted video panels for compatible wireless drones.")
                .define("portableFpvDetectorVideoEnabled", true);
        PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE = builder
                .comment("If true, the detector also lists drones owned by the detector holder.")
                .define("portableFpvDetectorDetectOwnDrones", true);
        PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE = builder
                .comment("If true, detector scans require line of sight to each drone.")
                .define("portableFpvDetectorRequireLineOfSight", false);
        PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE = builder
                .comment("Render the compact handheld detector HUD while the active detector is held.")
                .define("portableFpvDetectorHudEnabled", true);
        PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE = builder
                .comment("Show numeric signal percentages in the raised detector receiver screen.")
                .define("portableFpvDetectorShowSignalPercent", true);
        builder.pop();

        builder.push("laserDefenseTower");
        LASER_DEFENSE_TOWER_DRONE_RANGE_VALUE = builder
                .comment("Maximum drone targeting range in blocks for Superb Warfare Laser Defense Towers.")
                .defineInRange("laserDefenseTowerDroneRange", 500, 1, 1200);
        LASER_DEFENSE_TOWER_WHITELIST_ENABLED_VALUE = builder
                .comment("If true, Laser Defense Towers ignore drones owned by gamertags on the tower whitelist.")
                .define("laserDefenseTowerWhitelistEnabled", true);
        builder.pop();

        builder.push("fiberOptic");
        ENABLE_FIBER_OPTIC_MODE_VALUE = builder
                .comment("Enable the optional Fiber Optic spool module for FPV drones.")
                .define("enableFiberOpticMode", true);
        FIBER_OPTIC_CABLE_LENGTH_VALUE = builder
                .comment("Maximum cable length in blocks before a Fiber Optic link starts building tension.")
                .defineInRange("fiberOpticCableLength", 256, 16, 4096);
        FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE = builder
                .comment("Ticks an overstretched Fiber Optic cable can stay beyond its length limit before it snaps.")
                .defineInRange("fiberOpticCableBreakDelayTicks", 60, 1, 2400);
        FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE = builder
                .comment("Additional active-flight battery drain multiplier while controlling a drone through Fiber Optic mode.")
                .defineInRange("fiberOpticExtraBatteryDrainMultiplier", 1.15D, 1.0D, 10.0D);
        NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE = builder
                .comment("Allow normal jammer interference to affect drones using Fiber Optic mode.")
                .define("normalJammersAffectFiberOptic", false);
        ANTI_DRONE_NET_CUTS_FIBER_VALUE = builder
                .comment("If true, anti-drone nets sever Fiber Optic drone links and damage the installed spool.")
                .define("antiDroneNetCutsFiber", true);
        ANTI_DRONE_NET_PANEL_REQUIRES_SUPPORT_VALUE = builder
                .comment("Require Anti-Drone Net Panels to have a solid attached face. Keep false for free-floating panels.")
                .define("antiDroneNetPanelRequiresSupport", false);
        ANTI_DRONE_NET_PANEL_TRIGGER_THICKNESS_VALUE = builder
                .comment("Anti-Drone Net Panel trigger thickness in blocks. This can be thicker than the visual panel so drones reliably trigger it.")
                .defineInRange("antiDroneNetPanelTriggerThickness", 0.25D, 0.0625D, 0.5D);
        ANTI_DRONE_NET_PANEL_VISUAL_THICKNESS_VALUE = builder
                .comment("Anti-Drone Net Panel visual thickness in blocks.")
                .defineInRange("antiDroneNetPanelVisualThickness", 0.0625D, 0.03125D, 0.125D);
        VEHICLE_ANTI_DRONE_NET_ENABLED_VALUE = builder
                .comment("Enable the vehicle-mounted Anti-Drone Net upgrade for Superb Warfare vehicles.")
                .define("vehicleAntiDroneNetEnabled", true);
        VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE = builder
                .comment("Horizontal padding in blocks added around the detected vehicle body for the mounted anti-drone net.")
                .defineInRange("vehicleAntiDroneNetHorizontalPadding", 0.20D, 0.0D, 2.0D);
        VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE = builder
                .comment("Vertical padding in blocks added above the detected vehicle body for the mounted anti-drone net.")
                .defineInRange("vehicleAntiDroneNetTopPadding", 0.25D, 0.0D, 2.0D);
        VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE = builder
                .comment("If true, drones crossing the mounted vehicle net are jammed like normal Anti-Drone Net blocks.")
                .define("vehicleAntiDroneNetStopsDrones", true);
        VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE = builder
                .comment("Opacity of the visible vehicle-mounted net cage.")
                .defineInRange("vehicleAntiDroneNetOpacity", 0.70D, 0.05D, 1.0D);
        NET_BULK_PLACEMENT_ENABLED_VALUE = builder
                .comment("Enable sneak-right-click POS1/POS2 bulk placement for Anti-Drone Net, Panel, and Carpet blocks.")
                .define("netBulkPlacementEnabled", true);
        NET_BULK_PLACEMENT_MAX_WIDTH_VALUE = builder
                .comment("Maximum horizontal width for one anti-drone net bulk placement plane.")
                .defineInRange("netBulkPlacementMaxWidth", 256, 1, 512);
        NET_BULK_PLACEMENT_MAX_HEIGHT_VALUE = builder
                .comment("Maximum vertical height for net/panel planes, or Z depth for carpet planes.")
                .defineInRange("netBulkPlacementMaxHeight", 128, 1, 256);
        NET_BULK_PLACEMENT_MAX_BLOCKS_VALUE = builder
                .comment("Maximum total blocks in one anti-drone net bulk placement.")
                .defineInRange("netBulkPlacementMaxBlocks", 16384, 1, 32768);
        NET_BULK_PLACEMENT_MAXIMUM_REACH_VALUE = builder
                .comment("Maximum reach used when validating POS1 and POS2 on the server.")
                .defineInRange("netBulkPlacementMaximumReach", 256.0D, 1.0D, 256.0D);
        NET_BULK_PLACEMENT_ALL_OR_NOTHING_VALUE = builder
                .comment("Reject the whole placement when any target position is blocked.")
                .define("netBulkPlacementAllOrNothing", false);
        NET_BULK_PLACEMENT_SKIP_BLOCKED_VALUE = builder
                .comment("When all-or-nothing is disabled, skip blocked positions instead of failing the whole placement.")
                .define("netBulkPlacementSkipBlocked", true);
        NET_BULK_PLACEMENT_BLOCKS_PER_TICK_VALUE = builder
                .comment("Maximum anti-drone net blocks placed per server tick for large confirmed selections.")
                .defineInRange("netBulkPlacementBlocksPerTick", 128, 1, 512);
        NET_BULK_PLACEMENT_UNDO_SECONDS_VALUE = builder
                .comment("Seconds that /sbw net undo remains available for the player's most recent successful bulk placement.")
                .defineInRange("netBulkPlacementUndoSeconds", 60, 0, 3600);
        KEEP_DETACHED_FIBER_CABLE_ON_DRONE_DEATH_VALUE = builder
                .comment("Keep Fiber Optic cable segment entities in the world when the linked drone dies, crashes, despawns, or is removed.")
                .define("keepDetachedFiberCableOnDroneDeath", true);
        DETACHED_FIBER_CABLE_LIFETIME_TICKS_VALUE = builder
                .comment("Lifetime for detached Fiber Optic cable leftovers. -1 means never auto-despawn.")
                .defineInRange("detachedFiberCableLifetimeTicks", -1, -1, 1728000);
        DEBUG_FIBER_OPTIC_VALUE = builder
                .comment("Enable verbose Fiber Optic cable debug logging.")
                .define("debugFiberOptic", false);
        FIBER_CABLE_VISUAL_THICKNESS_VALUE = builder
                .comment("Visible width in blocks of the single client-rendered Fiber Optic cable ribbon.")
                .defineInRange("fiberCableVisualThickness", 0.015D, 0.008D, 0.025D);
        FIBER_CABLE_VISUAL_ALPHA_VALUE = builder
                .comment("Alpha transparency used by the rendered Fiber Optic cable wire.")
                .defineInRange("fiberCableVisualAlpha", 0.72D, 0.05D, 1.0D);
        FIBER_CABLE_VISUAL_COLOR_VALUE = builder
                .comment("Visible color preset for the Fiber Optic cable wire. Supported: charcoal, light_gray, off_white, pale_gray.")
                .define("fiberCableVisualColor", "charcoal", value -> value instanceof String stringValue && isSupportedFiberCableVisualColor(stringValue));
        FIBER_CABLE_MAX_RENDER_DISTANCE_VALUE = builder
                .comment("Maximum client render distance in blocks for Fiber Optic cable ribbons.")
                .defineInRange("fiberCableMaxRenderDistance", 192.0D, 32.0D, 512.0D);
        MAX_VISIBLE_FIBER_CABLES_VALUE = builder
                .comment("Maximum number of active Fiber Optic cables visible on one client.")
                .defineInRange("maxVisibleFiberCables", 16, 1, 64);
        FIBER_ROUTE_HISTORY_ENABLED_VALUE = builder
                .comment("Enable sparse zero-entity Fiber Optic route history rendering.")
                .define("fiberRouteHistoryEnabled", true);
        FIBER_ROUTE_POINT_SPACING_VALUE = builder
                .comment("Minimum distance in blocks from the last saved route point before appending another Fiber Optic route point.")
                .defineInRange("fiberRoutePointSpacing", 2.5D, 0.25D, 32.0D);
        FIBER_ROUTE_MINIMUM_TURN_DISTANCE_VALUE = builder
                .comment("Minimum movement distance in blocks before a turn can add a sparse Fiber Optic route point.")
                .defineInRange("fiberRouteMinimumTurnDistance", 1.0D, 0.1D, 16.0D);
        FIBER_ROUTE_TURN_ANGLE_THRESHOLD_VALUE = builder
                .comment("Movement direction change in degrees that commits a new Fiber Optic route point.")
                .defineInRange("fiberRouteTurnAngleThreshold", 18.0D, 1.0D, 175.0D);
        FIBER_ROUTE_MAXIMUM_POINTS_VALUE = builder
                .comment("Maximum saved Fiber Optic route points per active connection.")
                .defineInRange("fiberRouteMaximumPoints", 512, 8, 2048);
        FIBER_ROUTE_CORNER_SMOOTHING_VALUE = builder
                .comment("Smooth sparse Fiber Optic route corners during client rendering.")
                .define("fiberRouteCornerSmoothing", true);
        FIBER_ROUTE_DEBUG_LOGGING_VALUE = builder
                .comment("Enable compact debug logging for Fiber Optic route-history events.")
                .define("fiberRouteDebugLogging", false);
        FIBER_CABLE_CAN_BE_CUT_BY_OWNER_VALUE = builder
                .comment("Allow the drone owner or active fiber controller to manually cut their own Fiber Optic cable with shears.")
                .define("fiberCableCanBeCutByOwner", true);
        FIBER_CABLE_CAN_BE_CUT_BY_OTHER_PLAYERS_VALUE = builder
                .comment("Allow other players to manually cut active Fiber Optic cables with shears.")
                .define("fiberCableCanBeCutByOtherPlayers", true);
        FIBER_CABLE_CUT_REQUIRES_SHEARS_VALUE = builder
                .comment("Require vanilla shears for manual Fiber Optic cable cutting. This first implementation only supports shears.")
                .define("fiberCableCutRequiresShears", true);
        FIBER_CABLE_CUT_REACH_VALUE = builder
                .comment("Maximum distance in blocks from player eye to a Fiber Optic cable cut point.")
                .defineInRange("fiberCableCutReach", 5.0D, 1.0D, 16.0D);
        FIBER_CABLE_INTERACTION_RADIUS_VALUE = builder
                .comment("Invisible hit-test radius in blocks used only while trying to cut the thin Fiber Optic cable.")
                .defineInRange("fiberCableInteractionRadius", 0.12D, 0.02D, 0.50D);
        FIBER_CABLE_CUT_LINE_OF_SIGHT_REQUIRED_VALUE = builder
                .comment("Reject manual Fiber Optic cuts when a solid block blocks line of sight to the validated cut point.")
                .define("fiberCableCutLineOfSightRequired", true);
        FIBER_CABLE_TARGET_INDICATOR_VALUE = builder
                .comment("Optional accessibility hook for a tiny reticle change while targeting fiber. Default false; cable visuals never change.")
                .define("fiberCableTargetIndicator", false);
        FIBER_CABLE_DEBUG_VISUAL_THICKNESS_VALUE = builder
                .comment("Debug-only thickness used when Fiber Optic cable render debugging is enabled.")
                .defineInRange("fiberCableDebugVisualThickness", 0.025D, 0.008D, 0.08D);
        FIBER_CABLE_SEGMENT_SPACING_VALUE = builder
                .comment("Legacy compatibility only. Cable segment entities are no longer maintained in Phase 1.")
                .defineInRange("fiberCableSegmentSpacing", 3.0D, 0.5D, 16.0D);
        FIBER_CABLE_SEGMENT_HEALTH_VALUE = builder
                .comment("Health for each physical Fiber Optic cable segment entity.")
                .defineInRange("fiberCableSegmentHealth", 4.0D, 0.5D, 100.0D);
        FIBER_CABLE_SEGMENT_HITBOX_SIZE_VALUE = builder
                .comment("Hitbox width/height for each Fiber Optic cable segment entity.")
                .defineInRange("fiberCableSegmentHitboxSize", 0.55D, 0.1D, 2.0D);
        FIBER_CABLE_CAN_BE_DAMAGED_BY_PLAYERS_VALUE = builder
                .comment("Allow players to damage Fiber Optic cable segment entities with melee hits.")
                .define("fiberCableCanBeDamagedByPlayers", true);
        FIBER_CABLE_CAN_BE_DAMAGED_BY_PROJECTILES_VALUE = builder
                .comment("Allow projectiles to damage Fiber Optic cable segment entities.")
                .define("fiberCableCanBeDamagedByProjectiles", true);
        FIBER_CABLE_CAN_BE_DAMAGED_BY_EXPLOSIONS_VALUE = builder
                .comment("Allow explosions to damage Fiber Optic cable segment entities.")
                .define("fiberCableCanBeDamagedByExplosions", true);
        FIBER_CABLE_PROJECTILE_RAYCAST_BREAK_VALUE = builder
                .comment("Use a projectile movement raycast fallback to sever Fiber Optic cables when small segment hitboxes are missed.")
                .define("fiberCableProjectileRaycastBreak", true);
        FIBER_CABLE_BREAKS_WHEN_ANY_SEGMENT_DESTROYED_VALUE = builder
                .comment("If true, destroying any Fiber Optic cable segment immediately severs the link.")
                .define("fiberCableBreaksWhenAnySegmentDestroyed", true);
        MAX_FIBER_CABLE_SEGMENTS_PER_DRONE_VALUE = builder
                .comment("Maximum amount of physical Fiber Optic cable segment entities maintained per drone.")
                .defineInRange("maxFiberCableSegmentsPerDrone", 96, 1, 256);
        DEBUG_FIBER_CABLE_RENDER_VALUE = builder
                .comment("Enable verbose client-side Fiber Optic cable render debug logging.")
                .define("debugFiberCableRender", false);
        DEBUG_FIBER_CABLE_SEGMENTS_VALUE = builder
                .comment("Enable verbose Fiber Optic cable segment lifecycle debug logging.")
                .define("debugFiberCableSegments", false);
        RENDER_RED_DEBUG_CABLE_VALUE = builder
                .comment("Render the visible Fiber Optic cable in bright red while debugFiberCableRender is enabled.")
                .define("renderRedDebugCable", false);
        RENDER_LEGACY_FIBER_LINE_VALUE = builder
                .comment("Render the old client-only Fiber Optic line in addition to the physical cable segments.")
                .define("renderLegacyFiberLine", false);
        builder.pop();

        builder.push("weather");
        WEATHER_EFFECTS_ENABLED_VALUE = builder
                .comment("Enable rain and storm effects for drones.")
                .define("weatherEffectsEnabled", true);
        RAIN_VISIBILITY_MULTIPLIER_VALUE = builder
                .comment("Multiplier applied to effective drone visibility and control clarity during rain.")
                .defineInRange("rainVisibilityMultiplier", 0.8D, 0.1D, 1.0D);
        STORM_INSTABILITY_STRENGTH_VALUE = builder
                .comment("Strength of random wind instability applied to drones during thunderstorms.")
                .defineInRange("stormInstabilityStrength", 0.2D, 0.0D, 2.0D);
        builder.pop();

        builder.push("droneScreenshot");
        ENABLE_DRONE_SCREENSHOT_SOUND_VALUE = builder
                .comment("Play a shutter sound when taking a drone FPV screenshot.")
                .define("enableDroneScreenshotSound", true);
        builder.pop();

        builder.push("fpvCamera");
        FPV_CAMERA_FOV_VALUE = builder
                .comment("Wide FPV camera field of view for the Cubed FPV Drone.")
                .defineInRange("fpvCameraFov", 135.0D, 120.0D, 155.0D);
        FPV_CAMERA_TILT_DEGREES_VALUE = builder
                .comment("Forward camera tilt applied while controlling the Cubed FPV Drone.")
                .defineInRange("fpvCameraTiltDegrees", 20.0D, 0.0D, 45.0D);
        ENABLE_FPV_FISHEYE_EFFECT_VALUE = builder
                .comment("Render a subtle FPV lens vignette/fisheye-style screen edge effect.")
                .define("enableFpvFisheyeEffect", true);
        ENABLE_FPV_CAMERA_VIBRATION_VALUE = builder
                .comment("Add throttle-scaled camera vibration while flying the Cubed FPV Drone.")
                .define("enableFpvCameraVibration", true);
        builder.pop();

        builder.push("audio");
        DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE = builder
                .comment("Overall volume multiplier for the drone engine sound.")
                .defineInRange("droneEngineVolumeMultiplier", 1.0D,
                        DroneAudioConfigLimits.MIN_ENGINE_VOLUME_MULTIPLIER,
                        DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER);
        LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE = builder
                .comment("Dedicated volume multiplier for the LUCAS drone engine sound.")
                .defineInRange("lucasEngineVolumeMultiplier", 5.0D,
                        DroneAudioConfigLimits.MIN_ENGINE_VOLUME_MULTIPLIER,
                        DroneAudioConfigLimits.MAX_ENGINE_VOLUME_MULTIPLIER);
        DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE = builder
                .comment("Volume multiplier for the monitor control hum while using the linked drone screen.")
                .defineInRange("droneMonitorHumVolumeMultiplier", 1.0D,
                        DroneAudioConfigLimits.MIN_MONITOR_HUM_VOLUME_MULTIPLIER,
                        DroneAudioConfigLimits.MAX_MONITOR_HUM_VOLUME_MULTIPLIER);
        DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE = builder
                .comment("Extra engine volume multiplier applied while the drone is airborne.")
                .defineInRange("droneAirborneVolumeMultiplier", 1.35D,
                        DroneAudioConfigLimits.MIN_AIRBORNE_VOLUME_MULTIPLIER,
                        DroneAudioConfigLimits.MAX_AIRBORNE_VOLUME_MULTIPLIER);
        CUBED_FPV_EXTERNAL_VOLUME_VALUE = builder
                .comment("Master client-side volume for external Cubed FPV drone motor audio.")
                .defineInRange("cubedFpvExternalVolume", 1.0D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        CUBED_FPV_NEAR_VOLUME_VALUE = builder
                .comment("Near layer volume for external Cubed FPV drone motor audio.")
                .defineInRange("cubedFpvNearVolume", 1.0D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        CUBED_FPV_MID_VOLUME_VALUE = builder
                .comment("Mid-distance layer volume for external Cubed FPV drone motor audio.")
                .defineInRange("cubedFpvMidVolume", 0.8D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        CUBED_FPV_FAR_VOLUME_VALUE = builder
                .comment("Far-distance layer volume for external Cubed FPV drone motor audio.")
                .defineInRange("cubedFpvFarVolume", 0.55D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        LUCAS_EXTERNAL_VOLUME_VALUE = builder
                .comment("Master client-side volume for external LUCAS fixed-wing drone motor audio.")
                .defineInRange("lucasExternalVolume", 1.1D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        LUCAS_NEAR_VOLUME_VALUE = builder
                .comment("Near layer volume for external LUCAS fixed-wing drone motor audio.")
                .defineInRange("lucasNearVolume", 1.0D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        LUCAS_MID_VOLUME_VALUE = builder
                .comment("Mid-distance layer volume for external LUCAS fixed-wing drone motor audio.")
                .defineInRange("lucasMidVolume", 0.85D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        LUCAS_FAR_VOLUME_VALUE = builder
                .comment("Far-distance layer volume for external LUCAS fixed-wing drone motor audio.")
                .defineInRange("lucasFarVolume", 0.60D,
                        DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                        DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        FPV_INTERNAL_AUDIO_GAIN_VALUE = builder
                .comment("Local FPV monitor listener gain for the Cubed FPV motor mix.")
                .defineInRange("fpvInternalAudioGain", 0.65D,
                        DroneAudioConfigLimits.MIN_INTERNAL_AUDIO_GAIN,
                        DroneAudioConfigLimits.MAX_INTERNAL_AUDIO_GAIN);
        DRONE_DOPPLER_ENABLED_VALUE = builder
                .comment("Enable subtle client-side drone motor doppler pitch movement.")
                .define("droneDopplerEnabled", true);
        DRONE_DOPPLER_STRENGTH_VALUE = builder
                .comment("Strength of subtle client-side drone motor doppler pitch movement.")
                .defineInRange("droneDopplerStrength", 0.20D,
                        DroneAudioConfigLimits.MIN_DOPPLER_STRENGTH,
                        DroneAudioConfigLimits.MAX_DOPPLER_STRENGTH);
        DRONE_AUDIO_OCCLUSION_VALUE = builder
                .comment("Enable lightweight external drone audio occlusion behind solid blocks.")
                .define("droneAudioOcclusion", true);
        MAX_AUDIBLE_CUBED_FPV_DRONES_VALUE = builder
                .comment("Maximum Cubed FPV drone external audio sets managed on one client.")
                .defineInRange("maxAudibleCubedFpvDrones", 12,
                        DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                        DroneAudioConfigLimits.MAX_AUDIBLE_DRONES);
        MAX_AUDIBLE_LUCAS_DRONES_VALUE = builder
                .comment("Maximum LUCAS drone external audio sets managed on one client.")
                .defineInRange("maxAudibleLucasDrones", 8,
                        DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                        DroneAudioConfigLimits.MAX_AUDIBLE_DRONES);
        builder.pop();

        builder.push("spotlight");
        ENABLE_SPOTLIGHT_MODULE_VALUE = builder
                .comment("Enable the drone Spotlight Module upgrade system.")
                .define("enableSpotlightModule", true);
        SPOTLIGHT_RANGE_VALUE = builder
                .comment("Maximum render/search distance of the drone spotlight beam.")
                .defineInRange("spotlightRange", 48, 8, 100);
        SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE = builder
                .comment("Extra drone battery energy consumed per second while the spotlight is active.")
                .defineInRange("spotlightEnergyCostPerSecond", 2, 0, 1000);
        SPOTLIGHT_REQUIRES_MODULE_VALUE = builder
                .comment("Require a Spotlight Module item to be installed in the drone inventory before use.")
                .define("spotlightRequiresModule", true);
        SPOTLIGHT_VISIBLE_TO_OTHER_PLAYERS_VALUE = builder
                .comment("If true, other players can also see spotlight beams from active drones.")
                .define("spotlightVisibleToOtherPlayers", true);
        SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE = builder
                .comment("Automatically disable the spotlight when the drone battery runs empty.")
                .define("spotlightAutoOffWhenBatteryEmpty", true);
        SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE = builder
                .comment("Create temporary real light blocks in front of the drone spotlight.")
                .define("spotlightRealLightEnabled", true);
        SPOTLIGHT_LIGHT_RANGE_VALUE = builder
                .comment("Maximum distance used by the real spotlight light positions.")
                .defineInRange("spotlightLightRange", 48, 8, 100);
        SPOTLIGHT_LIGHT_LEVEL_VALUE = builder
                .comment("Base light level used for the nearest spotlight light source.")
                .defineInRange("spotlightLightLevel", 15, 1, 15);
        SPOTLIGHT_LIGHT_UPDATE_INTERVAL_TICKS_VALUE = builder
                .comment("How often the spotlight real light positions update while active.")
                .defineInRange("spotlightLightUpdateIntervalTicks", 2, 1, 10);
        builder.pop();

        builder.push("radarTowerMount");
        DEBUG_RADAR_TOWER_MOUNT_VALUE = builder
                .comment("Legacy debug option. The Radar Jammer Tower now renders as one static block model without an animated mount renderer.")
                .define("debugRadarTowerMount", false);
        RADAR_TOWER_ROOF_ANCHOR_X_VALUE = builder
                .comment("Local X offset for the Radar Jammer Tower roof/cage anchor.")
                .defineInRange("roofAnchorX", 1.24D, -4.0D, 4.0D);
        RADAR_TOWER_ROOF_ANCHOR_Y_VALUE = builder
                .comment("Local Y offset for the Radar Jammer Tower roof/cage anchor.")
                .defineInRange("roofAnchorY", 2.42D, -1.0D, 6.0D);
        RADAR_TOWER_ROOF_ANCHOR_Z_VALUE = builder
                .comment("Local Z offset for the Radar Jammer Tower roof/cage anchor.")
                .defineInRange("roofAnchorZ", 0.08D, -4.0D, 4.0D);
        RADAR_TOWER_CAGE_YAW_VALUE = builder
                .comment("Local yaw adjustment for the Radar Jammer Tower cage/head attachment.")
                .defineInRange("cageYaw", 0.0D, -180.0D, 180.0D);
        RADAR_TOWER_CAGE_PITCH_VALUE = builder
                .comment("Local pitch adjustment for the Radar Jammer Tower cage/head attachment.")
                .defineInRange("cagePitch", 0.0D, -180.0D, 180.0D);
        RADAR_TOWER_CAGE_ROLL_VALUE = builder
                .comment("Local roll adjustment for the Radar Jammer Tower cage/head attachment.")
                .defineInRange("cageRoll", 0.0D, -180.0D, 180.0D);
        RADAR_TOWER_CAGE_SCALE_VALUE = builder
                .comment("Local scale for the Radar Jammer Tower cage/head attachment.")
                .defineInRange("cageScale", 1.0D, 0.1D, 4.0D);
        RADAR_TOWER_MAX_BATTERY_CAPACITY_VALUE = builder
                .comment("Maximum internal solar battery capacity for the Radar Jammer Tower.")
                .defineInRange("maxBatteryCapacity", 24000, 1, 10000000);
        RADAR_TOWER_SOLAR_CHARGE_RATE_VALUE = builder
                .comment("Energy added per tick while the Radar Jammer Tower solar panel has clear daytime sky.")
                .defineInRange("solarChargeRate", 25, 0, 100000);
        RADAR_TOWER_CHARGING_STATION_CHARGE_RATE_VALUE = builder
                .comment("Energy added per tick when a Superb Warfare Charging Station or Creative Charging Station is below the Radar Jammer Tower base.")
                .defineInRange("chargingStationChargeRate", 100, 0, 100000);
        RADAR_TOWER_JAMMER_ENERGY_DRAIN_RATE_VALUE = builder
                .comment("Energy drained per active jammer tick from the Radar Jammer Tower battery.")
                .defineInRange("jammerEnergyDrainRate", 4, 0, 100000);
        RADAR_TOWER_MINIMUM_ENERGY_TO_ACTIVATE_VALUE = builder
                .comment("Minimum stored energy required before the Radar Jammer Tower GUI can open and the jammer can operate.")
                .defineInRange("minimumEnergyToActivate", 100, 0, 10000000);
        builder.pop();

        builder.push("payload");
        ANTI_VEHICLE_MORTAR_VEHICLE_DAMAGE_VALUE = builder
                .comment("Direct damage dealt by the Anti-Vehicle Mortar Payload to vehicle-like Superb Warfare entities.")
                .defineInRange("antiVehicleMortarVehicleDamage", 85.0D, 0.0D, 10000.0D);
        ANTI_VEHICLE_MORTAR_ENTITY_DAMAGE_VALUE = builder
                .comment("Direct damage dealt by the Anti-Vehicle Mortar Payload to normal non-vehicle entities.")
                .defineInRange("antiVehicleMortarEntityDamage", 12.0D, 0.0D, 10000.0D);
        ANTI_VEHICLE_MORTAR_EXPLOSION_RADIUS_VALUE = builder
                .comment("Radius used for payload entity damage checks.")
                .defineInRange("antiVehicleMortarExplosionRadius", 7.0D, 0.0D, 64.0D);
        ANTI_VEHICLE_MORTAR_BLOCK_DAMAGE_RADIUS_VALUE = builder
                .comment("Small vanilla explosion radius used for limited block damage. Set to 0 to disable block damage.")
                .defineInRange("antiVehicleMortarBlockDamageRadius", 2.0D, 0.0D, 16.0D);
        ANTI_VEHICLE_MORTAR_PAYLOAD_COOLDOWN_TICKS_VALUE = builder
                .comment("Cooldown in ticks between drone mortar payload drops.")
                .defineInRange("antiVehicleMortarPayloadCooldownTicks", 40, 0, 1200);
        DEBUG_PAYLOAD_MOUNTS_VALUE = builder
                .comment("Render colored payload mount debug markers and log payload mount transforms every second.")
                .define("debugPayloadMounts", false);
        builder.pop();

        SPEC = builder.build();
    }

    private AddonConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            bake();
            if (ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
                DroneTrackingRangeOverride.applyConfiguredRange();
            }
        }
    }

    private static void bake() {
        droneMaxRange = DRONE_MAX_RANGE_VALUE.get();
        droneVisualTrackingRange = DRONE_VISUAL_TRACKING_RANGE_VALUE.get();
        droneSpeedMultiplier = DRONE_SPEED_MULTIPLIER_VALUE.get();
        enableDroneChunkLoading = ENABLE_DRONE_CHUNK_LOADING_VALUE.get();
        droneChunkLoadRadius = DRONE_CHUNK_LOAD_RADIUS_VALUE.get();
        enableDronePlayerAnchor = ENABLE_DRONE_PLAYER_ANCHOR_VALUE.get();
        enableDroneCrashExplosionDamage = ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE.get();
        hidePlayerOnMinimapDuringDroneControl = HIDE_PLAYER_ON_MINIMAP_DURING_DRONE_CONTROL_VALUE.get();
        showDroneMarkerInstead = SHOW_DRONE_MARKER_INSTEAD_VALUE.get();
        lucasTakeoffAssistTicks = LUCAS_TAKEOFF_ASSIST_TICKS_VALUE.get();
        lucasTakeoffForwardBoost = LUCAS_TAKEOFF_FORWARD_BOOST_VALUE.get();
        lucasTakeoffUpBoost = LUCAS_TAKEOFF_UP_BOOST_VALUE.get();
        lucasMinSpeed = LUCAS_MIN_SPEED_VALUE.get();
        lucasMaxSpeed = Math.max(lucasMinSpeed, LUCAS_MAX_SPEED_VALUE.get());
        lucasAcceleration = LUCAS_ACCELERATION_VALUE.get();
        lucasDrag = LUCAS_DRAG_VALUE.get();
        lucasTurnRate = LUCAS_TURN_RATE_VALUE.get();
        lucasPitchRate = LUCAS_PITCH_RATE_VALUE.get();
        lucasClimbSpeed = LUCAS_CLIMB_SPEED_VALUE.get();
        lucasDescendSpeed = LUCAS_DESCEND_SPEED_VALUE.get();
        lucasLiftStrength = LUCAS_LIFT_STRENGTH_VALUE.get();
        lucasRollVisualAmount = LUCAS_ROLL_VISUAL_AMOUNT_VALUE.get();
        lucasMinLiftSpeed = LUCAS_MIN_LIFT_SPEED_VALUE.get();
        lucasStallGravity = LUCAS_STALL_GRAVITY_VALUE.get();
        lucasStallEnabled = LUCAS_STALL_ENABLED_VALUE.get();
        lucasCanHover = LUCAS_CAN_HOVER_VALUE.get();
        lucasMaxFuel = LUCAS_MAX_FUEL_VALUE.get();
        lucasFuelDrainIdle = LUCAS_FUEL_DRAIN_IDLE_VALUE.get();
        lucasFuelDrainFlying = LUCAS_FUEL_DRAIN_FLYING_VALUE.get();
        lucasFuelDrainClimb = LUCAS_FUEL_DRAIN_CLIMB_VALUE.get();
        lucasLowFuelWarningPercent = LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE.get();
        lucasCanFlyWithoutFuel = LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE.get();
        lucasHudScale = LUCAS_HUD_SCALE_VALUE.get();
        fuelMixerProcessTime = FUEL_MIXER_PROCESS_TIME_VALUE.get();
        gasolineJerrycanFuelAmount = GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE.get();
        enableThermalVision = ENABLE_THERMAL_VISION_VALUE.get();
        thermalVisionRange = THERMAL_VISION_RANGE_VALUE.get();
        thermalVisionKeybindEnabled = THERMAL_VISION_KEYBIND_ENABLED_VALUE.get();
        thermalVisionNoiseEffect = THERMAL_VISION_NOISE_EFFECT_VALUE.get();
        thermalHighlightPlayers = THERMAL_HIGHLIGHT_PLAYERS_VALUE.get();
        thermalHighlightHostileMobs = THERMAL_HIGHLIGHT_HOSTILE_MOBS_VALUE.get();
        thermalHighlightAnimals = THERMAL_HIGHLIGHT_ANIMALS_VALUE.get();
        enableBatterySystem = ENABLE_BATTERY_SYSTEM_VALUE.get();
        maxEnergy = MAX_ENERGY_VALUE.get();
        energyUsePerTick = ENERGY_USE_PER_TICK_VALUE.get();
        idleEnergyUsePerTick = IDLE_ENERGY_USE_PER_TICK_VALUE.get();
        lowEnergyThreshold = LOW_ENERGY_THRESHOLD_VALUE.get();
        returnHomeOnEmpty = RETURN_HOME_ON_EMPTY_VALUE.get();
        batteryTransferRate = BATTERY_TRANSFER_RATE_VALUE.get();
        enableDroneSiren = ENABLE_DRONE_SIREN_VALUE.get();
        droneSirenRange = DRONE_SIREN_RANGE_VALUE.get();
        droneSirenRedstoneOutput = DRONE_SIREN_REDSTONE_OUTPUT_VALUE.get();
        droneSirenCooldownTicks = DRONE_SIREN_COOLDOWN_TICKS_VALUE.get();
        detectFriendlyDrones = DETECT_FRIENDLY_DRONES_VALUE.get();
        ignoredDroneOwnerNames = normalizeOwnerNames(IGNORED_DRONE_OWNER_NAMES_VALUE.get());
        ignoredDroneOwnerUUIDs = parseOwnerUuids(IGNORED_DRONE_OWNER_UUIDS_VALUE.get());
        ignoreOwnerlessDrones = IGNORE_OWNERLESS_DRONES_VALUE.get();
        blacklistMatchCaseInsensitive = BLACKLIST_MATCH_CASE_INSENSITIVE_VALUE.get();
        ignoredDroneNames = normalizeConfiguredNames(IGNORED_DRONE_NAMES_VALUE.get(), blacklistMatchCaseInsensitive);
        ignoreUnnamedDrones = IGNORE_UNNAMED_DRONES_VALUE.get();
        enableDroneJammer = ENABLE_DRONE_JAMMER_VALUE.get();
        jammerRange = JAMMER_RANGE_VALUE.get();
        jammerBuildUpTicks = JAMMER_BUILD_UP_TICKS_VALUE.get();
        jammerRecoveryTicks = JAMMER_RECOVERY_TICKS_VALUE.get();
        overlayStartThreshold = clampThreshold(OVERLAY_START_THRESHOLD_VALUE.get(), 0.0D, 0.99D);
        controlInterferenceThreshold = Math.max(
                overlayStartThreshold,
                clampThreshold(CONTROL_INTERFERENCE_THRESHOLD_VALUE.get(), 0.0D, 0.99D)
        );
        hardJamThreshold = Math.max(
                controlInterferenceThreshold,
                clampThreshold(HARD_JAM_THRESHOLD_VALUE.get(), 0.50D, 1.0D)
        );
        debugJammer = DEBUG_JAMMER_VALUE.get();
        jammerMode = JAMMER_MODE_VALUE.get();
        affectFriendlyDrones = AFFECT_FRIENDLY_DRONES_VALUE.get();
        droneRadarDetectionRange = DRONE_RADAR_DETECTION_RANGE_VALUE.get();
        droneRadarBeepEnabled = DRONE_RADAR_BEEP_ENABLED_VALUE.get();
        droneRadarBeepVolume = DRONE_RADAR_BEEP_VOLUME_VALUE.get();
        droneRadarMinBeepIntervalTicks = DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE.get();
        droneRadarMaxBeepIntervalTicks = Math.max(
                droneRadarMinBeepIntervalTicks,
                DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE.get()
        );
        droneRadarOnlyBeepWhenHeld = DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE.get();
        droneRadarHudScale = DRONE_RADAR_HUD_SCALE_VALUE.get();
        droneRadarHudXOffset = DRONE_RADAR_HUD_X_OFFSET_VALUE.get();
        droneRadarHudYOffset = DRONE_RADAR_HUD_Y_OFFSET_VALUE.get();
        droneRadarCompactMode = DRONE_RADAR_COMPACT_MODE_VALUE.get();
        debugDroneRadar = DEBUG_DRONE_RADAR_VALUE.get();
        portableFpvDetectorRange = PORTABLE_FPV_DETECTOR_RANGE_VALUE.get();
        portableFpvDetectorScanIntervalTicks = PORTABLE_FPV_DETECTOR_SCAN_INTERVAL_TICKS_VALUE.get();
        portableFpvDetectorMaximumTargets = PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE.get();
        portableFpvDetectorVideoEnabled = PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE.get();
        portableFpvDetectorDetectOwnDrones = PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE.get();
        portableFpvDetectorRequireLineOfSight = PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE.get();
        portableFpvDetectorHudEnabled = PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE.get();
        portableFpvDetectorShowSignalPercent = PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE.get();
        laserDefenseTowerDroneRange = LASER_DEFENSE_TOWER_DRONE_RANGE_VALUE.get();
        laserDefenseTowerWhitelistEnabled = LASER_DEFENSE_TOWER_WHITELIST_ENABLED_VALUE.get();
        enableFiberOpticMode = ENABLE_FIBER_OPTIC_MODE_VALUE.get();
        fiberOpticCableLength = FIBER_OPTIC_CABLE_LENGTH_VALUE.get();
        fiberOpticCableBreakDelayTicks = FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE.get();
        fiberOpticExtraBatteryDrainMultiplier = FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE.get();
        normalJammersAffectFiberOptic = NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE.get();
        antiDroneNetCutsFiber = ANTI_DRONE_NET_CUTS_FIBER_VALUE.get();
        antiDroneNetPanelRequiresSupport = ANTI_DRONE_NET_PANEL_REQUIRES_SUPPORT_VALUE.get();
        antiDroneNetPanelTriggerThickness = ANTI_DRONE_NET_PANEL_TRIGGER_THICKNESS_VALUE.get();
        antiDroneNetPanelVisualThickness = ANTI_DRONE_NET_PANEL_VISUAL_THICKNESS_VALUE.get();
        vehicleAntiDroneNetEnabled = VEHICLE_ANTI_DRONE_NET_ENABLED_VALUE.get();
        vehicleAntiDroneNetHorizontalPadding = clamp(VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE.get(), 0.0D, 2.0D);
        vehicleAntiDroneNetTopPadding = clamp(VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE.get(), 0.0D, 2.0D);
        vehicleAntiDroneNetStopsDrones = VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE.get();
        vehicleAntiDroneNetOpacity = clamp(VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE.get(), 0.05D, 1.0D);
        netBulkPlacementEnabled = NET_BULK_PLACEMENT_ENABLED_VALUE.get();
        netBulkPlacementMaxWidth = NET_BULK_PLACEMENT_MAX_WIDTH_VALUE.get();
        netBulkPlacementMaxHeight = NET_BULK_PLACEMENT_MAX_HEIGHT_VALUE.get();
        netBulkPlacementMaxBlocks = Math.min(
                NET_BULK_PLACEMENT_MAX_BLOCKS_VALUE.get(),
                netBulkPlacementMaxWidth * netBulkPlacementMaxHeight
        );
        netBulkPlacementMaximumReach = clamp(NET_BULK_PLACEMENT_MAXIMUM_REACH_VALUE.get(), 1.0D, 256.0D);
        netBulkPlacementAllOrNothing = NET_BULK_PLACEMENT_ALL_OR_NOTHING_VALUE.get();
        netBulkPlacementSkipBlocked = NET_BULK_PLACEMENT_SKIP_BLOCKED_VALUE.get();
        netBulkPlacementBlocksPerTick = Math.max(1, NET_BULK_PLACEMENT_BLOCKS_PER_TICK_VALUE.get());
        netBulkPlacementUndoSeconds = Math.max(0, NET_BULK_PLACEMENT_UNDO_SECONDS_VALUE.get());
        keepDetachedFiberCableOnDroneDeath = KEEP_DETACHED_FIBER_CABLE_ON_DRONE_DEATH_VALUE.get();
        detachedFiberCableLifetimeTicks = DETACHED_FIBER_CABLE_LIFETIME_TICKS_VALUE.get();
        debugFiberOptic = DEBUG_FIBER_OPTIC_VALUE.get();
        fiberCableVisualThickness = FIBER_CABLE_VISUAL_THICKNESS_VALUE.get();
        fiberCableVisualAlpha = FIBER_CABLE_VISUAL_ALPHA_VALUE.get();
        fiberCableVisualColor = normalizeFiberCableVisualColor(FIBER_CABLE_VISUAL_COLOR_VALUE.get());
        fiberCableMaxRenderDistance = FIBER_CABLE_MAX_RENDER_DISTANCE_VALUE.get();
        maxVisibleFiberCables = MAX_VISIBLE_FIBER_CABLES_VALUE.get();
        fiberRouteHistoryEnabled = FIBER_ROUTE_HISTORY_ENABLED_VALUE.get();
        fiberRoutePointSpacing = FIBER_ROUTE_POINT_SPACING_VALUE.get();
        fiberRouteMinimumTurnDistance = FIBER_ROUTE_MINIMUM_TURN_DISTANCE_VALUE.get();
        fiberRouteTurnAngleThreshold = FIBER_ROUTE_TURN_ANGLE_THRESHOLD_VALUE.get();
        fiberRouteMaximumPoints = FIBER_ROUTE_MAXIMUM_POINTS_VALUE.get();
        fiberRouteCornerSmoothing = FIBER_ROUTE_CORNER_SMOOTHING_VALUE.get();
        fiberRouteDebugLogging = FIBER_ROUTE_DEBUG_LOGGING_VALUE.get();
        fiberCableCanBeCutByOwner = FIBER_CABLE_CAN_BE_CUT_BY_OWNER_VALUE.get();
        fiberCableCanBeCutByOtherPlayers = FIBER_CABLE_CAN_BE_CUT_BY_OTHER_PLAYERS_VALUE.get();
        fiberCableCutRequiresShears = FIBER_CABLE_CUT_REQUIRES_SHEARS_VALUE.get();
        fiberCableCutReach = clamp(FIBER_CABLE_CUT_REACH_VALUE.get(), 1.0D, 16.0D);
        fiberCableInteractionRadius = clamp(FIBER_CABLE_INTERACTION_RADIUS_VALUE.get(), 0.02D, 0.50D);
        fiberCableCutLineOfSightRequired = FIBER_CABLE_CUT_LINE_OF_SIGHT_REQUIRED_VALUE.get();
        fiberCableTargetIndicator = FIBER_CABLE_TARGET_INDICATOR_VALUE.get();
        fiberCableDebugVisualThickness = FIBER_CABLE_DEBUG_VISUAL_THICKNESS_VALUE.get();
        fiberCableSegmentSpacing = FIBER_CABLE_SEGMENT_SPACING_VALUE.get();
        fiberCableSegmentHealth = FIBER_CABLE_SEGMENT_HEALTH_VALUE.get();
        fiberCableSegmentHitboxSize = FIBER_CABLE_SEGMENT_HITBOX_SIZE_VALUE.get();
        fiberCableCanBeDamagedByPlayers = FIBER_CABLE_CAN_BE_DAMAGED_BY_PLAYERS_VALUE.get();
        fiberCableCanBeDamagedByProjectiles = FIBER_CABLE_CAN_BE_DAMAGED_BY_PROJECTILES_VALUE.get();
        fiberCableCanBeDamagedByExplosions = FIBER_CABLE_CAN_BE_DAMAGED_BY_EXPLOSIONS_VALUE.get();
        fiberCableProjectileRaycastBreak = FIBER_CABLE_PROJECTILE_RAYCAST_BREAK_VALUE.get();
        fiberCableBreaksWhenAnySegmentDestroyed = FIBER_CABLE_BREAKS_WHEN_ANY_SEGMENT_DESTROYED_VALUE.get();
        maxFiberCableSegmentsPerDrone = MAX_FIBER_CABLE_SEGMENTS_PER_DRONE_VALUE.get();
        debugFiberCableRender = DEBUG_FIBER_CABLE_RENDER_VALUE.get();
        debugFiberCableSegments = DEBUG_FIBER_CABLE_SEGMENTS_VALUE.get();
        renderRedDebugCable = RENDER_RED_DEBUG_CABLE_VALUE.get();
        renderLegacyFiberLine = RENDER_LEGACY_FIBER_LINE_VALUE.get();
        weatherEffectsEnabled = WEATHER_EFFECTS_ENABLED_VALUE.get();
        rainVisibilityMultiplier = RAIN_VISIBILITY_MULTIPLIER_VALUE.get();
        stormInstabilityStrength = STORM_INSTABILITY_STRENGTH_VALUE.get();
        enableDroneScreenshotSound = ENABLE_DRONE_SCREENSHOT_SOUND_VALUE.get();
        fpvCameraFov = FPV_CAMERA_FOV_VALUE.get();
        fpvCameraTiltDegrees = FPV_CAMERA_TILT_DEGREES_VALUE.get();
        enableFpvFisheyeEffect = ENABLE_FPV_FISHEYE_EFFECT_VALUE.get();
        enableFpvCameraVibration = ENABLE_FPV_CAMERA_VIBRATION_VALUE.get();
        droneEngineVolumeMultiplier = DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE.get();
        lucasEngineVolumeMultiplier = LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE.get();
        droneMonitorHumVolumeMultiplier = DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE.get();
        droneAirborneVolumeMultiplier = DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE.get();
        cubedFpvExternalVolume = clamp(CUBED_FPV_EXTERNAL_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvNearVolume = clamp(CUBED_FPV_NEAR_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvMidVolume = clamp(CUBED_FPV_MID_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvFarVolume = clamp(CUBED_FPV_FAR_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasExternalVolume = clamp(LUCAS_EXTERNAL_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasNearVolume = clamp(LUCAS_NEAR_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasMidVolume = clamp(LUCAS_MID_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasFarVolume = clamp(LUCAS_FAR_VOLUME_VALUE.get(),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        fpvInternalAudioGain = clamp(FPV_INTERNAL_AUDIO_GAIN_VALUE.get(),
                DroneAudioConfigLimits.MIN_INTERNAL_AUDIO_GAIN,
                DroneAudioConfigLimits.MAX_INTERNAL_AUDIO_GAIN);
        droneDopplerEnabled = DRONE_DOPPLER_ENABLED_VALUE.get();
        droneDopplerStrength = clamp(DRONE_DOPPLER_STRENGTH_VALUE.get(),
                DroneAudioConfigLimits.MIN_DOPPLER_STRENGTH,
                DroneAudioConfigLimits.MAX_DOPPLER_STRENGTH);
        droneAudioOcclusion = DRONE_AUDIO_OCCLUSION_VALUE.get();
        maxAudibleCubedFpvDrones = Math.max(
                DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                Math.min(DroneAudioConfigLimits.MAX_AUDIBLE_DRONES, MAX_AUDIBLE_CUBED_FPV_DRONES_VALUE.get())
        );
        maxAudibleLucasDrones = Math.max(
                DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                Math.min(DroneAudioConfigLimits.MAX_AUDIBLE_DRONES, MAX_AUDIBLE_LUCAS_DRONES_VALUE.get())
        );
        enableSpotlightModule = ENABLE_SPOTLIGHT_MODULE_VALUE.get();
        spotlightRange = SPOTLIGHT_RANGE_VALUE.get();
        spotlightEnergyCostPerSecond = SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE.get();
        spotlightRequiresModule = SPOTLIGHT_REQUIRES_MODULE_VALUE.get();
        spotlightVisibleToOtherPlayers = SPOTLIGHT_VISIBLE_TO_OTHER_PLAYERS_VALUE.get();
        spotlightAutoOffWhenBatteryEmpty = SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE.get();
        spotlightRealLightEnabled = SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE.get();
        spotlightLightRange = SPOTLIGHT_LIGHT_RANGE_VALUE.get();
        spotlightLightLevel = SPOTLIGHT_LIGHT_LEVEL_VALUE.get();
        spotlightLightUpdateIntervalTicks = SPOTLIGHT_LIGHT_UPDATE_INTERVAL_TICKS_VALUE.get();
        debugRadarTowerMount = DEBUG_RADAR_TOWER_MOUNT_VALUE.get();
        radarTowerRoofAnchorX = RADAR_TOWER_ROOF_ANCHOR_X_VALUE.get();
        radarTowerRoofAnchorY = RADAR_TOWER_ROOF_ANCHOR_Y_VALUE.get();
        radarTowerRoofAnchorZ = RADAR_TOWER_ROOF_ANCHOR_Z_VALUE.get();
        radarTowerCageYaw = RADAR_TOWER_CAGE_YAW_VALUE.get();
        radarTowerCagePitch = RADAR_TOWER_CAGE_PITCH_VALUE.get();
        radarTowerCageRoll = RADAR_TOWER_CAGE_ROLL_VALUE.get();
        radarTowerCageScale = RADAR_TOWER_CAGE_SCALE_VALUE.get();
        radarTowerMaxBatteryCapacity = RADAR_TOWER_MAX_BATTERY_CAPACITY_VALUE.get();
        radarTowerSolarChargeRate = RADAR_TOWER_SOLAR_CHARGE_RATE_VALUE.get();
        radarTowerChargingStationChargeRate = RADAR_TOWER_CHARGING_STATION_CHARGE_RATE_VALUE.get();
        radarTowerJammerEnergyDrainRate = RADAR_TOWER_JAMMER_ENERGY_DRAIN_RATE_VALUE.get();
        radarTowerMinimumEnergyToActivate = RADAR_TOWER_MINIMUM_ENERGY_TO_ACTIVATE_VALUE.get();
        antiVehicleMortarVehicleDamage = ANTI_VEHICLE_MORTAR_VEHICLE_DAMAGE_VALUE.get();
        antiVehicleMortarEntityDamage = ANTI_VEHICLE_MORTAR_ENTITY_DAMAGE_VALUE.get();
        antiVehicleMortarExplosionRadius = ANTI_VEHICLE_MORTAR_EXPLOSION_RADIUS_VALUE.get();
        antiVehicleMortarBlockDamageRadius = ANTI_VEHICLE_MORTAR_BLOCK_DAMAGE_RADIUS_VALUE.get();
        antiVehicleMortarPayloadCooldownTicks = ANTI_VEHICLE_MORTAR_PAYLOAD_COOLDOWN_TICKS_VALUE.get();
        debugPayloadMounts = DEBUG_PAYLOAD_MOUNTS_VALUE.get();
    }

    public static int droneMaxRange() {
        return droneMaxRange;
    }

    public static int droneVisualTrackingRange() {
        return droneVisualTrackingRange;
    }

    public static double droneSpeedMultiplier() {
        return droneSpeedMultiplier;
    }

    public static boolean enableDroneChunkLoading() {
        return enableDroneChunkLoading;
    }

    public static int droneChunkLoadRadius() {
        return droneChunkLoadRadius;
    }

    public static boolean enableDronePlayerAnchor() {
        return enableDronePlayerAnchor;
    }

    public static boolean enableDroneCrashExplosionDamage() {
        return enableDroneCrashExplosionDamage;
    }

    public static boolean hidePlayerOnMinimapDuringDroneControl() {
        return hidePlayerOnMinimapDuringDroneControl;
    }

    public static boolean showDroneMarkerInstead() {
        return showDroneMarkerInstead;
    }

    public static int lucasTakeoffAssistTicks() {
        return lucasTakeoffAssistTicks;
    }

    public static double lucasTakeoffForwardBoost() {
        return lucasTakeoffForwardBoost;
    }

    public static double lucasTakeoffUpBoost() {
        return lucasTakeoffUpBoost;
    }

    public static double lucasMinSpeed() {
        return lucasMinSpeed;
    }

    public static double lucasMaxSpeed() {
        return lucasMaxSpeed;
    }

    public static double lucasAcceleration() {
        return lucasAcceleration;
    }

    public static double lucasDrag() {
        return lucasDrag;
    }

    public static double lucasTurnRate() {
        return lucasTurnRate;
    }

    public static double lucasPitchRate() {
        return lucasPitchRate;
    }

    public static double lucasClimbSpeed() {
        return lucasClimbSpeed;
    }

    public static double lucasDescendSpeed() {
        return lucasDescendSpeed;
    }

    public static double lucasLiftStrength() {
        return lucasLiftStrength;
    }

    public static double lucasRollVisualAmount() {
        return lucasRollVisualAmount;
    }

    public static double lucasMinLiftSpeed() {
        return lucasMinLiftSpeed;
    }

    public static double lucasStallGravity() {
        return lucasStallGravity;
    }

    public static boolean lucasStallEnabled() {
        return lucasStallEnabled;
    }

    public static boolean lucasCanHover() {
        return lucasCanHover;
    }

    public static int lucasMaxFuel() {
        return lucasMaxFuel;
    }

    public static double lucasFuelDrainIdle() {
        return lucasFuelDrainIdle;
    }

    public static double lucasFuelDrainFlying() {
        return lucasFuelDrainFlying;
    }

    public static double lucasFuelDrainClimb() {
        return lucasFuelDrainClimb;
    }

    public static int lucasLowFuelWarningPercent() {
        return lucasLowFuelWarningPercent;
    }

    public static boolean lucasCanFlyWithoutFuel() {
        return lucasCanFlyWithoutFuel;
    }

    public static double lucasHudScale() {
        return lucasHudScale;
    }

    public static int fuelMixerProcessTime() {
        return fuelMixerProcessTime;
    }

    public static int gasolineJerrycanFuelAmount() {
        return gasolineJerrycanFuelAmount;
    }

    public static boolean enableThermalVision() {
        return enableThermalVision;
    }

    public static int thermalVisionRange() {
        return thermalVisionRange;
    }

    public static boolean thermalVisionKeybindEnabled() {
        return thermalVisionKeybindEnabled;
    }

    public static boolean thermalVisionNoiseEffect() {
        return thermalVisionNoiseEffect;
    }

    public static boolean thermalHighlightPlayers() {
        return thermalHighlightPlayers;
    }

    public static boolean thermalHighlightHostileMobs() {
        return thermalHighlightHostileMobs;
    }

    public static boolean thermalHighlightAnimals() {
        return thermalHighlightAnimals;
    }

    public static boolean thermalHighlightMobs() {
        return thermalHighlightHostileMobs || thermalHighlightAnimals;
    }

    public static boolean enableBatterySystem() {
        return enableBatterySystem;
    }

    public static int maxEnergy() {
        return maxEnergy;
    }

    public static int energyUsePerTick() {
        return energyUsePerTick;
    }

    public static int idleEnergyUsePerTick() {
        return idleEnergyUsePerTick;
    }

    public static int lowEnergyThreshold() {
        return lowEnergyThreshold;
    }

    public static boolean returnHomeOnEmpty() {
        return returnHomeOnEmpty;
    }

    public static int batteryTransferRate() {
        return batteryTransferRate;
    }

    public static boolean enableDroneSiren() {
        return enableDroneSiren;
    }

    public static int droneSirenRange() {
        return droneSirenRange;
    }

    public static boolean droneSirenRedstoneOutput() {
        return droneSirenRedstoneOutput;
    }

    public static int droneSirenCooldownTicks() {
        return droneSirenCooldownTicks;
    }

    public static boolean detectFriendlyDrones() {
        return detectFriendlyDrones;
    }

    public static Set<String> ignoredDroneOwnerNames() {
        return ignoredDroneOwnerNames;
    }

    public static Set<UUID> ignoredDroneOwnerUUIDs() {
        return ignoredDroneOwnerUUIDs;
    }

    public static boolean ignoreOwnerlessDrones() {
        return ignoreOwnerlessDrones;
    }

    public static Set<String> ignoredDroneNames() {
        return ignoredDroneNames;
    }

    public static boolean ignoreUnnamedDrones() {
        return ignoreUnnamedDrones;
    }

    public static boolean blacklistMatchCaseInsensitive() {
        return blacklistMatchCaseInsensitive;
    }

    public static boolean enableDroneJammer() {
        return enableDroneJammer;
    }

    public static int jammerRange() {
        return jammerRange;
    }

    public static int jammerBuildUpTicks() {
        return jammerBuildUpTicks;
    }

    public static int jammerRecoveryTicks() {
        return jammerRecoveryTicks;
    }

    public static double hardJamThreshold() {
        return hardJamThreshold;
    }

    public static double controlInterferenceThreshold() {
        return controlInterferenceThreshold;
    }

    public static double overlayStartThreshold() {
        return overlayStartThreshold;
    }

    public static boolean debugJammer() {
        return debugJammer;
    }

    public static DroneJammerMode jammerMode() {
        return jammerMode;
    }

    public static boolean affectFriendlyDrones() {
        return affectFriendlyDrones;
    }

    public static int droneRadarDetectionRange() {
        return droneRadarDetectionRange;
    }

    public static boolean droneRadarBeepEnabled() {
        return droneRadarBeepEnabled;
    }

    public static double droneRadarBeepVolume() {
        return droneRadarBeepVolume;
    }

    public static int droneRadarMinBeepIntervalTicks() {
        return droneRadarMinBeepIntervalTicks;
    }

    public static int droneRadarMaxBeepIntervalTicks() {
        return droneRadarMaxBeepIntervalTicks;
    }

    public static boolean droneRadarOnlyBeepWhenHeld() {
        return droneRadarOnlyBeepWhenHeld;
    }

    public static double droneRadarHudScale() {
        return droneRadarHudScale;
    }

    public static int droneRadarHudXOffset() {
        return droneRadarHudXOffset;
    }

    public static int droneRadarHudYOffset() {
        return droneRadarHudYOffset;
    }

    public static boolean droneRadarCompactMode() {
        return droneRadarCompactMode;
    }

    public static boolean debugDroneRadar() {
        return debugDroneRadar;
    }

    public static int portableFpvDetectorRange() {
        return portableFpvDetectorRange;
    }

    public static int portableFpvDetectorScanIntervalTicks() {
        return portableFpvDetectorScanIntervalTicks;
    }

    public static int portableFpvDetectorMaximumTargets() {
        return portableFpvDetectorMaximumTargets;
    }

    public static boolean portableFpvDetectorVideoEnabled() {
        return portableFpvDetectorVideoEnabled;
    }

    public static boolean portableFpvDetectorDetectOwnDrones() {
        return portableFpvDetectorDetectOwnDrones;
    }

    public static boolean portableFpvDetectorRequireLineOfSight() {
        return portableFpvDetectorRequireLineOfSight;
    }

    public static boolean portableFpvDetectorHudEnabled() {
        return portableFpvDetectorHudEnabled;
    }

    public static boolean portableFpvDetectorShowSignalPercent() {
        return portableFpvDetectorShowSignalPercent;
    }

    public static int laserDefenseTowerDroneRange() {
        return laserDefenseTowerDroneRange;
    }

    public static boolean laserDefenseTowerWhitelistEnabled() {
        return laserDefenseTowerWhitelistEnabled;
    }

    public static boolean enableFiberOpticMode() {
        return enableFiberOpticMode;
    }

    public static int fiberOpticCableLength() {
        return fiberOpticCableLength;
    }

    public static int fiberOpticCableBreakDelayTicks() {
        return fiberOpticCableBreakDelayTicks;
    }

    public static double fiberOpticExtraBatteryDrainMultiplier() {
        return fiberOpticExtraBatteryDrainMultiplier;
    }

    public static boolean normalJammersAffectFiberOptic() {
        return normalJammersAffectFiberOptic;
    }

    public static boolean antiDroneNetCutsFiber() {
        return antiDroneNetCutsFiber;
    }

    public static boolean antiDroneNetPanelRequiresSupport() {
        return antiDroneNetPanelRequiresSupport;
    }

    public static double antiDroneNetPanelTriggerThickness() {
        return antiDroneNetPanelTriggerThickness;
    }

    public static double antiDroneNetPanelVisualThickness() {
        return antiDroneNetPanelVisualThickness;
    }

    public static boolean vehicleAntiDroneNetEnabled() {
        return vehicleAntiDroneNetEnabled;
    }

    public static double vehicleAntiDroneNetHorizontalPadding() {
        return vehicleAntiDroneNetHorizontalPadding;
    }

    public static double vehicleAntiDroneNetTopPadding() {
        return vehicleAntiDroneNetTopPadding;
    }

    public static boolean vehicleAntiDroneNetStopsDrones() {
        return vehicleAntiDroneNetStopsDrones;
    }

    public static double vehicleAntiDroneNetOpacity() {
        return vehicleAntiDroneNetOpacity;
    }

    public static boolean netBulkPlacementEnabled() {
        return netBulkPlacementEnabled;
    }

    public static int netBulkPlacementMaxWidth() {
        return netBulkPlacementMaxWidth;
    }

    public static int netBulkPlacementMaxHeight() {
        return netBulkPlacementMaxHeight;
    }

    public static int netBulkPlacementMaxBlocks() {
        return netBulkPlacementMaxBlocks;
    }

    public static double netBulkPlacementMaximumReach() {
        return netBulkPlacementMaximumReach;
    }

    public static boolean netBulkPlacementAllOrNothing() {
        return netBulkPlacementAllOrNothing;
    }

    public static boolean netBulkPlacementSkipBlocked() {
        return netBulkPlacementSkipBlocked;
    }

    public static int netBulkPlacementBlocksPerTick() {
        return netBulkPlacementBlocksPerTick;
    }

    public static int netBulkPlacementUndoSeconds() {
        return netBulkPlacementUndoSeconds;
    }

    public static boolean keepDetachedFiberCableOnDroneDeath() {
        return keepDetachedFiberCableOnDroneDeath;
    }

    public static int detachedFiberCableLifetimeTicks() {
        return detachedFiberCableLifetimeTicks;
    }

    public static boolean debugFiberOptic() {
        return debugFiberOptic;
    }

    public static double fiberCableVisualThickness() {
        return fiberCableVisualThickness;
    }

    public static double fiberCableVisualAlpha() {
        return fiberCableVisualAlpha;
    }

    public static String fiberCableVisualColor() {
        return fiberCableVisualColor;
    }

    public static double fiberCableMaxRenderDistance() {
        return fiberCableMaxRenderDistance;
    }

    public static int maxVisibleFiberCables() {
        return maxVisibleFiberCables;
    }

    public static boolean fiberRouteHistoryEnabled() {
        return fiberRouteHistoryEnabled;
    }

    public static double fiberRoutePointSpacing() {
        return fiberRoutePointSpacing;
    }

    public static double fiberRouteMinimumTurnDistance() {
        return fiberRouteMinimumTurnDistance;
    }

    public static double fiberRouteTurnAngleThreshold() {
        return fiberRouteTurnAngleThreshold;
    }

    public static int fiberRouteMaximumPoints() {
        return fiberRouteMaximumPoints;
    }

    public static boolean fiberRouteCornerSmoothing() {
        return fiberRouteCornerSmoothing;
    }

    public static boolean fiberRouteDebugLogging() {
        return fiberRouteDebugLogging;
    }

    public static boolean fiberCableCanBeCutByOwner() {
        return fiberCableCanBeCutByOwner;
    }

    public static boolean fiberCableCanBeCutByOtherPlayers() {
        return fiberCableCanBeCutByOtherPlayers;
    }

    public static boolean fiberCableCutRequiresShears() {
        return fiberCableCutRequiresShears;
    }

    public static double fiberCableCutReach() {
        return fiberCableCutReach;
    }

    public static double fiberCableInteractionRadius() {
        return fiberCableInteractionRadius;
    }

    public static boolean fiberCableCutLineOfSightRequired() {
        return fiberCableCutLineOfSightRequired;
    }

    public static boolean fiberCableTargetIndicator() {
        return fiberCableTargetIndicator;
    }

    public static double fiberCableDebugVisualThickness() {
        return fiberCableDebugVisualThickness;
    }

    public static double fiberCableSegmentSpacing() {
        return fiberCableSegmentSpacing;
    }

    public static double fiberCableSegmentHealth() {
        return fiberCableSegmentHealth;
    }

    public static double fiberCableSegmentHitboxSize() {
        return fiberCableSegmentHitboxSize;
    }

    public static boolean fiberCableCanBeDamagedByPlayers() {
        return fiberCableCanBeDamagedByPlayers;
    }

    public static boolean fiberCableCanBeDamagedByProjectiles() {
        return fiberCableCanBeDamagedByProjectiles;
    }

    public static boolean fiberCableCanBeDamagedByExplosions() {
        return fiberCableCanBeDamagedByExplosions;
    }

    public static boolean fiberCableProjectileRaycastBreak() {
        return fiberCableProjectileRaycastBreak;
    }

    public static boolean fiberCableBreaksWhenAnySegmentDestroyed() {
        return fiberCableBreaksWhenAnySegmentDestroyed;
    }

    public static int maxFiberCableSegmentsPerDrone() {
        return maxFiberCableSegmentsPerDrone;
    }

    public static boolean debugFiberCableRender() {
        return debugFiberCableRender;
    }

    public static boolean debugFiberCableSegments() {
        return debugFiberCableSegments;
    }

    public static boolean renderRedDebugCable() {
        return renderRedDebugCable;
    }

    public static boolean renderLegacyFiberLine() {
        return renderLegacyFiberLine;
    }

    public static boolean weatherEffectsEnabled() {
        return weatherEffectsEnabled;
    }

    public static double rainVisibilityMultiplier() {
        return rainVisibilityMultiplier;
    }

    public static double stormInstabilityStrength() {
        return stormInstabilityStrength;
    }

    public static boolean enableDroneScreenshotSound() {
        return enableDroneScreenshotSound;
    }

    public static double fpvCameraFov() {
        return fpvCameraFov;
    }

    public static double fpvCameraTiltDegrees() {
        return fpvCameraTiltDegrees;
    }

    public static boolean enableFpvFisheyeEffect() {
        return enableFpvFisheyeEffect;
    }

    public static boolean enableFpvCameraVibration() {
        return enableFpvCameraVibration;
    }

    public static double droneEngineVolumeMultiplier() {
        return droneEngineVolumeMultiplier;
    }

    public static double lucasEngineVolumeMultiplier() {
        return lucasEngineVolumeMultiplier;
    }

    public static double droneMonitorHumVolumeMultiplier() {
        return droneMonitorHumVolumeMultiplier;
    }

    public static double droneAirborneVolumeMultiplier() {
        return droneAirborneVolumeMultiplier;
    }

    public static double cubedFpvExternalVolume() {
        return cubedFpvExternalVolume;
    }

    public static double cubedFpvNearVolume() {
        return cubedFpvNearVolume;
    }

    public static double cubedFpvMidVolume() {
        return cubedFpvMidVolume;
    }

    public static double cubedFpvFarVolume() {
        return cubedFpvFarVolume;
    }

    public static double lucasExternalVolume() {
        return lucasExternalVolume;
    }

    public static double lucasNearVolume() {
        return lucasNearVolume;
    }

    public static double lucasMidVolume() {
        return lucasMidVolume;
    }

    public static double lucasFarVolume() {
        return lucasFarVolume;
    }

    public static double fpvInternalAudioGain() {
        return fpvInternalAudioGain;
    }

    public static boolean droneDopplerEnabled() {
        return droneDopplerEnabled;
    }

    public static double droneDopplerStrength() {
        return droneDopplerStrength;
    }

    public static boolean droneAudioOcclusion() {
        return droneAudioOcclusion;
    }

    public static int maxAudibleCubedFpvDrones() {
        return maxAudibleCubedFpvDrones;
    }

    public static int maxAudibleLucasDrones() {
        return maxAudibleLucasDrones;
    }

    public static boolean enableSpotlightModule() {
        return enableSpotlightModule;
    }

    public static int spotlightRange() {
        return spotlightRange;
    }

    public static int spotlightEnergyCostPerSecond() {
        return spotlightEnergyCostPerSecond;
    }

    public static boolean spotlightRequiresModule() {
        return spotlightRequiresModule;
    }

    public static boolean spotlightVisibleToOtherPlayers() {
        return spotlightVisibleToOtherPlayers;
    }

    public static boolean spotlightAutoOffWhenBatteryEmpty() {
        return spotlightAutoOffWhenBatteryEmpty;
    }

    public static boolean spotlightRealLightEnabled() {
        return spotlightRealLightEnabled;
    }

    public static int spotlightLightRange() {
        return spotlightLightRange;
    }

    public static int spotlightLightLevel() {
        return spotlightLightLevel;
    }

    public static int spotlightLightUpdateIntervalTicks() {
        return spotlightLightUpdateIntervalTicks;
    }

    public static double antiVehicleMortarVehicleDamage() {
        return antiVehicleMortarVehicleDamage;
    }

    public static double antiVehicleMortarEntityDamage() {
        return antiVehicleMortarEntityDamage;
    }

    public static double antiVehicleMortarExplosionRadius() {
        return antiVehicleMortarExplosionRadius;
    }

    public static double antiVehicleMortarBlockDamageRadius() {
        return antiVehicleMortarBlockDamageRadius;
    }

    public static int antiVehicleMortarPayloadCooldownTicks() {
        return antiVehicleMortarPayloadCooldownTicks;
    }

    public static boolean debugPayloadMounts() {
        return debugPayloadMounts;
    }

    public static boolean debugRadarTowerMount() {
        return debugRadarTowerMount;
    }

    public static double radarTowerRoofAnchorX() {
        return radarTowerRoofAnchorX;
    }

    public static double radarTowerRoofAnchorY() {
        return radarTowerRoofAnchorY;
    }

    public static double radarTowerRoofAnchorZ() {
        return radarTowerRoofAnchorZ;
    }

    public static double radarTowerCageYaw() {
        return radarTowerCageYaw;
    }

    public static double radarTowerCagePitch() {
        return radarTowerCagePitch;
    }

    public static double radarTowerCageRoll() {
        return radarTowerCageRoll;
    }

    public static double radarTowerCageScale() {
        return radarTowerCageScale;
    }

    public static int radarTowerMaxBatteryCapacity() {
        return radarTowerMaxBatteryCapacity;
    }

    public static int radarTowerSolarChargeRate() {
        return radarTowerSolarChargeRate;
    }

    public static int radarTowerChargingStationChargeRate() {
        return radarTowerChargingStationChargeRate;
    }

    public static int radarTowerJammerEnergyDrainRate() {
        return radarTowerJammerEnergyDrainRate;
    }

    public static int radarTowerMinimumEnergyToActivate() {
        return radarTowerMinimumEnergyToActivate;
    }

    private static Set<String> normalizeOwnerNames(List<? extends String> rawNames) {
        return normalizeConfiguredNames(rawNames, true);
    }

    private static Set<String> normalizeConfiguredNames(List<? extends String> rawNames, boolean caseInsensitive) {
        LinkedHashSet<String> normalizedNames = new LinkedHashSet<>();
        for (String rawName : rawNames) {
            if (rawName == null) {
                continue;
            }
            String normalized = normalizeName(rawName, caseInsensitive);
            if (!normalized.isEmpty()) {
                normalizedNames.add(normalized);
            }
        }
        return Set.copyOf(normalizedNames);
    }

    private static Set<UUID> parseOwnerUuids(List<? extends String> rawUuids) {
        LinkedHashSet<UUID> uuids = new LinkedHashSet<>();
        for (String rawUuid : rawUuids) {
            if (rawUuid == null) {
                continue;
            }
            String trimmed = rawUuid.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                uuids.add(UUID.fromString(trimmed));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return Set.copyOf(uuids);
    }

    public static String normalizeName(String rawName, boolean caseInsensitive) {
        if (rawName == null) {
            return "";
        }
        String normalized = rawName.trim();
        if (caseInsensitive) {
            normalized = normalized.toLowerCase(Locale.ROOT);
        }
        return normalized;
    }

    private static double clampThreshold(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean isSupportedFiberCableVisualColor(String rawValue) {
        String normalized = normalizeFiberCableVisualColor(rawValue);
        return "charcoal".equals(normalized)
                || "light_gray".equals(normalized)
                || "off_white".equals(normalized)
                || "pale_gray".equals(normalized);
    }

    private static String normalizeFiberCableVisualColor(String rawValue) {
        if (rawValue == null) {
            return "charcoal";
        }

        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "charcoal", "off_white", "pale_gray", "light_gray" -> normalized;
            default -> "charcoal";
        };
    }

    public static void save() {
        SPEC.save();
        bake();
        if (ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            DroneTrackingRangeOverride.applyConfiguredRange();
        }
    }
}
