package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FiberCableCutSystem {
    private static final String TAG_LAST_CUT_REQUEST_TICK = "sbwdroneconfigLastFiberCutRequestTick";
    private static final String TAG_LAST_CUT_REQUEST_SESSION = "sbwdroneconfigLastFiberCutRequestSession";
    private static final String TAG_LAST_CUT_REQUEST_SEGMENT = "sbwdroneconfigLastFiberCutRequestSegment";
    private static final int SERVER_HINT_WINDOW = 8;
    private static final int DUPLICATE_WINDOW_TICKS = 2;

    private FiberCableCutSystem() {
    }

    public static boolean handleCutRequest(ServerPlayer sender, FiberCableCutRequestMessage message) {
        if (sender == null || message == null || !sender.isAlive()) {
            return false;
        }

        ServerLevel level = sender.serverLevel();
        Entity entity = level.getEntity(message.droneEntityId());
        if (!(entity instanceof CubedFpvDroneEntity drone) || !SbwCompat.isDrone(drone)) {
            return false;
        }
        if (!FiberOpticLinkSystem.isFiberSessionActive(drone)) {
            return false;
        }

        UUID currentSessionId = FiberCableRouteState.sessionId(drone);
        if (!currentSessionId.equals(message.sessionId())) {
            return false;
        }
        if (!playerHoldsAllowedCutTool(sender)) {
            return false;
        }
        if (!mayPlayerCutCable(level, drone, sender)) {
            return false;
        }
        if (isDuplicateRequest(sender, message, level.getGameTime())) {
            return false;
        }

        int serverRevision = FiberCableRouteState.revision(drone);
        if (!isFreshEnough(message.claimedRevision(), serverRevision)) {
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

        List<Vec3> polyline = authoritativeInteractionPolyline(drone);
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
                SERVER_HINT_WINDOW
        );
        if (hit.isEmpty()) {
            return false;
        }

        Vec3 validatedCutPosition = hit.get().closestPointOnCable();
        if (AddonConfig.fiberCableCutLineOfSightRequired() && !hasLineOfSight(level, sender, validatedCutPosition)) {
            return false;
        }

        markRequest(sender, message, level.getGameTime());
        if (!FiberOpticLinkSystem.cutFiberCableByShears(level, drone, sender, validatedCutPosition)) {
            return false;
        }

        damageHeldShears(sender);
        return true;
    }

    static List<Vec3> authoritativeInteractionPolyline(Entity drone) {
        if (drone == null) {
            return List.of();
        }

        List<Vec3> routePoints = new ArrayList<>();
        for (FiberCableRouteState.RoutePoint routePoint : FiberCableRouteState.readPoints(drone)) {
            Vec3 point = routePoint.toVec3();
            if (FiberCableHitTest.isFinite(point)) {
                routePoints.add(point);
            }
        }

        if (routePoints.isEmpty()) {
            Vec3 anchor = FiberOpticCableRenderMath.toRenderAnchor(new Vec3(
                    FiberOpticLinkSystem.getAnchorX(drone),
                    FiberOpticLinkSystem.getAnchorY(drone),
                    FiberOpticLinkSystem.getAnchorZ(drone)
            ));
            if (FiberCableHitTest.isFinite(anchor)) {
                routePoints.add(anchor);
            }
        }

        Vec3 dynamicEnd = FiberOpticLinkSystem.resolveServerSpoolPoint(drone);
        return List.of(FiberOpticCableRenderMath.buildCutInteractionPoints(routePoints, dynamicEnd));
    }

    static boolean mayPlayerCutCable(ServerLevel level, Entity drone, ServerPlayer player) {
        boolean protectedBreaker = FiberOpticLinkSystem.isProtectedCableBreaker(level, drone, player)
                || SbwCompat.isUsingLinkedMonitorForDrone(player, drone);
        if (protectedBreaker) {
            return AddonConfig.fiberCableCanBeCutByOwner();
        }
        return AddonConfig.fiberCableCanBeCutByOtherPlayers();
    }

    static boolean playerHoldsAllowedCutTool(Player player) {
        if (player == null) {
            return false;
        }
        if (!AddonConfig.fiberCableCutRequiresShears()) {
            return player.getMainHandItem().is(Items.SHEARS) || player.getOffhandItem().is(Items.SHEARS);
        }
        return player.getMainHandItem().is(Items.SHEARS) || player.getOffhandItem().is(Items.SHEARS);
    }

    static boolean isFreshEnough(int claimedRevision, int serverRevision) {
        return claimedRevision > 0 && serverRevision > 0 && Math.abs(serverRevision - claimedRevision) <= 16;
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

    private static boolean isDuplicateRequest(ServerPlayer player, FiberCableCutRequestMessage message, long gameTime) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(TAG_LAST_CUT_REQUEST_SESSION)) {
            return false;
        }

        long lastTick = data.getLong(TAG_LAST_CUT_REQUEST_TICK);
        return gameTime - lastTick <= DUPLICATE_WINDOW_TICKS
                && message.sessionId().equals(data.getUUID(TAG_LAST_CUT_REQUEST_SESSION))
                && message.claimedSegmentIndex() == data.getInt(TAG_LAST_CUT_REQUEST_SEGMENT);
    }

    private static void markRequest(ServerPlayer player, FiberCableCutRequestMessage message, long gameTime) {
        CompoundTag data = player.getPersistentData();
        data.putLong(TAG_LAST_CUT_REQUEST_TICK, gameTime);
        data.putUUID(TAG_LAST_CUT_REQUEST_SESSION, message.sessionId());
        data.putInt(TAG_LAST_CUT_REQUEST_SEGMENT, message.claimedSegmentIndex());
    }

    private static void damageHeldShears(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.SHEARS)) {
                continue;
            }

            EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, slot);
            return;
        }
    }
}
