package nl.smartstreamlabs.sbwdroneconfig;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(SbwDroneRangeConfig.MOD_ID)
public class SbwDroneRangeConfig {
    public static final String MOD_ID = "sbwdroneconfig";
    public static final String SBW_MOD_ID = "superbwarfare";
    public static final String DRONE_ENTITY_ID = "superbwarfare:drone";
    public static final String CUBED_FPV_DRONE_ENTITY_ID = MOD_ID + ":" + CubedFpvDroneDefinition.ID;
    public static final String LUCAS_DRONE_ENTITY_ID = MOD_ID + ":" + LucasDroneDefinition.ID;
    public static final String DELIVERY_DRONE_ENTITY_ID = MOD_ID + ":" + DeliveryDroneDefinition.ID;
    public static final String GLIDE_BOMB_ENTITY_ID = MOD_ID + ":" + GlideBombDefinition.ID;
    public static final String GERANIUM_2_ENTITY_ID = MOD_ID + ":" + Geranium2Definition.ID;
    public static final String HEAVY_BOMBER_DRONE_ENTITY_ID = MOD_ID + ":" + HeavyBomberDroneDefinition.ID;
    public static final String UKRAINIAN_BAYRAKTAR_TB2_ENTITY_ID = MOD_ID + ":" + BayraktarTb2Definition.ID;
    public static final String FIBER_OPTIC_CABLE_SEGMENT_ENTITY_ID = MOD_ID + ":fiber_optic_cable_segment";
    public static final String MONITOR_ITEM_ID = "superbwarfare:monitor";
    public static final Logger LOGGER = LoggerFactory.getLogger(SbwDroneRangeConfig.class);

    public SbwDroneRangeConfig(IEventBus modBus, ModContainer modContainer) {
        AddonCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        AddonEntities.ENTITY_TYPES.register(modBus);
        AddonBlocks.BLOCKS.register(modBus);
        AddonItems.ITEMS.register(modBus);
        AddonBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        AddonMenus.MENU_TYPES.register(modBus);
        AddonSounds.SOUND_EVENTS.register(modBus);
        ExplosionVfxParticles.TYPES.register(modBus);
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(AddonConfig::onLoad);
        modBus.addListener(this::registerPayloadHandlers);
        modBus.addListener(ChunkTicketManager::registerTicketControllers);
        modBus.addListener(MissionTickets::registerTicketControllers);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SbwDronePolyMeshFactory.registerForModNamespace();
            DroneConfigMenuClient.init(modContainer);
            FuelMixerClient.init(modBus);
            DroneAudioManager.init(modBus);
            DroneMonitorUseSoundClient.init(modBus);
            DroneThermalVisionClient.init(modBus);
            CameraInterferenceClient.init(modBus);
            DroneEnergyClient.init(modBus);
            ExplosionVfxClient.init(modBus);
            LucasFuelClient.init(modBus);
            DroneFiberOpticClient.init(modBus);
            AbandonedFiberCableClient.init(modBus);
            FiberCableCutClient.init(modBus);
            NetBulkPlacementClient.init(modBus);
            MonitorMouseInputClient.init(modBus);
            FpvGogglesOverlayClient.init(modBus);
            CubedFpvInputClient.init(modBus);
            CubedFpvReferenceGuiOverlay.init(modBus);
            CubedFpvCameraEffectsClient.init(modBus);
            LucasDroneHudOverlay.init(modBus);
            DeliveryDroneHudOverlay.init(modBus);
            LucasFlightDebugClient.init(modBus);
            FiberOpticCableRenderer.init(modBus);
            AbandonedFiberCableRenderer.init(modBus);
            DroneJammerClient.init(modBus);
            DroneJamOverlayClient.init(modBus);
            DroneWeatherEffectsClient.init(modBus);
            DroneScreenshotClient.init(modBus);
            DroneSpotlightClient.init(modBus);
            DronePayloadClient.init(modBus);
            VehicleAntiDroneNetClient.init(modBus);
            DeliveryCrateDeployClient.init(modBus);
            JourneyMapCompatClient.init(modBus);
            AddonClientRenderLayers.init(modBus);
            AddonEntityRenderers.init(modBus);
            Sd01Client.init(modBus);RubbleClient.init(modBus);
        }

        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Sk01Config",ModConfig.Type.SERVER,Sk01Config.SPEC,"sbwdroneconfig-sk01.toml"),"sbwdroneconfig-sk01.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("Sk01ClientConfig",ModConfig.Type.CLIENT,Sk01ClientConfig.SPEC,"sbwdroneconfig-sk01-client.toml"),"sbwdroneconfig-sk01-client.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Dj01Config",ModConfig.Type.SERVER,Dj01Config.SPEC,"sbwdroneconfig-dj01.toml"), "sbwdroneconfig-dj01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Gad01Config",ModConfig.Type.SERVER,Gad01Config.SPEC,"sbwdroneconfig-gad01.toml"), "sbwdroneconfig-gad01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Hg01Config",ModConfig.Type.SERVER,Hg01Config.SPEC,"sbwdroneconfig-hg01.toml"), "sbwdroneconfig-hg01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Lm01Config",ModConfig.Type.SERVER,Lm01Config.SPEC,"sbwdroneconfig-lm01.toml"), "sbwdroneconfig-lm01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Sd01Config",ModConfig.Type.SERVER,Sd01Config.SPEC,"sbwdroneconfig-sd01.toml"), "sbwdroneconfig-sd01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("RubbleConfig",ModConfig.Type.SERVER,RubbleConfig.SPEC,"sbwdroneconfig-rubble.toml"),"sbwdroneconfig-rubble.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("SecurityCraftBlastConfig",ModConfig.Type.SERVER,SecurityCraftBlastConfig.SPEC,"sbwdroneconfig-securitycraft-blasts.toml"),"sbwdroneconfig-securitycraft-blasts.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Mpl01Config",ModConfig.Type.SERVER,Mpl01Config.SPEC,"sbwdroneconfig-mpl01.toml"),"sbwdroneconfig-mpl01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Ps01Config",ModConfig.Type.SERVER,Ps01Config.SPEC,"sbwdroneconfig-ps01.toml"),"sbwdroneconfig-ps01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("DefenseConfig",ModConfig.Type.SERVER,DefenseConfig.SPEC,"sbwdroneconfig-defense.toml"),"sbwdroneconfig-defense.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Ndc01Config",ModConfig.Type.SERVER,Ndc01Config.SPEC,"sbwdroneconfig-ndc01.toml"),"sbwdroneconfig-ndc01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("PilotMapConfig",ModConfig.Type.SERVER,PilotMapConfig.SPEC,"sbwdroneconfig-map-compat.toml"),"sbwdroneconfig-map-compat.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("AdcConfig",ModConfig.Type.SERVER,AdcConfig.SPEC,"sbwdroneconfig-adc01.toml"),"sbwdroneconfig-adc01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Us01Config",ModConfig.Type.SERVER,Us01Config.SPEC,"sbwdroneconfig-us01.toml"),"sbwdroneconfig-us01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Pb01Config",ModConfig.Type.SERVER,Pb01Config.SPEC,"sbwdroneconfig-pb01.toml"),"sbwdroneconfig-pb01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("UfMusicConfig",ModConfig.Type.SERVER,UfMusicConfig.SPEC,"sbwdroneconfig-uf01-music.toml"),"sbwdroneconfig-uf01-music.toml");
        NeoForge.EVENT_BUS.register(UfMusicServer.class);
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("MoistureConfig",ModConfig.Type.SERVER,MoistureConfig.SPEC,"sbwdroneconfig-moisture.toml"),"sbwdroneconfig-moisture.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("MoistureClientConfig",ModConfig.Type.CLIENT,MoistureClientConfig.SPEC,"sbwdroneconfig-moisture-client.toml"),"sbwdroneconfig-moisture-client.toml");
        Ps01Compat.register();
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("RubbleClientConfig",ModConfig.Type.CLIENT,RubbleClientConfig.SPEC,"sbwdroneconfig-rubble-client.toml"),"sbwdroneconfig-rubble-client.toml");
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(RubbleEvents.class);
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Qd01Config",ModConfig.Type.SERVER,Qd01Config.SPEC,"sbwdroneconfig-qd01.toml"), "sbwdroneconfig-qd01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Hgd01Config",ModConfig.Type.SERVER,Hgd01Config.SPEC,"sbwdroneconfig-hgd01.toml"), "sbwdroneconfig-hgd01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Adv01Config",ModConfig.Type.SERVER,Adv01Config.SPEC,"sbwdroneconfig-adv01.toml"), "sbwdroneconfig-adv01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("AddonConfig",ModConfig.Type.SERVER,AddonConfig.SPEC,"sbwdroneconfig-gameplay.toml"), "sbwdroneconfig-gameplay.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("DwfConfig",ModConfig.Type.SERVER,DwfConfig.SPEC,"sbwdroneconfig-fabricator.toml"), "sbwdroneconfig-fabricator.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("MissionConfig",ModConfig.Type.SERVER,MissionConfig.SPEC,"sbwdroneconfig-missions.toml"), "sbwdroneconfig-missions.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("UniformArmorConfig",ModConfig.Type.SERVER,UniformArmorConfig.SPEC,"sbwdroneconfig-uniform-armor.toml"), "sbwdroneconfig-uniform-armor.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("VehicleDriveConfig",ModConfig.Type.SERVER,VehicleDriveConfig.SPEC,"sbwdroneconfig-drive-profiles.toml"), "sbwdroneconfig-drive-profiles.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("VehicleProfilesConfig",ModConfig.Type.SERVER,VehicleProfilesConfig.SPEC,"sbwdroneconfig-vehicle-profiles.toml"), "sbwdroneconfig-vehicle-profiles.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Fl01Config",ModConfig.Type.SERVER,Fl01Config.SPEC,"sbwdroneconfig-flamingo.toml"), "sbwdroneconfig-flamingo.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Hx2Config",ModConfig.Type.SERVER,Hx2Config.SPEC,"sbwdroneconfig-hx2.toml"),"sbwdroneconfig-hx2.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Id01Config",ModConfig.Type.SERVER,Id01Config.SPEC,"sbwdroneconfig-id01.toml"),"sbwdroneconfig-id01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("Rd01Config",ModConfig.Type.SERVER,Rd01Config.SPEC,"sbwdroneconfig-rd01.toml"),"sbwdroneconfig-rd01.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Ds02Config",ModConfig.Type.SERVER,Ds02Config.SPEC,"sbwdroneconfig-ds02.toml"), "sbwdroneconfig-ds02.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigCatalog.capture("Mj03Config",ModConfig.Type.SERVER,Mj03Config.SPEC,"sbwdroneconfig-mj03.toml"), "sbwdroneconfig-mj03.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigCatalog.capture("ExplosionAudioConfig",ModConfig.Type.CLIENT,ExplosionAudioConfig.SPEC,"sbwdroneconfig-explosion-audio.toml"), "sbwdroneconfig-explosion-audio.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigCatalog.capture("DroneOsdConfig",ModConfig.Type.CLIENT,DroneOsdConfig.SPEC,"sbwdroneconfig-osd.toml"), "sbwdroneconfig-osd.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigCatalog.capture("CameraInterferenceConfig",ModConfig.Type.CLIENT,CameraInterferenceConfig.SPEC,"sbwdroneconfig-video-interference.toml"), "sbwdroneconfig-video-interference.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("DvtConfig",ModConfig.Type.CLIENT,DvtConfig.SPEC,"sbwdroneconfig-dvt01.toml"),"sbwdroneconfig-dvt01.toml");
        NeoForge.EVENT_BUS.register(DvtServer.class);
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("InjuryConfig",ModConfig.Type.SERVER,InjuryConfig.SPEC,"sbwdroneconfig-injury.toml"),"sbwdroneconfig-injury.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("InjuryClientConfig",ModConfig.Type.CLIENT,InjuryClientConfig.SPEC,"sbwdroneconfig-injury-client.toml"),"sbwdroneconfig-injury-client.toml");
        NeoForge.EVENT_BUS.register(InjuryEvents.class);
        modContainer.registerConfig(ModConfig.Type.SERVER,ConfigCatalog.capture("DistanceWarConfig",ModConfig.Type.SERVER,DistanceWarConfig.SPEC,"sbwdroneconfig-distance-war.toml"),"sbwdroneconfig-distance-war.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("DistanceWarClientConfig",ModConfig.Type.CLIENT,DistanceWarClientConfig.SPEC,"sbwdroneconfig-distance-war-client.toml"),"sbwdroneconfig-distance-war-client.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT,ConfigCatalog.capture("AddonConfig",ModConfig.Type.CLIENT,AddonConfig.CLIENT_SPEC,"sbwdroneconfig-preferences.toml"),"sbwdroneconfig-preferences.toml");
        ConfigPreflight.globals();
        NeoForge.EVENT_BUS.register(DistanceWarServer.class);
        NeoForge.EVENT_BUS.register(ChunkTicketManager.class);
        NeoForge.EVENT_BUS.register(MissionEvents.class);
        NeoForge.EVENT_BUS.register(DroneClientChunkSyncManager.class);
        NeoForge.EVENT_BUS.register(PlayerDroneAnchorManager.class);
        NeoForge.EVENT_BUS.register(DroneControlEvents.class);
        NeoForge.EVENT_BUS.register(DroneJammerEvents.class);
        NeoForge.EVENT_BUS.register(DroneJamStateManager.class);
        NeoForge.EVENT_BUS.register(DroneInventorySystem.class);
        NeoForge.EVENT_BUS.register(AbandonedFiberCableManager.class);
        NeoForge.EVENT_BUS.register(VehicleAntiDroneNetSyncSystem.class);
        NeoForge.EVENT_BUS.register(Vc01Events.class);
        NeoForge.EVENT_BUS.register(NetBulkPlacementServer.class);
        NeoForge.EVENT_BUS.register(NetBulkPlacementCommand.class);
        NeoForge.EVENT_BUS.register(DeliveryDroneCommand.class);
        NeoForge.EVENT_BUS.register(BayraktarTb2Command.class);
        NeoForge.EVENT_BUS.register(LaserTowerWrenchEvents.class);
        NeoForge.EVENT_BUS.register(FiberOpticDebugCommand.class);
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        AddonNetwork.register(event);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded(SBW_MOD_ID)) {
                SbwDroneRuntimeAccess.initialize();
                  VatagNative.validate();
                DroneTrackingRangeOverride.applyConfiguredRange();
                NeoForge.EVENT_BUS.register(DroneBatterySystem.class);
            }
        });
    }
}
