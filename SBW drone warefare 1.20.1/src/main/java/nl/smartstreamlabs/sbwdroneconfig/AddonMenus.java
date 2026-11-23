package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class AddonMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SbwDroneRangeConfig.MOD_ID);
    public static final RegistryObject<MenuType<FuelMixerMenu>> FUEL_MIXER = MENU_TYPES.register(
            "fuel_mixer",
            () -> IForgeMenuType.create(FuelMixerMenu::new)
    );

    public static final RegistryObject<MenuType<LucasPlatformMenu>> LUCAS_PLATFORM=MENU_TYPES.register("lucas_launch_platform_lp01",()->IForgeMenuType.create(LucasPlatformMenu::new));

    public static final RegistryObject<MenuType<GeraniumPlatformMenu>> GERANIUM_PLATFORM=MENU_TYPES.register("geranium2_launch_platform_lp02",()->IForgeMenuType.create(GeraniumPlatformMenu::new));
    public static final RegistryObject<MenuType<Fl01Menu>> FL_LP=MENU_TYPES.register("flamingo_launch_platform",()->IForgeMenuType.create(Fl01Menu::new));

    public static final RegistryObject<MenuType<GlidePlatformMenu>> GLIDE_PLATFORM=MENU_TYPES.register("glide_bomb_platform_lp03",()->IForgeMenuType.create(GlidePlatformMenu::new));

    public static final RegistryObject<MenuType<Dc01Menu>> DC01=MENU_TYPES.register("drone_carrier_dc01",()->IForgeMenuType.create(Dc01Menu::new));
    public static final RegistryObject<MenuType<Ndc01Menu>> NDC01=MENU_TYPES.register("naval_drone_carrier_ndc01",()->IForgeMenuType.create(Ndc01Menu::new));
    public static final RegistryObject<MenuType<Gad01Menu>> GAD01=MENU_TYPES.register("ground_assault_drone_gad01",()->IForgeMenuType.create(Gad01Menu::new));
    public static final net.minecraftforge.registries.RegistryObject<MenuType<MplServiceMenu>> MPL_SERVICE=MENU_TYPES.register("mpl01_service",()->net.minecraftforge.common.extensions.IForgeMenuType.create(MplServiceMenu::new));
    public static final RegistryObject<MenuType<PlatformInventoryMenu>> PLATFORM_INVENTORY=MENU_TYPES.register("platform_drone_inventory",()->IForgeMenuType.create(PlatformInventoryMenu::new));
    public static final RegistryObject<MenuType<HxPlatformMenu>> HX_PLATFORM=MENU_TYPES.register("hx_platform",()->IForgeMenuType.create(HxPlatformMenu::new));
    public static final java.util.function.Supplier<MenuType<CarrierFleetMenu>> CARRIER_FLEET=MENU_TYPES.register("carrier_fleet",()->net.minecraftforge.common.extensions.IForgeMenuType.create(CarrierFleetMenu::new));
    private AddonMenus() {
    }

 public static final java.util.function.Supplier<MenuType<DwfMenu>> DWF01=MENU_TYPES.register("fabricator_dwf01",()->IForgeMenuType.create(DwfMenu::new));
}
