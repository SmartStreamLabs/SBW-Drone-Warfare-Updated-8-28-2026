package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;

abstract class DroneExternalSoundInstance extends AbstractTickableSoundInstance {
    private static final float VOLUME_SMOOTHING = 0.18F;
    private static final float PITCH_SMOOTHING = 0.12F;
    private static final float STOP_VOLUME = 0.004F;

    protected final VehicleEntity drone;
    private final DroneAudioProfile profile;
    private final DroneAudioProfile.Layer layer;
    private float targetVolume;
    private float targetPitch = 1.0F;
    private boolean stopping;
    private final long created;

    DroneExternalSoundInstance(VehicleEntity drone, DroneAudioProfile profile, DroneAudioProfile.Layer layer) {
        super(profile.sound(layer), SoundSource.AMBIENT, drone.level().getRandom());
        this.drone = drone;this.created=drone.level().getGameTime();
        this.profile = profile;
        this.layer = layer;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = Attenuation.NONE;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.x = drone.getX();
        this.y = drone.getY();
        this.z = drone.getZ();
    }

    @Override
    public void tick() {
        if (!DroneAudioManager.isAudiblyActive(this.drone, this.profile)) {
            requestStop();
        } else {
            this.x = this.drone.getX();
            this.y = this.drone.getY();
            this.z = this.drone.getZ();
        }

        this.volume = approach(this.volume, this.targetVolume, VOLUME_SMOOTHING);
        this.pitch = approach(this.pitch, this.targetPitch, PITCH_SMOOTHING);

        if (this.stopping && this.volume <= STOP_VOLUME) {
            this.stop();
        }
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    void updateTargets(float volume, float pitch) {
        this.targetVolume = Math.max(0.0F, volume);
        this.targetPitch = Math.max(0.1F, pitch);
        this.stopping = false;
    }

    boolean lostChannel(net.minecraft.client.sounds.SoundManager manager){return drone.level().getGameTime()-created>=10&&!manager.isActive(this);}
    void stopImmediately(){targetVolume=0;stopping=true;stop();net.minecraft.client.Minecraft.getInstance().getSoundManager().stop(this);}
    void requestStop() {
        this.targetVolume = 0.0F;
        this.stopping = true;
    }

    DroneAudioProfile profile() {
        return profile;
    }

    DroneAudioProfile.Layer layer() {
        return layer;
    }

    private static float approach(float current, float target, float amount) {
        return current + (target - current) * amount;
    }
}
