package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Mirrors the spotlight light points on the controlling client so the FPV user
 * can always see the temporary light blocks, even if the server-side light
 * updates are delayed or culled during rapid camera movement.
 */
public final class DroneSpotlightClientLightSystem {
    private static final Map<UUID, List<BlockPos>> ACTIVE_LIGHTS = new HashMap<>();
    private static long lastUpdateTick = Long.MIN_VALUE;

    private DroneSpotlightClientLightSystem() {
    }

    public static void tick(Minecraft minecraft, Set<UUID> activeDroneIds) {
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null || activeDroneIds.isEmpty()
                || !AddonConfig.enableSpotlightModule()
                || !AddonConfig.spotlightRealLightEnabled()) {
            clearAll(level);
            return;
        }

        long gameTime = level.getGameTime();
        int interval = Math.max(1, AddonConfig.spotlightLightUpdateIntervalTicks());
        if (lastUpdateTick != Long.MIN_VALUE && (gameTime - lastUpdateTick) < interval) {
            return;
        }
        lastUpdateTick = gameTime;

        Set<UUID> seen = new HashSet<>();
        for (UUID droneId : activeDroneIds) {
            Entity drone = findEntity(level, droneId);
            if (drone == null || !drone.isAlive() || drone.isRemoved()) {
                clearForDrone(level, droneId);
                continue;
            }

            updateLights(level, player, drone);
            seen.add(droneId);
        }

        for (UUID existingId : new ArrayList<>(ACTIVE_LIGHTS.keySet())) {
            if (!seen.contains(existingId)) {
                clearForDrone(level, existingId);
            }
        }
    }

    public static void clearAll(ClientLevel level) {
        if (level == null) {
            ACTIVE_LIGHTS.clear();
            return;
        }

        for (UUID droneId : new ArrayList<>(ACTIVE_LIGHTS.keySet())) {
            clearForDrone(level, droneId);
        }
        lastUpdateTick = Long.MIN_VALUE;
    }

    public static void clearForDrone(ClientLevel level, UUID droneId) {
        if (level == null) {
            ACTIVE_LIGHTS.remove(droneId);
            return;
        }

        List<BlockPos> positions = ACTIVE_LIGHTS.remove(droneId);
        if (positions == null) {
            return;
        }

        for (BlockPos pos : positions) {
            removeLight(level, pos);
        }
    }

    private static void updateLights(ClientLevel level, LocalPlayer player, Entity drone) {
        UUID droneId = drone.getUUID();
        List<BlockPos> newPositions = computeLightPositions(level, player, drone);
        List<BlockPos> oldPositions = ACTIVE_LIGHTS.getOrDefault(droneId, List.of());

        for (BlockPos oldPos : oldPositions) {
            if (!newPositions.contains(oldPos)) {
                removeLight(level, oldPos);
            }
        }

        for (int i = 0; i < newPositions.size(); i++) {
            placeLight(level, newPositions.get(i), getLightLevelForIndex(i));
        }

        if (newPositions.isEmpty()) {
            ACTIVE_LIGHTS.remove(droneId);
        } else {
            ACTIVE_LIGHTS.put(droneId, newPositions);
        }
    }

    private static List<BlockPos> computeLightPositions(ClientLevel level, LocalPlayer player, Entity drone) {
        int configuredRange = Math.min(AddonConfig.spotlightRange(), AddonConfig.spotlightLightRange());
        Vec3 direction = resolveSpotlightDirection(player, drone);
        Vec3 origin = drone.position().add(0.0D, 0.35D, 0.0D).add(direction.scale(0.9D));
        Vec3 end = origin.add(direction.scale(configuredRange));
        List<BlockPos> result = new ArrayList<>();

        BlockHitResult hitResult = level.clip(new ClipContext(origin, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, drone));
        double maxDistance = configuredRange;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            maxDistance = Math.max(4.0D, origin.distanceTo(hitResult.getLocation()));
        }

        for (double distance = 1.5D; distance <= maxDistance; distance += 1.5D) {
            Vec3 sample = origin.add(direction.scale(distance));
            addPrimaryLightPoint(level, result, BlockPos.containing(sample));
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            addImpactLights(level, hitResult, result);
        }

        if (result.size() > 48) {
            return new ArrayList<>(result.subList(0, 48));
        }

        return result;
    }

    private static Vec3 resolveSpotlightDirection(LocalPlayer player, Entity drone) {
        UUID controlledDroneId = DroneSpotlightClient.getControlledDroneUuid(player);
        if (controlledDroneId != null && controlledDroneId.equals(drone.getUUID())) {
            Vec3 cameraDirection = player.getLookAngle();
            if (cameraDirection.lengthSqr() >= 1.0E-4D) {
                return cameraDirection.normalize();
            }
        }

        Vec3 droneDirection = drone.getLookAngle();
        if (droneDirection.lengthSqr() >= 1.0E-4D) {
            return droneDirection.normalize();
        }
        return Vec3.directionFromRotation(drone.getXRot(), drone.getYRot());
    }

    private static void addPrimaryLightPoint(ClientLevel level, List<BlockPos> result, BlockPos target) {
        BlockPos pos = findPlaceableLightPos(level, target);
        if (pos != null && !result.contains(pos)) {
            result.add(pos);
        }
    }

    private static void addImpactLights(ClientLevel level, BlockHitResult hitResult, List<BlockPos> result) {
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
            BlockPos pos = findPlaceableLightPos(level, base.offset(offset[0], offset[1], offset[2]));
            if (pos != null && !result.contains(pos)) {
                result.add(pos);
            }
        }
    }

    private static BlockPos findPlaceableLightPos(ClientLevel level, BlockPos target) {
        if (!level.isLoaded(target)) {
            return null;
        }

        BlockPos.MutableBlockPos mutable = target.mutable();
        int[][] offsets = new int[][]{
                {0, 0, 0},
                {0, -1, 0},
                {0, -2, 0},
                {0, 1, 0},
                {1, 0, 0},
                {-1, 0, 0},
                {0, 0, 1},
                {0, 0, -1},
                {1, 1, 0},
                {-1, 1, 0},
                {0, 1, 1},
                {0, 1, -1}
        };

        for (int[] offset : offsets) {
            mutable.set(target).move(offset[0], offset[1], offset[2]);
            if (level.isLoaded(mutable) && canPlaceLight(level, mutable)) {
                return mutable.immutable();
            }
        }

        return null;
    }

    private static boolean canPlaceLight(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
                || state.is(Blocks.LIGHT)
                || (state.getFluidState().is(FluidTags.WATER) && state.getCollisionShape(level, pos).isEmpty());
    }

    private static void placeLight(ClientLevel level, BlockPos pos, int lightLevel) {
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

    private static void removeLight(ClientLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LIGHT)) {
            level.removeBlock(pos, false);
            level.getChunkSource().getLightEngine().checkBlock(pos);
        }
    }

    private static int getLightLevelForIndex(int index) {
        int base = AddonConfig.spotlightLightLevel();
        return Math.max(10, base - (index / 3));
    }

    private static Entity findEntity(ClientLevel level, UUID droneId) {
        for (Entity entity : level.entitiesForRendering()) {
            if (droneId.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }
}
