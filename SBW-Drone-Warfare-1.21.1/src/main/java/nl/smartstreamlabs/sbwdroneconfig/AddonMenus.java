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

    private AddonMenus() {
    }
}
