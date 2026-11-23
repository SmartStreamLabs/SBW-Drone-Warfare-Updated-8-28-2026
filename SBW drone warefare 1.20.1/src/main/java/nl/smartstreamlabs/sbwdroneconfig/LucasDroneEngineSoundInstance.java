package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LucasDroneEngineSoundInstance extends AbstractTickableSoundInstance {
    public static final double AUDIBLE_RANGE = 96.0D;
    private static final float BASE_VOLUME = 1.2F;
    private static final float MAX_VOLUME = 2.5F;
    private static final float MIN_PITCH = 0.75F;
    private static final float MAX_PITCH = 1.25F;
    private static final float GROUND_LISTENER_VOLUME_BOOST = 1.45F;
    private static final float MAX_AIRSPEED_REFERENCE = 3.20F;
    private static final float MAX_MOTION_REFERENCE = 1.75F;

    private final LucasDroneEntity drone;
    private boolean stopped;

    public LucasDroneEngineSoundInstance(LucasDroneEntity drone) {
        super(AddonSounds.LUCAS_DRONE_ENGINE.get(), SoundSource.AMBIENT, drone.level().getRandom());
        this.drone = drone;
        this.looping = true;
        this.delay = 0;
        this.relative = false;
        this.attenuation = Attenuation.LINEAR;
        this.volume = 0.0F;
        this.pitch = MIN_PITCH;
        this.x = drone.getX();
        this.y = drone.getY();
        this.z = drone.getZ();
    }

    @Override
    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!shouldPlayFor(player, this.drone)) {
            stopInternal();
            return;
        }

        this.x = this.drone.getX();
        this.y = this.drone.getY();
        this.z = this.drone.getZ();
        this.volume = computeVolume(player);
        this.pitch = computePitch();
    }

    public static boolean shouldPlayFor(LocalPlayer player, LucasDroneEntity drone) {
        if (player == null || drone == null) {
            return false;
        }

        if (!drone.level().isClientSide || !player.level().isClientSide) {
            return false;
        }

        if (!drone.isAlive() || drone.isRemoved() || !drone.engineRunning()) {
            return false;
        }

        if (drone.onGround()) {
            return false;
        }

        return player.position().distanceTo(drone.position()) <= AUDIBLE_RANGE;
    }

    public int getDroneEntityId() {
        return this.drone.getId();
    }

    public Vec3 getDronePosition() {
        return this.drone.position();
    }

    public double getDistanceTo(LocalPlayer player) {
        if (player == null) {
            return 0.0D;
        }
        return player.position().distanceTo(this.drone.position());
    }

    public float getCurrentVolume() {
        return this.volume;
    }

    public float getCurrentPitch() {
        return this.pitch;
    }

    public String getSoundEventId() {
        return SbwDroneRangeConfig.MOD_ID + ":lucas_drone_engine";
    }

    @Override
    public boolean isStopped() {
        return this.stopped;
    }

    public void requestStop() {
        stopInternal();
    }

    private float computeVolume(LocalPlayer player) {
        float speedFactor = computeSpeedFactor();
        float configuredMultiplier = (float) Math.max(0.0D, AddonConfig.lucasEngineVolumeMultiplier());
        float volume = Mth.lerp(speedFactor, BASE_VOLUME, MAX_VOLUME) * configuredMultiplier;

        if (player != null && player.onGround() && !SbwCompat.isUsingLinkedMonitorForDrone(player, this.drone)) {
            volume *= GROUND_LISTENER_VOLUME_BOOST;
        }

        return volume;
    }

    private float computePitch() {
        float speedFactor = computeSpeedFactor();
        return Mth.lerp(speedFactor, MIN_PITCH, MAX_PITCH);
    }

    private float computeSpeedFactor() {
        float throttleFactor = Mth.clamp(this.drone.getLucasThrottle(), 0.0F, 1.0F);
        float airspeedFactor = Mth.clamp(this.drone.getLucasAirspeed() / MAX_AIRSPEED_REFERENCE, 0.0F, 1.0F);
        float motionFactor = Mth.clamp((float) (this.drone.getDeltaMovement().length() / MAX_MOTION_REFERENCE), 0.0F, 1.0F);
        return Math.max(throttleFactor, Math.max(airspeedFactor, motionFactor));
    }

    private void stopInternal() {
        if (this.stopped) {
            return;
        }

        this.stopped = true;
        this.stop();
    }
}
