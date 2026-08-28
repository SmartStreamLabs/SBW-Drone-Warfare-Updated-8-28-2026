package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class DroneJamStateManager {
    private static final Map<UUID, JamState> STATES = new HashMap<>();
    private static final int HARD_JAM_HOLD_TICKS = 10;
    private static final float SYNC_EPSILON = 0.02F;
    private static final long DEBUG_LOG_INTERVAL_TICKS = 10L;

    private DroneJamStateManager() {
    }

    public static void recordInfluence(ServerLevel level, Entity drone, double distance, double range) {
        if (level == null || drone == null || !drone.isAlive() || !SbwCompat.isDrone(drone) || !AddonConfig.enableDroneJammer()) {
            return;
        }
        if (FiberOpticLinkSystem.isImmuneToNormalJammers(drone)) {
            return;
        }

        float influence = DroneJamProgressMath.computeInfluence(distance, range);
        if (influence <= 0.0F) {
            return;
        }

        long gameTime = level.getGameTime();
        JamState state = STATES.computeIfAbsent(drone.getUUID(), JamState::new);
        state.controllerPlayerId = state.controllerPlayerId != null ? state.controllerPlayerId : SbwCompat.getDroneControllerUuid(drone);
        if (state.lastInfluenceTick != gameTime) {
            state.lastInfluenceTick = gameTime;
            state.strongestInfluenceThisTick = influence;
            state.nearestDistanceThisTick = distance;
        } else if (influence > state.strongestInfluenceThisTick) {
            state.strongestInfluenceThisTick = influence;
            state.nearestDistanceThisTick = distance;
        }
    }

    public static void markControlled(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }

        JamState state = STATES.computeIfAbsent(drone.getUUID(), JamState::new);
        state.controllerPlayerId = player.getUUID();
    }

    public static float getJamProgress(Entity drone) {
        if (drone == null) {
            return 0.0F;
        }
        JamState state = STATES.get(drone.getUUID());
        return state == null ? 0.0F : state.jamProgress;
    }

    public static boolean hasInterference(Entity drone) {
        return getJamProgress(drone) >= AddonConfig.overlayStartThreshold();
    }

    public static boolean isHardJammed(Entity drone) {
        if (drone == null) {
            return false;
        }
        JamState state = STATES.get(drone.getUUID());
        return state != null
                && state.jamProgress >= AddonConfig.hardJamThreshold()
                && state.hardJamTicks >= HARD_JAM_HOLD_TICKS;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer() == null) {
            return;
        }

        if (!AddonConfig.enableDroneJammer()) {
            if (!STATES.isEmpty()) {
                STATES.clear();
            }
            return;
        }

        tickStates(event.getServer());
    }

    private static void tickStates(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        long gameTime = overworld != null ? overworld.getGameTime() : server.getTickCount();
        Iterator<JamState> iterator = STATES.values().iterator();
        while (iterator.hasNext()) {
            JamState state = iterator.next();
            Entity drone = findDrone(server, state.droneId);
            if (drone == null || !drone.isAlive() || !SbwCompat.isDrone(drone)) {
                iterator.remove();
                continue;
            }

            ServerPlayer controller = findActiveController(server, state, drone);
            if (FiberOpticLinkSystem.isImmuneToNormalJammers(drone)) {
                state.jamProgress = 0.0F;
                state.hardJamTicks = 0;
                state.hardDisconnectLatched = false;
                state.strongestInfluenceThisTick = 0.0F;
                state.nearestDistanceThisTick = Double.MAX_VALUE;
                if (controller != null) {
                    syncIfNeeded(controller, state, false);
                }
                iterator.remove();
                continue;
            }
            boolean influencedThisTick = state.lastInfluenceTick == gameTime && state.strongestInfluenceThisTick > 0.0F;
            float previousProgress = state.jamProgress;
            if (influencedThisTick) {
                state.jamProgress = DroneJamProgressMath.clampProgress(
                        state.jamProgress + DroneJamProgressMath.computeBuildStep(
                                state.strongestInfluenceThisTick,
                                AddonConfig.jammerBuildUpTicks()
                        )
                );
            } else {
                state.jamProgress = DroneJamProgressMath.clampProgress(
                        state.jamProgress - DroneJamProgressMath.computeRecoveryStep(AddonConfig.jammerRecoveryTicks())
                );
            }

            if (state.jamProgress >= AddonConfig.hardJamThreshold()) {
                state.hardJamTicks++;
            } else {
                state.hardJamTicks = 0;
                state.hardDisconnectLatched = false;
            }

            boolean hardJammed = state.hardJamTicks >= HARD_JAM_HOLD_TICKS;
            if (controller != null) {
                syncIfNeeded(controller, state, hardJammed);
                maybeLogDebug(state, controller, influencedThisTick, gameTime);
                if (hardJammed && !state.hardDisconnectLatched && drone.level() instanceof ServerLevel serverLevel) {
                    controller.displayClientMessage(
                            Component.translatable("message.sbwdroneconfig.drone_jammed_hard").withStyle(ChatFormatting.RED),
                            true
                    );
                    DroneWaterDisconnectSystem.forceControlDisconnect(serverLevel, drone);
                    state.hardDisconnectLatched = true;
                    syncIfNeeded(controller, state, true);
                }
            } else {
                maybeLogDebug(state, null, influencedThisTick, gameTime);
            }

            state.strongestInfluenceThisTick = 0.0F;
            state.nearestDistanceThisTick = Double.MAX_VALUE;

            if (state.jamProgress <= 0.0F && !influencedThisTick && controller == null && previousProgress <= 0.0F) {
                iterator.remove();
            }
        }
    }

    private static void syncIfNeeded(ServerPlayer player, JamState state, boolean hardJammed) {
        if (player == null) {
            return;
        }

        if (state.lastSentProgress < 0.0F
                || Math.abs(state.jamProgress - state.lastSentProgress) >= SYNC_EPSILON
                || (state.jamProgress <= 0.0F && state.lastSentProgress > 0.0F)
                || state.lastSentHardJammed != hardJammed) {
            AddonNetwork.sendDroneJam(player, new DroneJamSyncMessage(state.droneId, state.jamProgress, hardJammed));
            state.lastSentProgress = state.jamProgress;
            state.lastSentHardJammed = hardJammed;
        }
    }

    private static void maybeLogDebug(JamState state, ServerPlayer controller, boolean influencedThisTick, long gameTime) {
        if (!AddonConfig.debugJammer()) {
            return;
        }
        if (!influencedThisTick && state.jamProgress <= 0.0F) {
            return;
        }
        if (gameTime - state.lastDebugTick < DEBUG_LOG_INTERVAL_TICKS) {
            return;
        }

        state.lastDebugTick = gameTime;
        String playerLabel = controller != null
                ? controller.getGameProfile().getName()
                : String.valueOf(state.controllerPlayerId);
        SbwDroneRangeConfig.LOGGER.info(
                "Progressive jammer state drone={} player={} distance={} strength={} jamProgress={} hardJamTicks={}",
                state.droneId,
                playerLabel,
                influencedThisTick ? String.format("%.2f", state.nearestDistanceThisTick) : "n/a",
                influencedThisTick ? String.format("%.3f", state.strongestInfluenceThisTick) : "0.000",
                String.format("%.3f", state.jamProgress),
                state.hardJamTicks
        );
    }

    private static Entity findDrone(MinecraftServer server, UUID droneId) {
        if (server == null || droneId == null) {
            return null;
        }

        for (ServerLevel level : server.getAllLevels()) {
            Entity drone = level.getEntity(droneId);
            if (drone != null) {
                return drone;
            }
        }
        return null;
    }

    private static ServerPlayer findActiveController(MinecraftServer server, JamState state, Entity drone) {
        if (server == null || drone == null) {
            return null;
        }

        if (state.controllerPlayerId != null) {
            ServerPlayer directPlayer = server.getPlayerList().getPlayer(state.controllerPlayerId);
            if (directPlayer != null && SbwCompat.isUsingLinkedMonitorForDrone(directPlayer, drone)) {
                return directPlayer;
            }
        }

        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer controller = server.getPlayerList().getPlayer(controllerUuid);
            if (controller != null && SbwCompat.isUsingLinkedMonitorForDrone(controller, drone)) {
                state.controllerPlayerId = controller.getUUID();
                return controller;
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
                state.controllerPlayerId = player.getUUID();
                return player;
            }
        }

        return null;
    }

    private static final class JamState {
        private final UUID droneId;
        private UUID controllerPlayerId;
        private float jamProgress;
        private int hardJamTicks;
        private float strongestInfluenceThisTick;
        private double nearestDistanceThisTick = Double.MAX_VALUE;
        private long lastInfluenceTick = Long.MIN_VALUE;
        private boolean hardDisconnectLatched;
        private float lastSentProgress = -1.0F;
        private boolean lastSentHardJammed;
        private long lastDebugTick = Long.MIN_VALUE;

        private JamState(UUID droneId) {
            this.droneId = droneId;
        }
    }
}
