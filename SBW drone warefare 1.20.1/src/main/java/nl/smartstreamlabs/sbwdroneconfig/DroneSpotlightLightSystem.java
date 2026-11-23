package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Safe fallback real-light implementation for the drone spotlight.
 * This reuses vanilla light blocks as temporary invisible light points that
 * are moved every few ticks while the spotlight is active.
 */
public final class DroneSpotlightLightSystem {
    private static final String TAG_LAST_LIGHT_UPDATE = "sbwdroneconfigSpotlightLastLightUpdate";
    private static final Map<UUID, List<BlockPos>> ACTIVE_LIGHTS = new HashMap<>();

    private DroneSpotlightLightSystem() {
    }

    public static void tick(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone)) {
            return;
        }

        if (!AddonConfig.enableSpotlightModule()
                || !AddonConfig.spotlightRealLightEnabled()
                || !DroneSpotlightSystem.isSpotlightActive(drone)
                || !drone.isAlive()
                || drone.isRemoved()) {
            clear(serverLevel, drone.getUUID());
            return;
        }

        long gameTime = serverLevel.getGameTime();
        int interval = Math.max(1, AddonConfig.spotlightLightUpdateIntervalTicks());
        if ((gameTime - drone.getPersistentData().getLong(TAG_LAST_LIGHT_UPDATE)) < interval) {
            return;
        }
        drone.getPersistentData().putLong(TAG_LAST_LIGHT_UPDATE, gameTime);

        updateLights(serverLevel, drone);
    }

    public static void clearForDrone(Entity drone) {
        if (drone != null && drone.level() instanceof ServerLevel serverLevel) {
            clear(serverLevel, drone.getUUID());
        }
    }

    private static void updateLights(ServerLevel level, Entity drone) {
        List<BlockPos> newPositions = computeLightPositions(level, drone);
        UUID droneId = drone.getUUID();
        List<BlockPos> oldPositions = ACTIVE_LIGHTS.getOrDefault(droneId, List.of());

        for (BlockPos oldPos : oldPositions) {
            if (!newPositions.contains(oldPos)) {
                removeLight(level, oldPos);
            }
        }

        for (int i = 0; i < newPositions.size(); i++) {
            // Strongest light stays close to the drone; farther points dim down
            // so the spotlight feels more like a cone than a flat full-bright line.
            placeLight(level, newPositions.get(i), getLightLevelForIndex(i));
        }

        if (newPositions.isEmpty()) {
            ACTIVE_LIGHTS.remove(droneId);
        } else {
            ACTIVE_LIGHTS.put(droneId, newPositions);
        }
    }

    private static List<BlockPos> computeLightPositions(ServerLevel level, Entity drone) {
        int configuredRange = Math.min(AddonConfig.spotlightRange(), AddonConfig.spotlightLightRange());
        Vec3 direction = resolveSpotlightDirection(drone);
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = Vec3.directionFromRotation(drone.getXRot(), drone.getYRot());
        }

        Vec3 origin = drone.position().add(0.0D, 0.35D, 0.0D).add(direction.scale(0.9D));
        Vec3 end = origin.add(direction.scale(configuredRange));
        List<BlockPos> result = new ArrayList<>();
        double maxDistance = configuredRange;

        BlockHitResult hitResult = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, drone));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            maxDistance = Math.max(3.0D, origin.distanceTo(hitResult.getLocation()));
        }

        for (double distance = 1.5D; distance <= maxDistance; distance += 1.5D) {
            Vec3 sample = origin.add(direction.scale(distance));
            BlockPos pos = findPlaceableLightPos(level, BlockPos.containing(sample));
            if (pos != null && !result.contains(pos)) {
                result.add(pos);
            }
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            addImpactLights(level, hitResult, result);
        }

        if (result.size() > 40) {
            return new ArrayList<>(result.subList(0, 40));
        }

        return result;
    }

    private static Vec3 resolveSpotlightDirection(Entity drone) {
        ServerPlayer controller = getController(drone);
        if (controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone)) {
            Vec3 controllerDirection = controller.getLookAngle();
            if (controllerDirection.lengthSqr() >= 1.0E-4D) {
                return controllerDirection.normalize();
            }
        }

        return drone.getLookAngle().normalize();
    }

    private static ServerPlayer getController(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        UUID owner = SbwCompat.getDroneControllerUuid(drone);
        return owner == null ? null : serverLevel.getServer().getPlayerList().getPlayer(owner);
    }

    private static void addImpactLights(ServerLevel level, BlockHitResult hitResult, List<BlockPos> result) {
        Direction face = hitResult.getDirection();
        BlockPos base = hitResult.getBlockPos().relative(face);
        int[][] offsets = new int[][]{
                {0, 0, 0},
                {1, 0, 0},
                {-1, 0, 0},
                {0, 0, 1},
                {0, 0, -1},
                {1, 0, 1},
                {1, 0, -1},
                {-1, 0, 1},
                {-1, 0, -1},
                {2, 0, 0},
                {-2, 0, 0},
                {0, 0, 2},
                {0, 0, -2},
                {0, 1, 0},
                {1, 1, 0},
                {-1, 1, 0},
                {0, 1, 1},
                {0, 1, -1},
                {1, 1, 1},
                {1, 1, -1},
                {-1, 1, 1},
                {-1, 1, -1}
        };

        for (int[] offset : offsets) {
            BlockPos target = base.offset(offset[0], offset[1], offset[2]);
            BlockPos pos = findPlaceableLightPos(level, target);
            if (pos != null && !result.contains(pos)) {
                result.add(pos);
            }
        }
    }

    private static BlockPos findPlaceableLightPos(ServerLevel level, BlockPos target) {
        BlockPos.MutableBlockPos mutable = target.mutable();

        for (int step = 0; step < 3; step++) {
            if (canPlaceLight(level, mutable)) {
                return mutable.immutable();
            }
            mutable.move(0, -1, 0);
        }

        mutable.set(target);
        for (int step = 0; step < 2; step++) {
            mutable.move(0, 1, 0);
            if (canPlaceLight(level, mutable)) {
                return mutable.immutable();
            }
        }

        return null;
    }

    private static boolean canPlaceLight(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.LIGHT)
                || (state.getFluidState().is(FluidTags.WATER) && state.getCollisionShape(level, pos).isEmpty());
    }

    private static void placeLight(ServerLevel level, BlockPos pos, int lightLevel) {
        BlockState oldState = level.getBlockState(pos);
        boolean waterlogged = oldState.getFluidState().is(FluidTags.WATER);
        BlockState lightState = Blocks.LIGHT.defaultBlockState()
                .setValue(LightBlock.LEVEL, Mth.clamp(lightLevel, 1, 15))
                .setValue(LightBlock.WATERLOGGED, waterlogged);
        if (!oldState.equals(lightState)) {
            level.setBlock(pos, lightState, 3);
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }

    private static void removeLight(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.removeBlock(pos, false);
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }

    private static void clear(ServerLevel level, UUID droneId) {
        List<BlockPos> positions = ACTIVE_LIGHTS.remove(droneId);
        if (positions == null) {
            return;
        }
        for (BlockPos pos : positions) {
            removeLight(level, pos);
        }
    }

    private static int getLightLevelForIndex(int index) {
        int base = AddonConfig.spotlightLightLevel();
        return Math.max(10, base - (index / 3));
    }
}
