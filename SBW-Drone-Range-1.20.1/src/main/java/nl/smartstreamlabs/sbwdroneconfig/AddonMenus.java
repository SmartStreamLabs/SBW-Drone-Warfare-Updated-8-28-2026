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

    private AddonMenus() {
    }
}
