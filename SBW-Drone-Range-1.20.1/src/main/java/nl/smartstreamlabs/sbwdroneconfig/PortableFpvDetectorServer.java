package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PortableFpvDetectorServer {
    private static final Map<UUID, UUID> SELECTED_TARGETS = new ConcurrentHashMap<>();
    private static final List<DetectorAdapter> ADAPTERS = new ArrayList<>();

    static {
        registerAdapter(new BuiltInDetectorAdapter());
    }

    private PortableFpvDetectorServer() {
    }

    public static void registerAdapter(DetectorAdapter adapter) {
        if (adapter != null) {
            ADAPTERS.add(adapter);
        }
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            SELECTED_TARGETS.remove(player.getUUID());
        }
    }

    public static void tickHeldDetector(ServerPlayer player) {
        if (player == null || !player.isAlive()) {
            return;
        }
        if (!DroneJammerItem.isActiveJammerHeld(player)) {
            clear(player);
            return;
        }

        ServerLevel level = player.serverLevel();
        int interval = Math.max(1, AddonConfig.portableFpvDetectorScanIntervalTicks());
        if (Math.floorMod(level.getGameTime() + player.getId(), interval) != 0L) {
            return;
        }

        sendScan(player);
    }

    public static void selectTarget(ServerPlayer player, UUID targetId) {
        if (!canUseDetector(player)) {
            clear(player);
            return;
        }

        List<PortableFpvDetectorTarget> targets = scan(player);
        if (targetId == null) {
            SELECTED_TARGETS.remove(player.getUUID());
            sendScan(player, targets, null);
            return;
        }

        boolean validTarget = targets.stream().anyMatch(target -> targetId.equals(target.droneId()));
        if (validTarget) {
            SELECTED_TARGETS.put(player.getUUID(), targetId);
            sendScan(player, targets, targetId);
        } else {
            SELECTED_TARGETS.remove(player.getUUID());
            sendScan(player, targets, targets.isEmpty() ? null : targets.get(0).droneId());
        }
    }

    public static void sendScan(ServerPlayer player) {
        if (!canUseDetector(player)) {
            if (player != null) {
                AddonNetwork.sendPortableFpvDetectorScan(player, PortableFpvDetectorScanMessage.empty(false));
            }
            return;
        }

        List<PortableFpvDetectorTarget> targets = scan(player);
        UUID selectedTargetId = selectedTargetFor(player, targets);
        sendScan(player, targets, selectedTargetId);
    }

    private static void sendScan(ServerPlayer player, List<PortableFpvDetectorTarget> targets, UUID selectedTargetId) {
        AddonNetwork.sendPortableFpvDetectorScan(player, new PortableFpvDetectorScanMessage(
                AddonConfig.portableFpvDetectorRange(),
                targets,
                selectedTargetId,
                true,
                AddonConfig.portableFpvDetectorVideoEnabled()
        ));
    }

    private static boolean canUseDetector(ServerPlayer player) {
        return player != null && player.isAlive() && AddonConfig.enableDroneJammer() && DroneJammerItem.isActiveJammerHeld(player);
    }

    private static UUID selectedTargetFor(ServerPlayer player, List<PortableFpvDetectorTarget> targets) {
        UUID selectedTargetId = SELECTED_TARGETS.get(player.getUUID());
        if (selectedTargetId != null && targets.stream().anyMatch(target -> selectedTargetId.equals(target.droneId()))) {
            return selectedTargetId;
        }

        if (targets.isEmpty()) {
            SELECTED_TARGETS.remove(player.getUUID());
            return null;
        }

        UUID firstTargetId = targets.get(0).droneId();
        SELECTED_TARGETS.put(player.getUUID(), firstTargetId);
        return firstTargetId;
    }

    static List<PortableFpvDetectorTarget> scan(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return List.of();
        }

        double range = Math.max(1.0D, AddonConfig.portableFpvDetectorRange());
        AABB searchBox = player.getBoundingBox().inflate(range);
        List<PortableFpvDetectorTarget> targets = new ArrayList<>();

        for (Entity entity : level.getEntities(player, searchBox, entity -> isDetectableTarget(level, player, entity))) {
            double distance = Math.sqrt(entity.distanceToSqr(player));
            if (distance > range) {
                continue;
            }

            DetectorAdapter adapter = adapterFor(entity);
            if (adapter == null) {
                continue;
            }

            targets.add(adapter.toTarget(level, player, entity, distance, range));
        }

        targets.sort(Comparator
                .comparingDouble(PortableFpvDetectorTarget::signalStrength)
                .reversed()
                .thenComparingDouble(PortableFpvDetectorTarget::distance)
                .thenComparing(PortableFpvDetectorTarget::displayName));

        int maximumTargets = Math.max(1, AddonConfig.portableFpvDetectorMaximumTargets());
        return targets.size() <= maximumTargets ? List.copyOf(targets) : List.copyOf(targets.subList(0, maximumTargets));
    }

    private static boolean isDetectableTarget(ServerLevel level, ServerPlayer player, Entity entity) {
        if (level == null || player == null || entity == null || !entity.isAlive()) {
            return false;
        }
        DetectorAdapter adapter = adapterFor(entity);
        if (adapter == null) {
            return false;
        }
        if (adapter.linkMode(entity) == DroneLinkMode.FIBER_OPTIC) {
            return false;
        }
        if (!AddonConfig.portableFpvDetectorDetectOwnDrones()) {
            SbwCompat.DroneOwnerIdentity owner = SbwCompat.resolveDroneOwner(level, entity);
            if (owner.hasUuid() && player.getUUID().equals(owner.uuid())) {
                return false;
            }
        }
        return !AddonConfig.portableFpvDetectorRequireLineOfSight() || player.hasLineOfSight(entity);
    }

    private static DetectorAdapter adapterFor(Entity entity) {
        for (DetectorAdapter adapter : ADAPTERS) {
            if (adapter.supports(entity)) {
                return adapter;
            }
        }
        return null;
    }

    public interface DetectorAdapter {
        boolean supports(Entity entity);

        PortableFpvDetectorTarget toTarget(ServerLevel level, ServerPlayer player, Entity entity, double distance, double range);

        default DroneLinkMode linkMode(Entity entity) {
            return FiberOpticLinkSystem.getLinkMode(entity);
        }
    }

    private static final class BuiltInDetectorAdapter implements DetectorAdapter {
        @Override
        public boolean supports(Entity entity) {
            return SbwCompat.isDrone(entity);
        }

        @Override
        public PortableFpvDetectorTarget toTarget(ServerLevel level, ServerPlayer player, Entity entity, double distance, double range) {
            ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            String type = typeId == null ? entity.getType().toString() : typeId.toString();
            SbwCompat.DroneOwnerIdentity owner = SbwCompat.resolveDroneOwner(level, entity);
            boolean active = SbwCompat.getDroneControllerUuid(entity) != null || SbwCompat.findLinkedMonitorController(level, entity) != null;
            double signal = range <= 0.0D ? 0.0D : 1.0D - (distance / range);

            return new PortableFpvDetectorTarget(
                    entity.getUUID(),
                    entity.getId(),
                    type,
                    displayNameFor(entity, type),
                    owner.hasName() ? owner.name() : "",
                    linkMode(entity),
                    AddonConfig.portableFpvDetectorVideoEnabled() && hasCompatibleCamera(type),
                    active,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    distance,
                    signal
            );
        }

        private static boolean hasCompatibleCamera(String type) {
            return SbwDroneRangeConfig.CUBED_FPV_DRONE_ENTITY_ID.equals(type)
                    || SbwDroneRangeConfig.HEAVY_BOMBER_DRONE_ENTITY_ID.equals(type)
                    || SbwDroneRangeConfig.UKRAINIAN_BAYRAKTAR_TB2_ENTITY_ID.equals(type)
                    || SbwDroneRangeConfig.LUCAS_DRONE_ENTITY_ID.equals(type)
                    || SbwDroneRangeConfig.DRONE_ENTITY_ID.equals(type);
        }

        private static String displayNameFor(Entity entity, String type) {
            if (entity.hasCustomName() && entity.getCustomName() != null && !entity.getCustomName().getString().isBlank()) {
                return entity.getCustomName().getString();
            }
            if (SbwDroneRangeConfig.CUBED_FPV_DRONE_ENTITY_ID.equals(type)) {
                return "FPV Drone";
            }
            if (SbwDroneRangeConfig.LUCAS_DRONE_ENTITY_ID.equals(type)) {
                return "LUCAS Drone";
            }
            if (SbwDroneRangeConfig.HEAVY_BOMBER_DRONE_ENTITY_ID.equals(type)) {
                return "Heavy Bomber Drone";
            }
            if (SbwDroneRangeConfig.UKRAINIAN_BAYRAKTAR_TB2_ENTITY_ID.equals(type)) {
                return "Bayraktar TB2";
            }
            if (SbwDroneRangeConfig.DRONE_ENTITY_ID.equals(type)) {
                return "Superb Warfare Drone";
            }
            return SbwCompat.getDroneDisplayName(entity);
        }
    }
}
