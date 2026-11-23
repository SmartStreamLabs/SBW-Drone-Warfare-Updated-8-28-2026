package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class FiberCableCutClient {
    private static long lastCutRequestGameTime = Long.MIN_VALUE;

    private FiberCableCutClient() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FiberCableCutClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.register(FiberCableCutClient.class);
    }

    @SubscribeEvent
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.screen != null) {
            return;
        }
        if (!isHoldingShears(minecraft.player, event.getHand())) {
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        if (gameTime == lastCutRequestGameTime) {
            return;
        }

        Optional<ClientCutTarget> target = findTargetedCable(minecraft);
        if (target.isEmpty()) {
            Optional<AbandonedClientCutTarget> abandonedTarget = findTargetedAbandonedCable(minecraft);
            if (abandonedTarget.isEmpty()) {
                return;
            }

            AbandonedClientCutTarget cutTarget = abandonedTarget.get();
            AddonNetwork.sendAbandonedFiberCableCutRequest(new AbandonedFiberCableCutRequestMessage(
                    cutTarget.cableId(),
                    cutTarget.segmentIndex(),
                    cutTarget.hitPosition()
            ));
            lastCutRequestGameTime = gameTime;
            event.setCanceled(true);
            event.setSwingHand(true);
            return;
        }

        ClientCutTarget cutTarget = target.get();
        AddonNetwork.sendFiberCableCutRequest(new FiberCableCutRequestMessage(
                cutTarget.droneEntityId(),
                cutTarget.sessionId(),
                cutTarget.routeRevision(),
                cutTarget.segmentIndex(),
                cutTarget.hitPosition()
        ));
        lastCutRequestGameTime = gameTime;
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    static Optional<ClientCutTarget> findTargetedCable(Minecraft minecraft) {
        if (minecraft == null || minecraft.level == null || minecraft.gameRenderer == null) {
            return Optional.empty();
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.getPosition();
        Vector3f lookVector = camera.getLookVector();
        Vec3 lookDirection = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        double reach = AddonConfig.fiberCableCutReach();
        double radius = AddonConfig.fiberCableInteractionRadius();
        ClientLevel level = minecraft.level;

        ClientCutTarget bestTarget = null;
        for (DroneFiberOpticClient.FiberHudState fiberState : DroneFiberOpticClient.renderStates()) {
            if (fiberState == null || fiberState.linkMode() != DroneLinkMode.FIBER_OPTIC || !fiberState.fiberSessionActive()) {
                continue;
            }

            DroneFiberOpticClient.FiberRouteClientState routeState = DroneFiberOpticClient.routeStateFor(fiberState.droneId());
            if (routeState == null || !routeState.active() || routeState.points().isEmpty()) {
                continue;
            }

            Entity entity = DroneFiberOpticClient.findDroneEntity(level, fiberState);
            if (!(entity instanceof CubedFpvDroneEntity drone) || !drone.isAlive()) {
                continue;
            }

            float partialTick = partialTick(minecraft);
            Vec3 dynamicSpoolPoint = FiberOpticCableRenderMath.toCubedFpvSpoolAttachment(drone, partialTick);
            List<Vec3> interactionPoints = List.of(FiberOpticCableRenderMath.buildCutInteractionPoints(routeState.points(), dynamicSpoolPoint));
            Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(cameraPosition, lookDirection, reach, radius, interactionPoints);
            if (hit.isEmpty()) {
                continue;
            }

            FiberCableHitTest.Result result = hit.get();
            double distanceFromPlayer = minecraft.player == null
                    ? result.distanceFromOrigin()
                    : minecraft.player.getEyePosition(partialTick).distanceTo(result.closestPointOnCable());
            ClientCutTarget candidate = new ClientCutTarget(
                    fiberState.droneEntityId(),
                    routeState.sessionId(),
                    routeState.revision(),
                    result.segmentIndex(),
                    result.closestPointOnCable(),
                    result.distanceFromRay(),
                    distanceFromPlayer
            );
            if (bestTarget == null
                    || candidate.distanceFromRay() < bestTarget.distanceFromRay()
                    || (candidate.distanceFromRay() == bestTarget.distanceFromRay()
                    && candidate.distanceFromPlayer() < bestTarget.distanceFromPlayer())) {
                bestTarget = candidate;
            }
        }

        return Optional.ofNullable(bestTarget);
    }

    static Optional<AbandonedClientCutTarget> findTargetedAbandonedCable(Minecraft minecraft) {
        if (minecraft == null || minecraft.level == null || minecraft.gameRenderer == null) {
            return Optional.empty();
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPosition = camera.getPosition();
        Vector3f lookVector = camera.getLookVector();
        Vec3 lookDirection = new Vec3(lookVector.x(), lookVector.y(), lookVector.z());
        double reach = AddonConfig.fiberCableCutReach();
        double radius = AddonConfig.fiberCableInteractionRadius();
        float partialTick = partialTick(minecraft);

        AbandonedClientCutTarget bestTarget = null;
        for (AbandonedFiberCableData.CableRecord cable : AbandonedFiberCableClient.cables()) {
            if (cable == null || cable.points().size() < 2) {
                continue;
            }

            List<Vec3> points = cable.points();
            Vec3 staticEnd = points.get(points.size() - 1);
            List<Vec3> interactionPoints = List.of(FiberOpticCableRenderMath.buildCutInteractionPoints(points.subList(0, points.size() - 1), staticEnd));
            Optional<FiberCableHitTest.Result> hit = FiberCableHitTest.closestHit(cameraPosition, lookDirection, reach, radius, interactionPoints);
            if (hit.isEmpty()) {
                continue;
            }

            FiberCableHitTest.Result result = hit.get();
            double distanceFromPlayer = minecraft.player == null
                    ? result.distanceFromOrigin()
                    : minecraft.player.getEyePosition(partialTick).distanceTo(result.closestPointOnCable());
            AbandonedClientCutTarget candidate = new AbandonedClientCutTarget(
                    cable.cableId(),
                    result.segmentIndex(),
                    result.closestPointOnCable(),
                    result.distanceFromRay(),
                    distanceFromPlayer
            );
            if (bestTarget == null
                    || candidate.distanceFromRay() < bestTarget.distanceFromRay()
                    || (candidate.distanceFromRay() == bestTarget.distanceFromRay()
                    && candidate.distanceFromPlayer() < bestTarget.distanceFromPlayer())) {
                bestTarget = candidate;
            }
        }

        return Optional.ofNullable(bestTarget);
    }

    private static boolean isHoldingShears(LocalPlayer player, InteractionHand hand) {
        return player != null && hand != null && player.getItemInHand(hand).is(Items.SHEARS);
    }

    private static float partialTick(Minecraft minecraft) {
        return minecraft == null || minecraft.getTimer() == null
                ? 1.0F
                : minecraft.getTimer().getGameTimeDeltaPartialTick(true);
    }

    record ClientCutTarget(
            int droneEntityId,
            UUID sessionId,
            int routeRevision,
            int segmentIndex,
            Vec3 hitPosition,
            double distanceFromRay,
            double distanceFromPlayer
    ) {
    }

    record AbandonedClientCutTarget(
            UUID cableId,
            int segmentIndex,
            Vec3 hitPosition,
            double distanceFromRay,
            double distanceFromPlayer
    ) {
    }
}
