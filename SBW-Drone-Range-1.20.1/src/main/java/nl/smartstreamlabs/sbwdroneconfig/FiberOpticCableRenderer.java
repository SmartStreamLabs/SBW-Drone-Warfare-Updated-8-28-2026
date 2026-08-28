package nl.smartstreamlabs.sbwdroneconfig;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class FiberOpticCableRenderer {
    private static final double NEAR_CAMERA_MIN_DISTANCE = 0.25D;
    private static final double NEAR_CAMERA_FADE_END = 0.75D;
    private static final int MAIN_CHARCOAL = 0x1B1D20;
    private static final int EDGE_CHARCOAL = 0x454A4F;
    private static final long DEBUG_INTERVAL_TICKS = 20L;

    private static long lastDebugTick = Long.MIN_VALUE;

    private FiberOpticCableRenderer() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(FiberOpticCableRenderer::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(FiberOpticCableRenderer.class);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null) {
            return;
        }

        Collection<DroneFiberOpticClient.FiberHudState> states = DroneFiberOpticClient.renderStates();
        if (states.isEmpty()) {
            DroneFiberOpticClient.updateRenderDebugState(true, false, "No client fiber state found.");
            return;
        }

        Vec3 cameraPos = event.getCamera().getPosition();
        double maxRenderDistance = AddonConfig.fiberCableMaxRenderDistance();
        int maxVisibleCables = AddonConfig.maxVisibleFiberCables();
        CableRenderStyle style = resolveCableRenderStyle();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer consumer = buffers.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

        int renderedCables = 0;
        Set<UUID> renderedDroneIds = new HashSet<>();
        for (DroneFiberOpticClient.FiberHudState state : states) {
            if (renderedCables >= maxVisibleCables) {
                break;
            }
            if (state == null || state.linkMode() != DroneLinkMode.FIBER_OPTIC || !state.fiberSessionActive()) {
                continue;
            }

            Entity entity = DroneFiberOpticClient.findDroneEntity(level, state);
            if (!(entity instanceof CubedFpvDroneEntity drone) || !drone.isAlive()) {
                continue;
            }
            if (!renderedDroneIds.add(drone.getUUID())) {
                continue;
            }

            float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(true);
            Vec3 dynamicSpoolPoint = FiberOpticCableRenderMath.toCubedFpvSpoolAttachment(drone, partialTicks);
            List<Vec3> routePoints = resolveRoutePoints(state, drone, dynamicSpoolPoint);
            if (!isRouteInRenderRange(cameraPos, routePoints, dynamicSpoolPoint, maxRenderDistance)) {
                continue;
            }

            int sampleCount = computeSampleBudget(cameraPos, routePoints, dynamicSpoolPoint);
            Vec3[] cablePoints = FiberOpticCableRenderMath.buildRouteCurvePoints(
                    routePoints,
                    dynamicSpoolPoint,
                    sampleCount,
                    AddonConfig.fiberRouteCornerSmoothing()
            );
            renderCableRibbon(consumer, matrix, level, cameraPos, cablePoints, style, maxRenderDistance);
            renderedCables++;

            maybeLogDebug(level, state, drone, cameraPos, routePoints, dynamicSpoolPoint, sampleCount);
        }

        poseStack.popPose();
        buffers.endBatch(RenderType.leash());

        DroneFiberOpticClient.updateRenderDebugState(
                true,
                renderedCables > 0,
                renderedCables > 0 ? null : "No visible Cubed FPV fiber cable."
        );
    }

    private static List<Vec3> resolveRoutePoints(DroneFiberOpticClient.FiberHudState state, CubedFpvDroneEntity drone, Vec3 dynamicSpoolPoint) {
        List<Vec3> routePoints = DroneFiberOpticClient.routePointsFor(state);
        if (!routePoints.isEmpty()) {
            return trimDroneEndRoutePoints(routePoints, drone, dynamicSpoolPoint);
        }
        return List.of(FiberOpticCableRenderMath.toRenderAnchor(new Vec3(state.anchorX(), state.anchorY(), state.anchorZ())));
    }

    private static List<Vec3> trimDroneEndRoutePoints(List<Vec3> routePoints, CubedFpvDroneEntity drone, Vec3 dynamicSpoolPoint) {
        if (routePoints == null || routePoints.isEmpty()) {
            return List.of();
        }

        List<Vec3> sanitized = new ArrayList<>(routePoints.size());
        for (Vec3 point : routePoints) {
            if (FiberOpticCableRenderMath.isFinite(point)
                    && (sanitized.isEmpty() || !sanitized.get(sanitized.size() - 1).closerThan(point, 0.02D))) {
                sanitized.add(point);
            }
        }

        if (sanitized.size() <= 1 || drone == null || !FiberOpticCableRenderMath.isFinite(dynamicSpoolPoint)) {
            return sanitized;
        }

        double droneEndClearance = Math.max(0.35D, Math.max(drone.getBbWidth(), drone.getBbHeight()) * 0.60D);
        double droneEndClearanceSqr = droneEndClearance * droneEndClearance;
        while (sanitized.size() > 1) {
            Vec3 last = sanitized.get(sanitized.size() - 1);
            boolean staleNearConnector = last.distanceToSqr(dynamicSpoolPoint) <= droneEndClearanceSqr;
            boolean staleInsideDrone = drone.getBoundingBox().inflate(0.18D).contains(last);
            if (!staleNearConnector && !staleInsideDrone) {
                break;
            }
            sanitized.remove(sanitized.size() - 1);
        }

        return sanitized;
    }

    private static boolean isRouteInRenderRange(Vec3 cameraPos, List<Vec3> routePoints, Vec3 dynamicEnd, double maxRenderDistance) {
        if (routePoints.isEmpty()) {
            return cameraPos.distanceToSqr(dynamicEnd) <= maxRenderDistance * maxRenderDistance;
        }

        for (int index = 0; index < routePoints.size(); index++) {
            Vec3 start = routePoints.get(index);
            Vec3 end = index + 1 < routePoints.size() ? routePoints.get(index + 1) : dynamicEnd;
            if (isSegmentInRenderRange(cameraPos, start, end, maxRenderDistance)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSegmentInRenderRange(Vec3 cameraPos, Vec3 start, Vec3 end, double maxRenderDistance) {
        double maxDistanceSqr = maxRenderDistance * maxRenderDistance;
        Vec3 midpoint = start.lerp(end, 0.5D);
        return cameraPos.distanceToSqr(start) <= maxDistanceSqr
                || cameraPos.distanceToSqr(end) <= maxDistanceSqr
                || cameraPos.distanceToSqr(midpoint) <= maxDistanceSqr;
    }

    private static int computeSampleBudget(Vec3 cameraPos, List<Vec3> routePoints, Vec3 dynamicEnd) {
        double nearestDistance = cameraPos.distanceTo(dynamicEnd);
        for (Vec3 point : routePoints) {
            nearestDistance = Math.min(nearestDistance, cameraPos.distanceTo(point));
        }
        if (nearestDistance <= 32.0D) {
            return 96;
        }
        if (nearestDistance <= 96.0D) {
            return 64;
        }
        return 32;
    }

    static boolean renderStaticCable(
            VertexConsumer consumer,
            Matrix4f matrix,
            ClientLevel level,
            Vec3 cameraPos,
            List<Vec3> orderedPoints,
            double maxRenderDistance
    ) {
        List<Vec3> sanitized = sanitizeStaticCablePoints(orderedPoints);
        if (sanitized.size() < 2) {
            return false;
        }

        Vec3 staticEnd = sanitized.get(sanitized.size() - 1);
        List<Vec3> routePoints = sanitized.subList(0, sanitized.size() - 1);
        if (!isRouteInRenderRange(cameraPos, routePoints, staticEnd, maxRenderDistance)) {
            return false;
        }

        int sampleCount = computeSampleBudget(cameraPos, routePoints, staticEnd);
        Vec3[] cablePoints = FiberOpticCableRenderMath.buildRouteCurvePoints(
                routePoints,
                staticEnd,
                sampleCount,
                AddonConfig.fiberRouteCornerSmoothing()
        );
        renderCableRibbon(consumer, matrix, level, cameraPos, cablePoints, resolveCableRenderStyle(), maxRenderDistance);
        return true;
    }

    private static List<Vec3> sanitizeStaticCablePoints(List<Vec3> orderedPoints) {
        if (orderedPoints == null || orderedPoints.isEmpty()) {
            return List.of();
        }

        List<Vec3> sanitized = new ArrayList<>();
        for (Vec3 point : orderedPoints) {
            if (FiberOpticCableRenderMath.isFinite(point)
                    && (sanitized.isEmpty() || !sanitized.get(sanitized.size() - 1).closerThan(point, 0.02D))) {
                sanitized.add(point);
            }
        }
        return sanitized;
    }

    private static void renderCableRibbon(
            VertexConsumer consumer,
            Matrix4f matrix,
            ClientLevel level,
            Vec3 cameraPos,
            Vec3[] cablePoints,
            CableRenderStyle style,
            double maxRenderDistance
    ) {
        Vec3 previousOffset = null;
        for (int index = 0; index < cablePoints.length - 1; index++) {
            Vec3 start = clampNearCamera(cablePoints[index], cameraPos);
            Vec3 end = clampNearCamera(cablePoints[index + 1], cameraPos);
            if (!isSegmentInRenderRange(cameraPos, start, end, maxRenderDistance)) {
                continue;
            }
            Vec3 delta = end.subtract(start);
            double length = delta.length();
            if (length <= 1.0E-6D) {
                continue;
            }

            Vec3 direction = delta.scale(1.0D / length);
            Vec3 midpoint = start.lerp(end, 0.5D);
            double cameraDistance = Math.max(0.0D, cameraPos.distanceTo(midpoint));
            double width = adjustedWidth(style.width(), cameraDistance);
            int alpha = adjustedAlpha(style.alpha(), cameraDistance);
            Vec3 offset = stableRibbonOffset(direction, cameraPos.subtract(midpoint), width * 0.5D, previousOffset);
            if (!FiberOpticCableRenderMath.isFinite(offset) || offset.lengthSqr() <= 1.0E-12D) {
                continue;
            }
            previousOffset = offset;
            int packedLight = LevelRenderer.getLightColor(level, BlockPos.containing(midpoint));

            renderRibbonQuad(consumer, matrix, start, end, offset, style, alpha, packedLight);
        }
    }

    private static Vec3 clampNearCamera(Vec3 point, Vec3 cameraPos) {
        Vec3 fromCamera = point.subtract(cameraPos);
        double distance = fromCamera.length();
        if (distance >= NEAR_CAMERA_MIN_DISTANCE) {
            return point;
        }

        Vec3 direction = distance > 1.0E-6D ? fromCamera.scale(1.0D / distance) : new Vec3(0.0D, 1.0D, 0.0D);
        return cameraPos.add(direction.scale(NEAR_CAMERA_MIN_DISTANCE));
    }

    private static double adjustedWidth(double baseWidth, double cameraDistance) {
        if (cameraDistance >= NEAR_CAMERA_FADE_END) {
            return baseWidth;
        }

        double progress = Mth.clamp((cameraDistance - NEAR_CAMERA_MIN_DISTANCE) / (NEAR_CAMERA_FADE_END - NEAR_CAMERA_MIN_DISTANCE), 0.0D, 1.0D);
        return baseWidth * Mth.lerp(progress, 0.35D, 1.0D);
    }

    private static int adjustedAlpha(int baseAlpha, double cameraDistance) {
        if (cameraDistance >= NEAR_CAMERA_FADE_END) {
            return baseAlpha;
        }

        double progress = Mth.clamp((cameraDistance - NEAR_CAMERA_MIN_DISTANCE) / (NEAR_CAMERA_FADE_END - NEAR_CAMERA_MIN_DISTANCE), 0.0D, 1.0D);
        return Mth.clamp((int) Math.round(baseAlpha * Mth.lerp(progress, 0.35D, 1.0D)), 0, 255);
    }

    static Vec3 stableRibbonOffset(Vec3 direction, Vec3 reference, double halfWidth, Vec3 previousOffset) {
        if (!FiberOpticCableRenderMath.isFinite(direction) || !FiberOpticCableRenderMath.isFinite(reference) || !Double.isFinite(halfWidth)) {
            return previousOffset != null ? previousOffset : Vec3.ZERO;
        }

        Vec3 normalizedDirection = safeNormalize(direction, new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 side = normalizedDirection.cross(reference);
        if (side.lengthSqr() <= 1.0E-8D) {
            side = normalizedDirection.cross(new Vec3(0.0D, 1.0D, 0.0D));
        }
        if (side.lengthSqr() <= 1.0E-8D) {
            side = normalizedDirection.cross(new Vec3(1.0D, 0.0D, 0.0D));
        }

        Vec3 offset = safeNormalize(side, previousOffset != null ? previousOffset : new Vec3(1.0D, 0.0D, 0.0D))
                .scale(Math.max(0.0D, halfWidth));
        if (previousOffset != null && offset.dot(previousOffset) < 0.0D) {
            offset = offset.scale(-1.0D);
        }
        return offset;
    }

    private static Vec3 safeNormalize(Vec3 vector, Vec3 fallback) {
        if (!FiberOpticCableRenderMath.isFinite(vector) || vector.lengthSqr() <= 1.0E-12D) {
            return fallback;
        }
        Vec3 normalized = vector.normalize();
        return FiberOpticCableRenderMath.isFinite(normalized) ? normalized : fallback;
    }

    private static void renderRibbonQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            Vec3 offset,
            CableRenderStyle style,
            int alpha,
            int packedLight
    ) {
        Vec3 startEdge = start.subtract(offset);
        Vec3 startMain = start.add(offset);
        Vec3 endMain = end.add(offset);
        Vec3 endEdge = end.subtract(offset);

        putVertex(consumer, matrix, startEdge, style.edgeRed(), style.edgeGreen(), style.edgeBlue(), alpha, packedLight);
        putVertex(consumer, matrix, startMain, style.mainRed(), style.mainGreen(), style.mainBlue(), alpha, packedLight);
        putVertex(consumer, matrix, endMain, style.mainRed(), style.mainGreen(), style.mainBlue(), alpha, packedLight);
        putVertex(consumer, matrix, endEdge, style.edgeRed(), style.edgeGreen(), style.edgeBlue(), alpha, packedLight);

        putVertex(consumer, matrix, endEdge, style.edgeRed(), style.edgeGreen(), style.edgeBlue(), alpha, packedLight);
        putVertex(consumer, matrix, endMain, style.mainRed(), style.mainGreen(), style.mainBlue(), alpha, packedLight);
        putVertex(consumer, matrix, startMain, style.mainRed(), style.mainGreen(), style.mainBlue(), alpha, packedLight);
        putVertex(consumer, matrix, startEdge, style.edgeRed(), style.edgeGreen(), style.edgeBlue(), alpha, packedLight);
    }

    private static void putVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 point, int red, int green, int blue, int alpha, int packedLight) {
        consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
                .setColor(red, green, blue, alpha)
                .setLight(packedLight);
    }

    private static CableRenderStyle resolveCableRenderStyle() {
        double width = AddonConfig.debugFiberCableRender()
                ? AddonConfig.fiberCableDebugVisualThickness()
                : AddonConfig.fiberCableVisualThickness();
        int alpha = Mth.clamp((int) Math.round(AddonConfig.fiberCableVisualAlpha() * 255.0D), 0, 255);

        if (AddonConfig.debugFiberCableRender() && AddonConfig.renderRedDebugCable()) {
            return new CableRenderStyle(width, 230, 48, 48, 160, 32, 32, Math.max(alpha, 200));
        }

        return switch (AddonConfig.fiberCableVisualColor()) {
            case "off_white" -> fromPacked(width, 0xD2D2C8, 0x8A8C84, alpha);
            case "pale_gray" -> fromPacked(width, 0xB4B4AA, 0x74776F, alpha);
            case "light_gray" -> fromPacked(width, 0xA2A49B, 0x5F645E, alpha);
            default -> fromPacked(width, MAIN_CHARCOAL, EDGE_CHARCOAL, alpha);
        };
    }

    private static CableRenderStyle fromPacked(double width, int mainColor, int edgeColor, int alpha) {
        return new CableRenderStyle(
                width,
                (mainColor >> 16) & 0xFF,
                (mainColor >> 8) & 0xFF,
                mainColor & 0xFF,
                (edgeColor >> 16) & 0xFF,
                (edgeColor >> 8) & 0xFF,
                edgeColor & 0xFF,
                alpha
        );
    }

    private static void maybeLogDebug(
            ClientLevel level,
            DroneFiberOpticClient.FiberHudState state,
            CubedFpvDroneEntity drone,
            Vec3 cameraPos,
            List<Vec3> routePoints,
            Vec3 dynamicSpoolPoint,
            int sampleCount
    ) {
        if (!AddonConfig.debugFiberCableRender()) {
            return;
        }

        long gameTime = level.getGameTime();
        if (gameTime - lastDebugTick < DEBUG_INTERVAL_TICKS) {
            return;
        }

        lastDebugTick = gameTime;
        SbwDroneRangeConfig.LOGGER.info(
                "Fiber cable renderer drone={} anchorPos=[{}, {}, {}] routePoints={} dynamicSpool=[{}, {}, {}] cameraPos=[{}, {}, {}] currentLength={} samples={} width={} renderDistance={} visibleLimit={}",
                drone.getUUID(),
                format(state.anchorX()),
                format(state.anchorY()),
                format(state.anchorZ()),
                routePoints.size(),
                format(dynamicSpoolPoint.x),
                format(dynamicSpoolPoint.y),
                format(dynamicSpoolPoint.z),
                format(cameraPos.x),
                format(cameraPos.y),
                format(cameraPos.z),
                format(state.cableLength()),
                sampleCount,
                format(AddonConfig.fiberCableVisualThickness()),
                format(AddonConfig.fiberCableMaxRenderDistance()),
                AddonConfig.maxVisibleFiberCables()
        );
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record CableRenderStyle(
            double width,
            int mainRed,
            int mainGreen,
            int mainBlue,
            int edgeRed,
            int edgeGreen,
            int edgeBlue,
            int alpha
    ) {
    }
}
