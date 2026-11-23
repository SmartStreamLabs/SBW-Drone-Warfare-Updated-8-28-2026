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
    private static final String PROTOCOL_VERSION = "82-adv-radio-sbw089final";
    public static void configRequest(ConfigRequest m){PacketDistributor.sendToServer(m);}
    public static void configReply(ServerPlayer p,ConfigReply m){PacketDistributor.sendToPlayer(p,m);}
    public static void dwfAction(DwfAction m){PacketDistributor.sendToServer(m);}
    public static void dwfReply(ServerPlayer p,DwfReply m){PacketDistributor.sendToPlayer(p,m);}
    public static void sk01(ServerPlayer p,Sk01Packet m){PacketDistributor.sendToPlayer(p,m);}
    public static void sk01Clear(Sk01Clear m){PacketDistributor.sendToServer(m);}
    private static boolean registered;

    public static void ds02Control(Ds02ControlMessage m){PacketDistributor.sendToServer(m);}
public static void mj03Control(Mj03ControlMessage m){PacketDistributor.sendToServer(m);}
 public static void adv01Control(Adv01Control m){PacketDistributor.sendToServer(m);}
 public static void mpl01Action(Mpl01Action m){PacketDistributor.sendToServer(m);}
 public static void adcAction(AdcAction m){PacketDistributor.sendToServer(m);}
 public static void defenseAction(DefenseAction m){PacketDistributor.sendToServer(m);}
 public static void openMj03(ServerPlayer p,Mj03OpenMessage m){PacketDistributor.sendToPlayer(p,m);}
 public static void vatagInput(VatagInput m){PacketDistributor.sendToServer(m);}
 public static void vatagState(ServerPlayer p,VatagState m){PacketDistributor.sendToPlayer(p,m);}
    public static void id01Input(Id01Input m){PacketDistributor.sendToServer(m);}
    public static void id01State(ServerPlayer p,Id01State m){PacketDistributor.sendToPlayer(p,m);}
    public static void rd01Input(Rd01Input m){PacketDistributor.sendToServer(m);}
    public static void rd01State(ServerPlayer p,Rd01State m){PacketDistributor.sendToPlayer(p,m);}
    public static void sendDroneOsd(ServerPlayer p,DroneOsdTelemetry m){MoistureSystem.send(p,p.serverLevel().getEntity(m.drone()));PacketDistributor.sendToPlayer(p,m);}
    public static void ntRequest(NtRequest m){PacketDistributor.sendToServer(m);} public static void sendNt(ServerPlayer p,NtReply m){PacketDistributor.sendToPlayer(p,m);}
    public static void dvtRequest(DvtRequest m){PacketDistributor.sendToServer(m);}
    public static void dvtReply(ServerPlayer p,DvtReply m){if(m.selected()!=null)MoistureSystem.send(p,p.serverLevel().getEntity(m.selected()));PacketDistributor.sendToPlayer(p,m);}
    public static void injuryState(ServerPlayer p,InjurySync m){PacketDistributor.sendToPlayersTrackingEntityAndSelf(p,m);}
    public static void injuryStateTo(ServerPlayer p,InjurySync m){PacketDistributor.sendToPlayer(p,m);}
    public static void injuryFeedback(ServerPlayer p,InjuryFeedback m){PacketDistributor.sendToPlayer(p,m);}
    public static void distanceWar(ServerPlayer p,DistanceWarPacket m){PacketDistributor.sendToPlayer(p,m);}
    public static void groundExit(GroundVehicleExit m){PacketDistributor.sendToServer(m);}
    public static void rubbleUse(RubbleUse m){net.neoforged.neoforge.network.PacketDistributor.sendToServer(m);}
    public static void rubbleReply(ServerPlayer p,RubbleReply m){net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,m);}
    public static void rubbleBurst(ServerPlayer p,RubbleBurst m){net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(p,m);}
    public static void ufMusicRequest(UfMusicRequest m){PacketDistributor.sendToServer(m);}
    public static void ufMusicReply(ServerPlayer p,UfMusicReply m){PacketDistributor.sendToPlayer(p,m);}
 public static void moistureTracking(net.minecraft.world.entity.Entity e,MoisturePacket m){PacketDistributor.sendToPlayersTrackingEntityAndSelf(e,m);}
 public static void moistureTo(ServerPlayer p,MoisturePacket m){PacketDistributor.sendToPlayer(p,m);}
 public static void platformPilot(){platformPilot(false);}
 public static void platformPilot(boolean back){PacketDistributor.sendToServer(new PlatformPilotRequest(back?-1:0));}
    public static void monitorLinked(ServerPlayer p,MonitorLinkSoundPacket m){PacketDistributor.sendToPlayer(p,m);}
    public static void pilotMap(ServerPlayer p,PilotMapPacket m){PacketDistributor.sendToPlayer(p,m);}
    private AddonNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        if (registered) {
            return;
        }
        registered = true;
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToServer(DwfAction.TYPE,codec(DwfAction::encode,DwfAction::decode),DwfAction::handle);
        registrar.playToClient(DwfReply.TYPE,codec(DwfReply::encode,DwfReply::decode),DwfReply::handle);
        registrar.playToClient(Sk01Packet.TYPE,codec(Sk01Packet::encode,Sk01Packet::decode),Sk01Packet::handle);
        registrar.playToServer(Sk01Clear.TYPE,codec(Sk01Clear::encode,Sk01Clear::decode),Sk01Clear::handle);
        registrar.playToClient(PilotMapPacket.TYPE,codec(PilotMapPacket::encode,PilotMapPacket::decode),PilotMapPacket::handle);
        registrar.playToClient(MonitorLinkSoundPacket.TYPE,codec(MonitorLinkSoundPacket::encode,MonitorLinkSoundPacket::decode),MonitorLinkSoundPacket::handle);
        registrar.playToServer(ConfigRequest.TYPE,codec(ConfigRequest::encode,ConfigRequest::decode),ConfigRequest::handle);
        registrar.playToClient(ConfigReply.TYPE,codec(ConfigReply::encode,ConfigReply::decode),ConfigReply::handle);
        registrar.playToServer(PlatformPilotRequest.TYPE,codec(PlatformPilotRequest::encode,PlatformPilotRequest::decode),PlatformPilotRequest::handle);
        registrar.playToClient(MoisturePacket.TYPE,codec(MoisturePacket::encode,MoisturePacket::decode),MoisturePacket::handle);
        registrar.playToServer(UfMusicRequest.TYPE,codec(UfMusicRequest::encode,UfMusicRequest::decode),UfMusicRequest::handle);
        registrar.playToClient(UfMusicReply.TYPE,codec(UfMusicReply::encode,UfMusicReply::decode),UfMusicReply::handle);
        registrar.playToServer(RubbleUse.TYPE,codec(RubbleUse::encode,RubbleUse::decode),RubbleUse::handle);
        registrar.playToClient(RubbleReply.TYPE,codec(RubbleReply::encode,RubbleReply::decode),RubbleReply::handle);
        registrar.playToClient(RubbleBurst.TYPE,codec(RubbleBurst::encode,RubbleBurst::decode),RubbleBurst::handle);
        registrar.playToServer(GroundVehicleExit.TYPE,codec(GroundVehicleExit::encode,GroundVehicleExit::decode),GroundVehicleExit::handle);
        registrar.playToClient(DistanceWarPacket.TYPE,codec(DistanceWarPacket::encode,DistanceWarPacket::decode),DistanceWarPacket::handle);
        registrar.playToClient(InjurySync.TYPE,codec(InjurySync::encode,InjurySync::decode),InjurySync::handle);
        registrar.playToClient(InjuryFeedback.TYPE,codec(InjuryFeedback::encode,InjuryFeedback::decode),InjuryFeedback::handle);
        registrar.playToServer(DvtRequest.TYPE,codec(DvtRequest::encode,DvtRequest::decode),DvtRequest::handle);
        registrar.playToClient(DvtReply.TYPE,codec(DvtReply::encode,DvtReply::decode),DvtReply::handle);
        registrar.playToServer(NtRequest.TYPE,codec(NtRequest::encode,NtRequest::decode),NtRequest::handle);
        registrar.playToClient(NtReply.TYPE,codec(NtReply::encode,NtReply::decode),NtReply::handle);
        registrar.playToClient(DroneOsdTelemetry.TYPE,codec(DroneOsdTelemetry::encode,DroneOsdTelemetry::decode),DroneOsdTelemetry::handle);
        registrar.playToServer(Rd01Input.TYPE,codec(Rd01Input::encode,Rd01Input::decode),Rd01Input::handle);
        registrar.playToClient(Rd01State.TYPE,codec(Rd01State::encode,Rd01State::decode),Rd01State::handle);
        registrar.playToServer(Id01Input.TYPE,codec(Id01Input::encode,Id01Input::decode),Id01Input::handle);
        registrar.playToClient(Id01State.TYPE,codec(Id01State::encode,Id01State::decode),Id01State::handle);
        registrar.playToServer(VatagInput.TYPE,codec(VatagInput::encode,VatagInput::decode),VatagInput::handle);
        registrar.playToClient(VatagState.TYPE,codec(VatagState::encode,VatagState::decode),VatagState::handle);
        registrar.playToServer(Mj03ControlMessage.TYPE,codec(Mj03ControlMessage::encode,Mj03ControlMessage::decode),Mj03ControlMessage::handle);
        registrar.playToServer(Adv01Control.TYPE,codec(Adv01Control::encode,Adv01Control::decode),Adv01Control::handle);
        registrar.playToServer(Mpl01Action.TYPE,codec(Mpl01Action::encode,Mpl01Action::decode),Mpl01Action::handle);
        registrar.playToServer(AdcAction.TYPE,codec(AdcAction::encode,AdcAction::decode),AdcAction::handle);
        registrar.playToServer(DefenseAction.TYPE,codec(DefenseAction::encode,DefenseAction::decode),DefenseAction::handle);
        registrar.playToClient(Mj03OpenMessage.TYPE,codec(Mj03OpenMessage::encode,Mj03OpenMessage::decode),Mj03OpenMessage::handle);
        registrar.playToServer(Ds02ControlMessage.TYPE,codec(Ds02ControlMessage::encode,Ds02ControlMessage::decode),Ds02ControlMessage::handle);
        registrar.playToServer(MissionRequest.TYPE, codec(MissionRequest::encode, MissionRequest::decode), MissionRequest::handle);
        registrar.playToClient(ExplosionVfxMessage.TYPE, codec(ExplosionVfxMessage::encode, ExplosionVfxMessage::decode), ExplosionVfxMessage::handle);
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

    public static void sendExplosionVfx(ServerPlayer player, ExplosionVfxMessage message) {
        PacketDistributor.sendToPlayer(player, message);
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
