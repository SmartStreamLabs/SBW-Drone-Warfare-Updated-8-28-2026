package nl.smartstreamlabs.sbwdroneconfig;

import com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CubedFpvInputState {
    private static final int STALE_TICKS = 10;
    private static final Map<UUID, InputSnapshot> INPUTS = new ConcurrentHashMap<>();

    private CubedFpvInputState() {
    }

    public static void clear(Entity drone){INPUTS.remove(drone.getUUID());applyKeys(drone,(short)0);applyMouseSpeed(drone,0,0);}
    public static void receive(ServerPlayer controller, Entity drone, short keys, double mouseX, double mouseY, int sequence) {
        if (controller == null || !(drone instanceof CubedFpvDroneEntity)) {
            return;
        }

        INPUTS.put(drone.getUUID(), new InputSnapshot(controller.getUUID(), keys, mouseX, mouseY, drone.tickCount, sequence));
        applyKeys(drone, keys);
        applyMouseSpeed(drone, mouseX, mouseY);
    }

    public static void applyRecentInput(Entity drone) {
        if (!(drone instanceof CubedFpvDroneEntity) || drone.level().isClientSide()) {
            return;
        }

        InputSnapshot snapshot = INPUTS.get(drone.getUUID());
        if (snapshot == null) {
            return;
        }

        if (drone.tickCount - snapshot.tick > STALE_TICKS) {
            INPUTS.remove(drone.getUUID());
            applyKeys(drone, (short) 0);
            applyMouseSpeed(drone, 0.0D, 0.0D);
            return;
        }

        ServerPlayer controller = null;
        if (drone.level() instanceof ServerLevel serverLevel) {
            controller = serverLevel.getServer().getPlayerList().getPlayer(snapshot.controllerUuid);
            if (controller == null || !SbwCompat.isUsingLinkedMonitorForDrone(controller, drone)) {
                INPUTS.remove(drone.getUUID());
                applyKeys(drone, (short) 0);
                applyMouseSpeed(drone, 0.0D, 0.0D);
                return;
            }
        }

        applyKeys(drone, snapshot.keys);
        applyMouseSpeed(drone, snapshot.mouseX, snapshot.mouseY);
    }

    private static void applyKeys(Entity drone, short keys) {
        if (drone instanceof VehicleEntity vehicle) {
            vehicle.processInput(keys);
        }
    }

    private static void applyMouseSpeed(Entity drone, double mouseX, double mouseY) {
        if (drone instanceof VehicleEntity vehicle) {
            vehicle.mouseInput(mouseX, mouseY);
        }
    }

    private record InputSnapshot(UUID controllerUuid, short keys, double mouseX, double mouseY, int tick, int sequence) {
    }
}
