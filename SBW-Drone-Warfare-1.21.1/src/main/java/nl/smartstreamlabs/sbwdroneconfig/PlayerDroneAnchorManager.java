package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PlayerDroneAnchorManager {
    private static final double ANCHOR_VERTICAL_OFFSET = 1.6D;
    private static final Map<UUID, AnchorState> ACTIVE_ANCHORS = new HashMap<>();
    private static final Map<UUID, Long> PENDING_STABILIZATION = new HashMap<>();
    private static long serverTick;

    private PlayerDroneAnchorManager() {
    }

    public static void touch(ServerPlayer player, Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone) || !drone.isAlive()) {
            release(player, true);
            return;
        }

        AnchorState state = ACTIVE_ANCHORS.computeIfAbsent(player.getUUID(), ignored -> AnchorState.capture(player));
        state.lastTouchedTick = serverTick;
        state.droneUuid = drone.getUUID();
        state.droneDimension = serverLevel.dimension();
        state.applyTo(player, drone);
    }

    public static void release(ServerPlayer player, boolean restorePosition) {
        if (player == null) {
            return;
        }

        AnchorState state = ACTIVE_ANCHORS.remove(player.getUUID());
        if (state != null) {
            state.restore(player, restorePosition);
            PENDING_STABILIZATION.put(player.getUUID(), serverTick + 3L);
        }
    }

    static Vec3 getCapturedOriginalPosition(ServerPlayer player) {
        if (player == null) {
            return null;
        }

        AnchorState state = ACTIVE_ANCHORS.get(player.getUUID());
        return state != null ? state.originalPosition : null;
    }

    @SubscribeEvent
    public static void onServerLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide() || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverTick++;
        Iterator<Map.Entry<UUID, AnchorState>> iterator = ACTIVE_ANCHORS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, AnchorState> entry = iterator.next();
            AnchorState state = entry.getValue();
            if (!state.droneDimension.equals(serverLevel.dimension())) {
                continue;
            }

            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(entry.getKey());
            Entity drone = player == null ? null : serverLevel.getEntity(state.droneUuid);
            if (!isStillActive(player, drone, state)) {
                if (player != null) {
                    state.restore(player, true);
                }
                iterator.remove();
                continue;
            }

            state.applyTo(player, drone);
        }

        Iterator<Map.Entry<UUID, Long>> stabilizationIterator = PENDING_STABILIZATION.entrySet().iterator();
        while (stabilizationIterator.hasNext()) {
            Map.Entry<UUID, Long> entry = stabilizationIterator.next();
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null || serverTick > entry.getValue()) {
                stabilizationIterator.remove();
                continue;
            }

            if (!ACTIVE_ANCHORS.containsKey(entry.getKey())) {
                stabilizeReleasedPlayer(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            release(player, true);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            release(player, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            release(player, false);
        }
    }

    private static boolean isStillActive(ServerPlayer player, Entity drone, AnchorState state) {
        if (!AddonConfig.enableDronePlayerAnchor() || player == null || drone == null || !drone.isAlive()) {
            return false;
        }
        if (serverTick - state.lastTouchedTick > 5) {
            return false;
        }

        return SbwCompat.isUsingLinkedMonitor(player.getMainHandItem())
                && state.droneUuid.equals(SbwCompat.getLinkedDroneUuid(player.getMainHandItem()));
    }

    private static void stabilizeReleasedPlayer(ServerPlayer player) {
        if (!player.getAbilities().mayfly) {
            player.setNoGravity(false);
            player.noPhysics = false;
            player.getAbilities().flying = false;
            player.setDeltaMovement(player.getDeltaMovement().x, Math.min(player.getDeltaMovement().y, -0.08D), player.getDeltaMovement().z);
            player.onUpdateAbilities();
        }
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
    }

    private static final class AnchorState {
        private final ResourceKey<Level> originalDimension;
        private final Vec3 originalPosition;
        private final float originalYaw;
        private final float originalPitch;
        private final boolean originalInvisible;
        private final boolean originalNoGravity;
        private final boolean originalNoPhysics;
        private final boolean originalInvulnerable;
        private UUID droneUuid = new UUID(0L, 0L);
        private ResourceKey<Level> droneDimension = Level.OVERWORLD;
        private long lastTouchedTick;
        private boolean applied;
        private boolean visibilitySynced;

        private AnchorState(
                ResourceKey<Level> originalDimension,
                Vec3 originalPosition,
                float originalYaw,
                float originalPitch,
                boolean originalInvisible,
                boolean originalNoGravity,
                boolean originalNoPhysics,
                boolean originalInvulnerable
        ) {
            this.originalDimension = originalDimension;
            this.originalPosition = originalPosition;
            this.originalYaw = originalYaw;
            this.originalPitch = originalPitch;
            this.originalInvisible = originalInvisible;
            this.originalNoGravity = originalNoGravity;
            this.originalNoPhysics = originalNoPhysics;
            this.originalInvulnerable = originalInvulnerable;
        }

        private static AnchorState capture(ServerPlayer player) {
            return new AnchorState(
                    player.serverLevel().dimension(),
                    player.position(),
                    player.getYRot(),
                    player.getXRot(),
                    player.isInvisible(),
                    player.isNoGravity(),
                    player.noPhysics,
                    player.getAbilities().invulnerable
            );
        }

        private void applyTo(ServerPlayer player, Entity drone) {
            if (!applied) {
                player.setInvisible(true);
                player.setNoGravity(true);
                player.noPhysics = true;
                player.getAbilities().invulnerable = true;
                player.onUpdateAbilities();
                applied = true;
            }

            if (!visibilitySynced) {
                AddonNetwork.syncDroneOperatorVisibility(new DroneOperatorVisibilityMessage(player.getUUID(), true));
                visibilitySynced = true;
            }

            Vec3 target = drone.position().add(0.0D, Math.max(ANCHOR_VERTICAL_OFFSET, drone.getBbHeight() + 1.4D), 0.0D);
            teleport(player, player.serverLevel(), target, drone.getYRot(), player.getXRot());
            player.setDeltaMovement(Vec3.ZERO);
            player.hurtMarked = true;
            player.fallDistance = 0.0F;
        }

        private void restore(ServerPlayer player, boolean restorePosition) {
            player.setInvisible(originalInvisible);
            player.setNoGravity(originalNoGravity);
            player.noPhysics = originalNoPhysics;
            player.getAbilities().invulnerable = originalInvulnerable;
            player.stopFallFlying();
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.setDeltaMovement(Vec3.ZERO);
            player.fallDistance = 0.0F;
            player.hurtMarked = true;
            if (visibilitySynced) {
                AddonNetwork.syncDroneOperatorVisibility(new DroneOperatorVisibilityMessage(player.getUUID(), false));
                visibilitySynced = false;
            }

            if (restorePosition) {
                ServerLevel targetLevel = player.server.getLevel(originalDimension);
                if (targetLevel != null) {
                    teleport(player, targetLevel, resolveSafeRestorePosition(targetLevel, originalPosition), originalYaw, originalPitch);
                }
            }
        }

        private static Vec3 resolveSafeRestorePosition(ServerLevel level, Vec3 originalPosition) {
            BlockPos start = BlockPos.containing(originalPosition);
            int minY = level.getMinBuildHeight() + 1;
            int maxY = level.getMaxBuildHeight() - 2;
            int clampedY = Mth.clamp(start.getY(), minY, maxY);

            for (int offset = 0; offset <= 8; offset++) {
                int downY = clampedY - offset;
                if (downY >= minY) {
                    Vec3 safe = tryResolveStandingPosition(level, new BlockPos(start.getX(), downY, start.getZ()));
                    if (safe != null) {
                        return safe;
                    }
                }

                if (offset == 0) {
                    continue;
                }

                int upY = clampedY + offset;
                if (upY <= maxY) {
                    Vec3 safe = tryResolveStandingPosition(level, new BlockPos(start.getX(), upY, start.getZ()));
                    if (safe != null) {
                        return safe;
                    }
                }
            }

            return new Vec3(originalPosition.x, originalPosition.y, originalPosition.z);
        }

        private static Vec3 tryResolveStandingPosition(ServerLevel level, BlockPos feetPos) {
            BlockPos groundPos = feetPos.below();
            BlockState ground = level.getBlockState(groundPos);
            if (!ground.blocksMotion()) {
                return null;
            }

            BlockState feet = level.getBlockState(feetPos);
            BlockState head = level.getBlockState(feetPos.above());
            if (!feet.getCollisionShape(level, feetPos).isEmpty() || !head.getCollisionShape(level, feetPos.above()).isEmpty()) {
                return null;
            }

            return new Vec3(feetPos.getX() + 0.5D, feetPos.getY(), feetPos.getZ() + 0.5D);
        }

        private static void teleport(ServerPlayer player, ServerLevel targetLevel, Vec3 target, float yaw, float pitch) {
            if (player.serverLevel() != targetLevel) {
                player.teleportTo(targetLevel, target.x, target.y, target.z, Set.of(), yaw, pitch);
                return;
            }

            ServerGamePacketListenerImpl connection = player.connection;
            connection.teleport(target.x, target.y, target.z, yaw, pitch);
        }
    }
}
