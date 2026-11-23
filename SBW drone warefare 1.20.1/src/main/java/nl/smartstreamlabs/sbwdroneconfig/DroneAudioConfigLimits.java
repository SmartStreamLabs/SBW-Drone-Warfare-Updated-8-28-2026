package nl.smartstreamlabs.sbwdroneconfig;

public final class DroneAudioConfigLimits {
    public static final double MIN_ENGINE_VOLUME_MULTIPLIER = 0.0D;
    public static final double MAX_ENGINE_VOLUME_MULTIPLIER = 5.0D;
    public static final double MIN_MONITOR_HUM_VOLUME_MULTIPLIER = 0.0D;
    public static final double MAX_MONITOR_HUM_VOLUME_MULTIPLIER = 2.0D;
    public static final double MIN_AIRBORNE_VOLUME_MULTIPLIER = 1.0D;
    public static final double MAX_AIRBORNE_VOLUME_MULTIPLIER = 5.0D;
    public static final double MIN_EXTERNAL_LAYER_VOLUME = 0.0D;
    public static final double MAX_EXTERNAL_LAYER_VOLUME = 2.0D;
    public static final double MIN_INTERNAL_AUDIO_GAIN = 0.0D;
    public static final double MAX_INTERNAL_AUDIO_GAIN = 1.0D;
    public static final double MIN_DOPPLER_STRENGTH = 0.0D;
    public static final double MAX_DOPPLER_STRENGTH = 1.0D;
    public static final int MIN_AUDIBLE_DRONES = 0;
    public static final int MAX_AUDIBLE_DRONES = 64;

    private DroneAudioConfigLimits() {
    }
}
