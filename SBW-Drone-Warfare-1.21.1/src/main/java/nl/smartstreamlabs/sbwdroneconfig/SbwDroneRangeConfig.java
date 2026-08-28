package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
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

    public SbwDroneRangeConfig() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AddonCreativeTabs.CREATIVE_MODE_TABS.register(modBus);
        AddonEntities.ENTITY_TYPES.register(modBus);
        AddonBlocks.BLOCKS.register(modBus);
        AddonItems.ITEMS.register(modBus);
        AddonBlockEntities.BLOCK_ENTITY_TYPES.register(modBus);
        AddonMenus.MENU_TYPES.register(modBus);
        AddonSounds.SOUND_EVENTS.register(modBus);
        modBus.addListener(this::onCommonSetup);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            SbwDronePolyMeshFactory.registerForModNamespace();
            DroneConfigMenuClient.init();
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
            VehicleAntiDroneNetClient.init(modBus);
            DeliveryCrateDeployClient.init(modBus);
            AddonClientRenderLayers.init(modBus);
            AddonEntityRenderers.init(modBus);
        }

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AddonConfig.SPEC);
        AddonNetwork.register();
        MinecraftForge.EVENT_BUS.register(ChunkTicketManager.class);
        MinecraftForge.EVENT_BUS.register(PlayerDroneAnchorManager.class);
        MinecraftForge.EVENT_BUS.register(DroneControlEvents.class);
        MinecraftForge.EVENT_BUS.register(DroneJammerEvents.class);
        MinecraftForge.EVENT_BUS.register(DroneJamStateManager.class);
        MinecraftForge.EVENT_BUS.register(DroneInventorySystem.class);
        MinecraftForge.EVENT_BUS.register(AbandonedFiberCableManager.class);
        MinecraftForge.EVENT_BUS.register(VehicleAntiDroneNetSyncSystem.class);
        MinecraftForge.EVENT_BUS.register(NetBulkPlacementServer.class);
        MinecraftForge.EVENT_BUS.register(NetBulkPlacementCommand.class);
        MinecraftForge.EVENT_BUS.register(DeliveryDroneCommand.class);
        MinecraftForge.EVENT_BUS.register(BayraktarTb2Command.class);
        MinecraftForge.EVENT_BUS.register(FiberOpticDebugCommand.class);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded(SBW_MOD_ID)) {
                DroneTrackingRangeOverride.applyConfiguredRange();
                ChunkTicketManager.registerValidationCallback();
                MinecraftForge.EVENT_BUS.register(DroneBatterySystem.class);
            }
        });
    }
}
