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
        event.registerBlockEntityRenderer(AddonBlockEntities.SK01_DECORATION.get(),context->new Sk01DecorationRenderer());event.registerBlockEntityRenderer(AddonBlockEntities.DVT01.get(),context->new Dvt01Renderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.UF01.get(),context->new Uf01Renderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.PS01.get(),context->new Ps01Renderer());event.registerBlockEntityRenderer(AddonBlockEntities.PB01.get(),context->new Pb01Renderer());event.registerEntityRenderer(AddonEntities.PB01_SEAT.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);event.registerBlockEntityRenderer(AddonBlockEntities.ADC01.get(),context->new AdcRenderer());event.registerEntityRenderer(AddonEntities.ADC01_SHELL.get(),context->(net.minecraft.client.renderer.entity.EntityRenderer<AdcShell>)(net.minecraft.client.renderer.entity.EntityRenderer<?>)new com.atsuishio.superbwarfare.client.renderer.projectile.SmallCannonShellEntityRenderer(context));event.registerBlockEntityRenderer(AddonBlockEntities.DEFENSE.get(),context->new DefenseRenderer());event.registerEntityRenderer(AddonEntities.PS01_SEAT.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerBlockEntityRenderer(AddonBlockEntities.RADAR_JAMMER_TOWER.get(),context->new Rj01Renderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.FUEL_MIXER.get(),context->new Fm01Renderer());
        event.registerEntityRenderer(AddonEntities.RUBBLE_FRAGMENT.get(),RubbleFragmentRenderer::new);event.registerEntityRenderer(AddonEntities.SD01.get(),Sd01Renderer::new);event.registerEntityRenderer(AddonEntities.NDC01.get(),Ndc01Renderer::new);event.registerEntityRenderer(AddonEntities.WD01.get(),Wd01Renderer::new);event.registerEntityRenderer(AddonEntities.QD01.get(),Qd01Renderer::new);event.registerEntityRenderer(AddonEntities.HGD01.get(),Hgd01Renderer::new);
        event.registerEntityRenderer(AddonEntities.ADV01.get(),Adv01Renderer::new);
        event.registerEntityRenderer(AddonEntities.MPL01.get(),Mpl01Renderer::new);event.registerEntityRenderer(AddonEntities.PM01.get(),Pm01Renderer::new);
        event.registerEntityRenderer(AddonEntities.ADV01_SHELL.get(),context->(net.minecraft.client.renderer.entity.EntityRenderer<Adv01Shell>)(net.minecraft.client.renderer.entity.EntityRenderer<?>)new com.atsuishio.superbwarfare.client.renderer.projectile.SmallCannonShellEntityRenderer(context));
        event.registerEntityRenderer(AddonEntities.HG01.get(),Hg01Renderer::new);event.registerEntityRenderer(AddonEntities.GAD01.get(),Gad01Renderer::new);event.registerEntityRenderer(AddonEntities.DC01.get(),Dc01Renderer::new);
        event.registerEntityRenderer(AddonEntities.VATAG.get(), VatagRenderer::new);
        event.registerEntityRenderer(AddonEntities.MR01.get(), MineRoverRenderer::new);
        event.registerEntityRenderer(AddonEntities.HX2.get(),Hx2Renderer::new);
        event.registerEntityRenderer(AddonEntities.FL01.get(),Fl01Renderers.Aircraft::new);event.registerBlockEntityRenderer(AddonBlockEntities.FL_LP.get(),Fl01Renderers.Platform::new);event.registerBlockEntityRenderer(AddonBlockEntities.HX_PLATFORM.get(),context->new HxPlatformRenderer());
        event.registerEntityRenderer(AddonEntities.ID01.get(),Id01Renderer::new);
        event.registerEntityRenderer(AddonEntities.RD01.get(),Rd01Renderer::new);
        event.registerEntityRenderer(AddonEntities.RD01_HARNESS.get(),net.minecraft.client.renderer.entity.NoopRenderer::new);
        event.registerEntityRenderer(AddonEntities.SR01.get(), c -> new RescueRenderer<SupplyRoverEntity>(c,"supply_rover_sr01"));
        event.registerEntityRenderer(AddonEntities.MT01.get(), c -> new RescueRenderer<MedicalTrailerEntity>(c,"medical_trailer_mt01"));
        event.registerBlockEntityRenderer(AddonBlockEntities.DS02.get(),context->new Ds02Renderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.LUCAS_PLATFORM.get(),context->new LucasPlatformRenderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.GERANIUM_PLATFORM.get(),context->new GeraniumPlatformRenderer());
        event.registerBlockEntityRenderer(AddonBlockEntities.GLIDE_PLATFORM.get(),context->new GlidePlatformRenderer());
        event.registerEntityRenderer(AddonEntities.CUBED_FPV_DRONE.get(), CubedFpvDroneRenderer::new);
        event.registerEntityRenderer(AddonEntities.MJ03.get(),Mj03Renderer::new);
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
