package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AddonNetwork {
    private static final String PROTOCOL_VERSION = "20";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int nextMessageId;
    private static boolean registered;

    private AddonNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(DroneJamSyncMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DroneJamSyncMessage::encode)
                .decoder(DroneJamSyncMessage::decode)
                .consumerMainThread(DroneJamSyncMessage::handle)
                .add();
        CHANNEL.messageBuilder(DroneEnergySyncMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DroneEnergySyncMessage::encode)
                .decoder(DroneEnergySyncMessage::decode)
                .consumerMainThread(DroneEnergySyncMessage::handle)
                .add();
        CHANNEL.messageBuilder(LucasFuelSyncMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LucasFuelSyncMessage::encode)
                .decoder(LucasFuelSyncMessage::decode)
                .consumerMainThread(LucasFuelSyncMessage::handle)
                .add();
        CHANNEL.messageBuilder(DroneFiberOpticSyncMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DroneFiberOpticSyncMessage::encode)
                .decoder(DroneFiberOpticSyncMessage::decode)
                .consumerMainThread(DroneFiberOpticSyncMessage::handle)
                .add();
        CHANNEL.messageBuilder(FiberRouteSnapshotMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FiberRouteSnapshotMessage::encode)
                .decoder(FiberRouteSnapshotMessage::decode)
                .consumerMainThread(FiberRouteSnapshotMessage::handle)
                .add();
        CHANNEL.messageBuilder(FiberRouteAppendMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FiberRouteAppendMessage::encode)
                .decoder(FiberRouteAppendMessage::decode)
                .consumerMainThread(FiberRouteAppendMessage::handle)
                .add();
        CHANNEL.messageBuilder(FiberRouteClearMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FiberRouteClearMessage::encode)
                .decoder(FiberRouteClearMessage::decode)
                .consumerMainThread(FiberRouteClearMessage::handle)
                .add();
        CHANNEL.messageBuilder(FiberCableCutEffectMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FiberCableCutEffectMessage::encode)
                .decoder(FiberCableCutEffectMessage::decode)
                .consumerMainThread(FiberCableCutEffectMessage::handle)
                .add();
        CHANNEL.messageBuilder(AbandonedFiberCableSyncMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AbandonedFiberCableSyncMessage::encode)
                .decoder(AbandonedFiberCableSyncMessage::decode)
                .consumerMainThread(AbandonedFiberCableSyncMessage::handle)
                .add();
        CHANNEL.messageBuilder(DroneOperatorVisibilityMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DroneOperatorVisibilityMessage::encode)
                .decoder(DroneOperatorVisibilityMessage::decode)
                .consumerMainThread(DroneOperatorVisibilityMessage::handle)
                .add();
        CHANNEL.messageBuilder(DroneSpotlightStateMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DroneSpotlightStateMessage::encode)
                .decoder(DroneSpotlightStateMessage::decode)
                .consumerMainThread(DroneSpotlightStateMessage::handle)
                .add();
        CHANNEL.messageBuilder(VehicleAntiDroneNetStateMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(VehicleAntiDroneNetStateMessage::encode)
                .decoder(VehicleAntiDroneNetStateMessage::decode)
                .consumerMainThread(VehicleAntiDroneNetStateMessage::handle)
                .add();
        CHANNEL.messageBuilder(OpenSirenConfigMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenSirenConfigMessage::encode)
                .decoder(OpenSirenConfigMessage::decode)
                .consumerMainThread(OpenSirenConfigMessage::handle)
                .add();
        CHANNEL.messageBuilder(OpenJammerConfigMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenJammerConfigMessage::encode)
                .decoder(OpenJammerConfigMessage::decode)
                .consumerMainThread(OpenJammerConfigMessage::handle)
                .add();
        CHANNEL.messageBuilder(PortableFpvDetectorScanMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PortableFpvDetectorScanMessage::encode)
                .decoder(PortableFpvDetectorScanMessage::decode)
                .consumerMainThread(PortableFpvDetectorScanMessage::handle)
                .add();
        CHANNEL.messageBuilder(PortableFpvDetectorOpenMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PortableFpvDetectorOpenMessage::encode)
                .decoder(PortableFpvDetectorOpenMessage::decode)
                .consumerMainThread(PortableFpvDetectorOpenMessage::handle)
                .add();
        CHANNEL.messageBuilder(UpdateSirenBlacklistMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateSirenBlacklistMessage::encode)
                .decoder(UpdateSirenBlacklistMessage::decode)
                .consumerMainThread(UpdateSirenBlacklistMessage::handle)
                .add();
        CHANNEL.messageBuilder(UpdateJammerRangeMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateJammerRangeMessage::encode)
                .decoder(UpdateJammerRangeMessage::decode)
                .consumerMainThread(UpdateJammerRangeMessage::handle)
                .add();
        CHANNEL.messageBuilder(UpdateJammerWhitelistMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateJammerWhitelistMessage::encode)
                .decoder(UpdateJammerWhitelistMessage::decode)
                .consumerMainThread(UpdateJammerWhitelistMessage::handle)
                .add();
        CHANNEL.messageBuilder(PortableFpvDetectorSelectMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PortableFpvDetectorSelectMessage::encode)
                .decoder(PortableFpvDetectorSelectMessage::decode)
                .consumerMainThread(PortableFpvDetectorSelectMessage::handle)
                .add();
        CHANNEL.messageBuilder(ToggleSpotlightMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleSpotlightMessage::encode)
                .decoder(ToggleSpotlightMessage::decode)
                .consumerMainThread(ToggleSpotlightMessage::handle)
                .add();
        CHANNEL.messageBuilder(CubedFpvInputMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CubedFpvInputMessage::encode)
                .decoder(CubedFpvInputMessage::decode)
                .consumerMainThread(CubedFpvInputMessage::handle)
                .add();
        CHANNEL.messageBuilder(FiberCableCutRequestMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(FiberCableCutRequestMessage::encode)
                .decoder(FiberCableCutRequestMessage::decode)
                .consumerMainThread(FiberCableCutRequestMessage::handle)
                .add();
        CHANNEL.messageBuilder(AbandonedFiberCableCutRequestMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AbandonedFiberCableCutRequestMessage::encode)
                .decoder(AbandonedFiberCableCutRequestMessage::decode)
                .consumerMainThread(AbandonedFiberCableCutRequestMessage::handle)
                .add();
        CHANNEL.messageBuilder(NetBulkPlacementConfirmMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(NetBulkPlacementConfirmMessage::encode)
                .decoder(NetBulkPlacementConfirmMessage::decode)
                .consumerMainThread(NetBulkPlacementConfirmMessage::handle)
                .add();
        CHANNEL.messageBuilder(DeliveryCrateDeployMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeliveryCrateDeployMessage::encode)
                .decoder(DeliveryCrateDeployMessage::decode)
                .consumerMainThread(DeliveryCrateDeployMessage::handle)
                .add();
        CHANNEL.messageBuilder(DeliveryCratePickupMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DeliveryCratePickupMessage::encode)
                .decoder(DeliveryCratePickupMessage::decode)
                .consumerMainThread(DeliveryCratePickupMessage::handle)
                .add();
        CHANNEL.messageBuilder(OpenLaserTowerWhitelistMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenLaserTowerWhitelistMessage::encode)
                .decoder(OpenLaserTowerWhitelistMessage::decode)
                .consumerMainThread(OpenLaserTowerWhitelistMessage::handle)
                .add();
        CHANNEL.messageBuilder(UpdateLaserTowerWhitelistMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(UpdateLaserTowerWhitelistMessage::encode)
                .decoder(UpdateLaserTowerWhitelistMessage::decode)
                .consumerMainThread(UpdateLaserTowerWhitelistMessage::handle)
                .add();
    }

    public static void sendDroneEnergy(ServerPlayer player, DroneEnergySyncMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendLucasFuel(ServerPlayer player, LucasFuelSyncMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendDroneJam(ServerPlayer player, DroneJamSyncMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendDroneFiberOptic(ServerPlayer player, DroneFiberOpticSyncMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendFiberRouteSnapshot(ServerPlayer player, FiberRouteSnapshotMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendFiberRouteAppend(ServerPlayer player, FiberRouteAppendMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendFiberRouteClear(ServerPlayer player, FiberRouteClearMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendFiberCableCutEffect(ServerPlayer player, FiberCableCutEffectMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendAbandonedFiberCables(ServerPlayer player, AbandonedFiberCableSyncMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void openSirenConfig(ServerPlayer player, OpenSirenConfigMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void openJammerConfig(ServerPlayer player, OpenJammerConfigMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void openLaserTowerWhitelist(ServerPlayer player, OpenLaserTowerWhitelistMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendPortableFpvDetectorScan(ServerPlayer player, PortableFpvDetectorScanMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void openPortableFpvDetector(ServerPlayer player, PortableFpvDetectorOpenMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void syncDroneOperatorVisibility(DroneOperatorVisibilityMessage message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void syncDroneSpotlightState(DroneSpotlightStateMessage message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void syncVehicleAntiDroneNetState(VehicleAntiDroneNetStateMessage message) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), message);
    }

    public static void sendVehicleAntiDroneNetState(ServerPlayer player, VehicleAntiDroneNetStateMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void sendDroneSpotlightState(ServerPlayer player, DroneSpotlightStateMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static void updateSirenBlacklist(UpdateSirenBlacklistMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void updateJammerRange(UpdateJammerRangeMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void updateJammerWhitelist(UpdateJammerWhitelistMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void updateLaserTowerWhitelist(UpdateLaserTowerWhitelistMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void selectPortableFpvDetectorTarget(PortableFpvDetectorSelectMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void toggleSpotlight() {
        CHANNEL.sendToServer(new ToggleSpotlightMessage());
    }

    public static void sendCubedFpvInput(CubedFpvInputMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendFiberCableCutRequest(FiberCableCutRequestMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendAbandonedFiberCableCutRequest(AbandonedFiberCableCutRequestMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void sendNetBulkPlacementConfirm(NetBulkPlacementConfirmMessage message) {
        CHANNEL.sendToServer(message);
    }

    public static void deployDeliveryCrate() {
        CHANNEL.sendToServer(new DeliveryCrateDeployMessage());
    }

    public static void pickupDeliveryCrate() {
        CHANNEL.sendToServer(new DeliveryCratePickupMessage());
    }
}
