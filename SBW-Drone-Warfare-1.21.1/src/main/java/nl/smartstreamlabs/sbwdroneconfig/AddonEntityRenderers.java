package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class AddonEntityRenderers {
    private AddonEntityRenderers() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(AddonEntityRenderers::registerEntityRenderers);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AddonEntities.CUBED_FPV_DRONE.get(), CubedFpvDroneRenderer::new);
        event.registerEntityRenderer(AddonEntities.LUCAS_DRONE.get(), LucasDroneRenderer::new);
        event.registerEntityRenderer(AddonEntities.DELIVERY_DRONE.get(), DeliveryDroneRenderer::new);
        event.registerEntityRenderer(AddonEntities.GLIDE_BOMB.get(), GlideBombRenderer::new);
        event.registerEntityRenderer(AddonEntities.GERANIUM_2.get(), Geranium2Renderer::new);
        event.registerEntityRenderer(AddonEntities.HEAVY_BOMBER_DRONE.get(), HeavyBomberDroneRenderer::new);
        event.registerEntityRenderer(AddonEntities.UKRAINIAN_BAYRAKTAR_TB2.get(), BayraktarTb2Renderer::new);
        event.registerEntityRenderer(AddonEntities.ANTI_VEHICLE_MORTAR_PAYLOAD.get(), AntiVehicleMortarPayloadRenderer::new);
        event.registerEntityRenderer(AddonEntities.FIBER_OPTIC_CABLE_SEGMENT.get(), FiberOpticCableSegmentRenderer::new);
        event.registerEntityRenderer(AddonEntities.RECOVERABLE_FIBER_OPTIC_CABLE.get(), RecoverableFiberOpticCableRenderer::new);
    }
}
