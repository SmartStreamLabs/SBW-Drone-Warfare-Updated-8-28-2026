package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.tools.ParticleTool;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class DroneCrashExplosionSystem {
    private static final String TAG_CRASH_EXPLOSION_HANDLED = "sbwdroneconfigCrashExplosionHandled";
    private static final float CRASH_EXPLOSION_POWER = 2.8F;
    private static final float FPV_CRASH_EXPLOSION_POWER = 5.8F;
    private static final float GLIDE_BOMB_CRASH_EXPLOSION_POWER = FPV_CRASH_EXPLOSION_POWER * 10.0F;
    private static final float LUCAS_CRASH_EXPLOSION_POWER = 8.5F;
    private static final float GERANIUM_CRASH_EXPLOSION_POWER = LUCAS_CRASH_EXPLOSION_POWER * 2.0F;
    private static final float CRASH_SOUND_VOLUME = 1.8F;
    private static final float FPV_CRASH_SOUND_VOLUME = 3.8F;
    private static final float LUCAS_CRASH_SOUND_VOLUME = 4.2F;
    private static final float CRASH_SOUND_PITCH = 0.85F;
    private static final float FPV_CRASH_SOUND_PITCH = 0.72F;
    private static final float LUCAS_CRASH_SOUND_PITCH = 0.65F;
    private static final float DISTANT_EXPLOSION_SOUND_VOLUME = 3.0F;
    private static final float FPV_DISTANT_EXPLOSION_SOUND_VOLUME = 4.5F;
    private static final float LUCAS_DISTANT_EXPLOSION_SOUND_VOLUME = 4.8F;
    private static final float DISTANT_EXPLOSION_SOUND_PITCH = 1.0F;
    private static final float EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME = 8.0F;
    private static final float FPV_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME = 9.0F;
    private static final float LUCAS_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME = 10.0F;
    private static final float EXTREME_DISTANT_EXPLOSION_SOUND_PITCH = 0.92F;

    private DroneCrashExplosionSystem() {
    }

    /**
     * Restores the old addon crash explosion behavior for non-kamikaze drones.
     * This keeps the SBW destroy flow intact while layering heavier particles/sounds
     * and optional real explosion damage/block destruction on top.
     */
    public static void handleDroneDestroy(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }
        if (drone.getPersistentData().getBoolean(TAG_CRASH_EXPLOSION_HANDLED)) {
            return;
        }
        drone.getPersistentData().putBoolean(TAG_CRASH_EXPLOSION_HANDLED, true);

        boolean geranium = drone instanceof Geranium2Entity;
        boolean glideBomb = drone instanceof GlideBombEntity;
        boolean lucasDrone = drone instanceof LucasDroneEntity;
        boolean fpvDrone = drone instanceof CubedFpvDroneEntity;
        float explosionPower = geranium
                ? GERANIUM_CRASH_EXPLOSION_POWER
                : glideBomb
                ? GLIDE_BOMB_CRASH_EXPLOSION_POWER
                : lucasDrone
                ? LUCAS_CRASH_EXPLOSION_POWER
                : fpvDrone ? FPV_CRASH_EXPLOSION_POWER : CRASH_EXPLOSION_POWER;

        disconnectController(level, drone);
        FiberOpticLinkSystem.handleDroneCrashRecovery(level, drone);
        if (geranium) {
            spawnCrashEffects(level, drone.position(), fpvDrone, lucasDrone, glideBomb, true);
        } else {
            spawnCrashEffects(level, drone.position(), fpvDrone, lucasDrone, glideBomb);
        }

        if (AddonConfig.enableDroneCrashExplosionDamage()) {
            level.explode(null, drone.getX(), drone.getY(), drone.getZ(), explosionPower, Level.ExplosionInteraction.TNT);
        }
    }

    private static void disconnectController(ServerLevel level, Entity drone) {
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer controller = level.getServer().getPlayerList().getPlayer(controllerUuid);
            if (controller != null) {
                PlayerDroneAnchorManager.release(controller, true);
            }
        }

        SbwCompat.disconnectControllersForDrone(level, drone);
        SbwCompat.resetDroneInput(drone);
    }

    private static void spawnCrashEffects(ServerLevel level, Vec3 pos, boolean fpvDrone, boolean lucasDrone, boolean glideBomb) {
        spawnCrashEffects(level, pos, fpvDrone, lucasDrone, glideBomb, false);
    }

    private static void spawnCrashEffects(ServerLevel level, Vec3 pos, boolean fpvDrone, boolean lucasDrone, boolean glideBomb, boolean geranium) {
        ParticleTool.spawnHugeExplosionParticles(level, pos);
        if (fpvDrone || lucasDrone) {
            ParticleTool.spawnHugeExplosionParticles(level, pos);
        }
        if (lucasDrone || glideBomb || geranium) {
            ParticleTool.spawnHugeExplosionParticles(level, pos);
        }

        float soundVolume = geranium ? LUCAS_CRASH_SOUND_VOLUME * 1.35F : glideBomb ? FPV_CRASH_SOUND_VOLUME * 2.0F : lucasDrone ? LUCAS_CRASH_SOUND_VOLUME : fpvDrone ? FPV_CRASH_SOUND_VOLUME : CRASH_SOUND_VOLUME;
        float soundPitch = geranium ? LUCAS_CRASH_SOUND_PITCH * 0.95F : glideBomb ? FPV_CRASH_SOUND_PITCH * 0.9F : lucasDrone ? LUCAS_CRASH_SOUND_PITCH : fpvDrone ? FPV_CRASH_SOUND_PITCH : CRASH_SOUND_PITCH;
        level.playSound(null, pos.x, pos.y, pos.z, AddonSounds.DRONE_CLOSE_EXPLOSION.get(), SoundSource.BLOCKS, soundVolume, soundPitch);
        if (geranium) {
            playDistantExplosionSound(level, pos, fpvDrone, lucasDrone, glideBomb, true);
        } else {
            playDistantExplosionSound(level, pos, fpvDrone, lucasDrone, glideBomb);
        }

        int explosionEmitterCount = geranium ? 18 : glideBomb ? 24 : lucasDrone ? 14 : fpvDrone ? 10 : 5;
        int flashCount = geranium ? 120 : glideBomb ? 150 : lucasDrone ? 96 : fpvDrone ? 76 : 40;
        int lavaCount = geranium ? 72 : glideBomb ? 90 : lucasDrone ? 54 : fpvDrone ? 36 : 18;
        int flameCount = geranium ? 112 : glideBomb ? 140 : lucasDrone ? 90 : 0;
        int smokeCount = geranium ? 82 : glideBomb ? 100 : lucasDrone ? 65 : 0;
        double horizontalSpread = geranium ? 1.75D : glideBomb ? 2.2D : lucasDrone ? 1.45D : fpvDrone ? 0.9D : 0.35D;
        double verticalSpread = geranium ? 0.72D : glideBomb ? 0.85D : lucasDrone ? 0.65D : fpvDrone ? 0.45D : 0.25D;

        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 0.35D, pos.z, explosionEmitterCount, horizontalSpread, verticalSpread, horizontalSpread, 0.0D);
        level.sendParticles(ParticleTypes.FLASH, pos.x, pos.y + 0.35D, pos.z, flashCount, horizontalSpread * 1.4D, verticalSpread, horizontalSpread * 1.4D, 1.0D);
        level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.2D, pos.z, lavaCount, horizontalSpread, 0.3D, horizontalSpread, 0.02D);

        if (lucasDrone || glideBomb || geranium) {
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.25D, pos.z, flameCount, 1.85D, 0.85D, 1.85D, 0.08D);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y + 0.45D, pos.z, smokeCount, 1.35D, 0.65D, 1.35D, 0.025D);
        }
    }

    private static void playDistantExplosionSound(ServerLevel level, Vec3 pos, boolean fpvDrone, boolean lucasDrone, boolean glideBomb) {
        playDistantExplosionSound(level, pos, fpvDrone, lucasDrone, glideBomb, false);
    }

    private static void playDistantExplosionSound(ServerLevel level, Vec3 pos, boolean fpvDrone, boolean lucasDrone, boolean glideBomb, boolean geranium) {
        float volume = geranium ? LUCAS_DISTANT_EXPLOSION_SOUND_VOLUME * 1.35F : glideBomb ? FPV_DISTANT_EXPLOSION_SOUND_VOLUME * 2.0F : lucasDrone ? LUCAS_DISTANT_EXPLOSION_SOUND_VOLUME : fpvDrone ? FPV_DISTANT_EXPLOSION_SOUND_VOLUME : DISTANT_EXPLOSION_SOUND_VOLUME;
        level.playSound(null, pos.x, pos.y, pos.z, AddonSounds.DRONE_DISTANT_EXPLOSION.get(), SoundSource.BLOCKS, volume, DISTANT_EXPLOSION_SOUND_PITCH);
        playExtremeDistantExplosionSound(level, pos, fpvDrone, lucasDrone, glideBomb, geranium);
    }

    private static void playExtremeDistantExplosionSound(ServerLevel level, Vec3 pos, boolean fpvDrone, boolean lucasDrone, boolean glideBomb, boolean geranium) {
        float volume = geranium ? LUCAS_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME * 1.35F : glideBomb ? FPV_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME * 2.0F : lucasDrone ? LUCAS_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME : fpvDrone ? FPV_EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME : EXTREME_DISTANT_EXPLOSION_SOUND_VOLUME;
        level.playSound(null, pos.x, pos.y, pos.z, AddonSounds.DRONE_5000_BLOCK_EXPLOSION.get(), SoundSource.BLOCKS, volume, EXTREME_DISTANT_EXPLOSION_SOUND_PITCH);
    }
}
