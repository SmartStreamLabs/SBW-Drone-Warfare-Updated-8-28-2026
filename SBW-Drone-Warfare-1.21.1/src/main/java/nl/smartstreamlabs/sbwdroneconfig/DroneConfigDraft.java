package nl.smartstreamlabs.sbwdroneconfig;

final class DroneConfigDraft {
    int droneMaxRange;
    boolean enableDroneChunkLoading;
    int droneChunkLoadRadius;
    boolean enableDronePlayerAnchor;
    boolean enableDroneCrashExplosionDamage;
    int lucasTakeoffAssistTicks;
    double lucasTakeoffForwardBoost;
    double lucasTakeoffUpBoost;
    double lucasMinSpeed;
    double lucasMaxSpeed;
    double lucasAcceleration;
    double lucasDrag;
    double lucasTurnRate;
    double lucasPitchRate;
    double lucasClimbSpeed;
    double lucasDescendSpeed;
    double lucasLiftStrength;
    double lucasRollVisualAmount;
    double lucasMinLiftSpeed;
    double lucasStallGravity;
    boolean lucasStallEnabled;
    boolean lucasCanHover;
    int lucasMaxFuel;
    double lucasFuelDrainIdle;
    double lucasFuelDrainFlying;
    double lucasFuelDrainClimb;
    int lucasLowFuelWarningPercent;
    boolean lucasCanFlyWithoutFuel;
    double lucasHudScale;
    int fuelMixerProcessTime;
    int gasolineJerrycanFuelAmount;

    boolean enableBatterySystem;
    int maxEnergy;
    int energyUsePerTick;
    int lowEnergyThreshold;
    int batteryTransferRate;
    boolean returnHomeOnEmpty;

    boolean enableDroneJammer;
    int jammerRange;
    int jammerBuildUpTicks;
    int jammerRecoveryTicks;
    double controlInterferenceThreshold;
    double hardJamThreshold;
    boolean affectFriendlyDrones;
    int droneRadarDetectionRange;
    boolean droneRadarBeepEnabled;
    double droneRadarBeepVolume;
    int droneRadarMinBeepIntervalTicks;
    int droneRadarMaxBeepIntervalTicks;
    boolean droneRadarOnlyBeepWhenHeld;
    double droneRadarHudScale;
    int droneRadarHudXOffset;
    int droneRadarHudYOffset;
    boolean droneRadarCompactMode;
    boolean debugDroneRadar;

    boolean weatherEffectsEnabled;
    boolean enableThermalVision;
    int thermalVisionRange;
    boolean thermalVisionNoiseEffect;
    double rainVisibilityMultiplier;
    double stormInstabilityStrength;

    boolean enableFiberOpticMode;
    boolean antiDroneNetCutsFiber;
    int fiberOpticCableLength;
    int fiberOpticCableBreakDelayTicks;
    double fiberOpticExtraBatteryDrainMultiplier;
    boolean normalJammersAffectFiberOptic;
    boolean debugFiberOptic;

    boolean enableSpotlightModule;
    int spotlightRange;
    int spotlightEnergyCostPerSecond;
    boolean spotlightRequiresModule;
    boolean spotlightAutoOffWhenBatteryEmpty;
    boolean spotlightRealLightEnabled;
    int spotlightLightRange;

    boolean enableDroneScreenshotSound;
    double droneSpeedMultiplier;
    double droneEngineVolumeMultiplier;
    double lucasEngineVolumeMultiplier;
    double droneMonitorHumVolumeMultiplier;
    double droneAirborneVolumeMultiplier;
    double fpvCameraFov;
    double fpvCameraTiltDegrees;
    boolean enableFpvFisheyeEffect;
    boolean enableFpvCameraVibration;

    static DroneConfigDraft fromConfig() {
        DroneConfigDraft draft = new DroneConfigDraft();
        draft.loadFromCurrentConfig();
        return draft;
    }

    void apply() {
        AddonConfig.DRONE_MAX_RANGE_VALUE.set(droneMaxRange);
        AddonConfig.ENABLE_DRONE_CHUNK_LOADING_VALUE.set(enableDroneChunkLoading);
        AddonConfig.DRONE_CHUNK_LOAD_RADIUS_VALUE.set(droneChunkLoadRadius);
        AddonConfig.ENABLE_DRONE_PLAYER_ANCHOR_VALUE.set(enableDronePlayerAnchor);
        AddonConfig.ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE.set(enableDroneCrashExplosionDamage);
        AddonConfig.LUCAS_TAKEOFF_ASSIST_TICKS_VALUE.set(lucasTakeoffAssistTicks);
        AddonConfig.LUCAS_TAKEOFF_FORWARD_BOOST_VALUE.set(lucasTakeoffForwardBoost);
        AddonConfig.LUCAS_TAKEOFF_UP_BOOST_VALUE.set(lucasTakeoffUpBoost);
        AddonConfig.LUCAS_MIN_SPEED_VALUE.set(lucasMinSpeed);
        AddonConfig.LUCAS_MAX_SPEED_VALUE.set(Math.max(lucasMinSpeed, lucasMaxSpeed));
        AddonConfig.LUCAS_ACCELERATION_VALUE.set(lucasAcceleration);
        AddonConfig.LUCAS_DRAG_VALUE.set(lucasDrag);
        AddonConfig.LUCAS_TURN_RATE_VALUE.set(lucasTurnRate);
        AddonConfig.LUCAS_PITCH_RATE_VALUE.set(lucasPitchRate);
        AddonConfig.LUCAS_CLIMB_SPEED_VALUE.set(lucasClimbSpeed);
        AddonConfig.LUCAS_DESCEND_SPEED_VALUE.set(lucasDescendSpeed);
        AddonConfig.LUCAS_LIFT_STRENGTH_VALUE.set(lucasLiftStrength);
        AddonConfig.LUCAS_ROLL_VISUAL_AMOUNT_VALUE.set(lucasRollVisualAmount);
        AddonConfig.LUCAS_MIN_LIFT_SPEED_VALUE.set(lucasMinLiftSpeed);
        AddonConfig.LUCAS_STALL_GRAVITY_VALUE.set(lucasStallGravity);
        AddonConfig.LUCAS_STALL_ENABLED_VALUE.set(lucasStallEnabled);
        AddonConfig.LUCAS_CAN_HOVER_VALUE.set(lucasCanHover);
        AddonConfig.LUCAS_MAX_FUEL_VALUE.set(lucasMaxFuel);
        AddonConfig.LUCAS_FUEL_DRAIN_IDLE_VALUE.set(lucasFuelDrainIdle);
        AddonConfig.LUCAS_FUEL_DRAIN_FLYING_VALUE.set(lucasFuelDrainFlying);
        AddonConfig.LUCAS_FUEL_DRAIN_CLIMB_VALUE.set(lucasFuelDrainClimb);
        AddonConfig.LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE.set(lucasLowFuelWarningPercent);
        AddonConfig.LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE.set(lucasCanFlyWithoutFuel);
        AddonConfig.LUCAS_HUD_SCALE_VALUE.set(lucasHudScale);
        AddonConfig.LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE.set(lucasEngineVolumeMultiplier);
        AddonConfig.FUEL_MIXER_PROCESS_TIME_VALUE.set(fuelMixerProcessTime);
        AddonConfig.GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE.set(gasolineJerrycanFuelAmount);

        AddonConfig.ENABLE_BATTERY_SYSTEM_VALUE.set(enableBatterySystem);
        AddonConfig.MAX_ENERGY_VALUE.set(maxEnergy);
        AddonConfig.ENERGY_USE_PER_TICK_VALUE.set(energyUsePerTick);
        AddonConfig.LOW_ENERGY_THRESHOLD_VALUE.set(lowEnergyThreshold);
        AddonConfig.BATTERY_TRANSFER_RATE_VALUE.set(batteryTransferRate);
        AddonConfig.RETURN_HOME_ON_EMPTY_VALUE.set(returnHomeOnEmpty);

        AddonConfig.ENABLE_DRONE_JAMMER_VALUE.set(enableDroneJammer);
        AddonConfig.JAMMER_RANGE_VALUE.set(jammerRange);
        AddonConfig.JAMMER_BUILD_UP_TICKS_VALUE.set(jammerBuildUpTicks);
        AddonConfig.JAMMER_RECOVERY_TICKS_VALUE.set(jammerRecoveryTicks);
        AddonConfig.CONTROL_INTERFERENCE_THRESHOLD_VALUE.set(controlInterferenceThreshold);
        AddonConfig.HARD_JAM_THRESHOLD_VALUE.set(Math.max(controlInterferenceThreshold, hardJamThreshold));
        AddonConfig.AFFECT_FRIENDLY_DRONES_VALUE.set(affectFriendlyDrones);
        AddonConfig.DRONE_RADAR_DETECTION_RANGE_VALUE.set(droneRadarDetectionRange);
        AddonConfig.DRONE_RADAR_BEEP_ENABLED_VALUE.set(droneRadarBeepEnabled);
        AddonConfig.DRONE_RADAR_BEEP_VOLUME_VALUE.set(droneRadarBeepVolume);
        AddonConfig.DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE.set(droneRadarMinBeepIntervalTicks);
        AddonConfig.DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE.set(Math.max(droneRadarMinBeepIntervalTicks, droneRadarMaxBeepIntervalTicks));
        AddonConfig.DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE.set(droneRadarOnlyBeepWhenHeld);
        AddonConfig.DRONE_RADAR_HUD_SCALE_VALUE.set(droneRadarHudScale);
        AddonConfig.DRONE_RADAR_HUD_X_OFFSET_VALUE.set(droneRadarHudXOffset);
        AddonConfig.DRONE_RADAR_HUD_Y_OFFSET_VALUE.set(droneRadarHudYOffset);
        AddonConfig.DRONE_RADAR_COMPACT_MODE_VALUE.set(droneRadarCompactMode);
        AddonConfig.DEBUG_DRONE_RADAR_VALUE.set(debugDroneRadar);

        AddonConfig.WEATHER_EFFECTS_ENABLED_VALUE.set(weatherEffectsEnabled);
        AddonConfig.ENABLE_THERMAL_VISION_VALUE.set(enableThermalVision);
        AddonConfig.THERMAL_VISION_RANGE_VALUE.set(thermalVisionRange);
        AddonConfig.THERMAL_VISION_NOISE_EFFECT_VALUE.set(thermalVisionNoiseEffect);
        AddonConfig.RAIN_VISIBILITY_MULTIPLIER_VALUE.set(rainVisibilityMultiplier);
        AddonConfig.STORM_INSTABILITY_STRENGTH_VALUE.set(stormInstabilityStrength);

        AddonConfig.ENABLE_FIBER_OPTIC_MODE_VALUE.set(enableFiberOpticMode);
        AddonConfig.ANTI_DRONE_NET_CUTS_FIBER_VALUE.set(antiDroneNetCutsFiber);
        AddonConfig.FIBER_OPTIC_CABLE_LENGTH_VALUE.set(fiberOpticCableLength);
        AddonConfig.FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE.set(fiberOpticCableBreakDelayTicks);
        AddonConfig.FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE.set(fiberOpticExtraBatteryDrainMultiplier);
        AddonConfig.NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE.set(normalJammersAffectFiberOptic);
        AddonConfig.DEBUG_FIBER_OPTIC_VALUE.set(debugFiberOptic);

        AddonConfig.ENABLE_SPOTLIGHT_MODULE_VALUE.set(enableSpotlightModule);
        AddonConfig.SPOTLIGHT_RANGE_VALUE.set(spotlightRange);
        AddonConfig.SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE.set(spotlightEnergyCostPerSecond);
        AddonConfig.SPOTLIGHT_REQUIRES_MODULE_VALUE.set(spotlightRequiresModule);
        AddonConfig.SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE.set(spotlightAutoOffWhenBatteryEmpty);
        AddonConfig.SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE.set(spotlightRealLightEnabled);
        AddonConfig.SPOTLIGHT_LIGHT_RANGE_VALUE.set(spotlightLightRange);

        AddonConfig.ENABLE_DRONE_SCREENSHOT_SOUND_VALUE.set(enableDroneScreenshotSound);
        AddonConfig.DRONE_SPEED_MULTIPLIER_VALUE.set(droneSpeedMultiplier);
        AddonConfig.DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE.set(droneEngineVolumeMultiplier);
        AddonConfig.DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE.set(droneMonitorHumVolumeMultiplier);
        AddonConfig.DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE.set(droneAirborneVolumeMultiplier);
        AddonConfig.FPV_CAMERA_FOV_VALUE.set(fpvCameraFov);
        AddonConfig.FPV_CAMERA_TILT_DEGREES_VALUE.set(fpvCameraTiltDegrees);
        AddonConfig.ENABLE_FPV_FISHEYE_EFFECT_VALUE.set(enableFpvFisheyeEffect);
        AddonConfig.ENABLE_FPV_CAMERA_VIBRATION_VALUE.set(enableFpvCameraVibration);

        AddonConfig.save();
        loadFromCurrentConfig();
    }

    void resetCategory(DroneConfigCategory category) {
        switch (category) {
            case DRONE -> {
                droneMaxRange = AddonConfig.DRONE_MAX_RANGE_VALUE.getDefault();
                enableDroneChunkLoading = AddonConfig.ENABLE_DRONE_CHUNK_LOADING_VALUE.getDefault();
                droneChunkLoadRadius = AddonConfig.DRONE_CHUNK_LOAD_RADIUS_VALUE.getDefault();
                enableDronePlayerAnchor = AddonConfig.ENABLE_DRONE_PLAYER_ANCHOR_VALUE.getDefault();
                enableDroneCrashExplosionDamage = AddonConfig.ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE.getDefault();
            }
            case LUCAS_FLIGHT -> {
                lucasTakeoffAssistTicks = AddonConfig.LUCAS_TAKEOFF_ASSIST_TICKS_VALUE.getDefault();
                lucasTakeoffForwardBoost = AddonConfig.LUCAS_TAKEOFF_FORWARD_BOOST_VALUE.getDefault();
                lucasTakeoffUpBoost = AddonConfig.LUCAS_TAKEOFF_UP_BOOST_VALUE.getDefault();
                lucasMinSpeed = AddonConfig.LUCAS_MIN_SPEED_VALUE.getDefault();
                lucasMaxSpeed = AddonConfig.LUCAS_MAX_SPEED_VALUE.getDefault();
                lucasAcceleration = AddonConfig.LUCAS_ACCELERATION_VALUE.getDefault();
                lucasDrag = AddonConfig.LUCAS_DRAG_VALUE.getDefault();
                lucasTurnRate = AddonConfig.LUCAS_TURN_RATE_VALUE.getDefault();
                lucasPitchRate = AddonConfig.LUCAS_PITCH_RATE_VALUE.getDefault();
                lucasClimbSpeed = AddonConfig.LUCAS_CLIMB_SPEED_VALUE.getDefault();
                lucasDescendSpeed = AddonConfig.LUCAS_DESCEND_SPEED_VALUE.getDefault();
                lucasLiftStrength = AddonConfig.LUCAS_LIFT_STRENGTH_VALUE.getDefault();
                lucasRollVisualAmount = AddonConfig.LUCAS_ROLL_VISUAL_AMOUNT_VALUE.getDefault();
                lucasMinLiftSpeed = AddonConfig.LUCAS_MIN_LIFT_SPEED_VALUE.getDefault();
                lucasStallGravity = AddonConfig.LUCAS_STALL_GRAVITY_VALUE.getDefault();
                lucasStallEnabled = AddonConfig.LUCAS_STALL_ENABLED_VALUE.getDefault();
                lucasCanHover = AddonConfig.LUCAS_CAN_HOVER_VALUE.getDefault();
                lucasMaxFuel = AddonConfig.LUCAS_MAX_FUEL_VALUE.getDefault();
                lucasFuelDrainIdle = AddonConfig.LUCAS_FUEL_DRAIN_IDLE_VALUE.getDefault();
                lucasFuelDrainFlying = AddonConfig.LUCAS_FUEL_DRAIN_FLYING_VALUE.getDefault();
                lucasFuelDrainClimb = AddonConfig.LUCAS_FUEL_DRAIN_CLIMB_VALUE.getDefault();
                lucasLowFuelWarningPercent = AddonConfig.LUCAS_LOW_FUEL_WARNING_PERCENT_VALUE.getDefault();
                lucasCanFlyWithoutFuel = AddonConfig.LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE.getDefault();
                lucasHudScale = AddonConfig.LUCAS_HUD_SCALE_VALUE.getDefault();
                lucasEngineVolumeMultiplier = AddonConfig.LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
            }
            case FUEL -> {
                fuelMixerProcessTime = AddonConfig.FUEL_MIXER_PROCESS_TIME_VALUE.getDefault();
                gasolineJerrycanFuelAmount = AddonConfig.GASOLINE_JERRYCAN_FUEL_AMOUNT_VALUE.getDefault();
            }
            case BATTERY -> {
                enableBatterySystem = AddonConfig.ENABLE_BATTERY_SYSTEM_VALUE.getDefault();
                maxEnergy = AddonConfig.MAX_ENERGY_VALUE.getDefault();
                energyUsePerTick = AddonConfig.ENERGY_USE_PER_TICK_VALUE.getDefault();
                lowEnergyThreshold = AddonConfig.LOW_ENERGY_THRESHOLD_VALUE.getDefault();
                batteryTransferRate = AddonConfig.BATTERY_TRANSFER_RATE_VALUE.getDefault();
                returnHomeOnEmpty = AddonConfig.RETURN_HOME_ON_EMPTY_VALUE.getDefault();
            }
            case JAMMER -> {
                enableDroneJammer = AddonConfig.ENABLE_DRONE_JAMMER_VALUE.getDefault();
                jammerRange = AddonConfig.JAMMER_RANGE_VALUE.getDefault();
                jammerBuildUpTicks = AddonConfig.JAMMER_BUILD_UP_TICKS_VALUE.getDefault();
                jammerRecoveryTicks = AddonConfig.JAMMER_RECOVERY_TICKS_VALUE.getDefault();
                controlInterferenceThreshold = AddonConfig.CONTROL_INTERFERENCE_THRESHOLD_VALUE.getDefault();
                hardJamThreshold = AddonConfig.HARD_JAM_THRESHOLD_VALUE.getDefault();
                affectFriendlyDrones = AddonConfig.AFFECT_FRIENDLY_DRONES_VALUE.getDefault();
                droneRadarDetectionRange = AddonConfig.DRONE_RADAR_DETECTION_RANGE_VALUE.getDefault();
                droneRadarBeepEnabled = AddonConfig.DRONE_RADAR_BEEP_ENABLED_VALUE.getDefault();
                droneRadarBeepVolume = AddonConfig.DRONE_RADAR_BEEP_VOLUME_VALUE.getDefault();
                droneRadarMinBeepIntervalTicks = AddonConfig.DRONE_RADAR_MIN_BEEP_INTERVAL_TICKS_VALUE.getDefault();
                droneRadarMaxBeepIntervalTicks = AddonConfig.DRONE_RADAR_MAX_BEEP_INTERVAL_TICKS_VALUE.getDefault();
                droneRadarOnlyBeepWhenHeld = AddonConfig.DRONE_RADAR_ONLY_BEEP_WHEN_HELD_VALUE.getDefault();
                droneRadarHudScale = AddonConfig.DRONE_RADAR_HUD_SCALE_VALUE.getDefault();
                droneRadarHudXOffset = AddonConfig.DRONE_RADAR_HUD_X_OFFSET_VALUE.getDefault();
                droneRadarHudYOffset = AddonConfig.DRONE_RADAR_HUD_Y_OFFSET_VALUE.getDefault();
                droneRadarCompactMode = AddonConfig.DRONE_RADAR_COMPACT_MODE_VALUE.getDefault();
                debugDroneRadar = AddonConfig.DEBUG_DRONE_RADAR_VALUE.getDefault();
            }
            case WEATHER -> {
                weatherEffectsEnabled = AddonConfig.WEATHER_EFFECTS_ENABLED_VALUE.getDefault();
                enableThermalVision = AddonConfig.ENABLE_THERMAL_VISION_VALUE.getDefault();
                thermalVisionRange = AddonConfig.THERMAL_VISION_RANGE_VALUE.getDefault();
                thermalVisionNoiseEffect = AddonConfig.THERMAL_VISION_NOISE_EFFECT_VALUE.getDefault();
                rainVisibilityMultiplier = AddonConfig.RAIN_VISIBILITY_MULTIPLIER_VALUE.getDefault();
                stormInstabilityStrength = AddonConfig.STORM_INSTABILITY_STRENGTH_VALUE.getDefault();
            }
            case SPOTLIGHT -> {
                enableSpotlightModule = AddonConfig.ENABLE_SPOTLIGHT_MODULE_VALUE.getDefault();
                spotlightRange = AddonConfig.SPOTLIGHT_RANGE_VALUE.getDefault();
                spotlightEnergyCostPerSecond = AddonConfig.SPOTLIGHT_ENERGY_COST_PER_SECOND_VALUE.getDefault();
                spotlightRequiresModule = AddonConfig.SPOTLIGHT_REQUIRES_MODULE_VALUE.getDefault();
                spotlightAutoOffWhenBatteryEmpty = AddonConfig.SPOTLIGHT_AUTO_OFF_WHEN_BATTERY_EMPTY_VALUE.getDefault();
                spotlightRealLightEnabled = AddonConfig.SPOTLIGHT_REAL_LIGHT_ENABLED_VALUE.getDefault();
                spotlightLightRange = AddonConfig.SPOTLIGHT_LIGHT_RANGE_VALUE.getDefault();
            }
            case FPV_DRONE -> {
                enableFiberOpticMode = AddonConfig.ENABLE_FIBER_OPTIC_MODE_VALUE.getDefault();
                antiDroneNetCutsFiber = AddonConfig.ANTI_DRONE_NET_CUTS_FIBER_VALUE.getDefault();
                fiberOpticCableLength = AddonConfig.FIBER_OPTIC_CABLE_LENGTH_VALUE.getDefault();
                fiberOpticCableBreakDelayTicks = AddonConfig.FIBER_OPTIC_CABLE_BREAK_DELAY_TICKS_VALUE.getDefault();
                fiberOpticExtraBatteryDrainMultiplier = AddonConfig.FIBER_OPTIC_EXTRA_BATTERY_DRAIN_MULTIPLIER_VALUE.getDefault();
                normalJammersAffectFiberOptic = AddonConfig.NORMAL_JAMMERS_AFFECT_FIBER_OPTIC_VALUE.getDefault();
                debugFiberOptic = AddonConfig.DEBUG_FIBER_OPTIC_VALUE.getDefault();
                enableDroneScreenshotSound = AddonConfig.ENABLE_DRONE_SCREENSHOT_SOUND_VALUE.getDefault();
                droneSpeedMultiplier = AddonConfig.DRONE_SPEED_MULTIPLIER_VALUE.getDefault();
                droneEngineVolumeMultiplier = AddonConfig.DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
                droneMonitorHumVolumeMultiplier = AddonConfig.DRONE_MONITOR_HUM_VOLUME_MULTIPLIER_VALUE.getDefault();
                droneAirborneVolumeMultiplier = AddonConfig.DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE.getDefault();
                fpvCameraFov = AddonConfig.FPV_CAMERA_FOV_VALUE.getDefault();
                fpvCameraTiltDegrees = AddonConfig.FPV_CAMERA_TILT_DEGREES_VALUE.getDefault();
                enableFpvFisheyeEffect = AddonConfig.ENABLE_FPV_FISHEYE_EFFECT_VALUE.getDefault();
                enableFpvCameraVibration = AddonConfig.ENABLE_FPV_CAMERA_VIBRATION_VALUE.getDefault();
            }
            case DELIVERY_DRONE -> {
                droneSpeedMultiplier = AddonConfig.DRONE_SPEED_MULTIPLIER_VALUE.getDefault();
                droneEngineVolumeMultiplier = AddonConfig.DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
                droneAirborneVolumeMultiplier = AddonConfig.DRONE_AIRBORNE_VOLUME_MULTIPLIER_VALUE.getDefault();
                droneMaxRange = AddonConfig.DRONE_MAX_RANGE_VALUE.getDefault();
                enableDroneChunkLoading = AddonConfig.ENABLE_DRONE_CHUNK_LOADING_VALUE.getDefault();
                enableDroneCrashExplosionDamage = AddonConfig.ENABLE_DRONE_CRASH_EXPLOSION_DAMAGE_VALUE.getDefault();
            }
            case GLIDE_BOMB -> {
                droneSpeedMultiplier = AddonConfig.DRONE_SPEED_MULTIPLIER_VALUE.getDefault();
                lucasTakeoffForwardBoost = AddonConfig.LUCAS_TAKEOFF_FORWARD_BOOST_VALUE.getDefault();
                lucasDrag = AddonConfig.LUCAS_DRAG_VALUE.getDefault();
                droneEngineVolumeMultiplier = AddonConfig.DRONE_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
                fpvCameraFov = AddonConfig.FPV_CAMERA_FOV_VALUE.getDefault();
                fpvCameraTiltDegrees = AddonConfig.FPV_CAMERA_TILT_DEGREES_VALUE.getDefault();
            }
            case HEAVY_BOMBER_DRONE, GERANIUM_2 -> {
                lucasMaxSpeed = AddonConfig.LUCAS_MAX_SPEED_VALUE.getDefault();
                lucasAcceleration = AddonConfig.LUCAS_ACCELERATION_VALUE.getDefault();
                lucasDrag = AddonConfig.LUCAS_DRAG_VALUE.getDefault();
                lucasMaxFuel = AddonConfig.LUCAS_MAX_FUEL_VALUE.getDefault();
                lucasFuelDrainFlying = AddonConfig.LUCAS_FUEL_DRAIN_FLYING_VALUE.getDefault();
                lucasEngineVolumeMultiplier = AddonConfig.LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
                lucasCanFlyWithoutFuel = AddonConfig.LUCAS_CAN_FLY_WITHOUT_FUEL_VALUE.getDefault();
            }
            case BAYRAKTAR_TB2 -> {
                lucasMaxSpeed = AddonConfig.LUCAS_MAX_SPEED_VALUE.getDefault();
                lucasAcceleration = AddonConfig.LUCAS_ACCELERATION_VALUE.getDefault();
                lucasDrag = AddonConfig.LUCAS_DRAG_VALUE.getDefault();
                lucasMaxFuel = AddonConfig.LUCAS_MAX_FUEL_VALUE.getDefault();
                lucasFuelDrainFlying = AddonConfig.LUCAS_FUEL_DRAIN_FLYING_VALUE.getDefault();
                lucasEngineVolumeMultiplier = AddonConfig.LUCAS_ENGINE_VOLUME_MULTIPLIER_VALUE.getDefault();
            }
        }
    }

    private void loadFromCurrentConfig() {
        droneMaxRange = AddonConfig.droneMaxRange();
        enableDroneChunkLoading = AddonConfig.enableDroneChunkLoading();
        droneChunkLoadRadius = AddonConfig.droneChunkLoadRadius();
        enableDronePlayerAnchor = AddonConfig.enableDronePlayerAnchor();
        enableDroneCrashExplosionDamage = AddonConfig.enableDroneCrashExplosionDamage();
        lucasTakeoffAssistTicks = AddonConfig.lucasTakeoffAssistTicks();
        lucasTakeoffForwardBoost = AddonConfig.lucasTakeoffForwardBoost();
        lucasTakeoffUpBoost = AddonConfig.lucasTakeoffUpBoost();
        lucasMinSpeed = AddonConfig.lucasMinSpeed();
        lucasMaxSpeed = AddonConfig.lucasMaxSpeed();
        lucasAcceleration = AddonConfig.lucasAcceleration();
        lucasDrag = AddonConfig.lucasDrag();
        lucasTurnRate = AddonConfig.lucasTurnRate();
        lucasPitchRate = AddonConfig.lucasPitchRate();
        lucasClimbSpeed = AddonConfig.lucasClimbSpeed();
        lucasDescendSpeed = AddonConfig.lucasDescendSpeed();
        lucasLiftStrength = AddonConfig.lucasLiftStrength();
        lucasRollVisualAmount = AddonConfig.lucasRollVisualAmount();
        lucasMinLiftSpeed = AddonConfig.lucasMinLiftSpeed();
        lucasStallGravity = AddonConfig.lucasStallGravity();
        lucasStallEnabled = AddonConfig.lucasStallEnabled();
        lucasCanHover = AddonConfig.lucasCanHover();
        lucasMaxFuel = AddonConfig.lucasMaxFuel();
        lucasFuelDrainIdle = AddonConfig.lucasFuelDrainIdle();
        lucasFuelDrainFlying = AddonConfig.lucasFuelDrainFlying();
        lucasFuelDrainClimb = AddonConfig.lucasFuelDrainClimb();
        lucasLowFuelWarningPercent = AddonConfig.lucasLowFuelWarningPercent();
        lucasCanFlyWithoutFuel = AddonConfig.lucasCanFlyWithoutFuel();
        lucasHudScale = AddonConfig.lucasHudScale();
        lucasEngineVolumeMultiplier = AddonConfig.lucasEngineVolumeMultiplier();
        fuelMixerProcessTime = AddonConfig.fuelMixerProcessTime();
        gasolineJerrycanFuelAmount = AddonConfig.gasolineJerrycanFuelAmount();

        enableBatterySystem = AddonConfig.enableBatterySystem();
        maxEnergy = AddonConfig.maxEnergy();
        energyUsePerTick = AddonConfig.energyUsePerTick();
        lowEnergyThreshold = AddonConfig.lowEnergyThreshold();
        batteryTransferRate = AddonConfig.batteryTransferRate();
        returnHomeOnEmpty = AddonConfig.returnHomeOnEmpty();

        enableDroneJammer = AddonConfig.enableDroneJammer();
        jammerRange = AddonConfig.jammerRange();
        jammerBuildUpTicks = AddonConfig.jammerBuildUpTicks();
        jammerRecoveryTicks = AddonConfig.jammerRecoveryTicks();
        controlInterferenceThreshold = AddonConfig.controlInterferenceThreshold();
        hardJamThreshold = AddonConfig.hardJamThreshold();
        affectFriendlyDrones = AddonConfig.affectFriendlyDrones();
        droneRadarDetectionRange = AddonConfig.droneRadarDetectionRange();
        droneRadarBeepEnabled = AddonConfig.droneRadarBeepEnabled();
        droneRadarBeepVolume = AddonConfig.droneRadarBeepVolume();
        droneRadarMinBeepIntervalTicks = AddonConfig.droneRadarMinBeepIntervalTicks();
        droneRadarMaxBeepIntervalTicks = AddonConfig.droneRadarMaxBeepIntervalTicks();
        droneRadarOnlyBeepWhenHeld = AddonConfig.droneRadarOnlyBeepWhenHeld();
        droneRadarHudScale = AddonConfig.droneRadarHudScale();
        droneRadarHudXOffset = AddonConfig.droneRadarHudXOffset();
        droneRadarHudYOffset = AddonConfig.droneRadarHudYOffset();
        droneRadarCompactMode = AddonConfig.droneRadarCompactMode();
        debugDroneRadar = AddonConfig.debugDroneRadar();

        weatherEffectsEnabled = AddonConfig.weatherEffectsEnabled();
        enableThermalVision = AddonConfig.enableThermalVision();
        thermalVisionRange = AddonConfig.thermalVisionRange();
        thermalVisionNoiseEffect = AddonConfig.thermalVisionNoiseEffect();
        rainVisibilityMultiplier = AddonConfig.rainVisibilityMultiplier();
        stormInstabilityStrength = AddonConfig.stormInstabilityStrength();

        enableFiberOpticMode = AddonConfig.enableFiberOpticMode();
        antiDroneNetCutsFiber = AddonConfig.antiDroneNetCutsFiber();
        fiberOpticCableLength = AddonConfig.fiberOpticCableLength();
        fiberOpticCableBreakDelayTicks = AddonConfig.fiberOpticCableBreakDelayTicks();
        fiberOpticExtraBatteryDrainMultiplier = AddonConfig.fiberOpticExtraBatteryDrainMultiplier();
        normalJammersAffectFiberOptic = AddonConfig.normalJammersAffectFiberOptic();
        debugFiberOptic = AddonConfig.debugFiberOptic();

        enableSpotlightModule = AddonConfig.enableSpotlightModule();
        spotlightRange = AddonConfig.spotlightRange();
        spotlightEnergyCostPerSecond = AddonConfig.spotlightEnergyCostPerSecond();
        spotlightRequiresModule = AddonConfig.spotlightRequiresModule();
        spotlightAutoOffWhenBatteryEmpty = AddonConfig.spotlightAutoOffWhenBatteryEmpty();
        spotlightRealLightEnabled = AddonConfig.spotlightRealLightEnabled();
        spotlightLightRange = AddonConfig.spotlightLightRange();

        enableDroneScreenshotSound = AddonConfig.enableDroneScreenshotSound();
        droneSpeedMultiplier = AddonConfig.droneSpeedMultiplier();
        droneEngineVolumeMultiplier = AddonConfig.droneEngineVolumeMultiplier();
        droneMonitorHumVolumeMultiplier = AddonConfig.droneMonitorHumVolumeMultiplier();
        droneAirborneVolumeMultiplier = AddonConfig.droneAirborneVolumeMultiplier();
        fpvCameraFov = AddonConfig.fpvCameraFov();
        fpvCameraTiltDegrees = AddonConfig.fpvCameraTiltDegrees();
        enableFpvFisheyeEffect = AddonConfig.enableFpvFisheyeEffect();
        enableFpvCameraVibration = AddonConfig.enableFpvCameraVibration();
    }
}
