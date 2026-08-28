package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class AddonNetwork {
    private static final String PROTOCOL_VERSION = "20";
    private static boolean registered;

    private AddonNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        if (registered) {
            return;
        }
        registered = true;
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(DroneJamSyncMessage.TYPE, codec(DroneJamSyncMessage::encode, DroneJamSyncMessage::decode), DroneJamSyncMessage::handle);
        registrar.playToClient(DroneEnergySyncMessage.TYPE, codec(DroneEnergySyncMessage::encode, DroneEnergySyncMessage::decode), DroneEnergySyncMessage::handle);
        registrar.playToClient(LucasFuelSyncMessage.TYPE, codec(LucasFuelSyncMessage::encode, LucasFuelSyncMessage::decode), LucasFuelSyncMessage::handle);
        registrar.playToClient(DroneFiberOpticSyncMessage.TYPE, codec(DroneFiberOpticSyncMessage::encode, DroneFiberOpticSyncMessage::decode), DroneFiberOpticSyncMessage::handle);
        registrar.playToClient(FiberRouteSnapshotMessage.TYPE, codec(FiberRouteSnapshotMessage::encode, FiberRouteSnapshotMessage::decode), FiberRouteSnapshotMessage::handle);
        registrar.playToClient(FiberRouteAppendMessage.TYPE, codec(FiberRouteAppendMessage::encode, FiberRouteAppendMessage::decode), FiberRouteAppendMessage::handle);
        registrar.playToClient(FiberRouteClearMessage.TYPE, codec(FiberRouteClearMessage::encode, FiberRouteClearMessage::decode), FiberRouteClearMessage::handle);
        registrar.playToClient(FiberCableCutEffectMessage.TYPE, codec(FiberCableCutEffectMessage::encode, FiberCableCutEffectMessage::decode), FiberCableCutEffectMessage::handle);
        registrar.playToClient(AbandonedFiberCableSyncMessage.TYPE, codec(AbandonedFiberCableSyncMessage::encode, AbandonedFiberCableSyncMessage::decode), AbandonedFiberCableSyncMessage::handle);
        registrar.playToClient(DroneOperatorVisibilityMessage.TYPE, codec(DroneOperatorVisibilityMessage::encode, DroneOperatorVisibilityMessage::decode), DroneOperatorVisibilityMessage::handle);
        registrar.playToClient(DroneSpotlightStateMessage.TYPE, codec(DroneSpotlightStateMessage::encode, DroneSpotlightStateMessage::decode), DroneSpotlightStateMessage::handle);
        registrar.playToClient(VehicleAntiDroneNetStateMessage.TYPE, codec(VehicleAntiDroneNetStateMessage::encode, VehicleAntiDroneNetStateMessage::decode), VehicleAntiDroneNetStateMessage::handle);
        registrar.playToClient(OpenSirenConfigMessage.TYPE, codec(OpenSirenConfigMessage::encode, OpenSirenConfigMessage::decode), OpenSirenConfigMessage::handle);
        registrar.playToClient(OpenJammerConfigMessage.TYPE, codec(OpenJammerConfigMessage::encode, OpenJammerConfigMessage::decode), OpenJammerConfigMessage::handle);
        registrar.playToClient(PortableFpvDetectorScanMessage.TYPE, codec(PortableFpvDetectorScanMessage::encode, PortableFpvDetectorScanMessage::decode), PortableFpvDetectorScanMessage::handle);
        registrar.playToClient(PortableFpvDetectorOpenMessage.TYPE, codec(PortableFpvDetectorOpenMessage::encode, PortableFpvDetectorOpenMessage::decode), PortableFpvDetectorOpenMessage::handle);
        registrar.playToClient(OpenLaserTowerWhitelistMessage.TYPE, codec(OpenLaserTowerWhitelistMessage::encode, OpenLaserTowerWhitelistMessage::decode), OpenLaserTowerWhitelistMessage::handle);
        registrar.playToServer(UpdateSirenBlacklistMessage.TYPE, codec(UpdateSirenBlacklistMessage::encode, UpdateSirenBlacklistMessage::decode), UpdateSirenBlacklistMessage::handle);
        registrar.playToServer(UpdateJammerRangeMessage.TYPE, codec(UpdateJammerRangeMessage::encode, UpdateJammerRangeMessage::decode), UpdateJammerRangeMessage::handle);
        registrar.playToServer(UpdateJammerWhitelistMessage.TYPE, codec(UpdateJammerWhitelistMessage::encode, UpdateJammerWhitelistMessage::decode), UpdateJammerWhitelistMessage::handle);
        registrar.playToServer(UpdateLaserTowerWhitelistMessage.TYPE, codec(UpdateLaserTowerWhitelistMessage::encode, UpdateLaserTowerWhitelistMessage::decode), UpdateLaserTowerWhitelistMessage::handle);
        registrar.playToServer(FiberCableCutRequestMessage.TYPE, codec(FiberCableCutRequestMessage::encode, FiberCableCutRequestMessage::decode), FiberCableCutRequestMessage::handle);
        registrar.playToServer(AbandonedFiberCableCutRequestMessage.TYPE, codec(AbandonedFiberCableCutRequestMessage::encode, AbandonedFiberCableCutRequestMessage::decode), AbandonedFiberCableCutRequestMessage::handle);
        registrar.playToServer(NetBulkPlacementConfirmMessage.TYPE, codec(NetBulkPlacementConfirmMessage::encode, NetBulkPlacementConfirmMessage::decode), NetBulkPlacementConfirmMessage::handle);
        registrar.playToServer(PortableFpvDetectorSelectMessage.TYPE, codec(PortableFpvDetectorSelectMessage::encode, PortableFpvDetectorSelectMessage::decode), PortableFpvDetectorSelectMessage::handle);
        registrar.playToServer(ToggleSpotlightMessage.TYPE, codec(ToggleSpotlightMessage::encode, ToggleSpotlightMessage::decode), ToggleSpotlightMessage::handle);
        registrar.playToServer(DropMortarPayloadMessage.TYPE, codec(DropMortarPayloadMessage::encode, DropMortarPayloadMessage::decode), DropMortarPayloadMessage::handle);
        registrar.playToServer(DeliveryCrateDeployMessage.TYPE, codec(DeliveryCrateDeployMessage::encode, DeliveryCrateDeployMessage::decode), DeliveryCrateDeployMessage::handle);
        registrar.playToServer(DeliveryCratePickupMessage.TYPE, codec(DeliveryCratePickupMessage::encode, DeliveryCratePickupMessage::decode), DeliveryCratePickupMessage::handle);
        registrar.playToServer(CubedFpvInputMessage.TYPE, codec(CubedFpvInputMessage::encode, CubedFpvInputMessage::decode), CubedFpvInputMessage::handle);
    }

    private static <T extends CustomPacketPayload> StreamCodec<RegistryFriendlyByteBuf, T> codec(
            BiConsumer<T, FriendlyByteBuf> encoder,
            Function<FriendlyByteBuf, T> decoder
    ) {
        return StreamCodec.ofMember((message, buffer) -> encoder.accept(message, buffer), decoder::apply);
    }

    public static void sendDroneEnergy(ServerPlayer player, DroneEnergySyncMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendLucasFuel(ServerPlayer player, LucasFuelSyncMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendDroneJam(ServerPlayer player, DroneJamSyncMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendDroneFiberOptic(ServerPlayer player, DroneFiberOpticSyncMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendFiberRouteSnapshot(ServerPlayer player, FiberRouteSnapshotMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendFiberRouteAppend(ServerPlayer player, FiberRouteAppendMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendFiberRouteClear(ServerPlayer player, FiberRouteClearMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendFiberCableCutEffect(ServerPlayer player, FiberCableCutEffectMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendAbandonedFiberCables(ServerPlayer player, AbandonedFiberCableSyncMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void openSirenConfig(ServerPlayer player, OpenSirenConfigMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void openJammerConfig(ServerPlayer player, OpenJammerConfigMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void sendPortableFpvDetectorScan(ServerPlayer player, PortableFpvDetectorScanMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void openPortableFpvDetector(ServerPlayer player, PortableFpvDetectorOpenMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void openLaserTowerWhitelist(ServerPlayer player, OpenLaserTowerWhitelistMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void syncDroneOperatorVisibility(DroneOperatorVisibilityMessage message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public static void syncDroneSpotlightState(DroneSpotlightStateMessage message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public static void sendDroneSpotlightState(ServerPlayer player, DroneSpotlightStateMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void syncVehicleAntiDroneNetState(VehicleAntiDroneNetStateMessage message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    public static void sendVehicleAntiDroneNetState(ServerPlayer player, VehicleAntiDroneNetStateMessage message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void updateSirenBlacklist(UpdateSirenBlacklistMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void updateJammerRange(UpdateJammerRangeMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void updateJammerWhitelist(UpdateJammerWhitelistMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void updateLaserTowerWhitelist(UpdateLaserTowerWhitelistMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendFiberCableCutRequest(FiberCableCutRequestMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendAbandonedFiberCableCutRequest(AbandonedFiberCableCutRequestMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendNetBulkPlacementConfirm(NetBulkPlacementConfirmMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void selectPortableFpvDetectorTarget(PortableFpvDetectorSelectMessage message) {
        PacketDistributor.sendToServer(message);
    }

    public static void toggleSpotlight() {
        PacketDistributor.sendToServer(new ToggleSpotlightMessage());
    }

    public static void dropMortarPayload() {
        PacketDistributor.sendToServer(new DropMortarPayloadMessage());
    }

    public static void deployDeliveryCrate() {
        PacketDistributor.sendToServer(new DeliveryCrateDeployMessage());
    }

    public static void pickupDeliveryCrate() {
        PacketDistributor.sendToServer(new DeliveryCratePickupMessage());
    }

    public static void sendCubedFpvInput(CubedFpvInputMessage message) {
        PacketDistributor.sendToServer(message);
    }
}
