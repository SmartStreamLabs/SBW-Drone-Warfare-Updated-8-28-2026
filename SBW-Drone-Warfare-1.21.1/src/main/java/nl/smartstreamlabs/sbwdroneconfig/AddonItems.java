package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SbwDroneRangeConfig.MOD_ID);
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> DIL01=ITEMS.register("drone_interceptor_launcher_dil01",Dil01Item::new);
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Wd01Item> WD01=ITEMS.register("water_drone_wd01",()->new Wd01Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Ndc01Item> NDC01=ITEMS.register("naval_drone_carrier_ndc01",()->new Ndc01Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Sd01Item> SD01=ITEMS.register("submarine_drone_sd01",()->new Sd01Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Qd01Item> QD01=ITEMS.register("quadruped_drone_qd01",()->new Qd01Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item,Dj01Item> DJ01=ITEMS.register("directional_drone_jammer_dj01",()->new Dj01Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,NtItem> NT01_BUILDER=ITEMS.register("nt01_tunnel_builder",()->new NtItem(new Item.Properties().stacksTo(1)));











    public static final DeferredHolder<Item,Item> ID01=ITEMS.register("interceptor_drone_id01",()->new Id01Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> RD01=ITEMS.register("rescue_drone_rd01",()->new Rd01Item(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DRONE_DETECTION_SIREN = ITEMS.register(
            "drone_detection_siren",
            () -> new TooltipBlockItem(
                    AddonBlocks.DRONE_DETECTION_SIREN.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.drone_detection_siren.1",
                    "tooltip.sbwdroneconfig.drone_detection_siren.2"
            )
    );
    public static final DeferredHolder<Item, Item> ANTI_DRONE_NET = ITEMS.register(
            "anti_drone_net",
            () -> new TooltipBlockItem(
                    AddonBlocks.ANTI_DRONE_NET.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.anti_drone_net.1",
                    "tooltip.sbwdroneconfig.anti_drone_net.2"
            )
    );
    public static final DeferredHolder<Item, Item> ANTI_DRONE_NET_CARPET = ITEMS.register(
            "anti_drone_net_carpet",
            () -> new TooltipBlockItem(
                    AddonBlocks.ANTI_DRONE_NET_CARPET.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.anti_drone_net_carpet.1",
                    "tooltip.sbwdroneconfig.anti_drone_net_carpet.2"
            )
    );
    public static final DeferredHolder<Item, Item> ANTI_DRONE_NET_PANEL = ITEMS.register(
            "anti_drone_net_panel",
            () -> new TooltipBlockItem(
                    AddonBlocks.ANTI_DRONE_NET_PANEL.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.anti_drone_net_panel.1",
                    "tooltip.sbwdroneconfig.anti_drone_net_panel.2"
            )
    );
    public static final DeferredHolder<Item, Item> EXTRACTION_CRATE = ITEMS.register(
            "extraction_crate",
            () -> new TooltipBlockItem(
                    AddonBlocks.EXTRACTION_CRATE.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.extraction_crate.1",
                    "tooltip.sbwdroneconfig.extraction_crate.2"
            )
    );
    public static final DeferredHolder<Item, Item> JAMMER = ITEMS.register(
            "jammer",
            () -> new TooltipBlockItem(
                    AddonBlocks.JAMMER.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.jammer.1",
                    "tooltip.sbwdroneconfig.jammer.2"
            )
    );
    public static final DeferredHolder<Item, Item> RADAR_JAMMER_TOWER = ITEMS.register(
            "radar_jammer_tower",
            () -> new TooltipBlockItem(
                    AddonBlocks.RADAR_JAMMER_TOWER.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.radar_jammer_tower.1",
                    "tooltip.sbwdroneconfig.radar_jammer_tower.2",
                    "tooltip.sbwdroneconfig.radar_jammer_tower.3"
            )
    );
    public static final DeferredHolder<Item, Item> FUEL_MIXER = ITEMS.register(
            "fuel_mixer",
            () -> new TooltipBlockItem(
                    AddonBlocks.FUEL_MIXER.get(),
                    new Item.Properties(),
                    "tooltip.sbwdroneconfig.fuel_mixer.1",
                    "tooltip.sbwdroneconfig.fuel_mixer.2",
                    "tooltip.sbwdroneconfig.fuel_mixer.3"
            )
    );
    public static final DeferredHolder<Item, Item> DRONE_JAMMER = ITEMS.register(
            "drone_jammer",
            () -> new DroneJammerItem(new Item.Properties().stacksTo(1))
    );
    public static final DeferredHolder<Item, Item> FPV_GOGGLES = ITEMS.register(
            "fpv_goggles",
            () -> new FpvGogglesItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    "tooltip.sbwdroneconfig.fpv_goggles.1",
                    "tooltip.sbwdroneconfig.fpv_goggles.2",
                    "tooltip.sbwdroneconfig.fpv_goggles.3"
            )
    );
    public static final DeferredHolder<Item, Item> LASER_TOWER_WRENCH = ITEMS.register(
            "laser_tower_wrench",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(1),
                    "tooltip.sbwdroneconfig.laser_tower_wrench.1",
                    "tooltip.sbwdroneconfig.laser_tower_wrench.2"
            )
    );
    public static final DeferredHolder<Item, Item> SPOTLIGHT_MODULE = ITEMS.register(
            "spotlight_module",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(1),
                    "tooltip.sbwdroneconfig.spotlight_module.1",
                    "tooltip.sbwdroneconfig.spotlight_module.2"
            )
    );
    public static final DeferredHolder<Item, Item> FIBER_OPTIC_SPOOL_UPGRADE = ITEMS.register(
            "fiber_optic_spool_upgrade",
            () -> new TooltipItem(
                    new Item.Properties().durability(16),
                    "tooltip.sbwdroneconfig.fiber_optic_spool_upgrade.1",
                    "tooltip.sbwdroneconfig.fiber_optic_spool_upgrade.2"
            )
    );
    public static final DeferredHolder<Item, Item> VEHICLE_ANTI_DRONE_NET_UPGRADE = ITEMS.register(
            "vehicle_anti_drone_net_upgrade",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON),
                    "tooltip.sbwdroneconfig.vehicle_anti_drone_net_upgrade.1",
                    "tooltip.sbwdroneconfig.vehicle_anti_drone_net_upgrade.2",
                    "tooltip.sbwdroneconfig.vehicle_anti_drone_net_upgrade.3"
            )
    );
    public static final DeferredHolder<Item, Item> ANTI_VEHICLE_MORTAR_PAYLOAD = ITEMS.register(
            "anti_vehicle_mortar_payload",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(16).rarity(Rarity.RARE),
                    "tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.1",
                    "tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.2",
                    "tooltip.sbwdroneconfig.anti_vehicle_mortar_payload.3"
            )
    );
    public static final DeferredHolder<Item, Item> EMPTY_JERRYCAN = ITEMS.register(
            "empty_jerrycan",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(16),
                    "tooltip.sbwdroneconfig.empty_jerrycan.1",
                    "tooltip.sbwdroneconfig.empty_jerrycan.2"
            )
    );
    public static final DeferredHolder<Item, Item> GASOLINE_CANISTER = ITEMS.register(
            "gasoline_canister",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(16),
                    "tooltip.sbwdroneconfig.gasoline_canister.1"
            )
    );
    public static final DeferredHolder<Item, Item> GASOLINE_JERRYCAN = ITEMS.register(
            "gasoline_jerrycan",
            () -> new TooltipItem(
                    new Item.Properties().stacksTo(16),
                    "tooltip.sbwdroneconfig.gasoline_jerrycan.1",
                    "tooltip.sbwdroneconfig.gasoline_jerrycan.2"
            )
    );
    public static final DeferredHolder<Item, Item> CUBED_FPV_DRONE = ITEMS.register(
            CubedFpvDroneDefinition.ID,
            () -> new CubedFpvDroneItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, Item> LUCAS_DRONE = ITEMS.register(
            LucasDroneDefinition.ID,
            () -> new LucasDroneItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, Item> DELIVERY_DRONE = ITEMS.register(
            DeliveryDroneDefinition.ID,
            () -> new DeliveryDroneItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON))
    );
    public static final DeferredHolder<Item, Item> GLIDE_BOMB = ITEMS.register(
            GlideBombDefinition.ID,
            () -> new GlideBombItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, Item> GERANIUM_2 = ITEMS.register(
            Geranium2Definition.ID,
            () -> new Geranium2Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, Item> HEAVY_BOMBER_DRONE = ITEMS.register(
            HeavyBomberDroneDefinition.ID,
            () -> new HeavyBomberDroneItem(new Item.Properties().stacksTo(64).rarity(Rarity.RARE))
    );
    public static final DeferredHolder<Item, Item> UKRAINIAN_BAYRAKTAR_TB2 = ITEMS.register(
            BayraktarTb2Definition.ID,
            () -> new BayraktarTb2Item(new Item.Properties().stacksTo(64).rarity(Rarity.RARE))
    );



    public static final DeferredHolder<Item, Item> LUCAS_PLATFORM=ITEMS.register("lucas_launch_platform_lp01",()->new net.minecraft.world.item.BlockItem(AddonBlocks.LUCAS_PLATFORM.get(),new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> GERANIUM_PLATFORM=ITEMS.register("geranium2_launch_platform_lp02",()->new net.minecraft.world.item.BlockItem(AddonBlocks.GERANIUM_PLATFORM.get(),new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> FL_LP=ITEMS.register("flamingo_launch_platform",()->new Fl01PlatformItem(AddonBlocks.FL_LP.get(),new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> FL01=ITEMS.register("flamingo_fl01",()->new Fl01Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, Item> GLIDE_PLATFORM=ITEMS.register("glide_bomb_platform_lp03",()->new net.minecraft.world.item.BlockItem(AddonBlocks.GLIDE_PLATFORM.get(),new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Item, Item> DS02=ITEMS.register("drone_warning_siren_ds02",()->new net.minecraft.world.item.BlockItem(AddonBlocks.DS02.get(),new Item.Properties().stacksTo(16)));

    public static final DeferredHolder<Item, Item> MJ03=ITEMS.register("mobile_jammer_truck_mj03",()->new Mj03Item(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> HX_PLATFORM=ITEMS.register("hx2_launch_platform",()->new HxPlatformItem(new Item.Properties().stacksTo(1)));
    public static final net.neoforged.neoforge.registries.DeferredHolder<Item,Item> HX2=ITEMS.register("hx2_drone",()->new Hx2Item(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> NDZ01=ITEMS.register("no_drone_zone_sign_ndz01",()->new Ndz01Item(AddonBlocks.NDZ01.get(),new Item.Properties()));
    public static final DeferredHolder<Item, Item> MU01_HELMET=ITEMS.register("mu01_tactical_helmet",()->new Mu01ArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET));
    public static final DeferredHolder<Item, Item> MU01_VEST=ITEMS.register("mu01_tactical_vest",()->new Mu01ArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
    public static final DeferredHolder<Item, Item> MU01_TROUSERS=ITEMS.register("mu01_combat_trousers",()->new Mu01ArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
    public static final DeferredHolder<Item, Item> MU01_BOOTS=ITEMS.register("mu01_combat_boots",()->new Mu01ArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS));
    public static final DeferredHolder<Item, Item> UF01=ITEMS.register("ukrainian_flag_uf01",()->new Uf01Item(AddonBlocks.UF01.get(),new Item.Properties()));
    public static final DeferredHolder<Item, Item> DVT01=ITEMS.register("drone_video_terminal_dvt01",()->new Dvt01Item(AddonBlocks.DVT01.get(),new Item.Properties()));
    public static final DeferredHolder<Item, Item> FIELD_BANDAGE=ITEMS.register("field_bandage",()->new MedicalItem(false,new Item.Properties().stacksTo(16)));
    public static final DeferredHolder<Item, Item> TRAUMA_MEDKIT=ITEMS.register("trauma_medkit",()->new MedicalItem(true,new Item.Properties().stacksTo(4)));
    public static final DeferredHolder<Item,Item> GAD01=ITEMS.register("ground_assault_drone_gad01",()->new Gad01Item(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final DeferredHolder<Item,Item> HG01=ITEMS.register("hand_grenade_hg01",()->new Hg01Item(new Item.Properties().stacksTo(16)));
    public static final java.util.function.Supplier<Item> RS01=ITEMS.register("rubble_shovel_rs01",RubbleShovelItem::new), RCT01=ITEMS.register("rubble_clearing_tool_rct01",RubbleClearingItem::new);
    public static final java.util.function.Supplier<Item> PM01=ITEMS.register("patriot_missile_pm01",PatriotItem::new);
    public static final java.util.function.Supplier<Item> PS01=ITEMS.register("protective_shelter_ps01",()->new Ps01Item(AddonBlocks.PS01.get(),new Item.Properties().stacksTo(1)));
    public static final java.util.function.Supplier<Item> PL01=ITEMS.register("patriot_launcher_pl01",()->new DefenseItem(AddonBlocks.PL01.get(),new Item.Properties().stacksTo(1),0)),DR01=ITEMS.register("defense_radar_dr01",()->new DefenseItem(AddonBlocks.DR01.get(),new Item.Properties().stacksTo(1),1)),DEFENSE_LINK=ITEMS.register("defense_link_tool",()->new DefenseLinkTool(new Item.Properties().stacksTo(1)));
    public static final java.util.function.Supplier<Item> LM01=ITEMS.register("landmine_lm01",Lm01Item::new);
    private AddonItems() {
    }
    public static final java.util.function.Supplier<Item> ADC01=ITEMS.register("anti_drone_cannon_adc01",()->new AdcItem(AddonBlocks.ADC01.get(),new Item.Properties().stacksTo(1)));
 public static final java.util.function.Supplier<Item> PB01=ITEMS.register("protective_bunker_pb01",()->new Pb01Item(AddonBlocks.PB01.get(),new Item.Properties().stacksTo(1)));
 public static final java.util.function.Supplier<Item> US01_HELMET=ITEMS.register("us01_tactical_helmet",()->new Us01ArmorItem(net.minecraft.world.item.ArmorItem.Type.HELMET));
 public static final java.util.function.Supplier<Item> US01_VEST=ITEMS.register("us01_tactical_vest",()->new Us01ArmorItem(net.minecraft.world.item.ArmorItem.Type.CHESTPLATE));
 public static final java.util.function.Supplier<Item> US01_TROUSERS=ITEMS.register("us01_combat_trousers",()->new Us01ArmorItem(net.minecraft.world.item.ArmorItem.Type.LEGGINGS));
 public static final java.util.function.Supplier<Item> US01_BOOTS=ITEMS.register("us01_combat_boots",()->new Us01ArmorItem(net.minecraft.world.item.ArmorItem.Type.BOOTS));

 public static final java.util.function.Supplier<Item> DWF01=ITEMS.register("fabricator_dwf01",()->new DwfItem(AddonBlocks.DWF01.get(),new Item.Properties().stacksTo(1)));
 public static final java.util.function.Supplier<Item> SK01_DECORATION=ITEMS.register("soldier_skeleton_sk01",()->new Sk01DecorationItem(AddonBlocks.SK01_DECORATION.get(),new Item.Properties()));
}
