package nl.smartstreamlabs.sbwdroneconfig;
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT)
public final class Mj03ClientControls {
    private static final net.minecraft.client.KeyMapping PANEL=new net.minecraft.client.KeyMapping("key.sbwdroneconfig.mj03_panel",org.lwjgl.glfw.GLFW.GLFW_KEY_J,"key.categories.sbwdroneconfig");
    @net.minecraftforge.eventbus.api.SubscribeEvent public static void tick(net.minecraftforge.event.TickEvent.ClientTickEvent e){
        if(e.phase!=net.minecraftforge.event.TickEvent.Phase.END)return;
        var mc=net.minecraft.client.Minecraft.getInstance();while(PANEL.consumeClick())if(mc.screen==null && mc.player!=null && mc.player.getVehicle() instanceof Mj03Entity t)AddonNetwork.mj03Control(new Mj03ControlMessage(t.getId(),4,""));
    }
    @net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.minecraftforge.api.distmarker.Dist.CLIENT,bus=net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @net.minecraftforge.eventbus.api.SubscribeEvent public static void registerMj03ClientControlsKeys(net.minecraftforge.client.event.RegisterKeyMappingsEvent e){e.register(PANEL);}
    }
}
