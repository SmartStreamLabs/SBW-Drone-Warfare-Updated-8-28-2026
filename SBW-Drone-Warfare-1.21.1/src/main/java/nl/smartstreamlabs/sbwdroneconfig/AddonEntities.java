package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, SbwDroneRangeConfig.MOD_ID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Id01Entity>> ID01=ENTITY_TYPES.register("interceptor_drone_id01",()->EntityType.Builder.<Id01Entity>of(Id01Entity::new,MobCategory.MISC).sized(.5f,2.92f).clientTrackingRange(32).updateInterval(1).build("interceptor_drone_id01"));

    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<VatagEntity>> VATAG=ENTITY_TYPES.register("vatag_ugv_v01",()->EntityType.Builder.<VatagEntity>of(VatagEntity::new,MobCategory.MISC).sized(3.6f,2.8f).clientTrackingRange(256).updateInterval(1).build("vatag_ugv_v01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<SupplyRoverEntity>> SR01=ENTITY_TYPES.register("supply_rover_sr01",()->EntityType.Builder.<SupplyRoverEntity>of(SupplyRoverEntity::new,MobCategory.MISC).sized(2.1f,2.9f).clientTrackingRange(256).updateInterval(1).build("supply_rover_sr01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<MineRoverEntity>> MR01=ENTITY_TYPES.register("mine_rover_mr01",()->EntityType.Builder.<MineRoverEntity>of(MineRoverEntity::new,MobCategory.MISC).sized(2.03f,2.03f).clientTrackingRange(256).updateInterval(1).build("mine_rover_mr01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<MedicalTrailerEntity>> MT01=ENTITY_TYPES.register("medical_trailer_mt01",()->EntityType.Builder.<MedicalTrailerEntity>of(MedicalTrailerEntity::new,MobCategory.MISC).sized(2.1f,1.3f).clientTrackingRange(256).updateInterval(1).build("medical_trailer_mt01"));

    public static final DeferredHolder<EntityType<?>, EntityType<Mj03Entity>> MJ03=ENTITY_TYPES.register("mobile_jammer_truck_mj03",()->EntityType.Builder.<Mj03Entity>of(Mj03Entity::new,MobCategory.MISC).sized(2.6f,3.1f).clientTrackingRange(16).updateInterval(1).build("mobile_jammer_truck_mj03"));
    public static final DeferredHolder<EntityType<?>, EntityType<CubedFpvDroneEntity>> CUBED_FPV_DRONE = ENTITY_TYPES.register(
            CubedFpvDroneDefinition.ID,
            () -> EntityType.Builder.<CubedFpvDroneEntity>of(CubedFpvDroneEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(0.6F, 0.2F)
                    .build(CubedFpvDroneDefinition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<LucasDroneEntity>> LUCAS_DRONE = ENTITY_TYPES.register(
            LucasDroneDefinition.ID,
            () -> EntityType.Builder.<LucasDroneEntity>of(LucasDroneEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(3.2F, 0.35F)
                    .build(LucasDroneDefinition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<DeliveryDroneEntity>> DELIVERY_DRONE = ENTITY_TYPES.register(
            DeliveryDroneDefinition.ID,
            () -> EntityType.Builder.<DeliveryDroneEntity>of(DeliveryDroneEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(1.25F, 0.35F)
                    .build(DeliveryDroneDefinition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<GlideBombEntity>> GLIDE_BOMB = ENTITY_TYPES.register(
            GlideBombDefinition.ID,
            () -> EntityType.Builder.<GlideBombEntity>of(GlideBombEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(0.85F, 0.25F)
                    .build(GlideBombDefinition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<Geranium2Entity>> GERANIUM_2 = ENTITY_TYPES.register(
            Geranium2Definition.ID,
            () -> EntityType.Builder.<Geranium2Entity>of(Geranium2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(2.6F, 0.35F)
                    .build(Geranium2Definition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<HeavyBomberDroneEntity>> HEAVY_BOMBER_DRONE = ENTITY_TYPES.register(
            HeavyBomberDroneDefinition.ID,
            () -> EntityType.Builder.<HeavyBomberDroneEntity>of(HeavyBomberDroneEntity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(1.65F, 0.45F)
                    .build(HeavyBomberDroneDefinition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<BayraktarTb2Entity>> UKRAINIAN_BAYRAKTAR_TB2 = ENTITY_TYPES.register(
            BayraktarTb2Definition.ID,
            () -> EntityType.Builder.<BayraktarTb2Entity>of(BayraktarTb2Entity::new, MobCategory.MISC)
                    .setTrackingRange(512)
                    .setUpdateInterval(1)
                    .sized(4.5F, 1.1F)
                    .build(BayraktarTb2Definition.ID)
    );

    public static final DeferredHolder<EntityType<?>, EntityType<FiberOpticCableSegmentEntity>> FIBER_OPTIC_CABLE_SEGMENT = ENTITY_TYPES.register(
            "fiber_optic_cable_segment",
            () -> EntityType.Builder.<FiberOpticCableSegmentEntity>of(FiberOpticCableSegmentEntity::new, MobCategory.MISC)
                    .setTrackingRange(96)
                    .setUpdateInterval(10)
                    .sized(0.35F, 0.35F)
                    .build("fiber_optic_cable_segment")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<RecoverableFiberOpticCableEntity>> RECOVERABLE_FIBER_OPTIC_CABLE = ENTITY_TYPES.register(
            "recoverable_fiber_optic_cable",
            () -> EntityType.Builder.<RecoverableFiberOpticCableEntity>of(RecoverableFiberOpticCableEntity::new, MobCategory.MISC)
                    .setTrackingRange(128)
                    .setUpdateInterval(10)
                    .sized(0.45F, 0.2F)
                    .build("recoverable_fiber_optic_cable")
    );

    public static final DeferredHolder<EntityType<?>, EntityType<AntiVehicleMortarPayloadEntity>> ANTI_VEHICLE_MORTAR_PAYLOAD = ENTITY_TYPES.register(
            "anti_vehicle_mortar_payload",
            () -> EntityType.Builder.<AntiVehicleMortarPayloadEntity>of(AntiVehicleMortarPayloadEntity::new, MobCategory.MISC)
                    .setTrackingRange(128)
                    .setUpdateInterval(1)
                    .sized(0.35F, 0.35F)
                    .build("anti_vehicle_mortar_payload")
    );

    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Rd01Entity>> RD01=ENTITY_TYPES.register("rescue_drone_rd01",()->EntityType.Builder.<Rd01Entity>of(Rd01Entity::new,MobCategory.MISC).sized(1.5f,2.4f).clientTrackingRange(32).updateInterval(1).build("rescue_drone_rd01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Rd01HarnessEntity>> RD01_HARNESS=ENTITY_TYPES.register("rd01_harness",()->EntityType.Builder.<Rd01HarnessEntity>of(Rd01HarnessEntity::new,MobCategory.MISC).sized(1.0f,1.6f).clientTrackingRange(32).updateInterval(1).build("rd01_harness"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Ps01Seat>> PS01_SEAT=ENTITY_TYPES.register("ps01_seat",()->EntityType.Builder.<Ps01Seat>of(Ps01Seat::new,MobCategory.MISC).sized(.05f,.05f).clientTrackingRange(32).updateInterval(1).build("ps01_seat"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Pb01Seat>> PB01_SEAT=ENTITY_TYPES.register("pb01_seat",()->EntityType.Builder.<Pb01Seat>of(Pb01Seat::new,MobCategory.MISC).sized(.05f,.05f).clientTrackingRange(32).updateInterval(1).build("pb01_seat"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Dc01Entity>> DC01=ENTITY_TYPES.register("drone_carrier_dc01",()->EntityType.Builder.<Dc01Entity>of(Dc01Entity::new,MobCategory.MISC).sized(2.95f,1.48f).clientTrackingRange(256).updateInterval(1).build("drone_carrier_dc01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Wd01Entity>> WD01=ENTITY_TYPES.register("water_drone_wd01",()->EntityType.Builder.<Wd01Entity>of(Wd01Entity::new,MobCategory.MISC).sized(1.76f,1f).clientTrackingRange(256).updateInterval(1).build("water_drone_wd01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Ndc01Entity>> NDC01=ENTITY_TYPES.register("naval_drone_carrier_ndc01",()->EntityType.Builder.<Ndc01Entity>of(Ndc01Entity::new,MobCategory.MISC).sized(3.4f,4.46f).clientTrackingRange(256).updateInterval(1).build("naval_drone_carrier_ndc01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Sd01Entity>> SD01=ENTITY_TYPES.register("submarine_drone_sd01",()->EntityType.Builder.<Sd01Entity>of(Sd01Entity::new,MobCategory.MISC).sized(.9f,1.6f).clientTrackingRange(256).updateInterval(1).build("submarine_drone_sd01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Qd01Entity>> QD01=ENTITY_TYPES.register("quadruped_drone_qd01",()->EntityType.Builder.<Qd01Entity>of(Qd01Entity::new,MobCategory.MISC).sized(1.41f,1.77f).clientTrackingRange(256).updateInterval(1).build("quadruped_drone_qd01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Hgd01Entity>> HGD01=ENTITY_TYPES.register("heavy_ground_drone_hgd01",()->EntityType.Builder.<Hgd01Entity>of(Hgd01Entity::new,MobCategory.MISC).sized(3.48f,3.14f).clientTrackingRange(256).updateInterval(1).build("heavy_ground_drone_hgd01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Adv01Entity>> ADV01=ENTITY_TYPES.register("anti_drone_vehicle_adv01",()->EntityType.Builder.<Adv01Entity>of(Adv01Entity::new,MobCategory.MISC).sized(3.26f,2.5f).clientTrackingRange(256).updateInterval(1).build("anti_drone_vehicle_adv01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Mpl01Entity>> MPL01=ENTITY_TYPES.register("mobile_patriot_launcher_mpl01",()->EntityType.Builder.<Mpl01Entity>of(Mpl01Entity::new,MobCategory.MISC).sized(3.2f,4.3f).clientTrackingRange(256).updateInterval(1).build("mobile_patriot_launcher_mpl01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Pm01Entity>> PM01=ENTITY_TYPES.register("patriot_missile_pm01",()->EntityType.Builder.<Pm01Entity>of(Pm01Entity::new,MobCategory.MISC).sized(.35f,.35f).clientTrackingRange(256).updateInterval(1).build("patriot_missile_pm01"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Adv01Shell>> ADV01_SHELL=ENTITY_TYPES.register("adv01_flak_shell",()->EntityType.Builder.<Adv01Shell>of(Adv01Shell::new,MobCategory.MISC).sized(.14f,.14f).clientTrackingRange(256).updateInterval(1).build("adv01_flak_shell"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Hx2Entity>> HX2=ENTITY_TYPES.register("hx2_drone",()->EntityType.Builder.<Hx2Entity>of(Hx2Entity::new,MobCategory.MISC).sized(.34f,.34f).clientTrackingRange(128).updateInterval(1).build("hx2_drone"));
    public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<Fl01Entity>> FL01=ENTITY_TYPES.register("flamingo_fl01",()->EntityType.Builder.<Fl01Entity>of(Fl01Entity::new,MobCategory.MISC).sized(1.4f,1.4f).clientTrackingRange(128).updateInterval(1).build("flamingo_fl01"));
    public static final DeferredHolder<EntityType<?>,EntityType<Gad01Entity>> GAD01=ENTITY_TYPES.register("ground_assault_drone_gad01",()->EntityType.Builder.<Gad01Entity>of(Gad01Entity::new,MobCategory.MISC).sized(1.262f,.78f).clientTrackingRange(128).updateInterval(1).build("ground_assault_drone_gad01"));
    public static final DeferredHolder<EntityType<?>,EntityType<Hg01Entity>> HG01=ENTITY_TYPES.register("hand_grenade_hg01",()->EntityType.Builder.<Hg01Entity>of(Hg01Entity::new,MobCategory.MISC).sized(.14f,.20f).clientTrackingRange(64).updateInterval(1).build("hand_grenade_hg01"));
    public static final java.util.function.Supplier<EntityType<RubbleFragmentEntity>> RUBBLE_FRAGMENT=ENTITY_TYPES.register("rubble_fragment",()->EntityType.Builder.<RubbleFragmentEntity>of(RubbleFragmentEntity::new,net.minecraft.world.entity.MobCategory.MISC).sized(.4f,.4f).clientTrackingRange(48).updateInterval(3).build("rubble_fragment"));
    private AddonEntities() {
    }
 public static final java.util.function.Supplier<net.minecraft.world.entity.EntityType<AdcShell>> ADC01_SHELL=ENTITY_TYPES.register("adc01_shell",()->net.minecraft.world.entity.EntityType.Builder.<AdcShell>of(AdcShell::new,net.minecraft.world.entity.MobCategory.MISC).sized(.15f,.15f).clientTrackingRange(160).updateInterval(1).build("adc01_shell"));
}
