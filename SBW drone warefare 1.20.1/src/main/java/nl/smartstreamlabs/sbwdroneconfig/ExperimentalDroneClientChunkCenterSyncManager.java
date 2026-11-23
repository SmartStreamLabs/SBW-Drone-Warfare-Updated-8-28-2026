package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Opt-in A/B experiment that updates only the receiving client's chunk-cache center.
 * Vanilla player tracking and the existing player anchor remain responsible for chunk delivery.
 */
public final class ExperimentalDroneClientChunkCenterSyncManager {
    private static final Map<UUID, Session> ACTIVE_SESSIONS = new HashMap<>();

    private ExperimentalDroneClientChunkCenterSyncManager() {
    }

    public static void touch(ServerPlayer player, Entity drone) {
        if (!AddonConfig.experimentalDroneClientChunkCenterSync()
                || !(drone.level() instanceof ServerLevel serverLevel)
                || player.serverLevel() != serverLevel
                || !drone.isAlive()) {
            clear(player);
            return;
        }

        Session session = ACTIVE_SESSIONS.computeIfAbsent(player.getUUID(), ignored -> new Session());
        if (!session.matches(serverLevel.dimension(), drone.getUUID())) {
            session.begin(serverLevel.dimension(), drone.getUUID(), new ChunkPos(drone.blockPosition()));
            return;
        }
        session.sendIfChanged(player, new ChunkPos(drone.blockPosition()));
    }

    /**
     * Called after the player-anchor manager restores the physical player position.
     */
    public static void release(ServerPlayer player) {
        if (player == null) {
            return;
        }

        Session session = ACTIVE_SESSIONS.remove(player.getUUID());
        if (session != null) {
            session.sendRestore(player, player.chunkPosition());
        }
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            ACTIVE_SESSIONS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            clear(player);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ACTIVE_SESSIONS.clear();
    }

    private static final class Session {
        private ResourceKey<Level> dimension = Level.OVERWORLD;
        private UUID droneUuid = new UUID(0L, 0L);
        private ChunkPos lastTrackedCenter;

        private boolean matches(ResourceKey<Level> dimension, UUID droneUuid) {
            return this.dimension.equals(dimension) && this.droneUuid.equals(droneUuid);
        }

        private void begin(ResourceKey<Level> dimension, UUID droneUuid, ChunkPos initialCenter) {
            this.dimension = dimension;
            this.droneUuid = droneUuid;
            this.lastTrackedCenter = initialCenter;
        }

        private void sendIfChanged(ServerPlayer player, ChunkPos center) {
            if (lastTrackedCenter != null && lastTrackedCenter.equals(center)) {
                return;
            }

            ChunkPos previous = lastTrackedCenter;
            player.connection.send(new ClientboundSetChunkCacheCenterPacket(center.x, center.z));
            lastTrackedCenter = center;
            SbwDroneRangeConfig.LOGGER.info(
                    "Experimental drone client chunk center for {}: {} -> {}",
                    player.getGameProfile().getName(),
                    previous == null ? "none" : previous,
                    center
            );
        }

        private void sendRestore(ServerPlayer player, ChunkPos center) {
            ChunkPos previous = lastTrackedCenter;
            player.connection.send(new ClientboundSetChunkCacheCenterPacket(center.x, center.z));
            lastTrackedCenter = center;
            if (previous != null && !previous.equals(center)) {
                SbwDroneRangeConfig.LOGGER.info(
                        "Experimental drone client chunk center for {}: {} -> {}",
                        player.getGameProfile().getName(),
                        previous,
                        center
                );
            }
        }
    }
}
