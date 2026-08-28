package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LucasDroneSoundManager {
    private static final Map<Integer, LucasDroneEngineSoundInstance> ACTIVE_SOUNDS = new HashMap<>();
    private static final ResourceLocation PRIMARY_SOUND_RESOURCE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "sounds/lucas_drone_engine.ogg");
    private static final ResourceLocation FAR_SOUND_RESOURCE = new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "sounds/lucas_drone_engine.ogg");
    private static boolean resourceAvailabilityLogged;

    private LucasDroneSoundManager() {
    }

    public static void init(net.minecraftforge.eventbus.api.IEventBus modBus) {
        modBus.addListener(LucasDroneSoundManager::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(LucasDroneSoundManager.class);
            logSoundResourceAvailability(minecraft);
        });
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null || minecraft.getSoundManager() == null) {
            stopAll();
            return;
        }

        if (!resourceAvailabilityLogged) {
            logSoundResourceAvailability(minecraft);
        }

        Set<Integer> audibleDroneIds = new HashSet<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof LucasDroneEntity drone)) {
                continue;
            }

            if (!LucasDroneEngineSoundInstance.shouldPlayFor(player, drone)) {
                continue;
            }

            audibleDroneIds.add(drone.getId());
            LucasDroneEngineSoundInstance sound = ACTIVE_SOUNDS.get(drone.getId());
            if (sound != null && !sound.isStopped()) {
                continue;
            }

            sound = new LucasDroneEngineSoundInstance(drone);
            sound.tick();
            ACTIVE_SOUNDS.put(drone.getId(), sound);
            minecraft.getSoundManager().play(sound);
            logSoundStart(sound);
        }

        ACTIVE_SOUNDS.entrySet().removeIf(entry -> {
            LucasDroneEngineSoundInstance sound = entry.getValue();
            if (sound == null || sound.isStopped() || !audibleDroneIds.contains(entry.getKey())) {
                if (sound != null) {
                    sound.requestStop();
                }
                return true;
            }
            return false;
        });
    }

    private static void stopAll() {
        ACTIVE_SOUNDS.values().forEach(LucasDroneEngineSoundInstance::requestStop);
        ACTIVE_SOUNDS.clear();
    }

    private static void logSoundStart(LucasDroneEngineSoundInstance sound) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        SbwDroneRangeConfig.LOGGER.info(
                "LUCAS sound start event={} entityId={} dronePos={} distance={} volume={} pitch={} isClientSide={}",
                sound.getSoundEventId(),
                sound.getDroneEntityId(),
                formatVec3(sound.getDronePosition()),
                formatDouble(sound.getDistanceTo(player)),
                formatDouble(sound.getCurrentVolume()),
                formatDouble(sound.getCurrentPitch()),
                sound.getDronePosition() != null && minecraft.level != null && minecraft.level.isClientSide
        );
    }

    private static void logSoundResourceAvailability(Minecraft minecraft) {
        if (minecraft == null || minecraft.getResourceManager() == null) {
            return;
        }

        Optional<Resource> primarySound = minecraft.getResourceManager().getResource(PRIMARY_SOUND_RESOURCE);
        Optional<Resource> farSound = minecraft.getResourceManager().getResource(FAR_SOUND_RESOURCE);
        SbwDroneRangeConfig.LOGGER.info(
                "LUCAS sound resource check primaryEvent={} farEvent={} primaryResource={} primaryExists={} farResource={} farExists={}",
                SbwDroneRangeConfig.MOD_ID + ":lucas_drone_engine",
                SbwDroneRangeConfig.MOD_ID + ":lucas_drone_engine_far",
                PRIMARY_SOUND_RESOURCE,
                primarySound.isPresent(),
                FAR_SOUND_RESOURCE,
                farSound.isPresent()
        );
        resourceAvailabilityLogged = true;
    }

    private static String formatVec3(net.minecraft.world.phys.Vec3 vec3) {
        if (vec3 == null) {
            return "[0.00, 0.00, 0.00]";
        }
        return "[" + formatDouble(vec3.x) + ", " + formatDouble(vec3.y) + ", " + formatDouble(vec3.z) + "]";
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
