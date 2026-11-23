package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, SbwDroneRangeConfig.MOD_ID);
    public static final DeferredHolder<MenuType<?>, MenuType<FuelMixerMenu>> FUEL_MIXER = MENU_TYPES.register(
            "fuel_mixer",
            () -> IMenuTypeExtension.create(FuelMixerMenu::new)
    );

    public static final DeferredHolder<MenuType<?>, MenuType<LucasPlatformMenu>> LUCAS_PLATFORM=MENU_TYPES.register("lucas_launch_platform_lp01",()->IMenuTypeExtension.create(LucasPlatformMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GeraniumPlatformMenu>> GERANIUM_PLATFORM=MENU_TYPES.register("geranium2_launch_platform_lp02",()->IMenuTypeExtension.create(GeraniumPlatformMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<Fl01Menu>> FL_LP=MENU_TYPES.register("flamingo_launch_platform",()->IMenuTypeExtension.create(Fl01Menu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<GlidePlatformMenu>> GLIDE_PLATFORM=MENU_TYPES.register("glide_bomb_platform_lp03",()->IMenuTypeExtension.create(GlidePlatformMenu::new));

    public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>,MenuType<Dc01Menu>> DC01=MENU_TYPES.register("drone_carrier_dc01",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(Dc01Menu::new));
    public static final net.neoforged.neoforge.registries.DeferredHolder<MenuType<?>,MenuType<Ndc01Menu>> NDC01=MENU_TYPES.register("naval_drone_carrier_ndc01",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(Ndc01Menu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<Gad01Menu>> GAD01=MENU_TYPES.register("ground_assault_drone_gad01",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(Gad01Menu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<MplServiceMenu>> MPL_SERVICE=MENU_TYPES.register("mpl01_service",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(MplServiceMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<PlatformInventoryMenu>> PLATFORM_INVENTORY=MENU_TYPES.register("platform_drone_inventory",()->IMenuTypeExtension.create(PlatformInventoryMenu::new));
    public static final DeferredHolder<MenuType<?>,MenuType<HxPlatformMenu>> HX_PLATFORM=MENU_TYPES.register("hx_platform",()->IMenuTypeExtension.create(HxPlatformMenu::new));
    public static final java.util.function.Supplier<MenuType<CarrierFleetMenu>> CARRIER_FLEET=MENU_TYPES.register("carrier_fleet",()->IMenuTypeExtension.create(CarrierFleetMenu::new));
    private AddonMenus() {
    }

 public static final java.util.function.Supplier<MenuType<DwfMenu>> DWF01=MENU_TYPES.register("fabricator_dwf01",()->IMenuTypeExtension.create(DwfMenu::new));
}
