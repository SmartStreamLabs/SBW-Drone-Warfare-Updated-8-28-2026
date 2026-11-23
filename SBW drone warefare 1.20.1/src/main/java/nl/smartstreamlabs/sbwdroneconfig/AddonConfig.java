package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = SbwDroneRangeConfig.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AddonConfig {
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_REMOTE_CHUNK_TRACKING_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_EXTRA_TICKETS_PER_TICK_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_EXTRA_PENDING_CHUNKS_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_EXTRA_TICKET_LIMIT_VALUE;
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    private static final Set<ForgeConfigSpec.ConfigValue<?>> LOCAL_VALUES;
    public static final ForgeConfigSpec.IntValue DRONE_MAX_RANGE_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_VISUAL_TRACKING_RANGE_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_SPEED_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_CHUNK_LOADING_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_CHUNK_LOAD_RADIUS_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_PROACTIVE_LOAD_RADIUS_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_DRONE_CHUNK_LOADING_VALUE;
    public static final ForgeConfigSpec.BooleanValue EXPERIMENTAL_DRONE_CLIENT_CHUNK_CENTER_SYNC_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_PLAYER_ANCHOR_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE;
    public static final ForgeConfigSpec.IntValue LUCAS_TAKEOFF_ASSIST_TICKS_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_TAKEOFF_FORWARD_BOOST_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_TAKEOFF_UP_BOOST_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_MIN_SPEED_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_MAX_SPEED_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_ACCELERATION_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_DRAG_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_TURN_RATE_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_PITCH_RATE_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_CLIMB_SPEED_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_DESCEND_SPEED_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_LIFT_STRENGTH_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_ROLL_VISUAL_AMOUNT_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_MIN_LIFT_SPEED_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_STALL_GRAVITY_VALUE;
    public static final ForgeConfigSpec.BooleanValue LUCAS_STALL_ENABLED_VALUE;
    public static final ForgeConfigSpec.BooleanValue LUCAS_CAN_HOVER_VALUE;
    public static final ForgeConfigSpec.IntValue LUCAS_MAX_FUEL_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_IDLE_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_FLYING_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_FUEL_DRAIN_CLIMB_VALUE;
    public static final ForgeConfigSpec.IntValue LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE;
    public static final ForgeConfigSpec.BooleanValue LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_HUD_SCALE_VALUE;
    public static final ForgeConfigSpec.IntValue FUEL_MIXER_PROCESS_TIME_VALUE;
    public static final ForgeConfigSpec.IntValue GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_THERMAL_VISION_VALUE;
    public static final ForgeConfigSpec.IntValue THERMAL_VISION_RANGE_VALUE;
    public static final ForgeConfigSpec.BooleanValue THERMAL_VISION_KEYBIND_ENABLED_VALUE;
    public static final ForgeConfigSpec.BooleanValue THERMAL_VISION_NOISE_EFFECT_VALUE;
    public static final ForgeConfigSpec.BooleanValue THERMAL_HIGHLIGHT_PLAYERS_VALUE;
    public static final ForgeConfigSpec.BooleanValue THERMAL_HIGHLIGHT_HOSTILE_MOBS_VALUE;
    public static final ForgeConfigSpec.BooleanValue THERMAL_HIGHLIGHT_ANIMALS_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_BATTERY_SYSTEM_VALUE;
    public static final ForgeConfigSpec.IntValue MAX_ENERGY_VALUE;
    public static final ForgeConfigSpec.IntValue ENERGY_USE_PER_TICK_VALUE;
    public static final ForgeConfigSpec.IntValue IDLE_ENERGY_USE_PER_TICK_VALUE;
    public static final ForgeConfigSpec.IntValue LOW_ENERGY_THRESHOLD_VALUE;
    public static final ForgeConfigSpec.BooleanValue RETURN_HOME_ON_EMPTY_VALUE;
    public static final ForgeConfigSpec.IntValue BATTERY_TRANSFER_RATE_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_SIREN_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_SIREN_RANGE_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_SIREN_REDSTONE_OUTPUT_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_SIREN_COOLDOWN_TICKS_VALUE;
    public static final ForgeConfigSpec.BooleanValue DETECT_FRIENDLY_DRONES_VALUE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_OWNER_NAMES_VALUE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_OWNER_UUIDS_VALUE;
    public static final ForgeConfigSpec.BooleanValue IGNORE_OWNERLESS_DRONES_VALUE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_DRONE_NAMES_VALUE;
    public static final ForgeConfigSpec.BooleanValue IGNORE_UNNAMED_DRONES_VALUE;
    public static final ForgeConfigSpec.BooleanValue BLACKLIST_MATCH_CASE_INSENSITIVE_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_JAMMER_VALUE;
    public static final ForgeConfigSpec.IntValue JAMMER_RANGE_VALUE;
    public static final ForgeConfigSpec.IntValue JAMMER_BUILD_UP_TICKS_VALUE;
    public static final ForgeConfigSpec.IntValue JAMMER_RECOVERY_TICKS_VALUE;
    public static final ForgeConfigSpec.DoubleValue HARD_JAM_THRESHOLD_VALUE;
    public static final ForgeConfigSpec.DoubleValue CONTROL_INTERFERENCE_THRESHOLD_VALUE;
    public static final ForgeConfigSpec.DoubleValue OVERLAY_START_THRESHOLD_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_JAMMER_VALUE;
    public static final ForgeConfigSpec.EnumValue<DroneJammerMode> JAMMER_MODE_VALUE;
    public static final ForgeConfigSpec.BooleanValue AFFECT_FRIENDLY_DRONES_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_RADAR_DETECTION_RANGE_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_RADAR_BEEP_ENABLED_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_RADAR_BEEP_VOLUME_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_RADAR_HUD_SCALE_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_RADAR_HUD_X_OFFSET_VALUE;
    public static final ForgeConfigSpec.IntValue DRONE_RADAR_HUD_Y_OFFSET_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_RADAR_COMPACT_MODE_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_DRONE_RADAR_VALUE;
    public static final ForgeConfigSpec.IntValue PORTABLE_FPV_DETECTOR_RANGE_VALUE;
    public static final ForgeConfigSpec.IntValue PORTABLE_FPV_DETECTOR_SCAN_INTERVAL_TICKS_VALUE;
    public static final ForgeConfigSpec.IntValue PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE;
    public static final ForgeConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE;
    public static final ForgeConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE;
    public static final ForgeConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE;
    public static final ForgeConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE;
    public static final ForgeConfigSpec.BooleanValue PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FIBER_OPTIC_MODE_VALUE;
    public static final ForgeConfigSpec.IntValue FIBER_OPTIC_CABLE_LENGTH_VALUE;
    public static final ForgeConfigSpec.IntValue FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.BooleanValue NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE;
    public static final ForgeConfigSpec.BooleanValue ANTI_DRONE_NET_CUTS_FIBER_VALUE;
    public static final ForgeConfigSpec.BooleanValue ANTI_DRONE_NET_PANEL_REQUIRES_SUPPORT_VALUE;
    public static final ForgeConfigSpec.DoubleValue ANTI_DRONE_NET_PANEL_TRIGGER_THICKNESS_VALUE;
    public static final ForgeConfigSpec.DoubleValue ANTI_DRONE_NET_PANEL_VISUAL_THICKNESS_VALUE;
    public static final ForgeConfigSpec.BooleanValue VEHICLE_ANTI_DRONE_NET_ENABLED_VALUE;
    public static final ForgeConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE;
    public static final ForgeConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE;
    public static final ForgeConfigSpec.BooleanValue VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE;
    public static final ForgeConfigSpec.DoubleValue VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE;
    public static final ForgeConfigSpec.BooleanValue NET_BULK_PLACEMENT_ENABLED_VALUE;
    public static final ForgeConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_WIDTH_VALUE;
    public static final ForgeConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_HEIGHT_VALUE;
    public static final ForgeConfigSpec.IntValue NET_BULK_PLACEMENT_MAX_BLOCKS_VALUE;
    public static final ForgeConfigSpec.DoubleValue NET_BULK_PLACEMENT_MAXIMUM_REACH_VALUE;
    public static final ForgeConfigSpec.BooleanValue NET_BULK_PLACEMENT_ALL_OR_NOTHING_VALUE;
    public static final ForgeConfigSpec.BooleanValue NET_BULK_PLACEMENT_SKIP_BLOCKED_VALUE;
    public static final ForgeConfigSpec.IntValue NET_BULK_PLACEMENT_BLOCKS_PER_TICK_VALUE;
    public static final ForgeConfigSpec.IntValue NET_BULK_PLACEMENT_UNDO_SECONDS_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_FIBER_OPTIC_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_VISUAL_THICKNESS_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_VISUAL_ALPHA_VALUE;
    public static final ForgeConfigSpec.ConfigValue<String> FIBER_CABLE_VISUAL_COLOR_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_MAX_RENDER_DISTANCE_VALUE;
    public static final ForgeConfigSpec.IntValue MAX_VISIBLE_FIBER_CABLES_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_ROUTE_HISTORY_ENABLED_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_ROUTE_POINT_SPACING_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_ROUTE_MINIMUM_TURN_DISTANCE_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_ROUTE_TURN_ANGLE_THRESHOLD_VALUE;
    public static final ForgeConfigSpec.IntValue FIBER_ROUTE_MAXIMUM_POINTS_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_ROUTE_CORNER_SMOOTHING_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_ROUTE_DEBUG_LOGGING_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_CUT_BY_OWNER_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_CUT_BY_OTHER_PLAYERS_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CUT_REQUIRES_SHEARS_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_CUT_REACH_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_INTERACTION_RADIUS_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CUT_LINE_OF_SIGHT_REQUIRED_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_TARGET_INDICATOR_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_DEBUG_VISUAL_THICKNESS_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_SPACING_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_HEALTH_VALUE;
    public static final ForgeConfigSpec.DoubleValue FIBER_CABLE_SEGMENT_HITBOX_SIZE_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_PLAYERS_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_PROJECTILES_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_CAN_BE_DAMAGED_BY_EXPLOSIONS_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_PROJECTILE_RAYCAST_BREAK_VALUE;
    public static final ForgeConfigSpec.BooleanValue FIBER_CABLE_BREAKS_WHEN_ANY_SEGMENT_DESTROYED_VALUE;
    public static final ForgeConfigSpec.IntValue MAX_FIBER_CABLE_SEGMENTS_PER_DRONE_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_FIBER_CABLE_RENDER_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_FIBER_CABLE_SEGMENTS_VALUE;
    public static final ForgeConfigSpec.BooleanValue RENDER_RED_DEBUG_CABLE_VALUE;
    public static final ForgeConfigSpec.BooleanValue RENDER_LEGACY_FIBER_LINE_VALUE;
    public static final ForgeConfigSpec.BooleanValue WEATHER_EFFECTS_ENABLED_VALUE;
    public static final ForgeConfigSpec.DoubleValue RAIN_VISIBILITY_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.DoubleValue STORM_INSTABILITY_STRENGTH_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_DRONE_SCREENSHOT_SOUND_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE;
    public static final ForgeConfigSpec.DoubleValue CUBED_FPV_EXTERNAL_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue CUBED_FPV_NEAR_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue CUBED_FPV_MID_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue CUBED_FPV_FAR_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_EXTERNAL_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_NEAR_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_MID_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue LUCAS_FAR_VOLUME_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_INTERNAL_AUDIO_GAIN_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_DOPPLER_ENABLED_VALUE;
    public static final ForgeConfigSpec.DoubleValue DRONE_DOPPLER_STRENGTH_VALUE;
    public static final ForgeConfigSpec.BooleanValue DRONE_AUDIO_OCCLUSION_VALUE;
    public static final ForgeConfigSpec.IntValue MAX_AUDIBLE_CUBED_FPV_DRONES_VALUE;
    public static final ForgeConfigSpec.IntValue MAX_AUDIBLE_LUCAS_DRONES_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_CAMERA_FOV_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_CAMERA_TILT_DEGREES_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FPV_FISHEYE_EFFECT_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_FPV_CAMERA_VIBRATION_VALUE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_SPOTLIGHT_MODULE_VALUE;
    public static final ForgeConfigSpec.IntValue SPOTLIGHT_RANGE_VALUE;
    public static final ForgeConfigSpec.IntValue SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE;
    public static final ForgeConfigSpec.BooleanValue SPOTLIGHT_REQUIRES_MODULE_VALUE;
    public static final ForgeConfigSpec.BooleanValue SPOTLIGHT_VISIBLE_TO_OTHER_PLAYERS_VALUE;
    public static final ForgeConfigSpec.BooleanValue SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE;
    public static final ForgeConfigSpec.BooleanValue SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE;
    public static final ForgeConfigSpec.IntValue SPOTLIGHT_LIGHT_RANGE_VALUE;
    public static final ForgeConfigSpec.IntValue SPOTLIGHT_LIGHT_LEVEL_VALUE;
    public static final ForgeConfigSpec.IntValue SPOTLIGHT_LIGHT_UPDATE_INTERVAL_TICKS_VALUE;
    public static final ForgeConfigSpec.BooleanValue DEBUG_PAYLOAD_MOUNTS_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_ATTACHMENT_X_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_ATTACHMENT_Y_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_ATTACHMENT_Z_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_CORD_LENGTH_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_GRENADE_SCALE_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_GRENADE_ROTATION_PITCH_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_GRENADE_ROTATION_ROLL_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_GRENADE_ROTATION_YAW_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_SWING_MAX_ANGLE_VALUE;
    public static final ForgeConfigSpec.DoubleValue FPV_PAYLOAD_SWING_DAMPING_VALUE;

    private static volatile int droneMaxRange;
    private static volatile int droneVisualTrackingRange;
    private static volatile double droneSpeedMultiplier;
    private static volatile boolean enableDroneChunkLoading;
    private static volatile int droneChunkLoadRadius;
    private static volatile int droneProactiveLoadRadius;
    private static volatile boolean debugDroneChunkLoading;
    private static volatile boolean experimentalDroneClientChunkCenterSync;
    private static volatile boolean enableDronePlayerAnchor;
    private static volatile boolean enableDroneCrashExplosionDamage;
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
    private static volatile boolean debugPayloadMounts;
    private static volatile double fpvPayloadAttachmentX;
    private static volatile double fpvPayloadAttachmentY;
    private static volatile double fpvPayloadAttachmentZ;
    private static volatile double fpvPayloadCordLength;
    private static volatile double fpvPayloadGrenadeScale;
    private static volatile double fpvPayloadGrenadeRotationPitch;
    private static volatile double fpvPayloadGrenadeRotationRoll;
    private static volatile double fpvPayloadGrenadeRotationYaw;
    private static volatile double fpvPayloadSwingMaxAngle;
    private static volatile double fpvPayloadSwingDamping;

    static {
        AddonSettingsBuilder builder = new AddonSettingsBuilder();

        builder.push("drone");
        ENABLE_DRONE_REMOTE_CHUNK_TRACKING_VALUE = builder
                .comment("Use vanilla chunk and entity tracking around the validated drone camera while the body stays in place. Takes precedence over the legacy player anchor; disabling restores the legacy behavior.")
                .define("enableDroneRemoteChunkTracking", true);
        DRONE_EXTRA_TICKETS_PER_TICK_VALUE = builder.comment("Global new supplementary body/autonomous-flight tickets per server tick; camera views use vanilla's own throttler.")
                .defineInRange("droneExtraTicketsPerTick", 4, 1, 16);
        DRONE_EXTRA_PENDING_CHUNKS_VALUE = builder.comment("Global supplementary chunks allowed to be loading at once.")
                .defineInRange("droneExtraPendingChunks", 8, 1, 64);
        DRONE_EXTRA_TICKET_LIMIT_VALUE = builder.comment("Global supplementary ticket cap, shared by all drones, players and dimensions.")
                .defineInRange("droneExtraTicketLimit", 512, 16, 4096);
        builder.comment("Internal one-time migration marker for the obsolete 100-block default; do not edit.")
                .defineInRange("_manualRangeDefaultsVersion", 1, 0, 1);
        DRONE_MAX_RANGE_VALUE = builder
                .comment("Maximum allowed distance in blocks between the player and a linked Superb Warfare drone.")
                .defineInRange("droneMaxRange", 10000, 1, 30000);
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
        DRONE_PROACTIVE_LOAD_RADIUS_VALUE = builder
                .comment("Immediate chunk preload radius when a controlled drone enters a chunk. Limited to 1 to keep requests within a 3x3 area.")
                .defineInRange("droneProactiveLoadRadius", 1, 0, 1);
        DEBUG_DRONE_CHUNK_LOADING_VALUE = builder
                .comment("Log one entry whenever an actively controlled drone proactively requests chunks after crossing a chunk boundary.")
                .define("debugDroneChunkLoading", false);
        EXPERIMENTAL_DRONE_CLIENT_CHUNK_CENTER_SYNC_VALUE = builder
                .comment("Experimental: send the controlling client a chunk-cache center update when its drone crosses a chunk boundary.")
                .define("experimentalDroneClientChunkCenterSync", false);
        ENABLE_DRONE_PLAYER_ANCHOR_VALUE = builder
                .comment("Move the controlling player with the drone while using the monitor so vanilla chunk loading follows the drone.")
                .define("enableDronePlayerAnchor", true);
        ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE = builder
                .comment("Allow drone crash explosions to damage entities and destroy blocks.")
                .define("enableDroneCrashExplosionDamage", true);
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
                .comment("Server-side scan interval in ticks while the detector is ON and held.")
                .defineInRange("portableFpvDetectorScanIntervalTicks", 10, 1, 200);
        PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE = builder
                .comment("Maximum detected wireless drone targets sent to the detector display.")
                .defineInRange("portableFpvDetectorMaximumTargets", 16, 1, 64);
        PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE = builder
                .comment("Allow the detector UI to advertise live video support for compatible wireless drones.")
                .define("portableFpvDetectorVideoEnabled", true);
        PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE = builder
                .comment("If false, the handheld detector ignores drones owned by the detector holder.")
                .define("portableFpvDetectorDetectOwnDrones", true);
        PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE = builder
                .comment("If true, the detector only reports drones with direct line of sight.")
                .define("portableFpvDetectorRequireLineOfSight", false);
        PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE = builder
                .comment("Render the compact lower-right Portable FPV Drone Detector HUD while scanning.")
                .define("portableFpvDetectorHudEnabled", true);
        PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE = builder
                .comment("Show numeric signal percentages in the raised detector receiver screen.")
                .define("portableFpvDetectorShowSignalPercent", true);
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
                .comment("Enable the vehicle-mounted Anti-Drone Net Upgrade cage around Superb Warfare vehicles.")
                .define("vehicleAntiDroneNetEnabled", true);
        VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE = builder
                .comment("Extra horizontal clearance in blocks added around the vehicle hitbox for the mounted anti-drone net cage.")
                .defineInRange("vehicleAntiDroneNetHorizontalPadding", 0.20D, 0.0D, 2.0D);
        VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE = builder
                .comment("Extra top clearance in blocks added above the vehicle hitbox for the mounted anti-drone net cage.")
                .defineInRange("vehicleAntiDroneNetTopPadding", 0.25D, 0.0D, 2.0D);
        VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE = builder
                .comment("If true, drones crossing an active vehicle-mounted anti-drone net cage are jammed like normal anti-drone net blocks.")
                .define("vehicleAntiDroneNetStopsDrones", true);
        VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE = builder
                .comment("Client render opacity for the vehicle-mounted anti-drone net cage.")
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
                .comment("Analog-style FPV camera field of view for the Cubed FPV Drone.")
                .defineInRange("fpvCameraFov", CubedFpvCameraViewMath.DEFAULT_FPV_FOV, CubedFpvCameraViewMath.MIN_FPV_FOV, CubedFpvCameraViewMath.MAX_FPV_FOV);
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

        builder.push("payload");
        DEBUG_PAYLOAD_MOUNTS_VALUE = builder
                .comment("Render colored payload mount debug markers and log payload mount transforms every second.")
                .define("debugPayloadMounts", false);
        FPV_PAYLOAD_ATTACHMENT_X_VALUE = builder
                .comment("Local X offset for the single dangling Cubed FPV payload attachment point.")
                .defineInRange("fpvPayloadAttachmentX", 0.0D, -2.0D, 2.0D);
        FPV_PAYLOAD_ATTACHMENT_Y_VALUE = builder
                .comment("Local Y offset for the single dangling Cubed FPV payload attachment point.")
                .defineInRange("fpvPayloadAttachmentY", -0.30D, -2.0D, 1.0D);
        FPV_PAYLOAD_ATTACHMENT_Z_VALUE = builder
                .comment("Local Z offset for the single dangling Cubed FPV payload attachment point.")
                .defineInRange("fpvPayloadAttachmentZ", 0.0D, -2.0D, 2.0D);
        FPV_PAYLOAD_CORD_LENGTH_VALUE = builder
                .comment("Length in blocks of the visual cord below the Cubed FPV drone.")
                .defineInRange("fpvPayloadCordLength", 0.65D, 0.1D, 1.0D);
        FPV_PAYLOAD_GRENADE_SCALE_VALUE = builder
                .comment("Extra visual scale multiplier for the dangling Cubed FPV payload.")
                .defineInRange("fpvPayloadGrenadeScale", 0.62D, 0.1D, 2.0D);
        FPV_PAYLOAD_GRENADE_ROTATION_PITCH_VALUE = builder
                .comment("Extra visual pitch rotation for the dangling Cubed FPV payload.")
                .defineInRange("fpvPayloadGrenadeRotationPitch", 0.0D, -180.0D, 180.0D);
        FPV_PAYLOAD_GRENADE_ROTATION_ROLL_VALUE = builder
                .comment("Extra visual roll rotation for the dangling Cubed FPV payload.")
                .defineInRange("fpvPayloadGrenadeRotationRoll", 0.0D, -180.0D, 180.0D);
        FPV_PAYLOAD_GRENADE_ROTATION_YAW_VALUE = builder
                .comment("Extra visual yaw rotation for the dangling Cubed FPV payload.")
                .defineInRange("fpvPayloadGrenadeRotationYaw", 0.0D, -180.0D, 180.0D);
        FPV_PAYLOAD_SWING_MAX_ANGLE_VALUE = builder
                .comment("Maximum visual swing angle in degrees for the dangling Cubed FPV payload.")
                .defineInRange("fpvPayloadSwingMaxAngle", 35.0D, 0.0D, 45.0D);
        FPV_PAYLOAD_SWING_DAMPING_VALUE = builder
                .comment("Client-side damping for the dangling Cubed FPV payload swing.")
                .defineInRange("fpvPayloadSwingDamping", 0.88D, 0.0D, 0.99D);
        builder.pop();

        Vc01Settings.configure(builder);
        DroneVehicleDamageConfig.configure(builder);
        SPEC = builder.build();
        CLIENT_SPEC = builder.buildClient();
        LOCAL_VALUES = builder.localValues();
        bake();
    }

    private AddonConfig() {
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC || event.getConfig().getSpec() == CLIENT_SPEC) {
            bake();
            if (ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
                DroneTrackingRangeOverride.applyConfiguredRange();
            }
        }
    }

    private static <T> T read(ForgeConfigSpec.ConfigValue<T> value) {
        var owner = LOCAL_VALUES.contains(value) ? CLIENT_SPEC : SPEC;
        return owner.isLoaded() ? value.get() : value.getDefault();
    }

    private static void bake() {
        droneMaxRange = read(DRONE_MAX_RANGE_VALUE);
        droneVisualTrackingRange = read(DRONE_VISUAL_TRACKING_RANGE_VALUE);
        droneSpeedMultiplier = read(DRONE_SPEED_MULTIPLIER_VALUE);
        enableDroneChunkLoading = read(ENABLE_DRONE_CHUNK_LOADING_VALUE);
        droneChunkLoadRadius = read(DRONE_CHUNK_LOAD_RADIUS_VALUE);
        droneProactiveLoadRadius = read(DRONE_PROACTIVE_LOAD_RADIUS_VALUE);
        debugDroneChunkLoading = read(DEBUG_DRONE_CHUNK_LOADING_VALUE);
        experimentalDroneClientChunkCenterSync = read(EXPERIMENTAL_DRONE_CLIENT_CHUNK_CENTER_SYNC_VALUE);
        enableDronePlayerAnchor = read(ENABLE_DRONE_PLAYER_ANCHOR_VALUE);
        enableDroneCrashExplosionDamage = read(ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE);
        lucasTakeoffAssistTicks = read(LUCAS_TAKEOFF_ASSIST_TICKS_VALUE);
        lucasTakeoffForwardBoost = read(LUCAS_TAKEOFF_FORWARD_BOOST_VALUE);
        lucasTakeoffUpBoost = read(LUCAS_TAKEOFF_UP_BOOST_VALUE);
        lucasMinSpeed = read(LUCAS_MIN_SPEED_VALUE);
        lucasMaxSpeed = Math.max(lucasMinSpeed, read(LUCAS_MAX_SPEED_VALUE));
        lucasAcceleration = read(LUCAS_ACCELERATION_VALUE);
        lucasDrag = read(LUCAS_DRAG_VALUE);
        lucasTurnRate = read(LUCAS_TURN_RATE_VALUE);
        lucasPitchRate = read(LUCAS_PITCH_RATE_VALUE);
        lucasClimbSpeed = read(LUCAS_CLIMB_SPEED_VALUE);
        lucasDescendSpeed = read(LUCAS_DESCEND_SPEED_VALUE);
        lucasLiftStrength = read(LUCAS_LIFT_STRENGTH_VALUE);
        lucasRollVisualAmount = read(LUCAS_ROLL_VISUAL_AMOUNT_VALUE);
        lucasMinLiftSpeed = read(LUCAS_MIN_LIFT_SPEED_VALUE);
        lucasStallGravity = read(LUCAS_STALL_GRAVITY_VALUE);
        lucasStallEnabled = read(LUCAS_STALL_ENABLED_VALUE);
        lucasCanHover = read(LUCAS_CAN_HOVER_VALUE);
        lucasMaxFuel = read(LUCAS_MAX_FUEL_VALUE);
        lucasFuelDrainIdle = read(LUCAS_FUEL_DRAIN_IDLE_VALUE);
        lucasFuelDrainFlying = read(LUCAS_FUEL_DRAIN_FLYING_VALUE);
        lucasFuelDrainClimb = read(LUCAS_FUEL_DRAIN_CLIMB_VALUE);
        lucasLowFuelWarningPercent = read(LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE);
        lucasCanFlyWithoutFuel = read(LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE);
        lucasHudScale = read(LUCAS_HUD_SCALE_VALUE);
        fuelMixerProcessTime = read(FUEL_MIXER_PROCESS_TIME_VALUE);
        gasolineJerrycanFuelAmount = read(GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE);
        enableThermalVision = read(ENABLE_THERMAL_VISION_VALUE);
        thermalVisionRange = read(THERMAL_VISION_RANGE_VALUE);
        thermalVisionKeybindEnabled = read(THERMAL_VISION_KEYBIND_ENABLED_VALUE);
        thermalVisionNoiseEffect = read(THERMAL_VISION_NOISE_EFFECT_VALUE);
        thermalHighlightPlayers = read(THERMAL_HIGHLIGHT_PLAYERS_VALUE);
        thermalHighlightHostileMobs = read(THERMAL_HIGHLIGHT_HOSTILE_MOBS_VALUE);
        thermalHighlightAnimals = read(THERMAL_HIGHLIGHT_ANIMALS_VALUE);
        enableBatterySystem = read(ENABLE_BATTERY_SYSTEM_VALUE);
        maxEnergy = read(MAX_ENERGY_VALUE);
        energyUsePerTick = read(ENERGY_USE_PER_TICK_VALUE);
        idleEnergyUsePerTick = read(IDLE_ENERGY_USE_PER_TICK_VALUE);
        lowEnergyThreshold = read(LOW_ENERGY_THRESHOLD_VALUE);
        returnHomeOnEmpty = read(RETURN_HOME_ON_EMPTY_VALUE);
        batteryTransferRate = read(BATTERY_TRANSFER_RATE_VALUE);
        enableDroneSiren = read(ENABLE_DRONE_SIREN_VALUE);
        droneSirenRange = read(DRONE_SIREN_RANGE_VALUE);
        droneSirenRedstoneOutput = read(DRONE_SIREN_REDSTONE_OUTPUT_VALUE);
        droneSirenCooldownTicks = read(DRONE_SIREN_COOLDOWN_TICKS_VALUE);
        detectFriendlyDrones = read(DETECT_FRIENDLY_DRONES_VALUE);
        ignoredDroneOwnerNames = normalizeOwnerNames(read(IGNORED_DRONE_OWNER_NAMES_VALUE));
        ignoredDroneOwnerUUIDs = parseOwnerUuids(read(IGNORED_DRONE_OWNER_UUIDS_VALUE));
        ignoreOwnerlessDrones = read(IGNORE_OWNERLESS_DRONES_VALUE);
        blacklistMatchCaseInsensitive = read(BLACKLIST_MATCH_CASE_INSENSITIVE_VALUE);
        ignoredDroneNames = normalizeConfiguredNames(read(IGNORED_DRONE_NAMES_VALUE), blacklistMatchCaseInsensitive);
        ignoreUnnamedDrones = read(IGNORE_UNNAMED_DRONES_VALUE);
        enableDroneJammer = read(ENABLE_DRONE_JAMMER_VALUE);
        jammerRange = read(JAMMER_RANGE_VALUE);
        jammerBuildUpTicks = read(JAMMER_BUILD_UP_TICKS_VALUE);
        jammerRecoveryTicks = read(JAMMER_RECOVERY_TICKS_VALUE);
        overlayStartThreshold = clampThreshold(read(OVERLAY_START_THRESHOLD_VALUE), 0.0D, 0.99D);
        controlInterferenceThreshold = Math.max(
                overlayStartThreshold,
                clampThreshold(read(CONTROL_INTERFERENCE_THRESHOLD_VALUE), 0.0D, 0.99D)
        );
        hardJamThreshold = Math.max(
                controlInterferenceThreshold,
                clampThreshold(read(HARD_JAM_THRESHOLD_VALUE), 0.50D, 1.0D)
        );
        debugJammer = read(DEBUG_JAMMER_VALUE);
        jammerMode = read(JAMMER_MODE_VALUE);
        affectFriendlyDrones = read(AFFECT_FRIENDLY_DRONES_VALUE);
        droneRadarDetectionRange = read(DRONE_RADAR_DETECTION_RANGE_VALUE);
        droneRadarBeepEnabled = read(DRONE_RADAR_BEEP_ENABLED_VALUE);
        droneRadarBeepVolume = read(DRONE_RADAR_BEEP_VOLUME_VALUE);
        droneRadarMinBeepIntervalTicks = read(DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE);
        droneRadarMaxBeepIntervalTicks = Math.max(
                droneRadarMinBeepIntervalTicks,
                read(DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE)
        );
        droneRadarOnlyBeepWhenHeld = read(DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE);
        droneRadarHudScale = read(DRONE_RADAR_HUD_SCALE_VALUE);
        droneRadarHudXOffset = read(DRONE_RADAR_HUD_X_OFFSET_VALUE);
        droneRadarHudYOffset = read(DRONE_RADAR_HUD_Y_OFFSET_VALUE);
        droneRadarCompactMode = read(DRONE_RADAR_COMPACT_MODE_VALUE);
        debugDroneRadar = read(DEBUG_DRONE_RADAR_VALUE);
        portableFpvDetectorRange = read(PORTABLE_FPV_DETECTOR_RANGE_VALUE);
        portableFpvDetectorScanIntervalTicks = read(PORTABLE_FPV_DETECTOR_SCAN_INTERVAL_TICKS_VALUE);
        portableFpvDetectorMaximumTargets = read(PORTABLE_FPV_DETECTOR_MAXIMUM_TARGETS_VALUE);
        portableFpvDetectorVideoEnabled = read(PORTABLE_FPV_DETECTOR_VIDEO_ENABLED_VALUE);
        portableFpvDetectorDetectOwnDrones = read(PORTABLE_FPV_DETECTOR_DETECT_OWN_DRONES_VALUE);
        portableFpvDetectorRequireLineOfSight = read(PORTABLE_FPV_DETECTOR_REQUIRE_LINE_OF_SIGHT_VALUE);
        portableFpvDetectorHudEnabled = read(PORTABLE_FPV_DETECTOR_HUD_ENABLED_VALUE);
        portableFpvDetectorShowSignalPercent = read(PORTABLE_FPV_DETECTOR_SHOW_SIGNAL_PERCENT_VALUE);
        enableFiberOpticMode = read(ENABLE_FIBER_OPTIC_MODE_VALUE);
        fiberOpticCableLength = read(FIBER_OPTIC_CABLE_LENGTH_VALUE);
        fiberOpticCableBreakDelayTicks = read(FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE);
        fiberOpticExtraBatteryDrainMultiplier = read(FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE);
        normalJammersAffectFiberOptic = read(NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE);
        antiDroneNetCutsFiber = read(ANTI_DRONE_NET_CUTS_FIBER_VALUE);
        antiDroneNetPanelRequiresSupport = read(ANTI_DRONE_NET_PANEL_REQUIRES_SUPPORT_VALUE);
        antiDroneNetPanelTriggerThickness = read(ANTI_DRONE_NET_PANEL_TRIGGER_THICKNESS_VALUE);
        antiDroneNetPanelVisualThickness = read(ANTI_DRONE_NET_PANEL_VISUAL_THICKNESS_VALUE);
        vehicleAntiDroneNetEnabled = read(VEHICLE_ANTI_DRONE_NET_ENABLED_VALUE);
        vehicleAntiDroneNetHorizontalPadding = clamp(read(VEHICLE_ANTI_DRONE_NET_HORIZONTAL_PADDING_VALUE), 0.0D, 2.0D);
        vehicleAntiDroneNetTopPadding = clamp(read(VEHICLE_ANTI_DRONE_NET_TOP_PADDING_VALUE), 0.0D, 2.0D);
        vehicleAntiDroneNetStopsDrones = read(VEHICLE_ANTI_DRONE_NET_STOPS_DRONES_VALUE);
        vehicleAntiDroneNetOpacity = clamp(read(VEHICLE_ANTI_DRONE_NET_OPACITY_VALUE), 0.05D, 1.0D);
        netBulkPlacementEnabled = read(NET_BULK_PLACEMENT_ENABLED_VALUE);
        netBulkPlacementMaxWidth = read(NET_BULK_PLACEMENT_MAX_WIDTH_VALUE);
        netBulkPlacementMaxHeight = read(NET_BULK_PLACEMENT_MAX_HEIGHT_VALUE);
        netBulkPlacementMaxBlocks = Math.min(
                read(NET_BULK_PLACEMENT_MAX_BLOCKS_VALUE),
                netBulkPlacementMaxWidth * netBulkPlacementMaxHeight
        );
        netBulkPlacementMaximumReach = clamp(read(NET_BULK_PLACEMENT_MAXIMUM_REACH_VALUE), 1.0D, 256.0D);
        netBulkPlacementAllOrNothing = read(NET_BULK_PLACEMENT_ALL_OR_NOTHING_VALUE);
        netBulkPlacementSkipBlocked = read(NET_BULK_PLACEMENT_SKIP_BLOCKED_VALUE);
        netBulkPlacementBlocksPerTick = Math.max(1, read(NET_BULK_PLACEMENT_BLOCKS_PER_TICK_VALUE));
        netBulkPlacementUndoSeconds = Math.max(0, read(NET_BULK_PLACEMENT_UNDO_SECONDS_VALUE));
        debugFiberOptic = read(DEBUG_FIBER_OPTIC_VALUE);
        fiberCableVisualThickness = read(FIBER_CABLE_VISUAL_THICKNESS_VALUE);
        fiberCableVisualAlpha = read(FIBER_CABLE_VISUAL_ALPHA_VALUE);
        fiberCableVisualColor = normalizeFiberCableVisualColor(read(FIBER_CABLE_VISUAL_COLOR_VALUE));
        fiberCableMaxRenderDistance = read(FIBER_CABLE_MAX_RENDER_DISTANCE_VALUE);
        maxVisibleFiberCables = read(MAX_VISIBLE_FIBER_CABLES_VALUE);
        fiberRouteHistoryEnabled = read(FIBER_ROUTE_HISTORY_ENABLED_VALUE);
        fiberRoutePointSpacing = read(FIBER_ROUTE_POINT_SPACING_VALUE);
        fiberRouteMinimumTurnDistance = read(FIBER_ROUTE_MINIMUM_TURN_DISTANCE_VALUE);
        fiberRouteTurnAngleThreshold = read(FIBER_ROUTE_TURN_ANGLE_THRESHOLD_VALUE);
        fiberRouteMaximumPoints = read(FIBER_ROUTE_MAXIMUM_POINTS_VALUE);
        fiberRouteCornerSmoothing = read(FIBER_ROUTE_CORNER_SMOOTHING_VALUE);
        fiberRouteDebugLogging = read(FIBER_ROUTE_DEBUG_LOGGING_VALUE);
        fiberCableCanBeCutByOwner = read(FIBER_CABLE_CAN_BE_CUT_BY_OWNER_VALUE);
        fiberCableCanBeCutByOtherPlayers = read(FIBER_CABLE_CAN_BE_CUT_BY_OTHER_PLAYERS_VALUE);
        fiberCableCutRequiresShears = read(FIBER_CABLE_CUT_REQUIRES_SHEARS_VALUE);
        fiberCableCutReach = clamp(read(FIBER_CABLE_CUT_REACH_VALUE), 1.0D, 16.0D);
        fiberCableInteractionRadius = clamp(read(FIBER_CABLE_INTERACTION_RADIUS_VALUE), 0.02D, 0.50D);
        fiberCableCutLineOfSightRequired = read(FIBER_CABLE_CUT_LINE_OF_SIGHT_REQUIRED_VALUE);
        fiberCableTargetIndicator = read(FIBER_CABLE_TARGET_INDICATOR_VALUE);
        fiberCableDebugVisualThickness = read(FIBER_CABLE_DEBUG_VISUAL_THICKNESS_VALUE);
        fiberCableSegmentSpacing = read(FIBER_CABLE_SEGMENT_SPACING_VALUE);
        fiberCableSegmentHealth = read(FIBER_CABLE_SEGMENT_HEALTH_VALUE);
        fiberCableSegmentHitboxSize = read(FIBER_CABLE_SEGMENT_HITBOX_SIZE_VALUE);
        fiberCableCanBeDamagedByPlayers = read(FIBER_CABLE_CAN_BE_DAMAGED_BY_PLAYERS_VALUE);
        fiberCableCanBeDamagedByProjectiles = read(FIBER_CABLE_CAN_BE_DAMAGED_BY_PROJECTILES_VALUE);
        fiberCableCanBeDamagedByExplosions = read(FIBER_CABLE_CAN_BE_DAMAGED_BY_EXPLOSIONS_VALUE);
        fiberCableProjectileRaycastBreak = read(FIBER_CABLE_PROJECTILE_RAYCAST_BREAK_VALUE);
        fiberCableBreaksWhenAnySegmentDestroyed = read(FIBER_CABLE_BREAKS_WHEN_ANY_SEGMENT_DESTROYED_VALUE);
        maxFiberCableSegmentsPerDrone = read(MAX_FIBER_CABLE_SEGMENTS_PER_DRONE_VALUE);
        debugFiberCableRender = read(DEBUG_FIBER_CABLE_RENDER_VALUE);
        debugFiberCableSegments = read(DEBUG_FIBER_CABLE_SEGMENTS_VALUE);
        renderRedDebugCable = read(RENDER_RED_DEBUG_CABLE_VALUE);
        renderLegacyFiberLine = read(RENDER_LEGACY_FIBER_LINE_VALUE);
        weatherEffectsEnabled = read(WEATHER_EFFECTS_ENABLED_VALUE);
        rainVisibilityMultiplier = read(RAIN_VISIBILITY_MULTIPLIER_VALUE);
        stormInstabilityStrength = read(STORM_INSTABILITY_STRENGTH_VALUE);
        enableDroneScreenshotSound = read(ENABLE_DRONE_SCREENSHOT_SOUND_VALUE);
        fpvCameraFov = read(FPV_CAMERA_FOV_VALUE);
        fpvCameraTiltDegrees = read(FPV_CAMERA_TILT_DEGREES_VALUE);
        enableFpvFisheyeEffect = read(ENABLE_FPV_FISHEYE_EFFECT_VALUE);
        enableFpvCameraVibration = read(ENABLE_FPV_CAMERA_VIBRATION_VALUE);
        droneEngineVolumeMultiplier = read(DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE);
        lucasEngineVolumeMultiplier = read(LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE);
        droneMonitorHumVolumeMultiplier = read(DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE);
        droneAirborneVolumeMultiplier = read(DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE);
        cubedFpvExternalVolume = clamp(read(CUBED_FPV_EXTERNAL_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvNearVolume = clamp(read(CUBED_FPV_NEAR_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvMidVolume = clamp(read(CUBED_FPV_MID_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        cubedFpvFarVolume = clamp(read(CUBED_FPV_FAR_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasExternalVolume = clamp(read(LUCAS_EXTERNAL_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasNearVolume = clamp(read(LUCAS_NEAR_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasMidVolume = clamp(read(LUCAS_MID_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        lucasFarVolume = clamp(read(LUCAS_FAR_VOLUME_VALUE),
                DroneAudioConfigLimits.MIN_EXTERNAL_LAYER_VOLUME,
                DroneAudioConfigLimits.MAX_EXTERNAL_LAYER_VOLUME);
        fpvInternalAudioGain = clamp(read(FPV_INTERNAL_AUDIO_GAIN_VALUE),
                DroneAudioConfigLimits.MIN_INTERNAL_AUDIO_GAIN,
                DroneAudioConfigLimits.MAX_INTERNAL_AUDIO_GAIN);
        droneDopplerEnabled = read(DRONE_DOPPLER_ENABLED_VALUE);
        droneDopplerStrength = clamp(read(DRONE_DOPPLER_STRENGTH_VALUE),
                DroneAudioConfigLimits.MIN_DOPPLER_STRENGTH,
                DroneAudioConfigLimits.MAX_DOPPLER_STRENGTH);
        droneAudioOcclusion = read(DRONE_AUDIO_OCCLUSION_VALUE);
        maxAudibleCubedFpvDrones = Math.max(
                DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                Math.min(DroneAudioConfigLimits.MAX_AUDIBLE_DRONES, read(MAX_AUDIBLE_CUBED_FPV_DRONES_VALUE))
        );
        maxAudibleLucasDrones = Math.max(
                DroneAudioConfigLimits.MIN_AUDIBLE_DRONES,
                Math.min(DroneAudioConfigLimits.MAX_AUDIBLE_DRONES, read(MAX_AUDIBLE_LUCAS_DRONES_VALUE))
        );
        enableSpotlightModule = read(ENABLE_SPOTLIGHT_MODULE_VALUE);
        spotlightRange = read(SPOTLIGHT_RANGE_VALUE);
        spotlightEnergyCostPerSecond = read(SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE);
        spotlightRequiresModule = read(SPOTLIGHT_REQUIRES_MODULE_VALUE);
        spotlightVisibleToOtherPlayers = read(SPOTLIGHT_VISIBLE_TO_OTHER_PLAYERS_VALUE);
        spotlightAutoOffWhenBatteryEmpty = read(SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE);
        spotlightRealLightEnabled = read(SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE);
        spotlightLightRange = read(SPOTLIGHT_LIGHT_RANGE_VALUE);
        spotlightLightLevel = read(SPOTLIGHT_LIGHT_LEVEL_VALUE);
        spotlightLightUpdateIntervalTicks = read(SPOTLIGHT_LIGHT_UPDATE_INTERVAL_TICKS_VALUE);
        debugPayloadMounts = read(DEBUG_PAYLOAD_MOUNTS_VALUE);
        fpvPayloadAttachmentX = read(FPV_PAYLOAD_ATTACHMENT_X_VALUE);
        fpvPayloadAttachmentY = read(FPV_PAYLOAD_ATTACHMENT_Y_VALUE);
        fpvPayloadAttachmentZ = read(FPV_PAYLOAD_ATTACHMENT_Z_VALUE);
        fpvPayloadCordLength = read(FPV_PAYLOAD_CORD_LENGTH_VALUE);
        fpvPayloadGrenadeScale = read(FPV_PAYLOAD_GRENADE_SCALE_VALUE);
        fpvPayloadGrenadeRotationPitch = read(FPV_PAYLOAD_GRENADE_ROTATION_PITCH_VALUE);
        fpvPayloadGrenadeRotationRoll = read(FPV_PAYLOAD_GRENADE_ROTATION_ROLL_VALUE);
        fpvPayloadGrenadeRotationYaw = read(FPV_PAYLOAD_GRENADE_ROTATION_YAW_VALUE);
        fpvPayloadSwingMaxAngle = read(FPV_PAYLOAD_SWING_MAX_ANGLE_VALUE);
        fpvPayloadSwingDamping = read(FPV_PAYLOAD_SWING_DAMPING_VALUE);

        fpvPayloadAttachmentY = migrateLegacyPayloadDefault(fpvPayloadAttachmentY, -0.14D, -0.30D);
        fpvPayloadAttachmentZ = migrateLegacyPayloadDefault(fpvPayloadAttachmentZ, 0.10D, 0.0D);
        fpvPayloadCordLength = Math.max(0.1D, Math.min(1.0D, migrateLegacyPayloadDefault(fpvPayloadCordLength, 0.62D, 0.65D)));
        fpvPayloadGrenadeScale = migrateLegacyPayloadDefault(fpvPayloadGrenadeScale, 0.55D, 0.62D);
        fpvPayloadSwingMaxAngle = Math.max(0.0D, Math.min(45.0D, migrateLegacyPayloadDefault(fpvPayloadSwingMaxAngle, 42.0D, 35.0D)));
        fpvPayloadSwingDamping = migrateLegacyPayloadDefault(fpvPayloadSwingDamping, 0.86D, 0.88D);
    }

    private static double migrateLegacyPayloadDefault(double value, double oldDefault, double newDefault) {
        return Math.abs(value - oldDefault) < 1.0E-6D ? newDefault : value;
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

    public static int droneProactiveLoadRadius() {
        return droneProactiveLoadRadius;
    }

    public static boolean debugDroneChunkLoading() {
        return debugDroneChunkLoading;
    }

    public static boolean experimentalDroneClientChunkCenterSync() {
        return experimentalDroneClientChunkCenterSync;
    }

    public static boolean enableDronePlayerAnchor() {
        return enableDronePlayerAnchor;
    }

    public static boolean enableDroneCrashExplosionDamage() {
        return enableDroneCrashExplosionDamage;
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

    public static boolean debugPayloadMounts() {
        return debugPayloadMounts;
    }

    public static double fpvPayloadAttachmentX() {
        return fpvPayloadAttachmentX;
    }

    public static double fpvPayloadAttachmentY() {
        return fpvPayloadAttachmentY;
    }

    public static double fpvPayloadAttachmentZ() {
        return fpvPayloadAttachmentZ;
    }

    public static double fpvPayloadCordLength() {
        return fpvPayloadCordLength;
    }

    public static double fpvPayloadGrenadeScale() {
        return fpvPayloadGrenadeScale;
    }

    public static double fpvPayloadGrenadeRotationPitch() {
        return fpvPayloadGrenadeRotationPitch;
    }

    public static double fpvPayloadGrenadeRotationRoll() {
        return fpvPayloadGrenadeRotationRoll;
    }

    public static double fpvPayloadGrenadeRotationYaw() {
        return fpvPayloadGrenadeRotationYaw;
    }

    public static double fpvPayloadSwingMaxAngle() {
        return fpvPayloadSwingMaxAngle;
    }

    public static double fpvPayloadSwingDamping() {
        return fpvPayloadSwingDamping;
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

    /** Restored Laser Defense Tower defaults for the Forge 1.20.1 integration. */
    public static int laserDefenseTowerDroneRange() {
        return 500;
    }

    public static boolean laserDefenseTowerWhitelistEnabled() {
        return true;
    }

    public static void save() {
        if (SPEC.isLoaded()) SPEC.save();
        if (CLIENT_SPEC.isLoaded()) CLIENT_SPEC.save();
        bake();
        if (ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            DroneTrackingRangeOverride.applyConfiguredRange();
        }
    }

    public static boolean enableDroneRemoteChunkTracking() { return read(ENABLE_DRONE_REMOTE_CHUNK_TRACKING_VALUE); }
}
