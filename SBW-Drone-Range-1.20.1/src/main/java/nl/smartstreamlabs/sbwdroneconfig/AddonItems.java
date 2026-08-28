package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SbwDroneRangeConfig.MOD_ID);
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

    private AddonItems() {
    }
}
