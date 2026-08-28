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
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(AddonConfig::onLoad);
        modBus.addListener(this::registerPayloadHandlers);
        modBus.addListener(ChunkTicketManager::registerTicketControllers);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SbwDronePolyMeshFactory.registerForModNamespace();
            DroneConfigMenuClient.init(modContainer);
            FuelMixerClient.init(modBus);
            DroneAudioManager.init(modBus);
            DroneMonitorUseSoundClient.init(modBus);
            DroneThermalVisionClient.init(modBus);
            DroneEnergyClient.init(modBus);
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
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, AddonConfig.SPEC);
        NeoForge.EVENT_BUS.register(ChunkTicketManager.class);
        NeoForge.EVENT_BUS.register(PlayerDroneAnchorManager.class);
        NeoForge.EVENT_BUS.register(DroneControlEvents.class);
        NeoForge.EVENT_BUS.register(DroneJammerEvents.class);
        NeoForge.EVENT_BUS.register(DroneJamStateManager.class);
        NeoForge.EVENT_BUS.register(DroneInventorySystem.class);
        NeoForge.EVENT_BUS.register(AbandonedFiberCableManager.class);
        NeoForge.EVENT_BUS.register(VehicleAntiDroneNetSyncSystem.class);
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
                DroneTrackingRangeOverride.applyConfiguredRange();
                NeoForge.EVENT_BUS.register(DroneBatterySystem.class);
            }
        });
    }
}
