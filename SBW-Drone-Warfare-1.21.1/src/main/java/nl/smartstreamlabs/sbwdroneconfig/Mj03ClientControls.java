package nl.smartstreamlabs.sbwdroneconfig;
@net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT)
public final class Mj03ClientControls {
    private static final net.minecraft.client.KeyMapping PANEL=new net.minecraft.client.KeyMapping("key.sbwdroneconfig.mj03_panel",org.lwjgl.glfw.GLFW.GLFW_KEY_J,"key.categories.sbwdroneconfig");
    @net.neoforged.bus.api.SubscribeEvent public static void tick(net.neoforged.neoforge.client.event.ClientTickEvent.Post e){
        
        var mc=net.minecraft.client.Minecraft.getInstance();while(PANEL.consumeClick())if(mc.screen==null && mc.player!=null && mc.player.getVehicle() instanceof Mj03Entity t)AddonNetwork.mj03Control(new Mj03ControlMessage(t.getId(),4,""));
    }
    @net.neoforged.fml.common.EventBusSubscriber(modid=SbwDroneRangeConfig.MOD_ID,value=net.neoforged.api.distmarker.Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        @net.neoforged.bus.api.SubscribeEvent public static void registerMj03ClientControlsKeys(net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent e){e.register(PANEL);}
    }
}
