package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DroneClientChunkSyncManager {
    private static final Map<UUID, PlayerChunkViewState> ACTIVE_VIEWS = new HashMap<>();
    private static final Method UPDATE_CHUNK_TRACKING_METHOD = resolveUpdateChunkTrackingMethod();
    private static long serverTick;

    private DroneClientChunkSyncManager() {
    }

    public static void touch(ServerPlayer player, Entity drone, int radius) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone)) {
            return;
        }

        PlayerChunkViewState state = ACTIVE_VIEWS.computeIfAbsent(player.getUUID(), ignored -> new PlayerChunkViewState());
        state.lastTouchedTick = serverTick;
        state.dimension = serverLevel.dimension();
        state.droneUuid = drone.getUUID();
        state.radius = radius;
        state.syncTo(player, serverLevel, new ChunkPos(drone.blockPosition()));
    }

    @SubscribeEvent
    public static void onServerLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide() || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverTick++;
        Iterator<Map.Entry<UUID, PlayerChunkViewState>> iterator = ACTIVE_VIEWS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerChunkViewState> entry = iterator.next();
            PlayerChunkViewState state = entry.getValue();
            if (!state.dimension.equals(serverLevel.dimension())) {
                continue;
            }

            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(entry.getKey());
            Entity drone = player == null ? null : serverLevel.getEntity(state.droneUuid);
            if (!isStillActive(player, drone, state)) {
                state.release(player);
                iterator.remove();
                continue;
            }

            state.syncTo(player, serverLevel, new ChunkPos(drone.blockPosition()));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerChunkViewState state = ACTIVE_VIEWS.remove(player.getUUID());
            if (state != null) {
                state.release(player);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !SbwCompat.isDrone(event.getEntity())) {
            return;
        }

        Iterator<Map.Entry<UUID, PlayerChunkViewState>> iterator = ACTIVE_VIEWS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PlayerChunkViewState> entry = iterator.next();
            PlayerChunkViewState state = entry.getValue();
            if (!state.droneUuid.equals(event.getEntity().getUUID())) {
                continue;
            }

            ServerPlayer player = event.getEntity().getServer() == null
                    ? null
                    : event.getEntity().getServer().getPlayerList().getPlayer(entry.getKey());
            state.release(player);
            iterator.remove();
        }
    }

    private static boolean isStillActive(ServerPlayer player, Entity drone, PlayerChunkViewState state) {
        if (!AddonConfig.enableDroneChunkLoading() || player == null || drone == null || !drone.isAlive()) {
            return false;
        }
        if (serverTick - state.lastTouchedTick > 10) {
            return false;
        }

        return SbwCompat.isUsingLinkedMonitorForDrone(player, drone)
                && state.droneUuid.equals(drone.getUUID());
    }

    private static final class PlayerChunkViewState {
        private ResourceKey<Level> dimension = Level.OVERWORLD;
        private UUID droneUuid = new UUID(0L, 0L);
        private long lastTouchedTick;
        private int radius;
        private int sentRadius = Integer.MIN_VALUE;
        private ChunkPos lastCenter;
        private Set<Long> sentChunks = Collections.emptySet();

        private void syncTo(ServerPlayer player, ServerLevel level, ChunkPos center) {
            int effectiveRadius = Math.max(0, radius);
            int syncRadius = getSyncRadius(player, effectiveRadius);

            if (sentRadius != syncRadius) {
                player.connection.send(new ClientboundSetChunkCacheRadiusPacket(syncRadius));
                sentRadius = syncRadius;
            }

            if (lastCenter == null || lastCenter.x != center.x || lastCenter.z != center.z) {
                player.connection.send(new ClientboundSetChunkCacheCenterPacket(center.x, center.z));
                lastCenter = center;
            }

            Set<Long> desired = collectChunks(center, syncRadius);
            sentChunks = syncChunkView(player, level, desired, sentChunks);

        }

        private void release(ServerPlayer player) {
            if (player == null) {
                clear();
                return;
            }

            int viewDistance = player.server.getPlayerList().getViewDistance();
            ChunkPos playerChunk = player.chunkPosition();
            player.connection.send(new ClientboundSetChunkCacheCenterPacket(playerChunk.x, playerChunk.z));
            player.connection.send(new ClientboundSetChunkCacheRadiusPacket(viewDistance));
            Set<Long> desired = collectChunks(playerChunk, Math.max(2, viewDistance));
            syncChunkView(player, player.serverLevel(), desired, sentChunks);

            clear();
        }

        private void clear() {
            sentChunks = Collections.emptySet();
            sentRadius = Integer.MIN_VALUE;
            lastCenter = null;
        }
    }

    private static Set<Long> collectChunks(ChunkPos center, int radius) {
        Set<Long> chunks = new HashSet<>();
        for (int x = center.x - radius; x <= center.x + radius; x++) {
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                chunks.add(ChunkPos.asLong(x, z));
            }
        }
        return chunks;
    }

    private static int getSyncRadius(ServerPlayer player, int configuredRadius) {
        int viewDistance = player.server.getPlayerList().getViewDistance();
        return Math.max(2, Math.max(configuredRadius + 1, viewDistance));
    }

    private static Set<Long> syncChunkView(ServerPlayer player, ServerLevel level, Set<Long> desired, Set<Long> current) {
        ChunkMap chunkMap = level.getChunkSource().chunkMap;
        MutableObject<ClientboundLevelChunkWithLightPacket> packetCache = new MutableObject<>();
        for (long chunkLong : desired) {
            if (current.contains(chunkLong)) {
                continue;
            }

            ChunkPos pos = new ChunkPos(chunkLong);
            if (!invokeChunkTracking(chunkMap, player, pos, packetCache, false, true)) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
                if (chunk == null) {
                    chunk = (LevelChunk) level.getChunkSource().getChunk(pos.x, pos.z, ChunkStatus.FULL, true);
                }

                player.trackChunk(
                        pos,
                        new ClientboundLevelChunkWithLightPacket(chunk, level.getChunkSource().getLightEngine(), null, null)
                );
            }
        }

        for (long chunkLong : current) {
            if (!desired.contains(chunkLong)) {
                ChunkPos pos = new ChunkPos(chunkLong);
                if (!invokeChunkTracking(chunkMap, player, pos, packetCache, true, false)) {
                    player.untrackChunk(pos);
                }
            }
        }

        return desired;
    }

    private static Method resolveUpdateChunkTrackingMethod() {
        try {
            Method method = ChunkMap.class.getDeclaredMethod(
                    "updateChunkTracking",
                    ServerPlayer.class,
                    ChunkPos.class,
                    MutableObject.class,
                    boolean.class,
                    boolean.class
            );
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            SbwDroneRangeConfig.LOGGER.warn("Could not access ChunkMap.updateChunkTracking; falling back to direct chunk packets.", exception);
            return null;
        }
    }

    private static boolean invokeChunkTracking(
            ChunkMap chunkMap,
            ServerPlayer player,
            ChunkPos pos,
            MutableObject<ClientboundLevelChunkWithLightPacket> packetCache,
            boolean wasLoaded,
            boolean shouldBeLoaded
    ) {
        if (UPDATE_CHUNK_TRACKING_METHOD == null) {
            return false;
        }

        try {
            UPDATE_CHUNK_TRACKING_METHOD.invoke(chunkMap, player, pos, packetCache, wasLoaded, shouldBeLoaded);
            return true;
        } catch (ReflectiveOperationException exception) {
            SbwDroneRangeConfig.LOGGER.warn("Failed to sync drone chunk {} for {} through ChunkMap; using packet fallback.", pos, player.getGameProfile().getName(), exception);
            return false;
        }
    }
}
