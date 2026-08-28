package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Keeps the working FPV drone flow intact and layers the optional fiber optic
 * mode on top. Wireless drones stay unchanged; fiber drones keep cable tension
 * and spool state on the drone while the cable itself is rendered client-side.
 */
public final class FiberOpticLinkSystem {
    public static final String TAG_LINK_MODE = "sbwdroneconfigLinkMode";
    public static final String TAG_FIBER_SESSION_ACTIVE = "sbwdroneconfigFiberSessionActive";
    public static final String TAG_FIBER_CONTROLLER_UUID = "sbwdroneconfigFiberControllerUuid";
    public static final String TAG_FIBER_ANCHOR_X = "sbwdroneconfigFiberAnchorX";
    public static final String TAG_FIBER_ANCHOR_Y = "sbwdroneconfigFiberAnchorY";
    public static final String TAG_FIBER_ANCHOR_Z = "sbwdroneconfigFiberAnchorZ";
    public static final String TAG_FIBER_CABLE_LENGTH = "sbwdroneconfigFiberCableLength";
    public static final String TAG_FIBER_CABLE_TENSION = "sbwdroneconfigFiberCableTension";
    private static final String TAG_FORCE_DESCENT_AFTER_SEVER = "sbwdroneconfigFiberForceDescentAfterSever";
    private static final String TAG_LAST_DEBUG_TICK = "sbwdroneconfigFiberLastDebugTick";
    private static final String TAG_FIBER_SEGMENT_IDS = "sbwdroneconfigFiberSegmentIds";
    private static final String TAG_LAST_SEGMENT_SYNC_TICK = "sbwdroneconfigFiberLastSegmentSyncTick";
    private static final String TAG_LAST_CLIENT_SYNC_TICK = "sbwdroneconfigFiberLastClientSyncTick";
    private static final String TAG_LAST_TRACKING_SYNC_TICK = "sbwdroneconfigFiberLastTrackingSyncTick";
    private static final long DEBUG_LOG_INTERVAL_TICKS = 20L;
    private static final long CLIENT_SYNC_INTERVAL_TICKS = 5L;
    private static final long TRACKING_SYNC_INTERVAL_TICKS = 20L;
    private static final int SPOOL_DAMAGE_ON_BREAK = 1;

    private FiberOpticLinkSystem() {
    }

    public static void onDroneBaseTick(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone)) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        DroneLinkMode linkMode = getLinkMode(drone);
        data.putString(TAG_LINK_MODE, linkMode.name());

        if (linkMode != DroneLinkMode.FIBER_OPTIC) {
            clearSession(drone);
            return;
        }

        ServerPlayer controller = findActiveController(serverLevel, drone);
        boolean sessionActive = data.getBoolean(TAG_FIBER_SESSION_ACTIVE);
        if (controller == null && !shouldKeepSessionWhenControllerMissing(sessionActive, linkMode)) {
            clearSession(drone);
            return;
        }

        if (controller != null) {
            ensureSessionAnchor(drone, controller);
        }

        double cableLength = FiberOpticLinkMath.computeCableLength(
                data.getDouble(TAG_FIBER_ANCHOR_X),
                data.getDouble(TAG_FIBER_ANCHOR_Y),
                data.getDouble(TAG_FIBER_ANCHOR_Z),
                drone.getX(),
                drone.getY(),
                drone.getZ()
        );
        float tension = FiberOpticLinkMath.updateTension(
                data.getFloat(TAG_FIBER_CABLE_TENSION),
                cableLength,
                AddonConfig.fiberOpticCableLength(),
                AddonConfig.fiberOpticCableBreakDelayTicks()
        );

        data.putDouble(TAG_FIBER_CABLE_LENGTH, cableLength);
        data.putFloat(TAG_FIBER_CABLE_TENSION, tension);

        maybeLogDebug(drone, controller, cableLength, tension);
        discardLegacyCableSegments(serverLevel, drone);
        FiberCableRouteState.RouteSyncUpdate routeUpdate = FiberCableRouteState.ensureInitialized(
                drone,
                FiberOpticCableRenderMath.toRenderAnchor(new Vec3(
                        data.getDouble(TAG_FIBER_ANCHOR_X),
                        data.getDouble(TAG_FIBER_ANCHOR_Y),
                        data.getDouble(TAG_FIBER_ANCHOR_Z)
                ))
        );
        syncRouteUpdateForTrackingPlayers(serverLevel, drone, routeUpdate, true);
        syncRouteUpdateForTrackingPlayers(serverLevel, drone, FiberCableRouteState.appendIfNeeded(drone, resolveServerSpoolPoint(drone)), false);
        syncForTrackingPlayers(serverLevel, drone, false);

        if (tension >= 1.0F) {
            handleCableBreak(serverLevel, drone);
        }
    }

    public static void beforeDroneTravel(Entity drone) {
        if (drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        if (!data.getBoolean(TAG_FORCE_DESCENT_AFTER_SEVER)) {
            return;
        }

        invokeBooleanSetter(drone, "setLeftInputDown", false);
        invokeBooleanSetter(drone, "setRightInputDown", false);
        invokeBooleanSetter(drone, "setForwardInputDown", false);
        invokeBooleanSetter(drone, "setBackInputDown", false);
        invokeBooleanSetter(drone, "setUpInputDown", false);
        invokeBooleanSetter(drone, "setFireInputDown", false);
        invokeBooleanSetter(drone, "setDecoyInputDown", false);
        invokeBooleanSetter(drone, "setSprintInputDown", false);
        invokeFloatSetter(drone, "setMouseMoveSpeedX", 0.0F);
        invokeFloatSetter(drone, "setMouseMoveSpeedY", 0.0F);
        invokeFloatSetter(drone, "setPower", 0.0F);

        if (drone.onGround()) {
            data.remove(TAG_FORCE_DESCENT_AFTER_SEVER);
            invokeBooleanSetter(drone, "setDownInputDown", false);
            return;
        }

        invokeBooleanSetter(drone, "setDownInputDown", true);
        Vec3 currentMotion = drone.getDeltaMovement();
        drone.setDeltaMovement(
                currentMotion.x * 0.90D,
                Math.min(drone.getDeltaMovement().y, -0.12D),
                currentMotion.z * 0.90D
        );
        drone.hurtMarked = true;
    }

    public static void afterDroneTravel(Entity drone) {
        if (drone == null || drone.level().isClientSide() || !SbwCompat.isDrone(drone)) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        if (!data.getBoolean(TAG_FORCE_DESCENT_AFTER_SEVER)) {
            return;
        }

        if (drone.onGround()) {
            data.remove(TAG_FORCE_DESCENT_AFTER_SEVER);
            invokeBooleanSetter(drone, "setDownInputDown", false);
            invokeFloatSetter(drone, "setPower", 0.0F);
            return;
        }

        invokeBooleanSetter(drone, "setDownInputDown", true);
        invokeFloatSetter(drone, "setPower", 0.0F);
        Vec3 currentMotion = drone.getDeltaMovement();
        drone.setDeltaMovement(
                currentMotion.x * 0.80D,
                Math.min(currentMotion.y - 0.18D, -0.22D),
                currentMotion.z * 0.80D
        );
        drone.hurtMarked = true;
    }

    public static DroneLinkMode getLinkMode(Entity drone) {
        if (drone == null || !SbwCompat.isDrone(drone) || !AddonConfig.enableFiberOpticMode()) {
            return DroneLinkMode.WIRELESS;
        }

        return DroneModuleSystem.hasFiberOpticSpoolUpgrade(drone)
                ? DroneLinkMode.FIBER_OPTIC
                : DroneLinkMode.WIRELESS;
    }

    public static boolean isFiberOpticMode(Entity drone) {
        return getLinkMode(drone) == DroneLinkMode.FIBER_OPTIC;
    }

    public static boolean isFiberSessionActive(Entity drone) {
        return drone != null
                && SbwCompat.isDrone(drone)
                && drone.getPersistentData().getBoolean(TAG_FIBER_SESSION_ACTIVE)
                && getLinkMode(drone) == DroneLinkMode.FIBER_OPTIC;
    }

    public static boolean isImmuneToNormalJammers(Entity drone) {
        return isFiberOpticMode(drone) && !AddonConfig.normalJammersAffectFiberOptic();
    }

    public static boolean shouldAntiDroneNetCutFiber(Entity drone) {
        return drone != null
                && SbwCompat.isDrone(drone)
                && shouldAntiDroneNetCutFiber(getLinkMode(drone), AddonConfig.antiDroneNetCutsFiber());
    }

    public static boolean shouldAntiDroneNetCutFiber(DroneLinkMode linkMode, boolean antiDroneNetCutsFiber) {
        return antiDroneNetCutsFiber && linkMode == DroneLinkMode.FIBER_OPTIC;
    }

    static boolean shouldKeepSessionWhenControllerMissing(boolean sessionActive, DroneLinkMode linkMode) {
        return sessionActive && linkMode == DroneLinkMode.FIBER_OPTIC;
    }

    static boolean isProtectedCableBreaker(UUID attackerUuid, UUID ownerUuid, UUID controllerUuid) {
        return isProtectedCableBreaker(attackerUuid, ownerUuid, controllerUuid, null, null);
    }

    static boolean isProtectedCableBreaker(
            UUID attackerUuid,
            UUID ownerUuid,
            UUID controllerUuid,
            UUID storedFiberControllerUuid
    ) {
        return isProtectedCableBreaker(attackerUuid, ownerUuid, controllerUuid, storedFiberControllerUuid, null);
    }

    static boolean isProtectedCableBreaker(
            UUID attackerUuid,
            UUID ownerUuid,
            UUID controllerUuid,
            UUID storedFiberControllerUuid,
            UUID droneUuid
    ) {
        if (attackerUuid == null) {
            return false;
        }
        return attackerUuid.equals(ownerUuid)
                || attackerUuid.equals(controllerUuid)
                || attackerUuid.equals(storedFiberControllerUuid)
                || attackerUuid.equals(droneUuid);
    }

    static boolean isProtectedCableBreaker(ServerLevel level, Entity drone, Entity attacker) {
        if (level == null || drone == null || attacker == null) {
            return false;
        }

        SbwCompat.DroneOwnerIdentity ownerIdentity = SbwCompat.resolveDroneOwner(level, drone);
        UUID ownerUuid = ownerIdentity.hasUuid() ? ownerIdentity.uuid() : null;
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        UUID storedFiberControllerUuid = getStoredFiberControllerUuid(drone);
        return isProtectedCableBreaker(attacker.getUUID(), ownerUuid, controllerUuid, storedFiberControllerUuid, drone.getUUID());
    }

    static boolean isProtectedProjectileCableBreaker(ServerLevel level, Entity drone, Entity attacker) {
        if (level == null || drone == null || attacker == null) {
            return false;
        }

        if (attacker.getUUID().equals(drone.getUUID())) {
            return true;
        }

        if (attacker instanceof Player player) {
            return SbwCompat.isUsingLinkedMonitorForDrone(player, drone);
        }

        return isProtectedCableBreaker(level, drone, attacker);
    }

    static Entity resolveResponsibleCableAttacker(net.minecraft.world.damagesource.DamageSource source) {
        if (source == null) {
            return null;
        }

        Entity attacker = source.getEntity();
        if (attacker != null) {
            return attacker;
        }

        Entity directEntity = source.getDirectEntity();
        if (directEntity instanceof Projectile projectile && projectile.getOwner() != null) {
            return projectile.getOwner();
        }

        return directEntity;
    }

    public static int applyExtraBatteryDrain(Entity drone, int currentEnergyUse, boolean active) {
        if (!active || currentEnergyUse <= 0 || !isFiberOpticControlActive(drone)) {
            return currentEnergyUse;
        }

        return Math.max(
                currentEnergyUse,
                (int) Math.ceil(currentEnergyUse * AddonConfig.fiberOpticExtraBatteryDrainMultiplier())
        );
    }

    public static boolean isFiberOpticControlActive(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !isFiberOpticMode(drone)) {
            return false;
        }
        return findActiveController(serverLevel, drone) != null;
    }

    public static void syncForController(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }

        long gameTime = drone.level().getGameTime();
        CompoundTag data = drone.getPersistentData();
        long lastSyncTick = data.getLong(TAG_LAST_CLIENT_SYNC_TICK);
        if (gameTime - lastSyncTick < CLIENT_SYNC_INTERVAL_TICKS) {
            return;
        }

        data.putLong(TAG_LAST_CLIENT_SYNC_TICK, gameTime);
        sendFiberState(player, drone);
    }

    public static void syncForTrackingPlayer(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }

        sendFiberState(player, drone);
        sendRouteSnapshot(player, drone, FiberCableRouteState.snapshot(drone));
    }

    public static void syncVisualStateForTrackingPlayers(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel) || !SbwCompat.isDrone(drone) || !AddonConfig.enableFiberOpticMode()) {
            return;
        }

        syncForTrackingPlayers(serverLevel, drone, true);
    }

    public static void clearForTrackingPlayer(ServerPlayer player, Entity drone) {
        if (player == null || drone == null || !SbwCompat.isDrone(drone)) {
            return;
        }

        AddonNetwork.sendDroneFiberOptic(player, new DroneFiberOpticSyncMessage(
                drone.getUUID(),
                drone.getId(),
                getLinkMode(drone),
                false,
                0.0D,
                AddonConfig.fiberOpticCableLength(),
                0.0F,
                getSpoolPercent(drone),
                0,
                0.0D,
                0.0D,
                0.0D
        ));
        sendRouteClear(player, drone, new FiberCableRouteState.ClearData(
                FiberCableRouteState.sessionId(drone),
                FiberCableRouteState.revision(drone),
                FiberCableRouteState.isActive(drone)
        ));
    }

    private static void sendFiberState(ServerPlayer player, Entity drone) {
        AddonNetwork.sendDroneFiberOptic(player, new DroneFiberOpticSyncMessage(
                drone.getUUID(),
                drone.getId(),
                getLinkMode(drone),
                isFiberSessionActive(drone),
                getCurrentCableLength(drone),
                AddonConfig.fiberOpticCableLength(),
                getCurrentCableTension(drone),
                getSpoolPercent(drone),
                0,
                getAnchorX(drone),
                getAnchorY(drone),
                getAnchorZ(drone)
        ));
    }

    public static double getCurrentCableLength(Entity drone) {
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return 0.0D;
        }
        return drone.getPersistentData().getDouble(TAG_FIBER_CABLE_LENGTH);
    }

    public static float getCurrentCableTension(Entity drone) {
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return 0.0F;
        }
        return FiberOpticLinkMath.clampTension(drone.getPersistentData().getFloat(TAG_FIBER_CABLE_TENSION));
    }

    public static int getSpoolPercent(Entity drone) {
        if (drone == null || !SbwCompat.isDrone(drone)) {
            return 0;
        }

        DroneInventoryContainer inventory = new DroneInventoryContainer(drone);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !stack.is(AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get())) {
                continue;
            }

            if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
                return 100;
            }

            int maxDamage = stack.getMaxDamage();
            int remaining = Math.max(0, maxDamage - stack.getDamageValue());
            return Math.max(0, Math.min(100, Math.round((remaining * 100.0F) / maxDamage)));
        }

        return 0;
    }

    public static double getAnchorX(Entity drone) {
        return drone == null ? 0.0D : drone.getPersistentData().getDouble(TAG_FIBER_ANCHOR_X);
    }

    public static double getAnchorY(Entity drone) {
        return drone == null ? 0.0D : drone.getPersistentData().getDouble(TAG_FIBER_ANCHOR_Y);
    }

    public static double getAnchorZ(Entity drone) {
        return drone == null ? 0.0D : drone.getPersistentData().getDouble(TAG_FIBER_ANCHOR_Z);
    }

    static UUID getStoredFiberControllerUuid(Entity drone) {
        if (drone == null) {
            return null;
        }

        CompoundTag data = drone.getPersistentData();
        return data.hasUUID(TAG_FIBER_CONTROLLER_UUID) ? data.getUUID(TAG_FIBER_CONTROLLER_UUID) : null;
    }

    static boolean shouldProjectileFallbackBreak(double projectileMovementLengthSqr) {
        return projectileMovementLengthSqr >= 0.01D;
    }

    public static int getTrackedSegmentCount(Entity drone) {
        return 0;
    }

    public static long getLastClientSyncTick(Entity drone) {
        return drone == null ? 0L : drone.getPersistentData().getLong(TAG_LAST_CLIENT_SYNC_TICK);
    }

    public static String handleAntiDroneNetCut(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !shouldAntiDroneNetCutFiber(drone)) {
            return null;
        }

        boolean spoolDestroyed = damageInstalledSpool(drone);

        if (AddonConfig.debugFiberOptic()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Fiber optic cable cut by anti-drone net for drone={} spoolDestroyed={}",
                    drone.getUUID(),
                    spoolDestroyed
            );
        }

        DroneWaterDisconnectSystem.forceControlDisconnect(level, drone);
        clearSession(drone);
        drone.getPersistentData().putString(TAG_LINK_MODE, getLinkMode(drone).name());
        return spoolDestroyed
                ? "message.sbwdroneconfig.fiber_optic_spool_destroyed"
                : "message.sbwdroneconfig.fiber_optic_net_cut";
    }

    public static boolean cutFiberCableByShears(ServerLevel level, Entity drone, ServerPlayer cutter, Vec3 cutPosition) {
        if (level == null || drone == null || cutter == null || !isFiberSessionActive(drone) || !FiberCableHitTest.isFinite(cutPosition)) {
            return false;
        }

        UUID sessionId = FiberCableRouteState.sessionId(drone);
        if (AddonConfig.debugFiberOptic()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Fiber optic cable manually cut drone={} cutter={} session={} cutPos=[{}, {}, {}]",
                    drone.getUUID(),
                    cutter.getUUID(),
                    sessionId,
                    formatDebug(cutPosition.x),
                    formatDebug(cutPosition.y),
                    formatDebug(cutPosition.z)
            );
        }

        for (ServerPlayer player : routeTrackingPlayers(level, drone, true)) {
            AddonNetwork.sendFiberCableCutEffect(player, new FiberCableCutEffectMessage(
                    drone.getId(),
                    sessionId,
                    cutPosition.x,
                    cutPosition.y,
                    cutPosition.z
            ));
        }
        level.playSound(null, cutPosition.x, cutPosition.y, cutPosition.z, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.75F, 1.12F);
        notifyControllers(level, drone, "message.sbwdroneconfig.fiber_optic_cable_cut");
        DroneWaterDisconnectSystem.forceControlDisconnect(level, drone);
        drone.getPersistentData().putBoolean(TAG_FORCE_DESCENT_AFTER_SEVER, true);
        clearSession(drone);
        drone.getPersistentData().putString(TAG_LINK_MODE, getLinkMode(drone).name());
        return true;
    }

    public static void breakCableFromSegment(ServerLevel level, FiberOpticCableSegmentEntity segment) {
        if (level == null || segment == null) {
            return;
        }

        Entity drone = segment.getDroneUuid() != null ? level.getEntity(segment.getDroneUuid()) : null;
        if (drone == null || !SbwCompat.isDrone(drone)) {
            segment.discard();
            return;
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Fiber cable severed by damaged segment drone={} segment={} reason={}",
                drone.getUUID(),
                segment.getSegmentIndex(),
                segment.describeLastDamageContext()
        );

        disconnectFiberLink(level, drone, "message.sbwdroneconfig.fiber_optic_cable_severed", true);
    }

    public static void handleDroneCrashRecovery(ServerLevel level, Entity drone) {
        if (level == null || drone == null || !SbwCompat.isDrone(drone) || !AddonConfig.enableFiberOpticMode()) {
            return;
        }

        boolean hadActiveCable = isFiberSessionActive(drone) || FiberCableRouteState.isActive(drone);
        if (hadActiveCable && !AbandonedFiberCableManager.captureDroneCable(level, drone)) {
            if (AddonConfig.debugFiberOptic()) {
                SbwDroneRangeConfig.LOGGER.warn("Skipped fiber crash cleanup because abandoned cable capture failed for drone={}", drone.getUUID());
            }
            return;
        }

        ItemStack recoveredSpool = extractCrashRecoverySpool(drone);
        clearSession(drone);
        drone.getPersistentData().putString(TAG_LINK_MODE, getLinkMode(drone).name());
        if (recoveredSpool.isEmpty()) {
            return;
        }

        RecoverableFiberOpticCableEntity recoverableCable = AddonEntities.RECOVERABLE_FIBER_OPTIC_CABLE.get().create(level);
        if (recoverableCable == null) {
            drone.spawnAtLocation(recoveredSpool.copy());
            if (AddonConfig.debugFiberOptic()) {
                SbwDroneRangeConfig.LOGGER.info("Fiber crash recovery fell back to a raw item drop because the recoverable cable entity could not be created for drone={}", drone.getUUID());
            }
            return;
        }

        Vec3 recoveryPos = findCrashRecoveryGroundPosition(level, drone.position());
        recoverableCable.setRecoveredSpool(recoveredSpool);
        float yaw = level.random.nextFloat() * 360.0F;
        recoverableCable.moveTo(recoveryPos.x, recoveryPos.y, recoveryPos.z, yaw, 0.0F);
        recoverableCable.setYRot(yaw);
        recoverableCable.yRotO = recoverableCable.getYRot();
        if (!level.addFreshEntity(recoverableCable)) {
            drone.spawnAtLocation(recoveredSpool.copy());
            if (AddonConfig.debugFiberOptic()) {
                SbwDroneRangeConfig.LOGGER.info("Fiber crash recovery fell back to a raw item drop because the recoverable cable entity could not be added for drone={}", drone.getUUID());
            }
            return;
        }

        if (AddonConfig.debugFiberOptic()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Spawned recoverable fiber cable drone={} recoveryPos=[{}, {}, {}] spoolDamage={}/{}",
                    drone.getUUID(),
                    formatDebug(recoveryPos.x),
                    formatDebug(recoveryPos.y),
                    formatDebug(recoveryPos.z),
                    recoveredSpool.getDamageValue(),
                    recoveredSpool.getMaxDamage()
            );
        }
    }

    private static void ensureSessionAnchor(Entity drone, ServerPlayer controller) {
        CompoundTag data = drone.getPersistentData();
        UUID controllerUuid = controller.getUUID();
        boolean sessionActive = data.getBoolean(TAG_FIBER_SESSION_ACTIVE);
        boolean sameController = data.hasUUID(TAG_FIBER_CONTROLLER_UUID)
                && controllerUuid.equals(data.getUUID(TAG_FIBER_CONTROLLER_UUID));

        if (sessionActive && sameController) {
            return;
        }

        Vec3 anchor = FiberOpticCableRenderMath.resolveStoredAnchor(
                controller.position(),
                PlayerDroneAnchorManager.getCapturedOriginalPosition(controller)
        );

        data.putBoolean(TAG_FIBER_SESSION_ACTIVE, true);
        data.putUUID(TAG_FIBER_CONTROLLER_UUID, controllerUuid);
        data.putDouble(TAG_FIBER_ANCHOR_X, anchor.x);
        data.putDouble(TAG_FIBER_ANCHOR_Y, anchor.y);
        data.putDouble(TAG_FIBER_ANCHOR_Z, anchor.z);
        data.putDouble(TAG_FIBER_CABLE_LENGTH, 0.0D);
        data.putFloat(TAG_FIBER_CABLE_TENSION, 0.0F);
        data.remove(TAG_FORCE_DESCENT_AFTER_SEVER);

        if (AddonConfig.debugFiberOptic()) {
            SbwDroneRangeConfig.LOGGER.info(
                    "Fiber optic session anchored for drone={} controller={} launchAnchor=[{}, {}, {}] droneStart=[{}, {}, {}]",
                    drone.getUUID(),
                    controller.getGameProfile().getName(),
                    formatDebug(anchor.x),
                    formatDebug(anchor.y),
                    formatDebug(anchor.z),
                    formatDebug(drone.getX()),
                    formatDebug(drone.getY()),
                    formatDebug(drone.getZ())
            );
        }
    }

    private static void syncForTrackingPlayers(ServerLevel level, Entity drone, boolean force) {
        CompoundTag data = drone.getPersistentData();
        long gameTime = level.getGameTime();
        long lastSyncTick = data.getLong(TAG_LAST_TRACKING_SYNC_TICK);
        if (!force && gameTime - lastSyncTick < TRACKING_SYNC_INTERVAL_TICKS) {
            return;
        }

        data.putLong(TAG_LAST_TRACKING_SYNC_TICK, gameTime);
        double maxRenderDistance = AddonConfig.fiberCableMaxRenderDistance();
        double maxRenderDistanceSqr = maxRenderDistance * maxRenderDistance;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) {
                continue;
            }
            if (!force && player.distanceToSqr(drone) > maxRenderDistanceSqr) {
                continue;
            }

            sendFiberState(player, drone);
        }
    }

    private static void syncRouteUpdateForTrackingPlayers(
            ServerLevel level,
            Entity drone,
            FiberCableRouteState.RouteSyncUpdate update,
            boolean force
    ) {
        if (update == null || update.kind() == FiberCableRouteState.Kind.NONE) {
            return;
        }

        for (ServerPlayer player : routeTrackingPlayers(level, drone, force)) {
            if (update.kind() == FiberCableRouteState.Kind.SNAPSHOT) {
                AddonNetwork.sendFiberRouteSnapshot(player, new FiberRouteSnapshotMessage(
                        drone.getUUID(),
                        drone.getId(),
                        update.sessionId(),
                        update.revision(),
                        update.points()
                ));
                logRoutePacket("SNAPSHOT_SENT", drone, update.sessionId(), update.revision(), update.points().size(), update.reason());
            } else if (update.kind() == FiberCableRouteState.Kind.APPEND) {
                AddonNetwork.sendFiberRouteAppend(player, new FiberRouteAppendMessage(
                        drone.getUUID(),
                        drone.getId(),
                        update.sessionId(),
                        update.previousRevision(),
                        update.revision(),
                        update.points()
                ));
                logRoutePacket("ROUTE_APPEND", drone, update.sessionId(), update.revision(), update.points().size(), update.reason());
            }
        }
    }

    private static void syncRouteClearForTrackingPlayers(
            ServerLevel level,
            Entity drone,
            FiberCableRouteState.ClearData clearData,
            boolean force
    ) {
        for (ServerPlayer player : routeTrackingPlayers(level, drone, force)) {
            sendRouteClear(player, drone, clearData);
        }
    }

    private static void sendRouteSnapshot(ServerPlayer player, Entity drone, FiberCableRouteState.SnapshotData snapshot) {
        if (snapshot == null || snapshot.points().isEmpty()) {
            return;
        }

        AddonNetwork.sendFiberRouteSnapshot(player, new FiberRouteSnapshotMessage(
                drone.getUUID(),
                drone.getId(),
                snapshot.sessionId(),
                snapshot.revision(),
                snapshot.points()
        ));
        logRoutePacket("SNAPSHOT_SENT", drone, snapshot.sessionId(), snapshot.revision(), snapshot.points().size(), "tracking_snapshot");
    }

    private static void sendRouteClear(ServerPlayer player, Entity drone, FiberCableRouteState.ClearData clearData) {
        if (player == null || drone == null || clearData == null) {
            return;
        }

        AddonNetwork.sendFiberRouteClear(player, new FiberRouteClearMessage(
                drone.getUUID(),
                clearData.sessionId(),
                clearData.revision()
        ));
        logRoutePacket("ROUTE_CLEAR", drone, clearData.sessionId(), clearData.revision(), 0, "tracking_clear");
    }

    private static void logRoutePacket(String event, Entity drone, UUID sessionId, int revision, int pointCount, String detail) {
        if (!AddonConfig.fiberRouteDebugLogging()) {
            return;
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Fiber route {} droneEntityId={} session={} revision={} packetPoints={} detail={}",
                event,
                drone.getId(),
                sessionId,
                revision,
                pointCount,
                detail
        );
    }

    private static List<ServerPlayer> routeTrackingPlayers(ServerLevel level, Entity drone, boolean force) {
        List<ServerPlayer> players = new ArrayList<>();
        double maxRenderDistance = AddonConfig.fiberCableMaxRenderDistance();
        double maxRenderDistanceSqr = maxRenderDistance * maxRenderDistance;

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level) {
                continue;
            }
            if (!force && player.distanceToSqr(drone) > maxRenderDistanceSqr) {
                continue;
            }
            players.add(player);
        }

        return players;
    }

    static Vec3 resolveServerSpoolPoint(Entity drone) {
        if (drone instanceof CubedFpvDroneEntity cubedFpvDrone) {
            return FiberOpticCableRenderMath.toCubedFpvSpoolAttachment(cubedFpvDrone);
        }

        return FiberOpticCableRenderMath.toRenderEnd(drone.position(), drone.getYRot(), drone.getBbWidth(), drone.getBbHeight());
    }

    private static void maybeMaintainCableSegments(ServerLevel level, Entity drone) {
        discardLegacyCableSegments(level, drone);
    }

    private static void syncCableSegments(ServerLevel level, Entity drone) {
        discardLegacyCableSegments(level, drone);
    }

    private static void discardLegacyCableSegments(ServerLevel level, Entity drone) {
        removeCableSegments(drone);
    }

    private static List<FiberOpticCableSegmentEntity> resolveTrackedSegments(ServerLevel level, Entity drone) {
        List<FiberOpticCableSegmentEntity> segments = new ArrayList<>();
        for (UUID segmentId : readSegmentIds(drone)) {
            Entity entity = level.getEntity(segmentId);
            if (entity instanceof FiberOpticCableSegmentEntity cableSegment && cableSegment.isAlive()) {
                segments.add(cableSegment);
            }
        }
        return segments;
    }

    private static void removeCableSegments(Entity drone) {
        if (!(drone.level() instanceof ServerLevel serverLevel)) {
            drone.getPersistentData().remove(TAG_FIBER_SEGMENT_IDS);
            return;
        }

        for (UUID segmentId : readSegmentIds(drone)) {
            Entity entity = serverLevel.getEntity(segmentId);
            if (entity instanceof FiberOpticCableSegmentEntity cableSegment) {
                cableSegment.discard();
            }
        }
        drone.getPersistentData().remove(TAG_FIBER_SEGMENT_IDS);
        drone.getPersistentData().remove(TAG_LAST_SEGMENT_SYNC_TICK);
    }

    private static List<UUID> readSegmentIds(Entity drone) {
        CompoundTag data = drone.getPersistentData();
        if (!data.contains(TAG_FIBER_SEGMENT_IDS, Tag.TAG_LIST)) {
            return List.of();
        }

        ListTag listTag = data.getList(TAG_FIBER_SEGMENT_IDS, Tag.TAG_STRING);
        List<UUID> ids = new ArrayList<>(listTag.size());
        for (int index = 0; index < listTag.size(); index++) {
            try {
                ids.add(UUID.fromString(listTag.getString(index)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ids;
    }

    private static void writeSegmentIds(Entity drone, List<UUID> segmentIds) {
        CompoundTag data = drone.getPersistentData();
        if (segmentIds.isEmpty()) {
            data.remove(TAG_FIBER_SEGMENT_IDS);
            return;
        }

        ListTag listTag = new ListTag();
        for (UUID id : segmentIds) {
            listTag.add(StringTag.valueOf(id.toString()));
        }
        data.put(TAG_FIBER_SEGMENT_IDS, listTag);
    }

    private static void handleCableBreak(ServerLevel level, Entity drone) {
        if (AddonConfig.debugFiberOptic()) {
            SbwDroneRangeConfig.LOGGER.info("Fiber optic cable exceeded max tension for drone={}", drone.getUUID());
        }
        disconnectFiberLink(level, drone, "message.sbwdroneconfig.fiber_optic_cable_broken", true);
    }

    private static void disconnectFiberLink(ServerLevel level, Entity drone, String messageKey, boolean replaceWithSpoolDestroyedMessage) {
        boolean spoolDestroyed = damageInstalledSpool(drone);
        String finalMessageKey = spoolDestroyed && replaceWithSpoolDestroyedMessage
                ? "message.sbwdroneconfig.fiber_optic_spool_destroyed"
                : messageKey;

        notifyControllers(level, drone, finalMessageKey);
        DroneWaterDisconnectSystem.forceControlDisconnect(level, drone);
        drone.getPersistentData().putBoolean(TAG_FORCE_DESCENT_AFTER_SEVER, true);
        clearSession(drone);
        drone.getPersistentData().putString(TAG_LINK_MODE, getLinkMode(drone).name());
    }

    private static boolean damageInstalledSpool(Entity drone) {
        DroneInventoryContainer inventory = new DroneInventoryContainer(drone);

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !stack.is(AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get())) {
                continue;
            }

            if (!stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
                inventory.setItem(slot, ItemStack.EMPTY);
                inventory.saveToDrone();
                return true;
            }

            int nextDamage = stack.getDamageValue() + SPOOL_DAMAGE_ON_BREAK;
            if (nextDamage >= stack.getMaxDamage()) {
                inventory.setItem(slot, ItemStack.EMPTY);
                inventory.saveToDrone();
                return true;
            }

            stack.setDamageValue(nextDamage);
            inventory.setItem(slot, stack);
            inventory.saveToDrone();
            return false;
        }

        return false;
    }

    private static ItemStack extractCrashRecoverySpool(Entity drone) {
        DroneInventoryContainer inventory = new DroneInventoryContainer(drone);

        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !stack.is(AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get())) {
                continue;
            }

            ItemStack recoveredSpool = stack.copy();
            if (recoveredSpool.isDamageableItem() && recoveredSpool.getMaxDamage() > 0) {
                int recoveredDamage = Math.min(recoveredSpool.getMaxDamage() - 1, recoveredSpool.getDamageValue() + 1);
                recoveredSpool.setDamageValue(Math.max(0, recoveredDamage));
            }

            inventory.setItem(slot, ItemStack.EMPTY);
            inventory.saveToDrone();
            return recoveredSpool;
        }

        return ItemStack.EMPTY;
    }

    private static void clearSession(Entity drone) {
        removeCableSegments(drone);

        CompoundTag data = drone.getPersistentData();
        boolean wasActive = data.getBoolean(TAG_FIBER_SESSION_ACTIVE);
        FiberCableRouteState.ClearData routeClear = FiberCableRouteState.clear(drone);
        data.putBoolean(TAG_FIBER_SESSION_ACTIVE, false);
        data.remove(TAG_FIBER_CONTROLLER_UUID);
        data.remove(TAG_FIBER_ANCHOR_X);
        data.remove(TAG_FIBER_ANCHOR_Y);
        data.remove(TAG_FIBER_ANCHOR_Z);
        data.putDouble(TAG_FIBER_CABLE_LENGTH, 0.0D);
        data.putFloat(TAG_FIBER_CABLE_TENSION, 0.0F);

        if ((wasActive || routeClear.wasActive()) && drone.level() instanceof ServerLevel serverLevel) {
            syncRouteClearForTrackingPlayers(serverLevel, drone, routeClear, true);
            syncForTrackingPlayers(serverLevel, drone, true);
        }
    }

    private static Vec3 findCrashRecoveryGroundPosition(ServerLevel level, Vec3 crashPos) {
        BlockPos.MutableBlockPos cursor = BlockPos.containing(crashPos).mutable();
        int minY = level.getMinBuildHeight();

        for (int y = cursor.getY(); y >= minY; y--) {
            cursor.set(crashPos.x, y, crashPos.z);
            VoxelShape shape = level.getBlockState(cursor).getCollisionShape(level, cursor);
            if (shape.isEmpty()) {
                continue;
            }

            double top = cursor.getY() + shape.max(Direction.Axis.Y);
            return new Vec3(crashPos.x, top + 0.05D, crashPos.z);
        }

        return new Vec3(crashPos.x, Math.max(crashPos.y, minY + 1.0D), crashPos.z);
    }

    private static ServerPlayer findActiveController(ServerLevel level, Entity drone) {
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        if (controllerUuid != null) {
            ServerPlayer directController = level.getServer().getPlayerList().getPlayer(controllerUuid);
            if (directController != null && SbwCompat.isUsingLinkedMonitorForDrone(directController, drone)) {
                return directController;
            }
        }

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
                return player;
            }
        }

        return null;
    }

    private static void notifyControllers(ServerLevel level, Entity drone, String messageKey) {
        UUID controllerUuid = SbwCompat.getDroneControllerUuid(drone);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if ((controllerUuid != null && controllerUuid.equals(player.getUUID()))
                    || SbwCompat.isUsingLinkedMonitorForDrone(player, drone)) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(messageKey).withStyle(ChatFormatting.RED),
                        true
                );
            }
        }
    }

    private static void maybeLogDebug(Entity drone, ServerPlayer controller, double cableLength, float tension) {
        if (!AddonConfig.debugFiberOptic()) {
            return;
        }

        CompoundTag data = drone.getPersistentData();
        long gameTime = drone.level().getGameTime();
        long lastDebugTick = data.getLong(TAG_LAST_DEBUG_TICK);
        if (gameTime - lastDebugTick < DEBUG_LOG_INTERVAL_TICKS) {
            return;
        }

        data.putLong(TAG_LAST_DEBUG_TICK, gameTime);
        SbwDroneRangeConfig.LOGGER.info(
                "Fiber optic link drone={} controller={} anchorPos=[{}, {}, {}] dronePos=[{}, {}, {}] cableLength={} maxLength={} tension={} linkMode={}",
                drone.getUUID(),
                controller.getGameProfile().getName(),
                formatDebug(getAnchorX(drone)),
                formatDebug(getAnchorY(drone)),
                formatDebug(getAnchorZ(drone)),
                formatDebug(drone.getX()),
                formatDebug(drone.getY()),
                formatDebug(drone.getZ()),
                formatDebug(cableLength),
                AddonConfig.fiberOpticCableLength(),
                String.format(Locale.ROOT, "%.3f", tension),
                getLinkMode(drone)
        );
    }

    private static void maybeLogSegmentDebug(Entity drone, Vec3 renderStart, Vec3 renderEnd, int segmentCount) {
        if (!AddonConfig.debugFiberCableSegments()) {
            return;
        }

        SbwDroneRangeConfig.LOGGER.info(
                "Fiber cable segments drone={} anchorPos=[{}, {}, {}] renderEnd=[{}, {}, {}] currentLength={} segments={}",
                drone.getUUID(),
                formatDebug(renderStart.x),
                formatDebug(renderStart.y),
                formatDebug(renderStart.z),
                formatDebug(renderEnd.x),
                formatDebug(renderEnd.y),
                formatDebug(renderEnd.z),
                formatDebug(getCurrentCableLength(drone)),
                segmentCount
        );
    }

    private static void checkProjectileBreaks(ServerLevel level, Entity drone, List<FiberOpticCableSegmentEntity> segments) {
        if (!AddonConfig.fiberCableProjectileRaycastBreak() || segments.isEmpty()) {
            return;
        }

        AABB searchBounds = null;
        for (FiberOpticCableSegmentEntity segment : segments) {
            AABB segmentBox = segment.getBoundingBox();
            searchBounds = searchBounds == null ? segmentBox : searchBounds.minmax(segmentBox);
        }

        if (searchBounds == null) {
            return;
        }

        List<Projectile> projectiles = level.getEntitiesOfClass(
                Projectile.class,
                searchBounds.inflate(1.0D),
                projectile -> projectile != null
                        && projectile.isAlive()
                        && shouldProjectileFallbackBreak(projectile.getDeltaMovement().lengthSqr())
        );

        for (Projectile projectile : projectiles) {
            Entity responsibleAttacker = projectile.getOwner() != null ? projectile.getOwner() : projectile;
            if (isProtectedProjectileCableBreaker(level, drone, responsibleAttacker)) {
                continue;
            }

            AABB projectilePathBox = projectile.getBoundingBox().expandTowards(projectile.getDeltaMovement()).inflate(0.10D);
            for (FiberOpticCableSegmentEntity segment : segments) {
                if (!projectilePathBox.intersects(segment.getBoundingBox())) {
                    continue;
                }

                if (AddonConfig.debugFiberCableSegments()) {
                    SbwDroneRangeConfig.LOGGER.info(
                            "Fiber cable severed by projectile fallback drone={} projectile={} segment={}",
                            drone.getUUID(),
                            projectile.getUUID(),
                            segment.getSegmentIndex()
                    );
                }
                SbwDroneRangeConfig.LOGGER.info(
                        "Fiber cable severed by projectile fallback drone={} projectile={} type={} owner={} speedSqr={}",
                        drone.getUUID(),
                        projectile.getUUID(),
                        projectile.getType(),
                        projectile.getOwner() != null ? projectile.getOwner().getUUID() : "none",
                        String.format(Locale.ROOT, "%.5f", projectile.getDeltaMovement().lengthSqr())
                );
                breakCableFromSegment(level, segment);
                return;
            }
        }
    }

    private static String formatDebug(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static void invokeBooleanSetter(Entity entity, String methodName, boolean value) {
        try {
            Method method = entity.getClass().getMethod(methodName, boolean.class);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static void invokeFloatSetter(Entity entity, String methodName, float value) {
        try {
            Method method = entity.getClass().getMethod(methodName, float.class);
            method.invoke(entity, value);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
