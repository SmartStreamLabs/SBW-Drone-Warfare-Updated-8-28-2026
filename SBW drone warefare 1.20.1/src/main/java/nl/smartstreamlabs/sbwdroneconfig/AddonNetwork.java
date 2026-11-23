package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AddonNetwork {
    private static final String PROTOCOL_VERSION = "82-adv-radio-sbw089final";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(SbwDroneRangeConfig.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void configRequest(ConfigRequest m){CHANNEL.sendToServer(m);}
    public static void configReply(ServerPlayer p,ConfigReply m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void dwfAction(DwfAction m){CHANNEL.sendToServer(m);}
    public static void dwfReply(ServerPlayer p,DwfReply m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void sk01(ServerPlayer p,Sk01Packet m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void sk01Clear(Sk01Clear m){CHANNEL.sendToServer(m);}
    private static int nextMessageId;
    private static boolean registered;

    public static void ds02Control(Ds02ControlMessage m){CHANNEL.sendToServer(m);}
public static void mj03Control(Mj03ControlMessage m){CHANNEL.sendToServer(m);}
 public static void adv01Control(Adv01Control m){CHANNEL.sendToServer(m);}
 public static void mpl01Action(Mpl01Action m){CHANNEL.sendToServer(m);}
 public static void adcAction(AdcAction m){CHANNEL.sendToServer(m);}
 public static void defenseAction(DefenseAction m){CHANNEL.sendToServer(m);}
 public static void openMj03(ServerPlayer p,Mj03OpenMessage m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
 public static void vatagInput(VatagInput m){CHANNEL.sendToServer(m);}
 public static void vatagState(ServerPlayer p,VatagState m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void id01Input(Id01Input m){CHANNEL.sendToServer(m);}
    public static void id01State(ServerPlayer p,Id01State m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void rd01Input(Rd01Input m){CHANNEL.sendToServer(m);}
    public static void rd01State(ServerPlayer p,Rd01State m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void sendDroneOsd(ServerPlayer p,DroneOsdTelemetry m){MoistureSystem.send(p,p.serverLevel().getEntity(m.drone()));CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void ntRequest(NtRequest m){CHANNEL.sendToServer(m);} public static void sendNt(ServerPlayer p,NtReply m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void dvtRequest(DvtRequest m){CHANNEL.sendToServer(m);}
    public static void dvtReply(ServerPlayer p,DvtReply m){if(m.selected()!=null)MoistureSystem.send(p,p.serverLevel().getEntity(m.selected()));CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void injuryState(ServerPlayer p,InjurySync m){CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->p),m);}
    public static void injuryStateTo(ServerPlayer p,InjurySync m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void injuryFeedback(ServerPlayer p,InjuryFeedback m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void distanceWar(ServerPlayer p,DistanceWarPacket m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void groundExit(GroundVehicleExit m){CHANNEL.sendToServer(m);}
    public static void rubbleUse(RubbleUse m){CHANNEL.sendToServer(m);}
    public static void rubbleReply(ServerPlayer p,RubbleReply m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void rubbleBurst(ServerPlayer p,RubbleBurst m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void ufMusicRequest(UfMusicRequest m){CHANNEL.sendToServer(m);}
    public static void ufMusicReply(ServerPlayer p,UfMusicReply m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
 public static void moistureTracking(net.minecraft.world.entity.Entity e,MoisturePacket m){CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(()->e),m);}
 public static void moistureTo(ServerPlayer p,MoisturePacket m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
 public static void platformPilot(){platformPilot(false);}
 public static void platformPilot(boolean back){CHANNEL.sendToServer(new PlatformPilotRequest(back?-1:0));}
    public static void monitorLinked(ServerPlayer p,MonitorLinkSoundPacket m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    public static void pilotMap(ServerPlayer p,PilotMapPacket m){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),m);}
    private AddonNetwork() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(DwfAction.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(DwfAction::encode).decoder(DwfAction::decode).consumerMainThread(DwfAction::handle).add();
        CHANNEL.messageBuilder(DwfReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(DwfReply::encode).decoder(DwfReply::decode).consumerMainThread(DwfReply::handle).add();
        CHANNEL.messageBuilder(Sk01Packet.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Sk01Packet::encode).decoder(Sk01Packet::decode).consumerMainThread(Sk01Packet::handle).add();
        CHANNEL.messageBuilder(Sk01Clear.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Sk01Clear::encode).decoder(Sk01Clear::decode).consumerMainThread(Sk01Clear::handle).add();
        CHANNEL.messageBuilder(PilotMapPacket.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(PilotMapPacket::encode).decoder(PilotMapPacket::decode).consumerMainThread(PilotMapPacket::handle).add();
        CHANNEL.messageBuilder(MonitorLinkSoundPacket.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(MonitorLinkSoundPacket::encode).decoder(MonitorLinkSoundPacket::decode).consumerMainThread(MonitorLinkSoundPacket::handle).add();
        CHANNEL.messageBuilder(ConfigRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(ConfigRequest::encode).decoder(ConfigRequest::decode).consumerMainThread(ConfigRequest::handle).add();
        CHANNEL.messageBuilder(ConfigReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(ConfigReply::encode).decoder(ConfigReply::decode).consumerMainThread(ConfigReply::handle).add();
        CHANNEL.messageBuilder(PlatformPilotRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(PlatformPilotRequest::encode).decoder(PlatformPilotRequest::decode).consumerMainThread(PlatformPilotRequest::handle).add();

        CHANNEL.messageBuilder(MoisturePacket.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(MoisturePacket::encode).decoder(MoisturePacket::decode).consumerMainThread(MoisturePacket::handle).add();
        CHANNEL.messageBuilder(UfMusicRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(UfMusicRequest::encode).decoder(UfMusicRequest::decode).consumerMainThread(UfMusicRequest::handle).add();
        CHANNEL.messageBuilder(UfMusicReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(UfMusicReply::encode).decoder(UfMusicReply::decode).consumerMainThread(UfMusicReply::handle).add();
        CHANNEL.messageBuilder(RubbleUse.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(RubbleUse::encode).decoder(RubbleUse::decode).consumerMainThread(RubbleUse::handle).add();
        CHANNEL.messageBuilder(RubbleReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(RubbleReply::encode).decoder(RubbleReply::decode).consumerMainThread(RubbleReply::handle).add();
        CHANNEL.messageBuilder(RubbleBurst.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(RubbleBurst::encode).decoder(RubbleBurst::decode).consumerMainThread(RubbleBurst::handle).add();
        CHANNEL.messageBuilder(GroundVehicleExit.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(GroundVehicleExit::encode).decoder(GroundVehicleExit::decode).consumerMainThread(GroundVehicleExit::handle).add();
        CHANNEL.messageBuilder(DistanceWarPacket.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(DistanceWarPacket::encode).decoder(DistanceWarPacket::decode).consumerMainThread(DistanceWarPacket::handle).add();
        CHANNEL.messageBuilder(InjurySync.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(InjurySync::encode).decoder(InjurySync::decode).consumerMainThread(InjurySync::handle).add();
        CHANNEL.messageBuilder(InjuryFeedback.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(InjuryFeedback::encode).decoder(InjuryFeedback::decode).consumerMainThread(InjuryFeedback::handle).add();
        CHANNEL.messageBuilder(NtRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(NtRequest::encode).decoder(NtRequest::decode).consumerMainThread(NtRequest::handle).add();
        CHANNEL.messageBuilder(NtReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(NtReply::encode).decoder(NtReply::decode).consumerMainThread(NtReply::handle).add();
        CHANNEL.messageBuilder(Rd01Input.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Rd01Input::encode).decoder(Rd01Input::decode).consumerMainThread(Rd01Input::handle).add();
        CHANNEL.messageBuilder(Rd01State.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Rd01State::encode).decoder(Rd01State::decode).consumerMainThread(Rd01State::handle).add();
        CHANNEL.messageBuilder(Id01Input.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Id01Input::encode).decoder(Id01Input::decode).consumerMainThread(Id01Input::handle).add();
        CHANNEL.messageBuilder(Id01State.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Id01State::encode).decoder(Id01State::decode).consumerMainThread(Id01State::handle).add();
        CHANNEL.messageBuilder(VatagInput.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(VatagInput::encode).decoder(VatagInput::decode).consumerMainThread(VatagInput::handle).add();
        CHANNEL.messageBuilder(VatagState.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(VatagState::encode).decoder(VatagState::decode).consumerMainThread(VatagState::handle).add();
        CHANNEL.messageBuilder(Mj03ControlMessage.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Mj03ControlMessage::encode).decoder(Mj03ControlMessage::decode).consumerMainThread(Mj03ControlMessage::handle).add();
        CHANNEL.messageBuilder(Adv01Control.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Adv01Control::encode).decoder(Adv01Control::decode).consumerMainThread(Adv01Control::handle).add();
        CHANNEL.messageBuilder(Mpl01Action.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Mpl01Action::encode).decoder(Mpl01Action::decode).consumerMainThread(Mpl01Action::handle).add();
        CHANNEL.messageBuilder(AdcAction.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(AdcAction::encode).decoder(AdcAction::decode).consumerMainThread(AdcAction::handle).add();
        CHANNEL.messageBuilder(DefenseAction.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(DefenseAction::encode).decoder(DefenseAction::decode).consumerMainThread(DefenseAction::handle).add();
        CHANNEL.messageBuilder(Mj03OpenMessage.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(Mj03OpenMessage::encode).decoder(Mj03OpenMessage::decode).consumerMainThread(Mj03OpenMessage::handle).add();
        CHANNEL.messageBuilder(Ds02ControlMessage.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(Ds02ControlMessage::encode).decoder(Ds02ControlMessage::decode).consumerMainThread(Ds02ControlMessage::handle).add();
        CHANNEL.messageBuilder(MissionRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(MissionRequest::encode).decoder(MissionRequest::decode).consumerMainThread(MissionRequest::handle).add();
        CHANNEL.messageBuilder(ExplosionVfxMessage.class, nextMessageId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ExplosionVfxMessage::encode).decoder(ExplosionVfxMessage::decode)
                .consumerMainThread(ExplosionVfxMessage::handle).add();
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
            CHANNEL.messageBuilder(DroneOsdTelemetry.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(DroneOsdTelemetry::encode).decoder(DroneOsdTelemetry::decode).consumerMainThread(DroneOsdTelemetry::handle).add();
        CHANNEL.messageBuilder(DvtRequest.class,nextMessageId++,NetworkDirection.PLAY_TO_SERVER).encoder(DvtRequest::encode).decoder(DvtRequest::decode).consumerMainThread(DvtRequest::handle).add();
        CHANNEL.messageBuilder(DvtReply.class,nextMessageId++,NetworkDirection.PLAY_TO_CLIENT).encoder(DvtReply::encode).decoder(DvtReply::decode).consumerMainThread(DvtReply::handle).add();
    }

    public static void sendExplosionVfx(ServerPlayer player, ExplosionVfxMessage message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
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
