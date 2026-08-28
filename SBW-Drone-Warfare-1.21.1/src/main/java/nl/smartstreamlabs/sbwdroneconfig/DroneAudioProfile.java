package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

enum DroneAudioProfile {
    CUBED_FPV(
            AddonSounds.CUBED_FPV_DRONE_ENGINE_NEAR,
            AddonSounds.CUBED_FPV_DRONE_ENGINE_MID,
            AddonSounds.CUBED_FPV_DRONE_ENGINE_FAR,
            160.0D,
            new LayerRange(0.0D, 0.0D, 32.0D, 48.0D),
            new LayerRange(16.0D, 32.0D, 80.0D, 96.0D),
            new LayerRange(64.0D, 96.0D, 132.0D, 160.0D)
    ),
    LUCAS(
            AddonSounds.LUCAS_DRONE_ENGINE_NEAR,
            AddonSounds.LUCAS_DRONE_ENGINE_MID,
            AddonSounds.LUCAS_DRONE_ENGINE_FAR,
            220.0D,
            new LayerRange(0.0D, 0.0D, 40.0D, 64.0D),
            new LayerRange(24.0D, 48.0D, 96.0D, 120.0D),
            new LayerRange(80.0D, 120.0D, 180.0D, 220.0D)
    ),
    DELIVERY(
            AddonSounds.DELIVERY_DRONE_ENGINE,
            AddonSounds.DELIVERY_DRONE_ENGINE,
            AddonSounds.DELIVERY_DRONE_ENGINE,
            140.0D,
            new LayerRange(0.0D, 0.0D, 96.0D, 140.0D),
            new LayerRange(0.0D, 0.0D, 0.0D, 0.0D),
            new LayerRange(0.0D, 0.0D, 0.0D, 0.0D)
    ),
    GLIDE_BOMB(
            AddonSounds.GLIDE_BOMB_ENGINE,
            AddonSounds.GLIDE_BOMB_ENGINE,
            AddonSounds.GLIDE_BOMB_ENGINE,
            180.0D,
            new LayerRange(0.0D, 0.0D, 120.0D, 180.0D),
            new LayerRange(0.0D, 0.0D, 0.0D, 0.0D),
            new LayerRange(0.0D, 0.0D, 0.0D, 0.0D)
    );

    private static final float MAX_INSTANCE_VOLUME = 3.0F;

    private final RegistryObject<SoundEvent> nearSound;
    private final RegistryObject<SoundEvent> midSound;
    private final RegistryObject<SoundEvent> farSound;
    private final double maxDistance;
    private final LayerRange nearRange;
    private final LayerRange midRange;
    private final LayerRange farRange;

    DroneAudioProfile(RegistryObject<SoundEvent> nearSound,
                      RegistryObject<SoundEvent> midSound,
                      RegistryObject<SoundEvent> farSound,
                      double maxDistance,
                      LayerRange nearRange,
                      LayerRange midRange,
                      LayerRange farRange) {
        this.nearSound = nearSound;
        this.midSound = midSound;
        this.farSound = farSound;
        this.maxDistance = maxDistance;
        this.nearRange = nearRange;
        this.midRange = midRange;
        this.farRange = farRange;
    }

    SoundEvent sound(Layer layer) {
        return switch (layer) {
            case NEAR -> nearSound.get();
            case MID -> midSound.get();
            case FAR -> farSound.get();
        };
    }

    double maxDistance() {
        return maxDistance;
    }

    int maxAudibleDrones() {
        return switch (this) {
            case CUBED_FPV -> AddonConfig.maxAudibleCubedFpvDrones();
            case LUCAS -> AddonConfig.maxAudibleLucasDrones();
            case DELIVERY, GLIDE_BOMB -> AddonConfig.maxAudibleCubedFpvDrones();
        };
    }

    float targetVolume(Layer layer, double distance, double rpm, boolean internalFpvListener, double occlusionGain) {
        double gain = layerRange(layer).gain(distance);
        if (gain <= 0.0D) {
            return 0.0F;
        }

        double intensity = switch (this) {
            case CUBED_FPV -> 0.22D + Math.pow(clamp01(rpm), 1.25D) * 1.18D;
            case LUCAS -> 0.34D + Math.pow(clamp01(rpm), 1.05D) * 1.24D;
            case DELIVERY -> 0.26D + Math.pow(clamp01(rpm), 1.15D) * 1.10D;
            case GLIDE_BOMB -> 0.42D + Math.pow(clamp01(rpm), 1.10D) * 1.34D;
        };
        double internalGain = internalFpvListener && this == CUBED_FPV ? internalLayerGain(layer) : 1.0D;
        double volume = masterVolume() * layerVolume(layer) * gain * intensity * internalGain * occlusionGain;
        return (float) clamp(volume, 0.0D, MAX_INSTANCE_VOLUME);
    }

    float targetPitch(Layer layer, double rpm, double dopplerMultiplier, boolean internalFpvListener) {
        double clampedRpm = clamp01(rpm);
        double pitch = switch (this) {
            case CUBED_FPV -> switch (layer) {
                case NEAR -> lerp(clampedRpm, 0.92D, 1.28D);
                case MID -> lerp(clampedRpm, 0.84D, 1.10D);
                case FAR -> lerp(clampedRpm, 0.76D, 0.98D);
            };
            case LUCAS -> switch (layer) {
                case NEAR -> lerp(clampedRpm, 0.68D, 1.08D);
                case MID -> lerp(clampedRpm, 0.62D, 0.94D);
                case FAR -> lerp(clampedRpm, 0.56D, 0.80D);
            };
            case DELIVERY -> switch (layer) {
                case NEAR -> lerp(clampedRpm, 0.88D, 1.20D);
                case MID -> 1.0D;
                case FAR -> 1.0D;
            };
            case GLIDE_BOMB -> switch (layer) {
                case NEAR -> lerp(clampedRpm, 0.82D, 1.18D);
                case MID -> 1.0D;
                case FAR -> 1.0D;
            };
        };
        if (internalFpvListener && this == CUBED_FPV) {
            pitch *= 0.96D;
        }
        return (float) clamp(pitch * dopplerMultiplier, 0.45D, 1.65D);
    }

    double layerGainForDistance(Layer layer, double distance) {
        return layerRange(layer).gain(distance);
    }

    private double masterVolume() {
        return switch (this) {
            case CUBED_FPV -> AddonConfig.cubedFpvExternalVolume();
            case LUCAS -> AddonConfig.lucasExternalVolume();
            case DELIVERY, GLIDE_BOMB -> AddonConfig.cubedFpvExternalVolume();
        };
    }

    private double layerVolume(Layer layer) {
        return switch (this) {
            case CUBED_FPV -> switch (layer) {
                case NEAR -> AddonConfig.cubedFpvNearVolume();
                case MID -> AddonConfig.cubedFpvMidVolume();
                case FAR -> AddonConfig.cubedFpvFarVolume();
            };
            case LUCAS -> switch (layer) {
                case NEAR -> AddonConfig.lucasNearVolume();
                case MID -> AddonConfig.lucasMidVolume();
                case FAR -> AddonConfig.lucasFarVolume();
            };
            case DELIVERY -> switch (layer) {
                case NEAR -> AddonConfig.cubedFpvNearVolume();
                case MID, FAR -> 0.0D;
            };
            case GLIDE_BOMB -> switch (layer) {
                case NEAR -> AddonConfig.cubedFpvNearVolume();
                case MID, FAR -> 0.0D;
            };
        };
    }

    private double internalLayerGain(Layer layer) {
        double gain = AddonConfig.fpvInternalAudioGain();
        return switch (layer) {
            case NEAR -> gain * 0.38D;
            case MID -> gain * 0.58D;
            case FAR -> gain * 0.34D;
        };
    }

    private LayerRange layerRange(Layer layer) {
        return switch (layer) {
            case NEAR -> nearRange;
            case MID -> midRange;
            case FAR -> farRange;
        };
    }

    static double smoothLayerGain(double distance, double fadeInStart, double fullStart, double fullEnd, double fadeOutEnd) {
        if (distance < fadeInStart || distance > fadeOutEnd) {
            return 0.0D;
        }
        if (distance < fullStart) {
            return smoothstep((distance - fadeInStart) / Math.max(0.001D, fullStart - fadeInStart));
        }
        if (distance <= fullEnd) {
            return 1.0D;
        }
        return 1.0D - smoothstep((distance - fullEnd) / Math.max(0.001D, fadeOutEnd - fullEnd));
    }

    static double clamp01(double value) {
        return clamp(value, 0.0D, 1.0D);
    }

    private static double smoothstep(double value) {
        double x = clamp01(value);
        return x * x * (3.0D - 2.0D * x);
    }

    private static double lerp(double amount, double min, double max) {
        return min + (max - min) * amount;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    enum Layer {
        NEAR,
        MID,
        FAR
    }

    private record LayerRange(double fadeInStart, double fullStart, double fullEnd, double fadeOutEnd) {
        double gain(double distance) {
            return smoothLayerGain(distance, fadeInStart, fullStart, fullEnd, fadeOutEnd);
        }
    }
}
