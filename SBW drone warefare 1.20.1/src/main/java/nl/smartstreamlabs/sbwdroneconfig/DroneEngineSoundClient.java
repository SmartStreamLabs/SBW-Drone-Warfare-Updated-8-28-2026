package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.client.sound.VehicleSoundInstance;
import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class DroneEngineSoundClient {
    private static final double SOUND_FADE_START_DISTANCE = 6.0D;
    private static final double DEFAULT_DISTANCE_FALLOFF_POWER = 2.85D;
    private static final double SOUND_MAX_AUDIBLE_DISTANCE = 48.0D;
    private static final double LUCAS_SOUND_MAX_AUDIBLE_DISTANCE = 160.0D;
    private static final double LUCAS_DISTANCE_FALLOFF_POWER = 1.10D;
    private static final double HIGH_ALTITUDE_START_Y = 80.0D;
    private static final double HIGH_ALTITUDE_MAX_Y = 180.0D;
    private static final float HIGH_ALTITUDE_EXTRA_MULTIPLIER = 1.25F;
    private static final float LUCAS_ENGINE_VOLUME_BOOST = 20.0F;
    private static final float LUCAS_AIRBORNE_VOLUME_BOOST = 2.4F;
    private static final float LUCAS_GROUND_LISTENER_VOLUME_BOOST = 6.0F;
    private static final float LUCAS_HIGH_ALTITUDE_EXTRA_MULTIPLIER = 1.9F;
    private static final Map<UUID, DroneVehicleEngineSound> ACTIVE_SOUNDS = new HashMap<>();

    private DroneEngineSoundClient() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(DroneEngineSoundClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> VehicleEntity.playEngineSound = DroneEngineSoundClient::playEngineSound);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(DroneEngineSoundClient.class);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        VehicleEntity.playEngineSound = DroneEngineSoundClient::playEngineSound;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            ACTIVE_SOUNDS.clear();
            return;
        }

        ACTIVE_SOUNDS.entrySet().removeIf(entry -> entry.getValue().isStopped());
        ensureLinkedDroneSound(minecraft.player);
        ensureNearbyAudibleDroneSounds(minecraft.player, minecraft.level);
    }

    private static void playEngineSound(VehicleEntity vehicle) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.getSoundManager() == null) {
            return;
        }

        if (!SbwCompat.isDrone(vehicle)) {
            minecraft.getSoundManager().play(new VehicleSoundInstance.EngineSound(vehicle));
            return;
        }

        if (vehicle instanceof LucasDroneEntity) {
            // LucasDroneSoundManager handles the dedicated fixed-wing loop.
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null || !shouldPlayDroneSound(player, vehicle)) {
            return;
        }

        DroneVehicleEngineSound existing = ACTIVE_SOUNDS.get(vehicle.getUUID());
        if (existing != null && !existing.isStopped()) {
            existing.markActive();
            return;
        }

        DroneVehicleEngineSound sound = new DroneVehicleEngineSound(vehicle);
        ACTIVE_SOUNDS.put(vehicle.getUUID(), sound);
        minecraft.getSoundManager().play(sound);
    }

    private static void ensureLinkedDroneSound(LocalPlayer player) {
        if (player == null) {
            return;
        }

        ItemStack activeMonitor = getActiveMonitor(player);
        if (activeMonitor.isEmpty()) {
            return;
        }

        UUID linkedDroneUuid = SbwCompat.getLinkedDroneUuid(activeMonitor);
        if (linkedDroneUuid == null || !(player.level() instanceof ClientLevel clientLevel)) {
            return;
        }

        Entity linkedEntity = null;
        for (Entity entity : clientLevel.entitiesForRendering()) {
            if (linkedDroneUuid.equals(entity.getUUID())) {
                linkedEntity = entity;
                break;
            }
        }

        if (linkedEntity instanceof VehicleEntity vehicle && SbwCompat.isDrone(vehicle)) {
            playEngineSound(vehicle);
        }
    }

    private static void ensureNearbyAudibleDroneSounds(LocalPlayer player, ClientLevel level) {
        if (player == null || level == null) {
            return;
        }

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof VehicleEntity vehicle) || !SbwCompat.isDrone(vehicle)) {
                continue;
            }

            if (!shouldPlayDroneSound(player, vehicle)) {
                continue;
            }

            playEngineSound(vehicle);
        }
    }

    private static ItemStack getActiveMonitor(LocalPlayer player) {
        ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);
        return SbwCompat.isUsingLinkedMonitor(activeMonitor) ? activeMonitor : ItemStack.EMPTY;
    }

    private static double resolveListenerDistance(VehicleEntity drone) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return 0.0D;
        }

        Vec3 dronePos = drone.position();
        double distance = Double.MAX_VALUE;

        if (minecraft.gameRenderer != null && minecraft.gameRenderer.getMainCamera() != null) {
            distance = minecraft.gameRenderer.getMainCamera().getPosition().distanceTo(dronePos);
        }

        LocalPlayer player = minecraft.player;
        if (player != null) {
            distance = Math.min(distance, player.position().distanceTo(dronePos));
        }

        return distance == Double.MAX_VALUE ? 0.0D : distance;
    }

    private static float getDistanceAttenuation(double distance, double maxAudibleDistance, double falloffPower) {
        if (distance <= SOUND_FADE_START_DISTANCE) {
            return 1.0F;
        }

        if (distance >= maxAudibleDistance) {
            return 0.0F;
        }

        double fadeWindow = maxAudibleDistance - SOUND_FADE_START_DISTANCE;
        double normalized = (distance - SOUND_FADE_START_DISTANCE) / fadeWindow;
        return (float) Math.max(0.0D, Math.pow(1.0D - normalized, falloffPower));
    }

    private static float getEffectiveDroneVolume(VehicleEntity drone) {
        if(drone instanceof Fl01Entity)return (float)(.75*AddonConfig.droneEngineVolumeMultiplier()*AddonConfig.droneAirborneVolumeMultiplier());
        float baseVolume = (float) (drone.getEngineSoundVolume() * AddonConfig.droneEngineVolumeMultiplier());
        boolean lucasDrone = usesLucasEngineAudio(drone);
        if (lucasDrone) {
            baseVolume *= LUCAS_ENGINE_VOLUME_BOOST;
            baseVolume *= (float) AddonConfig.lucasEngineVolumeMultiplier();
        }
        if (drone.onGround()) {
            return baseVolume;
        }

        float volume = (float) (baseVolume * AddonConfig.droneAirborneVolumeMultiplier());
        if (lucasDrone) {
            volume *= LUCAS_AIRBORNE_VOLUME_BOOST;
        }
        double altitude = drone.getY();
        if (altitude > HIGH_ALTITUDE_START_Y) {
            double normalized = Math.min(1.0D, (altitude - HIGH_ALTITUDE_START_Y) / (HIGH_ALTITUDE_MAX_Y - HIGH_ALTITUDE_START_Y));
            float maxAltitudeMultiplier = lucasDrone ? LUCAS_HIGH_ALTITUDE_EXTRA_MULTIPLIER : HIGH_ALTITUDE_EXTRA_MULTIPLIER;
            float altitudeMultiplier = 1.0F + (float) normalized * (maxAltitudeMultiplier - 1.0F);
            volume *= altitudeMultiplier;
        }
        if (lucasDrone && hasGroundListenerBoost(drone)) {
            // Keep the fixed-wing drone clearly audible for players listening from the ground.
            volume *= LUCAS_GROUND_LISTENER_VOLUME_BOOST;
        }

        return volume;
    }

    private static boolean hasGroundListenerBoost(VehicleEntity drone) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return false;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return false;
        }

        if (SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
            return false;
        }

        return player.onGround();
    }

    private static boolean shouldPlayDroneSound(LocalPlayer player, VehicleEntity vehicle) {
        if (player == null || vehicle == null) {
            return false;
        }

        if (!vehicle.isAlive() || vehicle.isRemoved() || !vehicle.engineRunning()) {
            return false;
        }

        if (SbwCompat.isUsingLinkedMonitorForDrone(player, vehicle)) {
            return true;
        }

        if (vehicle.onGround()) {
            return false;
        }

        double maxAudibleDistance = usesLucasEngineAudio(vehicle) ? LUCAS_SOUND_MAX_AUDIBLE_DISTANCE : SOUND_MAX_AUDIBLE_DISTANCE;
        return resolveListenerDistance(vehicle) < maxAudibleDistance;
    }

    private static boolean usesLucasEngineAudio(VehicleEntity drone) {
        return drone instanceof LucasDroneEntity || drone instanceof Geranium2Entity || drone instanceof Fl01Entity;
    }

    private static final class DroneVehicleEngineSound extends VehicleSoundInstance {
        private final VehicleEntity drone;
        private int fade;
        private boolean dying;
        private double lastDistance;
        private boolean stopped;

        private DroneVehicleEngineSound(VehicleEntity drone) {
            super(resolveSoundEvent(drone), Minecraft.getInstance(), drone);
            this.drone = drone;
        }

        @Override
        protected boolean canPlay(VehicleEntity vehicle) {
            LocalPlayer player = Minecraft.getInstance().player;
            return shouldPlayDroneSound(player, vehicle);
        }

        @Override
        protected float getPitch(VehicleEntity vehicle) {
            return 1.0F;
        }

        @Override
        protected float getVolume(VehicleEntity vehicle) {
            double maxAudibleDistance = usesLucasEngineAudio(vehicle) ? LUCAS_SOUND_MAX_AUDIBLE_DISTANCE : SOUND_MAX_AUDIBLE_DISTANCE;
            double distanceFalloffPower = usesLucasEngineAudio(vehicle) ? LUCAS_DISTANCE_FALLOFF_POWER : DEFAULT_DISTANCE_FALLOFF_POWER;
            return Math.max(0.0F, getEffectiveDroneVolume(vehicle) * getDistanceAttenuation(resolveListenerDistance(vehicle), maxAudibleDistance, distanceFalloffPower));
        }

        @Override
        public void tick() {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer player = minecraft.player;
            if (player == null || this.drone.isRemoved()) {
                this.stopSound();
                return;
            }

            this.dying = !this.canPlay(this.drone);

            if (this.dying) {
                if (this.fade > 0) {
                    this.fade--;
                } else {
                    this.stopSound();
                    return;
                }
            } else if (this.fade < 3) {
                this.fade++;
            }

            this.volume = this.getVolume(this.drone) * (this.fade / 3.0F);
            this.x = this.drone.getX();
            this.y = this.drone.getY();
            this.z = this.drone.getZ();
            this.pitch = this.getPitch(this.drone);

            if (player.getVehicle() != this.drone) {
                double distance = this.drone.position().subtract(player.position()).length();
                this.pitch += (float) (0.16D * Math.atan(this.lastDistance - distance));
                this.lastDistance = distance;
            } else {
                this.lastDistance = 0.0D;
            }

            if (SbwCompat.isUsingLinkedMonitorForDrone(player, this.drone)) {
                this.pitch = 1.0F;
            }
        }

        @Override
        public boolean isStopped() {
            return this.stopped || super.isStopped();
        }

        private void markActive() {
            if (!this.stopped) {
                this.dying = false;
            }
        }

        private void stopSound() {
            this.stopped = true;
            ACTIVE_SOUNDS.remove(this.drone.getUUID(), this);
            this.stop();
        }

        private static SoundEvent resolveSoundEvent(VehicleEntity drone) {
            if(drone instanceof Fl01Entity)return net.minecraft.core.registries.BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.tryParse("superbwarfare:rocket_engine"));
            if (drone instanceof Geranium2Entity || drone instanceof Fl01Entity) {
                return AddonSounds.LUCAS_DRONE_ENGINE.get();
            }
            if (drone instanceof GlideBombEntity) {
                return AddonSounds.GLIDE_BOMB_ENGINE.get();
            }
            if (drone instanceof HeavyBomberDroneEntity) {
                return AddonSounds.GLIDE_BOMB_ENGINE.get();
            }
            if (drone instanceof DeliveryDroneEntity) {
                return AddonSounds.DELIVERY_DRONE_ENGINE.get();
            }
            if (drone instanceof CubedFpvDroneEntity) {
                return AddonSounds.CUBED_FPV_DRONE_ENGINE.get();
            }
            if (drone instanceof LucasDroneEntity) {
                return AddonSounds.LUCAS_DRONE_ENGINE.get();
            }
            SoundEvent soundEvent = drone.getEngineSound();
            if (soundEvent == null) {
                throw new IllegalStateException("Drone engine sound event is missing.");
            }
            return soundEvent;
        }
    }
}
