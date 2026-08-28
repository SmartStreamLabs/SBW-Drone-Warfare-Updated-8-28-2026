package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.client.sound.VehicleSoundInstance;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DroneAudioManager {
    private static final double MIN_LAYER_START_VOLUME = 0.015D;
    private static final double ORIGINAL_SBW_DRONE_SOUND_DISTANCE = 128.0D;
    private static final int ORIGINAL_SBW_DRONE_FADE_TICKS = 3;
    private static final Map<UUID, ManagedDroneAudio> ACTIVE_AUDIO = new HashMap<>();
    private static final Map<UUID, OccludedSbwDroneEngineSound> ACTIVE_SBW_DRONE_SOUNDS = new HashMap<>();
    private static ClientLevel activeLevel;

    private DroneAudioManager() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(DroneAudioManager::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> VehicleEntity.playEngineSound = DroneAudioManager::playEngineSound);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneAudioManager.class);
    }

    private static void playEngineSound(VehicleEntity vehicle) {
        if (isManagedDroneAudio(vehicle)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || vehicle == null || vehicle.isSilent()) {
            return;
        }
        if (SbwCompat.isDrone(vehicle)) {
            playOccludedSbwDroneSound(vehicle);
            return;
        }
        minecraft.getSoundManager().play(new VehicleSoundInstance.EngineSound(vehicle));
    }

    private static boolean isManagedDroneAudio(VehicleEntity vehicle) {
        return vehicle instanceof CubedFpvDroneEntity
                || vehicle instanceof LucasDroneEntity
                || vehicle instanceof DeliveryDroneEntity
                || vehicle instanceof GlideBombEntity
                || vehicle instanceof HeavyBomberDroneEntity
                || vehicle instanceof Geranium2Entity;
    }

    private static void playOccludedSbwDroneSound(VehicleEntity drone) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (!shouldPlayOriginalSbwDroneSound(player, drone)) {
            return;
        }

        OccludedSbwDroneEngineSound existing = ACTIVE_SBW_DRONE_SOUNDS.get(drone.getUUID());
        if (existing != null && !existing.isStopped()) {
            existing.markActive();
            return;
        }

        OccludedSbwDroneEngineSound sound = new OccludedSbwDroneEngineSound(drone);
        ACTIVE_SBW_DRONE_SOUNDS.put(drone.getUUID(), sound);
        minecraft.getSoundManager().play(sound);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        VehicleEntity.playEngineSound = DroneAudioManager::playEngineSound;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            stopAll();
            activeLevel = null;
            return;
        }

        if (activeLevel != level) {
            stopAll();
            activeLevel = level;
        }

        ListenerState listener = ListenerState.capture(minecraft, player);
        List<DroneCandidate> cubedCandidates = new ArrayList<>();
        List<DroneCandidate> lucasCandidates = new ArrayList<>();
        List<DroneCandidate> deliveryCandidates = new ArrayList<>();
        List<DroneCandidate> glideBombCandidates = new ArrayList<>();
        collectCandidates(level, player, listener, cubedCandidates, lucasCandidates, deliveryCandidates, glideBombCandidates);

        Set<UUID> selected = new HashSet<>();
        ensureNearbyOriginalSbwDroneSounds(player, level);
        selectAndUpdate(minecraft, player, listener, cubedCandidates, DroneAudioProfile.CUBED_FPV, selected);
        selectAndUpdate(minecraft, player, listener, lucasCandidates, DroneAudioProfile.LUCAS, selected);
        selectAndUpdate(minecraft, player, listener, deliveryCandidates, DroneAudioProfile.DELIVERY, selected);
        selectAndUpdate(minecraft, player, listener, glideBombCandidates, DroneAudioProfile.GLIDE_BOMB, selected);
        fadeOutUnselected(selected);
    }

    private static void collectCandidates(ClientLevel level,
                                          LocalPlayer player,
                                          ListenerState listener,
                                          List<DroneCandidate> cubedCandidates,
                                          List<DroneCandidate> lucasCandidates,
                                          List<DroneCandidate> deliveryCandidates,
        List<DroneCandidate> glideBombCandidates) {
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof Geranium2Entity geranium) {
                addCandidate(geranium, DroneAudioProfile.LUCAS, player, listener, lucasCandidates);
            } else if (entity instanceof HeavyBomberDroneEntity heavyBomber) {
                addCandidate(heavyBomber, DroneAudioProfile.GLIDE_BOMB, player, listener, glideBombCandidates);
            } else if (entity instanceof GlideBombEntity glideBomb) {
                addCandidate(glideBomb, DroneAudioProfile.GLIDE_BOMB, player, listener, glideBombCandidates);
            } else if (entity instanceof DeliveryDroneEntity deliveryDrone) {
                addCandidate(deliveryDrone, DroneAudioProfile.DELIVERY, player, listener, deliveryCandidates);
            } else if (entity instanceof CubedFpvDroneEntity cubedFpvDrone) {
                addCandidate(cubedFpvDrone, DroneAudioProfile.CUBED_FPV, player, listener, cubedCandidates);
            } else if (entity instanceof LucasDroneEntity lucasDrone) {
                addCandidate(lucasDrone, DroneAudioProfile.LUCAS, player, listener, lucasCandidates);
            }
        }
    }

    private static void addCandidate(VehicleEntity drone,
                                     DroneAudioProfile profile,
                                     LocalPlayer player,
                                     ListenerState listener,
                                     List<DroneCandidate> candidates) {
        if (!isAudiblyActive(drone, profile)) {
            return;
        }

        double distance = listener.position().distanceTo(drone.position());
        if (distance > profile.maxDistance()) {
            return;
        }

        double rpm = estimateRpm(drone, profile);
        double priority = priority(profile, drone, listener, distance, rpm);
        boolean internalFpvListener = isInternalFpvListener(player, drone);
        candidates.add(new DroneCandidate(drone, profile, distance, rpm, priority, internalFpvListener));
    }

    private static void selectAndUpdate(Minecraft minecraft,
                                        LocalPlayer player,
                                        ListenerState listener,
                                        List<DroneCandidate> candidates,
                                        DroneAudioProfile profile,
                                        Set<UUID> selected) {
        int limit = profile.maxAudibleDrones();
        if (limit <= 0 || candidates.isEmpty()) {
            return;
        }

        candidates.sort(Comparator.comparingDouble(DroneCandidate::priority).reversed());
        int count = Math.min(limit, candidates.size());
        for (int index = 0; index < count; index++) {
            DroneCandidate candidate = candidates.get(index);
            UUID droneId = candidate.drone().getUUID();
            selected.add(droneId);

            ManagedDroneAudio managed = ACTIVE_AUDIO.get(droneId);
            if (managed == null || managed.profile() != profile || managed.isStopped()) {
                if (managed != null) {
                    managed.requestStop();
                }
                managed = new ManagedDroneAudio(candidate.drone(), profile);
                ACTIVE_AUDIO.put(droneId, managed);
            }

            managed.update(minecraft, player, listener, candidate);
        }
    }

    private static void fadeOutUnselected(Set<UUID> selected) {
        ACTIVE_AUDIO.entrySet().removeIf(entry -> {
            ManagedDroneAudio managed = entry.getValue();
            if (!selected.contains(entry.getKey())) {
                managed.requestStop();
            }
            return managed.isStopped();
        });
    }

    private static void stopAll() {
        for (ManagedDroneAudio managed : ACTIVE_AUDIO.values()) {
            managed.requestStop();
        }
        ACTIVE_AUDIO.clear();
        for (OccludedSbwDroneEngineSound sound : List.copyOf(ACTIVE_SBW_DRONE_SOUNDS.values())) {
            sound.requestStop();
        }
        ACTIVE_SBW_DRONE_SOUNDS.clear();
        DroneSoundOcclusion.clear();
    }

    private static void ensureNearbyOriginalSbwDroneSounds(LocalPlayer player, ClientLevel level) {
        ACTIVE_SBW_DRONE_SOUNDS.entrySet().removeIf(entry -> entry.getValue().isStopped());
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof VehicleEntity drone && SbwCompat.isDrone(drone) && !isManagedDroneAudio(drone)) {
                playOccludedSbwDroneSound(drone);
            }
        }
    }

    private static boolean shouldPlayOriginalSbwDroneSound(LocalPlayer player, VehicleEntity drone) {
        if (player == null || drone == null || drone.isRemoved() || !drone.isAlive() || !drone.engineRunning()) {
            return false;
        }
        if (SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
            return true;
        }
        if (drone.onGround()) {
            return false;
        }
        return ListenerState.capture(Minecraft.getInstance(), player).position().distanceTo(drone.position()) <= ORIGINAL_SBW_DRONE_SOUND_DISTANCE;
    }

    private static boolean isInternalFpvListener(LocalPlayer player, VehicleEntity drone) {
        if (!(drone instanceof CubedFpvDroneEntity cubedFpvDrone)) {
            return false;
        }
        return DroneClientViewContext.isLocalFpvViewOf(cubedFpvDrone)
                || SbwCompat.isUsingLinkedMonitorForDrone(player, cubedFpvDrone);
    }

    static boolean isAudiblyActive(VehicleEntity drone, DroneAudioProfile profile) {
        if (drone == null || drone.isRemoved() || !drone.isAlive()) {
            return false;
        }
        if (drone.engineRunning()) {
            return true;
        }
        if (profile != DroneAudioProfile.DELIVERY) {
            return false;
        }

        Vec3 movement = drone.getDeltaMovement();
        return !drone.onGround()
                || movement.y > 0.01D
                || movement.horizontalDistanceSqr() > 0.0004D
                || DroneAudioProfile.clamp01(drone.getEngineSoundVolume()) > 0.01D;
    }

    static double estimateRpm(VehicleEntity drone, DroneAudioProfile profile) {
        Vec3 movement = drone.getDeltaMovement();
        double motion = movement.length();
        if (profile == DroneAudioProfile.LUCAS && drone instanceof LucasDroneEntity lucasDrone) {
            double throttle = DroneAudioProfile.clamp01(lucasDrone.getLucasThrottle());
            double airspeed = DroneAudioProfile.clamp01(lucasDrone.getLucasAirspeed() / 3.2D);
            double motionFactor = DroneAudioProfile.clamp01(motion / 1.75D);
            return Math.max(0.20D, Math.max(throttle, Math.max(airspeed, motionFactor)));
        }
        if (profile == DroneAudioProfile.LUCAS && drone instanceof Geranium2Entity geranium) {
            double throttle = DroneAudioProfile.clamp01(geranium.getGlideBombThrottle());
            double airspeed = DroneAudioProfile.clamp01(geranium.getGlideBombAirspeed() / 4.6D);
            double motionFactor = DroneAudioProfile.clamp01(motion / 2.2D);
            return Math.max(0.22D, Math.max(throttle, Math.max(airspeed, motionFactor)));
        }

        if (profile == DroneAudioProfile.DELIVERY) {
            double engineVolume = DroneAudioProfile.clamp01(drone.getEngineSoundVolume());
            double horizontalMotion = DroneAudioProfile.clamp01(motion / 1.15D);
            double verticalMotion = DroneAudioProfile.clamp01(Math.abs(movement.y) / 0.50D);
            double takeoffBoost = !drone.onGround() || movement.y > 0.01D ? 0.34D : 0.0D;
            double rpm = Math.max(takeoffBoost, Math.max(engineVolume, Math.max(horizontalMotion, verticalMotion)));
            return Math.max(0.20D, DroneAudioProfile.clamp01(rpm));
        }

        double engineVolume = DroneAudioProfile.clamp01(drone.getEngineSoundVolume());
        double horizontalMotion = DroneAudioProfile.clamp01(motion / 1.15D);
        double verticalMotion = DroneAudioProfile.clamp01(Math.abs(movement.y) / 0.70D);
        double rpm = Math.max(engineVolume, Math.max(horizontalMotion, verticalMotion));
        if (drone.onGround()) {
            rpm *= 0.45D;
        }
        return Math.max(0.16D, DroneAudioProfile.clamp01(rpm));
    }

    static double computeDopplerMultiplier(VehicleEntity drone, LocalPlayer player, Vec3 listenerPosition) {
        if (!AddonConfig.droneDopplerEnabled()) {
            return 1.0D;
        }

        Vec3 droneToListener = listenerPosition.subtract(drone.position());
        double distance = droneToListener.length();
        if (distance < 0.001D) {
            return 1.0D;
        }

        Vec3 directionToListener = droneToListener.scale(1.0D / distance);
        Vec3 listenerVelocity = player == null ? Vec3.ZERO : player.getDeltaMovement();
        double closingSpeed = drone.getDeltaMovement().subtract(listenerVelocity).dot(directionToListener);
        double shift = clamp(closingSpeed * 0.12D * AddonConfig.droneDopplerStrength(), -0.10D, 0.10D);
        return 1.0D + shift;
    }

    private static double priority(DroneAudioProfile profile,
                                   VehicleEntity drone,
                                   ListenerState listener,
                                   double distance,
                                   double rpm) {
        Vec3 toDrone = drone.position().subtract(listener.position());
        double facing = 0.0D;
        if (toDrone.lengthSqr() > 0.0001D) {
            facing = Math.max(0.0D, listener.look().normalize().dot(toDrone.normalize()));
        }
        double distanceScore = 1.0D - Math.min(1.0D, distance / profile.maxDistance());
        return distanceScore + facing * 0.25D + rpm * 0.20D;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ListenerState(Vec3 position, Vec3 look) {
        static ListenerState capture(Minecraft minecraft, LocalPlayer player) {
            Camera camera = minecraft.gameRenderer.getMainCamera();
            if (camera != null && camera.isInitialized()) {
                Vector3f lookVector = camera.getLookVector();
                return new ListenerState(
                        camera.getPosition(),
                        new Vec3(lookVector.x(), lookVector.y(), lookVector.z())
                );
            }
            return new ListenerState(player.getEyePosition(), player.getLookAngle());
        }
    }

    private record DroneCandidate(VehicleEntity drone,
                                  DroneAudioProfile profile,
                                  double distance,
                                  double rpm,
                                  double priority,
                                  boolean internalFpvListener) {
    }

    private static final class ManagedDroneAudio {
        private final VehicleEntity drone;
        private final DroneAudioProfile profile;
        private final EnumMap<DroneAudioProfile.Layer, DroneExternalSoundInstance> layers =
                new EnumMap<>(DroneAudioProfile.Layer.class);
        private ManagedDroneAudio(VehicleEntity drone, DroneAudioProfile profile) {
            this.drone = drone;
            this.profile = profile;
        }

        DroneAudioProfile profile() {
            return profile;
        }

        void update(Minecraft minecraft, LocalPlayer player, ListenerState listener, DroneCandidate candidate) {
            double doppler = computeDopplerMultiplier(this.drone, player, listener.position());
            double occlusion = updateOcclusion(minecraft.level, player, listener.position());

            for (DroneAudioProfile.Layer layer : DroneAudioProfile.Layer.values()) {
                float volume = this.profile.targetVolume(
                        layer,
                        candidate.distance(),
                        candidate.rpm(),
                        candidate.internalFpvListener(),
                        occlusion
                );
                float pitch = this.profile.targetPitch(
                        layer,
                        candidate.rpm(),
                        doppler,
                        candidate.internalFpvListener()
                );

                DroneExternalSoundInstance sound = this.layers.get(layer);
                if (volume > MIN_LAYER_START_VOLUME) {
                    if (sound == null || sound.isStopped()) {
                        sound = createSound(layer);
                        this.layers.put(layer, sound);
                        minecraft.getSoundManager().play(sound);
                    }
                    sound.updateTargets(volume, pitch);
                } else if (sound != null) {
                    sound.updateTargets(0.0F, pitch);
                    sound.requestStop();
                }
            }

            this.layers.entrySet().removeIf(entry -> entry.getValue().isStopped());
        }

        void requestStop() {
            for (DroneExternalSoundInstance sound : this.layers.values()) {
                sound.requestStop();
            }
        }

        boolean isStopped() {
            return this.layers.values().stream().allMatch(DroneExternalSoundInstance::isStopped);
        }

        private DroneExternalSoundInstance createSound(DroneAudioProfile.Layer layer) {
            return switch (this.profile) {
                case CUBED_FPV -> new CubedFpvExternalSoundInstance(this.drone, layer);
                case LUCAS -> new LucasExternalSoundInstance(this.drone, layer);
                case DELIVERY -> new DeliveryDroneExternalSoundInstance(this.drone, layer);
                case GLIDE_BOMB -> new GlideBombExternalSoundInstance(this.drone, layer);
            };
        }

        private double updateOcclusion(ClientLevel level, LocalPlayer player, Vec3 listenerPosition) {
            boolean internalListener = isInternalFpvListener(player, this.drone)
                    || SbwCompat.isUsingLinkedMonitorForDrone(player, this.drone);
            Vec3 source = this.drone.position().add(0.0D, this.drone.getBbHeight() * 0.5D, 0.0D);
            return DroneSoundOcclusion.getMultiplier(level, this.drone.getUUID(), listenerPosition, source, player, internalListener);
        }
    }

    private static final class OccludedSbwDroneEngineSound extends VehicleSoundInstance {
        private final VehicleEntity drone;
        private int fade;
        private boolean dying;
        private boolean stopped;

        private OccludedSbwDroneEngineSound(VehicleEntity drone) {
            super(resolveEngineSound(drone), Minecraft.getInstance(), drone);
            this.drone = drone;
        }

        @Override
        protected boolean canPlay(VehicleEntity vehicle) {
            return shouldPlayOriginalSbwDroneSound(Minecraft.getInstance().player, vehicle);
        }

        @Override
        protected float getPitch(VehicleEntity vehicle) {
            return 1.0F;
        }

        @Override
        protected float getVolume(VehicleEntity vehicle) {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            ClientLevel level = minecraft.level;
            if (level == null || player == null) {
                return 0.0F;
            }

            ListenerState listener = ListenerState.capture(minecraft, player);
            boolean monitorListener = SbwCompat.isUsingLinkedMonitorForDrone(player, vehicle);
            double distance = listener.position().distanceTo(vehicle.position());
            double distanceGain = getOriginalDistanceGain(distance);
            double occlusionGain = DroneSoundOcclusion.getMultiplier(
                    level,
                    vehicle.getUUID(),
                    listener.position(),
                    vehicle.position().add(0.0D, vehicle.getBbHeight() * 0.5D, 0.0D),
                    player,
                    monitorListener
            );
            return (float) Math.max(0.0D, vehicle.getEngineSoundVolume() * AddonConfig.droneEngineVolumeMultiplier() * distanceGain * occlusionGain);
        }

        @Override
        public void tick() {
            if (this.drone.isRemoved()) {
                stopSound();
                return;
            }

            this.dying = !canPlay(this.drone);
            if (this.dying) {
                if (this.fade > 0) {
                    this.fade--;
                } else {
                    stopSound();
                    return;
                }
            } else if (this.fade < ORIGINAL_SBW_DRONE_FADE_TICKS) {
                this.fade++;
            }

            this.volume = getVolume(this.drone) * (this.fade / (float) ORIGINAL_SBW_DRONE_FADE_TICKS);
            this.pitch = getPitch(this.drone);
            this.x = this.drone.getX();
            this.y = this.drone.getY();
            this.z = this.drone.getZ();
        }

        @Override
        public boolean isStopped() {
            return this.stopped || super.isStopped();
        }

        private void markActive() {
            this.dying = false;
        }

        private void requestStop() {
            stopSound();
        }

        private void stopSound() {
            this.stopped = true;
            ACTIVE_SBW_DRONE_SOUNDS.remove(this.drone.getUUID(), this);
            DroneSoundOcclusion.remove(this.drone.getUUID());
            stop();
        }

        private static net.minecraft.sounds.SoundEvent resolveEngineSound(VehicleEntity drone) {
            net.minecraft.sounds.SoundEvent engineSound = drone.getEngineSound();
            if (engineSound == null) {
                throw new IllegalStateException("Superb Warfare drone engine sound event is missing.");
            }
            return engineSound;
        }
    }

    private static double getOriginalDistanceGain(double distance) {
        if (distance <= 6.0D) {
            return 1.0D;
        }
        if (distance >= ORIGINAL_SBW_DRONE_SOUND_DISTANCE) {
            return 0.0D;
        }
        double normalized = (distance - 6.0D) / (ORIGINAL_SBW_DRONE_SOUND_DISTANCE - 6.0D);
        return Math.pow(1.0D - normalized, 1.65D);
    }
}
