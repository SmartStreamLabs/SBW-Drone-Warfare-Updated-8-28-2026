package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class AbandonedFiberCableManager {
    private static final int LOGIN_SNAPSHOT_RESYNC_DELAY_TICKS = 20;
    private static final Map<UUID, Integer> PENDING_LOGIN_SNAPSHOT_RESYNCS = new HashMap<>();

    private AbandonedFiberCableManager() {
    }

    static boolean captureDroneCable(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone)) {
            return false;
        }

        List<Vec3> points = capturePoints(drone);
        if (points.size() < 2) {
            return false;
        }

        UUID ownerId = null;
        SbwCompat.DroneOwnerIdentity owner = SbwCompat.resolveDroneOwner(level, drone);
        if (owner.hasUuid()) {
            ownerId = owner.uuid();
        }

        AbandonedFiberCableData data = AbandonedFiberCableData.get(level);
        AbandonedFiberCableData.CableRecord record = data.addCable(
                level.dimension().location().toString(),
                ownerId,
                points,
                level.getGameTime()
        );
        if (record == null) {
            return false;
        }

        syncRecordToNearbyPlayers(level, record);
        return true;
    }

    static boolean handleCutRequest(ServerPlayer sender, AbandonedFiberCableCutRequestMessage message) {
        if (sender == null || message == null || !sender.isAlive() || !FiberCableCutSystem.playerHoldsAllowedCutTool(sender)) {
            return false;
        }

        ServerLevel level = sender.serverLevel();
        AbandonedFiberCableData data = AbandonedFiberCableData.get(level);
        AbandonedFiberCableData.CableRecord record = data.cable(message.cableId());
        if (record == null || !level.dimension().location().toString().equals(record.dimension()) || !mayPlayerCut(record, sender)) {
            return false;
        }

        Vec3 claimedHitPosition = message.claimedHitPosition();
        if (!FiberCableHitTest.isFinite(claimedHitPosition)) {
            return false;
        }

        double reach = AddonConfig.fiberCableCutReach();
        double radius = AddonConfig.fiberCableInteractionRadius();
        Vec3 eyePosition = sender.getEyePosition();
        double reachWithTolerance = reach + radius;
        if (eyePosition.distanceToSqr(claimedHitPosition) > reachWithTolerance * reachWithTolerance) {
            return false;
        }

        List<Vec3> polyline = interactionPolyline(record);
        int segmentCount = Math.max(0, polyline.size() - 1);
        if (message.claimedSegmentIndex() < 0 || message.claimedSegmentIndex() >= segmentCount) {
            return false;
        }

        Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(
                eyePosition,
                sender.getLookAngle(),
                reach,
                radius,
                polyline,
                message.claimedSegmentIndex(),
                8
        );
        if (hit.isEmpty()) {
            return false;
        }

        Vec3 validatedCutPosition = hit.get().closestPointOnCable();
        if (AddonConfig.fiberCableCutLineOfSightRequired() && !hasLineOfSight(level, sender, validatedCutPosition)) {
            return false;
        }

        if (data.removeCable(record.cableId()) == null) {
            return false;
        }
        syncSnapshotToDimension(level);
        return true;
    }

    private static List<Vec3> capturePoints(Entity drone) {
        List<Vec3> points = new ArrayList<>();
        for (FiberCableRouteState.RoutePoint routePoint : FiberCableRouteState.readPoints(drone)) {
            Vec3 point = routePoint.toVec3();
            if (FiberCableHitTest.isFinite(point)) {
                points.add(point);
            }
        }

        if (points.isEmpty()) {
            Vec3 anchor = FiberOpticCableRenderMath.toRenderAnchor(new Vec3(
                    FiberOpticLinkSystem.getAnchorX(drone),
                    FiberOpticLinkSystem.getAnchorY(drone),
                    FiberOpticLinkSystem.getAnchorZ(drone)
            ));
            if (FiberCableHitTest.isFinite(anchor)) {
                points.add(anchor);
            }
        }

        Vec3 finalPoint = FiberOpticLinkSystem.resolveServerSpoolPoint(drone);
        if (FiberCableHitTest.isFinite(finalPoint)
                && (points.isEmpty() || !points.get(points.size() - 1).closerThan(finalPoint, 0.02D))) {
            points.add(finalPoint);
        }
        return points;
    }

    private static void syncRecordToNearbyPlayers(ServerLevel level, AbandonedFiberCableData.CableRecord record) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level) {
                AddonNetwork.sendAbandonedFiberCables(player, new AbandonedFiberCableSyncMessage(false, List.of(record)));
            }
        }
    }

    private static void syncSnapshotToDimension(ServerLevel level) {
        List<AbandonedFiberCableData.CableRecord> records = new ArrayList<>(AbandonedFiberCableData.get(level).records());
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() == level) {
                AddonNetwork.sendAbandonedFiberCables(player, new AbandonedFiberCableSyncMessage(true, records));
            }
        }
    }

    private static void syncSnapshot(ServerPlayer player) {
        if (player == null || !(player.level() instanceof ServerLevel level)) {
            return;
        }

        List<AbandonedFiberCableData.CableRecord> records = new ArrayList<>(AbandonedFiberCableData.get(level).records());
        AddonNetwork.sendAbandonedFiberCables(player, new AbandonedFiberCableSyncMessage(true, records));
    }

    private static void scheduleSnapshotSync(ServerPlayer player) {
        if (player == null) {
            return;
        }
        syncSnapshot(player);
        PENDING_LOGIN_SNAPSHOT_RESYNCS.put(player.getUUID(), LOGIN_SNAPSHOT_RESYNC_DELAY_TICKS);
    }

    private static void tickPendingSnapshotSyncs(MinecraftServer server) {
        if (server == null || PENDING_LOGIN_SNAPSHOT_RESYNCS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Integer>> iterator = PENDING_LOGIN_SNAPSHOT_RESYNCS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int ticksRemaining = entry.getValue() - 1;
            if (ticksRemaining > 0) {
                entry.setValue(ticksRemaining);
                continue;
            }

            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player != null) {
                syncSnapshot(player);
            }
            iterator.remove();
        }
    }

    private static List<Vec3> interactionPolyline(AbandonedFiberCableData.CableRecord record) {
        if (record == null || record.points().size() < 2) {
            return List.of();
        }

        List<Vec3> points = record.points();
        Vec3 endPoint = points.get(points.size() - 1);
        return List.of(FiberOpticCableRenderMath.buildCutInteractionPoints(points.subList(0, points.size() - 1), endPoint));
    }

    private static boolean mayPlayerCut(AbandonedFiberCableData.CableRecord record, ServerPlayer player) {
        boolean owner = record.ownerId() != null && record.ownerId().equals(player.getUUID());
        return owner ? AddonConfig.fiberCableCanBeCutByOwner() : AddonConfig.fiberCableCanBeCutByOtherPlayers();
    }

    private static boolean hasLineOfSight(ServerLevel level, ServerPlayer player, Vec3 cutPosition) {
        Vec3 eyePosition = player.getEyePosition();
        HitResult hitResult = level.clip(new ClipContext(
                eyePosition,
                cutPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) {
            return true;
        }

        double blockDistanceSqr = eyePosition.distanceToSqr(hitResult.getLocation());
        double cutDistanceSqr = eyePosition.distanceToSqr(cutPosition);
        return blockDistanceSqr + 1.0E-4D >= cutDistanceSqr;
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            scheduleSnapshotSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            scheduleSnapshotSync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PENDING_LOGIN_SNAPSHOT_RESYNCS.remove(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickPendingSnapshotSyncs(event.getServer());
    }
}
