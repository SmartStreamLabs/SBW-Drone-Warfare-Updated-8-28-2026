package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class DroneFiberOpticClient {
    private static final Map<UUID, FiberHudState> RENDER_STATES = new HashMap<>();
    private static final Map<UUID, FiberRouteClientState> ROUTE_STATES = new HashMap<>();
    private static final Map<UUID, FiberVisualState> VISUAL_STATES = new HashMap<>();

    private static FiberHudState state;
    private static ClientLevel lastClientLevel;
    private static boolean rendererCalled;
    private static boolean droneFoundClient;
    private static String renderWarning;

    private DroneFiberOpticClient() {
    }

    public static void init(net.neoforged.bus.api.IEventBus modBus) {
        modBus.addListener(DroneFiberOpticClient::onClientSetup);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(DroneFiberOpticClient.class);
    }

    public static void handleSync(DroneFiberOpticSyncMessage message) {
        if (message == null || message.droneId() == null) {
            return;
        }

        FiberHudState nextState = new FiberHudState(
                message.droneId(),
                message.droneEntityId(),
                message.linkMode(),
                message.fiberSessionActive(),
                message.cableLength(),
                message.maxCableLength(),
                message.cableTension(),
                message.spoolPercent(),
                message.segmentCount(),
                message.anchorX(),
                message.anchorY(),
                message.anchorZ()
        );

        if (nextState.linkMode() == DroneLinkMode.FIBER_OPTIC) {
            VISUAL_STATES.put(nextState.droneId(), new FiberVisualState(
                    nextState.droneId(),
                    nextState.droneEntityId(),
                    nextState.linkMode(),
                    nextState.spoolPercent()
            ));
        } else {
            VISUAL_STATES.remove(nextState.droneId());
        }

        if (nextState.linkMode() == DroneLinkMode.FIBER_OPTIC && nextState.fiberSessionActive()) {
            RENDER_STATES.put(nextState.droneId(), nextState);
        } else {
            RENDER_STATES.remove(nextState.droneId());
            ROUTE_STATES.remove(nextState.droneId());
        }

        UUID activeLinkedDroneId = activeLinkedDroneId();
        if (activeLinkedDroneId != null && activeLinkedDroneId.equals(nextState.droneId())) {
            state = nextState.fiberSessionActive() ? nextState : null;
        } else if (state != null && state.droneId().equals(nextState.droneId()) && !nextState.fiberSessionActive()) {
            state = null;
        }
    }

    public static void handleRouteSnapshot(FiberRouteSnapshotMessage message) {
        if (message == null || message.droneId() == null || message.sessionId() == null || message.points().isEmpty()) {
            return;
        }

        applyRouteSnapshot(
                ROUTE_STATES,
                message.droneId(),
                message.droneEntityId(),
                message.sessionId(),
                message.revision(),
                message.points(),
                clientGameTime()
        );
    }

    public static void handleRouteAppend(FiberRouteAppendMessage message) {
        if (message == null || message.droneId() == null || message.sessionId() == null || message.points().isEmpty()) {
            return;
        }

        applyRouteAppend(
                ROUTE_STATES,
                message.droneId(),
                message.droneEntityId(),
                message.sessionId(),
                message.previousRevision(),
                message.revision(),
                message.points(),
                clientGameTime()
        );
    }

    public static void handleRouteClear(FiberRouteClearMessage message) {
        if (message == null || message.droneId() == null || message.sessionId() == null) {
            return;
        }

        applyRouteClear(ROUTE_STATES, message.droneId(), message.sessionId(), message.revision(), "route_clear_packet");
    }

    public static void handleCutEffect(FiberCableCutEffectMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || message == null) {
            return;
        }

        Vec3 position = new Vec3(message.x(), message.y(), message.z());
        if (!FiberCableHitTest.isFinite(position)) {
            return;
        }

        for (int index = 0; index < 5; index++) {
            double dx = (minecraft.level.random.nextDouble() - 0.5D) * 0.035D;
            double dy = (minecraft.level.random.nextDouble() - 0.5D) * 0.035D;
            double dz = (minecraft.level.random.nextDouble() - 0.5D) * 0.035D;
            minecraft.level.addParticle(ParticleTypes.CRIT, position.x, position.y, position.z, dx, dy, dz);
        }

        FiberHudState currentState = currentState();
        if (minecraft.player != null && currentState != null && currentState.droneEntityId() == message.droneEntityId()) {
            minecraft.player.displayClientMessage(
                    Component.translatable("message.sbwdroneconfig.fiber_optic_cable_cut").withStyle(ChatFormatting.RED),
                    true
            );
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        rendererCalled = false;
        droneFoundClient = false;
        renderWarning = null;

        if (minecraft.player == null || minecraft.level == null) {
            clearClientCaches("client_disconnect_or_world_unload");
            state = null;
            lastClientLevel = null;
            return;
        }
        if (lastClientLevel != minecraft.level) {
            clearClientCaches("dimension_change");
            state = null;
            lastClientLevel = minecraft.level;
        }

        UUID activeLinkedDroneId = activeLinkedDroneId();
        state = activeLinkedDroneId != null ? RENDER_STATES.get(activeLinkedDroneId) : null;
        ROUTE_STATES.keySet().removeIf(droneId -> !RENDER_STATES.containsKey(droneId));
        pruneVisualStates(minecraft.level);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiEvent.Post event) {
        FiberHudState hudState = currentState();
        if (!shouldRenderHud(hudState)) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int x = 10;
        int y = 36;

        guiGraphics.drawString(minecraft.font, Component.translatable("overlay.sbwdroneconfig.link_fiber"), x, y, 0x8ED6FF, true);
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(
                        "overlay.sbwdroneconfig.fiber_cable",
                        formatCableLength(hudState.cableLength()),
                        hudState.maxCableLength()
                ),
                x,
                y + 12,
                0xD8F5FF,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(
                        "overlay.sbwdroneconfig.fiber_tension",
                        formatPercent(hudState.cableTension())
                ),
                x,
                y + 24,
                0xFFE28A,
                true
        );
        guiGraphics.drawString(
                minecraft.font,
                Component.translatable(
                        "overlay.sbwdroneconfig.fiber_spool",
                        hudState.spoolPercent()
                ),
                x,
                y + 36,
                hudState.spoolPercent() <= 20 ? 0xFF6B6B : 0xA8FF9E,
                true
        );

        if (AddonConfig.debugFiberCableRender()) {
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("fiber state active: " + hudState.fiberSessionActive()),
                    x,
                    y + 48,
                    hudState.fiberSessionActive() ? 0xA8FF9E : 0xFF9A9A,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("anchorPos: " + formatVec3(hudState.anchorX(), hudState.anchorY(), hudState.anchorZ())),
                    x,
                    y + 60,
                    0xD8F5FF,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("droneId: " + abbreviateUuid(hudState.droneId())),
                    x,
                    y + 72,
                    0xD8F5FF,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("droneFoundClient: " + droneFoundClient),
                    x,
                    y + 84,
                    droneFoundClient ? 0xA8FF9E : 0xFF9A9A,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("currentLength: " + formatCableLength(hudState.cableLength()) + " / " + hudState.maxCableLength()),
                    x,
                    y + 96,
                    0xD8F5FF,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("segmentCount: " + hudState.segmentCount()),
                    x,
                    y + 108,
                    0xD8F5FF,
                    true
            );
            FiberRouteClientState routeState = routeStateFor(hudState.droneId());
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("routePoints: " + (routeState == null ? 0 : routeState.points().size())),
                    x,
                    y + 120,
                    routeState == null ? 0xFF9A9A : 0xD8F5FF,
                    true
            );
            guiGraphics.drawString(
                    minecraft.font,
                    Component.literal("rendererCalled: " + rendererCalled),
                    x,
                    y + 132,
                    rendererCalled ? 0xA8FF9E : 0xFF9A9A,
                    true
            );

            if (renderWarning != null && !renderWarning.isBlank()) {
                guiGraphics.drawString(minecraft.font, Component.literal(renderWarning), x, y + 144, 0xFF6B6B, true);
            }
        }
    }

    private static boolean shouldRenderHud(FiberHudState hudState) {
        if (!AddonConfig.enableFiberOpticMode() || hudState == null || hudState.linkMode() != DroneLinkMode.FIBER_OPTIC) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !ModList.get().isLoaded(SbwDroneRangeConfig.SBW_MOD_ID)) {
            return false;
        }
        if (DroneClientViewContext.isActiveCubedFpvView()) {
            return false;
        }

        UUID linkedDroneId = activeLinkedDroneId();
        return linkedDroneId != null && linkedDroneId.equals(hudState.droneId());
    }

    private static UUID activeLinkedDroneId() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return null;
        }

        ItemStack activeMonitor = SbwCompat.getActiveLinkedMonitor(player);
        return activeMonitor.isEmpty() ? null : SbwCompat.getLinkedDroneUuid(activeMonitor);
    }

    private static String formatCableLength(double cableLength) {
        return String.format(Locale.ROOT, "%.1f", cableLength);
    }

    private static int formatPercent(float progress) {
        return Math.max(0, Math.min(100, Math.round(progress * 100.0F)));
    }

    static boolean hasSyncedFiberState() {
        FiberHudState currentState = currentState();
        return currentState != null && currentState.linkMode() == DroneLinkMode.FIBER_OPTIC && currentState.fiberSessionActive();
    }

    static boolean isFiberHudActiveFor(UUID droneId) {
        FiberHudState currentState = currentState();
        return droneId != null
                && currentState != null
                && currentState.linkMode() == DroneLinkMode.FIBER_OPTIC
                && currentState.fiberSessionActive()
                && droneId.equals(currentState.droneId());
    }

    static boolean hasFiberSpoolModule(Entity drone) {
        if (drone == null || !AddonConfig.enableFiberOpticMode()) {
            return false;
        }

        FiberVisualState visualState = VISUAL_STATES.get(drone.getUUID());
        if (visualState != null && visualState.droneEntityId() == drone.getId()) {
            return visualState.linkMode() == DroneLinkMode.FIBER_OPTIC;
        }

        return DroneModuleSystem.hasFiberOpticSpoolUpgrade(drone);
    }

    static FiberHudState currentState() {
        if (state != null) {
            return state;
        }

        UUID activeLinkedDroneId = activeLinkedDroneId();
        return activeLinkedDroneId != null ? RENDER_STATES.get(activeLinkedDroneId) : null;
    }

    static Collection<FiberHudState> renderStates() {
        return RENDER_STATES.values();
    }

    static FiberRouteClientState routeStateFor(UUID droneId) {
        return droneId == null ? null : ROUTE_STATES.get(droneId);
    }

    static List<Vec3> routePointsFor(FiberHudState fiberState) {
        FiberRouteClientState routeState = fiberState == null ? null : routeStateFor(fiberState.droneId());
        if (routeState == null || !routeState.active() || !routeState.droneId().equals(fiberState.droneId())) {
            return List.of();
        }
        return routeState.points();
    }

    static void updateRenderDebugState(boolean wasRendererCalled, boolean wasDroneFoundClient, String warning) {
        rendererCalled = wasRendererCalled;
        droneFoundClient = wasDroneFoundClient;
        renderWarning = warning;
    }

    static boolean applyRouteSnapshot(
            Map<UUID, FiberRouteClientState> cache,
            UUID droneId,
            int droneEntityId,
            UUID sessionId,
            int revision,
            List<FiberCableRouteState.RoutePoint> routePoints,
            long updateTime
    ) {
        if (cache == null || droneId == null || sessionId == null || routePoints == null || routePoints.isEmpty()) {
            return false;
        }

        List<Vec3> points = toVec3List(routePoints);
        if (points.isEmpty()) {
            return false;
        }

        cache.put(droneId, new FiberRouteClientState(
                droneId,
                droneEntityId,
                sessionId,
                revision,
                points,
                updateTime,
                true
        ));
        logClientRouteEvent("SNAPSHOT_RECEIVED", droneEntityId, sessionId, revision, points.size(), "snapshot");
        return true;
    }

    static boolean applyRouteAppend(
            Map<UUID, FiberRouteClientState> cache,
            UUID droneId,
            int droneEntityId,
            UUID sessionId,
            int previousRevision,
            int revision,
            List<FiberCableRouteState.RoutePoint> routePoints,
            long updateTime
    ) {
        if (cache == null || droneId == null || sessionId == null || routePoints == null || routePoints.isEmpty()) {
            return false;
        }

        FiberRouteClientState currentRoute = cache.get(droneId);
        if (currentRoute == null) {
            logClientRouteEvent("REVISION_REJECTED", droneEntityId, sessionId, revision, routePoints.size(), "missing_snapshot");
            return false;
        }
        if (!messageTargetsCurrentRoute(currentRoute, droneEntityId, sessionId)) {
            logClientRouteEvent("SESSION_REJECTED", droneEntityId, sessionId, revision, routePoints.size(), "session_or_entity_mismatch");
            return false;
        }
        if (previousRevision != currentRoute.revision() || revision <= currentRoute.revision()) {
            logClientRouteEvent("REVISION_REJECTED", droneEntityId, sessionId, revision, routePoints.size(), "revision_mismatch");
            return false;
        }

        List<Vec3> appendPoints = toVec3List(routePoints);
        if (appendPoints.isEmpty()) {
            return false;
        }

        List<Vec3> points = new ArrayList<>(currentRoute.points());
        points.addAll(appendPoints);
        points = clampClientRoutePoints(points);

        cache.put(droneId, new FiberRouteClientState(
                droneId,
                droneEntityId,
                sessionId,
                revision,
                points,
                updateTime,
                true
        ));
        logClientRouteEvent("ROUTE_APPEND", droneEntityId, sessionId, revision, appendPoints.size(), "append_received");
        return true;
    }

    static boolean applyRouteClear(Map<UUID, FiberRouteClientState> cache, UUID droneId, UUID sessionId, int revision, String reason) {
        if (cache == null || droneId == null || sessionId == null) {
            return false;
        }

        FiberRouteClientState currentRoute = cache.get(droneId);
        if (currentRoute == null) {
            return false;
        }
        if (!sessionId.equals(currentRoute.sessionId())) {
            logClientRouteEvent("SESSION_REJECTED", currentRoute.droneEntityId(), sessionId, revision, 0, "clear_old_session");
            return false;
        }
        if (revision < currentRoute.revision()) {
            logClientRouteEvent("REVISION_REJECTED", currentRoute.droneEntityId(), sessionId, revision, 0, "clear_old_revision");
            return false;
        }

        cache.remove(droneId);
        logClientRouteEvent("ROUTE_CLEAR", currentRoute.droneEntityId(), sessionId, revision, 0, reason);
        return true;
    }

    static void clearRouteCacheForTesting(Map<UUID, FiberRouteClientState> cache, String reason) {
        if (cache == null || cache.isEmpty()) {
            return;
        }
        cache.clear();
        logClientRouteEvent("CLIENT_CACHE_CLEAR", -1, FiberCableRouteState.EMPTY_SESSION, 0, 0, reason);
    }

    static Entity findActiveDroneEntity(ClientLevel level) {
        return findDroneEntity(level, currentState());
    }

    static Entity findDroneEntity(ClientLevel level, FiberHudState fiberState) {
        if (level == null || fiberState == null) {
            return null;
        }

        Entity byEntityId = level.getEntity(fiberState.droneEntityId());
        if (byEntityId != null && fiberState.droneId().equals(byEntityId.getUUID())) {
            return byEntityId;
        }

        return findEntity(level, fiberState.droneId());
    }

    private static Entity findDroneEntity(ClientLevel level, FiberVisualState visualState) {
        if (level == null || visualState == null) {
            return null;
        }

        Entity byEntityId = level.getEntity(visualState.droneEntityId());
        if (byEntityId != null && visualState.droneId().equals(byEntityId.getUUID())) {
            return byEntityId;
        }

        return findEntity(level, visualState.droneId());
    }

    private static Entity findEntity(ClientLevel level, UUID uuid) {
        for (Entity entity : level.entitiesForRendering()) {
            if (uuid.equals(entity.getUUID())) {
                return entity;
            }
        }
        return null;
    }

    private static void pruneVisualStates(ClientLevel level) {
        if (level == null || VISUAL_STATES.isEmpty()) {
            return;
        }

        VISUAL_STATES.entrySet().removeIf(entry -> {
            Entity entity = findDroneEntity(level, entry.getValue());
            return entity == null || !entry.getKey().equals(entity.getUUID());
        });
    }

    static record FiberHudState(
            UUID droneId,
            int droneEntityId,
            DroneLinkMode linkMode,
            boolean fiberSessionActive,
            double cableLength,
            int maxCableLength,
            float cableTension,
            int spoolPercent,
            int segmentCount,
            double anchorX,
            double anchorY,
            double anchorZ
    ) {
    }

    static record FiberVisualState(
            UUID droneId,
            int droneEntityId,
            DroneLinkMode linkMode,
            int spoolPercent
    ) {
    }

    static record FiberRouteClientState(
            UUID droneId,
            int droneEntityId,
            UUID sessionId,
            int revision,
            List<Vec3> points,
            long lastUpdateTime,
            boolean active
    ) {
        FiberRouteClientState {
            points = List.copyOf(points);
        }
    }

    private static List<Vec3> toVec3List(List<FiberCableRouteState.RoutePoint> routePoints) {
        List<Vec3> points = new ArrayList<>(routePoints.size());
        for (FiberCableRouteState.RoutePoint point : routePoints) {
            if (point != null && point.isFinite()) {
                points.add(point.toVec3());
            }
        }
        return points;
    }

    private static List<Vec3> clampClientRoutePoints(List<Vec3> points) {
        int maximumPoints = Math.max(1, AddonConfig.fiberRouteMaximumPoints());
        if (points.size() <= maximumPoints) {
            return points;
        }

        Vec3 anchor = points.get(0);
        List<Vec3> newestPoints = points.subList(points.size() - Math.max(1, maximumPoints - 1), points.size());
        List<Vec3> clamped = new ArrayList<>(maximumPoints);
        clamped.add(anchor);
        for (Vec3 point : newestPoints) {
            if (!clamped.get(clamped.size() - 1).closerThan(point, 0.02D)) {
                clamped.add(point);
            }
        }
        return clamped;
    }

    private static boolean messageTargetsCurrentRoute(FiberRouteClientState currentRoute, int droneEntityId, UUID sessionId) {
        return currentRoute != null
                && droneEntityId == currentRoute.droneEntityId()
                && sessionId.equals(currentRoute.sessionId());
    }

    private static void clearClientCaches(String reason) {
        RENDER_STATES.clear();
        VISUAL_STATES.clear();
        clearRouteCacheForTesting(ROUTE_STATES, reason);
    }

    private static void logClientRouteEvent(String event, int droneEntityId, UUID sessionId, int revision, int pointCount, String detail) {
        if (!AddonConfig.fiberRouteDebugLogging()) {
            return;
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Fiber route {} client droneEntityId={} session={} revision={} points={} detail={}",
                event,
                droneEntityId,
                sessionId,
                revision,
                pointCount,
                detail
        );
    }

    private static long clientGameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime();
    }

    private static String formatDebug(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String formatVec3(double x, double y, double z) {
        return formatDebug(x) + ", " + formatDebug(y) + ", " + formatDebug(z);
    }

    private static String abbreviateUuid(UUID uuid) {
        if (uuid == null) {
            return "none";
        }
        String full = uuid.toString();
        return full.length() <= 12 ? full : full.substring(0, 12);
    }
}
