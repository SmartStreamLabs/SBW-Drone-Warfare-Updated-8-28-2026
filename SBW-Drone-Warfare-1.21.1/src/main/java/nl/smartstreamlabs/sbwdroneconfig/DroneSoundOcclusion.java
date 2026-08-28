package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Lightweight, client-only terrain muffling shared by all drone engine loops. */
final class DroneSoundOcclusion {
    private static final int UPDATE_INTERVAL_TICKS = 6;
    private static final int MAX_PATH_SAMPLES = 16;
    private static final double MINIMUM_OCCLUSION_GAIN = 0.01D;
    private static final Map<UUID, CachedOcclusion> CACHE = new HashMap<>();

    private DroneSoundOcclusion() {
    }

    static double getMultiplier(ClientLevel level, UUID droneId, Vec3 listener, Vec3 source, Entity clippingEntity, boolean bypass) {
        if (bypass || !AddonConfig.droneAudioOcclusion() || level == null || droneId == null || listener == null || source == null) {
            return 1.0D;
        }

        CachedOcclusion cached = CACHE.computeIfAbsent(droneId, ignored -> new CachedOcclusion());
        long gameTime = level.getGameTime();
        if (cached.lastUpdateTick == Long.MIN_VALUE || gameTime - cached.lastUpdateTick >= UPDATE_INTERVAL_TICKS) {
            cached.targetGain = calculateOcclusion(level, listener, source, clippingEntity);
            cached.lastUpdateTick = gameTime;
        }
        cached.gain += (cached.targetGain - cached.gain) * 0.35D;
        return cached.gain;
    }

    static void remove(UUID droneId) {
        if (droneId != null) {
            CACHE.remove(droneId);
        }
    }

    static void clear() {
        CACHE.clear();
    }

    static double multiplierForSamples(int solidSamples, int foliageSamples) {
        int solids = Math.max(0, solidSamples);
        int foliage = Math.max(0, foliageSamples);
        return Math.max(MINIMUM_OCCLUSION_GAIN, Math.exp(-0.42D * solids - 0.12D * foliage));
    }

    private static double calculateOcclusion(ClientLevel level, Vec3 listener, Vec3 source, Entity clippingEntity) {
        Vec3 delta = source.subtract(listener);
        double distance = delta.length();
        if (distance < 2.0D) {
            return 1.0D;
        }

        BlockHitResult hit = level.clip(new ClipContext(listener, source, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, clippingEntity));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return 1.0D;
        }

        int solidSamples = 0;
        int foliageSamples = 0;
        int samples = Math.min(MAX_PATH_SAMPLES, Math.max(4, (int) Math.ceil(distance / 8.0D)));
        for (int sampleIndex = 1; sampleIndex < samples; sampleIndex++) {
            BlockPos position = BlockPos.containing(listener.add(delta.scale(sampleIndex / (double) samples)));
            if (!level.hasChunkAt(position)) {
                continue;
            }

            BlockState state = level.getBlockState(position);
            if (state.getCollisionShape(level, position).isEmpty()) {
                continue;
            }
            if (state.getBlock() instanceof LeavesBlock) {
                foliageSamples++;
            } else {
                solidSamples++;
            }
        }

        if (solidSamples == 0 && foliageSamples == 0) {
            BlockState hitState = level.getBlockState(hit.getBlockPos());
            if (hitState.getBlock() instanceof LeavesBlock) {
                foliageSamples = 1;
            } else {
                solidSamples = 1;
            }
        }
        return multiplierForSamples(solidSamples, foliageSamples);
    }

    private static final class CachedOcclusion {
        private long lastUpdateTick = Long.MIN_VALUE;
        private double gain = 1.0D;
        private double targetGain = 1.0D;
    }
}
