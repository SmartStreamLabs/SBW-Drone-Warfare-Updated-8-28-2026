package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Retains the pre-experiment ticket callback solely to release any old persisted
 * tickets. Drone control no longer creates or moves force-loaded chunks.
 */
public final class ChunkTicketManager {
    private static final TicketController TICKET_CONTROLLER = new TicketController(
            ResourceLocation.fromNamespaceAndPath(SbwDroneRangeConfig.MOD_ID, "drone_chunks"),
            ChunkTicketManager::validateLoadedTickets
    );
    private static final Map<DroneKey, DroneTicketState> ACTIVE_TICKETS = new HashMap<>();
    private static long serverTick;

    private ChunkTicketManager() {
    }

    public static void registerTicketControllers(RegisterTicketControllersEvent event) {
        event.register(TICKET_CONTROLLER);
    }

    private static void validateLoadedTickets(ServerLevel level, net.neoforged.neoforge.common.world.chunk.TicketHelper tickets) {
        for (UUID owner : tickets.getEntityTickets().keySet()) {
            tickets.removeAllTickets(owner);
        }
    }

    public static void touchDrone(Entity drone, int radius) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone)) {
            return;
        }

        DroneKey key = new DroneKey(serverLevel.dimension(), drone.getUUID());
        DroneTicketState state = ACTIVE_TICKETS.computeIfAbsent(key, ignored -> new DroneTicketState(serverLevel.dimension(), drone.getUUID()));
        state.lastTouchedTick = serverTick;
        state.radius = radius;
        state.level = serverLevel;
        state.syncTo(serverLevel, new ChunkPos(drone.blockPosition()), radius);
    }

    @SubscribeEvent
    public static void onServerLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || !(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return;
        }

        serverTick++;
        Iterator<Map.Entry<DroneKey, DroneTicketState>> iterator = ACTIVE_TICKETS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<DroneKey, DroneTicketState> entry = iterator.next();
            DroneTicketState state = entry.getValue();
            if (!state.dimension.equals(serverLevel.dimension())) {
                continue;
            }

            Entity entity = serverLevel.getEntity(state.droneUuid);
            if (!AddonConfig.enableDroneChunkLoading()
                    || entity == null
                    || !entity.isAlive()
                    || !SbwCompat.isDrone(entity)
                    || serverTick - state.lastTouchedTick > 10) {
                state.releaseAll(serverLevel);
                iterator.remove();
                continue;
            }

            state.syncTo(serverLevel, new ChunkPos(entity.blockPosition()), state.radius);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !SbwCompat.isDrone(event.getEntity())) {
            return;
        }

        DroneKey key = new DroneKey(event.getEntity().level().dimension(), event.getEntity().getUUID());
        DroneTicketState state = ACTIVE_TICKETS.remove(key);
        if (state != null && event.getLevel() instanceof ServerLevel serverLevel) {
            state.releaseAll(serverLevel);
        }
    }

    private record DroneKey(ResourceKey<Level> dimension, UUID droneUuid) {
    }

    private static final class DroneTicketState {
        private final ResourceKey<Level> dimension;
        private final UUID droneUuid;
        private long lastTouchedTick;
        private int radius;
        private ServerLevel level;
        private Set<Long> forcedChunks = Collections.emptySet();

        private DroneTicketState(ResourceKey<Level> dimension, UUID droneUuid) {
            this.dimension = dimension;
            this.droneUuid = droneUuid;
        }

        private void syncTo(ServerLevel level, ChunkPos center, int radius) {
            Set<Long> desired = collectChunks(center, radius);
            for (long chunk : desired) {
                if (!forcedChunks.contains(chunk)) {
                    ChunkPos pos = new ChunkPos(chunk);
                    TICKET_CONTROLLER.forceChunk(level, droneUuid, pos.x, pos.z, true, true);
                }
            }
            for (long chunk : forcedChunks) {
                if (!desired.contains(chunk)) {
                    ChunkPos pos = new ChunkPos(chunk);
                    TICKET_CONTROLLER.forceChunk(level, droneUuid, pos.x, pos.z, false, true);
                }
            }
            this.level = level;
            this.forcedChunks = desired;
        }

        private void releaseAll(ServerLevel level) {
            for (long chunk : forcedChunks) {
                ChunkPos pos = new ChunkPos(chunk);
                TICKET_CONTROLLER.forceChunk(level, droneUuid, pos.x, pos.z, false, true);
            }
            forcedChunks = Collections.emptySet();
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
    }
}
