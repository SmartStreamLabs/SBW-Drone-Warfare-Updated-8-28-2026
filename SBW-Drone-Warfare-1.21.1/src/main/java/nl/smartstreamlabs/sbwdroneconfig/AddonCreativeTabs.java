package nl.smartstreamlabs.sbwdroneconfig;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class AddonCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SbwDroneRangeConfig.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sbwdroneconfig.main"))
                    .icon(() -> new ItemStack(AddonItems.DRONE_JAMMER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(AddonItems.DRONE_DETECTION_SIREN.get());
                        output.accept(com.atsuishio.superbwarfare.init.ModItems.MONITOR.get());output.accept(((com.atsuishio.superbwarfare.item.material.BatteryItem)com.atsuishio.superbwarfare.init.ModItems.BATTERY.get()).makeFullEnergyStack());output.accept(((com.atsuishio.superbwarfare.item.material.BatteryItem)com.atsuishio.superbwarfare.init.ModItems.SMALL_BATTERY_PACK.get()).makeFullEnergyStack());output.accept(((com.atsuishio.superbwarfare.item.material.BatteryItem)com.atsuishio.superbwarfare.init.ModItems.LARGE_BATTERY_PACK.get()).makeFullEnergyStack());
                        output.accept(AddonItems.DS02.get());output.accept(AddonItems.PB01.get());output.accept(AddonItems.ADC01.get());output.accept(com.atsuishio.superbwarfare.init.ModItems.SMALL_SHELL_AA.get());output.accept(AddonItems.PL01.get());output.accept(AddonItems.DR01.get());output.accept(AddonItems.DEFENSE_LINK.get());output.accept(AddonItems.PS01.get());output.accept(AddonItems.UF01.get());output.accept(AddonItems.DVT01.get());output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.GAD01.get()));output.accept(AddonItems.HG01.get());output.accept(AddonItems.LM01.get());output.accept(AddonItems.FIELD_BANDAGE.get());output.accept(AddonItems.TRAUMA_MEDKIT.get());
                        output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.DC01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.WD01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.SD01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.MPL01.get()));output.accept(AddonItems.PM01.get());output.accept(AddonItems.RS01.get());output.accept(AddonItems.SK01_DECORATION.get());output.accept(AddonItems.RCT01.get());output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.QD01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.HGD01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.ADV01.get()));output.accept(Mj03Containers.create());output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.VATAG.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.SR01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.MR01.get()));output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.MT01.get()));
                        output.accept(AddonItems.ANTI_DRONE_NET.get());output.accept(AddonItems.NT01_BUILDER.get());
                        output.accept(AddonItems.ANTI_DRONE_NET_CARPET.get());
                        output.accept(AddonItems.ANTI_DRONE_NET_PANEL.get());
                        output.accept(AddonItems.EXTRACTION_CRATE.get());
                        output.accept(AddonItems.JAMMER.get());
                        output.accept(AddonItems.RADAR_JAMMER_TOWER.get());
                        output.accept(AddonItems.FUEL_MIXER.get());
                        output.accept(AddonItems.NDZ01.get());
                        output.accept(AddonItems.MU01_HELMET.get()); output.accept(AddonItems.MU01_VEST.get()); output.accept(AddonItems.MU01_TROUSERS.get()); output.accept(AddonItems.MU01_BOOTS.get()); output.accept(AddonItems.US01_HELMET.get()); output.accept(AddonItems.US01_VEST.get()); output.accept(AddonItems.US01_TROUSERS.get()); output.accept(AddonItems.US01_BOOTS.get());
                        output.accept(AddonItems.GLIDE_PLATFORM.get());
                        output.accept(AddonItems.GERANIUM_PLATFORM.get());
                        output.accept(AddonItems.FL_LP.get());output.accept(AddonItems.FL01.get());output.accept(com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.NDC01.get()));
                        output.accept(AddonItems.LUCAS_PLATFORM.get());
                        output.accept(AddonItems.DRONE_JAMMER.get());
                        output.accept(Dj01Item.fullStack());output.accept(AddonItems.DIL01.get());
                        output.accept(AddonItems.FPV_GOGGLES.get());
                        output.accept(AddonItems.LASER_TOWER_WRENCH.get());
                        output.accept(AddonItems.SPOTLIGHT_MODULE.get());
                        output.accept(AddonItems.FIBER_OPTIC_SPOOL_UPGRADE.get());
                        output.accept(AddonItems.VEHICLE_ANTI_DRONE_NET_UPGRADE.get());
                        output.accept(AddonItems.ANTI_VEHICLE_MORTAR_PAYLOAD.get());
                        output.accept(AddonItems.EMPTY_JERRYCAN.get());
                        output.accept(AddonItems.GASOLINE_CANISTER.get());
                        output.accept(AddonItems.GASOLINE_JERRYCAN.get());
                        output.accept(AddonItems.CUBED_FPV_DRONE.get());
                        output.accept(AddonItems.HX2.get());output.accept(AddonItems.HX_PLATFORM.get());
                        output.accept(AddonItems.ID01.get());output.accept(AddonItems.RD01.get());
                        output.accept(AddonItems.DWF01.get());output.accept(AddonItems.LUCAS_DRONE.get());
                        output.accept(AddonItems.DELIVERY_DRONE.get());
                        output.accept(AddonItems.GLIDE_BOMB.get());
                        output.accept(AddonItems.GERANIUM_2.get());
                        output.accept(AddonItems.HEAVY_BOMBER_DRONE.get());
                        output.accept(AddonItems.UKRAINIAN_BAYRAKTAR_TB2.get());
                    })
                    .build()
    );

    private AddonCreativeTabs() {
    }
}
