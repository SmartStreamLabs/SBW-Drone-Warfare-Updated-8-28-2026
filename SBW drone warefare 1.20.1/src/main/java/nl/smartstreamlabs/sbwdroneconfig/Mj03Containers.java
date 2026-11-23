package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
public final class Mj03Containers {
 public static net.minecraft.world.item.ItemStack create(){return com.atsuishio.superbwarfare.item.container.ContainerBlockItem.createInstance(AddonEntities.MJ03.get());}
 @net.minecraftforge.eventbus.api.SubscribeEvent public static void register(com.atsuishio.superbwarfare.api.event.RegisterContainersEvent e){e.add(AddonEntities.MJ03.get());e.add(AddonEntities.VATAG.get());e.add(AddonEntities.SR01.get());e.add(AddonEntities.MR01.get());e.add(AddonEntities.MT01.get());e.add(AddonEntities.DC01.get());e.add(AddonEntities.WD01.get());e.add(AddonEntities.NDC01.get());e.add(AddonEntities.SD01.get());e.add(AddonEntities.GAD01.get());e.add(AddonEntities.QD01.get());e.add(AddonEntities.HGD01.get());e.add(AddonEntities.ADV01.get());e.add(AddonEntities.MPL01.get());}
}
